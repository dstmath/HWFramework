package android.service.autofill;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.Pair;
import android.view.autofill.Helper;
import android.widget.RemoteViews;
import com.android.internal.util.Preconditions;
import java.util.ArrayList;

public final class BatchUpdates implements Parcelable {
    public static final Parcelable.Creator<BatchUpdates> CREATOR = new Parcelable.Creator<BatchUpdates>() {
        /* class android.service.autofill.BatchUpdates.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public BatchUpdates createFromParcel(Parcel parcel) {
            Builder builder = new Builder();
            int[] ids = parcel.createIntArray();
            if (ids != null) {
                InternalTransformation[] values = (InternalTransformation[]) parcel.readParcelableArray(null, InternalTransformation.class);
                int size = ids.length;
                for (int i = 0; i < size; i++) {
                    builder.transformChild(ids[i], values[i]);
                }
            }
            RemoteViews updates = (RemoteViews) parcel.readParcelable(null);
            if (updates != null) {
                builder.updateTemplate(updates);
            }
            return builder.build();
        }

        @Override // android.os.Parcelable.Creator
        public BatchUpdates[] newArray(int size) {
            return new BatchUpdates[size];
        }
    };
    private final ArrayList<Pair<Integer, InternalTransformation>> mTransformations;
    private final RemoteViews mUpdates;

    private BatchUpdates(Builder builder) {
        this.mTransformations = builder.mTransformations;
        this.mUpdates = builder.mUpdates;
    }

    public ArrayList<Pair<Integer, InternalTransformation>> getTransformations() {
        return this.mTransformations;
    }

    public RemoteViews getUpdates() {
        return this.mUpdates;
    }

    public static class Builder {
        private boolean mDestroyed;
        private ArrayList<Pair<Integer, InternalTransformation>> mTransformations;
        private RemoteViews mUpdates;

        public Builder updateTemplate(RemoteViews updates) {
            throwIfDestroyed();
            this.mUpdates = (RemoteViews) Preconditions.checkNotNull(updates);
            return this;
        }

        public Builder transformChild(int id, Transformation transformation) {
            throwIfDestroyed();
            Preconditions.checkArgument(transformation instanceof InternalTransformation, "not provided by Android System: " + transformation);
            if (this.mTransformations == null) {
                this.mTransformations = new ArrayList<>();
            }
            this.mTransformations.add(new Pair<>(Integer.valueOf(id), (InternalTransformation) transformation));
            return this;
        }

        public BatchUpdates build() {
            throwIfDestroyed();
            Preconditions.checkState((this.mUpdates == null && this.mTransformations == null) ? false : true, "must call either updateTemplate() or transformChild() at least once");
            this.mDestroyed = true;
            return new BatchUpdates(this);
        }

        private void throwIfDestroyed() {
            if (this.mDestroyed) {
                throw new IllegalStateException("Already called #build()");
            }
        }
    }

    public String toString() {
        if (!Helper.sDebug) {
            return super.toString();
        }
        StringBuilder sb = new StringBuilder("BatchUpdates: [");
        sb.append(", transformations=");
        ArrayList<Pair<Integer, InternalTransformation>> arrayList = this.mTransformations;
        sb.append(arrayList == null ? "N/A" : Integer.valueOf(arrayList.size()));
        sb.append(", updates=");
        sb.append(this.mUpdates);
        sb.append("]");
        return sb.toString();
    }

    @Override // android.os.Parcelable
    public int describeContents() {
        return 0;
    }

    @Override // android.os.Parcelable
    public void writeToParcel(Parcel dest, int flags) {
        ArrayList<Pair<Integer, InternalTransformation>> arrayList = this.mTransformations;
        if (arrayList == null) {
            dest.writeIntArray(null);
        } else {
            int size = arrayList.size();
            int[] ids = new int[size];
            InternalTransformation[] values = new InternalTransformation[size];
            for (int i = 0; i < size; i++) {
                Pair<Integer, InternalTransformation> pair = this.mTransformations.get(i);
                ids[i] = pair.first.intValue();
                values[i] = pair.second;
            }
            dest.writeIntArray(ids);
            dest.writeParcelableArray(values, flags);
        }
        dest.writeParcelable(this.mUpdates, flags);
    }
}
