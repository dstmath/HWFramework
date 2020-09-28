package android.service.autofill;

import android.app.assist.AssistStructure;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.ArrayMap;
import android.util.SparseIntArray;
import android.view.autofill.AutofillId;
import android.view.autofill.Helper;
import java.util.LinkedList;

public final class FillContext implements Parcelable {
    public static final Parcelable.Creator<FillContext> CREATOR = new Parcelable.Creator<FillContext>() {
        /* class android.service.autofill.FillContext.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public FillContext createFromParcel(Parcel parcel) {
            return new FillContext(parcel);
        }

        @Override // android.os.Parcelable.Creator
        public FillContext[] newArray(int size) {
            return new FillContext[size];
        }
    };
    private final AutofillId mFocusedId;
    private final int mRequestId;
    private final AssistStructure mStructure;
    private ArrayMap<AutofillId, AssistStructure.ViewNode> mViewNodeLookupTable;

    public FillContext(int requestId, AssistStructure structure, AutofillId autofillId) {
        this.mRequestId = requestId;
        this.mStructure = structure;
        this.mFocusedId = autofillId;
    }

    private FillContext(Parcel parcel) {
        this(parcel.readInt(), (AssistStructure) parcel.readParcelable(null), (AutofillId) parcel.readParcelable(null));
    }

    public int getRequestId() {
        return this.mRequestId;
    }

    public AssistStructure getStructure() {
        return this.mStructure;
    }

    public AutofillId getFocusedId() {
        return this.mFocusedId;
    }

    public String toString() {
        if (!Helper.sDebug) {
            return super.toString();
        }
        return "FillContext [reqId=" + this.mRequestId + ", focusedId=" + this.mFocusedId + "]";
    }

    @Override // android.os.Parcelable
    public int describeContents() {
        return 0;
    }

    @Override // android.os.Parcelable
    public void writeToParcel(Parcel parcel, int flags) {
        parcel.writeInt(this.mRequestId);
        parcel.writeParcelable(this.mStructure, flags);
        parcel.writeParcelable(this.mFocusedId, flags);
    }

    public AssistStructure.ViewNode[] findViewNodesByAutofillIds(AutofillId[] ids) {
        LinkedList<AssistStructure.ViewNode> nodesToProcess = new LinkedList<>();
        AssistStructure.ViewNode[] foundNodes = new AssistStructure.ViewNode[ids.length];
        SparseIntArray missingNodeIndexes = new SparseIntArray(ids.length);
        for (int i = 0; i < ids.length; i++) {
            ArrayMap<AutofillId, AssistStructure.ViewNode> arrayMap = this.mViewNodeLookupTable;
            if (arrayMap != null) {
                int lookupTableIndex = arrayMap.indexOfKey(ids[i]);
                if (lookupTableIndex >= 0) {
                    foundNodes[i] = this.mViewNodeLookupTable.valueAt(lookupTableIndex);
                } else {
                    missingNodeIndexes.put(i, 0);
                }
            } else {
                missingNodeIndexes.put(i, 0);
            }
        }
        int numWindowNodes = this.mStructure.getWindowNodeCount();
        for (int i2 = 0; i2 < numWindowNodes; i2++) {
            nodesToProcess.add(this.mStructure.getWindowNodeAt(i2).getRootViewNode());
        }
        while (missingNodeIndexes.size() > 0 && !nodesToProcess.isEmpty()) {
            AssistStructure.ViewNode node = nodesToProcess.removeFirst();
            int i3 = 0;
            while (true) {
                if (i3 >= missingNodeIndexes.size()) {
                    break;
                }
                int index = missingNodeIndexes.keyAt(i3);
                AutofillId id = ids[index];
                if (id.equals(node.getAutofillId())) {
                    foundNodes[index] = node;
                    if (this.mViewNodeLookupTable == null) {
                        this.mViewNodeLookupTable = new ArrayMap<>(ids.length);
                    }
                    this.mViewNodeLookupTable.put(id, node);
                    missingNodeIndexes.removeAt(i3);
                } else {
                    i3++;
                }
            }
            for (int i4 = 0; i4 < node.getChildCount(); i4++) {
                nodesToProcess.addLast(node.getChildAt(i4));
            }
        }
        for (int i5 = 0; i5 < missingNodeIndexes.size(); i5++) {
            if (this.mViewNodeLookupTable == null) {
                this.mViewNodeLookupTable = new ArrayMap<>(missingNodeIndexes.size());
            }
            this.mViewNodeLookupTable.put(ids[missingNodeIndexes.keyAt(i5)], null);
        }
        return foundNodes;
    }
}
