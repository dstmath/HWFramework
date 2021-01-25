package org.bouncycastle.operator;

import org.bouncycastle.asn1.x509.AlgorithmIdentifier;

public class GenericKey {
    private AlgorithmIdentifier algorithmIdentifier;
    private Object representation;

    public GenericKey(Object obj) {
        this.algorithmIdentifier = null;
        this.representation = obj;
    }

    protected GenericKey(AlgorithmIdentifier algorithmIdentifier2, Object obj) {
        this.algorithmIdentifier = algorithmIdentifier2;
        this.representation = obj;
    }

    public GenericKey(AlgorithmIdentifier algorithmIdentifier2, byte[] bArr) {
        this.algorithmIdentifier = algorithmIdentifier2;
        this.representation = bArr;
    }

    public AlgorithmIdentifier getAlgorithmIdentifier() {
        return this.algorithmIdentifier;
    }

    public Object getRepresentation() {
        return this.representation;
    }
}
