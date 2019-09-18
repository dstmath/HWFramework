package com.android.server.hidata.appqoe;

public class HwAPPChrExcpInfo {
    public int netType = -1;
    public int para1 = -1;
    public int para2 = -1;
    public int para3 = -1;
    public int para4 = -1;
    public int rsPacket = -1;
    public int rssi = -1;
    public int rtt = -1;
    public int rxByte = -1;
    public int rxPacket = -1;
    public int txByte = -1;
    public int txPacket = -1;

    public String toString() {
        return " HwAPPChrExcpInfo netType = " + this.netType + " rssi = " + this.rssi + " rtt = " + this.rtt + " txPacket = " + this.txPacket + " txByte = " + this.txByte + " rxPacket = " + this.rxPacket + " rxByte = " + this.rxByte + " rsPacket = " + this.rsPacket + " para1 = " + this.para1 + " para2 = " + this.para2 + " para3 = " + this.para3 + " para4 = " + this.para4;
    }
}
