package android.rms.resource;

import android.rms.HwSysCountRes;
import android.rms.utils.Utils;
import android.util.Log;

public final class CursorResource extends HwSysCountRes {
    private static final String TAG = "RMS.CursorResource";
    private static CursorResource mCursorResource = null;

    private CursorResource() {
        super(16, TAG);
    }

    /* JADX WARNING: Missing block: B:21:0x0043, code:
            return null;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public static synchronized CursorResource getInstance() {
        synchronized (CursorResource.class) {
            if (mCursorResource == null) {
                mCursorResource = new CursorResource();
                if (Utils.DEBUG) {
                    Log.d(TAG, "getInstance create new resource");
                }
            }
            if (mCursorResource.getConfig()) {
                if (Utils.DEBUG) {
                    Log.d(TAG, "getInstance getConfig");
                }
                CursorResource cursorResource = mCursorResource;
                return cursorResource;
            } else if (Utils.DEBUG) {
                Log.d(TAG, "RMS not ready!");
            }
        }
    }

    public int acquire(int callingUid, String pkg, int processTpye, int count) {
        int strategy = 1;
        int typeID = super.getTypeId(callingUid, null, processTpye);
        if (!(this.mResourceConfig == null || !isResourceCountOverload(callingUid, pkg, typeID, count) || (isInWhiteList(parsePackageNameFromToken(pkg), 0) ^ 1) == 0)) {
            strategy = this.mResourceConfig[typeID].getResourceStrategy();
            if (Utils.DEBUG) {
                Log.d(TAG, "getOverloadStrategy CountOverload = " + strategy);
            }
            if (typeID == 2 && (Utils.DEBUG || Utils.HWFLOW)) {
                Log.i(TAG, "process uid " + callingUid + " open too many cursor " + pkg);
            }
        }
        return strategy;
    }

    protected boolean needUpdateWhiteList() {
        return false;
    }

    private String parsePackageNameFromToken(String token) {
        String pkgName = "";
        if (token != null && token.contains(";") && token.contains("-")) {
            String[] list = token.split(";");
            int n = list[0].lastIndexOf("-");
            if (n > 0) {
                pkgName = list[0].substring(0, n);
            }
        }
        if (Utils.DEBUG) {
            Log.d(TAG, "Current parsed pkgName:" + pkgName + " from token:" + token);
        }
        return pkgName;
    }
}
