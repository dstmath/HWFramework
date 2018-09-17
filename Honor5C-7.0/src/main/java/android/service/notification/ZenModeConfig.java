package android.service.notification;

import android.app.ActivityManager;
import android.app.NotificationManager.Policy;
import android.content.ComponentName;
import android.content.Context;
import android.content.res.Resources;
import android.hardware.SensorManager;
import android.net.ProxyInfo;
import android.net.Uri;
import android.net.Uri.Builder;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import android.os.UserHandle;
import android.provider.CalendarContract.Instances;
import android.provider.Contacts;
import android.provider.Settings.Global;
import android.text.TextUtils;
import android.text.format.DateFormat;
import android.util.ArrayMap;
import android.util.ArraySet;
import android.util.Slog;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.GregorianCalendar;
import java.util.Locale;
import java.util.Objects;
import java.util.UUID;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlSerializer;

public class ZenModeConfig implements Parcelable {
    private static final String ALLOW_ATT_CALLS = "calls";
    private static final String ALLOW_ATT_CALLS_FROM = "callsFrom";
    private static final String ALLOW_ATT_EVENTS = "events";
    private static final String ALLOW_ATT_FROM = "from";
    private static final String ALLOW_ATT_MESSAGES = "messages";
    private static final String ALLOW_ATT_MESSAGES_FROM = "messagesFrom";
    private static final String ALLOW_ATT_REMINDERS = "reminders";
    private static final String ALLOW_ATT_REPEAT_CALLERS = "repeatCallers";
    private static final String ALLOW_ATT_SCREEN_OFF = "visualScreenOff";
    private static final String ALLOW_ATT_SCREEN_ON = "visualScreenOn";
    private static final String ALLOW_TAG = "allow";
    public static final int[] ALL_DAYS = null;
    private static final String AUTOMATIC_TAG = "automatic";
    private static final String CONDITION_ATT_COMPONENT = "component";
    private static final String CONDITION_ATT_FLAGS = "flags";
    private static final String CONDITION_ATT_ICON = "icon";
    private static final String CONDITION_ATT_ID = "id";
    private static final String CONDITION_ATT_LINE1 = "line1";
    private static final String CONDITION_ATT_LINE2 = "line2";
    private static final String CONDITION_ATT_STATE = "state";
    private static final String CONDITION_ATT_SUMMARY = "summary";
    private static final String CONDITION_TAG = "condition";
    public static final String COUNTDOWN_PATH = "countdown";
    public static final Creator<ZenModeConfig> CREATOR = null;
    private static final int DAY_MINUTES = 1440;
    private static final boolean DEFAULT_ALLOW_CALLS = true;
    private static final boolean DEFAULT_ALLOW_EVENTS = true;
    private static final boolean DEFAULT_ALLOW_MESSAGES = false;
    private static final boolean DEFAULT_ALLOW_REMINDERS = true;
    private static final boolean DEFAULT_ALLOW_REPEAT_CALLERS = false;
    private static final boolean DEFAULT_ALLOW_SCREEN_OFF = true;
    private static final boolean DEFAULT_ALLOW_SCREEN_ON = true;
    private static final int DEFAULT_SOURCE = 1;
    public static final String EVENT_PATH = "event";
    private static final String MANUAL_TAG = "manual";
    public static final int MAX_SOURCE = 2;
    private static final int MINUTES_MS = 60000;
    public static final int[] MINUTE_BUCKETS = null;
    private static final String RULE_ATT_COMPONENT = "component";
    private static final String RULE_ATT_CONDITION_ID = "conditionId";
    private static final String RULE_ATT_CREATION_TIME = "creationTime";
    private static final String RULE_ATT_ENABLED = "enabled";
    private static final String RULE_ATT_ID = "ruleId";
    private static final String RULE_ATT_NAME = "name";
    private static final String RULE_ATT_SNOOZING = "snoozing";
    private static final String RULE_ATT_ZEN = "zen";
    public static final String SCHEDULE_PATH = "schedule";
    private static final int SECONDS_MS = 1000;
    public static final int SOURCE_ANYONE = 0;
    public static final int SOURCE_CONTACT = 1;
    public static final int SOURCE_STAR = 2;
    public static final String SYSTEM_AUTHORITY = "android";
    private static String TAG = null;
    public static final int[] WEEKEND_DAYS = null;
    public static final int[] WEEKNIGHT_DAYS = null;
    private static final int XML_VERSION = 2;
    private static final String ZEN_ATT_USER = "user";
    private static final String ZEN_ATT_VERSION = "version";
    private static final String ZEN_TAG = "zen";
    private static final int ZERO_VALUE_MS = 10000;
    public boolean allowCalls;
    public int allowCallsFrom;
    public boolean allowEvents;
    public boolean allowMessages;
    public int allowMessagesFrom;
    public boolean allowReminders;
    public boolean allowRepeatCallers;
    public boolean allowWhenScreenOff;
    public boolean allowWhenScreenOn;
    public ArrayMap<String, ZenRule> automaticRules;
    public ZenRule manualRule;
    public int user;

    public static class Diff {
        private final ArrayList<String> lines;

        public Diff() {
            this.lines = new ArrayList();
        }

        public String toString() {
            StringBuilder sb = new StringBuilder("Diff[");
            int N = this.lines.size();
            for (int i = ZenModeConfig.SOURCE_ANYONE; i < N; i += ZenModeConfig.SOURCE_CONTACT) {
                if (i > 0) {
                    sb.append(',');
                }
                sb.append((String) this.lines.get(i));
            }
            return sb.append(']').toString();
        }

        private Diff addLine(String item, String action) {
            this.lines.add(item + ":" + action);
            return this;
        }

        public Diff addLine(String item, String subitem, Object from, Object to) {
            return addLine(item + "." + subitem, from, to);
        }

        public Diff addLine(String item, Object from, Object to) {
            return addLine(item, from + "->" + to);
        }
    }

    public static class EventInfo {
        public static final int REPLY_ANY_EXCEPT_NO = 0;
        public static final int REPLY_YES = 2;
        public static final int REPLY_YES_OR_MAYBE = 1;
        public String calendar;
        public int reply;
        public int userId;

        public EventInfo() {
            this.userId = UserHandle.USER_NULL;
        }

        public int hashCode() {
            return REPLY_ANY_EXCEPT_NO;
        }

        public boolean equals(Object o) {
            boolean z = ZenModeConfig.DEFAULT_ALLOW_REPEAT_CALLERS;
            if (!(o instanceof EventInfo)) {
                return ZenModeConfig.DEFAULT_ALLOW_REPEAT_CALLERS;
            }
            EventInfo other = (EventInfo) o;
            if (this.userId == other.userId && Objects.equals(this.calendar, other.calendar) && this.reply == other.reply) {
                z = ZenModeConfig.DEFAULT_ALLOW_SCREEN_ON;
            }
            return z;
        }

        public EventInfo copy() {
            EventInfo rt = new EventInfo();
            rt.userId = this.userId;
            rt.calendar = this.calendar;
            rt.reply = this.reply;
            return rt;
        }

        public static int resolveUserId(int userId) {
            return userId == UserHandle.USER_NULL ? ActivityManager.getCurrentUser() : userId;
        }
    }

    public interface Migration {
        ZenModeConfig migrate(XmlV1 xmlV1);
    }

    public static class ScheduleInfo {
        public int[] days;
        public int endHour;
        public int endMinute;
        public boolean exitAtAlarm;
        public long nextAlarm;
        public int startHour;
        public int startMinute;

        public int hashCode() {
            return ZenModeConfig.SOURCE_ANYONE;
        }

        public boolean equals(Object o) {
            boolean z = ZenModeConfig.DEFAULT_ALLOW_REPEAT_CALLERS;
            if (!(o instanceof ScheduleInfo)) {
                return ZenModeConfig.DEFAULT_ALLOW_REPEAT_CALLERS;
            }
            ScheduleInfo other = (ScheduleInfo) o;
            if (ZenModeConfig.toDayList(this.days).equals(ZenModeConfig.toDayList(other.days)) && this.startHour == other.startHour && this.startMinute == other.startMinute && this.endHour == other.endHour && this.endMinute == other.endMinute && this.exitAtAlarm == other.exitAtAlarm) {
                z = ZenModeConfig.DEFAULT_ALLOW_SCREEN_ON;
            }
            return z;
        }

        public ScheduleInfo copy() {
            ScheduleInfo rt = new ScheduleInfo();
            if (this.days != null) {
                rt.days = new int[this.days.length];
                System.arraycopy(this.days, ZenModeConfig.SOURCE_ANYONE, rt.days, ZenModeConfig.SOURCE_ANYONE, this.days.length);
            }
            rt.startHour = this.startHour;
            rt.startMinute = this.startMinute;
            rt.endHour = this.endHour;
            rt.endMinute = this.endMinute;
            rt.exitAtAlarm = this.exitAtAlarm;
            rt.nextAlarm = this.nextAlarm;
            return rt;
        }

        public String toString() {
            return "ScheduleInfo{days=" + Arrays.toString(this.days) + ", startHour=" + this.startHour + ", startMinute=" + this.startMinute + ", endHour=" + this.endHour + ", endMinute=" + this.endMinute + ", exitAtAlarm=" + this.exitAtAlarm + ", nextAlarm=" + this.nextAlarm + '}';
        }
    }

    public static final class XmlV1 {
        private static final String EXIT_CONDITION_ATT_COMPONENT = "component";
        private static final String EXIT_CONDITION_TAG = "exitCondition";
        private static final String SLEEP_ATT_END_HR = "endHour";
        private static final String SLEEP_ATT_END_MIN = "endMin";
        private static final String SLEEP_ATT_MODE = "mode";
        private static final String SLEEP_ATT_NONE = "none";
        private static final String SLEEP_ATT_START_HR = "startHour";
        private static final String SLEEP_ATT_START_MIN = "startMin";
        public static final String SLEEP_MODE_DAYS_PREFIX = "days:";
        public static final String SLEEP_MODE_NIGHTS = "nights";
        public static final String SLEEP_MODE_WEEKNIGHTS = "weeknights";
        private static final String SLEEP_TAG = "sleep";
        public boolean allowCalls;
        public boolean allowEvents;
        public int allowFrom;
        public boolean allowMessages;
        public boolean allowReminders;
        public ComponentName[] conditionComponents;
        public Uri[] conditionIds;
        public Condition exitCondition;
        public ComponentName exitConditionComponent;
        public int sleepEndHour;
        public int sleepEndMinute;
        public String sleepMode;
        public boolean sleepNone;
        public int sleepStartHour;
        public int sleepStartMinute;

        public XmlV1() {
            this.allowReminders = ZenModeConfig.DEFAULT_ALLOW_SCREEN_ON;
            this.allowEvents = ZenModeConfig.DEFAULT_ALLOW_SCREEN_ON;
            this.allowFrom = ZenModeConfig.SOURCE_ANYONE;
        }

        private static boolean isValidSleepMode(String sleepMode) {
            if (sleepMode == null || sleepMode.equals(SLEEP_MODE_NIGHTS) || sleepMode.equals(SLEEP_MODE_WEEKNIGHTS) || tryParseDays(sleepMode) != null) {
                return ZenModeConfig.DEFAULT_ALLOW_SCREEN_ON;
            }
            return ZenModeConfig.DEFAULT_ALLOW_REPEAT_CALLERS;
        }

        public static int[] tryParseDays(String sleepMode) {
            if (sleepMode == null) {
                return null;
            }
            sleepMode = sleepMode.trim();
            if (SLEEP_MODE_NIGHTS.equals(sleepMode)) {
                return ZenModeConfig.ALL_DAYS;
            }
            if (SLEEP_MODE_WEEKNIGHTS.equals(sleepMode)) {
                return ZenModeConfig.WEEKNIGHT_DAYS;
            }
            if (sleepMode.startsWith(SLEEP_MODE_DAYS_PREFIX) && !sleepMode.equals(SLEEP_MODE_DAYS_PREFIX)) {
                return ZenModeConfig.tryParseDayList(sleepMode.substring(SLEEP_MODE_DAYS_PREFIX.length()), ",");
            }
            return null;
        }

        /* JADX WARNING: inconsistent code. */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        public static XmlV1 readXml(XmlPullParser parser) throws XmlPullParserException, IOException {
            XmlV1 rt = new XmlV1();
            ArrayList<ComponentName> conditionComponents = new ArrayList();
            ArrayList<Uri> conditionIds = new ArrayList();
            while (true) {
                int type = parser.next();
                if (type == ZenModeConfig.SOURCE_CONTACT) {
                    break;
                }
                String tag = parser.getName();
                if (type == 3 && ZenModeConfig.ZEN_TAG.equals(tag)) {
                    break;
                } else if (type == ZenModeConfig.XML_VERSION) {
                    if (ZenModeConfig.ALLOW_TAG.equals(tag)) {
                        rt.allowCalls = ZenModeConfig.safeBoolean(parser, ZenModeConfig.ALLOW_ATT_CALLS, ZenModeConfig.DEFAULT_ALLOW_REPEAT_CALLERS);
                        rt.allowMessages = ZenModeConfig.safeBoolean(parser, ZenModeConfig.ALLOW_ATT_MESSAGES, ZenModeConfig.DEFAULT_ALLOW_REPEAT_CALLERS);
                        rt.allowReminders = ZenModeConfig.safeBoolean(parser, ZenModeConfig.ALLOW_ATT_REMINDERS, ZenModeConfig.DEFAULT_ALLOW_SCREEN_ON);
                        rt.allowEvents = ZenModeConfig.safeBoolean(parser, ZenModeConfig.ALLOW_ATT_EVENTS, ZenModeConfig.DEFAULT_ALLOW_SCREEN_ON);
                        rt.allowFrom = ZenModeConfig.safeInt(parser, ZenModeConfig.ALLOW_ATT_FROM, ZenModeConfig.SOURCE_ANYONE);
                        if (rt.allowFrom < 0 || rt.allowFrom > ZenModeConfig.XML_VERSION) {
                        }
                    } else if (SLEEP_TAG.equals(tag)) {
                        String mode = parser.getAttributeValue(null, SLEEP_ATT_MODE);
                        if (!isValidSleepMode(mode)) {
                            mode = null;
                        }
                        rt.sleepMode = mode;
                        rt.sleepNone = ZenModeConfig.safeBoolean(parser, SLEEP_ATT_NONE, ZenModeConfig.DEFAULT_ALLOW_REPEAT_CALLERS);
                        int startHour = ZenModeConfig.safeInt(parser, SLEEP_ATT_START_HR, ZenModeConfig.SOURCE_ANYONE);
                        int startMinute = ZenModeConfig.safeInt(parser, SLEEP_ATT_START_MIN, ZenModeConfig.SOURCE_ANYONE);
                        int endHour = ZenModeConfig.safeInt(parser, SLEEP_ATT_END_HR, ZenModeConfig.SOURCE_ANYONE);
                        int endMinute = ZenModeConfig.safeInt(parser, SLEEP_ATT_END_MIN, ZenModeConfig.SOURCE_ANYONE);
                        if (!ZenModeConfig.isValidHour(startHour)) {
                            startHour = ZenModeConfig.SOURCE_ANYONE;
                        }
                        rt.sleepStartHour = startHour;
                        if (!ZenModeConfig.isValidMinute(startMinute)) {
                            startMinute = ZenModeConfig.SOURCE_ANYONE;
                        }
                        rt.sleepStartMinute = startMinute;
                        if (!ZenModeConfig.isValidHour(endHour)) {
                            endHour = ZenModeConfig.SOURCE_ANYONE;
                        }
                        rt.sleepEndHour = endHour;
                        if (!ZenModeConfig.isValidMinute(endMinute)) {
                            endMinute = ZenModeConfig.SOURCE_ANYONE;
                        }
                        rt.sleepEndMinute = endMinute;
                    } else if (ZenModeConfig.CONDITION_TAG.equals(tag)) {
                        ComponentName component = ZenModeConfig.safeComponentName(parser, EXIT_CONDITION_ATT_COMPONENT);
                        Uri conditionId = ZenModeConfig.safeUri(parser, ZenModeConfig.CONDITION_ATT_ID);
                        if (!(component == null || conditionId == null)) {
                            conditionComponents.add(component);
                            conditionIds.add(conditionId);
                        }
                    } else if (EXIT_CONDITION_TAG.equals(tag)) {
                        rt.exitCondition = ZenModeConfig.readConditionXml(parser);
                        if (rt.exitCondition != null) {
                            rt.exitConditionComponent = ZenModeConfig.safeComponentName(parser, EXIT_CONDITION_ATT_COMPONENT);
                        }
                    }
                }
            }
            if (!conditionComponents.isEmpty()) {
                rt.conditionComponents = (ComponentName[]) conditionComponents.toArray(new ComponentName[conditionComponents.size()]);
                rt.conditionIds = (Uri[]) conditionIds.toArray(new Uri[conditionIds.size()]);
            }
            return rt;
        }
    }

    public static class ZenRule implements Parcelable {
        public static final Creator<ZenRule> CREATOR = null;
        public ComponentName component;
        public Condition condition;
        public Uri conditionId;
        public long creationTime;
        public boolean enabled;
        public String id;
        public String name;
        public boolean snoozing;
        public int zenMode;

        static {
            /* JADX: method processing error */
/*
            Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.service.notification.ZenModeConfig.ZenRule.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:263)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.service.notification.ZenModeConfig.ZenRule.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 8 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 9 more
*/
            /*
            // Can't load method instructions.
            */
            throw new UnsupportedOperationException("Method not decompiled: android.service.notification.ZenModeConfig.ZenRule.<clinit>():void");
        }

        public ZenRule(Parcel source) {
            boolean z;
            boolean z2 = ZenModeConfig.DEFAULT_ALLOW_REPEAT_CALLERS;
            if (source.readInt() == ZenModeConfig.SOURCE_CONTACT) {
                z = ZenModeConfig.DEFAULT_ALLOW_SCREEN_ON;
            } else {
                z = ZenModeConfig.DEFAULT_ALLOW_REPEAT_CALLERS;
            }
            this.enabled = z;
            if (source.readInt() == ZenModeConfig.SOURCE_CONTACT) {
                z2 = ZenModeConfig.DEFAULT_ALLOW_SCREEN_ON;
            }
            this.snoozing = z2;
            if (source.readInt() == ZenModeConfig.SOURCE_CONTACT) {
                this.name = source.readString();
            }
            this.zenMode = source.readInt();
            this.conditionId = (Uri) source.readParcelable(null);
            this.condition = (Condition) source.readParcelable(null);
            this.component = (ComponentName) source.readParcelable(null);
            if (source.readInt() == ZenModeConfig.SOURCE_CONTACT) {
                this.id = source.readString();
            }
            this.creationTime = source.readLong();
        }

        public int describeContents() {
            return ZenModeConfig.SOURCE_ANYONE;
        }

        public void writeToParcel(Parcel dest, int flags) {
            int i;
            if (this.enabled) {
                i = ZenModeConfig.SOURCE_CONTACT;
            } else {
                i = ZenModeConfig.SOURCE_ANYONE;
            }
            dest.writeInt(i);
            if (this.snoozing) {
                i = ZenModeConfig.SOURCE_CONTACT;
            } else {
                i = ZenModeConfig.SOURCE_ANYONE;
            }
            dest.writeInt(i);
            if (this.name != null) {
                dest.writeInt(ZenModeConfig.SOURCE_CONTACT);
                dest.writeString(this.name);
            } else {
                dest.writeInt(ZenModeConfig.SOURCE_ANYONE);
            }
            dest.writeInt(this.zenMode);
            dest.writeParcelable(this.conditionId, ZenModeConfig.SOURCE_ANYONE);
            dest.writeParcelable(this.condition, ZenModeConfig.SOURCE_ANYONE);
            dest.writeParcelable(this.component, ZenModeConfig.SOURCE_ANYONE);
            if (this.id != null) {
                dest.writeInt(ZenModeConfig.SOURCE_CONTACT);
                dest.writeString(this.id);
            } else {
                dest.writeInt(ZenModeConfig.SOURCE_ANYONE);
            }
            dest.writeLong(this.creationTime);
        }

        public String toString() {
            return new StringBuilder(ZenRule.class.getSimpleName()).append('[').append("enabled=").append(this.enabled).append(",snoozing=").append(this.snoozing).append(",name=").append(this.name).append(",zenMode=").append(Global.zenModeToString(this.zenMode)).append(",conditionId=").append(this.conditionId).append(",condition=").append(this.condition).append(",component=").append(this.component).append(",id=").append(this.id).append(",creationTime=").append(this.creationTime).append(']').toString();
        }

        private static void appendDiff(Diff d, String item, ZenRule from, ZenRule to) {
            if (d != null) {
                if (from == null) {
                    if (to != null) {
                        d.addLine(item, "insert");
                    }
                    return;
                }
                from.appendDiff(d, item, to);
            }
        }

        private void appendDiff(Diff d, String item, ZenRule to) {
            if (to == null) {
                d.addLine(item, "delete");
                return;
            }
            if (this.enabled != to.enabled) {
                d.addLine(item, ZenModeConfig.RULE_ATT_ENABLED, Boolean.valueOf(this.enabled), Boolean.valueOf(to.enabled));
            }
            if (this.snoozing != to.snoozing) {
                d.addLine(item, ZenModeConfig.RULE_ATT_SNOOZING, Boolean.valueOf(this.snoozing), Boolean.valueOf(to.snoozing));
            }
            if (!Objects.equals(this.name, to.name)) {
                d.addLine(item, ZenModeConfig.RULE_ATT_NAME, this.name, to.name);
            }
            if (this.zenMode != to.zenMode) {
                d.addLine(item, "zenMode", Integer.valueOf(this.zenMode), Integer.valueOf(to.zenMode));
            }
            if (!Objects.equals(this.conditionId, to.conditionId)) {
                d.addLine(item, ZenModeConfig.RULE_ATT_CONDITION_ID, this.conditionId, to.conditionId);
            }
            if (!Objects.equals(this.condition, to.condition)) {
                d.addLine(item, ZenModeConfig.CONDITION_TAG, this.condition, to.condition);
            }
            if (!Objects.equals(this.component, to.component)) {
                d.addLine(item, ZenModeConfig.RULE_ATT_COMPONENT, this.component, to.component);
            }
            if (!Objects.equals(this.id, to.id)) {
                d.addLine(item, ZenModeConfig.CONDITION_ATT_ID, this.id, to.id);
            }
            if (this.creationTime != to.creationTime) {
                d.addLine(item, ZenModeConfig.RULE_ATT_CREATION_TIME, Long.valueOf(this.creationTime), Long.valueOf(to.creationTime));
            }
        }

        public boolean equals(Object o) {
            boolean z = ZenModeConfig.DEFAULT_ALLOW_SCREEN_ON;
            if (!(o instanceof ZenRule)) {
                return ZenModeConfig.DEFAULT_ALLOW_REPEAT_CALLERS;
            }
            if (o == this) {
                return ZenModeConfig.DEFAULT_ALLOW_SCREEN_ON;
            }
            ZenRule other = (ZenRule) o;
            if (other.enabled != this.enabled || other.snoozing != this.snoozing || !Objects.equals(other.name, this.name) || other.zenMode != this.zenMode || !Objects.equals(other.conditionId, this.conditionId) || !Objects.equals(other.condition, this.condition) || !Objects.equals(other.component, this.component) || !Objects.equals(other.id, this.id)) {
                z = ZenModeConfig.DEFAULT_ALLOW_REPEAT_CALLERS;
            } else if (other.creationTime != this.creationTime) {
                z = ZenModeConfig.DEFAULT_ALLOW_REPEAT_CALLERS;
            }
            return z;
        }

        public int hashCode() {
            return Objects.hash(new Object[]{Boolean.valueOf(this.enabled), Boolean.valueOf(this.snoozing), this.name, Integer.valueOf(this.zenMode), this.conditionId, this.condition, this.component, this.id, Long.valueOf(this.creationTime)});
        }

        public boolean isAutomaticActive() {
            return (!this.enabled || this.snoozing || this.component == null) ? ZenModeConfig.DEFAULT_ALLOW_REPEAT_CALLERS : isTrueOrUnknown();
        }

        public boolean isTrueOrUnknown() {
            if (this.condition != null) {
                return (this.condition.state == ZenModeConfig.SOURCE_CONTACT || this.condition.state == ZenModeConfig.XML_VERSION) ? ZenModeConfig.DEFAULT_ALLOW_SCREEN_ON : ZenModeConfig.DEFAULT_ALLOW_REPEAT_CALLERS;
            } else {
                return ZenModeConfig.DEFAULT_ALLOW_REPEAT_CALLERS;
            }
        }
    }

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.service.notification.ZenModeConfig.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.service.notification.ZenModeConfig.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 7 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 8 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: android.service.notification.ZenModeConfig.<clinit>():void");
    }

    public ZenModeConfig() {
        this.allowCalls = DEFAULT_ALLOW_SCREEN_ON;
        this.allowRepeatCallers = DEFAULT_ALLOW_REPEAT_CALLERS;
        this.allowMessages = DEFAULT_ALLOW_REPEAT_CALLERS;
        this.allowReminders = DEFAULT_ALLOW_SCREEN_ON;
        this.allowEvents = DEFAULT_ALLOW_SCREEN_ON;
        this.allowCallsFrom = SOURCE_CONTACT;
        this.allowMessagesFrom = SOURCE_CONTACT;
        this.user = SOURCE_ANYONE;
        this.allowWhenScreenOff = DEFAULT_ALLOW_SCREEN_ON;
        this.allowWhenScreenOn = DEFAULT_ALLOW_SCREEN_ON;
        this.automaticRules = new ArrayMap();
    }

    public ZenModeConfig(Parcel source) {
        boolean z;
        boolean z2 = DEFAULT_ALLOW_SCREEN_ON;
        this.allowCalls = DEFAULT_ALLOW_SCREEN_ON;
        this.allowRepeatCallers = DEFAULT_ALLOW_REPEAT_CALLERS;
        this.allowMessages = DEFAULT_ALLOW_REPEAT_CALLERS;
        this.allowReminders = DEFAULT_ALLOW_SCREEN_ON;
        this.allowEvents = DEFAULT_ALLOW_SCREEN_ON;
        this.allowCallsFrom = SOURCE_CONTACT;
        this.allowMessagesFrom = SOURCE_CONTACT;
        this.user = SOURCE_ANYONE;
        this.allowWhenScreenOff = DEFAULT_ALLOW_SCREEN_ON;
        this.allowWhenScreenOn = DEFAULT_ALLOW_SCREEN_ON;
        this.automaticRules = new ArrayMap();
        if (source.readInt() == SOURCE_CONTACT) {
            z = DEFAULT_ALLOW_SCREEN_ON;
        } else {
            z = DEFAULT_ALLOW_REPEAT_CALLERS;
        }
        this.allowCalls = z;
        if (source.readInt() == SOURCE_CONTACT) {
            z = DEFAULT_ALLOW_SCREEN_ON;
        } else {
            z = DEFAULT_ALLOW_REPEAT_CALLERS;
        }
        this.allowRepeatCallers = z;
        if (source.readInt() == SOURCE_CONTACT) {
            z = DEFAULT_ALLOW_SCREEN_ON;
        } else {
            z = DEFAULT_ALLOW_REPEAT_CALLERS;
        }
        this.allowMessages = z;
        if (source.readInt() == SOURCE_CONTACT) {
            z = DEFAULT_ALLOW_SCREEN_ON;
        } else {
            z = DEFAULT_ALLOW_REPEAT_CALLERS;
        }
        this.allowReminders = z;
        if (source.readInt() == SOURCE_CONTACT) {
            z = DEFAULT_ALLOW_SCREEN_ON;
        } else {
            z = DEFAULT_ALLOW_REPEAT_CALLERS;
        }
        this.allowEvents = z;
        this.allowCallsFrom = source.readInt();
        this.allowMessagesFrom = source.readInt();
        this.user = source.readInt();
        this.manualRule = (ZenRule) source.readParcelable(null);
        int len = source.readInt();
        if (len > 0) {
            String[] ids = new String[len];
            ZenRule[] rules = new ZenRule[len];
            source.readStringArray(ids);
            source.readTypedArray(rules, ZenRule.CREATOR);
            for (int i = SOURCE_ANYONE; i < len; i += SOURCE_CONTACT) {
                this.automaticRules.put(ids[i], rules[i]);
            }
        }
        this.allowWhenScreenOff = source.readInt() == SOURCE_CONTACT ? DEFAULT_ALLOW_SCREEN_ON : DEFAULT_ALLOW_REPEAT_CALLERS;
        if (source.readInt() != SOURCE_CONTACT) {
            z2 = DEFAULT_ALLOW_REPEAT_CALLERS;
        }
        this.allowWhenScreenOn = z2;
    }

    public void writeToParcel(Parcel dest, int flags) {
        int i;
        int i2 = SOURCE_CONTACT;
        if (this.allowCalls) {
            i = SOURCE_CONTACT;
        } else {
            i = SOURCE_ANYONE;
        }
        dest.writeInt(i);
        if (this.allowRepeatCallers) {
            i = SOURCE_CONTACT;
        } else {
            i = SOURCE_ANYONE;
        }
        dest.writeInt(i);
        if (this.allowMessages) {
            i = SOURCE_CONTACT;
        } else {
            i = SOURCE_ANYONE;
        }
        dest.writeInt(i);
        if (this.allowReminders) {
            i = SOURCE_CONTACT;
        } else {
            i = SOURCE_ANYONE;
        }
        dest.writeInt(i);
        if (this.allowEvents) {
            i = SOURCE_CONTACT;
        } else {
            i = SOURCE_ANYONE;
        }
        dest.writeInt(i);
        dest.writeInt(this.allowCallsFrom);
        dest.writeInt(this.allowMessagesFrom);
        dest.writeInt(this.user);
        dest.writeParcelable(this.manualRule, SOURCE_ANYONE);
        if (this.automaticRules.isEmpty()) {
            dest.writeInt(SOURCE_ANYONE);
        } else {
            int len = this.automaticRules.size();
            String[] ids = new String[len];
            ZenRule[] rules = new ZenRule[len];
            for (int i3 = SOURCE_ANYONE; i3 < len; i3 += SOURCE_CONTACT) {
                ids[i3] = (String) this.automaticRules.keyAt(i3);
                rules[i3] = (ZenRule) this.automaticRules.valueAt(i3);
            }
            dest.writeInt(len);
            dest.writeStringArray(ids);
            dest.writeTypedArray(rules, SOURCE_ANYONE);
        }
        if (this.allowWhenScreenOff) {
            i = SOURCE_CONTACT;
        } else {
            i = SOURCE_ANYONE;
        }
        dest.writeInt(i);
        if (!this.allowWhenScreenOn) {
            i2 = SOURCE_ANYONE;
        }
        dest.writeInt(i2);
    }

    public String toString() {
        return new StringBuilder(ZenModeConfig.class.getSimpleName()).append('[').append("user=").append(this.user).append(",allowCalls=").append(this.allowCalls).append(",allowRepeatCallers=").append(this.allowRepeatCallers).append(",allowMessages=").append(this.allowMessages).append(",allowCallsFrom=").append(sourceToString(this.allowCallsFrom)).append(",allowMessagesFrom=").append(sourceToString(this.allowMessagesFrom)).append(",allowReminders=").append(this.allowReminders).append(",allowEvents=").append(this.allowEvents).append(",allowWhenScreenOff=").append(this.allowWhenScreenOff).append(",allowWhenScreenOn=").append(this.allowWhenScreenOn).append(",automaticRules=").append(this.automaticRules).append(",manualRule=").append(this.manualRule).append(']').toString();
    }

    private Diff diff(ZenModeConfig to) {
        Diff d = new Diff();
        if (to == null) {
            return d.addLine("config", "delete");
        }
        if (this.user != to.user) {
            d.addLine(ZEN_ATT_USER, Integer.valueOf(this.user), Integer.valueOf(to.user));
        }
        if (this.allowCalls != to.allowCalls) {
            d.addLine("allowCalls", Boolean.valueOf(this.allowCalls), Boolean.valueOf(to.allowCalls));
        }
        if (this.allowRepeatCallers != to.allowRepeatCallers) {
            d.addLine("allowRepeatCallers", Boolean.valueOf(this.allowRepeatCallers), Boolean.valueOf(to.allowRepeatCallers));
        }
        if (this.allowMessages != to.allowMessages) {
            d.addLine("allowMessages", Boolean.valueOf(this.allowMessages), Boolean.valueOf(to.allowMessages));
        }
        if (this.allowCallsFrom != to.allowCallsFrom) {
            d.addLine("allowCallsFrom", Integer.valueOf(this.allowCallsFrom), Integer.valueOf(to.allowCallsFrom));
        }
        if (this.allowMessagesFrom != to.allowMessagesFrom) {
            d.addLine("allowMessagesFrom", Integer.valueOf(this.allowMessagesFrom), Integer.valueOf(to.allowMessagesFrom));
        }
        if (this.allowReminders != to.allowReminders) {
            d.addLine("allowReminders", Boolean.valueOf(this.allowReminders), Boolean.valueOf(to.allowReminders));
        }
        if (this.allowEvents != to.allowEvents) {
            d.addLine("allowEvents", Boolean.valueOf(this.allowEvents), Boolean.valueOf(to.allowEvents));
        }
        if (this.allowWhenScreenOff != to.allowWhenScreenOff) {
            d.addLine("allowWhenScreenOff", Boolean.valueOf(this.allowWhenScreenOff), Boolean.valueOf(to.allowWhenScreenOff));
        }
        if (this.allowWhenScreenOn != to.allowWhenScreenOn) {
            d.addLine("allowWhenScreenOn", Boolean.valueOf(this.allowWhenScreenOn), Boolean.valueOf(to.allowWhenScreenOn));
        }
        ArraySet<String> allRules = new ArraySet();
        addKeys(allRules, this.automaticRules);
        addKeys(allRules, to.automaticRules);
        int N = allRules.size();
        for (int i = SOURCE_ANYONE; i < N; i += SOURCE_CONTACT) {
            String rule = (String) allRules.valueAt(i);
            ZenRule.appendDiff(d, "automaticRule[" + rule + "]", this.automaticRules != null ? (ZenRule) this.automaticRules.get(rule) : null, to.automaticRules != null ? (ZenRule) to.automaticRules.get(rule) : null);
        }
        ZenRule.appendDiff(d, "manualRule", this.manualRule, to.manualRule);
        return d;
    }

    public static Diff diff(ZenModeConfig from, ZenModeConfig to) {
        if (from != null) {
            return from.diff(to);
        }
        Diff d = new Diff();
        if (to != null) {
            d.addLine("config", "insert");
        }
        return d;
    }

    private static <T> void addKeys(ArraySet<T> set, ArrayMap<T, ?> map) {
        if (map != null) {
            for (int i = SOURCE_ANYONE; i < map.size(); i += SOURCE_CONTACT) {
                set.add(map.keyAt(i));
            }
        }
    }

    public boolean isValid() {
        if (!isValidManualRule(this.manualRule)) {
            return DEFAULT_ALLOW_REPEAT_CALLERS;
        }
        int N = this.automaticRules.size();
        for (int i = SOURCE_ANYONE; i < N; i += SOURCE_CONTACT) {
            if (!isValidAutomaticRule((ZenRule) this.automaticRules.valueAt(i))) {
                return DEFAULT_ALLOW_REPEAT_CALLERS;
            }
        }
        return DEFAULT_ALLOW_SCREEN_ON;
    }

    private static boolean isValidManualRule(ZenRule rule) {
        if (rule != null) {
            return Global.isValidZenMode(rule.zenMode) ? sameCondition(rule) : DEFAULT_ALLOW_REPEAT_CALLERS;
        } else {
            return DEFAULT_ALLOW_SCREEN_ON;
        }
    }

    private static boolean isValidAutomaticRule(ZenRule rule) {
        if (rule == null || TextUtils.isEmpty(rule.name) || !Global.isValidZenMode(rule.zenMode) || rule.conditionId == null) {
            return DEFAULT_ALLOW_REPEAT_CALLERS;
        }
        return sameCondition(rule);
    }

    private static boolean sameCondition(ZenRule rule) {
        boolean z = DEFAULT_ALLOW_SCREEN_ON;
        if (rule == null) {
            return DEFAULT_ALLOW_REPEAT_CALLERS;
        }
        if (rule.conditionId == null) {
            if (rule.condition != null) {
                z = DEFAULT_ALLOW_REPEAT_CALLERS;
            }
            return z;
        }
        if (rule.condition != null) {
            z = rule.conditionId.equals(rule.condition.id);
        }
        return z;
    }

    private static int[] generateMinuteBuckets() {
        int[] buckets = new int[15];
        buckets[SOURCE_ANYONE] = 15;
        buckets[SOURCE_CONTACT] = 30;
        buckets[XML_VERSION] = 45;
        for (int i = SOURCE_CONTACT; i <= 12; i += SOURCE_CONTACT) {
            buckets[i + XML_VERSION] = i * 60;
        }
        return buckets;
    }

    public static String sourceToString(int source) {
        switch (source) {
            case SOURCE_ANYONE /*0*/:
                return "anyone";
            case SOURCE_CONTACT /*1*/:
                return Contacts.AUTHORITY;
            case XML_VERSION /*2*/:
                return "stars";
            default:
                return "UNKNOWN";
        }
    }

    public boolean equals(Object o) {
        boolean z = DEFAULT_ALLOW_REPEAT_CALLERS;
        if (!(o instanceof ZenModeConfig)) {
            return DEFAULT_ALLOW_REPEAT_CALLERS;
        }
        if (o == this) {
            return DEFAULT_ALLOW_SCREEN_ON;
        }
        ZenModeConfig other = (ZenModeConfig) o;
        if (other.allowCalls == this.allowCalls && other.allowRepeatCallers == this.allowRepeatCallers && other.allowMessages == this.allowMessages && other.allowCallsFrom == this.allowCallsFrom && other.allowMessagesFrom == this.allowMessagesFrom && other.allowReminders == this.allowReminders && other.allowEvents == this.allowEvents && other.allowWhenScreenOff == this.allowWhenScreenOff && other.allowWhenScreenOn == this.allowWhenScreenOn && other.user == this.user && Objects.equals(other.automaticRules, this.automaticRules)) {
            z = Objects.equals(other.manualRule, this.manualRule);
        }
        return z;
    }

    public int hashCode() {
        return Objects.hash(new Object[]{Boolean.valueOf(this.allowCalls), Boolean.valueOf(this.allowRepeatCallers), Boolean.valueOf(this.allowMessages), Integer.valueOf(this.allowCallsFrom), Integer.valueOf(this.allowMessagesFrom), Boolean.valueOf(this.allowReminders), Boolean.valueOf(this.allowEvents), Boolean.valueOf(this.allowWhenScreenOff), Boolean.valueOf(this.allowWhenScreenOn), Integer.valueOf(this.user), this.automaticRules, this.manualRule});
    }

    private static String toDayList(int[] days) {
        if (days == null || days.length == 0) {
            return ProxyInfo.LOCAL_EXCL_LIST;
        }
        StringBuilder sb = new StringBuilder();
        for (int i = SOURCE_ANYONE; i < days.length; i += SOURCE_CONTACT) {
            if (i > 0) {
                sb.append('.');
            }
            sb.append(days[i]);
        }
        return sb.toString();
    }

    private static int[] tryParseDayList(String dayList, String sep) {
        if (dayList == null) {
            return null;
        }
        String[] tokens = dayList.split(sep);
        if (tokens.length == 0) {
            return null;
        }
        int[] rt = new int[tokens.length];
        for (int i = SOURCE_ANYONE; i < tokens.length; i += SOURCE_CONTACT) {
            int day = tryParseInt(tokens[i], -1);
            if (day == -1) {
                return null;
            }
            rt[i] = day;
        }
        return rt;
    }

    private static int tryParseInt(String value, int defValue) {
        if (TextUtils.isEmpty(value)) {
            return defValue;
        }
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            return defValue;
        }
    }

    private static long tryParseLong(String value, long defValue) {
        if (TextUtils.isEmpty(value)) {
            return defValue;
        }
        try {
            return Long.valueOf(value).longValue();
        } catch (NumberFormatException e) {
            return defValue;
        }
    }

    public static ZenModeConfig readXml(XmlPullParser parser, Migration migration) throws XmlPullParserException, IOException {
        if (parser.getEventType() != XML_VERSION) {
            return null;
        }
        if (!ZEN_TAG.equals(parser.getName())) {
            return null;
        }
        ZenModeConfig rt = new ZenModeConfig();
        if (safeInt(parser, ZEN_ATT_VERSION, XML_VERSION) == SOURCE_CONTACT) {
            return migration.migrate(XmlV1.readXml(parser));
        }
        rt.user = safeInt(parser, ZEN_ATT_USER, rt.user);
        while (true) {
            int type = parser.next();
            if (type == SOURCE_CONTACT) {
                break;
            }
            String tag = parser.getName();
            if (type == 3 && ZEN_TAG.equals(tag)) {
                return rt;
            }
            if (type == XML_VERSION) {
                if (ALLOW_TAG.equals(tag)) {
                    rt.allowCalls = safeBoolean(parser, ALLOW_ATT_CALLS, DEFAULT_ALLOW_REPEAT_CALLERS);
                    rt.allowRepeatCallers = safeBoolean(parser, ALLOW_ATT_REPEAT_CALLERS, DEFAULT_ALLOW_REPEAT_CALLERS);
                    rt.allowMessages = safeBoolean(parser, ALLOW_ATT_MESSAGES, DEFAULT_ALLOW_REPEAT_CALLERS);
                    rt.allowReminders = safeBoolean(parser, ALLOW_ATT_REMINDERS, DEFAULT_ALLOW_SCREEN_ON);
                    rt.allowEvents = safeBoolean(parser, ALLOW_ATT_EVENTS, DEFAULT_ALLOW_SCREEN_ON);
                    int from = safeInt(parser, ALLOW_ATT_FROM, -1);
                    int callsFrom = safeInt(parser, ALLOW_ATT_CALLS_FROM, -1);
                    int messagesFrom = safeInt(parser, ALLOW_ATT_MESSAGES_FROM, -1);
                    if (isValidSource(callsFrom) && isValidSource(messagesFrom)) {
                        rt.allowCallsFrom = callsFrom;
                        rt.allowMessagesFrom = messagesFrom;
                    } else if (isValidSource(from)) {
                        Slog.i(TAG, "Migrating existing shared 'from': " + sourceToString(from));
                        rt.allowCallsFrom = from;
                        rt.allowMessagesFrom = from;
                    } else {
                        rt.allowCallsFrom = SOURCE_CONTACT;
                        rt.allowMessagesFrom = SOURCE_CONTACT;
                    }
                    rt.allowWhenScreenOff = safeBoolean(parser, ALLOW_ATT_SCREEN_OFF, DEFAULT_ALLOW_SCREEN_ON);
                    rt.allowWhenScreenOn = safeBoolean(parser, ALLOW_ATT_SCREEN_ON, DEFAULT_ALLOW_SCREEN_ON);
                } else if (MANUAL_TAG.equals(tag)) {
                    rt.manualRule = readRuleXml(parser);
                } else if (AUTOMATIC_TAG.equals(tag)) {
                    String id = parser.getAttributeValue(null, RULE_ATT_ID);
                    ZenRule automaticRule = readRuleXml(parser);
                    if (!(id == null || automaticRule == null)) {
                        automaticRule.id = id;
                        rt.automaticRules.put(id, automaticRule);
                    }
                }
            }
        }
        throw new IllegalStateException("Failed to reach END_DOCUMENT");
    }

    public void writeXml(XmlSerializer out) throws IOException {
        out.startTag(null, ZEN_TAG);
        out.attribute(null, ZEN_ATT_VERSION, Integer.toString(XML_VERSION));
        out.attribute(null, ZEN_ATT_USER, Integer.toString(this.user));
        out.startTag(null, ALLOW_TAG);
        out.attribute(null, ALLOW_ATT_CALLS, Boolean.toString(this.allowCalls));
        out.attribute(null, ALLOW_ATT_REPEAT_CALLERS, Boolean.toString(this.allowRepeatCallers));
        out.attribute(null, ALLOW_ATT_MESSAGES, Boolean.toString(this.allowMessages));
        out.attribute(null, ALLOW_ATT_REMINDERS, Boolean.toString(this.allowReminders));
        out.attribute(null, ALLOW_ATT_EVENTS, Boolean.toString(this.allowEvents));
        out.attribute(null, ALLOW_ATT_CALLS_FROM, Integer.toString(this.allowCallsFrom));
        out.attribute(null, ALLOW_ATT_MESSAGES_FROM, Integer.toString(this.allowMessagesFrom));
        out.attribute(null, ALLOW_ATT_SCREEN_OFF, Boolean.toString(this.allowWhenScreenOff));
        out.attribute(null, ALLOW_ATT_SCREEN_ON, Boolean.toString(this.allowWhenScreenOn));
        out.endTag(null, ALLOW_TAG);
        if (this.manualRule != null) {
            out.startTag(null, MANUAL_TAG);
            writeRuleXml(this.manualRule, out);
            out.endTag(null, MANUAL_TAG);
        }
        int N = this.automaticRules.size();
        for (int i = SOURCE_ANYONE; i < N; i += SOURCE_CONTACT) {
            String id = (String) this.automaticRules.keyAt(i);
            ZenRule automaticRule = (ZenRule) this.automaticRules.valueAt(i);
            out.startTag(null, AUTOMATIC_TAG);
            out.attribute(null, RULE_ATT_ID, id);
            writeRuleXml(automaticRule, out);
            out.endTag(null, AUTOMATIC_TAG);
        }
        out.endTag(null, ZEN_TAG);
    }

    public static ZenRule readRuleXml(XmlPullParser parser) {
        ZenRule rt = new ZenRule();
        rt.enabled = safeBoolean(parser, RULE_ATT_ENABLED, DEFAULT_ALLOW_SCREEN_ON);
        rt.snoozing = safeBoolean(parser, RULE_ATT_SNOOZING, DEFAULT_ALLOW_REPEAT_CALLERS);
        rt.name = parser.getAttributeValue(null, RULE_ATT_NAME);
        String zen = parser.getAttributeValue(null, ZEN_TAG);
        rt.zenMode = tryParseZenMode(zen, -1);
        if (rt.zenMode == -1) {
            Slog.w(TAG, "Bad zen mode in rule xml:" + zen);
            return null;
        }
        rt.conditionId = safeUri(parser, RULE_ATT_CONDITION_ID);
        rt.component = safeComponentName(parser, RULE_ATT_COMPONENT);
        rt.creationTime = safeLong(parser, RULE_ATT_CREATION_TIME, 0);
        rt.condition = readConditionXml(parser);
        return rt;
    }

    public static void writeRuleXml(ZenRule rule, XmlSerializer out) throws IOException {
        out.attribute(null, RULE_ATT_ENABLED, Boolean.toString(rule.enabled));
        out.attribute(null, RULE_ATT_SNOOZING, Boolean.toString(rule.snoozing));
        if (rule.name != null) {
            out.attribute(null, RULE_ATT_NAME, rule.name);
        }
        out.attribute(null, ZEN_TAG, Integer.toString(rule.zenMode));
        if (rule.component != null) {
            out.attribute(null, RULE_ATT_COMPONENT, rule.component.flattenToString());
        }
        if (rule.conditionId != null) {
            out.attribute(null, RULE_ATT_CONDITION_ID, rule.conditionId.toString());
        }
        out.attribute(null, RULE_ATT_CREATION_TIME, Long.toString(rule.creationTime));
        if (rule.condition != null) {
            writeConditionXml(rule.condition, out);
        }
    }

    public static Condition readConditionXml(XmlPullParser parser) {
        Uri id = safeUri(parser, CONDITION_ATT_ID);
        if (id == null) {
            return null;
        }
        try {
            return new Condition(id, parser.getAttributeValue(null, CONDITION_ATT_SUMMARY), parser.getAttributeValue(null, CONDITION_ATT_LINE1), parser.getAttributeValue(null, CONDITION_ATT_LINE2), safeInt(parser, CONDITION_ATT_ICON, -1), safeInt(parser, CONDITION_ATT_STATE, -1), safeInt(parser, CONDITION_ATT_FLAGS, -1));
        } catch (IllegalArgumentException e) {
            Slog.w(TAG, "Unable to read condition xml", e);
            return null;
        }
    }

    public static void writeConditionXml(Condition c, XmlSerializer out) throws IOException {
        out.attribute(null, CONDITION_ATT_ID, c.id.toString());
        out.attribute(null, CONDITION_ATT_SUMMARY, c.summary);
        out.attribute(null, CONDITION_ATT_LINE1, c.line1);
        out.attribute(null, CONDITION_ATT_LINE2, c.line2);
        out.attribute(null, CONDITION_ATT_ICON, Integer.toString(c.icon));
        out.attribute(null, CONDITION_ATT_STATE, Integer.toString(c.state));
        out.attribute(null, CONDITION_ATT_FLAGS, Integer.toString(c.flags));
    }

    public static boolean isValidHour(int val) {
        return (val < 0 || val >= 24) ? DEFAULT_ALLOW_REPEAT_CALLERS : DEFAULT_ALLOW_SCREEN_ON;
    }

    public static boolean isValidMinute(int val) {
        return (val < 0 || val >= 60) ? DEFAULT_ALLOW_REPEAT_CALLERS : DEFAULT_ALLOW_SCREEN_ON;
    }

    private static boolean isValidSource(int source) {
        return (source < 0 || source > XML_VERSION) ? DEFAULT_ALLOW_REPEAT_CALLERS : DEFAULT_ALLOW_SCREEN_ON;
    }

    private static boolean safeBoolean(XmlPullParser parser, String att, boolean defValue) {
        return safeBoolean(parser.getAttributeValue(null, att), defValue);
    }

    private static boolean safeBoolean(String val, boolean defValue) {
        if (TextUtils.isEmpty(val)) {
            return defValue;
        }
        return Boolean.valueOf(val).booleanValue();
    }

    private static int safeInt(XmlPullParser parser, String att, int defValue) {
        return tryParseInt(parser.getAttributeValue(null, att), defValue);
    }

    private static ComponentName safeComponentName(XmlPullParser parser, String att) {
        String val = parser.getAttributeValue(null, att);
        if (TextUtils.isEmpty(val)) {
            return null;
        }
        return ComponentName.unflattenFromString(val);
    }

    private static Uri safeUri(XmlPullParser parser, String att) {
        String val = parser.getAttributeValue(null, att);
        if (TextUtils.isEmpty(val)) {
            return null;
        }
        return Uri.parse(val);
    }

    private static long safeLong(XmlPullParser parser, String att, long defValue) {
        return tryParseLong(parser.getAttributeValue(null, att), defValue);
    }

    public int describeContents() {
        return SOURCE_ANYONE;
    }

    public ZenModeConfig copy() {
        Parcel parcel = Parcel.obtain();
        try {
            writeToParcel(parcel, SOURCE_ANYONE);
            parcel.setDataPosition(SOURCE_ANYONE);
            ZenModeConfig zenModeConfig = new ZenModeConfig(parcel);
            return zenModeConfig;
        } finally {
            parcel.recycle();
        }
    }

    public Policy toNotificationPolicy() {
        int priorityCategories = SOURCE_ANYONE;
        if (this.allowCalls) {
            priorityCategories = 8;
        }
        if (this.allowMessages) {
            priorityCategories |= 4;
        }
        if (this.allowEvents) {
            priorityCategories |= XML_VERSION;
        }
        if (this.allowReminders) {
            priorityCategories |= SOURCE_CONTACT;
        }
        if (this.allowRepeatCallers) {
            priorityCategories |= 16;
        }
        int suppressedVisualEffects = SOURCE_ANYONE;
        if (!this.allowWhenScreenOff) {
            suppressedVisualEffects = SOURCE_CONTACT;
        }
        if (!this.allowWhenScreenOn) {
            suppressedVisualEffects |= XML_VERSION;
        }
        return new Policy(priorityCategories, sourceToPrioritySenders(this.allowCallsFrom, SOURCE_CONTACT), sourceToPrioritySenders(this.allowMessagesFrom, SOURCE_CONTACT), suppressedVisualEffects);
    }

    private static int sourceToPrioritySenders(int source, int def) {
        switch (source) {
            case SOURCE_ANYONE /*0*/:
                return SOURCE_ANYONE;
            case SOURCE_CONTACT /*1*/:
                return SOURCE_CONTACT;
            case XML_VERSION /*2*/:
                return XML_VERSION;
            default:
                return def;
        }
    }

    private static int prioritySendersToSource(int prioritySenders, int def) {
        switch (prioritySenders) {
            case SOURCE_ANYONE /*0*/:
                return SOURCE_ANYONE;
            case SOURCE_CONTACT /*1*/:
                return SOURCE_CONTACT;
            case XML_VERSION /*2*/:
                return XML_VERSION;
            default:
                return def;
        }
    }

    public void applyNotificationPolicy(Policy policy) {
        boolean z = DEFAULT_ALLOW_SCREEN_ON;
        if (policy != null) {
            boolean z2;
            if ((policy.priorityCategories & 8) != 0) {
                z2 = DEFAULT_ALLOW_SCREEN_ON;
            } else {
                z2 = DEFAULT_ALLOW_REPEAT_CALLERS;
            }
            this.allowCalls = z2;
            if ((policy.priorityCategories & 4) != 0) {
                z2 = DEFAULT_ALLOW_SCREEN_ON;
            } else {
                z2 = DEFAULT_ALLOW_REPEAT_CALLERS;
            }
            this.allowMessages = z2;
            if ((policy.priorityCategories & XML_VERSION) != 0) {
                z2 = DEFAULT_ALLOW_SCREEN_ON;
            } else {
                z2 = DEFAULT_ALLOW_REPEAT_CALLERS;
            }
            this.allowEvents = z2;
            if ((policy.priorityCategories & SOURCE_CONTACT) != 0) {
                z2 = DEFAULT_ALLOW_SCREEN_ON;
            } else {
                z2 = DEFAULT_ALLOW_REPEAT_CALLERS;
            }
            this.allowReminders = z2;
            if ((policy.priorityCategories & 16) != 0) {
                z2 = DEFAULT_ALLOW_SCREEN_ON;
            } else {
                z2 = DEFAULT_ALLOW_REPEAT_CALLERS;
            }
            this.allowRepeatCallers = z2;
            this.allowCallsFrom = prioritySendersToSource(policy.priorityCallSenders, this.allowCallsFrom);
            this.allowMessagesFrom = prioritySendersToSource(policy.priorityMessageSenders, this.allowMessagesFrom);
            if (policy.suppressedVisualEffects != -1) {
                if ((policy.suppressedVisualEffects & SOURCE_CONTACT) == 0) {
                    z2 = DEFAULT_ALLOW_SCREEN_ON;
                } else {
                    z2 = DEFAULT_ALLOW_REPEAT_CALLERS;
                }
                this.allowWhenScreenOff = z2;
                if ((policy.suppressedVisualEffects & XML_VERSION) != 0) {
                    z = DEFAULT_ALLOW_REPEAT_CALLERS;
                }
                this.allowWhenScreenOn = z;
            }
        }
    }

    public static Condition toTimeCondition(Context context, int minutesFromNow, int userHandle) {
        return toTimeCondition(context, minutesFromNow, userHandle, DEFAULT_ALLOW_REPEAT_CALLERS);
    }

    public static Condition toTimeCondition(Context context, int minutesFromNow, int userHandle, boolean shortVersion) {
        return toTimeCondition(context, System.currentTimeMillis() + ((long) (minutesFromNow == 0 ? ZERO_VALUE_MS : MINUTES_MS * minutesFromNow)), minutesFromNow, userHandle, shortVersion);
    }

    public static Condition toTimeCondition(Context context, long time, int minutes, int userHandle, boolean shortVersion) {
        String summary;
        String line1;
        String line2;
        CharSequence formattedTime = getFormattedTime(context, time, userHandle);
        Resources res = context.getResources();
        int num;
        int summaryResId;
        Object[] objArr;
        int line1ResId;
        if (minutes < 60) {
            num = minutes;
            if (shortVersion) {
                summaryResId = 18087947;
            } else {
                summaryResId = 18087946;
            }
            objArr = new Object[XML_VERSION];
            objArr[SOURCE_ANYONE] = Integer.valueOf(minutes);
            objArr[SOURCE_CONTACT] = formattedTime;
            summary = res.getQuantityString(summaryResId, minutes, objArr);
            if (shortVersion) {
                line1ResId = 18087951;
            } else {
                line1ResId = 18087950;
            }
            objArr = new Object[XML_VERSION];
            objArr[SOURCE_ANYONE] = Integer.valueOf(minutes);
            objArr[SOURCE_CONTACT] = formattedTime;
            line1 = res.getQuantityString(line1ResId, minutes, objArr);
            objArr = new Object[SOURCE_CONTACT];
            objArr[SOURCE_ANYONE] = formattedTime;
            line2 = res.getString(17040804, objArr);
        } else if (minutes < DAY_MINUTES) {
            num = Math.round(((float) minutes) / SensorManager.MAGNETIC_FIELD_EARTH_MAX);
            if (shortVersion) {
                summaryResId = 18087949;
            } else {
                summaryResId = 18087948;
            }
            objArr = new Object[XML_VERSION];
            objArr[SOURCE_ANYONE] = Integer.valueOf(num);
            objArr[SOURCE_CONTACT] = formattedTime;
            summary = res.getQuantityString(summaryResId, num, objArr);
            if (shortVersion) {
                line1ResId = 18087953;
            } else {
                line1ResId = 18087952;
            }
            objArr = new Object[XML_VERSION];
            objArr[SOURCE_ANYONE] = Integer.valueOf(num);
            objArr[SOURCE_CONTACT] = formattedTime;
            line1 = res.getQuantityString(line1ResId, num, objArr);
            objArr = new Object[SOURCE_CONTACT];
            objArr[SOURCE_ANYONE] = formattedTime;
            line2 = res.getString(17040804, objArr);
        } else {
            objArr = new Object[SOURCE_CONTACT];
            objArr[SOURCE_ANYONE] = formattedTime;
            line2 = res.getString(17040804, objArr);
            line1 = line2;
            summary = line2;
        }
        return new Condition(toCountdownConditionId(time), summary, line1, line2, SOURCE_ANYONE, SOURCE_CONTACT, SOURCE_CONTACT);
    }

    public static Condition toNextAlarmCondition(Context context, long now, long alarm, int userHandle) {
        CharSequence formattedTime = getFormattedTime(context, alarm, userHandle);
        Resources res = context.getResources();
        Object[] objArr = new Object[SOURCE_CONTACT];
        objArr[SOURCE_ANYONE] = formattedTime;
        return new Condition(toCountdownConditionId(alarm), ProxyInfo.LOCAL_EXCL_LIST, res.getString(17040805, objArr), ProxyInfo.LOCAL_EXCL_LIST, SOURCE_ANYONE, SOURCE_CONTACT, SOURCE_CONTACT);
    }

    private static CharSequence getFormattedTime(Context context, long time, int userHandle) {
        String skeleton = "EEE " + (DateFormat.is24HourFormat(context, userHandle) ? "Hm" : "hma");
        GregorianCalendar now = new GregorianCalendar();
        GregorianCalendar endTime = new GregorianCalendar();
        endTime.setTimeInMillis(time);
        if (now.get(SOURCE_CONTACT) == endTime.get(SOURCE_CONTACT) && now.get(XML_VERSION) == endTime.get(XML_VERSION) && now.get(5) == endTime.get(5)) {
            skeleton = DateFormat.is24HourFormat(context, userHandle) ? "Hm" : "hma";
        }
        return DateFormat.format(DateFormat.getBestDateTimePattern(Locale.getDefault(), skeleton), time);
    }

    public static Uri toCountdownConditionId(long time) {
        return new Builder().scheme(CONDITION_TAG).authority(SYSTEM_AUTHORITY).appendPath(COUNTDOWN_PATH).appendPath(Long.toString(time)).build();
    }

    public static long tryParseCountdownConditionId(Uri conditionId) {
        if (!Condition.isValidId(conditionId, SYSTEM_AUTHORITY) || conditionId.getPathSegments().size() != XML_VERSION || !COUNTDOWN_PATH.equals(conditionId.getPathSegments().get(SOURCE_ANYONE))) {
            return 0;
        }
        try {
            return Long.parseLong((String) conditionId.getPathSegments().get(SOURCE_CONTACT));
        } catch (RuntimeException e) {
            Slog.w(TAG, "Error parsing countdown condition: " + conditionId, e);
            return 0;
        }
    }

    public static boolean isValidCountdownConditionId(Uri conditionId) {
        return tryParseCountdownConditionId(conditionId) != 0 ? DEFAULT_ALLOW_SCREEN_ON : DEFAULT_ALLOW_REPEAT_CALLERS;
    }

    public static Uri toScheduleConditionId(ScheduleInfo schedule) {
        return new Builder().scheme(CONDITION_TAG).authority(SYSTEM_AUTHORITY).appendPath(SCHEDULE_PATH).appendQueryParameter("days", toDayList(schedule.days)).appendQueryParameter("start", schedule.startHour + "." + schedule.startMinute).appendQueryParameter(Instances.END, schedule.endHour + "." + schedule.endMinute).appendQueryParameter("exitAtAlarm", String.valueOf(schedule.exitAtAlarm)).build();
    }

    public static boolean isValidScheduleConditionId(Uri conditionId) {
        return tryParseScheduleConditionId(conditionId) != null ? DEFAULT_ALLOW_SCREEN_ON : DEFAULT_ALLOW_REPEAT_CALLERS;
    }

    public static ScheduleInfo tryParseScheduleConditionId(Uri conditionId) {
        boolean isSchedule;
        if (conditionId != null && conditionId.getScheme().equals(CONDITION_TAG) && conditionId.getAuthority().equals(SYSTEM_AUTHORITY) && conditionId.getPathSegments().size() == SOURCE_CONTACT) {
            isSchedule = ((String) conditionId.getPathSegments().get(SOURCE_ANYONE)).equals(SCHEDULE_PATH);
        } else {
            isSchedule = DEFAULT_ALLOW_REPEAT_CALLERS;
        }
        if (!isSchedule) {
            return null;
        }
        int[] start = tryParseHourAndMinute(conditionId.getQueryParameter("start"));
        int[] end = tryParseHourAndMinute(conditionId.getQueryParameter(Instances.END));
        if (start == null || end == null) {
            return null;
        }
        ScheduleInfo rt = new ScheduleInfo();
        rt.days = tryParseDayList(conditionId.getQueryParameter("days"), "\\.");
        rt.startHour = start[SOURCE_ANYONE];
        rt.startMinute = start[SOURCE_CONTACT];
        rt.endHour = end[SOURCE_ANYONE];
        rt.endMinute = end[SOURCE_CONTACT];
        rt.exitAtAlarm = safeBoolean(conditionId.getQueryParameter("exitAtAlarm"), DEFAULT_ALLOW_REPEAT_CALLERS);
        return rt;
    }

    public static ComponentName getScheduleConditionProvider() {
        return new ComponentName(SYSTEM_AUTHORITY, "ScheduleConditionProvider");
    }

    public static Uri toEventConditionId(EventInfo event) {
        return new Builder().scheme(CONDITION_TAG).authority(SYSTEM_AUTHORITY).appendPath(EVENT_PATH).appendQueryParameter("userId", Long.toString((long) event.userId)).appendQueryParameter("calendar", event.calendar != null ? event.calendar : ProxyInfo.LOCAL_EXCL_LIST).appendQueryParameter("reply", Integer.toString(event.reply)).build();
    }

    public static boolean isValidEventConditionId(Uri conditionId) {
        return tryParseEventConditionId(conditionId) != null ? DEFAULT_ALLOW_SCREEN_ON : DEFAULT_ALLOW_REPEAT_CALLERS;
    }

    public static EventInfo tryParseEventConditionId(Uri conditionId) {
        boolean isEvent;
        if (conditionId != null && conditionId.getScheme().equals(CONDITION_TAG) && conditionId.getAuthority().equals(SYSTEM_AUTHORITY) && conditionId.getPathSegments().size() == SOURCE_CONTACT) {
            isEvent = ((String) conditionId.getPathSegments().get(SOURCE_ANYONE)).equals(EVENT_PATH);
        } else {
            isEvent = DEFAULT_ALLOW_REPEAT_CALLERS;
        }
        if (!isEvent) {
            return null;
        }
        EventInfo rt = new EventInfo();
        rt.userId = tryParseInt(conditionId.getQueryParameter("userId"), UserHandle.USER_NULL);
        rt.calendar = conditionId.getQueryParameter("calendar");
        if (TextUtils.isEmpty(rt.calendar) || tryParseLong(rt.calendar, -1) != -1) {
            rt.calendar = null;
        }
        rt.reply = tryParseInt(conditionId.getQueryParameter("reply"), SOURCE_ANYONE);
        return rt;
    }

    public static ComponentName getEventConditionProvider() {
        return new ComponentName(SYSTEM_AUTHORITY, "EventConditionProvider");
    }

    private static int[] tryParseHourAndMinute(String value) {
        int[] iArr = null;
        if (TextUtils.isEmpty(value)) {
            return null;
        }
        int i = value.indexOf(46);
        if (i < SOURCE_CONTACT || i >= value.length() - 1) {
            return null;
        }
        int hour = tryParseInt(value.substring(SOURCE_ANYONE, i), -1);
        int minute = tryParseInt(value.substring(i + SOURCE_CONTACT), -1);
        if (isValidHour(hour) && isValidMinute(minute)) {
            iArr = new int[XML_VERSION];
            iArr[SOURCE_ANYONE] = hour;
            iArr[SOURCE_CONTACT] = minute;
        }
        return iArr;
    }

    private static int tryParseZenMode(String value, int defValue) {
        int rt = tryParseInt(value, defValue);
        return Global.isValidZenMode(rt) ? rt : defValue;
    }

    public static String newRuleId() {
        return UUID.randomUUID().toString().replace("-", ProxyInfo.LOCAL_EXCL_LIST);
    }

    public static String getConditionSummary(Context context, ZenModeConfig config, int userHandle, boolean shortVersion) {
        return getConditionLine(context, config, userHandle, DEFAULT_ALLOW_REPEAT_CALLERS, shortVersion);
    }

    private static String getConditionLine(Context context, ZenModeConfig config, int userHandle, boolean useLine1, boolean shortVersion) {
        if (config == null) {
            return ProxyInfo.LOCAL_EXCL_LIST;
        }
        if (config.manualRule != null) {
            Uri id = config.manualRule.conditionId;
            if (id == null) {
                return context.getString(17040806);
            }
            long time = tryParseCountdownConditionId(id);
            Condition c = config.manualRule.condition;
            if (time > 0) {
                c = toTimeCondition(context, time, Math.round(((float) (time - System.currentTimeMillis())) / 60000.0f), userHandle, shortVersion);
            }
            String rt = c == null ? ProxyInfo.LOCAL_EXCL_LIST : useLine1 ? c.line1 : c.summary;
            if (TextUtils.isEmpty(rt)) {
                rt = ProxyInfo.LOCAL_EXCL_LIST;
            }
            return rt;
        }
        String summary = ProxyInfo.LOCAL_EXCL_LIST;
        for (ZenRule automaticRule : config.automaticRules.values()) {
            if (automaticRule.isAutomaticActive()) {
                if (summary.isEmpty()) {
                    summary = automaticRule.name;
                } else {
                    Resources resources = context.getResources();
                    Object[] objArr = new Object[XML_VERSION];
                    objArr[SOURCE_ANYONE] = summary;
                    objArr[SOURCE_CONTACT] = automaticRule.name;
                    summary = resources.getString(17040808, objArr);
                }
            }
        }
        return summary;
    }
}
