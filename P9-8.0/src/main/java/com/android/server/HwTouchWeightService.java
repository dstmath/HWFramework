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

    /* JADX WARNING: Removed duplicated region for block: B:34:0x006a A:{SYNTHETIC, Splitter: B:34:0x006a} */
    /* JADX WARNING: Removed duplicated region for block: B:25:0x0054 A:{SYNTHETIC, Splitter: B:25:0x0054} */
    /* JADX WARNING: Removed duplicated region for block: B:39:0x0073 A:{SYNTHETIC, Splitter: B:39:0x0073} */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public synchronized String getTouchWeightValue() {
        String val;
        Throwable th;
        if (!this.isFeatureSupport) {
            return null;
        }
        if (!this.isFeatureEnabled) {
            enableTouchWeight();
        }
        FileInputStream in = null;
        try {
            FileInputStream in2 = new FileInputStream(new File(FILE_PATH));
            try {
                byte[] b = new byte[64];
                val = new String(b, 0, in2.read(b), "utf-8");
                Log.i("touch weight", "HwTouchWeightService getTouchWeightValue " + val);
                if (in2 != null) {
                    try {
                        in2.close();
                    } catch (IOException e) {
                    }
                }
            } catch (FileNotFoundException e2) {
                in = in2;
                if (in != null) {
                    try {
                        in.close();
                    } catch (IOException e3) {
                    }
                }
                resetTimeOut();
                Log.i("touch weight", "HwTouchWeightService getTouchWeightValue null");
                return null;
            } catch (IOException e4) {
                in = in2;
                if (in != null) {
                    try {
                        in.close();
                    } catch (IOException e5) {
                    }
                }
                resetTimeOut();
                Log.i("touch weight", "HwTouchWeightService getTouchWeightValue null");
                return null;
            } catch (Throwable th2) {
                th = th2;
                in = in2;
                if (in != null) {
                    try {
                        in.close();
                    } catch (IOException e6) {
                    }
                }
                throw th;
            }
        } catch (FileNotFoundException e7) {
            if (in != null) {
            }
            resetTimeOut();
            Log.i("touch weight", "HwTouchWeightService getTouchWeightValue null");
            return null;
        } catch (IOException e8) {
            if (in != null) {
            }
            resetTimeOut();
            Log.i("touch weight", "HwTouchWeightService getTouchWeightValue null");
            return null;
        } catch (Throwable th3) {
            th = th3;
            if (in != null) {
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

    /* JADX WARNING: Removed duplicated region for block: B:34:? A:{SYNTHETIC, RETURN} */
    /* JADX WARNING: Removed duplicated region for block: B:17:0x0047 A:{SYNTHETIC, Splitter: B:17:0x0047} */
    /* JADX WARNING: Removed duplicated region for block: B:33:? A:{SYNTHETIC, RETURN} */
    /* JADX WARNING: Removed duplicated region for block: B:12:0x003e A:{SYNTHETIC, Splitter: B:12:0x003e} */
    /* JADX WARNING: Removed duplicated region for block: B:22:0x0050 A:{SYNTHETIC, Splitter: B:22:0x0050} */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void setTouchWeightValue(String val) {
        Throwable th;
        Log.i("touch weight", "HwTouchWeightService setTouchWeightValue " + val);
        FileOutputStream fileOutWriteMode = null;
        try {
            FileOutputStream fileOutWriteMode2 = new FileOutputStream(new File(FILE_PATH));
            try {
                fileOutWriteMode2.write(val.getBytes("utf-8"));
                if (fileOutWriteMode2 != null) {
                    try {
                        fileOutWriteMode2.close();
                    } catch (IOException e) {
                    }
                }
                fileOutWriteMode = fileOutWriteMode2;
            } catch (FileNotFoundException e2) {
                fileOutWriteMode = fileOutWriteMode2;
                if (fileOutWriteMode == null) {
                }
            } catch (IOException e3) {
                fileOutWriteMode = fileOutWriteMode2;
                if (fileOutWriteMode == null) {
                }
            } catch (Throwable th2) {
                th = th2;
                fileOutWriteMode = fileOutWriteMode2;
                if (fileOutWriteMode != null) {
                }
                throw th;
            }
        } catch (FileNotFoundException e4) {
            if (fileOutWriteMode == null) {
                try {
                    fileOutWriteMode.close();
                } catch (IOException e5) {
                }
            }
        } catch (IOException e6) {
            if (fileOutWriteMode == null) {
                try {
                    fileOutWriteMode.close();
                } catch (IOException e7) {
                }
            }
        } catch (Throwable th3) {
            th = th3;
            if (fileOutWriteMode != null) {
                try {
                    fileOutWriteMode.close();
                } catch (IOException e8) {
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

    private void startHeartBeat() {
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
