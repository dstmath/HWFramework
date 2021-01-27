package com.huawei.pwdprotect;

import android.os.RemoteException;
import android.support.annotation.GuardedBy;
import android.util.Log;
import com.huawei.android.os.ServiceManagerEx;
import com.huawei.server.security.pwdprotect.IPwdProtectManager;
import huawei.android.security.IHwSecurityService;

public class PwdProtectManager {
    private static final String EMPTY_STRING = "";
    private static final Object INSTANCE_SYNC = new Object();
    private static final int PWD_PROTECT_PLUGIN_ID = 10;
    private static final String SECURITY_SERVICE = "securityserver";
    private static final String TAG = "PwdProtectManager";
    @GuardedBy("INSTANCE_SYNC")
    private static IPwdProtectManager sIPwdProtectManager;
    private static volatile PwdProtectManager sInstance = null;

    private PwdProtectManager() {
    }

    public static PwdProtectManager getInstance() {
        if (sInstance == null) {
            synchronized (PwdProtectManager.class) {
                if (sInstance == null) {
                    sInstance = new PwdProtectManager();
                }
            }
        }
        return sInstance;
    }

    public boolean startPwdProtect(String privateSpacePin, String question, String answer, String mainSpacePin) {
        IPwdProtectManager pwdProtectManager = getPwdProtectManagerService();
        if (pwdProtectManager == null) {
            Log.e(TAG, "startPwdProtect: IPwdProtectManager is null");
            return false;
        }
        try {
            return pwdProtectManager.startPwdProtect(privateSpacePin, question, answer, mainSpacePin);
        } catch (RemoteException e) {
            Log.e(TAG, "startPwdProtect: Failed!");
            return false;
        }
    }

    public String decodeCurrentPwd(String mainSpacePin, String answer) {
        IPwdProtectManager pwdProtectManager = getPwdProtectManagerService();
        if (pwdProtectManager == null) {
            Log.e(TAG, "decodeCurrentPwd: IPwdProtectManager is null");
            return null;
        }
        try {
            return pwdProtectManager.decodeCurrentPwd(mainSpacePin, answer);
        } catch (RemoteException e) {
            Log.e(TAG, "decodeCurrentPwd: Failed!");
            return null;
        }
    }

    public boolean modifyMainPwd(String originalPassword, String newPassword) {
        IPwdProtectManager pwdProtectManager = getPwdProtectManagerService();
        if (pwdProtectManager == null) {
            Log.e(TAG, "modifyMainPwd: IPwdProtectManager is null");
            return false;
        }
        try {
            return pwdProtectManager.modifyMainPwd(originalPassword, newPassword);
        } catch (RemoteException e) {
            Log.e(TAG, "modifyMainPwd: Failed!");
            return false;
        }
    }

    public boolean modifyPrivPwd(String password) {
        IPwdProtectManager pwdProtectManager = getPwdProtectManagerService();
        if (pwdProtectManager == null) {
            Log.e(TAG, "modifyPrivPwd: IPwdProtectManager is null");
            return false;
        }
        try {
            return pwdProtectManager.modifyPrivPwd(password);
        } catch (RemoteException e) {
            Log.e(TAG, "modifyPrivPwd: Failed!");
            return false;
        }
    }

    public boolean hasKeyFileExisted() {
        IPwdProtectManager pwdProtectManager = getPwdProtectManagerService();
        if (pwdProtectManager == null) {
            Log.e(TAG, "hasKeyFileExisted: IPwdProtectManager is null");
            return false;
        }
        try {
            return pwdProtectManager.hasKeyFileExisted();
        } catch (RemoteException e) {
            Log.e(TAG, "hasKeyFileExisted: Failed!");
            return false;
        }
    }

    public boolean removeKeyFile() {
        IPwdProtectManager pwdProtectManager = getPwdProtectManagerService();
        if (pwdProtectManager == null) {
            Log.e(TAG, "removeKeyFile: IPwdProtectManager is null");
            return false;
        }
        try {
            return pwdProtectManager.removeKeyFile();
        } catch (RemoteException e) {
            Log.e(TAG, "removeKeyFile: Failed!");
            return false;
        }
    }

    public String getPwdQuestion() {
        IPwdProtectManager pwdProtectManager = getPwdProtectManagerService();
        if (pwdProtectManager == null) {
            Log.e(TAG, "getPwdQuestion: IPwdProtectManager is null");
            return "";
        }
        try {
            return pwdProtectManager.getPwdQuestion();
        } catch (RemoteException e) {
            Log.e(TAG, "getPwdQuestion: Failed!");
            return "";
        }
    }

    public boolean pwdQAnswerVertify(byte[] pwdQuestionAnswer) {
        IPwdProtectManager pwdProtectManager = getPwdProtectManagerService();
        if (pwdProtectManager == null) {
            Log.e(TAG, "pwdQAnswerVertify: IPwdProtectManager is null");
            return false;
        }
        try {
            return pwdProtectManager.pwdQAnswerVertify(pwdQuestionAnswer);
        } catch (RemoteException e) {
            Log.e(TAG, "pwdQAnswerVerify: Failed!");
            return false;
        }
    }

    private static IPwdProtectManager getPwdProtectManagerService() {
        synchronized (INSTANCE_SYNC) {
            if (sIPwdProtectManager != null) {
                return sIPwdProtectManager;
            }
            IHwSecurityService secService = IHwSecurityService.Stub.asInterface(ServiceManagerEx.getService(SECURITY_SERVICE));
            if (secService != null) {
                try {
                    sIPwdProtectManager = IPwdProtectManager.Stub.asInterface(secService.querySecurityInterface(10));
                } catch (RemoteException e) {
                    Log.e(TAG, "getPwdProtectManagerService: Get PwdProtectManagerService failed!");
                }
            }
            return sIPwdProtectManager;
        }
    }
}
