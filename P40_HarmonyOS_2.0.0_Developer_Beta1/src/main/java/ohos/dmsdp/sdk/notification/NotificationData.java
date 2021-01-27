package ohos.dmsdp.sdk.notification;

import android.os.Parcel;
import android.os.Parcelable;

public class NotificationData implements Parcelable {
    public static final Parcelable.Creator<NotificationData> CREATOR = new Parcelable.Creator<NotificationData>() {
        /* class ohos.dmsdp.sdk.notification.NotificationData.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public NotificationData createFromParcel(Parcel parcel) {
            return new NotificationData(parcel);
        }

        @Override // android.os.Parcelable.Creator
        public NotificationData[] newArray(int i) {
            return new NotificationData[i];
        }
    };
    private String mContent;
    private long mDate;
    private int mGuideDirectionId;
    private int mGuideDistance;
    private String mGuideDistanceUnit;
    private String mGuideText;
    private int mIconId;
    private String mPackageName;
    private String mSubtitle;
    private int mTemplate;
    private String mTitle;
    private int mVibrate;

    @Override // android.os.Parcelable
    public int describeContents() {
        return 0;
    }

    public NotificationData() {
    }

    protected NotificationData(Parcel parcel) {
        this.mPackageName = parcel.readString();
        this.mTemplate = parcel.readInt();
        this.mIconId = parcel.readInt();
        this.mTitle = parcel.readString();
        this.mSubtitle = parcel.readString();
        this.mContent = parcel.readString();
        this.mDate = parcel.readLong();
        this.mGuideDistance = parcel.readInt();
        this.mGuideDistanceUnit = parcel.readString();
        this.mGuideDirectionId = parcel.readInt();
        this.mGuideText = parcel.readString();
        this.mVibrate = parcel.readInt();
    }

    @Override // android.os.Parcelable
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(this.mPackageName);
        parcel.writeInt(this.mTemplate);
        parcel.writeInt(this.mIconId);
        parcel.writeString(this.mTitle);
        parcel.writeString(this.mSubtitle);
        parcel.writeString(this.mContent);
        parcel.writeLong(this.mDate);
        parcel.writeInt(this.mGuideDistance);
        parcel.writeString(this.mGuideDistanceUnit);
        parcel.writeInt(this.mGuideDirectionId);
        parcel.writeString(this.mGuideText);
        parcel.writeInt(this.mVibrate);
    }

    public String getPackageName() {
        return this.mPackageName;
    }

    public void setPackageName(String str) {
        this.mPackageName = str;
    }

    public int getTemplate() {
        return this.mTemplate;
    }

    public void setTemplate(int i) {
        this.mTemplate = i;
    }

    public int getIconId() {
        return this.mIconId;
    }

    public void setIconId(int i) {
        this.mIconId = i;
    }

    public String getTitle() {
        return this.mTitle;
    }

    public void setTitle(String str) {
        this.mTitle = str;
    }

    public String getSubtitle() {
        return this.mSubtitle;
    }

    public void setSubtitle(String str) {
        this.mSubtitle = str;
    }

    public String getContent() {
        return this.mContent;
    }

    public void setContent(String str) {
        this.mContent = str;
    }

    public long getDate() {
        return this.mDate;
    }

    public void setDate(long j) {
        this.mDate = j;
    }

    public int getGuideDistance() {
        return this.mGuideDistance;
    }

    public void setGuideDistance(int i) {
        this.mGuideDistance = i;
    }

    public String getGuideDistanceUnit() {
        return this.mGuideDistanceUnit;
    }

    public void setGuideDistanceUnit(String str) {
        this.mGuideDistanceUnit = str;
    }

    public int getGuideDirectionId() {
        return this.mGuideDirectionId;
    }

    public void setGuideDirectionId(int i) {
        this.mGuideDirectionId = i;
    }

    public String getGuideText() {
        return this.mGuideText;
    }

    public void setGuideText(String str) {
        this.mGuideText = str;
    }

    public int getVibrate() {
        return this.mVibrate;
    }

    public void setVibrate(int i) {
        this.mVibrate = i;
    }
}
