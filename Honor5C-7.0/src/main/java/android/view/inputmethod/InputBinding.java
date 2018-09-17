package android.view.inputmethod;

import android.os.IBinder;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;

public final class InputBinding implements Parcelable {
    public static final Creator<InputBinding> CREATOR = null;
    static final String TAG = "InputBinding";
    final InputConnection mConnection;
    final IBinder mConnectionToken;
    final int mPid;
    final int mUid;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.view.inputmethod.InputBinding.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.view.inputmethod.InputBinding.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: android.view.inputmethod.InputBinding.<clinit>():void");
    }

    public InputBinding(InputConnection conn, IBinder connToken, int uid, int pid) {
        this.mConnection = conn;
        this.mConnectionToken = connToken;
        this.mUid = uid;
        this.mPid = pid;
    }

    public InputBinding(InputConnection conn, InputBinding binding) {
        this.mConnection = conn;
        this.mConnectionToken = binding.getConnectionToken();
        this.mUid = binding.getUid();
        this.mPid = binding.getPid();
    }

    InputBinding(Parcel source) {
        this.mConnection = null;
        this.mConnectionToken = source.readStrongBinder();
        this.mUid = source.readInt();
        this.mPid = source.readInt();
    }

    public InputConnection getConnection() {
        return this.mConnection;
    }

    public IBinder getConnectionToken() {
        return this.mConnectionToken;
    }

    public int getUid() {
        return this.mUid;
    }

    public int getPid() {
        return this.mPid;
    }

    public String toString() {
        return "InputBinding{" + this.mConnectionToken + " / uid " + this.mUid + " / pid " + this.mPid + "}";
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeStrongBinder(this.mConnectionToken);
        dest.writeInt(this.mUid);
        dest.writeInt(this.mPid);
    }

    public int describeContents() {
        return 0;
    }
}
