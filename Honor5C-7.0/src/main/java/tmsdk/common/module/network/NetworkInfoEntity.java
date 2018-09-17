package tmsdk.common.module.network;

import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import java.util.Date;
import tmsdkobf.jf;

/* compiled from: Unknown */
public class NetworkInfoEntity extends jf implements Parcelable, Comparable<NetworkInfoEntity> {
    public static final Creator<NetworkInfoEntity> CREATOR = null;
    public long mRetialForMonth;
    public Date mStartDate;
    public long mTotalForMonth;
    public long mUsedForDay;
    public long mUsedForMonth;
    public long mUsedReceiveForDay;
    public long mUsedReceiveForMonth;
    public long mUsedTranslateForDay;
    public long mUsedTranslateForMonth;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: tmsdk.common.module.network.NetworkInfoEntity.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: tmsdk.common.module.network.NetworkInfoEntity.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: tmsdk.common.module.network.NetworkInfoEntity.<clinit>():void");
    }

    public NetworkInfoEntity() {
        this.mTotalForMonth = 0;
        this.mUsedForMonth = 0;
        this.mUsedTranslateForMonth = 0;
        this.mUsedReceiveForMonth = 0;
        this.mRetialForMonth = 0;
        this.mUsedForDay = 0;
        this.mUsedTranslateForDay = 0;
        this.mUsedReceiveForDay = 0;
        this.mStartDate = new Date();
    }

    public int compareTo(NetworkInfoEntity networkInfoEntity) {
        return this.mStartDate.compareTo(networkInfoEntity.mStartDate);
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeLong(this.mTotalForMonth);
        parcel.writeLong(this.mUsedForMonth);
        parcel.writeLong(this.mUsedTranslateForMonth);
        parcel.writeLong(this.mUsedReceiveForMonth);
        parcel.writeLong(this.mRetialForMonth);
        parcel.writeLong(this.mUsedForDay);
        parcel.writeLong(this.mUsedTranslateForDay);
        parcel.writeLong(this.mUsedReceiveForDay);
        parcel.writeSerializable(this.mStartDate);
    }
}
