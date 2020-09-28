package android.view.textclassifier;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.ArrayMap;
import com.android.internal.util.Preconditions;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

/* access modifiers changed from: package-private */
public final class EntityConfidence implements Parcelable {
    public static final Parcelable.Creator<EntityConfidence> CREATOR = new Parcelable.Creator<EntityConfidence>() {
        /* class android.view.textclassifier.EntityConfidence.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public EntityConfidence createFromParcel(Parcel in) {
            return new EntityConfidence(in);
        }

        @Override // android.os.Parcelable.Creator
        public EntityConfidence[] newArray(int size) {
            return new EntityConfidence[size];
        }
    };
    private final ArrayMap<String, Float> mEntityConfidence;
    private final ArrayList<String> mSortedEntities;

    EntityConfidence() {
        this.mEntityConfidence = new ArrayMap<>();
        this.mSortedEntities = new ArrayList<>();
    }

    EntityConfidence(EntityConfidence source) {
        this.mEntityConfidence = new ArrayMap<>();
        this.mSortedEntities = new ArrayList<>();
        Preconditions.checkNotNull(source);
        this.mEntityConfidence.putAll((ArrayMap<? extends String, ? extends Float>) source.mEntityConfidence);
        this.mSortedEntities.addAll(source.mSortedEntities);
    }

    EntityConfidence(Map<String, Float> source) {
        this.mEntityConfidence = new ArrayMap<>();
        this.mSortedEntities = new ArrayList<>();
        Preconditions.checkNotNull(source);
        this.mEntityConfidence.ensureCapacity(source.size());
        for (Map.Entry<String, Float> it : source.entrySet()) {
            if (it.getValue().floatValue() > 0.0f) {
                this.mEntityConfidence.put(it.getKey(), Float.valueOf(Math.min(1.0f, it.getValue().floatValue())));
            }
        }
        resetSortedEntitiesFromMap();
    }

    public List<String> getEntities() {
        return Collections.unmodifiableList(this.mSortedEntities);
    }

    public float getConfidenceScore(String entity) {
        if (this.mEntityConfidence.containsKey(entity)) {
            return this.mEntityConfidence.get(entity).floatValue();
        }
        return 0.0f;
    }

    public String toString() {
        return this.mEntityConfidence.toString();
    }

    @Override // android.os.Parcelable
    public int describeContents() {
        return 0;
    }

    @Override // android.os.Parcelable
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.mEntityConfidence.size());
        for (Map.Entry<String, Float> entry : this.mEntityConfidence.entrySet()) {
            dest.writeString(entry.getKey());
            dest.writeFloat(entry.getValue().floatValue());
        }
    }

    private EntityConfidence(Parcel in) {
        this.mEntityConfidence = new ArrayMap<>();
        this.mSortedEntities = new ArrayList<>();
        int numEntities = in.readInt();
        this.mEntityConfidence.ensureCapacity(numEntities);
        for (int i = 0; i < numEntities; i++) {
            this.mEntityConfidence.put(in.readString(), Float.valueOf(in.readFloat()));
        }
        resetSortedEntitiesFromMap();
    }

    private void resetSortedEntitiesFromMap() {
        this.mSortedEntities.clear();
        this.mSortedEntities.ensureCapacity(this.mEntityConfidence.size());
        this.mSortedEntities.addAll(this.mEntityConfidence.keySet());
        this.mSortedEntities.sort(new Comparator() {
            /* class android.view.textclassifier.$$Lambda$EntityConfidence$YPh8hwgSYYK8OyQ1kFlQngc71Q0 */

            @Override // java.util.Comparator
            public final int compare(Object obj, Object obj2) {
                return EntityConfidence.this.lambda$resetSortedEntitiesFromMap$0$EntityConfidence((String) obj, (String) obj2);
            }
        });
    }

    public /* synthetic */ int lambda$resetSortedEntitiesFromMap$0$EntityConfidence(String e1, String e2) {
        return Float.compare(this.mEntityConfidence.get(e2).floatValue(), this.mEntityConfidence.get(e1).floatValue());
    }
}
