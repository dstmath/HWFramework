package android.util;

import com.android.internal.logging.MetricsProto.MetricsEvent;
import huawei.cust.HwCfgFilePolicy;
import java.io.UnsupportedEncodingException;

public class Base64 {
    static final /* synthetic */ boolean -assertionsDisabled = false;
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
        private static final int[] DECODE = null;
        private static final int[] DECODE_WEBSAFE = null;
        private static final int EQUALS = -2;
        private static final int SKIP = -1;
        private final int[] alphabet;
        private int state;
        private int value;

        static {
            /* JADX: method processing error */
/*
            Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.util.Base64.Decoder.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:263)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.util.Base64.Decoder.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 6 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 7 more
*/
            /*
            // Can't load method instructions.
            */
            throw new UnsupportedOperationException("Method not decompiled: android.util.Base64.Decoder.<clinit>():void");
        }

        public Decoder(int flags, byte[] output) {
            this.output = output;
            this.alphabet = (flags & Base64.URL_SAFE) == 0 ? DECODE : DECODE_WEBSAFE;
            this.state = Base64.DEFAULT;
            this.value = Base64.DEFAULT;
        }

        public int maxOutputSize(int len) {
            return ((len * 3) / Base64.CRLF) + 10;
        }

        public boolean process(byte[] input, int offset, int len, boolean finish) {
            if (this.state == 6) {
                return Base64.-assertionsDisabled;
            }
            int op;
            int p = offset;
            len += offset;
            int state = this.state;
            int value = this.value;
            int op2 = Base64.DEFAULT;
            byte[] output = this.output;
            int[] alphabet = this.alphabet;
            while (p < len) {
                if (state == 0) {
                    while (p + Base64.CRLF <= len) {
                        value = (((alphabet[input[p] & MetricsEvent.ACTION_DOUBLE_TAP_POWER_CAMERA_GESTURE] << 18) | (alphabet[input[p + Base64.NO_PADDING] & MetricsEvent.ACTION_DOUBLE_TAP_POWER_CAMERA_GESTURE] << 12)) | (alphabet[input[p + Base64.NO_WRAP] & MetricsEvent.ACTION_DOUBLE_TAP_POWER_CAMERA_GESTURE] << 6)) | alphabet[input[p + 3] & MetricsEvent.ACTION_DOUBLE_TAP_POWER_CAMERA_GESTURE];
                        if (value >= 0) {
                            output[op2 + Base64.NO_WRAP] = (byte) value;
                            output[op2 + Base64.NO_PADDING] = (byte) (value >> Base64.URL_SAFE);
                            output[op2] = (byte) (value >> Base64.NO_CLOSE);
                            op2 += 3;
                            p += Base64.CRLF;
                        } else if (p >= len) {
                            op = op2;
                            if (finish) {
                                switch (state) {
                                    case Base64.DEFAULT /*0*/:
                                        op2 = op;
                                        break;
                                    case Base64.NO_PADDING /*1*/:
                                        this.state = 6;
                                        return Base64.-assertionsDisabled;
                                    case Base64.NO_WRAP /*2*/:
                                        op2 = op + Base64.NO_PADDING;
                                        output[op] = (byte) (value >> Base64.CRLF);
                                        break;
                                    case HwCfgFilePolicy.BASE /*3*/:
                                        op2 = op + Base64.NO_PADDING;
                                        output[op] = (byte) (value >> 10);
                                        op = op2 + Base64.NO_PADDING;
                                        output[op2] = (byte) (value >> Base64.NO_WRAP);
                                        op2 = op;
                                        break;
                                    case Base64.CRLF /*4*/:
                                        this.state = 6;
                                        return Base64.-assertionsDisabled;
                                    case HwCfgFilePolicy.CLOUD_MCC /*5*/:
                                        op2 = op;
                                        break;
                                    default:
                                        op2 = op;
                                        break;
                                }
                                this.state = state;
                                this.op = op2;
                                return true;
                            }
                            this.state = state;
                            this.value = value;
                            this.op = op;
                            return true;
                        }
                    }
                    if (p >= len) {
                        op = op2;
                        if (finish) {
                            switch (state) {
                                case Base64.DEFAULT /*0*/:
                                    op2 = op;
                                    break;
                                case Base64.NO_PADDING /*1*/:
                                    this.state = 6;
                                    return Base64.-assertionsDisabled;
                                case Base64.NO_WRAP /*2*/:
                                    op2 = op + Base64.NO_PADDING;
                                    output[op] = (byte) (value >> Base64.CRLF);
                                    break;
                                case HwCfgFilePolicy.BASE /*3*/:
                                    op2 = op + Base64.NO_PADDING;
                                    output[op] = (byte) (value >> 10);
                                    op = op2 + Base64.NO_PADDING;
                                    output[op2] = (byte) (value >> Base64.NO_WRAP);
                                    op2 = op;
                                    break;
                                case Base64.CRLF /*4*/:
                                    this.state = 6;
                                    return Base64.-assertionsDisabled;
                                case HwCfgFilePolicy.CLOUD_MCC /*5*/:
                                    op2 = op;
                                    break;
                                default:
                                    op2 = op;
                                    break;
                            }
                            this.state = state;
                            this.op = op2;
                            return true;
                        }
                        this.state = state;
                        this.value = value;
                        this.op = op;
                        return true;
                    }
                }
                int p2 = p + Base64.NO_PADDING;
                int d = alphabet[input[p] & MetricsEvent.ACTION_DOUBLE_TAP_POWER_CAMERA_GESTURE];
                switch (state) {
                    case Base64.DEFAULT /*0*/:
                        if (d < 0) {
                            if (d == SKIP) {
                                break;
                            }
                            this.state = 6;
                            return Base64.-assertionsDisabled;
                        }
                        value = d;
                        state += Base64.NO_PADDING;
                        break;
                    case Base64.NO_PADDING /*1*/:
                        if (d < 0) {
                            if (d == SKIP) {
                                break;
                            }
                            this.state = 6;
                            return Base64.-assertionsDisabled;
                        }
                        value = (value << 6) | d;
                        state += Base64.NO_PADDING;
                        break;
                    case Base64.NO_WRAP /*2*/:
                        if (d < 0) {
                            if (d != EQUALS) {
                                if (d == SKIP) {
                                    break;
                                }
                                this.state = 6;
                                return Base64.-assertionsDisabled;
                            }
                            op = op2 + Base64.NO_PADDING;
                            output[op2] = (byte) (value >> Base64.CRLF);
                            state = Base64.CRLF;
                            op2 = op;
                            break;
                        }
                        value = (value << 6) | d;
                        state += Base64.NO_PADDING;
                        break;
                    case HwCfgFilePolicy.BASE /*3*/:
                        if (d < 0) {
                            if (d != EQUALS) {
                                if (d == SKIP) {
                                    break;
                                }
                                this.state = 6;
                                return Base64.-assertionsDisabled;
                            }
                            output[op2 + Base64.NO_PADDING] = (byte) (value >> Base64.NO_WRAP);
                            output[op2] = (byte) (value >> 10);
                            op2 += Base64.NO_WRAP;
                            state = 5;
                            break;
                        }
                        value = (value << 6) | d;
                        output[op2 + Base64.NO_WRAP] = (byte) value;
                        output[op2 + Base64.NO_PADDING] = (byte) (value >> Base64.URL_SAFE);
                        output[op2] = (byte) (value >> Base64.NO_CLOSE);
                        op2 += 3;
                        state = Base64.DEFAULT;
                        break;
                    case Base64.CRLF /*4*/:
                        if (d != EQUALS) {
                            if (d == SKIP) {
                                break;
                            }
                            this.state = 6;
                            return Base64.-assertionsDisabled;
                        }
                        state += Base64.NO_PADDING;
                        break;
                    case HwCfgFilePolicy.CLOUD_MCC /*5*/:
                        if (d == SKIP) {
                            break;
                        }
                        this.state = 6;
                        return Base64.-assertionsDisabled;
                    default:
                        break;
                }
                p = p2;
            }
            op = op2;
            if (finish) {
                this.state = state;
                this.value = value;
                this.op = op;
                return true;
            }
            switch (state) {
                case Base64.DEFAULT /*0*/:
                    op2 = op;
                    break;
                case Base64.NO_PADDING /*1*/:
                    this.state = 6;
                    return Base64.-assertionsDisabled;
                case Base64.NO_WRAP /*2*/:
                    op2 = op + Base64.NO_PADDING;
                    output[op] = (byte) (value >> Base64.CRLF);
                    break;
                case HwCfgFilePolicy.BASE /*3*/:
                    op2 = op + Base64.NO_PADDING;
                    output[op] = (byte) (value >> 10);
                    op = op2 + Base64.NO_PADDING;
                    output[op2] = (byte) (value >> Base64.NO_WRAP);
                    op2 = op;
                    break;
                case Base64.CRLF /*4*/:
                    this.state = 6;
                    return Base64.-assertionsDisabled;
                case HwCfgFilePolicy.CLOUD_MCC /*5*/:
                    op2 = op;
                    break;
                default:
                    op2 = op;
                    break;
            }
            this.state = state;
            this.op = op2;
            return true;
        }
    }

    static class Encoder extends Coder {
        static final /* synthetic */ boolean -assertionsDisabled = false;
        private static final byte[] ENCODE = null;
        private static final byte[] ENCODE_WEBSAFE = null;
        public static final int LINE_GROUPS = 19;
        private final byte[] alphabet;
        private int count;
        public final boolean do_cr;
        public final boolean do_newline;
        public final boolean do_padding;
        private final byte[] tail;
        int tailLen;

        static {
            /* JADX: method processing error */
/*
            Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.util.Base64.Encoder.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:263)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.util.Base64.Encoder.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 6 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e9
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 7 more
*/
            /*
            // Can't load method instructions.
            */
            throw new UnsupportedOperationException("Method not decompiled: android.util.Base64.Encoder.<clinit>():void");
        }

        public Encoder(int flags, byte[] output) {
            boolean z;
            int i;
            boolean z2 = true;
            this.output = output;
            if ((flags & Base64.NO_PADDING) == 0) {
                z = true;
            } else {
                z = -assertionsDisabled;
            }
            this.do_padding = z;
            if ((flags & Base64.NO_WRAP) == 0) {
                z = true;
            } else {
                z = -assertionsDisabled;
            }
            this.do_newline = z;
            if ((flags & Base64.CRLF) == 0) {
                z2 = -assertionsDisabled;
            }
            this.do_cr = z2;
            this.alphabet = (flags & Base64.URL_SAFE) == 0 ? ENCODE : ENCODE_WEBSAFE;
            this.tail = new byte[Base64.NO_WRAP];
            this.tailLen = Base64.DEFAULT;
            if (this.do_newline) {
                i = LINE_GROUPS;
            } else {
                i = -1;
            }
            this.count = i;
        }

        public int maxOutputSize(int len) {
            return ((len * Base64.URL_SAFE) / 5) + 10;
        }

        public boolean process(byte[] r15, int r16, int r17, boolean r18) {
            /* JADX: method processing error */
/*
            Error: jadx.core.utils.exceptions.JadxOverflowException: Regions stack size limit reached
	at jadx.core.utils.ErrorsCounter.addError(ErrorsCounter.java:42)
	at jadx.core.utils.ErrorsCounter.methodError(ErrorsCounter.java:66)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:33)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:17)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:14)
	at jadx.core.ProcessClass.process(ProcessClass.java:37)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
*/
            /*
            r14 = this;
            r1 = r14.alphabet;
            r5 = r14.output;
            r3 = 0;
            r2 = r14.count;
            r6 = r16;
            r17 = r17 + r16;
            r10 = -1;
            r11 = r14.tailLen;
            switch(r11) {
                case 0: goto L_0x0011;
                case 1: goto L_0x00b1;
                case 2: goto L_0x00d7;
                default: goto L_0x0011;
            };
        L_0x0011:
            r11 = -1;
            if (r10 == r11) goto L_0x00ae;
        L_0x0014:
            r11 = 0;
            r3 = 1;
            r12 = r10 >> 18;
            r12 = r12 & 63;
            r12 = r1[r12];
            r5[r11] = r12;
            r4 = r3 + 1;
            r11 = r10 >> 12;
            r11 = r11 & 63;
            r11 = r1[r11];
            r5[r3] = r11;
            r3 = r4 + 1;
            r11 = r10 >> 6;
            r11 = r11 & 63;
            r11 = r1[r11];
            r5[r4] = r11;
            r4 = r3 + 1;
            r11 = r10 & 63;
            r11 = r1[r11];
            r5[r3] = r11;
            r2 = r2 + -1;
            if (r2 != 0) goto L_0x0260;
        L_0x003e:
            r11 = r14.do_cr;
            if (r11 == 0) goto L_0x025d;
        L_0x0042:
            r3 = r4 + 1;
            r11 = 13;
            r5[r4] = r11;
        L_0x0048:
            r4 = r3 + 1;
            r11 = 10;
            r5[r3] = r11;
            r2 = 19;
            r7 = r6;
        L_0x0051:
            r11 = r7 + 3;
            r0 = r17;
            if (r11 > r0) goto L_0x00fd;
        L_0x0057:
            r11 = r15[r7];
            r11 = r11 & 255;
            r11 = r11 << 16;
            r12 = r7 + 1;
            r12 = r15[r12];
            r12 = r12 & 255;
            r12 = r12 << 8;
            r11 = r11 | r12;
            r12 = r7 + 2;
            r12 = r15[r12];
            r12 = r12 & 255;
            r10 = r11 | r12;
            r11 = r10 >> 18;
            r11 = r11 & 63;
            r11 = r1[r11];
            r5[r4] = r11;
            r11 = r4 + 1;
            r12 = r10 >> 12;
            r12 = r12 & 63;
            r12 = r1[r12];
            r5[r11] = r12;
            r11 = r4 + 2;
            r12 = r10 >> 6;
            r12 = r12 & 63;
            r12 = r1[r12];
            r5[r11] = r12;
            r11 = r4 + 3;
            r12 = r10 & 63;
            r12 = r1[r12];
            r5[r11] = r12;
            r6 = r7 + 3;
            r3 = r4 + 4;
            r2 = r2 + -1;
            if (r2 != 0) goto L_0x00ae;
        L_0x009a:
            r11 = r14.do_cr;
            if (r11 == 0) goto L_0x00a5;
        L_0x009e:
            r4 = r3 + 1;
            r11 = 13;
            r5[r3] = r11;
            r3 = r4;
        L_0x00a5:
            r4 = r3 + 1;
            r11 = 10;
            r5[r3] = r11;
            r2 = 19;
            r3 = r4;
        L_0x00ae:
            r7 = r6;
            r4 = r3;
            goto L_0x0051;
        L_0x00b1:
            r11 = r16 + 2;
            r0 = r17;
            if (r11 > r0) goto L_0x0011;
        L_0x00b7:
            r11 = r14.tail;
            r12 = 0;
            r11 = r11[r12];
            r11 = r11 & 255;
            r11 = r11 << 16;
            r6 = r16 + 1;
            r12 = r15[r16];
            r12 = r12 & 255;
            r12 = r12 << 8;
            r11 = r11 | r12;
            r7 = r6 + 1;
            r12 = r15[r6];
            r12 = r12 & 255;
            r10 = r11 | r12;
            r11 = 0;
            r14.tailLen = r11;
            r6 = r7;
            goto L_0x0011;
        L_0x00d7:
            r11 = r16 + 1;
            r0 = r17;
            if (r11 > r0) goto L_0x0011;
        L_0x00dd:
            r11 = r14.tail;
            r12 = 0;
            r11 = r11[r12];
            r11 = r11 & 255;
            r11 = r11 << 16;
            r12 = r14.tail;
            r13 = 1;
            r12 = r12[r13];
            r12 = r12 & 255;
            r12 = r12 << 8;
            r11 = r11 | r12;
            r6 = r16 + 1;
            r12 = r15[r16];
            r12 = r12 & 255;
            r10 = r11 | r12;
            r11 = 0;
            r14.tailLen = r11;
            goto L_0x0011;
        L_0x00fd:
            if (r18 == 0) goto L_0x021c;
        L_0x00ff:
            r11 = r14.tailLen;
            r11 = r7 - r11;
            r12 = r17 + -1;
            if (r11 != r12) goto L_0x016b;
        L_0x0107:
            r8 = 0;
            r11 = r14.tailLen;
            if (r11 <= 0) goto L_0x0166;
        L_0x010c:
            r11 = r14.tail;
            r12 = 0;
            r8 = 1;
            r11 = r11[r12];
            r6 = r7;
        L_0x0113:
            r11 = r11 & 255;
            r10 = r11 << 4;
            r11 = r14.tailLen;
            r11 = r11 - r8;
            r14.tailLen = r11;
            r3 = r4 + 1;
            r11 = r10 >> 6;
            r11 = r11 & 63;
            r11 = r1[r11];
            r5[r4] = r11;
            r4 = r3 + 1;
            r11 = r10 & 63;
            r11 = r1[r11];
            r5[r3] = r11;
            r11 = r14.do_padding;
            if (r11 == 0) goto L_0x025a;
        L_0x0132:
            r3 = r4 + 1;
            r11 = 61;
            r5[r4] = r11;
            r4 = r3 + 1;
            r11 = 61;
            r5[r3] = r11;
            r3 = r4;
        L_0x013f:
            r11 = r14.do_newline;
            if (r11 == 0) goto L_0x0155;
        L_0x0143:
            r11 = r14.do_cr;
            if (r11 == 0) goto L_0x014e;
        L_0x0147:
            r4 = r3 + 1;
            r11 = 13;
            r5[r3] = r11;
            r3 = r4;
        L_0x014e:
            r4 = r3 + 1;
            r11 = 10;
            r5[r3] = r11;
            r3 = r4;
        L_0x0155:
            r11 = -assertionsDisabled;
            if (r11 != 0) goto L_0x0209;
        L_0x0159:
            r11 = r14.tailLen;
            if (r11 != 0) goto L_0x0206;
        L_0x015d:
            r11 = 1;
        L_0x015e:
            if (r11 != 0) goto L_0x0209;
        L_0x0160:
            r11 = new java.lang.AssertionError;
            r11.<init>();
            throw r11;
        L_0x0166:
            r6 = r7 + 1;
            r11 = r15[r7];
            goto L_0x0113;
        L_0x016b:
            r11 = r14.tailLen;
            r11 = r7 - r11;
            r12 = r17 + -2;
            if (r11 != r12) goto L_0x01e4;
        L_0x0173:
            r8 = 0;
            r11 = r14.tailLen;
            r12 = 1;
            if (r11 <= r12) goto L_0x01d9;
        L_0x0179:
            r11 = r14.tail;
            r12 = 0;
            r8 = 1;
            r11 = r11[r12];
            r6 = r7;
        L_0x0180:
            r11 = r11 & 255;
            r12 = r11 << 10;
            r11 = r14.tailLen;
            if (r11 <= 0) goto L_0x01de;
        L_0x0188:
            r11 = r14.tail;
            r9 = r8 + 1;
            r11 = r11[r8];
            r8 = r9;
        L_0x018f:
            r11 = r11 & 255;
            r11 = r11 << 2;
            r10 = r12 | r11;
            r11 = r14.tailLen;
            r11 = r11 - r8;
            r14.tailLen = r11;
            r3 = r4 + 1;
            r11 = r10 >> 12;
            r11 = r11 & 63;
            r11 = r1[r11];
            r5[r4] = r11;
            r4 = r3 + 1;
            r11 = r10 >> 6;
            r11 = r11 & 63;
            r11 = r1[r11];
            r5[r3] = r11;
            r3 = r4 + 1;
            r11 = r10 & 63;
            r11 = r1[r11];
            r5[r4] = r11;
            r11 = r14.do_padding;
            if (r11 == 0) goto L_0x01c1;
        L_0x01ba:
            r4 = r3 + 1;
            r11 = 61;
            r5[r3] = r11;
            r3 = r4;
        L_0x01c1:
            r11 = r14.do_newline;
            if (r11 == 0) goto L_0x0155;
        L_0x01c5:
            r11 = r14.do_cr;
            if (r11 == 0) goto L_0x01d0;
        L_0x01c9:
            r4 = r3 + 1;
            r11 = 13;
            r5[r3] = r11;
            r3 = r4;
        L_0x01d0:
            r4 = r3 + 1;
            r11 = 10;
            r5[r3] = r11;
            r3 = r4;
            goto L_0x0155;
        L_0x01d9:
            r6 = r7 + 1;
            r11 = r15[r7];
            goto L_0x0180;
        L_0x01de:
            r7 = r6 + 1;
            r11 = r15[r6];
            r6 = r7;
            goto L_0x018f;
        L_0x01e4:
            r11 = r14.do_newline;
            if (r11 == 0) goto L_0x0202;
        L_0x01e8:
            if (r4 <= 0) goto L_0x0202;
        L_0x01ea:
            r11 = 19;
            if (r2 == r11) goto L_0x0202;
        L_0x01ee:
            r11 = r14.do_cr;
            if (r11 == 0) goto L_0x0258;
        L_0x01f2:
            r3 = r4 + 1;
            r11 = 13;
            r5[r4] = r11;
        L_0x01f8:
            r4 = r3 + 1;
            r11 = 10;
            r5[r3] = r11;
            r6 = r7;
            r3 = r4;
            goto L_0x0155;
        L_0x0202:
            r6 = r7;
            r3 = r4;
            goto L_0x0155;
        L_0x0206:
            r11 = 0;
            goto L_0x015e;
        L_0x0209:
            r11 = -assertionsDisabled;
            if (r11 != 0) goto L_0x022e;
        L_0x020d:
            r0 = r17;
            if (r6 != r0) goto L_0x021a;
        L_0x0211:
            r11 = 1;
        L_0x0212:
            if (r11 != 0) goto L_0x022e;
        L_0x0214:
            r11 = new java.lang.AssertionError;
            r11.<init>();
            throw r11;
        L_0x021a:
            r11 = 0;
            goto L_0x0212;
        L_0x021c:
            r11 = r17 + -1;
            if (r7 != r11) goto L_0x0234;
        L_0x0220:
            r11 = r14.tail;
            r12 = r14.tailLen;
            r13 = r12 + 1;
            r14.tailLen = r13;
            r13 = r15[r7];
            r11[r12] = r13;
            r6 = r7;
            r3 = r4;
        L_0x022e:
            r14.op = r3;
            r14.count = r2;
            r11 = 1;
            return r11;
        L_0x0234:
            r11 = r17 + -2;
            if (r7 != r11) goto L_0x0255;
        L_0x0238:
            r11 = r14.tail;
            r12 = r14.tailLen;
            r13 = r12 + 1;
            r14.tailLen = r13;
            r13 = r15[r7];
            r11[r12] = r13;
            r11 = r14.tail;
            r12 = r14.tailLen;
            r13 = r12 + 1;
            r14.tailLen = r13;
            r13 = r7 + 1;
            r13 = r15[r13];
            r11[r12] = r13;
            r6 = r7;
            r3 = r4;
            goto L_0x022e;
        L_0x0255:
            r6 = r7;
            r3 = r4;
            goto L_0x022e;
        L_0x0258:
            r3 = r4;
            goto L_0x01f8;
        L_0x025a:
            r3 = r4;
            goto L_0x013f;
        L_0x025d:
            r3 = r4;
            goto L_0x0048;
        L_0x0260:
            r7 = r6;
            goto L_0x0051;
            */
            throw new UnsupportedOperationException("Method not decompiled: android.util.Base64.Encoder.process(byte[], int, int, boolean):boolean");
        }
    }

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.util.Base64.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.util.Base64.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 5 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e9
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 6 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: android.util.Base64.<clinit>():void");
    }

    public static byte[] decode(String str, int flags) {
        return decode(str.getBytes(), flags);
    }

    public static byte[] decode(byte[] input, int flags) {
        return decode(input, DEFAULT, input.length, flags);
    }

    public static byte[] decode(byte[] input, int offset, int len, int flags) {
        Decoder decoder = new Decoder(flags, new byte[((len * 3) / CRLF)]);
        if (!decoder.process(input, offset, len, true)) {
            throw new IllegalArgumentException("bad base-64");
        } else if (decoder.op == decoder.output.length) {
            return decoder.output;
        } else {
            byte[] temp = new byte[decoder.op];
            System.arraycopy(decoder.output, DEFAULT, temp, DEFAULT, decoder.op);
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
        return encode(input, DEFAULT, input.length, flags);
    }

    public static byte[] encode(byte[] input, int offset, int len, int flags) {
        boolean z = true;
        Encoder encoder = new Encoder(flags, null);
        int output_len = (len / 3) * CRLF;
        if (!encoder.do_padding) {
            switch (len % 3) {
                case DEFAULT /*0*/:
                    break;
                case NO_PADDING /*1*/:
                    output_len += NO_WRAP;
                    break;
                case NO_WRAP /*2*/:
                    output_len += 3;
                    break;
                default:
                    break;
            }
        } else if (len % 3 > 0) {
            output_len += CRLF;
        }
        if (encoder.do_newline && len > 0) {
            int i;
            int i2 = ((len - 1) / 57) + NO_PADDING;
            if (encoder.do_cr) {
                i = NO_WRAP;
            } else {
                i = NO_PADDING;
            }
            output_len += i * i2;
        }
        encoder.output = new byte[output_len];
        encoder.process(input, offset, len, true);
        if (!-assertionsDisabled) {
            if (encoder.op != output_len) {
                z = -assertionsDisabled;
            }
            if (!z) {
                throw new AssertionError();
            }
        }
        return encoder.output;
    }

    private Base64() {
    }
}
