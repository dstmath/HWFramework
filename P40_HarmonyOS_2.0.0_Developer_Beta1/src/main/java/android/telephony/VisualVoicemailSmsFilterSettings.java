package android.telephony;

import android.os.Parcel;
import android.os.Parcelable;
import java.util.Collections;
import java.util.List;

public final class VisualVoicemailSmsFilterSettings implements Parcelable {
    public static final Parcelable.Creator<VisualVoicemailSmsFilterSettings> CREATOR = new Parcelable.Creator<VisualVoicemailSmsFilterSettings>() {
        /* class android.telephony.VisualVoicemailSmsFilterSettings.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public VisualVoicemailSmsFilterSettings createFromParcel(Parcel in) {
            Builder builder = new Builder();
            builder.setClientPrefix(in.readString());
            builder.setOriginatingNumbers(in.createStringArrayList());
            builder.setDestinationPort(in.readInt());
            builder.setPackageName(in.readString());
            return builder.build();
        }

        @Override // android.os.Parcelable.Creator
        public VisualVoicemailSmsFilterSettings[] newArray(int size) {
            return new VisualVoicemailSmsFilterSettings[size];
        }
    };
    public static final String DEFAULT_CLIENT_PREFIX = "//VVM";
    public static final int DEFAULT_DESTINATION_PORT = -1;
    public static final List<String> DEFAULT_ORIGINATING_NUMBERS = Collections.emptyList();
    public static final int DESTINATION_PORT_ANY = -1;
    public static final int DESTINATION_PORT_DATA_SMS = -2;
    public final String clientPrefix;
    public final int destinationPort;
    public final List<String> originatingNumbers;
    public final String packageName;

    public static class Builder {
        private String mClientPrefix = VisualVoicemailSmsFilterSettings.DEFAULT_CLIENT_PREFIX;
        private int mDestinationPort = -1;
        private List<String> mOriginatingNumbers = VisualVoicemailSmsFilterSettings.DEFAULT_ORIGINATING_NUMBERS;
        private String mPackageName;

        public VisualVoicemailSmsFilterSettings build() {
            return new VisualVoicemailSmsFilterSettings(this);
        }

        public Builder setClientPrefix(String clientPrefix) {
            if (clientPrefix != null) {
                this.mClientPrefix = clientPrefix;
                return this;
            }
            throw new IllegalArgumentException("Client prefix cannot be null");
        }

        public Builder setOriginatingNumbers(List<String> originatingNumbers) {
            if (originatingNumbers != null) {
                this.mOriginatingNumbers = originatingNumbers;
                return this;
            }
            throw new IllegalArgumentException("Originating numbers cannot be null");
        }

        public Builder setDestinationPort(int destinationPort) {
            this.mDestinationPort = destinationPort;
            return this;
        }

        public Builder setPackageName(String packageName) {
            this.mPackageName = packageName;
            return this;
        }
    }

    private VisualVoicemailSmsFilterSettings(Builder builder) {
        this.clientPrefix = builder.mClientPrefix;
        this.originatingNumbers = builder.mOriginatingNumbers;
        this.destinationPort = builder.mDestinationPort;
        this.packageName = builder.mPackageName;
    }

    @Override // android.os.Parcelable
    public int describeContents() {
        return 0;
    }

    @Override // android.os.Parcelable
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.clientPrefix);
        dest.writeStringList(this.originatingNumbers);
        dest.writeInt(this.destinationPort);
        dest.writeString(this.packageName);
    }

    public String toString() {
        return "[VisualVoicemailSmsFilterSettings clientPrefix=" + this.clientPrefix + ", originatingNumbers=" + this.originatingNumbers + ", destinationPort=" + this.destinationPort + "]";
    }
}
