package android.app;

import android.annotation.SystemApi;
import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;
import android.util.proto.ProtoOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.json.JSONException;
import org.json.JSONObject;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlSerializer;

public final class NotificationChannelGroup implements Parcelable {
    private static final String ATT_BLOCKED = "blocked";
    private static final String ATT_DESC = "desc";
    private static final String ATT_ID = "id";
    private static final String ATT_NAME = "name";
    public static final Parcelable.Creator<NotificationChannelGroup> CREATOR = new Parcelable.Creator<NotificationChannelGroup>() {
        public NotificationChannelGroup createFromParcel(Parcel in) {
            return new NotificationChannelGroup(in);
        }

        public NotificationChannelGroup[] newArray(int size) {
            return new NotificationChannelGroup[size];
        }
    };
    private static final int MAX_TEXT_LENGTH = 1000;
    private static final String TAG_GROUP = "channelGroup";
    private boolean mBlocked;
    private List<NotificationChannel> mChannels = new ArrayList();
    private String mDescription;
    private final String mId;
    private CharSequence mName;

    public NotificationChannelGroup(String id, CharSequence name) {
        this.mId = getTrimmedString(id);
        this.mName = name != null ? getTrimmedString(name.toString()) : null;
    }

    protected NotificationChannelGroup(Parcel in) {
        if (in.readByte() != 0) {
            this.mId = in.readString();
        } else {
            this.mId = null;
        }
        this.mName = (CharSequence) TextUtils.CHAR_SEQUENCE_CREATOR.createFromParcel(in);
        if (in.readByte() != 0) {
            this.mDescription = in.readString();
        } else {
            this.mDescription = null;
        }
        in.readParcelableList(this.mChannels, NotificationChannel.class.getClassLoader());
        this.mBlocked = in.readBoolean();
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
        if (this.mDescription != null) {
            dest.writeByte((byte) 1);
            dest.writeString(this.mDescription);
        } else {
            dest.writeByte((byte) 0);
        }
        dest.writeParcelableList(this.mChannels, flags);
        dest.writeBoolean(this.mBlocked);
    }

    public String getId() {
        return this.mId;
    }

    public CharSequence getName() {
        return this.mName;
    }

    public String getDescription() {
        return this.mDescription;
    }

    public List<NotificationChannel> getChannels() {
        return this.mChannels;
    }

    public boolean isBlocked() {
        return this.mBlocked;
    }

    public void setDescription(String description) {
        this.mDescription = getTrimmedString(description);
    }

    public void setBlocked(boolean blocked) {
        this.mBlocked = blocked;
    }

    public void addChannel(NotificationChannel channel) {
        this.mChannels.add(channel);
    }

    public void setChannels(List<NotificationChannel> channels) {
        this.mChannels = channels;
    }

    public void populateFromXml(XmlPullParser parser) {
        setDescription(parser.getAttributeValue(null, ATT_DESC));
        setBlocked(safeBool(parser, ATT_BLOCKED, false));
    }

    private static boolean safeBool(XmlPullParser parser, String att, boolean defValue) {
        String value = parser.getAttributeValue(null, att);
        if (TextUtils.isEmpty(value)) {
            return defValue;
        }
        return Boolean.parseBoolean(value);
    }

    public void writeXml(XmlSerializer out) throws IOException {
        out.startTag(null, TAG_GROUP);
        out.attribute(null, "id", getId());
        if (getName() != null) {
            out.attribute(null, "name", getName().toString());
        }
        if (getDescription() != null) {
            out.attribute(null, ATT_DESC, getDescription().toString());
        }
        out.attribute(null, ATT_BLOCKED, Boolean.toString(isBlocked()));
        out.endTag(null, TAG_GROUP);
    }

    @SystemApi
    public JSONObject toJson() throws JSONException {
        JSONObject record = new JSONObject();
        record.put("id", getId());
        record.put("name", getName());
        record.put(ATT_DESC, getDescription());
        record.put(ATT_BLOCKED, isBlocked());
        return record;
    }

    public int describeContents() {
        return 0;
    }

    public boolean equals(Object o) {
        boolean z = true;
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        NotificationChannelGroup that = (NotificationChannelGroup) o;
        if (isBlocked() != that.isBlocked()) {
            return false;
        }
        if (getId() == null ? that.getId() != null : !getId().equals(that.getId())) {
            return false;
        }
        if (getName() == null ? that.getName() != null : !getName().equals(that.getName())) {
            return false;
        }
        if (getDescription() == null ? that.getDescription() != null : !getDescription().equals(that.getDescription())) {
            return false;
        }
        if (getChannels() != null) {
            z = getChannels().equals(that.getChannels());
        } else if (that.getChannels() != null) {
            z = false;
        }
        return z;
    }

    public int hashCode() {
        int i = 0;
        int hashCode = 31 * ((31 * ((31 * ((31 * (getId() != null ? getId().hashCode() : 0)) + (getName() != null ? getName().hashCode() : 0))) + (getDescription() != null ? getDescription().hashCode() : 0))) + (isBlocked() ? 1 : 0));
        if (getChannels() != null) {
            i = getChannels().hashCode();
        }
        return hashCode + i;
    }

    public NotificationChannelGroup clone() {
        NotificationChannelGroup cloned = new NotificationChannelGroup(getId(), getName());
        cloned.setDescription(getDescription());
        cloned.setBlocked(isBlocked());
        cloned.setChannels(getChannels());
        return cloned;
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("NotificationChannelGroup{mId='");
        sb.append(this.mId);
        sb.append('\'');
        sb.append(", mName=");
        sb.append(this.mName);
        sb.append(", mDescription=");
        sb.append(!TextUtils.isEmpty(this.mDescription) ? "hasDescription " : "");
        sb.append(", mBlocked=");
        sb.append(this.mBlocked);
        sb.append(", mChannels=");
        sb.append(this.mChannels);
        sb.append('}');
        return sb.toString();
    }

    public void writeToProto(ProtoOutputStream proto, long fieldId) {
        long token = proto.start(fieldId);
        proto.write(1138166333441L, this.mId);
        proto.write(1138166333442L, this.mName.toString());
        proto.write(1138166333443L, this.mDescription);
        proto.write(1133871366148L, this.mBlocked);
        for (NotificationChannel channel : this.mChannels) {
            channel.writeToProto(proto, 2246267895813L);
        }
        proto.end(token);
    }
}
