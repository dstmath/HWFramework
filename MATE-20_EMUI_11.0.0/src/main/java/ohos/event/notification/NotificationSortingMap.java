package ohos.event.notification;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import ohos.event.EventConstant;
import ohos.hiviewdfx.HiLog;
import ohos.hiviewdfx.HiLogLabel;
import ohos.utils.Parcel;
import ohos.utils.Sequenceable;

public class NotificationSortingMap implements Sequenceable {
    private static final HiLogLabel LABEL = new HiLogLabel(3, (int) EventConstant.NOTIFICATION_DOMAIN, TAG);
    private static final int MAX_NUM = 1048576;
    public static final Sequenceable.Producer<NotificationSortingMap> PRODUCER = $$Lambda$NotificationSortingMap$wG6PWuu7vJdvS2RWB_Xlnqw3GmU.INSTANCE;
    private static final String TAG = "NotificationSortingMap";
    private List<String> sortedHashCode;
    private Map<String, NotificationSorting> sortings;

    static /* synthetic */ NotificationSortingMap lambda$static$0(Parcel parcel) {
        NotificationSortingMap notificationSortingMap = new NotificationSortingMap();
        notificationSortingMap.unmarshalling(parcel);
        return notificationSortingMap;
    }

    public NotificationSortingMap() {
        this(null);
    }

    public List<String> getHashCode() {
        return this.sortedHashCode;
    }

    public boolean getNotificationSorting(String str, NotificationSorting notificationSorting) {
        if (str == null || notificationSorting == null || !this.sortings.containsKey(str)) {
            return false;
        }
        notificationSorting.cloneNotificationSorting(this.sortings.get(str));
        return true;
    }

    public boolean marshalling(Parcel parcel) {
        int size = this.sortedHashCode.size();
        if (size > 1048576) {
            return false;
        }
        if (!parcel.writeInt(size)) {
            HiLog.warn(LABEL, "write size of sortedHashCode failed.", new Object[0]);
            return false;
        }
        for (int i = 0; i < size; i++) {
            if (!parcel.writeString(this.sortedHashCode.get(i))) {
                HiLog.warn(LABEL, "write hashCode failed.", new Object[0]);
                return false;
            }
        }
        if (!parcel.writeInt(this.sortings.size())) {
            HiLog.warn(LABEL, "write size of sortings failed.", new Object[0]);
            return false;
        }
        for (Map.Entry<String, NotificationSorting> entry : this.sortings.entrySet()) {
            if (!parcel.writeString(entry.getKey())) {
                HiLog.warn(LABEL, "write key of sortings failed.", new Object[0]);
                return false;
            }
            parcel.writeSequenceable(entry.getValue());
        }
        return true;
    }

    public boolean unmarshalling(Parcel parcel) {
        this.sortedHashCode.clear();
        int readInt = parcel.readInt();
        if (readInt > 1048576) {
            return false;
        }
        for (int i = 0; i < readInt; i++) {
            this.sortedHashCode.add(parcel.readString());
        }
        this.sortings.clear();
        int readInt2 = parcel.readInt();
        if (readInt2 > 1048576) {
            return false;
        }
        for (int i2 = 0; i2 < readInt2; i2++) {
            String readString = parcel.readString();
            NotificationSorting notificationSorting = new NotificationSorting();
            if (!parcel.readSequenceable(notificationSorting)) {
                return false;
            }
            this.sortings.put(readString, notificationSorting);
        }
        return true;
    }

    NotificationSortingMap(List<NotificationSorting> list) {
        String hashCode;
        this.sortedHashCode = new ArrayList();
        this.sortings = new HashMap();
        if (list != null) {
            int i = 0;
            for (NotificationSorting notificationSorting : list) {
                if (!(notificationSorting == null || (hashCode = notificationSorting.getHashCode()) == null)) {
                    this.sortedHashCode.add(hashCode);
                    this.sortings.put(hashCode, notificationSorting);
                    i++;
                    if (i >= 1048576) {
                        return;
                    }
                }
            }
        }
    }
}
