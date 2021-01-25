package com.huawei.networkit.grs.requestremote.base;

import android.text.TextUtils;
import com.huawei.networkit.grs.common.Logger;
import com.huawei.networkit.grs.requestremote.model.GrsServerBean;
import com.huawei.networkit.grs.utils.Io;
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
    private static final String GRS_SERVER_CONFIG_NAME = "grs_sdk_server_config.json";
    private static final String TAG = GrsServerConfigMgr.class.getSimpleName();
    private static GrsServerBean grsServerBean;

    public static synchronized GrsServerBean getGrsServerBean() {
        synchronized (GrsServerConfigMgr.class) {
            if (grsServerBean != null) {
                return grsServerBean;
            }
            String serverResult = Io.getConfigContent(GRS_SERVER_CONFIG_NAME);
            if (TextUtils.isEmpty(serverResult)) {
                return null;
            }
            try {
                JSONObject jsObjectGrsServer = new JSONObject(serverResult).getJSONObject(GRS_SERVER);
                JSONArray baseurlJSONArray = jsObjectGrsServer.getJSONArray(GRS_BASE_URL);
                List<String> grsBaseUrl = null;
                if (baseurlJSONArray != null && baseurlJSONArray.length() > 0) {
                    grsBaseUrl = new ArrayList<>();
                    for (int i = 0; i < baseurlJSONArray.length(); i++) {
                        grsBaseUrl.add(baseurlJSONArray.get(i).toString());
                    }
                }
                grsServerBean = new GrsServerBean();
                grsServerBean.setGrsBaseUrl(grsBaseUrl);
                grsServerBean.setGrsQueryEndpoint(jsObjectGrsServer.getString(GRS_QUERY_ENDPOINT));
                grsServerBean.setGrsQueryTimeout(jsObjectGrsServer.getInt(GRS_QUERY_TIMEOUT));
            } catch (JSONException e) {
                Logger.w(TAG, "getGrsServerBean catch JSONException", e);
            }
            return grsServerBean;
        }
    }
}
