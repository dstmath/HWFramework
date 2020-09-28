package android.telephony;

import android.annotation.SystemApi;
import android.content.ComponentName;
import android.content.Context;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.os.RemoteException;
import android.telephony.mbms.DownloadProgressListener;
import android.telephony.mbms.DownloadRequest;
import android.telephony.mbms.DownloadStatusListener;
import android.telephony.mbms.FileInfo;
import android.telephony.mbms.InternalDownloadProgressListener;
import android.telephony.mbms.InternalDownloadSessionCallback;
import android.telephony.mbms.InternalDownloadStatusListener;
import android.telephony.mbms.MbmsDownloadReceiver;
import android.telephony.mbms.MbmsDownloadSessionCallback;
import android.telephony.mbms.MbmsTempFileProvider;
import android.telephony.mbms.MbmsUtils;
import android.telephony.mbms.vendor.IMbmsDownloadService;
import android.util.Log;
import java.io.File;
import java.io.IOException;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

public class MbmsDownloadSession implements AutoCloseable {
    public static final String DEFAULT_TOP_LEVEL_TEMP_DIRECTORY = "androidMbmsTempFileRoot";
    private static final String DESTINATION_SANITY_CHECK_FILE_NAME = "destinationSanityCheckFile";
    public static final String EXTRA_MBMS_COMPLETED_FILE_URI = "android.telephony.extra.MBMS_COMPLETED_FILE_URI";
    public static final String EXTRA_MBMS_DOWNLOAD_REQUEST = "android.telephony.extra.MBMS_DOWNLOAD_REQUEST";
    public static final String EXTRA_MBMS_DOWNLOAD_RESULT = "android.telephony.extra.MBMS_DOWNLOAD_RESULT";
    public static final String EXTRA_MBMS_FILE_INFO = "android.telephony.extra.MBMS_FILE_INFO";
    private static final String LOG_TAG = MbmsDownloadSession.class.getSimpleName();
    @SystemApi
    public static final String MBMS_DOWNLOAD_SERVICE_ACTION = "android.telephony.action.EmbmsDownload";
    public static final String MBMS_DOWNLOAD_SERVICE_OVERRIDE_METADATA = "mbms-download-service-override";
    public static final int RESULT_CANCELLED = 2;
    public static final int RESULT_DOWNLOAD_FAILURE = 6;
    public static final int RESULT_EXPIRED = 3;
    public static final int RESULT_FILE_ROOT_UNREACHABLE = 8;
    public static final int RESULT_IO_ERROR = 4;
    public static final int RESULT_OUT_OF_STORAGE = 7;
    public static final int RESULT_SERVICE_ID_NOT_DEFINED = 5;
    public static final int RESULT_SUCCESSFUL = 1;
    public static final int STATUS_ACTIVELY_DOWNLOADING = 1;
    public static final int STATUS_PENDING_DOWNLOAD = 2;
    public static final int STATUS_PENDING_DOWNLOAD_WINDOW = 4;
    public static final int STATUS_PENDING_REPAIR = 3;
    public static final int STATUS_UNKNOWN = 0;
    private static AtomicBoolean sIsInitialized = new AtomicBoolean(false);
    private final Context mContext;
    private IBinder.DeathRecipient mDeathRecipient = new IBinder.DeathRecipient() {
        /* class android.telephony.MbmsDownloadSession.AnonymousClass1 */

        @Override // android.os.IBinder.DeathRecipient
        public void binderDied() {
            MbmsDownloadSession.this.sendErrorToApp(3, "Received death notification");
        }
    };
    private final InternalDownloadSessionCallback mInternalCallback;
    private final Map<DownloadProgressListener, InternalDownloadProgressListener> mInternalDownloadProgressListeners = new HashMap();
    private final Map<DownloadStatusListener, InternalDownloadStatusListener> mInternalDownloadStatusListeners = new HashMap();
    private AtomicReference<IMbmsDownloadService> mService = new AtomicReference<>(null);
    private int mSubscriptionId = -1;

    @Retention(RetentionPolicy.SOURCE)
    public @interface DownloadResultCode {
    }

    @Retention(RetentionPolicy.SOURCE)
    public @interface DownloadStatus {
    }

    private MbmsDownloadSession(Context context, Executor executor, int subscriptionId, MbmsDownloadSessionCallback callback) {
        this.mContext = context;
        this.mSubscriptionId = subscriptionId;
        this.mInternalCallback = new InternalDownloadSessionCallback(callback, executor);
    }

    public static MbmsDownloadSession create(Context context, Executor executor, MbmsDownloadSessionCallback callback) {
        return create(context, executor, SubscriptionManager.getDefaultSubscriptionId(), callback);
    }

    public static MbmsDownloadSession create(Context context, Executor executor, int subscriptionId, final MbmsDownloadSessionCallback callback) {
        if (sIsInitialized.compareAndSet(false, true)) {
            MbmsDownloadSession session = new MbmsDownloadSession(context, executor, subscriptionId, callback);
            final int result = session.bindAndInitialize();
            if (result == 0) {
                return session;
            }
            sIsInitialized.set(false);
            executor.execute(new Runnable() {
                /* class android.telephony.MbmsDownloadSession.AnonymousClass2 */

                public void run() {
                    MbmsDownloadSessionCallback.this.onError(result, null);
                }
            });
            return null;
        }
        throw new IllegalStateException("Cannot have two active instances");
    }

    private int bindAndInitialize() {
        return MbmsUtils.startBinding(this.mContext, MBMS_DOWNLOAD_SERVICE_ACTION, new ServiceConnection() {
            /* class android.telephony.MbmsDownloadSession.AnonymousClass3 */

            @Override // android.content.ServiceConnection
            public void onServiceConnected(ComponentName name, IBinder service) {
                IMbmsDownloadService downloadService = IMbmsDownloadService.Stub.asInterface(service);
                try {
                    int result = downloadService.initialize(MbmsDownloadSession.this.mSubscriptionId, MbmsDownloadSession.this.mInternalCallback);
                    if (result == -1) {
                        MbmsDownloadSession.this.close();
                        throw new IllegalStateException("Middleware must not return an unknown error code");
                    } else if (result != 0) {
                        MbmsDownloadSession.this.sendErrorToApp(result, "Error returned during initialization");
                        MbmsDownloadSession.sIsInitialized.set(false);
                    } else {
                        try {
                            downloadService.asBinder().linkToDeath(MbmsDownloadSession.this.mDeathRecipient, 0);
                            MbmsDownloadSession.this.mService.set(downloadService);
                        } catch (RemoteException e) {
                            MbmsDownloadSession.this.sendErrorToApp(3, "Middleware lost during initialization");
                            MbmsDownloadSession.sIsInitialized.set(false);
                        }
                    }
                } catch (RemoteException e2) {
                    Log.e(MbmsDownloadSession.LOG_TAG, "Service died before initialization");
                    MbmsDownloadSession.sIsInitialized.set(false);
                } catch (RuntimeException e3) {
                    Log.e(MbmsDownloadSession.LOG_TAG, "Runtime exception during initialization");
                    MbmsDownloadSession.this.sendErrorToApp(103, e3.toString());
                    MbmsDownloadSession.sIsInitialized.set(false);
                }
            }

            @Override // android.content.ServiceConnection
            public void onServiceDisconnected(ComponentName name) {
                Log.w(MbmsDownloadSession.LOG_TAG, "bindAndInitialize: Remote service disconnected");
                MbmsDownloadSession.sIsInitialized.set(false);
                MbmsDownloadSession.this.mService.set(null);
            }
        });
    }

    public void requestUpdateFileServices(List<String> classList) {
        IMbmsDownloadService downloadService = this.mService.get();
        if (downloadService != null) {
            try {
                int returnCode = downloadService.requestUpdateFileServices(this.mSubscriptionId, classList);
                if (returnCode != -1) {
                    if (returnCode != 0) {
                        sendErrorToApp(returnCode, null);
                    }
                    return;
                }
                close();
                throw new IllegalStateException("Middleware must not return an unknown error code");
            } catch (RemoteException e) {
                Log.w(LOG_TAG, "Remote process died");
                this.mService.set(null);
                sIsInitialized.set(false);
                sendErrorToApp(3, null);
            }
        } else {
            throw new IllegalStateException("Middleware not yet bound");
        }
    }

    public void setTempFileRootDirectory(File tempFileRootDirectory) {
        IMbmsDownloadService downloadService = this.mService.get();
        if (downloadService != null) {
            try {
                validateTempFileRootSanity(tempFileRootDirectory);
                try {
                    String filePath = tempFileRootDirectory.getCanonicalPath();
                    try {
                        int result = downloadService.setTempFileRootDirectory(this.mSubscriptionId, filePath);
                        if (result == -1) {
                            close();
                            throw new IllegalStateException("Middleware must not return an unknown error code");
                        } else if (result != 0) {
                            sendErrorToApp(result, null);
                        } else {
                            this.mContext.getSharedPreferences(MbmsTempFileProvider.TEMP_FILE_ROOT_PREF_FILE_NAME, 0).edit().putString(MbmsTempFileProvider.TEMP_FILE_ROOT_PREF_NAME, filePath).apply();
                        }
                    } catch (RemoteException e) {
                        this.mService.set(null);
                        sIsInitialized.set(false);
                        sendErrorToApp(3, null);
                    }
                } catch (IOException e2) {
                    throw new IllegalArgumentException("Unable to canonicalize the provided path: " + e2);
                }
            } catch (IOException e3) {
                throw new IllegalStateException("Got IOException checking directory sanity");
            }
        } else {
            throw new IllegalStateException("Middleware not yet bound");
        }
    }

    private void validateTempFileRootSanity(File tempFileRootDirectory) throws IOException {
        if (!tempFileRootDirectory.exists()) {
            throw new IllegalArgumentException("Provided directory does not exist");
        } else if (tempFileRootDirectory.isDirectory()) {
            String canonicalTempFilePath = tempFileRootDirectory.getCanonicalPath();
            if (this.mContext.getDataDir().getCanonicalPath().equals(canonicalTempFilePath)) {
                throw new IllegalArgumentException("Temp file root cannot be your data dir");
            } else if (this.mContext.getCacheDir().getCanonicalPath().equals(canonicalTempFilePath)) {
                throw new IllegalArgumentException("Temp file root cannot be your cache dir");
            } else if (this.mContext.getFilesDir().getCanonicalPath().equals(canonicalTempFilePath)) {
                throw new IllegalArgumentException("Temp file root cannot be your files dir");
            }
        } else {
            throw new IllegalArgumentException("Provided File is not a directory");
        }
    }

    public File getTempFileRootDirectory() {
        String path = this.mContext.getSharedPreferences(MbmsTempFileProvider.TEMP_FILE_ROOT_PREF_FILE_NAME, 0).getString(MbmsTempFileProvider.TEMP_FILE_ROOT_PREF_NAME, null);
        if (path != null) {
            return new File(path);
        }
        return null;
    }

    public void download(DownloadRequest request) {
        IMbmsDownloadService downloadService = this.mService.get();
        if (downloadService != null) {
            if (this.mContext.getSharedPreferences(MbmsTempFileProvider.TEMP_FILE_ROOT_PREF_FILE_NAME, 0).getString(MbmsTempFileProvider.TEMP_FILE_ROOT_PREF_NAME, null) == null) {
                File tempRootDirectory = new File(this.mContext.getFilesDir(), DEFAULT_TOP_LEVEL_TEMP_DIRECTORY);
                tempRootDirectory.mkdirs();
                setTempFileRootDirectory(tempRootDirectory);
            }
            checkDownloadRequestDestination(request);
            try {
                int result = downloadService.download(request);
                if (result == 0) {
                    writeDownloadRequestToken(request);
                } else if (result != -1) {
                    sendErrorToApp(result, null);
                } else {
                    close();
                    throw new IllegalStateException("Middleware must not return an unknown error code");
                }
            } catch (RemoteException e) {
                this.mService.set(null);
                sIsInitialized.set(false);
                sendErrorToApp(3, null);
            }
        } else {
            throw new IllegalStateException("Middleware not yet bound");
        }
    }

    public List<DownloadRequest> listPendingDownloads() {
        IMbmsDownloadService downloadService = this.mService.get();
        if (downloadService != null) {
            try {
                return downloadService.listPendingDownloads(this.mSubscriptionId);
            } catch (RemoteException e) {
                this.mService.set(null);
                sIsInitialized.set(false);
                sendErrorToApp(3, null);
                return Collections.emptyList();
            }
        } else {
            throw new IllegalStateException("Middleware not yet bound");
        }
    }

    public void addStatusListener(DownloadRequest request, Executor executor, DownloadStatusListener listener) {
        IMbmsDownloadService downloadService = this.mService.get();
        if (downloadService != null) {
            InternalDownloadStatusListener internalListener = new InternalDownloadStatusListener(listener, executor);
            try {
                int result = downloadService.addStatusListener(request, internalListener);
                if (result == -1) {
                    close();
                    throw new IllegalStateException("Middleware must not return an unknown error code");
                } else if (result == 0) {
                    this.mInternalDownloadStatusListeners.put(listener, internalListener);
                } else if (result != 402) {
                    sendErrorToApp(result, null);
                } else {
                    throw new IllegalArgumentException("Unknown download request.");
                }
            } catch (RemoteException e) {
                this.mService.set(null);
                sIsInitialized.set(false);
                sendErrorToApp(3, null);
            }
        } else {
            throw new IllegalStateException("Middleware not yet bound");
        }
    }

    public void removeStatusListener(DownloadRequest request, DownloadStatusListener listener) {
        try {
            IMbmsDownloadService downloadService = this.mService.get();
            if (downloadService != null) {
                InternalDownloadStatusListener internalListener = this.mInternalDownloadStatusListeners.get(listener);
                if (internalListener != null) {
                    try {
                        int result = downloadService.removeStatusListener(request, internalListener);
                        if (result == -1) {
                            close();
                            throw new IllegalStateException("Middleware must not return an unknown error code");
                        } else if (result == 0) {
                            InternalDownloadStatusListener internalCallback = this.mInternalDownloadStatusListeners.remove(listener);
                            if (internalCallback != null) {
                                internalCallback.stop();
                            }
                        } else if (result != 402) {
                            sendErrorToApp(result, null);
                        } else {
                            throw new IllegalArgumentException("Unknown download request.");
                        }
                    } catch (RemoteException e) {
                        this.mService.set(null);
                        sIsInitialized.set(false);
                        sendErrorToApp(3, null);
                        InternalDownloadStatusListener internalCallback2 = this.mInternalDownloadStatusListeners.remove(listener);
                        if (internalCallback2 != null) {
                            internalCallback2.stop();
                        }
                    }
                } else {
                    throw new IllegalArgumentException("Provided listener was never registered");
                }
            } else {
                throw new IllegalStateException("Middleware not yet bound");
            }
        } finally {
            InternalDownloadStatusListener internalCallback3 = this.mInternalDownloadStatusListeners.remove(listener);
            if (internalCallback3 != null) {
                internalCallback3.stop();
            }
        }
    }

    public void addProgressListener(DownloadRequest request, Executor executor, DownloadProgressListener listener) {
        IMbmsDownloadService downloadService = this.mService.get();
        if (downloadService != null) {
            InternalDownloadProgressListener internalListener = new InternalDownloadProgressListener(listener, executor);
            try {
                int result = downloadService.addProgressListener(request, internalListener);
                if (result == -1) {
                    close();
                    throw new IllegalStateException("Middleware must not return an unknown error code");
                } else if (result == 0) {
                    this.mInternalDownloadProgressListeners.put(listener, internalListener);
                } else if (result != 402) {
                    sendErrorToApp(result, null);
                } else {
                    throw new IllegalArgumentException("Unknown download request.");
                }
            } catch (RemoteException e) {
                this.mService.set(null);
                sIsInitialized.set(false);
                sendErrorToApp(3, null);
            }
        } else {
            throw new IllegalStateException("Middleware not yet bound");
        }
    }

    public void removeProgressListener(DownloadRequest request, DownloadProgressListener listener) {
        try {
            IMbmsDownloadService downloadService = this.mService.get();
            if (downloadService != null) {
                InternalDownloadProgressListener internalListener = this.mInternalDownloadProgressListeners.get(listener);
                if (internalListener != null) {
                    try {
                        int result = downloadService.removeProgressListener(request, internalListener);
                        if (result == -1) {
                            close();
                            throw new IllegalStateException("Middleware must not return an unknown error code");
                        } else if (result == 0) {
                            InternalDownloadProgressListener internalCallback = this.mInternalDownloadProgressListeners.remove(listener);
                            if (internalCallback != null) {
                                internalCallback.stop();
                            }
                        } else if (result != 402) {
                            sendErrorToApp(result, null);
                        } else {
                            throw new IllegalArgumentException("Unknown download request.");
                        }
                    } catch (RemoteException e) {
                        this.mService.set(null);
                        sIsInitialized.set(false);
                        sendErrorToApp(3, null);
                        InternalDownloadProgressListener internalCallback2 = this.mInternalDownloadProgressListeners.remove(listener);
                        if (internalCallback2 != null) {
                            internalCallback2.stop();
                        }
                    }
                } else {
                    throw new IllegalArgumentException("Provided listener was never registered");
                }
            } else {
                throw new IllegalStateException("Middleware not yet bound");
            }
        } finally {
            InternalDownloadProgressListener internalCallback3 = this.mInternalDownloadProgressListeners.remove(listener);
            if (internalCallback3 != null) {
                internalCallback3.stop();
            }
        }
    }

    public void cancelDownload(DownloadRequest downloadRequest) {
        IMbmsDownloadService downloadService = this.mService.get();
        if (downloadService != null) {
            try {
                int result = downloadService.cancelDownload(downloadRequest);
                if (result != -1) {
                    if (result != 0) {
                        sendErrorToApp(result, null);
                    } else {
                        deleteDownloadRequestToken(downloadRequest);
                    }
                    return;
                }
                close();
                throw new IllegalStateException("Middleware must not return an unknown error code");
            } catch (RemoteException e) {
                this.mService.set(null);
                sIsInitialized.set(false);
                sendErrorToApp(3, null);
            }
        } else {
            throw new IllegalStateException("Middleware not yet bound");
        }
    }

    public void requestDownloadState(DownloadRequest downloadRequest, FileInfo fileInfo) {
        IMbmsDownloadService downloadService = this.mService.get();
        if (downloadService != null) {
            try {
                int result = downloadService.requestDownloadState(downloadRequest, fileInfo);
                if (result != -1) {
                    if (result != 0) {
                        if (result == 402) {
                            throw new IllegalArgumentException("Unknown download request.");
                        } else if (result != 403) {
                            sendErrorToApp(result, null);
                        } else {
                            throw new IllegalArgumentException("Unknown file.");
                        }
                    }
                    return;
                }
                close();
                throw new IllegalStateException("Middleware must not return an unknown error code");
            } catch (RemoteException e) {
                this.mService.set(null);
                sIsInitialized.set(false);
                sendErrorToApp(3, null);
            }
        } else {
            throw new IllegalStateException("Middleware not yet bound");
        }
    }

    public void resetDownloadKnowledge(DownloadRequest downloadRequest) {
        IMbmsDownloadService downloadService = this.mService.get();
        if (downloadService != null) {
            try {
                int result = downloadService.resetDownloadKnowledge(downloadRequest);
                if (result != -1) {
                    if (result != 0) {
                        if (result != 402) {
                            sendErrorToApp(result, null);
                        } else {
                            throw new IllegalArgumentException("Unknown download request.");
                        }
                    }
                    return;
                }
                close();
                throw new IllegalStateException("Middleware must not return an unknown error code");
            } catch (RemoteException e) {
                this.mService.set(null);
                sIsInitialized.set(false);
                sendErrorToApp(3, null);
            }
        } else {
            throw new IllegalStateException("Middleware not yet bound");
        }
    }

    @Override // java.lang.AutoCloseable
    public void close() {
        try {
            IMbmsDownloadService downloadService = this.mService.get();
            if (downloadService == null) {
                Log.i(LOG_TAG, "Service already dead");
                this.mService.set(null);
                sIsInitialized.set(false);
                this.mInternalCallback.stop();
                return;
            }
            downloadService.dispose(this.mSubscriptionId);
            this.mService.set(null);
            sIsInitialized.set(false);
            this.mInternalCallback.stop();
        } catch (RemoteException e) {
            Log.i(LOG_TAG, "Remote exception while disposing of service");
        } catch (Throwable th) {
            this.mService.set(null);
            sIsInitialized.set(false);
            this.mInternalCallback.stop();
            throw th;
        }
    }

    private void writeDownloadRequestToken(DownloadRequest request) {
        File token = getDownloadRequestTokenPath(request);
        if (!token.getParentFile().exists()) {
            token.getParentFile().mkdirs();
        }
        if (token.exists()) {
            String str = LOG_TAG;
            Log.w(str, "Download token " + token.getName() + " already exists");
            return;
        }
        try {
            if (!token.createNewFile()) {
                throw new RuntimeException("Failed to create download token for request " + request + ". Token location is " + token.getPath());
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to create download token for request " + request + " due to IOException " + e + ". Attempted to write to " + token.getPath());
        }
    }

    private void deleteDownloadRequestToken(DownloadRequest request) {
        File token = getDownloadRequestTokenPath(request);
        if (!token.isFile()) {
            String str = LOG_TAG;
            Log.w(str, "Attempting to delete non-existent download token at " + token);
        } else if (!token.delete()) {
            String str2 = LOG_TAG;
            Log.w(str2, "Couldn't delete download token at " + token);
        }
    }

    private void checkDownloadRequestDestination(DownloadRequest request) {
        File downloadRequestDestination = new File(request.getDestinationUri().getPath());
        if (downloadRequestDestination.isDirectory()) {
            File testFile = new File(MbmsTempFileProvider.getEmbmsTempFileDir(this.mContext), DESTINATION_SANITY_CHECK_FILE_NAME);
            File testFileDestination = new File(downloadRequestDestination, DESTINATION_SANITY_CHECK_FILE_NAME);
            try {
                if (!testFile.exists()) {
                    testFile.createNewFile();
                }
                if (testFile.renameTo(testFileDestination)) {
                    testFile.delete();
                    testFileDestination.delete();
                    return;
                }
                throw new IllegalArgumentException("Destination provided in the download request is invalid -- files in the temp file directory cannot be directly moved there.");
            } catch (IOException e) {
                throw new IllegalStateException("Got IOException while testing out the destination: " + e);
            } catch (Throwable th) {
                testFile.delete();
                testFileDestination.delete();
                throw th;
            }
        } else {
            throw new IllegalArgumentException("The destination path must be a directory");
        }
    }

    private File getDownloadRequestTokenPath(DownloadRequest request) {
        File tempFileLocation = MbmsUtils.getEmbmsTempFileDirForService(this.mContext, request.getFileServiceId());
        return new File(tempFileLocation, request.getHash() + MbmsDownloadReceiver.DOWNLOAD_TOKEN_SUFFIX);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void sendErrorToApp(int errorCode, String message) {
        this.mInternalCallback.onError(errorCode, message);
    }
}
