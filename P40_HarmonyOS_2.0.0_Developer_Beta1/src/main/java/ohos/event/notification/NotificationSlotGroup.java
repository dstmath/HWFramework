package ohos.event.notification;

import java.util.ArrayList;
import java.util.List;
import ohos.event.EventConstant;
import ohos.hiviewdfx.HiLog;
import ohos.hiviewdfx.HiLogLabel;
import ohos.utils.Parcel;
import ohos.utils.Sequenceable;

public final class NotificationSlotGroup implements Sequenceable {
    private static final HiLogLabel LABEL = new HiLogLabel(3, (int) EventConstant.NOTIFICATION_DOMAIN, TAG);
    private static final int MAX_SLOTS = 1024;
    private static final int MAX_STR_LENGTH = 1000;
    public static final Sequenceable.Producer<NotificationSlotGroup> PRODUCER = $$Lambda$NotificationSlotGroup$iZWys9mnr66Ew2x4_acWOSokkqY.INSTANCE;
    private static final String TAG = "NotificationSlotGroup";
    private String desc;
    private boolean disabled;
    private String slotGroupId;
    private String slotGroupName;
    private List<NotificationSlot> slots;

    static /* synthetic */ NotificationSlotGroup lambda$static$0(Parcel parcel) {
        NotificationSlotGroup notificationSlotGroup = new NotificationSlotGroup();
        notificationSlotGroup.unmarshalling(parcel);
        return notificationSlotGroup;
    }

    public NotificationSlotGroup(String str, String str2) {
        this.slots = new ArrayList();
        this.slotGroupId = getTrimmedString(str);
        this.slotGroupName = getTrimmedString(str2);
    }

    public NotificationSlotGroup() {
        this(null, null);
    }

    public String getId() {
        return this.slotGroupId;
    }

    public String getName() {
        return this.slotGroupName;
    }

    /* access modifiers changed from: package-private */
    public void setSlots(List<NotificationSlot> list) {
        this.slots = list;
    }

    public List<NotificationSlot> getSlots() {
        return this.slots;
    }

    public void setDescription(String str) {
        this.desc = getTrimmedString(str);
    }

    public String getDescription() {
        return this.desc;
    }

    /* access modifiers changed from: package-private */
    public void setDisabled(boolean z) {
        this.disabled = z;
    }

    public boolean isDisabled() {
        return this.disabled;
    }

    public boolean marshalling(Parcel parcel) {
        return writeToParcel(parcel);
    }

    public boolean unmarshalling(Parcel parcel) {
        return readFromParcel(parcel);
    }

    private boolean writeToParcel(Parcel parcel) {
        if (!(parcel.writeString(this.slotGroupId) && parcel.writeString(this.slotGroupName) && parcel.writeString(this.desc) && parcel.writeBoolean(this.disabled))) {
            return false;
        }
        int size = this.slots.size();
        if (!parcel.writeInt(size)) {
            HiLog.warn(LABEL, "write size of slots failed.", new Object[0]);
            return false;
        } else if (size <= 0) {
            return true;
        } else {
            for (NotificationSlot notificationSlot : this.slots) {
                parcel.writeSequenceable(notificationSlot);
            }
            return true;
        }
    }

    private boolean readFromParcel(Parcel parcel) {
        this.slotGroupId = parcel.readString();
        this.slotGroupName = parcel.readString();
        this.desc = parcel.readString();
        this.disabled = parcel.readBoolean();
        this.slots.clear();
        int readInt = parcel.readInt();
        if (readInt > 1024) {
            HiLog.warn(LABEL, "read slots oversize.", new Object[0]);
            return false;
        }
        for (int i = 0; i < readInt; i++) {
            NotificationSlot notificationSlot = new NotificationSlot();
            if (!parcel.readSequenceable(notificationSlot)) {
                HiLog.warn(LABEL, "read slots fail.", new Object[0]);
                return false;
            }
            this.slots.add(notificationSlot);
        }
        return true;
    }

    private String getTrimmedString(String str) {
        return (str == null || str.length() <= 1000) ? str : str.substring(0, 1000);
    }

    public String toString() {
        return "NotificationSlotGroup[ slotGroupId = " + this.slotGroupId + " slotGroupName = " + this.slotGroupName + " desc = " + this.desc + " disabled = " + this.disabled + " slots = " + this.slots + "]";
    }
}
