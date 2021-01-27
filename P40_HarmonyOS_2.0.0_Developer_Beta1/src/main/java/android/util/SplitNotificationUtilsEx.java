package android.util;

import android.content.Context;
import java.util.List;

public class SplitNotificationUtilsEx {
    public static final int FLOAT_IMS_LIST = 3;
    public static final int SPLIT_IMS_LIST = 2;
    public static final int SPLIT_VIDEO_LIST = 1;
    private SplitNotificationUtils mNotificationUtils;

    public SplitNotificationUtils getNotificationUtils() {
        return this.mNotificationUtils;
    }

    public void setNotificationUtils(SplitNotificationUtils mNotificationUtils2) {
        this.mNotificationUtils = mNotificationUtils2;
    }

    public static synchronized SplitNotificationUtilsEx getInstance(Context context) {
        SplitNotificationUtilsEx splitNotificationUtilsEx;
        synchronized (SplitNotificationUtilsEx.class) {
            splitNotificationUtilsEx = new SplitNotificationUtilsEx();
            splitNotificationUtilsEx.setNotificationUtils(SplitNotificationUtils.getInstance(context));
        }
        return splitNotificationUtilsEx;
    }

    public void addPkgName(String pkgName, int type) {
        this.mNotificationUtils.addPkgName(pkgName, type);
    }

    public List<String> getListPkgName(int type) {
        return this.mNotificationUtils.getListPkgName(type);
    }

    public boolean shouldSkipTriggerFreeform(String pkgName, int userId) {
        return this.mNotificationUtils.shouldSkipTriggerFreeform(pkgName, userId);
    }
}
