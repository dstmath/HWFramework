package android.view.textclassifier;

import android.os.Parcel;
import android.os.Parcelable;
import com.android.internal.util.Preconditions;
import java.util.Locale;

public final class TextClassificationContext implements Parcelable {
    public static final Parcelable.Creator<TextClassificationContext> CREATOR = new Parcelable.Creator<TextClassificationContext>() {
        public TextClassificationContext createFromParcel(Parcel parcel) {
            return new TextClassificationContext(parcel);
        }

        public TextClassificationContext[] newArray(int size) {
            return new TextClassificationContext[size];
        }
    };
    private final String mPackageName;
    private final String mWidgetType;
    private final String mWidgetVersion;

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

    private TextClassificationContext(String packageName, String widgetType, String widgetVersion) {
        this.mPackageName = (String) Preconditions.checkNotNull(packageName);
        this.mWidgetType = (String) Preconditions.checkNotNull(widgetType);
        this.mWidgetVersion = widgetVersion;
    }

    public String getPackageName() {
        return this.mPackageName;
    }

    public String getWidgetType() {
        return this.mWidgetType;
    }

    public String getWidgetVersion() {
        return this.mWidgetVersion;
    }

    public String toString() {
        return String.format(Locale.US, "TextClassificationContext{packageName=%s, widgetType=%s, widgetVersion=%s}", new Object[]{this.mPackageName, this.mWidgetType, this.mWidgetVersion});
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel parcel, int flags) {
        parcel.writeString(this.mPackageName);
        parcel.writeString(this.mWidgetType);
        parcel.writeString(this.mWidgetVersion);
    }

    private TextClassificationContext(Parcel in) {
        this.mPackageName = in.readString();
        this.mWidgetType = in.readString();
        this.mWidgetVersion = in.readString();
    }
}
