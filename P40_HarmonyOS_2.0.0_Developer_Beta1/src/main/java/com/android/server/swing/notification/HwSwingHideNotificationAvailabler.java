package com.android.server.swing.notification;

import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.UserInfo;
import android.database.ContentObserver;
import android.os.Handler;
import android.os.Message;
import android.os.UserHandle;
import android.os.UserManager;
import android.provider.Settings;
import android.util.IntArray;
import android.util.Log;
import android.util.SparseArray;
import com.android.server.intellicom.common.SmartDualCardConsts;
import com.huawei.server.am.ProcessListEx;
import java.util.ArrayList;
import java.util.List;

public class HwSwingHideNotificationAvailabler extends HwSwingEventAvailabler {
    private static final int DEFAULT_LIST_SIZE = 6;
    private static final int EXTRA_REGISTER = 0;
    private static final int EXTRA_UNKNOW = -1;
    private static final int EXTRA_UNREGISTER = 1;
    private static final int FACE_BIND_LOCK_DISABLED = 0;
    private static final int HIDE_PRIVATE_IN_PUBLIC = 0;
    private static final String LOCK_WITH_PASSWORD = "lock_with_password";
    private static final int MSG_UPDATE_AVAILABLE = 0;
    private static final String SETTINGS_KEY_LOCK_UNLOCK_STATE_NOTIFY = "keyguard_lock_unlock_type_notify";
    private static final String SETTINGS_KEY_UNLOCK_TYPE_NOTIFY = "keyguard_unlock_type_notify";
    private static final String SETTINS_KEY_FACE_BIND_LOCK = "face_bind_with_lock";
    private static final List<String> UNLOCK_TYPES = new ArrayList(6);
    private static final String UNLOCK_TYPE_FACE = "face_unlock";
    private static final String UNLOCK_TYPE_FINGERPRINT = "fingerprint_unlock";
    private static final String UNLOCK_TYPE_OTHER = "unlock_other";
    private static final String UNLOCK_TYPE_PASSWORD = "unlock_password";
    private static final String UNLOCK_TYPE_SMART = "unlock_smart";
    private static final String UNLOCK_WITH_PASSWORD = "unlock_password";
    private ContentObserver mFaceUnlockSettingsObserver;
    private Handler mHandler = new Handler() {
        /* class com.android.server.swing.notification.HwSwingHideNotificationAvailabler.AnonymousClass1 */

        @Override // android.os.Handler
        public void handleMessage(Message msg) {
            if (msg.what == 0 && (msg.obj instanceof Integer)) {
                HwSwingHideNotificationAvailabler.this.updateAvailableState(((Integer) msg.obj).intValue());
            }
        }
    };
    private ContentObserver mLockUnlockStateSettingsObserver;
    private ContentObserver mLockscreenNotiSettingsObserver;
    private ContentObserver mUnlockTypeSettingsObserver;
    private final UserProfiles mUserProfiles = new UserProfiles();
    private BroadcastReceiver mUserStateReceiver;

    static {
        UNLOCK_TYPES.add(UNLOCK_TYPE_FACE);
        UNLOCK_TYPES.add(UNLOCK_TYPE_FINGERPRINT);
        UNLOCK_TYPES.add(UNLOCK_TYPE_SMART);
        UNLOCK_TYPES.add("unlock_password");
        UNLOCK_TYPES.add(UNLOCK_TYPE_OTHER);
        UNLOCK_TYPES.add("unlock_password");
    }

    public HwSwingHideNotificationAvailabler(Context context) {
        super(context);
    }

    /* access modifiers changed from: protected */
    @Override // com.android.server.swing.notification.HwSwingEventAvailabler
    public void init() {
        registerLockNotiObserver();
        registerFaceUnlockObserver();
        registerLockStateObserver();
        registerUserStateReceiver();
        this.mUserProfiles.updateCache(this.mContext);
        updateAvailableState(-1);
    }

    /* access modifiers changed from: protected */
    @Override // com.android.server.swing.notification.HwSwingEventAvailabler
    public void release() {
        if (this.mLockscreenNotiSettingsObserver != null) {
            this.mContext.getContentResolver().unregisterContentObserver(this.mLockscreenNotiSettingsObserver);
            this.mLockscreenNotiSettingsObserver = null;
        }
        if (this.mFaceUnlockSettingsObserver != null) {
            this.mContext.getContentResolver().unregisterContentObserver(this.mFaceUnlockSettingsObserver);
            this.mFaceUnlockSettingsObserver = null;
        }
        if (this.mUnlockTypeSettingsObserver != null) {
            this.mContext.getContentResolver().unregisterContentObserver(this.mUnlockTypeSettingsObserver);
            this.mUnlockTypeSettingsObserver = null;
        }
        if (this.mLockUnlockStateSettingsObserver != null) {
            this.mContext.getContentResolver().unregisterContentObserver(this.mLockUnlockStateSettingsObserver);
            this.mLockUnlockStateSettingsObserver = null;
        }
        if (this.mUserStateReceiver != null) {
            this.mContext.unregisterReceiver(this.mUserStateReceiver);
            this.mUserStateReceiver = null;
        }
    }

    private void registerLockNotiObserver() {
        if (this.mLockscreenNotiSettingsObserver == null) {
            this.mLockscreenNotiSettingsObserver = new ContentObserver(null) {
                /* class com.android.server.swing.notification.HwSwingHideNotificationAvailabler.AnonymousClass2 */

                @Override // android.database.ContentObserver
                public void onChange(boolean selfChange) {
                    Log.i(HwSwingHideNotificationAvailabler.this.TAG, "onChange: lock_screen_allow_private_notifications changed");
                    if (HwSwingHideNotificationAvailabler.this.mHandler != null) {
                        HwSwingHideNotificationAvailabler.this.mHandler.removeMessages(0);
                        Message.obtain(HwSwingHideNotificationAvailabler.this.mHandler, 0, -1).sendToTarget();
                    }
                }
            };
        }
        this.mContext.getContentResolver().registerContentObserver(Settings.Secure.getUriFor("lock_screen_allow_private_notifications"), true, this.mLockscreenNotiSettingsObserver, -1);
    }

    private void registerFaceUnlockObserver() {
        if (this.mFaceUnlockSettingsObserver == null) {
            this.mFaceUnlockSettingsObserver = new ContentObserver(null) {
                /* class com.android.server.swing.notification.HwSwingHideNotificationAvailabler.AnonymousClass3 */

                @Override // android.database.ContentObserver
                public void onChange(boolean selfChange) {
                    Log.i(HwSwingHideNotificationAvailabler.this.TAG, "onChange: face_bind_with_lock changed");
                    if (HwSwingHideNotificationAvailabler.this.mHandler != null) {
                        HwSwingHideNotificationAvailabler.this.mHandler.removeMessages(0);
                        Message.obtain(HwSwingHideNotificationAvailabler.this.mHandler, 0, -1).sendToTarget();
                    }
                }
            };
        }
        this.mContext.getContentResolver().registerContentObserver(Settings.Secure.getUriFor(SETTINS_KEY_FACE_BIND_LOCK), false, this.mFaceUnlockSettingsObserver, 0);
    }

    private void registerLockStateObserver() {
        if (this.mUnlockTypeSettingsObserver == null) {
            this.mUnlockTypeSettingsObserver = new ContentObserver(null) {
                /* class com.android.server.swing.notification.HwSwingHideNotificationAvailabler.AnonymousClass4 */

                @Override // android.database.ContentObserver
                public void onChange(boolean selfChange) {
                    String unlockType = Settings.Global.getStringForUser(HwSwingHideNotificationAvailabler.this.mContext.getContentResolver(), HwSwingHideNotificationAvailabler.SETTINGS_KEY_UNLOCK_TYPE_NOTIFY, 0);
                    String str = HwSwingHideNotificationAvailabler.this.TAG;
                    Log.i(str, "onChange: keyguard_unlock_type_notify: unlockType=" + unlockType);
                    if (HwSwingHideNotificationAvailabler.this.mHandler != null) {
                        HwSwingHideNotificationAvailabler.this.mHandler.removeMessages(0);
                        Message.obtain(HwSwingHideNotificationAvailabler.this.mHandler, 0, Integer.valueOf(!HwSwingHideNotificationAvailabler.UNLOCK_TYPES.contains(unlockType))).sendToTarget();
                    }
                }
            };
        }
        this.mContext.getContentResolver().registerContentObserver(Settings.Global.getUriFor(SETTINGS_KEY_UNLOCK_TYPE_NOTIFY), false, this.mUnlockTypeSettingsObserver, 0);
        if (this.mLockUnlockStateSettingsObserver == null) {
            this.mLockUnlockStateSettingsObserver = new ContentObserver(null) {
                /* class com.android.server.swing.notification.HwSwingHideNotificationAvailabler.AnonymousClass5 */

                @Override // android.database.ContentObserver
                public void onChange(boolean selfChange) {
                    String lockUnlockState = Settings.Global.getStringForUser(HwSwingHideNotificationAvailabler.this.mContext.getContentResolver(), HwSwingHideNotificationAvailabler.SETTINGS_KEY_LOCK_UNLOCK_STATE_NOTIFY, 0);
                    String str = HwSwingHideNotificationAvailabler.this.TAG;
                    Log.i(str, "onChange: keyguard_lock_unlock_type_notify: lockUnlockState=" + lockUnlockState);
                    HwSwingHideNotificationAvailabler.this.mHandler.removeMessages(0);
                    int extraState = -1;
                    if (HwSwingHideNotificationAvailabler.LOCK_WITH_PASSWORD.equals(lockUnlockState)) {
                        extraState = 1;
                    } else if ("unlock_password".equals(lockUnlockState)) {
                        extraState = 0;
                    }
                    Message.obtain(HwSwingHideNotificationAvailabler.this.mHandler, 0, Integer.valueOf(extraState)).sendToTarget();
                }
            };
        }
        this.mContext.getContentResolver().registerContentObserver(Settings.Global.getUriFor(SETTINGS_KEY_LOCK_UNLOCK_STATE_NOTIFY), false, this.mLockUnlockStateSettingsObserver, 0);
    }

    private void registerUserStateReceiver() {
        if (this.mUserStateReceiver == null) {
            this.mUserStateReceiver = new BroadcastReceiver() {
                /* class com.android.server.swing.notification.HwSwingHideNotificationAvailabler.AnonymousClass6 */

                @Override // android.content.BroadcastReceiver
                public void onReceive(Context context, Intent intent) {
                    if (intent != null) {
                        String action = intent.getAction();
                        if (SmartDualCardConsts.SYSTEM_STATE_ACTION_USER_SWITCHED.equals(action)) {
                            int userId = intent.getIntExtra("android.intent.extra.user_handle", ProcessListEx.INVALID_ADJ);
                            HwSwingHideNotificationAvailabler.this.mUserProfiles.updateCache(context);
                            String str = HwSwingHideNotificationAvailabler.this.TAG;
                            Log.i(str, "onReceive: switch user: id=" + userId);
                            HwSwingHideNotificationAvailabler.this.mHandler.removeMessages(0);
                            Message.obtain(HwSwingHideNotificationAvailabler.this.mHandler, 0, -1).sendToTarget();
                        } else if ("android.intent.action.USER_ADDED".equals(action)) {
                            int userId2 = intent.getIntExtra("android.intent.extra.user_handle", ProcessListEx.INVALID_ADJ);
                            String str2 = HwSwingHideNotificationAvailabler.this.TAG;
                            Log.i(str2, "onReceive: add user: id=" + userId2);
                            if (userId2 != -10000) {
                                HwSwingHideNotificationAvailabler.this.mUserProfiles.updateCache(context);
                                HwSwingHideNotificationAvailabler.this.mHandler.removeMessages(0);
                                Message.obtain(HwSwingHideNotificationAvailabler.this.mHandler, 0, -1).sendToTarget();
                            }
                        } else if ("android.intent.action.USER_REMOVED".equals(action)) {
                            int userId3 = intent.getIntExtra("android.intent.extra.user_handle", ProcessListEx.INVALID_ADJ);
                            String str3 = HwSwingHideNotificationAvailabler.this.TAG;
                            Log.i(str3, "onReceive: remove user: id=" + userId3);
                            HwSwingHideNotificationAvailabler.this.mUserProfiles.updateCache(context);
                            HwSwingHideNotificationAvailabler.this.mHandler.removeMessages(0);
                            Message.obtain(HwSwingHideNotificationAvailabler.this.mHandler, 0, -1).sendToTarget();
                        } else if ("android.intent.action.MANAGED_PROFILE_AVAILABLE".equals(action)) {
                            int userId4 = intent.getIntExtra("android.intent.extra.user_handle", -1);
                            String str4 = HwSwingHideNotificationAvailabler.this.TAG;
                            Log.i(str4, "onReceive: managed profile available: id=" + userId4);
                            if (userId4 >= 0) {
                                HwSwingHideNotificationAvailabler.this.mUserProfiles.updateCache(context);
                                HwSwingHideNotificationAvailabler.this.mHandler.removeMessages(0);
                                Message.obtain(HwSwingHideNotificationAvailabler.this.mHandler, 0, -1).sendToTarget();
                            }
                        } else if ("android.intent.action.MANAGED_PROFILE_UNAVAILABLE".equals(action)) {
                            int userId5 = intent.getIntExtra("android.intent.extra.user_handle", -1);
                            String str5 = HwSwingHideNotificationAvailabler.this.TAG;
                            Log.i(str5, "onReceive: managed profile unavailable: id=" + userId5);
                            if (userId5 >= 0) {
                                HwSwingHideNotificationAvailabler.this.mUserProfiles.updateCache(context);
                                HwSwingHideNotificationAvailabler.this.mHandler.removeMessages(0);
                                Message.obtain(HwSwingHideNotificationAvailabler.this.mHandler, 0, -1).sendToTarget();
                            }
                        } else {
                            Log.w(HwSwingHideNotificationAvailabler.this.TAG, "onReceive: never be here!");
                        }
                    }
                }
            };
        }
        IntentFilter filter = new IntentFilter();
        filter.addAction(SmartDualCardConsts.SYSTEM_STATE_ACTION_USER_SWITCHED);
        filter.addAction("android.intent.action.USER_ADDED");
        filter.addAction("android.intent.action.USER_REMOVED");
        filter.addAction("android.intent.action.MANAGED_PROFILE_AVAILABLE");
        filter.addAction("android.intent.action.MANAGED_PROFILE_UNAVAILABLE");
        this.mContext.registerReceiverAsUser(this.mUserStateReceiver, UserHandle.ALL, filter, null, null);
    }

    public static class UserProfiles {
        private final SparseArray<UserInfo> mCurrentProfiles = new SparseArray<>();
        private int mCurrentUserId;

        public void updateCache(Context context) {
            UserManager userManager = (UserManager) context.getSystemService("user");
            if (userManager != null) {
                this.mCurrentUserId = ActivityManager.getCurrentUser();
                List<UserInfo> profiles = userManager.getEnabledProfiles(this.mCurrentUserId);
                synchronized (this.mCurrentProfiles) {
                    this.mCurrentProfiles.clear();
                    for (UserInfo user : profiles) {
                        this.mCurrentProfiles.put(user.id, user);
                    }
                }
            }
        }

        public IntArray getCurrentProfileIds() {
            IntArray users;
            synchronized (this.mCurrentProfiles) {
                users = new IntArray(this.mCurrentProfiles.size());
                int len = this.mCurrentProfiles.size();
                for (int i = 0; i < len; i++) {
                    users.add(this.mCurrentProfiles.keyAt(i));
                }
            }
            return users;
        }

        public int getCurrentUserId() {
            return this.mCurrentUserId;
        }

        public boolean isManagedProfile(int userId) {
            boolean z;
            synchronized (this.mCurrentProfiles) {
                UserInfo user = this.mCurrentProfiles.get(userId);
                z = user != null && user.isManagedProfile();
            }
            return z;
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void updateAvailableState(int cmd) {
        boolean z = false;
        if (this.mUserProfiles.getCurrentUserId() != 0) {
            setAvailable(false);
            Log.i(this.TAG, "updateAvailableState: non owner");
            return;
        }
        ContentResolver resolver = this.mContext.getContentResolver();
        int lockNotiState = Settings.Secure.getIntForUser(resolver, "lock_screen_allow_private_notifications", 0, 0);
        if (lockNotiState != 0) {
            IntArray profileIds = this.mUserProfiles.getCurrentProfileIds();
            int len = profileIds.size();
            for (int i = 0; i < len; i++) {
                int profileId = profileIds.get(i);
                if (this.mUserProfiles.isManagedProfile(profileId) && (lockNotiState = Settings.Secure.getIntForUser(resolver, "lock_screen_allow_private_notifications", 0, profileId)) == 0) {
                    break;
                }
            }
        }
        if (lockNotiState != 0) {
            setAvailable(false);
            Log.i(this.TAG, "updateAvailableState: lock screen allow private notification");
        } else if (Settings.Secure.getIntForUser(resolver, SETTINS_KEY_FACE_BIND_LOCK, 0, 0) == 0) {
            setAvailable(false);
            Log.i(this.TAG, "updateAvailableState: face do not use unlock");
        } else {
            if (cmd != 1) {
                z = true;
            }
            setAvailable(z);
        }
    }
}
