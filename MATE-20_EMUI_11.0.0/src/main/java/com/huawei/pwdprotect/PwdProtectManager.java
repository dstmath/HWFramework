package com.huawei.pwdprotect;

import android.os.RemoteException;
import android.util.Log;
import com.huawei.android.os.ServiceManagerEx;
import com.huawei.server.security.pwdprotect.IPwdProtectManager;
import huawei.android.security.IHwSecurityService;

public class PwdProtectManager {
    private static final int PWDPROTECT_PLUGIN_ID = 10;
    private static final String SECURITY_SERVICE = "securityserver";
    private static final String TAG = "PwdProtectManager";
    private static final Object mInstanceSync = new Object();
    private static IPwdProtectManager sIPwdProtectManager;
    private static volatile PwdProtectManager sSelf = null;

    private PwdProtectManager() {
    }

    public static PwdProtectManager getInstance() {
        if (sSelf == null) {
            synchronized (PwdProtectManager.class) {
                if (sSelf == null) {
                    sSelf = new PwdProtectManager();
                }
            }
        }
        return sSelf;
    }

    private static IPwdProtectManager getPwdProtectManagerService() {
        synchronized (mInstanceSync) {
            if (sIPwdProtectManager != null) {
                return sIPwdProtectManager;
            }
            IHwSecurityService secService = IHwSecurityService.Stub.asInterface(ServiceManagerEx.getService(SECURITY_SERVICE));
            if (secService != null) {
                try {
                    sIPwdProtectManager = IPwdProtectManager.Stub.asInterface(secService.querySecurityInterface(10));
                } catch (RemoteException e) {
                    Log.e(TAG, "Get PwdProtectManagerService failed!");
                }
            }
            return sIPwdProtectManager;
        }
    }

    public boolean startPwdProtect(String privSpacePin, String question, String answer, String mainSpacePin) {
        if (getPwdProtectManagerService() == null) {
            return false;
        }
        try {
            return sIPwdProtectManager.startPwdProtect(privSpacePin, question, answer, mainSpacePin);
        } catch (RemoteException e) {
            Log.e(TAG, "startPwdProtect failed!");
            return false;
        }
    }

    public String decodeCurrentPwd(String mainSpacePin, String answer) {
        if (getPwdProtectManagerService() == null) {
            return null;
        }
        try {
            return sIPwdProtectManager.decodeCurrentPwd(mainSpacePin, answer);
        } catch (RemoteException e) {
            Log.e(TAG, "decodeCurrentPwd failed!");
            return null;
        }
    }

    public boolean modifyMainPwd(String origCredential, String newCredential) {
        if (getPwdProtectManagerService() == null) {
            return false;
        }
        try {
            return sIPwdProtectManager.modifyMainPwd(origCredential, newCredential);
        } catch (RemoteException e) {
            Log.e(TAG, "modifyMainPwd failed!");
            return false;
        }
    }

    public boolean modifyPrivPwd(String credential) {
        if (getPwdProtectManagerService() == null) {
            return false;
        }
        try {
            return sIPwdProtectManager.modifyPrivPwd(credential);
        } catch (RemoteException e) {
            Log.e(TAG, "modifyPrivPwd failed!");
            return false;
        }
    }

    public boolean hasKeyFileExisted() {
        if (getPwdProtectManagerService() == null) {
            return false;
        }
        try {
            return sIPwdProtectManager.hasKeyFileExisted();
        } catch (RemoteException e) {
            Log.e(TAG, "hasKeyFileExisted failed!");
            return false;
        }
    }

    public boolean removeKeyFile() {
        if (getPwdProtectManagerService() == null) {
            return false;
        }
        try {
            return sIPwdProtectManager.removeKeyFile();
        } catch (RemoteException e) {
            Log.e(TAG, "removeKeyFile failed!");
            return false;
        }
    }

    public String getPwdQuestion() {
        if (getPwdProtectManagerService() == null) {
            return null;
        }
        try {
            return sIPwdProtectManager.getPwdQuestion();
        } catch (RemoteException e) {
            Log.e(TAG, "getPwdQuestion failed!");
            return null;
        }
    }

    public boolean pwdQAnswerVertify(byte[] pwQuestion) {
        if (getPwdProtectManagerService() == null) {
            return false;
        }
        try {
            return sIPwdProtectManager.pwdQAnswerVertify(pwQuestion);
        } catch (RemoteException e) {
            Log.e(TAG, "pwdQAnswerVertify failed!");
            return false;
        }
    }
}
