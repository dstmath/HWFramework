package tmsdkobf;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.security.GeneralSecurityException;
import java.security.Key;
import java.security.interfaces.RSAPublicKey;
import java.util.Calendar;
import java.util.Properties;
import javax.crypto.Cipher;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.DESKeySpec;
import tmsdk.common.TMSDKContext;
import tmsdk.common.module.aresengine.SystemCallLogFilterConsts;
import tmsdk.common.utils.b;
import tmsdk.common.utils.d;
import tmsdk.fg.module.spacemanager.SpaceManager;
import tmsdk.fg.module.urlcheck.IUrlMonitorCallback;

/* compiled from: Unknown */
public final class jw {
    private static final byte[] uB = null;
    private static volatile jw uC;
    public static String uF;
    private jt uD;
    private Calendar uE;
    private jv uG;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: tmsdkobf.jw.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: tmsdkobf.jw.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 7 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 8 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: tmsdkobf.jw.<clinit>():void");
    }

    private jw() {
        this.uE = Calendar.getInstance();
        load();
    }

    private static long a(String str, RSAPublicKey rSAPublicKey) throws Exception {
        byte[] decode = b.decode(str, 0);
        int i = ByteBuffer.wrap(decode).getInt();
        Cipher instance = Cipher.getInstance("RSA/ECB/PKCS1Padding");
        instance.init(2, rSAPublicKey);
        byte[] doFinal = instance.doFinal(decode, 4, i);
        Cipher instance2 = Cipher.getInstance("DES/ECB/PKCS5Padding");
        instance2.init(2, SecretKeyFactory.getInstance("DES").generateSecret(new DESKeySpec(doFinal)));
        return Long.parseLong(new String(instance2.doFinal(decode, i + 4, (decode.length - 4) - i)), 16);
    }

    private static byte[] a(byte[] bArr, byte[] bArr2) {
        try {
            Key generateSecret = SecretKeyFactory.getInstance("DES").generateSecret(new DESKeySpec(bArr2));
            Cipher instance = Cipher.getInstance("DES/ECB/NoPadding");
            instance.init(2, generateSecret);
            return instance.doFinal(bArr);
        } catch (GeneralSecurityException e) {
            e.printStackTrace();
            return null;
        }
    }

    private byte[] aY(int i) {
        InputStream inputStream = null;
        try {
            inputStream = TMSDKContext.getApplicaionContext().getAssets().open(i != 0 ? "licence" + i + ".conf" : "licence.conf");
            byte[] bArr = new byte[inputStream.available()];
            inputStream.read(bArr);
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            return bArr;
        } catch (Throwable e2) {
            throw new RuntimeException(e2);
        } catch (Throwable th) {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e3) {
                    e3.printStackTrace();
                }
            }
        }
    }

    public static synchronized jw cH() {
        jw jwVar;
        synchronized (jw.class) {
            if (uC == null) {
                uC = new jw();
            }
            jwVar = uC;
        }
        return jwVar;
    }

    private boolean cI() {
        try {
            String cF = this.uG.cF();
            int parseInt = Integer.parseInt(this.uG.cE());
            if (dc.hn.toString().equals(cF) && parseInt == 999001) {
                return true;
            }
        } catch (Throwable e) {
            d.a("TMSLicenceManager", "isQQPimSecure", e);
        }
        return false;
    }

    private final void cK() {
        long a;
        int i;
        Calendar instance;
        boolean z = false;
        String string = new nc("licence").getString("expiry.enc_seconds", null);
        if (string != null) {
            try {
                a = a(string, cL());
            } catch (Exception e) {
                e.printStackTrace();
            }
            if (a == -1) {
                a = this.uG.cG();
            }
            if (System.currentTimeMillis() / 1000 >= a) {
                i = 1;
            } else {
                boolean z2 = false;
            }
            if (i == 0) {
                z = true;
            }
            instance = Calendar.getInstance();
            instance.setTimeInMillis(a * 1000);
            d.e("LicMan", "expirySeconds=" + a + "(" + instance.get(1) + "-" + instance.get(2) + "-" + instance.get(5) + ") expired=" + z);
            this.uD = new jt(z);
        }
        a = -1;
        if (a == -1) {
            a = this.uG.cG();
        }
        if (System.currentTimeMillis() / 1000 >= a) {
            boolean z22 = false;
        } else {
            i = 1;
        }
        if (i == 0) {
            z = true;
        }
        instance = Calendar.getInstance();
        instance.setTimeInMillis(a * 1000);
        d.e("LicMan", "expirySeconds=" + a + "(" + instance.get(1) + "-" + instance.get(2) + "-" + instance.get(5) + ") expired=" + z);
        this.uD = new jt(z);
    }

    private static RSAPublicKey cL() {
        return ju.h(b.decode("LS0tLS1CRUdJTiBDRVJUSUZJQ0FURS0tLS0tCk1JSUM5ekNDQWQ4Q0NRRGlsbUFjTWxiczVEQU5C\nZ2txaGtpRzl3MEJBUVVGQURCK01Rc3dDUVlEVlFRR0V3SkQKVGpFTE1Ba0dBMVVFQ0JNQ1IwUXhD\nekFKQmdOVkJBY1RBa2RhTVJJd0VBWURWUVFLRkFsMFpXTUlibU5sYm5ReApDekFKQmdOVkJBc1RB\nak5ITVE0d0RBWURWUVFERXdWdlltRnRZVEVrTUNJR0NTcUdTSWIzRFFFSkFSWVZiMkpoCmJXRjZa\nVzVuUUhSbGJtTmxiblF1WTI5dE1CNFhEVEV4TVRFeE5qRXhNVGN4TjFvWERURXlNREl5TkRFeE1U\nY3gKTjFvd2dZQXhDekFKQmdOVkJBWVRBa05PTVFzd0NRWURWUVFJRXdKSFJERUxNQWtHQTFVRUJ4\nTUNSMW94RURBTwpCZ05WQkFvVEIzUmxibU5sYm5ReEN6QUpCZ05WQkFzVEFqTkhNUkl3RUFZRFZR\nUURFd2x2WW1GdFlYcGxibWN4CkpEQWlCZ2txaGtpRzl3MEJDUUVXRlc5aVlXMWhlbVZ1WjBCMFpX\nNWpaVzUwTG1OdmJUQ0JuekFOQmdrcWhraUcKOXcwQkFRRUZBQU9CalFBd2dZa0NnWUVBd1kvV3FI\nV2NlRERkSm16anI3TlpSeS9qTllwS1NzVzExZngxaTIrQwpxTUE3NTJXb1d1bDZuSTB1MGZkWitk\nUzVUandRNkU0Qm13dXduVTVnQmJYK1VzQ2VHRHZaQVhQc045UEVWYnZTCkcvR25YclQrcTI2VUpP\nNHcrd3VNdmk5YWxkZHhhbkNKeXJ2ZWQ2NUdvMXhXUEErWGNHaVQxMndubjZtUHhyMnUKcVEwQ0F3\nRUFBVEFOQmdrcWhraUc5dzBCQVFVRkFBT0NBUUVBblpzV3FpSmV5SC9sT0prSWN6L2ZidDN3MXFL\nRApGTXJ5cFVHVFN6Z3NONWNaMW9yOGlvVG5ENGRLaDdJN2ttbDRpcGNvMDF0enc2MGhLYUtwNG9G\nMnYrMEs2NGZDCnBEMG9EUlkrOGoyK2RsMmNxeHBsT0FYdDc1RWFKNW40MG1DZDdTN0VBS0d2Z2Na\naVhyV0Z1eUtCL2QvNTh3Qm4KOEFGUVJhTnBySXNOSHpxMkMwL0JXR1pTSnJicmhOWExFY0ZtL0Ru\nTG14ZEVNYWxPSXhnSkhGcEFOS2tadXBzdgo0L0lDVFhSL0RJaURjbXJjbDFkNkc2VmgyaUcwaS9v\nRDBHQnBMZlFPcEF0Vmx6Y2lxZnBsTkphcnpRUTZUVXRyCm5GRmVNVDNDc2t5VGJwYnp1R2dDdUxj\nQVR3cnRQd1BOOWZzQXYrSjRJZm0rZUNVVDVnZlorMSsyNHc9PQotLS0tLUVORCBDRVJUSUZJQ0FU\nRS0tLS0tCg==\n".getBytes(), 0));
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void load() {
        Properties properties;
        uF = new String(uB);
        int i = 0;
        while (true) {
            try {
                Object aY = aY(i);
                if (aY == null) {
                    break;
                }
                RSAPublicKey cL = cL();
                Object obj = new byte[SystemCallLogFilterConsts.NOTIFY_SHORT_CALL];
                System.arraycopy(aY, 0, obj, 0, obj.length);
                byte[] a = ju.a(obj, cL);
                if (a == null) {
                    break;
                }
                byte[] bArr = new byte[(aY.length - 128)];
                System.arraycopy(aY, SystemCallLogFilterConsts.NOTIFY_SHORT_CALL, bArr, 0, bArr.length);
                byte[] a2 = a(bArr, a);
                if (a2 != null) {
                    InputStream byteArrayInputStream = new ByteArrayInputStream(a2);
                    properties = new Properties();
                    try {
                        properties.load(byteArrayInputStream);
                        try {
                            byteArrayInputStream.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    } catch (IOException e2) {
                        e2.printStackTrace();
                        break;
                    } catch (Throwable th) {
                        try {
                            byteArrayInputStream.close();
                        } catch (IOException e3) {
                            e3.printStackTrace();
                        }
                    }
                    this.uG = new jv(properties, TMSDKContext.getApplicaionContext());
                    if (this.uG.cD()) {
                        i++;
                    } else {
                        cK();
                        this.uE.setTimeInMillis(System.currentTimeMillis());
                        return;
                    }
                }
                return;
            } catch (RuntimeException e4) {
                throw new RuntimeException("Invaild signature! Please contact TMS(Tencent Mobile Secure) group.");
            }
        }
        this.uG = new jv(properties, TMSDKContext.getApplicaionContext());
        if (this.uG.cD()) {
            cK();
            this.uE.setTimeInMillis(System.currentTimeMillis());
            return;
        }
        i++;
    }

    public void bH(String str) {
        d.e("LicMan", "strTimeSec=" + str);
        mm.a(getClass());
        new nc("licence").a("expiry.enc_seconds", str, true);
        cK();
    }

    public final boolean cD() {
        return this.uG.cD();
    }

    public String cE() {
        return this.uG.cE();
    }

    public String cF() {
        return this.uG.cF();
    }

    public boolean cJ() {
        boolean z = false;
        if (cI()) {
            return true;
        }
        di diVar = new di();
        int a = this.uG.a(diVar);
        if (diVar.ir == null || diVar.ir.iQ == null || diVar.ir.iQ.length() == 0) {
            switch (a) {
                case IUrlMonitorCallback.DES_ID_HOBBYHORSE /*1*/:
                    return true;
                default:
                    if (!cl()) {
                        z = true;
                    }
                    return z;
            }
        }
        boolean z2;
        switch (a) {
            case SpaceManager.ERROR_CODE_PARAM /*-1*/:
                bH(diVar.ir.iQ);
                z2 = true;
                break;
            case SpaceManager.ERROR_CODE_OK /*0*/:
                throw new RuntimeException("Unknown licence! Please contact TMS(Tencent Mobile Secure) group.");
            case IUrlMonitorCallback.DES_ID_HOBBYHORSE /*1*/:
                bH(diVar.ir.iQ);
                z2 = false;
                break;
            default:
                z2 = cl();
                break;
        }
        if (!z2) {
            z = true;
        }
        return z;
    }

    public boolean cl() {
        if (cI()) {
            return false;
        }
        Calendar instance = Calendar.getInstance();
        instance.setTimeInMillis(System.currentTimeMillis());
        if (instance.get(1) != this.uE.get(1) || instance.get(6) != this.uE.get(6)) {
            cK();
        }
        this.uE.setTimeInMillis(System.currentTimeMillis());
        return this.uD.cl();
    }
}
