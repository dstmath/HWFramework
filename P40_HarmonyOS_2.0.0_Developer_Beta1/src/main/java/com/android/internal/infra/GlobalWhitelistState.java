package com.android.internal.infra;

import android.content.ComponentName;
import android.util.ArraySet;
import android.util.SparseArray;
import com.android.internal.annotations.GuardedBy;
import java.io.PrintWriter;
import java.util.List;

public class GlobalWhitelistState {
    protected final Object mGlobalWhitelistStateLock = new Object();
    @GuardedBy({"mGlobalWhitelistStateLock"})
    protected SparseArray<WhitelistHelper> mWhitelisterHelpers;

    public void setWhitelist(int userId, List<String> packageNames, List<ComponentName> components) {
        synchronized (this.mGlobalWhitelistStateLock) {
            if (this.mWhitelisterHelpers == null) {
                this.mWhitelisterHelpers = new SparseArray<>(1);
            }
            WhitelistHelper helper = this.mWhitelisterHelpers.get(userId);
            if (helper == null) {
                helper = new WhitelistHelper();
                this.mWhitelisterHelpers.put(userId, helper);
            }
            helper.setWhitelist(packageNames, components);
        }
    }

    public boolean isWhitelisted(int userId, String packageName) {
        synchronized (this.mGlobalWhitelistStateLock) {
            boolean z = false;
            if (this.mWhitelisterHelpers == null) {
                return false;
            }
            WhitelistHelper helper = this.mWhitelisterHelpers.get(userId);
            if (helper != null) {
                z = helper.isWhitelisted(packageName);
            }
            return z;
        }
    }

    public boolean isWhitelisted(int userId, ComponentName componentName) {
        synchronized (this.mGlobalWhitelistStateLock) {
            boolean z = false;
            if (this.mWhitelisterHelpers == null) {
                return false;
            }
            WhitelistHelper helper = this.mWhitelisterHelpers.get(userId);
            if (helper != null) {
                z = helper.isWhitelisted(componentName);
            }
            return z;
        }
    }

    public ArraySet<ComponentName> getWhitelistedComponents(int userId, String packageName) {
        synchronized (this.mGlobalWhitelistStateLock) {
            ArraySet<ComponentName> arraySet = null;
            if (this.mWhitelisterHelpers == null) {
                return null;
            }
            WhitelistHelper helper = this.mWhitelisterHelpers.get(userId);
            if (helper != null) {
                arraySet = helper.getWhitelistedComponents(packageName);
            }
            return arraySet;
        }
    }

    public void resetWhitelist(int userId) {
        synchronized (this.mGlobalWhitelistStateLock) {
            if (this.mWhitelisterHelpers != null) {
                this.mWhitelisterHelpers.remove(userId);
                if (this.mWhitelisterHelpers.size() == 0) {
                    this.mWhitelisterHelpers = null;
                }
            }
        }
    }

    public void dump(String prefix, PrintWriter pw) {
        pw.print(prefix);
        pw.print("State: ");
        synchronized (this.mGlobalWhitelistStateLock) {
            if (this.mWhitelisterHelpers == null) {
                pw.println("empty");
                return;
            }
            pw.print(this.mWhitelisterHelpers.size());
            pw.println(" services");
            String prefix2 = prefix + "  ";
            for (int i = 0; i < this.mWhitelisterHelpers.size(); i++) {
                this.mWhitelisterHelpers.valueAt(i).dump(prefix2, "Whitelist for userId " + this.mWhitelisterHelpers.keyAt(i), pw);
            }
        }
    }
}
