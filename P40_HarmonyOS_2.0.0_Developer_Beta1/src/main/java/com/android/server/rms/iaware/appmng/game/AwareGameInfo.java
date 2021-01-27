package com.android.server.rms.iaware.appmng.game;

import android.rms.iaware.AwareLog;
import com.android.server.rms.iaware.appmng.AwareAppAssociate;
import com.android.server.rms.iaware.appmng.AwareIntelligentRecg;
import org.json.JSONException;
import org.json.JSONObject;

public class AwareGameInfo {
    private static final String GAME_SDK_GAME_MODE_INDEX = "10000";
    private static final String GAME_SDK_INDEX_PKG = "pkg";
    private static final String GAME_SDK_INDEX_UID = "uid";
    private static final String GAME_SDK_PROC_STATUS_INDEX = "10001";
    private static final int GAME_SDK_SCENCE_SELF_LOADING = 5;
    private static final String GAME_SDK_STATE_DEFAULT = "0";
    private static final String INVALID_STRING_VALUE = "None";
    private static final int INVALID_VALUE = -1;
    private static final String TAG = "AwareGameInfo";
    private int curUID = -1;
    private String gameMode;
    protected String packageName;
    private String procStatus;
    protected int scence = -1;

    private AwareGameInfo() {
    }

    private int getScence(String pkgName, JSONObject jsonStr) throws JSONException {
        return jsonStr.optInt(AwareGameStatus.getInstance().getSceneIndex(pkgName), -1);
    }

    public boolean isGameing() {
        if (AwareIntelligentRecg.getInstance().isCurrentUser(this.curUID, AwareAppAssociate.getInstance().getCurUserId()) && this.scence >= 5) {
            return true;
        }
        return false;
    }

    public static AwareGameInfo initGameInfo(String info) {
        AwareGameInfo game = new AwareGameInfo();
        try {
            JSONObject jsonStr = new JSONObject(info);
            game.packageName = jsonStr.optString(GAME_SDK_INDEX_PKG, INVALID_STRING_VALUE);
            game.gameMode = jsonStr.optString(GAME_SDK_GAME_MODE_INDEX, GAME_SDK_STATE_DEFAULT);
            game.procStatus = jsonStr.optString(GAME_SDK_PROC_STATUS_INDEX, GAME_SDK_STATE_DEFAULT);
            game.curUID = jsonStr.optInt("uid", -1);
            game.scence = game.getScence(game.packageName, jsonStr);
            return game;
        } catch (JSONException e) {
            AwareLog.e(TAG, "resolve game info error");
            return null;
        }
    }

    public String toString() {
        return "pkg:" + this.packageName + "; uid:" + this.curUID + "; proc status:" + this.procStatus + "; game mode:" + this.gameMode + "; scence:" + this.scence;
    }
}
