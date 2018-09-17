package org.junit.experimental.theories;

public abstract class PotentialAssignment {

    public static class CouldNotGenerateValueException extends Exception {
        private static final long serialVersionUID = 1;

        public CouldNotGenerateValueException(Throwable e) {
            super(e);
        }
    }

    public abstract String getDescription() throws CouldNotGenerateValueException;

    public abstract Object getValue() throws CouldNotGenerateValueException;

    public static PotentialAssignment forValue(final String name, final Object value) {
        return new PotentialAssignment() {
            public Object getValue() {
                return value;
            }

            public String toString() {
                return String.format("[%s]", new Object[]{value});
            }

            public String getDescription() {
                String valueString;
                if (value == null) {
                    valueString = "null";
                } else {
                    try {
                        valueString = String.format("\"%s\"", new Object[]{value});
                    } catch (Throwable e) {
                        valueString = String.format("[toString() threw %s: %s]", new Object[]{e.getClass().getSimpleName(), e.getMessage()});
                    }
                }
                return String.format("%s <from %s>", new Object[]{valueString, name});
            }
        };
    }
}
