package ohos.event.commonevent;

import ohos.aafwk.content.IntentFilter;
import ohos.event.EventConstant;
import ohos.hiviewdfx.HiLog;
import ohos.hiviewdfx.HiLogLabel;
import ohos.utils.Parcel;
import ohos.utils.Sequenceable;

public final class CommonEventSubscribeInfo implements Sequenceable {
    private static final HiLogLabel LABEL = new HiLogLabel(3, (int) EventConstant.COMMON_EVENT_DOMAIN, TAG);
    public static final Sequenceable.Producer<CommonEventSubscribeInfo> PRODUCER = $$Lambda$CommonEventSubscribeInfo$1Jfp7mF0Q9nY26CabrvJuAUyuY.INSTANCE;
    private static final String TAG = "CommonEventSubscribeInfo";
    private static final int USER_CURRENT = -2;
    private IntentFilter intentFilter;
    private int priority;
    private String publisherDeviceId;
    private String publisherPermission;
    private ThreadMode threadMode = ThreadMode.HANDLER;
    private int userId = -2;

    public enum ThreadMode {
        HANDLER,
        POST,
        ASYNC,
        BACKGROUND
    }

    static /* synthetic */ CommonEventSubscribeInfo lambda$static$0(Parcel parcel) {
        CommonEventSubscribeInfo commonEventSubscribeInfo = new CommonEventSubscribeInfo();
        commonEventSubscribeInfo.unmarshalling(parcel);
        return commonEventSubscribeInfo;
    }

    private CommonEventSubscribeInfo() {
    }

    public CommonEventSubscribeInfo(IntentFilter intentFilter2) {
        if (intentFilter2 != null) {
            this.intentFilter = new IntentFilter(intentFilter2);
        }
    }

    public CommonEventSubscribeInfo(CommonEventSubscribeInfo commonEventSubscribeInfo) {
        if (commonEventSubscribeInfo != null) {
            this.publisherPermission = commonEventSubscribeInfo.publisherPermission;
            this.publisherDeviceId = commonEventSubscribeInfo.publisherDeviceId;
            this.threadMode = commonEventSubscribeInfo.threadMode;
            this.userId = commonEventSubscribeInfo.userId;
            this.intentFilter = commonEventSubscribeInfo.intentFilter;
            this.priority = commonEventSubscribeInfo.priority;
        }
    }

    public IntentFilter getIntentFilter() {
        return this.intentFilter;
    }

    public ThreadMode getThreadMode() {
        return this.threadMode;
    }

    public void setThreadMode(ThreadMode threadMode2) {
        if (threadMode2 != null) {
            this.threadMode = threadMode2;
        }
    }

    public String getPermission() {
        return this.publisherPermission;
    }

    public void setPermission(String str) {
        this.publisherPermission = str;
    }

    public void setDeviceId(String str) {
        this.publisherDeviceId = str;
    }

    public String getDeviceId() {
        return this.publisherDeviceId;
    }

    public void setUserId(int i) {
        this.userId = i;
    }

    public int getUserId() {
        return this.userId;
    }

    public void setPriority(int i) {
        this.priority = i;
    }

    public int getPriority() {
        return this.priority;
    }

    public boolean marshalling(Parcel parcel) {
        if (this.intentFilter == null) {
            if (!parcel.writeInt(0)) {
                HiLog.warn(LABEL, "write intentFilter failed.", new Object[0]);
                return false;
            }
        } else if (!parcel.writeInt(1)) {
            HiLog.warn(LABEL, "write intentFilter failed.", new Object[0]);
            return false;
        } else {
            parcel.writeSequenceable(this.intentFilter);
        }
        if (!parcel.writeString(this.publisherDeviceId)) {
            HiLog.warn(LABEL, "write publisherDeviceId failed.", new Object[0]);
            return false;
        } else if (!parcel.writeString(this.publisherPermission)) {
            HiLog.warn(LABEL, "write publisherPermission failed.", new Object[0]);
            return false;
        } else if (!parcel.writeInt(this.threadMode.ordinal())) {
            HiLog.warn(LABEL, "write threadMode failed.", new Object[0]);
            return false;
        } else if (!parcel.writeInt(this.userId)) {
            HiLog.warn(LABEL, "write userId failed.", new Object[0]);
            return false;
        } else if (parcel.writeInt(this.priority)) {
            return true;
        } else {
            HiLog.warn(LABEL, "write priority failed.", new Object[0]);
            return false;
        }
    }

    public boolean unmarshalling(Parcel parcel) {
        int readInt = parcel.readInt();
        if (readInt == 1) {
            this.intentFilter = new IntentFilter();
            if (!parcel.readSequenceable(this.intentFilter)) {
                HiLog.warn(LABEL, "read intentFilter failed.", new Object[0]);
                return false;
            }
        } else if (readInt == 0) {
            this.intentFilter = null;
        } else {
            HiLog.warn(LABEL, "read invalid parcel.", new Object[0]);
            return false;
        }
        this.publisherDeviceId = parcel.readString();
        this.publisherPermission = parcel.readString();
        int readInt2 = parcel.readInt();
        this.userId = parcel.readInt();
        if (readInt2 < ThreadMode.HANDLER.ordinal() || readInt2 > ThreadMode.BACKGROUND.ordinal()) {
            readInt2 = ThreadMode.HANDLER.ordinal();
        }
        this.threadMode = ThreadMode.values()[readInt2];
        this.priority = parcel.readInt();
        return true;
    }
}
