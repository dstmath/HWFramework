package com.huawei.nearbysdk.closeRange;

import java.util.HashMap;

class CloseRangeBusinessCounter {
    private HashMap<CloseRangeBusinessType, Integer> map = new HashMap<>();

    CloseRangeBusinessCounter() {
    }

    /* access modifiers changed from: package-private */
    public void increase(CloseRangeBusinessType type) {
        if (!this.map.containsKey(type)) {
            this.map.put(type, 1);
            return;
        }
        this.map.put(type, Integer.valueOf(this.map.get(type).intValue() + 1));
    }

    /* access modifiers changed from: package-private */
    public void decrease(CloseRangeBusinessType type) {
        if (this.map.containsKey(type)) {
            int val = this.map.get(type).intValue();
            if (val == 1) {
                this.map.remove(type);
            } else {
                this.map.put(type, Integer.valueOf(val - 1));
            }
        }
    }

    /* access modifiers changed from: package-private */
    public boolean containsType(CloseRangeBusinessType type) {
        return this.map.containsKey(type);
    }
}
