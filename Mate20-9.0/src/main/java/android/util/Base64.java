package android.util;

import android.os.BatteryStats;
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

    static class Decoder extends Coder {
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

        public int maxOutputSize(int len) {
            return ((len * 3) / 4) + 10;
        }

        /* JADX WARNING: Removed duplicated region for block: B:49:0x00e8  */
        /* JADX WARNING: Removed duplicated region for block: B:51:0x00ef  */
        /* JADX WARNING: Removed duplicated region for block: B:63:0x00e5 A[SYNTHETIC] */
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
                        int i = (alphabet2[input[p] & BatteryStats.HistoryItem.CMD_NULL] << 18) | (alphabet2[input[p + 1] & BatteryStats.HistoryItem.CMD_NULL] << 12) | (alphabet2[input[p + 2] & BatteryStats.HistoryItem.CMD_NULL] << 6) | alphabet2[input[p + 3] & BatteryStats.HistoryItem.CMD_NULL];
                        value2 = i;
                        if (i >= 0) {
                            output[op + 2] = (byte) value2;
                            output[op + 1] = (byte) (value2 >> 8);
                            output[op] = (byte) (value2 >> 16);
                            op += 3;
                            p += 4;
                        } else if (p >= len2) {
                            if (finish) {
                                this.state = state2;
                                this.value = value2;
                                this.op = op;
                                return true;
                            }
                            switch (state2) {
                                case 1:
                                    this.state = 6;
                                    return false;
                                case 2:
                                    output[op] = (byte) (value2 >> 4);
                                    op++;
                                    break;
                                case 3:
                                    int op2 = op + 1;
                                    output[op] = (byte) (value2 >> 10);
                                    op = op2 + 1;
                                    output[op2] = (byte) (value2 >> 2);
                                    break;
                                case 4:
                                    this.state = 6;
                                    return false;
                            }
                            this.state = state2;
                            this.op = op;
                            return true;
                        }
                    }
                    if (p >= len2) {
                    }
                }
                int p2 = p + 1;
                int d = alphabet2[input[p] & 255];
                switch (state2) {
                    case 0:
                        if (d < 0) {
                            if (d == -1) {
                                break;
                            } else {
                                this.state = 6;
                                return false;
                            }
                        } else {
                            value2 = d;
                            state2++;
                            break;
                        }
                    case 1:
                        if (d < 0) {
                            if (d == -1) {
                                break;
                            } else {
                                this.state = 6;
                                return false;
                            }
                        } else {
                            value2 = (value2 << 6) | d;
                            state2++;
                            break;
                        }
                    case 2:
                        if (d < 0) {
                            if (d != -2) {
                                if (d == -1) {
                                    break;
                                } else {
                                    this.state = 6;
                                    return false;
                                }
                            } else {
                                output[op] = (byte) (value2 >> 4);
                                state2 = 4;
                                op++;
                                break;
                            }
                        } else {
                            value2 = (value2 << 6) | d;
                            state2++;
                            break;
                        }
                    case 3:
                        if (d < 0) {
                            if (d != -2) {
                                if (d == -1) {
                                    break;
                                } else {
                                    this.state = 6;
                                    return false;
                                }
                            } else {
                                output[op + 1] = (byte) (value2 >> 2);
                                output[op] = (byte) (value2 >> 10);
                                op += 2;
                                state2 = 5;
                                break;
                            }
                        } else {
                            value2 = (value2 << 6) | d;
                            output[op + 2] = (byte) value2;
                            output[op + 1] = (byte) (value2 >> 8);
                            output[op] = (byte) (value2 >> 16);
                            op += 3;
                            state2 = 0;
                            break;
                        }
                    case 4:
                        if (d != -2) {
                            if (d == -1) {
                                break;
                            } else {
                                this.state = 6;
                                return false;
                            }
                        } else {
                            state2++;
                            break;
                        }
                    case 5:
                        if (d == -1) {
                            break;
                        } else {
                            this.state = 6;
                            return false;
                        }
                }
                p = p2;
            }
            if (finish) {
            }
        }
    }

    static class Encoder extends Coder {
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

        static {
            Class<Base64> cls = Base64.class;
        }

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

        public int maxOutputSize(int len) {
            return ((len * 8) / 5) + 10;
        }

        /*  JADX ERROR: JadxOverflowException in pass: RegionMakerVisitor
            jadx.core.utils.exceptions.JadxOverflowException: Regions count limit reached
            	at jadx.core.utils.ErrorsCounter.addError(ErrorsCounter.java:47)
            	at jadx.core.utils.ErrorsCounter.methodError(ErrorsCounter.java:81)
            */
        /* JADX WARNING: Can't fix incorrect switch cases order */
        /* JADX WARNING: Removed duplicated region for block: B:20:0x009e  */
        /* JADX WARNING: Removed duplicated region for block: B:27:0x00f3  */
        /* JADX WARNING: Removed duplicated region for block: B:74:0x01de  */
        /* JADX WARNING: Removed duplicated region for block: B:82:0x00f1 A[SYNTHETIC] */
        public boolean process(byte[] r18, int r19, int r20, boolean r21) {
            /*
                r17 = this;
                r0 = r17
                byte[] r1 = r0.alphabet
                byte[] r2 = r0.output
                r3 = 0
                int r4 = r0.count
                r5 = r19
                int r6 = r20 + r19
                r7 = -1
                int r8 = r0.tailLen
                r9 = 0
                r10 = 1
                switch(r8) {
                    case 0: goto L_0x0057;
                    case 1: goto L_0x0037;
                    case 2: goto L_0x0016;
                    default: goto L_0x0015;
                }
            L_0x0015:
                goto L_0x0058
            L_0x0016:
                int r8 = r5 + 1
                if (r8 > r6) goto L_0x0058
                byte[] r8 = r0.tail
                byte r8 = r8[r9]
                r8 = r8 & 255(0xff, float:3.57E-43)
                int r8 = r8 << 16
                byte[] r11 = r0.tail
                byte r11 = r11[r10]
                r11 = r11 & 255(0xff, float:3.57E-43)
                int r11 = r11 << 8
                r8 = r8 | r11
                int r11 = r5 + 1
                byte r5 = r18[r5]
                r5 = r5 & 255(0xff, float:3.57E-43)
                r7 = r8 | r5
                r0.tailLen = r9
                r8 = r11
                goto L_0x0059
            L_0x0037:
                int r8 = r5 + 2
                if (r8 > r6) goto L_0x0058
                byte[] r8 = r0.tail
                byte r8 = r8[r9]
                r8 = r8 & 255(0xff, float:3.57E-43)
                int r8 = r8 << 16
                int r11 = r5 + 1
                byte r5 = r18[r5]
                r5 = r5 & 255(0xff, float:3.57E-43)
                int r5 = r5 << 8
                r5 = r5 | r8
                int r8 = r11 + 1
                byte r11 = r18[r11]
                r11 = r11 & 255(0xff, float:3.57E-43)
                r7 = r5 | r11
                r0.tailLen = r9
                goto L_0x0059
            L_0x0057:
            L_0x0058:
                r8 = r5
            L_0x0059:
                r5 = -1
                r9 = 13
                r11 = 10
                if (r7 == r5) goto L_0x009a
                int r5 = r3 + 1
                int r12 = r7 >> 18
                r12 = r12 & 63
                byte r12 = r1[r12]
                r2[r3] = r12
                int r3 = r5 + 1
                int r12 = r7 >> 12
                r12 = r12 & 63
                byte r12 = r1[r12]
                r2[r5] = r12
                int r5 = r3 + 1
                int r12 = r7 >> 6
                r12 = r12 & 63
                byte r12 = r1[r12]
                r2[r3] = r12
                int r3 = r5 + 1
                r12 = r7 & 63
                byte r12 = r1[r12]
                r2[r5] = r12
                int r4 = r4 + -1
                if (r4 != 0) goto L_0x009a
                boolean r5 = r0.do_cr
                if (r5 == 0) goto L_0x0093
                int r5 = r3 + 1
                r2[r3] = r9
                r3 = r5
            L_0x0093:
                int r5 = r3 + 1
                r2[r3] = r11
                r4 = 19
            L_0x0099:
                r3 = r5
            L_0x009a:
                int r5 = r8 + 3
                if (r5 > r6) goto L_0x00f1
                byte r5 = r18[r8]
                r5 = r5 & 255(0xff, float:3.57E-43)
                int r5 = r5 << 16
                int r12 = r8 + 1
                byte r12 = r18[r12]
                r12 = r12 & 255(0xff, float:3.57E-43)
                int r12 = r12 << 8
                r5 = r5 | r12
                int r12 = r8 + 2
                byte r12 = r18[r12]
                r12 = r12 & 255(0xff, float:3.57E-43)
                r7 = r5 | r12
                int r5 = r7 >> 18
                r5 = r5 & 63
                byte r5 = r1[r5]
                r2[r3] = r5
                int r5 = r3 + 1
                int r12 = r7 >> 12
                r12 = r12 & 63
                byte r12 = r1[r12]
                r2[r5] = r12
                int r5 = r3 + 2
                int r12 = r7 >> 6
                r12 = r12 & 63
                byte r12 = r1[r12]
                r2[r5] = r12
                int r5 = r3 + 3
                r12 = r7 & 63
                byte r12 = r1[r12]
                r2[r5] = r12
                int r8 = r8 + 3
                int r3 = r3 + 4
                int r4 = r4 + -1
                if (r4 != 0) goto L_0x009a
                boolean r5 = r0.do_cr
                if (r5 == 0) goto L_0x00ea
                int r5 = r3 + 1
                r2[r3] = r9
                r3 = r5
            L_0x00ea:
                int r5 = r3 + 1
                r2[r3] = r11
                r4 = 19
                goto L_0x0099
            L_0x00f1:
                if (r21 == 0) goto L_0x01de
                int r12 = r0.tailLen
                int r12 = r8 - r12
                int r13 = r6 + -1
                r14 = 61
                if (r12 != r13) goto L_0x014d
                r12 = 0
                int r13 = r0.tailLen
                if (r13 <= 0) goto L_0x010c
                byte[] r13 = r0.tail
                int r15 = r12 + 1
                byte r12 = r13[r12]
                r13 = r8
                r8 = r12
                r12 = r15
                goto L_0x0110
            L_0x010c:
                int r13 = r8 + 1
                byte r8 = r18[r8]
            L_0x0110:
                r8 = r8 & 255(0xff, float:3.57E-43)
                int r7 = r8 << 4
                int r8 = r0.tailLen
                int r8 = r8 - r12
                r0.tailLen = r8
                int r8 = r3 + 1
                int r15 = r7 >> 6
                r15 = r15 & 63
                byte r15 = r1[r15]
                r2[r3] = r15
                int r3 = r8 + 1
                r15 = r7 & 63
                byte r15 = r1[r15]
                r2[r8] = r15
                boolean r8 = r0.do_padding
                if (r8 == 0) goto L_0x0137
                int r8 = r3 + 1
                r2[r3] = r14
                int r3 = r8 + 1
                r2[r8] = r14
            L_0x0137:
                boolean r8 = r0.do_newline
                if (r8 == 0) goto L_0x0149
                boolean r8 = r0.do_cr
                if (r8 == 0) goto L_0x0144
                int r8 = r3 + 1
                r2[r3] = r9
                goto L_0x0145
            L_0x0144:
                r8 = r3
            L_0x0145:
                int r3 = r8 + 1
                r2[r8] = r11
            L_0x0149:
            L_0x014a:
                r8 = r13
                goto L_0x01dc
            L_0x014d:
                int r12 = r0.tailLen
                int r12 = r8 - r12
                int r13 = r6 + -2
                if (r12 != r13) goto L_0x01c4
                r12 = 0
                int r13 = r0.tailLen
                if (r13 <= r10) goto L_0x0164
                byte[] r13 = r0.tail
                int r15 = r12 + 1
                byte r12 = r13[r12]
                r13 = r8
                r8 = r12
                r12 = r15
                goto L_0x0168
            L_0x0164:
                int r13 = r8 + 1
                byte r8 = r18[r8]
            L_0x0168:
                r8 = r8 & 255(0xff, float:3.57E-43)
                int r8 = r8 << r11
                int r10 = r0.tailLen
                if (r10 <= 0) goto L_0x0177
                byte[] r10 = r0.tail
                int r15 = r12 + 1
                byte r10 = r10[r12]
                r12 = r15
                goto L_0x0180
            L_0x0177:
                int r10 = r13 + 1
                byte r13 = r18[r13]
                r16 = r13
                r13 = r10
                r10 = r16
            L_0x0180:
                r10 = r10 & 255(0xff, float:3.57E-43)
                int r10 = r10 << 2
                r7 = r8 | r10
                int r8 = r0.tailLen
                int r8 = r8 - r12
                r0.tailLen = r8
                int r8 = r3 + 1
                int r10 = r7 >> 12
                r10 = r10 & 63
                byte r10 = r1[r10]
                r2[r3] = r10
                int r3 = r8 + 1
                int r10 = r7 >> 6
                r10 = r10 & 63
                byte r10 = r1[r10]
                r2[r8] = r10
                int r8 = r3 + 1
                r10 = r7 & 63
                byte r10 = r1[r10]
                r2[r3] = r10
                boolean r3 = r0.do_padding
                if (r3 == 0) goto L_0x01b0
                int r3 = r8 + 1
                r2[r8] = r14
                r8 = r3
            L_0x01b0:
                boolean r3 = r0.do_newline
                if (r3 == 0) goto L_0x01c2
                boolean r3 = r0.do_cr
                if (r3 == 0) goto L_0x01bd
                int r3 = r8 + 1
                r2[r8] = r9
                r8 = r3
            L_0x01bd:
                int r3 = r8 + 1
                r2[r8] = r11
                r8 = r3
            L_0x01c2:
                r3 = r8
                goto L_0x014a
            L_0x01c4:
                boolean r10 = r0.do_newline
                if (r10 == 0) goto L_0x01dc
                if (r3 <= 0) goto L_0x01dc
                r10 = 19
                if (r4 == r10) goto L_0x01dc
                boolean r10 = r0.do_cr
                if (r10 == 0) goto L_0x01d7
                int r10 = r3 + 1
                r2[r3] = r9
                goto L_0x01d8
            L_0x01d7:
                r10 = r3
            L_0x01d8:
                int r3 = r10 + 1
                r2[r10] = r11
            L_0x01dc:
                goto L_0x020d
            L_0x01de:
                int r9 = r6 + -1
                if (r8 != r9) goto L_0x01ef
                byte[] r9 = r0.tail
                int r10 = r0.tailLen
                int r11 = r10 + 1
                r0.tailLen = r11
                byte r11 = r18[r8]
                r9[r10] = r11
                goto L_0x020d
            L_0x01ef:
                int r9 = r6 + -2
                if (r8 != r9) goto L_0x020d
                byte[] r9 = r0.tail
                int r10 = r0.tailLen
                int r11 = r10 + 1
                r0.tailLen = r11
                byte r11 = r18[r8]
                r9[r10] = r11
                byte[] r9 = r0.tail
                int r10 = r0.tailLen
                int r11 = r10 + 1
                r0.tailLen = r11
                int r11 = r8 + 1
                byte r11 = r18[r11]
                r9[r10] = r11
            L_0x020d:
                r0.op = r3
                r0.count = r4
                r9 = 1
                return r9
            */
            throw new UnsupportedOperationException("Method not decompiled: android.util.Base64.Encoder.process(byte[], int, int, boolean):boolean");
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
        if (!encoder.do_padding) {
            switch (len % 3) {
                case 1:
                    output_len += 2;
                    break;
                case 2:
                    output_len += 3;
                    break;
            }
        } else if (len % 3 > 0) {
            output_len += 4;
        }
        if (encoder.do_newline && len > 0) {
            output_len += (((len - 1) / 57) + 1) * (encoder.do_cr ? 2 : 1);
        }
        encoder.output = new byte[output_len];
        encoder.process(input, offset, len, true);
        return encoder.output;
    }

    private Base64() {
    }
}
