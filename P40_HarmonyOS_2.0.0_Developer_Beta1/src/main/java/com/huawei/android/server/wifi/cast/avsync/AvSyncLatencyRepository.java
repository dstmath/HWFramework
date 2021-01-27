package com.huawei.android.server.wifi.cast.avsync;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.text.TextUtils;
import android.util.wifi.HwHiLog;
import com.huawei.android.server.wifi.cast.avsync.AvSyncLatencyInfo;
import huawei.cust.HwCfgFilePolicy;
import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class AvSyncLatencyRepository {
    private static final String AV_LATENCY_CONFIG = "av_latency_config.json";
    private static final int BUFFER_SIZE = 1024;
    private static final String CFG_FILE_NAME = "/av_latency_config.json";
    private static final String CFG_VER_DIR = "emcom/noncell";
    private static final int READ_LATENCY_CONFIG = 1;
    private static final String TAG = "AvSyncLatencyRepository";
    private AvSyncLatencyInfo mAvSyncLatencyInfo = new AvSyncLatencyInfo();
    private Handler mHandler;
    private IAvSyncParseResultListener mParserListener;

    public interface IAvSyncParseResultListener {
        void onFailed();

        void onSuccess();
    }

    public AvSyncLatencyRepository(Looper looper) {
        this.mHandler = new Handler(looper) {
            /* class com.huawei.android.server.wifi.cast.avsync.AvSyncLatencyRepository.AnonymousClass1 */

            @Override // android.os.Handler
            public void handleMessage(Message msg) {
                if (msg.what != 1) {
                    HwHiLog.i(AvSyncLatencyRepository.TAG, false, "do not handle this msg", new Object[0]);
                    return;
                }
                String result = AvSyncLatencyRepository.this.getLatencyConfig();
                if (TextUtils.isEmpty(result) || !AvSyncLatencyRepository.this.parseLatencyConfig(result)) {
                    HwHiLog.i(AvSyncLatencyRepository.TAG, false, "latency config is empty or invalid", new Object[0]);
                    if (AvSyncLatencyRepository.this.mAvSyncLatencyInfo != null) {
                        AvSyncLatencyRepository.this.mAvSyncLatencyInfo.clearData();
                    }
                    if (AvSyncLatencyRepository.this.mParserListener != null) {
                        AvSyncLatencyRepository.this.mParserListener.onFailed();
                    }
                }
            }
        };
    }

    public void readLatencyConfig() {
        Handler handler = this.mHandler;
        handler.sendMessage(handler.obtainMessage(1));
    }

    public int getAvSyncLatency(String pkgName, int appVersionCode, int castType) {
        AvSyncLatencyInfo avSyncLatencyInfo = this.mAvSyncLatencyInfo;
        if (avSyncLatencyInfo != null) {
            return avSyncLatencyInfo.getLatency(pkgName, appVersionCode, castType);
        }
        return 0;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private String getLatencyConfig() {
        String res = null;
        String[] cfgFileInfo = HwCfgFilePolicy.getDownloadCfgFile(CFG_VER_DIR, "emcom/noncell/av_latency_config.json");
        if (cfgFileInfo == null || cfgFileInfo.length == 0) {
            HwHiLog.i(TAG, false, "av sync config file is empty", new Object[0]);
            return "";
        }
        File targetFile = new File(cfgFileInfo[0]);
        if (!targetFile.isFile()) {
            HwHiLog.i(TAG, false, "av sync targetFile is not exist!", new Object[0]);
            return "";
        }
        FileInputStream inputStream = null;
        try {
            inputStream = new FileInputStream(targetFile);
            res = inputStream2String(inputStream);
        } catch (IOException e) {
            HwHiLog.i(TAG, false, "read push latency failed for IOException", new Object[0]);
        } catch (Throwable th) {
            closeFileStream(inputStream);
            throw th;
        }
        closeFileStream(inputStream);
        return res;
    }

    private String inputStream2String(InputStream is) throws IOException {
        if (is == null) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        byte[] buffer = new byte[1024];
        while (true) {
            int length = is.read(buffer);
            if (length == -1) {
                return sb.toString();
            }
            sb.append(new String(buffer, 0, length, "UTF-8"));
        }
    }

    private void closeFileStream(Closeable closeable) {
        if (closeable != null) {
            try {
                closeable.close();
            } catch (IOException e) {
                HwHiLog.e(TAG, false, "close file IOException", new Object[0]);
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private boolean parseLatencyConfig(String data) {
        if (TextUtils.isEmpty(data)) {
            HwHiLog.i(TAG, false, "latency data is null", new Object[0]);
            return false;
        }
        try {
            JSONArray latencyArray = new JSONArray(data);
            int length = latencyArray.length();
            for (int i = 0; i < length; i++) {
                JSONObject jsonObject = latencyArray.optJSONObject(i);
                if (jsonObject != null) {
                    if (jsonObject.optInt("version_major") == 1) {
                        parseLatencyBaseArray(jsonObject.optJSONArray(AvSyncLatencyInfo.TAG_LATENCY_BASE));
                        parseLatencyAppArray(jsonObject.optJSONArray(AvSyncLatencyInfo.TAG_LATENCY_APP));
                        HwHiLog.i(TAG, false, "parseLatencyData finished", new Object[0]);
                        if (this.mParserListener != null) {
                            this.mParserListener.onSuccess();
                        }
                        return true;
                    }
                }
            }
        } catch (JSONException e) {
            HwHiLog.i(TAG, false, "parse latency data occur JSON parse error", new Object[0]);
        }
        return false;
    }

    private void parseLatencyAppArray(JSONArray latencyAppArray) {
        JSONArray latencyAppDetailArray;
        if (!(latencyAppArray == null || latencyAppArray.length() == 0)) {
            for (int i = 0; i < latencyAppArray.length(); i++) {
                JSONObject latencyAppJson = latencyAppArray.optJSONObject(i);
                if (latencyAppJson != null) {
                    String pkgName = latencyAppJson.optString(AvSyncLatencyInfo.TAG_PKG_NAME);
                    if (!(TextUtils.isEmpty(pkgName) || (latencyAppDetailArray = latencyAppJson.optJSONArray(AvSyncLatencyInfo.TAG_LATENCY_MS)) == null || latencyAppDetailArray.length() == 0)) {
                        AvSyncLatencyInfo.AvSyncLatencyAppInfo latencyApp = new AvSyncLatencyInfo.AvSyncLatencyAppInfo(pkgName);
                        for (int j = 0; j < latencyAppDetailArray.length(); j++) {
                            JSONObject latencyAppDetail = latencyAppDetailArray.optJSONObject(j);
                            if (latencyAppDetail != null) {
                                latencyApp.addAppDetailLatency(latencyAppDetail.optInt(AvSyncLatencyInfo.TAG_VERSION_MIN), latencyAppDetail.optInt(AvSyncLatencyInfo.TAG_VERSION_MAX), latencyAppDetail.optInt(AvSyncLatencyInfo.TAG_LATENCY_MS));
                            }
                        }
                        parseAppLatencyBaseArray(latencyApp, latencyAppJson.optJSONArray(AvSyncLatencyInfo.TAG_LATENCY_BASE));
                        AvSyncLatencyInfo avSyncLatencyInfo = this.mAvSyncLatencyInfo;
                        if (avSyncLatencyInfo != null) {
                            avSyncLatencyInfo.addLatencyAppInfo(pkgName, latencyApp);
                        }
                    }
                }
            }
        }
    }

    private void parseAppLatencyBaseArray(AvSyncLatencyInfo.AvSyncLatencyAppInfo latencyApp, JSONArray latencyBaseAppArray) {
        if (latencyBaseAppArray != null && latencyBaseAppArray.length() > 0) {
            for (int j = 0; j < latencyBaseAppArray.length(); j++) {
                JSONObject latencyAppBase = latencyBaseAppArray.optJSONObject(j);
                if (latencyAppBase != null) {
                    latencyApp.addAppLatencyBase(latencyAppBase.optInt(AvSyncLatencyInfo.TAG_CAST_TYPE), latencyAppBase.optInt(AvSyncLatencyInfo.TAG_LATENCY_MS));
                }
            }
        }
    }

    private void parseLatencyBaseArray(JSONArray latencyBaseArray) {
        if (!(latencyBaseArray == null || latencyBaseArray.length() == 0)) {
            for (int i = 0; i < latencyBaseArray.length(); i++) {
                JSONObject latencyBase = latencyBaseArray.optJSONObject(i);
                if (latencyBase != null) {
                    int castType = latencyBase.optInt(AvSyncLatencyInfo.TAG_CAST_TYPE);
                    int latencyMs = latencyBase.optInt(AvSyncLatencyInfo.TAG_LATENCY_MS);
                    AvSyncLatencyInfo avSyncLatencyInfo = this.mAvSyncLatencyInfo;
                    if (avSyncLatencyInfo != null) {
                        avSyncLatencyInfo.addLatencyBaseInfo(castType, latencyMs);
                    }
                }
            }
        }
    }

    public void setParseResultListener(IAvSyncParseResultListener parserListener) {
        this.mParserListener = parserListener;
    }
}
