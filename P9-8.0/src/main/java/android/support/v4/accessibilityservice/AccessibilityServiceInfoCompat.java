package android.support.v4.accessibilityservice;

import android.accessibilityservice.AccessibilityServiceInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Build.VERSION;
import android.support.annotation.RequiresApi;

public final class AccessibilityServiceInfoCompat {
    public static final int CAPABILITY_CAN_FILTER_KEY_EVENTS = 8;
    public static final int CAPABILITY_CAN_REQUEST_ENHANCED_WEB_ACCESSIBILITY = 4;
    public static final int CAPABILITY_CAN_REQUEST_TOUCH_EXPLORATION = 2;
    public static final int CAPABILITY_CAN_RETRIEVE_WINDOW_CONTENT = 1;
    @Deprecated
    public static final int DEFAULT = 1;
    public static final int FEEDBACK_ALL_MASK = -1;
    public static final int FEEDBACK_BRAILLE = 32;
    public static final int FLAG_INCLUDE_NOT_IMPORTANT_VIEWS = 2;
    public static final int FLAG_REPORT_VIEW_IDS = 16;
    public static final int FLAG_REQUEST_ENHANCED_WEB_ACCESSIBILITY = 8;
    public static final int FLAG_REQUEST_FILTER_KEY_EVENTS = 32;
    public static final int FLAG_REQUEST_TOUCH_EXPLORATION_MODE = 4;
    private static final AccessibilityServiceInfoBaseImpl IMPL;

    static class AccessibilityServiceInfoBaseImpl {
        AccessibilityServiceInfoBaseImpl() {
        }

        public int getCapabilities(AccessibilityServiceInfo info) {
            if (AccessibilityServiceInfoCompat.getCanRetrieveWindowContent(info)) {
                return 1;
            }
            return 0;
        }

        public String loadDescription(AccessibilityServiceInfo info, PackageManager pm) {
            return null;
        }
    }

    @RequiresApi(16)
    static class AccessibilityServiceInfoApi16Impl extends AccessibilityServiceInfoBaseImpl {
        AccessibilityServiceInfoApi16Impl() {
        }

        public String loadDescription(AccessibilityServiceInfo info, PackageManager pm) {
            return info.loadDescription(pm);
        }
    }

    @RequiresApi(18)
    static class AccessibilityServiceInfoApi18Impl extends AccessibilityServiceInfoApi16Impl {
        AccessibilityServiceInfoApi18Impl() {
        }

        public int getCapabilities(AccessibilityServiceInfo info) {
            return info.getCapabilities();
        }
    }

    static {
        if (VERSION.SDK_INT >= 18) {
            IMPL = new AccessibilityServiceInfoApi18Impl();
        } else if (VERSION.SDK_INT >= 16) {
            IMPL = new AccessibilityServiceInfoApi16Impl();
        } else {
            IMPL = new AccessibilityServiceInfoBaseImpl();
        }
    }

    private AccessibilityServiceInfoCompat() {
    }

    @Deprecated
    public static String getId(AccessibilityServiceInfo info) {
        return info.getId();
    }

    @Deprecated
    public static ResolveInfo getResolveInfo(AccessibilityServiceInfo info) {
        return info.getResolveInfo();
    }

    @Deprecated
    public static String getSettingsActivityName(AccessibilityServiceInfo info) {
        return info.getSettingsActivityName();
    }

    @Deprecated
    public static boolean getCanRetrieveWindowContent(AccessibilityServiceInfo info) {
        return info.getCanRetrieveWindowContent();
    }

    @Deprecated
    public static String getDescription(AccessibilityServiceInfo info) {
        return info.getDescription();
    }

    public static String loadDescription(AccessibilityServiceInfo info, PackageManager packageManager) {
        return IMPL.loadDescription(info, packageManager);
    }

    public static String feedbackTypeToString(int feedbackType) {
        StringBuilder builder = new StringBuilder();
        builder.append("[");
        while (feedbackType > 0) {
            int feedbackTypeFlag = 1 << Integer.numberOfTrailingZeros(feedbackType);
            feedbackType &= ~feedbackTypeFlag;
            if (builder.length() > 1) {
                builder.append(", ");
            }
            switch (feedbackTypeFlag) {
                case 1:
                    builder.append("FEEDBACK_SPOKEN");
                    break;
                case 2:
                    builder.append("FEEDBACK_HAPTIC");
                    break;
                case 4:
                    builder.append("FEEDBACK_AUDIBLE");
                    break;
                case 8:
                    builder.append("FEEDBACK_VISUAL");
                    break;
                case 16:
                    builder.append("FEEDBACK_GENERIC");
                    break;
                default:
                    break;
            }
        }
        builder.append("]");
        return builder.toString();
    }

    public static String flagToString(int flag) {
        switch (flag) {
            case 1:
                return "DEFAULT";
            case 2:
                return "FLAG_INCLUDE_NOT_IMPORTANT_VIEWS";
            case 4:
                return "FLAG_REQUEST_TOUCH_EXPLORATION_MODE";
            case 8:
                return "FLAG_REQUEST_ENHANCED_WEB_ACCESSIBILITY";
            case 16:
                return "FLAG_REPORT_VIEW_IDS";
            case 32:
                return "FLAG_REQUEST_FILTER_KEY_EVENTS";
            default:
                return null;
        }
    }

    public static int getCapabilities(AccessibilityServiceInfo info) {
        return IMPL.getCapabilities(info);
    }

    public static String capabilityToString(int capability) {
        switch (capability) {
            case 1:
                return "CAPABILITY_CAN_RETRIEVE_WINDOW_CONTENT";
            case 2:
                return "CAPABILITY_CAN_REQUEST_TOUCH_EXPLORATION";
            case 4:
                return "CAPABILITY_CAN_REQUEST_ENHANCED_WEB_ACCESSIBILITY";
            case 8:
                return "CAPABILITY_CAN_FILTER_KEY_EVENTS";
            default:
                return "UNKNOWN";
        }
    }
}
