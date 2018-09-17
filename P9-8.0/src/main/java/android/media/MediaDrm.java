package android.media;

import android.app.ActivityThread;
import android.net.ProxyInfo;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.Parcel;
import android.util.Log;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

public final class MediaDrm {
    public static final int CERTIFICATE_TYPE_NONE = 0;
    public static final int CERTIFICATE_TYPE_X509 = 1;
    private static final int DRM_EVENT = 200;
    public static final int EVENT_KEY_EXPIRED = 3;
    public static final int EVENT_KEY_REQUIRED = 2;
    public static final int EVENT_PROVISION_REQUIRED = 1;
    public static final int EVENT_SESSION_RECLAIMED = 5;
    public static final int EVENT_VENDOR_DEFINED = 4;
    private static final int EXPIRATION_UPDATE = 201;
    private static final int KEY_STATUS_CHANGE = 202;
    public static final int KEY_TYPE_OFFLINE = 2;
    public static final int KEY_TYPE_RELEASE = 3;
    public static final int KEY_TYPE_STREAMING = 1;
    private static final String PERMISSION = "android.permission.ACCESS_DRM_CERTIFICATES";
    public static final String PROPERTY_ALGORITHMS = "algorithms";
    public static final String PROPERTY_DESCRIPTION = "description";
    public static final String PROPERTY_DEVICE_UNIQUE_ID = "deviceUniqueId";
    public static final String PROPERTY_VENDOR = "vendor";
    public static final String PROPERTY_VERSION = "version";
    private static final String TAG = "MediaDrm";
    private EventHandler mEventHandler;
    private long mNativeContext;
    private OnEventListener mOnEventListener;
    private EventHandler mOnExpirationUpdateEventHandler;
    private OnExpirationUpdateListener mOnExpirationUpdateListener;
    private EventHandler mOnKeyStatusChangeEventHandler;
    private OnKeyStatusChangeListener mOnKeyStatusChangeListener;

    public static final class Certificate {
        private byte[] mCertificateData;
        private byte[] mWrappedKey;

        Certificate() {
        }

        public byte[] getWrappedPrivateKey() {
            if (this.mWrappedKey != null) {
                return this.mWrappedKey;
            }
            throw new RuntimeException("Cerfificate is not initialized");
        }

        public byte[] getContent() {
            if (this.mCertificateData != null) {
                return this.mCertificateData;
            }
            throw new RuntimeException("Cerfificate is not initialized");
        }
    }

    public static final class CertificateRequest {
        private byte[] mData;
        private String mDefaultUrl;

        CertificateRequest(byte[] data, String defaultUrl) {
            this.mData = data;
            this.mDefaultUrl = defaultUrl;
        }

        public byte[] getData() {
            return this.mData;
        }

        public String getDefaultUrl() {
            return this.mDefaultUrl;
        }
    }

    public final class CryptoSession {
        private byte[] mSessionId;

        CryptoSession(byte[] sessionId, String cipherAlgorithm, String macAlgorithm) {
            this.mSessionId = sessionId;
            MediaDrm.setCipherAlgorithmNative(MediaDrm.this, sessionId, cipherAlgorithm);
            MediaDrm.setMacAlgorithmNative(MediaDrm.this, sessionId, macAlgorithm);
        }

        public byte[] encrypt(byte[] keyid, byte[] input, byte[] iv) {
            return MediaDrm.encryptNative(MediaDrm.this, this.mSessionId, keyid, input, iv);
        }

        public byte[] decrypt(byte[] keyid, byte[] input, byte[] iv) {
            return MediaDrm.decryptNative(MediaDrm.this, this.mSessionId, keyid, input, iv);
        }

        public byte[] sign(byte[] keyid, byte[] message) {
            return MediaDrm.signNative(MediaDrm.this, this.mSessionId, keyid, message);
        }

        public boolean verify(byte[] keyid, byte[] message, byte[] signature) {
            return MediaDrm.verifyNative(MediaDrm.this, this.mSessionId, keyid, message, signature);
        }
    }

    private class EventHandler extends Handler {
        private MediaDrm mMediaDrm;

        public EventHandler(MediaDrm md, Looper looper) {
            super(looper);
            this.mMediaDrm = md;
        }

        public void handleMessage(Message msg) {
            if (this.mMediaDrm.mNativeContext == 0) {
                Log.w(MediaDrm.TAG, "MediaDrm went away with unhandled events");
                return;
            }
            Parcel parcel;
            byte[] sessionId;
            switch (msg.what) {
                case 200:
                    if (!(MediaDrm.this.mOnEventListener == null || msg.obj == null || !(msg.obj instanceof Parcel))) {
                        parcel = msg.obj;
                        sessionId = parcel.createByteArray();
                        if (sessionId.length == 0) {
                            sessionId = null;
                        }
                        byte[] data = parcel.createByteArray();
                        if (data.length == 0) {
                            data = null;
                        }
                        Log.i(MediaDrm.TAG, "Drm event (" + msg.arg1 + "," + msg.arg2 + ")");
                        MediaDrm.this.mOnEventListener.onEvent(this.mMediaDrm, sessionId, msg.arg1, msg.arg2, data);
                    }
                    return;
                case 201:
                    if (!(MediaDrm.this.mOnExpirationUpdateListener == null || msg.obj == null || !(msg.obj instanceof Parcel))) {
                        parcel = (Parcel) msg.obj;
                        sessionId = parcel.createByteArray();
                        if (sessionId.length > 0) {
                            long expirationTime = parcel.readLong();
                            Log.i(MediaDrm.TAG, "Drm key expiration update: " + expirationTime);
                            MediaDrm.this.mOnExpirationUpdateListener.onExpirationUpdate(this.mMediaDrm, sessionId, expirationTime);
                        }
                    }
                    return;
                case 202:
                    if (!(MediaDrm.this.mOnKeyStatusChangeListener == null || msg.obj == null || !(msg.obj instanceof Parcel))) {
                        parcel = (Parcel) msg.obj;
                        sessionId = parcel.createByteArray();
                        if (sessionId.length > 0) {
                            List<KeyStatus> keyStatusList = MediaDrm.this.keyStatusListFromParcel(parcel);
                            boolean hasNewUsableKey = parcel.readInt() != 0;
                            Log.i(MediaDrm.TAG, "Drm key status changed");
                            MediaDrm.this.mOnKeyStatusChangeListener.onKeyStatusChange(this.mMediaDrm, sessionId, keyStatusList, hasNewUsableKey);
                        }
                    }
                    return;
                default:
                    Log.e(MediaDrm.TAG, "Unknown message type " + msg.what);
                    return;
            }
        }
    }

    public static final class KeyRequest {
        public static final int REQUEST_TYPE_INITIAL = 0;
        public static final int REQUEST_TYPE_RELEASE = 2;
        public static final int REQUEST_TYPE_RENEWAL = 1;
        private byte[] mData;
        private String mDefaultUrl;
        private int mRequestType;

        KeyRequest() {
        }

        public byte[] getData() {
            if (this.mData != null) {
                return this.mData;
            }
            throw new RuntimeException("KeyRequest is not initialized");
        }

        public String getDefaultUrl() {
            if (this.mDefaultUrl != null) {
                return this.mDefaultUrl;
            }
            throw new RuntimeException("KeyRequest is not initialized");
        }

        public int getRequestType() {
            return this.mRequestType;
        }
    }

    public static final class KeyStatus {
        public static final int STATUS_EXPIRED = 1;
        public static final int STATUS_INTERNAL_ERROR = 4;
        public static final int STATUS_OUTPUT_NOT_ALLOWED = 2;
        public static final int STATUS_PENDING = 3;
        public static final int STATUS_USABLE = 0;
        private final byte[] mKeyId;
        private final int mStatusCode;

        KeyStatus(byte[] keyId, int statusCode) {
            this.mKeyId = keyId;
            this.mStatusCode = statusCode;
        }

        public int getStatusCode() {
            return this.mStatusCode;
        }

        public byte[] getKeyId() {
            return this.mKeyId;
        }
    }

    public static final class MediaDrmStateException extends IllegalStateException {
        private final String mDiagnosticInfo;
        private final int mErrorCode;

        public MediaDrmStateException(int errorCode, String detailMessage) {
            super(detailMessage);
            this.mErrorCode = errorCode;
            this.mDiagnosticInfo = "android.media.MediaDrm.error_" + (errorCode < 0 ? "neg_" : ProxyInfo.LOCAL_EXCL_LIST) + Math.abs(errorCode);
        }

        public int getErrorCode() {
            return this.mErrorCode;
        }

        public String getDiagnosticInfo() {
            return this.mDiagnosticInfo;
        }
    }

    public interface OnEventListener {
        void onEvent(MediaDrm mediaDrm, byte[] bArr, int i, int i2, byte[] bArr2);
    }

    public interface OnExpirationUpdateListener {
        void onExpirationUpdate(MediaDrm mediaDrm, byte[] bArr, long j);
    }

    public interface OnKeyStatusChangeListener {
        void onKeyStatusChange(MediaDrm mediaDrm, byte[] bArr, List<KeyStatus> list, boolean z);
    }

    public static final class ProvisionRequest {
        private byte[] mData;
        private String mDefaultUrl;

        ProvisionRequest() {
        }

        public byte[] getData() {
            if (this.mData != null) {
                return this.mData;
            }
            throw new RuntimeException("ProvisionRequest is not initialized");
        }

        public String getDefaultUrl() {
            if (this.mDefaultUrl != null) {
                return this.mDefaultUrl;
            }
            throw new RuntimeException("ProvisionRequest is not initialized");
        }
    }

    private static final native byte[] decryptNative(MediaDrm mediaDrm, byte[] bArr, byte[] bArr2, byte[] bArr3, byte[] bArr4);

    private static final native byte[] encryptNative(MediaDrm mediaDrm, byte[] bArr, byte[] bArr2, byte[] bArr3, byte[] bArr4);

    private native ProvisionRequest getProvisionRequestNative(int i, String str);

    private static final native boolean isCryptoSchemeSupportedNative(byte[] bArr, String str);

    private final native void native_finalize();

    private static final native void native_init();

    private final native void native_setup(Object obj, byte[] bArr, String str);

    private native Certificate provideProvisionResponseNative(byte[] bArr) throws DeniedByServerException;

    private static final native void setCipherAlgorithmNative(MediaDrm mediaDrm, byte[] bArr, String str);

    private static final native void setMacAlgorithmNative(MediaDrm mediaDrm, byte[] bArr, String str);

    private static final native byte[] signNative(MediaDrm mediaDrm, byte[] bArr, byte[] bArr2, byte[] bArr3);

    private static final native byte[] signRSANative(MediaDrm mediaDrm, byte[] bArr, String str, byte[] bArr2, byte[] bArr3);

    private static final native boolean verifyNative(MediaDrm mediaDrm, byte[] bArr, byte[] bArr2, byte[] bArr3, byte[] bArr4);

    public native void closeSession(byte[] bArr);

    public native KeyRequest getKeyRequest(byte[] bArr, byte[] bArr2, String str, int i, HashMap<String, String> hashMap) throws NotProvisionedException;

    public native byte[] getPropertyByteArray(String str);

    public native String getPropertyString(String str);

    public native byte[] getSecureStop(byte[] bArr);

    public native List<byte[]> getSecureStops();

    public native byte[] openSession() throws NotProvisionedException, ResourceBusyException;

    public native byte[] provideKeyResponse(byte[] bArr, byte[] bArr2) throws NotProvisionedException, DeniedByServerException;

    public native HashMap<String, String> queryKeyStatus(byte[] bArr);

    public final native void release();

    public native void releaseAllSecureStops();

    public native void releaseSecureStops(byte[] bArr);

    public native void removeKeys(byte[] bArr);

    public native void restoreKeys(byte[] bArr, byte[] bArr2);

    public native void setPropertyByteArray(String str, byte[] bArr);

    public native void setPropertyString(String str, String str2);

    public static final boolean isCryptoSchemeSupported(UUID uuid) {
        return isCryptoSchemeSupportedNative(getByteArrayFromUUID(uuid), null);
    }

    public static final boolean isCryptoSchemeSupported(UUID uuid, String mimeType) {
        return isCryptoSchemeSupportedNative(getByteArrayFromUUID(uuid), mimeType);
    }

    private static final byte[] getByteArrayFromUUID(UUID uuid) {
        long msb = uuid.getMostSignificantBits();
        long lsb = uuid.getLeastSignificantBits();
        byte[] uuidBytes = new byte[16];
        for (int i = 0; i < 8; i++) {
            uuidBytes[i] = (byte) ((int) (msb >>> ((7 - i) * 8)));
            uuidBytes[i + 8] = (byte) ((int) (lsb >>> ((7 - i) * 8)));
        }
        return uuidBytes;
    }

    public MediaDrm(UUID uuid) throws UnsupportedSchemeException {
        Looper looper = Looper.myLooper();
        if (looper != null) {
            this.mEventHandler = new EventHandler(this, looper);
        } else {
            looper = Looper.getMainLooper();
            if (looper != null) {
                this.mEventHandler = new EventHandler(this, looper);
            } else {
                this.mEventHandler = null;
            }
        }
        native_setup(new WeakReference(this), getByteArrayFromUUID(uuid), ActivityThread.currentOpPackageName());
    }

    public void setOnExpirationUpdateListener(OnExpirationUpdateListener listener, Handler handler) {
        if (listener != null) {
            Looper looper = handler != null ? handler.getLooper() : Looper.myLooper();
            if (looper != null && (this.mEventHandler == null || this.mEventHandler.getLooper() != looper)) {
                this.mEventHandler = new EventHandler(this, looper);
            }
        }
        this.mOnExpirationUpdateListener = listener;
    }

    public void setOnKeyStatusChangeListener(OnKeyStatusChangeListener listener, Handler handler) {
        if (listener != null) {
            Looper looper = handler != null ? handler.getLooper() : Looper.myLooper();
            if (looper != null && (this.mEventHandler == null || this.mEventHandler.getLooper() != looper)) {
                this.mEventHandler = new EventHandler(this, looper);
            }
        }
        this.mOnKeyStatusChangeListener = listener;
    }

    public void setOnEventListener(OnEventListener listener) {
        this.mOnEventListener = listener;
    }

    private List<KeyStatus> keyStatusListFromParcel(Parcel parcel) {
        int nelems = parcel.readInt();
        List<KeyStatus> keyStatusList = new ArrayList(nelems);
        while (true) {
            int nelems2 = nelems;
            nelems = nelems2 - 1;
            if (nelems2 <= 0) {
                return keyStatusList;
            }
            keyStatusList.add(new KeyStatus(parcel.createByteArray(), parcel.readInt()));
        }
    }

    private static void postEventFromNative(Object mediadrm_ref, int what, int eventType, int extra, Object obj) {
        MediaDrm md = (MediaDrm) ((WeakReference) mediadrm_ref).get();
        if (!(md == null || md.mEventHandler == null)) {
            md.mEventHandler.sendMessage(md.mEventHandler.obtainMessage(what, eventType, extra, obj));
        }
    }

    public ProvisionRequest getProvisionRequest() {
        return getProvisionRequestNative(0, ProxyInfo.LOCAL_EXCL_LIST);
    }

    public void provideProvisionResponse(byte[] response) throws DeniedByServerException {
        provideProvisionResponseNative(response);
    }

    public CryptoSession getCryptoSession(byte[] sessionId, String cipherAlgorithm, String macAlgorithm) {
        return new CryptoSession(sessionId, cipherAlgorithm, macAlgorithm);
    }

    public CertificateRequest getCertificateRequest(int certType, String certAuthority) {
        ProvisionRequest provisionRequest = getProvisionRequestNative(certType, certAuthority);
        return new CertificateRequest(provisionRequest.getData(), provisionRequest.getDefaultUrl());
    }

    public Certificate provideCertificateResponse(byte[] response) throws DeniedByServerException {
        return provideProvisionResponseNative(response);
    }

    public byte[] signRSA(byte[] sessionId, String algorithm, byte[] wrappedKey, byte[] message) {
        return signRSANative(this, sessionId, algorithm, wrappedKey, message);
    }

    protected void finalize() {
        native_finalize();
    }

    static {
        System.loadLibrary("media_jni");
        native_init();
    }
}
