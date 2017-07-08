package javax.crypto;

import java.security.AlgorithmParameters;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.spec.AlgorithmParameterSpec;

public abstract class ExemptionMechanismSpi {
    protected abstract int engineGenExemptionBlob(byte[] bArr, int i) throws ShortBufferException, ExemptionMechanismException;

    protected abstract byte[] engineGenExemptionBlob() throws ExemptionMechanismException;

    protected abstract int engineGetOutputSize(int i);

    protected abstract void engineInit(Key key) throws InvalidKeyException, ExemptionMechanismException;

    protected abstract void engineInit(Key key, AlgorithmParameters algorithmParameters) throws InvalidKeyException, InvalidAlgorithmParameterException, ExemptionMechanismException;

    protected abstract void engineInit(Key key, AlgorithmParameterSpec algorithmParameterSpec) throws InvalidKeyException, InvalidAlgorithmParameterException, ExemptionMechanismException;
}
