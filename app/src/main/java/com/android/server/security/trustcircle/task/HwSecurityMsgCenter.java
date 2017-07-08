package com.android.server.security.trustcircle.task;

import com.android.server.security.trustcircle.task.HwSecurityTaskBase.EventListener;
import com.android.server.security.trustcircle.utils.LogHelper;
import java.util.HashMap;

public class HwSecurityMsgCenter {
    public static final String TAG = null;
    private static HwSecurityMsgCenter gInstance;
    private static Object mInstanceLock;
    private HashMap<Integer, HashMap<HwSecurityTaskBase, EventRegInfo>> mEvMaps;
    private Object mLock;

    private static class EventRegInfo {
        public boolean mEnable;
        public EventListener mListener;

        public EventRegInfo(EventListener listener, boolean enable) {
            this.mListener = listener;
            this.mEnable = enable;
        }
    }

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.server.security.trustcircle.task.HwSecurityMsgCenter.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.server.security.trustcircle.task.HwSecurityMsgCenter.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 7 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e9
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 8 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.server.security.trustcircle.task.HwSecurityMsgCenter.<clinit>():void");
    }

    public HwSecurityMsgCenter() {
        this.mEvMaps = new HashMap();
        this.mLock = new Object();
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

    public boolean registerEvent(int evId, HwSecurityTaskBase task, EventListener evListener) {
        synchronized (this.mLock) {
            if (task == null || evListener == null) {
                return false;
            }
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
            for (HwSecurityTaskBase task : copyMaps.keySet()) {
                if (task != null) {
                    EventRegInfo regInfo = (EventRegInfo) copyMaps.get(task);
                    if (regInfo != null && regInfo.mEnable && regInfo.mListener != null && regInfo.mListener.onEvent(ev)) {
                        break;
                    }
                }
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
