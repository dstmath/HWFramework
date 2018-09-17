package com.android.server.rms.shrinker;

import android.app.mtm.MultiTaskManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.util.Log;
import com.android.server.rms.IShrinker;
import com.android.server.rms.utils.Interrupt;

public class ProcessStopShrinker implements IShrinker {
    public static final String MODE_KEY = "mode";
    public static final String PID_KEY = "pid";
    public static final int RECLAIM_FORCESTOP_MODE = 2;
    public static final int RECLAIM_KILL_MODE = 1;
    static final String TAG = "RMS.ProcessStopShrinker";
    private final Handler mHandler;
    private final Interrupt mInterrupt = new Interrupt();

    public ProcessStopShrinker() {
        HandlerThread handlerThread = new HandlerThread(TAG);
        handlerThread.start();
        this.mHandler = new Handler(handlerThread.getLooper()) {
            public void handleMessage(Message msg) {
                int[] pids = msg.getData().getIntArray("pid");
                ProcessStopShrinker.this.mInterrupt.reset();
                switch (msg.what) {
                    case 1:
                        ProcessStopShrinker.this.killProcess(pids);
                        break;
                    case 2:
                        ProcessStopShrinker.this.forceStopProcess(pids);
                        break;
                }
                super.handleMessage(msg);
            }
        };
    }

    public int reclaim(String reason, Bundle extras) {
        if (extras == null) {
            return 0;
        }
        int mode = extras.getInt("mode");
        Message message = this.mHandler.obtainMessage();
        message.what = mode;
        message.setData(extras);
        this.mHandler.sendMessage(message);
        return 1;
    }

    public void killProcess(int[] pids) {
        if (pids != null && pids.length > 0) {
            int i = 0;
            while (i < pids.length) {
                if (this.mInterrupt.checkInterruptAndReset() || pids[i] <= 0) {
                    Log.d(TAG, "kill process stoped!");
                    break;
                }
                Log.d(TAG, "kill process: " + pids[i]);
                MultiTaskManager instance = MultiTaskManager.getInstance();
                if (instance != null) {
                    instance.killProcess(pids[i], false);
                }
                i++;
            }
        }
    }

    public void forceStopProcess(int[] pids) {
        if (pids != null && pids.length > 0) {
            int i = 0;
            while (i < pids.length) {
                if (this.mInterrupt.checkInterruptAndReset() || pids[i] <= 0) {
                    Log.d(TAG, "forceStop process stoped!");
                    break;
                }
                Log.d(TAG, "forceStop process: " + pids[i]);
                MultiTaskManager instance = MultiTaskManager.getInstance();
                if (instance != null) {
                    instance.forcestopApps(pids[i]);
                }
                i++;
            }
        }
    }

    public void interrupt() {
        Log.d(TAG, "interrupt");
        this.mHandler.removeMessages(1);
        this.mHandler.removeMessages(2);
        this.mInterrupt.trigger();
    }
}
