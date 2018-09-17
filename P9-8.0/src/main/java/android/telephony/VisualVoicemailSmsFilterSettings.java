package android.telephony;

import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import java.util.Collections;
import java.util.List;

public final class VisualVoicemailSmsFilterSettings implements Parcelable {
    public static final Creator<VisualVoicemailSmsFilterSettings> CREATOR = new Creator<VisualVoicemailSmsFilterSettings>() {
        public VisualVoicemailSmsFilterSettings createFromParcel(Parcel in) {
            Builder builder = new Builder();
            builder.setClientPrefix(in.readString());
            builder.setOriginatingNumbers(in.createStringArrayList());
            builder.setDestinationPort(in.readInt());
            return builder.build();
        }

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

    public static class Builder {
        private String mClientPrefix = VisualVoicemailSmsFilterSettings.DEFAULT_CLIENT_PREFIX;
        private int mDestinationPort = -1;
        private List<String> mOriginatingNumbers = VisualVoicemailSmsFilterSettings.DEFAULT_ORIGINATING_NUMBERS;

        public VisualVoicemailSmsFilterSettings build() {
            return new VisualVoicemailSmsFilterSettings(this, null);
        }

        public Builder setClientPrefix(String clientPrefix) {
            if (clientPrefix == null) {
                throw new IllegalArgumentException("Client prefix cannot be null");
            }
            this.mClientPrefix = clientPrefix;
            return this;
        }

        public Builder setOriginatingNumbers(List<String> originatingNumbers) {
            if (originatingNumbers == null) {
                throw new IllegalArgumentException("Originating numbers cannot be null");
            }
            this.mOriginatingNumbers = originatingNumbers;
            return this;
        }

        public Builder setDestinationPort(int destinationPort) {
            this.mDestinationPort = destinationPort;
            return this;
        }
    }

    /* synthetic */ VisualVoicemailSmsFilterSettings(Builder builder, VisualVoicemailSmsFilterSettings -this1) {
        this(builder);
    }

    private VisualVoicemailSmsFilterSettings(Builder builder) {
        this.clientPrefix = builder.mClientPrefix;
        this.originatingNumbers = builder.mOriginatingNumbers;
        this.destinationPort = builder.mDestinationPort;
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.clientPrefix);
        dest.writeStringList(this.originatingNumbers);
        dest.writeInt(this.destinationPort);
    }

    public String toString() {
        return "[VisualVoicemailSmsFilterSettings clientPrefix=" + this.clientPrefix + ", originatingNumbers=" + this.originatingNumbers + ", destinationPort=" + this.destinationPort + "]";
    }
}
