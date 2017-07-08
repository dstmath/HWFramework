package android.rog;

import java.util.List;

public interface IRogManager {
    public static final String INFO_KEY = "info_key";
    public static final int ROG3_FEATURE_SUPPORT_FLAG = 1;
    public static final boolean ROG_DEBUG_SWITCH = false;
    public static final String ROG_PERMISSION = "com.huawei.rog.permission.UPDATE_ROG_INFO";
    public static final String ROG_SERVICE = "rog_service";
    public static final String SWITCH_KEY = "switch_state_key";

    List<AppRogInfo> getAppRogInfos();

    AppRogInfo getOwnAppRogInfo(IHwRogListener iHwRogListener);

    boolean getRogSwitchState();

    AppRogInfo getSpecifiedAppRogInfo(String str);

    boolean isRogSupported();

    boolean registerRogListener(IHwRogListener iHwRogListener);

    void setRogSwitchState(boolean z);

    void unRegisterRogListener(IHwRogListener iHwRogListener);

    AppRogInfo updateAppRogInfo(AppRogInfo appRogInfo);

    List<AppRogInfo> updateBatchAppRogInfo(List<AppRogInfo> list);
}
