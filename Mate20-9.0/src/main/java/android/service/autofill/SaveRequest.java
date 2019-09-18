package android.service.autofill;

import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import com.android.internal.util.Preconditions;
import java.util.ArrayList;
import java.util.List;

public final class SaveRequest implements Parcelable {
    public static final Parcelable.Creator<SaveRequest> CREATOR = new Parcelable.Creator<SaveRequest>() {
        public SaveRequest createFromParcel(Parcel parcel) {
            return new SaveRequest(parcel);
        }

        public SaveRequest[] newArray(int size) {
            return new SaveRequest[size];
        }
    };
    private final Bundle mClientState;
    private final ArrayList<String> mDatasetIds;
    private final ArrayList<FillContext> mFillContexts;

    public SaveRequest(ArrayList<FillContext> fillContexts, Bundle clientState, ArrayList<String> datasetIds) {
        this.mFillContexts = (ArrayList) Preconditions.checkNotNull(fillContexts, "fillContexts");
        this.mClientState = clientState;
        this.mDatasetIds = datasetIds;
    }

    private SaveRequest(Parcel parcel) {
        this(parcel.createTypedArrayList(FillContext.CREATOR), parcel.readBundle(), parcel.createStringArrayList());
    }

    public List<FillContext> getFillContexts() {
        return this.mFillContexts;
    }

    public Bundle getClientState() {
        return this.mClientState;
    }

    public List<String> getDatasetIds() {
        return this.mDatasetIds;
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel parcel, int flags) {
        parcel.writeTypedList(this.mFillContexts, flags);
        parcel.writeBundle(this.mClientState);
        parcel.writeStringList(this.mDatasetIds);
    }
}
