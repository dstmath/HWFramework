package org.junit.experimental.theories;

public abstract class PotentialAssignment {
    public abstract String getDescription() throws CouldNotGenerateValueException;

    public abstract Object getValue() throws CouldNotGenerateValueException;

    public static class CouldNotGenerateValueException extends Exception {
        private static final long serialVersionUID = 1;

        public CouldNotGenerateValueException() {
        }

        public CouldNotGenerateValueException(Throwable e) {
            super(e);
        }
    }

    public static PotentialAssignment forValue(final String name, final Object value) {
        return new PotentialAssignment() {
            /* class org.junit.experimental.theories.PotentialAssignment.AnonymousClass1 */

            @Override // org.junit.experimental.theories.PotentialAssignment
            public Object getValue() {
                return value;
            }

            public String toString() {
                return String.format("[%s]", value);
            }

            @Override // org.junit.experimental.theories.PotentialAssignment
            public String getDescription() {
                String valueString;
                Object obj = value;
                if (obj == null) {
                    valueString = "null";
                } else {
                    try {
                        valueString = String.format("\"%s\"", obj);
                    } catch (Throwable e) {
                        valueString = String.format("[toString() threw %s: %s]", e.getClass().getSimpleName(), e.getMessage());
                    }
                }
                return String.format("%s <from %s>", valueString, name);
            }
        };
    }
}
