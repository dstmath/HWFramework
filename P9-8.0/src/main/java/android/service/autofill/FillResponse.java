package android.service.autofill;

import android.content.IntentSender;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import android.view.autofill.AutofillId;
import android.view.autofill.Helper;
import android.widget.RemoteViews;
import java.util.ArrayList;
import java.util.Arrays;

public final class FillResponse implements Parcelable {
    public static final Creator<FillResponse> CREATOR = new Creator<FillResponse>() {
        public FillResponse createFromParcel(Parcel parcel) {
            Builder builder = new Builder();
            ArrayList<Dataset> datasets = parcel.readTypedArrayList(null);
            int datasetCount = datasets != null ? datasets.size() : 0;
            for (int i = 0; i < datasetCount; i++) {
                builder.addDataset((Dataset) datasets.get(i));
            }
            builder.setSaveInfo((SaveInfo) parcel.readParcelable(null));
            builder.setClientState((Bundle) parcel.readParcelable(null));
            AutofillId[] authenticationIds = (AutofillId[]) parcel.readParcelableArray(null, AutofillId.class);
            IntentSender authentication = (IntentSender) parcel.readParcelable(null);
            RemoteViews presentation = (RemoteViews) parcel.readParcelable(null);
            if (authenticationIds != null) {
                builder.setAuthentication(authenticationIds, authentication, presentation);
            }
            builder.setIgnoredIds((AutofillId[]) parcel.readParcelableArray(null, AutofillId.class));
            FillResponse response = builder.build();
            response.setRequestId(parcel.readInt());
            return response;
        }

        public FillResponse[] newArray(int size) {
            return new FillResponse[size];
        }
    };
    private final IntentSender mAuthentication;
    private final AutofillId[] mAuthenticationIds;
    private final Bundle mClientState;
    private final ArrayList<Dataset> mDatasets;
    private final AutofillId[] mIgnoredIds;
    private final RemoteViews mPresentation;
    private int mRequestId;
    private final SaveInfo mSaveInfo;

    public static final class Builder {
        private IntentSender mAuthentication;
        private AutofillId[] mAuthenticationIds;
        private Bundle mCLientState;
        private ArrayList<Dataset> mDatasets;
        private boolean mDestroyed;
        private AutofillId[] mIgnoredIds;
        private RemoteViews mPresentation;
        private SaveInfo mSaveInfo;

        public Builder setAuthentication(AutofillId[] ids, IntentSender authentication, RemoteViews presentation) {
            int i = 1;
            throwIfDestroyed();
            if (ids == null || ids.length == 0) {
                throw new IllegalArgumentException("ids cannot be null or empry");
            }
            int i2;
            if (authentication == null) {
                i2 = 1;
            } else {
                i2 = 0;
            }
            if (presentation != null) {
                i = 0;
            }
            if ((i ^ i2) != 0) {
                throw new IllegalArgumentException("authentication and presentation must be both non-null or null");
            }
            this.mAuthentication = authentication;
            this.mPresentation = presentation;
            this.mAuthenticationIds = ids;
            return this;
        }

        public Builder setIgnoredIds(AutofillId... ids) {
            this.mIgnoredIds = ids;
            return this;
        }

        public Builder addDataset(Dataset dataset) {
            throwIfDestroyed();
            if (dataset == null) {
                return this;
            }
            if (this.mDatasets == null) {
                this.mDatasets = new ArrayList();
            }
            if (this.mDatasets.add(dataset)) {
                return this;
            }
            return this;
        }

        public Builder setSaveInfo(SaveInfo saveInfo) {
            throwIfDestroyed();
            this.mSaveInfo = saveInfo;
            return this;
        }

        public Builder setClientState(Bundle clientState) {
            throwIfDestroyed();
            this.mCLientState = clientState;
            return this;
        }

        public FillResponse build() {
            throwIfDestroyed();
            if (this.mAuthentication == null && this.mDatasets == null && this.mSaveInfo == null) {
                throw new IllegalArgumentException("need to provide at least one DataSet or a SaveInfo or an authentication with a presentation");
            }
            this.mDestroyed = true;
            return new FillResponse(this, null);
        }

        private void throwIfDestroyed() {
            if (this.mDestroyed) {
                throw new IllegalStateException("Already called #build()");
            }
        }
    }

    /* synthetic */ FillResponse(Builder builder, FillResponse -this1) {
        this(builder);
    }

    private FillResponse(Builder builder) {
        this.mDatasets = builder.mDatasets;
        this.mSaveInfo = builder.mSaveInfo;
        this.mClientState = builder.mCLientState;
        this.mPresentation = builder.mPresentation;
        this.mAuthentication = builder.mAuthentication;
        this.mAuthenticationIds = builder.mAuthenticationIds;
        this.mIgnoredIds = builder.mIgnoredIds;
        this.mRequestId = Integer.MIN_VALUE;
    }

    public Bundle getClientState() {
        return this.mClientState;
    }

    public ArrayList<Dataset> getDatasets() {
        return this.mDatasets;
    }

    public SaveInfo getSaveInfo() {
        return this.mSaveInfo;
    }

    public RemoteViews getPresentation() {
        return this.mPresentation;
    }

    public IntentSender getAuthentication() {
        return this.mAuthentication;
    }

    public AutofillId[] getAuthenticationIds() {
        return this.mAuthenticationIds;
    }

    public AutofillId[] getIgnoredIds() {
        return this.mIgnoredIds;
    }

    public void setRequestId(int requestId) {
        this.mRequestId = requestId;
    }

    public int getRequestId() {
        return this.mRequestId;
    }

    public String toString() {
        boolean z = true;
        if (!Helper.sDebug) {
            return super.toString();
        }
        boolean z2;
        StringBuilder append = new StringBuilder("FillResponse : [mRequestId=" + this.mRequestId).append(", datasets=").append(this.mDatasets).append(", saveInfo=").append(this.mSaveInfo).append(", clientState=");
        if (this.mClientState != null) {
            z2 = true;
        } else {
            z2 = false;
        }
        append = append.append(z2).append(", hasPresentation=");
        if (this.mPresentation != null) {
            z2 = true;
        } else {
            z2 = false;
        }
        StringBuilder append2 = append.append(z2).append(", hasAuthentication=");
        if (this.mAuthentication == null) {
            z = false;
        }
        return append2.append(z).append(", authenticationIds=").append(Arrays.toString(this.mAuthenticationIds)).append(", ignoredIds=").append(Arrays.toString(this.mIgnoredIds)).append("]").toString();
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel parcel, int flags) {
        parcel.writeTypedArrayList(this.mDatasets, flags);
        parcel.writeParcelable(this.mSaveInfo, flags);
        parcel.writeParcelable(this.mClientState, flags);
        parcel.writeParcelableArray(this.mAuthenticationIds, flags);
        parcel.writeParcelable(this.mAuthentication, flags);
        parcel.writeParcelable(this.mPresentation, flags);
        parcel.writeParcelableArray(this.mIgnoredIds, flags);
        parcel.writeInt(this.mRequestId);
    }
}
