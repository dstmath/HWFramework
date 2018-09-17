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
import com.android.server.am.ProcessList;
import com.android.server.wm.WindowManagerService.H;
import com.android.server.wm.WindowState;
import java.io.PrintWriter;
import java.util.Date;
import java.util.Objects;

public class ZenModeFiltering {
    private static final boolean DEBUG = false;
    static final RepeatCallers REPEAT_CALLERS = null;
    private static final String TAG = "ZenModeHelper";
    private final Context mContext;
    private ComponentName mDefaultPhoneApp;

    private static class RepeatCallers {
        private final ArrayMap<String, Long> mCalls;
        private int mThresholdMinutes;

        private RepeatCallers() {
            this.mCalls = new ArrayMap();
        }

        private synchronized boolean isRepeat(Context context, Bundle extras) {
            if (this.mThresholdMinutes <= 0) {
                this.mThresholdMinutes = context.getResources().getInteger(17694873);
            }
            if (this.mThresholdMinutes <= 0 || extras == null) {
                return ZenModeFiltering.DEBUG;
            }
            String peopleString = peopleString(extras);
            if (peopleString == null) {
                return ZenModeFiltering.DEBUG;
            }
            long now = System.currentTimeMillis();
            for (int i = this.mCalls.size() - 1; i >= 0; i--) {
                long time = ((Long) this.mCalls.valueAt(i)).longValue();
                if (time > now || now - time > ((long) ((this.mThresholdMinutes * ProcessList.PSS_SAFE_TIME_FROM_STATE_CHANGE) * 60))) {
                    this.mCalls.removeAt(i);
                }
            }
            boolean isRepeat = this.mCalls.containsKey(peopleString);
            this.mCalls.put(peopleString, Long.valueOf(now));
            return isRepeat;
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

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.server.notification.ZenModeFiltering.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.server.notification.ZenModeFiltering.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 5 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 6 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.server.notification.ZenModeFiltering.<clinit>():void");
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
                    pw.print((String) REPEAT_CALLERS.mCalls.keyAt(i));
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
            return DEBUG;
        }
        if (zen != 1 || (config.allowRepeatCallers && REPEAT_CALLERS.isRepeat(context, extras))) {
            return true;
        }
        if (!config.allowCalls) {
            return DEBUG;
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

    public boolean shouldIntercept(int zen, ZenModeConfig config, NotificationRecord record) {
        if (isSystem(record)) {
            return DEBUG;
        }
        switch (zen) {
            case WindowState.LOW_RESOLUTION_COMPOSITION_OFF /*1*/:
                if (isAlarm(record)) {
                    return DEBUG;
                }
                if (record.getPackagePriority() == 2) {
                    ZenLog.traceNotIntercepted(record, "priorityApp");
                    return DEBUG;
                } else if (isCall(record)) {
                    if (config.allowRepeatCallers && REPEAT_CALLERS.isRepeat(this.mContext, extras(record))) {
                        ZenLog.traceNotIntercepted(record, "repeatCaller");
                        return DEBUG;
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
                        return DEBUG;
                    }
                    ZenLog.traceIntercepted(record, "!allowEvents");
                    return true;
                } else if (!isReminder(record)) {
                    ZenLog.traceIntercepted(record, "!priority");
                    return true;
                } else if (config.allowReminders) {
                    return DEBUG;
                } else {
                    ZenLog.traceIntercepted(record, "!allowReminders");
                    return true;
                }
            case WindowState.LOW_RESOLUTION_COMPOSITION_ON /*2*/:
                ZenLog.traceIntercepted(record, "none");
                return true;
            case H.REPORT_LOSING_FOCUS /*3*/:
                if (isAlarm(record)) {
                    return DEBUG;
                }
                ZenLog.traceIntercepted(record, "alarmsOnly");
                return true;
            default:
                return DEBUG;
        }
    }

    private static boolean shouldInterceptAudience(int source, NotificationRecord record) {
        if (audienceMatches(source, record.getContactAffinity())) {
            return DEBUG;
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
            return DEBUG;
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
        if (pkg == null || this.mDefaultPhoneApp == null) {
            return DEBUG;
        }
        return pkg.equals(this.mDefaultPhoneApp.getPackageName());
    }

    private boolean isDefaultMessagingApp(NotificationRecord record) {
        int userId = record.getUserId();
        if (userId == -10000 || userId == -1) {
            return DEBUG;
        }
        return Objects.equals(Secure.getStringForUser(this.mContext.getContentResolver(), "sms_default_application", userId), record.sbn.getPackageName());
    }

    private boolean isMessage(NotificationRecord record) {
        return !record.isCategory("msg") ? isDefaultMessagingApp(record) : true;
    }

    private static boolean audienceMatches(int source, float contactAffinity) {
        boolean z = true;
        switch (source) {
            case WindowState.LOW_RESOLUTION_FEATURE_OFF /*0*/:
                return true;
            case WindowState.LOW_RESOLUTION_COMPOSITION_OFF /*1*/:
                if (contactAffinity < TaskPositioner.RESIZING_HINT_ALPHA) {
                    z = DEBUG;
                }
                return z;
            case WindowState.LOW_RESOLUTION_COMPOSITION_ON /*2*/:
                if (contactAffinity < 1.0f) {
                    z = DEBUG;
                }
                return z;
            default:
                Slog.w(TAG, "Encountered unknown source: " + source);
                return true;
        }
    }
}
