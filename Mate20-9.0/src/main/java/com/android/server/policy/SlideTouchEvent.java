package com.android.server.policy;

import android.app.ActivityManager;
import android.content.Context;
import android.content.res.Resources;
import android.database.ContentObserver;
import android.graphics.Rect;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.IBinder;
import android.os.Parcel;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.SystemClock;
import android.os.SystemProperties;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Flog;
import android.util.Log;
import android.view.Display;
import android.view.MotionEvent;
import android.view.WindowManager;
import android.view.WindowManagerGlobal;
import com.android.server.gesture.GestureNavConst;

public class SlideTouchEvent {
    public static final String KEY_SINGLE_HAND_SCREEN_ZOOM = "single_hand_screen_zoom";
    private static final int LAZYMODE_THRESHOLD_DEGREE_MAX = 70;
    private static final int LAZYMODE_THRESHOLD_DEGREE_MIN = 20;
    private static final long LAZYMODE_USE_DURATION = 3600000;
    private static final String LEFT = "left";
    private static final String RIGHT = "right";
    public static final float SCALE = 0.75f;
    public static final int STATE_LEFT = 1;
    public static final int STATE_MIDDLE = 0;
    public static final int STATE_RIGHT = 2;
    private static final int STOP_SDR_BINDER_ID = 1195;
    private static final int STOP_SDR_CMD = 1;
    private static final String TAG = "SlideTouchEvent";
    private Context mContext;
    private float[] mDownPoint = new float[2];
    private boolean mFirstOverThreshold;
    private Handler mHandler = new Handler();
    private boolean mIsSupport = true;
    private boolean mIsValidGuesture;
    private boolean mScreenZoomEnabled = true;
    private long mStartLazyModeTime = 0;
    private float mThreshHoldsLazyMode;
    private boolean mZoomGestureEnabled = false;

    public SlideTouchEvent(Context context) {
        this.mContext = context;
        init();
    }

    private void init() {
        if (this.mContext != null) {
            this.mIsSupport = isSupportSingleHand();
            if (this.mIsSupport) {
                this.mThreshHoldsLazyMode = (float) this.mContext.getResources().getDimensionPixelSize(17105186);
                boolean z = false;
                this.mScreenZoomEnabled = Settings.System.getIntForUser(this.mContext.getContentResolver(), "single_hand_screen_zoom", 1, ActivityManager.getCurrentUser()) == 1;
                if (Settings.Secure.getIntForUser(this.mContext.getContentResolver(), "accessibility_display_magnification_enabled", 0, ActivityManager.getCurrentUser()) == 1) {
                    z = true;
                }
                this.mZoomGestureEnabled = z;
                registerObserver();
            }
        }
    }

    private void registerObserver() {
        ContentObserver slideObserver = new ContentObserver(this.mHandler) {
            public void onChange(boolean selfChange) {
                SlideTouchEvent.this.updateSettings();
            }
        };
        this.mContext.getContentResolver().registerContentObserver(Settings.System.getUriFor("single_hand_screen_zoom"), false, slideObserver, -1);
        this.mContext.getContentResolver().registerContentObserver(Settings.Secure.getUriFor("accessibility_display_magnification_enabled"), false, slideObserver, -1);
    }

    public void updateSettings() {
        boolean z = false;
        this.mScreenZoomEnabled = Settings.System.getIntForUser(this.mContext.getContentResolver(), "single_hand_screen_zoom", 1, ActivityManager.getCurrentUser()) == 1;
        if (Settings.Secure.getIntForUser(this.mContext.getContentResolver(), "accessibility_display_magnification_enabled", 0, ActivityManager.getCurrentUser()) == 1) {
            z = true;
        }
        this.mZoomGestureEnabled = z;
        if (!this.mScreenZoomEnabled || this.mZoomGestureEnabled) {
            quitLazyMode();
        }
    }

    public void setGestureResultAtUp(boolean success) {
        Log.i(TAG, "gestureResultAtUp:" + success);
        this.mIsValidGuesture = this.mIsValidGuesture & success;
    }

    private boolean shouldHandleTouchEvent(MotionEvent event) {
        boolean should = true;
        if (event == null || event.getPointerCount() > 1) {
            should = false;
        }
        if (!this.mIsSupport || !this.mScreenZoomEnabled || this.mZoomGestureEnabled) {
            return false;
        }
        return should;
    }

    public void handleTouchEvent(MotionEvent event) {
        if (shouldHandleTouchEvent(event)) {
            float x = event.getX();
            float y = event.getY();
            float distanceX = Math.abs(this.mDownPoint[0] - x);
            float distanceY = Math.abs(this.mDownPoint[1] - y);
            switch (event.getAction()) {
                case 0:
                    Log.i(TAG, "MotionEvent.ACTION_DOWN");
                    this.mDownPoint[0] = event.getX();
                    this.mDownPoint[1] = event.getY();
                    this.mIsValidGuesture = false;
                    this.mFirstOverThreshold = false;
                    break;
                case 1:
                    if (!this.mIsValidGuesture) {
                        Log.i(TAG, "Sliding distance is too short, can not trigger the lazy mode");
                        break;
                    } else {
                        double rotationDegree = (Math.atan((double) (distanceY / distanceX)) / 3.141592653589793d) * 180.0d;
                        if (rotationDegree < 70.0d && rotationDegree > 20.0d && 1 == this.mContext.getResources().getConfiguration().orientation) {
                            Log.i(TAG, "preformStartLazyMode");
                            preformStartLazyMode(this.mDownPoint[0] - x);
                            return;
                        }
                    }
                case 2:
                    if (distanceY > this.mThreshHoldsLazyMode && !this.mFirstOverThreshold) {
                        this.mFirstOverThreshold = true;
                        double rotationDegree2 = (Math.atan((double) (distanceY / distanceX)) / 3.141592653589793d) * 180.0d;
                        Log.d(TAG, "rotationDegree=" + rotationDegree2 + ",mIsValidGuesture=" + this.mIsValidGuesture);
                        if (rotationDegree2 < 70.0d && rotationDegree2 > 20.0d && !this.mIsValidGuesture) {
                            this.mIsValidGuesture = true;
                            break;
                        }
                    }
            }
        }
    }

    public void preformStartLazyMode(float x) {
        final float distanceX = x;
        new AsyncTask<Void, Void, Void>() {
            /* access modifiers changed from: protected */
            /* JADX WARNING: Code restructure failed: missing block: B:16:0x0055, code lost:
                if (r5 != null) goto L_0x002e;
             */
            /* JADX WARNING: Code restructure failed: missing block: B:17:0x0058, code lost:
                android.util.Log.d("APS", "APS: SDR: special: Lazymode process wait begin ");
             */
            /* JADX WARNING: Code restructure failed: missing block: B:19:0x0066, code lost:
                if (1 != android.os.SystemProperties.getInt("sys.sdr.special", 0)) goto L_0x0076;
             */
            /* JADX WARNING: Code restructure failed: missing block: B:21:0x006a, code lost:
                if (r0 >= 100) goto L_0x0076;
             */
            /* JADX WARNING: Code restructure failed: missing block: B:24:?, code lost:
                java.lang.Thread.sleep(10);
             */
            /* JADX WARNING: Code restructure failed: missing block: B:27:0x0076, code lost:
                android.util.Log.d("APS", "APS: SDR: special: Lazymode process wait end, sleepCount=" + r0);
             */
            /* JADX WARNING: Code restructure failed: missing block: B:7:0x002c, code lost:
                if (r5 != null) goto L_0x002e;
             */
            /* JADX WARNING: Code restructure failed: missing block: B:8:0x002e, code lost:
                r5.recycle();
             */
            public Void doInBackground(Void... params) {
                int sleepCount;
                if (1 == SystemProperties.getInt("sys.sdr.enter", 0)) {
                    sleepCount = 0;
                    IBinder mFlinger = ServiceManager.getService("SurfaceFlinger");
                    Parcel data = Parcel.obtain();
                    Parcel reply = Parcel.obtain();
                    try {
                        data.writeInt(1);
                        data.writeInterfaceToken("android.ui.ISurfaceComposer");
                        mFlinger.transact(SlideTouchEvent.STOP_SDR_BINDER_ID, data, reply, 0);
                        if (data != null) {
                            data.recycle();
                        }
                    } catch (RemoteException ex) {
                        Log.e(SlideTouchEvent.TAG, "stop sdr exception. message = " + ex.getMessage());
                        if (data != null) {
                            data.recycle();
                        }
                    } catch (Throwable th) {
                        if (data != null) {
                            data.recycle();
                        }
                        if (reply != null) {
                            reply.recycle();
                        }
                        throw th;
                    }
                }
                return null;
                sleepCount++;
            }

            /* access modifiers changed from: protected */
            public void onPostExecute(Void result) {
                SlideTouchEvent.this.startLazyMode(distanceX);
            }
        }.execute(new Void[0]);
    }

    /* access modifiers changed from: private */
    public void startLazyMode(float distanceX) {
        String str = Settings.Global.getString(this.mContext.getContentResolver(), "single_hand_mode");
        if (distanceX > GestureNavConst.BOTTOM_WINDOW_SINGLE_HAND_RATIO && TextUtils.isEmpty(str)) {
            Flog.bdReport(this.mContext, 13);
            Settings.Global.putString(this.mContext.getContentResolver(), "single_hand_mode", RIGHT);
            this.mStartLazyModeTime = SystemClock.uptimeMillis();
        }
        if (distanceX < GestureNavConst.BOTTOM_WINDOW_SINGLE_HAND_RATIO && TextUtils.isEmpty(str)) {
            Flog.bdReport(this.mContext, 14);
            Settings.Global.putString(this.mContext.getContentResolver(), "single_hand_mode", LEFT);
            this.mStartLazyModeTime = SystemClock.uptimeMillis();
        }
        if (distanceX < GestureNavConst.BOTTOM_WINDOW_SINGLE_HAND_RATIO && str != null && str.contains(LEFT)) {
            quitLazyMode();
            reportLazyModeUsingTime();
        }
        if (distanceX > GestureNavConst.BOTTOM_WINDOW_SINGLE_HAND_RATIO && str != null && str.contains(RIGHT)) {
            quitLazyMode();
            reportLazyModeUsingTime();
        }
    }

    private void reportLazyModeUsingTime() {
        if (this.mStartLazyModeTime > 0 && SystemClock.uptimeMillis() - this.mStartLazyModeTime >= 3600000) {
            Log.i(TAG, "BDReporter.EVENT_ID_SINGLE_HAND_USING_ONEHOUR");
        }
    }

    public static int getLazyState(Context context) {
        String str = Settings.Global.getString(context.getContentResolver(), "single_hand_mode");
        if (TextUtils.isEmpty(str)) {
            return 0;
        }
        if (str.contains(LEFT)) {
            return 1;
        }
        if (str.contains(RIGHT)) {
            return 2;
        }
        return 0;
    }

    public static Rect getScreenshotRect(Context context) {
        Display display = ((WindowManager) context.getSystemService("window")).getDefaultDisplay();
        DisplayMetrics displayMetrics = new DisplayMetrics();
        display.getRealMetrics(displayMetrics);
        int state = getLazyState(context);
        if (1 == state) {
            return new Rect(0, (int) (((float) displayMetrics.heightPixels) * 0.25f), (int) (((float) displayMetrics.widthPixels) * 0.75f), displayMetrics.heightPixels);
        }
        if (2 == state) {
            return new Rect((int) (((float) displayMetrics.widthPixels) * 0.25f), (int) (((float) displayMetrics.heightPixels) * 0.25f), displayMetrics.widthPixels, displayMetrics.heightPixels);
        }
        return null;
    }

    public static boolean isLazyMode(Context context) {
        return getLazyState(context) != 0;
    }

    private void quitLazyMode() {
        Settings.Global.putString(this.mContext.getContentResolver(), "single_hand_mode", "");
    }

    /* JADX WARNING: Code restructure failed: missing block: B:17:0x005c, code lost:
        if (r3 != null) goto L_0x005e;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:18:0x005e, code lost:
        r3.recycle();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:37:0x00b1, code lost:
        if (r3 == null) goto L_0x00b4;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:38:0x00b4, code lost:
        return true;
     */
    /* JADX WARNING: Removed duplicated region for block: B:36:0x00ae  */
    private boolean isSupportSingleHand() {
        int single_hand_switch;
        boolean z = false;
        if (WindowManagerGlobal.getWindowManagerService() == null) {
            return false;
        }
        IBinder windowManagerBinder = WindowManagerGlobal.getWindowManagerService().asBinder();
        Parcel data = null;
        Parcel reply = null;
        try {
            Parcel data2 = Parcel.obtain();
            reply = Parcel.obtain();
            if (windowManagerBinder != null) {
                data2.writeInterfaceToken("android.view.IWindowManager");
                windowManagerBinder.transact(1991, data2, reply, 0);
                reply.readException();
                Log.i(TAG, "single_hand_switch = " + single_hand_switch);
                if (single_hand_switch == 1) {
                    z = true;
                }
                if (data2 != null) {
                    data2.recycle();
                }
                if (reply != null) {
                    reply.recycle();
                }
                return z;
            } else if (data2 != null) {
                data2.recycle();
            }
        } catch (RemoteException e) {
            Log.e(TAG, "read single_hand_switch exception. message = " + e.getMessage());
            int id = this.mContext.getResources().getIdentifier("single_hand_mode", "bool", "androidhwext");
            if (id != 0) {
                try {
                    boolean z2 = this.mContext.getResources().getBoolean(id);
                    if (data != null) {
                        data.recycle();
                    }
                    if (reply != null) {
                        reply.recycle();
                    }
                    return z2;
                } catch (Resources.NotFoundException ex) {
                    ex.printStackTrace();
                    if (data != null) {
                        data.recycle();
                    }
                }
            }
            if (data != null) {
            }
        } catch (Throwable th) {
            if (data != null) {
                data.recycle();
            }
            if (reply != null) {
                reply.recycle();
            }
            throw th;
        }
    }
}
