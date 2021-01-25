package ohos.bundle;

import java.util.ArrayList;
import java.util.List;
import ohos.utils.Parcel;
import ohos.utils.Sequenceable;

public class CommonEventInfo implements Sequenceable {
    private static final int MAX_LIMIT_SIZE = 50;
    public static final Sequenceable.Producer<CommonEventInfo> PRODUCER = $$Lambda$CommonEventInfo$Agf5u1uG85TCogQ6r7kfQnGAnpY.INSTANCE;
    private String bundleName = "";
    private String className = "";
    private List<String> data = new ArrayList();
    private List<String> events = new ArrayList();
    private String permission = "";
    private List<String> type = new ArrayList();
    private int uid = -1;

    public CommonEventInfo() {
    }

    public CommonEventInfo(CommonEventInfo commonEventInfo) {
        this.bundleName = commonEventInfo.bundleName;
        this.uid = commonEventInfo.uid;
        this.className = commonEventInfo.className;
        this.permission = commonEventInfo.permission;
        this.data.addAll(commonEventInfo.data);
        this.type.addAll(commonEventInfo.type);
        this.events.addAll(commonEventInfo.events);
    }

    static /* synthetic */ CommonEventInfo lambda$static$0(Parcel parcel) {
        CommonEventInfo commonEventInfo = new CommonEventInfo();
        commonEventInfo.unmarshalling(parcel);
        return commonEventInfo;
    }

    public String getBundleName() {
        return this.bundleName;
    }

    public void setBundleName(String str) {
        this.bundleName = str;
    }

    public int getUid() {
        return this.uid;
    }

    public void setUid(int i) {
        this.uid = i;
    }

    public String getClassName() {
        return this.className;
    }

    public void setClassName(String str) {
        this.className = str;
    }

    public String getPermission() {
        return this.permission;
    }

    public void setPermission(String str) {
        this.permission = str;
    }

    public List<String> getData() {
        return this.data;
    }

    public void setData(List<String> list) {
        this.data = list;
    }

    public List<String> getType() {
        return this.type;
    }

    public void setType(List<String> list) {
        this.type = list;
    }

    public List<String> getEvents() {
        return this.events;
    }

    public void setEvents(List<String> list) {
        this.events = list;
    }

    public boolean marshalling(Parcel parcel) {
        if (!(parcel.writeString(this.bundleName) && parcel.writeInt(this.uid) && parcel.writeString(this.className) && parcel.writeString(this.permission) && parcel.writeInt(this.data.size()))) {
            return false;
        }
        for (String str : this.data) {
            if (!parcel.writeString(str)) {
                return false;
            }
        }
        if (!parcel.writeInt(this.type.size())) {
            return false;
        }
        for (String str2 : this.type) {
            if (!parcel.writeString(str2)) {
                return false;
            }
        }
        if (!parcel.writeInt(this.events.size())) {
            return false;
        }
        for (String str3 : this.events) {
            if (!parcel.writeString(str3)) {
                return false;
            }
        }
        return true;
    }

    public boolean unmarshalling(Parcel parcel) {
        this.bundleName = parcel.readString();
        this.uid = parcel.readInt();
        this.className = parcel.readString();
        this.permission = parcel.readString();
        int readInt = parcel.readInt();
        if (readInt > 50) {
            return false;
        }
        for (int i = 0; i < readInt; i++) {
            this.data.add(parcel.readString());
        }
        int readInt2 = parcel.readInt();
        if (readInt2 > 50) {
            return false;
        }
        for (int i2 = 0; i2 < readInt2; i2++) {
            this.type.add(parcel.readString());
        }
        int readInt3 = parcel.readInt();
        if (readInt3 > 50) {
            return false;
        }
        for (int i3 = 0; i3 < readInt3; i3++) {
            this.events.add(parcel.readString());
        }
        return true;
    }
}
