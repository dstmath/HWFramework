package ohos.appexecfwk.utils;

import ohos.aafwk.content.Intent;
import ohos.com.sun.org.apache.xpath.internal.compiler.PsuedoNames;
import ohos.devtools.JLog;

public class JLogUtil {
    public static void debugLog(int i, String str, String str2, long j) {
        long currentTimeMillis = System.currentTimeMillis();
        JLog.debug(i, str + PsuedoNames.PSEUDONAME_ROOT + str2 + " cost: " + (currentTimeMillis - j) + "ms");
    }

    public static void printStartAbilityInfo(Intent intent, long j, int i) {
        JLog.printStartAbilityInfo(intent, j, i);
    }
}
