package com.android.server.notification;

import android.content.ComponentName;
import android.content.Context;
import android.os.Bundle;
import android.os.UserHandle;
import android.provider.Settings.Secure;
import android.service.notification.ZenModeConfig;
import android.telecom.TelecomManager;
import android.util.ArrayMap;
import android.util.Slog;
import java.io.PrintWriter;
import java.util.Date;
import java.util.Objects;

public class ZenModeFiltering {
    private static final boolean DEBUG = ZenModeHelper.DEBUG;
    private static final String DEFAULT_PHONEAPP_PACKAGENAME = "com.android.contacts";
    private static final String RECORD_SBN_PACKAGENAME = "com.android.incallui";
    static final RepeatCallers REPEAT_CALLERS = new RepeatCallers();
    private static final String TAG = "ZenModeHelper";
    private final Context mContext;
    private ComponentName mDefaultPhoneApp;

    private static class RepeatCallers {
        private final ArrayMap<String, Long> mCalls;
        private int mThresholdMinutes;

        /* synthetic */ RepeatCallers(RepeatCallers -this0) {
            this();
        }

        private RepeatCallers() {
            this.mCalls = new ArrayMap();
        }

        /* JADX WARNING: Missing block: B:6:0x000b, code:
            return;
     */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        private synchronized void recordCall(Context context, Bundle extras) {
            setThresholdMinutes(context);
            if (this.mThresholdMinutes > 0 && extras != null) {
                String peopleString = peopleString(extras);
                if (peopleString != null) {
                    long now = System.currentTimeMillis();
                    cleanUp(this.mCalls, now);
                    this.mCalls.put(peopleString, Long.valueOf(now));
                }
            }
        }

        /* JADX WARNING: Missing block: B:7:0x000c, code:
            return false;
     */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        private synchronized boolean isRepeat(Context context, Bundle extras) {
            setThresholdMinutes(context);
            if (this.mThresholdMinutes > 0 && extras != null) {
                String peopleString = peopleString(extras);
                if (peopleString == null) {
                    return false;
                }
                cleanUp(this.mCalls, System.currentTimeMillis());
                return this.mCalls.containsKey(peopleString);
            }
        }

        private synchronized void cleanUp(ArrayMap<String, Long> calls, long now) {
            for (int i = calls.size() - 1; i >= 0; i--) {
                long time = ((Long) this.mCalls.valueAt(i)).longValue();
                if (time > now || now - time > ((long) ((this.mThresholdMinutes * 1000) * 60))) {
                    calls.removeAt(i);
                }
            }
        }

        private void setThresholdMinutes(Context context) {
            if (this.mThresholdMinutes <= 0) {
                this.mThresholdMinutes = context.getResources().getInteger(17694909);
            }
        }

        private static String peopleString(Bundle extras) {
            String str = null;
            String[] extraPeople = ValidateNotificationPeople.getExtraPeople(extras);
            if (extraPeople == null || extraPeople.length == 0) {
                return null;
            }
            StringBuilder sb = new StringBuilder();
            for (String extraPerson : extraPeople) {
                String extraPerson2;
                if (extraPerson2 != null) {
                    extraPerson2 = extraPerson2.trim();
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
                    int callNumLength = callNum.length();
                    if (callNumLength >= 4) {
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

    private static Bundle extras(NotificationRecord record) {
        if (record == null || record.sbn == null || record.sbn.getNotification() == null) {
            return null;
        }
        return record.sbn.getNotification().extras;
    }

    protected void recordCall(NotificationRecord record) {
        REPEAT_CALLERS.recordCall(this.mContext, extras(record));
    }

    public boolean shouldIntercept(int zen, ZenModeConfig config, NotificationRecord record) {
        if (isSystem(record)) {
            return false;
        }
        switch (zen) {
            case 1:
                if (isAlarm(record)) {
                    return false;
                }
                if (record.getPackagePriority() == 2) {
                    ZenLog.traceNotIntercepted(record, "priorityApp");
                    return false;
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
                } else if (!isReminder(record)) {
                    ZenLog.traceIntercepted(record, "!priority");
                    return true;
                } else if (config.allowReminders) {
                    return false;
                } else {
                    ZenLog.traceIntercepted(record, "!allowReminders");
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
    }

    private static boolean shouldInterceptAudience(int source, NotificationRecord record) {
        if (audienceMatches(source, record.getContactAffinity())) {
            return false;
        }
        ZenLog.traceIntercepted(record, "!audienceMatches");
        return true;
    }

    private static boolean isSystem(NotificationRecord record) {
        return record.isCategory("sys");
    }

    private static boolean isAlarm(NotificationRecord record) {
        if (record.isCategory("alarm") || record.isAudioStream(4)) {
            return true;
        }
        return record.isAudioAttributesUsage(4);
    }

    private static boolean isEvent(NotificationRecord record) {
        return record.isCategory("event");
    }

    private static boolean isReminder(NotificationRecord record) {
        return record.isCategory("reminder");
    }

    public boolean isCall(NotificationRecord record) {
        if (record == null) {
            return false;
        }
        if (isDefaultPhoneApp(record.sbn.getPackageName())) {
            return true;
        }
        return record.isCategory("call");
    }

    private boolean isDefaultPhoneApp(String pkg) {
        ComponentName componentName = null;
        if (this.mDefaultPhoneApp == null) {
            TelecomManager telecomm = (TelecomManager) this.mContext.getSystemService("telecom");
            if (telecomm != null) {
                componentName = telecomm.getDefaultPhoneApp();
            }
            this.mDefaultPhoneApp = componentName;
            if (DEBUG) {
                Slog.d(TAG, "Default phone app: " + this.mDefaultPhoneApp);
            }
        }
        if (pkg == null || this.mDefaultPhoneApp == null || !RECORD_SBN_PACKAGENAME.equals(pkg)) {
            return false;
        }
        return DEFAULT_PHONEAPP_PACKAGENAME.equals(this.mDefaultPhoneApp.getPackageName());
    }

    private boolean isDefaultMessagingApp(NotificationRecord record) {
        int userId = record.getUserId();
        if (userId == -10000 || userId == -1) {
            return false;
        }
        return Objects.equals(Secure.getStringForUser(this.mContext.getContentResolver(), "sms_default_application", userId), record.sbn.getPackageName());
    }

    private boolean isMessage(NotificationRecord record) {
        return !record.isCategory("msg") ? isDefaultMessagingApp(record) : true;
    }

    private static boolean audienceMatches(int source, float contactAffinity) {
        boolean z = true;
        switch (source) {
            case 0:
                return true;
            case 1:
                if (contactAffinity < 0.5f) {
                    z = false;
                }
                return z;
            case 2:
                if (contactAffinity < 1.0f) {
                    z = false;
                }
                return z;
            default:
                Slog.w(TAG, "Encountered unknown source: " + source);
                return true;
        }
    }
}
