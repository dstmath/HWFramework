package android.service.autofill;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.Pair;
import android.view.autofill.Helper;
import android.widget.RemoteViews;
import com.android.internal.util.Preconditions;
import java.util.ArrayList;

public final class CustomDescription implements Parcelable {
    public static final Parcelable.Creator<CustomDescription> CREATOR = new Parcelable.Creator<CustomDescription>() {
        public CustomDescription createFromParcel(Parcel parcel) {
            RemoteViews parentPresentation = (RemoteViews) parcel.readParcelable(null);
            if (parentPresentation == null) {
                return null;
            }
            Builder builder = new Builder(parentPresentation);
            int[] ids = parcel.createIntArray();
            if (ids != null) {
                InternalTransformation[] values = (InternalTransformation[]) parcel.readParcelableArray(null, InternalTransformation.class);
                int size = ids.length;
                for (int i = 0; i < size; i++) {
                    builder.addChild(ids[i], values[i]);
                }
            }
            InternalValidator[] conditions = (InternalValidator[]) parcel.readParcelableArray(null, InternalValidator.class);
            if (conditions != null) {
                BatchUpdates[] updates = (BatchUpdates[]) parcel.readParcelableArray(null, BatchUpdates.class);
                int size2 = conditions.length;
                for (int i2 = 0; i2 < size2; i2++) {
                    builder.batchUpdate(conditions[i2], updates[i2]);
                }
            }
            return builder.build();
        }

        public CustomDescription[] newArray(int size) {
            return new CustomDescription[size];
        }
    };
    private final RemoteViews mPresentation;
    private final ArrayList<Pair<Integer, InternalTransformation>> mTransformations;
    private final ArrayList<Pair<InternalValidator, BatchUpdates>> mUpdates;

    public static class Builder {
        private boolean mDestroyed;
        /* access modifiers changed from: private */
        public final RemoteViews mPresentation;
        /* access modifiers changed from: private */
        public ArrayList<Pair<Integer, InternalTransformation>> mTransformations;
        /* access modifiers changed from: private */
        public ArrayList<Pair<InternalValidator, BatchUpdates>> mUpdates;

        public Builder(RemoteViews parentPresentation) {
            this.mPresentation = (RemoteViews) Preconditions.checkNotNull(parentPresentation);
        }

        public Builder addChild(int id, Transformation transformation) {
            throwIfDestroyed();
            Preconditions.checkArgument(transformation instanceof InternalTransformation, "not provided by Android System: " + transformation);
            if (this.mTransformations == null) {
                this.mTransformations = new ArrayList<>();
            }
            this.mTransformations.add(new Pair(Integer.valueOf(id), (InternalTransformation) transformation));
            return this;
        }

        public Builder batchUpdate(Validator condition, BatchUpdates updates) {
            throwIfDestroyed();
            Preconditions.checkArgument(condition instanceof InternalValidator, "not provided by Android System: " + condition);
            Preconditions.checkNotNull(updates);
            if (this.mUpdates == null) {
                this.mUpdates = new ArrayList<>();
            }
            this.mUpdates.add(new Pair((InternalValidator) condition, updates));
            return this;
        }

        public CustomDescription build() {
            throwIfDestroyed();
            this.mDestroyed = true;
            return new CustomDescription(this);
        }

        private void throwIfDestroyed() {
            if (this.mDestroyed) {
                throw new IllegalStateException("Already called #build()");
            }
        }
    }

    private CustomDescription(Builder builder) {
        this.mPresentation = builder.mPresentation;
        this.mTransformations = builder.mTransformations;
        this.mUpdates = builder.mUpdates;
    }

    public RemoteViews getPresentation() {
        return this.mPresentation;
    }

    public ArrayList<Pair<Integer, InternalTransformation>> getTransformations() {
        return this.mTransformations;
    }

    public ArrayList<Pair<InternalValidator, BatchUpdates>> getUpdates() {
        return this.mUpdates;
    }

    public String toString() {
        if (!Helper.sDebug) {
            return super.toString();
        }
        StringBuilder sb = new StringBuilder("CustomDescription: [presentation=");
        sb.append(this.mPresentation);
        sb.append(", transformations=");
        sb.append(this.mTransformations == null ? "N/A" : Integer.valueOf(this.mTransformations.size()));
        sb.append(", updates=");
        sb.append(this.mUpdates == null ? "N/A" : Integer.valueOf(this.mUpdates.size()));
        sb.append("]");
        return sb.toString();
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeParcelable(this.mPresentation, flags);
        if (this.mPresentation != null) {
            if (this.mTransformations == null) {
                dest.writeIntArray(null);
            } else {
                int size = this.mTransformations.size();
                int[] ids = new int[size];
                InternalTransformation[] values = new InternalTransformation[size];
                for (int i = 0; i < size; i++) {
                    Pair<Integer, InternalTransformation> pair = this.mTransformations.get(i);
                    ids[i] = ((Integer) pair.first).intValue();
                    values[i] = (InternalTransformation) pair.second;
                }
                dest.writeIntArray(ids);
                dest.writeParcelableArray(values, flags);
            }
            if (this.mUpdates == null) {
                dest.writeParcelableArray(null, flags);
            } else {
                int size2 = this.mUpdates.size();
                InternalValidator[] conditions = new InternalValidator[size2];
                BatchUpdates[] updates = new BatchUpdates[size2];
                for (int i2 = 0; i2 < size2; i2++) {
                    Pair<InternalValidator, BatchUpdates> pair2 = this.mUpdates.get(i2);
                    conditions[i2] = (InternalValidator) pair2.first;
                    updates[i2] = (BatchUpdates) pair2.second;
                }
                dest.writeParcelableArray(conditions, flags);
                dest.writeParcelableArray(updates, flags);
            }
        }
    }
}
