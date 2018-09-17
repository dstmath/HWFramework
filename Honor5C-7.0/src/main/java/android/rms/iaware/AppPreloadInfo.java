package android.rms.iaware;

import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;

public class AppPreloadInfo implements Parcelable {
    public static final Creator<AppPreloadInfo> CREATOR = null;
    private int coldstartTime;
    private String packageName;
    private int powerDissipation;
    private int preloadMem;
    private int warmstartTime;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.rms.iaware.AppPreloadInfo.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.rms.iaware.AppPreloadInfo.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: android.rms.iaware.AppPreloadInfo.<clinit>():void");
    }

    public AppPreloadInfo(Parcel source) {
        this.packageName = source.readString();
        this.powerDissipation = source.readInt();
        this.preloadMem = source.readInt();
        this.coldstartTime = source.readInt();
        this.warmstartTime = source.readInt();
    }

    public String getPackageName() {
        return this.packageName;
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }

    public int getPowerDissipation() {
        return this.powerDissipation;
    }

    public void setPowerDissipation(int powerDissipation) {
        this.powerDissipation = powerDissipation;
    }

    public int getPreloadMem() {
        return this.preloadMem;
    }

    public void setPreloadMem(int preloadMem) {
        this.preloadMem = preloadMem;
    }

    public int getColdstartTime() {
        return this.coldstartTime;
    }

    public void setColdstartTime(int coldstartTime) {
        this.coldstartTime = coldstartTime;
    }

    public int getWarmstartTime() {
        return this.warmstartTime;
    }

    public void setWarmstartTime(int warmstartTime) {
        this.warmstartTime = warmstartTime;
    }

    public String toString() {
        return "AppPreloadInfo [packageName=" + this.packageName + ", powerDissipation=" + this.powerDissipation + ", preloadMem=" + this.preloadMem + ", coldstartTime=" + this.coldstartTime + ", warmstartTime=" + this.warmstartTime + "]";
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.packageName);
        dest.writeInt(this.powerDissipation);
        dest.writeInt(this.preloadMem);
        dest.writeInt(this.coldstartTime);
        dest.writeInt(this.warmstartTime);
    }
}
