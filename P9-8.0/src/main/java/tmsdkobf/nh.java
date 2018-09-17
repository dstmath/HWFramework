package tmsdkobf;

import android.content.Context;
import android.os.Process;
import com.qq.taf.jce.JceStruct;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.InflaterInputStream;
import tmsdk.common.TMSDKContext;
import tmsdk.common.tcc.TccCryptor;
import tmsdk.common.utils.e;
import tmsdk.common.utils.i;
import tmsdkobf.nq.b;
import tmsdkobf.nw.f;

public class nh {
    private static i Dj;
    private static String Dk;
    private static boolean Dl = false;

    public static JceStruct a(Context context, byte[] bArr, byte[] bArr2, JceStruct jceStruct, boolean z) {
        if (bArr2 == null || bArr2.length == 0) {
            return null;
        }
        byte[] a = a(context, bArr, bArr2);
        return a != null ? nn.a(a, jceStruct, z) : null;
    }

    public static JceStruct a(Context context, byte[] bArr, byte[] bArr2, JceStruct jceStruct, boolean z, int i) {
        if (bArr2 == null || bArr2.length == 0) {
            return null;
        }
        JceStruct jceStruct2 = null;
        byte[] a = a(context, bArr, bArr2, i);
        if (!(a == null || a.length <= 0 || jceStruct == null)) {
            jceStruct2 = nn.a(a, jceStruct, z);
            if (jceStruct2 == null) {
                mb.s("ConverterUtil", "[shark_v4][shark_cmd]dataForReceive2JceStruct(), getJceStruct() return null! jceData: " + Arrays.toString(a));
            }
        }
        return jceStruct2;
    }

    public static JceStruct a(byte[] bArr, byte[] bArr2, JceStruct jceStruct) {
        return a(null, bArr, bArr2, jceStruct, false);
    }

    private static bx a(Context context, boolean z, f fVar, b bVar, ArrayList<bw> arrayList, String str, nl nlVar) {
        bx fR = nn.fR();
        fR.ey = fVar.Fq;
        fR.eH = 4;
        fR.eJ = arrayList;
        i a;
        if (!z || fVar.Fj || fVar.Fk || fVar.Fm) {
            mb.d("ConverterUtil", "[shark_v4][shark_fin] must take sharkfin: !isTcpChannel: " + (!z) + " isRsa: " + fVar.Fj + " isGuid: " + fVar.Fk + " isFP: " + fVar.Fm);
            a = a(context, fVar.Fj, bVar, str, nlVar);
            fR.eI = a;
            Dj = a;
        } else {
            if (!fVar.Fl) {
                a = a(context, false, bVar, str, nlVar);
                if (!a(a, Dj)) {
                    fR.eI = a;
                    Dj = a;
                } else if (Dl) {
                    fR.eI = a;
                    Dj = a;
                } else {
                    mb.n("ConverterUtil", "[shark_v4][shark_fin] sharkfin unchanged, no need to take sharkfin");
                }
            }
            return fR;
        }
        Dl = false;
        return fR;
    }

    private static i a(Context context, boolean z, b bVar, String str, nl nlVar) {
        boolean z2 = false;
        if (nlVar == null) {
            return null;
        }
        i iVar = new i();
        String str2 = bVar == null ? "" : bVar.DW;
        if (z) {
            str2 = "";
        }
        iVar.K = str2;
        iVar.L = 3059;
        iVar.s = w(context);
        iVar.M = i.J(context);
        iVar.authType = fz();
        StringBuilder append = new StringBuilder().append("[ip_list][conn_monitor]checkSharkfin(), apn=").append(iVar.s).append(" isWifi=");
        String str3 = "ConverterUtil";
        if (ln.yO == (byte) 3) {
            z2 = true;
        }
        mb.n(str3, append.append(z2).append(" authType=").append(iVar.authType).toString());
        iVar.I = str;
        iVar.N = nlVar.aM();
        iVar.O = nlVar.aR();
        iVar.P = nlVar.aS();
        if (Dk == null) {
            int myPid = Process.myPid();
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append(!nlVar.aB() ? "f" : "b");
            stringBuilder.append(myPid);
            Dk = stringBuilder.toString();
        }
        iVar.J = Dk;
        return iVar;
    }

    private static boolean a(i iVar, i iVar2) {
        boolean z = true;
        if (iVar == null && iVar2 == null) {
            return true;
        }
        if (iVar == null || iVar2 == null) {
            return false;
        }
        if (!(iVar.s == iVar2.s && iVar.authType == iVar2.authType && v(iVar.I, iVar2.I) && v(iVar.J, iVar2.J) && v(iVar.K, iVar2.K) && iVar.L == iVar2.L && iVar.M == iVar2.M && iVar.N == iVar2.N && iVar.O == iVar2.O && v(iVar.P, iVar2.P))) {
            z = false;
        }
        return z;
    }

    public static byte[] a(Context context, JceStruct jceStruct, int i, bw bwVar) {
        byte[] bArr = null;
        if (jceStruct != null) {
            bArr = nn.d(jceStruct);
        }
        return a(context, bArr, i, bwVar);
    }

    public static byte[] a(Context context, byte[] -l_5_R, int i, bw bwVar) {
        Object e;
        int i2;
        if (-l_5_R != null && -l_5_R.length > 50) {
            byte[] p = p(-l_5_R);
            if (bwVar != null) {
                try {
                    i2 = bwVar.eE;
                    if (p != null) {
                        if (p.length < -l_5_R.length) {
                            bwVar.eE &= -2;
                            mb.n("ConverterUtil", "[shark_compress]compressed, length: " + -l_5_R.length + " -> " + p.length + " cmdId: " + i + " flag: " + Integer.toBinaryString(i2) + " -> " + Integer.toBinaryString(bwVar.eE));
                        }
                    }
                    int length = p != null ? p.length : -1;
                    try {
                        bwVar.eE |= 1;
                        mb.n("ConverterUtil", "[shark_compress]donnot compress, length: " + -l_5_R.length + " (if compress)|-> " + length + " cmdId: " + i + " flag: " + Integer.toBinaryString(i2) + " -> " + Integer.toBinaryString(bwVar.eE));
                    } catch (Exception e2) {
                        e = e2;
                    }
                } catch (Exception e3) {
                    -l_5_R = p;
                    Exception e4 = e3;
                    mb.o("ConverterUtil", "jceStruct2DataForSend(), exception: " + e4);
                    Object obj = e4;
                    return null;
                }
            }
            -l_5_R = p;
        } else if (bwVar != null) {
            i2 = bwVar.eE;
            bwVar.eE |= 1;
            mb.n("ConverterUtil", "[shark_compress]without compress, length: " + (-l_5_R == null ? "null" : "" + -l_5_R.length) + " cmdId: " + i + " flag: " + Integer.toBinaryString(i2) + " -> " + Integer.toBinaryString(bwVar.eE));
        }
        return c(i, -l_5_R);
    }

    @Deprecated
    public static byte[] a(Context context, byte[] bArr, JceStruct jceStruct) {
        if (jceStruct == null) {
            return null;
        }
        byte[] c = c(jceStruct);
        return c != null ? TccCryptor.encrypt(c, bArr) : null;
    }

    public static byte[] a(Context context, byte[] bArr, byte[] bArr2) {
        if (bArr2 == null || bArr2.length == 0) {
            return null;
        }
        try {
            byte[] decrypt = TccCryptor.decrypt(bArr2, bArr);
            return decrypt != null ? q(decrypt) : null;
        } catch (Exception e) {
            return null;
        }
    }

    public static byte[] a(Context context, byte[] bArr, byte[] -l_4_R, int i) {
        boolean z = false;
        if (-l_4_R == null || -l_4_R.length == 0) {
            StringBuilder append = new StringBuilder().append("[shark_v4]dataForReceive2JceBytes(), null or empty data, null? ");
            String str = "ConverterUtil";
            if (-l_4_R == null) {
                z = true;
            }
            mb.s(str, append.append(z).toString());
            return null;
        }
        if ((i & 2) == 0) {
            try {
                -l_4_R = TccCryptor.decrypt(-l_4_R, bArr);
            } catch (Exception e) {
                mb.s("ConverterUtil", "[shark_v4]dataForReceive2JceBytes(), decrypt exception: " + e);
                -l_4_R = null;
            }
        }
        if (-l_4_R != null && -l_4_R.length >= 4) {
            byte[] o = o(-l_4_R);
            if (o != null && o.length > 0) {
                byte[] q = (i & 1) != 0 ? o : q(o);
                if (q == null) {
                    mb.s("ConverterUtil", "[shark_v4]dataForReceive2JceBytes(), decompress failed!");
                }
                return q;
            }
        }
        mb.s("ConverterUtil", "[shark_v4]dataForReceive2JceBytes(), data should be at least 4 bytes: " + (-l_4_R == null ? -1 : -l_4_R.length));
        return null;
    }

    public static byte[] a(f fVar, boolean z, String str, nl nlVar) {
        if (fVar == null) {
            return null;
        }
        byte[] d = !fVar.Fl ? nn.d(a(TMSDKContext.getApplicaionContext(), z, fVar, fVar.Fr, fVar.Ft, str, nlVar)) : new byte[]{(byte) fVar.Fx};
        mb.d("ConverterUtil", "createSendBytes(), isHello? " + fVar.Fl + " sendData.length: " + (d == null ? -1 : d.length));
        return d;
    }

    public static JceStruct b(byte[] bArr, JceStruct jceStruct) {
        return nn.a(bArr, jceStruct, false);
    }

    public static byte[] b(JceStruct jceStruct) {
        return jceStruct != null ? nn.d(jceStruct) : null;
    }

    private static byte[] c(int i, byte[] bArr) {
        try {
            OutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            DataOutputStream dataOutputStream = new DataOutputStream(byteArrayOutputStream);
            dataOutputStream.writeInt(i);
            if (bArr != null && bArr.length > 0) {
                dataOutputStream.write(bArr);
            }
            return byteArrayOutputStream.toByteArray();
        } catch (Exception e) {
            mb.s("ConverterUtil", "[shark_v4]appendIntHeader(), exception: " + e);
            return null;
        }
    }

    public static byte[] c(JceStruct jceStruct) {
        if (jceStruct == null) {
            return null;
        }
        byte[] p = p(nn.d(jceStruct));
        return p != null ? p : null;
    }

    public static byte[] c(byte[] bArr, JceStruct jceStruct) {
        return a(null, bArr, jceStruct);
    }

    public static byte[] decrypt(byte[] bArr, byte[] bArr2) {
        if (bArr == null || bArr.length == 0) {
            return bArr;
        }
        try {
            return TccCryptor.decrypt(bArr, bArr2);
        } catch (Throwable th) {
            mb.o("ConverterUtil", "decrypt(), exception: " + th.toString());
            return null;
        }
    }

    public static byte[] encrypt(byte[] bArr, byte[] bArr2) {
        if (bArr == null || bArr.length == 0) {
            return bArr;
        }
        try {
            return TccCryptor.encrypt(bArr, bArr2);
        } catch (Throwable th) {
            mb.o("ConverterUtil", "encrypt(), exception: " + th.toString());
            return null;
        }
    }

    public static void fy() {
        Dl = true;
    }

    private static int fz() {
        switch (e.iB()) {
            case 0:
                return 0;
            case 1:
                return 1;
            case 2:
                return 2;
            default:
                return 0;
        }
    }

    private static byte[] o(byte[] bArr) {
        if (bArr != null && bArr.length >= 4) {
            byte[] bArr2 = new byte[(bArr.length - 4)];
            System.arraycopy(bArr, 4, bArr2, 0, bArr.length - 4);
            return bArr2;
        }
        mb.s("ConverterUtil", "[shark_v4]deleteIntHeader(), mixData is not valid, len: " + (bArr == null ? -1 : bArr.length));
        return null;
    }

    private static byte[] p(byte[] bArr) {
        OutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        DeflaterOutputStream deflaterOutputStream = new DeflaterOutputStream(byteArrayOutputStream);
        try {
            deflaterOutputStream.write(bArr);
            deflaterOutputStream.finish();
            byte[] toByteArray = byteArrayOutputStream.toByteArray();
            try {
                byteArrayOutputStream.close();
                deflaterOutputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return toByteArray;
        } catch (Throwable th) {
            try {
                byteArrayOutputStream.close();
                deflaterOutputStream.close();
            } catch (IOException e2) {
                e2.printStackTrace();
            }
            throw th;
        }
    }

    private static byte[] q(byte[] bArr) {
        InputStream byteArrayInputStream = new ByteArrayInputStream(bArr);
        InflaterInputStream inflaterInputStream = new InflaterInputStream(byteArrayInputStream);
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        while (true) {
            try {
                int read = inflaterInputStream.read();
                if (read == -1) {
                    byte[] toByteArray = byteArrayOutputStream.toByteArray();
                    try {
                        byteArrayInputStream.close();
                        inflaterInputStream.close();
                        byteArrayOutputStream.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    return toByteArray;
                }
                byteArrayOutputStream.write(read);
            } catch (Exception e2) {
                mb.s("ConverterUtil", "inflater(), exception: " + e2);
                try {
                    byteArrayInputStream.close();
                    inflaterInputStream.close();
                    byteArrayOutputStream.close();
                } catch (IOException e3) {
                    e3.printStackTrace();
                }
                return null;
            } catch (Throwable th) {
                try {
                    byteArrayInputStream.close();
                    inflaterInputStream.close();
                    byteArrayOutputStream.close();
                } catch (IOException e4) {
                    e4.printStackTrace();
                }
                throw th;
            }
        }
    }

    private static boolean v(String str, String str2) {
        return (str == null && str2 == null) ? true : (str == null || str2 == null) ? false : str.equals(str2);
    }

    public static int w(Context context) {
        ln.yN = false;
        ln.q(context);
        if ((byte) 3 == ln.yO) {
            return 1;
        }
        switch (ln.yQ) {
            case (byte) 0:
                return 2;
            case (byte) 1:
                return 3;
            case (byte) 2:
                return 4;
            case (byte) 3:
                return 5;
            case (byte) 4:
                return 6;
            case (byte) 5:
                return 7;
            case (byte) 6:
                return 8;
            case (byte) 7:
                return 9;
            case (byte) 8:
                return 10;
            default:
                return 0;
        }
    }
}
