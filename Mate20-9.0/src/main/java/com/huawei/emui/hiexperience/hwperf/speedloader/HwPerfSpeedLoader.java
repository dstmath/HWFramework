package com.huawei.emui.hiexperience.hwperf.speedloader;

import android.widget.AbsListView;
import android.widget.ListView;
import com.huawei.emui.hiexperience.hwperf.HwPerfBase;
import com.huawei.emui.hiexperience.hwperf.utils.HwPerfLog;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;

public class HwPerfSpeedLoader extends HwPerfBase {
    private static String TAG = "HwPerfSpeedLoader";
    private int NUM_OF_ELEMENTS = 3;
    private boolean isCurrentStateHighVelocity = false;
    private boolean isoldStateHighVelocity = false;
    Field mFieldScroller = null;
    Field mFieldViewFlinger = null;
    Field mFileldOverScroller = null;
    private float mMaxSpeedValue = 30000.0f;
    private float mMinSpeedValue = 3000.0f;
    private Object mRecycleListView = null;
    private boolean mSpeedLoadOptimizeEffect = false;
    private String mSpeedLoadValue = "3000:30000:12000";
    private float mThresholdVelocity = 12000.0f;
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

    public boolean HwPerfSetSpeedLoaderListener(Object recycler_list_view, HwPerfVelocityCallback velocityCallback) {
        this.mRecycleListView = recycler_list_view;
        if (this.mRecycleListView == null || velocityCallback == null) {
            HwPerfLog.e(TAG, " HwPerfSetSpeedLoaderListener is failed: please input correct listview/recycleview object");
            return false;
        }
        this.mVelocityCallback = velocityCallback;
        HwPerfLog.d(TAG, this.mRecycleListView.getClass().toString());
        if (this.mRecycleListView instanceof ListView) {
            ((AbsListView) this.mRecycleListView).registerVelocityListener(this);
            setSpeedLoadOptimizeEffect(true);
            return true;
        } else if (!getRecycleviewObject()) {
            return false;
        } else {
            if (this.mFileldOverScroller != null) {
                HwPerfLog.d(TAG, "HwPerfSetSpeedLoaderListener mFileldOverScroller != null");
                try {
                    this.mFileldOverScroller.get(this.mFieldScroller.get(this.mFieldViewFlinger.get(this.mRecycleListView))).getClass().getMethod("registerVelocityListener", new Class[]{HwPerfVelocityCallback.class}).invoke(this.mFileldOverScroller.get(this.mFieldScroller.get(this.mFieldViewFlinger.get(this.mRecycleListView))), new Object[]{velocityCallback});
                    setSpeedLoadOptimizeEffect(true);
                    return true;
                } catch (IllegalAccessException | IllegalArgumentException | NoSuchMethodException | SecurityException | InvocationTargetException e) {
                    HwPerfLog.e(TAG, " register recyclerview failed: can not find/access OverScroller.registerVelocityListener method");
                    return false;
                }
            } else {
                HwPerfLog.d(TAG, "HwPerfSetSpeedLoaderListener mFileldOverScroller == null");
                try {
                    this.mFieldScroller.get(this.mFieldViewFlinger.get(this.mRecycleListView)).getClass().getMethod("registerVelocityListener", new Class[]{HwPerfSpeedLoader.class}).invoke(this.mFieldScroller.get(this.mFieldViewFlinger.get(this.mRecycleListView)), new Object[]{this});
                    setSpeedLoadOptimizeEffect(true);
                    return true;
                } catch (IllegalAccessException | IllegalArgumentException | NoSuchMethodException | SecurityException | InvocationTargetException e2) {
                    HwPerfLog.e(TAG, " register recyclerview failed null: can not find/access OverScroller.registerVelocityListener method");
                    return false;
                }
            }
        }
    }

    public String HwPerfGetSystemRefVelocity() {
        if (this.mVelocityCallback != null && getSpeedLoadOptimizeEffect()) {
            return this.mSpeedLoadValue;
        }
        HwPerfLog.i(TAG, "get system thresholdVelocity failed, please register listview/recyclerview first!! ");
        return "-1:-1:-1";
    }

    public void HwPerfSetThresholdVelocity(float thresholdVelocity) {
        if (this.mVelocityCallback == null || !getSpeedLoadOptimizeEffect()) {
            HwPerfLog.i(TAG, "set thresholdVelocity failed, please register listview/recyclerview first!! ");
            return;
        }
        if (thresholdVelocity < this.mMinSpeedValue) {
            this.mThresholdVelocity = this.mMinSpeedValue;
        } else if (thresholdVelocity > this.mMaxSpeedValue) {
            this.mThresholdVelocity = this.mMaxSpeedValue;
        } else {
            this.mThresholdVelocity = thresholdVelocity;
        }
        String str = TAG;
        HwPerfLog.d(str, " input thresholdVelocity: " + thresholdVelocity + " actutally mThresholdVelocity: " + this.mThresholdVelocity);
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
                    HwPerfLog.d(TAG, " register recyclerview v4/v7: can not find ScrollerCompat.mOverScroller");
                    this.mFileldOverScroller = null;
                }
                if (this.mFileldOverScroller == null) {
                    return true;
                }
                this.mFileldOverScroller.setAccessible(true);
                try {
                    this.mFileldOverScroller.get(this.mFieldScroller.get(this.mFieldViewFlinger.get(this.mRecycleListView)));
                    return true;
                } catch (IllegalAccessException | IllegalArgumentException e2) {
                    HwPerfLog.e(TAG, " register recyclerview failed: can not access ScrollerCompat.mOverScroller ");
                    return false;
                }
            } catch (IllegalAccessException | IllegalArgumentException | NoSuchFieldException | SecurityException e3) {
                HwPerfLog.e(TAG, " register recyclerview failed: can not access ViewFlinger.mScroller");
                return false;
            }
        } catch (IllegalAccessException | IllegalArgumentException | NoSuchFieldException | SecurityException e4) {
            HwPerfLog.e(TAG, " register recyclerview failed: can not access mViewFlinger");
            return false;
        }
    }

    private void setSpeedLoadOptimizeEffect(boolean speedLoadOptimizeEffect) {
        this.mSpeedLoadOptimizeEffect = speedLoadOptimizeEffect;
    }

    private boolean getSpeedLoadOptimizeEffect() {
        return this.mSpeedLoadOptimizeEffect;
    }

    private void parseSystemSpeedValue() {
        String[] speedValue = this.mSpeedLoadValue.split(":");
        if (speedValue.length != this.NUM_OF_ELEMENTS) {
            HwPerfLog.e(TAG, "parseSystemSpeedValue length error");
            return;
        }
        try {
            this.mMinSpeedValue = Float.parseFloat(speedValue[0]);
            this.mMaxSpeedValue = Float.parseFloat(speedValue[1]);
            this.mThresholdVelocity = Float.parseFloat(speedValue[2]);
        } catch (NumberFormatException e) {
            HwPerfLog.e(TAG, "parseSystemSpeedValue input number Format error failed!");
        }
    }

    public void onFlingStart() {
        if (this.mVelocityCallback == null || !getSpeedLoadOptimizeEffect()) {
            HwPerfLog.i(TAG, "onFlingStart failed, please register listview/recyclerview first!! ");
            return;
        }
        this.isoldStateHighVelocity = true;
        this.mVelocityCallback.HwPerfonVelocityUpToThreshold();
    }

    public void onFlingEnd() {
        if (this.mVelocityCallback == null || !getSpeedLoadOptimizeEffect()) {
            HwPerfLog.i(TAG, "onFlingEnd failed, please register listview/recyclerview first!! ");
            return;
        }
        this.isoldStateHighVelocity = false;
        this.mVelocityCallback.HwPerfonVelocityDownToThreshold();
    }

    public void onFlingRunning(float currentVelicity) {
        if (this.mVelocityCallback == null || !getSpeedLoadOptimizeEffect()) {
            HwPerfLog.i(TAG, "onFlingEnd failed, please register listview/recyclerview first!! ");
            return;
        }
        if (currentVelicity > this.mThresholdVelocity) {
            this.isCurrentStateHighVelocity = true;
        } else {
            this.isCurrentStateHighVelocity = false;
        }
        if (this.isCurrentStateHighVelocity != this.isoldStateHighVelocity) {
            this.isoldStateHighVelocity = this.isCurrentStateHighVelocity;
            if (this.isoldStateHighVelocity) {
                this.mVelocityCallback.HwPerfonVelocityUpToThreshold();
            } else {
                this.mVelocityCallback.HwPerfonVelocityDownToThreshold();
            }
        }
    }
}
