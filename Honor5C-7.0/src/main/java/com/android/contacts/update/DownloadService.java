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
    private static final String TAG = null;
    private MyBinder mBinder;
    private final SparseArray<Messenger> mClients;
    private int mCurrentJobId;
    private ExecutorService mExecutorService;
    private final SparseArray<DownloadProcessor> mRunningJobMap;

    class MyBinder extends Binder {
        MyBinder() {
        }

        DownloadService getService() {
            return DownloadService.this;
        }
    }

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.contacts.update.DownloadService.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.contacts.update.DownloadService.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 5 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e9
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 6 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.contacts.update.DownloadService.<clinit>():void");
    }

    public DownloadService() {
        this.mExecutorService = Executors.newSingleThreadExecutor();
        this.mRunningJobMap = new SparseArray();
        this.mClients = new SparseArray();
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
            return MSG_PROCESS;
        }
        if ("com.android.contacts.action.UPDATE_FILE".equals(intent.getAction())) {
            int fileId = intent.getIntExtra("fileId", MSG_PROCESS);
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
        return MSG_PROCESS;
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
            sendMsg(fileId, Message.obtain(null, MSG_PROCESS));
            this.mCurrentJobId += MSG_PROCESS;
        } else {
            sendMsg(fileId, Message.obtain(null, MSG_FAILED));
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
    }

    private synchronized boolean tryExecute(DownloadProcessor processor) {
        try {
            this.mExecutorService.execute(processor);
            this.mRunningJobMap.put(this.mCurrentJobId, processor);
        } catch (RejectedExecutionException e) {
            HwLog.e(TAG, "Failed to excetute a job.", e);
            return false;
        }
        return true;
    }

    synchronized void handleFinishDownload(int jobId) {
        this.mRunningJobMap.remove(jobId);
        stopServiceIfAppropriate();
    }

    public synchronized void handleCancelRequest(int fileId) {
        int jobId = fileId;
        DownloadProcessor processor = (DownloadProcessor) this.mRunningJobMap.get(fileId);
        this.mRunningJobMap.remove(fileId);
        if (processor != null) {
            processor.cancel(true);
        } else {
            String str = TAG;
            Object[] objArr = new Object[MSG_PROCESS];
            objArr[MSG_FAILED] = Integer.valueOf(fileId);
            HwLog.w(str, String.format("Tried to remove unknown job (id: %d)", objArr));
        }
        stopServiceIfAppropriate();
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private synchronized void cancelAllRequestsAndShutdown() {
        int i = MSG_FAILED;
        while (true) {
            if (i >= this.mRunningJobMap.size()) {
                break;
            }
            ((DownloadProcessor) this.mRunningJobMap.valueAt(i)).cancel(true);
            i += MSG_PROCESS;
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
