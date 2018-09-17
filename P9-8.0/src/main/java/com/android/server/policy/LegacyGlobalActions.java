package com.android.server.policy;

import android.app.ActivityManager;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.DialogInterface.OnDismissListener;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.UserInfo;
import android.database.ContentObserver;
import android.graphics.drawable.Drawable;
import android.media.AudioManager;
import android.net.ConnectivityManager;
import android.os.Build;
import android.os.Build.VERSION;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.os.UserManager;
import android.os.Vibrator;
import android.provider.Settings.Global;
import android.provider.Settings.System;
import android.service.dreams.IDreamManager;
import android.service.dreams.IDreamManager.Stub;
import android.telephony.PhoneStateListener;
import android.telephony.ServiceState;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.ArraySet;
import android.util.Log;
import android.util.TypedValue;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager.LayoutParams;
import android.view.WindowManagerGlobal;
import android.view.WindowManagerPolicy.WindowManagerFuncs;
import android.view.accessibility.AccessibilityEvent;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.ListView;
import android.widget.TextView;
import com.android.internal.app.AlertController;
import com.android.internal.app.AlertController.AlertParams;
import com.android.internal.logging.MetricsLogger;
import com.android.internal.util.EmergencyAffordanceManager;
import com.android.internal.widget.LockPatternUtils;
import com.android.server.audio.AudioService;
import java.util.ArrayList;
import java.util.List;

class LegacyGlobalActions implements OnDismissListener, OnClickListener {
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
    private MyAdapter mAdapter;
    private ContentObserver mAirplaneModeObserver = new ContentObserver(new Handler()) {
        public void onChange(boolean selfChange) {
            LegacyGlobalActions.this.onAirplaneModeChanged();
        }
    };
    private ToggleAction mAirplaneModeOn;
    private State mAirplaneState = State.Off;
    private final AudioManager mAudioManager;
    private BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if ("android.intent.action.CLOSE_SYSTEM_DIALOGS".equals(action) || "android.intent.action.SCREEN_OFF".equals(action)) {
                if (!PhoneWindowManager.SYSTEM_DIALOG_REASON_GLOBAL_ACTIONS.equals(intent.getStringExtra(PhoneWindowManager.SYSTEM_DIALOG_REASON_KEY))) {
                    LegacyGlobalActions.this.mHandler.sendEmptyMessage(0);
                }
            } else if ("android.intent.action.EMERGENCY_CALLBACK_MODE_CHANGED".equals(action) && !intent.getBooleanExtra("PHONE_IN_ECM_STATE", false) && LegacyGlobalActions.this.mIsWaitingForEcmExit) {
                LegacyGlobalActions.this.mIsWaitingForEcmExit = false;
                LegacyGlobalActions.this.changeAirplaneModeSystemSetting(true);
            }
        }
    };
    private final Context mContext;
    private boolean mDeviceProvisioned = false;
    private GlobalActionsDialog mDialog;
    private final IDreamManager mDreamManager;
    private final EmergencyAffordanceManager mEmergencyAffordanceManager;
    private Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 0:
                    if (LegacyGlobalActions.this.mDialog != null) {
                        LegacyGlobalActions.this.mDialog.dismiss();
                        LegacyGlobalActions.this.mDialog = null;
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
    private boolean mHasTelephony;
    private boolean mHasVibrator;
    private boolean mIsWaitingForEcmExit = false;
    private ArrayList<Action> mItems;
    private boolean mKeyguardShowing = false;
    private final Runnable mOnDismiss;
    PhoneStateListener mPhoneStateListener = new PhoneStateListener() {
        public void onServiceStateChanged(ServiceState serviceState) {
            if (LegacyGlobalActions.this.mHasTelephony) {
                LegacyGlobalActions.this.mAirplaneState = serviceState.getState() == 3 ? State.On : State.Off;
                LegacyGlobalActions.this.mAirplaneModeOn.updateState(LegacyGlobalActions.this.mAirplaneState);
                LegacyGlobalActions.this.mAdapter.notifyDataSetChanged();
            }
        }
    };
    private BroadcastReceiver mRingerModeReceiver = null;
    private final boolean mShowSilentToggle;
    private Action mSilentModeAction;
    private final WindowManagerFuncs mWindowManagerFuncs;

    public interface Action {
        View create(Context context, View view, ViewGroup viewGroup, LayoutInflater layoutInflater);

        CharSequence getLabelForAccessibility(Context context);

        boolean isEnabled();

        void onPress();

        boolean showBeforeProvisioning();

        boolean showDuringKeyguard();
    }

    private static abstract class SinglePressAction implements Action {
        private final Drawable mIcon;
        private final int mIconResId;
        private final CharSequence mMessage;
        private final int mMessageResId;

        public abstract void onPress();

        protected SinglePressAction(int iconResId, int messageResId) {
            this.mIconResId = iconResId;
            this.mMessageResId = messageResId;
            this.mMessage = null;
            this.mIcon = null;
        }

        protected SinglePressAction(int iconResId, Drawable icon, CharSequence message) {
            this.mIconResId = iconResId;
            this.mMessageResId = 0;
            this.mMessage = message;
            this.mIcon = icon;
        }

        public boolean isEnabled() {
            return true;
        }

        public String getStatus() {
            return null;
        }

        public CharSequence getLabelForAccessibility(Context context) {
            if (this.mMessage != null) {
                return this.mMessage;
            }
            return context.getString(this.mMessageResId);
        }

        public View create(Context context, View convertView, ViewGroup parent, LayoutInflater inflater) {
            View v = inflater.inflate(17367144, parent, false);
            ImageView icon = (ImageView) v.findViewById(16908294);
            TextView messageView = (TextView) v.findViewById(16908299);
            TextView statusView = (TextView) v.findViewById(16909328);
            String status = getStatus();
            if (TextUtils.isEmpty(status)) {
                statusView.setVisibility(8);
            } else {
                statusView.setText(status);
            }
            if (this.mIcon != null) {
                icon.setImageDrawable(this.mIcon);
                icon.setScaleType(ScaleType.CENTER_CROP);
            } else if (this.mIconResId != 0) {
                icon.setImageDrawable(context.getDrawable(this.mIconResId));
            }
            if (this.mMessage != null) {
                messageView.setText(this.mMessage);
            } else {
                messageView.setText(this.mMessageResId);
            }
            return v;
        }
    }

    public static abstract class ToggleAction implements Action {
        protected int mDisabledIconResid;
        protected int mDisabledStatusMessageResId;
        protected int mEnabledIconResId;
        protected int mEnabledStatusMessageResId;
        protected int mMessageResId;
        protected State mState = State.Off;

        enum State {
            Off(false),
            TurningOn(true),
            TurningOff(true),
            On(false);
            
            private final boolean inTransition;

            private State(boolean intermediate) {
                this.inTransition = intermediate;
            }

            public boolean inTransition() {
                return this.inTransition;
            }
        }

        abstract void onToggle(boolean z);

        public ToggleAction(int enabledIconResId, int disabledIconResid, int message, int enabledStatusMessageResId, int disabledStatusMessageResId) {
            this.mEnabledIconResId = enabledIconResId;
            this.mDisabledIconResid = disabledIconResid;
            this.mMessageResId = message;
            this.mEnabledStatusMessageResId = enabledStatusMessageResId;
            this.mDisabledStatusMessageResId = disabledStatusMessageResId;
        }

        void willCreate() {
        }

        public CharSequence getLabelForAccessibility(Context context) {
            return context.getString(this.mMessageResId);
        }

        public View create(Context context, View convertView, ViewGroup parent, LayoutInflater inflater) {
            willCreate();
            View v = inflater.inflate(17367144, parent, false);
            ImageView icon = (ImageView) v.findViewById(16908294);
            TextView messageView = (TextView) v.findViewById(16908299);
            TextView statusView = (TextView) v.findViewById(16909328);
            boolean enabled = isEnabled();
            if (messageView != null) {
                messageView.setText(this.mMessageResId);
                messageView.setEnabled(enabled);
            }
            boolean on = this.mState == State.On || this.mState == State.TurningOn;
            if (icon != null) {
                icon.setImageDrawable(context.getDrawable(on ? this.mEnabledIconResId : this.mDisabledIconResid));
                icon.setEnabled(enabled);
            }
            if (statusView != null) {
                statusView.setText(on ? this.mEnabledStatusMessageResId : this.mDisabledStatusMessageResId);
                statusView.setVisibility(0);
                statusView.setEnabled(enabled);
            }
            v.setEnabled(enabled);
            return v;
        }

        public final void onPress() {
            if (this.mState.inTransition()) {
                Log.w(LegacyGlobalActions.TAG, "shouldn't be able to toggle when in transition");
                return;
            }
            boolean nowOn = this.mState != State.On;
            onToggle(nowOn);
            changeStateFromPress(nowOn);
        }

        public boolean isEnabled() {
            return this.mState.inTransition() ^ 1;
        }

        protected void changeStateFromPress(boolean buttonOn) {
            this.mState = buttonOn ? State.On : State.Off;
        }

        public void updateState(State state) {
            this.mState = state;
        }
    }

    private interface LongPressAction extends Action {
        boolean onLongPress();
    }

    private class BugReportAction extends SinglePressAction implements LongPressAction {
        public BugReportAction() {
            super(17302411, 17039694);
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
            return LegacyGlobalActions.this.mContext.getString(17039693, new Object[]{VERSION.RELEASE, Build.ID});
        }
    }

    private static final class GlobalActionsDialog extends Dialog implements DialogInterface {
        private final MyAdapter mAdapter;
        private final AlertController mAlert = AlertController.create(this.mContext, this, getWindow());
        private final Context mContext = getContext();

        public GlobalActionsDialog(Context context, AlertParams params) {
            super(context, getDialogTheme(context));
            this.mAdapter = (MyAdapter) params.mAdapter;
            params.apply(this.mAlert);
        }

        private static int getDialogTheme(Context context) {
            TypedValue outValue = new TypedValue();
            context.getTheme().resolveAttribute(16843529, outValue, true);
            return outValue.resourceId;
        }

        protected void onStart() {
            super.setCanceledOnTouchOutside(true);
            super.onStart();
        }

        public ListView getListView() {
            return this.mAlert.getListView();
        }

        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            this.mAlert.installContent();
        }

        public boolean dispatchPopulateAccessibilityEvent(AccessibilityEvent event) {
            if (event.getEventType() == 32) {
                for (int i = 0; i < this.mAdapter.getCount(); i++) {
                    CharSequence label = this.mAdapter.getItem(i).getLabelForAccessibility(getContext());
                    if (label != null) {
                        event.getText().add(label);
                    }
                }
            }
            return super.dispatchPopulateAccessibilityEvent(event);
        }

        public boolean onKeyDown(int keyCode, KeyEvent event) {
            if (this.mAlert.onKeyDown(keyCode, event)) {
                return true;
            }
            return super.onKeyDown(keyCode, event);
        }

        public boolean onKeyUp(int keyCode, KeyEvent event) {
            if (this.mAlert.onKeyUp(keyCode, event)) {
                return true;
            }
            return super.onKeyUp(keyCode, event);
        }
    }

    private class MyAdapter extends BaseAdapter {
        /* synthetic */ MyAdapter(LegacyGlobalActions this$0, MyAdapter -this1) {
            this();
        }

        private MyAdapter() {
        }

        public int getCount() {
            int count = 0;
            for (int i = 0; i < LegacyGlobalActions.this.mItems.size(); i++) {
                Action action = (Action) LegacyGlobalActions.this.mItems.get(i);
                if ((!LegacyGlobalActions.this.mKeyguardShowing || (action.showDuringKeyguard() ^ 1) == 0) && (LegacyGlobalActions.this.mDeviceProvisioned || (action.showBeforeProvisioning() ^ 1) == 0)) {
                    count++;
                }
            }
            return count;
        }

        public boolean isEnabled(int position) {
            return getItem(position).isEnabled();
        }

        public boolean areAllItemsEnabled() {
            return false;
        }

        public Action getItem(int position) {
            int filteredPos = 0;
            for (int i = 0; i < LegacyGlobalActions.this.mItems.size(); i++) {
                Action action = (Action) LegacyGlobalActions.this.mItems.get(i);
                if ((!LegacyGlobalActions.this.mKeyguardShowing || (action.showDuringKeyguard() ^ 1) == 0) && (LegacyGlobalActions.this.mDeviceProvisioned || (action.showBeforeProvisioning() ^ 1) == 0)) {
                    if (filteredPos == position) {
                        return action;
                    }
                    filteredPos++;
                }
            }
            throw new IllegalArgumentException("position " + position + " out of range of showable actions" + ", filtered count=" + getCount() + ", keyguardshowing=" + LegacyGlobalActions.this.mKeyguardShowing + ", provisioned=" + LegacyGlobalActions.this.mDeviceProvisioned);
        }

        public long getItemId(int position) {
            return (long) position;
        }

        public View getView(int position, View convertView, ViewGroup parent) {
            return getItem(position).create(LegacyGlobalActions.this.mContext, convertView, parent, LayoutInflater.from(LegacyGlobalActions.this.mContext));
        }
    }

    private final class PowerAction extends SinglePressAction implements LongPressAction {
        /* synthetic */ PowerAction(LegacyGlobalActions this$0, PowerAction -this1) {
            this();
        }

        private PowerAction() {
            super(17301552, 17040069);
        }

        public boolean onLongPress() {
            if (((UserManager) LegacyGlobalActions.this.mContext.getSystemService("user")).hasUserRestriction("no_safe_boot")) {
                return false;
            }
            LegacyGlobalActions.this.mWindowManagerFuncs.rebootSafeMode(true);
            return true;
        }

        public boolean showDuringKeyguard() {
            return true;
        }

        public boolean showBeforeProvisioning() {
            return true;
        }

        public void onPress() {
            LegacyGlobalActions.this.mWindowManagerFuncs.shutdown(true);
        }
    }

    private final class RestartAction extends SinglePressAction implements LongPressAction {
        /* synthetic */ RestartAction(LegacyGlobalActions this$0, RestartAction -this1) {
            this();
        }

        private RestartAction() {
            super(17302600, 17040070);
        }

        public boolean onLongPress() {
            if (((UserManager) LegacyGlobalActions.this.mContext.getSystemService("user")).hasUserRestriction("no_safe_boot")) {
                return false;
            }
            LegacyGlobalActions.this.mWindowManagerFuncs.rebootSafeMode(true);
            return true;
        }

        public boolean showDuringKeyguard() {
            return true;
        }

        public boolean showBeforeProvisioning() {
            return true;
        }

        public void onPress() {
            LegacyGlobalActions.this.mWindowManagerFuncs.reboot(false);
        }
    }

    private class SilentModeToggleAction extends ToggleAction {
        public SilentModeToggleAction() {
            super(17302281, 17302279, 17040074, 17040073, 17040072);
        }

        void onToggle(boolean on) {
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
        private final int[] ITEM_IDS = new int[]{16909128, 16909129, 16909130};
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
            View v = inflater.inflate(17367145, parent, false);
            int selectedIndex = ringerModeToIndex(this.mAudioManager.getRingerMode());
            for (int i = 0; i < 3; i++) {
                boolean z;
                View itemView = v.findViewById(this.ITEM_IDS[i]);
                if (selectedIndex == i) {
                    z = true;
                } else {
                    z = false;
                }
                itemView.setSelected(z);
                itemView.setTag(Integer.valueOf(i));
                itemView.setOnClickListener(this);
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

        void willCreate() {
        }

        public void onClick(View v) {
            if (v.getTag() instanceof Integer) {
                this.mAudioManager.setRingerMode(indexToRingerMode(((Integer) v.getTag()).intValue()));
                this.mHandler.sendEmptyMessageDelayed(0, 300);
            }
        }
    }

    public LegacyGlobalActions(Context context, WindowManagerFuncs windowManagerFuncs, Runnable onDismiss) {
        this.mContext = context;
        this.mWindowManagerFuncs = windowManagerFuncs;
        this.mOnDismiss = onDismiss;
        this.mAudioManager = (AudioManager) this.mContext.getSystemService("audio");
        this.mDreamManager = Stub.asInterface(ServiceManager.getService("dreams"));
        IntentFilter filter = new IntentFilter();
        filter.addAction("android.intent.action.CLOSE_SYSTEM_DIALOGS");
        filter.addAction("android.intent.action.SCREEN_OFF");
        filter.addAction("android.intent.action.EMERGENCY_CALLBACK_MODE_CHANGED");
        context.registerReceiver(this.mBroadcastReceiver, filter);
        this.mHasTelephony = ((ConnectivityManager) context.getSystemService("connectivity")).isNetworkSupported(0);
        ((TelephonyManager) context.getSystemService("phone")).listen(this.mPhoneStateListener, 1);
        this.mContext.getContentResolver().registerContentObserver(Global.getUriFor("airplane_mode_on"), true, this.mAirplaneModeObserver);
        Vibrator vibrator = (Vibrator) this.mContext.getSystemService("vibrator");
        this.mHasVibrator = vibrator != null ? vibrator.hasVibrator() : false;
        this.mShowSilentToggle = this.mContext.getResources().getBoolean(17957039) ^ 1;
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

    private void handleShow() {
        awakenIfNecessary();
        this.mDialog = createDialog();
        prepareDialog();
        if (this.mAdapter.getCount() == 1 && (this.mAdapter.getItem(0) instanceof SinglePressAction) && ((this.mAdapter.getItem(0) instanceof LongPressAction) ^ 1) != 0) {
            ((SinglePressAction) this.mAdapter.getItem(0)).onPress();
            return;
        }
        LayoutParams attrs = this.mDialog.getWindow().getAttributes();
        attrs.setTitle(TAG);
        this.mDialog.getWindow().setAttributes(attrs);
        this.mDialog.show();
        this.mDialog.getWindow().getDecorView().setSystemUiVisibility(65536);
    }

    private GlobalActionsDialog createDialog() {
        if (this.mHasVibrator) {
            this.mSilentModeAction = new SilentModeTriStateAction(this.mContext, this.mAudioManager, this.mHandler);
        } else {
            this.mSilentModeAction = new SilentModeToggleAction();
        }
        this.mAirplaneModeOn = new ToggleAction(17302407, 17302409, 17040079, 17040078, 17040077) {
            void onToggle(boolean on) {
                if (LegacyGlobalActions.this.mHasTelephony && Boolean.parseBoolean(SystemProperties.get("ril.cdma.inecmmode"))) {
                    LegacyGlobalActions.this.mIsWaitingForEcmExit = true;
                    Intent ecmDialogIntent = new Intent("com.android.internal.intent.action.ACTION_SHOW_NOTICE_ECM_BLOCK_OTHERS", null);
                    ecmDialogIntent.addFlags(268435456);
                    LegacyGlobalActions.this.mContext.startActivity(ecmDialogIntent);
                    return;
                }
                LegacyGlobalActions.this.changeAirplaneModeSystemSetting(on);
            }

            protected void changeStateFromPress(boolean buttonOn) {
                if (LegacyGlobalActions.this.mHasTelephony && !Boolean.parseBoolean(SystemProperties.get("ril.cdma.inecmmode"))) {
                    this.mState = buttonOn ? State.TurningOn : State.TurningOff;
                    LegacyGlobalActions.this.mAirplaneState = this.mState;
                }
            }

            public boolean showDuringKeyguard() {
                return true;
            }

            public boolean showBeforeProvisioning() {
                return false;
            }
        };
        onAirplaneModeChanged();
        this.mItems = new ArrayList();
        String[] defaultActions = this.mContext.getResources().getStringArray(17236010);
        ArraySet<String> addedKeys = new ArraySet();
        for (String actionKey : defaultActions) {
            if (!addedKeys.contains(actionKey)) {
                if (GLOBAL_ACTION_KEY_POWER.equals(actionKey)) {
                    this.mItems.add(new PowerAction(this, null));
                } else if (GLOBAL_ACTION_KEY_REBOOT.equals(actionKey)) {
                    HwPolicyFactory.addRebootMenu(this.mItems);
                } else if (GLOBAL_ACTION_KEY_AIRPLANE.equals(actionKey)) {
                    this.mItems.add(this.mAirplaneModeOn);
                } else if (GLOBAL_ACTION_KEY_BUGREPORT.equals(actionKey)) {
                    if (Global.getInt(this.mContext.getContentResolver(), "bugreport_in_power_menu", 0) != 0 && isCurrentUserOwner()) {
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
                    this.mItems.add(new RestartAction(this, null));
                } else {
                    Log.e(TAG, "Invalid global action key " + actionKey);
                }
                addedKeys.add(actionKey);
            }
        }
        if (this.mEmergencyAffordanceManager.needsEmergencyAffordance()) {
            this.mItems.add(getEmergencyAction());
        }
        this.mAdapter = new MyAdapter(this, null);
        AlertParams params = new AlertParams(this.mContext);
        params.mAdapter = this.mAdapter;
        params.mOnClickListener = this;
        params.mForceInverseBackground = true;
        GlobalActionsDialog dialog = new GlobalActionsDialog(this.mContext, params);
        dialog.setCanceledOnTouchOutside(false);
        dialog.getListView().setItemsCanFocus(true);
        dialog.getListView().setLongClickable(true);
        dialog.getListView().setOnItemLongClickListener(new OnItemLongClickListener() {
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, int position, long id) {
                Action action = LegacyGlobalActions.this.mAdapter.getItem(position);
                if (action instanceof LongPressAction) {
                    return ((LongPressAction) action).onLongPress();
                }
                return false;
            }
        });
        dialog.getWindow().setType(2009);
        dialog.setOnDismissListener(this);
        return dialog;
    }

    private Action getSettingsAction() {
        return new SinglePressAction(17302607, 17040071) {
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
        return new SinglePressAction(17302178, 17040066) {
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
        return new SinglePressAction(17302257, 17040064) {
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
        return new SinglePressAction(17302637, 17040075) {
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
        return new SinglePressAction(17301551, 17040068) {
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
        return currentUser != null ? currentUser.isPrimary() : true;
    }

    /* JADX WARNING: Removed duplicated region for block: B:24:0x0072  */
    /* JADX WARNING: Removed duplicated region for block: B:13:0x003a  */
    /* JADX WARNING: Removed duplicated region for block: B:25:0x0074  */
    /* JADX WARNING: Removed duplicated region for block: B:16:0x004b  */
    /* JADX WARNING: Removed duplicated region for block: B:26:0x0078  */
    /* JADX WARNING: Removed duplicated region for block: B:19:0x0053  */
    /* JADX WARNING: Removed duplicated region for block: B:13:0x003a  */
    /* JADX WARNING: Removed duplicated region for block: B:24:0x0072  */
    /* JADX WARNING: Removed duplicated region for block: B:16:0x004b  */
    /* JADX WARNING: Removed duplicated region for block: B:25:0x0074  */
    /* JADX WARNING: Removed duplicated region for block: B:19:0x0053  */
    /* JADX WARNING: Removed duplicated region for block: B:26:0x0078  */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void addUsersToMenu(ArrayList<Action> items) {
        UserManager um = (UserManager) this.mContext.getSystemService("user");
        if (um.isUserSwitcherEnabled()) {
            List<UserInfo> users = um.getUsers();
            UserInfo currentUser = getCurrentUser();
            for (final UserInfo user : users) {
                if (user.supportsSwitchToByUser()) {
                    boolean isCurrentUser;
                    Drawable icon;
                    if (currentUser == null) {
                        isCurrentUser = false;
                        if (user.iconPath != null) {
                            icon = Drawable.createFromPath(user.iconPath);
                        } else {
                            icon = null;
                        }
                        items.add(new SinglePressAction(17302499, icon, (user.name != null ? user.name : "Primary") + (isCurrentUser ? " ✔" : "")) {
                            public void onPress() {
                                try {
                                    ActivityManager.getService().switchUser(user.id);
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
                        });
                    } else {
                        isCurrentUser = false;
                        if (user.iconPath != null) {
                        }
                        if (user.name != null) {
                        }
                        if (isCurrentUser) {
                        }
                        items.add(/* anonymous class already generated */);
                    }
                    isCurrentUser = true;
                    if (user.iconPath != null) {
                    }
                    if (user.name != null) {
                    }
                    if (isCurrentUser) {
                    }
                    items.add(/* anonymous class already generated */);
                }
            }
        }
    }

    private void prepareDialog() {
        refreshSilentMode();
        this.mAirplaneState = System.getInt(this.mContext.getContentResolver(), "airplane_mode_on", 0) == 1 ? State.On : State.Off;
        this.mAirplaneModeOn.updateState(this.mAirplaneState);
        this.mAdapter.notifyDataSetChanged();
        this.mDialog.getWindow().setType(2009);
        if (this.mShowSilentToggle) {
            IntentFilter filter = new IntentFilter("android.media.RINGER_MODE_CHANGED");
            Context context = this.mContext;
            BroadcastReceiver anonymousClass13 = new BroadcastReceiver() {
                public void onReceive(Context context, Intent intent) {
                    String action = intent.getAction();
                    if (action != null && "android.media.RINGER_MODE_CHANGED".equals(action)) {
                        LegacyGlobalActions.this.mHandler.sendEmptyMessage(1);
                    }
                }
            };
            this.mRingerModeReceiver = anonymousClass13;
            context.registerReceiver(anonymousClass13, filter);
        }
    }

    private void refreshSilentMode() {
        if (!this.mHasVibrator) {
            ((ToggleAction) this.mSilentModeAction).updateState(this.mAudioManager.getRingerMode() != 2 ? State.On : State.Off);
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

    private void onAirplaneModeChanged() {
        if (!this.mHasTelephony) {
            this.mAirplaneState = Global.getInt(this.mContext.getContentResolver(), "airplane_mode_on", 0) == 1 ? State.On : State.Off;
            this.mAirplaneModeOn.updateState(this.mAirplaneState);
        }
    }

    private void changeAirplaneModeSystemSetting(boolean on) {
        Global.putInt(this.mContext.getContentResolver(), "airplane_mode_on", on ? 1 : 0);
        Intent intent = new Intent("android.intent.action.AIRPLANE_MODE");
        intent.addFlags(536870912);
        intent.putExtra(AudioService.CONNECT_INTENT_KEY_STATE, on);
        this.mContext.sendBroadcastAsUser(intent, UserHandle.ALL);
        if (!this.mHasTelephony) {
            this.mAirplaneState = on ? State.On : State.Off;
        }
    }
}
