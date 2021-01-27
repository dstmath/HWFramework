package com.android.server.wm;

import android.graphics.Rect;
import android.graphics.Region;
import android.util.SparseArray;

/* access modifiers changed from: package-private */
public class TapExcludeRegionHolder {
    private SparseArray<Region> mTapExcludeRegions = new SparseArray<>();

    TapExcludeRegionHolder() {
    }

    /* access modifiers changed from: package-private */
    public void updateRegion(int regionId, Region region) {
        this.mTapExcludeRegions.remove(regionId);
        if (region != null && !region.isEmpty()) {
            this.mTapExcludeRegions.put(regionId, new Region(region));
        }
    }

    /* access modifiers changed from: package-private */
    public void amendRegion(Region region, Rect bounds) {
        for (int i = this.mTapExcludeRegions.size() - 1; i >= 0; i--) {
            Region r = this.mTapExcludeRegions.valueAt(i);
            if (bounds != null) {
                r.op(bounds, Region.Op.INTERSECT);
            }
            region.op(r, Region.Op.UNION);
        }
    }
}
