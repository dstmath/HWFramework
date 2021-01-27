package ohos.security.privacyability;

import ohos.rpc.IRemoteBroker;

public interface IDiffPrivacyManager extends IRemoteBroker {
    String getDiffPrivacyBitsHistogram(int[] iArr, String str);

    String getDiffPrivacyBloomFilter(String str, String str2);

    String getDiffPrivacyCountSketch(String str, String str2);
}
