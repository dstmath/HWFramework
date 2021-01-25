package org.bouncycastle.operator;

import java.io.OutputStream;
import org.bouncycastle.asn1.x509.AlgorithmIdentifier;
import org.bouncycastle.util.io.BufferingOutputStream;

public class BufferingContentSigner implements ContentSigner {
    private final ContentSigner contentSigner;
    private final OutputStream output;

    public BufferingContentSigner(ContentSigner contentSigner2) {
        this.contentSigner = contentSigner2;
        this.output = new BufferingOutputStream(contentSigner2.getOutputStream());
    }

    public BufferingContentSigner(ContentSigner contentSigner2, int i) {
        this.contentSigner = contentSigner2;
        this.output = new BufferingOutputStream(contentSigner2.getOutputStream(), i);
    }

    @Override // org.bouncycastle.operator.ContentSigner
    public AlgorithmIdentifier getAlgorithmIdentifier() {
        return this.contentSigner.getAlgorithmIdentifier();
    }

    @Override // org.bouncycastle.operator.ContentSigner
    public OutputStream getOutputStream() {
        return this.output;
    }

    @Override // org.bouncycastle.operator.ContentSigner
    public byte[] getSignature() {
        return this.contentSigner.getSignature();
    }
}
