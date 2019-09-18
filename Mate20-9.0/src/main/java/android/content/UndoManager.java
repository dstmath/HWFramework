package android.content;

import android.os.Parcel;
import android.text.TextUtils;
import android.util.ArrayMap;
import java.util.ArrayList;

public class UndoManager {
    public static final int MERGE_MODE_ANY = 2;
    public static final int MERGE_MODE_NONE = 0;
    public static final int MERGE_MODE_UNIQUE = 1;
    private int mCommitId = 1;
    private int mHistorySize = 20;
    private boolean mInUndo;
    private boolean mMerged;
    private int mNextSavedIdx;
    private final ArrayMap<String, UndoOwner> mOwners = new ArrayMap<>(1);
    private final ArrayList<UndoState> mRedos = new ArrayList<>();
    private UndoOwner[] mStateOwners;
    private int mStateSeq;
    private final ArrayList<UndoState> mUndos = new ArrayList<>();
    private int mUpdateCount;
    private UndoState mWorking;

    static final class UndoState {
        private boolean mCanMerge;
        private final int mCommitId;
        private boolean mExecuted;
        private CharSequence mLabel;
        private final UndoManager mManager;
        private final ArrayList<UndoOperation<?>> mOperations;
        private ArrayList<UndoOperation<?>> mRecent;

        UndoState(UndoManager manager, int commitId) {
            this.mOperations = new ArrayList<>();
            this.mCanMerge = true;
            this.mManager = manager;
            this.mCommitId = commitId;
        }

        UndoState(UndoManager manager, Parcel p, ClassLoader loader) {
            this.mOperations = new ArrayList<>();
            boolean z = true;
            this.mCanMerge = true;
            this.mManager = manager;
            this.mCommitId = p.readInt();
            int i = 0;
            this.mCanMerge = p.readInt() != 0;
            this.mExecuted = p.readInt() == 0 ? false : z;
            this.mLabel = (CharSequence) TextUtils.CHAR_SEQUENCE_CREATOR.createFromParcel(p);
            int N = p.readInt();
            while (true) {
                int i2 = i;
                if (i2 < N) {
                    UndoOwner owner = this.mManager.restoreOwner(p);
                    UndoOperation op = (UndoOperation) p.readParcelable(loader);
                    op.mOwner = owner;
                    this.mOperations.add(op);
                    i = i2 + 1;
                } else {
                    return;
                }
            }
        }

        /* access modifiers changed from: package-private */
        public void writeToParcel(Parcel p) {
            if (this.mRecent == null) {
                p.writeInt(this.mCommitId);
                p.writeInt(this.mCanMerge ? 1 : 0);
                p.writeInt(this.mExecuted ? 1 : 0);
                TextUtils.writeToParcel(this.mLabel, p, 0);
                int N = this.mOperations.size();
                p.writeInt(N);
                for (int i = 0; i < N; i++) {
                    UndoOperation op = this.mOperations.get(i);
                    this.mManager.saveOwner(op.mOwner, p);
                    p.writeParcelable(op, 0);
                }
                return;
            }
            throw new IllegalStateException("Can't save state before committing");
        }

        /* access modifiers changed from: package-private */
        public int getCommitId() {
            return this.mCommitId;
        }

        /* access modifiers changed from: package-private */
        public void setLabel(CharSequence label) {
            this.mLabel = label;
        }

        /* access modifiers changed from: package-private */
        public void updateLabel(CharSequence label) {
            if (this.mLabel != null) {
                this.mLabel = label;
            }
        }

        /* access modifiers changed from: package-private */
        public CharSequence getLabel() {
            return this.mLabel;
        }

        /* access modifiers changed from: package-private */
        public boolean setCanMerge(boolean state) {
            if (state && this.mExecuted) {
                return false;
            }
            this.mCanMerge = state;
            return true;
        }

        /* access modifiers changed from: package-private */
        public void makeExecuted() {
            this.mExecuted = true;
        }

        /* access modifiers changed from: package-private */
        public boolean canMerge() {
            return this.mCanMerge && !this.mExecuted;
        }

        /* access modifiers changed from: package-private */
        public int countOperations() {
            return this.mOperations.size();
        }

        /* access modifiers changed from: package-private */
        public boolean hasOperation(UndoOwner owner) {
            int N = this.mOperations.size();
            boolean z = false;
            if (owner == null) {
                if (N != 0) {
                    z = true;
                }
                return z;
            }
            for (int i = 0; i < N; i++) {
                if (this.mOperations.get(i).getOwner() == owner) {
                    return true;
                }
            }
            return false;
        }

        /* access modifiers changed from: package-private */
        public boolean hasMultipleOwners() {
            int N = this.mOperations.size();
            if (N <= 1) {
                return false;
            }
            UndoOwner owner = this.mOperations.get(0).getOwner();
            for (int i = 1; i < N; i++) {
                if (this.mOperations.get(i).getOwner() != owner) {
                    return true;
                }
            }
            return false;
        }

        /* access modifiers changed from: package-private */
        public void addOperation(UndoOperation<?> op) {
            if (!this.mOperations.contains(op)) {
                this.mOperations.add(op);
                if (this.mRecent == null) {
                    this.mRecent = new ArrayList<>();
                    this.mRecent.add(op);
                }
                op.mOwner.mOpCount++;
                return;
            }
            throw new IllegalStateException("Already holds " + op);
        }

        /* access modifiers changed from: package-private */
        public <T extends UndoOperation> T getLastOperation(Class<T> clazz, UndoOwner owner) {
            int N = this.mOperations.size();
            T t = null;
            if (clazz == null && owner == null) {
                if (N > 0) {
                    t = (UndoOperation) this.mOperations.get(N - 1);
                }
                return t;
            }
            int i = N - 1;
            while (i >= 0) {
                UndoOperation<?> op = this.mOperations.get(i);
                if (owner != null && op.getOwner() != owner) {
                    i--;
                } else if (clazz == null || op.getClass() == clazz) {
                    return op;
                } else {
                    return null;
                }
            }
            return null;
        }

        /* access modifiers changed from: package-private */
        public boolean matchOwner(UndoOwner owner) {
            for (int i = this.mOperations.size() - 1; i >= 0; i--) {
                if (this.mOperations.get(i).matchOwner(owner)) {
                    return true;
                }
            }
            return false;
        }

        /* access modifiers changed from: package-private */
        public boolean hasData() {
            for (int i = this.mOperations.size() - 1; i >= 0; i--) {
                if (this.mOperations.get(i).hasData()) {
                    return true;
                }
            }
            return false;
        }

        /* access modifiers changed from: package-private */
        public void commit() {
            int N = this.mRecent != null ? this.mRecent.size() : 0;
            for (int i = 0; i < N; i++) {
                this.mRecent.get(i).commit();
            }
            this.mRecent = null;
        }

        /* access modifiers changed from: package-private */
        public void undo() {
            for (int i = this.mOperations.size() - 1; i >= 0; i--) {
                this.mOperations.get(i).undo();
            }
        }

        /* access modifiers changed from: package-private */
        public void redo() {
            int N = this.mOperations.size();
            for (int i = 0; i < N; i++) {
                this.mOperations.get(i).redo();
            }
        }

        /* access modifiers changed from: package-private */
        public void destroy() {
            for (int i = this.mOperations.size() - 1; i >= 0; i--) {
                UndoOwner owner = this.mOperations.get(i).mOwner;
                owner.mOpCount--;
                if (owner.mOpCount <= 0) {
                    if (owner.mOpCount >= 0) {
                        this.mManager.removeOwner(owner);
                    } else {
                        throw new IllegalStateException("Underflow of op count on owner " + owner + " in op " + this.mOperations.get(i));
                    }
                }
            }
        }
    }

    public UndoOwner getOwner(String tag, Object data) {
        if (tag == null) {
            throw new NullPointerException("tag can't be null");
        } else if (data != null) {
            UndoOwner owner = this.mOwners.get(tag);
            if (owner != null) {
                if (owner.mData != data) {
                    if (owner.mData == null) {
                        owner.mData = data;
                    } else {
                        throw new IllegalStateException("Owner " + owner + " already exists with data " + owner.mData + " but giving different data " + data);
                    }
                }
                return owner;
            }
            UndoOwner owner2 = new UndoOwner(tag, this);
            owner2.mData = data;
            this.mOwners.put(tag, owner2);
            return owner2;
        } else {
            throw new NullPointerException("data can't be null");
        }
    }

    /* access modifiers changed from: package-private */
    public void removeOwner(UndoOwner owner) {
    }

    public void saveInstanceState(Parcel p) {
        if (this.mUpdateCount <= 0) {
            this.mStateSeq++;
            if (this.mStateSeq <= 0) {
                this.mStateSeq = 0;
            }
            this.mNextSavedIdx = 0;
            p.writeInt(this.mHistorySize);
            p.writeInt(this.mOwners.size());
            int i = this.mUndos.size();
            while (i > 0) {
                p.writeInt(1);
                i--;
                this.mUndos.get(i).writeToParcel(p);
            }
            int i2 = this.mRedos.size();
            while (i2 > 0) {
                p.writeInt(2);
                i2--;
                this.mRedos.get(i2).writeToParcel(p);
            }
            p.writeInt(0);
            return;
        }
        throw new IllegalStateException("Can't save state while updating");
    }

    /* access modifiers changed from: package-private */
    public void saveOwner(UndoOwner owner, Parcel out) {
        if (owner.mStateSeq == this.mStateSeq) {
            out.writeInt(owner.mSavedIdx);
            return;
        }
        owner.mStateSeq = this.mStateSeq;
        owner.mSavedIdx = this.mNextSavedIdx;
        out.writeInt(owner.mSavedIdx);
        out.writeString(owner.mTag);
        out.writeInt(owner.mOpCount);
        this.mNextSavedIdx++;
    }

    public void restoreInstanceState(Parcel p, ClassLoader loader) {
        if (this.mUpdateCount <= 0) {
            forgetUndos(null, -1);
            forgetRedos(null, -1);
            this.mHistorySize = p.readInt();
            this.mStateOwners = new UndoOwner[p.readInt()];
            while (true) {
                int readInt = p.readInt();
                int stype = readInt;
                if (readInt != 0) {
                    UndoState ustate = new UndoState(this, p, loader);
                    if (stype == 1) {
                        this.mUndos.add(0, ustate);
                    } else {
                        this.mRedos.add(0, ustate);
                    }
                } else {
                    return;
                }
            }
        } else {
            throw new IllegalStateException("Can't save state while updating");
        }
    }

    /* access modifiers changed from: package-private */
    public UndoOwner restoreOwner(Parcel in) {
        int idx = in.readInt();
        UndoOwner owner = this.mStateOwners[idx];
        if (owner != null) {
            return owner;
        }
        String tag = in.readString();
        int opCount = in.readInt();
        UndoOwner owner2 = new UndoOwner(tag, this);
        owner2.mOpCount = opCount;
        this.mStateOwners[idx] = owner2;
        this.mOwners.put(tag, owner2);
        return owner2;
    }

    public void setHistorySize(int size) {
        this.mHistorySize = size;
        if (this.mHistorySize >= 0 && countUndos(null) > this.mHistorySize) {
            forgetUndos(null, countUndos(null) - this.mHistorySize);
        }
    }

    public int getHistorySize() {
        return this.mHistorySize;
    }

    public int undo(UndoOwner[] owners, int count) {
        if (this.mWorking == null) {
            int num = 0;
            int i = -1;
            this.mInUndo = true;
            UndoState us = getTopUndo(null);
            if (us != null) {
                us.makeExecuted();
            }
            while (count > 0) {
                int findPrevState = findPrevState(this.mUndos, owners, i);
                i = findPrevState;
                if (findPrevState < 0) {
                    break;
                }
                UndoState state = this.mUndos.remove(i);
                state.undo();
                this.mRedos.add(state);
                count--;
                num++;
            }
            this.mInUndo = false;
            return num;
        }
        throw new IllegalStateException("Can't be called during an update");
    }

    public int redo(UndoOwner[] owners, int count) {
        if (this.mWorking == null) {
            int num = 0;
            int i = -1;
            this.mInUndo = true;
            while (count > 0) {
                int findPrevState = findPrevState(this.mRedos, owners, i);
                i = findPrevState;
                if (findPrevState < 0) {
                    break;
                }
                UndoState state = this.mRedos.remove(i);
                state.redo();
                this.mUndos.add(state);
                count--;
                num++;
            }
            this.mInUndo = false;
            return num;
        }
        throw new IllegalStateException("Can't be called during an update");
    }

    public boolean isInUndo() {
        return this.mInUndo;
    }

    public int forgetUndos(UndoOwner[] owners, int count) {
        if (count < 0) {
            count = this.mUndos.size();
        }
        int removed = 0;
        int i = 0;
        while (i < this.mUndos.size() && removed < count) {
            UndoState state = this.mUndos.get(i);
            if (count <= 0 || !matchOwners(state, owners)) {
                i++;
            } else {
                state.destroy();
                this.mUndos.remove(i);
                removed++;
            }
        }
        return removed;
    }

    public int forgetRedos(UndoOwner[] owners, int count) {
        if (count < 0) {
            count = this.mRedos.size();
        }
        int removed = 0;
        int i = 0;
        while (i < this.mRedos.size() && removed < count) {
            UndoState state = this.mRedos.get(i);
            if (count <= 0 || !matchOwners(state, owners)) {
                i++;
            } else {
                state.destroy();
                this.mRedos.remove(i);
                removed++;
            }
        }
        return removed;
    }

    public int countUndos(UndoOwner[] owners) {
        if (owners == null) {
            return this.mUndos.size();
        }
        int count = 0;
        int i = 0;
        while (true) {
            int findNextState = findNextState(this.mUndos, owners, i);
            int i2 = findNextState;
            if (findNextState < 0) {
                return count;
            }
            count++;
            i = i2 + 1;
        }
    }

    public int countRedos(UndoOwner[] owners) {
        if (owners == null) {
            return this.mRedos.size();
        }
        int count = 0;
        int i = 0;
        while (true) {
            int findNextState = findNextState(this.mRedos, owners, i);
            int i2 = findNextState;
            if (findNextState < 0) {
                return count;
            }
            count++;
            i = i2 + 1;
        }
    }

    public CharSequence getUndoLabel(UndoOwner[] owners) {
        UndoState state = getTopUndo(owners);
        if (state != null) {
            return state.getLabel();
        }
        return null;
    }

    public CharSequence getRedoLabel(UndoOwner[] owners) {
        UndoState state = getTopRedo(owners);
        if (state != null) {
            return state.getLabel();
        }
        return null;
    }

    public void beginUpdate(CharSequence label) {
        if (!this.mInUndo) {
            if (this.mUpdateCount <= 0) {
                createWorkingState();
                this.mMerged = false;
                this.mUpdateCount = 0;
            }
            this.mWorking.updateLabel(label);
            this.mUpdateCount++;
            return;
        }
        throw new IllegalStateException("Can't being update while performing undo/redo");
    }

    private void createWorkingState() {
        int i = this.mCommitId;
        this.mCommitId = i + 1;
        this.mWorking = new UndoState(this, i);
        if (this.mCommitId < 0) {
            this.mCommitId = 1;
        }
    }

    public boolean isInUpdate() {
        return this.mUpdateCount > 0;
    }

    public void setUndoLabel(CharSequence label) {
        if (this.mWorking != null) {
            this.mWorking.setLabel(label);
            return;
        }
        throw new IllegalStateException("Must be called during an update");
    }

    public void suggestUndoLabel(CharSequence label) {
        if (this.mWorking != null) {
            this.mWorking.updateLabel(label);
            return;
        }
        throw new IllegalStateException("Must be called during an update");
    }

    public int getUpdateNestingLevel() {
        return this.mUpdateCount;
    }

    public boolean hasOperation(UndoOwner owner) {
        if (this.mWorking != null) {
            return this.mWorking.hasOperation(owner);
        }
        throw new IllegalStateException("Must be called during an update");
    }

    public UndoOperation<?> getLastOperation(int mergeMode) {
        return getLastOperation(null, null, mergeMode);
    }

    public UndoOperation<?> getLastOperation(UndoOwner owner, int mergeMode) {
        return getLastOperation(null, owner, mergeMode);
    }

    public <T extends UndoOperation> T getLastOperation(Class<T> clazz, UndoOwner owner, int mergeMode) {
        if (this.mWorking != null) {
            if (mergeMode != 0 && !this.mMerged && !this.mWorking.hasData()) {
                UndoState state = getTopUndo(null);
                if (state != null && ((mergeMode == 2 || !state.hasMultipleOwners()) && state.canMerge())) {
                    UndoOperation<?> lastOperation = state.getLastOperation(clazz, owner);
                    UndoOperation<?> last = lastOperation;
                    if (lastOperation != null && last.allowMerge()) {
                        this.mWorking.destroy();
                        this.mWorking = state;
                        this.mUndos.remove(state);
                        this.mMerged = true;
                        return last;
                    }
                }
            }
            return this.mWorking.getLastOperation(clazz, owner);
        }
        throw new IllegalStateException("Must be called during an update");
    }

    public void addOperation(UndoOperation<?> op, int mergeMode) {
        if (this.mWorking == null) {
            throw new IllegalStateException("Must be called during an update");
        } else if (op.getOwner().mManager == this) {
            if (mergeMode != 0 && !this.mMerged && !this.mWorking.hasData()) {
                UndoState state = getTopUndo(null);
                if (state != null && ((mergeMode == 2 || !state.hasMultipleOwners()) && state.canMerge() && state.hasOperation(op.getOwner()))) {
                    this.mWorking.destroy();
                    this.mWorking = state;
                    this.mUndos.remove(state);
                    this.mMerged = true;
                }
            }
            this.mWorking.addOperation(op);
        } else {
            throw new IllegalArgumentException("Given operation's owner is not in this undo manager.");
        }
    }

    public void endUpdate() {
        if (this.mWorking != null) {
            this.mUpdateCount--;
            if (this.mUpdateCount == 0) {
                pushWorkingState();
                return;
            }
            return;
        }
        throw new IllegalStateException("Must be called during an update");
    }

    private void pushWorkingState() {
        int N = this.mUndos.size() + 1;
        if (this.mWorking.hasData()) {
            this.mUndos.add(this.mWorking);
            forgetRedos(null, -1);
            this.mWorking.commit();
            if (N >= 2) {
                this.mUndos.get(N - 2).makeExecuted();
            }
        } else {
            this.mWorking.destroy();
        }
        this.mWorking = null;
        if (this.mHistorySize >= 0 && N > this.mHistorySize) {
            forgetUndos(null, N - this.mHistorySize);
        }
    }

    public int commitState(UndoOwner owner) {
        if (this.mWorking == null || !this.mWorking.hasData()) {
            UndoState state = getTopUndo(null);
            if (state != null && (owner == null || state.hasOperation(owner))) {
                state.setCanMerge(false);
                return state.getCommitId();
            }
        } else if (owner == null || this.mWorking.hasOperation(owner)) {
            this.mWorking.setCanMerge(false);
            int commitId = this.mWorking.getCommitId();
            pushWorkingState();
            createWorkingState();
            this.mMerged = true;
            return commitId;
        }
        return -1;
    }

    public boolean uncommitState(int commitId, UndoOwner owner) {
        if (this.mWorking == null || this.mWorking.getCommitId() != commitId) {
            UndoState state = getTopUndo(null);
            if (state != null && ((owner == null || state.hasOperation(owner)) && state.getCommitId() == commitId)) {
                return state.setCanMerge(true);
            }
        } else if (owner == null || this.mWorking.hasOperation(owner)) {
            return this.mWorking.setCanMerge(true);
        }
        return false;
    }

    /* access modifiers changed from: package-private */
    public UndoState getTopUndo(UndoOwner[] owners) {
        UndoState undoState = null;
        if (this.mUndos.size() <= 0) {
            return null;
        }
        int i = findPrevState(this.mUndos, owners, -1);
        if (i >= 0) {
            undoState = this.mUndos.get(i);
        }
        return undoState;
    }

    /* access modifiers changed from: package-private */
    public UndoState getTopRedo(UndoOwner[] owners) {
        UndoState undoState = null;
        if (this.mRedos.size() <= 0) {
            return null;
        }
        int i = findPrevState(this.mRedos, owners, -1);
        if (i >= 0) {
            undoState = this.mRedos.get(i);
        }
        return undoState;
    }

    /* access modifiers changed from: package-private */
    public boolean matchOwners(UndoState state, UndoOwner[] owners) {
        if (owners == null) {
            return true;
        }
        for (UndoOwner matchOwner : owners) {
            if (state.matchOwner(matchOwner)) {
                return true;
            }
        }
        return false;
    }

    /* access modifiers changed from: package-private */
    public int findPrevState(ArrayList<UndoState> states, UndoOwner[] owners, int from) {
        int N = states.size();
        if (from == -1) {
            from = N - 1;
        }
        if (from >= N) {
            return -1;
        }
        if (owners == null) {
            return from;
        }
        while (from >= 0) {
            if (matchOwners(states.get(from), owners)) {
                return from;
            }
            from--;
        }
        return -1;
    }

    /* access modifiers changed from: package-private */
    public int findNextState(ArrayList<UndoState> states, UndoOwner[] owners, int from) {
        int N = states.size();
        if (from < 0) {
            from = 0;
        }
        if (from >= N) {
            return -1;
        }
        if (owners == null) {
            return from;
        }
        while (from < N) {
            if (matchOwners(states.get(from), owners)) {
                return from;
            }
            from++;
        }
        return -1;
    }
}
