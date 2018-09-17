package tmsdk.common.utils;

import com.qq.taf.jce.JceStruct;
import java.io.UnsupportedEncodingException;

public class b {
    static final /* synthetic */ boolean bF;

    static abstract class a {
        public byte[] Lm;
        public int Ln;

        a() {
        }
    }

    static class b extends a {
        private static final int[] Lo = new int[]{-1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, 62, -1, -1, -1, 63, 52, 53, 54, 55, 56, 57, 58, 59, 60, 61, -1, -1, -1, -2, -1, -1, -1, 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, -1, -1, -1, -1, -1, -1, 26, 27, 28, 29, 30, 31, 32, 33, 34, 35, 36, 37, 38, 39, 40, 41, 42, 43, 44, 45, 46, 47, 48, 49, 50, 51, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1};
        private static final int[] Lp = new int[]{-1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, 62, -1, -1, 52, 53, 54, 55, 56, 57, 58, 59, 60, 61, -1, -1, -1, -2, -1, -1, -1, 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, -1, -1, -1, -1, 63, -1, 26, 27, 28, 29, 30, 31, 32, 33, 34, 35, 36, 37, 38, 39, 40, 41, 42, 43, 44, 45, 46, 47, 48, 49, 50, 51, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1};
        private final int[] Lq;
        private int state;
        private int value;

        public b(int i, byte[] bArr) {
            this.Lm = bArr;
            this.Lq = (i & 8) != 0 ? Lp : Lo;
            this.state = 0;
            this.value = 0;
        }

        /* JADX WARNING: Removed duplicated region for block: B:71:0x0012 A:{SYNTHETIC} */
        /* JADX WARNING: Removed duplicated region for block: B:62:0x0106  */
        /* JADX WARNING: Removed duplicated region for block: B:6:0x0015  */
        /* JADX WARNING: Missing block: B:33:0x0092, code:
            r4 = (r4 << 6) | r1;
            r3 = r3 + 1;
     */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        public boolean a(byte[] bArr, int -l_5_I, int i, boolean z) {
            if (this.state == 6) {
                return false;
            }
            int i2;
            int i3 = i + -l_5_I;
            int i4 = this.state;
            int i5 = this.value;
            int i6 = 0;
            byte[] bArr2 = this.Lm;
            int[] iArr = this.Lq;
            while (-l_5_I < i3) {
                if (i4 == 0) {
                    while (-l_5_I + 4 <= i3) {
                        i5 = (((iArr[bArr[-l_5_I] & 255] << 18) | (iArr[bArr[-l_5_I + 1] & 255] << 12)) | (iArr[bArr[-l_5_I + 2] & 255] << 6)) | iArr[bArr[-l_5_I + 3] & 255];
                        if (i5 >= 0) {
                            bArr2[i6 + 2] = (byte) ((byte) i5);
                            bArr2[i6 + 1] = (byte) ((byte) (i5 >> 8));
                            bArr2[i6] = (byte) ((byte) (i5 >> 16));
                            i6 += 3;
                            -l_5_I += 4;
                        } else if (-l_5_I >= i3) {
                            i2 = i6;
                            if (z) {
                                this.state = i4;
                                this.value = i5;
                                this.Ln = i2;
                                return true;
                            }
                            switch (i4) {
                                case 1:
                                    this.state = 6;
                                    return false;
                                case 2:
                                    i6 = i2 + 1;
                                    bArr2[i2] = (byte) ((byte) (i5 >> 4));
                                    break;
                                case 3:
                                    i6 = i2 + 1;
                                    bArr2[i2] = (byte) ((byte) (i5 >> 10));
                                    i2 = i6 + 1;
                                    bArr2[i6] = (byte) ((byte) (i5 >> 2));
                                    break;
                                case 4:
                                    this.state = 6;
                                    return false;
                            }
                            i6 = i2;
                            this.state = i4;
                            this.Ln = i6;
                            return true;
                        }
                    }
                    if (-l_5_I >= i3) {
                    }
                }
                int i7 = -l_5_I + 1;
                int i8 = iArr[bArr[-l_5_I] & 255];
                switch (i4) {
                    case 0:
                        if (i8 < 0) {
                            if (i8 == -1) {
                                break;
                            }
                            this.state = 6;
                            return false;
                        }
                        i5 = i8;
                        i4++;
                        break;
                    case 1:
                        if (i8 < 0) {
                            if (i8 == -1) {
                                break;
                            }
                            this.state = 6;
                            return false;
                        }
                    case 2:
                        if (i8 < 0) {
                            if (i8 != -2) {
                                if (i8 == -1) {
                                    break;
                                }
                                this.state = 6;
                                return false;
                            }
                            i2 = i6 + 1;
                            bArr2[i6] = (byte) ((byte) (i5 >> 4));
                            i4 = 4;
                            i6 = i2;
                            break;
                        }
                    case 3:
                        if (i8 < 0) {
                            if (i8 != -2) {
                                if (i8 == -1) {
                                    break;
                                }
                                this.state = 6;
                                return false;
                            }
                            bArr2[i6 + 1] = (byte) ((byte) (i5 >> 2));
                            bArr2[i6] = (byte) ((byte) (i5 >> 10));
                            i6 += 2;
                            i4 = 5;
                            break;
                        }
                        i5 = (i5 << 6) | i8;
                        bArr2[i6 + 2] = (byte) ((byte) i5);
                        bArr2[i6 + 1] = (byte) ((byte) (i5 >> 8));
                        bArr2[i6] = (byte) ((byte) (i5 >> 16));
                        i6 += 3;
                        i4 = 0;
                        break;
                    case 4:
                        if (i8 != -2) {
                            if (i8 == -1) {
                                break;
                            }
                            this.state = 6;
                            return false;
                        }
                        i4++;
                        break;
                    case 5:
                        if (i8 == -1) {
                            break;
                        }
                        this.state = 6;
                        return false;
                    default:
                        break;
                }
                -l_5_I = i7;
            }
            i2 = i6;
            if (z) {
            }
        }
    }

    static class c extends a {
        private static final byte[] Lr = new byte[]{(byte) 65, (byte) 66, (byte) 67, (byte) 68, (byte) 69, (byte) 70, (byte) 71, (byte) 72, (byte) 73, (byte) 74, (byte) 75, (byte) 76, (byte) 77, (byte) 78, (byte) 79, (byte) 80, (byte) 81, (byte) 82, (byte) 83, (byte) 84, (byte) 85, (byte) 86, (byte) 87, (byte) 88, (byte) 89, (byte) 90, (byte) 97, (byte) 98, (byte) 99, (byte) 100, (byte) 101, (byte) 102, (byte) 103, (byte) 104, (byte) 105, (byte) 106, (byte) 107, (byte) 108, (byte) 109, (byte) 110, (byte) 111, (byte) 112, (byte) 113, (byte) 114, (byte) 115, (byte) 116, (byte) 117, (byte) 118, (byte) 119, (byte) 120, (byte) 121, (byte) 122, (byte) 48, (byte) 49, (byte) 50, (byte) 51, (byte) 52, (byte) 53, (byte) 54, (byte) 55, (byte) 56, (byte) 57, (byte) 43, (byte) 47};
        private static final byte[] Ls = new byte[]{(byte) 65, (byte) 66, (byte) 67, (byte) 68, (byte) 69, (byte) 70, (byte) 71, (byte) 72, (byte) 73, (byte) 74, (byte) 75, (byte) 76, (byte) 77, (byte) 78, (byte) 79, (byte) 80, (byte) 81, (byte) 82, (byte) 83, (byte) 84, (byte) 85, (byte) 86, (byte) 87, (byte) 88, (byte) 89, (byte) 90, (byte) 97, (byte) 98, (byte) 99, (byte) 100, (byte) 101, (byte) 102, (byte) 103, (byte) 104, (byte) 105, (byte) 106, (byte) 107, (byte) 108, (byte) 109, (byte) 110, (byte) 111, (byte) 112, (byte) 113, (byte) 114, (byte) 115, (byte) 116, (byte) 117, (byte) 118, (byte) 119, (byte) 120, (byte) 121, (byte) 122, (byte) 48, (byte) 49, (byte) 50, (byte) 51, (byte) 52, (byte) 53, (byte) 54, (byte) 55, (byte) 56, (byte) 57, (byte) 45, (byte) 95};
        static final /* synthetic */ boolean bF = (!b.class.desiredAssertionStatus());
        private final byte[] Lt;
        int Lu;
        public final boolean Lv;
        public final boolean Lw;
        public final boolean Lx;
        private final byte[] Ly;
        private int count;

        public c(int i, byte[] bArr) {
            boolean z = true;
            this.Lm = bArr;
            this.Lv = (i & 1) == 0;
            this.Lw = (i & 2) == 0;
            if ((i & 4) == 0) {
                z = false;
            }
            this.Lx = z;
            this.Ly = (i & 8) != 0 ? Ls : Lr;
            this.Lt = new byte[2];
            this.Lu = 0;
            this.count = !this.Lw ? -1 : 19;
        }

        /* JADX WARNING: Removed duplicated region for block: B:30:0x00b8  */
        /* JADX WARNING: Removed duplicated region for block: B:36:0x0112  */
        /* JADX WARNING: Removed duplicated region for block: B:8:0x001a  */
        /* JADX WARNING: Removed duplicated region for block: B:7:0x0018  */
        /* JADX WARNING: Removed duplicated region for block: B:30:0x00b8  */
        /* JADX WARNING: Removed duplicated region for block: B:8:0x001a  */
        /* JADX WARNING: Removed duplicated region for block: B:36:0x0112  */
        /* JADX WARNING: Removed duplicated region for block: B:30:0x00b8  */
        /* JADX WARNING: Removed duplicated region for block: B:36:0x0112  */
        /* JADX WARNING: Removed duplicated region for block: B:8:0x001a  */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        public boolean a(byte[] bArr, int -l_9_I, int i, boolean z) {
            int i2;
            int i3;
            byte[] bArr2 = this.Ly;
            byte[] bArr3 = this.Lm;
            int i4 = 0;
            int i5 = this.count;
            int i6 = i + -l_9_I;
            int i7 = -1;
            switch (this.Lu) {
                case 1:
                    if (-l_9_I + 2 <= i6) {
                        i2 = -l_9_I + 1;
                        -l_9_I = i2 + 1;
                        i7 = (((this.Lt[0] & 255) << 16) | ((bArr[-l_9_I] & 255) << 8)) | (bArr[i2] & 255);
                        this.Lu = 0;
                        break;
                    }
                    break;
                case 2:
                    if (-l_9_I + 1 <= i6) {
                        i2 = -l_9_I + 1;
                        i7 = (((this.Lt[0] & 255) << 16) | ((this.Lt[1] & 255) << 8)) | (bArr[-l_9_I] & 255);
                        this.Lu = 0;
                        -l_9_I = i2;
                        break;
                    }
                    break;
            }
            if (i7 != -1) {
                bArr3[0] = (byte) bArr2[(i7 >> 18) & 63];
                i3 = 1 + 1;
                bArr3[1] = (byte) bArr2[(i7 >> 12) & 63];
                i4 = i3 + 1;
                bArr3[i3] = (byte) bArr2[(i7 >> 6) & 63];
                i3 = i4 + 1;
                bArr3[i4] = (byte) bArr2[i7 & 63];
                i5--;
                if (i5 != 0) {
                    i2 = -l_9_I;
                    if (i2 + 3 > i6) {
                        i7 = (((bArr[i2] & 255) << 16) | ((bArr[i2 + 1] & 255) << 8)) | (bArr[i2 + 2] & 255);
                        bArr3[i3] = (byte) bArr2[(i7 >> 18) & 63];
                        bArr3[i3 + 1] = (byte) bArr2[(i7 >> 12) & 63];
                        bArr3[i3 + 2] = (byte) bArr2[(i7 >> 6) & 63];
                        bArr3[i3 + 3] = (byte) bArr2[i7 & 63];
                        -l_9_I = i2 + 3;
                        i4 = i3 + 4;
                        i5--;
                        if (i5 == 0) {
                            if (this.Lx) {
                                i3 = i4 + 1;
                                bArr3[i4] = JceStruct.SIMPLE_LIST;
                                i4 = i3;
                            }
                            i3 = i4 + 1;
                            bArr3[i4] = (byte) 10;
                            i5 = 19;
                            i2 = -l_9_I;
                            if (i2 + 3 > i6) {
                            }
                        }
                    } else if (z) {
                        byte[] bArr4;
                        if (i2 == i6 - 1) {
                            bArr4 = this.Lt;
                            i6 = this.Lu;
                            this.Lu = i6 + 1;
                            bArr4[i6] = (byte) bArr[i2];
                        } else if (i2 == i6 - 2) {
                            bArr4 = this.Lt;
                            i6 = this.Lu;
                            this.Lu = i6 + 1;
                            bArr4[i6] = (byte) bArr[i2];
                            bArr4 = this.Lt;
                            i6 = this.Lu;
                            this.Lu = i6 + 1;
                            bArr4[i6] = (byte) bArr[i2 + 1];
                        }
                        -l_9_I = i2;
                        i4 = i3;
                    } else {
                        int i8;
                        int i9;
                        if (i2 - this.Lu == i6 - 1) {
                            i8 = 0;
                            if (this.Lu <= 0) {
                                -l_9_I = i2 + 1;
                                i9 = bArr[i2];
                            } else {
                                i8 = 1;
                                i9 = this.Lt[0];
                                -l_9_I = i2;
                            }
                            i7 = (i9 & 255) << 4;
                            this.Lu -= i8;
                            i4 = i3 + 1;
                            bArr3[i3] = (byte) bArr2[(i7 >> 6) & 63];
                            i3 = i4 + 1;
                            bArr3[i4] = (byte) bArr2[i7 & 63];
                            if (this.Lv) {
                                i4 = i3 + 1;
                                bArr3[i3] = (byte) 61;
                                i3 = i4 + 1;
                                bArr3[i4] = (byte) 61;
                            }
                            i4 = i3;
                            if (this.Lw) {
                                if (this.Lx) {
                                    i3 = i4 + 1;
                                    bArr3[i4] = JceStruct.SIMPLE_LIST;
                                    i4 = i3;
                                }
                                i3 = i4 + 1;
                                bArr3[i4] = (byte) 10;
                                i4 = i3;
                            }
                        } else if (i2 - this.Lu != i6 - 2) {
                            if (this.Lw && i3 > 0 && i5 != 19) {
                                if (this.Lx) {
                                    i4 = i3 + 1;
                                    bArr3[i3] = JceStruct.SIMPLE_LIST;
                                } else {
                                    i4 = i3;
                                }
                                i3 = i4 + 1;
                                bArr3[i4] = (byte) 10;
                            }
                            -l_9_I = i2;
                            i4 = i3;
                        } else {
                            i8 = 0;
                            if (this.Lu <= 1) {
                                -l_9_I = i2 + 1;
                                i9 = bArr[i2];
                            } else {
                                i8 = 1;
                                i9 = this.Lt[0];
                                -l_9_I = i2;
                            }
                            int i10 = (i9 & 255) << 10;
                            if (this.Lu <= 0) {
                                i2 = -l_9_I + 1;
                                i9 = bArr[-l_9_I];
                                -l_9_I = i2;
                            } else {
                                int i11 = i8 + 1;
                                i9 = this.Lt[i8];
                                i8 = i11;
                            }
                            i7 = i10 | ((i9 & 255) << 2);
                            this.Lu -= i8;
                            i4 = i3 + 1;
                            bArr3[i3] = (byte) bArr2[(i7 >> 12) & 63];
                            i3 = i4 + 1;
                            bArr3[i4] = (byte) bArr2[(i7 >> 6) & 63];
                            i4 = i3 + 1;
                            bArr3[i3] = (byte) bArr2[i7 & 63];
                            if (this.Lv) {
                                i3 = i4 + 1;
                                bArr3[i4] = (byte) 61;
                                i4 = i3;
                            }
                            if (this.Lw) {
                                if (this.Lx) {
                                    i3 = i4 + 1;
                                    bArr3[i4] = JceStruct.SIMPLE_LIST;
                                    i4 = i3;
                                }
                                i3 = i4 + 1;
                                bArr3[i4] = (byte) 10;
                                i4 = i3;
                            }
                        }
                        if (!bF && this.Lu != 0) {
                            throw new AssertionError();
                        } else if (!(bF || r15 == i6)) {
                            throw new AssertionError();
                        }
                    }
                    if (z) {
                    }
                    this.Ln = i4;
                    this.count = i5;
                    return true;
                }
                if (this.Lx) {
                    i4 = i3 + 1;
                    bArr3[i3] = JceStruct.SIMPLE_LIST;
                } else {
                    i4 = i3;
                }
                i3 = i4 + 1;
                bArr3[i4] = (byte) 10;
                i5 = 19;
                i2 = -l_9_I;
                if (i2 + 3 > i6) {
                }
                if (z) {
                }
                this.Ln = i4;
                this.count = i5;
                return true;
            }
            i2 = -l_9_I;
            i3 = i4;
            if (i2 + 3 > i6) {
            }
            if (z) {
            }
            this.Ln = i4;
            this.count = i5;
            return true;
        }
    }

    static {
        boolean z = false;
        if (!b.class.desiredAssertionStatus()) {
            z = true;
        }
        bF = z;
    }

    private b() {
    }

    public static byte[] decode(String str, int i) {
        return decode(str.getBytes(), i);
    }

    public static byte[] decode(byte[] bArr, int i) {
        return decode(bArr, 0, bArr.length, i);
    }

    public static byte[] decode(byte[] bArr, int i, int i2, int i3) {
        b bVar = new b(i3, new byte[((i2 * 3) / 4)]);
        if (!bVar.a(bArr, i, i2, true)) {
            throw new IllegalArgumentException("bad base-64");
        } else if (bVar.Ln == bVar.Lm.length) {
            return bVar.Lm;
        } else {
            Object obj = new byte[bVar.Ln];
            System.arraycopy(bVar.Lm, 0, obj, 0, bVar.Ln);
            return obj;
        }
    }

    public static byte[] encode(byte[] bArr, int i) {
        return encode(bArr, 0, bArr.length, i);
    }

    public static byte[] encode(byte[] bArr, int i, int i2, int i3) {
        c cVar = new c(i3, null);
        int i4 = (i2 / 3) * 4;
        if (!cVar.Lv) {
            switch (i2 % 3) {
                case 1:
                    i4 += 2;
                    break;
                case 2:
                    i4 += 3;
                    break;
            }
        } else if (i2 % 3 > 0) {
            i4 += 4;
        }
        if (cVar.Lw && i2 > 0) {
            i4 += (!cVar.Lx ? 1 : 2) * (((i2 - 1) / 57) + 1);
        }
        cVar.Lm = new byte[i4];
        cVar.a(bArr, i, i2, true);
        if (bF || cVar.Ln == i4) {
            return cVar.Lm;
        }
        throw new AssertionError();
    }

    public static String encodeToString(byte[] bArr, int i) {
        try {
            return new String(encode(bArr, i), "US-ASCII");
        } catch (UnsupportedEncodingException e) {
            throw new AssertionError(e);
        }
    }
}
