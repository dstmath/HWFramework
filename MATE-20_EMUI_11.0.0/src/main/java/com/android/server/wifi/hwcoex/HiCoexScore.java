package com.android.server.wifi.hwcoex;

import android.content.Context;
import android.os.Message;

public class HiCoexScore {
    private static final int INITIAL_BUFF_SIZE = 50;
    private static final String TAG = "HiCoexScore";
    protected Context mContext;
    protected int mCurrentScene;
    protected String[] mSceneNames;
    protected int[] mSceneScores;

    public HiCoexScore(Context context) {
        this.mContext = context;
    }

    public void onReceiveEvent(Message msg) {
    }

    public int getCurrentScore() {
        if (HiCoexUtils.isDebugEnable()) {
            dumpSceneScore();
        }
        return getScore(this.mCurrentScene);
    }

    /* access modifiers changed from: protected */
    public int getScore(int sceneNo) {
        int maxScore = getDefaultScore();
        int[] iArr = this.mSceneScores;
        if (iArr == null || iArr.length == 0) {
            return maxScore;
        }
        int i = 0;
        while (true) {
            int[] iArr2 = this.mSceneScores;
            if (i >= iArr2.length) {
                return maxScore;
            }
            if (((1 << i) & sceneNo) > 0 && iArr2[i] > maxScore) {
                maxScore = iArr2[i];
            }
            i++;
        }
    }

    /* access modifiers changed from: protected */
    public int getDefaultScore() {
        return 0;
    }

    /* access modifiers changed from: protected */
    public int getMaxScoreScene() {
        int sceneNo = 0;
        int maxScore = getDefaultScore();
        int[] iArr = this.mSceneScores;
        if (iArr == null || iArr.length == 0) {
            return 0;
        }
        int i = 0;
        while (true) {
            int[] iArr2 = this.mSceneScores;
            if (i >= iArr2.length) {
                return sceneNo;
            }
            int tmpSceneNo = 1 << i;
            if ((this.mCurrentScene & tmpSceneNo) > 0 && iArr2[i] > maxScore) {
                sceneNo = tmpSceneNo;
                maxScore = iArr2[i];
            }
            i++;
        }
    }

    private void dumpSceneScore() {
        StringBuilder sb = new StringBuilder(50);
        int maxScore = getDefaultScore();
        sb.append("defaultScore:");
        sb.append(maxScore);
        if ((this.mSceneScores == null || this.mSceneNames == null) || this.mSceneScores.length != this.mSceneNames.length) {
            sb.append(",result:");
            sb.append(maxScore);
            HiCoexUtils.logD(TAG, sb.toString());
            return;
        }
        for (int i = 0; i < this.mSceneScores.length; i++) {
            if ((this.mCurrentScene & (1 << i)) > 0) {
                sb.append(",(");
                sb.append(i);
                sb.append(") ");
                sb.append(this.mSceneNames[i]);
                sb.append(":");
                sb.append(this.mSceneScores[i]);
                int[] iArr = this.mSceneScores;
                if (iArr[i] > maxScore) {
                    maxScore = iArr[i];
                }
            }
        }
        sb.append(",result:");
        sb.append(maxScore);
        HiCoexUtils.logD(TAG, sb.toString());
    }
}
