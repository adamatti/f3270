package net.sf.f3270

class Parameter {

    private String name;
    private String value;

    Parameter(final String name, final String value) {
        this.name = name
        this.value = "\"" + value + "\""
    }

    Parameter(final String name, final int value) {
        this.name = name
        this.value = "" + value
    }

    Parameter(final String name, final boolean value) {
        this.name = name
        this.value = "" + value
    }

    Parameter(final String name, final MatchMode value) {
        this.name = name
        this.value = "" + value
    }

    String getName() {
        name
    }

    String getValue() {
        value
    }

    @Override
    String toString() {
        name + "=" + value
    }
}
