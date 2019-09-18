package java.security;

import java.io.IOException;
import java.security.spec.AlgorithmParameterSpec;
import java.security.spec.InvalidParameterSpecException;

public abstract class AlgorithmParametersSpi {
    /* access modifiers changed from: protected */
    public abstract byte[] engineGetEncoded() throws IOException;

    /* access modifiers changed from: protected */
    public abstract byte[] engineGetEncoded(String str) throws IOException;

    /* access modifiers changed from: protected */
    public abstract <T extends AlgorithmParameterSpec> T engineGetParameterSpec(Class<T> cls) throws InvalidParameterSpecException;

    /* access modifiers changed from: protected */
    public abstract void engineInit(AlgorithmParameterSpec algorithmParameterSpec) throws InvalidParameterSpecException;

    /* access modifiers changed from: protected */
    public abstract void engineInit(byte[] bArr) throws IOException;

    /* access modifiers changed from: protected */
    public abstract void engineInit(byte[] bArr, String str) throws IOException;

    /* access modifiers changed from: protected */
    public abstract String engineToString();
}
