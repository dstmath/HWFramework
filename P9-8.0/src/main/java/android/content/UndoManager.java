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
    private final ArrayMap<String, UndoOwner> mOwners = new ArrayMap(1);
    private final ArrayList<UndoState> mRedos = new ArrayList();
    private UndoOwner[] mStateOwners;
    private int mStateSeq;
    private final ArrayList<UndoState> mUndos = new ArrayList();
    private int mUpdateCount;
    private UndoState mWorking;

    static final class UndoState {
        private boolean mCanMerge = true;
        private final int mCommitId;
        private boolean mExecuted;
        private CharSequence mLabel;
        private final UndoManager mManager;
        private final ArrayList<UndoOperation<?>> mOperations = new ArrayList();
        private ArrayList<UndoOperation<?>> mRecent;

        UndoState(UndoManager manager, int commitId) {
            this.mManager = manager;
            this.mCommitId = commitId;
        }

        UndoState(UndoManager manager, Parcel p, ClassLoader loader) {
            boolean z;
            boolean z2 = true;
            this.mManager = manager;
            this.mCommitId = p.readInt();
            if (p.readInt() != 0) {
                z = true;
            } else {
                z = false;
            }
            this.mCanMerge = z;
            if (p.readInt() == 0) {
                z2 = false;
            }
            this.mExecuted = z2;
            this.mLabel = (CharSequence) TextUtils.CHAR_SEQUENCE_CREATOR.createFromParcel(p);
            int N = p.readInt();
            for (int i = 0; i < N; i++) {
                UndoOperation op = (UndoOperation) p.readParcelable(loader);
                op.mOwner = this.mManager.restoreOwner(p);
                this.mOperations.add(op);
            }
        }

        void writeToParcel(Parcel p) {
            int i = 1;
            if (this.mRecent != null) {
                throw new IllegalStateException("Can't save state before committing");
            }
            int i2;
            p.writeInt(this.mCommitId);
            if (this.mCanMerge) {
                i2 = 1;
            } else {
                i2 = 0;
            }
            p.writeInt(i2);
            if (!this.mExecuted) {
                i = 0;
            }
            p.writeInt(i);
            TextUtils.writeToParcel(this.mLabel, p, 0);
            int N = this.mOperations.size();
            p.writeInt(N);
            for (int i3 = 0; i3 < N; i3++) {
                UndoOperation op = (UndoOperation) this.mOperations.get(i3);
                this.mManager.saveOwner(op.mOwner, p);
                p.writeParcelable(op, 0);
            }
        }

        int getCommitId() {
            return this.mCommitId;
        }

        void setLabel(CharSequence label) {
            this.mLabel = label;
        }

        void updateLabel(CharSequence label) {
            if (this.mLabel != null) {
                this.mLabel = label;
            }
        }

        CharSequence getLabel() {
            return this.mLabel;
        }

        boolean setCanMerge(boolean state) {
            if (state && this.mExecuted) {
                return false;
            }
            this.mCanMerge = state;
            return true;
        }

        void makeExecuted() {
            this.mExecuted = true;
        }

        boolean canMerge() {
            return this.mCanMerge ? this.mExecuted ^ 1 : false;
        }

        int countOperations() {
            return this.mOperations.size();
        }

        boolean hasOperation(UndoOwner owner) {
            int N = this.mOperations.size();
            if (owner == null) {
                return N != 0;
            }
            for (int i = 0; i < N; i++) {
                if (((UndoOperation) this.mOperations.get(i)).getOwner() == owner) {
                    return true;
                }
            }
            return false;
        }

        boolean hasMultipleOwners() {
            int N = this.mOperations.size();
            if (N <= 1) {
                return false;
            }
            UndoOwner owner = ((UndoOperation) this.mOperations.get(0)).getOwner();
            for (int i = 1; i < N; i++) {
                if (((UndoOperation) this.mOperations.get(i)).getOwner() != owner) {
                    return true;
                }
            }
            return false;
        }

        void addOperation(UndoOperation<?> op) {
            if (this.mOperations.contains(op)) {
                throw new IllegalStateException("Already holds " + op);
            }
            this.mOperations.add(op);
            if (this.mRecent == null) {
                this.mRecent = new ArrayList();
                this.mRecent.add(op);
            }
            UndoOwner undoOwner = op.mOwner;
            undoOwner.mOpCount++;
        }

        <T extends UndoOperation> T getLastOperation(Class<T> clazz, UndoOwner owner) {
            T t = null;
            int N = this.mOperations.size();
            if (clazz == null && owner == null) {
                if (N > 0) {
                    t = (UndoOperation) this.mOperations.get(N - 1);
                }
                return t;
            }
            int i = N - 1;
            while (i >= 0) {
                UndoOperation<?> op = (UndoOperation) this.mOperations.get(i);
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

        boolean matchOwner(UndoOwner owner) {
            for (int i = this.mOperations.size() - 1; i >= 0; i--) {
                if (((UndoOperation) this.mOperations.get(i)).matchOwner(owner)) {
                    return true;
                }
            }
            return false;
        }

        boolean hasData() {
            for (int i = this.mOperations.size() - 1; i >= 0; i--) {
                if (((UndoOperation) this.mOperations.get(i)).hasData()) {
                    return true;
                }
            }
            return false;
        }

        void commit() {
            int N = this.mRecent != null ? this.mRecent.size() : 0;
            for (int i = 0; i < N; i++) {
                ((UndoOperation) this.mRecent.get(i)).commit();
            }
            this.mRecent = null;
        }

        void undo() {
            for (int i = this.mOperations.size() - 1; i >= 0; i--) {
                ((UndoOperation) this.mOperations.get(i)).undo();
            }
        }

        void redo() {
            int N = this.mOperations.size();
            for (int i = 0; i < N; i++) {
                ((UndoOperation) this.mOperations.get(i)).redo();
            }
        }

        void destroy() {
            for (int i = this.mOperations.size() - 1; i >= 0; i--) {
                UndoOwner owner = ((UndoOperation) this.mOperations.get(i)).mOwner;
                owner.mOpCount--;
                if (owner.mOpCount <= 0) {
                    if (owner.mOpCount < 0) {
                        throw new IllegalStateException("Underflow of op count on owner " + owner + " in op " + this.mOperations.get(i));
                    }
                    this.mManager.removeOwner(owner);
                }
            }
        }
    }

    public UndoOwner getOwner(String tag, Object data) {
        if (tag == null) {
            throw new NullPointerException("tag can't be null");
        } else if (data == null) {
            throw new NullPointerException("data can't be null");
        } else {
            UndoOwner owner = (UndoOwner) this.mOwners.get(tag);
            if (owner != null) {
                if (owner.mData != data) {
                    if (owner.mData != null) {
                        throw new IllegalStateException("Owner " + owner + " already exists with data " + owner.mData + " but giving different data " + data);
                    }
                    owner.mData = data;
                }
                return owner;
            }
            owner = new UndoOwner(tag, this);
            owner.mData = data;
            this.mOwners.put(tag, owner);
            return owner;
        }
    }

    void removeOwner(UndoOwner owner) {
    }

    public void saveInstanceState(Parcel p) {
        if (this.mUpdateCount > 0) {
            throw new IllegalStateException("Can't save state while updating");
        }
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
            ((UndoState) this.mUndos.get(i)).writeToParcel(p);
        }
        i = this.mRedos.size();
        while (i > 0) {
            p.writeInt(2);
            i--;
            ((UndoState) this.mRedos.get(i)).writeToParcel(p);
        }
        p.writeInt(0);
    }

    void saveOwner(UndoOwner owner, Parcel out) {
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
        if (this.mUpdateCount > 0) {
            throw new IllegalStateException("Can't save state while updating");
        }
        forgetUndos(null, -1);
        forgetRedos(null, -1);
        this.mHistorySize = p.readInt();
        this.mStateOwners = new UndoOwner[p.readInt()];
        while (true) {
            int stype = p.readInt();
            if (stype != 0) {
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
    }

    UndoOwner restoreOwner(Parcel in) {
        int idx = in.readInt();
        UndoOwner owner = this.mStateOwners[idx];
        if (owner != null) {
            return owner;
        }
        String tag = in.readString();
        int opCount = in.readInt();
        owner = new UndoOwner(tag, this);
        owner.mOpCount = opCount;
        this.mStateOwners[idx] = owner;
        this.mOwners.put(tag, owner);
        return owner;
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
        if (this.mWorking != null) {
            throw new IllegalStateException("Can't be called during an update");
        }
        int num = 0;
        int i = -1;
        this.mInUndo = true;
        UndoState us = getTopUndo(null);
        if (us != null) {
            us.makeExecuted();
        }
        while (count > 0) {
            i = findPrevState(this.mUndos, owners, i);
            if (i < 0) {
                break;
            }
            UndoState state = (UndoState) this.mUndos.remove(i);
            state.undo();
            this.mRedos.add(state);
            count--;
            num++;
        }
        this.mInUndo = false;
        return num;
    }

    public int redo(UndoOwner[] owners, int count) {
        if (this.mWorking != null) {
            throw new IllegalStateException("Can't be called during an update");
        }
        int num = 0;
        int i = -1;
        this.mInUndo = true;
        while (count > 0) {
            i = findPrevState(this.mRedos, owners, i);
            if (i < 0) {
                break;
            }
            UndoState state = (UndoState) this.mRedos.remove(i);
            state.redo();
            this.mUndos.add(state);
            count--;
            num++;
        }
        this.mInUndo = false;
        return num;
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
            UndoState state = (UndoState) this.mUndos.get(i);
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
            UndoState state = (UndoState) this.mRedos.get(i);
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
            i = findNextState(this.mUndos, owners, i);
            if (i < 0) {
                return count;
            }
            count++;
            i++;
        }
    }

    public int countRedos(UndoOwner[] owners) {
        if (owners == null) {
            return this.mRedos.size();
        }
        int count = 0;
        int i = 0;
        while (true) {
            i = findNextState(this.mRedos, owners, i);
            if (i < 0) {
                return count;
            }
            count++;
            i++;
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
        if (this.mInUndo) {
            throw new IllegalStateException("Can't being update while performing undo/redo");
        }
        if (this.mUpdateCount <= 0) {
            createWorkingState();
            this.mMerged = false;
            this.mUpdateCount = 0;
        }
        this.mWorking.updateLabel(label);
        this.mUpdateCount++;
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
        if (this.mWorking == null) {
            throw new IllegalStateException("Must be called during an update");
        }
        this.mWorking.setLabel(label);
    }

    public void suggestUndoLabel(CharSequence label) {
        if (this.mWorking == null) {
            throw new IllegalStateException("Must be called during an update");
        }
        this.mWorking.updateLabel(label);
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
        if (this.mWorking == null) {
            throw new IllegalStateException("Must be called during an update");
        }
        if (!(mergeMode == 0 || (this.mMerged ^ 1) == 0 || (this.mWorking.hasData() ^ 1) == 0)) {
            UndoState state = getTopUndo(null);
            if (state != null && ((mergeMode == 2 || (state.hasMultipleOwners() ^ 1) != 0) && state.canMerge())) {
                UndoOperation<?> last = state.getLastOperation(clazz, owner);
                if (last != null && last.allowMerge()) {
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

    public void addOperation(UndoOperation<?> op, int mergeMode) {
        if (this.mWorking == null) {
            throw new IllegalStateException("Must be called during an update");
        } else if (op.getOwner().mManager != this) {
            throw new IllegalArgumentException("Given operation's owner is not in this undo manager.");
        } else {
            if (!(mergeMode == 0 || (this.mMerged ^ 1) == 0 || (this.mWorking.hasData() ^ 1) == 0)) {
                UndoState state = getTopUndo(null);
                if (state != null && ((mergeMode == 2 || (state.hasMultipleOwners() ^ 1) != 0) && state.canMerge() && state.hasOperation(op.getOwner()))) {
                    this.mWorking.destroy();
                    this.mWorking = state;
                    this.mUndos.remove(state);
                    this.mMerged = true;
                }
            }
            this.mWorking.addOperation(op);
        }
    }

    public void endUpdate() {
        if (this.mWorking == null) {
            throw new IllegalStateException("Must be called during an update");
        }
        this.mUpdateCount--;
        if (this.mUpdateCount == 0) {
            pushWorkingState();
        }
    }

    private void pushWorkingState() {
        int N = this.mUndos.size() + 1;
        if (this.mWorking.hasData()) {
            this.mUndos.add(this.mWorking);
            forgetRedos(null, -1);
            this.mWorking.commit();
            if (N >= 2) {
                ((UndoState) this.mUndos.get(N - 2)).makeExecuted();
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

    UndoState getTopUndo(UndoOwner[] owners) {
        UndoState undoState = null;
        if (this.mUndos.size() <= 0) {
            return null;
        }
        int i = findPrevState(this.mUndos, owners, -1);
        if (i >= 0) {
            undoState = (UndoState) this.mUndos.get(i);
        }
        return undoState;
    }

    UndoState getTopRedo(UndoOwner[] owners) {
        UndoState undoState = null;
        if (this.mRedos.size() <= 0) {
            return null;
        }
        int i = findPrevState(this.mRedos, owners, -1);
        if (i >= 0) {
            undoState = (UndoState) this.mRedos.get(i);
        }
        return undoState;
    }

    boolean matchOwners(UndoState state, UndoOwner[] owners) {
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

    int findPrevState(ArrayList<UndoState> states, UndoOwner[] owners, int from) {
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
            if (matchOwners((UndoState) states.get(from), owners)) {
                return from;
            }
            from--;
        }
        return -1;
    }

    int findNextState(ArrayList<UndoState> states, UndoOwner[] owners, int from) {
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
            if (matchOwners((UndoState) states.get(from), owners)) {
                return from;
            }
            from++;
        }
        return -1;
    }
}
