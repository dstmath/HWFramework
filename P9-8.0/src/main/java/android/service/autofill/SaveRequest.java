package android.service.autofill;

import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import com.android.internal.util.Preconditions;
import java.util.ArrayList;
import java.util.List;

public final class SaveRequest implements Parcelable {
    public static final Creator<SaveRequest> CREATOR = new Creator<SaveRequest>() {
        public SaveRequest createFromParcel(Parcel parcel) {
            return new SaveRequest(parcel, null);
        }

        public SaveRequest[] newArray(int size) {
            return new SaveRequest[size];
        }
    };
    private final Bundle mClientState;
    private final ArrayList<FillContext> mFillContexts;

    public SaveRequest(ArrayList<FillContext> fillContexts, Bundle clientState) {
        this.mFillContexts = (ArrayList) Preconditions.checkNotNull(fillContexts, "fillContexts");
        this.mClientState = clientState;
    }

    private SaveRequest(Parcel parcel) {
        this(parcel.createTypedArrayList(FillContext.CREATOR), parcel.readBundle());
    }

    public List<FillContext> getFillContexts() {
        return this.mFillContexts;
    }

    public Bundle getClientState() {
        return this.mClientState;
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel parcel, int flags) {
        parcel.writeTypedList(this.mFillContexts, flags);
        parcel.writeBundle(this.mClientState);
    }
}
