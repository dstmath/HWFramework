package ohos.aafwk.ability;

import java.util.ArrayList;
import java.util.List;
import ohos.aafwk.utils.log.Log;
import ohos.aafwk.utils.log.LogLabel;

public class AbilitySliceStack {
    private static final LogLabel LABEL = LogLabel.create();
    private List<AbilitySlice> slices = new ArrayList();

    /* access modifiers changed from: package-private */
    public synchronized boolean isEmpty() {
        return this.slices.isEmpty();
    }

    /* access modifiers changed from: package-private */
    public synchronized int size() {
        return this.slices.size();
    }

    /* access modifiers changed from: package-private */
    public synchronized AbilitySlice get(String str) {
        for (AbilitySlice abilitySlice : this.slices) {
            if (abilitySlice.getClass().getName().equals(str)) {
                return abilitySlice;
            }
        }
        return null;
    }

    /* access modifiers changed from: package-private */
    public synchronized boolean exist(AbilitySlice abilitySlice) {
        return this.slices.contains(abilitySlice);
    }

    /* access modifiers changed from: package-private */
    public synchronized boolean remove(AbilitySlice abilitySlice) {
        return this.slices.remove(abilitySlice);
    }

    /* access modifiers changed from: package-private */
    public synchronized AbilitySlice top() {
        if (isEmpty()) {
            Log.warn(LABEL, "Get top AbilitySlice failed, stack is empty.", new Object[0]);
            return null;
        }
        return this.slices.get(this.slices.size() - 1);
    }

    /* access modifiers changed from: package-private */
    public synchronized AbilitySlice pop() {
        AbilitySlice pVar = top();
        if (pVar == null) {
            return null;
        }
        this.slices.remove(pVar);
        return pVar;
    }

    /* access modifiers changed from: package-private */
    public synchronized void push(AbilitySlice abilitySlice) {
        if (abilitySlice == null) {
            Log.error(LABEL, "Push AbilitySlice failed, null object.", new Object[0]);
        } else {
            this.slices.add(abilitySlice);
        }
    }

    /* access modifiers changed from: package-private */
    public final synchronized List<AbilitySlice> getAllSlices() {
        return this.slices;
    }
}
