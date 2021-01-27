package ohos.miscservices.timeutility;

import android.app.AlarmManager;
import android.app.PendingIntent;
import java.util.Optional;
import ohos.aafwk.content.Intent;
import ohos.abilityshell.utils.IntentConverter;
import ohos.app.Context;
import ohos.bundle.ElementName;
import ohos.bundle.ShellInfo;
import ohos.miscservices.timeutility.Timer;

public class TranslateUtils {
    private static final String SHELL_SUFFIX_ABILITY = "ShellActivity";
    private static final String SHELL_SUFFIX_BROADCAST = "";
    private static final String SHELL_SUFFIX_SERVICE = "ShellService";
    private static final int TYPE_INVALID = -1;
    private static final int TYPE_MASK = 3;
    private static final int TYPE_MASK_REALTIME = 1;
    private static final int TYPE_MASK_REALTIME_WAKEUP = 3;
    private static final int TYPE_MASK_RTC = 0;
    private static final int TYPE_MASK_RTC_WAKEUP = 2;

    public static int getTimerType(int i) {
        int i2 = i & 3;
        if (i2 == 0) {
            return 1;
        }
        if (i2 == 1) {
            return 3;
        }
        if (i2 != 2) {
            return i2 != 3 ? -1 : 2;
        }
        return 0;
    }

    public static boolean isExactType(int i) {
        return (i & 4) != 0;
    }

    public static boolean isIdleType(int i) {
        return (i & 8) != 0;
    }

    public static PendingIntent getPendingIntent(Context context, Intent intent, int i) {
        android.content.Context context2;
        if (context == null || intent == null) {
            return null;
        }
        Optional<android.content.Intent> operationIntent = getOperationIntent(intent, i);
        android.content.Intent intent2 = operationIntent.isPresent() ? operationIntent.get() : null;
        Object hostContext = context.getHostContext();
        if (hostContext instanceof android.content.Context) {
            context2 = (android.content.Context) hostContext;
        } else {
            context2 = null;
        }
        if (context2 == null || intent2 == null) {
            return null;
        }
        if (i == 1) {
            return PendingIntent.getActivity(context2, 0, intent2, 134217728);
        }
        if (i == 2) {
            return PendingIntent.getBroadcast(context2, 0, intent2, 134217728);
        }
        if (i != 3) {
            return null;
        }
        return PendingIntent.getService(context2, 0, intent2, 134217728);
    }

    private static Optional<android.content.Intent> getOperationIntent(Intent intent, int i) {
        if (intent == null) {
            return Optional.empty();
        }
        ShellInfo shellInfo = null;
        if (i != 2) {
            ElementName element = intent.getElement();
            if (element == null) {
                return Optional.empty();
            }
            String bundleName = element.getBundleName();
            String abilityName = element.getAbilityName();
            if (bundleName == null || abilityName == null) {
                return Optional.empty();
            }
            shellInfo = buildShellInfo(bundleName, abilityName, i);
        }
        return IntentConverter.createAndroidIntent(intent, shellInfo);
    }

    private static ShellInfo buildShellInfo(String str, String str2, int i) {
        ShellInfo shellInfo = new ShellInfo();
        shellInfo.setPackageName(str);
        String str3 = "";
        if (i == 1) {
            str3 = SHELL_SUFFIX_ABILITY;
        } else if (i != 2 && i == 3) {
            str3 = SHELL_SUFFIX_SERVICE;
        }
        shellInfo.setName(str2 + str3);
        return shellInfo;
    }

    public static AlarmManager.OnAlarmListener getOnAlarmListener(final Timer.ITimerListener iTimerListener) {
        if (iTimerListener == null) {
            return null;
        }
        return new AlarmManager.OnAlarmListener() {
            /* class ohos.miscservices.timeutility.TranslateUtils.AnonymousClass1 */

            @Override // android.app.AlarmManager.OnAlarmListener
            public void onAlarm() {
                Timer.ITimerListener.this.onTrigger();
            }
        };
    }
}
