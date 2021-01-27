package android.service.autofill;

import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import com.android.internal.util.Preconditions;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.ArrayList;
import java.util.List;

public final class FillRequest implements Parcelable {
    public static final Parcelable.Creator<FillRequest> CREATOR = new Parcelable.Creator<FillRequest>() {
        /* class android.service.autofill.FillRequest.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public FillRequest createFromParcel(Parcel parcel) {
            return new FillRequest(parcel);
        }

        @Override // android.os.Parcelable.Creator
        public FillRequest[] newArray(int size) {
            return new FillRequest[size];
        }
    };
    public static final int FLAG_COMPATIBILITY_MODE_REQUEST = 2;
    public static final int FLAG_MANUAL_REQUEST = 1;
    public static final int FLAG_PASSWORD_INPUT_TYPE = 4;
    public static final int INVALID_REQUEST_ID = Integer.MIN_VALUE;
    private final Bundle mClientState;
    private final ArrayList<FillContext> mContexts;
    private final int mFlags;
    private final int mId;

    @Retention(RetentionPolicy.SOURCE)
    @interface RequestFlags {
    }

    private FillRequest(Parcel parcel) {
        this.mId = parcel.readInt();
        this.mContexts = new ArrayList<>();
        parcel.readParcelableList(this.mContexts, null);
        this.mClientState = parcel.readBundle();
        this.mFlags = parcel.readInt();
    }

    public FillRequest(int id, ArrayList<FillContext> contexts, Bundle clientState, int flags) {
        this.mId = id;
        this.mFlags = Preconditions.checkFlagsArgument(flags, 7);
        this.mContexts = (ArrayList) Preconditions.checkCollectionElementsNotNull(contexts, "contexts");
        this.mClientState = clientState;
    }

    public int getId() {
        return this.mId;
    }

    public int getFlags() {
        return this.mFlags;
    }

    public List<FillContext> getFillContexts() {
        return this.mContexts;
    }

    public String toString() {
        return "FillRequest: [id=" + this.mId + ", flags=" + this.mFlags + ", ctxts= " + this.mContexts + "]";
    }

    public Bundle getClientState() {
        return this.mClientState;
    }

    @Override // android.os.Parcelable
    public int describeContents() {
        return 0;
    }

    @Override // android.os.Parcelable
    public void writeToParcel(Parcel parcel, int flags) {
        parcel.writeInt(this.mId);
        parcel.writeParcelableList(this.mContexts, flags);
        parcel.writeBundle(this.mClientState);
        parcel.writeInt(this.mFlags);
    }
}
