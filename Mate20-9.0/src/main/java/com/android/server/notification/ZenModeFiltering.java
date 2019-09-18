package com.android.server.notification;

import android.app.NotificationManager;
import android.content.ComponentName;
import android.content.Context;
import android.media.AudioAttributes;
import android.os.Bundle;
import android.os.UserHandle;
import android.service.notification.ZenModeConfig;
import android.telecom.TelecomManager;
import android.util.ArrayMap;
import android.util.Slog;
import com.android.internal.util.NotificationMessagingUtil;
import com.android.server.pm.PackageManagerService;
import com.android.server.zrhung.IZRHungService;
import java.io.PrintWriter;
import java.util.Date;

public class ZenModeFiltering {
    private static final boolean DEBUG = ZenModeHelper.DEBUG;
    private static final String DEFAULT_PHONEAPP_PACKAGENAME = "com.android.contacts";
    private static final String RECORD_SBN_PACKAGENAME = "com.android.incallui";
    static final RepeatCallers REPEAT_CALLERS = new RepeatCallers();
    private static final String TAG = "ZenModeHelper";
    private final Context mContext;
    private ComponentName mDefaultPhoneApp;
    private final NotificationMessagingUtil mMessagingUtil;

    private static class RepeatCallers {
        /* access modifiers changed from: private */
        public final ArrayMap<String, Long> mCalls;
        /* access modifiers changed from: private */
        public int mThresholdMinutes;

        private RepeatCallers() {
            this.mCalls = new ArrayMap<>();
        }

        /* access modifiers changed from: private */
        /* JADX WARNING: Code restructure failed: missing block: B:15:0x0028, code lost:
            return;
         */
        public synchronized void recordCall(Context context, Bundle extras) {
            setThresholdMinutes(context);
            if (this.mThresholdMinutes > 0) {
                if (extras != null) {
                    String peopleString = peopleString(extras);
                    if (peopleString != null) {
                        long now = System.currentTimeMillis();
                        cleanUp(this.mCalls, now);
                        this.mCalls.put(peopleString, Long.valueOf(now));
                    }
                }
            }
        }

        /* access modifiers changed from: private */
        /* JADX WARNING: Code restructure failed: missing block: B:15:0x0026, code lost:
            return false;
         */
        public synchronized boolean isRepeat(Context context, Bundle extras) {
            setThresholdMinutes(context);
            if (this.mThresholdMinutes > 0) {
                if (extras != null) {
                    String peopleString = peopleString(extras);
                    if (peopleString == null) {
                        return false;
                    }
                    cleanUp(this.mCalls, System.currentTimeMillis());
                    return this.mCalls.containsKey(peopleString);
                }
            }
        }

        private synchronized void cleanUp(ArrayMap<String, Long> calls, long now) {
            for (int i = calls.size() - 1; i >= 0; i--) {
                long time = this.mCalls.valueAt(i).longValue();
                if (time > now || now - time > ((long) (this.mThresholdMinutes * 1000 * 60))) {
                    calls.removeAt(i);
                }
            }
        }

        private void setThresholdMinutes(Context context) {
            if (this.mThresholdMinutes <= 0) {
                this.mThresholdMinutes = context.getResources().getInteger(17694931);
            }
        }

        private static String peopleString(Bundle extras) {
            String[] extraPeople = ValidateNotificationPeople.getExtraPeople(extras);
            String str = null;
            if (extraPeople == null || extraPeople.length == 0) {
                return null;
            }
            StringBuilder sb = new StringBuilder();
            for (String extraPerson : extraPeople) {
                if (extraPerson != null) {
                    String extraPerson2 = extraPerson.trim();
                    if (!extraPerson2.isEmpty()) {
                        if (sb.length() > 0) {
                            sb.append('|');
                        }
                        sb.append(extraPerson2);
                    }
                }
            }
            if (sb.length() != 0) {
                str = sb.toString();
            }
            return str;
        }
    }

    public ZenModeFiltering(Context context) {
        this.mContext = context;
        this.mMessagingUtil = new NotificationMessagingUtil(this.mContext);
    }

    public ZenModeFiltering(Context context, NotificationMessagingUtil messagingUtil) {
        this.mContext = context;
        this.mMessagingUtil = messagingUtil;
    }

    public void dump(PrintWriter pw, String prefix) {
        pw.print(prefix);
        pw.print("mDefaultPhoneApp=");
        pw.println(this.mDefaultPhoneApp);
        pw.print(prefix);
        pw.print("RepeatCallers.mThresholdMinutes=");
        pw.println(REPEAT_CALLERS.mThresholdMinutes);
        synchronized (REPEAT_CALLERS) {
            if (!REPEAT_CALLERS.mCalls.isEmpty()) {
                pw.print(prefix);
                pw.println("RepeatCallers.mCalls=");
                for (int i = 0; i < REPEAT_CALLERS.mCalls.size(); i++) {
                    pw.print(prefix);
                    pw.print("  ");
                    String callNum = (String) REPEAT_CALLERS.mCalls.keyAt(i);
                    if (callNum.length() >= 4) {
                        callNum = "tel:*" + callNum.substring(callNumLength - 4);
                    }
                    pw.print(callNum);
                    pw.print(" at ");
                    pw.println(ts(((Long) REPEAT_CALLERS.mCalls.valueAt(i)).longValue()));
                }
            }
        }
    }

    private static String ts(long time) {
        return new Date(time) + " (" + time + ")";
    }

    public static boolean matchesCallFilter(Context context, int zen, ZenModeConfig config, UserHandle userHandle, Bundle extras, ValidateNotificationPeople validator, int contactsTimeoutMs, float timeoutAffinity) {
        if (zen == 2 || zen == 3) {
            return false;
        }
        if (zen != 1 || (config.allowRepeatCallers && REPEAT_CALLERS.isRepeat(context, extras))) {
            return true;
        }
        if (!config.allowCalls) {
            return false;
        }
        if (validator != null) {
            return audienceMatches(config.allowCallsFrom, validator.getContactAffinity(userHandle, extras, contactsTimeoutMs, timeoutAffinity));
        }
        return true;
    }

    public static boolean isRepeatCall(Context context, int zen, ZenModeConfig config, Bundle extras) {
        if (zen != 2 && zen != 3 && zen == 1 && config.allowRepeatCallers && REPEAT_CALLERS.isRepeat(context, extras)) {
            return true;
        }
        return false;
    }

    private static Bundle extras(NotificationRecord record) {
        if (record == null || record.sbn == null || record.sbn.getNotification() == null) {
            return null;
        }
        return record.sbn.getNotification().extras;
    }

    /* access modifiers changed from: protected */
    public void recordCall(NotificationRecord record) {
        REPEAT_CALLERS.recordCall(this.mContext, extras(record));
    }

    public boolean shouldIntercept(int zen, ZenModeConfig config, NotificationRecord record) {
        if (zen == 0) {
            return false;
        }
        if (!NotificationManager.Policy.areAllVisualEffectsSuppressed(config.suppressedVisualEffects) || !PackageManagerService.PLATFORM_PACKAGE_NAME.equals(record.sbn.getPackageName()) || 48 != record.sbn.getId()) {
            switch (zen) {
                case 1:
                    if (record.getPackagePriority() == 2) {
                        ZenLog.traceNotIntercepted(record, "priorityApp");
                        return false;
                    } else if (isAlarm(record)) {
                        if (config.allowAlarms) {
                            return false;
                        }
                        ZenLog.traceIntercepted(record, "!allowAlarms");
                        return true;
                    } else if (isCall(record)) {
                        if (config.allowRepeatCallers && REPEAT_CALLERS.isRepeat(this.mContext, extras(record))) {
                            ZenLog.traceNotIntercepted(record, "repeatCaller");
                            return false;
                        } else if (config.allowCalls) {
                            return shouldInterceptAudience(config.allowCallsFrom, record);
                        } else {
                            ZenLog.traceIntercepted(record, "!allowCalls");
                            return true;
                        }
                    } else if (isMessage(record)) {
                        if (config.allowMessages) {
                            return shouldInterceptAudience(config.allowMessagesFrom, record);
                        }
                        ZenLog.traceIntercepted(record, "!allowMessages");
                        return true;
                    } else if (isEvent(record)) {
                        if (config.allowEvents) {
                            return false;
                        }
                        ZenLog.traceIntercepted(record, "!allowEvents");
                        return true;
                    } else if (isReminder(record)) {
                        if (config.allowReminders) {
                            return false;
                        }
                        ZenLog.traceIntercepted(record, "!allowReminders");
                        return true;
                    } else if (isMedia(record)) {
                        if (config.allowMedia) {
                            return false;
                        }
                        ZenLog.traceIntercepted(record, "!allowMedia");
                        return true;
                    } else if (!isSystem(record)) {
                        ZenLog.traceIntercepted(record, "!priority");
                        return true;
                    } else if (config.allowSystem) {
                        return false;
                    } else {
                        ZenLog.traceIntercepted(record, "!allowSystem");
                        return true;
                    }
                case 2:
                    ZenLog.traceIntercepted(record, "none");
                    return true;
                case 3:
                    if (isAlarm(record)) {
                        return false;
                    }
                    ZenLog.traceIntercepted(record, "alarmsOnly");
                    return true;
                default:
                    return false;
            }
        } else {
            ZenLog.traceNotIntercepted(record, "systemDndChangedNotification");
            return false;
        }
    }

    private static boolean shouldInterceptAudience(int source, NotificationRecord record) {
        if (audienceMatches(source, record.getContactAffinity())) {
            return false;
        }
        ZenLog.traceIntercepted(record, "!audienceMatches");
        return true;
    }

    protected static boolean isAlarm(NotificationRecord record) {
        return record.isCategory("alarm") || record.isAudioAttributesUsage(4);
    }

    private static boolean isEvent(NotificationRecord record) {
        return record.isCategory(IZRHungService.PARA_EVENT);
    }

    private static boolean isReminder(NotificationRecord record) {
        return record.isCategory("reminder");
    }

    public boolean isCall(NotificationRecord record) {
        return record != null && (isDefaultPhoneApp(record.sbn.getPackageName()) || record.isCategory("call"));
    }

    public boolean isMedia(NotificationRecord record) {
        AudioAttributes aa = record.getAudioAttributes();
        return aa != null && AudioAttributes.SUPPRESSIBLE_USAGES.get(aa.getUsage()) == 5;
    }

    public boolean isSystem(NotificationRecord record) {
        AudioAttributes aa = record.getAudioAttributes();
        return aa != null && AudioAttributes.SUPPRESSIBLE_USAGES.get(aa.getUsage()) == 6;
    }

    private boolean isDefaultPhoneApp(String pkg) {
        if (this.mDefaultPhoneApp == null) {
            TelecomManager telecomm = (TelecomManager) this.mContext.getSystemService("telecom");
            this.mDefaultPhoneApp = telecomm != null ? telecomm.getDefaultPhoneApp() : null;
            if (DEBUG) {
                Slog.d(TAG, "Default phone app: " + this.mDefaultPhoneApp);
            }
        }
        return pkg != null && this.mDefaultPhoneApp != null && RECORD_SBN_PACKAGENAME.equals(pkg) && DEFAULT_PHONEAPP_PACKAGENAME.equals(this.mDefaultPhoneApp.getPackageName());
    }

    /* access modifiers changed from: protected */
    public boolean isMessage(NotificationRecord record) {
        return this.mMessagingUtil.isMessaging(record.sbn);
    }

    private static boolean audienceMatches(int source, float contactAffinity) {
        boolean z = false;
        switch (source) {
            case 0:
                return true;
            case 1:
                if (contactAffinity >= 0.5f) {
                    z = true;
                }
                return z;
            case 2:
                if (contactAffinity >= 1.0f) {
                    z = true;
                }
                return z;
            default:
                Slog.w(TAG, "Encountered unknown source: " + source);
                return true;
        }
    }
}
