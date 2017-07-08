package huawei.com.android.server.policy;

import android.app.ActivityManager;
import android.content.Context;
import android.cover.CoverManager;
import android.database.ContentObserver;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings.System;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewParent;
import android.view.ViewRootImpl;
import android.view.WindowManager;
import android.view.WindowManager.LayoutParams;
import android.view.WindowManagerPolicy;
import android.widget.FrameLayout;

public class HwScreenOnProximityLock {
    private static final boolean DEBUG = false;
    private static final long DELAY = 1000;
    private static final boolean HWFLOW = false;
    private static final String KEY_TOUCH_DISABLE_MODE = "touch_disable_mode";
    private static final int MSG_FIRST_PROXIMITY_IN_TIME = 1;
    private static final int MSG_SHOW_HINT_VIEW = 2;
    private static final String SCREENON_TAG = "ScreenOn";
    private static final String TAG = "HwScreenOnProximityLock";
    private static final float TYPICAL_PROXIMITY_THRESHOLD = 5.0f;
    private static final String sProximityWndName = "Emui:ProximityWnd";
    private Context mContext;
    private CoverManager mCoverManager;
    private Handler mHandler;
    private boolean mHeld;
    private ProximitySensorListener mListener;
    private final Object mLock;
    private float mProximityThreshold;
    private FrameLayout mProximityView;
    private SensorManager mSensorManager;
    public ContentObserver mTouchDisableObserver;
    private WindowManager mWindowManager;

    /* renamed from: huawei.com.android.server.policy.HwScreenOnProximityLock.1 */
    class AnonymousClass1 extends ContentObserver {
        AnonymousClass1(Handler $anonymous0) {
            super($anonymous0);
        }

        public void onChange(boolean selfChange) {
            boolean touchDisable = true;
            if (System.getIntForUser(HwScreenOnProximityLock.this.mContext.getContentResolver(), HwScreenOnProximityLock.KEY_TOUCH_DISABLE_MODE, HwScreenOnProximityLock.MSG_FIRST_PROXIMITY_IN_TIME, ActivityManager.getCurrentUser()) == 0) {
                touchDisable = HwScreenOnProximityLock.HWFLOW;
            }
            if (!touchDisable && HwScreenOnProximityLock.this.isShowing()) {
                HwScreenOnProximityLock.this.releaseLock();
            }
        }
    }

    private class ProximitySensorListener implements SensorEventListener {
        private boolean mIsProximity;

        public void onAccuracyChanged(Sensor sensor, int accuracy) {
        }

        public void onSensorChanged(SensorEvent event) {
            boolean z = HwScreenOnProximityLock.HWFLOW;
            float d = event.values[0];
            if (d >= 0.0f && d < HwScreenOnProximityLock.this.mProximityThreshold) {
                z = true;
            }
            this.mIsProximity = z;
            handleSensorChanges();
        }

        private void handleSensorChanges() {
            if (HwScreenOnProximityLock.this.mCoverManager != null) {
                boolean isCoverOpen = HwScreenOnProximityLock.this.mCoverManager.isCoverOpen();
                if (this.mIsProximity && isCoverOpen) {
                    if (HwScreenOnProximityLock.this.mHandler.hasMessages(HwScreenOnProximityLock.MSG_FIRST_PROXIMITY_IN_TIME)) {
                        HwScreenOnProximityLock.this.mHandler.removeMessages(HwScreenOnProximityLock.MSG_FIRST_PROXIMITY_IN_TIME);
                    }
                    synchronized (HwScreenOnProximityLock.this.mLock) {
                        if (HwScreenOnProximityLock.this.mProximityView == null) {
                            HwScreenOnProximityLock.this.preparePoriximityView();
                        }
                    }
                } else {
                    synchronized (HwScreenOnProximityLock.this.mLock) {
                        if (isCoverOpen) {
                            if (HwScreenOnProximityLock.this.mProximityView == null) {
                                return;
                            }
                        }
                        HwScreenOnProximityLock.this.releaseLock();
                    }
                }
            }
        }
    }

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: huawei.com.android.server.policy.HwScreenOnProximityLock.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: huawei.com.android.server.policy.HwScreenOnProximityLock.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 7 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 8 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: huawei.com.android.server.policy.HwScreenOnProximityLock.<clinit>():void");
    }

    public HwScreenOnProximityLock(Context context) {
        this.mLock = new Object();
        this.mTouchDisableObserver = new AnonymousClass1(new Handler());
        if (context == null) {
            Log.w(TAG, "HwScreenOnProximityLock context is null");
            return;
        }
        this.mContext = context;
        this.mSensorManager = (SensorManager) this.mContext.getSystemService("sensor");
        this.mWindowManager = (WindowManager) this.mContext.getSystemService("window");
        this.mCoverManager = new CoverManager();
        this.mListener = new ProximitySensorListener();
        this.mHandler = new Handler() {
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                switch (msg.what) {
                    case HwScreenOnProximityLock.MSG_FIRST_PROXIMITY_IN_TIME /*1*/:
                        HwScreenOnProximityLock.this.releaseLock();
                    case HwScreenOnProximityLock.MSG_SHOW_HINT_VIEW /*2*/:
                        HwScreenOnProximityLock.this.showHintView();
                    default:
                }
            }
        };
    }

    public void acquireLock(WindowManagerPolicy policy) {
        synchronized (this.mLock) {
            if (this.mHeld) {
                Log.w(TAG, "acquire Lock: return because sensor listener has been held  = " + this.mHeld);
                return;
            }
            if (policy == null) {
                Log.w(TAG, "acquire Lock: return because get Window Manager policy is null");
                return;
            }
            this.mContext.getContentResolver().registerContentObserver(System.getUriFor(KEY_TOUCH_DISABLE_MODE), true, this.mTouchDisableObserver);
            Sensor sensor = this.mSensorManager.getDefaultSensor(8);
            if (sensor == null) {
                Log.w(TAG, "acquire Lock: return because of proximity sensor is not existed");
                return;
            }
            this.mProximityThreshold = Math.min(sensor.getMaximumRange(), TYPICAL_PROXIMITY_THRESHOLD);
            this.mHeld = this.mSensorManager.registerListener(this.mListener, sensor, 3);
            if (this.mHeld) {
                this.mHandler.sendEmptyMessageDelayed(MSG_FIRST_PROXIMITY_IN_TIME, DELAY);
            } else {
                Log.w(TAG, "registerListener fail");
            }
            return;
        }
    }

    public void releaseLock() {
        synchronized (this.mLock) {
            if (this.mHeld) {
                removeProximityView();
                this.mSensorManager.unregisterListener(this.mListener);
                this.mHeld = HWFLOW;
                this.mHandler.removeCallbacksAndMessages(null);
                this.mContext.getContentResolver().unregisterContentObserver(this.mTouchDisableObserver);
                return;
            }
            Log.w(TAG, "releaseLock: return because sensor listener is held = " + this.mHeld);
        }
    }

    public boolean isShowing() {
        boolean z;
        synchronized (this.mLock) {
            z = this.mProximityView != null ? true : HWFLOW;
        }
        return z;
    }

    public void forceShowHint() {
        this.mHandler.sendEmptyMessage(MSG_SHOW_HINT_VIEW);
    }

    private void preparePoriximityView() {
        removeProximityView();
        synchronized (this.mLock) {
            View view = View.inflate(this.mContext, 34013254, null);
            if (view instanceof FrameLayout) {
                this.mProximityView = (FrameLayout) view;
                this.mProximityView.setOnTouchListener(new OnTouchListener() {
                    public boolean onTouch(View v, MotionEvent event) {
                        HwScreenOnProximityLock.this.showHintView();
                        return HwScreenOnProximityLock.HWFLOW;
                    }
                });
                LayoutParams params = new LayoutParams(-1, -1, 2100, 134223104, -2);
                params.inputFeatures |= 4;
                params.privateFlags |= -2147483632;
                params.hwFlags |= 4;
                params.setTitle(sProximityWndName);
                if (HWFLOW) {
                    Log.i(TAG, "preparePoriximityView addView ");
                }
                this.mWindowManager.addView(this.mProximityView, params);
                return;
            }
        }
    }

    private void showHintView() {
        synchronized (this.mLock) {
            if (this.mProximityView == null) {
                return;
            }
            View hintView = this.mProximityView.findViewById(34603178);
            if (hintView == null) {
                return;
            }
            hintView.setVisibility(0);
        }
    }

    private void removeProximityView() {
        synchronized (this.mLock) {
            if (this.mProximityView != null) {
                ViewParent vp = this.mProximityView.getParent();
                if (this.mWindowManager == null || !(vp instanceof ViewRootImpl)) {
                    Log.w(TAG, "removeView fail: mWindowManager = " + this.mWindowManager + ", viewparent = " + vp);
                } else {
                    if (HWFLOW) {
                        Log.i(TAG, "removeProximityView success vp " + vp);
                    }
                    this.mWindowManager.removeView(this.mProximityView);
                }
                this.mProximityView = null;
            }
        }
    }
}
