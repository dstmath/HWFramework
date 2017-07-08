package com.android.server.rms.iaware.memory.utils;

import android.rms.iaware.AwareLog;
import android.util.ArrayMap;

public class BigMemoryInfo {
    private static final String TAG = "AwareMem_BigMemConfig";
    private static final Object mLock = null;
    private static BigMemoryInfo sBigMemoryInfo;
    private ArrayMap<String, Long> memoryRequestMap;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.server.rms.iaware.memory.utils.BigMemoryInfo.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.server.rms.iaware.memory.utils.BigMemoryInfo.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: com.android.server.rms.iaware.memory.utils.BigMemoryInfo.<clinit>():void");
    }

    public static BigMemoryInfo getInstance() {
        BigMemoryInfo bigMemoryInfo;
        synchronized (mLock) {
            if (sBigMemoryInfo == null) {
                sBigMemoryInfo = new BigMemoryInfo();
            }
            bigMemoryInfo = sBigMemoryInfo;
        }
        return bigMemoryInfo;
    }

    private BigMemoryInfo() {
        this.memoryRequestMap = null;
        this.memoryRequestMap = new ArrayMap();
    }

    public boolean isBigMemoryApp(String appName) {
        synchronized (this) {
            if (!(this.memoryRequestMap == null || appName == null)) {
                if (!this.memoryRequestMap.isEmpty()) {
                    boolean containsKey = this.memoryRequestMap.containsKey(appName);
                    return containsKey;
                }
            }
            return false;
        }
    }

    public long getAppLaunchRequestMemory(String appName) {
        synchronized (this) {
            if (!(this.memoryRequestMap == null || appName == null)) {
                if (!this.memoryRequestMap.isEmpty() && this.memoryRequestMap.containsKey(appName)) {
                    long longValue = ((Long) this.memoryRequestMap.get(appName)).longValue();
                    return longValue;
                }
            }
            return 0;
        }
    }

    public void resetLaunchMemConfig() {
        synchronized (this) {
            if (this.memoryRequestMap == null) {
                this.memoryRequestMap = new ArrayMap();
            }
            this.memoryRequestMap.clear();
        }
    }

    public void setRequestMemForLaunch(String appName, long launchRequestMem) {
        AwareLog.d(TAG, "setRequestMemForLaunch appname is " + appName);
        synchronized (this) {
            if (appName == null) {
                return;
            }
            if (this.memoryRequestMap == null) {
                this.memoryRequestMap = new ArrayMap();
            }
            this.memoryRequestMap.put(appName, Long.valueOf(launchRequestMem));
        }
    }

    public void removeRequestMemForLaunch(String appName) {
        AwareLog.d(TAG, "removeRequestMemForLaunch appname is " + appName);
        synchronized (this) {
            if (!(this.memoryRequestMap == null || appName == null)) {
                this.memoryRequestMap.remove(appName);
            }
        }
    }
}
