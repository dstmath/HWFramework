package android.service.autofill;

import android.app.assist.AssistStructure;
import android.app.assist.AssistStructure.ViewNode;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import android.util.ArrayMap;
import android.util.SparseIntArray;
import android.view.autofill.AutofillId;
import android.view.autofill.Helper;
import java.util.LinkedList;

public final class FillContext implements Parcelable {
    public static final Creator<FillContext> CREATOR = new Creator<FillContext>() {
        public FillContext createFromParcel(Parcel parcel) {
            return new FillContext(parcel, null);
        }

        public FillContext[] newArray(int size) {
            return new FillContext[size];
        }
    };
    private final int mRequestId;
    private final AssistStructure mStructure;
    private ArrayMap<AutofillId, ViewNode> mViewNodeLookupTable;

    public FillContext(int requestId, AssistStructure structure) {
        this.mRequestId = requestId;
        this.mStructure = structure;
    }

    private FillContext(Parcel parcel) {
        this(parcel.readInt(), (AssistStructure) parcel.readParcelable(null));
    }

    public int getRequestId() {
        return this.mRequestId;
    }

    public AssistStructure getStructure() {
        return this.mStructure;
    }

    public String toString() {
        if (Helper.sDebug) {
            return "FillContext [reqId=" + this.mRequestId + "]";
        }
        return super.toString();
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel parcel, int flags) {
        parcel.writeInt(this.mRequestId);
        parcel.writeParcelable(this.mStructure, flags);
    }

    /* JADX WARNING: Removed duplicated region for block: B:30:0x0098 A:{LOOP_END, LOOP:4: B:28:0x0092->B:30:0x0098} */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public ViewNode[] findViewNodesByAutofillIds(AutofillId[] ids) {
        int i;
        LinkedList<ViewNode> nodesToProcess = new LinkedList();
        ViewNode[] foundNodes = new ViewNode[ids.length];
        SparseIntArray missingNodeIndexes = new SparseIntArray(ids.length);
        for (i = 0; i < ids.length; i++) {
            if (this.mViewNodeLookupTable != null) {
                int lookupTableIndex = this.mViewNodeLookupTable.indexOfKey(ids[i]);
                if (lookupTableIndex >= 0) {
                    foundNodes[i] = (ViewNode) this.mViewNodeLookupTable.valueAt(lookupTableIndex);
                } else {
                    missingNodeIndexes.put(i, 0);
                }
            } else {
                missingNodeIndexes.put(i, 0);
            }
        }
        int numWindowNodes = this.mStructure.getWindowNodeCount();
        for (i = 0; i < numWindowNodes; i++) {
            nodesToProcess.add(this.mStructure.getWindowNodeAt(i).getRootViewNode());
        }
        while (missingNodeIndexes.size() > 0 && (nodesToProcess.isEmpty() ^ 1) != 0) {
            ViewNode node = (ViewNode) nodesToProcess.removeFirst();
            i = 0;
            while (i < missingNodeIndexes.size()) {
                int index = missingNodeIndexes.keyAt(i);
                AutofillId id = ids[index];
                if (id.equals(node.getAutofillId())) {
                    foundNodes[index] = node;
                    if (this.mViewNodeLookupTable == null) {
                        this.mViewNodeLookupTable = new ArrayMap(ids.length);
                    }
                    this.mViewNodeLookupTable.put(id, node);
                    missingNodeIndexes.removeAt(i);
                    for (i = 0; i < node.getChildCount(); i++) {
                        nodesToProcess.addLast(node.getChildAt(i));
                    }
                } else {
                    i++;
                }
            }
            while (i < node.getChildCount()) {
            }
        }
        for (i = 0; i < missingNodeIndexes.size(); i++) {
            if (this.mViewNodeLookupTable == null) {
                this.mViewNodeLookupTable = new ArrayMap(missingNodeIndexes.size());
            }
            this.mViewNodeLookupTable.put(ids[missingNodeIndexes.keyAt(i)], null);
        }
        return foundNodes;
    }

    public ViewNode findViewNodeByAutofillId(AutofillId id) {
        int i;
        LinkedList<ViewNode> nodesToProcess = new LinkedList();
        int numWindowNodes = this.mStructure.getWindowNodeCount();
        for (i = 0; i < numWindowNodes; i++) {
            nodesToProcess.add(this.mStructure.getWindowNodeAt(i).getRootViewNode());
        }
        while (!nodesToProcess.isEmpty()) {
            ViewNode node = (ViewNode) nodesToProcess.removeFirst();
            if (id.equals(node.getAutofillId())) {
                return node;
            }
            for (i = 0; i < node.getChildCount(); i++) {
                nodesToProcess.addLast(node.getChildAt(i));
            }
        }
        return null;
    }
}
