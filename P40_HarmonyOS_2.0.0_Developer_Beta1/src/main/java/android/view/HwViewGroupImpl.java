package android.view;

import android.app.ActivityThread;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Parcel;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.util.Log;
import com.huawei.internal.widget.ConstantValues;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class HwViewGroupImpl implements IHwViewGroup {
    private static final String AGP_SERVICE = "AGPService";
    private static final String ALL_VIEW_EFFECT = "all";
    private static final int DELAY_SEND_IDLE_TIME = 50;
    private static final int IDLE_STATE = 0;
    private static final int MILLISECOND_PER_SECOND = 1000;
    private static final int OFFSET_THRESHOLD = 4;
    private static final int SETTLING_STATE = 2;
    private static final int SPEED_STEP_FACTOR = 50;
    private static final int SPEED_THRESHOLD = 200;
    private static final String TAG = "HwViewGroupImpl";
    private static final int TOUCH_SLOP_FOR_720P = 16;
    private static final int TRANSACT_GET_WHITEBLOCK_PARAMS = 181;
    private static final String VIEW_NAME_SEP = ";";
    private static volatile HwViewGroupImpl sInstance = null;
    private int mCurrentMoveCount = 0;
    private Method mDispatchScrollerStateMethod = null;
    private boolean mGetMethodErrored = false;
    private Method mGetScrollState = null;
    private boolean mHasMultiTouch = false;
    private Boolean mIsGetWhiteList = false;
    private long mLastEventTime = 0;
    private int mLastSingleMoveOffsetY = 0;
    private int mLastSpeedY = 0;
    private int mLastX = 0;
    private int mLastY = 0;
    private int mTriggeredMoveCount = 0;
    private String mViewNameWhiteList;

    private HwViewGroupImpl() {
    }

    public static synchronized HwViewGroupImpl getDefault() {
        HwViewGroupImpl hwViewGroupImpl;
        synchronized (HwViewGroupImpl.class) {
            if (sInstance == null) {
                sInstance = new HwViewGroupImpl();
            }
            hwViewGroupImpl = sInstance;
        }
        return hwViewGroupImpl;
    }

    public HwViewGroupImpl newInstance() {
        return new HwViewGroupImpl();
    }

    public void getWhiteBlockWhiteList(ViewGroup viewGroup, Context context) {
        if (!this.mIsGetWhiteList.booleanValue() && isSubRecyclerView(viewGroup)) {
            synchronized (this.mIsGetWhiteList) {
                if (!this.mIsGetWhiteList.booleanValue()) {
                    this.mIsGetWhiteList = true;
                    String[] params = new String[2];
                    try {
                        params[0] = ActivityThread.currentPackageName();
                        params[1] = context.getPackageManager().getPackageInfo(params[0], 0).versionName;
                        new WhiteBlockTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, params);
                    } catch (PackageManager.NameNotFoundException | IllegalStateException ex) {
                        Log.e(TAG, "get package info or execute task failed:" + ex);
                    }
                }
            }
        }
    }

    public Boolean isWhiteBlockEnable(ViewGroup viewGroup) {
        String str;
        if (!isSubRecyclerView(viewGroup) || (str = this.mViewNameWhiteList) == null) {
            return false;
        }
        if (!str.equals(ALL_VIEW_EFFECT)) {
            String str2 = this.mViewNameWhiteList;
            if (str2.indexOf(viewGroup.getClass().getName() + VIEW_NAME_SEP) == -1) {
                return false;
            }
        }
        return true;
    }

    public boolean isSubRecyclerView(ViewGroup instance) {
        Class clazz = instance.getClass();
        if (clazz == null) {
            return false;
        }
        clazz.getName();
        if (!clazz.getName().contains("RecyclerView")) {
            return false;
        }
        while (clazz != null) {
            if ("android.support.v7.widget.RecyclerView".equals(clazz.getName()) || "androidx.recyclerview.widget.RecyclerView".equals(clazz.getName())) {
                return true;
            }
            clazz = clazz.getSuperclass();
        }
        return false;
    }

    public boolean accelerateSliding(Context context, boolean isAccelerateSliding, MotionEvent ev) {
        boolean result = false;
        int actionMasked = ev.getAction() & ConstantValues.MAX_CHANNEL_VALUE;
        if (actionMasked == 0 || actionMasked == 1) {
            result = false;
            reset(ev);
        }
        if (actionMasked == 5 || actionMasked == 6) {
            this.mHasMultiTouch = true;
        }
        if (actionMasked == 2) {
            int i = this.mTriggeredMoveCount;
            if ((i != 0 && this.mCurrentMoveCount > i) || this.mHasMultiTouch) {
                return result;
            }
            this.mCurrentMoveCount++;
            int touchSlop = ViewConfiguration.get(context).getScaledTouchSlop();
            int moveY = (int) (ev.getY() + 0.5f);
            int moveX = (int) (ev.getX() + 0.5f);
            int singleMoveOffsetY = Math.abs(moveY - this.mLastY);
            int singleMoveOffsetX = Math.abs(moveX - this.mLastX);
            int offsetTime = (int) (ev.getEventTime() - this.mLastEventTime);
            int speedY = offsetTime == 0 ? 0 : (singleMoveOffsetY * 1000) / offsetTime;
            int i2 = this.mCurrentMoveCount;
            int speedThreashold = ((((i2 - 1) * 50) + 200) * touchSlop) / 16;
            int offsetThreashold = (touchSlop * 4) / 16;
            if (!isAccelerateSliding && this.mTriggeredMoveCount == 0 && singleMoveOffsetY > singleMoveOffsetX && speedY >= speedThreashold && singleMoveOffsetY >= offsetThreashold) {
                result = true;
                this.mTriggeredMoveCount = i2;
                Log.i(TAG, "accelerateSliding accelerate sliding.. mTriggeredMoveCount = " + this.mTriggeredMoveCount);
            }
            this.mLastSingleMoveOffsetY = singleMoveOffsetY;
            this.mLastY = moveY;
            this.mLastX = moveX;
            this.mLastSpeedY = speedY;
            this.mLastEventTime = ev.getEventTime();
        }
        return result;
    }

    public void accelerateWhiteBlockDisplay(MotionEvent ev, final ViewGroup viewGroup) {
        if (!this.mGetMethodErrored) {
            int state = 0;
            try {
                if (this.mGetScrollState == null) {
                    this.mGetScrollState = getReflectMethod(viewGroup, "getScrollState", new Class[0]);
                    if (this.mGetScrollState == null) {
                        Log.e(TAG, "can not fetch method:getScrollState from recyclerView.");
                        this.mGetMethodErrored = true;
                        return;
                    }
                }
                this.mGetScrollState.setAccessible(true);
                state = ((Integer) this.mGetScrollState.invoke(viewGroup, new Object[0])).intValue();
            } catch (IllegalAccessException | InvocationTargetException e) {
                Log.d(TAG, "invoke method getScrollState failed");
                this.mGetMethodErrored = true;
            }
            if (state == 2) {
                new Handler() {
                    /* class android.view.HwViewGroupImpl.AnonymousClass1 */

                    @Override // android.os.Handler
                    public void handleMessage(Message msg) {
                        HwViewGroupImpl.this.sendScrollerIdleState(viewGroup);
                    }
                }.sendEmptyMessageDelayed(0, 50);
            }
        }
    }

    private void reset(MotionEvent ev) {
        this.mCurrentMoveCount = 0;
        this.mTriggeredMoveCount = 0;
        this.mLastY = (int) (ev.getY() + 0.5f);
        this.mLastX = (int) (ev.getX() + 0.5f);
        this.mLastEventTime = ev.getEventTime();
        this.mLastSpeedY = 0;
        this.mLastSingleMoveOffsetY = 0;
        this.mHasMultiTouch = false;
    }

    private Method getReflectMethod(Object object, String methodName, Class<?>... parameterTypes) {
        for (Class<?> clazz = object.getClass(); clazz != Object.class; clazz = clazz.getSuperclass()) {
            try {
                return clazz.getDeclaredMethod(methodName, parameterTypes);
            } catch (NoSuchMethodException e) {
            }
        }
        return null;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void sendScrollerIdleState(ViewGroup viewGroup) {
        try {
            if (this.mDispatchScrollerStateMethod == null) {
                this.mDispatchScrollerStateMethod = getReflectMethod(viewGroup, "dispatchOnScrollStateChanged", Integer.TYPE);
                if (this.mDispatchScrollerStateMethod == null) {
                    Log.e(TAG, "can not fetch method:dispatchOnScrollStateChanged from recyclerView.");
                    this.mGetMethodErrored = true;
                    return;
                }
            }
            this.mDispatchScrollerStateMethod.setAccessible(true);
            this.mDispatchScrollerStateMethod.invoke(viewGroup, 0);
        } catch (IllegalAccessException | InvocationTargetException e) {
            this.mGetMethodErrored = true;
        }
    }

    public class WhiteBlockTask extends AsyncTask<String, Void, String> {
        public WhiteBlockTask() {
        }

        /* access modifiers changed from: protected */
        @Override // android.os.AsyncTask
        public void onPreExecute() {
        }

        /* access modifiers changed from: protected */
        public String doInBackground(String... params) {
            Parcel data = Parcel.obtain();
            Parcel reply = Parcel.obtain();
            try {
                IBinder agpService = ServiceManager.getService(HwViewGroupImpl.AGP_SERVICE);
                if (agpService == null) {
                    Log.e(HwViewGroupImpl.TAG, "getWhiteBlockSwitch: get apgservice failed");
                    return null;
                }
                String currentPackageName = params[0];
                String currentVersionName = params[1];
                data.writeInterfaceToken(HwViewGroupImpl.AGP_SERVICE);
                data.writeString(currentPackageName);
                data.writeString(currentVersionName);
                agpService.transact(HwViewGroupImpl.TRANSACT_GET_WHITEBLOCK_PARAMS, data, reply, 0);
                String readString = reply.readString();
                reply.recycle();
                data.recycle();
                return readString;
            } catch (RemoteException e) {
                Log.e(HwViewGroupImpl.TAG, "get whilte block white list failed:" + e);
                return null;
            } finally {
                reply.recycle();
                data.recycle();
            }
        }

        /* access modifiers changed from: protected */
        public void onProgressUpdate(Void... params) {
        }

        /* access modifiers changed from: protected */
        public void onPostExecute(String result) {
            HwViewGroupImpl.this.mViewNameWhiteList = result;
        }
    }
}
