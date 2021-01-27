package huawei.android.security.securityprofile;

import java.nio.ByteBuffer;

/* access modifiers changed from: package-private */
public class SignatureInformation {
    public final long apkSigningBlockPosition;
    public final long centralDirectoryPosition;
    public final ByteBuffer eocdContent;
    public final long eocdPosition;
    public final ByteBuffer signatureBlockContent;

    SignatureInformation(ByteBuffer eocdContent2, long eocdPosition2, long centralDirectoryPosition2, long apkSigningBlockPosition2, ByteBuffer signatureBlockContent2) {
        this.eocdContent = eocdContent2;
        this.eocdPosition = eocdPosition2;
        this.centralDirectoryPosition = centralDirectoryPosition2;
        this.apkSigningBlockPosition = apkSigningBlockPosition2;
        this.signatureBlockContent = signatureBlockContent2;
    }
}
