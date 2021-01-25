package android.app;

import android.annotation.SystemApi;
import android.annotation.UnsupportedAppUsage;
import android.content.ContentResolver;
import android.content.Context;
import android.media.AudioAttributes;
import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;
import android.provider.Settings;
import android.service.notification.NotificationListenerService;
import android.text.TextUtils;
import android.text.format.DateFormat;
import android.util.proto.ProtoOutputStream;
import com.android.internal.util.Preconditions;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.Objects;
import org.json.JSONException;
import org.json.JSONObject;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlSerializer;

public final class NotificationChannel implements Parcelable {
    private static final String ATT_ALLOW_BUBBLE = "can_bubble";
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
        /* class android.app.NotificationChannel.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public NotificationChannel createFromParcel(Parcel in) {
            return new NotificationChannel(in);
        }

        @Override // android.os.Parcelable.Creator
        public NotificationChannel[] newArray(int size) {
            return new NotificationChannel[size];
        }
    };
    private static final boolean DEFAULT_ALLOW_BUBBLE = true;
    public static final String DEFAULT_CHANNEL_ID = "miscellaneous";
    private static final boolean DEFAULT_DELETED = false;
    private static final int DEFAULT_IMPORTANCE = -1000;
    private static final int DEFAULT_LIGHT_COLOR = 0;
    private static final boolean DEFAULT_SHOW_BADGE = true;
    private static final int DEFAULT_VISIBILITY = -1000;
    private static final String DELIMITER = ",";
    public static final int[] LOCKABLE_FIELDS = {1, 2, 4, 8, 16, 32, 128, 256};
    private static final int MAX_TEXT_LENGTH = 1000;
    private static final String TAG_CHANNEL = "channel";
    public static final int USER_LOCKED_ALLOW_BUBBLE = 256;
    public static final int USER_LOCKED_IMPORTANCE = 4;
    public static final int USER_LOCKED_LIGHTS = 8;
    public static final int USER_LOCKED_PRIORITY = 1;
    public static final int USER_LOCKED_SHOW_BADGE = 128;
    public static final int USER_LOCKED_SOUND = 32;
    public static final int USER_LOCKED_VIBRATION = 16;
    public static final int USER_LOCKED_VISIBILITY = 2;
    private boolean mAllowBubbles;
    private AudioAttributes mAudioAttributes;
    private boolean mBlockableSystem;
    private boolean mBypassDnd;
    private boolean mDeleted;
    private String mDesc;
    private boolean mFgServiceShown;
    private String mGroup;
    @UnsupportedAppUsage
    private final String mId;
    private int mImportance;
    private boolean mImportanceLockedByOEM;
    private boolean mImportanceLockedDefaultApp;
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
        this.mAllowBubbles = true;
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
        this.mAllowBubbles = true;
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
        this.mAllowBubbles = in.readBoolean();
        this.mImportanceLockedByOEM = in.readBoolean();
    }

    @Override // android.os.Parcelable
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
        dest.writeBoolean(this.mAllowBubbles);
        dest.writeBoolean(this.mImportanceLockedByOEM);
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

    @UnsupportedAppUsage
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

    public void setAllowBubbles(boolean allowBubbles) {
        this.mAllowBubbles = allowBubbles;
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

    public boolean canBubble() {
        return this.mAllowBubbles;
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

    public void setImportanceLockedByOEM(boolean locked) {
        this.mImportanceLockedByOEM = locked;
    }

    public void setImportanceLockedByCriticalDeviceFunction(boolean locked) {
        this.mImportanceLockedDefaultApp = locked;
    }

    public boolean isImportanceLockedByOEM() {
        return this.mImportanceLockedByOEM;
    }

    public boolean isImportanceLockedByCriticalDeviceFunction() {
        return this.mImportanceLockedDefaultApp;
    }

    public boolean hasUserSetImportance() {
        return (this.mUserLockedFields & 4) != 0;
    }

    public void populateFromXmlForRestore(XmlPullParser parser, Context context) {
        populateFromXml(parser, true, context);
    }

    @SystemApi
    public void populateFromXml(XmlPullParser parser) {
        populateFromXml(parser, false, null);
    }

    private void populateFromXml(XmlPullParser parser, boolean forRestore, Context context) {
        Preconditions.checkArgument(!forRestore || context != null, "forRestore is true but got null context");
        setDescription(parser.getAttributeValue(null, ATT_DESC));
        setBypassDnd(safeInt(parser, "priority", 0) != 0);
        setLockscreenVisibility(safeInt(parser, "visibility", -1000));
        Uri sound = safeUri(parser, ATT_SOUND);
        setSound(forRestore ? restoreSoundUri(context, sound) : sound, safeAudioAttributes(parser));
        enableLights(safeBool(parser, ATT_LIGHTS, false));
        setLightColor(safeInt(parser, ATT_LIGHT_COLOR, 0));
        setVibrationPattern(safeLongArray(parser, ATT_VIBRATION, null));
        enableVibration(safeBool(parser, ATT_VIBRATION_ENABLED, false));
        setShowBadge(safeBool(parser, ATT_SHOW_BADGE, false));
        setDeleted(safeBool(parser, "deleted", false));
        setGroup(parser.getAttributeValue(null, "group"));
        lockFields(safeInt(parser, "locked", 0));
        setFgServiceShown(safeBool(parser, ATT_FG_SERVICE_SHOWN, false));
        setBlockableSystem(safeBool(parser, ATT_BLOCKABLE_SYSTEM, false));
        setAllowBubbles(safeBool(parser, ATT_ALLOW_BUBBLE, true));
    }

    private Uri restoreSoundUri(Context context, Uri uri) {
        if (uri == null || Uri.EMPTY.equals(uri)) {
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
        if (sound == null || Uri.EMPTY.equals(sound)) {
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
            out.attribute(null, "visibility", Integer.toString(getLockscreenVisibility()));
        }
        Uri sound = forBackup ? getSoundForBackup(context) : getSound();
        if (sound != null) {
            out.attribute(null, ATT_SOUND, sound.toString());
        }
        if (getAudioAttributes() != null) {
            out.attribute(null, ATT_USAGE, Integer.toString(getAudioAttributes().getUsage()));
            out.attribute(null, "content_type", Integer.toString(getAudioAttributes().getContentType()));
            out.attribute(null, "flags", Integer.toString(getAudioAttributes().getFlags()));
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
            out.attribute(null, "deleted", Boolean.toString(isDeleted()));
        }
        if (getGroup() != null) {
            out.attribute(null, "group", getGroup());
        }
        if (isBlockableSystem()) {
            out.attribute(null, ATT_BLOCKABLE_SYSTEM, Boolean.toString(isBlockableSystem()));
        }
        if (!canBubble()) {
            out.attribute(null, ATT_ALLOW_BUBBLE, Boolean.toString(canBubble()));
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
            record.put("visibility", Notification.visibilityToString(getLockscreenVisibility()));
        }
        if (getSound() != null) {
            record.put(ATT_SOUND, getSound().toString());
        }
        if (getAudioAttributes() != null) {
            record.put(ATT_USAGE, Integer.toString(getAudioAttributes().getUsage()));
            record.put("content_type", Integer.toString(getAudioAttributes().getContentType()));
            record.put("flags", Integer.toString(getAudioAttributes().getFlags()));
        }
        record.put(ATT_LIGHTS, Boolean.toString(shouldShowLights()));
        record.put(ATT_LIGHT_COLOR, Integer.toString(getLightColor()));
        record.put(ATT_VIBRATION_ENABLED, Boolean.toString(shouldVibrate()));
        record.put("locked", Integer.toString(getUserLockedFields()));
        record.put(ATT_FG_SERVICE_SHOWN, Boolean.toString(isFgServiceShown()));
        record.put(ATT_VIBRATION, longArrayToString(getVibrationPattern()));
        record.put(ATT_SHOW_BADGE, Boolean.toString(canShowBadge()));
        record.put("deleted", Boolean.toString(isDeleted()));
        record.put("group", getGroup());
        record.put(ATT_BLOCKABLE_SYSTEM, isBlockableSystem());
        record.put(ATT_ALLOW_BUBBLE, canBubble());
        return record;
    }

    private static AudioAttributes safeAudioAttributes(XmlPullParser parser) {
        int usage = safeInt(parser, ATT_USAGE, 5);
        int contentType = safeInt(parser, "content_type", 4);
        return new AudioAttributes.Builder().setUsage(usage).setContentType(contentType).setFlags(safeInt(parser, "flags", 0)).build();
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
        String[] values = attributeValue.split(",");
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
                sb.append(",");
            }
            sb.append(values[values.length - 1]);
        }
        return sb.toString();
    }

    @Override // android.os.Parcelable
    public int describeContents() {
        return 0;
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        NotificationChannel that = (NotificationChannel) o;
        if (getImportance() == that.getImportance() && this.mBypassDnd == that.mBypassDnd && getLockscreenVisibility() == that.getLockscreenVisibility() && this.mLights == that.mLights && getLightColor() == that.getLightColor() && getUserLockedFields() == that.getUserLockedFields() && isFgServiceShown() == that.isFgServiceShown() && this.mVibrationEnabled == that.mVibrationEnabled && this.mShowBadge == that.mShowBadge && isDeleted() == that.isDeleted() && isBlockableSystem() == that.isBlockableSystem() && this.mAllowBubbles == that.mAllowBubbles && Objects.equals(getId(), that.getId()) && Objects.equals(getName(), that.getName()) && Objects.equals(this.mDesc, that.mDesc) && Objects.equals(getSound(), that.getSound()) && Arrays.equals(this.mVibration, that.mVibration) && Objects.equals(getGroup(), that.getGroup()) && Objects.equals(getAudioAttributes(), that.getAudioAttributes()) && this.mImportanceLockedByOEM == that.mImportanceLockedByOEM && this.mImportanceLockedDefaultApp == that.mImportanceLockedDefaultApp) {
            return true;
        }
        return false;
    }

    public int hashCode() {
        return (Objects.hash(getId(), getName(), this.mDesc, Integer.valueOf(getImportance()), Boolean.valueOf(this.mBypassDnd), Integer.valueOf(getLockscreenVisibility()), getSound(), Boolean.valueOf(this.mLights), Integer.valueOf(getLightColor()), Integer.valueOf(getUserLockedFields()), Boolean.valueOf(isFgServiceShown()), Boolean.valueOf(this.mVibrationEnabled), Boolean.valueOf(this.mShowBadge), Boolean.valueOf(isDeleted()), getGroup(), getAudioAttributes(), Boolean.valueOf(isBlockableSystem()), Boolean.valueOf(this.mAllowBubbles), Boolean.valueOf(this.mImportanceLockedByOEM), Boolean.valueOf(this.mImportanceLockedDefaultApp)) * 31) + Arrays.hashCode(this.mVibration);
    }

    public void dump(PrintWriter pw, String prefix, boolean redacted) {
        String redactedName = this.mName;
        if (redacted) {
            redactedName = (String) TextUtils.trimToLengthWithEllipsis(redactedName, 3);
        }
        StringBuilder sb = new StringBuilder();
        sb.append("NotificationChannel{mId='");
        sb.append(this.mId);
        sb.append(DateFormat.QUOTE);
        sb.append(", mName=");
        sb.append(redactedName);
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
        sb.append(DateFormat.QUOTE);
        sb.append(", mAudioAttributes=");
        sb.append(this.mAudioAttributes);
        sb.append(", mBlockableSystem=");
        sb.append(this.mBlockableSystem);
        sb.append(", mAllowBubbles=");
        sb.append(this.mAllowBubbles);
        sb.append(", mImportanceLockedByOEM=");
        sb.append(this.mImportanceLockedByOEM);
        sb.append(", mImportanceLockedDefaultApp=");
        sb.append(this.mImportanceLockedDefaultApp);
        sb.append('}');
        String output = sb.toString();
        pw.println(prefix + output);
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("NotificationChannel{mId='");
        sb.append(this.mId);
        sb.append(DateFormat.QUOTE);
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
        sb.append(DateFormat.QUOTE);
        sb.append(", mAudioAttributes=");
        sb.append(this.mAudioAttributes);
        sb.append(", mBlockableSystem=");
        sb.append(this.mBlockableSystem);
        sb.append(", mAllowBubbles=");
        sb.append(this.mAllowBubbles);
        sb.append(", mImportanceLockedByOEM=");
        sb.append(this.mImportanceLockedByOEM);
        sb.append(", mImportanceLockedDefaultApp=");
        sb.append(this.mImportanceLockedDefaultApp);
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
        Uri uri = this.mSound;
        if (uri != null) {
            proto.write(1138166333447L, uri.toString());
        }
        proto.write(1133871366152L, this.mLights);
        proto.write(1120986464265L, this.mLightColor);
        long[] jArr = this.mVibration;
        if (jArr != null) {
            for (long v : jArr) {
                proto.write(NotificationChannelProto.VIBRATION, v);
            }
        }
        proto.write(1120986464267L, this.mUserLockedFields);
        proto.write(1133871366162L, this.mFgServiceShown);
        proto.write(1133871366156L, this.mVibrationEnabled);
        proto.write(1133871366157L, this.mShowBadge);
        proto.write(1133871366158L, this.mDeleted);
        proto.write(1138166333455L, this.mGroup);
        AudioAttributes audioAttributes = this.mAudioAttributes;
        if (audioAttributes != null) {
            audioAttributes.writeToProto(proto, 1146756268048L);
        }
        proto.write(1133871366161L, this.mBlockableSystem);
        proto.write(1133871366163L, this.mAllowBubbles);
        proto.end(token);
    }
}
