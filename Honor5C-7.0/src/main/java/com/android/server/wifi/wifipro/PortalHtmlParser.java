package com.android.server.wifi.wifipro;

import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;
import com.android.server.wifi.HwCHRWifiCPUUsage;
import com.android.server.wifi.HwSelfCureUtils;
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
    private static final String[] ATTRIBUTES_SELECTED = null;
    private static final int BUFFER_SIZE = 8192;
    private static final String CN_VALUE_PASSWORD = "\u5bc6\u7801";
    private static final String CN_VALUE_VERIFY_CODE = "\u9a8c\u8bc1\u7801";
    public static final boolean DBG = true;
    private static final String ID = "id=";
    private static final String[] KEY_WORDS_BUTTON_END = null;
    private static final String[] KEY_WORDS_BUTTON_START = null;
    private static final String[] KEY_WORDS_GET_PW_BTN = null;
    private static final String[] KEY_WORDS_HYPERLINK_END = null;
    private static final String[] KEY_WORDS_HYPERLINK_REF = null;
    private static final String[] KEY_WORDS_HYPERLINK_START = null;
    private static final String[] KEY_WORDS_IGNORE_BTN = null;
    private static final String[] KEY_WORDS_INPUT = null;
    private static final String[] KEY_WORDS_LINK_IMG = null;
    private static final String[] KEY_WORDS_LOGIN_BTN = null;
    private static final String PLACEHOLDER = "placeholder=";
    public static final String PORTAL_NETWORK_FLAG = "HW_WIFI_PRO_PORTAL_FLAG";
    private static final String[] PROBABLE_IDS_PASSWORD = null;
    private static final int SOCKET_TIMEOUT_MS = 10000;
    private static final int STRING_BUFFER_SIZE = 2097152;
    private static final String TAG = "WiFi_PRO_PortalHtmlParser";
    private static final String[] TYPES_BUTTON = null;
    private static final String[] TYPES_PASSWORD = null;
    private static final String[] TYPES_PHONE_NUMBER = null;
    private static final String[] TYPES_SELECTED = null;
    private static final String VALUE = "value=";

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.server.wifi.wifipro.PortalHtmlParser.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.server.wifi.wifipro.PortalHtmlParser.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 7 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 8 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.server.wifi.wifipro.PortalHtmlParser.<clinit>():void");
    }

    private static void setConnectionProperty(HttpURLConnection urlConnection) throws IOException {
        urlConnection.setInstanceFollowRedirects(false);
        urlConnection.setConnectTimeout(SOCKET_TIMEOUT_MS);
        urlConnection.setReadTimeout(SOCKET_TIMEOUT_MS);
        urlConnection.setUseCaches(false);
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private static String readAllHtml(InputStream in) throws IOException {
        OutOfMemoryError e;
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
            } catch (OutOfMemoryError e2) {
                e = e2;
                StringBuffer stringBuffer = html;
                LOGW("readAllHtml, OutOfMemoryError msg = " + e.getMessage());
                return null;
            }
        } catch (OutOfMemoryError e3) {
            e = e3;
            LOGW("readAllHtml, OutOfMemoryError msg = " + e.getMessage());
            return null;
        }
    }

    public static String downloadPortalWebHtml(ConnectivityManager manager, String dlUrl) {
        String str = null;
        HttpURLConnection httpURLConnection = null;
        InputStream inputStream = null;
        NetworkInfo networkInfo = manager.getNetworkInfo(1);
        if (networkInfo == null || !networkInfo.isConnected()) {
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
            if (respCode == HwSelfCureUtils.SCE_WIFI_DISABLED_DELAY) {
                str = readAllHtml(inputStream);
            }
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    LOGW("NetworkCheckerThread, exception of close, msg = " + e.getMessage());
                }
            }
            if (httpURLConnection != null) {
                httpURLConnection.disconnect();
            }
            return str;
        } catch (IOException e2) {
            LOGW("NetworkCheckerThread, IOException, msg = " + e2.getMessage());
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e22) {
                    LOGW("NetworkCheckerThread, exception of close, msg = " + e22.getMessage());
                }
            }
            if (httpURLConnection != null) {
                httpURLConnection.disconnect();
            }
        } catch (Throwable th) {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e222) {
                    LOGW("NetworkCheckerThread, exception of close, msg = " + e222.getMessage());
                }
            }
            if (httpURLConnection != null) {
                httpURLConnection.disconnect();
            }
        }
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public static List<String> selectKeyLinesFromHtml(String html) {
        List<String> sortedKeyLines = new ArrayList();
        Map<Integer, String> keyLines = new HashMap();
        int HTML_LENGTH = html.length();
        int i = 0;
        while (true) {
            int length = KEY_WORDS_INPUT.length;
            if (i >= r0) {
                break;
            }
            int start = html.indexOf(KEY_WORDS_INPUT[i], 0);
            while (start != -1) {
                int end = html.indexOf(">", start);
                if (end == -1 || end + 1 > HTML_LENGTH) {
                    LOGW("WARNING: start = " + start + ", but no ending found for <input>, abnormal");
                    break;
                }
                int offset = start;
                String input = html.substring(start, end + 1);
                int inputLength = input.length();
                LOGD("<input> = " + input);
                input = attributesNormalized(input);
                if (!input.contains("type=hidden")) {
                    StringBuffer buf = new StringBuffer();
                    buf.append(KEY_WORDS_INPUT[i]);
                    int k = 0;
                    while (true) {
                        if (k >= ATTRIBUTES_SELECTED.length) {
                            break;
                        }
                        String item = "";
                        int s = input.indexOf(ATTRIBUTES_SELECTED[k]);
                        if (s != -1) {
                            int e = input.indexOf("\" ", (ATTRIBUTES_SELECTED[k].length() + s) + 1);
                            if (e == -1 || e + 2 < s || e + 2 > inputLength) {
                                item = input.substring(s).replace("/>", "").replace(">", "");
                                item = item + HwCHRWifiCPUUsage.COL_SEP;
                            } else {
                                item = input.substring(s, e + 2);
                            }
                            buf.append(item);
                        }
                        k++;
                    }
                    buf.append("/>");
                    keyLines.put(Integer.valueOf(offset), buf.toString());
                }
                start = html.indexOf(KEY_WORDS_INPUT[i], offset + inputLength);
            }
            i++;
        }
        i = 0;
        while (true) {
            length = KEY_WORDS_HYPERLINK_START.length;
            if (i >= r0) {
                break;
            }
            int endOffset = KEY_WORDS_HYPERLINK_END[i].length();
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
                boolean matched = false;
                int j = 0;
                while (true) {
                    if (j >= KEY_WORDS_GET_PW_BTN.length) {
                        break;
                    }
                    if (href.contains(KEY_WORDS_GET_PW_BTN[j])) {
                        break;
                    }
                    j++;
                }
                j = 0;
                while (true) {
                    if (j >= KEY_WORDS_IGNORE_BTN.length) {
                        break;
                    }
                    if (href.contains(KEY_WORDS_IGNORE_BTN[j])) {
                        break;
                    }
                    j++;
                }
                LOGD("<a href> = " + href + ", matched = " + matched);
                if (matched) {
                    keyLines.put(Integer.valueOf(offset), href);
                }
                start = html.indexOf(KEY_WORDS_HYPERLINK_START[i], offset + hrefLength);
            }
            i++;
        }
        i = 0;
        while (true) {
            length = KEY_WORDS_LINK_IMG.length;
            if (i >= r0) {
                break;
            }
            start = html.indexOf(KEY_WORDS_LINK_IMG[i], 0);
            while (start != -1) {
                offset = start;
                end = html.indexOf(">", start);
                if (end == -1 || end + 1 > HTML_LENGTH) {
                    LOGW("WARNING: start = " + start + ", but no ending found for <img> link, abnormal");
                    break;
                }
                String imgLink = html.substring(start, end + 1);
                int length2 = imgLink.length();
                matched = false;
                if (imgLink.contains("onclick")) {
                    j = 0;
                    while (true) {
                        if (j >= KEY_WORDS_GET_PW_BTN.length) {
                            break;
                        }
                        if (imgLink.contains(KEY_WORDS_GET_PW_BTN[j])) {
                            break;
                        }
                        j++;
                    }
                    j = 0;
                    while (true) {
                        if (j >= KEY_WORDS_IGNORE_BTN.length) {
                            break;
                        }
                        if (imgLink.contains(KEY_WORDS_IGNORE_BTN[j])) {
                            break;
                        }
                        j++;
                    }
                }
                LOGD("<img> = " + imgLink + ", matched = " + matched);
                if (matched) {
                    keyLines.put(Integer.valueOf(offset), imgLink);
                }
                start = html.indexOf(KEY_WORDS_LINK_IMG[i], offset + length2);
            }
            i++;
        }
        i = 0;
        while (true) {
            length = KEY_WORDS_BUTTON_START.length;
            if (i >= r0) {
                break;
            }
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
                j = 0;
                while (true) {
                    if (j >= KEY_WORDS_GET_PW_BTN.length) {
                        break;
                    }
                    if (btnLink.contains(KEY_WORDS_GET_PW_BTN[j])) {
                        break;
                    }
                    j++;
                }
                matched = DBG;
                j = 0;
                while (true) {
                    if (j >= KEY_WORDS_IGNORE_BTN.length) {
                        break;
                    }
                    if (btnLink.contains(KEY_WORDS_IGNORE_BTN[j])) {
                        break;
                    }
                    j++;
                }
                matched = false;
                LOGD("<button> = " + btnLink + ", matched = " + matched);
                if (matched) {
                    keyLines.put(Integer.valueOf(offset), btnLink);
                }
                start = html.indexOf(KEY_WORDS_BUTTON_START[i], offset + btnLength);
            }
            i++;
        }
        if (keyLines.size() > 0) {
            Object[] keys = keyLines.keySet().toArray();
            Arrays.sort(keys);
            i = 0;
            while (true) {
                length = keys.length;
                if (i >= r0) {
                    break;
                }
                sortedKeyLines.add((String) keyLines.get(keys[i]));
                i++;
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
