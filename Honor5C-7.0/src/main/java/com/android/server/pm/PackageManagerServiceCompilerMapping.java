package com.android.server.pm;

import android.os.SystemProperties;
import com.android.server.wm.WindowManagerService.H;
import dalvik.system.DexFile;

class PackageManagerServiceCompilerMapping {
    static final String[] REASON_STRINGS = null;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.server.pm.PackageManagerServiceCompilerMapping.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.server.pm.PackageManagerServiceCompilerMapping.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: com.android.server.pm.PackageManagerServiceCompilerMapping.<clinit>():void");
    }

    PackageManagerServiceCompilerMapping() {
    }

    private static String getSystemPropertyName(int reason) {
        if (reason >= 0 && reason < REASON_STRINGS.length) {
            return "pm.dexopt." + REASON_STRINGS[reason];
        }
        throw new IllegalArgumentException("reason " + reason + " invalid");
    }

    private static String getAndCheckValidity(int reason) {
        String sysPropValue = SystemProperties.get(getSystemPropertyName(reason));
        if (sysPropValue == null || sysPropValue.isEmpty() || !DexFile.isValidCompilerFilter(sysPropValue)) {
            throw new IllegalStateException("Value \"" + sysPropValue + "\" not valid " + "(reason " + REASON_STRINGS[reason] + ")");
        }
        switch (reason) {
            case H.REMOVE_STARTING /*6*/:
            case H.FINISHED_STARTING /*7*/:
                if (DexFile.isProfileGuidedCompilerFilter(sysPropValue)) {
                    throw new IllegalStateException("\"" + sysPropValue + "\" is profile-guided, " + "but not allowed for " + REASON_STRINGS[reason]);
                }
                break;
        }
        return sysPropValue;
    }

    static void checkProperties() {
        RuntimeException toThrow = null;
        int reason = 0;
        while (reason <= 8) {
            try {
                String sysPropName = getSystemPropertyName(reason);
                if (sysPropName == null || sysPropName.isEmpty() || sysPropName.length() > 31) {
                    throw new IllegalStateException("Reason system property name \"" + sysPropName + "\" for reason " + REASON_STRINGS[reason]);
                }
                getAndCheckValidity(reason);
                reason++;
            } catch (Exception exc) {
                if (toThrow == null) {
                    toThrow = new IllegalStateException("PMS compiler filter settings are bad.");
                }
                toThrow.addSuppressed(exc);
            }
        }
        if (toThrow != null) {
            throw toThrow;
        }
    }

    public static String getCompilerFilterForReason(int reason) {
        return getAndCheckValidity(reason);
    }

    public static String getFullCompilerFilter() {
        String value = SystemProperties.get("dalvik.vm.dex2oat-filter");
        if (value == null || value.isEmpty()) {
            return "speed";
        }
        if (!DexFile.isValidCompilerFilter(value) || DexFile.isProfileGuidedCompilerFilter(value)) {
            return "speed";
        }
        return value;
    }

    public static String getNonProfileGuidedCompilerFilter(String filter) {
        return DexFile.getNonProfileGuidedCompilerFilter(filter);
    }
}
