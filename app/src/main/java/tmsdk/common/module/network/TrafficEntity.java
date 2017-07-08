package tmsdk.common.module.network;

import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import android.text.TextUtils;

/* compiled from: Unknown */
public final class TrafficEntity implements Parcelable {
    public static Creator<TrafficEntity> CREATOR;
    public long mLastDownValue;
    public long mLastUpValue;
    public long mMobileDownValue;
    public long mMobileUpValue;
    public String mPkg;
    public long mWIFIDownValue;
    public long mWIFIUpValue;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: tmsdk.common.module.network.TrafficEntity.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: tmsdk.common.module.network.TrafficEntity.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: tmsdk.common.module.network.TrafficEntity.<clinit>():void");
    }

    public TrafficEntity() {
        this.mLastUpValue = 0;
        this.mLastDownValue = 0;
        this.mMobileUpValue = 0;
        this.mMobileDownValue = 0;
        this.mWIFIUpValue = 0;
        this.mWIFIDownValue = 0;
    }

    public static TrafficEntity fromString(String str) {
        TrafficEntity trafficEntity = null;
        if (!TextUtils.isEmpty(str)) {
            TrafficEntity trafficEntity2 = new TrafficEntity();
            String[] split = str.trim().split("[,:]");
            try {
                trafficEntity2.mPkg = split[0];
                trafficEntity2.mLastUpValue = Long.parseLong(split[1]);
                trafficEntity2.mLastDownValue = Long.parseLong(split[2]);
                trafficEntity2.mMobileUpValue = Long.parseLong(split[3]);
                trafficEntity2.mMobileDownValue = Long.parseLong(split[4]);
                trafficEntity2.mWIFIUpValue = Long.parseLong(split[5]);
                trafficEntity2.mWIFIDownValue = Long.parseLong(split[6]);
                trafficEntity = trafficEntity2;
            } catch (NumberFormatException e) {
                return null;
            } catch (ArrayIndexOutOfBoundsException e2) {
                return null;
            } catch (Exception e3) {
                return null;
            }
        }
        return trafficEntity;
    }

    public static String toString(TrafficEntity trafficEntity) {
        return String.format("%s,%s,%s,%s,%s,%s,%s", new Object[]{trafficEntity.mPkg, Long.valueOf(trafficEntity.mLastUpValue), Long.valueOf(trafficEntity.mLastDownValue), Long.valueOf(trafficEntity.mMobileUpValue), Long.valueOf(trafficEntity.mMobileDownValue), Long.valueOf(trafficEntity.mWIFIUpValue), Long.valueOf(trafficEntity.mWIFIDownValue)});
    }

    public int describeContents() {
        return 0;
    }

    public String toString() {
        return toString(this);
    }

    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(this.mPkg);
        parcel.writeLong(this.mLastUpValue);
        parcel.writeLong(this.mLastDownValue);
        parcel.writeLong(this.mMobileUpValue);
        parcel.writeLong(this.mMobileDownValue);
        parcel.writeLong(this.mWIFIUpValue);
        parcel.writeLong(this.mWIFIDownValue);
    }
}
