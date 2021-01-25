package ohos.security.keystore.provider;

import java.nio.ByteBuffer;
import java.security.AlgorithmParameters;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.AlgorithmParameterSpec;
import javax.crypto.BadPaddingException;
import javax.crypto.CipherSpi;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.ShortBufferException;

abstract class HarmonyKeyStoreCipherSpiBase extends CipherSpi {
    protected Object androidKeyStoreCipherSpi;

    /* access modifiers changed from: protected */
    @Override // javax.crypto.CipherSpi
    public abstract AlgorithmParameters engineGetParameters();

    HarmonyKeyStoreCipherSpiBase() {
    }

    /* access modifiers changed from: protected */
    @Override // javax.crypto.CipherSpi
    public final void engineInit(int i, Key key, SecureRandom secureRandom) throws InvalidKeyException {
        Throwable throwable = ReflectUtil.invoke(this.androidKeyStoreCipherSpi, new Class[]{Integer.TYPE, Key.class, SecureRandom.class}, new Object[]{Integer.valueOf(i), TransferUtils.toAndroidKey(key), secureRandom}, Void.TYPE).getThrowable();
        if (throwable instanceof InvalidKeyException) {
            throw ((InvalidKeyException) throwable);
        }
    }

    /* access modifiers changed from: protected */
    @Override // javax.crypto.CipherSpi
    public final void engineInit(int i, Key key, AlgorithmParameters algorithmParameters, SecureRandom secureRandom) throws InvalidKeyException, InvalidAlgorithmParameterException {
        Throwable throwable = ReflectUtil.invoke(this.androidKeyStoreCipherSpi, new Class[]{Integer.TYPE, Key.class, AlgorithmParameters.class, SecureRandom.class}, new Object[]{Integer.valueOf(i), TransferUtils.toAndroidKey(key), algorithmParameters, secureRandom}, Void.TYPE).getThrowable();
        if (throwable instanceof InvalidKeyException) {
            throw ((InvalidKeyException) throwable);
        } else if (throwable instanceof InvalidAlgorithmParameterException) {
            throw ((InvalidAlgorithmParameterException) throwable);
        }
    }

    /* access modifiers changed from: protected */
    @Override // javax.crypto.CipherSpi
    public final void engineInit(int i, Key key, AlgorithmParameterSpec algorithmParameterSpec, SecureRandom secureRandom) throws InvalidKeyException, InvalidAlgorithmParameterException {
        Throwable throwable = ReflectUtil.invoke(this.androidKeyStoreCipherSpi, new Class[]{Integer.TYPE, Key.class, AlgorithmParameterSpec.class, SecureRandom.class}, new Object[]{Integer.valueOf(i), TransferUtils.toAndroidKey(key), algorithmParameterSpec, secureRandom}, Void.TYPE).getThrowable();
        if (throwable instanceof InvalidKeyException) {
            throw ((InvalidKeyException) throwable);
        } else if (throwable instanceof InvalidAlgorithmParameterException) {
            throw ((InvalidAlgorithmParameterException) throwable);
        }
    }

    /* access modifiers changed from: protected */
    @Override // javax.crypto.CipherSpi
    public final byte[] engineUpdate(byte[] bArr, int i, int i2) {
        return (byte[]) ReflectUtil.invoke(this.androidKeyStoreCipherSpi, new Class[]{byte[].class, Integer.TYPE, Integer.TYPE}, new Object[]{bArr, Integer.valueOf(i), Integer.valueOf(i2)}, byte[].class).getResult();
    }

    /* access modifiers changed from: protected */
    @Override // javax.crypto.CipherSpi
    public final int engineUpdate(byte[] bArr, int i, int i2, byte[] bArr2, int i3) throws ShortBufferException {
        InvokeResult invoke = ReflectUtil.invoke(this.androidKeyStoreCipherSpi, new Class[]{byte[].class, Integer.TYPE, Integer.TYPE, byte[].class, Integer.TYPE}, new Object[]{bArr, Integer.valueOf(i), Integer.valueOf(i2), bArr2, Integer.valueOf(i3)}, Integer.class);
        Throwable throwable = invoke.getThrowable();
        if (!(throwable instanceof ShortBufferException)) {
            return ((Integer) invoke.getResult()).intValue();
        }
        throw ((ShortBufferException) throwable);
    }

    /* access modifiers changed from: protected */
    @Override // javax.crypto.CipherSpi
    public final int engineUpdate(ByteBuffer byteBuffer, ByteBuffer byteBuffer2) throws ShortBufferException {
        InvokeResult invoke = ReflectUtil.invoke(this.androidKeyStoreCipherSpi, new Class[]{ByteBuffer.class, ByteBuffer.class}, new Object[]{byteBuffer, byteBuffer2}, Integer.class);
        Throwable throwable = invoke.getThrowable();
        if (!(throwable instanceof ShortBufferException)) {
            return ((Integer) invoke.getResult()).intValue();
        }
        throw ((ShortBufferException) throwable);
    }

    /* access modifiers changed from: protected */
    @Override // javax.crypto.CipherSpi
    public final void engineUpdateAAD(byte[] bArr, int i, int i2) {
        ReflectUtil.invoke(this.androidKeyStoreCipherSpi, new Class[]{byte[].class, Integer.TYPE, Integer.TYPE}, new Object[]{bArr, Integer.valueOf(i), Integer.valueOf(i2)}, Void.TYPE);
    }

    /* access modifiers changed from: protected */
    @Override // javax.crypto.CipherSpi
    public final void engineUpdateAAD(ByteBuffer byteBuffer) {
        ReflectUtil.invoke(this.androidKeyStoreCipherSpi, new Class[]{ByteBuffer.class}, new Object[]{byteBuffer}, Void.TYPE);
    }

    /* access modifiers changed from: protected */
    @Override // javax.crypto.CipherSpi
    public final byte[] engineDoFinal(byte[] bArr, int i, int i2) throws IllegalBlockSizeException, BadPaddingException {
        InvokeResult invoke = ReflectUtil.invoke(this.androidKeyStoreCipherSpi, new Class[]{byte[].class, Integer.TYPE, Integer.TYPE}, new Object[]{bArr, Integer.valueOf(i), Integer.valueOf(i2)}, byte[].class);
        Throwable throwable = invoke.getThrowable();
        if (throwable instanceof IllegalBlockSizeException) {
            throw ((IllegalBlockSizeException) throwable);
        } else if (!(throwable instanceof BadPaddingException)) {
            return (byte[]) invoke.getResult();
        } else {
            throw ((BadPaddingException) throwable);
        }
    }

    /* access modifiers changed from: protected */
    @Override // javax.crypto.CipherSpi
    public final int engineDoFinal(byte[] bArr, int i, int i2, byte[] bArr2, int i3) throws ShortBufferException, IllegalBlockSizeException, BadPaddingException {
        InvokeResult invoke = ReflectUtil.invoke(this.androidKeyStoreCipherSpi, new Class[]{byte[].class, Integer.TYPE, Integer.TYPE, byte[].class, Integer.TYPE}, new Object[]{bArr, Integer.valueOf(i), Integer.valueOf(i2), bArr2, Integer.valueOf(i3)}, Integer.class);
        Throwable throwable = invoke.getThrowable();
        if (throwable instanceof IllegalBlockSizeException) {
            throw ((IllegalBlockSizeException) throwable);
        } else if (throwable instanceof ShortBufferException) {
            throw ((ShortBufferException) throwable);
        } else if (!(throwable instanceof BadPaddingException)) {
            return ((Integer) invoke.getResult()).intValue();
        } else {
            throw ((BadPaddingException) throwable);
        }
    }

    /* access modifiers changed from: protected */
    @Override // javax.crypto.CipherSpi
    public final int engineDoFinal(ByteBuffer byteBuffer, ByteBuffer byteBuffer2) throws ShortBufferException, IllegalBlockSizeException, BadPaddingException {
        InvokeResult invoke = ReflectUtil.invoke(this.androidKeyStoreCipherSpi, new Class[]{ByteBuffer.class, ByteBuffer.class}, new Object[]{byteBuffer, byteBuffer2}, Integer.class);
        Throwable throwable = invoke.getThrowable();
        if (throwable instanceof IllegalBlockSizeException) {
            throw ((IllegalBlockSizeException) throwable);
        } else if (throwable instanceof ShortBufferException) {
            throw ((ShortBufferException) throwable);
        } else if (!(throwable instanceof BadPaddingException)) {
            return ((Integer) invoke.getResult()).intValue();
        } else {
            throw ((BadPaddingException) throwable);
        }
    }

    /* access modifiers changed from: protected */
    @Override // javax.crypto.CipherSpi
    public final byte[] engineWrap(Key key) throws IllegalBlockSizeException, InvalidKeyException {
        InvokeResult invoke = ReflectUtil.invoke(this.androidKeyStoreCipherSpi, new Class[]{Key.class}, new Object[]{TransferUtils.toAndroidKey(key)}, byte[].class);
        Throwable throwable = invoke.getThrowable();
        if (throwable instanceof IllegalBlockSizeException) {
            throw ((IllegalBlockSizeException) throwable);
        } else if (!(throwable instanceof InvalidKeyException)) {
            return (byte[]) invoke.getResult();
        } else {
            throw ((InvalidKeyException) throwable);
        }
    }

    /* access modifiers changed from: protected */
    @Override // javax.crypto.CipherSpi
    public final Key engineUnwrap(byte[] bArr, String str, int i) throws InvalidKeyException, NoSuchAlgorithmException {
        InvokeResult invoke = ReflectUtil.invoke(this.androidKeyStoreCipherSpi, new Class[]{byte[].class, String.class, Integer.TYPE}, new Object[]{bArr, str, Integer.valueOf(i)}, Key.class);
        Throwable throwable = invoke.getThrowable();
        if (throwable instanceof NoSuchAlgorithmException) {
            throw ((NoSuchAlgorithmException) throwable);
        } else if (!(throwable instanceof InvalidKeyException)) {
            return (Key) invoke.getResult();
        } else {
            throw ((InvalidKeyException) throwable);
        }
    }

    /* access modifiers changed from: protected */
    @Override // javax.crypto.CipherSpi
    public final void engineSetMode(String str) throws NoSuchAlgorithmException {
        throw new UnsupportedOperationException();
    }

    /* access modifiers changed from: protected */
    @Override // javax.crypto.CipherSpi
    public final void engineSetPadding(String str) throws NoSuchPaddingException {
        throw new UnsupportedOperationException();
    }

    /* access modifiers changed from: protected */
    @Override // javax.crypto.CipherSpi
    public final int engineGetKeySize(Key key) throws InvalidKeyException {
        throw new UnsupportedOperationException();
    }
}
