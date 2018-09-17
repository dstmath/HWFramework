package com.android.contacts.update;

import com.android.contacts.util.HwLog;
import org.json.JSONException;
import org.json.JSONObject;

public class DownloadResponse {
    private static final int CODE_ALREADY_NEW_VER = 200000;
    private static final int CODE_CLIENT_PARAMETER_ERROR = 100003;
    private static final int CODE_NOT_EXITS = 100001;
    private static final int CODE_OK = 0;
    private static final int CODE_PARAMETER_NULL = 100002;
    private static final int CODE_SERVER_ERROR = 100004;
    private static final String KEY_DOWNLOADURL = "downloadUrl";
    private static final String KEY_INFO = "info";
    private static final String KEY_RESULT_CODE = "resultCode";
    private static final String KEY_VER = "ver";
    private static final String TAG = DownloadResponse.class.getSimpleName();
    private String downloadUrl;
    private int resultCode = -1;
    private String ver;

    private DownloadResponse(int resultCode, String downloadUrl, String ver) {
        this.resultCode = resultCode;
        this.downloadUrl = downloadUrl;
        this.ver = ver;
    }

    public static DownloadResponse fromJson(String str) throws JSONException {
        JSONObject json = new JSONObject(str);
        return new DownloadResponse(json.getInt(KEY_RESULT_CODE), json.getString(KEY_DOWNLOADURL), json.getString(KEY_VER));
    }

    public String getDownloadUrl() {
        return this.downloadUrl;
    }

    public String getFileName() {
        return this.downloadUrl.substring(this.downloadUrl.lastIndexOf("/") + 1);
    }

    public String getVer() {
        return this.ver;
    }

    public boolean checkAvalible() {
        if (this.resultCode == 0) {
            return true;
        }
        if (!HwLog.HWDBG) {
            return false;
        }
        HwLog.d(TAG, "checkAvalible code : " + this.resultCode);
        return false;
    }
}
