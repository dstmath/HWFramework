package org.apache.xml.serializer;

public final class EncodingInfo {
    final String javaName;
    private InEncoding m_encoding;
    private final char m_highCharInContiguousGroup;
    final String name;

    private class EncodingImpl implements InEncoding {
        private static final int RANGE = 128;
        private InEncoding m_after;
        private final boolean[] m_alreadyKnown;
        private InEncoding m_before;
        private final String m_encoding;
        private final int m_explFirst;
        private final int m_explLast;
        private final int m_first;
        private final boolean[] m_isInEncoding;
        private final int m_last;

        public boolean isInEncoding(char ch1) {
            int codePoint = Encodings.toCodePoint(ch1);
            if (codePoint < this.m_explFirst) {
                if (this.m_before == null) {
                    EncodingImpl encodingImpl = new EncodingImpl(this.m_encoding, this.m_first, this.m_explFirst - 1, codePoint);
                    this.m_before = encodingImpl;
                }
                return this.m_before.isInEncoding(ch1);
            } else if (this.m_explLast < codePoint) {
                if (this.m_after == null) {
                    EncodingImpl encodingImpl2 = new EncodingImpl(this.m_encoding, this.m_explLast + 1, this.m_last, codePoint);
                    this.m_after = encodingImpl2;
                }
                return this.m_after.isInEncoding(ch1);
            } else {
                int idx = codePoint - this.m_explFirst;
                if (this.m_alreadyKnown[idx]) {
                    return this.m_isInEncoding[idx];
                }
                boolean ret = EncodingInfo.inEncoding(ch1, this.m_encoding);
                this.m_alreadyKnown[idx] = true;
                this.m_isInEncoding[idx] = ret;
                return ret;
            }
        }

        public boolean isInEncoding(char high, char low) {
            int codePoint = Encodings.toCodePoint(high, low);
            if (codePoint < this.m_explFirst) {
                if (this.m_before == null) {
                    EncodingImpl encodingImpl = new EncodingImpl(this.m_encoding, this.m_first, this.m_explFirst - 1, codePoint);
                    this.m_before = encodingImpl;
                }
                return this.m_before.isInEncoding(high, low);
            } else if (this.m_explLast < codePoint) {
                if (this.m_after == null) {
                    EncodingImpl encodingImpl2 = new EncodingImpl(this.m_encoding, this.m_explLast + 1, this.m_last, codePoint);
                    this.m_after = encodingImpl2;
                }
                return this.m_after.isInEncoding(high, low);
            } else {
                int idx = codePoint - this.m_explFirst;
                if (this.m_alreadyKnown[idx]) {
                    return this.m_isInEncoding[idx];
                }
                boolean ret = EncodingInfo.inEncoding(high, low, this.m_encoding);
                this.m_alreadyKnown[idx] = true;
                this.m_isInEncoding[idx] = ret;
                return ret;
            }
        }

        private EncodingImpl(EncodingInfo encodingInfo) {
            this(encodingInfo.javaName, 0, Integer.MAX_VALUE, 0);
        }

        private EncodingImpl(String encoding, int first, int last, int codePoint) {
            this.m_alreadyKnown = new boolean[128];
            this.m_isInEncoding = new boolean[128];
            this.m_first = first;
            this.m_last = last;
            this.m_explFirst = codePoint;
            this.m_explLast = codePoint + 127;
            this.m_encoding = encoding;
            if (EncodingInfo.this.javaName != null) {
                if (this.m_explFirst >= 0 && this.m_explFirst <= 127 && ("UTF8".equals(EncodingInfo.this.javaName) || "UTF-16".equals(EncodingInfo.this.javaName) || "ASCII".equals(EncodingInfo.this.javaName) || "US-ASCII".equals(EncodingInfo.this.javaName) || "Unicode".equals(EncodingInfo.this.javaName) || "UNICODE".equals(EncodingInfo.this.javaName) || EncodingInfo.this.javaName.startsWith("ISO8859"))) {
                    for (int unicode = 1; unicode < 127; unicode++) {
                        int idx = unicode - this.m_explFirst;
                        if (idx >= 0 && idx < 128) {
                            this.m_alreadyKnown[idx] = true;
                            this.m_isInEncoding[idx] = true;
                        }
                    }
                }
                if (EncodingInfo.this.javaName == null) {
                    for (int idx2 = 0; idx2 < this.m_alreadyKnown.length; idx2++) {
                        this.m_alreadyKnown[idx2] = true;
                        this.m_isInEncoding[idx2] = true;
                    }
                }
            }
        }
    }

    private interface InEncoding {
        boolean isInEncoding(char c);

        boolean isInEncoding(char c, char c2);
    }

    public boolean isInEncoding(char ch) {
        if (this.m_encoding == null) {
            this.m_encoding = new EncodingImpl();
        }
        return this.m_encoding.isInEncoding(ch);
    }

    public boolean isInEncoding(char high, char low) {
        if (this.m_encoding == null) {
            this.m_encoding = new EncodingImpl();
        }
        return this.m_encoding.isInEncoding(high, low);
    }

    public EncodingInfo(String name2, String javaName2, char highChar) {
        this.name = name2;
        this.javaName = javaName2;
        this.m_highCharInContiguousGroup = highChar;
    }

    /* access modifiers changed from: private */
    public static boolean inEncoding(char ch, String encoding) {
        try {
            return inEncoding(ch, new String(new char[]{ch}).getBytes(encoding));
        } catch (Exception e) {
            if (encoding == null) {
                return true;
            }
            return false;
        }
    }

    /* access modifiers changed from: private */
    public static boolean inEncoding(char high, char low, String encoding) {
        boolean isInEncoding = false;
        try {
            isInEncoding = inEncoding(high, new String(new char[]{high, low}).getBytes(encoding));
        } catch (Exception e) {
        }
        return isInEncoding;
    }

    private static boolean inEncoding(char ch, byte[] data) {
        if (data == null || data.length == 0) {
            return false;
        }
        if (data[0] == 0) {
            return false;
        }
        if (data[0] != 63 || ch == '?') {
            return true;
        }
        return false;
    }

    public final char getHighChar() {
        return this.m_highCharInContiguousGroup;
    }
}
