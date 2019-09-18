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
    private static volatile HwTouchWeightService mInstance = null;
    private boolean isFeatureEnabled = false;
    private boolean isFeatureSupport = false;
    private Handler mHandler;

    private final class TouchWeightHandler extends Handler {
        public TouchWeightHandler(Looper looper) {
            super(looper, null, true);
        }

        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 100:
                    HwTouchWeightService.this.resetTouchWeight();
                    break;
                case 101:
                    HwTouchWeightService.this.startHeartBeat();
                    break;
                default:
                    return;
            }
        }
    }

    public HwTouchWeightService(Context context, Handler handler) {
        this.mHandler = new TouchWeightHandler(handler.getLooper());
        this.isFeatureSupport = isFeatureSupport();
    }

    private boolean isFeatureSupport() {
        if (new File(FILE_PATH).exists()) {
            return true;
        }
        return false;
    }

    /* JADX WARNING: Unknown top exception splitter block from list: {B:24:0x0056=Splitter:B:24:0x0056, B:36:0x0067=Splitter:B:36:0x0067} */
    public synchronized String getTouchWeightValue() {
        String val;
        if (!this.isFeatureSupport) {
            return null;
        }
        if (!this.isFeatureEnabled) {
            enableTouchWeight();
        }
        FileInputStream in = null;
        try {
            in = new FileInputStream(new File(FILE_PATH));
            byte[] b = new byte[64];
            Log.i("touch weight", "HwTouchWeightService getTouchWeightValue " + val);
            try {
                in.close();
            } catch (IOException e) {
            }
        } catch (FileNotFoundException e2) {
            if (in != null) {
                in.close();
            }
            resetTimeOut();
            Log.i("touch weight", "HwTouchWeightService getTouchWeightValue null");
            return null;
        } catch (IOException e3) {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e4) {
                }
            }
            resetTimeOut();
            Log.i("touch weight", "HwTouchWeightService getTouchWeightValue null");
            return null;
        } catch (Throwable th) {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e5) {
                }
            }
            throw th;
        }
        return val;
    }

    public synchronized void resetTouchWeight() {
        if (this.isFeatureSupport) {
            if (this.mHandler.hasMessages(101)) {
                this.mHandler.removeMessages(101);
            }
            this.isFeatureEnabled = false;
            setTouchWeightValue("0");
        }
    }

    private void enableTouchWeight() {
        setTouchWeightValue("1");
        this.isFeatureEnabled = true;
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
            }
        } catch (FileNotFoundException e2) {
            if (fileOutWriteMode != null) {
                fileOutWriteMode.close();
            }
        } catch (IOException e3) {
            if (fileOutWriteMode != null) {
                fileOutWriteMode.close();
            }
        } catch (Throwable th) {
            if (fileOutWriteMode != null) {
                try {
                    fileOutWriteMode.close();
                } catch (IOException e4) {
                }
            }
            throw th;
        }
    }

    public static synchronized HwTouchWeightService getInstance(Context context, Handler handler) {
        HwTouchWeightService hwTouchWeightService;
        synchronized (HwTouchWeightService.class) {
            if (mInstance == null) {
                mInstance = new HwTouchWeightService(context, handler);
            }
            hwTouchWeightService = mInstance;
        }
        return hwTouchWeightService;
    }

    /* access modifiers changed from: private */
    public void startHeartBeat() {
        setTouchWeightValue("2");
        if (this.mHandler == null) {
            Log.e(TAG, "startHeartBeat mHandler is null");
            return;
        }
        this.mHandler.sendMessageDelayed(this.mHandler.obtainMessage(101), 1000);
    }

    private void resetTimeOut() {
        if (this.mHandler.hasMessages(100)) {
            this.mHandler.removeMessages(100);
        }
        this.mHandler.sendMessageDelayed(this.mHandler.obtainMessage(100), 1800);
    }
}
