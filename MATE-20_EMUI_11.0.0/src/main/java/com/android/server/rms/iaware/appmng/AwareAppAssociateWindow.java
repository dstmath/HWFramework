package com.android.server.rms.iaware.appmng;

import android.app.AppOpsManager;
import android.os.Bundle;
import android.rms.iaware.AwareLog;
import android.util.ArrayMap;
import android.util.SparseArray;
import com.android.server.mtm.MultiTaskManagerService;
import com.android.server.mtm.iaware.appmng.AwareProcessWindowInfo;
import com.android.server.mtm.utils.SparseSet;
import com.android.server.rms.iaware.appmng.AwareAppAssociateUtils;
import com.android.server.rms.iaware.memory.utils.MemoryConstant;
import com.android.server.rms.iaware.qos.AwareBinderSchedManager;
import com.huawei.android.app.AppOpsManagerExt;
import com.huawei.android.os.UserHandleEx;
import com.huawei.android.view.HwWindowManager;
import java.util.List;
import java.util.Map;

public class AwareAppAssociateWindow {
    private static final int INVALID_VALUE = -1;
    private static final String TAG = "RMS.AwareAppAssociate";
    private static final int VISIBLE_WINDOWS_ADD_WINDOW = 4;
    private static final int VISIBLE_WINDOWS_CACHE_CHANGE_MODE = 3;
    private static final int VISIBLE_WINDOWS_CACHE_CLR = 2;
    private static final int VISIBLE_WINDOWS_CACHE_DEL = 1;
    private static final int VISIBLE_WINDOWS_CACHE_UPDATE = 0;
    private static final int VISIBLE_WINDOWS_REMOVE_WINDOW = 5;
    private boolean mIsScreenOff = false;
    private final SparseSet mVisWinDurScreenOff = new SparseSet();
    private final ArrayMap<Integer, AwareProcessWindowInfo> mVisibleWindows = new ArrayMap<>();
    private final ArrayMap<Integer, AwareProcessWindowInfo> mVisibleWindowsCache = new ArrayMap<>();

    protected static boolean checkAndUpdateWindowInfo(AwareProcessWindowInfo winInfo, int width, int height, float alpha, boolean hasSurface) {
        if (winInfo == null) {
            return false;
        }
        if (width == -1 && height == -1) {
            width = winInfo.width;
            height = winInfo.height;
        }
        winInfo.width = width;
        winInfo.height = height;
        boolean isMiniWindow = width <= AwareProcessWindowInfo.getMinWindowWidth() || height <= AwareProcessWindowInfo.getMinWindowHeight();
        boolean isInvisible = alpha == 0.0f || (!hasSurface && AwareIntelligentRecg.getInstance().isRecogOptEnable());
        if (isMiniWindow || isInvisible) {
            return true;
        }
        return false;
    }

    public void getVisibleWindowsInRestriction(SparseSet windowPids) {
        synchronized (this.mVisibleWindows) {
            for (Map.Entry<Integer, AwareProcessWindowInfo> window : this.mVisibleWindows.entrySet()) {
                AwareProcessWindowInfo winInfo = window.getValue();
                if (winInfo.inRestriction && !winInfo.isEvil()) {
                    windowPids.add(window.getKey().intValue());
                }
            }
        }
        if (AwareAppAssociate.isDebugEnabled()) {
            AwareLog.d(TAG, "WindowPids in restriction:" + windowPids);
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:21:0x0075  */
    /* JADX WARNING: Removed duplicated region for block: B:42:0x0010 A[SYNTHETIC] */
    public void getVisibleWindows(SparseSet windowPids, SparseSet evilPids) {
        boolean allowedWindow;
        if (windowPids != null) {
            synchronized (this.mVisibleWindows) {
                for (Map.Entry<Integer, AwareProcessWindowInfo> window : this.mVisibleWindows.entrySet()) {
                    AwareProcessWindowInfo winInfo = window.getValue();
                    if (winInfo.mode != 0) {
                        if (winInfo.mode != 3) {
                            allowedWindow = false;
                            AwareLog.i(TAG, "[getVisibleWindows]:" + window.getKey() + " [allowedWindow]:" + allowedWindow + " isEvil:" + winInfo.isEvil());
                            if (!allowedWindow && !winInfo.isEvil()) {
                                windowPids.add(window.getKey().intValue());
                            } else if (evilPids == null) {
                                evilPids.add(window.getKey().intValue());
                            }
                        }
                    }
                    allowedWindow = true;
                    AwareLog.i(TAG, "[getVisibleWindows]:" + window.getKey() + " [allowedWindow]:" + allowedWindow + " isEvil:" + winInfo.isEvil());
                    if (!allowedWindow) {
                    }
                    if (evilPids == null) {
                    }
                }
            }
            synchronized (this.mVisWinDurScreenOff) {
                if (!this.mVisWinDurScreenOff.isEmpty()) {
                    windowPids.addAll(this.mVisWinDurScreenOff);
                }
            }
            if (AwareAppAssociate.isDebugEnabled()) {
                AwareLog.d(TAG, "WindowPids:" + windowPids + ", evilPids:" + evilPids);
            }
        }
    }

    private boolean isAllowedAlertWindowOps(AwareProcessWindowInfo winInfo) {
        return winInfo.mode == 0 || winInfo.mode == 3;
    }

    /* access modifiers changed from: protected */
    public boolean isVisibleWindows(int userId, String pkg) {
        if (pkg == null) {
            return true;
        }
        synchronized (this.mVisibleWindows) {
            for (Map.Entry<Integer, AwareProcessWindowInfo> window : this.mVisibleWindows.entrySet()) {
                AwareProcessWindowInfo winInfo = window.getValue();
                boolean allowedWindow = isAllowedAlertWindowOps(winInfo);
                AwareLog.i(TAG, "[isVisibleWindows]:" + window.getKey() + " pkg:" + pkg + " [allowedWindow]:" + allowedWindow + " isEvil:" + winInfo.isEvil());
                if (pkg.equals(winInfo.pkg) && ((userId == -1 || userId == UserHandleEx.getUserId(winInfo.uid)) && allowedWindow && !winInfo.isEvil())) {
                    return true;
                }
            }
            return false;
        }
    }

    /* access modifiers changed from: protected */
    public boolean hasWindow(int uid) {
        synchronized (this.mVisibleWindows) {
            for (Map.Entry<Integer, AwareProcessWindowInfo> window : this.mVisibleWindows.entrySet()) {
                if (uid == window.getValue().uid) {
                    return true;
                }
            }
            return false;
        }
    }

    /* access modifiers changed from: protected */
    public boolean isEvilAlertWindow(int window, int code) {
        boolean result;
        synchronized (this.mVisibleWindows) {
            AwareProcessWindowInfo winInfo = this.mVisibleWindows.get(Integer.valueOf(window));
            if (winInfo == null || (isAllowedAlertWindowOps(winInfo) && !winInfo.isEvil(code))) {
                result = false;
            } else {
                result = true;
            }
        }
        return result;
    }

    private void updateVisibleWindowsCache(AwareProcessWindowInfo awareProcessWindowInfo, int type, int pid, int code, boolean evil) {
        synchronized (this.mVisibleWindowsCache) {
            updateVisibleWindowsCacheLocked(awareProcessWindowInfo, type, pid, code, evil);
        }
    }

    /* access modifiers changed from: protected */
    public void initVisibleWindows() {
        List<Bundle> windowsList;
        int code;
        String pkg;
        AwareProcessWindowInfo winInfo;
        float alpha;
        List<Bundle> windowsList2 = HwWindowManager.getVisibleWindows(24);
        if (windowsList2 == null) {
            AwareLog.w(TAG, "Catch null when initVisibleWindows.");
            return;
        }
        synchronized (this.mVisibleWindows) {
            try {
                this.mVisibleWindows.clear();
                AwareProcessWindowInfo windowInfo = new AwareProcessWindowInfo(-1, null, -1);
                updateVisibleWindowsCache(windowInfo, 2, -1, -1, false);
                for (Bundle windowState : windowsList2) {
                    if (windowState != null) {
                        int window = windowState.getInt("window_pid");
                        int mode = windowState.getInt("window_value");
                        int code2 = windowState.getInt("window_state");
                        int width = windowState.getInt("window_width");
                        int height = windowState.getInt("window_height");
                        boolean hasSurface = windowState.getBoolean("hasSurface");
                        float alpha2 = windowState.getFloat("window_alpha");
                        String pkg2 = windowState.getString("window_package");
                        int uid = windowState.getInt("window_uid");
                        if (AwareAppAssociate.isDebugEnabled()) {
                            StringBuilder sb = new StringBuilder();
                            windowsList = windowsList2;
                            sb.append("initVisibleWindows pid:");
                            sb.append(window);
                            sb.append(" mode:");
                            sb.append(mode);
                            sb.append(" code:");
                            sb.append(code2);
                            sb.append(" width:");
                            sb.append(width);
                            sb.append(" height:");
                            sb.append(height);
                            AwareLog.i(TAG, sb.toString());
                        } else {
                            windowsList = windowsList2;
                        }
                        AwareProcessWindowInfo winInfo2 = this.mVisibleWindows.get(Integer.valueOf(window));
                        if (winInfo2 == null) {
                            pkg = pkg2;
                            code = code2;
                            alpha = alpha2;
                            winInfo = new AwareProcessWindowInfo(mode, pkg, uid, width, height);
                        } else {
                            code = code2;
                            pkg = pkg2;
                            alpha = alpha2;
                            winInfo = winInfo2;
                        }
                        boolean isEvil = checkAndUpdateWindowInfo(winInfo, width, height, alpha, hasSurface);
                        this.mVisibleWindows.put(Integer.valueOf(window), winInfo);
                        updateVisibleWindowsCache(new AwareProcessWindowInfo(mode, pkg, uid), 0, window, -1, false);
                        if (!isEvil) {
                            AwareAppAssociate.getInstance().notifyVisibleWindowsChange(2, window, mode);
                        }
                        winInfo.addWindow(Integer.valueOf(code), isEvil);
                        windowInfo = new AwareProcessWindowInfo(-1, null, -1);
                        updateVisibleWindowsCache(windowInfo, 4, window, code, isEvil);
                        windowsList2 = windowsList;
                    }
                }
            } catch (Throwable th) {
                th = th;
                throw th;
            }
        }
    }

    /* access modifiers changed from: protected */
    public void deinitVisibleWindows() {
        synchronized (this.mVisibleWindows) {
            this.mVisibleWindows.clear();
            AwareAppAssociate.getInstance().notifyVisibleWindowsChange(0, -1, -1);
            updateVisibleWindowsCache(new AwareProcessWindowInfo(-1, null, -1), 2, -1, -1, false);
        }
    }

    private void updateVisibleWindowsCacheLocked(AwareProcessWindowInfo awareProcessWindowInfo, int type, int pid, int code, boolean evil) {
        AwareProcessWindowInfo winInfo;
        if (type == 0) {
            this.mVisibleWindowsCache.put(Integer.valueOf(pid), awareProcessWindowInfo);
        } else if (type == 1) {
            this.mVisibleWindowsCache.remove(Integer.valueOf(pid));
        } else if (type == 2) {
            this.mVisibleWindowsCache.clear();
        } else if (type == 3) {
            AwareProcessWindowInfo winInfo2 = this.mVisibleWindowsCache.get(Integer.valueOf(pid));
            if (winInfo2 != null) {
                winInfo2.mode = awareProcessWindowInfo.mode;
            }
        } else if (type == 4) {
            AwareProcessWindowInfo winInfo3 = this.mVisibleWindowsCache.get(Integer.valueOf(pid));
            if (winInfo3 != null) {
                winInfo3.addWindow(Integer.valueOf(code), evil);
            }
        } else if (type == 5 && (winInfo = this.mVisibleWindowsCache.get(Integer.valueOf(pid))) != null) {
            winInfo.removeWindow(Integer.valueOf(code));
        }
    }

    /* JADX INFO: Multiple debug info for r0v21 'isEvil'  boolean: [D('isEvil' boolean), D('winInfo' com.android.server.mtm.iaware.appmng.AwareProcessWindowInfo)] */
    /* access modifiers changed from: protected */
    public void addWindow(Bundle bundleArgs) {
        ArrayMap<Integer, AwareProcessWindowInfo> arrayMap;
        Throwable th;
        AwareProcessWindowInfo winInfo;
        boolean isEvil;
        if (bundleArgs != null) {
            int window = bundleArgs.getInt("window");
            int mode = bundleArgs.getInt("windowmode");
            int code = bundleArgs.getInt("hashcode");
            int width = bundleArgs.getInt("width");
            int height = bundleArgs.getInt("height");
            float alpha = bundleArgs.getFloat("alpha");
            int uid = bundleArgs.getInt("uid");
            AwareLog.i(TAG, "[addWindow]:" + window + " [mode]:" + mode + " [code]:" + code + " width:" + width + " height:" + height + " alpha:" + alpha);
            if (window > 0) {
                String pkg = bundleArgs.getString(MemoryConstant.MEM_PREREAD_ITEM_NAME);
                boolean isEvil2 = false;
                AwareBinderSchedManager.getInstance().setProcessQos(uid, window, true, 0);
                ArrayMap<Integer, AwareProcessWindowInfo> arrayMap2 = this.mVisibleWindows;
                synchronized (arrayMap2) {
                    try {
                        AwareProcessWindowInfo winInfo2 = this.mVisibleWindows.get(Integer.valueOf(window));
                        if ((width <= AwareProcessWindowInfo.getMinWindowWidth() && width > 0) || ((height <= AwareProcessWindowInfo.getMinWindowHeight() && height > 0) || alpha == 0.0f)) {
                            isEvil2 = true;
                        }
                        if (winInfo2 == null) {
                            isEvil = isEvil2;
                            arrayMap = arrayMap2;
                            try {
                                AwareProcessWindowInfo winInfo3 = new AwareProcessWindowInfo(mode, pkg, uid, width, height);
                                this.mVisibleWindows.put(Integer.valueOf(window), winInfo3);
                                updateVisibleWindowsCache(new AwareProcessWindowInfo(mode, pkg, uid), 0, window, -1, false);
                                if (!isEvil) {
                                    AwareAppAssociate.getInstance().notifyVisibleWindowsChange(2, window, mode);
                                }
                                winInfo = winInfo3;
                            } catch (Throwable th2) {
                                th = th2;
                                throw th;
                            }
                        } else {
                            isEvil = isEvil2;
                            arrayMap = arrayMap2;
                            winInfo = winInfo2;
                        }
                        winInfo.addWindow(Integer.valueOf(code), isEvil);
                        updateVisibleWindowsCache(new AwareProcessWindowInfo(-1, null, -1), 4, window, code, isEvil);
                        AwareLog.i(TAG, "[addWindow]:" + window + " [mode]:" + mode + " [code]:" + code + " isEvil:" + isEvil);
                        if (AwareAppAssociate.isDebugEnabled()) {
                            AwareLog.i(TAG, "[addVisibleWindows]:" + window + " [mode]:" + mode + " [code]:" + code);
                        }
                    } catch (Throwable th3) {
                        th = th3;
                        arrayMap = arrayMap2;
                        throw th;
                    }
                }
            }
        }
    }

    /* access modifiers changed from: protected */
    public void updateWindow(Bundle bundleArgs) {
        ArrayMap<Integer, AwareProcessWindowInfo> arrayMap;
        Throwable th;
        boolean isEvil;
        if (bundleArgs != null) {
            int window = bundleArgs.getInt("window");
            int mode = bundleArgs.getInt("windowmode");
            int code = bundleArgs.getInt("hashcode");
            int width = bundleArgs.getInt("width");
            int height = bundleArgs.getInt("height");
            float alpha = bundleArgs.getFloat("alpha");
            boolean hasSurface = bundleArgs.getBoolean("hasSurface");
            AwareLog.i(TAG, "[updateWindow]:" + window + " [mode]:" + mode + " [code]:" + code + " width:" + width + " height:" + height + " alpha:" + alpha + " hasSurface:" + hasSurface);
            if (window > 0) {
                ArrayMap<Integer, AwareProcessWindowInfo> arrayMap2 = this.mVisibleWindows;
                synchronized (arrayMap2) {
                    try {
                        AwareProcessWindowInfo winInfo = this.mVisibleWindows.get(Integer.valueOf(window));
                        if (winInfo == null) {
                            AwareLog.i(TAG, "[updateWindow]: do not update before add ?" + window + " [mode]:" + mode + " [code]:" + code);
                        } else {
                            boolean isEvil2 = checkAndUpdateWindowInfo(winInfo, width, height, alpha, hasSurface);
                            if (winInfo.containsWindow(code)) {
                                winInfo.addWindow(Integer.valueOf(code), isEvil2);
                                isEvil = isEvil2;
                                arrayMap = arrayMap2;
                                try {
                                    updateVisibleWindowsCache(new AwareProcessWindowInfo(-1, null, -1), 4, window, code, isEvil);
                                } catch (Throwable th2) {
                                    th = th2;
                                    throw th;
                                }
                            } else {
                                isEvil = isEvil2;
                                arrayMap = arrayMap2;
                            }
                            AwareLog.i(TAG, "[updateWindow]:" + window + " [mode]:" + mode + " [code]:" + code + " isEvil:" + isEvil);
                            if (AwareAppAssociate.isDebugEnabled()) {
                                AwareLog.i(TAG, "[updateWindow]:" + window + " [mode]:" + mode + " [code]:" + code);
                            }
                        }
                    } catch (Throwable th3) {
                        th = th3;
                        arrayMap = arrayMap2;
                        throw th;
                    }
                }
            }
        }
    }

    /* access modifiers changed from: protected */
    /* JADX WARNING: Code restructure failed: missing block: B:40:0x00c9, code lost:
        r0 = th;
     */
    public void removeWindow(int window, int code, int uid) {
        boolean isEvil;
        if (window > 0) {
            boolean removed = false;
            AwareBinderSchedManager.getInstance().setProcessQos(uid, window, false, 0);
            synchronized (this.mVisibleWindows) {
                AwareProcessWindowInfo winInfo = this.mVisibleWindows.get(Integer.valueOf(window));
                if (winInfo == null) {
                    this.mVisibleWindows.remove(Integer.valueOf(window));
                    updateVisibleWindowsCache(new AwareProcessWindowInfo(-1, null, -1), 1, window, -1, false);
                    return;
                }
                isEvil = winInfo.isEvil();
                winInfo.removeWindow(Integer.valueOf(code));
                updateVisibleWindowsCache(new AwareProcessWindowInfo(-1, null, -1), 5, window, code, false);
                if (winInfo.windows.size() == 0) {
                    this.mVisibleWindows.remove(Integer.valueOf(window));
                    updateVisibleWindowsCache(new AwareProcessWindowInfo(-1, null, -1), 1, window, -1, false);
                    if (!isEvil) {
                        AwareAppAssociate.getInstance().notifyVisibleWindowsChange(1, window, -1);
                    }
                    removed = true;
                }
            }
            if (removed && this.mIsScreenOff && !isEvil) {
                synchronized (this.mVisWinDurScreenOff) {
                    this.mVisWinDurScreenOff.add(window);
                }
            }
            if (AwareAppAssociate.isDebugEnabled()) {
                AwareLog.d(TAG, "[removeVisibleWindows]:" + window + " [code]:" + code);
                return;
            }
            return;
        }
        return;
        while (true) {
        }
    }

    /* access modifiers changed from: protected */
    public void updateWindowOpsList(MultiTaskManagerService mtmService) {
        synchronized (this.mVisibleWindows) {
            for (Map.Entry<Integer, AwareProcessWindowInfo> window : this.mVisibleWindows.entrySet()) {
                AwareProcessWindowInfo winInfo = window.getValue();
                Object obj = mtmService.context().getSystemService("appops");
                int mode = 0;
                if (obj instanceof AppOpsManager) {
                    mode = AppOpsManagerExt.checkOpNoThrow((AppOpsManager) obj, 24, winInfo.uid, winInfo.pkg);
                }
                winInfo.inRestriction = AwareAppAssociateUtils.isInRestriction(winInfo.mode, mode);
                winInfo.mode = mode;
                updateVisibleWindowsCache(new AwareProcessWindowInfo(mode, null, -1), 3, window.getKey().intValue(), -1, false);
            }
        }
    }

    /* access modifiers changed from: protected */
    public void updateWindowOps(String pkgName, MultiTaskManagerService mtmService, SparseArray<AwareAppAssociateUtils.AssocBaseRecord> procPidMap) {
        synchronized (this.mVisibleWindows) {
            updateWindowOpsLocked(pkgName, mtmService, procPidMap);
        }
    }

    private void updateWindowOpsLocked(String pkgName, MultiTaskManagerService mtmService, SparseArray<AwareAppAssociateUtils.AssocBaseRecord> procPidMap) {
        for (Map.Entry<Integer, AwareProcessWindowInfo> window : this.mVisibleWindows.entrySet()) {
            int pid = window.getKey().intValue();
            AwareProcessWindowInfo winInfo = window.getValue();
            AwareAppAssociateUtils.AssocBaseRecord record = procPidMap.get(pid);
            if (!(record == null || record.pkgList == null || winInfo == null || !record.pkgList.contains(pkgName))) {
                Object obj = mtmService.context().getSystemService("appops");
                int mode = obj instanceof AppOpsManager ? AppOpsManagerExt.checkOpNoThrow((AppOpsManager) obj, 24, record.uid, pkgName) : 0;
                winInfo.mode = mode;
                updateVisibleWindowsCache(new AwareProcessWindowInfo(mode, null, -1), 3, pid, -1, false);
                if (!winInfo.isEvil()) {
                    AwareAppAssociate.getInstance().notifyVisibleWindowsChange(2, pid, mode);
                    return;
                }
                return;
            }
        }
    }

    public void screenStateChange(boolean screenOff) {
        this.mIsScreenOff = screenOff;
    }

    /* access modifiers changed from: protected */
    public void clearRemoveVisWinDurScreenOff() {
        synchronized (this.mVisWinDurScreenOff) {
            if (!this.mVisWinDurScreenOff.isEmpty()) {
                this.mVisWinDurScreenOff.clear();
            }
        }
    }

    /* access modifiers changed from: protected */
    public boolean isVisibleWindow(int pid) {
        boolean allowedWindow;
        synchronized (this.mVisibleWindowsCache) {
            AwareProcessWindowInfo winInfo = this.mVisibleWindowsCache.get(Integer.valueOf(pid));
            if (winInfo != null) {
                if (winInfo.mode != 0) {
                    if (winInfo.mode != 3) {
                        allowedWindow = false;
                        if (allowedWindow && !winInfo.isEvil()) {
                            return true;
                        }
                    }
                }
                allowedWindow = true;
                return true;
            }
            return false;
        }
    }

    /* access modifiers changed from: protected */
    public void report(int eventId, Bundle bundleArgs) {
        if (bundleArgs != null) {
            if (eventId == 8) {
                addWindow(bundleArgs);
            } else if (eventId == 9) {
                removeWindow(bundleArgs.getInt("window"), bundleArgs.getInt("hashcode"), bundleArgs.getInt("uid"));
            } else if (eventId == 27) {
                updateWindow(bundleArgs);
            }
        }
    }
}
