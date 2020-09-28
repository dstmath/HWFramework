package android.view.textclassifier;

import android.os.Parcel;
import android.os.Parcelable;
import com.android.internal.util.Preconditions;
import java.util.Locale;

public final class TextClassificationContext implements Parcelable {
    public static final Parcelable.Creator<TextClassificationContext> CREATOR = new Parcelable.Creator<TextClassificationContext>() {
        /* class android.view.textclassifier.TextClassificationContext.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public TextClassificationContext createFromParcel(Parcel parcel) {
            return new TextClassificationContext(parcel);
        }

        @Override // android.os.Parcelable.Creator
        public TextClassificationContext[] newArray(int size) {
            return new TextClassificationContext[size];
        }
    };
    private final String mPackageName;
    private int mUserId;
    private final String mWidgetType;
    private final String mWidgetVersion;

    private TextClassificationContext(String packageName, String widgetType, String widgetVersion) {
        this.mUserId = -10000;
        this.mPackageName = (String) Preconditions.checkNotNull(packageName);
        this.mWidgetType = (String) Preconditions.checkNotNull(widgetType);
        this.mWidgetVersion = widgetVersion;
    }

    public String getPackageName() {
        return this.mPackageName;
    }

    /* access modifiers changed from: package-private */
    public void setUserId(int userId) {
        this.mUserId = userId;
    }

    public int getUserId() {
        return this.mUserId;
    }

    public String getWidgetType() {
        return this.mWidgetType;
    }

    public String getWidgetVersion() {
        return this.mWidgetVersion;
    }

    public String toString() {
        return String.format(Locale.US, "TextClassificationContext{packageName=%s, widgetType=%s, widgetVersion=%s, userId=%d}", this.mPackageName, this.mWidgetType, this.mWidgetVersion, Integer.valueOf(this.mUserId));
    }

    public static final class Builder {
        private final String mPackageName;
        private final String mWidgetType;
        private String mWidgetVersion;

        public Builder(String packageName, String widgetType) {
            this.mPackageName = (String) Preconditions.checkNotNull(packageName);
            this.mWidgetType = (String) Preconditions.checkNotNull(widgetType);
        }

        public Builder setWidgetVersion(String widgetVersion) {
            this.mWidgetVersion = widgetVersion;
            return this;
        }

        public TextClassificationContext build() {
            return new TextClassificationContext(this.mPackageName, this.mWidgetType, this.mWidgetVersion);
        }
    }

    @Override // android.os.Parcelable
    public int describeContents() {
        return 0;
    }

    @Override // android.os.Parcelable
    public void writeToParcel(Parcel parcel, int flags) {
        parcel.writeString(this.mPackageName);
        parcel.writeString(this.mWidgetType);
        parcel.writeString(this.mWidgetVersion);
        parcel.writeInt(this.mUserId);
    }

    private TextClassificationContext(Parcel in) {
        this.mUserId = -10000;
        this.mPackageName = in.readString();
        this.mWidgetType = in.readString();
        this.mWidgetVersion = in.readString();
        this.mUserId = in.readInt();
    }
}
