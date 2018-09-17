package com.android.internal.util;

public class CharSequences {

    /* renamed from: com.android.internal.util.CharSequences.1 */
    static class AnonymousClass1 implements CharSequence {
        final /* synthetic */ byte[] val$bytes;

        AnonymousClass1(byte[] val$bytes) {
            this.val$bytes = val$bytes;
        }

        public char charAt(int index) {
            return (char) this.val$bytes[index];
        }

        public int length() {
            return this.val$bytes.length;
        }

        public CharSequence subSequence(int start, int end) {
            return CharSequences.forAsciiBytes(this.val$bytes, start, end);
        }

        public String toString() {
            return new String(this.val$bytes);
        }
    }

    /* renamed from: com.android.internal.util.CharSequences.2 */
    static class AnonymousClass2 implements CharSequence {
        final /* synthetic */ byte[] val$bytes;
        final /* synthetic */ int val$end;
        final /* synthetic */ int val$start;

        AnonymousClass2(byte[] val$bytes, int val$start, int val$end) {
            this.val$bytes = val$bytes;
            this.val$start = val$start;
            this.val$end = val$end;
        }

        public char charAt(int index) {
            return (char) this.val$bytes[this.val$start + index];
        }

        public int length() {
            return this.val$end - this.val$start;
        }

        public CharSequence subSequence(int newStart, int newEnd) {
            newStart -= this.val$start;
            newEnd -= this.val$start;
            CharSequences.validate(newStart, newEnd, length());
            return CharSequences.forAsciiBytes(this.val$bytes, newStart, newEnd);
        }

        public String toString() {
            return new String(this.val$bytes, this.val$start, length());
        }
    }

    public static CharSequence forAsciiBytes(byte[] bytes) {
        return new AnonymousClass1(bytes);
    }

    public static CharSequence forAsciiBytes(byte[] bytes, int start, int end) {
        validate(start, end, bytes.length);
        return new AnonymousClass2(bytes, start, end);
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
        int end;
        int anotherPos;
        int myPos;
        int myLen = me.length();
        int anotherLen = another.length();
        if (myLen < anotherLen) {
            end = myLen;
            anotherPos = 0;
            myPos = 0;
        } else {
            end = anotherLen;
            anotherPos = 0;
            myPos = 0;
        }
        while (myPos < end) {
            int myPos2 = myPos + 1;
            int anotherPos2 = anotherPos + 1;
            int result = Character.toLowerCase(me.charAt(myPos)) - Character.toLowerCase(another.charAt(anotherPos));
            if (result != 0) {
                return result;
            }
            anotherPos = anotherPos2;
            myPos = myPos2;
        }
        return myLen - anotherLen;
    }
}
