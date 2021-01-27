package com.huawei.emui.hiexperience.hwperf.speedloader;

import android.widget.AbsListView;
import android.widget.ListView;
import com.huawei.emui.hiexperience.hwperf.HwPerfBase;
import com.huawei.emui.hiexperience.hwperf.utils.HwLog;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;

public class HwPerfSpeedLoader extends HwPerfBase {
    private static final int NUM_OF_ELEMENTS = 3;
    private static final float SPEED_THRESHOLD = 12000.0f;
    private static final String SPEED_VALUE_DEFAULT = "3000:30000:12000";
    private static final float SPEED_VALUE_MAX = 30000.0f;
    private static final float SPEED_VALUE_MIN = 3000.0f;
    private static final String SPEED_VALUE_UNNORMAL = "-1:-1:-1";
    private static final String TAG = "HwPerfSpeedLoader";
    private Field mFieldScroller = null;
    private Field mFieldViewFlinger = null;
    private Field mFileldOverScroller = null;
    private boolean mIsCurrentStateHighVelocity = false;
    private boolean mIsOldStateHighVelocity = false;
    private boolean mIsSpeedLoadOptimizeEffect = false;
    private float mMaxSpeedValue = SPEED_VALUE_MAX;
    private float mMinSpeedValue = SPEED_VALUE_MIN;
    private Object mRecycleListView = null;
    private String mSpeedLoadValue = SPEED_VALUE_DEFAULT;
    private float mThresholdVelocity = SPEED_THRESHOLD;
    private HwPerfVelocityCallback mVelocityCallback = null;

    public interface HwPerfVelocityCallback {
        void HwPerfonVelocityDownToThreshold();

        void HwPerfonVelocityUpToThreshold();
    }

    public HwPerfSpeedLoader() {
        setSpeedLoadOptimizeEffect(false);
    }

    public void setSpeedValue(String speedValue) {
        this.mSpeedLoadValue = speedValue;
        parseSystemSpeedValue();
    }

    public boolean HwPerfSetSpeedLoaderListener(Object viewComponent, HwPerfVelocityCallback velocityCallback) {
        this.mRecycleListView = viewComponent;
        Object obj = this.mRecycleListView;
        if (obj == null || velocityCallback == null) {
            HwLog.e(TAG, "HwPerfSetSpeedLoaderListenerfailed: wrong viewComponent object");
            return false;
        }
        this.mVelocityCallback = velocityCallback;
        if (obj instanceof ListView) {
            ((AbsListView) obj).registerVelocityListener(this);
            setSpeedLoadOptimizeEffect(true);
            return true;
        } else if (!getRecycleviewObject()) {
            return false;
        } else {
            Field field = this.mFileldOverScroller;
            if (field != null) {
                try {
                    field.get(this.mFieldScroller.get(this.mFieldViewFlinger.get(this.mRecycleListView))).getClass().getMethod("registerVelocityListener", HwPerfVelocityCallback.class).invoke(this.mFileldOverScroller.get(this.mFieldScroller.get(this.mFieldViewFlinger.get(this.mRecycleListView))), velocityCallback);
                    setSpeedLoadOptimizeEffect(true);
                    return true;
                } catch (IllegalAccessException | IllegalArgumentException | NoSuchMethodException | SecurityException | InvocationTargetException e) {
                    HwLog.e(TAG, "register recyclerview failed: find OverScroller.registerVelocityListener method");
                    return false;
                }
            } else {
                HwLog.d(TAG, "HwPerfSetSpeedLoaderListener mFileldOverScroller == null");
                try {
                    this.mFieldScroller.get(this.mFieldViewFlinger.get(this.mRecycleListView)).getClass().getMethod("registerVelocityListener", HwPerfSpeedLoader.class).invoke(this.mFieldScroller.get(this.mFieldViewFlinger.get(this.mRecycleListView)), this);
                    setSpeedLoadOptimizeEffect(true);
                    return true;
                } catch (IllegalAccessException | IllegalArgumentException | NoSuchMethodException | SecurityException | InvocationTargetException e2) {
                    HwLog.e(TAG, " register recyclerview failed null = method");
                    return false;
                }
            }
        }
    }

    public String HwPerfGetSystemRefVelocity() {
        if (this.mVelocityCallback != null && getSpeedLoadOptimizeEffect()) {
            return this.mSpeedLoadValue;
        }
        HwLog.i(TAG, "get thresholdVelocity failed, register listview/recyclerview first");
        return SPEED_VALUE_UNNORMAL;
    }

    public void HwPerfSetThresholdVelocity(float thresholdVelocity) {
        if (this.mVelocityCallback == null || !getSpeedLoadOptimizeEffect()) {
            HwLog.i(TAG, "set thresholdVelocity failed, register listview/recyclerview first");
            return;
        }
        float f = this.mMinSpeedValue;
        if (thresholdVelocity < f) {
            this.mThresholdVelocity = f;
            return;
        }
        float f2 = this.mMaxSpeedValue;
        if (thresholdVelocity > f2) {
            this.mThresholdVelocity = f2;
        } else {
            this.mThresholdVelocity = thresholdVelocity;
        }
    }

    private boolean getRecycleviewObject() {
        try {
            this.mFieldViewFlinger = this.mRecycleListView.getClass().getDeclaredField("mViewFlinger");
            this.mFieldViewFlinger.setAccessible(true);
            this.mFieldViewFlinger.get(this.mRecycleListView);
            try {
                this.mFieldScroller = this.mFieldViewFlinger.get(this.mRecycleListView).getClass().getDeclaredField("mScroller");
                this.mFieldScroller.setAccessible(true);
                this.mFieldScroller.get(this.mFieldViewFlinger.get(this.mRecycleListView));
                try {
                    this.mFileldOverScroller = this.mFieldScroller.get(this.mFieldViewFlinger.get(this.mRecycleListView)).getClass().getDeclaredField("mScroller");
                } catch (IllegalAccessException | IllegalArgumentException | NoSuchFieldException | SecurityException e) {
                    HwLog.d(TAG, " register recyclerview v4/v7: can not find ScrollerCompat.mOverScroller");
                    this.mFileldOverScroller = null;
                }
                Field field = this.mFileldOverScroller;
                if (field != null) {
                    field.setAccessible(true);
                    try {
                        this.mFileldOverScroller.get(this.mFieldScroller.get(this.mFieldViewFlinger.get(this.mRecycleListView)));
                    } catch (IllegalAccessException | IllegalArgumentException e2) {
                        HwLog.e(TAG, " register recyclerview failed: can not access ScrollerCompat.mOverScroller");
                        return false;
                    }
                }
                return true;
            } catch (IllegalAccessException | IllegalArgumentException | NoSuchFieldException | SecurityException e3) {
                HwLog.e(TAG, " register recyclerview failed: can not access ViewFlinger.mScroller");
                return false;
            }
        } catch (IllegalAccessException | IllegalArgumentException | NoSuchFieldException | SecurityException e4) {
            HwLog.e(TAG, " register recyclerview failed: can not access mViewFlinger");
            return false;
        }
    }

    private void setSpeedLoadOptimizeEffect(boolean isLoadOptimizeEffect) {
        this.mIsSpeedLoadOptimizeEffect = isLoadOptimizeEffect;
    }

    private boolean getSpeedLoadOptimizeEffect() {
        return this.mIsSpeedLoadOptimizeEffect;
    }

    private void parseSystemSpeedValue() {
        String[] speedValues = this.mSpeedLoadValue.split(":");
        if (speedValues.length != 3) {
            HwLog.e(TAG, "parseSystemSpeedValue length error");
            return;
        }
        try {
            this.mMinSpeedValue = Float.parseFloat(speedValues[0]);
            this.mMaxSpeedValue = Float.parseFloat(speedValues[1]);
            this.mThresholdVelocity = Float.parseFloat(speedValues[2]);
        } catch (NumberFormatException e) {
            HwLog.e(TAG, "parseSystemSpeedValue input number Format error failed!");
        }
    }

    public void onFlingStart() {
        if (this.mVelocityCallback == null || !getSpeedLoadOptimizeEffect()) {
            HwLog.i(TAG, "onFlingStart failed, please register listview/recyclerview first!! ");
            return;
        }
        this.mIsOldStateHighVelocity = true;
        this.mVelocityCallback.HwPerfonVelocityUpToThreshold();
    }

    public void onFlingEnd() {
        if (this.mVelocityCallback == null || !getSpeedLoadOptimizeEffect()) {
            HwLog.i(TAG, "onFlingEnd failed, please register listview/recyclerview first!! ");
            return;
        }
        this.mIsOldStateHighVelocity = false;
        this.mVelocityCallback.HwPerfonVelocityDownToThreshold();
    }

    public void onFlingRunning(float currentVelicity) {
        if (this.mVelocityCallback == null || !getSpeedLoadOptimizeEffect()) {
            HwLog.i(TAG, "onFlingEnd failed, please register listview/recyclerview first!! ");
            return;
        }
        if (currentVelicity > this.mThresholdVelocity) {
            this.mIsCurrentStateHighVelocity = true;
        } else {
            this.mIsCurrentStateHighVelocity = false;
        }
        boolean z = this.mIsCurrentStateHighVelocity;
        if (z != this.mIsOldStateHighVelocity) {
            this.mIsOldStateHighVelocity = z;
            if (this.mIsOldStateHighVelocity) {
                this.mVelocityCallback.HwPerfonVelocityUpToThreshold();
            } else {
                this.mVelocityCallback.HwPerfonVelocityDownToThreshold();
            }
        }
    }
}
