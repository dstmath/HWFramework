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

    public EriInfo(int roamingIndicator, int iconIndex, int iconMode, String eriText, int callPromptId, int alertId) {
        this.roamingIndicator = roamingIndicator;
        this.iconIndex = iconIndex;
        this.iconMode = iconMode;
        this.eriText = eriText;
        this.callPromptId = callPromptId;
        this.alertId = alertId;
    }
}
