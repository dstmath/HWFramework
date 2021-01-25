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
import java.lang.reflect.InvocationTargetException;

public class PwdProtectService extends IPwdProtectManager.Stub implements IHwSecurityPlugin {
    public static final IHwSecurityPlugin.Creator CREATOR = new IHwSecurityPlugin.Creator() {
        /* class com.huawei.server.security.pwdprotect.PwdProtectService.AnonymousClass1 */

        @Override // com.huawei.server.security.core.IHwSecurityPlugin.Creator
        public IHwSecurityPlugin createPlugin(Context context) {
            Log.d(PwdProtectService.TAG, "create PwdProtectService");
            return new PwdProtectService(context);
        }

        @Override // com.huawei.server.security.core.IHwSecurityPlugin.Creator
        public String getPluginPermission() {
            return null;
        }
    };
    private static final String DB_KEY_PRIVACY_USER_PWD_PROTECT = "privacy_user_pwd_protect";
    private static final String FIELD_FLAGS = "flags";
    private static final int FLAG_HW_PRIVATE_SPACE = 33554432;
    private static final String METHOD_GET_USER_INFO = "getUserInfo";
    private static final String NOTIFICATION_ACTION = "com.huawei.privatespace.action.send_notification";
    private static final String PRIV_SPACE_PWD_PROTECT_PERMISSION = "com.huawei.privacyspace.permission.PASSWORD_RESET";
    private static final String TAG = "PwdProtectService";
    private static final int TYPE_PRIVSPACE_PWD_PROTECT_MODIFY_ANSWER = 991310133;
    private static final int TYPE_PRIVSPACE_PWD_PROTECT_OPEN = 991310130;
    private static final int TYPE_PRIVSPACE_PWD_PROTECT_RESET_FAIL = 991310132;
    private Context mContext;
    private boolean mIsShow = false;
    private boolean mIsSwitchToPrivateUser = false;
    private int mPrivateUserId = 0;
    private ScreenReceiver mReceiver;

    public PwdProtectService(Context context) {
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
        this.mContext.enforceCallingOrSelfPermission(PRIV_SPACE_PWD_PROTECT_PERMISSION, "does not have privSpace pwd protect permission!");
        if (PasswordIvsCache.FILE_E_PIN2.exists() || PasswordIvsCache.FILE_E_SK2.exists() || PasswordIvsCache.FILE_E_PWDQANSWER.exists() || PasswordIvsCache.FILE_E_PWDQ.exists()) {
            return true;
        }
        return false;
    }

    public boolean removeKeyFile() throws RemoteException {
        this.mContext.enforceCallingOrSelfPermission(PRIV_SPACE_PWD_PROTECT_PERMISSION, "does not have privSpace pwd protect permission!");
        if (!hasKeyFileExisted()) {
            Log.e(TAG, "removeKeyFile fail, files do not exist");
            return false;
        } else if (!PasswordIvsCache.FILE_E_PIN2.delete() || !PasswordIvsCache.FILE_E_SK2.delete() || !PasswordIvsCache.FILE_E_PWDQANSWER.delete() || !PasswordIvsCache.FILE_E_PWDQ.delete()) {
            return false;
        } else {
            Log.i(TAG, "removeKeyFile success");
            return true;
        }
    }

    public boolean modifyPrivPwd(String password) throws RemoteException {
        this.mContext.enforceCallingOrSelfPermission(PRIV_SPACE_PWD_PROTECT_PERMISSION, "does not have privSpace pwd protect permission!");
        if (!TextUtils.isEmpty(password)) {
            return ModifyPassWord.modifyPrivSpacePw(password);
        }
        Log.e(TAG, "modifyPrivPwd failed,str is null");
        return false;
    }

    public boolean modifyMainPwd(String origPassword, String newPassword) throws RemoteException {
        this.mContext.enforceCallingOrSelfPermission(PRIV_SPACE_PWD_PROTECT_PERMISSION, "does not have privSpace pwd protect permission!");
        if (!TextUtils.isEmpty(origPassword) || !TextUtils.isEmpty(newPassword)) {
            return ModifyPassWord.modifyMainSpacePw(origPassword, newPassword);
        }
        Log.e(TAG, "modifyMainPwd failed,str is null");
        return false;
    }

    public String decodeCurrentPwd(String mainSpacePin, String answer) throws RemoteException {
        this.mContext.enforceCallingOrSelfPermission(PRIV_SPACE_PWD_PROTECT_PERMISSION, "does not have privSpace pwd protect permission!");
        if (!TextUtils.isEmpty(mainSpacePin) || !TextUtils.isEmpty(answer)) {
            String origPwd = ResetPassWord.decodeCurrentPwd(mainSpacePin, answer);
            if (TextUtils.isEmpty(origPwd)) {
                HiEventEx eventEx = new HiEventEx((int) TYPE_PRIVSPACE_PWD_PROTECT_RESET_FAIL);
                eventEx.putAppInfo(this.mContext);
                HiViewEx.report(eventEx);
            }
            return origPwd;
        }
        Log.e(TAG, "decodeCurrentPwd failed,str is null");
        return null;
    }

    public boolean startPwdProtect(String privSpacePin, String question, String answer, String mainSpacePin) throws RemoteException {
        this.mContext.enforceCallingOrSelfPermission(PRIV_SPACE_PWD_PROTECT_PERMISSION, "does not have privSpace pwd protect permission!");
        if (TextUtils.isEmpty(privSpacePin) && TextUtils.isEmpty(question) && TextUtils.isEmpty(answer) && TextUtils.isEmpty(mainSpacePin)) {
            Log.e(TAG, "startPwdProtect failed,str is null");
            return false;
        } else if (Boolean.valueOf(new StartPwdProtect().turnOnPwdProtect(privSpacePin, question, answer, mainSpacePin)).booleanValue()) {
            Log.i(TAG, "startPwdProtect success");
            if (isPrivSpacePwdProtectOpened()) {
                HiEventEx eventEx = new HiEventEx((int) TYPE_PRIVSPACE_PWD_PROTECT_MODIFY_ANSWER);
                eventEx.putAppInfo(this.mContext);
                HiViewEx.report(eventEx);
                return true;
            }
            HiEventEx eventEx2 = new HiEventEx((int) TYPE_PRIVSPACE_PWD_PROTECT_OPEN);
            eventEx2.putAppInfo(this.mContext);
            HiViewEx.report(eventEx2);
            return true;
        } else {
            Log.e(TAG, "startPwdProtect failed");
            return false;
        }
    }

    public String getPwdQuestion() throws RemoteException {
        this.mContext.enforceCallingOrSelfPermission(PRIV_SPACE_PWD_PROTECT_PERMISSION, "does not have privSpace pwd protect permission!");
        return ResetPassWord.getPwdQuestion();
    }

    public boolean pwdQAnswerVertify(byte[] pwQuestion) throws RemoteException {
        this.mContext.enforceCallingOrSelfPermission(PRIV_SPACE_PWD_PROTECT_PERMISSION, "does not have privSpace pwd protect permission!");
        if (pwQuestion != null) {
            return ResetPassWord.pwdQAnswerVertify(pwQuestion).booleanValue();
        }
        Log.e(TAG, "pwdQAnswerVertify failed,str is null");
        return false;
    }

    private boolean isPrivSpacePwdProtectOpened() {
        Context context = this.mContext;
        if (context != null && Settings.Global.getInt(context.getContentResolver(), DB_KEY_PRIVACY_USER_PWD_PROTECT, 0) == 1) {
            return true;
        }
        return false;
    }

    private void listenForUserSwitches() {
        try {
            ActivityManagerExt.registerUserSwitchObserver(new UserSwitchObserverExt() {
                /* class com.huawei.server.security.pwdprotect.PwdProtectService.AnonymousClass2 */

                public void onUserSwitching(int newUserId, IRemoteCallbackExt reply) throws RemoteException {
                    PwdProtectService.super.onUserSwitching(newUserId, reply);
                    Log.i(PwdProtectService.TAG, "onUserSwitching: Get new userId: " + newUserId);
                    if (!PwdProtectService.this.isPrivateUser(newUserId)) {
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
            Log.e(TAG, "Occur RemoteException when listenForUserSwitches.");
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private boolean isPrivateUserUnlocked() {
        Context context = this.mContext;
        if (context == null) {
            return false;
        }
        return UserManagerExt.isUserUnlocked(UserManagerExt.get(context), this.mPrivateUserId);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private boolean isPrivateUser(int userId) {
        Context context = this.mContext;
        if (context == null) {
            Log.e(TAG, "isPrivateUser: mContext is null.");
            return false;
        }
        UserManager userManager = (UserManager) context.getSystemService("user");
        if (userManager == null) {
            Log.e(TAG, "isPrivateUser: userManager is null.");
            return false;
        }
        UserInfoExt userInfoExt = UserManagerExt.getUserInfoEx(userManager, userId);
        if (userInfoExt == null) {
            Log.e(TAG, "isPrivateUser: userInfoExt is null.");
            return false;
        }
        try {
            Object userInfo = UserInfoExt.class.getDeclaredMethod(METHOD_GET_USER_INFO, new Class[0]).invoke(userInfoExt, new Object[0]);
            if (userInfo == null) {
                Log.e(TAG, "isPrivateUser: userInfo is null.");
                return false;
            }
            Object value = userInfo.getClass().getDeclaredField(FIELD_FLAGS).get(userInfo);
            if (!(value instanceof Integer) || (((Integer) value).intValue() & FLAG_HW_PRIVATE_SPACE) != FLAG_HW_PRIVATE_SPACE) {
                return false;
            }
            return true;
        } catch (NoSuchMethodException e) {
            Log.e(TAG, "isPrivateUser: method not found!");
        } catch (IllegalAccessException e2) {
            Log.e(TAG, "isPrivateUser: illegal access!");
        } catch (InvocationTargetException e3) {
            Log.e(TAG, "isPrivateUser: invocation error!");
        } catch (NoSuchFieldException e4) {
            Log.e(TAG, "isPrivateUser: field not found!");
        }
        return false;
    }

    private void startActivity() {
        if (this.mContext != null) {
            Intent notifyIntent = new Intent();
            notifyIntent.setAction(NOTIFICATION_ACTION);
            notifyIntent.addCategory("android.intent.category.DEFAULT");
            notifyIntent.setFlags(268435456);
            ContextEx.startActivityAsUser(this.mContext, notifyIntent, (Bundle) null, UserHandleEx.getUserHandle(this.mPrivateUserId));
        }
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
        if (isPrivateUser(this.mPrivateUserId)) {
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
                    if (!PwdProtectService.this.isPrivateUserUnlocked()) {
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
