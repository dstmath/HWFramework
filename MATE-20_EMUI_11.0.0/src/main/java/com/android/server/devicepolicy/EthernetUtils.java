package com.android.server.devicepolicy;

import android.content.Context;
import android.net.IEthernetManager;
import android.net.IpConfiguration;
import android.net.LinkAddress;
import android.net.NetworkUtils;
import android.net.ProxyInfo;
import android.net.StaticIpConfiguration;
import android.os.Binder;
import android.os.Bundle;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.text.TextUtils;
import com.android.server.devicepolicy.plugins.SettingsMDMPlugin;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class EthernetUtils {
    private static final int DEFALUT_LIST_SIZE = 3;
    private static final String DHCP = "1";
    private static final String ETH_DNS_SERVERS = "ethDnsServers";
    private static final String ETH_GATEWAY = "ethGateway";
    private static final String ETH_IFACE_DEFAULT = "eth0";
    private static final String ETH_INTERFACE_NAME = "ethInterFaceName";
    private static final String ETH_IPADDRESS = "ethIpAddress";
    private static final String ETH_KEY_WORDS = "interfaceName";
    private static final String ETH_PROXY_HOST = "ethProxyHost";
    private static final String ETH_PROXY_PORT = "ethProxyPort";
    private static final String ETH_STATIC_OR_DHCP = "ethStaticOrDhcp";
    private static final String ETH_SUBNET_MASK = "ethSubnetMask";
    private static final String STATIC = "2";
    private static final String TAG = "EthernetUtils";

    private EthernetUtils() {
    }

    public static boolean setEthernetConfiguration(Context context, Bundle policyData) {
        IpConfiguration ipConfiguration;
        if (policyData == null || context == null) {
            HwLog.i(TAG, "config Ethernet but policy data is null");
            return false;
        }
        String ethStaticOrdhcp = policyData.getString(ETH_STATIC_OR_DHCP, SettingsMDMPlugin.EMPTY_STRING);
        if (DHCP.equals(ethStaticOrdhcp)) {
            ipConfiguration = new IpConfiguration(IpConfiguration.IpAssignment.DHCP, IpConfiguration.ProxySettings.NONE, (StaticIpConfiguration) null, ProxyInfo.buildDirectProxy(SettingsMDMPlugin.EMPTY_STRING, 0));
        } else if (STATIC.equals(ethStaticOrdhcp)) {
            ipConfiguration = convertParamsToIpConfiguration(policyData);
        } else {
            HwLog.i(TAG, "configEthernet neither static or dhcp, so return");
            return false;
        }
        long callingId = Binder.clearCallingIdentity();
        if (ipConfiguration != null) {
            try {
                IEthernetManager service = IEthernetManager.Stub.asInterface(ServiceManager.getServiceOrThrow("ethernet"));
                if (service != null) {
                    service.setConfiguration(policyData.getString(ETH_INTERFACE_NAME, ETH_IFACE_DEFAULT), ipConfiguration);
                    HwLog.i(TAG, "ethernet config finished");
                    Binder.restoreCallingIdentity(callingId);
                    return true;
                }
            } catch (ServiceManager.ServiceNotFoundException e) {
                HwLog.e(TAG, "setConfiguration ServiceNotFoundException");
            } catch (RemoteException e2) {
                HwLog.e(TAG, "setConfiguration RemoteException");
            } catch (Throwable th) {
                Binder.restoreCallingIdentity(callingId);
                throw th;
            }
        } else {
            HwLog.e(TAG, "configEthernet error ipConfiguration is null");
        }
        Binder.restoreCallingIdentity(callingId);
        return false;
    }

    private static ArrayList<InetAddress> convertStrintToInetAddress(ArrayList<String> dnsServersStr) {
        ArrayList<InetAddress> dnsServers = new ArrayList<>(3);
        if (dnsServersStr == null || dnsServersStr.isEmpty()) {
            return dnsServers;
        }
        Iterator<String> it = dnsServersStr.iterator();
        while (it.hasNext()) {
            dnsServers.add(NetworkUtils.numericToInetAddress(it.next()));
        }
        return dnsServers;
    }

    private static int getNetPrefixLength(String maskStr) {
        int i;
        if (TextUtils.isEmpty(maskStr)) {
            return 0;
        }
        int prefixLength = 0;
        for (String ip : maskStr.split("\\.")) {
            try {
                String str = Integer.toBinaryString(Integer.parseInt(ip));
                int count = 0;
                int i2 = 0;
                while (i2 < str.length() && (i = str.indexOf(DHCP, i2)) != -1) {
                    count++;
                    i2 = i + 1;
                }
                prefixLength += count;
            } catch (NumberFormatException e) {
                HwLog.e(TAG, "getPrefixLength error!");
                return 0;
            }
        }
        return prefixLength;
    }

    private static IpConfiguration convertParamsToIpConfiguration(Bundle policyData) {
        IpConfiguration ipConfiguration;
        IpConfiguration ipConfiguration2;
        if (policyData == null) {
            return null;
        }
        String ipAddressStr = policyData.getString(ETH_IPADDRESS, SettingsMDMPlugin.EMPTY_STRING);
        ArrayList<String> dnsServersStr = null;
        try {
            dnsServersStr = policyData.getStringArrayList(ETH_DNS_SERVERS);
        } catch (ArrayIndexOutOfBoundsException e) {
            HwLog.e(TAG, "get dnsServer failed");
        }
        StaticIpConfiguration staticIpConfiguration = new StaticIpConfiguration();
        LinkAddress ipAddress = getIpLinkAddress(policyData.getString(ETH_STATIC_OR_DHCP, SettingsMDMPlugin.EMPTY_STRING), getNetPrefixLength(policyData.getString(ETH_SUBNET_MASK, SettingsMDMPlugin.EMPTY_STRING)), ipAddressStr);
        InetAddress gateway = NetworkUtils.numericToInetAddress(policyData.getString(ETH_GATEWAY, SettingsMDMPlugin.EMPTY_STRING));
        ArrayList<InetAddress> dnsServers = convertStrintToInetAddress(dnsServersStr);
        staticIpConfiguration.ipAddress = ipAddress;
        staticIpConfiguration.gateway = gateway;
        if (dnsServers != null) {
            staticIpConfiguration.dnsServers.addAll(dnsServers);
        }
        try {
            String proxyHost = policyData.getString(ETH_PROXY_HOST, SettingsMDMPlugin.EMPTY_STRING);
            if (!TextUtils.isEmpty(proxyHost)) {
                int portProxy = 0;
                String proxyPort = policyData.getString(ETH_PROXY_PORT);
                if (!TextUtils.isEmpty(proxyPort)) {
                    try {
                        portProxy = Integer.valueOf(proxyPort).intValue();
                    } catch (NumberFormatException e2) {
                        try {
                            HwLog.e(TAG, "proxy port numberFormatException");
                        } catch (ArrayIndexOutOfBoundsException | IllegalArgumentException e3) {
                            ipConfiguration = null;
                            HwLog.e(TAG, "configEthernet error ");
                            return ipConfiguration;
                        }
                    }
                }
                ipConfiguration = null;
                try {
                } catch (ArrayIndexOutOfBoundsException | IllegalArgumentException e4) {
                    HwLog.e(TAG, "configEthernet error ");
                    return ipConfiguration;
                }
                try {
                    ipConfiguration2 = new IpConfiguration(IpConfiguration.IpAssignment.STATIC, IpConfiguration.ProxySettings.STATIC, staticIpConfiguration, ProxyInfo.buildDirectProxy(proxyHost, portProxy));
                } catch (ArrayIndexOutOfBoundsException | IllegalArgumentException e5) {
                    HwLog.e(TAG, "configEthernet error ");
                    return ipConfiguration;
                }
            } else {
                ipConfiguration2 = new IpConfiguration(IpConfiguration.IpAssignment.STATIC, IpConfiguration.ProxySettings.NONE, staticIpConfiguration, ProxyInfo.buildDirectProxy(SettingsMDMPlugin.EMPTY_STRING, 0));
            }
            return ipConfiguration2;
        } catch (ArrayIndexOutOfBoundsException | IllegalArgumentException e6) {
            ipConfiguration = null;
            HwLog.e(TAG, "configEthernet error ");
            return ipConfiguration;
        }
    }

    private static LinkAddress getIpLinkAddress(String ipAssignment, int netPrefixLength, String ipAddressStr) {
        if (!STATIC.equals(ipAssignment) || netPrefixLength != 0) {
            return new LinkAddress(NetworkUtils.numericToInetAddress(ipAddressStr), netPrefixLength);
        }
        return new LinkAddress(ipAddressStr);
    }

    public static Bundle getEthernetConfiguration(Context context, Bundle keyWords) {
        IpConfiguration ipConfiguration;
        Bundle result = new Bundle();
        if (!(keyWords == null || context == null || (ipConfiguration = getIpConfiguration(context, keyWords.getString(ETH_KEY_WORDS))) == null)) {
            IpConfiguration.IpAssignment ipAssignment = ipConfiguration.getIpAssignment();
            if (ipAssignment != null) {
                if (ipAssignment == IpConfiguration.IpAssignment.STATIC) {
                    result.putString(ETH_STATIC_OR_DHCP, STATIC);
                } else if (ipAssignment == IpConfiguration.IpAssignment.DHCP) {
                    result.putString(ETH_STATIC_OR_DHCP, DHCP);
                } else {
                    HwLog.i(TAG, "get ipAssignment error");
                }
            }
            StaticIpConfiguration staticIpConfiguration = ipConfiguration.getStaticIpConfiguration();
            if (staticIpConfiguration != null) {
                LinkAddress linkAddress = staticIpConfiguration.getIpAddress();
                InetAddress inetIpAddress = null;
                if (linkAddress != null) {
                    inetIpAddress = linkAddress.getAddress();
                    result.putString(ETH_SUBNET_MASK, linkAddress.getPrefixLength() + SettingsMDMPlugin.EMPTY_STRING);
                }
                if (inetIpAddress != null) {
                    result.putString(ETH_IPADDRESS, inetIpAddress.getHostAddress());
                }
                InetAddress inetAddress = staticIpConfiguration.getGateway();
                if (inetAddress != null) {
                    result.putString(ETH_GATEWAY, inetAddress.getHostAddress());
                }
                result.putStringArrayList(ETH_DNS_SERVERS, getDnsServers(staticIpConfiguration.getDnsServers()));
            }
            ProxyInfo proxyInfo = ipConfiguration.getHttpProxy();
            if (proxyInfo != null) {
                result.putString(ETH_PROXY_HOST, proxyInfo.getHost());
                result.putString(ETH_PROXY_PORT, proxyInfo.getPort() + SettingsMDMPlugin.EMPTY_STRING);
            }
        }
        return result;
    }

    private static IpConfiguration getIpConfiguration(Context context, String interfaceName) {
        IpConfiguration ipConfiguration = null;
        long callingId = Binder.clearCallingIdentity();
        try {
            IEthernetManager service = IEthernetManager.Stub.asInterface(ServiceManager.getServiceOrThrow("ethernet"));
            if (service != null) {
                ipConfiguration = service.getConfiguration(interfaceName);
            }
        } catch (ServiceManager.ServiceNotFoundException e) {
            HwLog.e(TAG, "getIpConfiguration ServiceNotFoundException");
        } catch (RemoteException e2) {
            HwLog.e(TAG, "getIpConfiguration RemoteException");
        } catch (Throwable th) {
            Binder.restoreCallingIdentity(callingId);
            throw th;
        }
        Binder.restoreCallingIdentity(callingId);
        return ipConfiguration;
    }

    private static ArrayList<String> getDnsServers(List<InetAddress> dnsServers) {
        ArrayList<String> dnsServerTarget = new ArrayList<>(3);
        if (dnsServers != null) {
            for (InetAddress inetAddress : dnsServers) {
                String server = inetAddress.getHostAddress();
                if (!TextUtils.isEmpty(server)) {
                    dnsServerTarget.add(server);
                }
            }
        }
        return dnsServerTarget;
    }
}
