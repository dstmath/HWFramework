package android.view;

import java.util.List;

@Deprecated
public class HwAdCleaner implements IHwAdCleaner {
    private static final int CODE_GET_AD_KEY_LIST = 1016;
    private static final String DESCRIPTOR_HW_AD_CLEANER = "android.view.HwAdCleaner";
    private static final String TAG = "HwAdCleaner";
    private static final int VIEW_IS_ADVIEW = 0;
    private static final int VIEW_IS_NOT_ADVIEW_OR_DISABLE = 1;
    private static final int VIEW_IS_NOT_CHECKED = -1;
    private static HwAdCleaner mInstance = null;
    private List<String> mAdIdList = null;
    private List<String> mAdViewList = null;
    private String mApkName = "none";
    private boolean mEnableAdChecked = true;
    private boolean mEnableDebug = false;
    private boolean mInit = true;
    private boolean mRulesHasRead = false;

    @Deprecated
    private HwAdCleaner() {
    }

    @Deprecated
    public static synchronized HwAdCleaner getDefault() {
        synchronized (HwAdCleaner.class) {
        }
        return null;
    }

    @Deprecated
    public void readRulesInThread() {
    }

    @Deprecated
    public boolean doReadRulesByBinder() {
        return false;
    }

    @Deprecated
    public int checkAdCleaner(View inView, String appName, int uid) {
        return -1;
    }

    @Deprecated
    private boolean isViewMatched(String viewClsName, String viewIdName) {
        return false;
    }
}
