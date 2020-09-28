package com.huawei.emui.hiexperience.hwperf;

import android.content.Context;
import android.os.SystemProperties;
import com.huawei.emui.hiexperience.hwperf.imageview.HwPerfImageEffect;
import com.huawei.emui.hiexperience.hwperf.smoothslide.HwPerfSmoothSlideAnimation;
import com.huawei.emui.hiexperience.hwperf.speedloader.HwPerfSpeedLoader;
import com.huawei.emui.hiexperience.hwperf.threadpool.HwPerfThreadPoolSize;
import com.huawei.emui.hiexperience.hwperf.thumbnailmanager.HwPerfThumbnailManager;
import com.huawei.emui.hiexperience.hwperf.utils.HwLog;

public class HwPerfFactory {
    private static final int ALL_PROP_AWITCH_ARRAY_SIZE = 2;
    private static final int ALL_PROP_SWITCH_ADDRESS = 0;
    private static final int ALL_PROP_SWITCH_ARRAY_ADDRESS = 0;
    private static final int CORE_NUM_STRING_ADDRESS = 1;
    private static final String ENABLE_STRING = "1";
    public static final int FEATURE_FLINGER_VELICITY = 1;
    public static final int FEATURE_IMAGE_FADE = 2;
    public static final int FEATURE_POOL_SIZE = 4;
    public static final int FEATURE_SMOOTH_SLIDE = 5;
    public static final int FEATURE_THUMB_IMAGE = 3;
    private static final String HWEMUI_HWPERF_FEATURE_PROP = SystemProperties.get("hw_emui_hwperf_all_prop", "0 0 0 0 0,3000:30000:12000 -1");
    private static final int IMAGE_FADE_PROP_ADDRESS = 2;
    private static final int LINGER_VELICITY_PROP_ADDRESS = 1;
    private static final int MAX_CORE_NUM = 16;
    private static final int MIN_CORE_NUM = 1;
    private static final int MIN_CORE_UNNORMAL = -1;
    private static final int POOL_SIZE_PROP_ADDRESS = 4;
    private static final int SPEED_ARRAY_ADDRESS = 1;
    private static final int SPEED_ARRAY_SIZE = 3;
    private static final int SPEED_PROP_ARRAY_SIZE = 2;
    private static final int SPEED_STRRING_ADDRESS = 0;
    private static final int SUB_PROP_AWITCH_ARRAY_SIZE = 5;
    private static final String TAG = "HwPerfFactory";
    private static final int THUMB_IMAGE_PROP_ADDRESS = 3;
    private static int sCoreNumInt = MIN_CORE_UNNORMAL;
    private static HwPerfFactory sFactory = null;
    private static boolean sIsAllEnable = false;
    private static boolean sIsFlingVelociyEnable = false;
    private static boolean sIsImageFadeEnable = false;
    private static boolean sIsSmoothSlideEnable = false;
    private static boolean sIsThreadPoolEnable = false;
    private static boolean sIsThumbnailEnable = false;
    private static String sSpeedValueString = "3000:30000:12000";
    private Context mContext = null;
    private HwPerfBase mHwPerfBase = null;
    private HwPerfSmoothSlideAnimation mHwPerfSmoothSlideAnimation = null;
    private HwPerfSpeedLoader mHwPerfSpeedLoader = null;
    private HwPerfThreadPoolSize mHwPerfThreadPoolSize = null;

    private static void initProp() {
        sIsAllEnable = false;
        String[] propArray = HWEMUI_HWPERF_FEATURE_PROP.split(",");
        if (propArray.length != 2) {
            HwLog.e(TAG, " HWEMUI_HWPERF_FEATURE_PROP length error!");
            return;
        }
        String allSwitchArray = propArray[0];
        String speedArray = propArray[1];
        String[] switchArray = allSwitchArray.split(" ");
        if (switchArray.length != 5) {
            HwLog.e(TAG, " HWEMUI_HWPERF_FEATURE_PROP allswitchArray length error!");
            return;
        }
        sIsAllEnable = ENABLE_STRING.equals(switchArray[0]);
        sIsFlingVelociyEnable = ENABLE_STRING.equals(switchArray[1]);
        sIsImageFadeEnable = ENABLE_STRING.equals(switchArray[2]);
        sIsThumbnailEnable = ENABLE_STRING.equals(switchArray[3]);
        sIsThreadPoolEnable = ENABLE_STRING.equals(switchArray[4]);
        String[] speedArraySplits = speedArray.split(" ");
        if (speedArraySplits.length != 2) {
            HwLog.e(TAG, " HWEMUI_HWPERF_FEATURE_PROP speedArraySplits length error!");
            sIsAllEnable = false;
            return;
        }
        String speedValueString = speedArraySplits[0];
        String coreNumString = speedArraySplits[1];
        if (speedValueString.split(":").length != 3) {
            HwLog.e(TAG, " HWEMUI_HWPERF_FEATURE_PROP speedArrayNums length error!");
            sIsAllEnable = false;
            return;
        }
        sSpeedValueString = speedValueString;
        try {
            sCoreNumInt = Integer.parseInt(coreNumString);
            if (sCoreNumInt < 1 || sCoreNumInt > MAX_CORE_NUM) {
                sCoreNumInt = MIN_CORE_UNNORMAL;
            }
        } catch (NumberFormatException e) {
            HwLog.e(TAG, " HWEMUI_HWPERF_FEATURE_PROP sCoreNumInt NumberFormatException error!");
            sIsAllEnable = false;
        }
    }

    public static final HwPerfFactory getInstance(Context context) {
        if (sFactory == null) {
            initProp();
            if (!sIsAllEnable) {
                return null;
            }
            sFactory = new HwPerfFactory();
        }
        HwPerfFactory hwPerfFactory = sFactory;
        hwPerfFactory.mContext = context;
        return hwPerfFactory;
    }

    public HwPerfBase createFeature(int featureId) {
        if (featureId != 1) {
            if (featureId != 2) {
                if (featureId != 3) {
                    if (featureId != 4) {
                        if (featureId != 5) {
                            this.mHwPerfBase = null;
                        } else {
                            this.mHwPerfSmoothSlideAnimation = new HwPerfSmoothSlideAnimation(this.mContext);
                            sIsSmoothSlideEnable = this.mHwPerfSmoothSlideAnimation.isOptimizeEnable();
                            if (isFeatureEnable(featureId)) {
                                this.mHwPerfBase = this.mHwPerfSmoothSlideAnimation;
                            }
                        }
                    } else if (isFeatureEnable(featureId)) {
                        this.mHwPerfThreadPoolSize = new HwPerfThreadPoolSize(this.mContext);
                        this.mHwPerfThreadPoolSize.setPoolSize(sCoreNumInt);
                        this.mHwPerfBase = this.mHwPerfThreadPoolSize;
                    }
                } else if (isFeatureEnable(featureId)) {
                    this.mHwPerfBase = new HwPerfThumbnailManager(this.mContext);
                }
            } else if (isFeatureEnable(featureId)) {
                this.mHwPerfBase = new HwPerfImageEffect(this.mContext);
            }
        } else if (isFeatureEnable(featureId)) {
            this.mHwPerfSpeedLoader = new HwPerfSpeedLoader();
            this.mHwPerfSpeedLoader.setSpeedValue(sSpeedValueString);
            this.mHwPerfBase = this.mHwPerfSpeedLoader;
        }
        return this.mHwPerfBase;
    }

    public boolean isFeatureEnable(int featureId) {
        boolean isEnable = false;
        if (featureId == 1) {
            if (sIsAllEnable) {
                isEnable = sIsFlingVelociyEnable;
            }
            return isEnable;
        } else if (featureId == 2) {
            if (sIsAllEnable) {
                isEnable = sIsImageFadeEnable;
            }
            return isEnable;
        } else if (featureId == 3) {
            if (sIsAllEnable) {
                isEnable = sIsThumbnailEnable;
            }
            return isEnable;
        } else if (featureId == 4) {
            if (sIsAllEnable) {
                isEnable = sIsThreadPoolEnable;
            }
            return isEnable;
        } else if (featureId != 5) {
            return false;
        } else {
            if (sIsAllEnable) {
                isEnable = sIsSmoothSlideEnable;
            }
            return isEnable;
        }
    }
}
