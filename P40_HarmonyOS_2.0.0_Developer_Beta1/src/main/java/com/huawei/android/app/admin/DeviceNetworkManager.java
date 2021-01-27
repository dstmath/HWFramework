package com.huawei.android.app.admin;

import android.content.ComponentName;
import android.os.Bundle;
import android.util.Log;
import com.huawei.android.app.admin.DeviceEthernetProfile;
import huawei.android.app.admin.HwDevicePolicyManagerEx;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class DeviceNetworkManager {
    private static final String DHCP = "1";
    private static final String ETH_DNS_SERVERS = "ethDnsServers";
    private static final String ETH_GATEWAY = "ethGateway";
    private static final String ETH_INTERFACE_NAME = "ethInterFaceName";
    private static final String ETH_IPADDRESS = "ethIpAddress";
    private static final String ETH_KEY_WORDS = "interfaceName";
    private static final String ETH_PROXY_HOST = "ethProxyHost";
    private static final String ETH_PROXY_PORT = "ethProxyPort";
    private static final String ETH_STATIC_OR_DHCP = "ethStaticOrDhcp";
    private static final String ETH_SUBNET_MASK = "ethSubnetMask";
    private static final String POLICY_NETWORK_BLACK_DOMAIN_LIST = "network-black-domain-list";
    private static final String POLICY_NETWORK_BLACK_IP_LIST = "network-black-ip-list";
    private static final String POLICY_NETWORK_BLACK_LIST = "network-black-list";
    private static final String POLICY_NETWORK_BLOCK_LIST = "network-block-list";
    private static final String POLICY_NETWORK_ETHERNET = "network-set-ethernet-config";
    private static final String POLICY_NETWORK_TRUST_APP_LIST = "network-trust-app-list";
    private static final String POLICY_NETWORK_WHITE_DOMAIN_LIST = "network-white-domain-list";
    private static final String POLICY_NETWORK_WHITE_IP_LIST = "network-white-ip-list";
    private static final String STATIC = "2";
    private static final String TAG = "DeviceNetworkManager";
    private final HwDevicePolicyManagerEx mDpm = new HwDevicePolicyManagerEx();

    public void addApn(ComponentName admin, Map<String, String> apnInfo) {
        this.mDpm.addApn(admin, apnInfo);
    }

    public void deleteApn(ComponentName admin, String apnId) {
        this.mDpm.deleteApn(admin, apnId);
    }

    public void updateApn(ComponentName admin, Map<String, String> apnInfo, String apnId) {
        this.mDpm.updateApn(admin, apnInfo, apnId);
    }

    public void setPreferApn(ComponentName admin, String apnId) {
        this.mDpm.setPreferApn(admin, apnId);
    }

    public List<String> queryApn(ComponentName admin, Map<String, String> apnInfo) {
        return this.mDpm.queryApn(admin, apnInfo);
    }

    public Map<String, String> getApnInfo(ComponentName admin, String apnId) {
        return this.mDpm.getApnInfo(admin, apnId);
    }

    @Deprecated
    public void addNetworkAccessWhitelist(ComponentName admin, List<String> list) {
    }

    @Deprecated
    public void removeNetworkAccessWhitelist(ComponentName admin, List<String> list) {
    }

    @Deprecated
    public List<String> getNetworkAccessWhitelist(ComponentName admin) {
        return Collections.emptyList();
    }

    @Deprecated
    public boolean addNetworkAccessBlackList(ComponentName admin, ArrayList<String> addDomainList) {
        Bundle bundle = new Bundle();
        bundle.putStringArrayList("value", addDomainList);
        return this.mDpm.setPolicy(admin, POLICY_NETWORK_BLACK_LIST, bundle);
    }

    @Deprecated
    public boolean removeNetworkAccessBlackList(ComponentName admin, ArrayList<String> removeDomainList) {
        return removeNetworkAccessBlockList(admin, removeDomainList);
    }

    @Deprecated
    public List<String> getNetworkAccessBlackList(ComponentName admin) {
        return getNetworkAccessBlockList(admin);
    }

    public List<String> getNetworkAccessBlockList(ComponentName admin) {
        Bundle bundle = this.mDpm.getPolicy(admin, POLICY_NETWORK_BLACK_LIST);
        List<String> lists = new ArrayList<>();
        if (bundle == null) {
            return lists;
        }
        try {
            return bundle.getStringArrayList("value");
        } catch (ArrayIndexOutOfBoundsException e) {
            Log.e(TAG, "getNetworkAccessBlockList exception.");
            return lists;
        }
    }

    public boolean addNetworkAccessBlockList(ComponentName admin, ArrayList<String> addDomainList) {
        Bundle bundle = new Bundle();
        bundle.putStringArrayList("value", addDomainList);
        return this.mDpm.setPolicy(admin, POLICY_NETWORK_BLOCK_LIST, bundle);
    }

    public boolean removeNetworkAccessBlockList(ComponentName admin, ArrayList<String> removeDomainList) {
        Bundle bundle = new Bundle();
        bundle.putStringArrayList("value", removeDomainList);
        return this.mDpm.removePolicy(admin, POLICY_NETWORK_BLACK_LIST, bundle);
    }

    public boolean addNetworkList(ComponentName admin, boolean isTrustList, boolean isDomainList, ArrayList<String> addrList) {
        Bundle bundle = new Bundle();
        String listType = getNetworkAccessListType(isTrustList, isDomainList);
        bundle.putStringArrayList("value", addrList);
        return this.mDpm.setPolicy(admin, listType, bundle);
    }

    public List<String> getNetworkList(ComponentName admin, boolean isTrustList, boolean isDomainList) {
        Bundle bundle = this.mDpm.getPolicy(admin, getNetworkAccessListType(isTrustList, isDomainList));
        List<String> lists = new ArrayList<>();
        if (bundle == null) {
            return lists;
        }
        try {
            return bundle.getStringArrayList("value");
        } catch (ArrayIndexOutOfBoundsException e) {
            Log.e(TAG, "getNetworkList exception.");
            return lists;
        }
    }

    public boolean removeNetworkList(ComponentName admin, boolean isTrustList, boolean isDomainList, ArrayList<String> addrList) {
        Bundle bundle = new Bundle();
        String listType = getNetworkAccessListType(isTrustList, isDomainList);
        bundle.putStringArrayList("value", addrList);
        return this.mDpm.removePolicy(admin, listType, bundle);
    }

    public boolean clearNetworkList(ComponentName admin) {
        String listType = getNetworkAccessListType(true, false);
        Bundle policyList = this.mDpm.getPolicy(admin, listType);
        if (policyList != null) {
            this.mDpm.removePolicy(admin, listType, policyList);
        }
        String listType2 = getNetworkAccessListType(true, true);
        Bundle policyList2 = this.mDpm.getPolicy(admin, listType2);
        if (policyList2 != null) {
            this.mDpm.removePolicy(admin, listType2, policyList2);
        }
        String listType3 = getNetworkAccessListType(false, false);
        Bundle policyList3 = this.mDpm.getPolicy(admin, listType3);
        if (policyList3 != null) {
            this.mDpm.removePolicy(admin, listType3, policyList3);
        }
        String listType4 = getNetworkAccessListType(false, true);
        Bundle policyList4 = this.mDpm.getPolicy(admin, listType4);
        if (policyList4 != null) {
            this.mDpm.removePolicy(admin, listType4, policyList4);
        }
        return true;
    }

    public List<String> queryBrowsingHistory(ComponentName admin) {
        Bundle bundle = this.mDpm.getCustomPolicy(admin, "queryBrowsingHistory", null);
        List<String> lists = new ArrayList<>();
        if (bundle == null) {
            return lists;
        }
        try {
            return bundle.getStringArrayList("value");
        } catch (ArrayIndexOutOfBoundsException e) {
            Log.e(TAG, "queryBrowsingHistory exception.");
            return lists;
        }
    }

    public void resetNetworkSetting(ComponentName admin) {
        this.mDpm.resetNetworkSetting(admin);
    }

    private String getNetworkAccessListType(boolean isWhiteList, boolean isDomainList) {
        if (isWhiteList) {
            if (isDomainList) {
                return POLICY_NETWORK_WHITE_DOMAIN_LIST;
            }
            return POLICY_NETWORK_WHITE_IP_LIST;
        } else if (isDomainList) {
            return POLICY_NETWORK_BLACK_DOMAIN_LIST;
        } else {
            return POLICY_NETWORK_BLACK_IP_LIST;
        }
    }

    public boolean setEthernetConfiguration(ComponentName admin, DeviceEthernetProfile deviceEthernetProfile) {
        if (deviceEthernetProfile == null) {
            return false;
        }
        Bundle bundle = new Bundle();
        DeviceEthernetProfile.IpAssignment ipAssignment = deviceEthernetProfile.getIpAssignment();
        if (ipAssignment == null) {
            return false;
        }
        if (ipAssignment == DeviceEthernetProfile.IpAssignment.DHCP) {
            bundle.putString(ETH_STATIC_OR_DHCP, DHCP);
        } else {
            bundle.putString(ETH_STATIC_OR_DHCP, STATIC);
        }
        bundle.putString(ETH_IPADDRESS, deviceEthernetProfile.getIpAddress());
        bundle.putString(ETH_SUBNET_MASK, deviceEthernetProfile.getSubnetMask());
        bundle.putString(ETH_GATEWAY, deviceEthernetProfile.getGateway());
        bundle.putStringArrayList(ETH_DNS_SERVERS, deviceEthernetProfile.getDnsServers());
        bundle.putString(ETH_PROXY_HOST, deviceEthernetProfile.getProxyHost());
        bundle.putString(ETH_PROXY_PORT, deviceEthernetProfile.getProxyPort() + "");
        bundle.putString(ETH_INTERFACE_NAME, deviceEthernetProfile.getInterfaceName());
        return this.mDpm.setCustomPolicy(admin, POLICY_NETWORK_ETHERNET, bundle);
    }

    public DeviceEthernetProfile getEthernetConfiguration(ComponentName admin, String interfaceName) {
        DeviceEthernetProfile result = new DeviceEthernetProfile();
        if (admin == null) {
            return result;
        }
        Bundle keywords = new Bundle();
        keywords.putString(ETH_KEY_WORDS, interfaceName);
        Bundle bundle = this.mDpm.getCustomPolicy(admin, POLICY_NETWORK_ETHERNET, keywords);
        if (bundle != null) {
            String type = bundle.getString(ETH_STATIC_OR_DHCP, "");
            if (DHCP.equals(type)) {
                result.setIpAssignment(DeviceEthernetProfile.IpAssignment.DHCP);
            } else if (STATIC.equals(type)) {
                result.setIpAssignment(DeviceEthernetProfile.IpAssignment.STATIC);
            } else {
                Log.e(TAG, "ethernet type get failed");
            }
            result.setInterfaceName(bundle.getString(ETH_INTERFACE_NAME, interfaceName));
            result.setIpAddress(bundle.getString(ETH_IPADDRESS, ""));
            result.setSubnetMask(bundle.getString(ETH_SUBNET_MASK, ""));
            result.setGateway(bundle.getString(ETH_GATEWAY, ""));
            result.setProxyHost(bundle.getString(ETH_PROXY_HOST, ""));
            try {
                result.setProxyPort(Integer.valueOf(bundle.getString(ETH_PROXY_PORT)).intValue());
            } catch (NumberFormatException e) {
                Log.e(TAG, "proxy port get exception");
            }
            try {
                result.setDnsServers(bundle.getStringArrayList(ETH_DNS_SERVERS));
            } catch (ArrayIndexOutOfBoundsException e2) {
                Log.e(TAG, "set dnsServers failed");
            }
        }
        return result;
    }

    public boolean addNetworkAccessAppList(ComponentName admin, ArrayList<String> appList, boolean isTrustList) {
        Bundle bundle = new Bundle();
        if (!isTrustList) {
            Log.e(TAG, "not supported add network access app block list");
            return false;
        }
        bundle.putStringArrayList("value", appList);
        return this.mDpm.setPolicy(admin, POLICY_NETWORK_TRUST_APP_LIST, bundle);
    }

    public boolean removeNetworkAccessAppList(ComponentName admin, ArrayList<String> appList, boolean isTrustList) {
        Bundle bundle = new Bundle();
        if (!isTrustList) {
            Log.e(TAG, "not supported remove network access app block list");
            return false;
        }
        bundle.putStringArrayList("value", appList);
        return this.mDpm.removePolicy(admin, POLICY_NETWORK_TRUST_APP_LIST, bundle);
    }

    public boolean clearNetworkAccessAppList(ComponentName admin) {
        Bundle policyList = this.mDpm.getPolicy(admin, POLICY_NETWORK_TRUST_APP_LIST);
        if (policyList != null) {
            return this.mDpm.removePolicy(admin, POLICY_NETWORK_TRUST_APP_LIST, policyList);
        }
        return true;
    }

    public ArrayList<String> getNetworkAccessAppList(ComponentName admin, boolean isTrustList) {
        if (!isTrustList) {
            Log.e(TAG, "not supported get network access app block list");
            return new ArrayList<>(Collections.emptyList());
        }
        Bundle bundle = this.mDpm.getPolicy(admin, POLICY_NETWORK_TRUST_APP_LIST);
        ArrayList<String> lists = new ArrayList<>();
        if (bundle == null) {
            return lists;
        }
        try {
            return bundle.getStringArrayList("value");
        } catch (ArrayIndexOutOfBoundsException e) {
            Log.e(TAG, "getStringArrayList get exception");
            return lists;
        }
    }
}
