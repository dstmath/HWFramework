package ohos.security.privacyability;

import com.huawei.privacyability.DiffPrivacyManager;
import ohos.rpc.IRemoteObject;

class DiffPrivacyManagerProxy implements IDiffPrivacyManager {
    private final IRemoteObject mRemote;

    DiffPrivacyManagerProxy(IRemoteObject iRemoteObject) {
        this.mRemote = iRemoteObject;
    }

    @Override // ohos.rpc.IRemoteBroker
    public IRemoteObject asObject() {
        return this.mRemote;
    }

    @Override // ohos.security.privacyability.IDiffPrivacyManager
    public String getDiffPrivacyBloomFilter(String str, String str2) {
        return DiffPrivacyManager.getInstance().diffPrivacyBloomfilter(str, str2);
    }

    @Override // ohos.security.privacyability.IDiffPrivacyManager
    public String getDiffPrivacyBitsHistogram(int[] iArr, String str) {
        return DiffPrivacyManager.getInstance().diffPrivacyBitshistogram(iArr, str);
    }

    @Override // ohos.security.privacyability.IDiffPrivacyManager
    public String getDiffPrivacyCountSketch(String str, String str2) {
        return DiffPrivacyManager.getInstance().diffPrivacyCountsketch(str, str2);
    }
}
