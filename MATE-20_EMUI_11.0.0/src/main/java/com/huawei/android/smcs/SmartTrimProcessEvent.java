package com.huawei.android.smcs;

import android.os.Parcel;
import android.os.Parcelable;
import java.util.HashSet;
import java.util.Iterator;

public abstract class SmartTrimProcessEvent implements Parcelable {
    public static final Parcelable.Creator<SmartTrimProcessEvent> CREATOR = new Parcelable.Creator<SmartTrimProcessEvent>() {
        /* class com.huawei.android.smcs.SmartTrimProcessEvent.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public SmartTrimProcessEvent createFromParcel(Parcel source) {
            int event = source.readInt();
            if (event == 0) {
                return new SmartTrimProcessAddRelation(source, event);
            }
            if (event != 1) {
                return null;
            }
            return new SmartTrimProcessPkgResume(source, event);
        }

        @Override // android.os.Parcelable.Creator
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

    @Override // android.os.Parcelable
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.mEvent);
    }

    @Override // android.os.Parcelable
    public int describeContents() {
        return 0;
    }

    public void readFromParcel(Parcel source) {
        this.mEvent = source.readInt();
    }

    /* access modifiers changed from: protected */
    public String[] hashSet2strings(HashSet<String> source) {
        int i = 0;
        if (source == null || source.size() == 0) {
            return null;
        }
        String[] dst = new String[source.size()];
        Iterator<String> it = source.iterator();
        while (it.hasNext()) {
            dst[i] = it.next();
            i++;
        }
        return dst;
    }

    /* access modifiers changed from: protected */
    public HashSet<String> strings2hashSet(String[] src) {
        if (src == null || (len = src.length) == 0) {
            return null;
        }
        HashSet<String> dst = new HashSet<>();
        for (String str : src) {
            dst.add(str);
        }
        return dst;
    }
}
