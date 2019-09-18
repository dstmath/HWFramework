package com.android.server.policy;

import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.UserInfo;
import android.database.ContentObserver;
import android.graphics.drawable.Drawable;
import android.media.AudioManager;
import android.net.ConnectivityManager;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.os.UserManager;
import android.os.Vibrator;
import android.provider.Settings;
import android.service.dreams.IDreamManager;
import android.telephony.PhoneStateListener;
import android.telephony.ServiceState;
import android.telephony.TelephonyManager;
import android.util.ArraySet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.WindowManagerGlobal;
import android.widget.AdapterView;
import com.android.internal.app.AlertController;
import com.android.internal.globalactions.Action;
import com.android.internal.globalactions.ActionsAdapter;
import com.android.internal.globalactions.ActionsDialog;
import com.android.internal.globalactions.LongPressAction;
import com.android.internal.globalactions.SinglePressAction;
import com.android.internal.globalactions.ToggleAction;
import com.android.internal.logging.MetricsLogger;
import com.android.internal.util.EmergencyAffordanceManager;
import com.android.internal.widget.LockPatternUtils;
import com.android.server.audio.AudioService;
import com.android.server.policy.WindowManagerPolicy;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BooleanSupplier;

class LegacyGlobalActions implements DialogInterface.OnDismissListener, DialogInterface.OnClickListener {
    private static final int DIALOG_DISMISS_DELAY = 300;
    private static final String GLOBAL_ACTION_KEY_AIRPLANE = "airplane";
    private static final String GLOBAL_ACTION_KEY_ASSIST = "assist";
    private static final String GLOBAL_ACTION_KEY_BUGREPORT = "bugreport";
    private static final String GLOBAL_ACTION_KEY_LOCKDOWN = "lockdown";
    private static final String GLOBAL_ACTION_KEY_POWER = "power";
    private static final String GLOBAL_ACTION_KEY_REBOOT = "hwrestart";
    private static final String GLOBAL_ACTION_KEY_RESTART = "restart";
    private static final String GLOBAL_ACTION_KEY_SETTINGS = "settings";
    private static final String GLOBAL_ACTION_KEY_SILENT = "silent";
    private static final String GLOBAL_ACTION_KEY_USERS = "users";
    private static final String GLOBAL_ACTION_KEY_VOICEASSIST = "voiceassist";
    private static final int MESSAGE_DISMISS = 0;
    private static final int MESSAGE_REFRESH = 1;
    private static final int MESSAGE_SHOW = 2;
    private static final boolean SHOW_SILENT_TOGGLE = true;
    private static final String TAG = "LegacyGlobalActions";
    /* access modifiers changed from: private */
    public ActionsAdapter mAdapter;
    private ContentObserver mAirplaneModeObserver = new ContentObserver(new Handler()) {
        public void onChange(boolean selfChange) {
            LegacyGlobalActions.this.onAirplaneModeChanged();
        }
    };
    /* access modifiers changed from: private */
    public ToggleAction mAirplaneModeOn;
    /* access modifiers changed from: private */
    public ToggleAction.State mAirplaneState = ToggleAction.State.Off;
    /* access modifiers changed from: private */
    public final AudioManager mAudioManager;
    private BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if ("android.intent.action.CLOSE_SYSTEM_DIALOGS".equals(action) || "android.intent.action.SCREEN_OFF".equals(action)) {
                if (!PhoneWindowManager.SYSTEM_DIALOG_REASON_GLOBAL_ACTIONS.equals(intent.getStringExtra(PhoneWindowManager.SYSTEM_DIALOG_REASON_KEY))) {
                    LegacyGlobalActions.this.mHandler.sendEmptyMessage(0);
                }
            } else if ("android.intent.action.EMERGENCY_CALLBACK_MODE_CHANGED".equals(action) && !intent.getBooleanExtra("PHONE_IN_ECM_STATE", false) && LegacyGlobalActions.this.mIsWaitingForEcmExit) {
                boolean unused = LegacyGlobalActions.this.mIsWaitingForEcmExit = false;
                LegacyGlobalActions.this.changeAirplaneModeSystemSetting(true);
            }
        }
    };
    /* access modifiers changed from: private */
    public final Context mContext;
    /* access modifiers changed from: private */
    public boolean mDeviceProvisioned = false;
    /* access modifiers changed from: private */
    public ActionsDialog mDialog;
    private final IDreamManager mDreamManager;
    /* access modifiers changed from: private */
    public final EmergencyAffordanceManager mEmergencyAffordanceManager;
    /* access modifiers changed from: private */
    public Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 0:
                    if (LegacyGlobalActions.this.mDialog != null) {
                        LegacyGlobalActions.this.mDialog.dismiss();
                        ActionsDialog unused = LegacyGlobalActions.this.mDialog = null;
                        return;
                    }
                    return;
                case 1:
                    LegacyGlobalActions.this.refreshSilentMode();
                    LegacyGlobalActions.this.mAdapter.notifyDataSetChanged();
                    return;
                case 2:
                    LegacyGlobalActions.this.handleShow();
                    return;
                default:
                    return;
            }
        }
    };
    /* access modifiers changed from: private */
    public boolean mHasTelephony;
    private boolean mHasVibrator;
    /* access modifiers changed from: private */
    public boolean mIsWaitingForEcmExit = false;
    private ArrayList<Action> mItems;
    /* access modifiers changed from: private */
    public boolean mKeyguardShowing = false;
    private final Runnable mOnDismiss;
    PhoneStateListener mPhoneStateListener = new PhoneStateListener() {
        public void onServiceStateChanged(ServiceState serviceState) {
            if (LegacyGlobalActions.this.mHasTelephony) {
                ToggleAction.State unused = LegacyGlobalActions.this.mAirplaneState = serviceState.getState() == 3 ? ToggleAction.State.On : ToggleAction.State.Off;
                LegacyGlobalActions.this.mAirplaneModeOn.updateState(LegacyGlobalActions.this.mAirplaneState);
                LegacyGlobalActions.this.mAdapter.notifyDataSetChanged();
            }
        }
    };
    private BroadcastReceiver mRingerModeReceiver = null;
    private final boolean mShowSilentToggle;
    private Action mSilentModeAction;
    private final WindowManagerPolicy.WindowManagerFuncs mWindowManagerFuncs;

    private class BugReportAction extends SinglePressAction implements LongPressAction {
        public BugReportAction() {
            super(17302421, 17039706);
        }

        public void onPress() {
            if (!ActivityManager.isUserAMonkey()) {
                LegacyGlobalActions.this.mHandler.postDelayed(new Runnable() {
                    public void run() {
                        try {
                            MetricsLogger.action(LegacyGlobalActions.this.mContext, 292);
                            ActivityManager.getService().requestBugReport(1);
                        } catch (RemoteException e) {
                        }
                    }
                }, 500);
            }
        }

        public boolean onLongPress() {
            if (ActivityManager.isUserAMonkey()) {
                return false;
            }
            try {
                MetricsLogger.action(LegacyGlobalActions.this.mContext, 293);
                ActivityManager.getService().requestBugReport(0);
            } catch (RemoteException e) {
            }
            return false;
        }

        public boolean showDuringKeyguard() {
            return true;
        }

        public boolean showBeforeProvisioning() {
            return false;
        }

        public String getStatus() {
            return LegacyGlobalActions.this.mContext.getString(17039705, new Object[]{Build.VERSION.RELEASE, Build.ID});
        }
    }

    private class SilentModeToggleAction extends ToggleAction {
        public SilentModeToggleAction() {
            super(17302287, 17302285, 17040136, 17040134, 17040133);
        }

        public void onToggle(boolean on) {
            if (on) {
                LegacyGlobalActions.this.mAudioManager.setRingerMode(0);
            } else {
                LegacyGlobalActions.this.mAudioManager.setRingerMode(2);
            }
        }

        public boolean showDuringKeyguard() {
            return true;
        }

        public boolean showBeforeProvisioning() {
            return false;
        }
    }

    private static class SilentModeTriStateAction implements Action, View.OnClickListener {
        private final int[] ITEM_IDS = {16909165, 16909166, 16909167};
        private final AudioManager mAudioManager;
        private final Context mContext;
        private final Handler mHandler;

        SilentModeTriStateAction(Context context, AudioManager audioManager, Handler handler) {
            this.mAudioManager = audioManager;
            this.mHandler = handler;
            this.mContext = context;
        }

        private int ringerModeToIndex(int ringerMode) {
            return ringerMode;
        }

        private int indexToRingerMode(int index) {
            return index;
        }

        public CharSequence getLabelForAccessibility(Context context) {
            return null;
        }

        public View create(Context context, View convertView, ViewGroup parent, LayoutInflater inflater) {
            View v = inflater.inflate(17367150, parent, false);
            int selectedIndex = ringerModeToIndex(this.mAudioManager.getRingerMode());
            int i = 0;
            while (i < 3) {
                View itemView = v.findViewById(this.ITEM_IDS[i]);
                itemView.setSelected(selectedIndex == i);
                itemView.setTag(Integer.valueOf(i));
                itemView.setOnClickListener(this);
                i++;
            }
            return v;
        }

        public void onPress() {
        }

        public boolean showDuringKeyguard() {
            return true;
        }

        public boolean showBeforeProvisioning() {
            return false;
        }

        public boolean isEnabled() {
            return true;
        }

        /* access modifiers changed from: package-private */
        public void willCreate() {
        }

        public void onClick(View v) {
            if (v.getTag() instanceof Integer) {
                this.mAudioManager.setRingerMode(indexToRingerMode(((Integer) v.getTag()).intValue()));
                this.mHandler.sendEmptyMessageDelayed(0, 300);
            }
        }
    }

    public LegacyGlobalActions(Context context, WindowManagerPolicy.WindowManagerFuncs windowManagerFuncs, Runnable onDismiss) {
        boolean z = false;
        this.mContext = context;
        this.mWindowManagerFuncs = windowManagerFuncs;
        this.mOnDismiss = onDismiss;
        this.mAudioManager = (AudioManager) this.mContext.getSystemService("audio");
        this.mDreamManager = IDreamManager.Stub.asInterface(ServiceManager.getService("dreams"));
        IntentFilter filter = new IntentFilter();
        filter.addAction("android.intent.action.CLOSE_SYSTEM_DIALOGS");
        filter.addAction("android.intent.action.SCREEN_OFF");
        filter.addAction("android.intent.action.EMERGENCY_CALLBACK_MODE_CHANGED");
        context.registerReceiver(this.mBroadcastReceiver, filter);
        this.mHasTelephony = ((ConnectivityManager) context.getSystemService("connectivity")).isNetworkSupported(0);
        ((TelephonyManager) context.getSystemService("phone")).listen(this.mPhoneStateListener, 1);
        this.mContext.getContentResolver().registerContentObserver(Settings.Global.getUriFor("airplane_mode_on"), true, this.mAirplaneModeObserver);
        Vibrator vibrator = (Vibrator) this.mContext.getSystemService("vibrator");
        if (vibrator != null && vibrator.hasVibrator()) {
            z = true;
        }
        this.mHasVibrator = z;
        this.mShowSilentToggle = !this.mContext.getResources().getBoolean(17957060);
        this.mEmergencyAffordanceManager = new EmergencyAffordanceManager(context);
    }

    public void showDialog(boolean keyguardShowing, boolean isDeviceProvisioned) {
        this.mKeyguardShowing = keyguardShowing;
        this.mDeviceProvisioned = isDeviceProvisioned;
        if (this.mDialog != null) {
            this.mDialog.dismiss();
            this.mDialog = null;
            this.mHandler.sendEmptyMessage(2);
            return;
        }
        handleShow();
    }

    private void awakenIfNecessary() {
        if (this.mDreamManager != null) {
            try {
                if (this.mDreamManager.isDreaming()) {
                    this.mDreamManager.awaken();
                }
            } catch (RemoteException e) {
            }
        }
    }

    /* access modifiers changed from: private */
    public void handleShow() {
        awakenIfNecessary();
        this.mDialog = createDialog();
        prepareDialog();
        if (this.mAdapter.getCount() == 1 && (this.mAdapter.getItem(0) instanceof SinglePressAction) && !(this.mAdapter.getItem(0) instanceof LongPressAction)) {
            this.mAdapter.getItem(0).onPress();
        } else if (this.mDialog != null) {
            WindowManager.LayoutParams attrs = this.mDialog.getWindow().getAttributes();
            attrs.setTitle(TAG);
            this.mDialog.getWindow().setAttributes(attrs);
            this.mDialog.show();
            this.mDialog.getWindow().getDecorView().setSystemUiVisibility(65536);
        }
    }

    private ActionsDialog createDialog() {
        if (!this.mHasVibrator) {
            this.mSilentModeAction = new SilentModeToggleAction();
        } else {
            this.mSilentModeAction = new SilentModeTriStateAction(this.mContext, this.mAudioManager, this.mHandler);
        }
        AnonymousClass1 r4 = new ToggleAction(17302417, 17302419, 17040141, 17040140, 17040139) {
            public void onToggle(boolean on) {
                if (!LegacyGlobalActions.this.mHasTelephony || !Boolean.parseBoolean(SystemProperties.get("ril.cdma.inecmmode"))) {
                    LegacyGlobalActions.this.changeAirplaneModeSystemSetting(on);
                    return;
                }
                boolean unused = LegacyGlobalActions.this.mIsWaitingForEcmExit = true;
                Intent ecmDialogIntent = new Intent("com.android.internal.intent.action.ACTION_SHOW_NOTICE_ECM_BLOCK_OTHERS", null);
                ecmDialogIntent.addFlags(268435456);
                LegacyGlobalActions.this.mContext.startActivity(ecmDialogIntent);
            }

            /* access modifiers changed from: protected */
            public void changeStateFromPress(boolean buttonOn) {
                if (LegacyGlobalActions.this.mHasTelephony && !Boolean.parseBoolean(SystemProperties.get("ril.cdma.inecmmode"))) {
                    this.mState = buttonOn ? ToggleAction.State.TurningOn : ToggleAction.State.TurningOff;
                    ToggleAction.State unused = LegacyGlobalActions.this.mAirplaneState = this.mState;
                }
            }

            public boolean showDuringKeyguard() {
                return true;
            }

            public boolean showBeforeProvisioning() {
                return false;
            }
        };
        this.mAirplaneModeOn = r4;
        onAirplaneModeChanged();
        this.mItems = new ArrayList<>();
        String[] defaultActions = this.mContext.getResources().getStringArray(17236010);
        ArraySet<String> addedKeys = new ArraySet<>();
        for (String actionKey : defaultActions) {
            if (!addedKeys.contains(actionKey)) {
                if (GLOBAL_ACTION_KEY_POWER.equals(actionKey)) {
                    this.mItems.add(new PowerAction(this.mContext, this.mWindowManagerFuncs));
                } else if (GLOBAL_ACTION_KEY_REBOOT.equals(actionKey)) {
                    HwPolicyFactory.addRebootMenu(this.mItems);
                } else if (GLOBAL_ACTION_KEY_AIRPLANE.equals(actionKey)) {
                    this.mItems.add(this.mAirplaneModeOn);
                } else if (GLOBAL_ACTION_KEY_BUGREPORT.equals(actionKey)) {
                    if (Settings.Global.getInt(this.mContext.getContentResolver(), "bugreport_in_power_menu", 0) != 0 && isCurrentUserOwner()) {
                        this.mItems.add(new BugReportAction());
                    }
                } else if (GLOBAL_ACTION_KEY_SILENT.equals(actionKey)) {
                    if (this.mShowSilentToggle) {
                        this.mItems.add(this.mSilentModeAction);
                    }
                } else if ("users".equals(actionKey)) {
                    if (SystemProperties.getBoolean("fw.power_user_switcher", false)) {
                        addUsersToMenu(this.mItems);
                    }
                } else if (GLOBAL_ACTION_KEY_SETTINGS.equals(actionKey)) {
                    this.mItems.add(getSettingsAction());
                } else if (GLOBAL_ACTION_KEY_LOCKDOWN.equals(actionKey)) {
                    this.mItems.add(getLockdownAction());
                } else if (GLOBAL_ACTION_KEY_VOICEASSIST.equals(actionKey)) {
                    this.mItems.add(getVoiceAssistAction());
                } else if ("assist".equals(actionKey)) {
                    this.mItems.add(getAssistAction());
                } else if (GLOBAL_ACTION_KEY_RESTART.equals(actionKey)) {
                    this.mItems.add(new RestartAction(this.mContext, this.mWindowManagerFuncs));
                } else {
                    Log.e(TAG, "Invalid global action key " + actionKey);
                }
                addedKeys.add(actionKey);
            }
        }
        if (this.mEmergencyAffordanceManager.needsEmergencyAffordance()) {
            this.mItems.add(getEmergencyAction());
        }
        this.mAdapter = new ActionsAdapter(this.mContext, this.mItems, new BooleanSupplier() {
            public final boolean getAsBoolean() {
                return LegacyGlobalActions.this.mDeviceProvisioned;
            }
        }, new BooleanSupplier() {
            public final boolean getAsBoolean() {
                return LegacyGlobalActions.this.mKeyguardShowing;
            }
        });
        AlertController.AlertParams params = new AlertController.AlertParams(this.mContext);
        params.mAdapter = this.mAdapter;
        params.mOnClickListener = this;
        params.mForceInverseBackground = true;
        ActionsDialog dialog = new ActionsDialog(this.mContext, params);
        dialog.setCanceledOnTouchOutside(false);
        dialog.getListView().setItemsCanFocus(true);
        dialog.getListView().setLongClickable(true);
        dialog.getListView().setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, int position, long id) {
                LongPressAction item = LegacyGlobalActions.this.mAdapter.getItem(position);
                if (item instanceof LongPressAction) {
                    return item.onLongPress();
                }
                return false;
            }
        });
        dialog.getWindow().setType(2009);
        dialog.setOnDismissListener(this);
        return dialog;
    }

    private Action getSettingsAction() {
        return new SinglePressAction(17302749, 17040132) {
            public void onPress() {
                Intent intent = new Intent("android.settings.SETTINGS");
                intent.addFlags(335544320);
                LegacyGlobalActions.this.mContext.startActivity(intent);
            }

            public boolean showDuringKeyguard() {
                return true;
            }

            public boolean showBeforeProvisioning() {
                return true;
            }
        };
    }

    private Action getEmergencyAction() {
        return new SinglePressAction(17302180, 17040125) {
            public void onPress() {
                LegacyGlobalActions.this.mEmergencyAffordanceManager.performEmergencyCall();
            }

            public boolean showDuringKeyguard() {
                return true;
            }

            public boolean showBeforeProvisioning() {
                return true;
            }
        };
    }

    private Action getAssistAction() {
        return new SinglePressAction(17302261, 17040121) {
            public void onPress() {
                Intent intent = new Intent("android.intent.action.ASSIST");
                intent.addFlags(335544320);
                LegacyGlobalActions.this.mContext.startActivity(intent);
            }

            public boolean showDuringKeyguard() {
                return true;
            }

            public boolean showBeforeProvisioning() {
                return true;
            }
        };
    }

    private Action getVoiceAssistAction() {
        return new SinglePressAction(17302781, 17040137) {
            public void onPress() {
                Intent intent = new Intent("android.intent.action.VOICE_ASSIST");
                intent.addFlags(335544320);
                LegacyGlobalActions.this.mContext.startActivity(intent);
            }

            public boolean showDuringKeyguard() {
                return true;
            }

            public boolean showBeforeProvisioning() {
                return true;
            }
        };
    }

    private Action getLockdownAction() {
        return new SinglePressAction(17301551, 17040127) {
            public void onPress() {
                new LockPatternUtils(LegacyGlobalActions.this.mContext).requireCredentialEntry(-1);
                try {
                    WindowManagerGlobal.getWindowManagerService().lockNow(null);
                } catch (RemoteException e) {
                    Log.e(LegacyGlobalActions.TAG, "Error while trying to lock device.", e);
                }
            }

            public boolean showDuringKeyguard() {
                return true;
            }

            public boolean showBeforeProvisioning() {
                return false;
            }
        };
    }

    private UserInfo getCurrentUser() {
        try {
            return ActivityManager.getService().getCurrentUser();
        } catch (RemoteException e) {
            return null;
        }
    }

    private boolean isCurrentUserOwner() {
        UserInfo currentUser = getCurrentUser();
        return currentUser == null || currentUser.isPrimary();
    }

    private void addUsersToMenu(ArrayList<Action> items) {
        Drawable drawable;
        UserManager um = (UserManager) this.mContext.getSystemService("user");
        if (um.isUserSwitcherEnabled()) {
            List<UserInfo> users = um.getUsers();
            UserInfo currentUser = getCurrentUser();
            for (UserInfo user : users) {
                if (user.supportsSwitchToByUser()) {
                    boolean z = false;
                    if (currentUser != null ? currentUser.id == user.id : user.id == 0) {
                        z = true;
                    }
                    boolean isCurrentUser = z;
                    if (user.iconPath != null) {
                        drawable = Drawable.createFromPath(user.iconPath);
                    } else {
                        drawable = null;
                    }
                    Drawable icon = drawable;
                    StringBuilder sb = new StringBuilder();
                    sb.append(user.name != null ? user.name : "Primary");
                    sb.append(isCurrentUser ? " ✔" : BackupManagerConstants.DEFAULT_BACKUP_FINISHED_NOTIFICATION_RECEIVERS);
                    final UserInfo userInfo = user;
                    SinglePressAction switchToUser = new SinglePressAction(17302636, icon, sb.toString()) {
                        public void onPress() {
                            try {
                                ActivityManager.getService().switchUser(userInfo.id);
                            } catch (RemoteException re) {
                                Log.e(LegacyGlobalActions.TAG, "Couldn't switch user " + re);
                            }
                        }

                        public boolean showDuringKeyguard() {
                            return true;
                        }

                        public boolean showBeforeProvisioning() {
                            return false;
                        }
                    };
                    items.add(switchToUser);
                }
            }
        }
    }

    private void prepareDialog() {
        refreshSilentMode();
        boolean airplaneModeOn = true;
        if (Settings.System.getInt(this.mContext.getContentResolver(), "airplane_mode_on", 0) != 1) {
            airplaneModeOn = false;
        }
        this.mAirplaneState = airplaneModeOn ? ToggleAction.State.On : ToggleAction.State.Off;
        this.mAirplaneModeOn.updateState(this.mAirplaneState);
        this.mAdapter.notifyDataSetChanged();
        this.mDialog.getWindow().setType(2009);
        if (this.mShowSilentToggle) {
            IntentFilter filter = new IntentFilter("android.media.RINGER_MODE_CHANGED");
            Context context = this.mContext;
            AnonymousClass9 r3 = new BroadcastReceiver() {
                public void onReceive(Context context, Intent intent) {
                    String action = intent.getAction();
                    if (action != null && "android.media.RINGER_MODE_CHANGED".equals(action)) {
                        LegacyGlobalActions.this.mHandler.sendEmptyMessage(1);
                    }
                }
            };
            this.mRingerModeReceiver = r3;
            context.registerReceiver(r3, filter);
        }
    }

    /* access modifiers changed from: private */
    public void refreshSilentMode() {
        if (!this.mHasVibrator) {
            this.mSilentModeAction.updateState(this.mAudioManager.getRingerMode() != 2 ? ToggleAction.State.On : ToggleAction.State.Off);
        }
    }

    public void onDismiss(DialogInterface dialog) {
        if (this.mOnDismiss != null) {
            this.mOnDismiss.run();
        }
        if (this.mShowSilentToggle) {
            try {
                if (this.mRingerModeReceiver != null) {
                    this.mContext.unregisterReceiver(this.mRingerModeReceiver);
                    this.mRingerModeReceiver = null;
                }
            } catch (IllegalArgumentException ie) {
                Log.w(TAG, ie);
            }
        }
    }

    public void onClick(DialogInterface dialog, int which) {
        if (!(this.mAdapter.getItem(which) instanceof SilentModeTriStateAction)) {
            dialog.dismiss();
        }
        this.mAdapter.getItem(which).onPress();
    }

    /* access modifiers changed from: private */
    public void onAirplaneModeChanged() {
        if (!this.mHasTelephony) {
            boolean airplaneModeOn = true;
            if (Settings.Global.getInt(this.mContext.getContentResolver(), "airplane_mode_on", 0) != 1) {
                airplaneModeOn = false;
            }
            this.mAirplaneState = airplaneModeOn ? ToggleAction.State.On : ToggleAction.State.Off;
            this.mAirplaneModeOn.updateState(this.mAirplaneState);
        }
    }

    /* access modifiers changed from: private */
    public void changeAirplaneModeSystemSetting(boolean on) {
        Settings.Global.putInt(this.mContext.getContentResolver(), "airplane_mode_on", on);
        Intent intent = new Intent("android.intent.action.AIRPLANE_MODE");
        intent.addFlags(536870912);
        intent.putExtra(AudioService.CONNECT_INTENT_KEY_STATE, on);
        this.mContext.sendBroadcastAsUser(intent, UserHandle.ALL);
        if (!this.mHasTelephony) {
            this.mAirplaneState = on ? ToggleAction.State.On : ToggleAction.State.Off;
        }
    }
}
