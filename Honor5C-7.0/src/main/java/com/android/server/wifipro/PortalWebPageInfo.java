package com.android.server.wifipro;

import com.android.server.rms.iaware.hiber.constant.AppHibernateCst;
import java.util.Map;

public class PortalWebPageInfo {
    public String bssid;
    public String cellid;
    public String htmlBtnNumber;
    public String loginBtnId;
    public String loginBtnName;
    public String loginBtnValue;
    public String loginNodeType;
    public String phoneNumInputId;
    public String phoneNumInputName;
    public String smsPwInputId;
    public String smsPwInputName;
    public String smsPwInputValue;
    public String sndBtnId;
    public String ssid;
    public boolean updateOnly;
    public String url;

    public PortalWebPageInfo() {
        this.ssid = AppHibernateCst.INVALID_PKG;
        this.url = AppHibernateCst.INVALID_PKG;
        this.phoneNumInputId = AppHibernateCst.INVALID_PKG;
        this.phoneNumInputName = AppHibernateCst.INVALID_PKG;
        this.smsPwInputId = AppHibernateCst.INVALID_PKG;
        this.smsPwInputName = AppHibernateCst.INVALID_PKG;
        this.smsPwInputValue = AppHibernateCst.INVALID_PKG;
        this.htmlBtnNumber = "-1";
        this.bssid = AppHibernateCst.INVALID_PKG;
        this.cellid = "-1";
        this.sndBtnId = AppHibernateCst.INVALID_PKG;
        this.loginBtnId = AppHibernateCst.INVALID_PKG;
        this.loginBtnName = AppHibernateCst.INVALID_PKG;
        this.loginBtnValue = AppHibernateCst.INVALID_PKG;
        this.loginNodeType = AppHibernateCst.INVALID_PKG;
        this.updateOnly = false;
    }

    public PortalWebPageInfo(String ssid, String url, String phoneNumInputId, String smsPwInputId) {
        this.ssid = ssid;
        this.url = url;
        this.phoneNumInputId = phoneNumInputId;
        this.phoneNumInputName = AppHibernateCst.INVALID_PKG;
        this.smsPwInputId = smsPwInputId;
        this.smsPwInputName = AppHibernateCst.INVALID_PKG;
        this.smsPwInputValue = AppHibernateCst.INVALID_PKG;
        this.htmlBtnNumber = "-1";
        this.bssid = AppHibernateCst.INVALID_PKG;
        this.cellid = "-1";
        this.sndBtnId = AppHibernateCst.INVALID_PKG;
        this.loginBtnId = AppHibernateCst.INVALID_PKG;
        this.loginBtnName = AppHibernateCst.INVALID_PKG;
        this.loginBtnValue = AppHibernateCst.INVALID_PKG;
        this.loginNodeType = AppHibernateCst.INVALID_PKG;
        this.updateOnly = false;
    }

    public PortalWebPageInfo(String ssid, String url, String phoneNumInputId, String smsPwInputId, String bssid, String cellid, String sndBtnId, String loginBtnId, String htmlBtnNumber) {
        this.ssid = ssid;
        this.url = url;
        this.phoneNumInputId = phoneNumInputId;
        this.smsPwInputId = smsPwInputId;
        this.bssid = bssid;
        this.cellid = cellid;
        this.sndBtnId = sndBtnId;
        this.loginBtnId = loginBtnId;
        this.htmlBtnNumber = htmlBtnNumber;
        this.updateOnly = false;
    }

    public static PortalWebPageInfo createPortalInfo(Map<String, String> items) {
        if (items != null) {
            return new PortalWebPageInfo((String) items.get(WifiProCommonDefs.AP_SSID), (String) items.get(WifiProCommonDefs.AP_URL), (String) items.get(WifiProCommonDefs.AP_Phone_Number_ID), (String) items.get(WifiProCommonDefs.AP_Code_Input_ID), (String) items.get(WifiProCommonDefs.AP_BSSID), (String) items.get(WifiProCommonDefs.AP_CELLID), (String) items.get(WifiProCommonDefs.AP_Send_Button_ID), (String) items.get(WifiProCommonDefs.AP_Login_Button_ID), (String) items.get(WifiProCommonDefs.AP_BTN_NUM));
        }
        return null;
    }

    public String toString() {
        return "ssid=" + this.ssid + ", url=" + this.url + ", phoneNumInputId=" + this.phoneNumInputId + ", phoneNumInputName=" + this.phoneNumInputName + ", smsPwInputId=" + this.smsPwInputId + ", smsPwInputName=" + this.smsPwInputName + ", htmlBtnNumber=" + this.htmlBtnNumber + ", bssid=" + this.bssid + ", cellid=" + this.cellid + ", sndBtnId=" + this.sndBtnId + ", loginBtnId=" + this.loginBtnId + ", updateOnly = " + this.updateOnly;
    }
}
