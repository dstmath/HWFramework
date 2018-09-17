package android.transition;

import android.util.ArrayMap;
import android.util.LongSparseArray;
import android.util.SparseArray;
import android.view.View;

class TransitionValuesMaps {
    SparseArray<View> idValues;
    LongSparseArray<View> itemIdValues;
    ArrayMap<String, View> nameValues;
    ArrayMap<View, TransitionValues> viewValues;

    TransitionValuesMaps() {
        this.viewValues = new ArrayMap();
        this.idValues = new SparseArray();
        this.itemIdValues = new LongSparseArray();
        this.nameValues = new ArrayMap();
    }
}
