package com.huawei.motiondetection.motionrelay;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningAppProcessInfo;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.os.Message;
import android.os.Process;
import android.os.UserHandle;
import com.huawei.motiondetection.MRLog;
import com.huawei.motiondetection.MotionConfig;

public class RelayBroadcast implements IRelay {
    private static final String TAG = "RelayBroadcast";
    private Context mContext = null;
    private RelayHandler mRHandler = null;
    private RelayListener mRelayListener = null;
    private RelayReceiver mRelayReceiver = null;

    class RelayHandler extends Handler {
        RelayHandler() {
        }

        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 1:
                    RelayBroadcast.this.processMotionRecoResult((Intent) msg.obj);
                    return;
                default:
                    return;
            }
        }
    }

    public RelayBroadcast(Context context) {
        this.mContext = context;
        this.mRHandler = new RelayHandler();
        this.mRelayReceiver = new RelayReceiver(this.mRHandler);
        initReceiver();
    }

    public RelayBroadcast(Context context, boolean isEx) {
        this.mContext = context;
        this.mRHandler = new RelayHandler();
        this.mRelayReceiver = new RelayReceiver(this.mRHandler);
        initReceiver(isEx);
    }

    public void destroy() {
        this.mContext.unregisterReceiver(this.mRelayReceiver);
        this.mRelayReceiver.destroy();
        this.mRelayReceiver = null;
        this.mRelayListener = null;
        this.mRHandler = null;
        this.mContext = null;
    }

    public void startMotionService() {
        Intent intent = new Intent(MotionConfig.MOTION_ACTION_SERVICE);
        intent.setPackage(MotionConfig.MOTION_SERVICE_PACKAGE);
        intent.setFlags(32);
        intent.putExtra(MotionConfig.MOTION_SERVICE_START_TYPE, 1);
        this.mContext.startService(intent);
    }

    public void stopMotionService() {
        Intent intent = new Intent(MotionConfig.MOTION_ACTION_SERVICE);
        intent.setPackage(MotionConfig.MOTION_SERVICE_PACKAGE);
        this.mContext.stopService(intent);
    }

    public void startMotionReco(int motionType) {
        startMotionReco(motionType, false);
    }

    public void startMotionReco(int motionType, boolean isEx) {
        doModtionReco(motionType, 1, isEx);
    }

    public void startMotionRecoAsUser(int motionType, int userId) {
        doModtionRecoAsUser(motionType, 1, userId);
    }

    public void stopMotionReco(int motionType) {
        stopMotionReco(motionType, false);
    }

    public void stopMotionReco(int motionType, boolean isEx) {
        doModtionReco(motionType, 0, isEx);
    }

    public void stopMotionRecoAsUser(int motionType, int userId) {
        doModtionRecoAsUser(motionType, 0, userId);
    }

    public void setRelayListener(RelayListener prListener) {
        this.mRelayListener = prListener;
    }

    private void initReceiver() {
        initReceiver(false);
    }

    private void initReceiver(boolean isEx) {
        try {
            String str;
            if (isEx) {
                str = MotionConfig.MOTION_ACTION_RECOGNITION_EX;
            } else {
                str = MotionConfig.MOTION_ACTION_RECOGNITION;
            }
            this.mContext.registerReceiver(this.mRelayReceiver, new IntentFilter(str), "com.huawei.motion.permission.MOTION_ACTION_RECOGNITION", null);
        } catch (Exception ex) {
            MRLog.w(TAG, ex.getMessage());
        }
    }

    private void doModtionReco(int motionType, int operateType, boolean isEx) {
        MRLog.d(TAG, "doModtionReco | registerPkg: " + this.mContext.getPackageName() + " , motionType: " + motionType + " , opType(register:1 | unregister:0): " + operateType);
        sendRegisterBroadcastToMotionService(motionType, operateType, isEx ? MotionConfig.MOTION_ACTION_OPERATE_EX : MotionConfig.MOTION_ACTION_OPERATE, "com.huawei.motion.permission.MOTION_ACTION_OPERATE");
    }

    private void sendRegisterBroadcastToMotionService(int motionType, int operateType, String action, String permission) {
        Intent intent = new Intent(action);
        intent.putExtra(MotionConfig.APP_MOTION_TYPE, motionType);
        intent.putExtra(MotionConfig.APP_OPERATION_TYPE, operateType);
        intent.putExtra(MotionConfig.APP_PROCESS_NAME, getCurProcessName(this.mContext));
        intent.putExtra(MotionConfig.APP_PKG_NAME, this.mContext.getPackageName());
        intent.putExtra(MotionConfig.APP_CLASS_NAME, getClass().getName());
        this.mContext.sendBroadcast(intent, permission);
    }

    private void doModtionRecoAsUser(int motionType, int operateType, int userId) {
        Intent intent = new Intent(MotionConfig.MOTION_ACTION_OPERATE);
        intent.putExtra(MotionConfig.APP_MOTION_TYPE, motionType);
        intent.putExtra(MotionConfig.APP_OPERATION_TYPE, operateType);
        intent.putExtra(MotionConfig.APP_PROCESS_NAME, getCurProcessName(this.mContext));
        intent.putExtra(MotionConfig.APP_PKG_NAME, this.mContext.getPackageName());
        intent.putExtra(MotionConfig.APP_CLASS_NAME, getClass().getName());
        MRLog.d(TAG, "doModtionRecoAsUser sendBroadcast motionType: " + motionType + "  operateType: " + operateType);
        this.mContext.sendBroadcastAsUser(intent, new UserHandle(userId), "com.huawei.motion.permission.MOTION_ACTION_OPERATE");
    }

    private String getCurProcessName(Context context) {
        int pid = Process.myPid();
        MRLog.d(TAG, "getCurProcessName pid: " + pid);
        ActivityManager mActivityManager = (ActivityManager) context.getSystemService("activity");
        if (!(mActivityManager == null || mActivityManager.getRunningAppProcesses() == null)) {
            for (RunningAppProcessInfo appProcess : mActivityManager.getRunningAppProcesses()) {
                if (appProcess != null && appProcess.pid == pid) {
                    return appProcess.processName;
                }
            }
        }
        return "";
    }

    private void processMotionRecoResult(Intent mReco) {
        try {
            if (this.mRelayListener != null) {
                this.mRelayListener.notifyResult(1, mReco);
            } else {
                MRLog.w(TAG, "processMotionRecoResult mRelayListener = null");
            }
        } catch (Exception ex) {
            MRLog.w(TAG, ex.getMessage());
        }
    }
}
