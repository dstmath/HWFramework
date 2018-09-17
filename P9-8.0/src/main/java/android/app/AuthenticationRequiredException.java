package android.app;

import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import com.android.internal.util.Preconditions;

public final class AuthenticationRequiredException extends SecurityException implements Parcelable {
    public static final Creator<AuthenticationRequiredException> CREATOR = new Creator<AuthenticationRequiredException>() {
        public AuthenticationRequiredException createFromParcel(Parcel source) {
            return new AuthenticationRequiredException(source);
        }

        public AuthenticationRequiredException[] newArray(int size) {
            return new AuthenticationRequiredException[size];
        }
    };
    private static final String TAG = "AuthenticationRequiredException";
    private final PendingIntent mUserAction;

    public AuthenticationRequiredException(Parcel in) {
        this(new SecurityException(in.readString()), (PendingIntent) PendingIntent.CREATOR.createFromParcel(in));
    }

    public AuthenticationRequiredException(Throwable cause, PendingIntent userAction) {
        super(cause.getMessage());
        this.mUserAction = (PendingIntent) Preconditions.checkNotNull(userAction);
    }

    public PendingIntent getUserAction() {
        return this.mUserAction;
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(getMessage());
        this.mUserAction.writeToParcel(dest, flags);
    }
}
