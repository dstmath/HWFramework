package com.android.server.policy;

import android.app.ActivityManager;
import android.content.Context;
import android.content.res.Resources.NotFoundException;
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
import android.provider.Settings.Global;
import android.provider.Settings.Secure;
import android.provider.Settings.System;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Flog;
import android.util.Log;
import android.view.Display;
import android.view.MotionEvent;
import android.view.WindowManager;
import android.view.WindowManagerGlobal;

public class SlideTouchEvent {
    public static final String KEY_SINGLE_HAND_SCREEN_ZOOM = "single_hand_screen_zoom";
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
        boolean z = true;
        if (this.mContext != null) {
            this.mIsSupport = isSupportSingleHand();
            if (this.mIsSupport) {
                this.mThreshHoldsLazyMode = (float) this.mContext.getResources().getDimensionPixelSize(17105141);
                this.mScreenZoomEnabled = System.getIntForUser(this.mContext.getContentResolver(), "single_hand_screen_zoom", 1, ActivityManager.getCurrentUser()) == 1;
                if (Secure.getIntForUser(this.mContext.getContentResolver(), "accessibility_display_magnification_enabled", 0, ActivityManager.getCurrentUser()) != 1) {
                    z = false;
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
        this.mContext.getContentResolver().registerContentObserver(System.getUriFor("single_hand_screen_zoom"), false, slideObserver, -1);
        this.mContext.getContentResolver().registerContentObserver(Secure.getUriFor("accessibility_display_magnification_enabled"), false, slideObserver, -1);
    }

    public void updateSettings() {
        boolean z = true;
        this.mScreenZoomEnabled = System.getIntForUser(this.mContext.getContentResolver(), "single_hand_screen_zoom", 1, ActivityManager.getCurrentUser()) == 1;
        if (Secure.getIntForUser(this.mContext.getContentResolver(), "accessibility_display_magnification_enabled", 0, ActivityManager.getCurrentUser()) != 1) {
            z = false;
        }
        this.mZoomGestureEnabled = z;
        if (!this.mScreenZoomEnabled || this.mZoomGestureEnabled) {
            quitLazyMode();
        }
    }

    private boolean shouldHandleTouchEvent(MotionEvent event) {
        boolean should = true;
        if (event == null || event.getPointerCount() > 1) {
            should = false;
        }
        if (this.mIsSupport && (this.mScreenZoomEnabled ^ 1) == 0 && !this.mZoomGestureEnabled) {
            return should;
        }
        return false;
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
                    } else if ((Math.atan((double) (distanceY / distanceX)) / 3.141592653589793d) * 180.0d < 70.0d && 1 == this.mContext.getResources().getConfiguration().orientation) {
                        Log.i(TAG, "preformStartLazyMode");
                        preformStartLazyMode(this.mDownPoint[0] - x);
                        return;
                    }
                case 2:
                    if (distanceY > this.mThreshHoldsLazyMode && (this.mFirstOverThreshold ^ 1) != 0) {
                        this.mFirstOverThreshold = true;
                        double rotationDegree = (Math.atan((double) (distanceY / distanceX)) / 3.141592653589793d) * 180.0d;
                        Log.d(TAG, "rotationDegree=" + rotationDegree + ",mIsValidGuesture=" + this.mIsValidGuesture);
                        if (rotationDegree < 70.0d && (this.mIsValidGuesture ^ 1) != 0) {
                            this.mIsValidGuesture = true;
                            break;
                        }
                    }
            }
        }
    }

    public void preformStartLazyMode(final float x) {
        float distanceX = x;
        new AsyncTask<Void, Void, Void>() {
            protected Void doInBackground(Void... params) {
                if (1 == SystemProperties.getInt("sys.sdr.enter", 0)) {
                    int sleepCount = 0;
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
                        if (reply != null) {
                            reply.recycle();
                        }
                    } catch (RemoteException ex) {
                        Log.e(SlideTouchEvent.TAG, "stop sdr exception. message = " + ex.getMessage());
                        if (data != null) {
                            data.recycle();
                        }
                        if (reply != null) {
                            reply.recycle();
                        }
                    } catch (Throwable th) {
                        if (data != null) {
                            data.recycle();
                        }
                        if (reply != null) {
                            reply.recycle();
                        }
                    }
                    Log.d("APS", "APS: SDR: special: Lazymode process wait begin ");
                    while (1 == SystemProperties.getInt("sys.sdr.special", 0) && sleepCount < 100) {
                        try {
                            Thread.sleep(10);
                        } catch (InterruptedException e) {
                        }
                        sleepCount++;
                    }
                    Log.d("APS", "APS: SDR: special: Lazymode process wait end, sleepCount=" + sleepCount);
                }
                return null;
            }

            protected void onPostExecute(Void result) {
                SlideTouchEvent.this.startLazyMode(x);
            }
        }.execute(new Void[0]);
    }

    private void startLazyMode(float distanceX) {
        String str = Global.getString(this.mContext.getContentResolver(), "single_hand_mode");
        if (distanceX > 0.0f && TextUtils.isEmpty(str)) {
            Flog.bdReport(this.mContext, 13);
            Global.putString(this.mContext.getContentResolver(), "single_hand_mode", RIGHT);
            this.mStartLazyModeTime = SystemClock.uptimeMillis();
        }
        if (distanceX < 0.0f && TextUtils.isEmpty(str)) {
            Flog.bdReport(this.mContext, 14);
            Global.putString(this.mContext.getContentResolver(), "single_hand_mode", LEFT);
            this.mStartLazyModeTime = SystemClock.uptimeMillis();
        }
        if (distanceX < 0.0f && str != null && str.contains(LEFT)) {
            quitLazyMode();
            reportLazyModeUsingTime();
        }
        if (distanceX > 0.0f && str != null && str.contains(RIGHT)) {
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
        String str = Global.getString(context.getContentResolver(), "single_hand_mode");
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
        Global.putString(this.mContext.getContentResolver(), "single_hand_mode", "");
    }

    /* JADX WARNING: Removed duplicated region for block: B:32:0x00ac  */
    /* JADX WARNING: Removed duplicated region for block: B:34:0x00b1  */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private boolean isSupportSingleHand() {
        boolean z = true;
        IBinder windowManagerBinder = WindowManagerGlobal.getWindowManagerService().asBinder();
        Parcel parcel = null;
        Parcel parcel2 = null;
        try {
            parcel = Parcel.obtain();
            parcel2 = Parcel.obtain();
            if (windowManagerBinder != null) {
                parcel.writeInterfaceToken("android.view.IWindowManager");
                windowManagerBinder.transact(1991, parcel, parcel2, 0);
                parcel2.readException();
                boolean single_hand_switch = parcel2.readInt();
                Log.i(TAG, "single_hand_switch = " + single_hand_switch);
                if (single_hand_switch != z) {
                    z = false;
                }
                if (parcel != null) {
                    parcel.recycle();
                }
                if (parcel2 != null) {
                    parcel2.recycle();
                }
                return z;
            }
            if (parcel != null) {
                parcel.recycle();
            }
            if (parcel2 != null) {
                parcel2.recycle();
            }
            return z;
        } catch (RemoteException e) {
            Log.e(TAG, "read single_hand_switch exception. message = " + e.getMessage());
            int id = this.mContext.getResources().getIdentifier("single_hand_mode", "bool", "androidhwext");
            if (id != 0) {
                try {
                    z = this.mContext.getResources().getBoolean(id);
                    if (parcel != null) {
                        parcel.recycle();
                    }
                    if (parcel2 != null) {
                        parcel2.recycle();
                    }
                    return z;
                } catch (NotFoundException ex) {
                    ex.printStackTrace();
                    if (parcel != null) {
                    }
                    if (parcel2 != null) {
                    }
                    return z;
                }
            }
            if (parcel != null) {
                parcel.recycle();
            }
            if (parcel2 != null) {
                parcel2.recycle();
            }
        } catch (Throwable th) {
            if (parcel != null) {
                parcel.recycle();
            }
            if (parcel2 != null) {
                parcel2.recycle();
            }
            throw th;
        }
    }
}
