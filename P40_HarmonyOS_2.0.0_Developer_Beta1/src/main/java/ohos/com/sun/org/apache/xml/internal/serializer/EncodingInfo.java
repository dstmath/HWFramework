package ohos.com.sun.org.apache.xml.internal.serializer;

public final class EncodingInfo {
    final String javaName;
    private InEncoding m_encoding;
    final String name;

    /* access modifiers changed from: private */
    public interface InEncoding {
        boolean isInEncoding(char c);

        boolean isInEncoding(char c, char c2);
    }

    public boolean isInEncoding(char c) {
        if (this.m_encoding == null) {
            this.m_encoding = new EncodingImpl();
        }
        return this.m_encoding.isInEncoding(c);
    }

    public boolean isInEncoding(char c, char c2) {
        if (this.m_encoding == null) {
            this.m_encoding = new EncodingImpl();
        }
        return this.m_encoding.isInEncoding(c, c2);
    }

    public EncodingInfo(String str, String str2) {
        this.name = str;
        this.javaName = str2;
    }

    /* access modifiers changed from: private */
    public class EncodingImpl implements InEncoding {
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

        @Override // ohos.com.sun.org.apache.xml.internal.serializer.EncodingInfo.InEncoding
        public boolean isInEncoding(char c) {
            int codePoint = Encodings.toCodePoint(c);
            int i = this.m_explFirst;
            if (codePoint < i) {
                if (this.m_before == null) {
                    this.m_before = new EncodingImpl(this.m_encoding, this.m_first, i - 1, codePoint);
                }
                return this.m_before.isInEncoding(c);
            }
            int i2 = this.m_explLast;
            if (i2 < codePoint) {
                if (this.m_after == null) {
                    this.m_after = new EncodingImpl(this.m_encoding, i2 + 1, this.m_last, codePoint);
                }
                return this.m_after.isInEncoding(c);
            }
            int i3 = codePoint - i;
            if (this.m_alreadyKnown[i3]) {
                return this.m_isInEncoding[i3];
            }
            boolean inEncoding = EncodingInfo.inEncoding(c, this.m_encoding);
            this.m_alreadyKnown[i3] = true;
            this.m_isInEncoding[i3] = inEncoding;
            return inEncoding;
        }

        @Override // ohos.com.sun.org.apache.xml.internal.serializer.EncodingInfo.InEncoding
        public boolean isInEncoding(char c, char c2) {
            int codePoint = Encodings.toCodePoint(c, c2);
            int i = this.m_explFirst;
            if (codePoint < i) {
                if (this.m_before == null) {
                    this.m_before = new EncodingImpl(this.m_encoding, this.m_first, i - 1, codePoint);
                }
                return this.m_before.isInEncoding(c, c2);
            }
            int i2 = this.m_explLast;
            if (i2 < codePoint) {
                if (this.m_after == null) {
                    this.m_after = new EncodingImpl(this.m_encoding, i2 + 1, this.m_last, codePoint);
                }
                return this.m_after.isInEncoding(c, c2);
            }
            int i3 = codePoint - i;
            if (this.m_alreadyKnown[i3]) {
                return this.m_isInEncoding[i3];
            }
            boolean inEncoding = EncodingInfo.inEncoding(c, c2, this.m_encoding);
            this.m_alreadyKnown[i3] = true;
            this.m_isInEncoding[i3] = inEncoding;
            return inEncoding;
        }

        private EncodingImpl(EncodingInfo encodingInfo) {
            this(encodingInfo.javaName, 0, Integer.MAX_VALUE, 0);
        }

        private EncodingImpl(String str, int i, int i2, int i3) {
            this.m_alreadyKnown = new boolean[128];
            this.m_isInEncoding = new boolean[128];
            this.m_first = i;
            this.m_last = i2;
            this.m_explFirst = (i3 / 128) * 128;
            this.m_explLast = this.m_explFirst + 127;
            this.m_encoding = str;
            if (EncodingInfo.this.javaName != null) {
                int i4 = this.m_explFirst;
                if (i4 >= 0 && i4 <= 127 && ("UTF8".equals(EncodingInfo.this.javaName) || "UTF-16".equals(EncodingInfo.this.javaName) || "ASCII".equals(EncodingInfo.this.javaName) || "US-ASCII".equals(EncodingInfo.this.javaName) || "Unicode".equals(EncodingInfo.this.javaName) || "UNICODE".equals(EncodingInfo.this.javaName) || EncodingInfo.this.javaName.startsWith("ISO8859"))) {
                    for (int i5 = 1; i5 < 127; i5++) {
                        int i6 = i5 - this.m_explFirst;
                        if (i6 >= 0 && i6 < 128) {
                            this.m_alreadyKnown[i6] = true;
                            this.m_isInEncoding[i6] = true;
                        }
                    }
                }
                if (EncodingInfo.this.javaName == null) {
                    int i7 = 0;
                    while (true) {
                        boolean[] zArr = this.m_alreadyKnown;
                        if (i7 < zArr.length) {
                            zArr[i7] = true;
                            this.m_isInEncoding[i7] = true;
                            i7++;
                        } else {
                            return;
                        }
                    }
                }
            }
        }
    }

    /* access modifiers changed from: private */
    public static boolean inEncoding(char c, String str) {
        try {
            return inEncoding(c, new String(new char[]{c}).getBytes(str));
        } catch (Exception unused) {
            return str == null;
        }
    }

    /* access modifiers changed from: private */
    public static boolean inEncoding(char c, char c2, String str) {
        try {
            return inEncoding(c, new String(new char[]{c, c2}).getBytes(str));
        } catch (Exception unused) {
            return false;
        }
    }

    private static boolean inEncoding(char c, byte[] bArr) {
        if (bArr == null || bArr.length == 0 || bArr[0] == 0) {
            return false;
        }
        if (bArr[0] != 63 || c == '?') {
            return true;
        }
        return false;
    }
}
