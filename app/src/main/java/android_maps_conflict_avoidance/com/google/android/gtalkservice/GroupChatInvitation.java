package android_maps_conflict_avoidance.com.google.android.gtalkservice;

import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;

public class GroupChatInvitation implements Parcelable {
    public static final Creator<GroupChatInvitation> CREATOR = null;
    private long mGroupContactId;
    private String mInviter;
    private String mPassword;
    private String mReason;
    private String mRoomAddress;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android_maps_conflict_avoidance.com.google.android.gtalkservice.GroupChatInvitation.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android_maps_conflict_avoidance.com.google.android.gtalkservice.GroupChatInvitation.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: android_maps_conflict_avoidance.com.google.android.gtalkservice.GroupChatInvitation.<clinit>():void");
    }

    public GroupChatInvitation(Parcel source) {
        this.mRoomAddress = source.readString();
        this.mInviter = source.readString();
        this.mReason = source.readString();
        this.mPassword = source.readString();
        this.mGroupContactId = source.readLong();
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.mRoomAddress);
        dest.writeString(this.mInviter);
        dest.writeString(this.mReason);
        dest.writeString(this.mPassword);
        dest.writeLong(this.mGroupContactId);
    }

    public int describeContents() {
        return 0;
    }
}
