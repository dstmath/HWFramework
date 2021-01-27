package com.huawei.wifi2;

import com.huawei.wifi2.HwWifi2Native;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

public class HwWifiIface {
    public static final int IFACE_TYPE_AP = 0;
    public static final int IFACE_TYPE_STA_FOR_CONNECTIVITY = 1;
    public static final int IFACE_TYPE_STA_FOR_SCAN = 2;
    protected HwWifi2Native.InterfaceCallback externalListener;
    protected final int id;
    protected boolean isUp;
    protected String name;
    protected HwWifi2Native.NetworkObserverInternal networkObserver;
    protected final int type;

    @Retention(RetentionPolicy.SOURCE)
    public @interface IfaceType {
    }

    HwWifiIface(int id2, int type2) {
        this.id = id2;
        this.type = type2;
    }

    public String toString() {
        String typeString;
        StringBuffer sb = new StringBuffer();
        int i = this.type;
        if (i == 0) {
            typeString = "AP";
        } else if (i == 1) {
            typeString = "STA_CONNECTIVITY";
        } else if (i != 2) {
            typeString = "<UNKNOWN>";
        } else {
            typeString = "STA_SCAN";
        }
        sb.append("Iface:");
        sb.append("{");
        sb.append("Name=");
        sb.append(this.name);
        sb.append(",");
        sb.append("Id=");
        sb.append(this.id);
        sb.append(",");
        sb.append("Type=");
        sb.append(typeString);
        sb.append("}");
        return sb.toString();
    }
}
