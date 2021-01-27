package com.huawei.dalvik.system;

import dalvik.system.DexFile;
import java.io.FileNotFoundException;

public class DexFileEx {
    public static String[] getDexFileOutputPaths(String fileName, String instructionSet) throws FileNotFoundException {
        return DexFile.getDexFileOutputPaths(fileName, instructionSet);
    }
}
