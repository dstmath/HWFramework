package android.media;

import android.app.ActivityThread;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.Parcel;
import android.os.PersistableBundle;
import android.util.Log;
import dalvik.system.CloseGuard;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;

public final class MediaDrm implements AutoCloseable {
    public static final int CERTIFICATE_TYPE_NONE = 0;
    public static final int CERTIFICATE_TYPE_X509 = 1;
    private static final int DRM_EVENT = 200;
    public static final int EVENT_KEY_EXPIRED = 3;
    public static final int EVENT_KEY_REQUIRED = 2;
    public static final int EVENT_PROVISION_REQUIRED = 1;
    public static final int EVENT_SESSION_RECLAIMED = 5;
    public static final int EVENT_VENDOR_DEFINED = 4;
    private static final int EXPIRATION_UPDATE = 201;
    public static final int HDCP_LEVEL_UNKNOWN = 0;
    public static final int HDCP_NONE = 1;
    public static final int HDCP_NO_DIGITAL_OUTPUT = Integer.MAX_VALUE;
    public static final int HDCP_V1 = 2;
    public static final int HDCP_V2 = 3;
    public static final int HDCP_V2_1 = 4;
    public static final int HDCP_V2_2 = 5;
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
    public static final int SECURITY_LEVEL_HW_SECURE_ALL = 5;
    public static final int SECURITY_LEVEL_HW_SECURE_CRYPTO = 3;
    public static final int SECURITY_LEVEL_HW_SECURE_DECODE = 4;
    public static final int SECURITY_LEVEL_MAX = 6;
    public static final int SECURITY_LEVEL_SW_SECURE_CRYPTO = 1;
    public static final int SECURITY_LEVEL_SW_SECURE_DECODE = 2;
    public static final int SECURITY_LEVEL_UNKNOWN = 0;
    private static final String TAG = "MediaDrm";
    private final CloseGuard mCloseGuard = CloseGuard.get();
    private final AtomicBoolean mClosed = new AtomicBoolean();
    private EventHandler mEventHandler;
    /* access modifiers changed from: private */
    public long mNativeContext;
    /* access modifiers changed from: private */
    public OnEventListener mOnEventListener;
    private EventHandler mOnExpirationUpdateEventHandler;
    /* access modifiers changed from: private */
    public OnExpirationUpdateListener mOnExpirationUpdateListener;
    private EventHandler mOnKeyStatusChangeEventHandler;
    /* access modifiers changed from: private */
    public OnKeyStatusChangeListener mOnKeyStatusChangeListener;

    @Retention(RetentionPolicy.SOURCE)
    public @interface ArrayProperty {
    }

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

    @Retention(RetentionPolicy.SOURCE)
    public @interface CertificateType {
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

    @Retention(RetentionPolicy.SOURCE)
    public @interface DrmEvent {
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
            switch (msg.what) {
                case 200:
                    if (!(MediaDrm.this.mOnEventListener == null || msg.obj == null || !(msg.obj instanceof Parcel))) {
                        Parcel parcel = (Parcel) msg.obj;
                        byte[] sessionId = parcel.createByteArray();
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
                        Parcel parcel2 = (Parcel) msg.obj;
                        byte[] sessionId2 = parcel2.createByteArray();
                        if (sessionId2.length > 0) {
                            long expirationTime = parcel2.readLong();
                            Log.i(MediaDrm.TAG, "Drm key expiration update: " + expirationTime);
                            MediaDrm.this.mOnExpirationUpdateListener.onExpirationUpdate(this.mMediaDrm, sessionId2, expirationTime);
                        }
                    }
                    return;
                case 202:
                    if (!(MediaDrm.this.mOnKeyStatusChangeListener == null || msg.obj == null || !(msg.obj instanceof Parcel))) {
                        Parcel parcel3 = (Parcel) msg.obj;
                        byte[] sessionId3 = parcel3.createByteArray();
                        if (sessionId3.length > 0) {
                            List<KeyStatus> keyStatusList = MediaDrm.this.keyStatusListFromParcel(parcel3);
                            boolean hasNewUsableKey = parcel3.readInt() != 0;
                            Log.i(MediaDrm.TAG, "Drm key status changed");
                            MediaDrm.this.mOnKeyStatusChangeListener.onKeyStatusChange(this.mMediaDrm, sessionId3, keyStatusList, hasNewUsableKey);
                        }
                    }
                    return;
                default:
                    Log.e(MediaDrm.TAG, "Unknown message type " + msg.what);
                    return;
            }
        }
    }

    @Retention(RetentionPolicy.SOURCE)
    public @interface HdcpLevel {
    }

    public static final class KeyRequest {
        public static final int REQUEST_TYPE_INITIAL = 0;
        public static final int REQUEST_TYPE_NONE = 3;
        public static final int REQUEST_TYPE_RELEASE = 2;
        public static final int REQUEST_TYPE_RENEWAL = 1;
        public static final int REQUEST_TYPE_UPDATE = 4;
        private byte[] mData;
        private String mDefaultUrl;
        private int mRequestType;

        @Retention(RetentionPolicy.SOURCE)
        public @interface RequestType {
        }

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

        @Retention(RetentionPolicy.SOURCE)
        public @interface KeyStatusCode {
        }

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

    @Retention(RetentionPolicy.SOURCE)
    public @interface KeyType {
    }

    public static final class MediaDrmStateException extends IllegalStateException {
        private final String mDiagnosticInfo;
        private final int mErrorCode;

        public MediaDrmStateException(int errorCode, String detailMessage) {
            super(detailMessage);
            this.mErrorCode = errorCode;
            String sign = errorCode < 0 ? "neg_" : "";
            this.mDiagnosticInfo = "android.media.MediaDrm.error_" + sign + Math.abs(errorCode);
        }

        public int getErrorCode() {
            return this.mErrorCode;
        }

        public String getDiagnosticInfo() {
            return this.mDiagnosticInfo;
        }
    }

    public static final class MetricsConstants {
        public static final String CLOSE_SESSION_ERROR_COUNT = "drm.mediadrm.close_session.error.count";
        public static final String CLOSE_SESSION_ERROR_LIST = "drm.mediadrm.close_session.error.list";
        public static final String CLOSE_SESSION_OK_COUNT = "drm.mediadrm.close_session.ok.count";
        public static final String EVENT_KEY_EXPIRED_COUNT = "drm.mediadrm.event.KEY_EXPIRED.count";
        public static final String EVENT_KEY_NEEDED_COUNT = "drm.mediadrm.event.KEY_NEEDED.count";
        public static final String EVENT_PROVISION_REQUIRED_COUNT = "drm.mediadrm.event.PROVISION_REQUIRED.count";
        public static final String EVENT_SESSION_RECLAIMED_COUNT = "drm.mediadrm.event.SESSION_RECLAIMED.count";
        public static final String EVENT_VENDOR_DEFINED_COUNT = "drm.mediadrm.event.VENDOR_DEFINED.count";
        public static final String GET_DEVICE_UNIQUE_ID_ERROR_COUNT = "drm.mediadrm.get_device_unique_id.error.count";
        public static final String GET_DEVICE_UNIQUE_ID_ERROR_LIST = "drm.mediadrm.get_device_unique_id.error.list";
        public static final String GET_DEVICE_UNIQUE_ID_OK_COUNT = "drm.mediadrm.get_device_unique_id.ok.count";
        public static final String GET_KEY_REQUEST_ERROR_COUNT = "drm.mediadrm.get_key_request.error.count";
        public static final String GET_KEY_REQUEST_ERROR_LIST = "drm.mediadrm.get_key_request.error.list";
        public static final String GET_KEY_REQUEST_OK_COUNT = "drm.mediadrm.get_key_request.ok.count";
        public static final String GET_KEY_REQUEST_OK_TIME_MICROS = "drm.mediadrm.get_key_request.ok.average_time_micros";
        public static final String GET_PROVISION_REQUEST_ERROR_COUNT = "drm.mediadrm.get_provision_request.error.count";
        public static final String GET_PROVISION_REQUEST_ERROR_LIST = "drm.mediadrm.get_provision_request.error.list";
        public static final String GET_PROVISION_REQUEST_OK_COUNT = "drm.mediadrm.get_provision_request.ok.count";
        public static final String KEY_STATUS_EXPIRED_COUNT = "drm.mediadrm.key_status.EXPIRED.count";
        public static final String KEY_STATUS_INTERNAL_ERROR_COUNT = "drm.mediadrm.key_status.INTERNAL_ERROR.count";
        public static final String KEY_STATUS_OUTPUT_NOT_ALLOWED_COUNT = "drm.mediadrm.key_status_change.OUTPUT_NOT_ALLOWED.count";
        public static final String KEY_STATUS_PENDING_COUNT = "drm.mediadrm.key_status_change.PENDING.count";
        public static final String KEY_STATUS_USABLE_COUNT = "drm.mediadrm.key_status_change.USABLE.count";
        public static final String OPEN_SESSION_ERROR_COUNT = "drm.mediadrm.open_session.error.count";
        public static final String OPEN_SESSION_ERROR_LIST = "drm.mediadrm.open_session.error.list";
        public static final String OPEN_SESSION_OK_COUNT = "drm.mediadrm.open_session.ok.count";
        public static final String PROVIDE_KEY_RESPONSE_ERROR_COUNT = "drm.mediadrm.provide_key_response.error.count";
        public static final String PROVIDE_KEY_RESPONSE_ERROR_LIST = "drm.mediadrm.provide_key_response.error.list";
        public static final String PROVIDE_KEY_RESPONSE_OK_COUNT = "drm.mediadrm.provide_key_response.ok.count";
        public static final String PROVIDE_KEY_RESPONSE_OK_TIME_MICROS = "drm.mediadrm.provide_key_response.ok.average_time_micros";
        public static final String PROVIDE_PROVISION_RESPONSE_ERROR_COUNT = "drm.mediadrm.provide_provision_response.error.count";
        public static final String PROVIDE_PROVISION_RESPONSE_ERROR_LIST = "drm.mediadrm.provide_provision_response.error.list";
        public static final String PROVIDE_PROVISION_RESPONSE_OK_COUNT = "drm.mediadrm.provide_provision_response.ok.count";
        public static final String SESSION_END_TIMES_MS = "drm.mediadrm.session_end_times_ms";
        public static final String SESSION_START_TIMES_MS = "drm.mediadrm.session_start_times_ms";

        private MetricsConstants() {
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

    @Retention(RetentionPolicy.SOURCE)
    public @interface SecurityLevel {
    }

    @Retention(RetentionPolicy.SOURCE)
    public @interface StringProperty {
    }

    /* access modifiers changed from: private */
    public static final native byte[] decryptNative(MediaDrm mediaDrm, byte[] bArr, byte[] bArr2, byte[] bArr3, byte[] bArr4);

    /* access modifiers changed from: private */
    public static final native byte[] encryptNative(MediaDrm mediaDrm, byte[] bArr, byte[] bArr2, byte[] bArr3, byte[] bArr4);

    private native PersistableBundle getMetricsNative();

    private native ProvisionRequest getProvisionRequestNative(int i, String str);

    private static final native boolean isCryptoSchemeSupportedNative(byte[] bArr, String str);

    private static final native void native_init();

    private final native void native_setup(Object obj, byte[] bArr, String str);

    private native Certificate provideProvisionResponseNative(byte[] bArr) throws DeniedByServerException;

    /* access modifiers changed from: private */
    public static final native void setCipherAlgorithmNative(MediaDrm mediaDrm, byte[] bArr, String str);

    /* access modifiers changed from: private */
    public static final native void setMacAlgorithmNative(MediaDrm mediaDrm, byte[] bArr, String str);

    /* access modifiers changed from: private */
    public static final native byte[] signNative(MediaDrm mediaDrm, byte[] bArr, byte[] bArr2, byte[] bArr3);

    private static final native byte[] signRSANative(MediaDrm mediaDrm, byte[] bArr, String str, byte[] bArr2, byte[] bArr3);

    /* access modifiers changed from: private */
    public static final native boolean verifyNative(MediaDrm mediaDrm, byte[] bArr, byte[] bArr2, byte[] bArr3, byte[] bArr4);

    public native void closeSession(byte[] bArr);

    public native int getConnectedHdcpLevel();

    public native KeyRequest getKeyRequest(byte[] bArr, byte[] bArr2, String str, int i, HashMap<String, String> hashMap) throws NotProvisionedException;

    public native int getMaxHdcpLevel();

    public native int getMaxSessionCount();

    public native int getOpenSessionCount();

    public native byte[] getPropertyByteArray(String str);

    public native String getPropertyString(String str);

    public native byte[] getSecureStop(byte[] bArr);

    public native List<byte[]> getSecureStopIds();

    public native List<byte[]> getSecureStops();

    public native int getSecurityLevel(byte[] bArr);

    public final native void native_release();

    public native byte[] openSession(int i) throws NotProvisionedException, ResourceBusyException;

    public native byte[] provideKeyResponse(byte[] bArr, byte[] bArr2) throws NotProvisionedException, DeniedByServerException;

    public native HashMap<String, String> queryKeyStatus(byte[] bArr);

    public native void releaseSecureStops(byte[] bArr);

    public native void removeAllSecureStops();

    public native void removeKeys(byte[] bArr);

    public native void removeSecureStop(byte[] bArr);

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
            uuidBytes[8 + i] = (byte) ((int) (lsb >>> (8 * (7 - i))));
        }
        return uuidBytes;
    }

    public MediaDrm(UUID uuid) throws UnsupportedSchemeException {
        Looper myLooper = Looper.myLooper();
        Looper looper = myLooper;
        if (myLooper != null) {
            this.mEventHandler = new EventHandler(this, looper);
        } else {
            Looper mainLooper = Looper.getMainLooper();
            Looper looper2 = mainLooper;
            if (mainLooper != null) {
                this.mEventHandler = new EventHandler(this, looper2);
            } else {
                this.mEventHandler = null;
            }
        }
        native_setup(new WeakReference(this), getByteArrayFromUUID(uuid), ActivityThread.currentOpPackageName());
        this.mCloseGuard.open("release");
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

    /* access modifiers changed from: private */
    public List<KeyStatus> keyStatusListFromParcel(Parcel parcel) {
        int nelems = parcel.readInt();
        List<KeyStatus> keyStatusList = new ArrayList<>(nelems);
        while (true) {
            int nelems2 = nelems - 1;
            if (nelems <= 0) {
                return keyStatusList;
            }
            keyStatusList.add(new KeyStatus(parcel.createByteArray(), parcel.readInt()));
            nelems = nelems2;
        }
    }

    private static void postEventFromNative(Object mediadrm_ref, int what, int eventType, int extra, Object obj) {
        MediaDrm md = (MediaDrm) ((WeakReference) mediadrm_ref).get();
        if (!(md == null || md.mEventHandler == null)) {
            md.mEventHandler.sendMessage(md.mEventHandler.obtainMessage(what, eventType, extra, obj));
        }
    }

    public byte[] openSession() throws NotProvisionedException, ResourceBusyException {
        return openSession(getMaxSecurityLevel());
    }

    public ProvisionRequest getProvisionRequest() {
        return getProvisionRequestNative(0, "");
    }

    public void provideProvisionResponse(byte[] response) throws DeniedByServerException {
        provideProvisionResponseNative(response);
    }

    public void releaseAllSecureStops() {
        removeAllSecureStops();
    }

    public static final int getMaxSecurityLevel() {
        return 6;
    }

    public PersistableBundle getMetrics() {
        return getMetricsNative();
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

    /* access modifiers changed from: protected */
    public void finalize() throws Throwable {
        try {
            if (this.mCloseGuard != null) {
                this.mCloseGuard.warnIfOpen();
            }
            release();
        } finally {
            super.finalize();
        }
    }

    public void close() {
        release();
    }

    @Deprecated
    public void release() {
        this.mCloseGuard.close();
        if (this.mClosed.compareAndSet(false, true)) {
            native_release();
        }
    }

    static {
        System.loadLibrary("media_jni");
        native_init();
    }
}
