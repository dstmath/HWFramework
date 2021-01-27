package android.util;

import android.annotation.UnsupportedAppUsage;
import java.io.UnsupportedEncodingException;

public class Base64 {
    static final /* synthetic */ boolean $assertionsDisabled = false;
    public static final int CRLF = 4;
    public static final int DEFAULT = 0;
    public static final int NO_CLOSE = 16;
    public static final int NO_PADDING = 1;
    public static final int NO_WRAP = 2;
    public static final int URL_SAFE = 8;

    static abstract class Coder {
        public int op;
        public byte[] output;

        public abstract int maxOutputSize(int i);

        public abstract boolean process(byte[] bArr, int i, int i2, boolean z);

        Coder() {
        }
    }

    public static byte[] decode(String str, int flags) {
        return decode(str.getBytes(), flags);
    }

    public static byte[] decode(byte[] input, int flags) {
        return decode(input, 0, input.length, flags);
    }

    public static byte[] decode(byte[] input, int offset, int len, int flags) {
        Decoder decoder = new Decoder(flags, new byte[((len * 3) / 4)]);
        if (!decoder.process(input, offset, len, true)) {
            throw new IllegalArgumentException("bad base-64");
        } else if (decoder.op == decoder.output.length) {
            return decoder.output;
        } else {
            byte[] temp = new byte[decoder.op];
            System.arraycopy(decoder.output, 0, temp, 0, decoder.op);
            return temp;
        }
    }

    /* access modifiers changed from: package-private */
    public static class Decoder extends Coder {
        private static final int[] DECODE = {-1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, 62, -1, -1, -1, 63, 52, 53, 54, 55, 56, 57, 58, 59, 60, 61, -1, -1, -1, -2, -1, -1, -1, 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, -1, -1, -1, -1, -1, -1, 26, 27, 28, 29, 30, 31, 32, 33, 34, 35, 36, 37, 38, 39, 40, 41, 42, 43, 44, 45, 46, 47, 48, 49, 50, 51, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1};
        private static final int[] DECODE_WEBSAFE = {-1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, 62, -1, -1, 52, 53, 54, 55, 56, 57, 58, 59, 60, 61, -1, -1, -1, -2, -1, -1, -1, 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, -1, -1, -1, -1, 63, -1, 26, 27, 28, 29, 30, 31, 32, 33, 34, 35, 36, 37, 38, 39, 40, 41, 42, 43, 44, 45, 46, 47, 48, 49, 50, 51, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1};
        private static final int EQUALS = -2;
        private static final int SKIP = -1;
        private final int[] alphabet;
        private int state;
        private int value;

        public Decoder(int flags, byte[] output) {
            this.output = output;
            this.alphabet = (flags & 8) == 0 ? DECODE : DECODE_WEBSAFE;
            this.state = 0;
            this.value = 0;
        }

        @Override // android.util.Base64.Coder
        public int maxOutputSize(int len) {
            return ((len * 3) / 4) + 10;
        }

        @Override // android.util.Base64.Coder
        public boolean process(byte[] input, int offset, int len, boolean finish) {
            if (this.state == 6) {
                return false;
            }
            int p = offset;
            int len2 = len + offset;
            int state2 = this.state;
            int value2 = this.value;
            int op = 0;
            byte[] output = this.output;
            int[] alphabet2 = this.alphabet;
            while (p < len2) {
                if (state2 == 0) {
                    while (p + 4 <= len2) {
                        int i = (alphabet2[input[p] & 255] << 18) | (alphabet2[input[p + 1] & 255] << 12) | (alphabet2[input[p + 2] & 255] << 6) | alphabet2[input[p + 3] & 255];
                        value2 = i;
                        if (i < 0) {
                            break;
                        }
                        output[op + 2] = (byte) value2;
                        output[op + 1] = (byte) (value2 >> 8);
                        output[op] = (byte) (value2 >> 16);
                        op += 3;
                        p += 4;
                    }
                    if (p >= len2) {
                        break;
                    }
                }
                int p2 = p + 1;
                int d = alphabet2[input[p] & 255];
                if (state2 != 0) {
                    if (state2 != 1) {
                        if (state2 != 2) {
                            if (state2 != 3) {
                                if (state2 != 4) {
                                    if (state2 == 5 && d != -1) {
                                        this.state = 6;
                                        return false;
                                    }
                                } else if (d == -2) {
                                    state2++;
                                } else if (d != -1) {
                                    this.state = 6;
                                    return false;
                                }
                            } else if (d >= 0) {
                                value2 = (value2 << 6) | d;
                                output[op + 2] = (byte) value2;
                                output[op + 1] = (byte) (value2 >> 8);
                                output[op] = (byte) (value2 >> 16);
                                op += 3;
                                state2 = 0;
                            } else if (d == -2) {
                                output[op + 1] = (byte) (value2 >> 2);
                                output[op] = (byte) (value2 >> 10);
                                op += 2;
                                state2 = 5;
                            } else if (d != -1) {
                                this.state = 6;
                                return false;
                            }
                        } else if (d >= 0) {
                            value2 = (value2 << 6) | d;
                            state2++;
                        } else if (d == -2) {
                            output[op] = (byte) (value2 >> 4);
                            state2 = 4;
                            op++;
                        } else if (d != -1) {
                            this.state = 6;
                            return false;
                        }
                    } else if (d >= 0) {
                        value2 = (value2 << 6) | d;
                        state2++;
                    } else if (d != -1) {
                        this.state = 6;
                        return false;
                    }
                } else if (d >= 0) {
                    value2 = d;
                    state2++;
                } else if (d != -1) {
                    this.state = 6;
                    return false;
                }
                p = p2;
            }
            if (!finish) {
                this.state = state2;
                this.value = value2;
                this.op = op;
                return true;
            }
            if (state2 != 0) {
                if (state2 == 1) {
                    this.state = 6;
                    return false;
                } else if (state2 == 2) {
                    output[op] = (byte) (value2 >> 4);
                    op++;
                } else if (state2 == 3) {
                    int op2 = op + 1;
                    output[op] = (byte) (value2 >> 10);
                    op = op2 + 1;
                    output[op2] = (byte) (value2 >> 2);
                } else if (state2 == 4) {
                    this.state = 6;
                    return false;
                }
            }
            this.state = state2;
            this.op = op;
            return true;
        }
    }

    public static String encodeToString(byte[] input, int flags) {
        try {
            return new String(encode(input, flags), "US-ASCII");
        } catch (UnsupportedEncodingException e) {
            throw new AssertionError(e);
        }
    }

    public static String encodeToString(byte[] input, int offset, int len, int flags) {
        try {
            return new String(encode(input, offset, len, flags), "US-ASCII");
        } catch (UnsupportedEncodingException e) {
            throw new AssertionError(e);
        }
    }

    public static byte[] encode(byte[] input, int flags) {
        return encode(input, 0, input.length, flags);
    }

    public static byte[] encode(byte[] input, int offset, int len, int flags) {
        Encoder encoder = new Encoder(flags, null);
        int output_len = (len / 3) * 4;
        int i = 2;
        if (!encoder.do_padding) {
            int i2 = len % 3;
            if (i2 != 0) {
                if (i2 == 1) {
                    output_len += 2;
                } else if (i2 == 2) {
                    output_len += 3;
                }
            }
        } else if (len % 3 > 0) {
            output_len += 4;
        }
        if (encoder.do_newline && len > 0) {
            int i3 = ((len - 1) / 57) + 1;
            if (!encoder.do_cr) {
                i = 1;
            }
            output_len += i3 * i;
        }
        encoder.output = new byte[output_len];
        encoder.process(input, offset, len, true);
        return encoder.output;
    }

    /* access modifiers changed from: package-private */
    public static class Encoder extends Coder {
        static final /* synthetic */ boolean $assertionsDisabled = false;
        private static final byte[] ENCODE = {65, 66, 67, 68, 69, 70, 71, 72, 73, 74, 75, 76, 77, 78, 79, 80, 81, 82, 83, 84, 85, 86, 87, 88, 89, 90, 97, 98, 99, 100, 101, 102, 103, 104, 105, 106, 107, 108, 109, 110, 111, 112, 113, 114, 115, 116, 117, 118, 119, 120, 121, 122, 48, 49, 50, 51, 52, 53, 54, 55, 56, 57, 43, 47};
        private static final byte[] ENCODE_WEBSAFE = {65, 66, 67, 68, 69, 70, 71, 72, 73, 74, 75, 76, 77, 78, 79, 80, 81, 82, 83, 84, 85, 86, 87, 88, 89, 90, 97, 98, 99, 100, 101, 102, 103, 104, 105, 106, 107, 108, 109, 110, 111, 112, 113, 114, 115, 116, 117, 118, 119, 120, 121, 122, 48, 49, 50, 51, 52, 53, 54, 55, 56, 57, 45, 95};
        public static final int LINE_GROUPS = 19;
        private final byte[] alphabet;
        private int count;
        public final boolean do_cr;
        public final boolean do_newline;
        public final boolean do_padding;
        private final byte[] tail;
        int tailLen;

        public Encoder(int flags, byte[] output) {
            this.output = output;
            boolean z = true;
            this.do_padding = (flags & 1) == 0;
            this.do_newline = (flags & 2) == 0;
            this.do_cr = (flags & 4) == 0 ? false : z;
            this.alphabet = (flags & 8) == 0 ? ENCODE : ENCODE_WEBSAFE;
            this.tail = new byte[2];
            this.tailLen = 0;
            this.count = this.do_newline ? 19 : -1;
        }

        @Override // android.util.Base64.Coder
        public int maxOutputSize(int len) {
            return ((len * 8) / 5) + 10;
        }

        /* JADX WARNING: Removed duplicated region for block: B:14:0x0062  */
        /* JADX WARNING: Removed duplicated region for block: B:22:0x00a0  */
        /* JADX WARNING: Removed duplicated region for block: B:29:0x00f6  */
        /* JADX WARNING: Removed duplicated region for block: B:75:0x01df  */
        @Override // android.util.Base64.Coder
        public boolean process(byte[] input, int offset, int len, boolean finish) {
            int p;
            byte b;
            int p2;
            int t;
            int p3;
            byte b2;
            int t2;
            byte b3;
            int p4;
            byte[] alphabet2 = this.alphabet;
            byte[] output = this.output;
            int op = 0;
            int count2 = this.count;
            int len2 = len + offset;
            int v = -1;
            int i = this.tailLen;
            if (i != 0) {
                if (i != 1) {
                    if (i == 2 && offset + 1 <= len2) {
                        byte[] bArr = this.tail;
                        p = offset + 1;
                        v = ((bArr[1] & 255) << 8) | ((bArr[0] & 255) << 16) | (input[offset] & 255);
                        this.tailLen = 0;
                        if (v != -1) {
                            int op2 = 0 + 1;
                            output[0] = alphabet2[(v >> 18) & 63];
                            int op3 = op2 + 1;
                            output[op2] = alphabet2[(v >> 12) & 63];
                            int op4 = op3 + 1;
                            output[op3] = alphabet2[(v >> 6) & 63];
                            op = op4 + 1;
                            output[op4] = alphabet2[v & 63];
                            count2--;
                            if (count2 == 0) {
                                if (this.do_cr) {
                                    output[op] = 13;
                                    op++;
                                }
                                output[op] = 10;
                                count2 = 19;
                                op++;
                            }
                        }
                        while (p + 3 <= len2) {
                            int v2 = ((input[p] & 255) << 16) | ((input[p + 1] & 255) << 8) | (input[p + 2] & 255);
                            output[op] = alphabet2[(v2 >> 18) & 63];
                            output[op + 1] = alphabet2[(v2 >> 12) & 63];
                            output[op + 2] = alphabet2[(v2 >> 6) & 63];
                            output[op + 3] = alphabet2[v2 & 63];
                            p += 3;
                            op += 4;
                            count2--;
                            if (count2 == 0) {
                                if (this.do_cr) {
                                    output[op] = 13;
                                    op++;
                                }
                                output[op] = 10;
                                count2 = 19;
                                op++;
                            }
                        }
                        if (finish) {
                            int p5 = this.tailLen;
                            if (p - p5 == len2 - 1) {
                                if (p5 > 0) {
                                    t2 = 0 + 1;
                                    b3 = this.tail[0];
                                    p4 = p;
                                } else {
                                    p4 = p + 1;
                                    b3 = input[p];
                                    t2 = 0;
                                }
                                int v3 = (b3 & 255) << 4;
                                this.tailLen -= t2;
                                int op5 = op + 1;
                                output[op] = alphabet2[(v3 >> 6) & 63];
                                op = op5 + 1;
                                output[op5] = alphabet2[v3 & 63];
                                if (this.do_padding) {
                                    int op6 = op + 1;
                                    output[op] = 61;
                                    op = op6 + 1;
                                    output[op6] = 61;
                                }
                                if (this.do_newline) {
                                    if (this.do_cr) {
                                        output[op] = 13;
                                        op++;
                                    }
                                    output[op] = 10;
                                    op++;
                                }
                            } else if (p - p5 == len2 - 2) {
                                int t3 = 0;
                                if (p5 > 1) {
                                    t3 = 0 + 1;
                                    b = this.tail[0];
                                    p2 = p;
                                } else {
                                    p2 = p + 1;
                                    b = input[p];
                                }
                                int i2 = (b & 255) << 10;
                                if (this.tailLen > 0) {
                                    t = t3 + 1;
                                    b2 = this.tail[t3];
                                    p3 = p2;
                                } else {
                                    p3 = p2 + 1;
                                    t = t3;
                                    b2 = input[p2];
                                }
                                int v4 = ((b2 & 255) << 2) | i2;
                                this.tailLen -= t;
                                int op7 = op + 1;
                                output[op] = alphabet2[(v4 >> 12) & 63];
                                int op8 = op7 + 1;
                                output[op7] = alphabet2[(v4 >> 6) & 63];
                                int op9 = op8 + 1;
                                output[op8] = alphabet2[v4 & 63];
                                if (this.do_padding) {
                                    op = op9 + 1;
                                    output[op9] = 61;
                                } else {
                                    op = op9;
                                }
                                if (this.do_newline) {
                                    if (this.do_cr) {
                                        output[op] = 13;
                                        op++;
                                    }
                                    output[op] = 10;
                                    op++;
                                }
                            } else if (this.do_newline && op > 0 && count2 != 19) {
                                if (this.do_cr) {
                                    output[op] = 13;
                                    op++;
                                }
                                output[op] = 10;
                                op++;
                            }
                        } else if (p == len2 - 1) {
                            byte[] bArr2 = this.tail;
                            int i3 = this.tailLen;
                            this.tailLen = i3 + 1;
                            bArr2[i3] = input[p];
                        } else if (p == len2 - 2) {
                            byte[] bArr3 = this.tail;
                            int i4 = this.tailLen;
                            this.tailLen = i4 + 1;
                            bArr3[i4] = input[p];
                            int i5 = this.tailLen;
                            this.tailLen = i5 + 1;
                            bArr3[i5] = input[p + 1];
                        }
                        this.op = op;
                        this.count = count2;
                        return true;
                    }
                } else if (offset + 2 <= len2) {
                    int p6 = offset + 1;
                    v = ((input[offset] & 255) << 8) | ((this.tail[0] & 255) << 16) | (input[p6] & 255);
                    this.tailLen = 0;
                    p = p6 + 1;
                    if (v != -1) {
                    }
                    while (p + 3 <= len2) {
                    }
                    if (finish) {
                    }
                    this.op = op;
                    this.count = count2;
                    return true;
                }
            }
            p = offset;
            if (v != -1) {
            }
            while (p + 3 <= len2) {
            }
            if (finish) {
            }
            this.op = op;
            this.count = count2;
            return true;
        }
    }

    @UnsupportedAppUsage
    private Base64() {
    }
}
