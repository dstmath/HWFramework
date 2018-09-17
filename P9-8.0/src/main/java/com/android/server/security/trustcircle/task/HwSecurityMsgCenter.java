package com.android.server.security.trustcircle.task;

import com.android.server.security.trustcircle.task.HwSecurityTaskBase.EventListener;
import com.android.server.security.trustcircle.utils.LogHelper;
import java.util.HashMap;

public class HwSecurityMsgCenter {
    public static final String TAG = HwSecurityMsgCenter.class.getSimpleName();
    private static HwSecurityMsgCenter gInstance = null;
    private static Object mInstanceLock = new Object();
    private HashMap<Integer, HashMap<HwSecurityTaskBase, EventRegInfo>> mEvMaps = new HashMap();
    private Object mLock = new Object();

    private static class EventRegInfo {
        public boolean mEnable;
        public EventListener mListener;

        public EventRegInfo(EventListener listener, boolean enable) {
            this.mListener = listener;
            this.mEnable = enable;
        }
    }

    protected HashMap<HwSecurityTaskBase, EventRegInfo> getEventMap(int evId, boolean createWhileEmpty) {
        if (this.mEvMaps == null) {
            LogHelper.e(TAG, "mEvMaps is empty!!!");
            return null;
        } else if (this.mEvMaps.containsKey(Integer.valueOf(evId))) {
            return (HashMap) this.mEvMaps.get(Integer.valueOf(evId));
        } else {
            if (!createWhileEmpty) {
                return null;
            }
            this.mEvMaps.put(Integer.valueOf(evId), new HashMap());
            return (HashMap) this.mEvMaps.get(Integer.valueOf(evId));
        }
    }

    /* JADX WARNING: Missing block: B:5:0x000a, code:
            return false;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public boolean registerEvent(int evId, HwSecurityTaskBase task, EventListener evListener) {
        synchronized (this.mLock) {
            if (task == null || evListener == null) {
            } else {
                HashMap<HwSecurityTaskBase, EventRegInfo> maps = getEventMap(evId, true);
                if (maps == null) {
                    return false;
                } else if (maps.containsKey(task)) {
                    return false;
                } else {
                    HwSecurityTaskBase parent = task.getParent();
                    if (parent != null && maps.containsKey(parent)) {
                        EventRegInfo parentInfo = (EventRegInfo) maps.get(parent);
                        if (parentInfo != null) {
                            parentInfo.mEnable = false;
                        }
                    }
                    maps.put(task, new EventRegInfo(evListener, true));
                    return true;
                }
            }
        }
    }

    public void unregisterEvent(int evId, HwSecurityTaskBase task) {
        synchronized (this.mLock) {
            HashMap<HwSecurityTaskBase, EventRegInfo> maps = getEventMap(evId, false);
            if (maps == null) {
                return;
            }
            HwSecurityTaskBase parent = task.getParent();
            if (parent != null && maps.containsKey(parent)) {
                EventRegInfo parentInfo = (EventRegInfo) maps.get(parent);
                if (parentInfo != null) {
                    parentInfo.mEnable = true;
                }
            }
            maps.remove(task);
        }
    }

    /* JADX WARNING: Missing block: B:17:0x0051, code:
            r7 = r0.keySet().iterator();
     */
    /* JADX WARNING: Missing block: B:19:0x005d, code:
            if (r7.hasNext() == false) goto L_0x007f;
     */
    /* JADX WARNING: Missing block: B:20:0x005f, code:
            r6 = (com.android.server.security.trustcircle.task.HwSecurityTaskBase) r7.next();
     */
    /* JADX WARNING: Missing block: B:21:0x0065, code:
            if (r6 == null) goto L_0x0059;
     */
    /* JADX WARNING: Missing block: B:22:0x0067, code:
            r5 = (com.android.server.security.trustcircle.task.HwSecurityMsgCenter.EventRegInfo) r0.get(r6);
     */
    /* JADX WARNING: Missing block: B:23:0x006d, code:
            if (r5 == null) goto L_0x0059;
     */
    /* JADX WARNING: Missing block: B:25:0x0071, code:
            if (r5.mEnable == false) goto L_0x0059;
     */
    /* JADX WARNING: Missing block: B:27:0x0075, code:
            if (r5.mListener == null) goto L_0x0059;
     */
    /* JADX WARNING: Missing block: B:29:0x007d, code:
            if (r5.mListener.onEvent(r13) == false) goto L_0x0059;
     */
    /* JADX WARNING: Missing block: B:30:0x007f, code:
            return;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void processEvent(HwSecurityEvent ev) {
        HashMap<HwSecurityTaskBase, EventRegInfo> copyMaps = new HashMap();
        synchronized (this.mLock) {
            int evId = ev.getEvID();
            LogHelper.i(TAG, "processEvent: " + evId);
            HashMap<HwSecurityTaskBase, EventRegInfo> maps = getEventMap(evId, false);
            if (maps == null) {
                return;
            }
            for (HwSecurityTaskBase key : maps.keySet()) {
                copyMaps.put(key, (EventRegInfo) maps.get(key));
            }
        }
    }

    public static boolean staticRegisterEvent(int evId, HwSecurityTaskBase task, EventListener evListener) {
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
