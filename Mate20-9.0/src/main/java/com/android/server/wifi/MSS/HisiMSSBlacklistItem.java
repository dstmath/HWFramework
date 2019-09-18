package com.android.server.wifi.MSS;

import com.android.server.wifi.HwQoE.HwQoEUtils;
import java.util.ArrayList;
import java.util.List;

public class HisiMSSBlacklistItem {
    public static final int ETH_ALEN = 6;
    public static final int ITEM_SIZE = 8;
    public static final int MAX_BLACK_ITEM_NUM = 16;
    private static final String TAG = "HisiMSSBlacklistItem";
    public byte actionType;
    public byte recv;
    private String ssid;
    public byte[] userMacAddr;

    public HisiMSSBlacklistItem(String ssid2, String bssid, int actiontype) {
        this.recv = 0;
        parseMacString(bssid);
        this.actionType = (byte) actiontype;
        this.ssid = ssid2 == null ? "" : ssid2;
    }

    public HisiMSSBlacklistItem(byte[] macaddr, byte actiontype) {
        this.recv = 0;
        this.ssid = "";
        if (macaddr.length == 6) {
            this.userMacAddr = new byte[6];
            System.arraycopy(macaddr, 0, this.userMacAddr, 0, 6);
        } else {
            this.userMacAddr = new byte[0];
        }
        this.actionType = actiontype;
    }

    private void parseMacString(String strmac) {
        if (strmac != null) {
            String[] strAddrItems = strmac.split(":");
            if (strAddrItems.length == 6) {
                this.userMacAddr = new byte[6];
                int i = 0;
                while (i < strAddrItems.length) {
                    try {
                        this.userMacAddr[i] = (byte) Integer.parseInt(strAddrItems[i], 16);
                        i++;
                    } catch (NumberFormatException e) {
                        e.printStackTrace();
                    }
                }
                return;
            }
        }
        this.userMacAddr = new byte[0];
    }

    public boolean isDataValid() {
        return this.userMacAddr != null && this.userMacAddr.length == 6;
    }

    public static List<HisiMSSBlacklistItem> parse(List<HwMSSDatabaseItem> items) {
        List<HisiMSSBlacklistItem> lists = new ArrayList<>();
        for (HwMSSDatabaseItem item : items) {
            if (item != null) {
                HisiMSSBlacklistItem blackitem = new HisiMSSBlacklistItem(item.ssid, item.bssid, item.reasoncode);
                if (blackitem.isDataValid()) {
                    lists.add(blackitem);
                }
            }
        }
        return lists;
    }

    public byte[] toByteArray() {
        if (this.userMacAddr.length != 6) {
            return new byte[0];
        }
        byte[] buff = new byte[8];
        System.arraycopy(this.userMacAddr, 0, buff, 0, 6);
        buff[6] = this.actionType;
        buff[7] = this.recv;
        return buff;
    }

    public static byte[] toByteArray(List<HisiMSSBlacklistItem> list) {
        int valididx = 0;
        byte[] tmpbytes = new byte[HwQoEUtils.QOE_MSG_GAME_TIMEOUT];
        for (HisiMSSBlacklistItem item : list) {
            byte[] bytes = item.toByteArray();
            if (bytes.length == 8) {
                System.arraycopy(bytes, 0, tmpbytes, valididx * 8, 8);
                valididx++;
                if (valididx >= 16) {
                    break;
                }
            }
        }
        byte[] totalBytes = new byte[((valididx * 8) + 1)];
        totalBytes[0] = (byte) valididx;
        System.arraycopy(tmpbytes, 0, totalBytes, 1, valididx * 8);
        return totalBytes;
    }

    public String toString() {
        return "ssid:" + this.ssid + ",bssid:" + HwMSSUtils.parseMaskMacBytes(this.userMacAddr) + ",type" + this.actionType;
    }
}
