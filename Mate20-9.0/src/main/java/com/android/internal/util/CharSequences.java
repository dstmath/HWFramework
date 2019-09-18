package com.android.internal.util;

public class CharSequences {
    public static CharSequence forAsciiBytes(final byte[] bytes) {
        return new CharSequence() {
            public char charAt(int index) {
                return (char) bytes[index];
            }

            public int length() {
                return bytes.length;
            }

            public CharSequence subSequence(int start, int end) {
                return CharSequences.forAsciiBytes(bytes, start, end);
            }

            public String toString() {
                return new String(bytes);
            }
        };
    }

    public static CharSequence forAsciiBytes(final byte[] bytes, final int start, final int end) {
        validate(start, end, bytes.length);
        return new CharSequence() {
            public char charAt(int index) {
                return (char) bytes[start + index];
            }

            public int length() {
                return end - start;
            }

            public CharSequence subSequence(int newStart, int newEnd) {
                int newStart2 = newStart - start;
                int newEnd2 = newEnd - start;
                CharSequences.validate(newStart2, newEnd2, length());
                return CharSequences.forAsciiBytes(bytes, newStart2, newEnd2);
            }

            public String toString() {
                return new String(bytes, start, length());
            }
        };
    }

    static void validate(int start, int end, int length) {
        if (start < 0) {
            throw new IndexOutOfBoundsException();
        } else if (end < 0) {
            throw new IndexOutOfBoundsException();
        } else if (end > length) {
            throw new IndexOutOfBoundsException();
        } else if (start > end) {
            throw new IndexOutOfBoundsException();
        }
    }

    public static boolean equals(CharSequence a, CharSequence b) {
        if (a.length() != b.length()) {
            return false;
        }
        int length = a.length();
        for (int i = 0; i < length; i++) {
            if (a.charAt(i) != b.charAt(i)) {
                return false;
            }
        }
        return true;
    }

    public static int compareToIgnoreCase(CharSequence me, CharSequence another) {
        int myLen = me.length();
        int anotherLen = another.length();
        int myPos = 0;
        int anotherPos = 0;
        int end = myLen < anotherLen ? myLen : anotherLen;
        while (myPos < end) {
            int myPos2 = myPos + 1;
            int anotherPos2 = anotherPos + 1;
            int lowerCase = Character.toLowerCase(me.charAt(myPos)) - Character.toLowerCase(another.charAt(anotherPos));
            int result = lowerCase;
            if (lowerCase != 0) {
                return result;
            }
            myPos = myPos2;
            anotherPos = anotherPos2;
        }
        return myLen - anotherLen;
    }
}
