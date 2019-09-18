package android.telecom;

import android.content.ComponentName;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.Process;
import android.os.UserHandle;
import android.telephony.SubscriptionInfo;
import android.util.Log;
import java.util.Objects;

public final class PhoneAccountHandle implements Parcelable {
    public static final Parcelable.Creator<PhoneAccountHandle> CREATOR = new Parcelable.Creator<PhoneAccountHandle>() {
        public PhoneAccountHandle createFromParcel(Parcel in) {
            return new PhoneAccountHandle(in);
        }

        public PhoneAccountHandle[] newArray(int size) {
            return new PhoneAccountHandle[size];
        }
    };
    private final ComponentName mComponentName;
    private final String mId;
    private final UserHandle mUserHandle;

    public PhoneAccountHandle(ComponentName componentName, String id) {
        this(componentName, id, Process.myUserHandle());
    }

    public PhoneAccountHandle(ComponentName componentName, String id, UserHandle userHandle) {
        checkParameters(componentName, userHandle);
        this.mComponentName = componentName;
        this.mId = id;
        this.mUserHandle = userHandle;
    }

    public ComponentName getComponentName() {
        return this.mComponentName;
    }

    public String getId() {
        return this.mId;
    }

    public UserHandle getUserHandle() {
        return this.mUserHandle;
    }

    public int hashCode() {
        return Objects.hash(new Object[]{this.mComponentName, this.mId, this.mUserHandle});
    }

    public String toString() {
        return this.mComponentName + ", " + SubscriptionInfo.givePrintableIccid(Log.pii(this.mId)) + ", " + this.mUserHandle;
    }

    public boolean equals(Object other) {
        return other != null && (other instanceof PhoneAccountHandle) && Objects.equals(((PhoneAccountHandle) other).getComponentName(), getComponentName()) && Objects.equals(((PhoneAccountHandle) other).getId(), getId()) && Objects.equals(((PhoneAccountHandle) other).getUserHandle(), getUserHandle());
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel out, int flags) {
        this.mComponentName.writeToParcel(out, flags);
        out.writeString(this.mId);
        this.mUserHandle.writeToParcel(out, flags);
    }

    private void checkParameters(ComponentName componentName, UserHandle userHandle) {
        if (componentName == null) {
            Log.w("PhoneAccountHandle", (Throwable) new Exception("PhoneAccountHandle has been created with null ComponentName!"));
        }
        if (userHandle == null) {
            Log.w("PhoneAccountHandle", (Throwable) new Exception("PhoneAccountHandle has been created with null UserHandle!"));
        }
    }

    private PhoneAccountHandle(Parcel in) {
        this((ComponentName) ComponentName.CREATOR.createFromParcel(in), in.readString(), UserHandle.CREATOR.createFromParcel(in));
    }
}
