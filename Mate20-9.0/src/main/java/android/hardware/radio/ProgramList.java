package android.hardware.radio;

import android.annotation.SystemApi;
import android.hardware.radio.ProgramList;
import android.hardware.radio.ProgramSelector;
import android.hardware.radio.RadioManager;
import android.os.Parcel;
import android.os.Parcelable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.Executor;
import java.util.function.Consumer;
import java.util.stream.Collectors;

@SystemApi
public final class ProgramList implements AutoCloseable {
    private boolean mIsClosed = false;
    private boolean mIsComplete = false;
    private final List<ListCallback> mListCallbacks = new ArrayList();
    private final Object mLock = new Object();
    private OnCloseListener mOnCloseListener;
    private final List<OnCompleteListener> mOnCompleteListeners = new ArrayList();
    private final Map<ProgramSelector.Identifier, RadioManager.ProgramInfo> mPrograms = new HashMap();

    public static final class Chunk implements Parcelable {
        public static final Parcelable.Creator<Chunk> CREATOR = new Parcelable.Creator<Chunk>() {
            public Chunk createFromParcel(Parcel in) {
                return new Chunk(in);
            }

            public Chunk[] newArray(int size) {
                return new Chunk[size];
            }
        };
        private final boolean mComplete;
        private final Set<RadioManager.ProgramInfo> mModified;
        private final boolean mPurge;
        private final Set<ProgramSelector.Identifier> mRemoved;

        public Chunk(boolean purge, boolean complete, Set<RadioManager.ProgramInfo> modified, Set<ProgramSelector.Identifier> removed) {
            this.mPurge = purge;
            this.mComplete = complete;
            this.mModified = modified != null ? modified : Collections.emptySet();
            this.mRemoved = removed != null ? removed : Collections.emptySet();
        }

        private Chunk(Parcel in) {
            boolean z = false;
            this.mPurge = in.readByte() != 0;
            this.mComplete = in.readByte() != 0 ? true : z;
            this.mModified = Utils.createSet(in, RadioManager.ProgramInfo.CREATOR);
            this.mRemoved = Utils.createSet(in, ProgramSelector.Identifier.CREATOR);
        }

        public void writeToParcel(Parcel dest, int flags) {
            dest.writeByte(this.mPurge ? (byte) 1 : 0);
            dest.writeByte(this.mComplete ? (byte) 1 : 0);
            Utils.writeSet(dest, this.mModified);
            Utils.writeSet(dest, this.mRemoved);
        }

        public int describeContents() {
            return 0;
        }

        public boolean isPurge() {
            return this.mPurge;
        }

        public boolean isComplete() {
            return this.mComplete;
        }

        public Set<RadioManager.ProgramInfo> getModified() {
            return this.mModified;
        }

        public Set<ProgramSelector.Identifier> getRemoved() {
            return this.mRemoved;
        }
    }

    public static final class Filter implements Parcelable {
        public static final Parcelable.Creator<Filter> CREATOR = new Parcelable.Creator<Filter>() {
            public Filter createFromParcel(Parcel in) {
                return new Filter(in);
            }

            public Filter[] newArray(int size) {
                return new Filter[size];
            }
        };
        private final boolean mExcludeModifications;
        private final Set<Integer> mIdentifierTypes;
        private final Set<ProgramSelector.Identifier> mIdentifiers;
        private final boolean mIncludeCategories;
        private final Map<String, String> mVendorFilter;

        public Filter(Set<Integer> identifierTypes, Set<ProgramSelector.Identifier> identifiers, boolean includeCategories, boolean excludeModifications) {
            this.mIdentifierTypes = (Set) Objects.requireNonNull(identifierTypes);
            this.mIdentifiers = (Set) Objects.requireNonNull(identifiers);
            this.mIncludeCategories = includeCategories;
            this.mExcludeModifications = excludeModifications;
            this.mVendorFilter = null;
        }

        public Filter() {
            this.mIdentifierTypes = Collections.emptySet();
            this.mIdentifiers = Collections.emptySet();
            this.mIncludeCategories = false;
            this.mExcludeModifications = false;
            this.mVendorFilter = null;
        }

        public Filter(Map<String, String> vendorFilter) {
            this.mIdentifierTypes = Collections.emptySet();
            this.mIdentifiers = Collections.emptySet();
            this.mIncludeCategories = false;
            this.mExcludeModifications = false;
            this.mVendorFilter = vendorFilter;
        }

        private Filter(Parcel in) {
            this.mIdentifierTypes = Utils.createIntSet(in);
            this.mIdentifiers = Utils.createSet(in, ProgramSelector.Identifier.CREATOR);
            boolean z = false;
            this.mIncludeCategories = in.readByte() != 0;
            this.mExcludeModifications = in.readByte() != 0 ? true : z;
            this.mVendorFilter = Utils.readStringMap(in);
        }

        public void writeToParcel(Parcel dest, int flags) {
            Utils.writeIntSet(dest, this.mIdentifierTypes);
            Utils.writeSet(dest, this.mIdentifiers);
            dest.writeByte(this.mIncludeCategories ? (byte) 1 : 0);
            dest.writeByte(this.mExcludeModifications ? (byte) 1 : 0);
            Utils.writeStringMap(dest, this.mVendorFilter);
        }

        public int describeContents() {
            return 0;
        }

        public Map<String, String> getVendorFilter() {
            return this.mVendorFilter;
        }

        public Set<Integer> getIdentifierTypes() {
            return this.mIdentifierTypes;
        }

        public Set<ProgramSelector.Identifier> getIdentifiers() {
            return this.mIdentifiers;
        }

        public boolean areCategoriesIncluded() {
            return this.mIncludeCategories;
        }

        public boolean areModificationsExcluded() {
            return this.mExcludeModifications;
        }
    }

    public static abstract class ListCallback {
        public void onItemChanged(ProgramSelector.Identifier id) {
        }

        public void onItemRemoved(ProgramSelector.Identifier id) {
        }
    }

    interface OnCloseListener {
        void onClose();
    }

    public interface OnCompleteListener {
        void onComplete();
    }

    ProgramList() {
    }

    public void registerListCallback(final Executor executor, final ListCallback callback) {
        registerListCallback(new ListCallback() {
            public void onItemChanged(ProgramSelector.Identifier id) {
                executor.execute(new Runnable(id) {
                    private final /* synthetic */ ProgramSelector.Identifier f$1;

                    {
                        this.f$1 = r2;
                    }

                    public final void run() {
                        ProgramList.ListCallback.this.onItemChanged(this.f$1);
                    }
                });
            }

            public void onItemRemoved(ProgramSelector.Identifier id) {
                executor.execute(new Runnable(id) {
                    private final /* synthetic */ ProgramSelector.Identifier f$1;

                    {
                        this.f$1 = r2;
                    }

                    public final void run() {
                        ProgramList.ListCallback.this.onItemRemoved(this.f$1);
                    }
                });
            }
        });
    }

    public void registerListCallback(ListCallback callback) {
        synchronized (this.mLock) {
            if (!this.mIsClosed) {
                this.mListCallbacks.add((ListCallback) Objects.requireNonNull(callback));
            }
        }
    }

    public void unregisterListCallback(ListCallback callback) {
        synchronized (this.mLock) {
            if (!this.mIsClosed) {
                this.mListCallbacks.remove(Objects.requireNonNull(callback));
            }
        }
    }

    static /* synthetic */ void lambda$addOnCompleteListener$0(Executor executor, OnCompleteListener listener) {
        Objects.requireNonNull(listener);
        executor.execute(new Runnable() {
            public final void run() {
                ProgramList.OnCompleteListener.this.onComplete();
            }
        });
    }

    public void addOnCompleteListener(Executor executor, OnCompleteListener listener) {
        addOnCompleteListener(new OnCompleteListener(executor, listener) {
            private final /* synthetic */ Executor f$0;
            private final /* synthetic */ ProgramList.OnCompleteListener f$1;

            {
                this.f$0 = r1;
                this.f$1 = r2;
            }

            public final void onComplete() {
                ProgramList.lambda$addOnCompleteListener$0(this.f$0, this.f$1);
            }
        });
    }

    /* JADX WARNING: Code restructure failed: missing block: B:11:0x001c, code lost:
        return;
     */
    public void addOnCompleteListener(OnCompleteListener listener) {
        synchronized (this.mLock) {
            if (!this.mIsClosed) {
                this.mOnCompleteListeners.add((OnCompleteListener) Objects.requireNonNull(listener));
                if (this.mIsComplete) {
                    listener.onComplete();
                }
            }
        }
    }

    public void removeOnCompleteListener(OnCompleteListener listener) {
        synchronized (this.mLock) {
            if (!this.mIsClosed) {
                this.mOnCompleteListeners.remove(Objects.requireNonNull(listener));
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void setOnCloseListener(OnCloseListener listener) {
        synchronized (this.mLock) {
            if (this.mOnCloseListener == null) {
                this.mOnCloseListener = listener;
            } else {
                throw new IllegalStateException("Close callback is already set");
            }
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:11:0x0028, code lost:
        return;
     */
    public void close() {
        synchronized (this.mLock) {
            if (!this.mIsClosed) {
                this.mIsClosed = true;
                this.mPrograms.clear();
                this.mListCallbacks.clear();
                this.mOnCompleteListeners.clear();
                if (this.mOnCloseListener != null) {
                    this.mOnCloseListener.onClose();
                    this.mOnCloseListener = null;
                }
            }
        }
    }

    /* access modifiers changed from: package-private */
    /* JADX WARNING: Code restructure failed: missing block: B:14:0x005a, code lost:
        return;
     */
    public void apply(Chunk chunk) {
        synchronized (this.mLock) {
            if (!this.mIsClosed) {
                this.mIsComplete = false;
                if (chunk.isPurge()) {
                    new HashSet(this.mPrograms.keySet()).stream().forEach(new Consumer() {
                        public final void accept(Object obj) {
                            ProgramList.this.removeLocked((ProgramSelector.Identifier) obj);
                        }
                    });
                }
                chunk.getRemoved().stream().forEach(new Consumer() {
                    public final void accept(Object obj) {
                        ProgramList.this.removeLocked((ProgramSelector.Identifier) obj);
                    }
                });
                chunk.getModified().stream().forEach(new Consumer() {
                    public final void accept(Object obj) {
                        ProgramList.this.putLocked((RadioManager.ProgramInfo) obj);
                    }
                });
                if (chunk.isComplete()) {
                    this.mIsComplete = true;
                    this.mOnCompleteListeners.forEach($$Lambda$ProgramList$GfCj9jJ5znxw2TV4c2uykq35dgI.INSTANCE);
                }
            }
        }
    }

    /* access modifiers changed from: private */
    public void putLocked(RadioManager.ProgramInfo value) {
        this.mPrograms.put((ProgramSelector.Identifier) Objects.requireNonNull(value.getSelector().getPrimaryId()), value);
        this.mListCallbacks.forEach(new Consumer() {
            public final void accept(Object obj) {
                ((ProgramList.ListCallback) obj).onItemChanged(ProgramSelector.Identifier.this);
            }
        });
    }

    /* access modifiers changed from: private */
    public void removeLocked(ProgramSelector.Identifier key) {
        RadioManager.ProgramInfo removed = this.mPrograms.remove(Objects.requireNonNull(key));
        if (removed != null) {
            this.mListCallbacks.forEach(new Consumer() {
                public final void accept(Object obj) {
                    ((ProgramList.ListCallback) obj).onItemRemoved(ProgramSelector.Identifier.this);
                }
            });
        }
    }

    public List<RadioManager.ProgramInfo> toList() {
        List<RadioManager.ProgramInfo> list;
        synchronized (this.mLock) {
            list = (List) this.mPrograms.values().stream().collect(Collectors.toList());
        }
        return list;
    }

    public RadioManager.ProgramInfo get(ProgramSelector.Identifier id) {
        RadioManager.ProgramInfo programInfo;
        synchronized (this.mLock) {
            programInfo = this.mPrograms.get(Objects.requireNonNull(id));
        }
        return programInfo;
    }
}
