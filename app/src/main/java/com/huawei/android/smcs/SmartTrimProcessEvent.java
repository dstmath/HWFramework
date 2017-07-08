package com.huawei.android.smcs;

import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import java.util.HashSet;
import java.util.Iterator;

public abstract class SmartTrimProcessEvent implements Parcelable {
    public static final Creator<SmartTrimProcessEvent> CREATOR = null;
    public static final int STPE_ADD_RELATION = 0;
    public static final int STPE_PKG_RESUME = 1;
    public static final int STPE_TYPE_NUM = 2;
    public static final String ST_EVENT_INTER_STRING_TOKEN = ";";
    public static final String ST_EVENT_STRING_TOKEN = ",";
    private static final String TAG = "SmartTrimProcessEvent";
    private static final boolean mDebugLocalClass = false;
    public int mEvent;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.huawei.android.smcs.SmartTrimProcessEvent.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.huawei.android.smcs.SmartTrimProcessEvent.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 7 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 8 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: com.huawei.android.smcs.SmartTrimProcessEvent.<clinit>():void");
    }

    public SmartTrimProcessEvent(int event) {
        this.mEvent = -1;
        this.mEvent = event;
    }

    SmartTrimProcessEvent(Parcel source) {
        this.mEvent = -1;
        readFromParcel(source);
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.mEvent);
    }

    public int describeContents() {
        return STPE_ADD_RELATION;
    }

    public void readFromParcel(Parcel source) {
        this.mEvent = source.readInt();
    }

    protected String[] hashSet2strings(HashSet<String> source) {
        int i = STPE_ADD_RELATION;
        if (source == null || source.size() == 0) {
            return null;
        }
        String[] dst = new String[source.size()];
        Iterator<String> it = source.iterator();
        while (it.hasNext()) {
            dst[i] = (String) it.next();
            i += STPE_PKG_RESUME;
        }
        return dst;
    }

    protected HashSet<String> strings2hashSet(String[] src) {
        if (src != null) {
            int len = src.length;
            if (len != 0) {
                HashSet<String> dst = new HashSet();
                for (int i = STPE_ADD_RELATION; i < len; i += STPE_PKG_RESUME) {
                    dst.add(src[i]);
                }
                return dst;
            }
        }
        return null;
    }
}
