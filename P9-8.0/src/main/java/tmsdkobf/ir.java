package tmsdkobf;

import android.content.Context;
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
import tmsdk.common.utils.b;

public final class ir {
    private static final byte[] rR = new byte[]{(byte) 99, (byte) 111, (byte) 109, (byte) 46, (byte) 116, (byte) 101, (byte) 110, (byte) 99, (byte) 101, (byte) 110, (byte) 116, (byte) 46, (byte) 113, (byte) 113, (byte) 112, (byte) 105, (byte) 109, (byte) 115, (byte) 101, (byte) 99, (byte) 117, (byte) 114, (byte) 101};
    private static volatile ir rS = null;
    public static final String rV = new String(rR);
    private io rT;
    private Calendar rU = Calendar.getInstance();
    private iq rW;

    private ir() {
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

    private byte[] af(int i) {
        InputStream inputStream = null;
        try {
            Context currentContext = TMSDKContext.getCurrentContext();
            if (currentContext == null) {
                currentContext = TMSDKContext.getApplicaionContext();
            }
            inputStream = currentContext.getAssets().open(i != 0 ? "licence" + i + ".conf" : "licence.conf");
            byte[] bArr = new byte[inputStream.available()];
            inputStream.read(bArr);
            byte[] bArr2 = bArr;
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

    public static synchronized ir bU() {
        ir irVar;
        synchronized (ir.class) {
            if (rS == null) {
                rS = new ir();
            }
            irVar = rS;
        }
        return irVar;
    }

    private final void bW() {
        String string = new md("licence").getString("expiry.enc_seconds", null);
        long j = -1;
        if (string != null) {
            try {
                j = a(string, bX());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if (j == -1) {
            j = this.rW.bT();
        }
        boolean z = (((System.currentTimeMillis() / 1000) > j ? 1 : ((System.currentTimeMillis() / 1000) == j ? 0 : -1)) < 0 ? 1 : 0) == 0;
        Calendar instance = Calendar.getInstance();
        instance.setTimeInMillis(j * 1000);
        mb.d("LicMan", "expirySeconds=" + j + "(" + instance.get(1) + "-" + instance.get(2) + "-" + instance.get(5) + ") expired=" + z);
        this.rT = new io(z);
    }

    private static RSAPublicKey bX() {
        return ip.h(b.decode("LS0tLS1CRUdJTiBDRVJUSUZJQ0FURS0tLS0tCk1JSUM5ekNDQWQ4Q0NRRGlsbUFjTWxiczVEQU5C\nZ2txaGtpRzl3MEJBUVVGQURCK01Rc3dDUVlEVlFRR0V3SkQKVGpFTE1Ba0dBMVVFQ0JNQ1IwUXhD\nekFKQmdOVkJBY1RBa2RhTVJJd0VBWURWUVFLRkFsMFpXTUlibU5sYm5ReApDekFKQmdOVkJBc1RB\nak5ITVE0d0RBWURWUVFERXdWdlltRnRZVEVrTUNJR0NTcUdTSWIzRFFFSkFSWVZiMkpoCmJXRjZa\nVzVuUUhSbGJtTmxiblF1WTI5dE1CNFhEVEV4TVRFeE5qRXhNVGN4TjFvWERURXlNREl5TkRFeE1U\nY3gKTjFvd2dZQXhDekFKQmdOVkJBWVRBa05PTVFzd0NRWURWUVFJRXdKSFJERUxNQWtHQTFVRUJ4\nTUNSMW94RURBTwpCZ05WQkFvVEIzUmxibU5sYm5ReEN6QUpCZ05WQkFzVEFqTkhNUkl3RUFZRFZR\nUURFd2x2WW1GdFlYcGxibWN4CkpEQWlCZ2txaGtpRzl3MEJDUUVXRlc5aVlXMWhlbVZ1WjBCMFpX\nNWpaVzUwTG1OdmJUQ0JuekFOQmdrcWhraUcKOXcwQkFRRUZBQU9CalFBd2dZa0NnWUVBd1kvV3FI\nV2NlRERkSm16anI3TlpSeS9qTllwS1NzVzExZngxaTIrQwpxTUE3NTJXb1d1bDZuSTB1MGZkWitk\nUzVUandRNkU0Qm13dXduVTVnQmJYK1VzQ2VHRHZaQVhQc045UEVWYnZTCkcvR25YclQrcTI2VUpP\nNHcrd3VNdmk5YWxkZHhhbkNKeXJ2ZWQ2NUdvMXhXUEErWGNHaVQxMndubjZtUHhyMnUKcVEwQ0F3\nRUFBVEFOQmdrcWhraUc5dzBCQVFVRkFBT0NBUUVBblpzV3FpSmV5SC9sT0prSWN6L2ZidDN3MXFL\nRApGTXJ5cFVHVFN6Z3NONWNaMW9yOGlvVG5ENGRLaDdJN2ttbDRpcGNvMDF0enc2MGhLYUtwNG9G\nMnYrMEs2NGZDCnBEMG9EUlkrOGoyK2RsMmNxeHBsT0FYdDc1RWFKNW40MG1DZDdTN0VBS0d2Z2Na\naVhyV0Z1eUtCL2QvNTh3Qm4KOEFGUVJhTnBySXNOSHpxMkMwL0JXR1pTSnJicmhOWExFY0ZtL0Ru\nTG14ZEVNYWxPSXhnSkhGcEFOS2tadXBzdgo0L0lDVFhSL0RJaURjbXJjbDFkNkc2VmgyaUcwaS9v\nRDBHQnBMZlFPcEF0Vmx6Y2lxZnBsTkphcnpRUTZUVXRyCm5GRmVNVDNDc2t5VGJwYnp1R2dDdUxj\nQVR3cnRQd1BOOWZzQXYrSjRJZm0rZUNVVDVnZlorMSsyNHc9PQotLS0tLUVORCBDRVJUSUZJQ0FU\nRS0tLS0tCg==\n".getBytes(), 0));
    }

    private static byte[] c(byte[] bArr, byte[] bArr2) {
        byte[] bArr3 = null;
        try {
            Key generateSecret = SecretKeyFactory.getInstance("DES").generateSecret(new DESKeySpec(bArr2));
            Cipher instance = Cipher.getInstance("DES/ECB/NoPadding");
            instance.init(2, generateSecret);
            return instance.doFinal(bArr);
        } catch (GeneralSecurityException e) {
            e.printStackTrace();
            return bArr3;
        }
    }

    private void load() {
        int i = 0;
        while (true) {
            try {
                Object af = af(i);
                if (af != null) {
                    RSAPublicKey bX = bX();
                    Object obj = new byte[128];
                    System.arraycopy(af, 0, obj, 0, obj.length);
                    byte[] a = ip.a(obj, bX);
                    if (a != null) {
                        Object obj2 = new byte[(af.length - 128)];
                        System.arraycopy(af, 128, obj2, 0, obj2.length);
                        byte[] c = c(obj2, a);
                        if (c != null) {
                            InputStream byteArrayInputStream = new ByteArrayInputStream(c);
                            Properties properties = new Properties();
                            try {
                                properties.load(byteArrayInputStream);
                                try {
                                    byteArrayInputStream.close();
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            } catch (IOException e2) {
                                e2.printStackTrace();
                                try {
                                    byteArrayInputStream.close();
                                } catch (IOException e22) {
                                    e22.printStackTrace();
                                }
                            } catch (Throwable th) {
                                try {
                                    byteArrayInputStream.close();
                                } catch (IOException e3) {
                                    e3.printStackTrace();
                                }
                                throw th;
                            }
                            this.rW = new iq(properties, TMSDKContext.getApplicaionContext());
                            if (this.rW.bS()) {
                                bW();
                                this.rU.setTimeInMillis(System.currentTimeMillis());
                                return;
                            }
                            i++;
                        } else {
                            return;
                        }
                    }
                    throw new RuntimeException("RSA decrypt error.");
                }
                throw new RuntimeException("Certification file is missing! Please contact TMS(Tencent Mobile Secure) group.");
            } catch (RuntimeException e4) {
                throw new RuntimeException("loadLicence Invaild signature! Please contact TMS(Tencent Mobile Secure) group.");
            }
        }
    }

    public boolean bE() {
        return false;
    }

    public String bQ() {
        return this.rW.bQ();
    }

    public final boolean bS() {
        return this.rW.bS();
    }

    public boolean bV() {
        return true;
    }
}
