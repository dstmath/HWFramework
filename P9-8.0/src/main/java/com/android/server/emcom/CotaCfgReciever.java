package com.android.server.emcom;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class CotaCfgReciever extends BroadcastReceiver {
    public static final String ACTION_CFG_UPDATED = "huawei.android.hwouc.intent.action.CFG_UPDATED";
    public static final String EXTRA_CFG_REL_DIR = "cfgDir";
    public static final String EXTRA_UPDATE_MODE = "updateMode";
    static final String TAG = "CotaCfgReciever";
    protected final String featureCfgDir;

    public CotaCfgReciever(String featureCfgDir) {
        this.featureCfgDir = featureCfgDir;
    }

    public void onReceive(Context context, Intent intent) {
        Log.d(TAG, "myReceiver start");
        if (intent != null) {
            String action = intent.getAction();
            if (action != null && action.equals(ACTION_CFG_UPDATED)) {
                try {
                    String cotaParaCfgDir = getCfgDirFromIntent(intent);
                    String cotaParaUpdateMode = getUpdateModeFromIntent(intent);
                    Log.d(TAG, "cfgDir = " + cotaParaCfgDir + ", updateMode = " + cotaParaUpdateMode);
                    if (cotaParaCfgDir == null || this.featureCfgDir == null) {
                        Log.e(TAG, "cotaParaCfgDir or featureCfgDir is null");
                    } else if (cotaParaCfgDir.equals(this.featureCfgDir)) {
                        notifyEmcomParaUpgrade(cotaParaCfgDir, cotaParaUpdateMode, context);
                    } else {
                        Log.d(TAG, "not for the designated Reciever, ignoring");
                    }
                } catch (IllegalArgumentException e) {
                    Log.e(TAG, "IllegalArgumentException: can't get cfgDir or updateMode ");
                }
            }
        }
    }

    private String getCfgDirFromIntent(Intent i) {
        String cfgDir = i.getStringExtra("cfgDir");
        if (cfgDir != null) {
            return cfgDir;
        }
        throw new IllegalArgumentException("Missing required content cfgDir, ignoring.");
    }

    private String getUpdateModeFromIntent(Intent i) {
        String updateMode = i.getStringExtra("updateMode");
        if (updateMode != null) {
            return updateMode;
        }
        throw new IllegalArgumentException("Missing required content updateMode, ignoring.");
    }

    private void notifyEmcomParaUpgrade(String cfgDir, String updateMode, Context context) {
        if (cfgDir == null || updateMode == null) {
            Log.e(TAG, "input of cfgDir or updateMode is null, ignoring");
            return;
        }
        Log.d(TAG, "notifyEmcomParaUpgrade: cfgDir = " + cfgDir + ", updateMode = " + updateMode);
        ParaManager paraManager = ParaManager.getInstance();
        if (paraManager == null) {
            Log.e(TAG, "The instance of paraManager is null, the feature may not been Enabled");
        } else {
            paraManager.handlerCotaParaUpgrade(cfgDir, updateMode);
        }
    }
}
