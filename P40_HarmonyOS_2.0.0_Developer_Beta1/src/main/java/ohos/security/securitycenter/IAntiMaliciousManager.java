package ohos.security.securitycenter;

import java.util.List;
import ohos.rpc.IRemoteBroker;

interface IAntiMaliciousManager extends IRemoteBroker {
    public static final String DESCRIPTOR = "IAntiMaliciousManager";

    List<VirusAppInfo> getVirusAppList() throws AntiMaliciousException;

    int getWifiThreatDetectStatus() throws AntiMaliciousException;
}
