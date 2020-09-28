package android.app;

import android.app.ActivityManager;
import android.os.Parcel;
import android.os.Parcelable;

public class HwRecentTaskInfo extends ActivityManager.RecentTaskInfo {
    public static final Parcelable.Creator<HwRecentTaskInfo> CREATOR = new Parcelable.Creator<HwRecentTaskInfo>() {
        /* class android.app.HwRecentTaskInfo.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public HwRecentTaskInfo createFromParcel(Parcel source) {
            return new HwRecentTaskInfo(source);
        }

        @Override // android.os.Parcelable.Creator
        public HwRecentTaskInfo[] newArray(int size) {
            return new HwRecentTaskInfo[size];
        }
    };
    public int displayId;
    public boolean isStackVisibility;
    public int systemUiVisibility;
    public int windowState;

    public HwRecentTaskInfo() {
    }

    @Override // android.os.Parcelable, android.app.ActivityManager.RecentTaskInfo
    public int describeContents() {
        return 0;
    }

    @Override // android.os.Parcelable, android.app.ActivityManager.RecentTaskInfo, android.app.TaskInfo
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
        dest.writeInt(this.windowState);
        dest.writeInt(this.displayId);
        dest.writeInt(this.systemUiVisibility);
        dest.writeBoolean(this.isStackVisibility);
    }

    @Override // android.app.ActivityManager.RecentTaskInfo, android.app.TaskInfo
    public void readFromParcel(Parcel source) {
        super.readFromParcel(source);
        this.windowState = source.readInt();
        this.displayId = source.readInt();
        this.systemUiVisibility = source.readInt();
        this.isStackVisibility = source.readBoolean();
    }

    private HwRecentTaskInfo(Parcel source) {
        readFromParcel(source);
    }

    public void translateRecentTaskinfo(ActivityManager.RecentTaskInfo rti) {
        this.id = rti.id;
        this.persistentId = rti.persistentId;
        this.baseIntent = rti.baseIntent;
        this.origActivity = rti.origActivity;
        this.realActivity = rti.realActivity;
        this.description = rti.description;
        this.stackId = rti.stackId;
        this.userId = rti.userId;
        this.taskDescription = rti.taskDescription;
        this.lastActiveTime = rti.lastActiveTime;
        this.affiliatedTaskId = rti.affiliatedTaskId;
        this.numActivities = rti.numActivities;
        this.supportsSplitScreenMultiWindow = rti.supportsSplitScreenMultiWindow;
        this.resizeMode = rti.resizeMode;
        this.numActivities = rti.numActivities;
        this.baseActivity = rti.baseActivity;
        this.topActivity = rti.topActivity;
    }
}
