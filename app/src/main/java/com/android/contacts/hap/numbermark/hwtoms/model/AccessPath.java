package com.android.contacts.hap.numbermark.hwtoms.model;

import android.os.Build;
import android.os.Build.VERSION;
import android.text.TextUtils;
import com.android.contacts.hap.numbermark.utils.AESUtils;
import java.util.ArrayList;
import java.util.List;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

public class AccessPath {
    private static String ABNORMAL_SERVER = null;
    public static final String BASE_URL = "http://hw.118114.net:9080/hy114/";
    public static final String CONNECTION_TIMEOUT = "connect overtime";
    private static final int CONNECT_OVERTIME_THRESHOLD = 2000;
    private static final int CONNECT_SUCCESS = 200;
    public static final String CORRECTION_URL = "http://dianhua.118114.cn:8088/middlePageService/Correction";
    public static final String DETAIL_FOR_HUAWEI_URL = "http://hw.118114.net:9080/hy114/DetailForHuawei";
    public static final String INFO_FOR_HUAWEI_URL = "http://hw.118114.net:9080/hy114/InfoForHuawei";
    private static String NORESPONSE = null;
    private static String REQUEST_FAILED = null;
    private static String SEND_FAILURE = null;
    public static final String TEL_FOR_HUAWEI_URL = "http://hw.118114.net:9080/hy114/TelForHuawei";
    private static HttpClient httpClient;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.contacts.hap.numbermark.hwtoms.model.AccessPath.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.contacts.hap.numbermark.hwtoms.model.AccessPath.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 5 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 6 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.contacts.hap.numbermark.hwtoms.model.AccessPath.<clinit>():void");
    }

    public static String getNetworkResposeResult(String appKey, String info, String path) {
        try {
            httpClient.getParams().setParameter("http.connection.timeout", Integer.valueOf(CONNECT_OVERTIME_THRESHOLD));
            httpClient.getParams().setParameter("http.socket.timeout", Integer.valueOf(CONNECT_OVERTIME_THRESHOLD));
            HttpPost httpPost = new HttpPost(path);
            List<BasicNameValuePair> parameters = new ArrayList();
            String infos = AESUtils.encrypt4AES(info, appKey);
            httpPost.addHeader("User-Agent", getPhoneUserAgent());
            parameters.add(new BasicNameValuePair("appKey", appKey));
            parameters.add(new BasicNameValuePair("info", infos));
            httpPost.setEntity(new UrlEncodedFormEntity(parameters, "UTF-8"));
            HttpResponse response = httpClient.execute(httpPost);
            if (response.getStatusLine().getStatusCode() == CONNECT_SUCCESS) {
                String result = EntityUtils.toString(response.getEntity(), "UTF-8");
                if (TextUtils.isEmpty(result)) {
                    return NORESPONSE;
                }
                return result;
            } else if (response.getStatusLine().getStatusCode() == 404) {
                return ABNORMAL_SERVER;
            } else {
                return REQUEST_FAILED;
            }
        } catch (Exception e) {
            if (null != null) {
                return SEND_FAILURE;
            }
            e.printStackTrace();
            return CONNECTION_TIMEOUT;
        }
    }

    private static String getPhoneUserAgent() {
        StringBuffer buffer = new StringBuffer();
        String version = VERSION.RELEASE;
        if ("REL".equals(VERSION.CODENAME)) {
            String model = Build.MODEL;
            if (model.length() > 0) {
                buffer.append(" ");
                buffer.append(model);
            }
        }
        String id = Build.ID;
        if (id.length() > 0) {
            buffer.append(" Build/");
            buffer.append(id);
        }
        buffer.append("; ");
        if (version.length() <= 0) {
            buffer.append("1.0");
        } else if (Character.isDigit(version.charAt(0))) {
            buffer.append(version);
        } else {
            buffer.append("4.1.1");
        }
        return buffer.toString();
    }
}
