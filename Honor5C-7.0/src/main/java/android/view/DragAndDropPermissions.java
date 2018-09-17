package android.view;

import android.app.ActivityManagerNative;
import android.os.IBinder;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import android.os.RemoteException;
import com.android.internal.view.IDragAndDropPermissions;
import com.android.internal.view.IDragAndDropPermissions.Stub;

public final class DragAndDropPermissions implements Parcelable {
    public static final Creator<DragAndDropPermissions> CREATOR = null;
    private final IDragAndDropPermissions mDragAndDropPermissions;
    private IBinder mPermissionOwnerToken;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.view.DragAndDropPermissions.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.view.DragAndDropPermissions.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: android.view.DragAndDropPermissions.<clinit>():void");
    }

    public static DragAndDropPermissions obtain(DragEvent dragEvent) {
        if (dragEvent.getDragAndDropPermissions() == null) {
            return null;
        }
        return new DragAndDropPermissions(dragEvent.getDragAndDropPermissions());
    }

    private DragAndDropPermissions(IDragAndDropPermissions dragAndDropPermissions) {
        this.mDragAndDropPermissions = dragAndDropPermissions;
    }

    public boolean take(IBinder activityToken) {
        try {
            this.mDragAndDropPermissions.take(activityToken);
            return true;
        } catch (RemoteException e) {
            return false;
        }
    }

    public boolean takeTransient() {
        try {
            this.mPermissionOwnerToken = ActivityManagerNative.getDefault().newUriPermissionOwner("drop");
            this.mDragAndDropPermissions.takeTransient(this.mPermissionOwnerToken);
            return true;
        } catch (RemoteException e) {
            return false;
        }
    }

    public void release() {
        try {
            this.mDragAndDropPermissions.release();
            this.mPermissionOwnerToken = null;
        } catch (RemoteException e) {
        }
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel destination, int flags) {
        destination.writeStrongInterface(this.mDragAndDropPermissions);
        destination.writeStrongBinder(this.mPermissionOwnerToken);
    }

    private DragAndDropPermissions(Parcel in) {
        this.mDragAndDropPermissions = Stub.asInterface(in.readStrongBinder());
        this.mPermissionOwnerToken = in.readStrongBinder();
    }
}
