package com.huawei.motiondetection.motionrelay;

import android.content.Context;
import com.huawei.motiondetection.MRLog;

public class RelayManager {
    private Context mContext = null;
    private RelayListener mRListener = null;
    private IRelay mRelay = null;
    private RelayListener mRelayListener = new RelayListener() {
        public void notifyResult(int relayType, Object mrecoRes) {
            RelayManager.this.processRecoResult(relayType, mrecoRes);
        }
    };

    public RelayManager(Context context) {
        this.mContext = context;
        this.mRelay = new RelayBroadcast(this.mContext);
        this.mRelay.setRelayListener(this.mRelayListener);
    }

    public RelayManager(Context context, boolean isEx) {
        this.mContext = context;
        this.mRelay = new RelayBroadcast(this.mContext, isEx);
        this.mRelay.setRelayListener(this.mRelayListener);
    }

    public void startMotionService() {
        this.mRelay.startMotionService();
    }

    public void stopMotionService() {
        this.mRelay.stopMotionService();
    }

    public void startMotionRecognition(int motionType) {
        startMotionRecognition(motionType, false);
    }

    public void startMotionRecognition(int motionType, boolean isEx) {
        this.mRelay.startMotionReco(motionType, isEx);
    }

    public void startMotionRecognitionAsUser(int motionType, int userId) {
        this.mRelay.startMotionRecoAsUser(motionType, userId);
    }

    public void stopMotionRecognition(int motionType) {
        stopMotionRecognition(motionType, false);
    }

    public void stopMotionRecognition(int motionType, boolean isEx) {
        this.mRelay.stopMotionReco(motionType, isEx);
    }

    public void stopMotionRecognitionAsUser(int motionType, int userId) {
        this.mRelay.stopMotionRecoAsUser(motionType, userId);
    }

    public void destroy() {
        this.mRelay.destroy();
        this.mRelay = null;
        this.mRelayListener = null;
        this.mRListener = null;
        this.mContext = null;
    }

    public void setRelayListener(RelayListener prListener) {
        this.mRListener = prListener;
    }

    private void processRecoResult(int relayType, Object mrecoRes) {
        try {
            if (this.mRListener != null) {
                this.mRListener.notifyResult(relayType, mrecoRes);
            }
        } catch (Exception ex) {
            MRLog.w("RelayManager", ex.getMessage());
        }
    }
}
