package com.huawei.zxing.resultdispatch;

import android.util.Log;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

public class SignUtil {
    public static String getSign(HashMap<String, String> map) {
        StringBuffer sb = new StringBuffer();
        getBody(sb, map);
        String si = "";
        try {
            si = URLEncoder.encode(sb.toString(), "UTF-8");
        } catch (UnsupportedEncodingException e) {
            Log.v("SignUtil", "unlawful result ");
        }
        Log.v("encoded string：", si);
        return SHA256.Encrypt(si, null);
    }

    public static void getBody(StringBuffer body, HashMap<String, String> map) {
        List<String> list = new ArrayList(map.keySet());
        Collections.sort(list);
        for (int i = 0; i < list.size(); i++) {
            body.append(((String) list.get(i)) + "=" + ((String) map.get(list.get(i))));
            if (i != list.size() - 1) {
                body.append("&");
            }
        }
    }
}
