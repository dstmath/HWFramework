package android.net;

import android.net.ICaptivePortal.Stub;
import android.os.IBinder;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import android.os.RemoteException;

public class CaptivePortal implements Parcelable {
    public static final int APP_RETURN_DISMISSED = 0;
    public static final int APP_RETURN_UNWANTED = 1;
    public static final int APP_RETURN_WANTED_AS_IS = 2;
    public static final Creator<CaptivePortal> CREATOR = null;
    private final IBinder mBinder;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.net.CaptivePortal.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.net.CaptivePortal.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: android.net.CaptivePortal.<clinit>():void");
    }

    public CaptivePortal(IBinder binder) {
        this.mBinder = binder;
    }

    public int describeContents() {
        return APP_RETURN_DISMISSED;
    }

    public void writeToParcel(Parcel out, int flags) {
        out.writeStrongBinder(this.mBinder);
    }

    public void reportCaptivePortalDismissed() {
        try {
            Stub.asInterface(this.mBinder).appResponse(APP_RETURN_DISMISSED);
        } catch (RemoteException e) {
        }
    }

    public void ignoreNetwork() {
        try {
            Stub.asInterface(this.mBinder).appResponse(APP_RETURN_UNWANTED);
        } catch (RemoteException e) {
        }
    }

    public void useNetwork() {
        try {
            Stub.asInterface(this.mBinder).appResponse(APP_RETURN_WANTED_AS_IS);
        } catch (RemoteException e) {
        }
    }
}
