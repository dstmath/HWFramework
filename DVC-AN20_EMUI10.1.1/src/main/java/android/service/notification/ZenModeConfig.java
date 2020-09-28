package android.service.notification;

import android.annotation.UnsupportedAppUsage;
import android.app.ActivityManager;
import android.app.AlarmManager;
import android.app.NotificationManager;
import android.app.job.JobInfo;
import android.content.ComponentName;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;
import android.provider.Contacts;
import android.provider.Settings;
import android.provider.SettingsStringUtil;
import android.provider.Telephony;
import android.service.notification.ZenPolicy;
import android.telecom.Logging.Session;
import android.text.TextUtils;
import android.text.format.DateFormat;
import android.util.ArrayMap;
import android.util.ArraySet;
import android.util.Slog;
import android.util.proto.ProtoOutputStream;
import com.android.internal.R;
import com.android.internal.content.NativeLibraryHelper;
import com.android.internal.telephony.IccCardConstants;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.TimeZone;
import java.util.UUID;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlSerializer;

public class ZenModeConfig implements Parcelable {
    private static final String ALLOW_ATT_ALARMS = "alarms";
    private static final String ALLOW_ATT_CALLS = "calls";
    private static final String ALLOW_ATT_CALLS_FROM = "callsFrom";
    private static final String ALLOW_ATT_EVENTS = "events";
    private static final String ALLOW_ATT_FROM = "from";
    private static final String ALLOW_ATT_MEDIA = "media";
    private static final String ALLOW_ATT_MESSAGES = "messages";
    private static final String ALLOW_ATT_MESSAGES_FROM = "messagesFrom";
    private static final String ALLOW_ATT_REMINDERS = "reminders";
    private static final String ALLOW_ATT_REPEAT_CALLERS = "repeatCallers";
    private static final String ALLOW_ATT_SCREEN_OFF = "visualScreenOff";
    private static final String ALLOW_ATT_SCREEN_ON = "visualScreenOn";
    private static final String ALLOW_ATT_SYSTEM = "system";
    private static final String ALLOW_TAG = "allow";
    public static final int[] ALL_DAYS = {1, 2, 3, 4, 5, 6, 7};
    private static final String AUTOMATIC_TAG = "automatic";
    private static final String CONDITION_ATT_FLAGS = "flags";
    private static final String CONDITION_ATT_ICON = "icon";
    private static final String CONDITION_ATT_ID = "id";
    private static final String CONDITION_ATT_LINE1 = "line1";
    private static final String CONDITION_ATT_LINE2 = "line2";
    private static final String CONDITION_ATT_STATE = "state";
    private static final String CONDITION_ATT_SUMMARY = "summary";
    public static final String COUNTDOWN_PATH = "countdown";
    public static final Parcelable.Creator<ZenModeConfig> CREATOR = new Parcelable.Creator<ZenModeConfig>() {
        /* class android.service.notification.ZenModeConfig.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public ZenModeConfig createFromParcel(Parcel source) {
            return new ZenModeConfig(source);
        }

        @Override // android.os.Parcelable.Creator
        public ZenModeConfig[] newArray(int size) {
            return new ZenModeConfig[size];
        }
    };
    private static final int DAY_MINUTES = 1440;
    private static final boolean DEFAULT_ALLOW_ALARMS = true;
    private static final boolean DEFAULT_ALLOW_CALLS = true;
    private static final boolean DEFAULT_ALLOW_EVENTS = false;
    private static final boolean DEFAULT_ALLOW_MEDIA = true;
    private static final boolean DEFAULT_ALLOW_MESSAGES = false;
    private static final boolean DEFAULT_ALLOW_REMINDERS = false;
    private static final boolean DEFAULT_ALLOW_REPEAT_CALLERS = true;
    private static final boolean DEFAULT_ALLOW_SYSTEM = false;
    private static final int DEFAULT_CALLS_SOURCE = 2;
    private static final boolean DEFAULT_CHANNELS_BYPASSING_DND = false;
    public static final List<String> DEFAULT_RULE_IDS = Arrays.asList(EVERY_NIGHT_DEFAULT_RULE_ID, EVENTS_DEFAULT_RULE_ID);
    private static final int DEFAULT_SOURCE = 1;
    private static final int DEFAULT_SUPPRESSED_VISUAL_EFFECTS = 0;
    private static final String DISALLOW_ATT_VISUAL_EFFECTS = "visualEffects";
    private static final String DISALLOW_TAG = "disallow";
    public static final String EVENTS_DEFAULT_RULE_ID = "EVENTS_DEFAULT_RULE";
    public static final String EVENT_PATH = "event";
    public static final String EVERY_NIGHT_DEFAULT_RULE_ID = "EVERY_NIGHT_DEFAULT_RULE";
    public static final String IS_ALARM_PATH = "alarm";
    private static final String MANUAL_TAG = "manual";
    public static final int MAX_SOURCE = 2;
    private static final int MINUTES_MS = 60000;
    public static final int[] MINUTE_BUCKETS = generateMinuteBuckets();
    private static final String RULE_ATT_COMPONENT = "component";
    private static final String RULE_ATT_CONDITION_ID = "conditionId";
    private static final String RULE_ATT_CONFIG_ACTIVITY = "configActivity";
    private static final String RULE_ATT_CREATION_TIME = "creationTime";
    private static final String RULE_ATT_ENABLED = "enabled";
    private static final String RULE_ATT_ENABLER = "enabler";
    private static final String RULE_ATT_ID = "ruleId";
    private static final String RULE_ATT_MODIFIED = "modified";
    private static final String RULE_ATT_NAME = "name";
    private static final String RULE_ATT_SNOOZING = "snoozing";
    private static final String RULE_ATT_ZEN = "zen";
    public static final String SCHEDULE_PATH = "schedule";
    private static final int SECONDS_MS = 1000;
    private static final String SHOW_ATT_AMBIENT = "showAmbient";
    private static final String SHOW_ATT_BADGES = "showBadges";
    private static final String SHOW_ATT_FULL_SCREEN_INTENT = "showFullScreenIntent";
    private static final String SHOW_ATT_LIGHTS = "showLights";
    private static final String SHOW_ATT_NOTIFICATION_LIST = "showNotificationList";
    private static final String SHOW_ATT_PEEK = "shoePeek";
    private static final String SHOW_ATT_STATUS_BAR_ICONS = "showStatusBarIcons";
    public static final int SOURCE_ANYONE = 0;
    public static final int SOURCE_CONTACT = 1;
    public static final int SOURCE_STAR = 2;
    private static final String STATE_ATT_CHANNELS_BYPASSING_DND = "areChannelsBypassingDnd";
    private static final String STATE_TAG = "state";
    public static final String SYSTEM_AUTHORITY = "android";
    private static String TAG = "ZenModeConfig";
    public static final int XML_VERSION = 8;
    public static final int XML_VERSION_P = 8;
    private static final String ZEN_ATT_USER = "user";
    private static final String ZEN_ATT_VERSION = "version";
    private static final String ZEN_POLICY_TAG = "zen_policy";
    public static final String ZEN_TAG = "zen";
    private static final int ZERO_VALUE_MS = 10000;
    @UnsupportedAppUsage
    public boolean allowAlarms = true;
    public boolean allowCalls = true;
    public int allowCallsFrom = 2;
    public boolean allowEvents = false;
    public boolean allowMedia = true;
    public boolean allowMessages = false;
    public int allowMessagesFrom = 1;
    public boolean allowReminders = false;
    public boolean allowRepeatCallers = true;
    public boolean allowSystem = false;
    public boolean areChannelsBypassingDnd = false;
    @UnsupportedAppUsage
    public ArrayMap<String, ZenRule> automaticRules = new ArrayMap<>();
    public ZenRule manualRule;
    public int suppressedVisualEffects = 0;
    public int user = 0;
    public int version;

    @UnsupportedAppUsage
    public ZenModeConfig() {
    }

    public ZenModeConfig(Parcel source) {
        boolean z = true;
        this.allowCalls = source.readInt() == 1;
        this.allowRepeatCallers = source.readInt() == 1;
        this.allowMessages = source.readInt() == 1;
        this.allowReminders = source.readInt() == 1;
        this.allowEvents = source.readInt() == 1;
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
            for (int i = 0; i < len; i++) {
                this.automaticRules.put(ids[i], rules[i]);
            }
        }
        this.allowAlarms = source.readInt() == 1;
        this.allowMedia = source.readInt() == 1;
        this.allowSystem = source.readInt() == 1;
        this.suppressedVisualEffects = source.readInt();
        this.areChannelsBypassingDnd = source.readInt() != 1 ? false : z;
    }

    @Override // android.os.Parcelable
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.allowCalls ? 1 : 0);
        dest.writeInt(this.allowRepeatCallers ? 1 : 0);
        dest.writeInt(this.allowMessages ? 1 : 0);
        dest.writeInt(this.allowReminders ? 1 : 0);
        dest.writeInt(this.allowEvents ? 1 : 0);
        dest.writeInt(this.allowCallsFrom);
        dest.writeInt(this.allowMessagesFrom);
        dest.writeInt(this.user);
        dest.writeParcelable(this.manualRule, 0);
        if (!this.automaticRules.isEmpty()) {
            int len = this.automaticRules.size();
            String[] ids = new String[len];
            ZenRule[] rules = new ZenRule[len];
            for (int i = 0; i < len; i++) {
                ids[i] = this.automaticRules.keyAt(i);
                rules[i] = this.automaticRules.valueAt(i);
            }
            dest.writeInt(len);
            dest.writeStringArray(ids);
            dest.writeTypedArray(rules, 0);
        } else {
            dest.writeInt(0);
        }
        dest.writeInt(this.allowAlarms ? 1 : 0);
        dest.writeInt(this.allowMedia ? 1 : 0);
        dest.writeInt(this.allowSystem ? 1 : 0);
        dest.writeInt(this.suppressedVisualEffects);
        dest.writeInt(this.areChannelsBypassingDnd ? 1 : 0);
    }

    public String toString() {
        return ZenModeConfig.class.getSimpleName() + '[' + "user=" + this.user + ",allowAlarms=" + this.allowAlarms + ",allowMedia=" + this.allowMedia + ",allowSystem=" + this.allowSystem + ",allowReminders=" + this.allowReminders + ",allowEvents=" + this.allowEvents + ",allowCalls=" + this.allowCalls + ",allowRepeatCallers=" + this.allowRepeatCallers + ",allowMessages=" + this.allowMessages + ",allowCallsFrom=" + sourceToString(this.allowCallsFrom) + ",allowMessagesFrom=" + sourceToString(this.allowMessagesFrom) + ",suppressedVisualEffects=" + this.suppressedVisualEffects + ",areChannelsBypassingDnd=" + this.areChannelsBypassingDnd + ",\nautomaticRules=" + rulesToString() + ",\nmanualRule=" + this.manualRule + ']';
    }

    private String rulesToString() {
        if (this.automaticRules.isEmpty()) {
            return "{}";
        }
        StringBuilder buffer = new StringBuilder(this.automaticRules.size() * 28);
        buffer.append('{');
        for (int i = 0; i < this.automaticRules.size(); i++) {
            if (i > 0) {
                buffer.append(",\n");
            }
            buffer.append(this.automaticRules.valueAt(i));
        }
        buffer.append('}');
        return buffer.toString();
    }

    public Diff diff(ZenModeConfig to) {
        Diff d = new Diff();
        if (to == null) {
            return d.addLine("config", "delete");
        }
        int i = this.user;
        if (i != to.user) {
            d.addLine("user", Integer.valueOf(i), Integer.valueOf(to.user));
        }
        boolean z = this.allowAlarms;
        if (z != to.allowAlarms) {
            d.addLine("allowAlarms", Boolean.valueOf(z), Boolean.valueOf(to.allowAlarms));
        }
        boolean z2 = this.allowMedia;
        if (z2 != to.allowMedia) {
            d.addLine("allowMedia", Boolean.valueOf(z2), Boolean.valueOf(to.allowMedia));
        }
        boolean z3 = this.allowSystem;
        if (z3 != to.allowSystem) {
            d.addLine("allowSystem", Boolean.valueOf(z3), Boolean.valueOf(to.allowSystem));
        }
        boolean z4 = this.allowCalls;
        if (z4 != to.allowCalls) {
            d.addLine("allowCalls", Boolean.valueOf(z4), Boolean.valueOf(to.allowCalls));
        }
        boolean z5 = this.allowReminders;
        if (z5 != to.allowReminders) {
            d.addLine("allowReminders", Boolean.valueOf(z5), Boolean.valueOf(to.allowReminders));
        }
        boolean z6 = this.allowEvents;
        if (z6 != to.allowEvents) {
            d.addLine("allowEvents", Boolean.valueOf(z6), Boolean.valueOf(to.allowEvents));
        }
        boolean z7 = this.allowRepeatCallers;
        if (z7 != to.allowRepeatCallers) {
            d.addLine("allowRepeatCallers", Boolean.valueOf(z7), Boolean.valueOf(to.allowRepeatCallers));
        }
        boolean z8 = this.allowMessages;
        if (z8 != to.allowMessages) {
            d.addLine("allowMessages", Boolean.valueOf(z8), Boolean.valueOf(to.allowMessages));
        }
        int i2 = this.allowCallsFrom;
        if (i2 != to.allowCallsFrom) {
            d.addLine("allowCallsFrom", Integer.valueOf(i2), Integer.valueOf(to.allowCallsFrom));
        }
        int i3 = this.allowMessagesFrom;
        if (i3 != to.allowMessagesFrom) {
            d.addLine("allowMessagesFrom", Integer.valueOf(i3), Integer.valueOf(to.allowMessagesFrom));
        }
        int i4 = this.suppressedVisualEffects;
        if (i4 != to.suppressedVisualEffects) {
            d.addLine("suppressedVisualEffects", Integer.valueOf(i4), Integer.valueOf(to.suppressedVisualEffects));
        }
        ArraySet<String> allRules = new ArraySet<>();
        addKeys(allRules, this.automaticRules);
        addKeys(allRules, to.automaticRules);
        int N = allRules.size();
        for (int i5 = 0; i5 < N; i5++) {
            String rule = allRules.valueAt(i5);
            ArrayMap<String, ZenRule> arrayMap = this.automaticRules;
            ZenRule toRule = null;
            ZenRule fromRule = arrayMap != null ? arrayMap.get(rule) : null;
            ArrayMap<String, ZenRule> arrayMap2 = to.automaticRules;
            if (arrayMap2 != null) {
                toRule = arrayMap2.get(rule);
            }
            ZenRule.appendDiff(d, "automaticRule[" + rule + "]", fromRule, toRule);
        }
        ZenRule.appendDiff(d, "manualRule", this.manualRule, to.manualRule);
        boolean z9 = this.areChannelsBypassingDnd;
        if (z9 != to.areChannelsBypassingDnd) {
            d.addLine(STATE_ATT_CHANNELS_BYPASSING_DND, Boolean.valueOf(z9), Boolean.valueOf(to.areChannelsBypassingDnd));
        }
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
            for (int i = 0; i < map.size(); i++) {
                set.add(map.keyAt(i));
            }
        }
    }

    public boolean isValid() {
        if (!isValidManualRule(this.manualRule)) {
            return false;
        }
        int N = this.automaticRules.size();
        for (int i = 0; i < N; i++) {
            if (!isValidAutomaticRule(this.automaticRules.valueAt(i))) {
                return false;
            }
        }
        return true;
    }

    private static boolean isValidManualRule(ZenRule rule) {
        return rule == null || (Settings.Global.isValidZenMode(rule.zenMode) && sameCondition(rule));
    }

    private static boolean isValidAutomaticRule(ZenRule rule) {
        return rule != null && !TextUtils.isEmpty(rule.name) && Settings.Global.isValidZenMode(rule.zenMode) && rule.conditionId != null && sameCondition(rule);
    }

    private static boolean sameCondition(ZenRule rule) {
        if (rule == null) {
            return false;
        }
        if (rule.conditionId == null) {
            if (rule.condition == null) {
                return true;
            }
            return false;
        } else if (rule.condition == null || rule.conditionId.equals(rule.condition.id)) {
            return true;
        } else {
            return false;
        }
    }

    private static int[] generateMinuteBuckets() {
        int[] buckets = new int[15];
        buckets[0] = 15;
        buckets[1] = 30;
        buckets[2] = 45;
        for (int i = 1; i <= 12; i++) {
            buckets[i + 2] = i * 60;
        }
        return buckets;
    }

    public static String sourceToString(int source) {
        if (source == 0) {
            return "anyone";
        }
        if (source == 1) {
            return Contacts.AUTHORITY;
        }
        if (source != 2) {
            return IccCardConstants.INTENT_VALUE_ICC_UNKNOWN;
        }
        return "stars";
    }

    public boolean equals(Object o) {
        if (!(o instanceof ZenModeConfig)) {
            return false;
        }
        if (o == this) {
            return true;
        }
        ZenModeConfig other = (ZenModeConfig) o;
        if (other.allowAlarms == this.allowAlarms && other.allowMedia == this.allowMedia && other.allowSystem == this.allowSystem && other.allowCalls == this.allowCalls && other.allowRepeatCallers == this.allowRepeatCallers && other.allowMessages == this.allowMessages && other.allowCallsFrom == this.allowCallsFrom && other.allowMessagesFrom == this.allowMessagesFrom && other.allowReminders == this.allowReminders && other.allowEvents == this.allowEvents && other.user == this.user && Objects.equals(other.automaticRules, this.automaticRules) && Objects.equals(other.manualRule, this.manualRule) && other.suppressedVisualEffects == this.suppressedVisualEffects && other.areChannelsBypassingDnd == this.areChannelsBypassingDnd) {
            return true;
        }
        return false;
    }

    public int hashCode() {
        return Objects.hash(Boolean.valueOf(this.allowAlarms), Boolean.valueOf(this.allowMedia), Boolean.valueOf(this.allowSystem), Boolean.valueOf(this.allowCalls), Boolean.valueOf(this.allowRepeatCallers), Boolean.valueOf(this.allowMessages), Integer.valueOf(this.allowCallsFrom), Integer.valueOf(this.allowMessagesFrom), Boolean.valueOf(this.allowReminders), Boolean.valueOf(this.allowEvents), Integer.valueOf(this.user), this.automaticRules, this.manualRule, Integer.valueOf(this.suppressedVisualEffects), Boolean.valueOf(this.areChannelsBypassingDnd));
    }

    /* access modifiers changed from: private */
    public static String toDayList(int[] days) {
        if (days == null || days.length == 0) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < days.length; i++) {
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
        for (int i = 0; i < tokens.length; i++) {
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
            return Long.parseLong(value);
        } catch (NumberFormatException e) {
            return defValue;
        }
    }

    private static Long tryParseLong(String value, Long defValue) {
        if (TextUtils.isEmpty(value)) {
            return defValue;
        }
        try {
            return Long.valueOf(Long.parseLong(value));
        } catch (NumberFormatException e) {
            return defValue;
        }
    }

    public static ZenModeConfig readXml(XmlPullParser parser) throws XmlPullParserException, IOException {
        if (parser.getEventType() != 2 || !"zen".equals(parser.getName())) {
            return null;
        }
        ZenModeConfig rt = new ZenModeConfig();
        rt.version = safeInt(parser, "version", 8);
        rt.user = safeInt(parser, "user", rt.user);
        boolean readSuppressedEffects = false;
        while (true) {
            int type = parser.next();
            if (type != 1) {
                String tag = parser.getName();
                if (type == 3 && "zen".equals(tag)) {
                    return rt;
                }
                if (type == 2) {
                    if (ALLOW_TAG.equals(tag)) {
                        rt.allowCalls = safeBoolean(parser, ALLOW_ATT_CALLS, true);
                        rt.allowRepeatCallers = safeBoolean(parser, ALLOW_ATT_REPEAT_CALLERS, true);
                        rt.allowMessages = safeBoolean(parser, ALLOW_ATT_MESSAGES, false);
                        rt.allowReminders = safeBoolean(parser, ALLOW_ATT_REMINDERS, false);
                        rt.allowEvents = safeBoolean(parser, ALLOW_ATT_EVENTS, false);
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
                            rt.allowCallsFrom = 2;
                            rt.allowMessagesFrom = 1;
                        }
                        rt.allowAlarms = safeBoolean(parser, ALLOW_ATT_ALARMS, true);
                        rt.allowMedia = safeBoolean(parser, "media", true);
                        rt.allowSystem = safeBoolean(parser, "system", false);
                        Boolean allowWhenScreenOff = unsafeBoolean(parser, ALLOW_ATT_SCREEN_OFF);
                        if (allowWhenScreenOff != null) {
                            readSuppressedEffects = true;
                            if (!allowWhenScreenOff.booleanValue()) {
                                rt.suppressedVisualEffects |= 12;
                            }
                        }
                        Boolean allowWhenScreenOn = unsafeBoolean(parser, ALLOW_ATT_SCREEN_ON);
                        if (allowWhenScreenOn != null) {
                            readSuppressedEffects = true;
                            if (!allowWhenScreenOn.booleanValue()) {
                                rt.suppressedVisualEffects |= 16;
                            }
                        }
                        if (readSuppressedEffects) {
                            Slog.d(TAG, "Migrated visual effects to " + rt.suppressedVisualEffects);
                        }
                    } else if (DISALLOW_TAG.equals(tag) && !readSuppressedEffects) {
                        rt.suppressedVisualEffects = safeInt(parser, DISALLOW_ATT_VISUAL_EFFECTS, 0);
                    } else if ("manual".equals(tag)) {
                        rt.manualRule = readRuleXml(parser);
                    } else if (AUTOMATIC_TAG.equals(tag)) {
                        String id = parser.getAttributeValue(null, RULE_ATT_ID);
                        ZenRule automaticRule = readRuleXml(parser);
                        if (!(id == null || automaticRule == null)) {
                            automaticRule.id = id;
                            rt.automaticRules.put(id, automaticRule);
                        }
                    } else if ("state".equals(tag)) {
                        rt.areChannelsBypassingDnd = safeBoolean(parser, STATE_ATT_CHANNELS_BYPASSING_DND, false);
                    }
                }
            } else {
                throw new IllegalStateException("Failed to reach END_DOCUMENT");
            }
        }
    }

    public void writeXml(XmlSerializer out, Integer version2) throws IOException {
        out.startTag(null, "zen");
        out.attribute(null, "version", Integer.toString(version2 == null ? 8 : version2.intValue()));
        out.attribute(null, "user", Integer.toString(this.user));
        out.startTag(null, ALLOW_TAG);
        out.attribute(null, ALLOW_ATT_CALLS, Boolean.toString(this.allowCalls));
        out.attribute(null, ALLOW_ATT_REPEAT_CALLERS, Boolean.toString(this.allowRepeatCallers));
        out.attribute(null, ALLOW_ATT_MESSAGES, Boolean.toString(this.allowMessages));
        out.attribute(null, ALLOW_ATT_REMINDERS, Boolean.toString(this.allowReminders));
        out.attribute(null, ALLOW_ATT_EVENTS, Boolean.toString(this.allowEvents));
        out.attribute(null, ALLOW_ATT_CALLS_FROM, Integer.toString(this.allowCallsFrom));
        out.attribute(null, ALLOW_ATT_MESSAGES_FROM, Integer.toString(this.allowMessagesFrom));
        out.attribute(null, ALLOW_ATT_ALARMS, Boolean.toString(this.allowAlarms));
        out.attribute(null, "media", Boolean.toString(this.allowMedia));
        out.attribute(null, "system", Boolean.toString(this.allowSystem));
        out.endTag(null, ALLOW_TAG);
        out.startTag(null, DISALLOW_TAG);
        out.attribute(null, DISALLOW_ATT_VISUAL_EFFECTS, Integer.toString(this.suppressedVisualEffects));
        out.endTag(null, DISALLOW_TAG);
        if (this.manualRule != null) {
            out.startTag(null, "manual");
            writeRuleXml(this.manualRule, out);
            out.endTag(null, "manual");
        }
        int N = this.automaticRules.size();
        for (int i = 0; i < N; i++) {
            out.startTag(null, AUTOMATIC_TAG);
            out.attribute(null, RULE_ATT_ID, this.automaticRules.keyAt(i));
            writeRuleXml(this.automaticRules.valueAt(i), out);
            out.endTag(null, AUTOMATIC_TAG);
        }
        out.startTag(null, "state");
        out.attribute(null, STATE_ATT_CHANNELS_BYPASSING_DND, Boolean.toString(this.areChannelsBypassingDnd));
        out.endTag(null, "state");
        out.endTag(null, "zen");
    }

    public static ZenRule readRuleXml(XmlPullParser parser) {
        String str;
        ZenRule rt = new ZenRule();
        rt.enabled = safeBoolean(parser, "enabled", true);
        rt.name = parser.getAttributeValue(null, "name");
        String zen = parser.getAttributeValue(null, "zen");
        rt.zenMode = tryParseZenMode(zen, -1);
        if (rt.zenMode == -1) {
            Slog.w(TAG, "Bad zen mode in rule xml:" + zen);
            return null;
        }
        rt.conditionId = safeUri(parser, RULE_ATT_CONDITION_ID);
        rt.component = safeComponentName(parser, "component");
        rt.configurationActivity = safeComponentName(parser, RULE_ATT_CONFIG_ACTIVITY);
        if (rt.component != null) {
            str = rt.component.getPackageName();
        } else if (rt.configurationActivity != null) {
            str = rt.configurationActivity.getPackageName();
        } else {
            str = null;
        }
        rt.pkg = str;
        rt.creationTime = safeLong(parser, "creationTime", 0);
        rt.enabler = parser.getAttributeValue(null, RULE_ATT_ENABLER);
        rt.condition = readConditionXml(parser);
        if (rt.zenMode != 1 && Condition.isValidId(rt.conditionId, "android")) {
            Slog.i(TAG, "Updating zenMode of automatic rule " + rt.name);
            rt.zenMode = 1;
        }
        rt.modified = safeBoolean(parser, "modified", false);
        rt.zenPolicy = readZenPolicyXml(parser);
        return rt;
    }

    public static void writeRuleXml(ZenRule rule, XmlSerializer out) throws IOException {
        out.attribute(null, "enabled", Boolean.toString(rule.enabled));
        if (rule.name != null) {
            out.attribute(null, "name", rule.name);
        }
        out.attribute(null, "zen", Integer.toString(rule.zenMode));
        if (rule.component != null) {
            out.attribute(null, "component", rule.component.flattenToString());
        }
        if (rule.configurationActivity != null) {
            out.attribute(null, RULE_ATT_CONFIG_ACTIVITY, rule.configurationActivity.flattenToString());
        }
        if (rule.conditionId != null) {
            out.attribute(null, RULE_ATT_CONDITION_ID, rule.conditionId.toString());
        }
        out.attribute(null, "creationTime", Long.toString(rule.creationTime));
        if (rule.enabler != null) {
            out.attribute(null, RULE_ATT_ENABLER, rule.enabler);
        }
        if (rule.condition != null) {
            writeConditionXml(rule.condition, out);
        }
        if (rule.zenPolicy != null) {
            writeZenPolicyXml(rule.zenPolicy, out);
        }
        out.attribute(null, "modified", Boolean.toString(rule.modified));
    }

    public static Condition readConditionXml(XmlPullParser parser) {
        Uri id = safeUri(parser, "id");
        if (id == null) {
            return null;
        }
        try {
            return new Condition(id, parser.getAttributeValue(null, "summary"), parser.getAttributeValue(null, CONDITION_ATT_LINE1), parser.getAttributeValue(null, CONDITION_ATT_LINE2), safeInt(parser, "icon", -1), safeInt(parser, "state", -1), safeInt(parser, "flags", -1));
        } catch (IllegalArgumentException e) {
            Slog.w(TAG, "Unable to read condition xml", e);
            return null;
        }
    }

    public static void writeConditionXml(Condition c, XmlSerializer out) throws IOException {
        out.attribute(null, "id", c.id.toString());
        out.attribute(null, "summary", c.summary);
        out.attribute(null, CONDITION_ATT_LINE1, c.line1);
        out.attribute(null, CONDITION_ATT_LINE2, c.line2);
        out.attribute(null, "icon", Integer.toString(c.icon));
        out.attribute(null, "state", Integer.toString(c.state));
        out.attribute(null, "flags", Integer.toString(c.flags));
    }

    public static ZenPolicy readZenPolicyXml(XmlPullParser parser) {
        boolean policySet = false;
        ZenPolicy.Builder builder = new ZenPolicy.Builder();
        int calls = safeInt(parser, ALLOW_ATT_CALLS_FROM, 0);
        int messages = safeInt(parser, ALLOW_ATT_MESSAGES_FROM, 0);
        int repeatCallers = safeInt(parser, ALLOW_ATT_REPEAT_CALLERS, 0);
        int alarms = safeInt(parser, ALLOW_ATT_ALARMS, 0);
        int media = safeInt(parser, "media", 0);
        int system = safeInt(parser, "system", 0);
        int events = safeInt(parser, ALLOW_ATT_EVENTS, 0);
        int reminders = safeInt(parser, ALLOW_ATT_REMINDERS, 0);
        if (calls != 0) {
            builder.allowCalls(calls);
            policySet = true;
        }
        if (messages != 0) {
            builder.allowMessages(messages);
            policySet = true;
        }
        if (repeatCallers != 0) {
            builder.allowRepeatCallers(repeatCallers == 1);
            policySet = true;
        }
        if (alarms != 0) {
            builder.allowAlarms(alarms == 1);
            policySet = true;
        }
        if (media != 0) {
            builder.allowMedia(media == 1);
            policySet = true;
        }
        if (system != 0) {
            builder.allowSystem(system == 1);
            policySet = true;
        }
        if (events != 0) {
            builder.allowEvents(events == 1);
            policySet = true;
        }
        if (reminders != 0) {
            builder.allowReminders(reminders == 1);
            policySet = true;
        }
        int fullScreenIntent = safeInt(parser, SHOW_ATT_FULL_SCREEN_INTENT, 0);
        int lights = safeInt(parser, SHOW_ATT_LIGHTS, 0);
        int peek = safeInt(parser, SHOW_ATT_PEEK, 0);
        int statusBar = safeInt(parser, SHOW_ATT_STATUS_BAR_ICONS, 0);
        boolean policySet2 = policySet;
        int badges = safeInt(parser, SHOW_ATT_BADGES, 0);
        int ambient = safeInt(parser, SHOW_ATT_AMBIENT, 0);
        int notificationList = safeInt(parser, SHOW_ATT_NOTIFICATION_LIST, 0);
        if (fullScreenIntent != 0) {
            builder.showFullScreenIntent(fullScreenIntent == 1);
            policySet2 = true;
        }
        if (lights != 0) {
            builder.showLights(lights == 1);
            policySet2 = true;
        }
        if (peek != 0) {
            builder.showPeeking(peek == 1);
            policySet2 = true;
        }
        if (statusBar != 0) {
            builder.showStatusBarIcons(statusBar == 1);
            policySet2 = true;
        }
        if (badges != 0) {
            builder.showBadges(badges == 1);
            policySet2 = true;
        }
        if (ambient != 0) {
            builder.showInAmbientDisplay(ambient == 1);
            policySet2 = true;
        }
        if (notificationList != 0) {
            boolean z = true;
            if (notificationList != 1) {
                z = false;
            }
            builder.showInNotificationList(z);
            policySet2 = true;
        }
        if (policySet2) {
            return builder.build();
        }
        return null;
    }

    public static void writeZenPolicyXml(ZenPolicy policy, XmlSerializer out) throws IOException {
        writeZenPolicyState(ALLOW_ATT_CALLS_FROM, policy.getPriorityCallSenders(), out);
        writeZenPolicyState(ALLOW_ATT_MESSAGES_FROM, policy.getPriorityMessageSenders(), out);
        writeZenPolicyState(ALLOW_ATT_REPEAT_CALLERS, policy.getPriorityCategoryRepeatCallers(), out);
        writeZenPolicyState(ALLOW_ATT_ALARMS, policy.getPriorityCategoryAlarms(), out);
        writeZenPolicyState("media", policy.getPriorityCategoryMedia(), out);
        writeZenPolicyState("system", policy.getPriorityCategorySystem(), out);
        writeZenPolicyState(ALLOW_ATT_REMINDERS, policy.getPriorityCategoryReminders(), out);
        writeZenPolicyState(ALLOW_ATT_EVENTS, policy.getPriorityCategoryEvents(), out);
        writeZenPolicyState(SHOW_ATT_FULL_SCREEN_INTENT, policy.getVisualEffectFullScreenIntent(), out);
        writeZenPolicyState(SHOW_ATT_LIGHTS, policy.getVisualEffectLights(), out);
        writeZenPolicyState(SHOW_ATT_PEEK, policy.getVisualEffectPeek(), out);
        writeZenPolicyState(SHOW_ATT_STATUS_BAR_ICONS, policy.getVisualEffectStatusBar(), out);
        writeZenPolicyState(SHOW_ATT_BADGES, policy.getVisualEffectBadge(), out);
        writeZenPolicyState(SHOW_ATT_AMBIENT, policy.getVisualEffectAmbient(), out);
        writeZenPolicyState(SHOW_ATT_NOTIFICATION_LIST, policy.getVisualEffectNotificationList(), out);
    }

    private static void writeZenPolicyState(String attr, int val, XmlSerializer out) throws IOException {
        if (Objects.equals(attr, ALLOW_ATT_CALLS_FROM) || Objects.equals(attr, ALLOW_ATT_MESSAGES_FROM)) {
            if (val != 0) {
                out.attribute(null, attr, Integer.toString(val));
            }
        } else if (val != 0) {
            out.attribute(null, attr, Integer.toString(val));
        }
    }

    public static boolean isValidHour(int val) {
        return val >= 0 && val < 24;
    }

    public static boolean isValidMinute(int val) {
        return val >= 0 && val < 60;
    }

    private static boolean isValidSource(int source) {
        return source >= 0 && source <= 2;
    }

    private static Boolean unsafeBoolean(XmlPullParser parser, String att) {
        String val = parser.getAttributeValue(null, att);
        if (TextUtils.isEmpty(val)) {
            return null;
        }
        return Boolean.valueOf(Boolean.parseBoolean(val));
    }

    private static boolean safeBoolean(XmlPullParser parser, String att, boolean defValue) {
        return safeBoolean(parser.getAttributeValue(null, att), defValue);
    }

    private static boolean safeBoolean(String val, boolean defValue) {
        if (TextUtils.isEmpty(val)) {
            return defValue;
        }
        return Boolean.parseBoolean(val);
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

    @Override // android.os.Parcelable
    public int describeContents() {
        return 0;
    }

    public ZenModeConfig copy() {
        Parcel parcel = Parcel.obtain();
        try {
            writeToParcel(parcel, 0);
            parcel.setDataPosition(0);
            return new ZenModeConfig(parcel);
        } finally {
            parcel.recycle();
        }
    }

    public NotificationManager.Policy toNotificationPolicy(ZenPolicy zenPolicy) {
        NotificationManager.Policy defaultPolicy = toNotificationPolicy();
        int priorityCategories = 0;
        int suppressedVisualEffects2 = 0;
        int callSenders = defaultPolicy.priorityCallSenders;
        int messageSenders = defaultPolicy.priorityMessageSenders;
        if (zenPolicy.isCategoryAllowed(0, isPriorityCategoryEnabled(1, defaultPolicy))) {
            priorityCategories = 0 | 1;
        }
        if (zenPolicy.isCategoryAllowed(1, isPriorityCategoryEnabled(2, defaultPolicy))) {
            priorityCategories |= 2;
        }
        if (zenPolicy.isCategoryAllowed(2, isPriorityCategoryEnabled(4, defaultPolicy))) {
            priorityCategories |= 4;
            messageSenders = getNotificationPolicySenders(zenPolicy.getPriorityMessageSenders(), messageSenders);
        }
        if (zenPolicy.isCategoryAllowed(3, isPriorityCategoryEnabled(8, defaultPolicy))) {
            priorityCategories |= 8;
            callSenders = getNotificationPolicySenders(zenPolicy.getPriorityCallSenders(), callSenders);
        }
        if (zenPolicy.isCategoryAllowed(4, isPriorityCategoryEnabled(16, defaultPolicy))) {
            priorityCategories |= 16;
        }
        if (zenPolicy.isCategoryAllowed(5, isPriorityCategoryEnabled(32, defaultPolicy))) {
            priorityCategories |= 32;
        }
        if (zenPolicy.isCategoryAllowed(6, isPriorityCategoryEnabled(64, defaultPolicy))) {
            priorityCategories |= 64;
        }
        if (zenPolicy.isCategoryAllowed(7, isPriorityCategoryEnabled(128, defaultPolicy))) {
            priorityCategories |= 128;
        }
        boolean suppressFullScreenIntent = !zenPolicy.isVisualEffectAllowed(0, isVisualEffectAllowed(4, defaultPolicy));
        boolean suppressLights = !zenPolicy.isVisualEffectAllowed(1, isVisualEffectAllowed(8, defaultPolicy));
        boolean suppressAmbient = true ^ zenPolicy.isVisualEffectAllowed(5, isVisualEffectAllowed(128, defaultPolicy));
        if (suppressFullScreenIntent && suppressLights && suppressAmbient) {
            suppressedVisualEffects2 = 0 | 1;
        }
        if (suppressFullScreenIntent) {
            suppressedVisualEffects2 |= 4;
        }
        if (suppressLights) {
            suppressedVisualEffects2 |= 8;
        }
        if (!zenPolicy.isVisualEffectAllowed(2, isVisualEffectAllowed(16, defaultPolicy))) {
            suppressedVisualEffects2 = suppressedVisualEffects2 | 16 | 2;
        }
        if (!zenPolicy.isVisualEffectAllowed(3, isVisualEffectAllowed(32, defaultPolicy))) {
            suppressedVisualEffects2 |= 32;
        }
        if (!zenPolicy.isVisualEffectAllowed(4, isVisualEffectAllowed(64, defaultPolicy))) {
            suppressedVisualEffects2 |= 64;
        }
        if (suppressAmbient) {
            suppressedVisualEffects2 |= 128;
        }
        if (!zenPolicy.isVisualEffectAllowed(6, isVisualEffectAllowed(256, defaultPolicy))) {
            suppressedVisualEffects2 |= 256;
        }
        return new NotificationManager.Policy(priorityCategories, callSenders, messageSenders, suppressedVisualEffects2, defaultPolicy.state);
    }

    private boolean isPriorityCategoryEnabled(int categoryType, NotificationManager.Policy policy) {
        return (policy.priorityCategories & categoryType) != 0;
    }

    private boolean isVisualEffectAllowed(int visualEffect, NotificationManager.Policy policy) {
        return (policy.suppressedVisualEffects & visualEffect) == 0;
    }

    private int getNotificationPolicySenders(int senders, int defaultPolicySender) {
        if (senders == 1) {
            return 0;
        }
        if (senders == 2) {
            return 1;
        }
        if (senders != 3) {
            return defaultPolicySender;
        }
        return 2;
    }

    public static int getZenPolicySenders(int senders) {
        if (senders == 0) {
            return 1;
        }
        if (senders != 1) {
            return 3;
        }
        return 2;
    }

    public NotificationManager.Policy toNotificationPolicy() {
        int priorityCategories = 0;
        if (this.allowCalls) {
            priorityCategories = 0 | 8;
        }
        if (this.allowMessages) {
            priorityCategories |= 4;
        }
        if (this.allowEvents) {
            priorityCategories |= 2;
        }
        if (this.allowReminders) {
            priorityCategories |= 1;
        }
        if (this.allowRepeatCallers) {
            priorityCategories |= 16;
        }
        if (this.allowAlarms) {
            priorityCategories |= 32;
        }
        if (this.allowMedia) {
            priorityCategories |= 64;
        }
        if (this.allowSystem) {
            priorityCategories |= 128;
        }
        return new NotificationManager.Policy(priorityCategories, sourceToPrioritySenders(this.allowCallsFrom, 1), sourceToPrioritySenders(this.allowMessagesFrom, 1), this.suppressedVisualEffects, this.areChannelsBypassingDnd ? 1 : 0);
    }

    public static ScheduleCalendar toScheduleCalendar(Uri conditionId) {
        ScheduleInfo schedule = tryParseScheduleConditionId(conditionId);
        if (schedule == null || schedule.days == null || schedule.days.length == 0) {
            return null;
        }
        ScheduleCalendar sc = new ScheduleCalendar();
        sc.setSchedule(schedule);
        sc.setTimeZone(TimeZone.getDefault());
        return sc;
    }

    private static int sourceToPrioritySenders(int source, int def) {
        if (source == 0) {
            return 0;
        }
        if (source == 1) {
            return 1;
        }
        if (source != 2) {
            return def;
        }
        return 2;
    }

    private static int prioritySendersToSource(int prioritySenders, int def) {
        if (prioritySenders == 0) {
            return 0;
        }
        if (prioritySenders == 1) {
            return 1;
        }
        if (prioritySenders != 2) {
            return def;
        }
        return 2;
    }

    public void applyNotificationPolicy(NotificationManager.Policy policy) {
        if (policy != null) {
            boolean z = false;
            this.allowAlarms = (policy.priorityCategories & 32) != 0;
            this.allowMedia = (policy.priorityCategories & 64) != 0;
            this.allowSystem = (policy.priorityCategories & 128) != 0;
            this.allowEvents = (policy.priorityCategories & 2) != 0;
            this.allowReminders = (policy.priorityCategories & 1) != 0;
            this.allowCalls = (policy.priorityCategories & 8) != 0;
            this.allowMessages = (policy.priorityCategories & 4) != 0;
            this.allowRepeatCallers = (policy.priorityCategories & 16) != 0;
            this.allowCallsFrom = prioritySendersToSource(policy.priorityCallSenders, this.allowCallsFrom);
            this.allowMessagesFrom = prioritySendersToSource(policy.priorityMessageSenders, this.allowMessagesFrom);
            if (policy.suppressedVisualEffects != -1) {
                this.suppressedVisualEffects = policy.suppressedVisualEffects;
            }
            if (policy.state != -1) {
                if ((policy.state & 1) != 0) {
                    z = true;
                }
                this.areChannelsBypassingDnd = z;
            }
        }
    }

    public static Condition toTimeCondition(Context context, int minutesFromNow, int userHandle) {
        return toTimeCondition(context, minutesFromNow, userHandle, false);
    }

    public static Condition toTimeCondition(Context context, int minutesFromNow, int userHandle, boolean shortVersion) {
        return toTimeCondition(context, System.currentTimeMillis() + (minutesFromNow == 0 ? JobInfo.MIN_BACKOFF_MILLIS : (long) (60000 * minutesFromNow)), minutesFromNow, userHandle, shortVersion);
    }

    public static Condition toTimeCondition(Context context, long time, int minutes, int userHandle, boolean shortVersion) {
        String summary;
        String line2;
        String line22;
        int summaryResId;
        int line1ResId;
        int summaryResId2;
        int line1ResId2;
        CharSequence formattedTime = getFormattedTime(context, time, isToday(time), userHandle);
        Resources res = context.getResources();
        if (minutes < 60) {
            if (shortVersion) {
                summaryResId2 = R.plurals.zen_mode_duration_minutes_summary_short;
            } else {
                summaryResId2 = R.plurals.zen_mode_duration_minutes_summary;
            }
            summary = res.getQuantityString(summaryResId2, minutes, Integer.valueOf(minutes), formattedTime);
            if (shortVersion) {
                line1ResId2 = R.plurals.zen_mode_duration_minutes_short;
            } else {
                line1ResId2 = R.plurals.zen_mode_duration_minutes;
            }
            line2 = res.getQuantityString(line1ResId2, minutes, Integer.valueOf(minutes), formattedTime);
            line22 = res.getString(R.string.zen_mode_until, formattedTime);
        } else if (minutes < 1440) {
            int num = Math.round(((float) minutes) / 60.0f);
            if (shortVersion) {
                summaryResId = R.plurals.zen_mode_duration_hours_summary_short;
            } else {
                summaryResId = R.plurals.zen_mode_duration_hours_summary;
            }
            summary = res.getQuantityString(summaryResId, num, Integer.valueOf(num), formattedTime);
            if (shortVersion) {
                line1ResId = R.plurals.zen_mode_duration_hours_short;
            } else {
                line1ResId = R.plurals.zen_mode_duration_hours;
            }
            line2 = res.getQuantityString(line1ResId, num, Integer.valueOf(num), formattedTime);
            line22 = res.getString(R.string.zen_mode_until, formattedTime);
        } else {
            line22 = res.getString(R.string.zen_mode_until, formattedTime);
            summary = line22;
            line2 = line22;
        }
        return new Condition(toCountdownConditionId(time, false), summary, line2, line22, 0, 1, 1);
    }

    public static Condition toNextAlarmCondition(Context context, long alarm, int userHandle) {
        CharSequence formattedTime = getFormattedTime(context, alarm, isToday(alarm), userHandle);
        return new Condition(toCountdownConditionId(alarm, true), "", context.getResources().getString(R.string.zen_mode_until, formattedTime), "", 0, 1, 1);
    }

    public static CharSequence getFormattedTime(Context context, long time, boolean isSameDay, int userHandle) {
        StringBuilder sb = new StringBuilder();
        sb.append(!isSameDay ? "EEE " : "");
        sb.append(DateFormat.is24HourFormat(context, userHandle) ? "Hm" : "hma");
        return DateFormat.format(DateFormat.getBestDateTimePattern(Locale.getDefault(), sb.toString()), time);
    }

    public static boolean isToday(long time) {
        GregorianCalendar now = new GregorianCalendar();
        GregorianCalendar endTime = new GregorianCalendar();
        endTime.setTimeInMillis(time);
        if (now.get(1) == endTime.get(1) && now.get(2) == endTime.get(2) && now.get(5) == endTime.get(5)) {
            return true;
        }
        return false;
    }

    public static Uri toCountdownConditionId(long time, boolean alarm) {
        return new Uri.Builder().scheme(Condition.SCHEME).authority("android").appendPath(COUNTDOWN_PATH).appendPath(Long.toString(time)).appendPath("alarm").appendPath(Boolean.toString(alarm)).build();
    }

    public static long tryParseCountdownConditionId(Uri conditionId) {
        if (!Condition.isValidId(conditionId, "android") || conditionId.getPathSegments().size() < 2 || !COUNTDOWN_PATH.equals(conditionId.getPathSegments().get(0))) {
            return 0;
        }
        try {
            return Long.parseLong(conditionId.getPathSegments().get(1));
        } catch (RuntimeException e) {
            String str = TAG;
            Slog.w(str, "Error parsing countdown condition: " + conditionId, e);
            return 0;
        }
    }

    public static boolean isValidCountdownConditionId(Uri conditionId) {
        return tryParseCountdownConditionId(conditionId) != 0;
    }

    public static boolean isValidCountdownToAlarmConditionId(Uri conditionId) {
        if (tryParseCountdownConditionId(conditionId) == 0 || conditionId.getPathSegments().size() < 4 || !"alarm".equals(conditionId.getPathSegments().get(2))) {
            return false;
        }
        try {
            return Boolean.parseBoolean(conditionId.getPathSegments().get(3));
        } catch (RuntimeException e) {
            String str = TAG;
            Slog.w(str, "Error parsing countdown alarm condition: " + conditionId, e);
            return false;
        }
    }

    public static Uri toScheduleConditionId(ScheduleInfo schedule) {
        Uri.Builder appendQueryParameter = new Uri.Builder().scheme(Condition.SCHEME).authority("android").appendPath(SCHEDULE_PATH).appendQueryParameter("days", toDayList(schedule.days));
        Uri.Builder appendQueryParameter2 = appendQueryParameter.appendQueryParameter(Telephony.BaseMmsColumns.START, schedule.startHour + "." + schedule.startMinute);
        return appendQueryParameter2.appendQueryParameter("end", schedule.endHour + "." + schedule.endMinute).appendQueryParameter("exitAtAlarm", String.valueOf(schedule.exitAtAlarm)).build();
    }

    public static boolean isValidScheduleConditionId(Uri conditionId) {
        try {
            ScheduleInfo info = tryParseScheduleConditionId(conditionId);
            if (info == null || info.days == null || info.days.length == 0) {
                return false;
            }
            return true;
        } catch (ArrayIndexOutOfBoundsException | NullPointerException e) {
            return false;
        }
    }

    public static boolean isValidScheduleConditionId(Uri conditionId, boolean allowNever) {
        try {
            ScheduleInfo info = tryParseScheduleConditionId(conditionId);
            if (info != null) {
                if (allowNever) {
                    return true;
                }
                if (info.days == null || info.days.length == 0) {
                    return false;
                }
                return true;
            }
            return false;
        } catch (ArrayIndexOutOfBoundsException | NullPointerException e) {
            return false;
        }
    }

    @UnsupportedAppUsage
    public static ScheduleInfo tryParseScheduleConditionId(Uri conditionId) {
        if (!(conditionId != null && Condition.SCHEME.equals(conditionId.getScheme()) && "android".equals(conditionId.getAuthority()) && conditionId.getPathSegments().size() == 1 && SCHEDULE_PATH.equals(conditionId.getPathSegments().get(0)))) {
            return null;
        }
        int[] start = tryParseHourAndMinute(conditionId.getQueryParameter(Telephony.BaseMmsColumns.START));
        int[] end = tryParseHourAndMinute(conditionId.getQueryParameter("end"));
        if (start == null || end == null) {
            return null;
        }
        ScheduleInfo rt = new ScheduleInfo();
        rt.days = tryParseDayList(conditionId.getQueryParameter("days"), "\\.");
        rt.startHour = start[0];
        rt.startMinute = start[1];
        rt.endHour = end[0];
        rt.endMinute = end[1];
        rt.exitAtAlarm = safeBoolean(conditionId.getQueryParameter("exitAtAlarm"), false);
        return rt;
    }

    public static ComponentName getScheduleConditionProvider() {
        return new ComponentName("android", "ScheduleConditionProvider");
    }

    public static class ScheduleInfo {
        @UnsupportedAppUsage
        public int[] days;
        @UnsupportedAppUsage
        public int endHour;
        @UnsupportedAppUsage
        public int endMinute;
        public boolean exitAtAlarm;
        public long nextAlarm;
        @UnsupportedAppUsage
        public int startHour;
        @UnsupportedAppUsage
        public int startMinute;

        public int hashCode() {
            return 0;
        }

        public boolean equals(Object o) {
            if (!(o instanceof ScheduleInfo)) {
                return false;
            }
            ScheduleInfo other = (ScheduleInfo) o;
            if (ZenModeConfig.toDayList(this.days).equals(ZenModeConfig.toDayList(other.days)) && this.startHour == other.startHour && this.startMinute == other.startMinute && this.endHour == other.endHour && this.endMinute == other.endMinute && this.exitAtAlarm == other.exitAtAlarm) {
                return true;
            }
            return false;
        }

        public ScheduleInfo copy() {
            ScheduleInfo rt = new ScheduleInfo();
            int[] iArr = this.days;
            if (iArr != null) {
                rt.days = new int[iArr.length];
                int[] iArr2 = this.days;
                System.arraycopy(iArr2, 0, rt.days, 0, iArr2.length);
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
            return "ScheduleInfo{days=" + Arrays.toString(this.days) + ", startHour=" + this.startHour + ", startMinute=" + this.startMinute + ", endHour=" + this.endHour + ", endMinute=" + this.endMinute + ", exitAtAlarm=" + this.exitAtAlarm + ", nextAlarm=" + ts(this.nextAlarm) + '}';
        }

        protected static String ts(long time) {
            return new Date(time) + " (" + time + ")";
        }
    }

    public static Uri toEventConditionId(EventInfo event) {
        String str = "";
        Uri.Builder appendQueryParameter = new Uri.Builder().scheme(Condition.SCHEME).authority("android").appendPath("event").appendQueryParameter("userId", Long.toString((long) event.userId)).appendQueryParameter("calendar", event.calName != null ? event.calName : str);
        if (event.calendarId != null) {
            str = event.calendarId.toString();
        }
        return appendQueryParameter.appendQueryParameter("calendarId", str).appendQueryParameter("reply", Integer.toString(event.reply)).build();
    }

    public static boolean isValidEventConditionId(Uri conditionId) {
        return tryParseEventConditionId(conditionId) != null;
    }

    public static EventInfo tryParseEventConditionId(Uri conditionId) {
        boolean isEvent = true;
        if (conditionId == null || !Condition.SCHEME.equals(conditionId.getScheme()) || !"android".equals(conditionId.getAuthority()) || conditionId.getPathSegments().size() != 1 || !"event".equals(conditionId.getPathSegments().get(0))) {
            isEvent = false;
        }
        if (!isEvent) {
            return null;
        }
        EventInfo rt = new EventInfo();
        rt.userId = tryParseInt(conditionId.getQueryParameter("userId"), -10000);
        rt.calName = conditionId.getQueryParameter("calendar");
        if (TextUtils.isEmpty(rt.calName)) {
            rt.calName = null;
        }
        rt.calendarId = tryParseLong(conditionId.getQueryParameter("calendarId"), (Long) null);
        rt.reply = tryParseInt(conditionId.getQueryParameter("reply"), 0);
        return rt;
    }

    public static ComponentName getEventConditionProvider() {
        return new ComponentName("android", "EventConditionProvider");
    }

    public static class EventInfo {
        public static final int REPLY_ANY_EXCEPT_NO = 0;
        public static final int REPLY_YES = 2;
        public static final int REPLY_YES_OR_MAYBE = 1;
        public String calName;
        public Long calendarId;
        public int reply;
        public int userId = -10000;

        public int hashCode() {
            return Objects.hash(Integer.valueOf(this.userId), this.calName, this.calendarId, Integer.valueOf(this.reply));
        }

        public boolean equals(Object o) {
            if (!(o instanceof EventInfo)) {
                return false;
            }
            EventInfo other = (EventInfo) o;
            if (this.userId != other.userId || !Objects.equals(this.calName, other.calName) || this.reply != other.reply || !Objects.equals(this.calendarId, other.calendarId)) {
                return false;
            }
            return true;
        }

        public EventInfo copy() {
            EventInfo rt = new EventInfo();
            rt.userId = this.userId;
            rt.calName = this.calName;
            rt.reply = this.reply;
            rt.calendarId = this.calendarId;
            return rt;
        }

        public static int resolveUserId(int userId2) {
            return userId2 == -10000 ? ActivityManager.getCurrentUser() : userId2;
        }
    }

    private static int[] tryParseHourAndMinute(String value) {
        int i;
        if (TextUtils.isEmpty(value) || (i = value.indexOf(46)) < 1 || i >= value.length() - 1) {
            return null;
        }
        int hour = tryParseInt(value.substring(0, i), -1);
        int minute = tryParseInt(value.substring(i + 1), -1);
        if (!isValidHour(hour) || !isValidMinute(minute)) {
            return null;
        }
        return new int[]{hour, minute};
    }

    private static int tryParseZenMode(String value, int defValue) {
        int rt = tryParseInt(value, defValue);
        return Settings.Global.isValidZenMode(rt) ? rt : defValue;
    }

    public static String newRuleId() {
        return UUID.randomUUID().toString().replace(NativeLibraryHelper.CLEAR_ABI_OVERRIDE, "");
    }

    public static String getOwnerCaption(Context context, String owner) {
        CharSequence seq;
        PackageManager pm = context.getPackageManager();
        try {
            ApplicationInfo info = pm.getApplicationInfo(owner, 0);
            if (info == null || (seq = info.loadLabel(pm)) == null) {
                return "";
            }
            String str = seq.toString().trim();
            if (str.length() > 0) {
                return str;
            }
            return "";
        } catch (Throwable e) {
            Slog.w(TAG, "Error loading owner caption", e);
            return "";
        }
    }

    public static String getConditionSummary(Context context, ZenModeConfig config, int userHandle, boolean shortVersion) {
        return getConditionLine(context, config, userHandle, false, shortVersion);
    }

    private static String getConditionLine(Context context, ZenModeConfig config, int userHandle, boolean useLine1, boolean shortVersion) {
        String str = "";
        if (config == null) {
            return str;
        }
        String summary = "";
        ZenRule zenRule = config.manualRule;
        if (zenRule != null) {
            Uri id = zenRule.conditionId;
            if (config.manualRule.enabler != null) {
                summary = getOwnerCaption(context, config.manualRule.enabler);
            } else if (id == null) {
                summary = context.getString(R.string.zen_mode_forever);
            } else {
                long time = tryParseCountdownConditionId(id);
                Condition c = config.manualRule.condition;
                if (time > 0) {
                    c = toTimeCondition(context, time, Math.round(((float) (time - System.currentTimeMillis())) / 60000.0f), userHandle, shortVersion);
                }
                String rt = c == null ? str : useLine1 ? c.line1 : c.summary;
                if (!TextUtils.isEmpty(rt)) {
                    str = rt;
                }
                summary = str;
            }
        }
        for (ZenRule automaticRule : config.automaticRules.values()) {
            if (automaticRule.isAutomaticActive()) {
                if (summary.isEmpty()) {
                    summary = automaticRule.name;
                } else {
                    summary = context.getResources().getString(R.string.zen_mode_rule_name_combination, summary, automaticRule.name);
                }
            }
        }
        return summary;
    }

    public static class ZenRule implements Parcelable {
        public static final Parcelable.Creator<ZenRule> CREATOR = new Parcelable.Creator<ZenRule>() {
            /* class android.service.notification.ZenModeConfig.ZenRule.AnonymousClass1 */

            @Override // android.os.Parcelable.Creator
            public ZenRule createFromParcel(Parcel source) {
                return new ZenRule(source);
            }

            @Override // android.os.Parcelable.Creator
            public ZenRule[] newArray(int size) {
                return new ZenRule[size];
            }
        };
        public ComponentName component;
        public Condition condition;
        @UnsupportedAppUsage
        public Uri conditionId;
        public ComponentName configurationActivity;
        @UnsupportedAppUsage
        public long creationTime;
        @UnsupportedAppUsage
        public boolean enabled;
        public String enabler;
        public String id;
        public boolean modified;
        @UnsupportedAppUsage
        public String name;
        public String pkg;
        @UnsupportedAppUsage
        public boolean snoozing;
        @UnsupportedAppUsage
        public int zenMode;
        public ZenPolicy zenPolicy;

        public ZenRule() {
        }

        public ZenRule(Parcel source) {
            boolean z = false;
            this.enabled = source.readInt() == 1;
            this.snoozing = source.readInt() == 1;
            if (source.readInt() == 1) {
                this.name = source.readString();
            }
            this.zenMode = source.readInt();
            this.conditionId = (Uri) source.readParcelable(null);
            this.condition = (Condition) source.readParcelable(null);
            this.component = (ComponentName) source.readParcelable(null);
            this.configurationActivity = (ComponentName) source.readParcelable(null);
            if (source.readInt() == 1) {
                this.id = source.readString();
            }
            this.creationTime = source.readLong();
            if (source.readInt() == 1) {
                this.enabler = source.readString();
            }
            this.zenPolicy = (ZenPolicy) source.readParcelable(null);
            this.modified = source.readInt() == 1 ? true : z;
            this.pkg = source.readString();
        }

        @Override // android.os.Parcelable
        public int describeContents() {
            return 0;
        }

        @Override // android.os.Parcelable
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeInt(this.enabled ? 1 : 0);
            dest.writeInt(this.snoozing ? 1 : 0);
            if (this.name != null) {
                dest.writeInt(1);
                dest.writeString(this.name);
            } else {
                dest.writeInt(0);
            }
            dest.writeInt(this.zenMode);
            dest.writeParcelable(this.conditionId, 0);
            dest.writeParcelable(this.condition, 0);
            dest.writeParcelable(this.component, 0);
            dest.writeParcelable(this.configurationActivity, 0);
            if (this.id != null) {
                dest.writeInt(1);
                dest.writeString(this.id);
            } else {
                dest.writeInt(0);
            }
            dest.writeLong(this.creationTime);
            if (this.enabler != null) {
                dest.writeInt(1);
                dest.writeString(this.enabler);
            } else {
                dest.writeInt(0);
            }
            dest.writeParcelable(this.zenPolicy, 0);
            dest.writeInt(this.modified ? 1 : 0);
            dest.writeString(this.pkg);
        }

        public String toString() {
            return ZenRule.class.getSimpleName() + '[' + "id=" + this.id + ",enabled=" + String.valueOf(this.enabled).toUpperCase() + ",snoozing=" + this.snoozing + ",name=" + this.name + ",zenMode=" + Settings.Global.zenModeToString(this.zenMode) + ",conditionId=" + this.conditionId + ",condition=" + this.condition + ",pkg=" + this.pkg + ",component=" + this.component + ",configActivity=" + this.configurationActivity + ",creationTime=" + this.creationTime + ",enabler=" + this.enabler + ",zenPolicy=" + this.zenPolicy + ",modified=" + this.modified + ']';
        }

        public void writeToProto(ProtoOutputStream proto, long fieldId) {
            long token = proto.start(fieldId);
            proto.write(1138166333441L, this.id);
            proto.write(1138166333442L, this.name);
            proto.write(1112396529667L, this.creationTime);
            proto.write(1133871366148L, this.enabled);
            proto.write(1138166333445L, this.enabler);
            proto.write(1133871366150L, this.snoozing);
            proto.write(1159641169927L, this.zenMode);
            Uri uri = this.conditionId;
            if (uri != null) {
                proto.write(1138166333448L, uri.toString());
            }
            Condition condition2 = this.condition;
            if (condition2 != null) {
                condition2.writeToProto(proto, 1146756268041L);
            }
            ComponentName componentName = this.component;
            if (componentName != null) {
                componentName.writeToProto(proto, 1146756268042L);
            }
            ZenPolicy zenPolicy2 = this.zenPolicy;
            if (zenPolicy2 != null) {
                zenPolicy2.writeToProto(proto, 1146756268043L);
            }
            proto.write(1133871366156L, this.modified);
            proto.end(token);
        }

        /* access modifiers changed from: private */
        public static void appendDiff(Diff d, String item, ZenRule from, ZenRule to) {
            if (d != null) {
                if (from != null) {
                    from.appendDiff(d, item, to);
                } else if (to != null) {
                    d.addLine(item, "insert");
                }
            }
        }

        private void appendDiff(Diff d, String item, ZenRule to) {
            if (to == null) {
                d.addLine(item, "delete");
                return;
            }
            boolean z = this.enabled;
            if (z != to.enabled) {
                d.addLine(item, "enabled", Boolean.valueOf(z), Boolean.valueOf(to.enabled));
            }
            boolean z2 = this.snoozing;
            if (z2 != to.snoozing) {
                d.addLine(item, ZenModeConfig.RULE_ATT_SNOOZING, Boolean.valueOf(z2), Boolean.valueOf(to.snoozing));
            }
            if (!Objects.equals(this.name, to.name)) {
                d.addLine(item, "name", this.name, to.name);
            }
            int i = this.zenMode;
            if (i != to.zenMode) {
                d.addLine(item, "zenMode", Integer.valueOf(i), Integer.valueOf(to.zenMode));
            }
            if (!Objects.equals(this.conditionId, to.conditionId)) {
                d.addLine(item, ZenModeConfig.RULE_ATT_CONDITION_ID, this.conditionId, to.conditionId);
            }
            if (!Objects.equals(this.condition, to.condition)) {
                d.addLine(item, Condition.SCHEME, this.condition, to.condition);
            }
            if (!Objects.equals(this.component, to.component)) {
                d.addLine(item, "component", this.component, to.component);
            }
            if (!Objects.equals(this.configurationActivity, to.configurationActivity)) {
                d.addLine(item, ZenModeConfig.RULE_ATT_CONFIG_ACTIVITY, this.configurationActivity, to.configurationActivity);
            }
            if (!Objects.equals(this.id, to.id)) {
                d.addLine(item, "id", this.id, to.id);
            }
            long j = this.creationTime;
            if (j != to.creationTime) {
                d.addLine(item, "creationTime", Long.valueOf(j), Long.valueOf(to.creationTime));
            }
            if (!Objects.equals(this.enabler, to.enabler)) {
                d.addLine(item, ZenModeConfig.RULE_ATT_ENABLER, this.enabler, to.enabler);
            }
            if (!Objects.equals(this.zenPolicy, to.zenPolicy)) {
                d.addLine(item, "zenPolicy", this.zenPolicy, to.zenPolicy);
            }
            boolean z3 = this.modified;
            if (z3 != to.modified) {
                d.addLine(item, "modified", Boolean.valueOf(z3), Boolean.valueOf(to.modified));
            }
            String str = this.pkg;
            String str2 = to.pkg;
            if (str != str2) {
                d.addLine(item, "pkg", str, str2);
            }
        }

        public boolean equals(Object o) {
            if (!(o instanceof ZenRule)) {
                return false;
            }
            if (o == this) {
                return true;
            }
            ZenRule other = (ZenRule) o;
            if (other.enabled != this.enabled || other.snoozing != this.snoozing || !Objects.equals(other.name, this.name) || other.zenMode != this.zenMode || !Objects.equals(other.conditionId, this.conditionId) || !Objects.equals(other.condition, this.condition) || !Objects.equals(other.component, this.component) || !Objects.equals(other.configurationActivity, this.configurationActivity) || !Objects.equals(other.id, this.id) || !Objects.equals(other.enabler, this.enabler) || !Objects.equals(other.zenPolicy, this.zenPolicy) || !Objects.equals(other.pkg, this.pkg) || other.modified != this.modified) {
                return false;
            }
            return true;
        }

        public int hashCode() {
            return Objects.hash(Boolean.valueOf(this.enabled), Boolean.valueOf(this.snoozing), this.name, Integer.valueOf(this.zenMode), this.conditionId, this.condition, this.component, this.configurationActivity, this.pkg, this.id, this.enabler, this.zenPolicy, Boolean.valueOf(this.modified));
        }

        public boolean isAutomaticActive() {
            return this.enabled && !this.snoozing && this.pkg != null && isTrueOrUnknown();
        }

        public boolean isTrueOrUnknown() {
            Condition condition2 = this.condition;
            return condition2 != null && (condition2.state == 1 || this.condition.state == 2);
        }
    }

    public static class Diff {
        private final ArrayList<String> lines = new ArrayList<>();

        public String toString() {
            StringBuilder sb = new StringBuilder("Diff[");
            int N = this.lines.size();
            for (int i = 0; i < N; i++) {
                if (i > 0) {
                    sb.append(",\n");
                }
                sb.append(this.lines.get(i));
            }
            sb.append(']');
            return sb.toString();
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private Diff addLine(String item, String action) {
            ArrayList<String> arrayList = this.lines;
            arrayList.add(item + SettingsStringUtil.DELIMITER + action);
            return this;
        }

        public Diff addLine(String item, String subitem, Object from, Object to) {
            return addLine(item + "." + subitem, from, to);
        }

        public Diff addLine(String item, Object from, Object to) {
            return addLine(item, from + Session.SUBSESSION_SEPARATION_CHAR + to);
        }
    }

    public static boolean areAllPriorityOnlyNotificationZenSoundsMuted(NotificationManager.Policy policy) {
        return !((policy.priorityCategories & 1) != 0) && !((policy.priorityCategories & 8) != 0) && !((policy.priorityCategories & 4) != 0) && !((policy.priorityCategories & 2) != 0) && !((policy.priorityCategories & 16) != 0) && !((policy.state & 1) != 0);
    }

    public static boolean areAllZenBehaviorSoundsMuted(NotificationManager.Policy policy) {
        return !((policy.priorityCategories & 32) != 0) && !((policy.priorityCategories & 64) != 0) && !((policy.priorityCategories & 128) != 0) && areAllPriorityOnlyNotificationZenSoundsMuted(policy);
    }

    public static boolean isZenOverridingRinger(int zen, NotificationManager.Policy consolidatedPolicy) {
        if (zen == 2 || zen == 3) {
            return true;
        }
        if (zen != 1 || !areAllPriorityOnlyNotificationZenSoundsMuted(consolidatedPolicy)) {
            return false;
        }
        return true;
    }

    public static boolean areAllPriorityOnlyNotificationZenSoundsMuted(ZenModeConfig config) {
        return !config.allowReminders && !config.allowCalls && !config.allowMessages && !config.allowEvents && !config.allowRepeatCallers && !config.areChannelsBypassingDnd;
    }

    public static boolean areAllZenBehaviorSoundsMuted(ZenModeConfig config) {
        return !config.allowAlarms && !config.allowMedia && !config.allowSystem && areAllPriorityOnlyNotificationZenSoundsMuted(config);
    }

    public static String getDescription(Context context, boolean zenOn, ZenModeConfig config, boolean describeForeverCondition) {
        if (!zenOn || config == null) {
            return null;
        }
        String secondaryText = "";
        long latestEndTime = -1;
        ZenRule zenRule = config.manualRule;
        if (zenRule != null) {
            Uri id = zenRule.conditionId;
            if (config.manualRule.enabler != null) {
                String appName = getOwnerCaption(context, config.manualRule.enabler);
                if (!appName.isEmpty()) {
                    secondaryText = appName;
                }
            } else if (id != null) {
                latestEndTime = tryParseCountdownConditionId(id);
                if (latestEndTime > 0) {
                    secondaryText = context.getString(R.string.zen_mode_until, getFormattedTime(context, latestEndTime, isToday(latestEndTime), context.getUserId()));
                }
            } else if (describeForeverCondition) {
                return context.getString(R.string.zen_mode_forever);
            } else {
                return null;
            }
        }
        for (ZenRule automaticRule : config.automaticRules.values()) {
            if (automaticRule.isAutomaticActive()) {
                if (!(isValidEventConditionId(automaticRule.conditionId) || isValidScheduleConditionId(automaticRule.conditionId))) {
                    return automaticRule.name;
                }
                long endTime = parseAutomaticRuleEndTime(context, automaticRule.conditionId);
                if (endTime > latestEndTime) {
                    latestEndTime = endTime;
                    secondaryText = automaticRule.name;
                }
            }
        }
        if (!secondaryText.equals("")) {
            return secondaryText;
        }
        return null;
    }

    private static long parseAutomaticRuleEndTime(Context context, Uri id) {
        if (isValidEventConditionId(id)) {
            return Long.MAX_VALUE;
        }
        if (!isValidScheduleConditionId(id)) {
            return -1;
        }
        ScheduleCalendar schedule = toScheduleCalendar(id);
        long endTimeMs = schedule.getNextChangeTime(System.currentTimeMillis());
        if (schedule.exitAtAlarm()) {
            long nextAlarm = getNextAlarm(context);
            schedule.maybeSetNextAlarm(System.currentTimeMillis(), nextAlarm);
            if (schedule.shouldExitForAlarm(endTimeMs)) {
                return nextAlarm;
            }
        }
        return endTimeMs;
    }

    private static long getNextAlarm(Context context) {
        AlarmManager.AlarmClockInfo info = ((AlarmManager) context.getSystemService("alarm")).getNextAlarmClock(context.getUserId());
        if (info != null) {
            return info.getTriggerTime();
        }
        return 0;
    }
}
