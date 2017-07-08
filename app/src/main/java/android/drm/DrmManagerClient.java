package android.drm;

import android.content.ContentValues;
import android.content.Context;
import android.content.IntentFilter;
import android.database.Cursor;
import android.database.sqlite.SQLiteException;
import android.drm.DrmStore.Action;
import android.net.ProxyInfo;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.provider.VoicemailContract.Voicemails;
import android.rms.iaware.AwareNRTConstant;
import android.service.voice.VoiceInteractionSession;
import android.speech.SpeechRecognizer;
import android.speech.tts.TextToSpeech.Engine;
import android.telecom.AudioState;
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
    private final CloseGuard mCloseGuard;
    private final AtomicBoolean mClosed;
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

    private class EventHandler extends Handler {
        public EventHandler(Looper looper) {
            super(looper);
        }

        public void handleMessage(Message msg) {
            DrmEvent drmEvent = null;
            DrmErrorEvent error = null;
            HashMap<String, Object> attributes = new HashMap();
            switch (msg.what) {
                case DrmManagerClient.ACTION_REMOVE_ALL_RIGHTS /*1001*/:
                    if (DrmManagerClient.this._removeAllRights(DrmManagerClient.this.mUniqueId) != 0) {
                        error = new DrmErrorEvent(DrmManagerClient.this.mUniqueId, DrmErrorEvent.TYPE_REMOVE_ALL_RIGHTS_FAILED, null);
                        break;
                    } else {
                        drmEvent = new DrmEvent(DrmManagerClient.this.mUniqueId, DrmManagerClient.ACTION_REMOVE_ALL_RIGHTS, null);
                        break;
                    }
                case DrmManagerClient.ACTION_PROCESS_DRM_INFO /*1002*/:
                    DrmInfo drmInfo = msg.obj;
                    DrmInfoStatus status = DrmManagerClient.this._processDrmInfo(DrmManagerClient.this.mUniqueId, drmInfo);
                    attributes.put(DrmEvent.DRM_INFO_STATUS_OBJECT, status);
                    attributes.put(DrmEvent.DRM_INFO_OBJECT, drmInfo);
                    if (status != null && 1 == status.statusCode) {
                        drmEvent = new DrmEvent(DrmManagerClient.this.mUniqueId, DrmManagerClient.this.getEventType(status.infoType), null, attributes);
                        break;
                    } else {
                        error = new DrmErrorEvent(DrmManagerClient.this.mUniqueId, DrmManagerClient.this.getErrorType(status != null ? status.infoType : drmInfo.getInfoType()), null, attributes);
                        break;
                    }
                    break;
                default:
                    Log.e(DrmManagerClient.TAG, "Unknown message type " + msg.what);
                    return;
            }
            if (!(DrmManagerClient.this.mOnEventListener == null || drmEvent == null)) {
                DrmManagerClient.this.mOnEventListener.onEvent(DrmManagerClient.this, drmEvent);
            }
            if (!(DrmManagerClient.this.mOnErrorListener == null || error == null)) {
                DrmManagerClient.this.mOnErrorListener.onError(DrmManagerClient.this, error);
            }
        }
    }

    private class InfoHandler extends Handler {
        public static final int INFO_EVENT_TYPE = 1;

        public InfoHandler(Looper looper) {
            super(looper);
        }

        public void handleMessage(Message msg) {
            DrmInfoEvent info = null;
            DrmErrorEvent drmErrorEvent = null;
            switch (msg.what) {
                case INFO_EVENT_TYPE /*1*/:
                    int uniqueId = msg.arg1;
                    int infoType = msg.arg2;
                    String message = msg.obj.toString();
                    switch (infoType) {
                        case INFO_EVENT_TYPE /*1*/:
                        case Engine.DEFAULT_STREAM /*3*/:
                        case AudioState.ROUTE_WIRED_HEADSET /*4*/:
                        case AudioState.ROUTE_WIRED_OR_EARPIECE /*5*/:
                        case SpeechRecognizer.ERROR_SPEECH_TIMEOUT /*6*/:
                            info = new DrmInfoEvent(uniqueId, infoType, message);
                            break;
                        case AudioState.ROUTE_BLUETOOTH /*2*/:
                            try {
                                DrmUtils.removeFile(message);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            info = new DrmInfoEvent(uniqueId, infoType, message);
                            break;
                        default:
                            drmErrorEvent = new DrmErrorEvent(uniqueId, infoType, message);
                            break;
                    }
                    if (!(DrmManagerClient.this.mOnInfoListener == null || info == null)) {
                        DrmManagerClient.this.mOnInfoListener.onInfo(DrmManagerClient.this, info);
                    }
                    if (!(DrmManagerClient.this.mOnErrorListener == null || drmErrorEvent == null)) {
                        DrmManagerClient.this.mOnErrorListener.onError(DrmManagerClient.this, drmErrorEvent);
                    }
                default:
                    Log.e(DrmManagerClient.TAG, "Unknown message type " + msg.what);
            }
        }
    }

    public interface OnErrorListener {
        void onError(DrmManagerClient drmManagerClient, DrmErrorEvent drmErrorEvent);
    }

    public interface OnEventListener {
        void onEvent(DrmManagerClient drmManagerClient, DrmEvent drmEvent);
    }

    public interface OnInfoListener {
        void onInfo(DrmManagerClient drmManagerClient, DrmInfoEvent drmInfoEvent);
    }

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.drm.DrmManagerClient.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.drm.DrmManagerClient.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 5 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 6 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: android.drm.DrmManagerClient.<clinit>():void");
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

    private native DrmInfoStatus _processDrmInfo(int i, DrmInfo drmInfo);

    private native void _release(int i);

    private native int _removeAllRights(int i);

    private native int _removeRights(int i, String str);

    private native int _saveRights(int i, DrmRights drmRights, String str, String str2);

    private native void _setListeners(int i, Object obj);

    public static void notify(Object thisReference, int uniqueId, int infoType, String message) {
        DrmManagerClient instance = (DrmManagerClient) ((WeakReference) thisReference).get();
        if (instance != null && instance.mInfoHandler != null) {
            instance.mInfoHandler.sendMessage(instance.mInfoHandler.obtainMessage(1, uniqueId, infoType, message));
        }
    }

    public DrmManagerClient(Context context) {
        this.mClosed = new AtomicBoolean();
        this.mCloseGuard = CloseGuard.get();
        this.mContext = context;
        createEventThreads();
        this.mUniqueId = _initialize();
        this.mCloseGuard.open("release");
    }

    protected void finalize() throws Throwable {
        try {
            this.mCloseGuard.warnIfOpen();
            close();
        } finally {
            super.finalize();
        }
    }

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
        ArrayList<String> descriptions = new ArrayList();
        for (int i = ERROR_NONE; i < supportInfos.length; i++) {
            descriptions.add(supportInfos[i].getDescriprition());
        }
        return (String[]) descriptions.toArray(new String[descriptions.size()]);
    }

    public ContentValues getConstraints(String path, int action) {
        if (path != null && !path.equals(ProxyInfo.LOCAL_EXCL_LIST) && Action.isValid(action)) {
            return _getConstraints(this.mUniqueId, path, action);
        }
        throw new IllegalArgumentException("Given usage or path is invalid/null");
    }

    public ContentValues getMetadata(String path) {
        if (path != null && !path.equals(ProxyInfo.LOCAL_EXCL_LIST)) {
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
        if (!(rightsPath == null || rightsPath.equals(ProxyInfo.LOCAL_EXCL_LIST))) {
            DrmUtils.writeToFile(rightsPath, drmRights.getData());
        }
        return _saveRights(this.mUniqueId, drmRights, rightsPath, contentPath);
    }

    public void installDrmEngine(String engineFilePath) {
        if (engineFilePath == null || engineFilePath.equals(ProxyInfo.LOCAL_EXCL_LIST)) {
            throw new IllegalArgumentException("Given engineFilePath: " + engineFilePath + "is not valid");
        }
        _installDrmEngine(this.mUniqueId, engineFilePath);
    }

    public boolean canHandle(String path, String mimeType) {
        if ((path != null && !path.equals(ProxyInfo.LOCAL_EXCL_LIST)) || (mimeType != null && !mimeType.equals(ProxyInfo.LOCAL_EXCL_LIST))) {
            return _canHandle(this.mUniqueId, path, mimeType);
        }
        throw new IllegalArgumentException("Path or the mimetype should be non null");
    }

    public boolean canHandle(Uri uri, String mimeType) {
        if ((uri != null && Uri.EMPTY != uri) || (mimeType != null && !mimeType.equals(ProxyInfo.LOCAL_EXCL_LIST))) {
            return canHandle(convertUriToPath(uri), mimeType);
        }
        throw new IllegalArgumentException("Uri or the mimetype should be non null");
    }

    public int processDrmInfo(DrmInfo drmInfo) {
        if (drmInfo == null || !drmInfo.isValid()) {
            throw new IllegalArgumentException("Given drmInfo is invalid/null");
        } else if (this.mEventHandler == null) {
            return ERROR_UNKNOWN;
        } else {
            if (this.mEventHandler.sendMessage(this.mEventHandler.obtainMessage(ACTION_PROCESS_DRM_INFO, drmInfo))) {
                return ERROR_NONE;
            }
            return ERROR_UNKNOWN;
        }
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
        if ((path != null && !path.equals(ProxyInfo.LOCAL_EXCL_LIST)) || (mimeType != null && !mimeType.equals(ProxyInfo.LOCAL_EXCL_LIST))) {
            return _getDrmObjectType(this.mUniqueId, path, mimeType);
        }
        throw new IllegalArgumentException("Path or the mimetype should be non null");
    }

    public int getDrmObjectType(Uri uri, String mimeType) {
        if ((uri == null || Uri.EMPTY == uri) && (mimeType == null || mimeType.equals(ProxyInfo.LOCAL_EXCL_LIST))) {
            throw new IllegalArgumentException("Uri or the mimetype should be non null");
        }
        String path = ProxyInfo.LOCAL_EXCL_LIST;
        try {
            path = convertUriToPath(uri);
        } catch (Exception e) {
            Log.w(TAG, "Given Uri could not be found in media store");
        }
        return getDrmObjectType(path, mimeType);
    }

    public String getOriginalMimeType(String path) {
        Throwable th;
        if (path == null || path.equals(ProxyInfo.LOCAL_EXCL_LIST)) {
            throw new IllegalArgumentException("Given path should be non null");
        }
        String mime = null;
        FileInputStream fileInputStream = null;
        FileDescriptor fd = null;
        try {
            File file = new File(path);
            if (file.exists()) {
                FileInputStream is = new FileInputStream(file);
                try {
                    fd = is.getFD();
                    fileInputStream = is;
                } catch (IOException e) {
                    fileInputStream = is;
                    if (fileInputStream != null) {
                        try {
                            fileInputStream.close();
                        } catch (IOException e2) {
                        }
                    }
                    return mime;
                } catch (Throwable th2) {
                    th = th2;
                    fileInputStream = is;
                    if (fileInputStream != null) {
                        try {
                            fileInputStream.close();
                        } catch (IOException e3) {
                        }
                    }
                    throw th;
                }
            }
            mime = _getOriginalMimeType(this.mUniqueId, path, fd);
            if (fileInputStream != null) {
                try {
                    fileInputStream.close();
                } catch (IOException e4) {
                }
            }
        } catch (IOException e5) {
            if (fileInputStream != null) {
                fileInputStream.close();
            }
            return mime;
        } catch (Throwable th3) {
            th = th3;
            if (fileInputStream != null) {
                fileInputStream.close();
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
        return checkRightsStatus(path, (int) ERROR_NONE);
    }

    public int checkRightsStatus(Uri uri) {
        if (uri != null && Uri.EMPTY != uri) {
            return checkRightsStatus(convertUriToPath(uri));
        }
        throw new IllegalArgumentException("Given uri is not valid");
    }

    public int checkRightsStatus(String path, int action) {
        if (path != null && !path.equals(ProxyInfo.LOCAL_EXCL_LIST) && Action.isValid(action)) {
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
        if (path != null && !path.equals(ProxyInfo.LOCAL_EXCL_LIST)) {
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
        if (this.mEventHandler == null) {
            return ERROR_UNKNOWN;
        }
        if (this.mEventHandler.sendMessage(this.mEventHandler.obtainMessage(ACTION_REMOVE_ALL_RIGHTS))) {
            return ERROR_NONE;
        }
        return ERROR_UNKNOWN;
    }

    public int openConvertSession(String mimeType) {
        if (mimeType != null && !mimeType.equals(ProxyInfo.LOCAL_EXCL_LIST)) {
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

    private int getEventType(int infoType) {
        switch (infoType) {
            case AudioState.ROUTE_EARPIECE /*1*/:
            case AudioState.ROUTE_BLUETOOTH /*2*/:
            case Engine.DEFAULT_STREAM /*3*/:
                return ACTION_PROCESS_DRM_INFO;
            default:
                return INVALID_SESSION;
        }
    }

    private int getErrorType(int infoType) {
        switch (infoType) {
            case AudioState.ROUTE_EARPIECE /*1*/:
            case AudioState.ROUTE_BLUETOOTH /*2*/:
            case Engine.DEFAULT_STREAM /*3*/:
                return AwareNRTConstant.EAPA_DME_ID;
            default:
                return INVALID_SESSION;
        }
    }

    private String convertUriToPath(Uri uri) {
        if (uri == null) {
            return null;
        }
        String scheme = uri.getScheme();
        if (scheme == null || scheme.equals(ProxyInfo.LOCAL_EXCL_LIST) || scheme.equals(WifiManager.EXTRA_PASSPOINT_ICON_FILE)) {
            return uri.getPath();
        }
        if (scheme.equals(IntentFilter.SCHEME_HTTP)) {
            return uri.toString();
        }
        if (scheme.equals(VoiceInteractionSession.KEY_CONTENT)) {
            Cursor cursor = null;
            try {
                cursor = this.mContext.getContentResolver().query(uri, new String[]{Voicemails._DATA}, null, null, null);
                if (!(cursor == null || cursor.getCount() == 0)) {
                    if (cursor.moveToFirst()) {
                        String path = cursor.getString(cursor.getColumnIndexOrThrow(Voicemails._DATA));
                        if (cursor == null) {
                            return path;
                        }
                        cursor.close();
                        return path;
                    }
                }
                throw new IllegalArgumentException("Given Uri could not be found in media store");
            } catch (SQLiteException e) {
                throw new IllegalArgumentException("Given Uri is not formatted in a way so that it can be found in media store.");
            } catch (Throwable th) {
                if (cursor != null) {
                    cursor.close();
                }
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
