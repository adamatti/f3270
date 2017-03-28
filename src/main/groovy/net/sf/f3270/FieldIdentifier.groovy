package net.sf.f3270

import groovy.util.logging.Slf4j
import org.h3270.host.Field

@Slf4j
class FieldIdentifier {

    private static final MatchMode DEFAULT_MATCH_MODE = MatchMode.CONTAINS

    final String label
    final int skip
    final int matchNumber
    final MatchMode matchMode
    final Integer fieldId

    FieldIdentifier(Map args = [:]){
        label = args.label ?: ""
        skip = (args.skip != null) ? args.skip : 1
        matchNumber = args.matchNumber ?: 1
        matchMode = args.matchMode ?: DEFAULT_MATCH_MODE
        fieldId = args.fieldId
    }

    FieldIdentifier(String label) {
        this(label, 1, 1, DEFAULT_MATCH_MODE)
    }

    FieldIdentifier(String label, int skip) {
        this(label, skip, 1, DEFAULT_MATCH_MODE)
    }

    FieldIdentifier(String label, MatchMode matchMode) {
        this(label, 1, matchMode)
    }

    FieldIdentifier(String label, int skip, int matchNumber) {
        this(label, skip, matchNumber, DEFAULT_MATCH_MODE)
    }

    FieldIdentifier(String label, int skip, MatchMode matchMode) {
        this(label, skip, 1, matchMode)
    }

    FieldIdentifier(String label, int skip, int matchNumber, MatchMode matchMode) {
        this.label = label
        this.skip = skip
        this.matchNumber = matchNumber
        this.matchMode = matchMode
        fieldId = null
    }

    Collection<Parameter> buildParameters() {
        Collection<Parameter> parameters = new ArrayList<Parameter>()

        if (label) {
            parameters.add(new Parameter("label", label))
        }

        if (fieldId){
            parameters.add(new Parameter("field_id", fieldId))
        }

        if (skip != 1) {
            parameters.add(new Parameter("skip", skip))
        }
        if (matchNumber != 1) {
            parameters.add(new Parameter("matchNumber", matchNumber))
        }
        if (matchMode != DEFAULT_MATCH_MODE) {
            parameters.add(new Parameter("matchMode", matchMode))
        }
        parameters
    }

    Field find(List<Field> fields) {
        int indexOfLabel = getFieldIndexOfLabel(fields)
        if (indexOfLabel == -1) {
            throw new RuntimeException(String.format("field [%s] could not be found using match mode [%s]", label, matchMode))
        }
        final int indexOfField = indexOfLabel + skip
        if (indexOfField >= fields.size()) {
            throw new RuntimeException(String.format("field [%s] at index [%i] plus skip [%i] exceed the number of available fields in the screen [%i]", label, indexOfLabel, skip, indexOfField))
        }
        fields.get(indexOfField)

    }

    int getFieldIndexOfLabel(List<Field> fields) {
        if (fieldId){
            return fieldId
        }

        int matches_ = 0
        for (int i = 0; i < fields.size(); i++) {
            String value = fields.get(i).value.toLowerCase()
            boolean didMatch = matches(label.toLowerCase(), value)

            //log.debug "match? [value: ${value}, label: ${label}, result: ${didMatch}]"

            if (didMatch) {
                matches_++
                if (matches_ == matchNumber) {
                    return i
                }
            }
        }
        -1
    }

    private boolean matches(String expected, String actual) {
        matchExact(expected, actual) ||
        matchExactAfterTrim(expected, actual) ||
        matchRegex(expected, actual) ||
        matchContains(expected, actual)
    }

    private boolean matchExact(String expected, String actual) {
        matchMode == MatchMode.EXACT && actual == expected
    }

    private boolean matchExactAfterTrim(String expected, String actual) {
        matchMode == MatchMode.EXACT_AFTER_TRIM && actual.trim() == expected
    }

    private boolean matchRegex(String expected, String actual) {
        matchMode == MatchMode.REGEX && actual.matches(expected)
    }

    private boolean matchContains(String expected, String actual) {
        matchMode == MatchMode.CONTAINS && actual.contains(expected)
    }
}
