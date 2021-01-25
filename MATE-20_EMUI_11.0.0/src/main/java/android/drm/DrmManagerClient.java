package android.drm;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.IntentFilter;
import android.database.Cursor;
import android.database.sqlite.SQLiteException;
import android.drm.DrmStore;
import android.net.Uri;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.os.SystemProperties;
import android.util.Log;
import dalvik.system.CloseGuard;
import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.atomic.AtomicBoolean;

public class DrmManagerClient implements AutoCloseable {
    private static final int ACTION_PROCESS_DRM_INFO = 1002;
    private static final int ACTION_REMOVE_ALL_RIGHTS = 1001;
    public static final int ERROR_NONE = 0;
    public static final int ERROR_UNKNOWN = -2000;
    public static final int INVALID_SESSION = -1;
    private static final String TAG = "DrmManagerClient";
    private final CloseGuard mCloseGuard = CloseGuard.get();
    private final AtomicBoolean mClosed = new AtomicBoolean();
    private Context mContext;
    private EventHandler mEventHandler;
    HandlerThread mEventThread;
    private InfoHandler mInfoHandler;
    HandlerThread mInfoThread;
    private long mNativeContext;
    private OnErrorListener mOnErrorListener;
    private OnEventListener mOnEventListener;
    private OnInfoListener mOnInfoListener;
    private int mUniqueId;

    public interface OnErrorListener {
        void onError(DrmManagerClient drmManagerClient, DrmErrorEvent drmErrorEvent);
    }

    public interface OnEventListener {
        void onEvent(DrmManagerClient drmManagerClient, DrmEvent drmEvent);
    }

    public interface OnInfoListener {
        void onInfo(DrmManagerClient drmManagerClient, DrmInfoEvent drmInfoEvent);
    }

    private native DrmInfo _acquireDrmInfo(int i, DrmInfoRequest drmInfoRequest);

    private native boolean _canHandle(int i, String str, String str2);

    private native int _checkRightsStatus(int i, String str, int i2);

    private native DrmConvertedStatus _closeConvertSession(int i, int i2);

    private native DrmConvertedStatus _convertData(int i, int i2, byte[] bArr);

    private native DrmSupportInfo[] _getAllSupportInfo(int i);

    private native ContentValues _getConstraints(int i, String str, int i2);

    private native int _getDrmObjectType(int i, String str, String str2);

    private native ContentValues _getMetadata(int i, String str);

    private native String _getOriginalMimeType(int i, String str, FileDescriptor fileDescriptor);

    private native int _initialize();

    private native void _installDrmEngine(int i, String str);

    private native int _openConvertSession(int i, String str);

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private native DrmInfoStatus _processDrmInfo(int i, DrmInfo drmInfo);

    private native void _release(int i);

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private native int _removeAllRights(int i);

    private native int _removeRights(int i, String str);

    private native int _saveRights(int i, DrmRights drmRights, String str, String str2);

    private native void _setListeners(int i, Object obj);

    static {
        System.loadLibrary("drmframework_jni");
    }

    /* access modifiers changed from: private */
    public class EventHandler extends Handler {
        public EventHandler(Looper looper) {
            super(looper);
        }

        @Override // android.os.Handler
        public void handleMessage(Message msg) {
            DrmEvent event = null;
            DrmErrorEvent error = null;
            HashMap<String, Object> attributes = new HashMap<>();
            int i = msg.what;
            if (i == 1001) {
                DrmManagerClient drmManagerClient = DrmManagerClient.this;
                if (drmManagerClient._removeAllRights(drmManagerClient.mUniqueId) == 0) {
                    event = new DrmEvent(DrmManagerClient.this.mUniqueId, 1001, null);
                } else {
                    error = new DrmErrorEvent(DrmManagerClient.this.mUniqueId, 2007, null);
                }
            } else if (i != 1002) {
                Log.e(DrmManagerClient.TAG, "Unknown message type " + msg.what);
                return;
            } else {
                DrmInfo drmInfo = (DrmInfo) msg.obj;
                DrmManagerClient drmManagerClient2 = DrmManagerClient.this;
                DrmInfoStatus status = drmManagerClient2._processDrmInfo(drmManagerClient2.mUniqueId, drmInfo);
                attributes.put(DrmEvent.DRM_INFO_STATUS_OBJECT, status);
                attributes.put(DrmEvent.DRM_INFO_OBJECT, drmInfo);
                if (status == null || 1 != status.statusCode) {
                    error = new DrmErrorEvent(DrmManagerClient.this.mUniqueId, DrmManagerClient.this.getErrorType(status != null ? status.infoType : drmInfo.getInfoType()), null, attributes);
                } else {
                    event = new DrmEvent(DrmManagerClient.this.mUniqueId, DrmManagerClient.this.getEventType(status.infoType), null, attributes);
                }
            }
            if (!(DrmManagerClient.this.mOnEventListener == null || event == null)) {
                DrmManagerClient.this.mOnEventListener.onEvent(DrmManagerClient.this, event);
            }
            if (DrmManagerClient.this.mOnErrorListener != null && error != null) {
                DrmManagerClient.this.mOnErrorListener.onError(DrmManagerClient.this, error);
            }
        }
    }

    public static void notify(Object thisReference, int uniqueId, int infoType, String message) {
        InfoHandler infoHandler;
        DrmManagerClient instance = (DrmManagerClient) ((WeakReference) thisReference).get();
        if (instance != null && (infoHandler = instance.mInfoHandler) != null) {
            instance.mInfoHandler.sendMessage(infoHandler.obtainMessage(1, uniqueId, infoType, message));
        }
    }

    /* access modifiers changed from: private */
    public class InfoHandler extends Handler {
        public static final int INFO_EVENT_TYPE = 1;

        public InfoHandler(Looper looper) {
            super(looper);
        }

        @Override // android.os.Handler
        public void handleMessage(Message msg) {
            DrmInfoEvent info = null;
            DrmErrorEvent error = null;
            if (msg.what != 1) {
                Log.e(DrmManagerClient.TAG, "Unknown message type " + msg.what);
                return;
            }
            int uniqueId = msg.arg1;
            int infoType = msg.arg2;
            String message = msg.obj.toString();
            switch (infoType) {
                case 1:
                case 3:
                case 4:
                case 5:
                case 6:
                    info = new DrmInfoEvent(uniqueId, infoType, message);
                    break;
                case 2:
                    try {
                        DrmUtils.removeFile(message);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    info = new DrmInfoEvent(uniqueId, infoType, message);
                    break;
                default:
                    error = new DrmErrorEvent(uniqueId, infoType, message);
                    break;
            }
            if (!(DrmManagerClient.this.mOnInfoListener == null || info == null)) {
                DrmManagerClient.this.mOnInfoListener.onInfo(DrmManagerClient.this, info);
            }
            if (DrmManagerClient.this.mOnErrorListener != null && error != null) {
                DrmManagerClient.this.mOnErrorListener.onError(DrmManagerClient.this, error);
            }
        }
    }

    public DrmManagerClient(Context context) {
        this.mContext = context;
        if (SystemProperties.get("drm.service.enabled") != null && !SystemProperties.get("drm.service.enabled").equals("")) {
            createEventThreads();
        }
        this.mUniqueId = _initialize();
        this.mCloseGuard.open("release");
    }

    /* access modifiers changed from: protected */
    @Override // java.lang.Object
    public void finalize() throws Throwable {
        try {
            if (this.mCloseGuard != null) {
                this.mCloseGuard.warnIfOpen();
            }
            close();
        } finally {
            super.finalize();
        }
    }

    @Override // java.lang.AutoCloseable
    public void close() {
        this.mCloseGuard.close();
        if (this.mClosed.compareAndSet(false, true)) {
            if (this.mEventHandler != null) {
                this.mEventThread.quit();
                this.mEventThread = null;
            }
            if (this.mInfoHandler != null) {
                this.mInfoThread.quit();
                this.mInfoThread = null;
            }
            this.mEventHandler = null;
            this.mInfoHandler = null;
            this.mOnEventListener = null;
            this.mOnInfoListener = null;
            this.mOnErrorListener = null;
            _release(this.mUniqueId);
        }
    }

    @Deprecated
    public void release() {
        close();
    }

    public synchronized void setOnInfoListener(OnInfoListener infoListener) {
        this.mOnInfoListener = infoListener;
        if (infoListener != null) {
            createListeners();
        }
    }

    public synchronized void setOnEventListener(OnEventListener eventListener) {
        this.mOnEventListener = eventListener;
        if (eventListener != null) {
            createListeners();
        }
    }

    public synchronized void setOnErrorListener(OnErrorListener errorListener) {
        this.mOnErrorListener = errorListener;
        if (errorListener != null) {
            createListeners();
        }
    }

    public String[] getAvailableDrmEngines() {
        DrmSupportInfo[] supportInfos = _getAllSupportInfo(this.mUniqueId);
        ArrayList<String> descriptions = new ArrayList<>();
        for (DrmSupportInfo drmSupportInfo : supportInfos) {
            descriptions.add(drmSupportInfo.getDescriprition());
        }
        return (String[]) descriptions.toArray(new String[descriptions.size()]);
    }

    public ContentValues getConstraints(String path, int action) {
        if (path != null && !path.equals("") && DrmStore.Action.isValid(action)) {
            return _getConstraints(this.mUniqueId, path, action);
        }
        throw new IllegalArgumentException("Given usage or path is invalid/null");
    }

    public ContentValues getMetadata(String path) {
        if (path != null && !path.equals("")) {
            return _getMetadata(this.mUniqueId, path);
        }
        throw new IllegalArgumentException("Given path is invalid/null");
    }

    public ContentValues getConstraints(Uri uri, int action) {
        if (uri != null && Uri.EMPTY != uri) {
            return getConstraints(convertUriToPath(uri), action);
        }
        throw new IllegalArgumentException("Uri should be non null");
    }

    public ContentValues getMetadata(Uri uri) {
        if (uri != null && Uri.EMPTY != uri) {
            return getMetadata(convertUriToPath(uri));
        }
        throw new IllegalArgumentException("Uri should be non null");
    }

    public int saveRights(DrmRights drmRights, String rightsPath, String contentPath) throws IOException {
        if (drmRights == null || !drmRights.isValid()) {
            throw new IllegalArgumentException("Given drmRights or contentPath is not valid");
        }
        if (rightsPath != null && !rightsPath.equals("")) {
            DrmUtils.writeToFile(rightsPath, drmRights.getData());
        }
        return _saveRights(this.mUniqueId, drmRights, rightsPath, contentPath);
    }

    public void installDrmEngine(String engineFilePath) {
        if (engineFilePath == null || engineFilePath.equals("")) {
            throw new IllegalArgumentException("Given engineFilePath: " + engineFilePath + "is not valid");
        }
        _installDrmEngine(this.mUniqueId, engineFilePath);
    }

    public boolean canHandle(String path, String mimeType) {
        if ((path != null && !path.equals("")) || (mimeType != null && !mimeType.equals(""))) {
            return _canHandle(this.mUniqueId, path, mimeType);
        }
        throw new IllegalArgumentException("Path or the mimetype should be non null");
    }

    public boolean canHandle(Uri uri, String mimeType) {
        if ((uri != null && Uri.EMPTY != uri) || (mimeType != null && !mimeType.equals(""))) {
            return canHandle(convertUriToPath(uri), mimeType);
        }
        throw new IllegalArgumentException("Uri or the mimetype should be non null");
    }

    public int processDrmInfo(DrmInfo drmInfo) {
        if (drmInfo == null || !drmInfo.isValid()) {
            throw new IllegalArgumentException("Given drmInfo is invalid/null");
        }
        EventHandler eventHandler = this.mEventHandler;
        if (eventHandler == null) {
            return ERROR_UNKNOWN;
        }
        return this.mEventHandler.sendMessage(eventHandler.obtainMessage(1002, drmInfo)) ? 0 : -2000;
    }

    public DrmInfo acquireDrmInfo(DrmInfoRequest drmInfoRequest) {
        if (drmInfoRequest != null && drmInfoRequest.isValid()) {
            return _acquireDrmInfo(this.mUniqueId, drmInfoRequest);
        }
        throw new IllegalArgumentException("Given drmInfoRequest is invalid/null");
    }

    public int acquireRights(DrmInfoRequest drmInfoRequest) {
        DrmInfo drmInfo = acquireDrmInfo(drmInfoRequest);
        if (drmInfo == null) {
            return ERROR_UNKNOWN;
        }
        return processDrmInfo(drmInfo);
    }

    public int getDrmObjectType(String path, String mimeType) {
        if ((path != null && !path.equals("")) || (mimeType != null && !mimeType.equals(""))) {
            return _getDrmObjectType(this.mUniqueId, path, mimeType);
        }
        throw new IllegalArgumentException("Path or the mimetype should be non null");
    }

    public int getDrmObjectType(Uri uri, String mimeType) {
        if ((uri == null || Uri.EMPTY == uri) && (mimeType == null || mimeType.equals(""))) {
            throw new IllegalArgumentException("Uri or the mimetype should be non null");
        }
        String path = "";
        try {
            path = convertUriToPath(uri);
        } catch (Exception e) {
            Log.w(TAG, "Given Uri could not be found in media store");
        }
        return getDrmObjectType(path, mimeType);
    }

    public String getOriginalMimeType(String path) {
        if (path == null || path.equals("")) {
            throw new IllegalArgumentException("Given path should be non null");
        }
        String mime = null;
        FileInputStream is = null;
        FileDescriptor fd = null;
        try {
            File file = new File(path);
            if (file.exists()) {
                is = new FileInputStream(file);
                fd = is.getFD();
            }
            mime = _getOriginalMimeType(this.mUniqueId, path, fd);
            if (is != null) {
                try {
                    is.close();
                } catch (IOException e) {
                }
            }
        } catch (IOException e2) {
            if (0 != 0) {
                is.close();
            }
        } catch (Throwable th) {
            if (0 != 0) {
                try {
                    is.close();
                } catch (IOException e3) {
                }
            }
            throw th;
        }
        return mime;
    }

    public String getOriginalMimeType(Uri uri) {
        if (uri != null && Uri.EMPTY != uri) {
            return getOriginalMimeType(convertUriToPath(uri));
        }
        throw new IllegalArgumentException("Given uri is not valid");
    }

    public int checkRightsStatus(String path) {
        return checkRightsStatus(path, 0);
    }

    public int checkRightsStatus(Uri uri) {
        if (uri != null && Uri.EMPTY != uri) {
            return checkRightsStatus(convertUriToPath(uri));
        }
        throw new IllegalArgumentException("Given uri is not valid");
    }

    public int checkRightsStatus(String path, int action) {
        if (path != null && !path.equals("") && DrmStore.Action.isValid(action)) {
            return _checkRightsStatus(this.mUniqueId, path, action);
        }
        throw new IllegalArgumentException("Given path or action is not valid");
    }

    public int checkRightsStatus(Uri uri, int action) {
        if (uri != null && Uri.EMPTY != uri) {
            return checkRightsStatus(convertUriToPath(uri), action);
        }
        throw new IllegalArgumentException("Given uri is not valid");
    }

    public int removeRights(String path) {
        if (path != null && !path.equals("")) {
            return _removeRights(this.mUniqueId, path);
        }
        throw new IllegalArgumentException("Given path should be non null");
    }

    public int removeRights(Uri uri) {
        if (uri != null && Uri.EMPTY != uri) {
            return removeRights(convertUriToPath(uri));
        }
        throw new IllegalArgumentException("Given uri is not valid");
    }

    public int removeAllRights() {
        EventHandler eventHandler = this.mEventHandler;
        if (eventHandler == null) {
            return ERROR_UNKNOWN;
        }
        return this.mEventHandler.sendMessage(eventHandler.obtainMessage(1001)) ? 0 : -2000;
    }

    public int openConvertSession(String mimeType) {
        if (mimeType != null && !mimeType.equals("")) {
            return _openConvertSession(this.mUniqueId, mimeType);
        }
        throw new IllegalArgumentException("Path or the mimeType should be non null");
    }

    public DrmConvertedStatus convertData(int convertId, byte[] inputData) {
        if (inputData != null && inputData.length > 0) {
            return _convertData(this.mUniqueId, convertId, inputData);
        }
        throw new IllegalArgumentException("Given inputData should be non null");
    }

    public DrmConvertedStatus closeConvertSession(int convertId) {
        return _closeConvertSession(this.mUniqueId, convertId);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private int getEventType(int infoType) {
        if (infoType == 1 || infoType == 2 || infoType == 3) {
            return 1002;
        }
        return -1;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private int getErrorType(int infoType) {
        if (infoType == 1 || infoType == 2 || infoType == 3) {
            return 2006;
        }
        return -1;
    }

    private String convertUriToPath(Uri uri) {
        if (uri == null) {
            return null;
        }
        String scheme = uri.getScheme();
        if (scheme == null || scheme.equals("") || scheme.equals(ContentResolver.SCHEME_FILE)) {
            return uri.getPath();
        }
        if (scheme.equals(IntentFilter.SCHEME_HTTP) || scheme.equals(IntentFilter.SCHEME_HTTPS)) {
            return uri.toString();
        }
        if (scheme.equals("content")) {
            Cursor cursor = null;
            try {
                Cursor cursor2 = this.mContext.getContentResolver().query(uri, new String[]{"_data"}, null, null, null);
                if (cursor2 == null || cursor2.getCount() == 0 || !cursor2.moveToFirst()) {
                    throw new IllegalArgumentException("Given Uri could not be found in media store");
                }
                String path = cursor2.getString(cursor2.getColumnIndexOrThrow("_data"));
                cursor2.close();
                return path;
            } catch (SQLiteException e) {
                throw new IllegalArgumentException("Given Uri is not formatted in a way so that it can be found in media store.");
            } catch (Throwable th) {
                if (0 != 0) {
                    cursor.close();
                }
                throw th;
            }
        } else {
            throw new IllegalArgumentException("Given Uri scheme is not supported");
        }
    }

    private void createEventThreads() {
        if (this.mEventHandler == null && this.mInfoHandler == null) {
            this.mInfoThread = new HandlerThread("DrmManagerClient.InfoHandler");
            this.mInfoThread.start();
            this.mInfoHandler = new InfoHandler(this.mInfoThread.getLooper());
            this.mEventThread = new HandlerThread("DrmManagerClient.EventHandler");
            this.mEventThread.start();
            this.mEventHandler = new EventHandler(this.mEventThread.getLooper());
        }
    }

    private void createListeners() {
        _setListeners(this.mUniqueId, new WeakReference(this));
    }
}
