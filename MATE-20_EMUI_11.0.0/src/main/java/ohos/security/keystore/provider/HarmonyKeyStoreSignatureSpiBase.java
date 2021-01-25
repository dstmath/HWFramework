package ohos.security.keystore.provider;

import java.nio.ByteBuffer;
import java.security.InvalidKeyException;
import java.security.InvalidParameterException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.SignatureException;
import java.security.SignatureSpi;

abstract class HarmonyKeyStoreSignatureSpiBase extends SignatureSpi {
    protected Object androidSignatureSpi;

    HarmonyKeyStoreSignatureSpiBase() {
    }

    /* access modifiers changed from: protected */
    @Override // java.security.SignatureSpi
    public final void engineInitSign(PrivateKey privateKey) throws InvalidKeyException {
        engineInitSign(privateKey, null);
    }

    /* access modifiers changed from: protected */
    @Override // java.security.SignatureSpi
    public final void engineInitSign(PrivateKey privateKey, SecureRandom secureRandom) throws InvalidKeyException {
        Throwable throwable = ReflectUtil.invoke(this.androidSignatureSpi, new Class[]{PrivateKey.class, SecureRandom.class}, new Object[]{TransferUtils.toAndroidPrivateKey(privateKey), secureRandom}, Void.TYPE).getThrowable();
        if (throwable instanceof InvalidKeyException) {
            throw ((InvalidKeyException) throwable);
        }
    }

    /* access modifiers changed from: protected */
    @Override // java.security.SignatureSpi
    public final void engineInitVerify(PublicKey publicKey) throws InvalidKeyException {
        Throwable throwable = ReflectUtil.invoke(this.androidSignatureSpi, new Class[]{PublicKey.class}, new Object[]{TransferUtils.toAndroidPublicKey(publicKey)}, Void.TYPE).getThrowable();
        if (throwable instanceof InvalidKeyException) {
            throw ((InvalidKeyException) throwable);
        }
    }

    /* access modifiers changed from: protected */
    @Override // java.security.SignatureSpi
    public final void engineUpdate(byte[] bArr, int i, int i2) throws SignatureException {
        Throwable throwable = ReflectUtil.invoke(this.androidSignatureSpi, new Class[]{byte[].class, Integer.TYPE, Integer.TYPE}, new Object[]{bArr, Integer.valueOf(i), Integer.valueOf(i2)}, Void.TYPE).getThrowable();
        if (throwable instanceof SignatureException) {
            throw ((SignatureException) throwable);
        }
    }

    /* access modifiers changed from: protected */
    @Override // java.security.SignatureSpi
    public final void engineUpdate(byte b) throws SignatureException {
        engineUpdate(new byte[]{b}, 0, 1);
    }

    /* access modifiers changed from: protected */
    @Override // java.security.SignatureSpi
    public final void engineUpdate(ByteBuffer byteBuffer) {
        ReflectUtil.invoke(this.androidSignatureSpi, new Class[]{ByteBuffer.class}, new Object[]{byteBuffer}, Void.TYPE);
    }

    /* access modifiers changed from: protected */
    @Override // java.security.SignatureSpi
    public final byte[] engineSign() throws SignatureException {
        InvokeResult invoke = ReflectUtil.invoke(this.androidSignatureSpi, new Class[0], new Object[0], byte[].class);
        Throwable throwable = invoke.getThrowable();
        if (!(throwable instanceof SignatureException)) {
            return (byte[]) invoke.getResult();
        }
        throw ((SignatureException) throwable);
    }

    /* access modifiers changed from: protected */
    @Override // java.security.SignatureSpi
    public final boolean engineVerify(byte[] bArr) throws SignatureException {
        InvokeResult invoke = ReflectUtil.invoke(this.androidSignatureSpi, new Class[]{byte[].class}, new Object[]{bArr}, Boolean.class);
        Throwable throwable = invoke.getThrowable();
        if (!(throwable instanceof SignatureException)) {
            return ((Boolean) invoke.getResult()).booleanValue();
        }
        throw ((SignatureException) throwable);
    }

    /* access modifiers changed from: protected */
    @Override // java.security.SignatureSpi
    public final boolean engineVerify(byte[] bArr, int i, int i2) throws SignatureException {
        return engineVerify(ArrayUtils.subarray(bArr, i, i2));
    }

    /* access modifiers changed from: protected */
    @Override // java.security.SignatureSpi
    @Deprecated
    public final Object engineGetParameter(String str) throws InvalidParameterException {
        throw new InvalidParameterException();
    }

    /* access modifiers changed from: protected */
    @Override // java.security.SignatureSpi
    @Deprecated
    public final void engineSetParameter(String str, Object obj) throws InvalidParameterException {
        throw new InvalidParameterException();
    }
}
