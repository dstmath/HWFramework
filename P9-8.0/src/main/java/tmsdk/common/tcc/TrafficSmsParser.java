package tmsdk.common.tcc;

import java.util.concurrent.atomic.AtomicInteger;
import tmsdk.common.TMSDKContext;

public class TrafficSmsParser {

    public static class MatchRule {
        public String postfix;
        public String prefix;
        public int type;
        public int unit;

        public MatchRule(int i, int i2, String str, String str2) {
            this.unit = i;
            this.type = i2;
            this.prefix = str;
            this.postfix = str2;
        }
    }

    static {
        TMSDKContext.registerNatives(6, TrafficSmsParser.class);
    }

    public static int getNumberEntrance(String str, String str2, MatchRule matchRule, AtomicInteger atomicInteger) {
        return nativeGetNumberEntrance(str, str2, matchRule, atomicInteger);
    }

    public static int getWrongSmsType(String str, String str2) {
        return nativeGetWrongSmsType(str, str2);
    }

    private static native int nativeGetNumberEntrance(String str, String str2, MatchRule matchRule, AtomicInteger atomicInteger);

    private static native int nativeGetWrongSmsType(String str, String str2);
}
