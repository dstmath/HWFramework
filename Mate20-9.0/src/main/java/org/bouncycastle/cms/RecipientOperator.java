package org.bouncycastle.cms;

import java.io.InputStream;
import org.bouncycastle.asn1.x509.AlgorithmIdentifier;
import org.bouncycastle.operator.InputDecryptor;
import org.bouncycastle.operator.MacCalculator;
import org.bouncycastle.util.io.TeeInputStream;

public class RecipientOperator {
    private final AlgorithmIdentifier algorithmIdentifier;
    private final Object operator;

    public RecipientOperator(InputDecryptor inputDecryptor) {
        this.algorithmIdentifier = inputDecryptor.getAlgorithmIdentifier();
        this.operator = inputDecryptor;
    }

    public RecipientOperator(MacCalculator macCalculator) {
        this.algorithmIdentifier = macCalculator.getAlgorithmIdentifier();
        this.operator = macCalculator;
    }

    public InputStream getInputStream(InputStream inputStream) {
        return this.operator instanceof InputDecryptor ? ((InputDecryptor) this.operator).getInputStream(inputStream) : new TeeInputStream(inputStream, ((MacCalculator) this.operator).getOutputStream());
    }

    public byte[] getMac() {
        return ((MacCalculator) this.operator).getMac();
    }

    public boolean isMacBased() {
        return this.operator instanceof MacCalculator;
    }
}
