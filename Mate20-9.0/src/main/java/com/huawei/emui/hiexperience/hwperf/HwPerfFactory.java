package com.huawei.emui.hiexperience.hwperf;

import android.content.Context;
import android.os.SystemProperties;
import com.huawei.emui.hiexperience.hwperf.imageview.HwPerfImageEffect;
import com.huawei.emui.hiexperience.hwperf.speedloader.HwPerfSpeedLoader;
import com.huawei.emui.hiexperience.hwperf.threadpool.HwPerfThreadPoolSize;
import com.huawei.emui.hiexperience.hwperf.thumbnailmanager.HwPerfThumbnailManager;
import com.huawei.emui.hiexperience.hwperf.utils.HwPerfLog;

public class HwPerfFactory {
    private static final int ALL_PROP_AWITCH_ARRAY_SIZE = 2;
    private static final int ALL_PROP_SWITCH_ADDRESS = 0;
    private static final int ALL_PROP_SWITCH_ARRAY_ADDRESS = 0;
    private static final int CORE_NUM_STRING_ADDRESS = 1;
    public static final int FEATURE_FLINGER_VELICITY = 1;
    public static final int FEATURE_IMAGE_FADE = 2;
    public static final int FEATURE_LAST_ID = 6;
    public static final int FEATURE_LIST_PRELOAD = 5;
    public static final int FEATURE_POOL_SIZE = 4;
    public static final int FEATURE_THUMB_IMAGE = 3;
    private static boolean HWEMUI_HWPERF_ALL_PROP = false;
    private static final String HWEMUI_HWPERF_FEATURE_PROP = SystemProperties.get("hw_emui_hwperf_all_prop", "0 0 0 0 0,3000:30000:12000 -1");
    private static boolean HWEMUI_HWPERF_FLINGER_VELICITY_PROP = false;
    private static boolean HWEMUI_HWPERF_IMAGE_FADE_PROP = false;
    private static boolean HWEMUI_HWPERF_POOL_SIZE_PROP = false;
    private static boolean HWEMUI_HWPERF_THUMB_IMAGE_PROP = false;
    private static final int IMAGE_FADE_PROP_ADDRESS = 2;
    private static final int LINGER_VELICITY_PROP_ADDRESS = 1;
    public static final int MAX_CORE_NUM = 50;
    public static final int MIN_CORE_NUM = 0;
    public static final int MIN_CORE_UNNORMAL = -1;
    private static int NUM_OF_ELEMENTS = 3;
    private static final int POOL_SIZE_PROP_ADDRESS = 4;
    private static final int SPEED_ARRAY_ADDRESS = 1;
    private static final int SPEED_ARRAY_SIZE = 3;
    private static final int SPEED_PROP_ARRAY_SIZE = 2;
    private static final int SPEED_STRRING_ADDRESS = 0;
    private static final int SUB_PROP_AWITCH_ARRAY_SIZE = 5;
    private static final String TAG = "HwPerfFactory";
    private static final int THUMB_IMAGE_PROP_ADDRESS = 3;
    private static int mCoreNumInt = -1;
    public static HwPerfFactory mFactory = null;
    private static String mSpeedValueString = "3000:30000:12000";
    public Context mContext = null;
    HwPerfSpeedLoader mHwPerfSpeedLoader = null;
    HwPerfThreadPoolSize mHwPerfThreadPoolSize = null;
    HwPerfBase perfBase = null;

    private static void initProp() {
        String[] propArray = HWEMUI_HWPERF_FEATURE_PROP.split(",");
        HwPerfLog.d(TAG, " HWEMUI_HWPERF_FEATURE_PROP: " + HWEMUI_HWPERF_FEATURE_PROP);
        if (propArray.length == 2) {
            String allSwitchArray = propArray[0];
            String speedArray = propArray[1];
            String[] switchArray = allSwitchArray.split(" ");
            if (switchArray.length == 5) {
                HWEMUI_HWPERF_ALL_PROP = "1".equals(switchArray[0]);
                HWEMUI_HWPERF_FLINGER_VELICITY_PROP = "1".equals(switchArray[1]);
                HWEMUI_HWPERF_IMAGE_FADE_PROP = "1".equals(switchArray[2]);
                HWEMUI_HWPERF_THUMB_IMAGE_PROP = "1".equals(switchArray[3]);
                HWEMUI_HWPERF_POOL_SIZE_PROP = "1".equals(switchArray[4]);
                String[] speedArraySplit = speedArray.split(" ");
                if (speedArraySplit.length == 2) {
                    String speedValueString = speedArraySplit[0];
                    String coreNumString = speedArraySplit[1];
                    if (speedValueString.split(":").length == 3) {
                        mSpeedValueString = speedValueString;
                        try {
                            mCoreNumInt = Integer.parseInt(coreNumString);
                            if (mCoreNumInt < 0 || mCoreNumInt > 50) {
                                mCoreNumInt = -1;
                            }
                        } catch (NumberFormatException e) {
                            HwPerfLog.e(TAG, " HWEMUI_HWPERF_FEATURE_PROP mCoreNumInt NumberFormatException error!");
                            HWEMUI_HWPERF_ALL_PROP = false;
                        }
                    } else {
                        HwPerfLog.e(TAG, " HWEMUI_HWPERF_FEATURE_PROP speedArrayNum length error!");
                        HWEMUI_HWPERF_ALL_PROP = false;
                    }
                } else {
                    HwPerfLog.e(TAG, " HWEMUI_HWPERF_FEATURE_PROP speedArraySplit length error!");
                    HWEMUI_HWPERF_ALL_PROP = false;
                }
            } else {
                HwPerfLog.e(TAG, " HWEMUI_HWPERF_FEATURE_PROP allswitchArray length error!");
                HWEMUI_HWPERF_ALL_PROP = false;
            }
        } else {
            HwPerfLog.e(TAG, " HWEMUI_HWPERF_FEATURE_PROP length error!");
            HWEMUI_HWPERF_ALL_PROP = false;
        }
    }

    public static final HwPerfFactory getInstance(Context context) {
        if (mFactory == null) {
            initProp();
            if (!HWEMUI_HWPERF_ALL_PROP) {
                return null;
            }
            mFactory = new HwPerfFactory();
        }
        mFactory.mContext = context;
        return mFactory;
    }

    public HwPerfBase createFeature(int featureid) {
        switch (featureid) {
            case 1:
                if (isFeatureEnable(featureid)) {
                    this.mHwPerfSpeedLoader = new HwPerfSpeedLoader();
                    this.mHwPerfSpeedLoader.setSpeedValue(mSpeedValueString);
                    this.perfBase = this.mHwPerfSpeedLoader;
                    break;
                }
                break;
            case 2:
                if (isFeatureEnable(featureid)) {
                    this.perfBase = new HwPerfImageEffect(this.mContext);
                    break;
                }
                break;
            case FEATURE_THUMB_IMAGE /*3*/:
                if (isFeatureEnable(featureid)) {
                    this.perfBase = new HwPerfThumbnailManager(this.mContext);
                    break;
                }
                break;
            case 4:
                if (isFeatureEnable(featureid)) {
                    this.mHwPerfThreadPoolSize = new HwPerfThreadPoolSize(this.mContext);
                    this.mHwPerfThreadPoolSize.setPoolSize(mCoreNumInt);
                    this.perfBase = this.mHwPerfThreadPoolSize;
                    break;
                }
                break;
            default:
                this.perfBase = null;
                break;
        }
        return this.perfBase;
    }

    public boolean isFeatureEnable(int featureid) {
        boolean result = false;
        switch (featureid) {
            case 1:
                if (HWEMUI_HWPERF_ALL_PROP) {
                    result = HWEMUI_HWPERF_FLINGER_VELICITY_PROP;
                }
                return result;
            case 2:
                if (HWEMUI_HWPERF_ALL_PROP) {
                    result = HWEMUI_HWPERF_IMAGE_FADE_PROP;
                }
                return result;
            case FEATURE_THUMB_IMAGE /*3*/:
                if (HWEMUI_HWPERF_ALL_PROP) {
                    result = HWEMUI_HWPERF_THUMB_IMAGE_PROP;
                }
                return result;
            case 4:
                if (HWEMUI_HWPERF_ALL_PROP) {
                    result = HWEMUI_HWPERF_POOL_SIZE_PROP;
                }
                return result;
            default:
                return false;
        }
    }
}
