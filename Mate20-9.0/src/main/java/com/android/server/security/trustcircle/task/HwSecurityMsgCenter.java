package com.android.server.security.trustcircle.task;

import com.android.server.security.trustcircle.task.HwSecurityTaskBase;
import com.android.server.security.trustcircle.utils.LogHelper;
import java.util.HashMap;

public class HwSecurityMsgCenter {
    public static final String TAG = HwSecurityMsgCenter.class.getSimpleName();
    private static HwSecurityMsgCenter gInstance = null;
    private static Object mInstanceLock = new Object();
    private HashMap<Integer, HashMap<HwSecurityTaskBase, EventRegInfo>> mEvMaps = new HashMap<>();
    private Object mLock = new Object();

    private static class EventRegInfo {
        public boolean mEnable;
        public HwSecurityTaskBase.EventListener mListener;

        public EventRegInfo(HwSecurityTaskBase.EventListener listener, boolean enable) {
            this.mListener = listener;
            this.mEnable = enable;
        }
    }

    /* access modifiers changed from: protected */
    public HashMap<HwSecurityTaskBase, EventRegInfo> getEventMap(int evId, boolean createWhileEmpty) {
        if (this.mEvMaps == null) {
            LogHelper.e(TAG, "mEvMaps is empty!!!");
            return null;
        } else if (this.mEvMaps.containsKey(Integer.valueOf(evId))) {
            return this.mEvMaps.get(Integer.valueOf(evId));
        } else {
            if (!createWhileEmpty) {
                return null;
            }
            this.mEvMaps.put(Integer.valueOf(evId), new HashMap());
            return this.mEvMaps.get(Integer.valueOf(evId));
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:26:0x003b, code lost:
        return false;
     */
    public boolean registerEvent(int evId, HwSecurityTaskBase task, HwSecurityTaskBase.EventListener evListener) {
        synchronized (this.mLock) {
            if (task != null && evListener != null) {
                HashMap<HwSecurityTaskBase, EventRegInfo> maps = getEventMap(evId, true);
                if (maps == null) {
                    return false;
                }
                if (maps.containsKey(task)) {
                    return false;
                }
                HwSecurityTaskBase parent = task.getParent();
                if (parent != null && maps.containsKey(parent)) {
                    EventRegInfo parentInfo = maps.get(parent);
                    if (parentInfo != null) {
                        parentInfo.mEnable = false;
                    }
                }
                maps.put(task, new EventRegInfo(evListener, true));
                return true;
            }
        }
    }

    public void unregisterEvent(int evId, HwSecurityTaskBase task) {
        synchronized (this.mLock) {
            HashMap<HwSecurityTaskBase, EventRegInfo> maps = getEventMap(evId, false);
            if (maps != null) {
                HwSecurityTaskBase parent = task.getParent();
                if (parent != null && maps.containsKey(parent)) {
                    EventRegInfo parentInfo = maps.get(parent);
                    if (parentInfo != null) {
                        parentInfo.mEnable = true;
                    }
                }
                maps.remove(task);
            }
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:12:0x004d, code lost:
        r2 = r0.keySet().iterator();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:14:0x0059, code lost:
        if (r2.hasNext() == false) goto L_0x007d;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:15:0x005b, code lost:
        r3 = r2.next();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:16:0x0061, code lost:
        if (r3 == null) goto L_0x0055;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:17:0x0063, code lost:
        r4 = r0.get(r3);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:18:0x0069, code lost:
        if (r4 == null) goto L_0x0055;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:20:0x006d, code lost:
        if (r4.mEnable == false) goto L_0x0055;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:22:0x0071, code lost:
        if (r4.mListener == null) goto L_0x0055;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:24:0x0079, code lost:
        if (r4.mListener.onEvent(r8) == false) goto L_0x0055;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:25:0x007d, code lost:
        return;
     */
    public void processEvent(HwSecurityEvent ev) {
        HashMap<HwSecurityTaskBase, EventRegInfo> copyMaps = new HashMap<>();
        synchronized (this.mLock) {
            int evId = ev.getEvID();
            String str = TAG;
            LogHelper.i(str, "processEvent: " + evId);
            HashMap<HwSecurityTaskBase, EventRegInfo> maps = getEventMap(evId, false);
            if (maps != null) {
                for (HwSecurityTaskBase key : maps.keySet()) {
                    copyMaps.put(key, maps.get(key));
                }
            }
        }
    }

    public static boolean staticRegisterEvent(int evId, HwSecurityTaskBase task, HwSecurityTaskBase.EventListener evListener) {
        HwSecurityMsgCenter gMsgCenter = getInstance();
        if (gMsgCenter != null) {
            return gMsgCenter.registerEvent(evId, task, evListener);
        }
        return false;
    }

    public static boolean staticUnregisterEvent(int evId, HwSecurityTaskBase task) {
        HwSecurityMsgCenter gMsgCenter = getInstance();
        if (gMsgCenter == null) {
            return false;
        }
        gMsgCenter.unregisterEvent(evId, task);
        return true;
    }

    public static void createInstance() {
        synchronized (mInstanceLock) {
            if (gInstance == null) {
                gInstance = new HwSecurityMsgCenter();
            }
        }
    }

    public static HwSecurityMsgCenter getInstance() {
        HwSecurityMsgCenter hwSecurityMsgCenter;
        synchronized (mInstanceLock) {
            hwSecurityMsgCenter = gInstance;
        }
        return hwSecurityMsgCenter;
    }

    public static void destroyInstance() {
        synchronized (mInstanceLock) {
            if (gInstance != null) {
                gInstance = null;
            }
        }
    }
}
