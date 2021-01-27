package com.android.server.wifi.grs.requestremote.base;

import android.text.TextUtils;
import android.util.Log;
import com.android.server.wifi.grs.requestremote.model.GrsServerBean;
import com.android.server.wifi.grs.utils.Io;
import java.util.ArrayList;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class GrsServerConfigMgr {
    private static final String GRS_BASE_URL = "grs_base_url";
    private static final String GRS_QUERY_ENDPOINT = "grs_query_endpoint";
    private static final String GRS_QUERY_TIMEOUT = "grs_query_timeout";
    private static final String GRS_SERVER = "grs_server";
    private static final String GRS_SERVER_CONFIG_NAME = "grs_networkprobe_server_config.json";
    private static final String TAG = GrsServerConfigMgr.class.getSimpleName();
    private static GrsServerBean sGrsServerBean;

    private GrsServerConfigMgr() {
    }

    public static synchronized GrsServerBean getGrsServerBean() {
        synchronized (GrsServerConfigMgr.class) {
            if (sGrsServerBean != null) {
                return sGrsServerBean;
            }
            String serverResult = Io.getConfigContent(GRS_SERVER_CONFIG_NAME);
            if (TextUtils.isEmpty(serverResult)) {
                Log.d(TAG, "grs_networkprobe_server_config.json must exists");
                return null;
            }
            try {
                JSONObject jsObjectGrsServer = new JSONObject(serverResult).getJSONObject(GRS_SERVER);
                JSONArray baseUrlJsonArray = jsObjectGrsServer.getJSONArray(GRS_BASE_URL);
                List<String> grsBaseUrls = null;
                if (baseUrlJsonArray != null && baseUrlJsonArray.length() > 0) {
                    grsBaseUrls = new ArrayList<>(baseUrlJsonArray.length());
                    for (int i = 0; i < baseUrlJsonArray.length(); i++) {
                        grsBaseUrls.add(baseUrlJsonArray.get(i).toString());
                    }
                }
                sGrsServerBean = new GrsServerBean();
                sGrsServerBean.setGrsBaseUrl(grsBaseUrls);
                sGrsServerBean.setGrsQueryEndpoint(jsObjectGrsServer.getString(GRS_QUERY_ENDPOINT));
                sGrsServerBean.setGrsQueryTimeout(jsObjectGrsServer.getInt(GRS_QUERY_TIMEOUT));
            } catch (JSONException e) {
                Log.d(TAG, "getGrsServerBean catch JSONException");
            }
            return sGrsServerBean;
        }
    }
}
