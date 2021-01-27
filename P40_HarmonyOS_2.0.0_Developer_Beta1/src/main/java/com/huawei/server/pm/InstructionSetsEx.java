package com.huawei.server.pm;

import android.content.pm.ApplicationInfo;
import com.android.server.pm.InstructionSets;
import com.huawei.annotation.HwSystemApi;

@HwSystemApi
public class InstructionSetsEx {
    public static String[] getAppDexInstructionSets(ApplicationInfo info) {
        return InstructionSets.getAppDexInstructionSets(info);
    }

    public static String[] getDexCodeInstructionSets(String[] instructionSets) {
        return InstructionSets.getDexCodeInstructionSets(instructionSets);
    }

    public static String[] getAllDexCodeInstructionSets() {
        return InstructionSets.getAllDexCodeInstructionSets();
    }
}
