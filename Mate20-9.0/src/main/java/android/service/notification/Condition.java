package android.service.notification;

import android.annotation.SystemApi;
import android.content.Context;
import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;
import android.security.keystore.KeyProperties;
import android.util.proto.ProtoOutputStream;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.Objects;

public final class Condition implements Parcelable {
    public static final Parcelable.Creator<Condition> CREATOR = new Parcelable.Creator<Condition>() {
        public Condition createFromParcel(Parcel source) {
            return new Condition(source);
        }

        public Condition[] newArray(int size) {
            return new Condition[size];
        }
    };
    @SystemApi
    public static final int FLAG_RELEVANT_ALWAYS = 2;
    @SystemApi
    public static final int FLAG_RELEVANT_NOW = 1;
    @SystemApi
    public static final String SCHEME = "condition";
    @SystemApi
    public static final int STATE_ERROR = 3;
    public static final int STATE_FALSE = 0;
    public static final int STATE_TRUE = 1;
    @SystemApi
    public static final int STATE_UNKNOWN = 2;
    @SystemApi
    public final int flags;
    @SystemApi
    public final int icon;
    public final Uri id;
    @SystemApi
    public final String line1;
    @SystemApi
    public final String line2;
    public final int state;
    public final String summary;

    @Retention(RetentionPolicy.SOURCE)
    public @interface State {
    }

    public Condition(Uri id2, String summary2, int state2) {
        this(id2, summary2, "", "", -1, state2, 2);
    }

    @SystemApi
    public Condition(Uri id2, String summary2, String line12, String line22, int icon2, int state2, int flags2) {
        if (id2 == null) {
            throw new IllegalArgumentException("id is required");
        } else if (summary2 == null) {
            throw new IllegalArgumentException("summary is required");
        } else if (isValidState(state2)) {
            this.id = id2;
            this.summary = summary2;
            this.line1 = line12;
            this.line2 = line22;
            this.icon = icon2;
            this.state = state2;
            this.flags = flags2;
        } else {
            throw new IllegalArgumentException("state is invalid: " + state2);
        }
    }

    public Condition(Parcel source) {
        this((Uri) source.readParcelable(Condition.class.getClassLoader()), source.readString(), source.readString(), source.readString(), source.readInt(), source.readInt(), source.readInt());
    }

    private static boolean isValidState(int state2) {
        return state2 >= 0 && state2 <= 3;
    }

    public void writeToParcel(Parcel dest, int flags2) {
        dest.writeParcelable(this.id, 0);
        dest.writeString(this.summary);
        dest.writeString(this.line1);
        dest.writeString(this.line2);
        dest.writeInt(this.icon);
        dest.writeInt(this.state);
        dest.writeInt(this.flags);
    }

    public String toString() {
        return Condition.class.getSimpleName() + '[' + "id=" + this.id + ",summary=" + this.summary + ",line1=" + this.line1 + ",line2=" + this.line2 + ",icon=" + this.icon + ",state=" + stateToString(this.state) + ",flags=" + this.flags + ']';
    }

    public void writeToProto(ProtoOutputStream proto, long fieldId) {
        long token = proto.start(fieldId);
        proto.write(1138166333441L, this.id.toString());
        proto.write(1138166333442L, this.summary);
        proto.write(1138166333443L, this.line1);
        proto.write(1138166333444L, this.line2);
        proto.write(1120986464261L, this.icon);
        proto.write(1159641169926L, this.state);
        proto.write(1120986464263L, this.flags);
        proto.end(token);
    }

    @SystemApi
    public static String stateToString(int state2) {
        if (state2 == 0) {
            return "STATE_FALSE";
        }
        if (state2 == 1) {
            return "STATE_TRUE";
        }
        if (state2 == 2) {
            return "STATE_UNKNOWN";
        }
        if (state2 == 3) {
            return "STATE_ERROR";
        }
        throw new IllegalArgumentException("state is invalid: " + state2);
    }

    @SystemApi
    public static String relevanceToString(int flags2) {
        boolean always = false;
        boolean now = (flags2 & 1) != 0;
        if ((flags2 & 2) != 0) {
            always = true;
        }
        if (!now && !always) {
            return KeyProperties.DIGEST_NONE;
        }
        if (now && always) {
            return "NOW, ALWAYS";
        }
        return now ? "NOW" : "ALWAYS";
    }

    public boolean equals(Object o) {
        if (!(o instanceof Condition)) {
            return false;
        }
        boolean z = true;
        if (o == this) {
            return true;
        }
        Condition other = (Condition) o;
        if (!Objects.equals(other.id, this.id) || !Objects.equals(other.summary, this.summary) || !Objects.equals(other.line1, this.line1) || !Objects.equals(other.line2, this.line2) || other.icon != this.icon || other.state != this.state || other.flags != this.flags) {
            z = false;
        }
        return z;
    }

    public int hashCode() {
        return Objects.hash(new Object[]{this.id, this.summary, this.line1, this.line2, Integer.valueOf(this.icon), Integer.valueOf(this.state), Integer.valueOf(this.flags)});
    }

    public int describeContents() {
        return 0;
    }

    @SystemApi
    public Condition copy() {
        Parcel parcel = Parcel.obtain();
        try {
            writeToParcel(parcel, 0);
            parcel.setDataPosition(0);
            return new Condition(parcel);
        } finally {
            parcel.recycle();
        }
    }

    @SystemApi
    public static Uri.Builder newId(Context context) {
        return new Uri.Builder().scheme(SCHEME).authority(context.getPackageName());
    }

    @SystemApi
    public static boolean isValidId(Uri id2, String pkg) {
        return id2 != null && SCHEME.equals(id2.getScheme()) && pkg.equals(id2.getAuthority());
    }
}
