package org.metastatic.sexp4j;

import com.google.common.base.Preconditions;

/**
 * A display hint. Display hints prefix atoms to provide free-from
 * additional information about the atom that follows.
 */
public class DisplayHint {
    private final Atom value;

    /**
     * Create a new display hint.
     *
     * @param value The display hint's value.
     * @throws java.lang.NullPointerException If the argument is null.
     * @throws java.lang.IllegalArgumentException If the argument has a display hint.
     */
    public DisplayHint(Atom value) {
        Preconditions.checkNotNull(value);
        Preconditions.checkArgument(!value.displayHint().isPresent());
        this.value = value;
    }

    /**
     * Create a new display hint, creating an atom from a string.
     *
     * @param value The display hint's value.
     * @throws java.lang.NullPointerException If the argument is null.
     */
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
