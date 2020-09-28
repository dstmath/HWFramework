package com.huawei.android.app.admin;

import java.util.ArrayList;

public class DeviceEthernetProfile {
    private ArrayList<String> dnsServers;
    private String gateway;
    private String interfaceName;
    private String ipAddress;
    private IpAssignment ipAssignment;
    private String proxyHost;
    private int proxyPort;
    private String subnetMask;

    public enum IpAssignment {
        DHCP,
        STATIC
    }

    public DeviceEthernetProfile() {
    }

    public DeviceEthernetProfile(IpAssignment ipAssignment2, String interfaceName2) {
        this.ipAssignment = ipAssignment2;
        this.interfaceName = interfaceName2;
    }

    public IpAssignment getIpAssignment() {
        return this.ipAssignment;
    }

    public void setIpAssignment(IpAssignment ipAssignment2) {
        this.ipAssignment = ipAssignment2;
    }

    public String getInterfaceName() {
        return this.interfaceName;
    }

    public void setInterfaceName(String interfaceName2) {
        this.interfaceName = interfaceName2;
    }

    public String getIpAddress() {
        return this.ipAddress;
    }

    public void setIpAddress(String ipAddress2) {
        this.ipAddress = ipAddress2;
    }

    public String getSubnetMask() {
        return this.subnetMask;
    }

    public void setSubnetMask(String subnetMask2) {
        this.subnetMask = subnetMask2;
    }

    public String getGateway() {
        return this.gateway;
    }

    public void setGateway(String gateway2) {
        this.gateway = gateway2;
    }

    public ArrayList<String> getDnsServers() {
        return this.dnsServers;
    }

    public void setDnsServers(ArrayList<String> dnsServers2) {
        this.dnsServers = dnsServers2;
    }

    public String getProxyHost() {
        return this.proxyHost;
    }

    public void setProxyHost(String proxyHost2) {
        this.proxyHost = proxyHost2;
    }

    public int getProxyPort() {
        return this.proxyPort;
    }

    public void setProxyPort(int proxyPort2) {
        this.proxyPort = proxyPort2;
    }
}
