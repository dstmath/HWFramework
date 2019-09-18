package com.android.org.conscrypt;

final class Preconditions {
    private Preconditions() {
    }

    static <T> T checkNotNull(T reference, String errorMessage) {
        if (reference != null) {
            return reference;
        }
        throw new NullPointerException(errorMessage);
    }

    static void checkArgument(boolean condition, String errorMessage) {
        if (!condition) {
            throw new IllegalArgumentException(errorMessage);
        }
    }

    static void checkArgument(boolean condition, String errorMessageTemplate, Object arg) {
        if (!condition) {
            throw new IllegalArgumentException(String.format(errorMessageTemplate, new Object[]{arg}));
        }
    }

    static void checkPositionIndexes(int start, int end, int size) {
        if (start < 0 || end < start || end > size) {
            throw new IndexOutOfBoundsException(badPositionIndexes(start, end, size));
        }
    }

    private static String badPositionIndexes(int start, int end, int size) {
        if (start < 0 || start > size) {
            return badPositionIndex(start, size, "start index");
        }
        if (end < 0 || end > size) {
            return badPositionIndex(end, size, "end index");
        }
        return String.format("end index (%s) must not be less than start index (%s)", new Object[]{Integer.valueOf(end), Integer.valueOf(start)});
    }

    private static String badPositionIndex(int index, int size, String desc) {
        if (index < 0) {
            return String.format("%s (%s) must not be negative", new Object[]{desc, Integer.valueOf(index)});
        } else if (size >= 0) {
            return String.format("%s (%s) must not be greater than size (%s)", new Object[]{desc, Integer.valueOf(index), Integer.valueOf(size)});
        } else {
            throw new IllegalArgumentException("negative size: " + size);
        }
    }
}
