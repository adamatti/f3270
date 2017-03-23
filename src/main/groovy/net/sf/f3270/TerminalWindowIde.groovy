package net.sf.f3270

import java.awt.BorderLayout
import java.awt.Color
import java.awt.Component
import java.awt.Container
import java.awt.Dimension

import javax.swing.Box
import javax.swing.BoxLayout
import javax.swing.JFrame
import javax.swing.JLabel
import javax.swing.JPanel
import javax.swing.JScrollPane
import javax.swing.JTable
import javax.swing.JTextField
import javax.swing.JTextPane
import javax.swing.UIManager
import javax.swing.border.EmptyBorder
import javax.swing.border.LineBorder

class TerminalWindowIde {

    private JFrame frame

    static void main(String[] args) {
        new TerminalWindowIde()
    }

    TerminalWindowIde() {
        UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName())
        createFrame("Shell")
    }

    private void createFrame(final String title) {
        JPanel mainPane = new JPanel()
        BoxLayout boxLayout = new BoxLayout(mainPane, BoxLayout.Y_AXIS)
        mainPane.setLayout(boxLayout)
        mainPane.setBorder(new EmptyBorder(5, 5, 5, 5))
        mainPane.add(createLabel("Commands"))
        mainPane.add(createCommands())
        mainPane.add(Box.createRigidArea(new Dimension(0, 10)))
        mainPane.add(createLabel("Commands"))
        mainPane.add(createInput())
        mainPane.add(Box.createRigidArea(new Dimension(0, 10)))
        mainPane.add(createLabel("Output"))
        mainPane.add(createOutput())

        frame = new JFrame(title)
        final Container contentPane = frame.getContentPane()
        contentPane.setBackground(new Color(224, 224, 224))
        contentPane.add(mainPane, BorderLayout.CENTER)
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE)
        frame.setResizable(true)
        frame.pack()
        frame.setVisible(true)
    }

    private static Component createLabel(String caption) {
        JLabel label = new JLabel(caption)
        label.alignmentX = Component.LEFT_ALIGNMENT
        label
    }

    private static Component createCommands() {
        JTable commandsTable = new JTable()
        JScrollPane scroller = new JScrollPane(commandsTable)
        scroller.border = new LineBorder(Color.gray)
        scroller
    }

    private static Component createInput() {
        JTextField inputTextField = new JTextField()
        inputTextField.border = new LineBorder(Color.gray)
        inputTextField
    }

    private static Component createOutput() {
        JTextPane outputTextPane = new JTextPane()
        JScrollPane scroller = new JScrollPane(outputTextPane)
        scroller.border = new LineBorder(Color.gray)
        scroller
    }

    void close() {
        frame.visible = false
    }

}