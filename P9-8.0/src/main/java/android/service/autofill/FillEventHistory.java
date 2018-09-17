package android.service.autofill;

import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import com.android.internal.util.Preconditions;
import java.util.ArrayList;
import java.util.List;

public final class FillEventHistory implements Parcelable {
    public static final Creator<FillEventHistory> CREATOR = new Creator<FillEventHistory>() {
        public FillEventHistory createFromParcel(Parcel parcel) {
            FillEventHistory selection = new FillEventHistory(0, 0, parcel.readBundle());
            int numEvents = parcel.readInt();
            for (int i = 0; i < numEvents; i++) {
                selection.addEvent(new Event(parcel.readInt(), parcel.readString()));
            }
            return selection;
        }

        public FillEventHistory[] newArray(int size) {
            return new FillEventHistory[size];
        }
    };
    private final Bundle mClientState;
    List<Event> mEvents;
    private final int mServiceUid;
    private final int mSessionId;

    public static final class Event {
        public static final int TYPE_AUTHENTICATION_SELECTED = 2;
        public static final int TYPE_DATASET_AUTHENTICATION_SELECTED = 1;
        public static final int TYPE_DATASET_SELECTED = 0;
        public static final int TYPE_SAVE_SHOWN = 3;
        private final String mDatasetId;
        private final int mEventType;

        public int getType() {
            return this.mEventType;
        }

        public String getDatasetId() {
            return this.mDatasetId;
        }

        public Event(int eventType, String datasetId) {
            this.mEventType = Preconditions.checkArgumentInRange(eventType, 0, 3, "eventType");
            this.mDatasetId = datasetId;
        }
    }

    public int getServiceUid() {
        return this.mServiceUid;
    }

    public int getSessionId() {
        return this.mSessionId;
    }

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

    public FillEventHistory(int serviceUid, int sessionId, Bundle clientState) {
        this.mClientState = clientState;
        this.mServiceUid = serviceUid;
        this.mSessionId = sessionId;
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeBundle(this.mClientState);
        if (this.mEvents == null) {
            dest.writeInt(0);
            return;
        }
        dest.writeInt(this.mEvents.size());
        int numEvents = this.mEvents.size();
        for (int i = 0; i < numEvents; i++) {
            Event event = (Event) this.mEvents.get(i);
            dest.writeInt(event.getType());
            dest.writeString(event.getDatasetId());
        }
    }
}
