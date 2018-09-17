package com.android.contacts.update;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.util.SparseArray;
import com.android.contacts.util.HwLog;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.RejectedExecutionException;

public class DownloadService extends Service {
    public static final int MSG_CANCEL = 4;
    public static final int MSG_DONE = 2;
    public static final int MSG_FAILED = 0;
    public static final int MSG_OK = 3;
    public static final int MSG_PROCESS = 1;
    private static final String TAG = DownloadService.class.getSimpleName();
    private MyBinder mBinder;
    private final SparseArray<Messenger> mClients = new SparseArray();
    private int mCurrentJobId;
    private ExecutorService mExecutorService = Executors.newSingleThreadExecutor();
    private final SparseArray<DownloadProcessor> mRunningJobMap = new SparseArray();

    class MyBinder extends Binder {
        MyBinder() {
        }

        DownloadService getService() {
            return DownloadService.this;
        }
    }

    public IBinder onBind(Intent arg0) {
        return this.mBinder;
    }

    public void onCreate() {
        super.onCreate();
        if (HwLog.HWDBG) {
            HwLog.d(TAG, TAG + " onCreate");
        }
        this.mBinder = new MyBinder();
    }

    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent == null) {
            stopServiceIfAppropriate();
        } else if ("com.android.contacts.action.UPDATE_FILE".equals(intent.getAction())) {
            int fileId = intent.getIntExtra("fileId", 1);
            if (HwLog.HWDBG) {
                HwLog.d(TAG, fileId + " : onStartCommand");
            }
            if (UpdateHelper.getUpdaterInstance(fileId, this).tryUpdate()) {
                if (HwLog.HWDBG) {
                    HwLog.d(TAG, fileId + " : handleDownloadRequest");
                }
                handleDownloadRequest(fileId);
            } else {
                if (HwLog.HWDBG) {
                    HwLog.d(TAG, fileId + " : stopServiceIfAppropriate");
                }
                stopServiceIfAppropriate();
            }
        }
        return 1;
    }

    public void onDestroy() {
        if (HwLog.HWDBG) {
            HwLog.d(TAG, TAG + " onDestroy");
        }
        cancelAllRequestsAndShutdown();
        super.onDestroy();
    }

    public void registerListener(int fileId, Messenger msgr) {
        this.mClients.put(fileId, msgr);
    }

    public void unregisterListener(int fileId) {
        this.mClients.remove(fileId);
    }

    public synchronized void handleDownloadRequest(int fileId) {
        if (tryExecute(new DownloadProcessor(this, fileId, this.mCurrentJobId))) {
            sendMsg(fileId, Message.obtain(null, 1));
            this.mCurrentJobId++;
        } else {
            sendMsg(fileId, Message.obtain(null, 0));
        }
    }

    synchronized void sendMsg(int fileId, Message msg) {
        Messenger msgr = (Messenger) this.mClients.get(fileId);
        if (msgr != null) {
            try {
                msgr.send(msg);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
        return;
    }

    private synchronized boolean tryExecute(DownloadProcessor processor) {
        boolean z;
        try {
            this.mExecutorService.execute(processor);
            this.mRunningJobMap.put(this.mCurrentJobId, processor);
            z = true;
        } catch (RejectedExecutionException e) {
            HwLog.e(TAG, "Failed to excetute a job.", e);
            z = false;
        }
        return z;
    }

    synchronized void handleFinishDownload(int jobId) {
        this.mRunningJobMap.remove(jobId);
        stopServiceIfAppropriate();
    }

    public synchronized void handleCancelRequest(int fileId) {
        int jobId = fileId;
        DownloadProcessor processor = (DownloadProcessor) this.mRunningJobMap.get(jobId);
        this.mRunningJobMap.remove(jobId);
        if (processor != null) {
            processor.cancel(true);
        } else {
            HwLog.w(TAG, String.format("Tried to remove unknown job (id: %d)", new Object[]{Integer.valueOf(jobId)}));
        }
        stopServiceIfAppropriate();
    }

    private synchronized void cancelAllRequestsAndShutdown() {
        int size = this.mRunningJobMap.size();
        for (int i = 0; i < size; i++) {
            ((DownloadProcessor) this.mRunningJobMap.valueAt(i)).cancel(true);
        }
        this.mRunningJobMap.clear();
        this.mExecutorService.shutdown();
        if (HwLog.HWDBG) {
            HwLog.d(TAG, "mExecutorService.shutdown");
        }
    }

    private synchronized void stopServiceIfAppropriate() {
        if (this.mRunningJobMap.size() <= 0) {
            if (HwLog.HWFLOW) {
                HwLog.i(TAG, "No unfinished job. Stop this service.");
            }
            stopSelf();
        }
    }
}
