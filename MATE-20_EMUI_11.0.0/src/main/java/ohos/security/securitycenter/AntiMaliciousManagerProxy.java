package ohos.security.securitycenter;

import com.huawei.securitycenter.HwVirusAppInfo;
import com.huawei.securitycenter.HwVirusManager;
import com.huawei.securitycenter.PermissionDenyException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import ohos.rpc.IRemoteObject;

class AntiMaliciousManagerProxy implements IAntiMaliciousManager {
    private final IRemoteObject mRemote;

    AntiMaliciousManagerProxy(IRemoteObject iRemoteObject) {
        this.mRemote = iRemoteObject;
    }

    @Override // ohos.rpc.IRemoteBroker
    public IRemoteObject asObject() {
        return this.mRemote;
    }

    @Override // ohos.security.securitycenter.IAntiMaliciousManager
    public List<VirusAppInfo> getVirusAppList() throws AntiMaliciousException {
        try {
            List<HwVirusAppInfo> virusAppList = HwVirusManager.getInstance().getVirusAppList();
            if (virusAppList != null) {
                if (virusAppList.size() != 0) {
                    ArrayList arrayList = new ArrayList(virusAppList.size());
                    for (HwVirusAppInfo hwVirusAppInfo : virusAppList) {
                        arrayList.add(new VirusAppInfo(hwVirusAppInfo.getApkCategory(), hwVirusAppInfo.getApkPackageName(), hwVirusAppInfo.getApkSha256()));
                    }
                    return arrayList;
                }
            }
            return Collections.emptyList();
        } catch (PermissionDenyException e) {
            throw new AntiMaliciousException(e.getMessage());
        }
    }

    @Override // ohos.security.securitycenter.IAntiMaliciousManager
    public int getWifiThreatDetectStatus() throws AntiMaliciousException {
        try {
            return HwVirusManager.getInstance().getWifiThreatDetectStatus();
        } catch (PermissionDenyException e) {
            throw new AntiMaliciousException(e.getMessage());
        }
    }
}
