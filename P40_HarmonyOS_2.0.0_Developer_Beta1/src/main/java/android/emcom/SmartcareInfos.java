package android.emcom;

import android.util.Log;
import com.huawei.android.os.storage.StorageManagerExt;

public class SmartcareInfos {
    private static final byte BYTE_VALUE_DEFAULT = -1;
    private static final String LOG_TAG = "SmartcareInfos";
    private static final long LONG_VALUE_DEFAULT = -1;
    private static final short SHORT_VALUE_DEFAULT = -1;
    private static final byte SUCC_VALUE_DEFAULT = 1;
    private static final int VALUE_DEFAULT = -1;
    public BrowserInfo browserInfo;
    public EmailInfo emailInfo;
    public FwkNetworkInfo fwkNetworkInfo;
    public GameInfo gameInfo;
    public HttpInfo httpInfo;
    public TcpStatusInfo tcpStatusInfo;
    public VideoUploadInfo videoUploadInfo;
    public WechatInfo wechatInfo;

    public static class SmartcareBaseInfo {
        public String pkgName = StorageManagerExt.INVALID_KEY_DESC;
        public SmartcareInfos smarcareInfos;

        public void addToInfos(SmartcareInfos sci) {
            if (sci != null) {
                this.smarcareInfos = sci;
                if (Log.HWINFO) {
                    Log.d(SmartcareInfos.LOG_TAG, "addToInfos is: " + sci + ",this: " + this + ", " + this.smarcareInfos + "," + this.pkgName);
                }
            }
        }

        public void recycle() {
            this.pkgName = StorageManagerExt.INVALID_KEY_DESC;
            if (Log.HWINFO) {
                Log.d("is", "recycle: " + this);
            }
        }
    }

    public static class BrowserInfo extends SmartcareBaseInfo {
        public String appName = StorageManagerExt.INVALID_KEY_DESC;
        public short connectLatency = SmartcareInfos.SHORT_VALUE_DEFAULT;
        public byte connectSuccessFlag = SmartcareInfos.BYTE_VALUE_DEFAULT;
        public short dnsLatency = SmartcareInfos.SHORT_VALUE_DEFAULT;
        public byte dnsSuccessFlag = SmartcareInfos.BYTE_VALUE_DEFAULT;
        public int downloadAvgThput = -1;
        public long pageId = SmartcareInfos.LONG_VALUE_DEFAULT;
        public int pageLatency = -1;
        public boolean result = true;
        public short rspCode = SmartcareInfos.SHORT_VALUE_DEFAULT;

        @Override // android.emcom.SmartcareInfos.SmartcareBaseInfo
        public void addToInfos(SmartcareInfos sci) {
            if (sci != null) {
                super.addToInfos(sci);
                Log.d("is", "BrowserInfo: " + this + ",is: " + sci);
                sci.browserInfo = this;
            }
        }

        public BrowserInfo copyFrom(BrowserInfo bi) {
            if (bi == null) {
                return this;
            }
            this.pageId = bi.pageId;
            this.appName = bi.appName;
            this.pageLatency = bi.pageLatency;
            this.result = bi.result;
            this.rspCode = bi.rspCode;
            this.dnsLatency = bi.dnsLatency;
            this.connectLatency = bi.connectLatency;
            this.dnsSuccessFlag = bi.dnsSuccessFlag;
            this.connectSuccessFlag = bi.connectSuccessFlag;
            this.downloadAvgThput = bi.downloadAvgThput;
            return this;
        }

        @Override // android.emcom.SmartcareInfos.SmartcareBaseInfo
        public void recycle() {
            super.recycle();
            this.pageId = SmartcareInfos.LONG_VALUE_DEFAULT;
            this.appName = StorageManagerExt.INVALID_KEY_DESC;
            this.pageLatency = -1;
            this.result = true;
            this.rspCode = SmartcareInfos.SHORT_VALUE_DEFAULT;
            this.dnsLatency = SmartcareInfos.SHORT_VALUE_DEFAULT;
            this.connectLatency = SmartcareInfos.SHORT_VALUE_DEFAULT;
            this.dnsSuccessFlag = SmartcareInfos.BYTE_VALUE_DEFAULT;
            this.connectSuccessFlag = SmartcareInfos.BYTE_VALUE_DEFAULT;
            this.downloadAvgThput = -1;
        }

        public String toString() {
            return "BrowserInfo:, hash: " + hashCode() + ", pageId: " + this.pageId + ", pageLatency: " + this.pageLatency + ", rspCode: " + ((int) this.rspCode) + ", dnsLatency: " + ((int) this.dnsLatency) + ", connectLatency: " + ((int) this.connectLatency) + ",, dnsSuccessFlag: " + ((int) this.dnsSuccessFlag) + ",, connectSuccessFlag: " + ((int) this.connectSuccessFlag) + ",, downloadAvgThput: " + this.downloadAvgThput + ",";
        }
    }

    public static class WechatInfo extends SmartcareBaseInfo {
        public int endTime = 0;
        public String host;
        public int latancy = -1;
        public int startDate = 0;
        public int startTime = 0;
        public byte successFlag = SmartcareInfos.SUCC_VALUE_DEFAULT;
        public int type = 0;

        @Override // android.emcom.SmartcareInfos.SmartcareBaseInfo
        public void addToInfos(SmartcareInfos sci) {
            if (sci != null) {
                super.addToInfos(sci);
                sci.wechatInfo = this;
            }
        }

        public WechatInfo copyFrom(WechatInfo wci) {
            if (wci == null) {
                return this;
            }
            this.successFlag = wci.successFlag;
            this.latancy = wci.latancy;
            this.type = wci.type;
            this.host = wci.host;
            this.startDate = wci.startDate;
            this.startTime = wci.startTime;
            this.endTime = wci.endTime;
            return this;
        }

        @Override // android.emcom.SmartcareInfos.SmartcareBaseInfo
        public void recycle() {
            super.recycle();
            this.successFlag = SmartcareInfos.SUCC_VALUE_DEFAULT;
            this.latancy = -1;
            this.type = -1;
            this.host = StorageManagerExt.INVALID_KEY_DESC;
            this.startDate = 0;
            this.startTime = 0;
            this.endTime = 0;
        }

        public String toString() {
            return "WechatInfo: , hash: " + hashCode() + ", latancy = " + this.latancy + ",successFlag = " + ((int) this.successFlag) + ",type = " + this.type;
        }
    }

    public static class HttpInfo extends SmartcareBaseInfo {
        public String appName;
        public int endTime;
        public String host;
        public int numStreams;
        public int startDate;
        public int startTime;
        public int uid;

        public HttpInfo copyFrom(HttpInfo hi) {
            if (hi == null) {
                return this;
            }
            this.host = hi.host;
            this.startDate = hi.startDate;
            this.startTime = hi.startTime;
            this.endTime = hi.endTime;
            this.numStreams = hi.numStreams;
            this.uid = hi.uid;
            this.appName = hi.appName;
            return this;
        }

        @Override // android.emcom.SmartcareInfos.SmartcareBaseInfo
        public void recycle() {
            super.recycle();
            this.host = StorageManagerExt.INVALID_KEY_DESC;
            this.startDate = 0;
            this.startTime = 0;
            this.endTime = 0;
            this.numStreams = 0;
            this.uid = 0;
            this.appName = StorageManagerExt.INVALID_KEY_DESC;
        }

        @Override // android.emcom.SmartcareInfos.SmartcareBaseInfo
        public void addToInfos(SmartcareInfos is) {
            if (is != null) {
                super.addToInfos(is);
                is.httpInfo = this;
            }
        }

        public String toString() {
            return "HttpInfo:, hash: " + hashCode() + ", startDate: " + this.startDate + ", startTime: " + this.startTime + ", endTime: " + this.endTime + ",";
        }
    }

    public static class TcpStatusInfo extends SmartcareBaseInfo {
        public int dnsDelay;
        public short synRtrans;
        public int synRtt;
        public short tcpDLWinZeroCount;
        public short tcpDlDisorderPkts;
        public int tcpDlPackages;
        public short tcpDlThreeDupAcks;
        public short tcpULWinZeroCount;
        public short tcpUlFastRetrans;
        public int tcpUlPackages;
        public short tcpUlTimeoutRetrans;

        @Override // android.emcom.SmartcareInfos.SmartcareBaseInfo
        public void addToInfos(SmartcareInfos sci) {
            if (sci != null) {
                super.addToInfos(sci);
                sci.tcpStatusInfo = this;
            }
        }

        public TcpStatusInfo copyFrom(TcpStatusInfo tsi) {
            if (tsi == null) {
                return this;
            }
            this.tcpUlPackages = tsi.tcpUlPackages;
            this.tcpDlPackages = tsi.tcpDlPackages;
            this.synRtrans = tsi.synRtrans;
            this.tcpDLWinZeroCount = tsi.tcpDLWinZeroCount;
            this.tcpUlTimeoutRetrans = tsi.tcpUlTimeoutRetrans;
            this.tcpULWinZeroCount = tsi.tcpULWinZeroCount;
            this.tcpDlThreeDupAcks = tsi.tcpDlThreeDupAcks;
            this.tcpDlDisorderPkts = tsi.tcpDlDisorderPkts;
            this.dnsDelay = tsi.dnsDelay;
            this.synRtt = tsi.synRtt;
            this.tcpUlFastRetrans = tsi.tcpUlFastRetrans;
            return this;
        }

        @Override // android.emcom.SmartcareInfos.SmartcareBaseInfo
        public void recycle() {
            super.recycle();
            this.tcpUlPackages = 0;
            this.tcpDlPackages = 0;
            this.synRtrans = 0;
            this.tcpDLWinZeroCount = 0;
            this.tcpUlTimeoutRetrans = 0;
            this.tcpULWinZeroCount = 0;
            this.tcpDlThreeDupAcks = 0;
            this.tcpDlDisorderPkts = 0;
            this.dnsDelay = 0;
            this.synRtt = 0;
            this.tcpUlFastRetrans = 0;
        }

        public String toString() {
            return "TcpStatusInfo:, hash: " + hashCode() + "dnsDelay: " + this.dnsDelay;
        }
    }

    public static class FwkNetworkInfo extends SmartcareBaseInfo {
        private static final int INVALID_INTEGER_VALUE = -1;
        private int mWifiApCap;
        private int mWifiFrequency;
        private int mWifiMode;
        private int mWifiSecurity;
        public short mcc;
        public short mnc;
        public byte rat;
        public byte rsrp;
        public byte rsrq;
        public int sinr;
        public String timeAndCid;
        public String wlanBssid;
        public byte wlanSignalStrength;
        public String wlanSsid;

        public FwkNetworkInfo copyFrom(FwkNetworkInfo fci) {
            if (fci == null) {
                return this;
            }
            this.mcc = fci.mcc;
            this.mnc = fci.mnc;
            this.rat = fci.rat;
            this.timeAndCid = fci.timeAndCid;
            this.rsrp = fci.rsrp;
            this.rsrq = fci.rsrq;
            this.sinr = fci.sinr;
            this.wlanSignalStrength = fci.wlanSignalStrength;
            this.wlanBssid = fci.wlanBssid;
            this.wlanSsid = fci.wlanSsid;
            setWifiApCap(fci.mWifiApCap);
            setWifiApCap(fci.mWifiMode);
            setWifiFrequency(fci.mWifiFrequency);
            setWifiSecurity(fci.mWifiSecurity);
            return this;
        }

        @Override // android.emcom.SmartcareInfos.SmartcareBaseInfo
        public void addToInfos(SmartcareInfos sci) {
            if (sci != null) {
                super.addToInfos(sci);
                sci.fwkNetworkInfo = this;
            }
        }

        @Override // android.emcom.SmartcareInfos.SmartcareBaseInfo
        public void recycle() {
            super.recycle();
            this.mcc = 0;
            this.mnc = 0;
            this.rat = 0;
            this.timeAndCid = StorageManagerExt.INVALID_KEY_DESC;
            this.rsrp = SmartcareInfos.BYTE_VALUE_DEFAULT;
            this.rsrq = SmartcareInfos.BYTE_VALUE_DEFAULT;
            this.sinr = -1;
            this.wlanSignalStrength = SmartcareInfos.BYTE_VALUE_DEFAULT;
            this.wlanBssid = StorageManagerExt.INVALID_KEY_DESC;
            this.wlanSsid = StorageManagerExt.INVALID_KEY_DESC;
            this.mWifiApCap = -1;
            this.mWifiMode = -1;
            this.mWifiFrequency = -1;
            this.mWifiSecurity = -1;
        }

        public String toString() {
            return "FwkNetworkInfo:, hash: " + hashCode() + ", rat: " + ((int) this.rat) + ", rsrp: " + ((int) this.rsrp) + ", rsrq: " + ((int) this.rsrq) + ", sinr: " + this.sinr + ", wlanSignalStrength: " + ((int) this.wlanSignalStrength) + ", mWifiApCap: " + this.mWifiApCap + ", mWifiMode: " + this.mWifiMode + ", mWifiFrequency: " + this.mWifiFrequency + ", mWifiSecurity: " + this.mWifiSecurity + ",";
        }

        public void setWifiApCap(int wifiApCap) {
            this.mWifiApCap = wifiApCap;
        }

        public int getWifiApCap() {
            return this.mWifiApCap;
        }

        public void setWifiMode(int wifiMode) {
            this.mWifiMode = wifiMode;
        }

        public int getWifiMode() {
            return this.mWifiMode;
        }

        public void setWifiFrequency(int wifiFrequency) {
            this.mWifiFrequency = wifiFrequency;
        }

        public int getWifiFrequency() {
            return this.mWifiFrequency;
        }

        public void setWifiSecurity(int wifiSecurity) {
            this.mWifiSecurity = wifiSecurity;
        }

        public int getWifiSecurity() {
            return this.mWifiSecurity;
        }
    }

    public void recycle() {
        FwkNetworkInfo fwkNetworkInfo2 = this.fwkNetworkInfo;
        if (fwkNetworkInfo2 != null) {
            fwkNetworkInfo2.recycle();
        }
        GameInfo gameInfo2 = this.gameInfo;
        if (gameInfo2 != null) {
            gameInfo2.recycle();
        }
        VideoUploadInfo videoUploadInfo2 = this.videoUploadInfo;
        if (videoUploadInfo2 != null) {
            videoUploadInfo2.recycle();
        }
        EmailInfo emailInfo2 = this.emailInfo;
        if (emailInfo2 != null) {
            emailInfo2.recycle();
        }
        WechatInfo wechatInfo2 = this.wechatInfo;
        if (wechatInfo2 != null) {
            wechatInfo2.recycle();
        }
        BrowserInfo browserInfo2 = this.browserInfo;
        if (browserInfo2 != null) {
            browserInfo2.recycle();
        }
        HttpInfo httpInfo2 = this.httpInfo;
        if (httpInfo2 != null) {
            httpInfo2.recycle();
        }
        TcpStatusInfo tcpStatusInfo2 = this.tcpStatusInfo;
        if (tcpStatusInfo2 != null) {
            tcpStatusInfo2.recycle();
        }
    }

    public SmartcareInfos copyFrom(SmartcareInfos sci) {
        if (sci == null) {
            return this;
        }
        if (sci.browserInfo != null) {
            if (this.browserInfo == null) {
                new BrowserInfo().addToInfos(this);
            }
            this.browserInfo.copyFrom(sci.browserInfo);
        }
        if (sci.httpInfo != null) {
            if (this.httpInfo == null) {
                new HttpInfo().addToInfos(this);
            }
            this.httpInfo.copyFrom(sci.httpInfo);
        }
        if (sci.tcpStatusInfo != null) {
            if (this.tcpStatusInfo == null) {
                new TcpStatusInfo().addToInfos(this);
            }
            this.tcpStatusInfo.copyFrom(sci.tcpStatusInfo);
        }
        if (sci.fwkNetworkInfo != null) {
            if (this.fwkNetworkInfo == null) {
                new FwkNetworkInfo().addToInfos(this);
            }
            this.fwkNetworkInfo.copyFrom(sci.fwkNetworkInfo);
        }
        return this;
    }

    public String toString() {
        return "SmartcareInfos:  hash: " + hashCode() + " gameInfo: " + this.gameInfo + " browserInfo: " + this.browserInfo + " videoUploadInfo: " + this.videoUploadInfo + " emailInfo: " + this.emailInfo + " httpInfo: " + this.httpInfo + " fwkNetworkInfo: " + this.fwkNetworkInfo + " tcpStatusInfo: " + this.tcpStatusInfo;
    }
}
