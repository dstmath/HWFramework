package android.net.wifi;

import java.util.List;

public interface HwInnerNetworkManager {
    List<String> getApLinkedStaList();

    String getWiFiDnsStats(int i);

    void setAccessPointHw(String str, String str2);

    void setSoftapDisassociateSta(String str);

    void setSoftapMacFilter(String str);
}
