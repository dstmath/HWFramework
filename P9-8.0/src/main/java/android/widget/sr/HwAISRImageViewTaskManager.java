package android.widget.sr;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;
import java.lang.ref.WeakReference;
import java.util.Vector;

public class HwAISRImageViewTaskManager extends HwSuperResolutionListener {
    public static final int DDK_COMMAND_SUCCESS = 0;
    public static final int DDK_ERROR_AI_EEXIST = 16;
    public static final int DDK_ERROR_AI_EIO = 5;
    public static final int DDK_ERROR_AI_ENODEV = 18;
    public static final int DDK_ERROR_AI_ENOENT = 2;
    public static final int DDK_ERROR_AI_ENOMODEL = 34;
    private static final int DDK_STATUS_NOT_STARTED = 0;
    private static final int DDK_STATUS_PROCESSING = 4;
    private static final int DDK_STATUS_STARTED = 2;
    private static final int DDK_STATUS_STARTING = 1;
    private static final int DDK_STATUS_STOPPING = 3;
    private static final int HISI_DDK_WECHAT_MODE = 1;
    private static final long KEEP_WAITING_DURATION = 60000;
    private static final int MAX_PROCESS_ERROR_COUNT = 30;
    private static final int MAX_START_ERROR_COUNT = 5;
    private static final int SR_IMAGEVIEW_RATIO = 3;
    private static final String SR_TAG = "SuperResolution";
    private static final long TIMEOUT_DURATION = 3000;
    private static HwAISRImageViewTaskManager sInstance;
    private static boolean sIsDDKStatusAvailable = true;
    private static boolean sIsSuperResolutionSupport = Utils.isSuperResolutionSupport();
    private Runnable mClearWhenTimeoutRunnable = new Runnable() {
        public void run() {
            if (HwAISRImageViewTaskManager.this.mCurrentSRTaskInfo != null) {
                HwAISRImageViewTaskManager.this.onTimeOut(HwAISRImageViewTaskManager.this.mCurrentSRTaskInfo.mSrcBitmap);
            }
        }
    };
    private Context mContext;
    private SRTaskInfo mCurrentSRTaskInfo;
    private int mDDKStatus = 0;
    private Handler mManageHandler;
    private HandlerThread mManageThread;
    private SRMemoryRecorder mMemoryRecorder = new SRMemoryRecorder();
    private int mProcessErrorCount = 0;
    private int mStartErrorCount = 0;
    private HwSuperResolution mSuperResolution;
    private Vector<SRTaskInfo> mTaskQueue = new Vector();

    public interface SRTaskCallback {
        SRTaskInfo getCurrentSRTask();

        void onSRTaskFail(SRTaskInfo sRTaskInfo);

        void onSRTaskSuccess(SRTaskInfo sRTaskInfo, Bitmap bitmap);
    }

    public static class SRTaskInfo {
        private WeakReference<SRTaskCallback> mCallback;
        private Bitmap mSrcBitmap;
        private WeakReference<Drawable> mSrcDrawable;

        /* synthetic */ SRTaskInfo(SRTaskCallback callback, Bitmap src, Drawable drawable, SRTaskInfo -this3) {
            this(callback, src, drawable);
        }

        private SRTaskInfo(SRTaskCallback callback) {
            this(callback, null, null);
        }

        private SRTaskInfo(SRTaskCallback callback, Bitmap src, Drawable drawable) {
            this.mCallback = new WeakReference(callback);
            this.mSrcBitmap = src;
            this.mSrcDrawable = new WeakReference(drawable);
        }
    }

    public static boolean isSuperResolutionAvailable() {
        if (sIsSuperResolutionSupport) {
            return sIsDDKStatusAvailable;
        }
        return false;
    }

    private static void setIsDDKStatusAvailable(boolean flag) {
        sIsDDKStatusAvailable = flag;
    }

    public static synchronized HwAISRImageViewTaskManager getInstance(Context context) {
        HwAISRImageViewTaskManager hwAISRImageViewTaskManager;
        synchronized (HwAISRImageViewTaskManager.class) {
            if (sInstance == null) {
                Context appContext = null;
                if (context != null) {
                    appContext = context.getApplicationContext();
                }
                sInstance = new HwAISRImageViewTaskManager(appContext);
            }
            hwAISRImageViewTaskManager = sInstance;
        }
        return hwAISRImageViewTaskManager;
    }

    private HwAISRImageViewTaskManager(Context context) {
        createManageThread();
        this.mContext = context;
    }

    private HwSuperResolution getSuperResolution() {
        if (this.mSuperResolution == null) {
            this.mSuperResolution = new HwSuperResolution(this.mContext, this);
        }
        return this.mSuperResolution;
    }

    private void increaseStartErrorCount() {
        Log.d(SR_TAG, "increaseStartErrorCount");
        this.mStartErrorCount++;
        if (this.mStartErrorCount >= 5) {
            Log.w(SR_TAG, "increaseStartErrorCount: start error too many times");
            setIsDDKStatusAvailable(false);
        }
    }

    private void increaseProcessErrorCount() {
        Log.d(SR_TAG, "increaseProcessErrorCount");
        this.mProcessErrorCount++;
        if (this.mProcessErrorCount >= 30) {
            Log.w(SR_TAG, "increaseProcessErrorCount: process error too many times");
            setIsDDKStatusAvailable(false);
        }
    }

    private void resetStatus() {
        this.mDDKStatus = 0;
        this.mCurrentSRTaskInfo = null;
        this.mTaskQueue.clear();
        this.mManageHandler.removeCallbacksAndMessages(null);
    }

    private void createManageThread() {
        this.mManageThread = new HandlerThread("SRManagerThread");
        this.mManageThread.start();
        this.mManageHandler = new Handler(this.mManageThread.getLooper());
    }

    private boolean isCurrentTaskProcessing() {
        return this.mCurrentSRTaskInfo != null;
    }

    public SRTaskInfo postNewTask(SRTaskCallback callback, Drawable drawable) {
        if (!isSuperResolutionAvailable()) {
            Log.w(SR_TAG, "postNewTask: SuperResolution is not available now.");
            return null;
        } else if (callback == null || drawable == null) {
            return null;
        } else {
            SRTaskInfo taskInfo = new SRTaskInfo(callback, null, drawable, null);
            postNewTask(taskInfo);
            return taskInfo;
        }
    }

    private void postNewTask(final SRTaskInfo taskInfo) {
        Log.d(SR_TAG, "postNewTask: ");
        if (taskInfo != null) {
            this.mManageHandler.post(new Runnable() {
                public void run() {
                    HwAISRImageViewTaskManager.this.scheduleTask(taskInfo);
                }
            });
        }
    }

    private synchronized void scheduleTask(SRTaskInfo taskInfo) {
        this.mTaskQueue.add(taskInfo);
        if (!isCurrentTaskProcessing() && this.mTaskQueue.size() == 1) {
            getAndDoNextTask();
        }
    }

    private synchronized boolean doNewTask(SRTaskInfo taskInfo) {
        Drawable drawable = null;
        synchronized (this) {
            Log.d(SR_TAG, "doNewTask: taskInfo = " + taskInfo);
            this.mCurrentSRTaskInfo = taskInfo;
            if (isSuperResolutionAvailable()) {
                if (taskInfo.mSrcDrawable != null) {
                    drawable = (Drawable) taskInfo.mSrcDrawable.get();
                }
                taskInfo.mSrcBitmap = SRUtils.drawableToBitmap(drawable);
                if (taskInfo.mSrcBitmap == null) {
                    Log.w(SR_TAG, "doNewTask: mSrcBitmap is empty ! ");
                    this.mDDKStatus = 4;
                    return false;
                } else if (!enoughRoomForSize((taskInfo.mSrcBitmap.getByteCount() * 3) * 3)) {
                    Log.w(SR_TAG, "doNewTask: there is not enough room ! ");
                    this.mDDKStatus = 4;
                    return false;
                } else if (process(taskInfo.mSrcBitmap)) {
                    this.mManageHandler.postDelayed(this.mClearWhenTimeoutRunnable, TIMEOUT_DURATION);
                    return true;
                } else {
                    Log.w(SR_TAG, "doNewTask: process fail");
                    return false;
                }
            }
            Log.w(SR_TAG, "postNewTask: SuperResolution is not available now.");
            this.mDDKStatus = 4;
            return false;
        }
    }

    /* JADX WARNING: Missing block: B:18:0x0050, code:
            return;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private synchronized void getAndDoNextTask() {
        Log.d(SR_TAG, "getAndDoNextTask: ");
        if (!isCurrentTaskProcessing()) {
            if (this.mTaskQueue.size() != 0) {
                switch (this.mDDKStatus) {
                    case 0:
                        start();
                        break;
                    case 2:
                        SRTaskInfo taskInfo = (SRTaskInfo) this.mTaskQueue.get(0);
                        this.mTaskQueue.remove(0);
                        if (!(doNewTask(taskInfo) || this.mCurrentSRTaskInfo == null)) {
                            onProcessFail(this.mCurrentSRTaskInfo.mSrcBitmap);
                            break;
                        }
                    default:
                        Log.w(SR_TAG, "getAndDoNextTask: some exception mStatus = " + this.mDDKStatus);
                        break;
                }
            }
            Log.w(SR_TAG, "getAndDoNextTask: the taskQueue is empty. ");
            return;
        }
        Log.w(SR_TAG, "getAndDoNextTask: there is a task still doing. We shall never call this method at this point. ");
    }

    public void postCancelTask(final SRTaskInfo taskInfo) {
        if (isSuperResolutionAvailable()) {
            this.mManageHandler.post(new Runnable() {
                public void run() {
                    HwAISRImageViewTaskManager.this.doCancelTask(taskInfo);
                }
            });
        } else {
            Log.w(SR_TAG, "postCancelTask: SuperResolution is not available now.");
        }
    }

    /* JADX WARNING: Missing block: B:16:0x003e, code:
            return;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private synchronized void doCancelTask(SRTaskInfo taskInfo) {
        Log.d(SR_TAG, "doCancelTask: taskInfo = " + taskInfo);
        if (!isSuperResolutionAvailable()) {
            Log.w(SR_TAG, "doCancelTask: SuperResolution is not available now.");
        } else if (taskInfo == null) {
            Log.w(SR_TAG, "doCancelTask: can't cancel an null taskInfo");
        } else if (taskInfo != this.mCurrentSRTaskInfo) {
            for (SRTaskInfo iSRTaskInfo : this.mTaskQueue) {
                if (taskInfo == iSRTaskInfo) {
                    this.mTaskQueue.remove(taskInfo);
                    Log.d(SR_TAG, "doCancelTask: remove taskInfo = " + taskInfo);
                    return;
                }
            }
            Log.w(SR_TAG, "doCancelTask: can't find taskInfo, it has been processed or removed");
        }
    }

    private synchronized void clearCurrentTaskAndGetNext() {
        this.mManageHandler.removeCallbacks(this.mClearWhenTimeoutRunnable);
        this.mCurrentSRTaskInfo = null;
        this.mDDKStatus = 2;
        if (this.mTaskQueue.size() == 0) {
            stop();
        } else {
            getAndDoNextTask();
        }
    }

    public void addMemory(int size) {
        this.mMemoryRecorder.add(size);
    }

    public void removeMemory(int size) {
        this.mMemoryRecorder.remove(size);
    }

    public boolean enoughRoomForSize(int size) {
        return this.mMemoryRecorder.enoughRoomForSize(size);
    }

    /* JADX WARNING: Missing block: B:16:0x0057, code:
            return false;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private synchronized boolean start() {
        Log.d(SR_TAG, "start: ");
        if (this.mDDKStatus == 0) {
            this.mDDKStatus = 1;
            int result = getSuperResolution().start(1);
            if (result == 0) {
                Log.d(SR_TAG, "start: command success");
                return true;
            }
            Log.w(SR_TAG, "start: command fail, result = " + result);
            if (result == 16) {
                Log.w(SR_TAG, "start: already called");
            } else {
                this.mSuperResolution = null;
            }
            increaseStartErrorCount();
            resetStatus();
        } else {
            Log.w(SR_TAG, "start: DDK status is " + this.mDDKStatus + ". Can not call start now");
        }
    }

    /* JADX WARNING: Missing block: B:18:0x004e, code:
            return false;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private synchronized boolean process(Bitmap bitmap) {
        Log.d(SR_TAG, "process: ");
        if (this.mDDKStatus == 2) {
            this.mDDKStatus = 4;
            int result = getSuperResolution().process(bitmap, 3);
            if (result == 0) {
                Log.d(SR_TAG, "process: command success");
                return true;
            }
            Log.w(SR_TAG, "process: command fail, result = " + result);
            if (result == 2 || result == 18) {
                increaseProcessErrorCount();
            }
        } else {
            Log.w(SR_TAG, "process: DDK status is " + this.mDDKStatus + ". Can not call process now");
        }
    }

    /* JADX WARNING: Missing block: B:14:0x0050, code:
            return false;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private synchronized boolean stop() {
        Log.d(SR_TAG, "stop: ");
        if (this.mDDKStatus == 2) {
            this.mDDKStatus = 3;
            int result = getSuperResolution().stop();
            if (result == 0) {
                Log.d(SR_TAG, "stop: command success");
                return true;
            }
            Log.w(SR_TAG, "stop: command fail, result = " + result);
            this.mSuperResolution = null;
            this.mStartErrorCount = 0;
            this.mProcessErrorCount = 0;
            resetStatus();
        } else {
            Log.w(SR_TAG, "stop: DDK status is " + this.mDDKStatus + ". Can not call process now");
        }
    }

    public synchronized void onError(int errCode) {
        Log.w(SR_TAG, "onError: error code = " + errCode);
        switch (this.mDDKStatus) {
            case 1:
                this.mSuperResolution = null;
                increaseStartErrorCount();
                resetStatus();
                break;
            case 3:
                this.mStartErrorCount = 0;
                this.mProcessErrorCount = 0;
                resetStatus();
                break;
            case 4:
                if (this.mCurrentSRTaskInfo != null) {
                    onProcessFail(this.mCurrentSRTaskInfo.mSrcBitmap);
                    break;
                }
                break;
            default:
                Log.w(SR_TAG, "onError: DDK status is " + this.mDDKStatus + ". Abnormal callback.");
                break;
        }
    }

    /* JADX WARNING: Missing block: B:20:0x004b, code:
            return;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public synchronized void onProcessDone(Bitmap src, Bitmap des) {
        if (this.mDDKStatus != 4) {
            Log.w(SR_TAG, "onProcessDone: DDK status is " + this.mDDKStatus + ". Abnormal callback.");
        } else if (this.mCurrentSRTaskInfo == null || this.mCurrentSRTaskInfo.mSrcBitmap != src) {
            Log.w(SR_TAG, "onProcessDone: this task doesn't match");
        } else {
            SRTaskCallback callback = this.mCurrentSRTaskInfo.mCallback == null ? null : (SRTaskCallback) this.mCurrentSRTaskInfo.mCallback.get();
            SRTaskInfo taskInfo = this.mCurrentSRTaskInfo;
            if (callback != null) {
                callback.onSRTaskSuccess(taskInfo, des);
            }
            clearCurrentTaskAndGetNext();
        }
    }

    public synchronized void onTimeOut(Bitmap bitmap) {
        Log.d(SR_TAG, "onTimeOut: ");
        if (this.mDDKStatus != 4) {
            Log.w(SR_TAG, "onTimeOut: DDK status is " + this.mDDKStatus + ". Abnormal callback.");
        } else {
            onProcessFail(bitmap);
        }
    }

    public synchronized void onServiceDied() {
        Log.w(SR_TAG, "onServiceDied: ");
        if (this.mDDKStatus == 4 && this.mCurrentSRTaskInfo != null) {
            onProcessFail(this.mCurrentSRTaskInfo.mSrcBitmap);
        }
        this.mSuperResolution = null;
        this.mStartErrorCount = 0;
        this.mProcessErrorCount = 0;
        resetStatus();
    }

    public synchronized void onStartDone() {
        Log.d(SR_TAG, "onStartDone: ");
        if (this.mDDKStatus == 1) {
            Log.d(SR_TAG, "onStartDone: DDK has been started");
            this.mDDKStatus = 2;
            this.mStartErrorCount = 0;
            if (this.mTaskQueue.size() > 0) {
                getAndDoNextTask();
            } else if (this.mTaskQueue.size() == 0) {
                Log.d(SR_TAG, "onStartDone: no task to do");
                stop();
            }
        } else {
            Log.w(SR_TAG, "onStartDone: DDK status is " + this.mDDKStatus + ". Abnormal callback.");
        }
    }

    public synchronized void onStopDone() {
        Log.d(SR_TAG, "onStopDone: ");
        if (this.mDDKStatus == 3) {
            Log.d(SR_TAG, "onStopDone: DDK has been stopped");
            this.mDDKStatus = 0;
            this.mStartErrorCount = 0;
            this.mProcessErrorCount = 0;
            if (this.mTaskQueue.size() > 0) {
                getAndDoNextTask();
            }
        } else {
            Log.w(SR_TAG, "onStopDone:  DDK status is " + this.mDDKStatus + ". Abnormal callback.");
        }
    }

    /* JADX WARNING: Missing block: B:20:0x0054, code:
            return;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private synchronized void onProcessFail(Bitmap srcBitmap) {
        Log.d(SR_TAG, "onProcessFail: ");
        if (this.mDDKStatus != 4) {
            Log.w(SR_TAG, "onProcessFail: DDK status is " + this.mDDKStatus + ". Abnormal callback.");
        } else if (this.mCurrentSRTaskInfo == null || this.mCurrentSRTaskInfo.mSrcBitmap != srcBitmap) {
            Log.w(SR_TAG, "onProcessFail: this task doesn't match");
        } else {
            SRTaskCallback callback = this.mCurrentSRTaskInfo.mCallback == null ? null : (SRTaskCallback) this.mCurrentSRTaskInfo.mCallback.get();
            SRTaskInfo taskInfo = this.mCurrentSRTaskInfo;
            if (callback != null) {
                callback.onSRTaskFail(taskInfo);
            }
            clearCurrentTaskAndGetNext();
        }
    }
}
