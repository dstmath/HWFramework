package com.android.server.policy;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.database.ContentObserver;
import android.graphics.Rect;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.IBinder;
import android.os.Parcel;
import android.os.RemoteException;
import android.os.SystemClock;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Flog;
import android.util.Log;
import android.view.Display;
import android.view.MotionEvent;
import android.view.WindowManager;
import com.android.server.gesture.GestureNavConst;
import com.huawei.android.app.ActivityManagerEx;
import com.huawei.android.app.WindowManagerExt;
import com.huawei.android.content.ContentResolverExt;
import com.huawei.android.os.ServiceManagerEx;
import com.huawei.android.os.SystemPropertiesEx;
import com.huawei.android.provider.SettingsEx;
import com.huawei.utils.HwPartResourceUtils;

public class SlideTouchEvent {
    private static final int GESTURE_EXCEED_MAX_DEGREE = 2;
    private static final int GESTURE_SINGLE_HAND = 1;
    private static final int GESTURE_UNCHECKED = 0;
    private static final String KEY_SINGLE_HAND_SCREEN_ZOOM = "single_hand_screen_zoom";
    private static final int LAZYMODE_THRESHOLD_DEGREE_MAX = 70;
    private static final int LAZYMODE_THRESHOLD_DEGREE_MIN = 20;
    private static final long LAZYMODE_USE_DURATION = 3600000;
    private static final String LEFT = "left";
    private static final String RIGHT = "right";
    private static final float SCALE = 0.75f;
    public static final int STATE_LEFT = 1;
    public static final int STATE_MIDDLE = 0;
    public static final int STATE_RIGHT = 2;
    private static final int STOP_SDR_BINDER_ID = 1195;
    private static final int STOP_SDR_CMD = 1;
    private static final String TAG = "SlideTouchEvent";
    private Context mContext;
    private float[] mDownPoint;
    private int mGestureCheckState;
    private Handler mHandler;
    private boolean mIsFirstOverThreshold;
    private boolean mIsScreenZoomEnabled;
    private boolean mIsSupport;
    private boolean mIsValidGuesture;
    private boolean mIsZoomGestureEnabled;
    private int mLastGestureCheckState;
    private SlideGestureListener mSlideGestureListener;
    private long mStartLazyModeTime;
    private float mThreshHoldsLazyMode;

    public interface SlideGestureListener {
        boolean onSlideGestureSuccess();
    }

    public SlideTouchEvent(Context context) {
        this(context, null);
    }

    public SlideTouchEvent(Context context, SlideGestureListener listener) {
        this.mStartLazyModeTime = 0;
        this.mIsSupport = true;
        this.mIsScreenZoomEnabled = true;
        this.mIsZoomGestureEnabled = false;
        this.mLastGestureCheckState = 0;
        this.mGestureCheckState = 0;
        this.mDownPoint = new float[2];
        this.mHandler = new Handler();
        this.mContext = context;
        this.mSlideGestureListener = listener;
        init();
    }

    private void init() {
        if (this.mContext != null) {
            this.mIsSupport = isSupportSingleHand();
            if (this.mIsSupport) {
                this.mThreshHoldsLazyMode = (float) this.mContext.getResources().getDimensionPixelSize(HwPartResourceUtils.getResourceId("navigation_bar_height"));
                boolean z = false;
                this.mIsScreenZoomEnabled = SettingsEx.System.getIntForUser(this.mContext.getContentResolver(), KEY_SINGLE_HAND_SCREEN_ZOOM, 1, ActivityManagerEx.getCurrentUser()) == 1;
                if (SettingsEx.Secure.getIntForUser(this.mContext.getContentResolver(), "accessibility_display_magnification_enabled", 0, ActivityManagerEx.getCurrentUser()) == 1) {
                    z = true;
                }
                this.mIsZoomGestureEnabled = z;
                registerObserver();
            }
        }
    }

    private void registerObserver() {
        ContentObserver slideObserver = new ContentObserver(this.mHandler) {
            /* class com.android.server.policy.SlideTouchEvent.AnonymousClass1 */

            @Override // android.database.ContentObserver
            public void onChange(boolean selfChange) {
                SlideTouchEvent.this.updateSettings();
            }
        };
        ContentResolverExt.registerContentObserver(this.mContext.getContentResolver(), Settings.System.getUriFor(KEY_SINGLE_HAND_SCREEN_ZOOM), false, slideObserver, -1);
        ContentResolverExt.registerContentObserver(this.mContext.getContentResolver(), Settings.Secure.getUriFor("accessibility_display_magnification_enabled"), false, slideObserver, -1);
    }

    public void updateSettings() {
        boolean z = false;
        this.mIsScreenZoomEnabled = SettingsEx.System.getIntForUser(this.mContext.getContentResolver(), KEY_SINGLE_HAND_SCREEN_ZOOM, 1, ActivityManagerEx.getCurrentUser()) == 1;
        if (SettingsEx.Secure.getIntForUser(this.mContext.getContentResolver(), "accessibility_display_magnification_enabled", 0, ActivityManagerEx.getCurrentUser()) == 1) {
            z = true;
        }
        this.mIsZoomGestureEnabled = z;
        if (!this.mIsScreenZoomEnabled || this.mIsZoomGestureEnabled) {
            quitLazyMode();
        }
    }

    public void setGestureResultAtUp(boolean isSuccess) {
        Log.i(TAG, "gestureResultAtUp:" + isSuccess);
        this.mIsValidGuesture = this.mIsValidGuesture & isSuccess;
    }

    public boolean isBeginFailedAsExceedDegree() {
        return this.mLastGestureCheckState == 0 && this.mGestureCheckState == 2;
    }

    public boolean isSingleHandEnableAndAvailable() {
        return this.mIsSupport && this.mIsScreenZoomEnabled && !this.mIsZoomGestureEnabled;
    }

    private boolean shouldHandleTouchEvent(MotionEvent event) {
        if (isSingleHandEnableAndAvailable() && event != null && event.getPointerCount() <= 1) {
            return true;
        }
        return false;
    }

    public void handleTouchEvent(MotionEvent event) {
        if (shouldHandleTouchEvent(event)) {
            float ex = event.getX();
            float ey = event.getY();
            float distanceX = Math.abs(this.mDownPoint[0] - ex);
            float distanceY = Math.abs(this.mDownPoint[1] - ey);
            this.mLastGestureCheckState = this.mGestureCheckState;
            int action = event.getAction();
            if (action == 0) {
                Log.i(TAG, "MotionEvent.ACTION_DOWN");
                this.mDownPoint[0] = event.getX();
                this.mDownPoint[1] = event.getY();
                this.mIsValidGuesture = false;
                this.mIsFirstOverThreshold = false;
                this.mGestureCheckState = 0;
                this.mLastGestureCheckState = 0;
            } else if (action != 1) {
                if (action == 2) {
                    if (distanceY > this.mThreshHoldsLazyMode && !this.mIsFirstOverThreshold) {
                        this.mIsFirstOverThreshold = true;
                        double rotationDegree = (Math.atan((double) (distanceY / distanceX)) / 3.141592653589793d) * 180.0d;
                        Log.d(TAG, "rotationDegree=" + rotationDegree + ",mIsValidGuesture=" + this.mIsValidGuesture);
                        if (rotationDegree < 70.0d && rotationDegree > 20.0d && !this.mIsValidGuesture) {
                            this.mIsValidGuesture = true;
                            this.mGestureCheckState = 1;
                        } else if (rotationDegree >= 70.0d) {
                            this.mGestureCheckState = 2;
                        }
                    }
                }
            } else if (this.mIsValidGuesture) {
                double rotationDegree2 = (Math.atan((double) (distanceY / distanceX)) / 3.141592653589793d) * 180.0d;
                if (rotationDegree2 < 70.0d && rotationDegree2 > 20.0d && this.mContext.getResources().getConfiguration().orientation == 1) {
                    tryStartLazyMode(ex);
                }
            } else {
                Log.i(TAG, "Sliding distance is too short, can not trigger the lazy mode");
            }
        }
    }

    private void tryStartLazyMode(float ex) {
        SlideGestureListener slideGestureListener = this.mSlideGestureListener;
        if (slideGestureListener == null) {
            Log.i(TAG, "preformStartLazyMode");
            preformStartLazyMode(this.mDownPoint[0] - ex);
        } else if (slideGestureListener.onSlideGestureSuccess()) {
            startSingleHandNewGestureGuide();
        }
    }

    public void preformStartLazyMode(final float dx) {
        new AsyncTask<Void, Void, Void>() {
            /* class com.android.server.policy.SlideTouchEvent.AnonymousClass2 */

            /* access modifiers changed from: protected */
            /* JADX WARNING: Code restructure failed: missing block: B:15:0x0053, code lost:
                if (r5 != null) goto L_0x002c;
             */
            /* JADX WARNING: Code restructure failed: missing block: B:16:0x0056, code lost:
                android.util.Log.d("APS", "APS: SDR: special: Lazymode process wait begin ");
             */
            /* JADX WARNING: Code restructure failed: missing block: B:18:0x0063, code lost:
                if (com.huawei.android.os.SystemPropertiesEx.getInt("sys.sdr.special", 0) != 1) goto L_0x0078;
             */
            /* JADX WARNING: Code restructure failed: missing block: B:20:0x0067, code lost:
                if (r1 >= 100) goto L_0x0078;
             */
            /* JADX WARNING: Code restructure failed: missing block: B:23:?, code lost:
                java.lang.Thread.sleep(10);
             */
            /* JADX WARNING: Code restructure failed: missing block: B:25:0x0070, code lost:
                android.util.Log.e("APS", "preformStartLazyMode() InterruptedException");
             */
            /* JADX WARNING: Code restructure failed: missing block: B:27:0x0078, code lost:
                android.util.Log.d("APS", "APS: SDR: special: Lazymode process wait end, sleepCount=" + r1);
             */
            /* JADX WARNING: Code restructure failed: missing block: B:38:?, code lost:
                return null;
             */
            /* JADX WARNING: Code restructure failed: missing block: B:6:0x002a, code lost:
                if (r5 != null) goto L_0x002c;
             */
            /* JADX WARNING: Code restructure failed: missing block: B:7:0x002c, code lost:
                r5.recycle();
             */
            public Void doInBackground(Void... params) {
                int sleepCount;
                if (SystemPropertiesEx.getInt("sys.sdr.enter", 0) != 1) {
                    return null;
                }
                sleepCount = 0;
                IBinder mFlinger = ServiceManagerEx.getService("SurfaceFlinger");
                Parcel data = Parcel.obtain();
                Parcel reply = Parcel.obtain();
                try {
                    data.writeInt(1);
                    data.writeInterfaceToken("android.ui.ISurfaceComposer");
                    mFlinger.transact(SlideTouchEvent.STOP_SDR_BINDER_ID, data, reply, 0);
                    data.recycle();
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
                sleepCount++;
            }

            /* access modifiers changed from: protected */
            public void onPostExecute(Void result) {
                SlideTouchEvent.this.startLazyMode(dx);
            }
        }.execute(new Void[0]);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void startLazyMode(float distanceX) {
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
        if (this.mStartLazyModeTime > 0 && SystemClock.uptimeMillis() - this.mStartLazyModeTime >= LAZYMODE_USE_DURATION) {
            Log.i(TAG, "BDReporter.EVENT_ID_SINGLE_HAND_USING_ONEHOUR");
        }
    }

    private void startSingleHandNewGestureGuide() {
        Log.i(TAG, "start single hand new gesture guide");
        Intent intent = new Intent();
        intent.setAction("com.huawei.intent.action.SINGLE_HAND_TIP");
        intent.setFlags(268435456);
        try {
            this.mContext.startActivity(intent);
        } catch (ActivityNotFoundException e) {
            Log.e(TAG, "activity not found");
        } catch (Exception e2) {
            Log.e(TAG, "launch activity fail!");
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
        if (state == 1) {
            return new Rect(0, (int) (((float) displayMetrics.heightPixels) * 0.25f), (int) (((float) displayMetrics.widthPixels) * 0.75f), displayMetrics.heightPixels);
        }
        if (state == 2) {
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

    /* JADX WARNING: Code restructure failed: missing block: B:14:0x0053, code lost:
        if (r4 != null) goto L_0x0055;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:15:0x0055, code lost:
        r4.recycle();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:32:0x00a6, code lost:
        if (0 == 0) goto L_0x00a9;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:33:0x00a9, code lost:
        return true;
     */
    /* JADX WARNING: Removed duplicated region for block: B:31:0x00a3  */
    private boolean isSupportSingleHand() {
        boolean z = false;
        if (!WindowManagerExt.isServiceReady()) {
            return false;
        }
        IBinder windowManagerBinder = WindowManagerExt.asBinder();
        Parcel data = null;
        Parcel reply = null;
        try {
            Parcel data2 = Parcel.obtain();
            reply = Parcel.obtain();
            if (windowManagerBinder != null) {
                data2.writeInterfaceToken("android.view.IWindowManager");
                windowManagerBinder.transact(1991, data2, reply, 0);
                reply.readException();
                int singleHandWwitch = reply.readInt();
                Log.i(TAG, "singleHandWwitch = " + singleHandWwitch);
                if (singleHandWwitch == 1) {
                    z = true;
                }
                data2.recycle();
                reply.recycle();
                return z;
            } else if (data2 != null) {
                data2.recycle();
            }
        } catch (RemoteException e) {
            Log.e(TAG, "read singleHandWwitch exception. message = " + e.getMessage());
            int id = this.mContext.getResources().getIdentifier("single_hand_mode", "bool", "androidhwext");
            if (id != 0) {
                try {
                    boolean z2 = this.mContext.getResources().getBoolean(id);
                    if (0 != 0) {
                        data.recycle();
                    }
                    if (0 != 0) {
                        reply.recycle();
                    }
                    return z2;
                } catch (Resources.NotFoundException e2) {
                    Log.e(TAG, "isSupportSingleHand NotFoundException");
                    if (0 != 0) {
                        data.recycle();
                    }
                }
            }
            if (0 != 0) {
            }
        } catch (Throwable th) {
            if (0 != 0) {
                data.recycle();
            }
            if (0 != 0) {
                reply.recycle();
            }
            throw th;
        }
    }
}
