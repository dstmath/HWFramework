package com.android.server.pm.dex;

import java.io.OutputStream;
import java.util.Map;
import java.util.Set;

public interface IHwPackageDynamicCodeLoading {
    void clear();

    void dump(OutputStream outputStream);

    void makeDexOptReport();

    void readAndSync(Map<String, Set<Integer>> map);

    void record(String str, long j, int i);

    void recordDynamic(String str, String str2, int i, String str3, int i2);

    void removePackage(String str);

    void removeUserPackage(String str, int i);

    void writeNow();
}
