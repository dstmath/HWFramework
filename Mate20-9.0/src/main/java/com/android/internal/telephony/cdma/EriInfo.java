package com.android.internal.telephony.cdma;

public final class EriInfo {
    public static final int ROAMING_ICON_MODE_FLASH = 1;
    public static final int ROAMING_ICON_MODE_NORMAL = 0;
    public static final int ROAMING_INDICATOR_FLASH = 2;
    public static final int ROAMING_INDICATOR_OFF = 1;
    public static final int ROAMING_INDICATOR_ON = 0;
    public int alertId;
    public int callPromptId;
    public String eriText;
    public int iconIndex;
    public int iconMode;
    public int roamingIndicator;

    public EriInfo(int roamingIndicator2, int iconIndex2, int iconMode2, String eriText2, int callPromptId2, int alertId2) {
        this.roamingIndicator = roamingIndicator2;
        this.iconIndex = iconIndex2;
        this.iconMode = iconMode2;
        this.eriText = eriText2;
        this.callPromptId = callPromptId2;
        this.alertId = alertId2;
    }
}
