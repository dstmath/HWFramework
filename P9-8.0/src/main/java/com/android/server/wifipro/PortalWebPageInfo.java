package com.android.server.wifipro;

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
        this.ssid = "";
        this.url = "";
        this.phoneNumInputId = "";
        this.phoneNumInputName = "";
        this.smsPwInputId = "";
        this.smsPwInputName = "";
        this.smsPwInputValue = "";
        this.htmlBtnNumber = "-1";
        this.bssid = "";
        this.cellid = "-1";
        this.sndBtnId = "";
        this.loginBtnId = "";
        this.loginBtnName = "";
        this.loginBtnValue = "";
        this.loginNodeType = "";
        this.updateOnly = false;
    }

    public PortalWebPageInfo(String ssid, String url, String phoneNumInputId, String smsPwInputId) {
        this.ssid = ssid;
        this.url = url;
        this.phoneNumInputId = phoneNumInputId;
        this.phoneNumInputName = "";
        this.smsPwInputId = smsPwInputId;
        this.smsPwInputName = "";
        this.smsPwInputValue = "";
        this.htmlBtnNumber = "-1";
        this.bssid = "";
        this.cellid = "-1";
        this.sndBtnId = "";
        this.loginBtnId = "";
        this.loginBtnName = "";
        this.loginBtnValue = "";
        this.loginNodeType = "";
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
        return "ssid=" + this.ssid + ", url=" + this.url + ", phoneNumInputId=" + this.phoneNumInputId + ", phoneNumInputName=" + this.phoneNumInputName + ", smsPwInputId=" + this.smsPwInputId + ", smsPwInputName=" + this.smsPwInputName + ", htmlBtnNumber=" + this.htmlBtnNumber + ", bssid= ***" + ", cellid=" + this.cellid + ", sndBtnId=" + this.sndBtnId + ", loginBtnId=" + this.loginBtnId + ", updateOnly = " + this.updateOnly;
    }
}
