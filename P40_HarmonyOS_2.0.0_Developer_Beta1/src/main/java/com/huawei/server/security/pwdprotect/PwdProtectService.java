package com.huawei.server.security.pwdprotect;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.RemoteException;
import android.os.UserManager;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;
import com.huawei.android.app.ActivityManagerExt;
import com.huawei.android.app.HiEventEx;
import com.huawei.android.app.HiViewEx;
import com.huawei.android.app.UserSwitchObserverExt;
import com.huawei.android.content.ContextEx;
import com.huawei.android.content.pm.UserInfoExt;
import com.huawei.android.os.IRemoteCallbackExt;
import com.huawei.android.os.UserHandleEx;
import com.huawei.android.os.UserManagerExt;
import com.huawei.server.security.core.IHwSecurityPlugin;
import com.huawei.server.security.pwdprotect.IPwdProtectManager;
import com.huawei.server.security.pwdprotect.logic.ModifyPassWord;
import com.huawei.server.security.pwdprotect.logic.ResetPassWord;
import com.huawei.server.security.pwdprotect.logic.StartPwdProtect;
import com.huawei.server.security.pwdprotect.model.PasswordIvsCache;

public class PwdProtectService extends IPwdProtectManager.Stub implements IHwSecurityPlugin {
    public static final IHwSecurityPlugin.Creator CREATOR = new IHwSecurityPlugin.Creator() {
        /* class com.huawei.server.security.pwdprotect.PwdProtectService.AnonymousClass1 */

        @Override // com.huawei.server.security.core.IHwSecurityPlugin.Creator
        public IHwSecurityPlugin createPlugin(Context context) {
            Log.i(PwdProtectService.TAG, "create PwdProtectService.");
            if (context != null) {
                return new PwdProtectService(context);
            }
            Log.e(PwdProtectService.TAG, "IHwSecurityPlugin: context is null!");
            return null;
        }

        @Override // com.huawei.server.security.core.IHwSecurityPlugin.Creator
        public String getPluginPermission() {
            return null;
        }
    };
    private static final String DB_KEY_PRIVACY_USER_PWD_PROTECT = "privacy_user_pwd_protect";
    private static final String EMPTY_STRING = "";
    private static final String FIELD_FLAGS = "flags";
    private static final int FLAG_HW_PRIVATE_SPACE = 33554432;
    private static final String METHOD_GET_USER_INFO = "getUserInfo";
    private static final String NOTIFICATION_ACTION = "com.huawei.privatespace.action.send_notification";
    private static final String PERMISSION_ERROR = "does not have privSpace pwd protect permission!";
    private static final String PRIVACY_SPACE_PWD_PROTECT_PERMISSION = "com.huawei.privacyspace.permission.PASSWORD_RESET";
    private static final String TAG = "PwdProtectService";
    private static final int TURN_ON_PWD_PROTECT_STATUS = 1;
    private static final int TYPE_PRIVATE_SPACE_PWD_PROTECT_MODIFY_ANSWER = 991310133;
    private static final int TYPE_PRIVATE_SPACE_PWD_PROTECT_OPEN = 991310130;
    private static final int TYPE_PRIVATE_SPACE_PWD_PROTECT_RESET_FAIL = 991310132;
    private Context mContext;
    private boolean mIsShow;
    private boolean mIsSwitchToPrivateUser;
    private int mPrivateUserId;
    private ScreenReceiver mReceiver;

    private PwdProtectService(Context context) {
        this.mIsSwitchToPrivateUser = false;
        this.mIsShow = false;
        this.mPrivateUserId = 0;
        this.mContext = context;
    }

    /* JADX DEBUG: Multi-variable search result rejected for r0v0, resolved type: com.huawei.server.security.pwdprotect.PwdProtectService */
    /* JADX WARN: Multi-variable type inference failed */
    @Override // com.huawei.server.security.core.IHwSecurityPlugin
    public IBinder asBinder() {
        return this;
    }

    @Override // com.huawei.server.security.core.IHwSecurityPlugin
    public void onStart() {
        listenForUserSwitches();
    }

    @Override // com.huawei.server.security.core.IHwSecurityPlugin
    public void onStop() {
    }

    public boolean hasKeyFileExisted() throws RemoteException {
        Context context = this.mContext;
        if (context == null) {
            Log.e(TAG, "hasKeyFileExisted: context is null!");
            return false;
        } else if (context.checkCallingPermission(PRIVACY_SPACE_PWD_PROTECT_PERMISSION) != 0) {
            Log.e(TAG, "hasKeyFileExisted: does not have privSpace pwd protect permission!");
            return false;
        } else if (!PasswordIvsCache.FILE_E_PIN2.exists() || !PasswordIvsCache.FILE_E_SK2.exists() || !PasswordIvsCache.FILE_E_PWDQANSWER.exists() || !PasswordIvsCache.FILE_E_PWDQ.exists()) {
            return false;
        } else {
            return true;
        }
    }

    public boolean removeKeyFile() throws RemoteException {
        Context context = this.mContext;
        if (context == null) {
            Log.e(TAG, "removeKeyFile: context is null!");
            return false;
        } else if (context.checkCallingPermission(PRIVACY_SPACE_PWD_PROTECT_PERMISSION) != 0) {
            Log.e(TAG, "removeKeyFile: does not have privSpace pwd protect permission!");
            return false;
        } else if (!verifyFile()) {
            Log.e(TAG, "removeKeyFile: Fail, files do not exist!");
            return false;
        } else if (!PasswordIvsCache.FILE_E_PIN2.delete() || !PasswordIvsCache.FILE_E_SK2.delete() || !PasswordIvsCache.FILE_E_PWDQANSWER.delete() || !PasswordIvsCache.FILE_E_PWDQ.delete()) {
            return false;
        } else {
            Log.i(TAG, "removeKeyFile: Success.");
            return true;
        }
    }

    public boolean modifyPrivPwd(String password) throws RemoteException {
        Context context = this.mContext;
        if (context == null) {
            Log.e(TAG, "modifyPrivPwd: context is null!");
            return false;
        } else if (context.checkCallingPermission(PRIVACY_SPACE_PWD_PROTECT_PERMISSION) != 0) {
            Log.e(TAG, "modifyPrivPwd: does not have privSpace pwd protect permission!");
            return false;
        } else if (!TextUtils.isEmpty(password)) {
            return ModifyPassWord.modifyPrivateSpacePwd(password);
        } else {
            Log.e(TAG, "modifyPrivPwd: Failed, password is null!");
            return false;
        }
    }

    public boolean modifyMainPwd(String origPassword, String newPassword) throws RemoteException {
        Context context = this.mContext;
        if (context == null) {
            Log.e(TAG, "modifyMainPwd: context is null!");
            return false;
        } else if (context.checkCallingPermission(PRIVACY_SPACE_PWD_PROTECT_PERMISSION) != 0) {
            Log.e(TAG, "modifyMainPwd: does not have privSpace pwd protect permission!");
            return false;
        } else if (!TextUtils.isEmpty(origPassword) && !TextUtils.isEmpty(newPassword)) {
            return ModifyPassWord.modifyMainSpacePwd(origPassword, newPassword);
        } else {
            Log.e(TAG, "modifyMainPwd: Failed, password is null!");
            return false;
        }
    }

    public String decodeCurrentPwd(String mainSpacePin, String answer) throws RemoteException {
        Context context = this.mContext;
        if (context == null) {
            Log.e(TAG, "decodeCurrentPwd: context is null!");
            return null;
        } else if (context.checkCallingPermission(PRIVACY_SPACE_PWD_PROTECT_PERMISSION) != 0) {
            Log.e(TAG, "decodeCurrentPwd: does not have privSpace pwd protect permission!");
            return null;
        } else if (TextUtils.isEmpty(mainSpacePin) || TextUtils.isEmpty(answer)) {
            Log.e(TAG, "decodeCurrentPwd: Failed, input is null!");
            return null;
        } else {
            String originalPwd = ResetPassWord.decodeCurrentPwd(mainSpacePin, answer);
            if (TextUtils.isEmpty(originalPwd)) {
                HiEventEx eventEx = new HiEventEx((int) TYPE_PRIVATE_SPACE_PWD_PROTECT_RESET_FAIL);
                eventEx.putAppInfo(this.mContext);
                HiViewEx.report(eventEx);
            }
            return originalPwd;
        }
    }

    public boolean startPwdProtect(String privateSpacePin, String question, String answer, String mainSpacePin) throws RemoteException {
        Context context = this.mContext;
        if (context == null) {
            Log.e(TAG, "startPwdProtect: context is null!");
            return false;
        } else if (context.checkCallingPermission(PRIVACY_SPACE_PWD_PROTECT_PERMISSION) != 0) {
            Log.e(TAG, "startPwdProtect: does not have privSpace pwd protect permission!");
            return false;
        } else if (TextUtils.isEmpty(question) || TextUtils.isEmpty(answer)) {
            Log.e(TAG, "startPwdProtect: Failed, input is null!");
            return false;
        } else if (TextUtils.isEmpty(privateSpacePin) || TextUtils.isEmpty(mainSpacePin)) {
            Log.e(TAG, "startPwdProtect: Failed, invalid input!");
            return false;
        } else if (new StartPwdProtect().turnOnPwdProtect(privateSpacePin, question, answer, mainSpacePin)) {
            Log.i(TAG, "startPwdProtect: Success.");
            if (isPrivateSpacePwdProtectOpened()) {
                HiEventEx eventEx = new HiEventEx((int) TYPE_PRIVATE_SPACE_PWD_PROTECT_MODIFY_ANSWER);
                eventEx.putAppInfo(this.mContext);
                HiViewEx.report(eventEx);
                return true;
            }
            HiEventEx eventEx2 = new HiEventEx((int) TYPE_PRIVATE_SPACE_PWD_PROTECT_OPEN);
            eventEx2.putAppInfo(this.mContext);
            HiViewEx.report(eventEx2);
            return true;
        } else {
            Log.e(TAG, "startPwdProtect: Failed!");
            return false;
        }
    }

    public String getPwdQuestion() throws RemoteException {
        Context context = this.mContext;
        if (context == null) {
            Log.e(TAG, "getPwdQuestion: context is null!");
            return "";
        } else if (context.checkCallingPermission(PRIVACY_SPACE_PWD_PROTECT_PERMISSION) == 0) {
            return ResetPassWord.getPwdQuestion();
        } else {
            Log.e(TAG, "getPwdQuestion: does not have privSpace pwd protect permission!");
            return "";
        }
    }

    public boolean pwdQAnswerVertify(byte[] pwdQuestionAnswer) throws RemoteException {
        Context context = this.mContext;
        if (context == null) {
            Log.e(TAG, "pwdQAnswerVertify: context is null!");
            return false;
        } else if (context.checkCallingPermission(PRIVACY_SPACE_PWD_PROTECT_PERMISSION) != 0) {
            Log.e(TAG, "pwdQAnswerVertify: does not have privSpace pwd protect permission!");
            return false;
        } else if (pwdQuestionAnswer != null) {
            return ResetPassWord.verifyPwdQuestionAnswer(pwdQuestionAnswer);
        } else {
            Log.e(TAG, "pwdQAnswerVerify: Failed, input is null!");
            return false;
        }
    }

    private boolean verifyFile() {
        return PasswordIvsCache.FILE_E_PIN2.exists() && PasswordIvsCache.FILE_E_SK2.exists() && PasswordIvsCache.FILE_E_PWDQANSWER.exists() && PasswordIvsCache.FILE_E_PWDQ.exists();
    }

    private boolean isPrivateSpacePwdProtectOpened() {
        Context context = this.mContext;
        if (context == null) {
            Log.e(TAG, "isPrivateSpacePwdProtectOpened: context is null!");
            return false;
        } else if (Settings.Global.getInt(context.getContentResolver(), DB_KEY_PRIVACY_USER_PWD_PROTECT, 0) == 1) {
            return true;
        } else {
            return false;
        }
    }

    private void listenForUserSwitches() {
        try {
            ActivityManagerExt.registerUserSwitchObserver(new UserSwitchObserverExt() {
                /* class com.huawei.server.security.pwdprotect.PwdProtectService.AnonymousClass2 */

                public void onUserSwitching(int newUserId, IRemoteCallbackExt reply) throws RemoteException {
                    PwdProtectService.super.onUserSwitching(newUserId, reply);
                    Log.i(PwdProtectService.TAG, "onUserSwitching: Get new userId: " + newUserId);
                    if (!PwdProtectService.this.verifyUserType(newUserId)) {
                        PwdProtectService.this.unregister();
                        PwdProtectService.this.mIsSwitchToPrivateUser = false;
                        return;
                    }
                    PwdProtectService.this.mPrivateUserId = newUserId;
                    PwdProtectService.this.mIsSwitchToPrivateUser = true;
                    PwdProtectService.this.register();
                }

                public void onUserSwitchComplete(int newUserId) throws RemoteException {
                    PwdProtectService.super.onUserSwitchComplete(newUserId);
                }
            }, TAG);
        } catch (RemoteException e) {
            Log.e(TAG, "onUserSwitchComplete: Occur RemoteException when listenForUserSwitches.");
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private boolean getLockStatus() {
        Context context = this.mContext;
        if (context != null) {
            return UserManagerExt.isUserUnlocked(UserManagerExt.get(context), this.mPrivateUserId);
        }
        Log.e(TAG, "getLockStatus: context is null!");
        return false;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private boolean verifyUserType(int userId) {
        Context context = this.mContext;
        if (context == null) {
            Log.e(TAG, "verifyUserType: context is null!");
            return false;
        }
        UserManager userManager = (UserManager) context.getSystemService("user");
        if (userManager == null) {
            Log.e(TAG, "verifyUserType: The userManager is null.");
            return false;
        }
        UserInfoExt userInfoExt = UserManagerExt.getUserInfoEx(userManager, userId);
        if (userInfoExt != null) {
            return UserManagerExt.isHwHiddenSpace(userInfoExt);
        }
        Log.e(TAG, "verifyUserType: The userInfoExt is null.");
        return false;
    }

    private void startActivity() {
        if (this.mContext == null) {
            Log.e(TAG, "startActivity: context is null!");
            return;
        }
        Intent notifyIntent = new Intent();
        notifyIntent.setAction(NOTIFICATION_ACTION);
        notifyIntent.addCategory("android.intent.category.DEFAULT");
        notifyIntent.setFlags(268435456);
        ContextEx.startActivityAsUser(this.mContext, notifyIntent, (Bundle) null, UserHandleEx.getUserHandle(this.mPrivateUserId));
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void register() {
        Log.i(TAG, "register!");
        if (this.mReceiver == null) {
            this.mReceiver = new ScreenReceiver();
        }
        IntentFilter filter = new IntentFilter();
        filter.addAction("android.intent.action.USER_PRESENT");
        filter.addAction("android.intent.action.USER_UNLOCKED");
        Context context = this.mContext;
        if (context != null) {
            ContextEx.registerReceiverAsUser(context, this.mReceiver, UserHandleEx.ALL, filter, (String) null, (Handler) null);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void unregister() {
        Context context;
        Log.i(TAG, "unregister!");
        ScreenReceiver screenReceiver = this.mReceiver;
        if (!(screenReceiver == null || (context = this.mContext) == null)) {
            context.unregisterReceiver(screenReceiver);
        }
        this.mReceiver = null;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void doStart() {
        if (verifyUserType(this.mPrivateUserId)) {
            Log.i(TAG, "doStart!");
            startActivity();
            this.mIsSwitchToPrivateUser = false;
        }
    }

    /* access modifiers changed from: private */
    public class ScreenReceiver extends BroadcastReceiver {
        private ScreenReceiver() {
        }

        @Override // android.content.BroadcastReceiver
        public void onReceive(Context context, Intent intent) {
            if (intent != null) {
                if (!PwdProtectService.this.mIsSwitchToPrivateUser) {
                    Log.i(PwdProtectService.TAG, "onReceive: mIsSwitchToPrivateUser: false!");
                    return;
                }
                String action = intent.getAction();
                if ("android.intent.action.USER_PRESENT".equals(action)) {
                    Log.i(PwdProtectService.TAG, "onReceive: ACTION_USER_PRESENT.");
                    if (!PwdProtectService.this.getLockStatus()) {
                        PwdProtectService.this.mIsShow = true;
                        return;
                    }
                    PwdProtectService.this.doStart();
                }
                if ("android.intent.action.USER_UNLOCKED".equals(action) && PwdProtectService.this.mIsShow) {
                    Log.i(PwdProtectService.TAG, "onReceive: ACTION_USER_UNLOCKED.");
                    PwdProtectService.this.mIsShow = false;
                    PwdProtectService.this.doStart();
                }
            }
        }
    }
}
