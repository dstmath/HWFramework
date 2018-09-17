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
    private static final String MOTION_SERVICE_APK_ACTION = "com.huawei.action.MOTION_SETTINGS";
    private static final String TAG = "MotionDetectionManager";
    private Context mContext = null;
    private boolean mDestroyed = false;
    private ArrayList<MotionDetectionListener> mMDListenerList = null;
    private ArrayList<Integer> mMotionAppsRegList = null;
    private RelayListener mRelayListener = new RelayListener() {
        public void notifyResult(int relayType, Object mrecoRes) {
            MotionDetectionManager.this.processMotionRecoResult(relayType, mrecoRes);
        }
    };
    private RelayManager mRelayManager = null;

    public static boolean isMotionRecoApkExist(Context context) {
        List<ResolveInfo> packages = context.getApplicationContext().getPackageManager().queryIntentActivities(new Intent(MOTION_SERVICE_APK_ACTION), 0);
        if (packages != null && packages.size() != 0) {
            return true;
        }
        MRLog.e("MotionRecoApkCheck", "Motion service not installed, it can not do motion recognize.");
        return false;
    }

    public MotionDetectionManager(Context context) {
        this.mContext = context;
        this.mRelayManager = new RelayManager(this.mContext);
        this.mRelayManager.setRelayListener(this.mRelayListener);
        this.mMDListenerList = new ArrayList();
        this.mMotionAppsRegList = new ArrayList();
    }

    public MotionDetectionManager(Context context, boolean isEx) {
        this.mContext = context;
        this.mRelayManager = new RelayManager(this.mContext, isEx);
        this.mRelayManager.setRelayListener(this.mRelayListener);
        this.mMDListenerList = new ArrayList();
        this.mMotionAppsRegList = new ArrayList();
    }

    public void startMotionService() {
        if (this.mDestroyed) {
            MRLog.w(TAG, "startMotionService destroy called already ");
            return;
        }
        if (!MRUtils.isServiceRunning(this.mContext, MotionConfig.MOTION_SERVICE_PROCESS)) {
            this.mRelayManager.startMotionService();
        }
    }

    public void startMotionServiceAsUser(int userId) {
        if (this.mDestroyed) {
            MRLog.w(TAG, "startMotionServiceAsUser destroy called already ");
            return;
        }
        if (!MRUtils.isServiceRunningAsUser(this.mContext, MotionConfig.MOTION_SERVICE_PROCESS, userId)) {
            this.mRelayManager.startMotionService();
        }
    }

    public void stopMotionService() {
        if (this.mDestroyed) {
            MRLog.w(TAG, "stopMotionService destroy called already ");
            return;
        }
        if (MRUtils.isServiceRunning(this.mContext, MotionConfig.MOTION_SERVICE_PROCESS)) {
            this.mRelayManager.stopMotionService();
        }
    }

    public void stopMotionServiceAsUser(int userId) {
        if (this.mDestroyed) {
            MRLog.w(TAG, "stopMotionServiceAsUser destroy called already ");
            return;
        }
        if (MRUtils.isServiceRunningAsUser(this.mContext, MotionConfig.MOTION_SERVICE_PROCESS, userId)) {
            this.mRelayManager.stopMotionService();
        }
    }

    public void startMotionRecoTutorial(int motionApps) {
        if (this.mDestroyed) {
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
            MRLog.e(TAG, "startMotionRecoTutorial motionApps " + motionApps + " is not supported.");
            return;
        }
        this.mRelayManager.startMotionRecognition(motionTypeReco);
        synchronized (this.mMotionAppsRegList) {
            this.mMotionAppsRegList.add(Integer.valueOf(motionApps));
        }
    }

    public void stopMotionRecoTutorial(int motionApps) {
        if (this.mDestroyed) {
            MRLog.w(TAG, "stopMotionRecoTutorial destroy called already ");
            return;
        }
        MRLog.d(TAG, "stopMotionRecoTutorial motionApps: " + motionApps);
        int motionTypeReco = MotionTypeApps.getMotionTypeByMotionApps(motionApps);
        if (motionTypeReco == motionApps || this.mMotionAppsRegList.contains(Integer.valueOf(motionApps))) {
            this.mRelayManager.stopMotionRecognition(motionTypeReco);
            synchronized (this.mMotionAppsRegList) {
                this.mMotionAppsRegList.remove(Integer.valueOf(motionApps));
            }
            return;
        }
        MRLog.d(TAG, "stopMotionRecoTutorial not recognition motionApps " + motionApps);
    }

    public boolean startMotionAppsReco(int motionApps) {
        return startMotionAppsReco(motionApps, false);
    }

    public boolean startMotionAppsReco(int motionApps, boolean isEx) {
        if (this.mDestroyed) {
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
                synchronized (this.mMotionAppsRegList) {
                    this.mMotionAppsRegList.add(Integer.valueOf(motionApps));
                }
                return true;
            }
            MRLog.w(TAG, "startMotionAppsReco motionApps: " + motionApps + " disabled");
        } else {
            MRLog.w(TAG, "startMotionAppsReco motionTypeReco: " + motionTypeReco + " disabled");
        }
        return false;
    }

    public boolean startMotionAppsRecoAsUser(int motionApps, int userId) {
        if (this.mDestroyed) {
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
                synchronized (this.mMotionAppsRegList) {
                    this.mMotionAppsRegList.add(Integer.valueOf(motionApps));
                }
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
        if (this.mDestroyed) {
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
            synchronized (this.mMotionAppsRegList) {
                this.mMotionAppsRegList.remove(Integer.valueOf(motionApps));
            }
            return true;
        }
        MRLog.d(TAG, "stopMotionAppsReco not recognition motionApps " + motionApps);
        return false;
    }

    public boolean stopMotionAppsRecoAsUser(int motionApps, int userId) {
        if (this.mDestroyed) {
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
            synchronized (this.mMotionAppsRegList) {
                this.mMotionAppsRegList.remove(Integer.valueOf(motionApps));
            }
            return true;
        }
        MRLog.d(TAG, "stopMotionAppsRecoAsUser not recognition motionApps " + motionApps);
        return false;
    }

    public void destroy() {
        if (this.mDestroyed) {
            MRLog.w(TAG, "destroy() called already ");
            return;
        }
        stopAllMotionReco();
        this.mDestroyed = true;
        synchronized (this.mMotionAppsRegList) {
            this.mMotionAppsRegList.clear();
            this.mMotionAppsRegList = null;
        }
        this.mMDListenerList.clear();
        this.mMDListenerList = null;
        this.mRelayManager.destroy();
        this.mRelayManager = null;
        this.mContext = null;
    }

    public void addMotionListener(MotionDetectionListener mdListener) {
        if (this.mDestroyed) {
            MRLog.w(TAG, "addMotionListener destroy called already ");
            return;
        }
        if (!(this.mMDListenerList == null || (this.mMDListenerList.contains(mdListener) ^ 1) == 0)) {
            this.mMDListenerList.add(mdListener);
        }
    }

    public void removeMotionListener(MotionDetectionListener mdListener) {
        if (this.mDestroyed) {
            MRLog.w(TAG, "removeMotionListener destroy called already ");
            return;
        }
        if (this.mMDListenerList != null && this.mMDListenerList.contains(mdListener)) {
            this.mMDListenerList.remove(mdListener);
        }
    }

    private boolean isMotionStopValid(int motionApps, int motionTypeReco) {
        int motionAppsSize = this.mMotionAppsRegList.size();
        int i = 0;
        while (i < motionAppsSize) {
            int tmpMApps = ((Integer) this.mMotionAppsRegList.get(i)).intValue();
            if (MotionTypeApps.getMotionTypeByMotionApps(tmpMApps) != motionTypeReco || tmpMApps == motionApps) {
                i++;
            } else {
                MRLog.w(TAG, "isMotionStopValid same motionReco running by other motionApps: " + tmpMApps);
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

    private void processMotionRecoResult(int relayType, Object mrecoRes) {
        MRLog.d(TAG, "processReceiverMsg ... ");
        if (relayType == 1) {
            notifyMotionRecoResult((Intent) mrecoRes);
        }
    }

    private void stopAllMotionReco() {
        if (this.mMotionAppsRegList != null && this.mMotionAppsRegList.size() > 0) {
            int appsRegListSize = this.mMotionAppsRegList.size();
            int i = 0;
            while (i < appsRegListSize) {
                if (stopMotionAppsReco(((Integer) this.mMotionAppsRegList.get(i)).intValue())) {
                    i--;
                    appsRegListSize--;
                }
                i++;
            }
        }
    }

    private void notifyMotionRecoResult(Intent recoIntent) {
        int motionTypeReco = getRecoMotionType(recoIntent);
        int rRes = getRecoMotionResult(recoIntent);
        int rDirect = getRecoMotionDirect(recoIntent);
        Bundle rExtras = getRecoMotionExtras(recoIntent);
        try {
            ArrayList<Integer> maTypeList = getMotionsAppsByMotionReco(motionTypeReco);
            if (this.mMDListenerList.size() > 0 && this.mMotionAppsRegList.size() > 0) {
                MRLog.d(TAG, "notifyMotionRecoResult motionTypeReco: " + motionTypeReco + "  recoRes: " + rRes + " rDirect: " + rDirect + "mMDListenerList size: " + this.mMDListenerList.size() + "mMotionAppsRegList size: " + this.mMotionAppsRegList.size());
            }
            int maTypeListSize = maTypeList.size();
            int listenerSize = this.mMDListenerList.size();
            for (int j = 0; j < maTypeListSize; j++) {
                for (int i = 0; i < listenerSize; i++) {
                    ((MotionDetectionListener) this.mMDListenerList.get(i)).notifyMotionRecoResult(getRecoResult(((Integer) maTypeList.get(j)).intValue(), rRes, rDirect, rExtras));
                }
            }
        } catch (Exception ex) {
            MRLog.w(TAG, ex.getMessage());
        }
    }

    private MotionRecoResult getRecoResult(int pMApps, int pRes, int pDirect, Bundle pExtras) {
        return new MotionRecoResult(pMApps, pRes, pDirect, pExtras);
    }

    private ArrayList<Integer> getMotionsAppsByMotionReco(int mType) {
        ArrayList<Integer> maList = new ArrayList();
        int appsRegListSize = this.mMotionAppsRegList.size();
        if (appsRegListSize > 0) {
            for (int i = 0; i < appsRegListSize; i++) {
                if (MotionTypeApps.getMotionTypeByMotionApps(((Integer) this.mMotionAppsRegList.get(i)).intValue()) == mType) {
                    maList.add((Integer) this.mMotionAppsRegList.get(i));
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
