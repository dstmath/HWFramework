package ohos.event.notification;

import ohos.event.EventConstant;
import ohos.hiviewdfx.HiLog;
import ohos.hiviewdfx.HiLogLabel;
import ohos.utils.Parcel;
import ohos.utils.Sequenceable;
import ohos.utils.net.Uri;

public final class NotificationSlot implements Sequenceable {
    private static final byte HAS_VALUE = 1;
    private static final HiLogLabel LABEL = new HiLogLabel(3, (int) EventConstant.NOTIFICATION_DOMAIN, TAG);
    public static final int LEVEL_DEFAULT = 3;
    public static final int LEVEL_HIGH = 4;
    public static final int LEVEL_LOW = 2;
    public static final int LEVEL_MIN = 1;
    public static final int LEVEL_NONE = 0;
    public static final int LEVEL_UNDEFINED = -1000;
    private static final int LIGHT_COLOR_DEFAULT = 0;
    private static final int MAX_STR_LENGTH = 1000;
    private static final byte NO_VALUE = 0;
    public static final Sequenceable.Producer<NotificationSlot> PRODUCER = $$Lambda$NotificationSlot$0_tQ_Cw92CmRVBUVQT50Lm0TKs.INSTANCE;
    private static final String TAG = "NotificationSlot";
    private boolean bypassDnd;
    private String desc;
    private int level;
    private int lightColor;
    private boolean lightEnabled;
    private int lockscreenVisibility;
    private boolean showBadgeFlag;
    private String slotGroupId;
    private String slotId;
    private String slotName;
    private Uri sound;
    private boolean vibrationEnabled;

    static /* synthetic */ NotificationSlot lambda$static$0(Parcel parcel) {
        NotificationSlot notificationSlot = new NotificationSlot();
        notificationSlot.unmarshalling(parcel);
        return notificationSlot;
    }

    public NotificationSlot(String str, String str2, int i) {
        this.level = 3;
        this.showBadgeFlag = true;
        this.lockscreenVisibility = -1000;
        this.sound = new Uri.Builder().build();
        this.lightColor = 0;
        this.slotId = getTrimmedString(str);
        this.slotName = getTrimmedString(str2);
        this.level = i;
    }

    NotificationSlot() {
        this(null, null, 3);
    }

    public void setLevel(int i) {
        this.level = i;
    }

    public void setName(String str) {
        this.slotName = str != null ? getTrimmedString(str) : null;
    }

    public void setDescription(String str) {
        this.desc = getTrimmedString(str);
    }

    public void enableBadge(boolean z) {
        this.showBadgeFlag = z;
    }

    public void enableBypassDnd(boolean z) {
        this.bypassDnd = z;
    }

    public void setEnableVibration(boolean z) {
        this.vibrationEnabled = z;
    }

    public void setLockscreenVisibleness(int i) {
        this.lockscreenVisibility = i;
    }

    public void setSound(Uri uri) {
        this.sound = uri;
    }

    public void setEnableLight(boolean z) {
        this.lightEnabled = z;
    }

    public void setLedLightColor(int i) {
        this.lightColor = i;
    }

    public void setSlotGroup(String str) {
        this.slotGroupId = str;
    }

    public String getId() {
        return this.slotId;
    }

    public String getName() {
        return this.slotName;
    }

    public String getDescription() {
        return this.desc;
    }

    public int getLevel() {
        return this.level;
    }

    public boolean isShowBadge() {
        return this.showBadgeFlag;
    }

    public boolean isEnableBypassDnd() {
        return this.bypassDnd;
    }

    public boolean canVibrate() {
        return this.vibrationEnabled;
    }

    public int getLockscreenVisibleness() {
        return this.lockscreenVisibility;
    }

    public Uri getSound() {
        return this.sound;
    }

    public boolean canEnableLight() {
        return this.lightEnabled;
    }

    public int getLedLightColor() {
        return this.lightColor;
    }

    public String getSlotGroup() {
        return this.slotGroupId;
    }

    public boolean marshalling(Parcel parcel) {
        return writeToParcel(parcel);
    }

    public boolean unmarshalling(Parcel parcel) {
        return readFromParcel(parcel);
    }

    private String getTrimmedString(String str) {
        return (str == null || str.length() <= 1000) ? str : str.substring(0, 1000);
    }

    private boolean writeToParcel(Parcel parcel) {
        if (!parcel.writeString(this.slotId) || !parcel.writeString(this.slotName) || !parcel.writeString(this.desc) || !parcel.writeInt(this.level) || !parcel.writeBoolean(this.showBadgeFlag) || !parcel.writeBoolean(this.bypassDnd) || !parcel.writeInt(this.lockscreenVisibility) || !parcel.writeBoolean(this.vibrationEnabled)) {
            return false;
        }
        if (this.sound != null) {
            if (!parcel.writeByte((byte) 1)) {
                return false;
            }
            parcel.writeSequenceable(this.sound);
        } else if (!parcel.writeByte((byte) 0)) {
            return false;
        }
        if (parcel.writeBoolean(this.lightEnabled) && parcel.writeInt(this.lightColor)) {
            return parcel.writeString(this.slotGroupId);
        }
        return false;
    }

    private boolean readFromParcel(Parcel parcel) {
        this.slotId = parcel.readString();
        this.slotName = parcel.readString();
        this.desc = parcel.readString();
        this.level = parcel.readInt();
        this.showBadgeFlag = parcel.readBoolean();
        this.bypassDnd = parcel.readBoolean();
        this.lockscreenVisibility = parcel.readInt();
        this.vibrationEnabled = parcel.readBoolean();
        byte readByte = parcel.readByte();
        if (readByte == 1) {
            try {
                this.sound = Uri.readFromParcel(parcel);
            } catch (IllegalArgumentException unused) {
                this.sound = null;
            }
        } else if (readByte == 0) {
            this.sound = null;
        } else {
            HiLog.warn(LABEL, "NotificationSlot: readFromParcel read sound parcel fail.", new Object[0]);
            return false;
        }
        this.lightEnabled = parcel.readBoolean();
        this.lightColor = parcel.readInt();
        this.slotGroupId = parcel.readString();
        return true;
    }
}
