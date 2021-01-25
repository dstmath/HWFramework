package com.android.server.pm;

import android.util.Slog;
import java.util.Collections;
import java.util.Comparator;

/* access modifiers changed from: package-private */
/* compiled from: SELinuxMMAC */
public final class PolicyComparator implements Comparator<Policy> {
    private boolean duplicateFound = false;

    PolicyComparator() {
    }

    public boolean foundDuplicate() {
        return this.duplicateFound;
    }

    public int compare(Policy p1, Policy p2) {
        if (p1.hasInnerPackages() != p2.hasInnerPackages()) {
            if (p1.hasInnerPackages()) {
                return -1;
            }
            return 1;
        } else if (!p1.getSignatures().equals(p2.getSignatures())) {
            return 0;
        } else {
            if (p1.hasGlobalSeinfo()) {
                this.duplicateFound = true;
                Slog.e("SELinuxMMAC", "Duplicate policy entry: " + p1.toString());
            }
            if (Collections.disjoint(p1.getInnerPackages().keySet(), p2.getInnerPackages().keySet())) {
                return 0;
            }
            this.duplicateFound = true;
            Slog.e("SELinuxMMAC", "Duplicate policy entry: " + p1.toString());
            return 0;
        }
    }
}
