package android.media;

import android.hardware.cas.V1_0.IDescramblerBase;
import android.media.MediaCas;
import android.media.MediaCasException;
import android.media.MediaCodec;
import android.os.IHwBinder;
import android.os.RemoteException;
import android.os.ServiceSpecificException;
import android.util.Log;
import java.nio.ByteBuffer;

public final class MediaDescrambler implements AutoCloseable {
    public static final byte SCRAMBLE_CONTROL_EVEN_KEY = 2;
    public static final byte SCRAMBLE_CONTROL_ODD_KEY = 3;
    public static final byte SCRAMBLE_CONTROL_RESERVED = 1;
    public static final byte SCRAMBLE_CONTROL_UNSCRAMBLED = 0;
    public static final byte SCRAMBLE_FLAG_PES_HEADER = 1;
    private static final String TAG = "MediaDescrambler";
    private IDescramblerBase mIDescrambler;
    private long mNativeContext;

    private final native int native_descramble(byte b, byte b2, int i, int[] iArr, int[] iArr2, ByteBuffer byteBuffer, int i2, int i3, ByteBuffer byteBuffer2, int i4, int i5) throws RemoteException;

    private static final native void native_init();

    private final native void native_release();

    private final native void native_setup(IHwBinder iHwBinder);

    private final void validateInternalStates() {
        if (this.mIDescrambler == null) {
            throw new IllegalStateException();
        }
    }

    private final void cleanupAndRethrowIllegalState() {
        this.mIDescrambler = null;
        throw new IllegalStateException();
    }

    public MediaDescrambler(int CA_system_id) throws MediaCasException.UnsupportedCasException {
        try {
            this.mIDescrambler = MediaCas.getService().createDescrambler(CA_system_id);
            if (this.mIDescrambler == null) {
                throw new MediaCasException.UnsupportedCasException("Unsupported CA_system_id " + CA_system_id);
            }
        } catch (Exception e) {
            Log.e(TAG, "Failed to create descrambler: " + e);
            this.mIDescrambler = null;
            if (this.mIDescrambler == null) {
                throw new MediaCasException.UnsupportedCasException("Unsupported CA_system_id " + CA_system_id);
            }
        } catch (Throwable th) {
            if (this.mIDescrambler == null) {
                throw new MediaCasException.UnsupportedCasException("Unsupported CA_system_id " + CA_system_id);
            }
            throw th;
        }
        native_setup(this.mIDescrambler.asBinder());
    }

    /* access modifiers changed from: package-private */
    public IHwBinder getBinder() {
        validateInternalStates();
        return this.mIDescrambler.asBinder();
    }

    public final boolean requiresSecureDecoderComponent(String mime) {
        validateInternalStates();
        try {
            return this.mIDescrambler.requiresSecureDecoderComponent(mime);
        } catch (RemoteException e) {
            cleanupAndRethrowIllegalState();
            return true;
        }
    }

    public final void setMediaCasSession(MediaCas.Session session) {
        validateInternalStates();
        try {
            MediaCasStateException.throwExceptionIfNeeded(this.mIDescrambler.setMediaCasSession(session.mSessionId));
        } catch (RemoteException e) {
            cleanupAndRethrowIllegalState();
        }
    }

    public final int descramble(ByteBuffer srcBuf, ByteBuffer dstBuf, MediaCodec.CryptoInfo cryptoInfo) {
        validateInternalStates();
        if (cryptoInfo.numSubSamples <= 0) {
            throw new IllegalArgumentException("Invalid CryptoInfo: invalid numSubSamples=" + cryptoInfo.numSubSamples);
        } else if (cryptoInfo.numBytesOfClearData == null && cryptoInfo.numBytesOfEncryptedData == null) {
            throw new IllegalArgumentException("Invalid CryptoInfo: clearData and encryptedData size arrays are both null!");
        } else if (cryptoInfo.numBytesOfClearData != null && cryptoInfo.numBytesOfClearData.length < cryptoInfo.numSubSamples) {
            throw new IllegalArgumentException("Invalid CryptoInfo: numBytesOfClearData is too small!");
        } else if (cryptoInfo.numBytesOfEncryptedData != null && cryptoInfo.numBytesOfEncryptedData.length < cryptoInfo.numSubSamples) {
            throw new IllegalArgumentException("Invalid CryptoInfo: numBytesOfEncryptedData is too small!");
        } else if (cryptoInfo.key == null || cryptoInfo.key.length != 16) {
            throw new IllegalArgumentException("Invalid CryptoInfo: key array is invalid!");
        } else {
            try {
                return native_descramble(cryptoInfo.key[0], cryptoInfo.key[1], cryptoInfo.numSubSamples, cryptoInfo.numBytesOfClearData, cryptoInfo.numBytesOfEncryptedData, srcBuf, srcBuf.position(), srcBuf.limit(), dstBuf, dstBuf.position(), dstBuf.limit());
            } catch (ServiceSpecificException e) {
                MediaCasStateException.throwExceptionIfNeeded(e.errorCode, e.getMessage());
                return -1;
            } catch (RemoteException e2) {
                cleanupAndRethrowIllegalState();
                return -1;
            }
        }
    }

    @Override // java.lang.AutoCloseable
    public void close() {
        IDescramblerBase iDescramblerBase = this.mIDescrambler;
        if (iDescramblerBase != null) {
            try {
                iDescramblerBase.release();
            } catch (RemoteException e) {
            } catch (Throwable th) {
                this.mIDescrambler = null;
                throw th;
            }
            this.mIDescrambler = null;
        }
        native_release();
    }

    /* access modifiers changed from: protected */
    @Override // java.lang.Object
    public void finalize() {
        close();
    }

    static {
        System.loadLibrary("media_jni");
        native_init();
    }
}
