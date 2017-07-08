package android.net.arp;

import android.text.TextUtils;
import android.util.Log;
import java.util.ArrayList;

public class HWMultiGW {
    private static final boolean DBG = true;
    private static final int MAC_ADDR_LENGTH = 6;
    private static final String TAG = "HWMultiGW";
    private int mCurrentIdx;
    private String mGWIpAddr;
    private ArrayList<String> mMACAddrList;
    private long mRttArp;

    public HWMultiGW() {
        this.mCurrentIdx = 0;
        this.mGWIpAddr = null;
        this.mRttArp = -1;
        this.mMACAddrList = new ArrayList();
    }

    public int getGWNum() {
        return this.mMACAddrList.size();
    }

    public void setGWIPAddr(String gateway) {
        Log.d(TAG, "setGateWay " + gateway);
        if (!TextUtils.isEmpty(gateway)) {
            this.mGWIpAddr = gateway;
        }
    }

    public String getGWIPAddr() {
        return this.mGWIpAddr;
    }

    public void setGWMACAddr(byte[] addr) {
        String strMACAddr = macByte2String(addr);
        if (!TextUtils.isEmpty(strMACAddr) && !isMACDuplicated(strMACAddr)) {
            this.mMACAddrList.add(strMACAddr);
        }
    }

    public void setGWMACAddr(String addr) {
        if (!TextUtils.isEmpty(addr) && !isMACDuplicated(addr)) {
            this.mMACAddrList.add(addr);
        }
    }

    public ArrayList<String> getGWMACAddrList() {
        return this.mMACAddrList;
    }

    public String getGWMACAddr() {
        if (this.mMACAddrList.size() > 0) {
            return (String) this.mMACAddrList.get(0);
        }
        return null;
    }

    public String getNextGWMACAddr() {
        if (this.mCurrentIdx >= this.mMACAddrList.size()) {
            return null;
        }
        String macAddr = (String) this.mMACAddrList.get(this.mCurrentIdx);
        this.mCurrentIdx++;
        return macAddr;
    }

    public void clearGW() {
        this.mCurrentIdx = 0;
        this.mGWIpAddr = null;
        this.mMACAddrList.clear();
    }

    private boolean isMACDuplicated(String addr) {
        for (int i = 0; i < this.mMACAddrList.size(); i++) {
            if (addr.equals(this.mMACAddrList.get(i))) {
                return DBG;
            }
        }
        return false;
    }

    private String macByte2String(byte[] macByte) {
        if (macByte == null || macByte.length != MAC_ADDR_LENGTH) {
            return "";
        }
        Object[] objArr = new Object[MAC_ADDR_LENGTH];
        objArr[0] = Byte.valueOf(macByte[0]);
        objArr[1] = Byte.valueOf(macByte[1]);
        objArr[2] = Byte.valueOf(macByte[2]);
        objArr[3] = Byte.valueOf(macByte[3]);
        objArr[4] = Byte.valueOf(macByte[4]);
        objArr[5] = Byte.valueOf(macByte[5]);
        return String.format("%02x:%02x:%02x:%02x:%02x:%02x", objArr);
    }

    public long getArpRTT() {
        return this.mRttArp;
    }

    public void setArpRTT(long rtt) {
        this.mRttArp = rtt;
    }
}
