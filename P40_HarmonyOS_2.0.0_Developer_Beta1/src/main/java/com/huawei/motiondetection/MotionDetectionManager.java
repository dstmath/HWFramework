package com.huawei.motiondetection;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ResolveInfo;
import android.os.Bundle;
import com.huawei.motiondetection.motionrelay.RelayListener;
import com.huawei.motiondetection.motionrelay.RelayManager;
import java.util.ArrayList;
import java.util.List;

public class MotionDetectionManager {
    private static final int INVALID_VALUE = -1;
    private static final String MOTION_SERVICE_APK_ACTION = "com.huawei.action.MOTION_SETTINGS";
    private static final String TAG = "MotionDetectionManager";
    private Context mContext = null;
    private boolean mIsDestroyed = false;
    private ArrayList<MotionDetectionListener> mMdListenerList = null;
    private ArrayList<Integer> mMotionAppsRegList = null;
    private RelayListener mRelayListener = new RelayListener() {
        /* class com.huawei.motiondetection.MotionDetectionManager.AnonymousClass1 */

        @Override // com.huawei.motiondetection.motionrelay.RelayListener
        public void notifyResult(int relayType, Object mrecoRes) {
            MotionDetectionManager.this.processMotionRecoResult(relayType, mrecoRes);
        }
    };
    private RelayManager mRelayManager = null;

    public MotionDetectionManager(Context context) {
        this.mContext = context;
        this.mRelayManager = new RelayManager(this.mContext);
        this.mRelayManager.setRelayListener(this.mRelayListener);
        this.mMdListenerList = new ArrayList<>();
        this.mMotionAppsRegList = new ArrayList<>();
    }

    public MotionDetectionManager(Context context, boolean isEx) {
        this.mContext = context;
        this.mRelayManager = new RelayManager(this.mContext, isEx);
        this.mRelayManager.setRelayListener(this.mRelayListener);
        this.mMdListenerList = new ArrayList<>();
        this.mMotionAppsRegList = new ArrayList<>();
    }

    public static boolean isMotionRecoApkExist(Context context) {
        List<ResolveInfo> packages = context.getApplicationContext().getPackageManager().queryIntentActivities(new Intent(MOTION_SERVICE_APK_ACTION), 0);
        if (packages != null && packages.size() != 0) {
            return true;
        }
        MRLog.e("MotionRecoApkCheck", "Motion service not installed, it can not do motion recognize.");
        return false;
    }

    public void startMotionService() {
        if (this.mIsDestroyed) {
            MRLog.w(TAG, "startMotionService destroy called already ");
        } else if (!MRUtils.isServiceRunning(this.mContext, MotionConfig.MOTION_SERVICE_PROCESS)) {
            this.mRelayManager.startMotionService();
        }
    }

    public void startMotionServiceAsUser(int userId) {
        if (this.mIsDestroyed) {
            MRLog.w(TAG, "startMotionServiceAsUser destroy called already ");
        } else if (!MRUtils.isServiceRunningAsUser(this.mContext, MotionConfig.MOTION_SERVICE_PROCESS, userId)) {
            this.mRelayManager.startMotionService();
        }
    }

    public void stopMotionService() {
        if (this.mIsDestroyed) {
            MRLog.w(TAG, "stopMotionService destroy called already ");
        } else if (MRUtils.isServiceRunning(this.mContext, MotionConfig.MOTION_SERVICE_PROCESS)) {
            this.mRelayManager.stopMotionService();
        }
    }

    public void stopMotionServiceAsUser(int userId) {
        if (this.mIsDestroyed) {
            MRLog.w(TAG, "stopMotionServiceAsUser destroy called already ");
        } else if (MRUtils.isServiceRunningAsUser(this.mContext, MotionConfig.MOTION_SERVICE_PROCESS, userId)) {
            this.mRelayManager.stopMotionService();
        }
    }

    public void startMotionRecoTutorial(int motionApps) {
        if (this.mIsDestroyed) {
            MRLog.w(TAG, "startMotionRecoTutorial destroy called already ");
            return;
        }
        MRLog.d(TAG, "startMotionRecoTutorial motionApps: " + motionApps);
        if (this.mMotionAppsRegList.contains(Integer.valueOf(motionApps))) {
            MRLog.d(TAG, "startMotionRecoTutorial repeat motionApps " + motionApps);
            return;
        }
        int motionTypeReco = MotionTypeApps.getMotionTypeByMotionApps(motionApps);
        if (motionTypeReco == motionApps) {
            MRLog.e(TAG, "startMotionRecoTutorial motionApps " + motionApps + " is not supported. ");
            return;
        }
        this.mRelayManager.startMotionRecognition(motionTypeReco);
        this.mMotionAppsRegList.add(Integer.valueOf(motionApps));
    }

    public void stopMotionRecoTutorial(int motionApps) {
        if (this.mIsDestroyed) {
            MRLog.w(TAG, "stopMotionRecoTutorial destroy called already ");
            return;
        }
        MRLog.d(TAG, "stopMotionRecoTutorial motionApps: " + motionApps);
        int motionTypeReco = MotionTypeApps.getMotionTypeByMotionApps(motionApps);
        if (motionTypeReco == motionApps || this.mMotionAppsRegList.contains(Integer.valueOf(motionApps))) {
            this.mRelayManager.stopMotionRecognition(motionTypeReco);
            this.mMotionAppsRegList.remove(Integer.valueOf(motionApps));
            return;
        }
        MRLog.d(TAG, "stopMotionRecoTutorial not recognition motionApps " + motionApps);
    }

    public boolean startMotionAppsReco(int motionApps) {
        return startMotionAppsReco(motionApps, false);
    }

    public boolean startMotionAppsReco(int motionApps, boolean isEx) {
        if (this.mIsDestroyed) {
            MRLog.w(TAG, "startMotionAppsReco destroy called already ");
            return false;
        }
        MRLog.d(TAG, "startMotionAppsReco motionApps: " + motionApps);
        if (this.mMotionAppsRegList.contains(Integer.valueOf(motionApps))) {
            MRLog.w(TAG, "startMotionAppsReco repeat motionApps " + motionApps);
            return false;
        }
        int motionTypeReco = MotionTypeApps.getMotionTypeByMotionApps(motionApps);
        if (motionTypeReco == motionApps) {
            MRLog.e(TAG, "startMotionAppsReco motionApps " + motionApps + " is not supported.");
            return false;
        }
        String motionKey = MotionTypeApps.getMotionKeyByMotionApps(motionTypeReco);
        if (resetMotionState(MRUtils.getMotionEnableState(this.mContext, motionKey, isEx), motionKey, isEx) == 1) {
            if (MRUtils.getMotionEnableState(this.mContext, MotionTypeApps.getMotionKeyByMotionApps(motionApps), isEx) == 1) {
                MRLog.d(TAG, "startMotionAppsReco motionTypeReco: " + motionTypeReco);
                this.mRelayManager.startMotionRecognition(motionTypeReco, isEx);
                this.mMotionAppsRegList.add(Integer.valueOf(motionApps));
                return true;
            }
            MRLog.w(TAG, "startMotionAppsReco motionApps: " + motionApps + " disabled ");
        } else {
            MRLog.w(TAG, "startMotionAppsReco motionTypeReco: " + motionTypeReco + " disabled ");
        }
        return false;
    }

    public boolean startMotionAppsRecoAsUser(int motionApps, int userId) {
        if (this.mIsDestroyed) {
            MRLog.w(TAG, "startMotionAppsRecoAsUser destroy called already ");
            return false;
        }
        MRLog.d(TAG, "startMotionAppsRecoAsUser motionApps: " + motionApps);
        if (this.mMotionAppsRegList.contains(Integer.valueOf(motionApps))) {
            MRLog.w(TAG, "startMotionAppsRecoAsUser repeat motionApps " + motionApps);
            return false;
        }
        int motionTypeReco = MotionTypeApps.getMotionTypeByMotionApps(motionApps);
        if (motionTypeReco == motionApps) {
            MRLog.e(TAG, "startMotionAppsRecoAsUser motionApps " + motionApps + " is not supported.");
            return false;
        }
        String motionKey = MotionTypeApps.getMotionKeyByMotionApps(motionTypeReco);
        if (resetMotionState(MRUtils.getMotionEnableStateAsUser(this.mContext, motionKey, userId), motionKey, false) == 1) {
            if (MRUtils.getMotionEnableStateAsUser(this.mContext, MotionTypeApps.getMotionKeyByMotionApps(motionApps), userId) == 1) {
                MRLog.d(TAG, "startMotionAppsRecoAsUser motionTypeReco: " + motionTypeReco);
                this.mRelayManager.startMotionRecognitionAsUser(motionTypeReco, userId);
                this.mMotionAppsRegList.add(Integer.valueOf(motionApps));
                return true;
            }
            MRLog.w(TAG, "startMotionAppsRecoAsUser motionApps: " + motionApps + " disabled");
        } else {
            MRLog.w(TAG, "startMotionAppsRecoAsUser motionTypeReco: " + motionTypeReco + " disabled");
        }
        return false;
    }

    public boolean stopMotionAppsReco(int motionApps) {
        return stopMotionAppsReco(motionApps, false);
    }

    public boolean stopMotionAppsReco(int motionApps, boolean isEx) {
        if (this.mIsDestroyed) {
            MRLog.w(TAG, "stopMotionAppsReco destroy called already ");
            return false;
        }
        MRLog.d(TAG, "stopMotionAppsReco motionApps: " + motionApps);
        int motionTypeReco = MotionTypeApps.getMotionTypeByMotionApps(motionApps);
        if (motionTypeReco == motionApps || this.mMotionAppsRegList.contains(Integer.valueOf(motionApps))) {
            if (isMotionStopValid(motionApps, motionTypeReco)) {
                MRLog.d(TAG, "stopMotionAppsReco motionTypeReco: " + motionTypeReco);
                this.mRelayManager.stopMotionRecognition(motionTypeReco);
            } else {
                MRLog.w(TAG, "stopMotionAppsReco can not stop motionReco: " + motionTypeReco);
            }
            this.mMotionAppsRegList.remove(Integer.valueOf(motionApps));
            return true;
        }
        MRLog.d(TAG, "stopMotionAppsReco not recognition motionApps " + motionApps);
        return false;
    }

    public boolean stopMotionAppsRecoAsUser(int motionApps, int userId) {
        if (this.mIsDestroyed) {
            MRLog.w(TAG, "stopMotionAppsRecoAsUser destroy called already ");
            return false;
        }
        MRLog.d(TAG, "stopMotionAppsRecoAsUser motionApps: " + motionApps);
        int motionTypeReco = MotionTypeApps.getMotionTypeByMotionApps(motionApps);
        if (motionTypeReco == motionApps || this.mMotionAppsRegList.contains(Integer.valueOf(motionApps))) {
            if (isMotionStopValid(motionApps, motionTypeReco)) {
                MRLog.d(TAG, "stopMotionAppsRecoAsUser motionTypeReco: " + motionTypeReco);
                this.mRelayManager.stopMotionRecognitionAsUser(motionTypeReco, userId);
            } else {
                MRLog.w(TAG, "stopMotionAppsRecoAsUser can not stop motionReco: " + motionTypeReco);
            }
            this.mMotionAppsRegList.remove(Integer.valueOf(motionApps));
            return true;
        }
        MRLog.d(TAG, "stopMotionAppsRecoAsUser not recognition motionApps " + motionApps);
        return false;
    }

    public void destroy() {
        if (this.mIsDestroyed) {
            MRLog.w(TAG, "destroy() called already ");
            return;
        }
        stopAllMotionReco();
        this.mIsDestroyed = true;
        this.mMotionAppsRegList.clear();
        this.mMotionAppsRegList = null;
        this.mMdListenerList.clear();
        this.mMdListenerList = null;
        this.mRelayManager.destroy();
        this.mRelayManager = null;
        this.mContext = null;
    }

    public void addMotionListener(MotionDetectionListener mdListener) {
        if (this.mIsDestroyed) {
            MRLog.w(TAG, "addMotionListener destroy called already ");
            return;
        }
        ArrayList<MotionDetectionListener> arrayList = this.mMdListenerList;
        if (arrayList != null && !arrayList.contains(mdListener)) {
            this.mMdListenerList.add(mdListener);
        }
    }

    public void removeMotionListener(MotionDetectionListener mdListener) {
        if (this.mIsDestroyed) {
            MRLog.w(TAG, "removeMotionListener destroy called already ");
            return;
        }
        ArrayList<MotionDetectionListener> arrayList = this.mMdListenerList;
        if (arrayList != null && arrayList.contains(mdListener)) {
            this.mMdListenerList.remove(mdListener);
        }
    }

    private boolean isMotionStopValid(int motionApps, int motionTypeReco) {
        int motionAppsSize = this.mMotionAppsRegList.size();
        for (int i = 0; i < motionAppsSize; i++) {
            int tmpMverApps = this.mMotionAppsRegList.get(i).intValue();
            if (MotionTypeApps.getMotionTypeByMotionApps(tmpMverApps) == motionTypeReco && tmpMverApps != motionApps) {
                MRLog.w(TAG, "isMotionStopValid same motionReco running by other motionApps: " + tmpMverApps);
                return false;
            }
        }
        return true;
    }

    private int resetMotionState(int motionState, String motionKey, boolean isEx) {
        if (motionState != -1) {
            return motionState;
        }
        MRUtils.setMotionEnableState(this.mContext, motionKey, 1, isEx);
        return 1;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void processMotionRecoResult(int relayType, Object mrecoRes) {
        MRLog.d(TAG, "processReceiverMsg ... ");
        if (relayType == 1) {
            notifyMotionRecoResult((Intent) mrecoRes);
        }
    }

    private void stopAllMotionReco() {
        ArrayList<Integer> arrayList = this.mMotionAppsRegList;
        if (arrayList != null && arrayList.size() > 0) {
            int appsRegListSize = this.mMotionAppsRegList.size();
            int i = 0;
            while (i < appsRegListSize) {
                if (stopMotionAppsReco(this.mMotionAppsRegList.get(i).intValue())) {
                    i--;
                    appsRegListSize--;
                }
                i++;
            }
        }
    }

    private void notifyMotionRecoResult(Intent recoIntent) {
        int motionTypeReco = getRecoMotionType(recoIntent);
        int recoRes = getRecoMotionResult(recoIntent);
        int recoDirect = getRecoMotionDirect(recoIntent);
        Bundle recoExtras = getRecoMotionExtras(recoIntent);
        ArrayList<Integer> maTypeList = getMotionsAppsByMotionReco(motionTypeReco);
        if (this.mMdListenerList.size() > 0 && this.mMotionAppsRegList.size() > 0) {
            MRLog.d(TAG, "notifyMotionRecoResult motionTypeReco: " + motionTypeReco + "  recoRes: " + recoRes + " recoDirect: " + recoDirect + "mMdListenerList size: " + this.mMdListenerList.size() + "mMotionAppsRegList size: " + this.mMotionAppsRegList.size());
        }
        int maTypeListSize = maTypeList.size();
        int listenerSize = this.mMdListenerList.size();
        for (int j = 0; j < maTypeListSize; j++) {
            for (int i = 0; i < listenerSize; i++) {
                this.mMdListenerList.get(i).notifyMotionRecoResult(getRecoResult(maTypeList.get(j).intValue(), recoRes, recoDirect, recoExtras));
            }
        }
    }

    private MotionRecoResult getRecoResult(int app, int res, int direct, Bundle extras) {
        return new MotionRecoResult(app, res, direct, extras);
    }

    private ArrayList<Integer> getMotionsAppsByMotionReco(int type) {
        ArrayList<Integer> maList = new ArrayList<>();
        int appsRegListSize = this.mMotionAppsRegList.size();
        if (appsRegListSize > 0) {
            for (int i = 0; i < appsRegListSize; i++) {
                if (MotionTypeApps.getMotionTypeByMotionApps(this.mMotionAppsRegList.get(i).intValue()) == type) {
                    maList.add(this.mMotionAppsRegList.get(i));
                }
            }
        }
        return maList;
    }

    private int getRecoMotionType(Intent intent) {
        return intent.getIntExtra(MotionConfig.MOTION_TYPE_RECOGNITION, 0);
    }

    private int getRecoMotionResult(Intent intent) {
        return intent.getIntExtra(MotionConfig.MOTION_RECOGNITION_RESULT, 0);
    }

    private int getRecoMotionDirect(Intent recoRes) {
        return recoRes.getIntExtra(MotionConfig.MOTION_RECOGNITION_DIRECTION, 0);
    }

    private Bundle getRecoMotionExtras(Intent recoIntent) {
        return recoIntent.getBundleExtra(MotionConfig.MOTION_RECOGNITION_EXTRAS);
    }
}
