package com.android.server.emcom.policy;

import android.content.Context;
import android.database.ContentObserver;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings.Global;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;
import com.android.server.emcom.networkevaluation.NetworkEvaluationEntry;
import com.android.server.emcom.util.EMCOMConstants;
import com.android.server.emcom.xengine.XEngineConfigInfo;
import com.android.server.emcom.xengine.XEngineConfigInfo.HicomFeaturesInfo;
import com.android.server.emcom.xengine.XEngineForegroundAppInfo;
import com.android.server.emcom.xengine.XEngineForegroundAppInfo.HiParam;
import com.android.server.emcom.xengine.XEngineMpipControl;
import com.android.server.emcom.xengine.XEngineMpipControl.IMpipStatusCallback;
import com.android.server.emcom.xengine.XEngineSpeedControl;
import com.android.server.emcom.xengine.XEngineWifiAcc;
import java.util.ArrayList;

public class HicomPolicyManager implements EMCOMConstants {
    private static final int DEFAULT_SUBCARD = 0;
    private static final int NETWORK_DISCONNECT = 0;
    private static final int NETWORK_MOBILE_CONNECT = 2;
    private static final int NETWORK_WIFI_CONNECT = 1;
    private static final String SETTINGS_INCALL_DATA_SWITCH = "incall_data_switch";
    private static final int SWITCH_OFF = 0;
    private static final int SWITCH_ON = 1;
    private static final String TAG = "HicomPolicyManager";
    private static volatile HicomPolicyManager s_hicomPolicyManager;
    private Context mContext;
    private String mCurrentForegroundPackageName;
    private int mCurrentForegroundUid;
    private DataConnectionStatus[] mDataConnectionStatus;
    private boolean mForePackageChanged;
    private ArrayList<XEngineForegroundAppInfo> mForegroundAppInfos = new ArrayList();
    private XEngineForegroundAppInfo mGameSpaceInfo;
    private Handler mHandler;
    private boolean mHasStartedEvaluation;
    private boolean mHasStartedMpIp;
    private boolean mHasStartedSpeedControl;
    private boolean mHasStartedUdpAcc;
    private ArrayList<XEngineConfigInfo> mHicomFeaturesAppInfos = new ArrayList();
    private ContentObserver mInCallDataSwitchOnObserver = new ContentObserver(new Handler()) {
        public void onChange(boolean selfChange) {
            boolean inCallDataSwitchOn = Global.getInt(HicomPolicyManager.this.mContext.getContentResolver(), HicomPolicyManager.SETTINGS_INCALL_DATA_SWITCH, 0) != 0;
            Log.d(HicomPolicyManager.TAG, "onChange...inCallDataSwitchOn = " + inCallDataSwitchOn);
            if (inCallDataSwitchOn) {
                HicomPolicyManager.this.stopMpip();
            } else {
                HicomPolicyManager.this.startMpip();
            }
        }
    };
    private int mLastUdpAccUid;
    private ArrayList<IRttUpdateListener> mListeners = new ArrayList();
    private int mMpipStatus = 1;
    private ArrayList<Integer> mMultipathUidList = new ArrayList();
    private int mNetworkState;
    private PhoneStateListener[] mPhoneStateListener;

    public static class DataConnectionStatus {
        int connectionStatus;

        public String toString() {
            StringBuffer buffer = new StringBuffer();
            buffer.append("{connectionStatus=").append("}");
            return buffer.toString();
        }
    }

    public interface IRttUpdateListener {
        void onRttChanged(int i);
    }

    private class MpipStatusCallback implements IMpipStatusCallback {
        /* synthetic */ MpipStatusCallback(HicomPolicyManager this$0, MpipStatusCallback -this1) {
            this();
        }

        private MpipStatusCallback() {
        }

        public void onMpipStatusChanged(int status) {
            HicomPolicyManager.this.handleMpipStatusChanged(status);
        }
    }

    private HicomPolicyManager() {
    }

    public static HicomPolicyManager getInstance() {
        if (s_hicomPolicyManager == null) {
            synchronized (HicomPolicyManager.class) {
                if (s_hicomPolicyManager == null) {
                    s_hicomPolicyManager = new HicomPolicyManager();
                }
            }
        }
        return s_hicomPolicyManager;
    }

    public void init(Context context, Handler handler) {
        Log.d(TAG, "HicomPolicyManager init.");
        this.mContext = context;
        this.mHandler = handler;
        initNetWorkEvaluation();
        registerPhoneListener();
        XEngineMpipControl.getInstance(this.mContext).registerMpipStatusCallback(new MpipStatusCallback(this, null));
        this.mContext.getContentResolver().registerContentObserver(Global.getUriFor(SETTINGS_INCALL_DATA_SWITCH), true, this.mInCallDataSwitchOnObserver);
    }

    private void initNetWorkEvaluation() {
        Log.d(TAG, "initNetWorkEvaluation");
        NetworkEvaluationEntry.getInstance(this.mContext);
    }

    private void startUdpAcc(int uid, int wifiMode) {
        if (this.mHasStartedUdpAcc) {
            Log.e(TAG, "udp acc is already start.");
            return;
        }
        Log.d(TAG, "startUdpAcc   wifiMode=" + wifiMode + " uid= " + uid);
        XEngineWifiAcc.getInstance().start(uid, wifiMode);
        this.mLastUdpAccUid = uid;
        this.mHasStartedUdpAcc = true;
    }

    private void stopUdpAcc() {
        if (this.mHasStartedUdpAcc) {
            Log.d(TAG, "stopUdpAcc  mLastUdpAccuid " + this.mLastUdpAccUid);
            XEngineWifiAcc.getInstance().stop(this.mLastUdpAccUid);
            this.mHasStartedUdpAcc = false;
            return;
        }
        Log.e(TAG, "udp acc is not start yet!");
    }

    public void startEvaluateNetwork(int subId, int networkType) {
        NetworkEvaluationEntry entry = NetworkEvaluationEntry.getInstance(null);
        if (entry == null) {
            Log.e(TAG, "NetworkEvaluationEntry is null");
            return;
        }
        entry.startEvaluation(networkType, subId);
        this.mHasStartedEvaluation = true;
        Log.d(TAG, "startEvaluateNetwork networkState = " + networkType + " subId= " + subId);
    }

    public void stopEvaluateNetwork() {
        if (this.mHasStartedEvaluation) {
            NetworkEvaluationEntry entry = NetworkEvaluationEntry.getInstance(null);
            if (entry != null) {
                entry.stopEvaluation();
                this.mHasStartedEvaluation = false;
                Log.d(TAG, "stopEvaluateNetwork");
            }
            return;
        }
        Log.e(TAG, "Network Evaluation is not start yet!");
    }

    public void startMpip() {
        if (this.mMpipStatus == 2) {
            Log.d(TAG, "MpIp is forbidden");
        } else if (this.mHasStartedMpIp) {
            Log.d(TAG, "MpIp is already start");
        } else if (isInCallDataSwitchOn()) {
            Log.d(TAG, "isInCallDataSwitchOn is on, just return.");
        } else {
            Log.d(TAG, "startMpIp");
            XEngineMpipControl.getInstance(this.mContext).startMpip();
            this.mHasStartedMpIp = true;
        }
    }

    public void stopMpip() {
        if (this.mHasStartedMpIp) {
            Log.d(TAG, "stopMpIp");
            XEngineMpipControl.getInstance(this.mContext).stopMpip();
            this.mHasStartedMpIp = false;
            return;
        }
        Log.d(TAG, "MpIp is not start yet");
    }

    public void startSpeedControl(int uid, int objDelay, int maxGrade, int minGrade) {
        if (this.mMpipStatus == 0 && isExistedInCurrentList(this.mMultipathUidList, this.mCurrentForegroundUid)) {
            Log.d(TAG, "Mpip has started, not need to start SpeedControl");
        } else if (this.mHasStartedSpeedControl) {
            Log.d(TAG, "speed control is already start");
        } else {
            XEngineSpeedControl.getInstance().start(uid, objDelay, maxGrade, minGrade);
            Log.d(TAG, "startSpeedControl  max=" + maxGrade + "minGrade = " + minGrade);
            this.mHasStartedSpeedControl = true;
        }
    }

    public void stopSpeedControl() {
        if (this.mHasStartedSpeedControl) {
            XEngineSpeedControl.getInstance().stop();
            Log.d(TAG, "stopSpeedControl");
            this.mHasStartedSpeedControl = false;
            return;
        }
        Log.d(TAG, "speed control is not start");
    }

    private void obtainSpeedCtrlParam() {
        XEngineForegroundAppInfo info = getForeAppInfoByPackageName(this.mCurrentForegroundPackageName);
        if (info != null) {
            HiParam param = info.getParam();
            if (param == null || param.getObjectiveDelay() <= 0) {
                Log.d(TAG, "SpeedControl param is null or ObjectiveDelay <= 0");
            } else {
                Log.d(TAG, "SpeedControl current forepackage isExistedInCurrentList param = " + param);
                startSpeedControl(this.mCurrentForegroundUid, param.getObjectiveDelay(), param.getMaxGrade(), param.getMinGrade());
            }
        }
    }

    public void handleNetworkStatus() {
        ConnectivityManager manager = (ConnectivityManager) this.mContext.getSystemService("connectivity");
        if (manager == null) {
            Log.e(TAG, "get connectivityManager service failed ");
            return;
        }
        NetworkInfo activeNetwork = manager.getActiveNetworkInfo();
        if (activeNetwork == null) {
            handleDisconnectNetwork();
        } else if (activeNetwork.isConnected()) {
            obtainSpeedCtrlParam();
            int type = activeNetwork.getType();
            Log.d(TAG, "Current network is connected  type= " + type);
            handleNetworkConnect(type);
        } else {
            handleDisconnectNetwork();
        }
    }

    private void handleMpipStatusChanged(int status) {
        if (this.mMpipStatus == status) {
            Log.d(TAG, "mpip status is same as last report");
            return;
        }
        Log.d(TAG, "handleMpipStatusChanged current status is : " + status);
        this.mMpipStatus = status;
        switch (status) {
            case 0:
                if (isExistedInCurrentList(this.mMultipathUidList, this.mCurrentForegroundUid)) {
                    stopSpeedControl();
                    break;
                }
                break;
            case 1:
            case 2:
                if (this.mNetworkState != 0) {
                    obtainSpeedCtrlParam();
                    break;
                }
                break;
            default:
                Log.e(TAG, "it is a mpip error status ");
                break;
        }
    }

    private void handleNetworkConnect(int type) {
        switch (type) {
            case 0:
                handleMobileConnet();
                return;
            case 1:
                handleWifiConnect();
                return;
            default:
                handleDisconnectNetwork();
                return;
        }
    }

    private boolean isNetworkEvaluationEnable() {
        if (this.mHicomFeaturesAppInfos == null || this.mHicomFeaturesAppInfos.size() <= 0 || !isCurrentAppExistedInList(this.mHicomFeaturesAppInfos, this.mCurrentForegroundPackageName)) {
            return (this.mGameSpaceInfo == null || this.mGameSpaceInfo.getParam() == null) ? false : true;
        } else {
            return true;
        }
    }

    private boolean isMpipEnable() {
        if (this.mMultipathUidList != null && this.mMultipathUidList.size() > 0) {
            return true;
        }
        if (this.mGameSpaceInfo == null || this.mGameSpaceInfo.getParam() == null || this.mGameSpaceInfo.getParam().getMultiPath() <= 0) {
            return false;
        }
        return true;
    }

    private void stopAllBesidesMpip() {
        if (this.mHasStartedEvaluation) {
            stopEvaluateNetwork();
        }
        if (this.mHasStartedSpeedControl) {
            stopSpeedControl();
        }
        if (this.mHasStartedUdpAcc) {
            stopUdpAcc();
        }
    }

    private void handleMobileConnet() {
        this.mNetworkState = 2;
        if (this.mHasStartedUdpAcc) {
            stopUdpAcc();
        }
        if (isMpipEnable()) {
            startMpip();
        }
    }

    private void handleWifiConnect() {
        this.mNetworkState = 1;
        XEngineForegroundAppInfo info = getForeAppInfoByPackageName(this.mCurrentForegroundPackageName);
        if (info != null) {
            HiParam param = info.getParam();
            if (param == null || param.getWifiMode() <= 0) {
                Log.d(TAG, "wifimode param is null or wifimode <= 0");
                return;
            } else {
                Log.d(TAG, "WIFI connected and param = " + param);
                startUdpAcc(this.mCurrentForegroundUid, param.getWifiMode());
            }
        }
        if (isNetworkEvaluationEnable()) {
            Log.d(TAG, "EvaluateNetwork current forepackage isCurrentAppExistedInList");
            startEvaluateNetwork(0, 1);
        }
        obtainSpeedCtrlParam();
        stopMpip();
    }

    public boolean isWifiConnected() {
        if (this.mNetworkState == 1) {
            return true;
        }
        return false;
    }

    private void handleDisconnectNetwork() {
        Log.d(TAG, "network unknown or disconnect, stop all");
        this.mNetworkState = 0;
        stopAllBesidesMpip();
        if (isMpipEnable()) {
            startMpip();
        }
    }

    public void handleScreenStatusChange(int type) {
        Log.d(TAG, "handlerScreenStatus  type = " + type);
        if (7 == type) {
            stopAllBesidesMpip();
        } else if (6 != type) {
            Log.e(TAG, "ScreenStatus  error ");
        } else if (!this.mForePackageChanged) {
            handleScreenOn();
        }
    }

    public void delayToHandleScreenStatusChanged(int actionId) {
        this.mForePackageChanged = false;
        Message msg = this.mHandler.obtainMessage();
        msg.what = 9;
        msg.arg1 = actionId;
        msg.arg2 = 1;
        Log.d(TAG, "delayToHandleScreenStatusChanged : " + msg.arg1);
        this.mHandler.sendMessageDelayed(msg, 2000);
    }

    private void handleScreenOn() {
        if (this.mNetworkState != 0) {
            handleEvaluateNetwork();
            obtainSpeedCtrlParam();
            if (this.mNetworkState == 1) {
                handleWifiConnect();
            }
        }
    }

    private void handleEvaluateNetwork() {
        if (isNetworkEvaluationEnable()) {
            if (this.mNetworkState == 1) {
                startEvaluateNetwork(0, 1);
                return;
            }
            for (int i = 0; i < this.mDataConnectionStatus.length; i++) {
                if (this.mDataConnectionStatus[i] == null) {
                    Log.d(TAG, "current sim card" + i + " object is null");
                } else if (this.mDataConnectionStatus[i].connectionStatus == 2) {
                    Log.d(TAG, "current sim card" + i + " data connected ");
                    startEvaluateNetwork(i, 0);
                    return;
                }
            }
            Log.d(TAG, "current network disconnect stopEvaluateNetwork ");
            stopEvaluateNetwork();
        }
    }

    public void updateMpipUidList(ArrayList<Integer> multiPathUidList) {
        Log.d(TAG, "updateMpipUidList current listsize = " + multiPathUidList.size() + " multiPathUidList " + multiPathUidList);
        XEngineMpipControl.getInstance(this.mContext).updateMpipUidList(multiPathUidList);
    }

    public void updateAppExperienceStatus(int rtt) {
        onRttChanged(rtt);
    }

    public void registerListener(IRttUpdateListener listener) {
        synchronized (this.mListeners) {
            this.mListeners.add(listener);
        }
    }

    public void unRegisterListener(IRttUpdateListener listener) {
        synchronized (this.mListeners) {
            this.mListeners.remove(listener);
        }
    }

    public void onRttChanged(int rtt) {
        int size = this.mListeners.size();
        for (int i = 0; i < size; i++) {
            IRttUpdateListener listener = (IRttUpdateListener) this.mListeners.get(i);
            if (listener != null) {
                listener.onRttChanged(rtt);
            }
        }
    }

    public void handleAppForeground(String pkgName, XEngineForegroundAppInfo foreAppInfo, int uid) {
        Log.d(TAG, "foreAppInfo = " + foreAppInfo + "pkgName = " + pkgName + " uid= " + uid);
        if (this.mCurrentForegroundUid == uid && this.mCurrentForegroundPackageName.equals(pkgName)) {
            Log.d(TAG, "forground uid is same as before");
            return;
        }
        this.mForePackageChanged = true;
        this.mCurrentForegroundPackageName = pkgName;
        this.mCurrentForegroundUid = uid;
        stopUdpAcc();
        stopSpeedControl();
        if (!isNetworkEvaluationEnable()) {
            stopEvaluateNetwork();
        }
        if (foreAppInfo == null) {
            Log.e(TAG, "foreground app info is null");
            return;
        }
        HiParam param = foreAppInfo.getParam();
        if (param == null) {
            Log.d(TAG, "unsupport Hicom accelerate.");
            return;
        }
        if (!(this.mNetworkState == 0 || (this.mHasStartedEvaluation ^ 1) == 0)) {
            handleEvaluateNetwork();
        }
        if (this.mNetworkState != 1 && isMpipEnable()) {
            startMpip();
        }
        if (param.getWifiMode() > 0 && this.mNetworkState == 1) {
            startUdpAcc(uid, param.getWifiMode());
        }
        if (param.getObjectiveDelay() > 0) {
            Log.d(TAG, "handleXengineSpeedControlAppForeground");
            if (this.mNetworkState != 0) {
                startSpeedControl(uid, param.getObjectiveDelay(), param.getMaxGrade(), param.getMinGrade());
            }
        }
    }

    public void addHicomFeaturesAppInfo(XEngineConfigInfo config, String pkgName, int uid) {
        if (getHicomFeaturesAppInfoByPackageName(pkgName) != null) {
            Log.i(TAG, pkgName + "is already exsit in HicomFeatures app list.");
            return;
        }
        this.mHicomFeaturesAppInfos.add(config);
        handleEvaluateNetwork();
        HicomFeaturesInfo hiInfo = config.hicomFeaturesInfo;
        if (!(hiInfo == null || hiInfo.multiPath <= 0 || isExistedInCurrentList(this.mMultipathUidList, uid))) {
            this.mMultipathUidList.add(Integer.valueOf(uid));
            updateMpipUidList(this.mMultipathUidList);
            if (this.mNetworkState != 1) {
                startMpip();
            }
        }
    }

    public void removeHicomFeaturesAppInfo(String pkgName) {
        int size = this.mHicomFeaturesAppInfos.size();
        int i = 0;
        while (i < size) {
            XEngineConfigInfo hicomFeaturesAppInfo = (XEngineConfigInfo) this.mHicomFeaturesAppInfos.get(i);
            if (hicomFeaturesAppInfo == null || !pkgName.equals(hicomFeaturesAppInfo.getPackageName())) {
                i++;
            } else {
                this.mHicomFeaturesAppInfos.remove(hicomFeaturesAppInfo);
                Log.d(TAG, "remove HicomFeaturesAppInfo, current app config:" + this.mHicomFeaturesAppInfos);
                if (!isNetworkEvaluationEnable()) {
                    Log.d(TAG, "HicomFeaturesAppInfos is null   stopEvaluateNetwork");
                    stopEvaluateNetwork();
                    return;
                }
                return;
            }
        }
    }

    public void removeMultiPathUid(String pkgName) {
        XEngineConfigInfo info = getHicomFeaturesAppInfoByPackageName(pkgName);
        if (info != null) {
            int size = this.mMultipathUidList.size();
            for (int i = 0; i < size; i++) {
                if (info.getUid() == ((Integer) this.mMultipathUidList.get(i)).intValue()) {
                    this.mMultipathUidList.remove(i);
                    Log.d(TAG, "remove MultiPathUid  uid = " + info.getUid());
                    updateMpipUidList(this.mMultipathUidList);
                    return;
                }
            }
        }
    }

    private XEngineConfigInfo getHicomFeaturesAppInfoByPackageName(String packageName) {
        int size = this.mHicomFeaturesAppInfos.size();
        for (int i = 0; i < size; i++) {
            XEngineConfigInfo appInfo = (XEngineConfigInfo) this.mHicomFeaturesAppInfos.get(i);
            if (appInfo != null && appInfo.getPackageName().equals(packageName)) {
                return appInfo;
            }
        }
        return null;
    }

    public void updateGameSpaceInfo(XEngineForegroundAppInfo info, int uid) {
        this.mGameSpaceInfo = info;
        if (info != null) {
            updateMpipUidList(copyOnAddList(this.mMultipathUidList, Integer.valueOf(uid)));
        }
    }

    public ArrayList<Integer> copyOnAddList(ArrayList<Integer> list, Integer item) {
        ArrayList<Integer> result = new ArrayList();
        int size = list.size();
        for (int i = 0; i < size; i++) {
            result.add((Integer) list.get(i));
        }
        result.add(item);
        return result;
    }

    private boolean isExistedInCurrentList(ArrayList<Integer> list, int uid) {
        int size = list.size();
        for (int i = 0; i < size; i++) {
            if (uid == ((Integer) list.get(i)).intValue()) {
                return true;
            }
        }
        return false;
    }

    private boolean isCurrentAppExistedInList(ArrayList<XEngineConfigInfo> list, String pkgName) {
        if (list == null || pkgName == null) {
            Log.d(TAG, "CurrentList is null or pkgName is null eva");
            return false;
        }
        int size = list.size();
        for (int i = 0; i < size; i++) {
            if (pkgName.equals(((XEngineConfigInfo) list.get(i)).getPackageName())) {
                return true;
            }
        }
        return false;
    }

    public void addForegroundAppInfo(XEngineForegroundAppInfo appInfo) {
        this.mForegroundAppInfos.add(appInfo);
    }

    public void removeForegroundAppInfo(XEngineForegroundAppInfo appInfo) {
        this.mForegroundAppInfos.remove(appInfo);
    }

    private XEngineForegroundAppInfo getForeAppInfoByPackageName(String packageName) {
        int size = this.mForegroundAppInfos.size();
        for (int i = 0; i < size; i++) {
            XEngineForegroundAppInfo appInfo = (XEngineForegroundAppInfo) this.mForegroundAppInfos.get(i);
            if (appInfo != null && appInfo.getPackageName().equals(packageName)) {
                return appInfo;
            }
        }
        if (this.mGameSpaceInfo == null || !this.mGameSpaceInfo.getPackageName().equals(packageName)) {
            return null;
        }
        return this.mGameSpaceInfo;
    }

    private PhoneStateListener getPhoneStateListener(final int sub) {
        return new PhoneStateListener(Integer.valueOf(sub)) {
            public void onDataConnectionStateChanged(int state, int networkType) {
                Log.d(HicomPolicyManager.TAG, "DataConnectionStateChanged  sub=" + sub + "state = " + state);
                switch (state) {
                    case 0:
                        Log.d(HicomPolicyManager.TAG, "DATA_DISCONNECTED");
                        HicomPolicyManager.this.mDataConnectionStatus[sub].connectionStatus = state;
                        if (HicomPolicyManager.this.isNetworkDisconnected()) {
                            HicomPolicyManager.this.stopEvaluateNetwork();
                            return;
                        }
                        return;
                    case 2:
                        if (HicomPolicyManager.this.isNetworkEvaluationEnable()) {
                            HicomPolicyManager.this.startEvaluateNetwork(sub, 0);
                        }
                        HicomPolicyManager.this.mDataConnectionStatus[sub].connectionStatus = state;
                        return;
                    default:
                        Log.d(HicomPolicyManager.TAG, "unrelated state");
                        return;
                }
            }
        };
    }

    private boolean isNetworkDisconnected() {
        if (!isPhoneDataDisconnected() || this.mNetworkState == 1) {
            return false;
        }
        return true;
    }

    private void registerPhoneListener() {
        int i;
        int mSubNumber = TelephonyManager.getDefault().getPhoneCount();
        this.mPhoneStateListener = new PhoneStateListener[mSubNumber];
        this.mDataConnectionStatus = new DataConnectionStatus[mSubNumber];
        for (i = 0; i < this.mDataConnectionStatus.length; i++) {
            this.mDataConnectionStatus[i] = new DataConnectionStatus();
        }
        TelephonyManager teleMnger = (TelephonyManager) this.mContext.getSystemService("phone");
        for (i = 0; i < mSubNumber; i++) {
            this.mPhoneStateListener[i] = getPhoneStateListener(i);
            teleMnger.listen(this.mPhoneStateListener[i], 65);
        }
    }

    public boolean isPhoneDataDisconnected() {
        for (DataConnectionStatus dataConnectionStatus : this.mDataConnectionStatus) {
            if (dataConnectionStatus.connectionStatus != 0) {
                return false;
            }
        }
        return true;
    }

    private boolean isInCallDataSwitchOn() {
        if (Global.getInt(this.mContext.getContentResolver(), SETTINGS_INCALL_DATA_SWITCH, 0) == 1) {
            return true;
        }
        return false;
    }
}
