package ohos.event.notification;

import java.util.HashSet;
import java.util.Set;
import ohos.event.EventConstant;
import ohos.hiviewdfx.HiLog;
import ohos.hiviewdfx.HiLogLabel;
import ohos.utils.Parcel;
import ohos.utils.Sequenceable;

public class NotificationSubscribeInfo implements Sequenceable {
    private static final HiLogLabel LABEL = new HiLogLabel(3, (int) EventConstant.NOTIFICATION_DOMAIN, TAG);
    private static final int MAX_NUM = 1048576;
    public static final Sequenceable.Producer<NotificationSubscribeInfo> PRODUCER = $$Lambda$NotificationSubscribeInfo$2e7lX5BRhO3FNlooF29_gA5WWog.INSTANCE;
    private static final String TAG = "NotificationSubscribeInfo";
    private Set<String> appNames;
    private Set<String> deviceNames;
    private int userId;

    static /* synthetic */ NotificationSubscribeInfo lambda$static$0(Parcel parcel) {
        NotificationSubscribeInfo notificationSubscribeInfo = new NotificationSubscribeInfo();
        notificationSubscribeInfo.unmarshalling(parcel);
        return notificationSubscribeInfo;
    }

    public NotificationSubscribeInfo() {
        this(null);
    }

    public NotificationSubscribeInfo(NotificationSubscribeInfo notificationSubscribeInfo) {
        this.appNames = new HashSet();
        this.deviceNames = new HashSet();
        if (notificationSubscribeInfo != null) {
            this.userId = notificationSubscribeInfo.userId;
            Set<String> set = notificationSubscribeInfo.appNames;
            if (set != null) {
                this.appNames = new HashSet(set);
            }
            Set<String> set2 = notificationSubscribeInfo.deviceNames;
            if (set2 != null) {
                this.deviceNames = new HashSet(set2);
            }
        }
    }

    public int getUserId() {
        return this.userId;
    }

    public NotificationSubscribeInfo setUserId(int i) {
        this.userId = i;
        return this;
    }

    public Set<String> getAppNames() {
        return new HashSet(this.appNames);
    }

    public NotificationSubscribeInfo addAppName(String str) {
        this.appNames.add(str);
        return this;
    }

    public NotificationSubscribeInfo addAppNames(String[] strArr) {
        if (strArr != null) {
            for (String str : strArr) {
                this.appNames.add(str);
            }
        }
        return this;
    }

    public Set<String> getDeviceNames() {
        return new HashSet(this.deviceNames);
    }

    public NotificationSubscribeInfo addDeviceName(String str) {
        this.deviceNames.add(str);
        return this;
    }

    public NotificationSubscribeInfo addDeviceName(String[] strArr) {
        if (strArr != null) {
            for (String str : strArr) {
                this.deviceNames.add(str);
            }
        }
        return this;
    }

    public boolean marshalling(Parcel parcel) {
        if (!parcel.writeInt(this.userId)) {
            HiLog.warn(LABEL, "write userId failed.", new Object[0]);
            return false;
        } else if (!writeStringSet(new HashSet(this.appNames), parcel)) {
            HiLog.warn(LABEL, "write appNames failed.", new Object[0]);
            return false;
        } else if (writeStringSet(new HashSet(this.deviceNames), parcel)) {
            return true;
        } else {
            HiLog.warn(LABEL, "write device names failed.", new Object[0]);
            return false;
        }
    }

    public boolean unmarshalling(Parcel parcel) {
        this.userId = parcel.readInt();
        this.appNames = readStringSet(parcel);
        this.deviceNames = readStringSet(parcel);
        return true;
    }

    private boolean writeStringSet(Set<String> set, Parcel parcel) {
        if (set == null || !parcel.writeInt(set.size())) {
            return false;
        }
        for (String str : set) {
            if (!parcel.writeString(str)) {
                return false;
            }
        }
        return true;
    }

    private Set<String> readStringSet(Parcel parcel) {
        int readInt = parcel.readInt();
        if (readInt <= 0 || readInt > 1048576) {
            return new HashSet();
        }
        HashSet hashSet = new HashSet();
        for (int i = 0; i < readInt; i++) {
            hashSet.add(parcel.readString());
        }
        return hashSet;
    }
}
