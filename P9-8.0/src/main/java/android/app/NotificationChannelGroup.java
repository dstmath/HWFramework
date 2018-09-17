package android.app;

import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import android.text.TextUtils;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.json.JSONException;
import org.json.JSONObject;
import org.xmlpull.v1.XmlSerializer;

public final class NotificationChannelGroup implements Parcelable {
    private static final String ATT_ID = "id";
    private static final String ATT_NAME = "name";
    public static final Creator<NotificationChannelGroup> CREATOR = new Creator<NotificationChannelGroup>() {
        public NotificationChannelGroup createFromParcel(Parcel in) {
            return new NotificationChannelGroup(in);
        }

        public NotificationChannelGroup[] newArray(int size) {
            return new NotificationChannelGroup[size];
        }
    };
    private static final int MAX_TEXT_LENGTH = 1000;
    private static final String TAG_GROUP = "channelGroup";
    private List<NotificationChannel> mChannels = new ArrayList();
    private final String mId;
    private CharSequence mName;

    public NotificationChannelGroup(String id, CharSequence name) {
        CharSequence charSequence = null;
        this.mId = getTrimmedString(id);
        if (name != null) {
            charSequence = getTrimmedString(name.toString());
        }
        this.mName = charSequence;
    }

    protected NotificationChannelGroup(Parcel in) {
        if (in.readByte() != (byte) 0) {
            this.mId = in.readString();
        } else {
            this.mId = null;
        }
        this.mName = (CharSequence) TextUtils.CHAR_SEQUENCE_CREATOR.createFromParcel(in);
        in.readParcelableList(this.mChannels, NotificationChannel.class.getClassLoader());
    }

    private String getTrimmedString(String input) {
        if (input == null || input.length() <= 1000) {
            return input;
        }
        return input.substring(0, 1000);
    }

    public void writeToParcel(Parcel dest, int flags) {
        if (this.mId != null) {
            dest.writeByte((byte) 1);
            dest.writeString(this.mId);
        } else {
            dest.writeByte((byte) 0);
        }
        TextUtils.writeToParcel(this.mName, dest, flags);
        dest.writeParcelableList(this.mChannels, flags);
    }

    public String getId() {
        return this.mId;
    }

    public CharSequence getName() {
        return this.mName;
    }

    public List<NotificationChannel> getChannels() {
        return this.mChannels;
    }

    public void addChannel(NotificationChannel channel) {
        this.mChannels.add(channel);
    }

    public void writeXml(XmlSerializer out) throws IOException {
        out.startTag(null, TAG_GROUP);
        out.attribute(null, "id", getId());
        if (getName() != null) {
            out.attribute(null, "name", getName().toString());
        }
        out.endTag(null, TAG_GROUP);
    }

    public JSONObject toJson() throws JSONException {
        JSONObject record = new JSONObject();
        record.put("id", getId());
        record.put("name", getName());
        return record;
    }

    public int describeContents() {
        return 0;
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        NotificationChannelGroup that = (NotificationChannelGroup) o;
        if (getId() == null ? that.getId() != null : (getId().equals(that.getId()) ^ 1) != 0) {
            return false;
        }
        return getName() == null ? that.getName() != null : (getName().equals(that.getName()) ^ 1) != 0;
    }

    public NotificationChannelGroup clone() {
        return new NotificationChannelGroup(getId(), getName());
    }

    public int hashCode() {
        return ((getId() != null ? getId().hashCode() : 0) * 31) + (getName() != null ? getName().hashCode() : 0);
    }

    public String toString() {
        return "NotificationChannelGroup{mId='" + this.mId + '\'' + ", mName=" + this.mName + ", mChannels=" + this.mChannels + '}';
    }
}
