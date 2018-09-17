package android.media;

import android.media.MediaCas.Session;
import android.media.MediaCasException.UnsupportedCasException;
import android.media.MediaCodec.CryptoInfo;
import android.os.IBinder;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import android.os.RemoteException;
import android.os.ServiceSpecificException;
import android.util.Log;
import java.nio.ByteBuffer;

public final class MediaDescrambler implements AutoCloseable {
    private static final String TAG = "MediaDescrambler";
    private IDescrambler mIDescrambler;
    private long mNativeContext;

    static class DescrambleInfo implements Parcelable {
        public static final Creator<DescrambleInfo> CREATOR = new Creator<DescrambleInfo>() {
            public DescrambleInfo createFromParcel(Parcel in) {
                return new DescrambleInfo(in, null);
            }

            public DescrambleInfo[] newArray(int size) {
                return new DescrambleInfo[size];
            }
        };

        /* synthetic */ DescrambleInfo(Parcel in, DescrambleInfo -this1) {
            this(in);
        }

        private DescrambleInfo() {
        }

        private DescrambleInfo(Parcel in) {
        }

        public int describeContents() {
            return 0;
        }

        public void writeToParcel(Parcel dest, int flags) {
        }
    }

    private final native int native_descramble(byte b, int i, int[] iArr, int[] iArr2, ByteBuffer byteBuffer, int i2, int i3, ByteBuffer byteBuffer2, int i4, int i5);

    private static final native void native_init();

    private final native void native_release();

    private final native void native_setup(IBinder iBinder);

    private final void validateInternalStates() {
        if (this.mIDescrambler == null) {
            throw new IllegalStateException();
        }
    }

    private final void cleanupAndRethrowIllegalState() {
        this.mIDescrambler = null;
        throw new IllegalStateException();
    }

    public MediaDescrambler(int CA_system_id) throws UnsupportedCasException {
        try {
            this.mIDescrambler = MediaCas.getService().createDescrambler(CA_system_id);
            if (this.mIDescrambler == null) {
                throw new UnsupportedCasException("Unsupported CA_system_id " + CA_system_id);
            }
        } catch (Exception e) {
            Log.e(TAG, "Failed to create descrambler: " + e);
            this.mIDescrambler = null;
            if (this.mIDescrambler == null) {
                throw new UnsupportedCasException("Unsupported CA_system_id " + CA_system_id);
            }
        } catch (Throwable th) {
            if (this.mIDescrambler == null) {
                UnsupportedCasException unsupportedCasException = new UnsupportedCasException("Unsupported CA_system_id " + CA_system_id);
            }
        }
        native_setup(this.mIDescrambler.asBinder());
    }

    IBinder getBinder() {
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

    public final void setMediaCasSession(Session session) {
        validateInternalStates();
        try {
            this.mIDescrambler.setMediaCasSession(session.mSessionId);
        } catch (ServiceSpecificException e) {
            MediaCasStateException.throwExceptions(e);
        } catch (RemoteException e2) {
            cleanupAndRethrowIllegalState();
        }
    }

    public final int descramble(ByteBuffer srcBuf, ByteBuffer dstBuf, CryptoInfo cryptoInfo) {
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
                return native_descramble(cryptoInfo.key[0], cryptoInfo.numSubSamples, cryptoInfo.numBytesOfClearData, cryptoInfo.numBytesOfEncryptedData, srcBuf, srcBuf.position(), srcBuf.limit(), dstBuf, dstBuf.position(), dstBuf.limit());
            } catch (ServiceSpecificException e) {
                MediaCasStateException.throwExceptions(e);
                return -1;
            }
        }
    }

    public void close() {
        if (this.mIDescrambler != null) {
            try {
                this.mIDescrambler.release();
            } catch (RemoteException e) {
            } catch (Throwable th) {
                this.mIDescrambler = null;
            }
            this.mIDescrambler = null;
        }
        native_release();
    }

    protected void finalize() {
        close();
    }

    static {
        System.loadLibrary("media_jni");
        native_init();
    }
}
