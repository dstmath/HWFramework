package org.ifaa.android.manager;

import android.content.Context;
import android.provider.Settings;
import android.util.Log;
import com.huawei.android.os.SystemPropertiesEx;
import huawei.android.hardware.fingerprint.FingerprintManagerEx;
import org.json.JSONException;
import org.json.JSONObject;

public class IFAAManagerV3Impl extends IFAAManagerV3 {
    private static final String APS_INIT_HEIGHT = "aps_init_height";
    private static final int DEFAULT_INIT_HEIGHT = 2880;
    private static final int EN_FINGERPRINT_VIEW_ICON_ONLY = 3;
    private static final String EXT_INFO_FAILURE = "NULL";
    private static final int FINGERPRINT_LOGO_COVER_HEIGHT = 320;
    private static final int FINGERPRINT_LOGO_COVER_WIDTH = 320;
    private static final int FINGERPRINT_SITE_ELEMETTS_COUNT = 4;
    private static final String LOG_TAG = "IFAAManagerV3Impl";
    private final Context mContext;
    private final IFAAManagerV2Impl mV2Impl;

    public IFAAManagerV3Impl(Context context) {
        this.mContext = context;
        this.mV2Impl = new IFAAManagerV2Impl(context);
    }

    @Override // org.ifaa.android.manager.IFAAManager
    public int getSupportBIOTypes(Context context) {
        int type = this.mV2Impl.getSupportBIOTypes(context);
        if (FingerprintManagerEx.hasFingerprintInScreen()) {
            Log.i(LOG_TAG, "support inner fingerprint");
            type |= 16;
        }
        Log.i(LOG_TAG, "V3 getSupportBIOTypes is " + type);
        return type;
    }

    @Override // org.ifaa.android.manager.IFAAManager
    public int startBIOManager(Context context, int authType) {
        return this.mV2Impl.startBIOManager(context, authType);
    }

    @Override // org.ifaa.android.manager.IFAAManager
    public String getDeviceModel() {
        return this.mV2Impl.getDeviceModel();
    }

    @Override // org.ifaa.android.manager.IFAAManager
    public int getVersion() {
        return this.mV2Impl.getVersion();
    }

    @Override // org.ifaa.android.manager.IFAAManagerV2
    public byte[] processCmdV2(Context context, byte[] param) {
        return this.mV2Impl.processCmdV2(context, param);
    }

    private boolean checkPositionData(int[] position) {
        for (int i = 0; i < 4; i++) {
            if (position[i] < 0) {
                return false;
            }
        }
        if (position[2] - position[0] < 0 || position[3] - position[1] < 0) {
            return false;
        }
        return true;
    }

    private String getInScreenFingerprintLocation() {
        if (!FingerprintManagerEx.hasFingerprintInScreen()) {
            Log.e(LOG_TAG, "don't support inside fingerprint");
            return EXT_INFO_FAILURE;
        }
        int[] position = FingerprintManagerEx.getHardwarePosition();
        if (position.length != 4) {
            Log.e(LOG_TAG, "position sites length error as" + position.length);
            return EXT_INFO_FAILURE;
        } else if (!checkPositionData(position)) {
            Log.e(LOG_TAG, "position sites logical error " + position[0] + " " + position[1] + " " + position[2] + " " + position[3]);
            return EXT_INFO_FAILURE;
        } else {
            int defaultDisplayHeight = Settings.Global.getInt(this.mContext.getContentResolver(), APS_INIT_HEIGHT, DEFAULT_INIT_HEIGHT);
            int curheight = SystemPropertiesEx.getInt("persist.sys.rog.height", defaultDisplayHeight);
            if (defaultDisplayHeight == 0) {
                Log.e(LOG_TAG, "defaultDisplayHeight is 0");
                return EXT_INFO_FAILURE;
            }
            float scale = ((float) curheight) / ((float) defaultDisplayHeight);
            Log.i(LOG_TAG, "scale is" + scale);
            int startX = (int) (((float) (((position[2] + position[0]) / 2) + -160)) * scale);
            int startY = (int) (((float) (((position[3] + position[1]) / 2) + -160)) * scale);
            int width = (int) (scale * 320.0f);
            int height = (int) (320.0f * scale);
            JSONObject location = new JSONObject();
            JSONObject fullView = new JSONObject();
            try {
                fullView.put("startX", startX);
                fullView.put("startY", startY);
                fullView.put("width", width);
                fullView.put("height", height);
                fullView.put("unit", "px");
                fullView.put("navConflict", false);
                location.put("type", 0);
                location.put("fullView", fullView);
                return location.toString();
            } catch (JSONException ex) {
                Log.e(LOG_TAG, "json expection " + ex);
                return EXT_INFO_FAILURE;
            }
        }
    }

    @Override // org.ifaa.android.manager.IFAAManagerV3
    public String getExtInfo(int authType, String keyExtInfo) {
        Log.i(LOG_TAG, "getExtInfo in v3 ");
        if (authType != 1 || !IFAAManagerV3.KEY_GET_SENSOR_LOCATION.equals(keyExtInfo)) {
            return EXT_INFO_FAILURE;
        }
        return getInScreenFingerprintLocation();
    }

    @Override // org.ifaa.android.manager.IFAAManagerV3
    public void setExtInfo(int authType, String keyExtInfo, String valExtInfo) {
        if (authType == 1 && IFAAManagerV3.KEY_FINGERPRINT_FULLVIEW.equals(keyExtInfo)) {
            if (!FingerprintManagerEx.hasFingerprintInScreen()) {
                Log.e(LOG_TAG, "don't support inside fingerprint");
                return;
            }
            Context context = this.mContext;
            if (context == null) {
                Log.e(LOG_TAG, "mContext empty!");
                return;
            }
            FingerprintManagerEx fpManager = new FingerprintManagerEx(context);
            if (IFAAManagerV3.VALUE_FINGERPRINT_DISABLE.equals(valExtInfo)) {
                fpManager.disableFingerprintView(true);
            } else if (IFAAManagerV3.VLAUE_FINGERPRINT_ENABLE.equals(valExtInfo)) {
                fpManager.enableFingerprintView(true, 3);
            } else {
                Log.e(LOG_TAG, "valExtInfo error");
            }
        }
        Log.i(LOG_TAG, "setExtInfo finish");
    }
}
