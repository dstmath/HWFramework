package com.android.server;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

public class HwTouchWeightService {
    private static final String FILE_PATH = "/sys/touchscreen/touch_weight";
    private static final int GET_WEIGHT_TIMEOUT = 1800;
    private static final int GET_WEIGHT_TIMEOUT_MESSAGE = 100;
    private static final int HAERT_BEAT_MESSAGE = 101;
    private static final int HAERT_BEAT_TIME = 1000;
    private static final String HAERT_BEAT_VALUE = "2";
    private static final String RUN_TOUCH_WEIGHT_VALUE = "1";
    private static final String STOP_TOUCH_WEIGHT_VALUE = "0";
    private static final String TAG = "HwTouchWeightService";
    private static volatile HwTouchWeightService sInstance = null;
    private Handler mHandler;
    private boolean mIsFeatureEnabled = false;
    private boolean mIsFeatureSupport = false;

    public HwTouchWeightService(Context context, Handler handler) {
        this.mHandler = new TouchWeightHandler(handler.getLooper());
        this.mIsFeatureSupport = isFeatureSupport();
    }

    private boolean isFeatureSupport() {
        if (new File(FILE_PATH).exists()) {
            return true;
        }
        return false;
    }

    public synchronized String getTouchWeightValue() {
        String str;
        String str2;
        if (!this.mIsFeatureSupport) {
            return null;
        }
        if (!this.mIsFeatureEnabled) {
            enableTouchWeight();
        }
        FileInputStream in = null;
        try {
            in = new FileInputStream(new File(FILE_PATH));
            byte[] bt = new byte[64];
            String val = new String(bt, 0, in.read(bt), "utf-8");
            Log.i("touch weight", "HwTouchWeightService getTouchWeightValue " + val);
            try {
                in.close();
            } catch (IOException e) {
                Log.e(TAG, "close IOException");
            }
            return val;
        } catch (FileNotFoundException e2) {
            Log.e(TAG, "getTouchWeightValue FileNotFoundException");
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e3) {
                    str = TAG;
                    str2 = "close IOException";
                }
            }
            resetTimeOut();
            Log.i("touch weight", "HwTouchWeightService getTouchWeightValue null");
            return null;
        } catch (IOException e4) {
            Log.e(TAG, "getTouchWeightValue IOException");
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e5) {
                    str = TAG;
                    str2 = "close IOException";
                }
            }
            resetTimeOut();
            Log.i("touch weight", "HwTouchWeightService getTouchWeightValue null");
            return null;
        } catch (Throwable th) {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e6) {
                    Log.e(TAG, "close IOException");
                }
            }
            throw th;
        }
        Log.e(str, str2);
        resetTimeOut();
        Log.i("touch weight", "HwTouchWeightService getTouchWeightValue null");
        return null;
    }

    public synchronized void resetTouchWeight() {
        if (this.mIsFeatureSupport) {
            if (this.mHandler.hasMessages(101)) {
                this.mHandler.removeMessages(101);
            }
            this.mIsFeatureEnabled = false;
            setTouchWeightValue("0");
        }
    }

    private final class TouchWeightHandler extends Handler {
        TouchWeightHandler(Looper looper) {
            super(looper, null, true);
        }

        @Override // android.os.Handler
        public void handleMessage(Message msg) {
            int i = msg.what;
            if (i == 100) {
                HwTouchWeightService.this.resetTouchWeight();
            } else if (i == 101) {
                HwTouchWeightService.this.startHeartBeat();
            }
        }
    }

    private void enableTouchWeight() {
        setTouchWeightValue("1");
        this.mIsFeatureEnabled = true;
        startHeartBeat();
    }

    private void setTouchWeightValue(String val) {
        Log.i("touch weight", "HwTouchWeightService setTouchWeightValue " + val);
        FileOutputStream fileOutWriteMode = null;
        try {
            fileOutWriteMode = new FileOutputStream(new File(FILE_PATH));
            fileOutWriteMode.write(val.getBytes("utf-8"));
            try {
                fileOutWriteMode.close();
            } catch (IOException e) {
                Log.e(TAG, "close IOException");
            }
        } catch (FileNotFoundException e2) {
            Log.e(TAG, "setTouchWeightValue FileNotFoundException");
            if (fileOutWriteMode != null) {
                fileOutWriteMode.close();
            }
        } catch (IOException e3) {
            Log.e(TAG, "setTouchWeightValue IOException");
            if (fileOutWriteMode != null) {
                fileOutWriteMode.close();
            }
        } catch (Throwable th) {
            if (fileOutWriteMode != null) {
                try {
                    fileOutWriteMode.close();
                } catch (IOException e4) {
                    Log.e(TAG, "close IOException");
                }
            }
            throw th;
        }
    }

    public static synchronized HwTouchWeightService getInstance(Context context, Handler handler) {
        HwTouchWeightService hwTouchWeightService;
        synchronized (HwTouchWeightService.class) {
            if (sInstance == null) {
                sInstance = new HwTouchWeightService(context, handler);
            }
            hwTouchWeightService = sInstance;
        }
        return hwTouchWeightService;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void startHeartBeat() {
        setTouchWeightValue("2");
        Handler handler = this.mHandler;
        if (handler == null) {
            Log.e(TAG, "startHeartBeat mHandler is null");
            return;
        }
        this.mHandler.sendMessageDelayed(handler.obtainMessage(101), 1000);
    }

    private void resetTimeOut() {
        if (this.mHandler.hasMessages(100)) {
            this.mHandler.removeMessages(100);
        }
        this.mHandler.sendMessageDelayed(this.mHandler.obtainMessage(100), 1800);
    }
}
