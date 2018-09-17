package android.emcom;

import android.util.Log;

public class SmartcareInfos {
    private static final String LOG_TAG = "SmartcareInfos";
    public BrowserInfo browserInfo;
    public EmailInfo emailInfo;
    public FwkNetworkInfo fwkNetworkInfo;
    public HttpInfo httpInfo;
    public TcpStatusInfo tcpStatusInfo;
    public VideoInfo videoInfo;
    public WechatInfo wechatInfo;

    public static class SmartcareBaseInfo {
        public String pkgName = "";
        public SmartcareInfos smarcareInfos;

        public SmartcareBaseInfo() {
            if (Log.HWINFO) {
                Log.d(SmartcareInfos.LOG_TAG, "construct<>: " + this);
            }
        }

        public void addToInfos(SmartcareInfos is) {
            this.smarcareInfos = is;
            if (Log.HWINFO) {
                Log.d(SmartcareInfos.LOG_TAG, "addToInfos is: " + is + ",this: " + this + ", " + this.smarcareInfos + "," + this.pkgName);
            }
        }

        public void recycle() {
            if (Log.HWINFO) {
                Log.d("is", "recycle: " + this);
            }
        }
    }

    public static class BrowserInfo extends SmartcareBaseInfo {
        public String appName;
        public int name;
        public long pageId;
        public int pageLatency;
        public boolean result;
        public short rspCode;

        public void addToInfos(SmartcareInfos is) {
            super.addToInfos(is);
            Log.d("is", "BrowserInfo: " + this + ",is: " + is);
            is.browserInfo = this;
        }

        public BrowserInfo copyFrom(BrowserInfo wci) {
            this.name = wci.name;
            this.pageId = wci.pageId;
            this.appName = wci.appName;
            this.pageLatency = wci.pageLatency;
            this.result = wci.result;
            this.rspCode = wci.rspCode;
            return this;
        }

        public void recycle() {
            super.recycle();
            this.name = 0;
            this.pageId = -1;
            this.appName = null;
            this.pageLatency = -1;
            this.result = false;
            this.rspCode = (short) -1;
        }
    }

    public static class FwkNetworkInfo extends SmartcareBaseInfo {
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
            return this;
        }

        public void addToInfos(SmartcareInfos is) {
            super.addToInfos(is);
            is.fwkNetworkInfo = this;
        }

        public void recycle() {
            super.recycle();
            this.mcc = (short) 0;
            this.mnc = (short) 0;
            this.rat = (byte) 0;
            this.timeAndCid = null;
            this.rsrp = (byte) -1;
            this.rsrq = (byte) -1;
            this.sinr = -1;
            this.wlanSignalStrength = (byte) -1;
            this.wlanBssid = null;
            this.wlanSsid = null;
        }

        public String toString() {
            return "FwkNetworkInfo:, hash: " + hashCode();
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

        public HttpInfo copyFrom(HttpInfo sci) {
            this.host = sci.host;
            this.startDate = sci.startDate;
            this.startTime = sci.startTime;
            this.endTime = sci.endTime;
            this.numStreams = sci.numStreams;
            this.uid = sci.uid;
            this.appName = sci.appName;
            return this;
        }

        public void recycle() {
            super.recycle();
            this.host = null;
            this.startDate = 0;
            this.startTime = 0;
            this.endTime = 0;
            this.numStreams = 0;
            this.uid = 0;
            this.appName = null;
        }

        public void addToInfos(SmartcareInfos is) {
            super.addToInfos(is);
            is.httpInfo = this;
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

        public void addToInfos(SmartcareInfos is) {
            super.addToInfos(is);
            is.tcpStatusInfo = this;
        }

        public TcpStatusInfo copyFrom(TcpStatusInfo tsi) {
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

        public void recycle() {
            super.recycle();
            this.tcpUlPackages = 0;
            this.tcpDlPackages = 0;
            this.synRtrans = (short) 0;
            this.tcpDLWinZeroCount = (short) 0;
            this.tcpUlTimeoutRetrans = (short) 0;
            this.tcpULWinZeroCount = (short) 0;
            this.tcpDlThreeDupAcks = (short) 0;
            this.tcpDlDisorderPkts = (short) 0;
            this.dnsDelay = 0;
            this.synRtt = 0;
            this.tcpUlFastRetrans = (short) 0;
        }

        public String toString() {
            return "TcpStatusInfo:, hash: " + hashCode() + "dnsDelay: " + this.dnsDelay;
        }
    }

    public static class WechatInfo extends SmartcareBaseInfo {
        public int endTime = 0;
        public String host;
        public int latancy = -1;
        public int startDate = 0;
        public int startTime = 0;
        public byte successFlag = (byte) 1;
        public int type = 0;

        public void addToInfos(SmartcareInfos is) {
            super.addToInfos(is);
            is.wechatInfo = this;
        }

        public WechatInfo copyFrom(WechatInfo wci) {
            this.successFlag = wci.successFlag;
            this.latancy = wci.latancy;
            this.type = wci.type;
            this.host = wci.host;
            this.startDate = wci.startDate;
            this.startTime = wci.startTime;
            this.endTime = wci.endTime;
            return this;
        }

        public void recycle() {
            super.recycle();
            this.successFlag = (byte) 1;
            this.latancy = -1;
            this.type = -1;
            this.host = "";
            this.startDate = 0;
            this.startTime = 0;
            this.endTime = 0;
        }

        public String toString() {
            return "WechatInfo: , hash: " + hashCode() + ", latancy = " + this.latancy + ",successFlag = " + this.successFlag + ",type = " + this.type;
        }
    }

    public void recycle() {
        if (this.fwkNetworkInfo != null) {
            this.fwkNetworkInfo.recycle();
        }
        if (this.videoInfo != null) {
            this.videoInfo.recycle();
        }
        if (this.emailInfo != null) {
            this.emailInfo.recycle();
        }
        if (this.wechatInfo != null) {
            this.wechatInfo.recycle();
        }
        if (this.browserInfo != null) {
            this.browserInfo.recycle();
        }
        if (this.httpInfo != null) {
            this.httpInfo.recycle();
        }
        if (this.tcpStatusInfo != null) {
            this.tcpStatusInfo.recycle();
        }
    }

    public SmartcareInfos copyFrom(SmartcareInfos is) {
        if (is.browserInfo != null) {
            if (this.browserInfo == null) {
                new BrowserInfo().addToInfos(this);
            }
            this.browserInfo.copyFrom(is.browserInfo);
        }
        if (is.httpInfo != null) {
            if (this.httpInfo == null) {
                new HttpInfo().addToInfos(this);
            }
            this.httpInfo.copyFrom(is.httpInfo);
        }
        if (is.tcpStatusInfo != null) {
            if (this.tcpStatusInfo == null) {
                new TcpStatusInfo().addToInfos(this);
            }
            this.tcpStatusInfo.copyFrom(is.tcpStatusInfo);
        }
        if (is.fwkNetworkInfo != null) {
            if (this.fwkNetworkInfo == null) {
                new FwkNetworkInfo().addToInfos(this);
            }
            this.fwkNetworkInfo.copyFrom(is.fwkNetworkInfo);
        }
        return this;
    }

    public String toString() {
        return "SmartcareInfos:  hash: " + hashCode() + " browserInfo: " + this.browserInfo + " videoInfo: " + this.videoInfo + " emailInfo: " + this.emailInfo + " httpInfo: " + this.httpInfo + " fwkNetworkInfo: " + this.fwkNetworkInfo + " tcpStatusInfo: " + this.tcpStatusInfo;
    }
}
