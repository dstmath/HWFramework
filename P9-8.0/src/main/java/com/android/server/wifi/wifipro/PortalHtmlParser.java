package com.android.server.wifi.wifipro;

import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;
import com.android.server.wifi.HwCHRWifiCPUUsage;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PortalHtmlParser {
    private static final String[] ATTRIBUTES_SELECTED = new String[]{"type=", ID, "name=", VALUE, PLACEHOLDER};
    private static final int BUFFER_SIZE = 8192;
    private static final String CN_VALUE_PASSWORD = "密码";
    private static final String CN_VALUE_VERIFY_CODE = "验证码";
    public static final boolean DBG = true;
    private static final String ID = "id=";
    private static final String[] KEY_WORDS_BUTTON_END = new String[]{"/button>", "/BUTTON>"};
    private static final String[] KEY_WORDS_BUTTON_START = new String[]{"<button ", "<BUTTON "};
    private static final String[] KEY_WORDS_GET_PW_BTN = new String[]{"获取", "发送", CN_VALUE_PASSWORD, CN_VALUE_VERIFY_CODE, "verify"};
    private static final String[] KEY_WORDS_HYPERLINK_END = new String[]{"/a>", "/A>"};
    private static final String[] KEY_WORDS_HYPERLINK_REF = new String[]{"href=", "HREF="};
    private static final String[] KEY_WORDS_HYPERLINK_START = new String[]{"<a ", "<A "};
    private static final String[] KEY_WORDS_IGNORE_BTN = new String[]{"忘记密码", "修改密码"};
    private static final String[] KEY_WORDS_INPUT = new String[]{"<input ", "<INPUT "};
    private static final String[] KEY_WORDS_LINK_IMG = new String[]{"<img ", "<IMG "};
    private static final String[] KEY_WORDS_LOGIN_BTN = new String[]{"登录", "登入", "连接", "接入", "上网", "start"};
    private static final String PLACEHOLDER = "placeholder=";
    public static final String PORTAL_NETWORK_FLAG = "HW_WIFI_PRO_PORTAL_FLAG";
    private static final String[] PROBABLE_IDS_PASSWORD = new String[]{"password", "passwd", "pw", "code"};
    private static final int SOCKET_TIMEOUT_MS = 10000;
    private static final int STRING_BUFFER_SIZE = 2097152;
    private static final String TAG = "WiFi_PRO_PortalHtmlParser";
    private static final String[] TYPES_BUTTON = new String[]{"type=\"button\"", "type='button'", "type=\"submit\"", "type='submit'", "type=\"BUTTON\"", "type='BUTTON'", "type=\"SUBMIT\"", "type='SUBMIT'"};
    private static final String[] TYPES_PASSWORD = new String[]{"type=\"text\"", "type='text'", "type=\"password\"", "type='password'", "type=\"TEXT\"", "type='TEXT'", "type=\"PASSWORD\"", "type='PASSWORD'", "type=\"tel\""};
    private static final String[] TYPES_PHONE_NUMBER = new String[]{"type=\"text\"", "type='text'", "type=\"TEXT\"", "type='TEXT'", "type=\"tel\""};
    private static final String[] TYPES_SELECTED = new String[]{"type=\"text\"", "type='text'", "type=\"password\"", "type='password'", "type=\"button\"", "type='button'", "type=\"submit\"", "type='submit'", "type=\"TEXT\"", "type='TEXT'", "type=\"PASSWORD\"", "type='PASSWORD'", "type=\"BUTTON\"", "type='BUTTON'", "type=\"SUBMIT\"", "type='SUBMIT'", "type=\"tel\"", "type=\"TEL\""};
    private static final String VALUE = "value=";

    private static void setConnectionProperty(HttpURLConnection urlConnection) throws IOException {
        urlConnection.setInstanceFollowRedirects(false);
        urlConnection.setConnectTimeout(SOCKET_TIMEOUT_MS);
        urlConnection.setReadTimeout(SOCKET_TIMEOUT_MS);
        urlConnection.setUseCaches(false);
    }

    private static String readAllHtml(InputStream in) throws IOException {
        InputStreamReader isr = new InputStreamReader(in);
        try {
            StringBuffer html = new StringBuffer(STRING_BUFFER_SIZE);
            try {
                char[] buffer = new char[BUFFER_SIZE];
                int htmlLength = 0;
                while (true) {
                    int length = isr.read(buffer, 0, BUFFER_SIZE);
                    if (length != -1 && htmlLength + length < STRING_BUFFER_SIZE) {
                        htmlLength += length;
                        html.append(buffer, 0, length);
                    }
                }
                return html.toString();
            } catch (OutOfMemoryError e) {
                StringBuffer stringBuffer = html;
                LOGW("readAllHtml, OutOfMemoryError msg receive ");
                return null;
            }
        } catch (OutOfMemoryError e2) {
            LOGW("readAllHtml, OutOfMemoryError msg receive ");
            return null;
        }
    }

    public static String downloadPortalWebHtml(ConnectivityManager manager, String dlUrl) {
        String downloadHtml = null;
        HttpURLConnection httpURLConnection = null;
        InputStream inputStream = null;
        NetworkInfo networkInfo = manager.getNetworkInfo(1);
        if (networkInfo == null || (networkInfo.isConnected() ^ 1) != 0) {
            LOGW("downloadPortalWebHtml failed, networkInfo = " + networkInfo);
            return null;
        }
        try {
            URLConnection connection = new URL(dlUrl).openConnection();
            if (!(connection instanceof HttpURLConnection)) {
                return null;
            }
            httpURLConnection = (HttpURLConnection) connection;
            setConnectionProperty(httpURLConnection);
            inputStream = httpURLConnection.getInputStream();
            int respCode = httpURLConnection.getResponseCode();
            LOGD("downloadPortalWebHtml, respCode = " + respCode + ", url=" + dlUrl);
            if (respCode == 200) {
                downloadHtml = readAllHtml(inputStream);
            }
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    LOGW("NetworkCheckerThread, exception of close, msg receive ");
                }
            }
            if (httpURLConnection != null) {
                httpURLConnection.disconnect();
            }
            return downloadHtml;
        } catch (IOException e2) {
            LOGW("NetworkCheckerThread, IOException, msg receive ");
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e3) {
                    LOGW("NetworkCheckerThread, exception of close, msg receive ");
                }
            }
            if (httpURLConnection != null) {
                httpURLConnection.disconnect();
            }
        } catch (Exception e4) {
            LOGW("NetworkCheckerThread, Exception, msg receive ");
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e5) {
                    LOGW("NetworkCheckerThread, exception of close, msg receive ");
                }
            }
            if (httpURLConnection != null) {
                httpURLConnection.disconnect();
            }
        } catch (Throwable th) {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e6) {
                    LOGW("NetworkCheckerThread, exception of close, msg receive ");
                }
            }
            if (httpURLConnection != null) {
                httpURLConnection.disconnect();
            }
        }
    }

    public static List<String> selectKeyLinesFromHtml(String html) {
        int i;
        int start;
        int end;
        int offset;
        int endOffset;
        boolean matched;
        List<String> sortedKeyLines = new ArrayList();
        Map<Integer, String> keyLines = new HashMap();
        int HTML_LENGTH = html.length();
        for (i = 0; i < KEY_WORDS_INPUT.length; i++) {
            start = html.indexOf(KEY_WORDS_INPUT[i], 0);
            while (start != -1) {
                end = html.indexOf(">", start);
                if (end == -1 || end + 1 > HTML_LENGTH) {
                    LOGW("WARNING: start = " + start + ", but no ending found for <input>, abnormal");
                    break;
                }
                offset = start;
                String input = html.substring(start, end + 1);
                int inputLength = input.length();
                LOGD("<input> = " + input);
                input = attributesNormalized(input);
                if (!input.contains("type=hidden")) {
                    StringBuffer buf = new StringBuffer();
                    buf.append(KEY_WORDS_INPUT[i]);
                    for (int k = 0; k < ATTRIBUTES_SELECTED.length; k++) {
                        String item = "";
                        int s = input.indexOf(ATTRIBUTES_SELECTED[k]);
                        if (s != -1) {
                            int e = input.indexOf("\" ", (ATTRIBUTES_SELECTED[k].length() + s) + 1);
                            if (e == -1 || e + 2 < s || e + 2 > inputLength) {
                                item = input.substring(s).replace("/>", "").replace(">", "") + HwCHRWifiCPUUsage.COL_SEP;
                            } else {
                                item = input.substring(s, e + 2);
                            }
                            buf.append(item);
                        }
                    }
                    buf.append("/>");
                    keyLines.put(Integer.valueOf(offset), buf.toString());
                }
                start = html.indexOf(KEY_WORDS_INPUT[i], offset + inputLength);
            }
        }
        for (i = 0; i < KEY_WORDS_HYPERLINK_START.length; i++) {
            endOffset = KEY_WORDS_HYPERLINK_END[i].length();
            start = html.indexOf(KEY_WORDS_HYPERLINK_START[i], 0);
            while (start != -1) {
                offset = start;
                end = html.indexOf(KEY_WORDS_HYPERLINK_END[i], start);
                if (end == -1 || end + endOffset > HTML_LENGTH) {
                    LOGW("WARNING: start = " + start + ", but no ending found for <href>, abnormal");
                    break;
                }
                String href = html.substring(start, end + endOffset);
                int hrefLength = href.length();
                matched = false;
                for (String contains : KEY_WORDS_GET_PW_BTN) {
                    if (href.contains(contains)) {
                        matched = true;
                        break;
                    }
                }
                for (String contains2 : KEY_WORDS_IGNORE_BTN) {
                    if (href.contains(contains2)) {
                        matched = false;
                        break;
                    }
                }
                LOGD("<a href> = " + href + ", matched = " + matched);
                if (matched) {
                    keyLines.put(Integer.valueOf(offset), href);
                }
                start = html.indexOf(KEY_WORDS_HYPERLINK_START[i], offset + hrefLength);
            }
        }
        for (i = 0; i < KEY_WORDS_LINK_IMG.length; i++) {
            start = html.indexOf(KEY_WORDS_LINK_IMG[i], 0);
            while (start != -1) {
                offset = start;
                end = html.indexOf(">", start);
                if (end == -1 || end + 1 > HTML_LENGTH) {
                    LOGW("WARNING: start = " + start + ", but no ending found for <img> link, abnormal");
                    break;
                }
                String imgLink = html.substring(start, end + 1);
                int length = imgLink.length();
                matched = false;
                if (imgLink.contains("onclick")) {
                    for (String contains22 : KEY_WORDS_GET_PW_BTN) {
                        if (imgLink.contains(contains22)) {
                            matched = true;
                            break;
                        }
                    }
                    for (String contains222 : KEY_WORDS_IGNORE_BTN) {
                        if (imgLink.contains(contains222)) {
                            matched = false;
                            break;
                        }
                    }
                }
                LOGD("<img> = " + imgLink + ", matched = " + matched);
                if (matched) {
                    keyLines.put(Integer.valueOf(offset), imgLink);
                }
                start = html.indexOf(KEY_WORDS_LINK_IMG[i], offset + length);
            }
        }
        for (i = 0; i < KEY_WORDS_BUTTON_START.length; i++) {
            endOffset = KEY_WORDS_BUTTON_END[i].length();
            start = html.indexOf(KEY_WORDS_BUTTON_START[i], 0);
            while (start != -1) {
                offset = start;
                end = html.indexOf(KEY_WORDS_BUTTON_END[i], start);
                if (end == -1 || end + endOffset > HTML_LENGTH) {
                    LOGW("WARNING: start = " + start + ", but no ending found for <button> link, abnormal");
                    break;
                }
                String btnLink = html.substring(start, end + endOffset);
                int btnLength = btnLink.length();
                matched = false;
                for (String contains2222 : KEY_WORDS_GET_PW_BTN) {
                    if (btnLink.contains(contains2222)) {
                        matched = true;
                        break;
                    }
                }
                for (String contains22222 : KEY_WORDS_IGNORE_BTN) {
                    if (btnLink.contains(contains22222)) {
                        matched = false;
                        break;
                    }
                }
                LOGD("<button> = " + btnLink + ", matched = " + matched);
                if (matched) {
                    keyLines.put(Integer.valueOf(offset), btnLink);
                }
                start = html.indexOf(KEY_WORDS_BUTTON_START[i], offset + btnLength);
            }
        }
        if (keyLines.size() > 0) {
            Object[] keys = keyLines.keySet().toArray();
            Arrays.sort(keys);
            for (Object obj : keys) {
                sortedKeyLines.add((String) keyLines.get(obj));
            }
        }
        for (i = 0; i < sortedKeyLines.size(); i++) {
            LOGD("selectKeyLinesFromHtml, key line = " + ((String) sortedKeyLines.get(i)));
        }
        return sortedKeyLines;
    }

    private static String attributesNormalized(String input) {
        return new String(input.getBytes(StandardCharsets.UTF_8), StandardCharsets.UTF_8).replace("TYPE=", "type=").replace("TYPE = ", "type=").replace("type = ", "type=").replace("ID=", ID).replace("ID = ", ID).replace("id = ", ID).replace("NAME=", "name=").replace("NAME = ", "name=").replace("id = ", "name=").replace("VALUE=", VALUE).replace("VALUE = ", VALUE).replace("value = ", VALUE).replace("PLACEHOLDER=", PLACEHOLDER).replace("PLACEHOLDER = ", PLACEHOLDER).replace("placeholder = ", PLACEHOLDER);
    }

    public static List<String> parsePortalWebHtml(String html, Map<String, String> map) {
        return selectKeyLinesFromHtml(html);
    }

    public static int getInputNumber(List<String> keyLines) {
        int num = 0;
        for (int i = 0; i < keyLines.size(); i++) {
            for (CharSequence contains : TYPES_PASSWORD) {
                if (((String) keyLines.get(i)).contains(contains)) {
                    num++;
                    break;
                }
            }
        }
        LOGD("getInputNumber, num = " + num);
        return num;
    }

    public static void LOGD(String msg) {
        Log.d(TAG, msg);
    }

    public static void LOGW(String msg) {
        Log.w(TAG, msg);
    }
}
