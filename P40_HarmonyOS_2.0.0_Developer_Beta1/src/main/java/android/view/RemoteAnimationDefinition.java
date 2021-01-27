package android.view;

import android.annotation.UnsupportedAppUsage;
import android.app.WindowConfiguration;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.ArraySet;
import android.util.SparseArray;

public class RemoteAnimationDefinition implements Parcelable {
    public static final Parcelable.Creator<RemoteAnimationDefinition> CREATOR = new Parcelable.Creator<RemoteAnimationDefinition>() {
        /* class android.view.RemoteAnimationDefinition.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public RemoteAnimationDefinition createFromParcel(Parcel in) {
            return new RemoteAnimationDefinition(in);
        }

        @Override // android.os.Parcelable.Creator
        public RemoteAnimationDefinition[] newArray(int size) {
            return new RemoteAnimationDefinition[size];
        }
    };
    private final SparseArray<RemoteAnimationAdapterEntry> mTransitionAnimationMap;

    @UnsupportedAppUsage
    public RemoteAnimationDefinition() {
        this.mTransitionAnimationMap = new SparseArray<>();
    }

    @UnsupportedAppUsage
    public void addRemoteAnimation(int transition, @WindowConfiguration.ActivityType int activityTypeFilter, RemoteAnimationAdapter adapter) {
        this.mTransitionAnimationMap.put(transition, new RemoteAnimationAdapterEntry(adapter, activityTypeFilter));
    }

    @UnsupportedAppUsage
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

    @Override // android.os.Parcelable
    public int describeContents() {
        return 0;
    }

    @Override // android.os.Parcelable
    public void writeToParcel(Parcel dest, int flags) {
        int size = this.mTransitionAnimationMap.size();
        dest.writeInt(size);
        for (int i = 0; i < size; i++) {
            dest.writeInt(this.mTransitionAnimationMap.keyAt(i));
            dest.writeTypedObject(this.mTransitionAnimationMap.valueAt(i), flags);
        }
    }

    /* access modifiers changed from: private */
    public static class RemoteAnimationAdapterEntry implements Parcelable {
        private static final Parcelable.Creator<RemoteAnimationAdapterEntry> CREATOR = new Parcelable.Creator<RemoteAnimationAdapterEntry>() {
            /* class android.view.RemoteAnimationDefinition.RemoteAnimationAdapterEntry.AnonymousClass1 */

            @Override // android.os.Parcelable.Creator
            public RemoteAnimationAdapterEntry createFromParcel(Parcel in) {
                return new RemoteAnimationAdapterEntry(in);
            }

            @Override // android.os.Parcelable.Creator
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

        @Override // android.os.Parcelable
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeParcelable(this.adapter, flags);
            dest.writeInt(this.activityTypeFilter);
        }

        @Override // android.os.Parcelable
        public int describeContents() {
            return 0;
        }
    }
}
