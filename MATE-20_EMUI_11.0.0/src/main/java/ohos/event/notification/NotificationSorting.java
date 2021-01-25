package ohos.event.notification;

import ohos.event.EventConstant;
import ohos.hiviewdfx.HiLog;
import ohos.hiviewdfx.HiLogLabel;
import ohos.utils.Parcel;
import ohos.utils.Sequenceable;

public class NotificationSorting implements Sequenceable {
    private static final HiLogLabel LABEL = new HiLogLabel(3, (int) EventConstant.NOTIFICATION_DOMAIN, TAG);
    public static final Sequenceable.Producer<NotificationSorting> PRODUCER = $$Lambda$NotificationSorting$Ia89gv0shCUqoKMzNB9fEXWTJOw.INSTANCE;
    private static final String TAG = "NotificationSorting";
    private String groupKeyOverride;
    private String hashCode;
    private int importance;
    private boolean isDisplayBadge;
    private boolean isHiddenNotification;
    private boolean isSuitInterruptionFilter;
    private int ranking = -1;
    private NotificationSlot slot = new NotificationSlot();
    private int visiblenessOverride;

    static /* synthetic */ NotificationSorting lambda$static$0(Parcel parcel) {
        NotificationSorting notificationSorting = new NotificationSorting();
        notificationSorting.unmarshalling(parcel);
        return notificationSorting;
    }

    public String getHashCode() {
        return this.hashCode;
    }

    public int getRanking() {
        return this.ranking;
    }

    public NotificationSlot getSlot() {
        return this.slot;
    }

    public boolean isDisplayBadge() {
        return this.isDisplayBadge;
    }

    public boolean isHiddenNotification() {
        return this.isHiddenNotification;
    }

    public boolean isSuitInterruptionFilter() {
        return this.isSuitInterruptionFilter;
    }

    public int getImportance() {
        return this.importance;
    }

    public String getGroupKeyOverride() {
        return this.groupKeyOverride;
    }

    public int getVisiblenessOverride() {
        return this.visiblenessOverride;
    }

    public boolean marshalling(Parcel parcel) {
        if (!parcel.writeString(this.hashCode)) {
            HiLog.warn(LABEL, "write hash code failed.", new Object[0]);
            return false;
        } else if (!parcel.writeInt(this.ranking)) {
            HiLog.warn(LABEL, "write ranking failed.", new Object[0]);
            return false;
        } else {
            parcel.writeSequenceable(this.slot);
            if (!parcel.writeBoolean(this.isDisplayBadge)) {
                HiLog.warn(LABEL, "write isDisplayBadge failed.", new Object[0]);
                return false;
            } else if (!parcel.writeBoolean(this.isHiddenNotification)) {
                HiLog.warn(LABEL, "write isHiddenNotification failed.", new Object[0]);
                return false;
            } else if (!parcel.writeBoolean(this.isSuitInterruptionFilter)) {
                HiLog.warn(LABEL, "write isSuitInterruptionFilter failed.", new Object[0]);
                return false;
            } else if (!parcel.writeInt(this.importance)) {
                HiLog.warn(LABEL, "write importance failed.", new Object[0]);
                return false;
            } else if (!parcel.writeString(this.groupKeyOverride)) {
                HiLog.warn(LABEL, "write groupKeyOverride failed.", new Object[0]);
                return false;
            } else if (parcel.writeInt(this.visiblenessOverride)) {
                return true;
            } else {
                HiLog.warn(LABEL, "write visiblenessOverride failed.", new Object[0]);
                return false;
            }
        }
    }

    public boolean unmarshalling(Parcel parcel) {
        this.hashCode = parcel.readString();
        this.ranking = parcel.readInt();
        if (!parcel.readSequenceable(this.slot)) {
            return false;
        }
        this.isDisplayBadge = parcel.readBoolean();
        this.isHiddenNotification = parcel.readBoolean();
        this.isSuitInterruptionFilter = parcel.readBoolean();
        this.importance = parcel.readInt();
        this.groupKeyOverride = parcel.readString();
        this.visiblenessOverride = parcel.readInt();
        return true;
    }

    /* access modifiers changed from: package-private */
    public void setHashCode(String str) {
        this.hashCode = str;
    }

    /* access modifiers changed from: package-private */
    public void setRanking(int i) {
        this.ranking = i;
    }

    /* access modifiers changed from: package-private */
    public void setNotificationSlot(NotificationSlot notificationSlot) {
        this.slot = notificationSlot;
    }

    /* access modifiers changed from: package-private */
    public void setIsDisplayBadge(boolean z) {
        this.isDisplayBadge = z;
    }

    /* access modifiers changed from: package-private */
    public void setIsHiddenNotification(boolean z) {
        this.isHiddenNotification = z;
    }

    /* access modifiers changed from: package-private */
    public void setIsSuitInterruptionFilter(boolean z) {
        this.isSuitInterruptionFilter = z;
    }

    /* access modifiers changed from: package-private */
    public void setImportance(int i) {
        this.importance = i;
    }

    /* access modifiers changed from: package-private */
    public void setGroupKeyOverride(String str) {
        this.groupKeyOverride = str;
    }

    /* access modifiers changed from: package-private */
    public void setVisiblenessOverride(int i) {
        this.visiblenessOverride = i;
    }

    /* access modifiers changed from: package-private */
    public void cloneNotificationSorting(NotificationSorting notificationSorting) {
        if (notificationSorting != null) {
            this.hashCode = notificationSorting.hashCode;
            this.ranking = notificationSorting.ranking;
            this.slot = notificationSorting.slot;
            this.isDisplayBadge = notificationSorting.isDisplayBadge;
            this.isHiddenNotification = notificationSorting.isHiddenNotification;
            this.isSuitInterruptionFilter = notificationSorting.isSuitInterruptionFilter;
            this.importance = notificationSorting.importance;
            this.groupKeyOverride = notificationSorting.groupKeyOverride;
            this.visiblenessOverride = notificationSorting.visiblenessOverride;
        }
    }
}
