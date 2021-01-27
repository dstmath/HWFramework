package ohos.event.commonevent;

import java.util.ArrayList;
import java.util.List;
import ohos.event.EventConstant;
import ohos.hiviewdfx.HiLog;
import ohos.hiviewdfx.HiLogLabel;
import ohos.utils.Parcel;
import ohos.utils.Sequenceable;

public class CommonEventConvertInfo implements Sequenceable {
    private static final HiLogLabel LABEL = new HiLogLabel(3, (int) EventConstant.COMMON_EVENT_DOMAIN, TAG);
    public static final Sequenceable.Producer<CommonEventConvertInfo> PRODUCER = $$Lambda$CommonEventConvertInfo$jh6nTWe5nt7BhS2wyQ64QABSag4.INSTANCE;
    private static final String TAG = "CommonEventConvertInfo";
    private List<String> actions;
    private String className;
    private String jarName;

    static /* synthetic */ CommonEventConvertInfo lambda$static$0(Parcel parcel) {
        CommonEventConvertInfo commonEventConvertInfo = new CommonEventConvertInfo();
        commonEventConvertInfo.unmarshalling(parcel);
        return commonEventConvertInfo;
    }

    public CommonEventConvertInfo() {
        this(null, null, null);
    }

    public CommonEventConvertInfo(String str, String str2, List<String> list) {
        this.jarName = str;
        this.className = str2;
        this.actions = list;
    }

    public String getJarName() {
        return this.jarName;
    }

    public void setJarName(String str) {
        this.jarName = str;
    }

    public String getClassName() {
        return this.className;
    }

    public void setClassName(String str) {
        this.className = str;
    }

    public List<String> getActions() {
        return this.actions;
    }

    public void setActions(List<String> list) {
        this.actions = list;
    }

    public boolean marshalling(Parcel parcel) {
        if (!parcel.writeString(this.jarName)) {
            HiLog.warn(LABEL, "write jarName failed.", new Object[0]);
            return false;
        } else if (!parcel.writeString(this.className)) {
            HiLog.warn(LABEL, "write className failed.", new Object[0]);
            return false;
        } else {
            List<String> list = this.actions;
            if (list == null || list.isEmpty()) {
                if (!parcel.writeInt(0)) {
                    return false;
                }
                return true;
            } else if (!parcel.writeInt(this.actions.size())) {
                return false;
            } else {
                for (String str : this.actions) {
                    if (!parcel.writeString(str)) {
                        HiLog.warn(LABEL, "write action failed.", new Object[0]);
                        return false;
                    }
                }
                return true;
            }
        }
    }

    public boolean unmarshalling(Parcel parcel) {
        this.jarName = parcel.readString();
        this.className = parcel.readString();
        int readInt = parcel.readInt();
        if (readInt <= 0) {
            this.actions = null;
            return true;
        }
        this.actions = new ArrayList();
        while (readInt > 0) {
            this.actions.add(parcel.readString());
            readInt--;
        }
        return true;
    }
}
