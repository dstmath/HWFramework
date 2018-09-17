package android.app.admin;

import android.os.Build;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import java.io.IOException;
import java.util.Objects;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlSerializer;

public final class SystemUpdateInfo implements Parcelable {
    private static final String ATTR_ORIGINAL_BUILD = "original-build";
    private static final String ATTR_RECEIVED_TIME = "received-time";
    private static final String ATTR_SECURITY_PATCH_STATE = "security-patch-state";
    public static final Creator<SystemUpdateInfo> CREATOR = new Creator<SystemUpdateInfo>() {
        public SystemUpdateInfo createFromParcel(Parcel in) {
            return new SystemUpdateInfo(in, null);
        }

        public SystemUpdateInfo[] newArray(int size) {
            return new SystemUpdateInfo[size];
        }
    };
    public static final int SECURITY_PATCH_STATE_FALSE = 1;
    public static final int SECURITY_PATCH_STATE_TRUE = 2;
    public static final int SECURITY_PATCH_STATE_UNKNOWN = 0;
    private final long mReceivedTime;
    private final int mSecurityPatchState;

    private SystemUpdateInfo(long receivedTime, int securityPatchState) {
        this.mReceivedTime = receivedTime;
        this.mSecurityPatchState = securityPatchState;
    }

    private SystemUpdateInfo(Parcel in) {
        this.mReceivedTime = in.readLong();
        this.mSecurityPatchState = in.readInt();
    }

    public static SystemUpdateInfo of(long receivedTime) {
        return receivedTime == -1 ? null : new SystemUpdateInfo(receivedTime, 0);
    }

    public static SystemUpdateInfo of(long receivedTime, boolean isSecurityPatch) {
        if (receivedTime == -1) {
            return null;
        }
        return new SystemUpdateInfo(receivedTime, isSecurityPatch ? 2 : 1);
    }

    public long getReceivedTime() {
        return this.mReceivedTime;
    }

    public int getSecurityPatchState() {
        return this.mSecurityPatchState;
    }

    public void writeToXml(XmlSerializer out, String tag) throws IOException {
        out.startTag(null, tag);
        out.attribute(null, ATTR_RECEIVED_TIME, String.valueOf(this.mReceivedTime));
        out.attribute(null, ATTR_SECURITY_PATCH_STATE, String.valueOf(this.mSecurityPatchState));
        out.attribute(null, ATTR_ORIGINAL_BUILD, Build.FINGERPRINT);
        out.endTag(null, tag);
    }

    public static SystemUpdateInfo readFromXml(XmlPullParser parser) {
        if (Build.FINGERPRINT.equals(parser.getAttributeValue(null, ATTR_ORIGINAL_BUILD))) {
            return new SystemUpdateInfo(Long.parseLong(parser.getAttributeValue(null, ATTR_RECEIVED_TIME)), Integer.parseInt(parser.getAttributeValue(null, ATTR_SECURITY_PATCH_STATE)));
        }
        return null;
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(getReceivedTime());
        dest.writeInt(getSecurityPatchState());
    }

    public String toString() {
        return String.format("SystemUpdateInfo (receivedTime = %d, securityPatchState = %s)", new Object[]{Long.valueOf(this.mReceivedTime), securityPatchStateToString(this.mSecurityPatchState)});
    }

    private static String securityPatchStateToString(int state) {
        switch (state) {
            case 0:
                return "unknown";
            case 1:
                return "false";
            case 2:
                return "true";
            default:
                throw new IllegalArgumentException("Unrecognized security patch state: " + state);
        }
    }

    public boolean equals(Object o) {
        boolean z = true;
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        SystemUpdateInfo that = (SystemUpdateInfo) o;
        if (this.mReceivedTime != that.mReceivedTime) {
            z = false;
        } else if (this.mSecurityPatchState != that.mSecurityPatchState) {
            z = false;
        }
        return z;
    }

    public int hashCode() {
        return Objects.hash(new Object[]{Long.valueOf(this.mReceivedTime), Integer.valueOf(this.mSecurityPatchState)});
    }
}
