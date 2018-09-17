package tmsdkobf;

import android.content.Context;
import android.content.pm.PackageInfo;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.HashMap;
import java.util.Properties;
import tmsdk.common.TMSDKContext;
import tmsdk.common.creator.ManagerCreatorC;
import tmsdk.common.utils.l;

/* compiled from: Unknown */
final class jv {
    private static final HashMap<String, String> uy = null;
    private static final long uz = 0;
    private Context mContext;
    private Properties uA;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: tmsdkobf.jv.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: tmsdkobf.jv.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: tmsdkobf.jv.<clinit>():void");
    }

    jv(Properties properties, Context context) {
        this.uA = properties;
        this.mContext = context;
    }

    private String bG(String str) {
        PackageInfo packageInfo;
        String str2 = null;
        try {
            packageInfo = this.mContext.getPackageManager().getPackageInfo(str, 64);
        } catch (Exception e) {
            e.printStackTrace();
            packageInfo = null;
        }
        if (packageInfo != null) {
            InputStream byteArrayInputStream = new ByteArrayInputStream(packageInfo.signatures[0].toByteArray());
            try {
                str2 = nb.p(((X509Certificate) CertificateFactory.getInstance("X.509").generateCertificate(byteArrayInputStream)).getEncoded());
                byteArrayInputStream.close();
            } catch (CertificateException e2) {
                e2.printStackTrace();
            } catch (IOException e3) {
                e3.printStackTrace();
            }
        }
        return str2;
    }

    int a(di diVar) {
        return ((qt) ManagerCreatorC.getManager(qt.class)).a(new dj(bG(this.mContext.getPackageName()), l.dk(TMSDKContext.getStrFromEnvMap(TMSDKContext.CON_CHANNEL))), diVar);
    }

    public boolean cD() {
        String packageName = this.mContext.getPackageName();
        String bG = bG(packageName);
        if (bG == null) {
            return true;
        }
        boolean equals = bG.equals(this.uA.getProperty("signature").toUpperCase().trim());
        if (equals) {
            new nc("tms").a("reportsig", packageName + ":" + bG, true);
        }
        return equals;
    }

    public String cE() {
        return this.uA.getProperty("lc_sdk_channel");
    }

    public String cF() {
        return this.uA.getProperty("lc_sdk_pid");
    }

    public long cG() {
        return Long.parseLong(this.uA.getProperty("expiry.seconds", Long.toString(uz)));
    }
}
