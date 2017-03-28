package net.sf.f3270

import java.awt.BorderLayout
import java.awt.Color
import java.awt.Container
import java.awt.Dimension
import java.awt.Font
import java.awt.FontMetrics
import java.awt.Rectangle
import javax.swing.BoxLayout
import javax.swing.JDialog
import javax.swing.JFrame
import javax.swing.JPanel
import javax.swing.JScrollPane
import javax.swing.JTabbedPane
import javax.swing.JTable
import javax.swing.JTextPane
import javax.swing.SwingUtilities
import javax.swing.UIManager
import javax.swing.border.EmptyBorder
import javax.swing.border.LineBorder
import javax.swing.table.AbstractTableModel
import javax.swing.text.BadLocationException
import javax.swing.text.DefaultStyledDocument
import javax.swing.text.Style
import javax.swing.text.StyleConstants
import javax.swing.text.StyleContext

import org.h3270.host.Field
import org.h3270.host.InputField
import org.h3270.host.S3270

class TerminalWindow {
	
	private S3270 s3270
	private int currentWidth
	private int currentHeight

	private Style styleInputChanged
	private Style styleInput
	private Style styleBlack

	private Style styleCommand
	private Style stylePunctuation
	private Style styleReturn
	private Style styleParamName
	private Style styleParamValue

	private final Font monospacedFont = new Font(Font.MONOSPACED, Font.PLAIN, 12)
	private final Font sansFont = new Font(Font.SANS_SERIF, Font.PLAIN, 11)

	private Color[] extendedColors = [
		Color.cyan, Color.blue, Color.red, Color.pink, Color.green,
		Color.magenta, Color.yellow, new Color(198, 198, 198)
	]

	private Map<String, Style> stylesFlyweight = new HashMap<String, Style>()

	private JFrame frame
	private JTextPane textPane3270
	private JTextPane textPaneDebug
	private DefaultStyledDocument documentDebug

	private JTable fieldsTable
	private JTabbedPane tabbedPane

	TerminalWindow(final S3270 s3270) {
		this.s3270 = s3270

		UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName())

		initializeStyles()
		createFrame(s3270.hostname)
	}

	private void initializeStyles() {
		styleInputChanged = createStyle(Color.black, Color.red, false)
		styleInput = createStyle(Color.green, Color.black, false)
		styleCommand = createStyle(Color.black, Color.white, false)
		stylePunctuation = createStyle(Color.gray, Color.white, false)
		styleReturn = createStyle(Color.magenta, Color.white, false)
		styleParamName = createStyle(new Color(128, 0, 0), Color.white, false)
		styleParamValue = createStyle(Color.blue, Color.white, false)
	}

	void update(final String command, final String returned, final Parameter... parameters) {
		updateTerminal()
		updateDebug(command, returned, parameters)
		updateFieldsTable()
	}

	private void updateTerminal() {
		final DefaultStyledDocument doc = new DefaultStyledDocument()
		for (Field f : s3270.screen.fields) {
			final Style s = getStyle(f)
			final String text = f.text.replace('\u0000', ' ')
			if ((f instanceof InputField) && text.startsWith(" ")) {
				appendText(doc, " ", styleBlack)
				appendText(doc, text.substring(1), s)
			} else {
				appendText(doc, text, s)
			}
		}
		SwingUtilities.invokeLater(new Runnable() {
			void run() {
				try {
					boolean sizeChanged = updateTextPane3270Size()
					if (sizeChanged) {
						updateTabbedPaneSize()
						frame.pack()
					}
					textPane3270.document = doc
				} catch (RuntimeException e) {
					// do nothing
				}
			}
		})
	}

	private void updateDebug(final String command, final String returned, final Parameter... parameters) {
		if (documentDebug.getLength() > 0) {
			appendText(documentDebug, "\n", stylePunctuation)
		}
		appendText(documentDebug, command, styleCommand)
		appendText(documentDebug, "(", stylePunctuation)
		for (int i = 0; i < parameters.length; i++) {
			appendText(documentDebug, parameters[i].name, styleParamName)
			appendText(documentDebug, "=", stylePunctuation)
			appendText(documentDebug, parameters[i].value, styleParamValue)
			if (i != parameters.length - 1) {
				appendText(documentDebug, ", ", stylePunctuation)
			}
		}
		appendText(documentDebug, ")", stylePunctuation)
		if (returned != null) {
			appendText(documentDebug, " = ", stylePunctuation)
			appendText(documentDebug, "\"" + returned + "\"", styleReturn)
		}
		SwingUtilities.invokeLater(new Runnable() {
			void run() {
				textPaneDebug.scrollRectToVisible(new Rectangle(0,
						textPaneDebug.height * 2, 1, 1))
			}
		})
	}

	private void updateFieldsTable() {
		AbstractTableModel table = fieldsTable.model
		table.fireTableDataChanged()
	}

	private Style getStyle(final Field f) {
		final boolean isInput = f instanceof InputField
		if (isInput) {
			final InputField inputField = f
			if (inputField.isChanged()) {
				return styleInputChanged
			} else {
				return styleInput
			}
		}

		final int i = (f.extendedColor == 0) ? 0 : f.extendedColor - 0xf0
		Color foregroundColor = extendedColors[i]
		Color backgroundColor = Color.black
		if (f.extendedHighlight == Field.ATTR_EH_REV_VIDEO) {
			final Color tmp = backgroundColor
			backgroundColor = foregroundColor
			foregroundColor = tmp
		}
		boolean isUnderline = f.extendedHighlight == Field.ATTR_EH_UNDERSCORE

		if (f.isIntensified()) {
			foregroundColor = Color.white
		}

		if (f.isHidden()) {
			foregroundColor = Color.black
			backgroundColor = Color.black
			isUnderline = false
		}

		createStyle(foregroundColor, backgroundColor, isUnderline)
	}

	private void appendText(final DefaultStyledDocument doc, final String text, final Style style) {
		try {
			doc.insertString(doc.length, text, style)
		} catch (final BadLocationException e) {
			throw new RuntimeException(e)
		}
	}

	private void createFrame(final String title) {
		buildTextPane3270()
		final JScrollPane tableScroller = buildFieldsTablePanel()

		tabbedPane = new JTabbedPane()
		tabbedPane.addTab("Terminal", null, textPane3270, "")
		tabbedPane.addTab("Fields", null, tableScroller, "")
		updateTabbedPaneSize()

		final JPanel debugPanel = buildDebugPanel(monospacedFont, sansFont)

		frame = new JFrame(title)

		final Container contentPane = frame.contentPane
		contentPane.background = new Color(224, 224, 224)
		contentPane.add(tabbedPane, BorderLayout.NORTH)
		contentPane.add(debugPanel, BorderLayout.CENTER)

		frame.defaultCloseOperation = JFrame.EXIT_ON_CLOSE
		frame.resizable = false
		frame.pack()
		frame.visible = true
	}

	private void updateTabbedPaneSize() {
		tabbedPane.setPreferredSize(
			new Dimension(
				(int) textPane3270.preferredSize.width + 40,
				(int) textPane3270.preferredSize.height + 40
			)
		)
	}

	private JPanel buildDebugPanel(final Font monospacedFont, final Font sansFont) {
		final JScrollPane textPaneDebugScroller = buildTextPaneDebug()

		documentDebug = new DefaultStyledDocument()
		textPaneDebug.document = documentDebug

		final JPanel debugPanel = new JPanel()
		final BoxLayout boxLayout = new BoxLayout(debugPanel, BoxLayout.PAGE_AXIS)
		debugPanel.layout = boxLayout
		debugPanel.background = new Color(224, 224, 224)
		debugPanel.border = new EmptyBorder(3, 0, 0, 0)
		debugPanel.add(textPaneDebugScroller)
		debugPanel
	}

	private JScrollPane buildTextPaneDebug() {
		textPaneDebug = createTextPane(sansFont, Color.white)
		textPaneDebug.autoscrolls = true
		textPaneDebug.border = new EmptyBorder(3, 3, 3, 3)
		final FontMetrics fontMetricsSans = textPane3270.getFontMetrics(monospacedFont)
		final JScrollPane textPaneDebugScroller = new JScrollPane(textPaneDebug)
		textPaneDebugScroller.preferredSize = new Dimension(textPane3270.width, 3 + 10 * fontMetricsSans.height)
		textPaneDebugScroller.alignmentX = JDialog.LEFT_ALIGNMENT
		textPaneDebugScroller.border = new LineBorder(Color.gray)
		textPaneDebugScroller.autoscrolls = true
		textPaneDebugScroller
	}

	private void buildTextPane3270() {
		textPane3270 = createTextPane(monospacedFont, Color.black)
		updateTextPane3270Size()
		textPane3270.alignmentX = JDialog.LEFT_ALIGNMENT
	}

	private boolean updateTextPane3270Size() {
		final FontMetrics fontMetricsMonospaced = textPane3270.getFontMetrics(monospacedFont)
		int w = s3270.screen.width
		int h = s3270.screen.height
		if (w != currentWidth || h != currentHeight) {
			textPane3270.setPreferredSize(
				new Dimension(
					(w + 2) * fontMetricsMonospaced.charWidth(' ' as char) as int,
					(h + 2) * fontMetricsMonospaced.height as int
				)
			)
			currentWidth = w
			currentHeight = h
			return true
		}
		false
	}

	private JScrollPane buildFieldsTablePanel() {
		fieldsTable = new JTable(new AbstractTableModel() {
			private String[] columnNames = [ "Id", "Type", "Value" ]

			int getColumnCount() {
				3
			}

			int getRowCount() {
				try {
					return s3270.screen.fields.size()
				} catch (Exception e) {
					return 0
				}
			}

			@Override
			String getColumnName(final int column) {
				columnNames[column]
			}

			Object getValueAt(final int rowIndex, final int columnIndex) {
				if (columnIndex == 0) {
					return rowIndex
				}
				Field f
				try {
					f = s3270.screen.fields.get(rowIndex)
				} catch (Exception e) {
					// nasty hack to handle some random not connected exceptions from s3270
					return ""
				}
				if (columnIndex == 1) {
					boolean isInputField = f instanceof InputField
					return (isInputField ? "in" : "out")
							+ ((isInputField && f.isChanged()) ? " *" : "")
				}
				if (columnIndex == 2) {
					return "[" + f.getValue().replace('\u0000', ' ') + "]"
				}
				throw new RuntimeException("unknown column index "
						+ columnIndex)
			}

			boolean isCellEditable(final int rowIndex, final int columnIndex) {
				columnIndex == 2
			}
		})

		fieldsTable.columnModel.getColumn(0).setPreferredWidth(25)
		fieldsTable.columnModel.getColumn(1).setPreferredWidth(35)
		fieldsTable.columnModel.getColumn(2).setPreferredWidth(600)
		// fieldsTable.setAutoCreateRowSorter(true)

		final JScrollPane tableScroller = new JScrollPane(fieldsTable)
		tableScroller
	}

	private static JTextPane createTextPane(final Font font, final Color color) {
		final JTextPane textPane = new JTextPane()
		textPane.font = font
		textPane.background = color
		textPane.editable = false
		textPane
	}

	private Style createStyle(final Color foregroundColor, final Color backgrondColor, final boolean isItalic) {
		final String key = String.format("%d-%d-%d %d-%d-%d %s",
				foregroundColor.getRed(), foregroundColor.getGreen(),
				foregroundColor.getBlue(), backgrondColor.getRed(),
				backgrondColor.getGreen(), backgrondColor.getBlue(), isItalic)

		Style style = stylesFlyweight.get(key)
		if (style == null) {
			style = StyleContext.defaultStyleContext.addStyle(null, null)
			StyleConstants.setForeground(style, foregroundColor)
			StyleConstants.setBackground(style, backgrondColor)
			StyleConstants.setItalic(style, isItalic)
			stylesFlyweight.put(key, style)
		}

		style
	}

	void close() {
		frame.visible = false
	}
}