package com.android.server.wifi.wifipro;

import android.content.Intent;
import android.os.Bundle;
import android.telephony.SmsMessage;
import android.text.TextUtils;
import android.util.Log;
import android.util.SparseArray;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

public class WiFiProAuthCodeParser {
    private static final String AUTH_CODE_EXCLUSIVE_REGEX = "\\d{7,}";
    private static final String AUTH_CODE_NECESSARY_REGEX_1 = "\\d{4,6}";
    private static final String AUTH_CODE_NECESSARY_REGEX_2 = "([A-Z]|[a-z]|\\d){3}";
    private static final String AUTH_SMS_NUM_BEGIN = "106";
    private static final int AUTH_SMS_NUM_MIN_LEN = 5;
    private static final boolean DBG = true;
    private static final boolean DDBG = false;
    private static final String FIGURE_REGEX = "\\d{4,}";
    private static final String REGEX_1_DATE_YYYY_MM_DD = "\\d{4}-\\d{1,2}-\\d{1,2}";
    private static final String REGEX_2_DATE_YYYY_MM_DD = "\\d{4}[/]\\d{1,2}[/]\\d{1,2}";
    private static final boolean SAMPLES_COLLECT_CHR = true;
    private static final String TAG = "WiFi_PRO_AuthCodeParser";
    private String mAuthSmsBodyKeyWordRegex;
    private String mDateRegex3;
    private Map<String, String> mParserRegexMapLists;
    private WifiProConfigurationManager mWifiProConfigurationManager;

    public WiFiProAuthCodeParser(WifiProConfigurationManager config) {
        this.mWifiProConfigurationManager = config;
        initRegex();
    }

    private void initRegex() {
        this.mAuthSmsBodyKeyWordRegex = "";
        this.mDateRegex3 = "";
        this.mParserRegexMapLists = this.mWifiProConfigurationManager.getRegexMapLis();
        if (this.mParserRegexMapLists != null) {
            if (this.mParserRegexMapLists.containsKey(WifiproUtils.SMS_BODY_OPT)) {
                this.mAuthSmsBodyKeyWordRegex = (String) this.mParserRegexMapLists.get(WifiproUtils.SMS_BODY_OPT);
            }
            if (this.mParserRegexMapLists.containsKey(WifiproUtils.CODE_EXCLUSIVE_DATE_3)) {
                this.mDateRegex3 = (String) this.mParserRegexMapLists.get(WifiproUtils.CODE_EXCLUSIVE_DATE_3);
            }
        }
    }

    public boolean isPortalAuthSms(String sms_num) {
        if (sms_num != null && sms_num.length() >= 5) {
            return true;
        }
        logBug("is not auth_Sms: sms_num =" + sms_num);
        return false;
    }

    private boolean isPortalAuthSmsbody(String sms_body) {
        if (TextUtils.isEmpty(sms_body) || TextUtils.isEmpty(this.mAuthSmsBodyKeyWordRegex)) {
            return false;
        }
        try {
            if (Pattern.compile(this.mAuthSmsBodyKeyWordRegex).matcher(sms_body).find()) {
                logD("receive msg: not portal auth sms");
                return true;
            }
        } catch (PatternSyntaxException e) {
            logBug("PatternSyntaxException");
        }
        logD("receive msg: is not portal auth sms");
        return false;
    }

    private String parsePortalAuthCode(String sms_body, String regulation) {
        if (TextUtils.isEmpty(sms_body) || TextUtils.isEmpty(regulation)) {
            return null;
        }
        String auth_code = null;
        Pattern pattern_code = null;
        try {
            sms_body = sms_body.replaceAll(AUTH_CODE_EXCLUSIVE_REGEX, "").replaceAll(REGEX_1_DATE_YYYY_MM_DD, "").replaceAll(REGEX_2_DATE_YYYY_MM_DD, "");
            if (this.mDateRegex3.length() > 0) {
                sms_body = sms_body.replaceAll(this.mDateRegex3, "");
            }
            pattern_code = Pattern.compile(regulation);
        } catch (PatternSyntaxException e) {
        }
        if (pattern_code == null) {
            return null;
        }
        SparseArray<String> authCodeRecords = new SparseArray();
        List<Integer> authCodeIndexList = new ArrayList();
        Matcher matcher_code = pattern_code.matcher(sms_body);
        while (matcher_code.find()) {
            auth_code = matcher_code.group(0);
            int index = matcher_code.start();
            authCodeRecords.put(index, auth_code);
            authCodeIndexList.add(Integer.valueOf(index));
        }
        if (authCodeRecords.size() > 1) {
            int index_opt = 0;
            if (this.mAuthSmsBodyKeyWordRegex.length() > 0) {
                try {
                    Matcher matcher_body = Pattern.compile(this.mAuthSmsBodyKeyWordRegex).matcher(sms_body);
                    if (matcher_body.find()) {
                        index_opt = matcher_body.start();
                        logD("index_opt =" + index_opt);
                    }
                } catch (PatternSyntaxException e2) {
                }
            }
            int i = 0;
            int j = 1;
            while (i < authCodeIndexList.size() - 1 && j < authCodeIndexList.size()) {
                if (Math.abs(((Integer) authCodeIndexList.get(i)).intValue() - index_opt) <= Math.abs(((Integer) authCodeIndexList.get(j)).intValue() - index_opt)) {
                    return (String) authCodeRecords.get(((Integer) authCodeIndexList.get(i)).intValue());
                }
                i++;
                j++;
            }
        }
        return auth_code;
    }

    public String parsePortalAuthCode(String sms_body) {
        if (TextUtils.isEmpty(sms_body) || !isPortalAuthSmsbody(sms_body)) {
            return null;
        }
        String auth_code = parsePortalAuthCode(sms_body, AUTH_CODE_NECESSARY_REGEX_1);
        if (TextUtils.isEmpty(auth_code)) {
            auth_code = parsePortalAuthCode(sms_body, AUTH_CODE_NECESSARY_REGEX_2);
        }
        return auth_code;
    }

    public String[] obtainAuthSms(Intent intent) {
        String[] authSmsArray = new String[0];
        if (intent == null) {
            return authSmsArray;
        }
        Bundle bundle = intent.getExtras();
        if (bundle != null) {
            Object[] pdus = (Object[]) bundle.get("pdus");
            if (pdus.length > 0) {
                SmsMessage[] msgs = new SmsMessage[pdus.length];
                logD("sms pdus.length = " + pdus.length);
                for (int i = 0; i < pdus.length; i++) {
                    msgs[i] = SmsMessage.createFromPdu((byte[]) pdus[i]);
                }
                String sms_number = null;
                StringBuffer body_buffer = new StringBuffer();
                authSmsArray = new String[2];
                for (SmsMessage msg : msgs) {
                    sms_number = msg.getDisplayOriginatingAddress();
                    body_buffer.append(msg.getDisplayMessageBody());
                }
                logBug("number= " + sms_number + ", rec_sms_body len:" + body_buffer.toString().length());
                authSmsArray[0] = sms_number;
                authSmsArray[1] = body_buffer.toString();
            }
        }
        return authSmsArray;
    }

    public String replaceCommercialUserSmsNum(String num) {
        if (TextUtils.isEmpty(num)) {
            return num;
        }
        if (!num.startsWith(AUTH_SMS_NUM_BEGIN)) {
            num = "2" + num.length();
        } else if (num.length() >= 5) {
            num = "0";
        } else {
            num = "1";
        }
        logD("num =" + num);
        return num;
    }

    public String replaceCommercialUserSmsBody(String sms_body) {
        if (TextUtils.isEmpty(sms_body)) {
            return sms_body;
        }
        StringBuffer body_chr = new StringBuffer();
        Pattern pattern_keyword = null;
        try {
            sms_body = sms_body.replaceAll(REGEX_1_DATE_YYYY_MM_DD, "").replaceAll(REGEX_2_DATE_YYYY_MM_DD, "");
            if (this.mDateRegex3.length() > 0) {
                sms_body = sms_body.replaceAll(this.mDateRegex3, "");
            }
            if (this.mAuthSmsBodyKeyWordRegex.length() > 0) {
                pattern_keyword = Pattern.compile(this.mAuthSmsBodyKeyWordRegex);
            }
        } catch (PatternSyntaxException e) {
        }
        if (pattern_keyword == null) {
            return body_chr.toString();
        }
        Matcher matcher_keyword = pattern_keyword.matcher(sms_body);
        while (matcher_keyword.find()) {
            body_chr.append("K");
        }
        try {
            Matcher matcher_figure = Pattern.compile(FIGURE_REGEX).matcher(sms_body);
            while (matcher_figure.find()) {
                int figure_len = matcher_figure.group(0).length();
                body_chr.append("[");
                body_chr.append(figure_len);
                body_chr.append("] ");
            }
        } catch (PatternSyntaxException e2) {
        }
        logD("body_chr =" + body_chr.toString());
        return body_chr.toString();
    }

    private void logD(String log) {
        Log.d(TAG, log);
    }

    private void logBug(String log) {
    }
}
