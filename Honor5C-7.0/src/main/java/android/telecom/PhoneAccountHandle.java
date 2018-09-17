package android.telecom;

import android.content.ComponentName;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import android.os.Process;
import android.os.UserHandle;
import android.util.Log;
import java.util.Objects;

public final class PhoneAccountHandle implements Parcelable {
    public static final Creator<PhoneAccountHandle> CREATOR = null;
    private final ComponentName mComponentName;
    private final String mId;
    private final UserHandle mUserHandle;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.telecom.PhoneAccountHandle.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.telecom.PhoneAccountHandle.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: android.telecom.PhoneAccountHandle.<clinit>():void");
    }

    public PhoneAccountHandle(ComponentName componentName, String id) {
        this(componentName, id, Process.myUserHandle());
    }

    public PhoneAccountHandle(ComponentName componentName, String id, UserHandle userHandle) {
        checkParameters(componentName, userHandle);
        this.mComponentName = componentName;
        this.mId = id;
        this.mUserHandle = userHandle;
    }

    public ComponentName getComponentName() {
        return this.mComponentName;
    }

    public String getId() {
        return this.mId;
    }

    public UserHandle getUserHandle() {
        return this.mUserHandle;
    }

    public int hashCode() {
        return Objects.hash(new Object[]{this.mComponentName, this.mId, this.mUserHandle});
    }

    public String toString() {
        return this.mComponentName + ", " + Log.pii(this.mId) + ", " + this.mUserHandle;
    }

    public boolean equals(Object other) {
        return (other != null && (other instanceof PhoneAccountHandle) && Objects.equals(((PhoneAccountHandle) other).getComponentName(), getComponentName()) && Objects.equals(((PhoneAccountHandle) other).getId(), getId())) ? Objects.equals(((PhoneAccountHandle) other).getUserHandle(), getUserHandle()) : false;
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel out, int flags) {
        this.mComponentName.writeToParcel(out, flags);
        out.writeString(this.mId);
        this.mUserHandle.writeToParcel(out, flags);
    }

    private void checkParameters(ComponentName componentName, UserHandle userHandle) {
        if (componentName == null) {
            Log.w("PhoneAccountHandle", new Exception("PhoneAccountHandle has been created with null ComponentName!"));
        }
        if (userHandle == null) {
            Log.w("PhoneAccountHandle", new Exception("PhoneAccountHandle has been created with null UserHandle!"));
        }
    }

    private PhoneAccountHandle(Parcel in) {
        this((ComponentName) ComponentName.CREATOR.createFromParcel(in), in.readString(), (UserHandle) UserHandle.CREATOR.createFromParcel(in));
    }
}
