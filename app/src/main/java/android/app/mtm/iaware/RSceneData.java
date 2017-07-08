package android.app.mtm.iaware;

import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;

public class RSceneData implements Parcelable {
    public static final Creator<RSceneData> CREATOR = null;
    private Bundle mBundle;
    private int mFeatureId;
    private int mTypeId;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.app.mtm.iaware.RSceneData.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.app.mtm.iaware.RSceneData.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: android.app.mtm.iaware.RSceneData.<clinit>():void");
    }

    public RSceneData(int typeId, int featureId, Bundle bundle) {
        this.mTypeId = typeId;
        this.mFeatureId = featureId;
        this.mBundle = bundle;
    }

    public int getTypeId() {
        return this.mTypeId;
    }

    public void setTypeId(int typeId) {
        this.mTypeId = typeId;
    }

    public int getFeatureId() {
        return this.mFeatureId;
    }

    public void setFeatureId(int featureId) {
        this.mFeatureId = featureId;
    }

    public Bundle getBundle() {
        return this.mBundle;
    }

    public void setBundle(Bundle bundle) {
        this.mBundle = bundle;
    }

    public String toString() {
        return "RSceneData[mTypeId=" + this.mTypeId + ", mFeatureId=" + this.mFeatureId + ", mBundle=" + this.mBundle + "]";
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.mTypeId);
        dest.writeInt(this.mFeatureId);
        dest.writeBundle(this.mBundle);
    }
}
