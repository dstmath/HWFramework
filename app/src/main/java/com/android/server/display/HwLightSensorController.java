package com.android.server.display;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.util.Slog;
import com.android.server.security.trustcircle.IOTController;
import huawei.com.android.server.policy.HwGlobalActionsView;
import java.util.ArrayList;
import java.util.List;

public class HwLightSensorController {
    private static boolean DEBUG = false;
    private static final int MSG_TIMER = 1;
    private static final String TAG = "HwLightSensorController";
    private final LightSensorCallbacks mCallbacks;
    private List<Integer> mDataList;
    private boolean mEnable;
    private long mEnableTime;
    private Handler mHandler;
    private int mLastSensorValue;
    private Sensor mLightSensor;
    private int mRateMillis;
    private final SensorEventListener mSensorEventListener;
    private SensorManager mSensorManager;
    private boolean mWarmUpFlg;

    public interface LightSensorCallbacks {
        void processSensorData(long j, int i);
    }

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.server.display.HwLightSensorController.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.server.display.HwLightSensorController.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: com.android.server.display.HwLightSensorController.<clinit>():void");
    }

    public HwLightSensorController(LightSensorCallbacks callbacks, SensorManager sensorManager, int sensorRateMillis) {
        this.mLastSensorValue = -1;
        this.mWarmUpFlg = true;
        this.mRateMillis = HwGlobalActionsView.VIBRATE_DELAY;
        this.mSensorEventListener = new SensorEventListener() {
            public void onSensorChanged(SensorEvent event) {
                if (HwLightSensorController.this.mEnable) {
                    int lux = (int) event.values[0];
                    long timeStamp = event.timestamp / 1000000;
                    if (!HwLightSensorController.this.mWarmUpFlg) {
                        HwLightSensorController.this.setSensorData(lux);
                    } else if (timeStamp < HwLightSensorController.this.mEnableTime) {
                        if (HwLightSensorController.DEBUG) {
                            Slog.i(HwLightSensorController.TAG, "sensor not ready yet");
                        }
                    } else {
                        HwLightSensorController.this.setSensorData(lux);
                        HwLightSensorController.this.mWarmUpFlg = false;
                        HwLightSensorController.this.mHandler.sendEmptyMessage(HwLightSensorController.MSG_TIMER);
                    }
                }
            }

            public void onAccuracyChanged(Sensor sensor, int accuracy) {
            }
        };
        this.mHandler = new Handler() {
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case HwLightSensorController.MSG_TIMER /*1*/:
                        int lux = HwLightSensorController.this.getSensorData();
                        if (lux >= 0) {
                            try {
                                HwLightSensorController.this.mCallbacks.processSensorData(SystemClock.elapsedRealtime(), lux);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                        if (HwLightSensorController.this.mEnable) {
                            sendEmptyMessageDelayed(HwLightSensorController.MSG_TIMER, (long) HwLightSensorController.this.mRateMillis);
                        }
                    default:
                        Slog.e(HwLightSensorController.TAG, "Invalid message");
                }
            }
        };
        this.mCallbacks = callbacks;
        this.mSensorManager = sensorManager;
        this.mRateMillis = sensorRateMillis;
        this.mLightSensor = this.mSensorManager.getDefaultSensor(5);
        this.mDataList = new ArrayList();
    }

    public void enableSensor() {
        if (!this.mEnable) {
            this.mEnable = true;
            this.mWarmUpFlg = true;
            this.mEnableTime = SystemClock.elapsedRealtime();
            this.mSensorManager.registerListener(this.mSensorEventListener, this.mLightSensor, this.mRateMillis * IOTController.TYPE_MASTER);
        }
    }

    public void disableSensor() {
        if (this.mEnable) {
            this.mEnable = false;
            this.mSensorManager.unregisterListener(this.mSensorEventListener);
            this.mHandler.removeMessages(MSG_TIMER);
            clearSensorData();
        }
    }

    private void setSensorData(int lux) {
        synchronized (this.mDataList) {
            this.mDataList.add(Integer.valueOf(lux));
        }
    }

    private int getSensorData() {
        synchronized (this.mDataList) {
            if (this.mDataList.isEmpty()) {
                int i = this.mLastSensorValue;
                return i;
            }
            int count = 0;
            int sum = 0;
            for (Integer data : this.mDataList) {
                sum += data.intValue();
                count += MSG_TIMER;
            }
            if (count != 0) {
                int average = sum / count;
                if (average >= 0) {
                    this.mLastSensorValue = average;
                }
            }
            this.mDataList.clear();
            i = this.mLastSensorValue;
            return i;
        }
    }

    private void clearSensorData() {
        synchronized (this.mDataList) {
            this.mDataList.clear();
            this.mLastSensorValue = -1;
        }
    }
}
