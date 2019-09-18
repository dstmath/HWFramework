package com.android.server.pm;

import android.util.Slog;
import java.util.Collections;
import java.util.Comparator;

/* compiled from: SELinuxMMAC */
final class PolicyComparator implements Comparator<Policy> {
    private boolean duplicateFound = false;

    PolicyComparator() {
    }

    public boolean foundDuplicate() {
        return this.duplicateFound;
    }

    public int compare(Policy p1, Policy p2) {
        int i = 1;
        if (p1.hasInnerPackages() != p2.hasInnerPackages()) {
            if (p1.hasInnerPackages()) {
                i = -1;
            }
            return i;
        }
        if (p1.getSignatures().equals(p2.getSignatures())) {
            if (p1.hasGlobalSeinfo()) {
                this.duplicateFound = true;
                Slog.e("SELinuxMMAC", "Duplicate policy entry: " + p1.toString());
            }
            if (!Collections.disjoint(p1.getInnerPackages().keySet(), p2.getInnerPackages().keySet())) {
                this.duplicateFound = true;
                Slog.e("SELinuxMMAC", "Duplicate policy entry: " + p1.toString());
            }
        }
        return 0;
    }
}
