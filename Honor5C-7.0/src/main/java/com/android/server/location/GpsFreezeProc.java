package com.android.server.location;

import android.os.WorkSource;
import android.util.Log;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;

public class GpsFreezeProc {
    private static String TAG;
    private static GpsFreezeProc mGpsFreezeProc;
    private ArrayList<GpsFreezeListener> mFreezeListenerList;
    private HashMap<String, Integer> mFreezeProcesses;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.server.location.GpsFreezeProc.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.server.location.GpsFreezeProc.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 7 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 8 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.server.location.GpsFreezeProc.<clinit>():void");
    }

    private GpsFreezeProc() {
        this.mFreezeProcesses = new HashMap();
        this.mFreezeListenerList = new ArrayList();
    }

    public static GpsFreezeProc getInstance() {
        if (mGpsFreezeProc == null) {
            mGpsFreezeProc = new GpsFreezeProc();
        }
        return mGpsFreezeProc;
    }

    public void addFreezeProcess(String pkg, int uid) {
        synchronized (this.mFreezeProcesses) {
            this.mFreezeProcesses.put(pkg, Integer.valueOf(uid));
        }
        Log.d(TAG, "addFreezeProcess pkg:" + pkg);
        for (GpsFreezeListener freezeListener : this.mFreezeListenerList) {
            freezeListener.onFreezeProChange();
        }
    }

    public void removeFreezeProcess(String pkg, int uid) {
        synchronized (this.mFreezeProcesses) {
            if (uid == 0) {
                if ("".equals(pkg)) {
                    this.mFreezeProcesses.clear();
                }
            }
            this.mFreezeProcesses.remove(pkg);
        }
        Log.d(TAG, "removeFreezeProcess pkg:" + pkg);
        for (GpsFreezeListener freezeListener : this.mFreezeListenerList) {
            freezeListener.onFreezeProChange();
        }
    }

    public boolean isFreeze(String pkgName) {
        boolean containsKey;
        synchronized (this.mFreezeProcesses) {
            containsKey = this.mFreezeProcesses.containsKey(pkgName);
        }
        return containsKey;
    }

    public void registerFreezeListener(GpsFreezeListener freezeListener) {
        this.mFreezeListenerList.add(freezeListener);
    }

    public boolean shouldFreeze(WorkSource workSource) {
        boolean shouldFreeze = true;
        for (int i = 0; i < workSource.size(); i++) {
            if (!getInstance().isFreeze(workSource.getName(i))) {
                shouldFreeze = false;
            }
        }
        if (shouldFreeze) {
            Log.i(TAG, "should freeze gps");
        }
        return shouldFreeze;
    }

    public void dump(PrintWriter pw) {
        pw.println("Location Freeze Proc:");
        synchronized (this.mFreezeProcesses) {
            for (String pkg : this.mFreezeProcesses.keySet()) {
                pw.println("   " + pkg);
            }
        }
    }
}
