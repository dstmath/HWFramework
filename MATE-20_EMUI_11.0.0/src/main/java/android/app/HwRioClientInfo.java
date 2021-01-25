package android.app;

import android.os.Parcel;
import android.os.Parcelable;

public class HwRioClientInfo implements Parcelable {
    public static final Parcelable.Creator<HwRioClientInfo> CREATOR = new Parcelable.Creator<HwRioClientInfo>() {
        /* class android.app.HwRioClientInfo.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public HwRioClientInfo createFromParcel(Parcel parcel) {
            return new HwRioClientInfo(parcel);
        }

        @Override // android.os.Parcelable.Creator
        public HwRioClientInfo[] newArray(int idx) {
            return new HwRioClientInfo[idx];
        }
    };
    private int mDpi;
    private String mPackageName;
    private long mPackageVersion;
    private int mScreenHeight;
    private int mScreenWidth;
    private int mUiMode;
    private String mWindowTitle;

    public HwRioClientInfo() {
    }

    public HwRioClientInfo(Parcel parcel) {
        this.mScreenWidth = parcel.readInt();
        this.mScreenHeight = parcel.readInt();
        this.mUiMode = parcel.readInt();
        this.mDpi = parcel.readInt();
        this.mPackageName = parcel.readString();
        this.mPackageVersion = parcel.readLong();
        this.mWindowTitle = parcel.readString();
    }

    public int getScreenWidth() {
        return this.mScreenWidth;
    }

    public void setScreenWidth(int screenWidth) {
        this.mScreenWidth = screenWidth;
    }

    public int getScreenHeight() {
        return this.mScreenHeight;
    }

    public void setScreenHeight(int screenHeight) {
        this.mScreenHeight = screenHeight;
    }

    public int getUiMode() {
        return this.mUiMode;
    }

    public void setUiMode(int uiMode) {
        this.mUiMode = uiMode;
    }

    public int getDpi() {
        return this.mDpi;
    }

    public void setDpi(int dpi) {
        this.mDpi = dpi;
    }

    public String getPackageName() {
        return this.mPackageName;
    }

    public void setPackageName(String packageName) {
        this.mPackageName = packageName;
    }

    public long getPackageVersion() {
        return this.mPackageVersion;
    }

    public void setPackageVersion(long packageVersion) {
        this.mPackageVersion = packageVersion;
    }

    public String getWindowTitle() {
        return this.mWindowTitle;
    }

    public void setWindowTitle(String windowTitle) {
        this.mWindowTitle = windowTitle;
    }

    @Override // android.os.Parcelable
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeInt(this.mScreenWidth);
        parcel.writeInt(this.mScreenHeight);
        parcel.writeInt(this.mUiMode);
        parcel.writeInt(this.mDpi);
        parcel.writeString(this.mPackageName);
        parcel.writeLong(this.mPackageVersion);
        parcel.writeString(this.mWindowTitle);
    }

    @Override // android.os.Parcelable
    public int describeContents() {
        return 0;
    }

    public String toString() {
        return "hash: " + hashCode() + ",mScreenWidth: " + this.mScreenWidth + ",mScreenHeight: " + this.mScreenHeight + ",mUiMode: " + this.mUiMode + ",mDpi: " + this.mDpi + ",mPackageName: " + this.mPackageName + ",mPackageVersion: " + this.mPackageVersion + ",mWindowTitle: " + this.mWindowTitle;
    }
}
