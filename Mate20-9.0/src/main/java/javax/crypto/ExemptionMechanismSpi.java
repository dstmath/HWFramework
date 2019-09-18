package javax.crypto;

import java.security.AlgorithmParameters;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.spec.AlgorithmParameterSpec;

public abstract class ExemptionMechanismSpi {
    /* access modifiers changed from: protected */
    public abstract int engineGenExemptionBlob(byte[] bArr, int i) throws ShortBufferException, ExemptionMechanismException;

    /* access modifiers changed from: protected */
    public abstract byte[] engineGenExemptionBlob() throws ExemptionMechanismException;

    /* access modifiers changed from: protected */
    public abstract int engineGetOutputSize(int i);

    /* access modifiers changed from: protected */
    public abstract void engineInit(Key key) throws InvalidKeyException, ExemptionMechanismException;

    /* access modifiers changed from: protected */
    public abstract void engineInit(Key key, AlgorithmParameters algorithmParameters) throws InvalidKeyException, InvalidAlgorithmParameterException, ExemptionMechanismException;

    /* access modifiers changed from: protected */
    public abstract void engineInit(Key key, AlgorithmParameterSpec algorithmParameterSpec) throws InvalidKeyException, InvalidAlgorithmParameterException, ExemptionMechanismException;
}
