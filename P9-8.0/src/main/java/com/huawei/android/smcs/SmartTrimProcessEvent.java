package com.huawei.android.smcs;

import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import java.util.HashSet;
import java.util.Iterator;

public abstract class SmartTrimProcessEvent implements Parcelable {
    public static final Creator<SmartTrimProcessEvent> CREATOR = new Creator<SmartTrimProcessEvent>() {
        public SmartTrimProcessEvent createFromParcel(Parcel source) {
            int event = source.readInt();
            switch (event) {
                case 0:
                    return new SmartTrimProcessAddRelation(source, event);
                case 1:
                    return new SmartTrimProcessPkgResume(source, event);
                default:
                    return null;
            }
        }

        public SmartTrimProcessEvent[] newArray(int size) {
            return new SmartTrimProcessEvent[size];
        }
    };
    public static final int STPE_ADD_RELATION = 0;
    public static final int STPE_PKG_RESUME = 1;
    public static final int STPE_TYPE_NUM = 2;
    public static final String ST_EVENT_INTER_STRING_TOKEN = ";";
    public static final String ST_EVENT_STRING_TOKEN = ",";
    private static final String TAG = "SmartTrimProcessEvent";
    private static final boolean mDebugLocalClass = false;
    public int mEvent = -1;

    public SmartTrimProcessEvent(int event) {
        this.mEvent = event;
    }

    SmartTrimProcessEvent(Parcel source) {
        readFromParcel(source);
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.mEvent);
    }

    public int describeContents() {
        return 0;
    }

    public void readFromParcel(Parcel source) {
        this.mEvent = source.readInt();
    }

    protected String[] hashSet2strings(HashSet<String> source) {
        int i = 0;
        if (source == null || source.size() == 0) {
            return null;
        }
        String[] dst = new String[source.size()];
        Iterator<String> it = source.iterator();
        while (it.hasNext()) {
            dst[i] = (String) it.next();
            i++;
        }
        return dst;
    }

    protected HashSet<String> strings2hashSet(String[] src) {
        if (src != null) {
            if (len != 0) {
                HashSet<String> dst = new HashSet();
                for (Object add : src) {
                    dst.add(add);
                }
                return dst;
            }
        }
        return null;
    }
}
