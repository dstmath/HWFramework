package android.service.autofill;

import android.content.IntentSender;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import android.view.autofill.AutofillId;
import android.view.autofill.AutofillValue;
import android.view.autofill.Helper;
import android.widget.RemoteViews;
import com.android.internal.util.Preconditions;
import java.util.ArrayList;

public final class Dataset implements Parcelable {
    public static final Creator<Dataset> CREATOR = new Creator<Dataset>() {
        public Dataset createFromParcel(Parcel parcel) {
            Builder builder;
            RemoteViews presentation = (RemoteViews) parcel.readParcelable(null);
            if (presentation == null) {
                builder = new Builder();
            } else {
                builder = new Builder(presentation);
            }
            ArrayList<AutofillId> ids = parcel.createTypedArrayList(AutofillId.CREATOR);
            ArrayList<AutofillValue> values = parcel.createTypedArrayList(AutofillValue.CREATOR);
            ArrayList<RemoteViews> presentations = new ArrayList();
            parcel.readParcelableList(presentations, null);
            int idCount = ids != null ? ids.size() : 0;
            int valueCount = values != null ? values.size() : 0;
            int i = 0;
            while (i < idCount) {
                RemoteViews fieldPresentation;
                AutofillId id = (AutofillId) ids.get(i);
                AutofillValue value = valueCount > i ? (AutofillValue) values.get(i) : null;
                if (presentations.isEmpty()) {
                    fieldPresentation = null;
                } else {
                    fieldPresentation = (RemoteViews) presentations.get(i);
                }
                builder.setValueAndPresentation(id, value, fieldPresentation);
                i++;
            }
            builder.setAuthentication((IntentSender) parcel.readParcelable(null));
            builder.setId(parcel.readString());
            return builder.build();
        }

        public Dataset[] newArray(int size) {
            return new Dataset[size];
        }
    };
    private final IntentSender mAuthentication;
    private final ArrayList<AutofillId> mFieldIds;
    private final ArrayList<RemoteViews> mFieldPresentations;
    private final ArrayList<AutofillValue> mFieldValues;
    String mId;
    private final RemoteViews mPresentation;

    public static final class Builder {
        private IntentSender mAuthentication;
        private boolean mDestroyed;
        private ArrayList<AutofillId> mFieldIds;
        private ArrayList<RemoteViews> mFieldPresentations;
        private ArrayList<AutofillValue> mFieldValues;
        private String mId;
        private RemoteViews mPresentation;

        public Builder(RemoteViews presentation) {
            Preconditions.checkNotNull(presentation, "presentation must be non-null");
            this.mPresentation = presentation;
        }

        public Builder setAuthentication(IntentSender authentication) {
            throwIfDestroyed();
            this.mAuthentication = authentication;
            return this;
        }

        public Builder setId(String id) {
            throwIfDestroyed();
            this.mId = id;
            return this;
        }

        public Builder setValue(AutofillId id, AutofillValue value) {
            throwIfDestroyed();
            if (this.mPresentation == null) {
                throw new IllegalStateException("Dataset presentation not set on constructor");
            }
            setValueAndPresentation(id, value, null);
            return this;
        }

        public Builder setValue(AutofillId id, AutofillValue value, RemoteViews presentation) {
            throwIfDestroyed();
            Preconditions.checkNotNull(presentation, "presentation cannot be null");
            setValueAndPresentation(id, value, presentation);
            return this;
        }

        private void setValueAndPresentation(AutofillId id, AutofillValue value, RemoteViews presentation) {
            Preconditions.checkNotNull(id, "id cannot be null");
            if (this.mFieldIds != null) {
                int existingIdx = this.mFieldIds.indexOf(id);
                if (existingIdx >= 0) {
                    this.mFieldValues.set(existingIdx, value);
                    this.mFieldPresentations.set(existingIdx, presentation);
                    return;
                }
            }
            this.mFieldIds = new ArrayList();
            this.mFieldValues = new ArrayList();
            this.mFieldPresentations = new ArrayList();
            this.mFieldIds.add(id);
            this.mFieldValues.add(value);
            this.mFieldPresentations.add(presentation);
        }

        public Dataset build() {
            throwIfDestroyed();
            this.mDestroyed = true;
            if (this.mFieldIds != null) {
                return new Dataset(this, null);
            }
            throw new IllegalArgumentException("at least one value must be set");
        }

        private void throwIfDestroyed() {
            if (this.mDestroyed) {
                throw new IllegalStateException("Already called #build()");
            }
        }
    }

    /* synthetic */ Dataset(Builder builder, Dataset -this1) {
        this(builder);
    }

    private Dataset(Builder builder) {
        this.mFieldIds = builder.mFieldIds;
        this.mFieldValues = builder.mFieldValues;
        this.mFieldPresentations = builder.mFieldPresentations;
        this.mPresentation = builder.mPresentation;
        this.mAuthentication = builder.mAuthentication;
        this.mId = builder.mId;
    }

    public ArrayList<AutofillId> getFieldIds() {
        return this.mFieldIds;
    }

    public ArrayList<AutofillValue> getFieldValues() {
        return this.mFieldValues;
    }

    public RemoteViews getFieldPresentation(int index) {
        RemoteViews customPresentation = (RemoteViews) this.mFieldPresentations.get(index);
        return customPresentation != null ? customPresentation : this.mPresentation;
    }

    public IntentSender getAuthentication() {
        return this.mAuthentication;
    }

    public boolean isEmpty() {
        return this.mFieldIds != null ? this.mFieldIds.isEmpty() : true;
    }

    public String toString() {
        boolean z = true;
        if (!Helper.sDebug) {
            return super.toString();
        }
        boolean z2;
        StringBuilder append = new StringBuilder("Dataset " + this.mId + " [").append("fieldIds=").append(this.mFieldIds).append(", fieldValues=").append(this.mFieldValues).append(", fieldPresentations=").append(this.mFieldPresentations == null ? 0 : this.mFieldPresentations.size()).append(", hasPresentation=");
        if (this.mPresentation != null) {
            z2 = true;
        } else {
            z2 = false;
        }
        StringBuilder append2 = append.append(z2).append(", hasAuthentication=");
        if (this.mAuthentication == null) {
            z = false;
        }
        return append2.append(z).append(']').toString();
    }

    public String getId() {
        return this.mId;
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel parcel, int flags) {
        parcel.writeParcelable(this.mPresentation, flags);
        parcel.writeTypedList(this.mFieldIds, flags);
        parcel.writeTypedList(this.mFieldValues, flags);
        parcel.writeParcelableList(this.mFieldPresentations, flags);
        parcel.writeParcelable(this.mAuthentication, flags);
        parcel.writeString(this.mId);
    }
}
