package android.icu.impl;

import java.nio.ByteBuffer;

public final class UResource {

    public static abstract class Sink {
        public abstract void put(Key key, Value value, boolean z);
    }

    public interface Array {
        int getSize();

        boolean getValue(int i, Value value);
    }

    public static abstract class Value {
        public abstract String getAliasString();

        public abstract Array getArray();

        public abstract ByteBuffer getBinary();

        public abstract int getInt();

        public abstract int[] getIntVector();

        public abstract String getString();

        public abstract String[] getStringArray();

        public abstract String[] getStringArrayOrStringAsArray();

        public abstract String getStringOrFirstOfArray();

        public abstract Table getTable();

        public abstract int getType();

        public abstract int getUInt();

        public abstract boolean isNoInheritanceMarker();

        protected Value() {
        }

        public String toString() {
            switch (getType()) {
                case 0:
                    return getString();
                case 1:
                    return "(binary blob)";
                case 2:
                    return "(table)";
                case 7:
                    return Integer.toString(getInt());
                case 8:
                    return "(array)";
                case 14:
                    int[] iv = getIntVector();
                    StringBuilder sb = new StringBuilder("[");
                    sb.append(iv.length).append("]{");
                    if (iv.length != 0) {
                        sb.append(iv[0]);
                        for (int i = 1; i < iv.length; i++) {
                            sb.append(", ").append(iv[i]);
                        }
                    }
                    return sb.append('}').toString();
                default:
                    return "???";
            }
        }
    }

    public interface Table {
        boolean getKeyAndValue(int i, Key key, Value value);

        int getSize();
    }

    public static final class Key implements CharSequence, Cloneable, Comparable<Key> {
        static final /* synthetic */ boolean -assertionsDisabled = (Key.class.desiredAssertionStatus() ^ 1);
        private byte[] bytes;
        private int length;
        private int offset;
        private String s;

        public Key() {
            this.s = "";
        }

        public Key(String s) {
            setString(s);
        }

        private Key(byte[] keyBytes, int keyOffset, int keyLength) {
            this.bytes = keyBytes;
            this.offset = keyOffset;
            this.length = keyLength;
        }

        public Key setBytes(byte[] keyBytes, int keyOffset) {
            this.bytes = keyBytes;
            this.offset = keyOffset;
            this.length = 0;
            while (keyBytes[this.length + keyOffset] != (byte) 0) {
                this.length++;
            }
            this.s = null;
            return this;
        }

        public Key setToEmpty() {
            this.bytes = null;
            this.length = 0;
            this.offset = 0;
            this.s = "";
            return this;
        }

        public Key setString(String s) {
            if (s.isEmpty()) {
                setToEmpty();
            } else {
                this.bytes = new byte[s.length()];
                this.offset = 0;
                this.length = s.length();
                int i = 0;
                while (i < this.length) {
                    char c = s.charAt(i);
                    if (c <= 127) {
                        this.bytes[i] = (byte) c;
                        i++;
                    } else {
                        throw new IllegalArgumentException('\"' + s + "\" is not an ASCII string");
                    }
                }
                this.s = s;
            }
            return this;
        }

        public Key clone() {
            try {
                return (Key) super.clone();
            } catch (CloneNotSupportedException e) {
                return null;
            }
        }

        public char charAt(int i) {
            if (-assertionsDisabled || (i >= 0 && i < this.length)) {
                return (char) this.bytes[this.offset + i];
            }
            throw new AssertionError();
        }

        public int length() {
            return this.length;
        }

        public Key subSequence(int start, int end) {
            if (!-assertionsDisabled && (start < 0 || start >= this.length)) {
                throw new AssertionError();
            } else if (-assertionsDisabled || (start <= end && end <= this.length)) {
                return new Key(this.bytes, this.offset + start, end - start);
            } else {
                throw new AssertionError();
            }
        }

        public String toString() {
            if (this.s == null) {
                this.s = internalSubString(0, this.length);
            }
            return this.s;
        }

        private String internalSubString(int start, int end) {
            StringBuilder sb = new StringBuilder(end - start);
            for (int i = start; i < end; i++) {
                sb.append((char) this.bytes[this.offset + i]);
            }
            return sb.toString();
        }

        public String substring(int start) {
            if (-assertionsDisabled || (start >= 0 && start < this.length)) {
                return internalSubString(start, this.length);
            }
            throw new AssertionError();
        }

        public String substring(int start, int end) {
            if (!-assertionsDisabled && (start < 0 || start >= this.length)) {
                throw new AssertionError();
            } else if (-assertionsDisabled || (start <= end && end <= this.length)) {
                return internalSubString(start, end);
            } else {
                throw new AssertionError();
            }
        }

        private boolean regionMatches(byte[] otherBytes, int otherOffset, int n) {
            for (int i = 0; i < n; i++) {
                if (this.bytes[this.offset + i] != otherBytes[otherOffset + i]) {
                    return false;
                }
            }
            return true;
        }

        private boolean regionMatches(int start, CharSequence cs, int n) {
            for (int i = 0; i < n; i++) {
                if (this.bytes[(this.offset + start) + i] != cs.charAt(i)) {
                    return false;
                }
            }
            return true;
        }

        public boolean equals(Object other) {
            boolean z = false;
            if (other == null) {
                return false;
            }
            if (this == other) {
                return true;
            }
            if (!(other instanceof Key)) {
                return false;
            }
            Key otherKey = (Key) other;
            if (this.length == otherKey.length) {
                z = regionMatches(otherKey.bytes, otherKey.offset, this.length);
            }
            return z;
        }

        public boolean contentEquals(CharSequence cs) {
            boolean z = false;
            if (cs == null) {
                return false;
            }
            if (this == cs) {
                z = true;
            } else if (cs.length() == this.length) {
                z = regionMatches(0, cs, this.length);
            }
            return z;
        }

        public boolean startsWith(CharSequence cs) {
            int csLength = cs.length();
            if (csLength <= this.length) {
                return regionMatches(0, cs, csLength);
            }
            return false;
        }

        public boolean endsWith(CharSequence cs) {
            int csLength = cs.length();
            return csLength <= this.length ? regionMatches(this.length - csLength, cs, csLength) : false;
        }

        public boolean regionMatches(int start, CharSequence cs) {
            int csLength = cs.length();
            return csLength == this.length - start ? regionMatches(start, cs, csLength) : false;
        }

        public int hashCode() {
            if (this.length == 0) {
                return 0;
            }
            int h = this.bytes[this.offset];
            for (int i = 1; i < this.length; i++) {
                h = (h * 37) + this.bytes[this.offset];
            }
            return h;
        }

        public int compareTo(Key other) {
            return compareTo((CharSequence) other);
        }

        public int compareTo(CharSequence cs) {
            int csLength = cs.length();
            int minLength = this.length <= csLength ? this.length : csLength;
            for (int i = 0; i < minLength; i++) {
                int diff = charAt(i) - cs.charAt(i);
                if (diff != 0) {
                    return diff;
                }
            }
            return this.length - csLength;
        }
    }
}
