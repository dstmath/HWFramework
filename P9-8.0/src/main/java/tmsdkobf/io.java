package tmsdkobf;

import java.lang.reflect.Array;
import java.security.SecureRandom;
import tmsdk.common.exception.BadExpiryDataException;
import tmsdk.common.module.intelli_sms.SmsCheckResult;

final class io {
    private int rK;
    private long rL;
    private long rM;

    /* JADX WARNING: Missing block: B:82:0x02a6, code:
            if ((r20 < ((long) r33) * r24 ? 1 : null) == null) goto L_0x027b;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    io(boolean z) {
        int nextInt;
        lo.a(ir.class);
        SecureRandom secureRandom = new SecureRandom();
        do {
            nextInt = secureRandom.nextInt(900) + SmsCheckResult.ESCT_NORMAL;
        } while (ae(nextInt));
        this.rK = nextInt;
        int i = this.rK;
        int[] iArr = new int[100];
        double sqrt = Math.sqrt((double) i);
        int i2 = ((int) sqrt) - 1;
        while ((i2 + 1) * (i2 + 1) <= i) {
            i2++;
        }
        iArr[0] = i2;
        if (i2 * i2 != i) {
            int i3 = 0;
            double d = sqrt;
            int i4 = 1;
            int[] iArr2 = new int[5];
            iArr2[1] = 1;
            iArr2[2] = 0;
            iArr2[3] = -iArr[0];
            iArr2[4] = 1;
            int[][] iArr3 = (int[][]) Array.newInstance(Integer.TYPE, new int[]{100, 5});
            int i5 = 0;
            while (i4 < 100) {
                if (iArr2[1] < 0) {
                    iArr2[1] = -iArr2[1];
                    iArr2[2] = -iArr2[2];
                    iArr2[3] = -iArr2[3];
                    iArr2[4] = -iArr2[4];
                }
                int k = k(k(k(iArr2[1], Math.abs(iArr2[2])), Math.abs(iArr2[3])), Math.abs(iArr2[4]));
                if (k > 1) {
                    iArr2[1] = iArr2[1] / k;
                    iArr2[2] = iArr2[2] / k;
                    iArr2[3] = iArr2[3] / k;
                    iArr2[4] = iArr2[4] / k;
                }
                i3 = 0;
                while (i3 < i5) {
                    int[] iArr4 = iArr3[i3];
                    if (iArr4[1] == iArr2[1] && iArr4[2] == iArr2[2] && iArr4[3] == iArr2[3] && iArr4[4] == iArr2[4]) {
                        break;
                    }
                    i3++;
                }
                if (i3 < i5) {
                    break;
                }
                iArr3[i5][1] = iArr2[1];
                iArr3[i5][2] = iArr2[2];
                iArr3[i5][3] = iArr2[3];
                iArr3[i5][4] = iArr2[4];
                i5++;
                iArr[i4] = (int) Math.floor(a(sqrt, iArr2));
                a(i, iArr2, iArr[i4]);
                i4++;
            }
            int i6 = i4 - 1;
            int i7 = i3;
            long j = 0;
            long j2 = 1;
            int i8 = 1;
            Object obj = null;
            while (true) {
                if ((j < 200 ? 1 : null) != null || obj == null) {
                    i8++;
                    j = 0;
                    j2 = 1;
                    i4 = i8 - 1;
                    while (i4 >= 0) {
                        long j3 = j;
                        long j4 = j2 + (((long) (i8 > i6 ? iArr[((i4 - i7) % i6) + i7] : iArr[i4])) * j);
                        j2 = j;
                        j = j4;
                        i4--;
                    }
                    if ((j < 1000000 ? 1 : null) == null) {
                        throw new RuntimeException();
                    }
                    long j5 = j * j;
                    long j6 = j2 * j2;
                    long j7 = ((long) i) * j6;
                    long j8 = j5 * 1000;
                    if ((j < 200 ? 1 : null) == null) {
                        if ((999 * j7 >= j8 ? 1 : null) == null) {
                            if ((j8 >= 1001 * j7 ? 1 : null) == null && obj == null) {
                                if (z) {
                                }
                                if (!z) {
                                    if ((j5 <= ((long) i) * j6 ? 1 : null) != null) {
                                    }
                                    obj = 1;
                                }
                            }
                        }
                    }
                } else {
                    b(j, j2);
                    return;
                }
            }
        }
    }

    private static final double a(double d, int[] iArr) {
        return (((double) iArr[1]) + (((double) iArr[2]) * d)) / (((double) iArr[3]) + (((double) iArr[4]) * d));
    }

    private static final void a(int i, int[] iArr, int i2) {
        iArr[1] = iArr[1] - (iArr[3] * i2);
        iArr[2] = iArr[2] - (iArr[4] * i2);
        int i3 = (iArr[1] * iArr[4]) - (iArr[2] * iArr[3]);
        int i4 = (iArr[1] * iArr[1]) - ((iArr[2] * iArr[2]) * i);
        iArr[1] = (iArr[1] * iArr[3]) - ((iArr[2] * iArr[4]) * i);
        iArr[2] = i3;
        iArr[3] = i4;
        iArr[4] = 0;
    }

    private static final boolean ae(int i) {
        int sqrt = ((int) Math.sqrt((double) i)) - 1;
        while ((sqrt + 1) * (sqrt + 1) <= i) {
            sqrt++;
        }
        return sqrt * sqrt == i;
    }

    private void bR() throws BadExpiryDataException {
        long j = (long) this.rK;
        long j2 = this.rL;
        long j3 = this.rM;
        if ((j2 >= 200 ? 1 : null) == null) {
            throw new BadExpiryDataException();
        }
        long j4 = j * (j3 * j3);
        long j5 = j4 * 1000;
        long j6 = (j2 * j2) * 1000;
        if ((j6 <= j5 - j4 ? 1 : null) == null) {
            if ((j6 < j5 + j4 ? 1 : null) != null) {
                return;
            }
        }
        throw new BadExpiryDataException();
    }

    private static final int k(int i, int i2) {
        if (i == 0) {
            return i2;
        }
        if (i2 == 0) {
            return i;
        }
        if (i < i2) {
            return k(i2, i);
        }
        while (true) {
            int i3 = i % i2;
            if (i3 == 0) {
                return i2;
            }
            i = i2;
            i2 = i3;
        }
    }

    final void b(long j, long j2) {
        lo.a(getClass(), ir.class);
        this.rL = j;
        this.rM = j2;
        bR();
    }
}
