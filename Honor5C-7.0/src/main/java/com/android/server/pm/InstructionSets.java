package com.android.server.pm;

import android.content.pm.ApplicationInfo;
import android.os.Build;
import android.os.SystemProperties;
import android.text.TextUtils;
import android.util.ArraySet;
import dalvik.system.VMRuntime;
import java.util.ArrayList;
import java.util.List;

public class InstructionSets {
    private static final String PREFERRED_INSTRUCTION_SET = null;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.server.pm.InstructionSets.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.server.pm.InstructionSets.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 5 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 6 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.server.pm.InstructionSets.<clinit>():void");
    }

    public static String[] getAppDexInstructionSets(ApplicationInfo info) {
        if (info.primaryCpuAbi == null) {
            return new String[]{getPreferredInstructionSet()};
        } else if (info.secondaryCpuAbi != null) {
            return new String[]{VMRuntime.getInstructionSet(info.primaryCpuAbi), VMRuntime.getInstructionSet(info.secondaryCpuAbi)};
        } else {
            return new String[]{VMRuntime.getInstructionSet(info.primaryCpuAbi)};
        }
    }

    public static String[] getAppDexInstructionSets(PackageSetting ps) {
        if (ps.primaryCpuAbiString == null) {
            return new String[]{getPreferredInstructionSet()};
        } else if (ps.secondaryCpuAbiString != null) {
            return new String[]{VMRuntime.getInstructionSet(ps.primaryCpuAbiString), VMRuntime.getInstructionSet(ps.secondaryCpuAbiString)};
        } else {
            return new String[]{VMRuntime.getInstructionSet(ps.primaryCpuAbiString)};
        }
    }

    public static String getPreferredInstructionSet() {
        return PREFERRED_INSTRUCTION_SET;
    }

    public static String getDexCodeInstructionSet(String sharedLibraryIsa) {
        String dexCodeIsa = SystemProperties.get("ro.dalvik.vm.isa." + sharedLibraryIsa);
        return TextUtils.isEmpty(dexCodeIsa) ? sharedLibraryIsa : dexCodeIsa;
    }

    public static String[] getDexCodeInstructionSets(String[] instructionSets) {
        ArraySet<String> dexCodeInstructionSets = new ArraySet(instructionSets.length);
        for (String instructionSet : instructionSets) {
            dexCodeInstructionSets.add(getDexCodeInstructionSet(instructionSet));
        }
        return (String[]) dexCodeInstructionSets.toArray(new String[dexCodeInstructionSets.size()]);
    }

    public static String[] getAllDexCodeInstructionSets() {
        String[] supportedInstructionSets = new String[Build.SUPPORTED_ABIS.length];
        for (int i = 0; i < supportedInstructionSets.length; i++) {
            supportedInstructionSets[i] = VMRuntime.getInstructionSet(Build.SUPPORTED_ABIS[i]);
        }
        return getDexCodeInstructionSets(supportedInstructionSets);
    }

    public static List<String> getAllInstructionSets() {
        String[] allAbis = Build.SUPPORTED_ABIS;
        List<String> allInstructionSets = new ArrayList(allAbis.length);
        for (String abi : allAbis) {
            String instructionSet = VMRuntime.getInstructionSet(abi);
            if (!allInstructionSets.contains(instructionSet)) {
                allInstructionSets.add(instructionSet);
            }
        }
        return allInstructionSets;
    }

    public static String getPrimaryInstructionSet(ApplicationInfo info) {
        if (info.primaryCpuAbi == null) {
            return getPreferredInstructionSet();
        }
        return VMRuntime.getInstructionSet(info.primaryCpuAbi);
    }
}
