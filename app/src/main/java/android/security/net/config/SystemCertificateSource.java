package android.security.net.config;

import android.os.Environment;
import android.os.UserHandle;
import java.io.File;
import java.security.cert.X509Certificate;
import java.util.Set;

public final class SystemCertificateSource extends DirectoryCertificateSource {
    private final File mUserRemovedCaDir;

    private static class NoPreloadHolder {
        private static final SystemCertificateSource INSTANCE = null;

        static {
            /* JADX: method processing error */
/*
            Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.security.net.config.SystemCertificateSource.NoPreloadHolder.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:263)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.security.net.config.SystemCertificateSource.NoPreloadHolder.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 8 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 9 more
*/
            /*
            // Can't load method instructions.
            */
            throw new UnsupportedOperationException("Method not decompiled: android.security.net.config.SystemCertificateSource.NoPreloadHolder.<clinit>():void");
        }

        private NoPreloadHolder() {
        }
    }

    /* synthetic */ SystemCertificateSource(SystemCertificateSource systemCertificateSource) {
        this();
    }

    public /* bridge */ /* synthetic */ Set findAllByIssuerAndSignature(X509Certificate cert) {
        return super.findAllByIssuerAndSignature(cert);
    }

    public /* bridge */ /* synthetic */ X509Certificate findByIssuerAndSignature(X509Certificate cert) {
        return super.findByIssuerAndSignature(cert);
    }

    public /* bridge */ /* synthetic */ X509Certificate findBySubjectAndPublicKey(X509Certificate cert) {
        return super.findBySubjectAndPublicKey(cert);
    }

    public /* bridge */ /* synthetic */ Set getCertificates() {
        return super.getCertificates();
    }

    public /* bridge */ /* synthetic */ void handleTrustStorageUpdate() {
        super.handleTrustStorageUpdate();
    }

    private SystemCertificateSource() {
        super(new File(System.getenv("ANDROID_ROOT") + "/etc/security/cacerts"));
        this.mUserRemovedCaDir = new File(Environment.getUserConfigDirectory(UserHandle.myUserId()), "cacerts-removed");
    }

    public static SystemCertificateSource getInstance() {
        return NoPreloadHolder.INSTANCE;
    }

    protected boolean isCertMarkedAsRemoved(String caFile) {
        return new File(this.mUserRemovedCaDir, caFile).exists();
    }
}
