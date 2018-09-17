package android.app;

import android.media.AudioAttributes;
import android.media.AudioAttributes.Builder;
import android.net.ProxyInfo;
import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import android.provider.Settings.System;
import android.service.notification.NotificationListenerService.Ranking;
import android.text.TextUtils;
import java.io.IOException;
import java.util.Arrays;
import org.json.JSONException;
import org.json.JSONObject;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlSerializer;

public final class NotificationChannel implements Parcelable {
    private static final String ATT_BLOCKABLE_SYSTEM = "blockable_system";
    private static final String ATT_CONTENT_TYPE = "content_type";
    private static final String ATT_DELETED = "deleted";
    private static final String ATT_DESC = "desc";
    private static final String ATT_FLAGS = "flags";
    private static final String ATT_GROUP = "group";
    private static final String ATT_ID = "id";
    private static final String ATT_IMPORTANCE = "importance";
    private static final String ATT_LIGHTS = "lights";
    private static final String ATT_LIGHT_COLOR = "light_color";
    private static final String ATT_NAME = "name";
    private static final String ATT_PRIORITY = "priority";
    private static final String ATT_SHOW_BADGE = "show_badge";
    private static final String ATT_SOUND = "sound";
    private static final String ATT_USAGE = "usage";
    private static final String ATT_USER_LOCKED = "locked";
    private static final String ATT_VIBRATION = "vibration";
    private static final String ATT_VIBRATION_ENABLED = "vibration_enabled";
    private static final String ATT_VISIBILITY = "visibility";
    public static final Creator<NotificationChannel> CREATOR = new Creator<NotificationChannel>() {
        public NotificationChannel createFromParcel(Parcel in) {
            return new NotificationChannel(in);
        }

        public NotificationChannel[] newArray(int size) {
            return new NotificationChannel[size];
        }
    };
    public static final String DEFAULT_CHANNEL_ID = "miscellaneous";
    private static final boolean DEFAULT_DELETED = false;
    private static final int DEFAULT_IMPORTANCE = -1000;
    private static final int DEFAULT_LIGHT_COLOR = 0;
    private static final boolean DEFAULT_SHOW_BADGE = true;
    private static final int DEFAULT_VISIBILITY = -1000;
    private static final String DELIMITER = ",";
    public static final int[] LOCKABLE_FIELDS = new int[]{1, 2, 4, 8, 16, 32, 128};
    private static final int MAX_TEXT_LENGTH = 1000;
    private static final String TAG_CHANNEL = "channel";
    public static final int USER_LOCKED_IMPORTANCE = 4;
    public static final int USER_LOCKED_LIGHTS = 8;
    public static final int USER_LOCKED_PRIORITY = 1;
    public static final int USER_LOCKED_SHOW_BADGE = 128;
    public static final int USER_LOCKED_SOUND = 32;
    public static final int USER_LOCKED_VIBRATION = 16;
    public static final int USER_LOCKED_VISIBILITY = 2;
    private AudioAttributes mAudioAttributes = Notification.AUDIO_ATTRIBUTES_DEFAULT;
    private boolean mBlockableSystem = false;
    private boolean mBypassDnd;
    private boolean mDeleted = false;
    private String mDesc;
    private String mGroup;
    private final String mId;
    private int mImportance = -1000;
    private int mLightColor = 0;
    private boolean mLights;
    private int mLockscreenVisibility = -1000;
    private String mName;
    private boolean mShowBadge = true;
    private Uri mSound = System.DEFAULT_NOTIFICATION_URI;
    private int mUserLockedFields;
    private long[] mVibration;
    private boolean mVibrationEnabled;

    public NotificationChannel(String id, CharSequence name, int importance) {
        String str = null;
        this.mId = getTrimmedString(id);
        if (name != null) {
            str = getTrimmedString(name.toString());
        }
        this.mName = str;
        this.mImportance = importance;
    }

    protected NotificationChannel(Parcel in) {
        boolean z;
        AudioAttributes audioAttributes;
        boolean z2 = true;
        if (in.readByte() != (byte) 0) {
            this.mId = in.readString();
        } else {
            this.mId = null;
        }
        if (in.readByte() != (byte) 0) {
            this.mName = in.readString();
        } else {
            this.mName = null;
        }
        if (in.readByte() != (byte) 0) {
            this.mDesc = in.readString();
        } else {
            this.mDesc = null;
        }
        this.mImportance = in.readInt();
        if (in.readByte() != (byte) 0) {
            z = true;
        } else {
            z = false;
        }
        this.mBypassDnd = z;
        this.mLockscreenVisibility = in.readInt();
        if (in.readByte() != (byte) 0) {
            this.mSound = (Uri) Uri.CREATOR.createFromParcel(in);
        } else {
            this.mSound = null;
        }
        if (in.readByte() != (byte) 0) {
            z = true;
        } else {
            z = false;
        }
        this.mLights = z;
        this.mVibration = in.createLongArray();
        this.mUserLockedFields = in.readInt();
        if (in.readByte() != (byte) 0) {
            z = true;
        } else {
            z = false;
        }
        this.mVibrationEnabled = z;
        if (in.readByte() != (byte) 0) {
            z = true;
        } else {
            z = false;
        }
        this.mShowBadge = z;
        if (in.readByte() == (byte) 0) {
            z2 = false;
        }
        this.mDeleted = z2;
        if (in.readByte() != (byte) 0) {
            this.mGroup = in.readString();
        } else {
            this.mGroup = null;
        }
        if (in.readInt() > 0) {
            audioAttributes = (AudioAttributes) AudioAttributes.CREATOR.createFromParcel(in);
        } else {
            audioAttributes = null;
        }
        this.mAudioAttributes = audioAttributes;
        this.mLightColor = in.readInt();
        this.mBlockableSystem = in.readBoolean();
    }

    public void writeToParcel(Parcel dest, int flags) {
        byte b;
        if (this.mId != null) {
            dest.writeByte((byte) 1);
            dest.writeString(this.mId);
        } else {
            dest.writeByte((byte) 0);
        }
        if (this.mName != null) {
            dest.writeByte((byte) 1);
            dest.writeString(this.mName);
        } else {
            dest.writeByte((byte) 0);
        }
        if (this.mDesc != null) {
            dest.writeByte((byte) 1);
            dest.writeString(this.mDesc);
        } else {
            dest.writeByte((byte) 0);
        }
        dest.writeInt(this.mImportance);
        if (this.mBypassDnd) {
            b = (byte) 1;
        } else {
            b = (byte) 0;
        }
        dest.writeByte(b);
        dest.writeInt(this.mLockscreenVisibility);
        if (this.mSound != null) {
            dest.writeByte((byte) 1);
            this.mSound.writeToParcel(dest, 0);
        } else {
            dest.writeByte((byte) 0);
        }
        if (this.mLights) {
            b = (byte) 1;
        } else {
            b = (byte) 0;
        }
        dest.writeByte(b);
        dest.writeLongArray(this.mVibration);
        dest.writeInt(this.mUserLockedFields);
        if (this.mVibrationEnabled) {
            b = (byte) 1;
        } else {
            b = (byte) 0;
        }
        dest.writeByte(b);
        if (this.mShowBadge) {
            b = (byte) 1;
        } else {
            b = (byte) 0;
        }
        dest.writeByte(b);
        if (this.mDeleted) {
            b = (byte) 1;
        } else {
            b = (byte) 0;
        }
        dest.writeByte(b);
        if (this.mGroup != null) {
            dest.writeByte((byte) 1);
            dest.writeString(this.mGroup);
        } else {
            dest.writeByte((byte) 0);
        }
        if (this.mAudioAttributes != null) {
            dest.writeInt(1);
            this.mAudioAttributes.writeToParcel(dest, 0);
        } else {
            dest.writeInt(0);
        }
        dest.writeInt(this.mLightColor);
        dest.writeBoolean(this.mBlockableSystem);
    }

    public void lockFields(int field) {
        this.mUserLockedFields |= field;
    }

    public void unlockFields(int field) {
        this.mUserLockedFields &= ~field;
    }

    public void setDeleted(boolean deleted) {
        this.mDeleted = deleted;
    }

    public void setBlockableSystem(boolean blockableSystem) {
        this.mBlockableSystem = blockableSystem;
    }

    public void setName(CharSequence name) {
        String str = null;
        if (name != null) {
            str = getTrimmedString(name.toString());
        }
        this.mName = str;
    }

    public void setDescription(String description) {
        this.mDesc = getTrimmedString(description);
    }

    private String getTrimmedString(String input) {
        if (input == null || input.length() <= 1000) {
            return input;
        }
        return input.substring(0, 1000);
    }

    public void setGroup(String groupId) {
        this.mGroup = groupId;
    }

    public void setShowBadge(boolean showBadge) {
        this.mShowBadge = showBadge;
    }

    public void setSound(Uri sound, AudioAttributes audioAttributes) {
        this.mSound = sound;
        this.mAudioAttributes = audioAttributes;
    }

    public void enableLights(boolean lights) {
        this.mLights = lights;
    }

    public void setLightColor(int argb) {
        this.mLightColor = argb;
    }

    public void enableVibration(boolean vibration) {
        this.mVibrationEnabled = vibration;
    }

    public void setVibrationPattern(long[] vibrationPattern) {
        boolean z = false;
        if (vibrationPattern != null && vibrationPattern.length > 0) {
            z = true;
        }
        this.mVibrationEnabled = z;
        this.mVibration = vibrationPattern;
    }

    public void setImportance(int importance) {
        this.mImportance = importance;
    }

    public void setBypassDnd(boolean bypassDnd) {
        this.mBypassDnd = bypassDnd;
    }

    public void setLockscreenVisibility(int lockscreenVisibility) {
        this.mLockscreenVisibility = lockscreenVisibility;
    }

    public String getId() {
        return this.mId;
    }

    public CharSequence getName() {
        return this.mName;
    }

    public String getDescription() {
        return this.mDesc;
    }

    public int getImportance() {
        return this.mImportance;
    }

    public boolean canBypassDnd() {
        return this.mBypassDnd;
    }

    public Uri getSound() {
        return this.mSound;
    }

    public AudioAttributes getAudioAttributes() {
        return this.mAudioAttributes;
    }

    public boolean shouldShowLights() {
        return this.mLights;
    }

    public int getLightColor() {
        return this.mLightColor;
    }

    public boolean shouldVibrate() {
        return this.mVibrationEnabled;
    }

    public long[] getVibrationPattern() {
        return this.mVibration;
    }

    public int getLockscreenVisibility() {
        return this.mLockscreenVisibility;
    }

    public boolean canShowBadge() {
        return this.mShowBadge;
    }

    public String getGroup() {
        return this.mGroup;
    }

    public boolean isDeleted() {
        return this.mDeleted;
    }

    public int getUserLockedFields() {
        return this.mUserLockedFields;
    }

    public boolean isBlockableSystem() {
        return this.mBlockableSystem;
    }

    public void populateFromXml(XmlPullParser parser) {
        setDescription(parser.getAttributeValue(null, ATT_DESC));
        setBypassDnd(safeInt(parser, "priority", 0) != 0);
        setLockscreenVisibility(safeInt(parser, ATT_VISIBILITY, -1000));
        setSound(safeUri(parser, ATT_SOUND), safeAudioAttributes(parser));
        enableLights(safeBool(parser, ATT_LIGHTS, false));
        setLightColor(safeInt(parser, ATT_LIGHT_COLOR, 0));
        setVibrationPattern(safeLongArray(parser, ATT_VIBRATION, null));
        enableVibration(safeBool(parser, ATT_VIBRATION_ENABLED, false));
        setShowBadge(safeBool(parser, ATT_SHOW_BADGE, false));
        setDeleted(safeBool(parser, ATT_DELETED, false));
        setGroup(parser.getAttributeValue(null, "group"));
        lockFields(safeInt(parser, "locked", 0));
        setBlockableSystem(safeBool(parser, ATT_BLOCKABLE_SYSTEM, false));
    }

    public void writeXml(XmlSerializer out) throws IOException {
        out.startTag(null, "channel");
        out.attribute(null, "id", getId());
        if (getName() != null) {
            out.attribute(null, "name", getName().toString());
        }
        if (getDescription() != null) {
            out.attribute(null, ATT_DESC, getDescription());
        }
        if (getImportance() != -1000) {
            out.attribute(null, ATT_IMPORTANCE, Integer.toString(getImportance()));
        }
        if (canBypassDnd()) {
            out.attribute(null, "priority", Integer.toString(2));
        }
        if (getLockscreenVisibility() != -1000) {
            out.attribute(null, ATT_VISIBILITY, Integer.toString(getLockscreenVisibility()));
        }
        if (getSound() != null) {
            out.attribute(null, ATT_SOUND, getSound().toString());
        }
        if (getAudioAttributes() != null) {
            out.attribute(null, ATT_USAGE, Integer.toString(getAudioAttributes().getUsage()));
            out.attribute(null, ATT_CONTENT_TYPE, Integer.toString(getAudioAttributes().getContentType()));
            out.attribute(null, ATT_FLAGS, Integer.toString(getAudioAttributes().getFlags()));
        }
        if (shouldShowLights()) {
            out.attribute(null, ATT_LIGHTS, Boolean.toString(shouldShowLights()));
        }
        if (getLightColor() != 0) {
            out.attribute(null, ATT_LIGHT_COLOR, Integer.toString(getLightColor()));
        }
        if (shouldVibrate()) {
            out.attribute(null, ATT_VIBRATION_ENABLED, Boolean.toString(shouldVibrate()));
        }
        if (getVibrationPattern() != null) {
            out.attribute(null, ATT_VIBRATION, longArrayToString(getVibrationPattern()));
        }
        if (getUserLockedFields() != 0) {
            out.attribute(null, "locked", Integer.toString(getUserLockedFields()));
        }
        if (canShowBadge()) {
            out.attribute(null, ATT_SHOW_BADGE, Boolean.toString(canShowBadge()));
        }
        if (isDeleted()) {
            out.attribute(null, ATT_DELETED, Boolean.toString(isDeleted()));
        }
        if (getGroup() != null) {
            out.attribute(null, "group", getGroup());
        }
        if (isBlockableSystem()) {
            out.attribute(null, ATT_BLOCKABLE_SYSTEM, Boolean.toString(isBlockableSystem()));
        }
        out.endTag(null, "channel");
    }

    public JSONObject toJson() throws JSONException {
        JSONObject record = new JSONObject();
        record.put("id", getId());
        record.put("name", getName());
        record.put(ATT_DESC, getDescription());
        if (getImportance() != -1000) {
            record.put(ATT_IMPORTANCE, Ranking.importanceToString(getImportance()));
        }
        if (canBypassDnd()) {
            record.put("priority", 2);
        }
        if (getLockscreenVisibility() != -1000) {
            record.put(ATT_VISIBILITY, Notification.visibilityToString(getLockscreenVisibility()));
        }
        if (getSound() != null) {
            record.put(ATT_SOUND, getSound().toString());
        }
        if (getAudioAttributes() != null) {
            record.put(ATT_USAGE, Integer.toString(getAudioAttributes().getUsage()));
            record.put(ATT_CONTENT_TYPE, Integer.toString(getAudioAttributes().getContentType()));
            record.put(ATT_FLAGS, Integer.toString(getAudioAttributes().getFlags()));
        }
        record.put(ATT_LIGHTS, Boolean.toString(shouldShowLights()));
        record.put(ATT_LIGHT_COLOR, Integer.toString(getLightColor()));
        record.put(ATT_VIBRATION_ENABLED, Boolean.toString(shouldVibrate()));
        record.put("locked", Integer.toString(getUserLockedFields()));
        record.put(ATT_VIBRATION, longArrayToString(getVibrationPattern()));
        record.put(ATT_SHOW_BADGE, Boolean.toString(canShowBadge()));
        record.put(ATT_DELETED, Boolean.toString(isDeleted()));
        record.put("group", getGroup());
        record.put(ATT_BLOCKABLE_SYSTEM, isBlockableSystem());
        return record;
    }

    private static AudioAttributes safeAudioAttributes(XmlPullParser parser) {
        int usage = safeInt(parser, ATT_USAGE, 5);
        int contentType = safeInt(parser, ATT_CONTENT_TYPE, 4);
        return new Builder().setUsage(usage).setContentType(contentType).setFlags(safeInt(parser, ATT_FLAGS, 0)).build();
    }

    private static Uri safeUri(XmlPullParser parser, String att) {
        String val = parser.getAttributeValue(null, att);
        if (val == null) {
            return null;
        }
        return Uri.parse(val);
    }

    private static int safeInt(XmlPullParser parser, String att, int defValue) {
        return tryParseInt(parser.getAttributeValue(null, att), defValue);
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

    private static boolean safeBool(XmlPullParser parser, String att, boolean defValue) {
        String value = parser.getAttributeValue(null, att);
        if (TextUtils.isEmpty(value)) {
            return defValue;
        }
        return Boolean.parseBoolean(value);
    }

    private static long[] safeLongArray(XmlPullParser parser, String att, long[] defValue) {
        String attributeValue = parser.getAttributeValue(null, att);
        if (TextUtils.isEmpty(attributeValue)) {
            return defValue;
        }
        String[] values = attributeValue.split(DELIMITER);
        long[] longValues = new long[values.length];
        for (int i = 0; i < values.length; i++) {
            try {
                longValues[i] = Long.parseLong(values[i]);
            } catch (NumberFormatException e) {
                longValues[i] = 0;
            }
        }
        return longValues;
    }

    private static String longArrayToString(long[] values) {
        StringBuffer sb = new StringBuffer();
        if (values != null) {
            for (int i = 0; i < values.length - 1; i++) {
                sb.append(values[i]).append(DELIMITER);
            }
            sb.append(values[values.length - 1]);
        }
        return sb.toString();
    }

    public int describeContents() {
        return 0;
    }

    public boolean equals(Object o) {
        boolean z = true;
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        NotificationChannel that = (NotificationChannel) o;
        if (getImportance() != that.getImportance() || this.mBypassDnd != that.mBypassDnd || getLockscreenVisibility() != that.getLockscreenVisibility() || this.mLights != that.mLights || getLightColor() != that.getLightColor() || getUserLockedFields() != that.getUserLockedFields() || this.mVibrationEnabled != that.mVibrationEnabled || this.mShowBadge != that.mShowBadge || isDeleted() != that.isDeleted() || isBlockableSystem() != that.isBlockableSystem()) {
            return false;
        }
        if (getId() == null ? that.getId() != null : (getId().equals(that.getId()) ^ 1) != 0) {
            return false;
        }
        if (getName() == null ? that.getName() != null : (getName().equals(that.getName()) ^ 1) != 0) {
            return false;
        }
        if (getDescription() == null ? that.getDescription() != null : (getDescription().equals(that.getDescription()) ^ 1) != 0) {
            return false;
        }
        if (getSound() == null ? that.getSound() != null : (getSound().equals(that.getSound()) ^ 1) != 0) {
            return false;
        }
        if (!Arrays.equals(this.mVibration, that.mVibration)) {
            return false;
        }
        if (getGroup() == null ? that.getGroup() != null : (getGroup().equals(that.getGroup()) ^ 1) != 0) {
            return false;
        }
        if (getAudioAttributes() != null) {
            z = getAudioAttributes().equals(that.getAudioAttributes());
        } else if (that.getAudioAttributes() != null) {
            z = false;
        }
        return z;
    }

    public int hashCode() {
        int hashCode;
        int i = 1;
        int hashCode2 = (getId() != null ? getId().hashCode() : 0) * 31;
        if (getName() != null) {
            hashCode = getName().hashCode();
        } else {
            hashCode = 0;
        }
        hashCode2 = (hashCode2 + hashCode) * 31;
        if (getDescription() != null) {
            hashCode = getDescription().hashCode();
        } else {
            hashCode = 0;
        }
        hashCode2 = (((hashCode2 + hashCode) * 31) + getImportance()) * 31;
        if (this.mBypassDnd) {
            hashCode = 1;
        } else {
            hashCode = 0;
        }
        hashCode2 = (((hashCode2 + hashCode) * 31) + getLockscreenVisibility()) * 31;
        if (getSound() != null) {
            hashCode = getSound().hashCode();
        } else {
            hashCode = 0;
        }
        hashCode2 = (hashCode2 + hashCode) * 31;
        if (this.mLights) {
            hashCode = 1;
        } else {
            hashCode = 0;
        }
        hashCode2 = (((((((hashCode2 + hashCode) * 31) + getLightColor()) * 31) + Arrays.hashCode(this.mVibration)) * 31) + getUserLockedFields()) * 31;
        if (this.mVibrationEnabled) {
            hashCode = 1;
        } else {
            hashCode = 0;
        }
        hashCode2 = (hashCode2 + hashCode) * 31;
        if (this.mShowBadge) {
            hashCode = 1;
        } else {
            hashCode = 0;
        }
        hashCode2 = (hashCode2 + hashCode) * 31;
        if (isDeleted()) {
            hashCode = 1;
        } else {
            hashCode = 0;
        }
        hashCode2 = (hashCode2 + hashCode) * 31;
        if (getGroup() != null) {
            hashCode = getGroup().hashCode();
        } else {
            hashCode = 0;
        }
        hashCode2 = (hashCode2 + hashCode) * 31;
        if (getAudioAttributes() != null) {
            hashCode = getAudioAttributes().hashCode();
        } else {
            hashCode = 0;
        }
        hashCode = (hashCode2 + hashCode) * 31;
        if (!isBlockableSystem()) {
            i = 0;
        }
        return hashCode + i;
    }

    public String toString() {
        return "NotificationChannel{mId='" + this.mId + '\'' + ", mName=" + this.mName + ", mDescription=" + (!TextUtils.isEmpty(this.mDesc) ? "hasDescription " : ProxyInfo.LOCAL_EXCL_LIST) + ", mImportance=" + this.mImportance + ", mBypassDnd=" + this.mBypassDnd + ", mLockscreenVisibility=" + this.mLockscreenVisibility + ", mSound=" + this.mSound + ", mLights=" + this.mLights + ", mLightColor=" + this.mLightColor + ", mVibration=" + Arrays.toString(this.mVibration) + ", mUserLockedFields=" + this.mUserLockedFields + ", mVibrationEnabled=" + this.mVibrationEnabled + ", mShowBadge=" + this.mShowBadge + ", mDeleted=" + this.mDeleted + ", mGroup='" + this.mGroup + '\'' + ", mAudioAttributes=" + this.mAudioAttributes + ", mBlockableSystem=" + this.mBlockableSystem + '}';
    }
}
