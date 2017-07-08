package com.android.server.wifi;

import android.security.KeyStore;

public class WifiInjector {
    private final BuildProperties mBuildProperties;
    private final Clock mClock;
    private final KeyStore mKeyStore;
    private final PropertyService mPropertyService;
    private final WifiLastResortWatchdog mWifiLastResortWatchdog;
    private final WifiMetrics mWifiMetrics;

    private static class LazyHolder {
        public static final WifiInjector sInstance = null;

        static {
            /* JADX: method processing error */
/*
            Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.server.wifi.WifiInjector.LazyHolder.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:263)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.server.wifi.WifiInjector.LazyHolder.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 8 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 9 more
*/
            /*
            // Can't load method instructions.
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.server.wifi.WifiInjector.LazyHolder.<clinit>():void");
        }

        private LazyHolder() {
        }
    }

    public WifiInjector() {
        this.mClock = new Clock();
        this.mWifiMetrics = new WifiMetrics(this.mClock);
        this.mWifiLastResortWatchdog = new WifiLastResortWatchdog(this.mWifiMetrics);
        this.mPropertyService = new SystemPropertyService();
        this.mBuildProperties = new SystemBuildProperties();
        this.mKeyStore = KeyStore.getInstance();
    }

    public static WifiInjector getInstance() {
        return LazyHolder.sInstance;
    }

    public WifiMetrics getWifiMetrics() {
        return this.mWifiMetrics;
    }

    public WifiLastResortWatchdog getWifiLastResortWatchdog() {
        return this.mWifiLastResortWatchdog;
    }

    public Clock getClock() {
        return this.mClock;
    }

    public PropertyService getPropertyService() {
        return this.mPropertyService;
    }

    public BuildProperties getBuildProperties() {
        return this.mBuildProperties;
    }

    public KeyStore getKeyStore() {
        return this.mKeyStore;
    }
}
