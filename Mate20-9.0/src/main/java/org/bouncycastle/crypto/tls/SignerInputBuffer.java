package org.bouncycastle.crypto.tls;

import java.io.ByteArrayOutputStream;
import org.bouncycastle.crypto.Signer;

class SignerInputBuffer extends ByteArrayOutputStream {
    SignerInputBuffer() {
    }

    /* access modifiers changed from: package-private */
    public void updateSigner(Signer signer) {
        signer.update(this.buf, 0, this.count);
    }
}
