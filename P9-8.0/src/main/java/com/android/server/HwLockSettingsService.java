package com.android.server;

import android.content.Context;
import android.content.Intent;
import android.os.Parcel;
import android.os.RemoteException;
import android.os.StrictMode;
import android.os.UserHandle;
import android.service.gatekeeper.GateKeeperResponse;
import android.text.TextUtils;
import android.util.Slog;
import com.android.internal.widget.ICheckCredentialProgressCallback;
import com.android.internal.widget.VerifyCredentialResponse;
import com.android.server.LockSettingsStorage.CredentialHash;
import java.nio.charset.StandardCharsets;

public class HwLockSettingsService extends LockSettingsService {
    public static final String DESCRIPTOR = "com.android.internal.widget.ILockSettings";
    public static final String LOCK_PASSWORD_FILE2 = "password2.key";
    private static final int PASSWORD_STATUS_CHANGED = 2;
    private static final int PASSWORD_STATUS_OFF = 0;
    private static final int PASSWORD_STATUS_ON = 1;
    private static final String PERMISSION_GET_LOCK_PASSWORD_CHANGED = "com.huawei.permission.GET_LOCK_PASSWORD_CHANGED";
    private static final String RECEIVER_ACTION_LOCK_PASSWORD_CHANGED = "com.huawei.locksettingsservice.action.LOCK_PASSWORD_CHANGED";
    private static final String RECEIVER_PACKAGE = "com.huawei.hwid";
    private static final String SYSTEM_DIRECTORY = "/system/";
    private static final String TAG = "HwLockSettingsService";
    public static final int transaction_checkvisitorpassword = 1002;
    public static final int transaction_setlockvisitorpassword = 1001;
    private final Context mContext;
    private final HwLockSettingsStorage mStorage2;

    public HwLockSettingsService(Context context) {
        super(context);
        this.mStorage2 = new HwLockSettingsStorage(context);
        this.mContext = context;
    }

    private void setVisitorLockPassword(String password, int userId) throws RemoteException {
        checkWritePermission(userId);
        setKeystorePassword(password, userId);
    }

    public boolean checkVisitorPassword(String password, int userId) throws RemoteException {
        return false;
    }

    public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
        switch (code) {
            case 1001:
                data.enforceInterface("com.android.internal.widget.ILockSettings");
                setVisitorLockPassword(data.readString(), data.readInt());
                reply.writeInt(0);
                reply.writeNoException();
                return true;
            case 1002:
                data.enforceInterface("com.android.internal.widget.ILockSettings");
                if (checkVisitorPassword(data.readString(), data.readInt())) {
                    reply.writeInt(0);
                } else {
                    reply.writeInt(1);
                }
                reply.writeNoException();
                return true;
            default:
                return super.onTransact(code, data, reply, flags);
        }
    }

    protected int getOldCredentialType(int userId) {
        CredentialHash oldCredentialHash = this.mStorage.readCredentialHash(userId);
        if (oldCredentialHash != null) {
            return oldCredentialHash.type;
        }
        return -1;
    }

    protected int getPasswordStatus(int currentCredentialType, int oldCredentialType) {
        Slog.i(TAG, "getPasswordStatus, currentCredentialType=" + currentCredentialType + ", oldCredentialType=" + oldCredentialType);
        if (currentCredentialType == -1) {
            return 0;
        }
        if (oldCredentialType == -1) {
            return 1;
        }
        return 2;
    }

    protected void notifyPasswordStatusChanged(int userId, int status) {
        Intent intent = new Intent(RECEIVER_ACTION_LOCK_PASSWORD_CHANGED);
        intent.setPackage(RECEIVER_PACKAGE);
        intent.putExtra("status", status);
        Slog.i(TAG, "notifyPasswordStatusChanged:" + status + ", userId:" + userId);
        this.mContext.sendBroadcastAsUser(intent, new UserHandle(userId), PERMISSION_GET_LOCK_PASSWORD_CHANGED);
    }

    private VerifyCredentialResponse verifyCredentialEx(int userId, CredentialHash storedHash, String credential, boolean hasChallenge, long challenge, ICheckCredentialProgressCallback progressCallback) throws RemoteException {
        if ((storedHash == null || storedHash.hash.length == 0) && TextUtils.isEmpty(credential)) {
            Slog.w(TAG, "no stored Password/Pattern, verifyCredential success");
            return VerifyCredentialResponse.OK;
        } else if (storedHash == null || TextUtils.isEmpty(credential)) {
            Slog.w(TAG, "no entered Password/Pattern, verifyCredential ERROR");
            return VerifyCredentialResponse.ERROR;
        } else {
            StrictMode.noteDiskRead();
            try {
                if (getGateKeeperService() == null) {
                    return VerifyCredentialResponse.ERROR;
                }
                return convertResponse(getGateKeeperService().verifyChallenge(userId, challenge, storedHash.hash, credential.getBytes(StandardCharsets.UTF_8)));
            } catch (RemoteException e) {
                return VerifyCredentialResponse.ERROR;
            }
        }
    }

    private VerifyCredentialResponse doVerifyCredentialEx(String credential, int credentialType, boolean hasChallenge, long challenge, int userId, ICheckCredentialProgressCallback progressCallback) throws RemoteException {
        CredentialHash storedHash = this.mStorage2.readCredentialHashEx(userId);
        if (storedHash != null && storedHash.hash != null && storedHash.hash.length != 0) {
            return verifyCredentialEx(userId, storedHash, credential, hasChallenge, challenge, progressCallback);
        }
        Slog.w(TAG, "no Pattern saved VerifyPattern success");
        return VerifyCredentialResponse.OK;
    }

    private boolean checkPasswordEx(String password, int userId, ICheckCredentialProgressCallback progressCallback) {
        if (password == null || password.equals("")) {
            return false;
        }
        try {
            if (doVerifyCredentialEx(password, 2, false, 0, userId, progressCallback).getResponseCode() == 0) {
                return true;
            }
            return false;
        } catch (RemoteException e) {
            return false;
        }
    }

    private byte[] enrollCredentialEx(byte[] enrolledHandle, String enrolledCredential, String toEnroll, int userId) throws RemoteException {
        byte[] enrolledCredentialBytes;
        byte[] toEnrollBytes;
        if (enrolledCredential == null) {
            enrolledCredentialBytes = null;
        } else {
            enrolledCredentialBytes = enrolledCredential.getBytes(StandardCharsets.UTF_8);
        }
        if (toEnroll == null) {
            toEnrollBytes = null;
        } else {
            toEnrollBytes = toEnroll.getBytes(StandardCharsets.UTF_8);
        }
        GateKeeperResponse response = getGateKeeperService().enroll(userId, enrolledHandle, enrolledCredentialBytes, toEnrollBytes);
        if (response != null) {
            return response.getPayload();
        }
        Slog.w(TAG, "enrollCredential response null");
        return new byte[0];
    }

    private VerifyCredentialResponse convertResponse(GateKeeperResponse gateKeeperResponse) {
        int responseCode = gateKeeperResponse.getResponseCode();
        if (responseCode == 1) {
            return new VerifyCredentialResponse(gateKeeperResponse.getTimeout());
        }
        if (responseCode != 0) {
            return VerifyCredentialResponse.ERROR;
        }
        byte[] token = gateKeeperResponse.getPayload();
        if (token != null) {
            return new VerifyCredentialResponse(token);
        }
        Slog.e(TAG, "verifyChallenge response had no associated payload");
        return VerifyCredentialResponse.ERROR;
    }

    public boolean setExtendLockScreenPassword(String password, String phoneNumber, int userHandle) {
        checkWritePermission(userHandle);
        try {
            byte[] enrolledHandle = enrollCredentialEx(this.mStorage2.readCredentialHashEx(userHandle).hash, null, password, userHandle);
            if (enrolledHandle.length == 0) {
                return false;
            }
            this.mStorage2.writeCredentialHashEx(CredentialHash.create(enrolledHandle, 2), userHandle);
            Intent intent = new Intent("com.huawei.intent.action.OPERATOR_REMOTE_LOCK");
            intent.setPackage("com.android.systemui");
            intent.putExtra("PhoneNumber", phoneNumber);
            this.mContext.sendBroadcast(intent);
            return true;
        } catch (RemoteException e) {
            return false;
        }
    }

    public boolean clearExtendLockScreenPassword(String password, int userHandle) {
        checkPasswordReadPermission(userHandle);
        if (!this.mStorage2.hasSetPassword(userHandle)) {
            Slog.i(TAG, "has not set password");
            return false;
        } else if (checkPasswordEx(password, userHandle, null)) {
            this.mStorage2.deleteExPasswordFile(userHandle);
            Intent intent = new Intent("com.huawei.intent.action.OPERATOR_REMOTE_UNLOCK");
            intent.setPackage("com.android.systemui");
            this.mContext.sendBroadcast(intent);
            return true;
        } else {
            Slog.e(TAG, "wrong unlock password");
            return false;
        }
    }
}
