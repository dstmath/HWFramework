package android.media.projection;

import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import android.os.UserHandle;
import java.util.Objects;

public final class MediaProjectionInfo implements Parcelable {
    public static final Creator<MediaProjectionInfo> CREATOR = null;
    private final String mPackageName;
    private final UserHandle mUserHandle;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.media.projection.MediaProjectionInfo.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.media.projection.MediaProjectionInfo.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: android.media.projection.MediaProjectionInfo.<clinit>():void");
    }

    public MediaProjectionInfo(String packageName, UserHandle handle) {
        this.mPackageName = packageName;
        this.mUserHandle = handle;
    }

    public MediaProjectionInfo(Parcel in) {
        this.mPackageName = in.readString();
        this.mUserHandle = UserHandle.readFromParcel(in);
    }

    public String getPackageName() {
        return this.mPackageName;
    }

    public UserHandle getUserHandle() {
        return this.mUserHandle;
    }

    public boolean equals(Object o) {
        boolean z = false;
        if (!(o instanceof MediaProjectionInfo)) {
            return false;
        }
        MediaProjectionInfo other = (MediaProjectionInfo) o;
        if (Objects.equals(other.mPackageName, this.mPackageName)) {
            z = Objects.equals(other.mUserHandle, this.mUserHandle);
        }
        return z;
    }

    public int hashCode() {
        return Objects.hash(new Object[]{this.mPackageName, this.mUserHandle});
    }

    public String toString() {
        return "MediaProjectionInfo{mPackageName=" + this.mPackageName + ", mUserHandle=" + this.mUserHandle + "}";
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel out, int flags) {
        out.writeString(this.mPackageName);
        UserHandle.writeToParcel(this.mUserHandle, out);
    }
}
