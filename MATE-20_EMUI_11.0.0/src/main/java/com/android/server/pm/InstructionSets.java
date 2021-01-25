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
    private static final String PREFERRED_INSTRUCTION_SET = VMRuntime.getInstructionSet(Build.SUPPORTED_ABIS[0]);

    public static String[] getAppDexInstructionSets(ApplicationInfo info) {
        return info.primaryCpuAbi != null ? info.secondaryCpuAbi != null ? new String[]{VMRuntime.getInstructionSet(info.primaryCpuAbi), VMRuntime.getInstructionSet(info.secondaryCpuAbi)} : new String[]{VMRuntime.getInstructionSet(info.primaryCpuAbi)} : new String[]{getPreferredInstructionSet()};
    }

    public static String[] getAppDexInstructionSets(PackageSetting ps) {
        return ps.primaryCpuAbiString != null ? ps.secondaryCpuAbiString != null ? new String[]{VMRuntime.getInstructionSet(ps.primaryCpuAbiString), VMRuntime.getInstructionSet(ps.secondaryCpuAbiString)} : new String[]{VMRuntime.getInstructionSet(ps.primaryCpuAbiString)} : new String[]{getPreferredInstructionSet()};
    }

    public static String getPreferredInstructionSet() {
        return PREFERRED_INSTRUCTION_SET;
    }

    public static String getDexCodeInstructionSet(String sharedLibraryIsa) {
        String dexCodeIsa = SystemProperties.get("ro.dalvik.vm.isa." + sharedLibraryIsa);
        return TextUtils.isEmpty(dexCodeIsa) ? sharedLibraryIsa : dexCodeIsa;
    }

    public static String[] getDexCodeInstructionSets(String[] instructionSets) {
        ArraySet<String> dexCodeInstructionSets = new ArraySet<>(instructionSets.length);
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
        List<String> allInstructionSets = new ArrayList<>(allAbis.length);
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
