package android_maps_conflict_avoidance.com.google.android.gtalkservice;

import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;

public class GroupChatInvitation implements Parcelable {
    public static final Creator<GroupChatInvitation> CREATOR = new Creator<GroupChatInvitation>() {
        public GroupChatInvitation createFromParcel(Parcel source) {
            return new GroupChatInvitation(source);
        }

        public GroupChatInvitation[] newArray(int size) {
            return new GroupChatInvitation[size];
        }
    };
    private long mGroupContactId;
    private String mInviter;
    private String mPassword;
    private String mReason;
    private String mRoomAddress;

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
