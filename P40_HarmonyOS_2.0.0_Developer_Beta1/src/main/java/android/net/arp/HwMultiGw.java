package android.net.arp;

import android.text.TextUtils;
import android.util.wifi.HwHiLog;
import com.huawei.android.os.storage.StorageManagerExt;
import java.util.ArrayList;
import java.util.Locale;

public class HwMultiGw {
    private static final int MAC_ADDR_LENGTH = 6;
    private static final String TAG = "HwMultiGw";
    private int mCurrentIdx = 0;
    private String mGwIpAddr = null;
    private ArrayList<String> mMacAddrList = new ArrayList<>();
    private long mRttArp = -1;

    public int getGwNum() {
        return this.mMacAddrList.size();
    }

    public void setGwIpAddr(String gateway) {
        HwHiLog.d(TAG, false, "setGateWay %{private}s", gateway);
        if (!TextUtils.isEmpty(gateway)) {
            this.mGwIpAddr = gateway;
        }
    }

    public String getGwIpAddr() {
        return this.mGwIpAddr;
    }

    public void setGwMacAddr(byte[] addr) {
        String macAddress = macByte2String(addr);
        if (!TextUtils.isEmpty(macAddress) && !isMacDuplicated(macAddress)) {
            this.mMacAddrList.add(macAddress);
        }
    }

    public void setGwMacAddr(String addr) {
        if (!TextUtils.isEmpty(addr) && !isMacDuplicated(addr)) {
            this.mMacAddrList.add(addr);
        }
    }

    public ArrayList<String> getGwMacAddrList() {
        return this.mMacAddrList;
    }

    public String getGwMacAddr() {
        ArrayList<String> arrayList = this.mMacAddrList;
        if (arrayList == null || arrayList.isEmpty()) {
            return null;
        }
        return this.mMacAddrList.get(0);
    }

    public String getNextGwMacAddr() {
        if (this.mCurrentIdx >= this.mMacAddrList.size()) {
            return null;
        }
        String macAddr = this.mMacAddrList.get(this.mCurrentIdx);
        this.mCurrentIdx++;
        return macAddr;
    }

    public void clearGw() {
        this.mCurrentIdx = 0;
        this.mGwIpAddr = null;
        this.mMacAddrList.clear();
    }

    private boolean isMacDuplicated(String addr) {
        int listSize = this.mMacAddrList.size();
        for (int i = 0; i < listSize; i++) {
            if (addr.equals(this.mMacAddrList.get(i))) {
                return true;
            }
        }
        return false;
    }

    private String macByte2String(byte[] macByte) {
        if (macByte == null || macByte.length != 6) {
            return StorageManagerExt.INVALID_KEY_DESC;
        }
        return String.format(Locale.ENGLISH, "%02x:%02x:%02x:%02x:%02x:%02x", Byte.valueOf(macByte[0]), Byte.valueOf(macByte[1]), Byte.valueOf(macByte[2]), Byte.valueOf(macByte[3]), Byte.valueOf(macByte[4]), Byte.valueOf(macByte[5]));
    }

    public long getArpRtt() {
        return this.mRttArp;
    }

    public void setArpRtt(long rtt) {
        this.mRttArp = rtt;
    }
}
