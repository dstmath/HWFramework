package android.os.health;

import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;

public class HealthStatsParceler implements Parcelable {
    public static final Creator<HealthStatsParceler> CREATOR = null;
    private HealthStats mHealthStats;
    private HealthStatsWriter mWriter;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.os.health.HealthStatsParceler.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.os.health.HealthStatsParceler.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 5 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 6 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: android.os.health.HealthStatsParceler.<clinit>():void");
    }

    public HealthStatsParceler(HealthStatsWriter writer) {
        this.mWriter = writer;
    }

    public HealthStatsParceler(Parcel in) {
        this.mHealthStats = new HealthStats(in);
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel out, int flags) {
        if (this.mWriter != null) {
            this.mWriter.flattenToParcel(out);
            return;
        }
        throw new RuntimeException("Can not re-parcel HealthStatsParceler that was constructed from a Parcel");
    }

    public HealthStats getHealthStats() {
        if (this.mWriter != null) {
            Parcel parcel = Parcel.obtain();
            this.mWriter.flattenToParcel(parcel);
            parcel.setDataPosition(0);
            this.mHealthStats = new HealthStats(parcel);
            parcel.recycle();
        }
        return this.mHealthStats;
    }
}
