package ohos.event.commonevent;

import java.util.Arrays;
import ohos.event.EventConstant;
import ohos.hiviewdfx.HiLog;
import ohos.hiviewdfx.HiLogLabel;
import ohos.utils.Parcel;
import ohos.utils.Sequenceable;

public final class CommonEventPublishInfo implements Sequenceable {
    private static final HiLogLabel LABEL = new HiLogLabel(3, (int) EventConstant.COMMON_EVENT_DOMAIN, TAG);
    public static final Sequenceable.Producer<CommonEventPublishInfo> PRODUCER = $$Lambda$CommonEventPublishInfo$YvcrYUKrOeVuQSHVH0qSQi_ns.INSTANCE;
    private static final String TAG = "CommonEventPublishInfo";
    private boolean ordered;
    private boolean sticky;
    private String[] subscriberPermissions;
    private int userId;

    static /* synthetic */ CommonEventPublishInfo lambda$static$0(Parcel parcel) {
        CommonEventPublishInfo commonEventPublishInfo = new CommonEventPublishInfo();
        commonEventPublishInfo.unmarshalling(parcel);
        return commonEventPublishInfo;
    }

    public CommonEventPublishInfo() {
        this(null);
    }

    public CommonEventPublishInfo(CommonEventPublishInfo commonEventPublishInfo) {
        this.subscriberPermissions = new String[0];
        if (commonEventPublishInfo != null) {
            this.userId = commonEventPublishInfo.userId;
            this.sticky = commonEventPublishInfo.sticky;
            this.ordered = commonEventPublishInfo.ordered;
            this.subscriberPermissions = (String[]) commonEventPublishInfo.subscriberPermissions.clone();
        }
    }

    public void setUserId(int i) {
        this.userId = i;
    }

    public int getUserId() {
        return this.userId;
    }

    public void setSticky(boolean z) {
        this.sticky = z;
    }

    public boolean isSticky() {
        return this.sticky;
    }

    public void setOrdered(boolean z) {
        this.ordered = z;
    }

    public boolean isOrdered() {
        return this.ordered;
    }

    public void setSubscriberPermissions(String[] strArr) {
        if (strArr != null) {
            this.subscriberPermissions = (String[]) strArr.clone();
        } else {
            this.subscriberPermissions = new String[0];
        }
    }

    public String[] getSubscriberPermissions() {
        return (String[]) this.subscriberPermissions.clone();
    }

    public boolean marshalling(Parcel parcel) {
        if (!parcel.writeInt(this.userId)) {
            HiLog.warn(LABEL, "write userId failed.", new Object[0]);
            return false;
        } else if (!parcel.writeBoolean(this.sticky)) {
            HiLog.warn(LABEL, "write sticky failed.", new Object[0]);
            return false;
        } else if (!parcel.writeBoolean(this.ordered)) {
            HiLog.warn(LABEL, "write ordered failed.", new Object[0]);
            return false;
        } else if (parcel.writeStringArray(this.subscriberPermissions)) {
            return true;
        } else {
            HiLog.warn(LABEL, "write subscriberPermissions failed.", new Object[0]);
            return false;
        }
    }

    public boolean unmarshalling(Parcel parcel) {
        this.userId = parcel.readInt();
        this.sticky = parcel.readBoolean();
        this.ordered = parcel.readBoolean();
        this.subscriberPermissions = parcel.readStringArray();
        return true;
    }

    public String toString() {
        return "CommonEventPublishInfo[ sticky = " + this.sticky + " ordered = " + this.ordered + " subscriberPermissions = " + Arrays.toString(this.subscriberPermissions) + "]";
    }
}
