package android.rms.resource;

import android.os.Bundle;
import android.rms.HwSysCountRes;
import android.rms.config.ResourceConfig;
import android.rms.control.ResourceCountControl;
import android.rms.utils.Utils;
import android.util.Log;

public final class ContentObserverResource extends HwSysCountRes {
    private static final String TAG = "RMS.ContentOberverResource";
    private static ContentObserverResource mContentObserverResource;

    private ContentObserverResource() {
        super(29, TAG);
    }

    /* JADX WARNING: Missing block: B:19:0x0036, code:
            return null;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public static synchronized ContentObserverResource getInstance() {
        synchronized (ContentObserverResource.class) {
            if (mContentObserverResource == null) {
                mContentObserverResource = new ContentObserverResource();
            }
            if (mContentObserverResource.getConfig()) {
                if (Utils.DEBUG) {
                    Log.d(TAG, "getInstance create new resource");
                }
                ContentObserverResource contentObserverResource = mContentObserverResource;
                return contentObserverResource;
            } else if (Utils.DEBUG) {
                Log.d(TAG, "RMS not ready!");
            }
        }
    }

    public int acquire(int callingUid, String pkg, int processTpye) {
        int strategy = 1;
        if (pkg == null) {
            Log.d(TAG, "pkg is null!");
            return 1;
        } else if (!needCountObserverNumber(callingUid, pkg)) {
            return 1;
        } else {
            int typeID = super.getTypeId(callingUid, pkg, processTpye);
            if (this.mResourceConfig == null || typeID >= this.mResourceConfig.length) {
                Log.e(TAG, "contentObserverconfig is null!!");
            } else if (!(!isResourceCountOverload(callingUid, pkg, typeID) || this.mResourceConfig[typeID] == null || (isInWhiteList(pkg, 0) ^ 1) == 0)) {
                strategy = this.mResourceConfig[typeID].getResourceStrategy();
                if (Utils.DEBUG) {
                    Log.d(TAG, "getOverloadStrategy strategy = " + strategy);
                }
            }
            return strategy;
        }
    }

    public void release(int callingUid, String pkg, int processTpye) {
        if (pkg == null) {
            Log.d(TAG, "pkg is null!");
        } else if (needCountObserverNumber(callingUid, pkg)) {
            long id = super.getResourceId(callingUid, pkg, super.getTypeId(callingUid, pkg, processTpye));
            if (this.mResourceCountControl != null) {
                this.mResourceCountControl.reduceCurrentCount(id);
            }
        }
    }

    public void clear(int callingUid, String pkg, int processTpye) {
        if (pkg == null) {
            Log.d(TAG, "pkg is null!");
        } else if (needCountObserverNumber(callingUid, pkg)) {
            long id = super.getResourceId(callingUid, pkg, super.getTypeId(callingUid, pkg, processTpye));
            if (this.mResourceCountControl != null) {
                this.mResourceCountControl.removeResourceCountRecord(id);
                Log.d(TAG, "clear ObserverResource in contentService");
            }
        }
    }

    private boolean needCountObserverNumber(int callingUid, String pkg) {
        if (pkg == null) {
            Log.w(TAG, "packageName is null!");
            return false;
        } else if (callingUid == 1000 || pkg.contains(":")) {
            return false;
        } else {
            return true;
        }
    }

    protected Bundle createBundleForResource(long id, int typeID, ResourceConfig config, ResourceCountControl mResourceCountControl, String pkg) {
        if (config == null || mResourceCountControl == null) {
            return null;
        }
        if (Utils.DEBUG) {
            Log.d(TAG, "createBundleForResource Call In ContentObserverResource");
        }
        Bundle bundle = new Bundle();
        bundle.putInt(Utils.BUNDLE_HARD_THRESHOLD, config.getResouceUrgentThreshold());
        bundle.putInt(Utils.BUNDLE_CURRENT_COUNT, mResourceCountControl.getTotalCount(id));
        bundle.putBoolean(Utils.BUNDLE_IS_IN_WHITELIST, isInWhiteList(pkg, 0));
        return bundle;
    }
}
