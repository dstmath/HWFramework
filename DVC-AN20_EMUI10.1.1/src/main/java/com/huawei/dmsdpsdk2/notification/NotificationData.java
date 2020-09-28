package com.huawei.dmsdpsdk2.notification;

import android.os.Parcel;
import android.os.Parcelable;

public class NotificationData implements Parcelable {
    public static final Parcelable.Creator<NotificationData> CREATOR = new Parcelable.Creator<NotificationData>() {
        /* class com.huawei.dmsdpsdk2.notification.NotificationData.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public NotificationData createFromParcel(Parcel in) {
            return new NotificationData(in);
        }

        @Override // android.os.Parcelable.Creator
        public NotificationData[] newArray(int size) {
            return new NotificationData[size];
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

    public NotificationData() {
    }

    protected NotificationData(Parcel in) {
        this.mPackageName = in.readString();
        this.mTemplate = in.readInt();
        this.mIconId = in.readInt();
        this.mTitle = in.readString();
        this.mSubtitle = in.readString();
        this.mContent = in.readString();
        this.mDate = in.readLong();
        this.mGuideDistance = in.readInt();
        this.mGuideDistanceUnit = in.readString();
        this.mGuideDirectionId = in.readInt();
        this.mGuideText = in.readString();
        this.mVibrate = in.readInt();
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.mPackageName);
        dest.writeInt(this.mTemplate);
        dest.writeInt(this.mIconId);
        dest.writeString(this.mTitle);
        dest.writeString(this.mSubtitle);
        dest.writeString(this.mContent);
        dest.writeLong(this.mDate);
        dest.writeInt(this.mGuideDistance);
        dest.writeString(this.mGuideDistanceUnit);
        dest.writeInt(this.mGuideDirectionId);
        dest.writeString(this.mGuideText);
        dest.writeInt(this.mVibrate);
    }

    public int describeContents() {
        return 0;
    }

    public String getPackageName() {
        return this.mPackageName;
    }

    public void setPackageName(String packageName) {
        this.mPackageName = packageName;
    }

    public int getTemplate() {
        return this.mTemplate;
    }

    public void setTemplate(int template) {
        this.mTemplate = template;
    }

    public int getIconId() {
        return this.mIconId;
    }

    public void setIconId(int iconId) {
        this.mIconId = iconId;
    }

    public String getTitle() {
        return this.mTitle;
    }

    public void setTitle(String title) {
        this.mTitle = title;
    }

    public String getSubtitle() {
        return this.mSubtitle;
    }

    public void setSubtitle(String subtitle) {
        this.mSubtitle = subtitle;
    }

    public String getContent() {
        return this.mContent;
    }

    public void setContent(String content) {
        this.mContent = content;
    }

    public long getDate() {
        return this.mDate;
    }

    public void setDate(long date) {
        this.mDate = date;
    }

    public int getGuideDistance() {
        return this.mGuideDistance;
    }

    public void setGuideDistance(int guideDistance) {
        this.mGuideDistance = guideDistance;
    }

    public String getGuideDistanceUnit() {
        return this.mGuideDistanceUnit;
    }

    public void setGuideDistanceUnit(String guideDistanceUnit) {
        this.mGuideDistanceUnit = guideDistanceUnit;
    }

    public int getGuideDirectionId() {
        return this.mGuideDirectionId;
    }

    public void setGuideDirectionId(int guideDirectionId) {
        this.mGuideDirectionId = guideDirectionId;
    }

    public String getGuideText() {
        return this.mGuideText;
    }

    public void setGuideText(String guideText) {
        this.mGuideText = guideText;
    }

    public int getVibrate() {
        return this.mVibrate;
    }

    public void setVibrate(int vibrate) {
        this.mVibrate = vibrate;
    }
}
