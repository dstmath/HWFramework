package android.icu.impl;

import java.nio.ByteBuffer;

public final class UResource {

    public interface Array {
        int getSize();

        boolean getValue(int i, Value value);
    }

    public static final class Key implements CharSequence, Cloneable, Comparable<Key> {
        static final /* synthetic */ boolean $assertionsDisabled = false;
        private byte[] bytes;
        private int length;
        private int offset;
        private String s;

        static {
            Class<UResource> cls = UResource.class;
        }

        public Key() {
            this.s = "";
        }

        public Key(String s2) {
            setString(s2);
        }

        private Key(byte[] keyBytes, int keyOffset, int keyLength) {
            this.bytes = keyBytes;
            this.offset = keyOffset;
            this.length = keyLength;
        }

        public Key setBytes(byte[] keyBytes, int keyOffset) {
            this.bytes = keyBytes;
            this.offset = keyOffset;
            int i = 0;
            while (true) {
                this.length = i;
                if (keyBytes[this.length + keyOffset] != 0) {
                    i = this.length + 1;
                } else {
                    this.s = null;
                    return this;
                }
            }
        }

        public Key setToEmpty() {
            this.bytes = null;
            this.length = 0;
            this.offset = 0;
            this.s = "";
            return this;
        }

        public Key setString(String s2) {
            if (s2.isEmpty()) {
                setToEmpty();
            } else {
                this.bytes = new byte[s2.length()];
                int i = 0;
                this.offset = 0;
                this.length = s2.length();
                while (i < this.length) {
                    char c = s2.charAt(i);
                    if (c <= 127) {
                        this.bytes[i] = (byte) c;
                        i++;
                    } else {
                        throw new IllegalArgumentException('\"' + s2 + "\" is not an ASCII string");
                    }
                }
                this.s = s2;
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
            return (char) this.bytes[this.offset + i];
        }

        public int length() {
            return this.length;
        }

        public Key subSequence(int start, int end) {
            return new Key(this.bytes, this.offset + start, end - start);
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
            return internalSubString(start, this.length);
        }

        public String substring(int start, int end) {
            return internalSubString(start, end);
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
                if (this.bytes[this.offset + start + i] != cs.charAt(i)) {
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
            if (this.length == otherKey.length && regionMatches(otherKey.bytes, otherKey.offset, this.length)) {
                z = true;
            }
            return z;
        }

        public boolean contentEquals(CharSequence cs) {
            boolean z = false;
            if (cs == null) {
                return false;
            }
            if (this == cs || (cs.length() == this.length && regionMatches(0, cs, this.length))) {
                z = true;
            }
            return z;
        }

        public boolean startsWith(CharSequence cs) {
            int csLength = cs.length();
            return csLength <= this.length && regionMatches(0, cs, csLength);
        }

        public boolean endsWith(CharSequence cs) {
            int csLength = cs.length();
            return csLength <= this.length && regionMatches(this.length - csLength, cs, csLength);
        }

        public boolean regionMatches(int start, CharSequence cs) {
            int csLength = cs.length();
            return csLength == this.length - start && regionMatches(start, cs, csLength);
        }

        public int hashCode() {
            if (this.length == 0) {
                return 0;
            }
            int h = this.bytes[this.offset];
            for (int i = 1; i < this.length; i++) {
                h = (37 * h) + this.bytes[this.offset];
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

    public static abstract class Sink {
        public abstract void put(Key key, Value value, boolean z);
    }

    public interface Table {
        boolean getKeyAndValue(int i, Key key, Value value);

        int getSize();
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
            int type = getType();
            if (type != 14) {
                switch (type) {
                    case 0:
                        return getString();
                    case 1:
                        return "(binary blob)";
                    case 2:
                        return "(table)";
                    default:
                        switch (type) {
                            case 7:
                                return Integer.toString(getInt());
                            case 8:
                                return "(array)";
                            default:
                                return "???";
                        }
                }
            } else {
                int[] iv = getIntVector();
                StringBuilder sb = new StringBuilder("[");
                sb.append(iv.length);
                sb.append("]{");
                if (iv.length != 0) {
                    sb.append(iv[0]);
                    for (int i = 1; i < iv.length; i++) {
                        sb.append(", ");
                        sb.append(iv[i]);
                    }
                }
                sb.append('}');
                return sb.toString();
            }
        }
    }
}
