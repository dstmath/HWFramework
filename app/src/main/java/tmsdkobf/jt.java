package tmsdkobf;

import android.os.SystemClock;
import java.lang.reflect.Array;
import java.security.SecureRandom;
import tmsdk.common.exception.BadExpiryDataException;
import tmsdk.common.module.intelli_sms.SmsCheckResult;
import tmsdk.common.utils.j;

/* compiled from: Unknown */
final class jt {
    private int uv;
    private long uw;
    private long ux;

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    jt(boolean z) {
        int nextInt;
        mm.a(jw.class);
        SecureRandom secureRandom = new SecureRandom(("" + SystemClock.uptimeMillis() + (((long) j.iM()) * ((long) System.getProperty("os.version", "wtf").hashCode()))).getBytes());
        do {
            nextInt = secureRandom.nextInt(900) + SmsCheckResult.ESCT_NORMAL;
        } while (aX(nextInt));
        this.uv = nextInt;
        int i = this.uv;
        int[] iArr = new int[100];
        double sqrt = Math.sqrt((double) i);
        nextInt = ((int) sqrt) - 1;
        while ((nextInt + 1) * (nextInt + 1) <= i) {
            nextInt++;
        }
        iArr[0] = nextInt;
        if (nextInt * nextInt != i) {
            int i2 = 1;
            int[] iArr2 = new int[5];
            iArr2[1] = 1;
            iArr2[2] = 0;
            iArr2[3] = -iArr[0];
            iArr2[4] = 1;
            int[][] iArr3 = (int[][]) Array.newInstance(Integer.TYPE, new int[]{100, 5});
            int i3 = 0;
            int i4 = 0;
            while (i2 < 100) {
                if (iArr2[1] < 0) {
                    iArr2[1] = -iArr2[1];
                    iArr2[2] = -iArr2[2];
                    iArr2[3] = -iArr2[3];
                    iArr2[4] = -iArr2[4];
                }
                i3 = h(h(h(iArr2[1], Math.abs(iArr2[2])), Math.abs(iArr2[3])), Math.abs(iArr2[4]));
                if (i3 > 1) {
                    iArr2[1] = iArr2[1] / i3;
                    iArr2[2] = iArr2[2] / i3;
                    iArr2[3] = iArr2[3] / i3;
                    iArr2[4] = iArr2[4] / i3;
                }
                i3 = 0;
                while (i3 < i4) {
                    int[] iArr4 = iArr3[i3];
                    if (iArr4[1] == iArr2[1] && iArr4[2] == iArr2[2] && iArr4[3] == iArr2[3]) {
                        if (iArr4[4] == iArr2[4]) {
                            break;
                        }
                    }
                    i3++;
                }
                if (i3 < i4) {
                    break;
                }
                iArr3[i4][1] = iArr2[1];
                iArr3[i4][2] = iArr2[2];
                iArr3[i4][3] = iArr2[3];
                iArr3[i4][4] = iArr2[4];
                i4++;
                iArr[i2] = (int) Math.floor(a(sqrt, iArr2));
                a(i, iArr2, iArr[i2]);
                i2++;
            }
            nextInt = i3;
            int i5 = i2 - 1;
            long j = 0;
            long j2 = 1;
            Object obj = null;
            i2 = 1;
            while (true) {
                if ((j < 200 ? 1 : null) != null || obj == null) {
                    int i6 = i2 + 1;
                    j = 0;
                    j2 = 1;
                    i2 = i6 - 1;
                    while (i2 >= 0) {
                        i3 = i6 > i5 ? iArr[((i2 - nextInt) % i5) + nextInt] : iArr[i2];
                        i2--;
                        long j3 = j2 + (((long) i3) * j);
                        j2 = j;
                        j = j3;
                    }
                    if ((j < 1000000 ? 1 : null) == null) {
                        break;
                    }
                    Object obj2;
                    long j4 = j * j;
                    long j5 = j2 * j2;
                    long j6 = ((long) i) * j5;
                    long j7 = 1000 * j4;
                    if ((j < 200 ? 1 : null) == null) {
                        if ((999 * j6 >= j7 ? 1 : null) == null) {
                            if ((j7 >= j6 * 1001 ? 1 : null) == null && obj == null) {
                                if (z) {
                                    if ((j4 < ((long) i) * j5 ? 1 : null) == null) {
                                    }
                                    obj2 = 1;
                                    obj = obj2;
                                    i2 = i6;
                                }
                                if (!z) {
                                }
                            }
                        }
                    }
                    obj2 = obj;
                    obj = obj2;
                    i2 = i6;
                } else {
                    b(j, j2);
                    return;
                }
            }
            throw new RuntimeException();
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

    private static final boolean aX(int i) {
        int sqrt = ((int) Math.sqrt((double) i)) - 1;
        while ((sqrt + 1) * (sqrt + 1) <= i) {
            sqrt++;
        }
        return sqrt * sqrt == i;
    }

    private void cC() throws BadExpiryDataException {
        Object obj = 1;
        long j = (long) this.uv;
        long j2 = this.uw;
        long j3 = this.ux;
        if ((j2 >= 200 ? 1 : null) == null) {
            throw new BadExpiryDataException();
        }
        long j4 = j2 * j2;
        j *= j3 * j3;
        j2 = j * 1000;
        j3 = j4 * 1000;
        if ((j3 <= j2 - j ? 1 : null) == null) {
            if (j3 >= j2 + j) {
                obj = null;
            }
            if (obj != null) {
                return;
            }
        }
        throw new BadExpiryDataException();
    }

    private static final int h(int i, int i2) {
        if (i == 0) {
            return i2;
        }
        if (i2 == 0) {
            return i;
        }
        if (i < i2) {
            return h(i2, i);
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
        mm.a(getClass(), jw.class);
        this.uw = j;
        this.ux = j2;
        cC();
    }

    final boolean cl() {
        cC();
        long j = (long) this.uv;
        long j2 = this.uw;
        long j3 = this.ux;
        return !(((j2 * j2) > (j * (j3 * j3)) ? 1 : ((j2 * j2) == (j * (j3 * j3)) ? 0 : -1)) >= 0);
    }
}
