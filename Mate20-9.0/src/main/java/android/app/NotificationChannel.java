package android.app;

import android.annotation.SystemApi;
import android.content.ContentResolver;
import android.content.Context;
import android.media.AudioAttributes;
import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;
import android.provider.Settings;
import android.service.notification.NotificationListenerService;
import android.text.TextUtils;
import android.util.proto.ProtoOutputStream;
import com.android.internal.util.Preconditions;
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
    private static final String ATT_FG_SERVICE_SHOWN = "fgservice";
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
    public static final Parcelable.Creator<NotificationChannel> CREATOR = new Parcelable.Creator<NotificationChannel>() {
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
    public static final int[] LOCKABLE_FIELDS = {1, 2, 4, 8, 16, 32, 128};
    private static final int MAX_TEXT_LENGTH = 1000;
    private static final String TAG_CHANNEL = "channel";
    public static final int USER_LOCKED_IMPORTANCE = 4;
    public static final int USER_LOCKED_LIGHTS = 8;
    public static final int USER_LOCKED_PRIORITY = 1;
    public static final int USER_LOCKED_SHOW_BADGE = 128;
    public static final int USER_LOCKED_SOUND = 32;
    public static final int USER_LOCKED_VIBRATION = 16;
    public static final int USER_LOCKED_VISIBILITY = 2;
    private AudioAttributes mAudioAttributes;
    private boolean mBlockableSystem;
    private boolean mBypassDnd;
    private boolean mDeleted;
    private String mDesc;
    private boolean mFgServiceShown;
    private String mGroup;
    private final String mId;
    private int mImportance;
    private int mLightColor;
    private boolean mLights;
    private int mLockscreenVisibility;
    private String mName;
    private boolean mShowBadge;
    private Uri mSound;
    private int mUserLockedFields;
    private long[] mVibration;
    private boolean mVibrationEnabled;

    public NotificationChannel(String id, CharSequence name, int importance) {
        this.mImportance = -1000;
        this.mLockscreenVisibility = -1000;
        this.mSound = Settings.System.DEFAULT_NOTIFICATION_URI;
        this.mLightColor = 0;
        this.mShowBadge = true;
        this.mDeleted = false;
        this.mAudioAttributes = Notification.AUDIO_ATTRIBUTES_DEFAULT;
        this.mBlockableSystem = false;
        this.mId = getTrimmedString(id);
        this.mName = name != null ? getTrimmedString(name.toString()) : null;
        this.mImportance = importance;
    }

    protected NotificationChannel(Parcel in) {
        this.mImportance = -1000;
        this.mLockscreenVisibility = -1000;
        this.mSound = Settings.System.DEFAULT_NOTIFICATION_URI;
        boolean z = false;
        this.mLightColor = 0;
        this.mShowBadge = true;
        this.mDeleted = false;
        this.mAudioAttributes = Notification.AUDIO_ATTRIBUTES_DEFAULT;
        this.mBlockableSystem = false;
        AudioAttributes audioAttributes = null;
        if (in.readByte() != 0) {
            this.mId = in.readString();
        } else {
            this.mId = null;
        }
        if (in.readByte() != 0) {
            this.mName = in.readString();
        } else {
            this.mName = null;
        }
        if (in.readByte() != 0) {
            this.mDesc = in.readString();
        } else {
            this.mDesc = null;
        }
        this.mImportance = in.readInt();
        this.mBypassDnd = in.readByte() != 0;
        this.mLockscreenVisibility = in.readInt();
        if (in.readByte() != 0) {
            this.mSound = Uri.CREATOR.createFromParcel(in);
        } else {
            this.mSound = null;
        }
        this.mLights = in.readByte() != 0;
        this.mVibration = in.createLongArray();
        this.mUserLockedFields = in.readInt();
        this.mFgServiceShown = in.readByte() != 0;
        this.mVibrationEnabled = in.readByte() != 0;
        this.mShowBadge = in.readByte() != 0;
        this.mDeleted = in.readByte() != 0 ? true : z;
        if (in.readByte() != 0) {
            this.mGroup = in.readString();
        } else {
            this.mGroup = null;
        }
        this.mAudioAttributes = in.readInt() > 0 ? AudioAttributes.CREATOR.createFromParcel(in) : audioAttributes;
        this.mLightColor = in.readInt();
        this.mBlockableSystem = in.readBoolean();
    }

    public void writeToParcel(Parcel dest, int flags) {
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
        dest.writeByte(this.mBypassDnd ? (byte) 1 : 0);
        dest.writeInt(this.mLockscreenVisibility);
        if (this.mSound != null) {
            dest.writeByte((byte) 1);
            this.mSound.writeToParcel(dest, 0);
        } else {
            dest.writeByte((byte) 0);
        }
        dest.writeByte(this.mLights ? (byte) 1 : 0);
        dest.writeLongArray(this.mVibration);
        dest.writeInt(this.mUserLockedFields);
        dest.writeByte(this.mFgServiceShown ? (byte) 1 : 0);
        dest.writeByte(this.mVibrationEnabled ? (byte) 1 : 0);
        dest.writeByte(this.mShowBadge ? (byte) 1 : 0);
        dest.writeByte(this.mDeleted ? (byte) 1 : 0);
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

    public void setFgServiceShown(boolean shown) {
        this.mFgServiceShown = shown;
    }

    public void setDeleted(boolean deleted) {
        this.mDeleted = deleted;
    }

    public void setBlockableSystem(boolean blockableSystem) {
        this.mBlockableSystem = blockableSystem;
    }

    public void setName(CharSequence name) {
        this.mName = name != null ? getTrimmedString(name.toString()) : null;
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
        this.mVibrationEnabled = vibrationPattern != null && vibrationPattern.length > 0;
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

    @SystemApi
    public boolean isDeleted() {
        return this.mDeleted;
    }

    @SystemApi
    public int getUserLockedFields() {
        return this.mUserLockedFields;
    }

    public boolean isFgServiceShown() {
        return this.mFgServiceShown;
    }

    public boolean isBlockableSystem() {
        return this.mBlockableSystem;
    }

    public void populateFromXmlForRestore(XmlPullParser parser, Context context) {
        populateFromXml(parser, true, context);
    }

    @SystemApi
    public void populateFromXml(XmlPullParser parser) {
        populateFromXml(parser, false, null);
    }

    private void populateFromXml(XmlPullParser parser, boolean forRestore, Context context) {
        boolean z = true;
        Preconditions.checkArgument(!forRestore || context != null, "forRestore is true but got null context");
        setDescription(parser.getAttributeValue(null, ATT_DESC));
        if (safeInt(parser, "priority", 0) == 0) {
            z = false;
        }
        setBypassDnd(z);
        setLockscreenVisibility(safeInt(parser, ATT_VISIBILITY, -1000));
        Uri sound = safeUri(parser, ATT_SOUND);
        setSound(forRestore ? restoreSoundUri(context, sound) : sound, safeAudioAttributes(parser));
        enableLights(safeBool(parser, ATT_LIGHTS, false));
        setLightColor(safeInt(parser, ATT_LIGHT_COLOR, 0));
        setVibrationPattern(safeLongArray(parser, ATT_VIBRATION, null));
        enableVibration(safeBool(parser, ATT_VIBRATION_ENABLED, false));
        setShowBadge(safeBool(parser, ATT_SHOW_BADGE, false));
        setDeleted(safeBool(parser, ATT_DELETED, false));
        setGroup(parser.getAttributeValue(null, ATT_GROUP));
        lockFields(safeInt(parser, "locked", 0));
        setFgServiceShown(safeBool(parser, ATT_FG_SERVICE_SHOWN, false));
        setBlockableSystem(safeBool(parser, ATT_BLOCKABLE_SYSTEM, false));
    }

    private Uri restoreSoundUri(Context context, Uri uri) {
        if (uri == null) {
            return null;
        }
        ContentResolver contentResolver = context.getContentResolver();
        Uri canonicalizedUri = contentResolver.canonicalize(uri);
        if (canonicalizedUri == null) {
            return Settings.System.DEFAULT_NOTIFICATION_URI;
        }
        return contentResolver.uncanonicalize(canonicalizedUri);
    }

    @SystemApi
    public void writeXml(XmlSerializer out) throws IOException {
        writeXml(out, false, null);
    }

    public void writeXmlForBackup(XmlSerializer out, Context context) throws IOException {
        writeXml(out, true, context);
    }

    private Uri getSoundForBackup(Context context) {
        Uri sound = getSound();
        if (sound == null) {
            return null;
        }
        Uri canonicalSound = context.getContentResolver().canonicalize(sound);
        if (canonicalSound == null) {
            return Settings.System.DEFAULT_NOTIFICATION_URI;
        }
        return canonicalSound;
    }

    private void writeXml(XmlSerializer out, boolean forBackup, Context context) throws IOException {
        Preconditions.checkArgument(!forBackup || context != null, "forBackup is true but got null context");
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
        Uri sound = forBackup ? getSoundForBackup(context) : getSound();
        if (sound != null) {
            out.attribute(null, ATT_SOUND, sound.toString());
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
        if (isFgServiceShown()) {
            out.attribute(null, ATT_FG_SERVICE_SHOWN, Boolean.toString(isFgServiceShown()));
        }
        if (canShowBadge()) {
            out.attribute(null, ATT_SHOW_BADGE, Boolean.toString(canShowBadge()));
        }
        if (isDeleted()) {
            out.attribute(null, ATT_DELETED, Boolean.toString(isDeleted()));
        }
        if (getGroup() != null) {
            out.attribute(null, ATT_GROUP, getGroup());
        }
        if (isBlockableSystem()) {
            out.attribute(null, ATT_BLOCKABLE_SYSTEM, Boolean.toString(isBlockableSystem()));
        }
        out.endTag(null, "channel");
    }

    @SystemApi
    public JSONObject toJson() throws JSONException {
        JSONObject record = new JSONObject();
        record.put("id", getId());
        record.put("name", getName());
        record.put(ATT_DESC, getDescription());
        if (getImportance() != -1000) {
            record.put(ATT_IMPORTANCE, NotificationListenerService.Ranking.importanceToString(getImportance()));
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
        record.put(ATT_FG_SERVICE_SHOWN, Boolean.toString(isFgServiceShown()));
        record.put(ATT_VIBRATION, longArrayToString(getVibrationPattern()));
        record.put(ATT_SHOW_BADGE, Boolean.toString(canShowBadge()));
        record.put(ATT_DELETED, Boolean.toString(isDeleted()));
        record.put(ATT_GROUP, getGroup());
        record.put(ATT_BLOCKABLE_SYSTEM, isBlockableSystem());
        return record;
    }

    private static AudioAttributes safeAudioAttributes(XmlPullParser parser) {
        int usage = safeInt(parser, ATT_USAGE, 5);
        int contentType = safeInt(parser, ATT_CONTENT_TYPE, 4);
        return new AudioAttributes.Builder().setUsage(usage).setContentType(contentType).setFlags(safeInt(parser, ATT_FLAGS, 0)).build();
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
        if (values != null && values.length > 0) {
            for (int i = 0; i < values.length - 1; i++) {
                sb.append(values[i]);
                sb.append(DELIMITER);
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
        if (getId() == null ? that.getId() != null : !getId().equals(that.getId())) {
            return false;
        }
        if (getName() == null ? that.getName() != null : !getName().equals(that.getName())) {
            return false;
        }
        if (getDescription() == null ? that.getDescription() != null : !getDescription().equals(that.getDescription())) {
            return false;
        }
        if (getSound() == null ? that.getSound() != null : !getSound().equals(that.getSound())) {
            return false;
        }
        if (!Arrays.equals(this.mVibration, that.mVibration)) {
            return false;
        }
        if (getGroup() == null ? that.getGroup() != null : !getGroup().equals(that.getGroup())) {
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
        int i = 0;
        int result = 31 * ((31 * ((31 * ((31 * ((31 * ((31 * ((31 * ((31 * ((31 * ((31 * ((31 * ((31 * ((31 * ((31 * ((31 * (getId() != null ? getId().hashCode() : 0)) + (getName() != null ? getName().hashCode() : 0))) + (getDescription() != null ? getDescription().hashCode() : 0))) + getImportance())) + (this.mBypassDnd ? 1 : 0))) + getLockscreenVisibility())) + (getSound() != null ? getSound().hashCode() : 0))) + (this.mLights ? 1 : 0))) + getLightColor())) + Arrays.hashCode(this.mVibration))) + getUserLockedFields())) + (this.mVibrationEnabled ? 1 : 0))) + (this.mShowBadge ? 1 : 0))) + (isDeleted() ? 1 : 0))) + (getGroup() != null ? getGroup().hashCode() : 0));
        if (getAudioAttributes() != null) {
            i = getAudioAttributes().hashCode();
        }
        return (31 * (result + i)) + (isBlockableSystem() ? 1 : 0);
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("NotificationChannel{mId='");
        sb.append(this.mId);
        sb.append('\'');
        sb.append(", mDescription=");
        sb.append(!TextUtils.isEmpty(this.mDesc) ? "hasDescription " : "");
        sb.append(", mImportance=");
        sb.append(this.mImportance);
        sb.append(", mBypassDnd=");
        sb.append(this.mBypassDnd);
        sb.append(", mLockscreenVisibility=");
        sb.append(this.mLockscreenVisibility);
        sb.append(", mSound=");
        sb.append(this.mSound);
        sb.append(", mLights=");
        sb.append(this.mLights);
        sb.append(", mLightColor=");
        sb.append(this.mLightColor);
        sb.append(", mVibration=");
        sb.append(Arrays.toString(this.mVibration));
        sb.append(", mUserLockedFields=");
        sb.append(Integer.toHexString(this.mUserLockedFields));
        sb.append(", mFgServiceShown=");
        sb.append(this.mFgServiceShown);
        sb.append(", mVibrationEnabled=");
        sb.append(this.mVibrationEnabled);
        sb.append(", mShowBadge=");
        sb.append(this.mShowBadge);
        sb.append(", mDeleted=");
        sb.append(this.mDeleted);
        sb.append(", mGroup='");
        sb.append(this.mGroup);
        sb.append('\'');
        sb.append(", mAudioAttributes=");
        sb.append(this.mAudioAttributes);
        sb.append(", mBlockableSystem=");
        sb.append(this.mBlockableSystem);
        sb.append('}');
        return sb.toString();
    }

    public void writeToProto(ProtoOutputStream proto, long fieldId) {
        long token = proto.start(fieldId);
        proto.write(1138166333441L, this.mId);
        proto.write(1138166333442L, this.mName);
        proto.write(1138166333443L, this.mDesc);
        proto.write(1120986464260L, this.mImportance);
        proto.write(1133871366149L, this.mBypassDnd);
        proto.write(1120986464262L, this.mLockscreenVisibility);
        if (this.mSound != null) {
            proto.write(1138166333447L, this.mSound.toString());
        }
        proto.write(NotificationChannelProto.USE_LIGHTS, this.mLights);
        proto.write(NotificationChannelProto.LIGHT_COLOR, this.mLightColor);
        if (this.mVibration != null) {
            for (long v : this.mVibration) {
                proto.write(NotificationChannelProto.VIBRATION, v);
            }
        }
        proto.write(1120986464267L, this.mUserLockedFields);
        proto.write(NotificationChannelProto.FG_SERVICE_SHOWN, this.mFgServiceShown);
        proto.write(NotificationChannelProto.IS_VIBRATION_ENABLED, this.mVibrationEnabled);
        proto.write(NotificationChannelProto.SHOW_BADGE, this.mShowBadge);
        proto.write(1133871366158L, this.mDeleted);
        proto.write(1138166333455L, this.mGroup);
        if (this.mAudioAttributes != null) {
            this.mAudioAttributes.writeToProto(proto, 1146756268048L);
        }
        proto.write(NotificationChannelProto.IS_BLOCKABLE_SYSTEM, this.mBlockableSystem);
        proto.end(token);
    }
}
