package android.service.autofill;

import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.ArrayMap;
import android.util.ArraySet;
import android.util.Log;
import android.view.autofill.AutofillId;
import android.view.autofill.Helper;
import com.android.internal.util.ArrayUtils;
import com.android.internal.util.Preconditions;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

public final class FillEventHistory implements Parcelable {
    public static final Parcelable.Creator<FillEventHistory> CREATOR = new Parcelable.Creator<FillEventHistory>() {
        public FillEventHistory createFromParcel(Parcel parcel) {
            ArrayList<ArrayList<String>> manuallyFilledDatasetIds;
            Parcel parcel2 = parcel;
            int i = 0;
            FillEventHistory selection = new FillEventHistory(0, parcel.readBundle());
            int numEvents = parcel.readInt();
            int i2 = 0;
            while (i2 < numEvents) {
                int eventType = parcel.readInt();
                String datasetId = parcel.readString();
                Bundle clientState = parcel.readBundle();
                ArrayList<String> selectedDatasetIds = parcel.createStringArrayList();
                FieldClassification[] detectedFieldClassifications = null;
                ArraySet<? extends Object> readArraySet = parcel2.readArraySet(null);
                ArrayList<AutofillId> changedFieldIds = parcel2.createTypedArrayList(AutofillId.CREATOR);
                ArrayList<String> changedDatasetIds = parcel.createStringArrayList();
                ArrayList<AutofillId> manuallyFilledFieldIds = parcel2.createTypedArrayList(AutofillId.CREATOR);
                if (manuallyFilledFieldIds != null) {
                    int size = manuallyFilledFieldIds.size();
                    ArrayList<ArrayList<String>> manuallyFilledDatasetIds2 = new ArrayList<>(size);
                    for (int j = i; j < size; j++) {
                        manuallyFilledDatasetIds2.add(parcel.createStringArrayList());
                    }
                    manuallyFilledDatasetIds = manuallyFilledDatasetIds2;
                } else {
                    manuallyFilledDatasetIds = null;
                }
                AutofillId[] detectedFieldIds = (AutofillId[]) parcel2.readParcelableArray(null, AutofillId.class);
                if (detectedFieldIds != null) {
                    detectedFieldClassifications = FieldClassification.readArrayFromParcel(parcel);
                }
                Event event = r5;
                ArrayList<AutofillId> arrayList = manuallyFilledFieldIds;
                Event event2 = new Event(eventType, datasetId, clientState, selectedDatasetIds, readArraySet, changedFieldIds, changedDatasetIds, manuallyFilledFieldIds, manuallyFilledDatasetIds, detectedFieldIds, detectedFieldClassifications);
                selection.addEvent(event);
                i2++;
                i = 0;
            }
            return selection;
        }

        public FillEventHistory[] newArray(int size) {
            return new FillEventHistory[size];
        }
    };
    private static final String TAG = "FillEventHistory";
    private final Bundle mClientState;
    List<Event> mEvents;
    private final int mSessionId;

    public static final class Event {
        public static final int TYPE_AUTHENTICATION_SELECTED = 2;
        public static final int TYPE_CONTEXT_COMMITTED = 4;
        public static final int TYPE_DATASET_AUTHENTICATION_SELECTED = 1;
        public static final int TYPE_DATASET_SELECTED = 0;
        public static final int TYPE_SAVE_SHOWN = 3;
        /* access modifiers changed from: private */
        public final ArrayList<String> mChangedDatasetIds;
        /* access modifiers changed from: private */
        public final ArrayList<AutofillId> mChangedFieldIds;
        /* access modifiers changed from: private */
        public final Bundle mClientState;
        /* access modifiers changed from: private */
        public final String mDatasetId;
        /* access modifiers changed from: private */
        public final FieldClassification[] mDetectedFieldClassifications;
        /* access modifiers changed from: private */
        public final AutofillId[] mDetectedFieldIds;
        /* access modifiers changed from: private */
        public final int mEventType;
        /* access modifiers changed from: private */
        public final ArraySet<String> mIgnoredDatasetIds;
        /* access modifiers changed from: private */
        public final ArrayList<ArrayList<String>> mManuallyFilledDatasetIds;
        /* access modifiers changed from: private */
        public final ArrayList<AutofillId> mManuallyFilledFieldIds;
        /* access modifiers changed from: private */
        public final List<String> mSelectedDatasetIds;

        @Retention(RetentionPolicy.SOURCE)
        @interface EventIds {
        }

        public int getType() {
            return this.mEventType;
        }

        public String getDatasetId() {
            return this.mDatasetId;
        }

        public Bundle getClientState() {
            return this.mClientState;
        }

        public Set<String> getSelectedDatasetIds() {
            if (this.mSelectedDatasetIds == null) {
                return Collections.emptySet();
            }
            return new ArraySet(this.mSelectedDatasetIds);
        }

        public Set<String> getIgnoredDatasetIds() {
            return this.mIgnoredDatasetIds == null ? Collections.emptySet() : this.mIgnoredDatasetIds;
        }

        public Map<AutofillId, String> getChangedFields() {
            if (this.mChangedFieldIds == null || this.mChangedDatasetIds == null) {
                return Collections.emptyMap();
            }
            int size = this.mChangedFieldIds.size();
            ArrayMap<AutofillId, String> changedFields = new ArrayMap<>(size);
            for (int i = 0; i < size; i++) {
                changedFields.put(this.mChangedFieldIds.get(i), this.mChangedDatasetIds.get(i));
            }
            return changedFields;
        }

        public Map<AutofillId, FieldClassification> getFieldsClassification() {
            if (this.mDetectedFieldIds == null) {
                return Collections.emptyMap();
            }
            int size = this.mDetectedFieldIds.length;
            ArrayMap<AutofillId, FieldClassification> map = new ArrayMap<>(size);
            for (int i = 0; i < size; i++) {
                AutofillId id = this.mDetectedFieldIds[i];
                FieldClassification fc = this.mDetectedFieldClassifications[i];
                if (Helper.sVerbose) {
                    Log.v(FillEventHistory.TAG, "getFieldsClassification[" + i + "]: id=" + id + ", fc=" + fc);
                }
                map.put(id, fc);
            }
            return map;
        }

        public Map<AutofillId, Set<String>> getManuallyEnteredField() {
            if (this.mManuallyFilledFieldIds == null || this.mManuallyFilledDatasetIds == null) {
                return Collections.emptyMap();
            }
            int size = this.mManuallyFilledFieldIds.size();
            Map<AutofillId, Set<String>> manuallyFilledFields = new ArrayMap<>(size);
            for (int i = 0; i < size; i++) {
                manuallyFilledFields.put(this.mManuallyFilledFieldIds.get(i), new ArraySet(this.mManuallyFilledDatasetIds.get(i)));
            }
            return manuallyFilledFields;
        }

        public Event(int eventType, String datasetId, Bundle clientState, List<String> selectedDatasetIds, ArraySet<String> ignoredDatasetIds, ArrayList<AutofillId> changedFieldIds, ArrayList<String> changedDatasetIds, ArrayList<AutofillId> manuallyFilledFieldIds, ArrayList<ArrayList<String>> manuallyFilledDatasetIds, AutofillId[] detectedFieldIds, FieldClassification[] detectedFieldClassifications) {
            this.mEventType = Preconditions.checkArgumentInRange(eventType, 0, 4, "eventType");
            this.mDatasetId = datasetId;
            this.mClientState = clientState;
            this.mSelectedDatasetIds = selectedDatasetIds;
            this.mIgnoredDatasetIds = ignoredDatasetIds;
            boolean z = true;
            if (changedFieldIds != null) {
                Preconditions.checkArgument(!ArrayUtils.isEmpty(changedFieldIds) && changedDatasetIds != null && changedFieldIds.size() == changedDatasetIds.size(), "changed ids must have same length and not be empty");
            }
            this.mChangedFieldIds = changedFieldIds;
            this.mChangedDatasetIds = changedDatasetIds;
            if (manuallyFilledFieldIds != null) {
                Preconditions.checkArgument((ArrayUtils.isEmpty(manuallyFilledFieldIds) || manuallyFilledDatasetIds == null || manuallyFilledFieldIds.size() != manuallyFilledDatasetIds.size()) ? false : z, "manually filled ids must have same length and not be empty");
            }
            this.mManuallyFilledFieldIds = manuallyFilledFieldIds;
            this.mManuallyFilledDatasetIds = manuallyFilledDatasetIds;
            this.mDetectedFieldIds = detectedFieldIds;
            this.mDetectedFieldClassifications = detectedFieldClassifications;
        }

        public String toString() {
            return "FillEvent [datasetId=" + this.mDatasetId + ", type=" + this.mEventType + ", selectedDatasets=" + this.mSelectedDatasetIds + ", ignoredDatasetIds=" + this.mIgnoredDatasetIds + ", changedFieldIds=" + this.mChangedFieldIds + ", changedDatasetsIds=" + this.mChangedDatasetIds + ", manuallyFilledFieldIds=" + this.mManuallyFilledFieldIds + ", manuallyFilledDatasetIds=" + this.mManuallyFilledDatasetIds + ", detectedFieldIds=" + Arrays.toString(this.mDetectedFieldIds) + ", detectedFieldClassifications =" + Arrays.toString(this.mDetectedFieldClassifications) + "]";
        }
    }

    public int getSessionId() {
        return this.mSessionId;
    }

    @Deprecated
    public Bundle getClientState() {
        return this.mClientState;
    }

    public List<Event> getEvents() {
        return this.mEvents;
    }

    public void addEvent(Event event) {
        if (this.mEvents == null) {
            this.mEvents = new ArrayList(1);
        }
        this.mEvents.add(event);
    }

    public FillEventHistory(int sessionId, Bundle clientState) {
        this.mClientState = clientState;
        this.mSessionId = sessionId;
    }

    public String toString() {
        return this.mEvents == null ? "no events" : this.mEvents.toString();
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel parcel, int flags) {
        parcel.writeBundle(this.mClientState);
        if (this.mEvents == null) {
            parcel.writeInt(0);
            return;
        }
        parcel.writeInt(this.mEvents.size());
        int numEvents = this.mEvents.size();
        for (int i = 0; i < numEvents; i++) {
            Event event = this.mEvents.get(i);
            parcel.writeInt(event.mEventType);
            parcel.writeString(event.mDatasetId);
            parcel.writeBundle(event.mClientState);
            parcel.writeStringList(event.mSelectedDatasetIds);
            parcel.writeArraySet(event.mIgnoredDatasetIds);
            parcel.writeTypedList(event.mChangedFieldIds);
            parcel.writeStringList(event.mChangedDatasetIds);
            parcel.writeTypedList(event.mManuallyFilledFieldIds);
            if (event.mManuallyFilledFieldIds != null) {
                int size = event.mManuallyFilledFieldIds.size();
                for (int j = 0; j < size; j++) {
                    parcel.writeStringList((List) event.mManuallyFilledDatasetIds.get(j));
                }
            }
            AutofillId[] detectedFields = event.mDetectedFieldIds;
            parcel.writeParcelableArray(detectedFields, flags);
            if (detectedFields != null) {
                FieldClassification.writeArrayToParcel(parcel, event.mDetectedFieldClassifications);
            }
        }
    }
}
