package android.view;

import android.app.WindowConfiguration;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.ArraySet;
import android.util.SparseArray;

public class RemoteAnimationDefinition implements Parcelable {
    public static final Parcelable.Creator<RemoteAnimationDefinition> CREATOR = new Parcelable.Creator<RemoteAnimationDefinition>() {
        public RemoteAnimationDefinition createFromParcel(Parcel in) {
            return new RemoteAnimationDefinition(in);
        }

        public RemoteAnimationDefinition[] newArray(int size) {
            return new RemoteAnimationDefinition[size];
        }
    };
    private final SparseArray<RemoteAnimationAdapterEntry> mTransitionAnimationMap;

    private static class RemoteAnimationAdapterEntry implements Parcelable {
        /* access modifiers changed from: private */
        public static final Parcelable.Creator<RemoteAnimationAdapterEntry> CREATOR = new Parcelable.Creator<RemoteAnimationAdapterEntry>() {
            public RemoteAnimationAdapterEntry createFromParcel(Parcel in) {
                return new RemoteAnimationAdapterEntry(in);
            }

            public RemoteAnimationAdapterEntry[] newArray(int size) {
                return new RemoteAnimationAdapterEntry[size];
            }
        };
        @WindowConfiguration.ActivityType
        final int activityTypeFilter;
        final RemoteAnimationAdapter adapter;

        RemoteAnimationAdapterEntry(RemoteAnimationAdapter adapter2, int activityTypeFilter2) {
            this.adapter = adapter2;
            this.activityTypeFilter = activityTypeFilter2;
        }

        private RemoteAnimationAdapterEntry(Parcel in) {
            this.adapter = (RemoteAnimationAdapter) in.readParcelable(RemoteAnimationAdapter.class.getClassLoader());
            this.activityTypeFilter = in.readInt();
        }

        public void writeToParcel(Parcel dest, int flags) {
            dest.writeParcelable(this.adapter, flags);
            dest.writeInt(this.activityTypeFilter);
        }

        public int describeContents() {
            return 0;
        }
    }

    public RemoteAnimationDefinition() {
        this.mTransitionAnimationMap = new SparseArray<>();
    }

    public void addRemoteAnimation(int transition, @WindowConfiguration.ActivityType int activityTypeFilter, RemoteAnimationAdapter adapter) {
        this.mTransitionAnimationMap.put(transition, new RemoteAnimationAdapterEntry(adapter, activityTypeFilter));
    }

    public void addRemoteAnimation(int transition, RemoteAnimationAdapter adapter) {
        addRemoteAnimation(transition, 0, adapter);
    }

    public boolean hasTransition(int transition, ArraySet<Integer> activityTypes) {
        return getAdapter(transition, activityTypes) != null;
    }

    public RemoteAnimationAdapter getAdapter(int transition, ArraySet<Integer> activityTypes) {
        RemoteAnimationAdapterEntry entry = this.mTransitionAnimationMap.get(transition);
        if (entry == null) {
            return null;
        }
        if (entry.activityTypeFilter == 0 || activityTypes.contains(Integer.valueOf(entry.activityTypeFilter))) {
            return entry.adapter;
        }
        return null;
    }

    public RemoteAnimationDefinition(Parcel in) {
        int size = in.readInt();
        this.mTransitionAnimationMap = new SparseArray<>(size);
        for (int i = 0; i < size; i++) {
            this.mTransitionAnimationMap.put(in.readInt(), (RemoteAnimationAdapterEntry) in.readTypedObject(RemoteAnimationAdapterEntry.CREATOR));
        }
    }

    public void setCallingPid(int pid) {
        for (int i = this.mTransitionAnimationMap.size() - 1; i >= 0; i--) {
            this.mTransitionAnimationMap.valueAt(i).adapter.setCallingPid(pid);
        }
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel dest, int flags) {
        int size = this.mTransitionAnimationMap.size();
        dest.writeInt(size);
        for (int i = 0; i < size; i++) {
            dest.writeInt(this.mTransitionAnimationMap.keyAt(i));
            dest.writeTypedObject(this.mTransitionAnimationMap.valueAt(i), flags);
        }
    }
}
