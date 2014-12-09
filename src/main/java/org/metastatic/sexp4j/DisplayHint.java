package org.metastatic.sexp4j;

import com.google.common.base.Preconditions;

public class DisplayHint {
    private final Atom value;

    public DisplayHint(Atom value) {
        Preconditions.checkNotNull(value);
        this.value = value;
    }

    public DisplayHint(String value) {
        this(Atom.atom(value));
    }

    public Atom atom() {
        return value;
    }

    @Override
    public boolean equals(Object obj) {
        return (obj instanceof DisplayHint) && value.equals(((DisplayHint) obj).value);
    }

    @Override
    public String toString() {
        return String.format("%s { value: %s }", super.toString(), value.stringValue());
    }
}
