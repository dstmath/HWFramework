package android.widget.sr;

import android.graphics.Bitmap;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.rms.iaware.AppTypeInfo;
import android.util.Log;
import java.util.ArrayList;

public class SRBitmapManagerImpl extends HwSuperResolutionListener implements SRBitmapManager {
    private static final boolean DB = true;
    private static final int DO_SR = 1;
    private static final int HISI_DDK_MODE = 1;
    public static final int NOT_STARTED_STATE = 1;
    private static final int PROCESS_ERROR_CALLBACK_NOT_START = 18;
    private static final int PROCESS_ERROR_COUNT_LIMIT = 30;
    private static final int PROCESS_RETURN_NOT_START = 2;
    private static final int PROCESS_TIMEOUT_CODE = -1;
    private static final int QUEUE_LIMIT = 3;
    private static final int RET_OK = 0;
    private static int SR_MAX_EDGE_LIMIT = 960;
    private static int SR_MAX_PIXEL = 691200;
    private static int SR_MIN_EDGE_LIMIT = AppTypeInfo.PG_TYPE_BASE;
    private static int SR_MIN_PIXEL = 250000;
    private static final int SR_RATIO = 1;
    public static final int STARTED_STATE = 3;
    private static final int START_COUNT_LIMIT = 5;
    private static final int START_TWICE = 16;
    private static final int STOP_DDK = 2;
    private static final long STOP_WAIT_TIME = 60000;
    private static final String TAG = "SRBitmapManager";
    private static SRBitmapManagerImpl sInstance = null;
    private volatile boolean mCondition;
    private boolean mDoing = false;
    private int mErrorCode = 0;
    private Handler mHandler;
    private final HandlerThread mManagerThread = new HandlerThread("queueThread");
    private boolean mNeverDoSR = false;
    private final Object mNeverDoSRLock = new Object();
    private int mProcessErrorCount;
    private Bitmap mProcessSrcBitmap;
    private Bitmap mProcesseDstBitmap;
    private int mSRStatus = 1;
    private HwSuperResolution mSuperResolution;
    private ArrayList<SRBitmapTask> mTaskQueue = new ArrayList<>();

    private class ManagerHandler extends Handler {
        public ManagerHandler(Looper looper) {
            super(looper);
        }

        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case 1:
                    SRBitmapManagerImpl.this.doSuperResolution();
                    return;
                case 2:
                    SRBitmapManagerImpl.this.SRClientStop();
                    return;
                default:
                    return;
            }
        }
    }

    private SRBitmapManagerImpl() {
        this.mManagerThread.setDaemon(true);
        this.mManagerThread.start();
        this.mHandler = new ManagerHandler(this.mManagerThread.getLooper());
        this.mSuperResolution = new HwSuperResolution(null, this);
    }

    public static synchronized SRBitmapManagerImpl getInstance() {
        synchronized (SRBitmapManagerImpl.class) {
            if (sInstance == null) {
                sInstance = new SRBitmapManagerImpl();
                SRBitmapManagerImpl sRBitmapManagerImpl = sInstance;
                return sRBitmapManagerImpl;
            }
            SRBitmapManagerImpl sRBitmapManagerImpl2 = sInstance;
            return sRBitmapManagerImpl2;
        }
    }

    public int getSRStatus() {
        return this.mSRStatus;
    }

    public void srBitmap(Bitmap bitmap) {
        Log.i(TAG, "srBitmap: " + bitmap);
        if (bitmap == null || !bitmap.isMutable()) {
            Log.w(TAG, "srBitmap: bitmap null or unMutable = " + bitmap);
            return;
        }
        int h = bitmap.getHeight();
        int w = bitmap.getWidth();
        int maxEdge = w > h ? w : h;
        if ((w > h ? h : w) < SR_MIN_EDGE_LIMIT || maxEdge > SR_MAX_EDGE_LIMIT) {
            DebugUtil.debugPixelFail(bitmap);
            return;
        }
        int pixelCount = w * h;
        if (pixelCount < SR_MIN_PIXEL || pixelCount > SR_MAX_PIXEL) {
            DebugUtil.debugPixelFail(bitmap);
        } else if (getNeverDOSR()) {
            Log.w(TAG, "srBitmapManager stopped");
        } else {
            SRBitmapTask task = new SRBitmapTask(bitmap);
            if (task.getAshmemBitmap() != null) {
                pushTask(task);
                task.waitTask(this);
                DebugUtil.drawBitmapPixel(bitmap);
            }
        }
    }

    private HwSuperResolution getSuperResolution() {
        return this.mSuperResolution;
    }

    private void SRClientStart() {
        if (this.mSRStatus != 3) {
            Log.i(TAG, "before client start");
            for (int i = 0; i < 5; i++) {
                int start_return = getSuperResolution().start(1);
                if (start_return == 0) {
                    waitQueueThread();
                    if (this.mErrorCode == 0) {
                        this.mSRStatus = 3;
                        Log.i(TAG, "after client start success");
                        return;
                    }
                } else if (start_return == 16) {
                    break;
                } else {
                    this.mSuperResolution = new HwSuperResolution(null, this);
                }
            }
            setNeverDoSR(true);
            Log.i(TAG, "after client start for error");
        }
    }

    private void SRClientProcess(SRBitmapTask task) {
        Log.i(TAG, "sr SRClientProcess start");
        int process_return = getSuperResolution().process(task.getAshmemBitmap(), 1);
        if (process_return == 0) {
            waitQueueThread();
            int i = this.mErrorCode;
            if (i == 0) {
                Log.i(TAG, "sr SRClientProcess success : " + task.getAshmemBitmap());
                task.setAshmemBitmap(this.mProcessSrcBitmap, this.mProcesseDstBitmap);
                this.mProcessErrorCount = 0;
            } else if (i == 18) {
                this.mProcessErrorCount++;
                Log.w(TAG, "sr SRClientProcess not start in callback");
            }
        } else if (process_return == 2) {
            this.mProcessErrorCount++;
            Log.w(TAG, "sr SRClientProcess not start in return");
        }
        notifyBitmapThread(task);
        if (this.mProcessErrorCount >= 30) {
            setNeverDoSR(true);
        }
        Log.i(TAG, "sr SRClientProcess end");
    }

    /* access modifiers changed from: private */
    public void SRClientStop() {
        Log.i(TAG, "sr SRClientStop start");
        if (getSuperResolution().stop() != 0) {
            this.mSuperResolution = new HwSuperResolution(null, this);
        } else {
            waitQueueThread();
        }
        this.mProcessErrorCount = 0;
        this.mSRStatus = 1;
        Log.i(TAG, "sr SRClientStop end");
    }

    private void sendDoSRMessage() {
        this.mHandler.sendEmptyMessage(1);
    }

    private synchronized SRBitmapTask pollTask() {
        if (this.mTaskQueue.size() == 0) {
            Log.w(TAG, "pollTask : queue has no task");
            return null;
        }
        this.mDoing = true;
        return this.mTaskQueue.remove(0);
    }

    public synchronized void removeTaskFromQueue(SRBitmapTask task) {
        if (this.mTaskQueue.size() == 0) {
            Log.w(TAG, "removeTaskFromQueue : queue has no task");
        } else {
            this.mTaskQueue.remove(task);
        }
    }

    private synchronized void clearBitmapQueue() {
        int size = this.mTaskQueue.size();
        for (int i = 0; i < size; i++) {
            notifyBitmapThread(this.mTaskQueue.get(i));
        }
        this.mTaskQueue.clear();
    }

    private synchronized void sendNextSrMessage() {
        if (this.mTaskQueue.size() == 0) {
            SRClientStop();
        } else {
            sendDoSRMessage();
        }
        this.mDoing = false;
    }

    private synchronized void pushTask(SRBitmapTask task) {
        if (this.mTaskQueue.size() > 3) {
            Log.i(TAG, "queue oversize:" + this.mTaskQueue.size() + " and pollTask head task");
            notifyBitmapThread(this.mTaskQueue.remove(0));
        }
        this.mTaskQueue.add(task);
        if (this.mTaskQueue.size() == 1 && !this.mDoing) {
            sendDoSRMessage();
        }
    }

    private void setNeverDoSR(boolean neverDoSR) {
        synchronized (this.mNeverDoSRLock) {
            this.mNeverDoSR = neverDoSR;
        }
    }

    private boolean getNeverDOSR() {
        boolean z;
        synchronized (this.mNeverDoSRLock) {
            z = this.mNeverDoSR;
        }
        return z;
    }

    public void onStartDone() {
        this.mErrorCode = 0;
        notifyQueueThread();
        Log.i(TAG, "start Done");
    }

    public void onProcessDone(Bitmap src, Bitmap des) {
        this.mErrorCode = 0;
        this.mProcessSrcBitmap = src;
        this.mProcesseDstBitmap = des;
        notifyQueueThread();
        Log.i(TAG, "process Done");
    }

    public void onStopDone() {
        this.mErrorCode = 0;
        notifyQueueThread();
        Log.i(TAG, "stop Done");
    }

    public void onError(int errCode) {
        super.onError(errCode);
        this.mErrorCode = errCode;
        notifyQueueThread();
        Log.w(TAG, "error : " + errCode);
    }

    public void onTimeOut(Bitmap bitmap) {
        super.onTimeOut(bitmap);
        this.mErrorCode = -1;
        notifyQueueThread();
        Log.w(TAG, "process timeout");
    }

    public void onServiceDied() {
        super.onServiceDied();
        this.mSRStatus = 1;
        notifyQueueThread();
        Log.w(TAG, "service died");
    }

    private void notifyBitmapThread(SRBitmapTask task) {
        task.notifyTask();
    }

    private void notifyQueueThread() {
        synchronized (this.mManagerThread) {
            Log.i(TAG, "queue thread notify");
            this.mCondition = true;
            this.mManagerThread.notifyAll();
        }
    }

    private void waitQueueThread() {
        synchronized (this.mManagerThread) {
            Log.i(TAG, "mCondition is " + this.mCondition);
            while (!this.mCondition) {
                try {
                    Log.i(TAG, "queue thread wait");
                    this.mManagerThread.wait();
                } catch (InterruptedException e) {
                    Log.e(TAG, "SRbitmapManager: " + e.toString());
                }
            }
            this.mCondition = false;
        }
    }

    /* access modifiers changed from: private */
    public void doSuperResolution() {
        SRClientStart();
        if (getNeverDOSR()) {
            clearBitmapQueue();
            return;
        }
        SRBitmapTask bitmapTask = pollTask();
        if (bitmapTask != null) {
            SRClientProcess(bitmapTask);
        }
        sendNextSrMessage();
    }
}
