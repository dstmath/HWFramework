package com.android.server.wifi.MSS;

import android.util.wifi.HwHiLog;
import com.android.server.wifi.hwUtil.StringUtilEx;
import java.util.ArrayList;
import java.util.List;

public class HisiMssBlackListItem {
    public static final int ETH_ALEN = 6;
    public static final int ITEM_SIZE = 8;
    public static final int MAX_BLACK_ITEM_NUM = 16;
    private static final String TAG = "HisiMssBlackListItem";
    protected byte actionType;
    protected byte recv;
    private String ssid;
    protected byte[] userMacAddr;

    public HisiMssBlackListItem(String ssid2, String bssid, int actiontype) {
        this.recv = 0;
        parseMacString(bssid);
        this.actionType = (byte) actiontype;
        this.ssid = ssid2 == null ? "" : ssid2;
    }

    public HisiMssBlackListItem(byte[] macAddr, byte actiontype) {
        this.recv = 0;
        this.ssid = "";
        if (macAddr.length == 6) {
            this.userMacAddr = new byte[6];
            System.arraycopy(macAddr, 0, this.userMacAddr, 0, 6);
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
                for (int i = 0; i < strAddrItems.length; i++) {
                    try {
                        this.userMacAddr[i] = (byte) Integer.parseInt(strAddrItems[i], 16);
                    } catch (NumberFormatException e) {
                        HwHiLog.e(TAG, false, "parseMacString failed", new Object[0]);
                    }
                }
                return;
            }
        }
        this.userMacAddr = new byte[0];
    }

    public boolean isDataValid() {
        byte[] bArr = this.userMacAddr;
        return bArr != null && bArr.length == 6;
    }

    public static List<HisiMssBlackListItem> parse(List<HwMssDatabaseItem> items) {
        List<HisiMssBlackListItem> lists = new ArrayList<>();
        for (HwMssDatabaseItem item : items) {
            if (item != null) {
                HisiMssBlackListItem blackitem = new HisiMssBlackListItem(item.ssid, item.bssid, item.reasonCode);
                if (blackitem.isDataValid()) {
                    lists.add(blackitem);
                }
            }
        }
        return lists;
    }

    public byte[] toByteArray() {
        byte[] bArr = this.userMacAddr;
        if (bArr.length != 6) {
            return new byte[0];
        }
        byte[] buff = new byte[8];
        System.arraycopy(bArr, 0, buff, 0, 6);
        buff[6] = this.actionType;
        buff[7] = this.recv;
        return buff;
    }

    public static byte[] toByteArray(List<HisiMssBlackListItem> list) {
        int validIdx = 0;
        byte[] tmpBytes = new byte[128];
        for (HisiMssBlackListItem item : list) {
            byte[] bytes = item.toByteArray();
            if (bytes.length == 8) {
                System.arraycopy(bytes, 0, tmpBytes, validIdx * 8, 8);
                validIdx++;
                if (validIdx >= 16) {
                    break;
                }
            }
        }
        byte[] totalBytes = new byte[((validIdx * 8) + 1)];
        totalBytes[0] = (byte) validIdx;
        System.arraycopy(tmpBytes, 0, totalBytes, 1, validIdx * 8);
        return totalBytes;
    }

    public String toString() {
        return "ssid:" + StringUtilEx.safeDisplaySsid(this.ssid) + ",bssid:" + HwMssUtils.parseMaskMacBytes(this.userMacAddr) + ",type" + ((int) this.actionType);
    }
}
