package com.android.server.policy;

import android.app.ActivityManager;
import android.app.ActivityManagerNative;
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
import android.os.SystemClock;
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
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
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
import com.android.internal.widget.LockPatternUtils;
import com.android.server.audio.AudioService;
import java.util.ArrayList;
import java.util.List;

class GlobalActions implements OnDismissListener, OnClickListener {
    private static final int DIALOG_DISMISS_DELAY = 300;
    private static final String GLOBAL_ACTION_KEY_AIRPLANE = "airplane";
    private static final String GLOBAL_ACTION_KEY_ASSIST = "assist";
    private static final String GLOBAL_ACTION_KEY_BUGREPORT = "bugreport";
    private static final String GLOBAL_ACTION_KEY_LOCKDOWN = "lockdown";
    private static final String GLOBAL_ACTION_KEY_POWER = "power";
    private static final String GLOBAL_ACTION_KEY_REBOOT = "hwrestart";
    private static final String GLOBAL_ACTION_KEY_SETTINGS = "settings";
    private static final String GLOBAL_ACTION_KEY_SILENT = "silent";
    private static final String GLOBAL_ACTION_KEY_USERS = "users";
    private static final String GLOBAL_ACTION_KEY_VOICEASSIST = "voiceassist";
    private static final int MESSAGE_DISMISS = 0;
    private static final int MESSAGE_REFRESH = 1;
    private static final int MESSAGE_SHOW = 2;
    private static final boolean SHOW_SILENT_TOGGLE = true;
    private static final String TAG = "GlobalActions";
    private MyAdapter mAdapter;
    private ContentObserver mAirplaneModeObserver;
    private ToggleAction mAirplaneModeOn;
    private State mAirplaneState;
    private final AudioManager mAudioManager;
    private BroadcastReceiver mBroadcastReceiver;
    private final Context mContext;
    private boolean mDeviceProvisioned;
    private GlobalActionsDialog mDialog;
    private final IDreamManager mDreamManager;
    private Handler mHandler;
    private boolean mHasTelephony;
    private boolean mHasVibrator;
    private boolean mIsWaitingForEcmExit;
    private ArrayList<Action> mItems;
    private boolean mKeyguardShowing;
    PhoneStateListener mPhoneStateListener;
    private BroadcastReceiver mRingerModeReceiver;
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
            this.mMessageResId = GlobalActions.MESSAGE_DISMISS;
            this.mMessage = message;
            this.mIcon = icon;
        }

        public boolean isEnabled() {
            return GlobalActions.SHOW_SILENT_TOGGLE;
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
            View v = inflater.inflate(17367142, parent, false);
            ImageView icon = (ImageView) v.findViewById(16908294);
            TextView messageView = (TextView) v.findViewById(16908299);
            TextView statusView = (TextView) v.findViewById(16909152);
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

    /* renamed from: com.android.server.policy.GlobalActions.10 */
    class AnonymousClass10 extends SinglePressAction {
        AnonymousClass10(int $anonymous0, int $anonymous1) {
            super($anonymous0, $anonymous1);
        }

        public void onPress() {
            new LockPatternUtils(GlobalActions.this.mContext).requireCredentialEntry(-1);
            try {
                WindowManagerGlobal.getWindowManagerService().lockNow(null);
            } catch (RemoteException e) {
                Log.e(GlobalActions.TAG, "Error while trying to lock device.", e);
            }
        }

        public boolean showDuringKeyguard() {
            return GlobalActions.SHOW_SILENT_TOGGLE;
        }

        public boolean showBeforeProvisioning() {
            return false;
        }
    }

    /* renamed from: com.android.server.policy.GlobalActions.11 */
    class AnonymousClass11 extends SinglePressAction {
        final /* synthetic */ UserInfo val$user;

        AnonymousClass11(int $anonymous0, Drawable $anonymous1, CharSequence $anonymous2, UserInfo val$user) {
            this.val$user = val$user;
            super($anonymous0, $anonymous1, $anonymous2);
        }

        public void onPress() {
            try {
                ActivityManagerNative.getDefault().switchUser(this.val$user.id);
            } catch (RemoteException re) {
                Log.e(GlobalActions.TAG, "Couldn't switch user " + re);
            }
        }

        public boolean showDuringKeyguard() {
            return GlobalActions.SHOW_SILENT_TOGGLE;
        }

        public boolean showBeforeProvisioning() {
            return false;
        }
    }

    /* renamed from: com.android.server.policy.GlobalActions.3 */
    class AnonymousClass3 extends ContentObserver {
        AnonymousClass3(Handler $anonymous0) {
            super($anonymous0);
        }

        public void onChange(boolean selfChange) {
            GlobalActions.this.onAirplaneModeChanged();
        }
    }

    public static abstract class ToggleAction implements Action {
        protected int mDisabledIconResid;
        protected int mDisabledStatusMessageResId;
        protected int mEnabledIconResId;
        protected int mEnabledStatusMessageResId;
        protected int mMessageResId;
        protected State mState;

        enum State {
            ;
            
            private final boolean inTransition;

            static {
                /* JADX: method processing error */
/*
                Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.server.policy.GlobalActions.ToggleAction.State.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:263)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:263)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.server.policy.GlobalActions.ToggleAction.State.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 7 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 8 more
*/
                /*
                // Can't load method instructions.
                */
                throw new UnsupportedOperationException("Method not decompiled: com.android.server.policy.GlobalActions.ToggleAction.State.<clinit>():void");
            }

            private State(boolean intermediate) {
                this.inTransition = intermediate;
            }

            public boolean inTransition() {
                return this.inTransition;
            }
        }

        abstract void onToggle(boolean z);

        public ToggleAction(int enabledIconResId, int disabledIconResid, int message, int enabledStatusMessageResId, int disabledStatusMessageResId) {
            this.mState = State.Off;
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
            View v = inflater.inflate(17367142, parent, false);
            ImageView icon = (ImageView) v.findViewById(16908294);
            TextView messageView = (TextView) v.findViewById(16908299);
            TextView statusView = (TextView) v.findViewById(16909152);
            boolean enabled = isEnabled();
            if (messageView != null) {
                messageView.setText(this.mMessageResId);
                messageView.setEnabled(enabled);
            }
            boolean on = (this.mState == State.On || this.mState == State.TurningOn) ? GlobalActions.SHOW_SILENT_TOGGLE : false;
            if (icon != null) {
                icon.setImageDrawable(context.getDrawable(on ? this.mEnabledIconResId : this.mDisabledIconResid));
                icon.setEnabled(enabled);
            }
            if (statusView != null) {
                statusView.setText(on ? this.mEnabledStatusMessageResId : this.mDisabledStatusMessageResId);
                statusView.setVisibility(GlobalActions.MESSAGE_DISMISS);
                statusView.setEnabled(enabled);
            }
            v.setEnabled(enabled);
            return v;
        }

        public final void onPress() {
            if (this.mState.inTransition()) {
                Log.w(GlobalActions.TAG, "shouldn't be able to toggle when in transition");
                return;
            }
            boolean nowOn = this.mState != State.On ? GlobalActions.SHOW_SILENT_TOGGLE : false;
            onToggle(nowOn);
            changeStateFromPress(nowOn);
        }

        public boolean isEnabled() {
            return this.mState.inTransition() ? false : GlobalActions.SHOW_SILENT_TOGGLE;
        }

        protected void changeStateFromPress(boolean buttonOn) {
            this.mState = buttonOn ? State.On : State.Off;
        }

        public void updateState(State state) {
            this.mState = state;
        }
    }

    /* renamed from: com.android.server.policy.GlobalActions.5 */
    class AnonymousClass5 extends ToggleAction {
        final /* synthetic */ GlobalActions this$0;

        AnonymousClass5(GlobalActions this$0, int $anonymous0, int $anonymous1, int $anonymous2, int $anonymous3, int $anonymous4) {
            this.this$0 = this$0;
            super($anonymous0, $anonymous1, $anonymous2, $anonymous3, $anonymous4);
        }

        void onToggle(boolean on) {
            if (this.this$0.mHasTelephony && Boolean.parseBoolean(SystemProperties.get("ril.cdma.inecmmode"))) {
                this.this$0.mIsWaitingForEcmExit = GlobalActions.SHOW_SILENT_TOGGLE;
                Intent ecmDialogIntent = new Intent("android.intent.action.ACTION_SHOW_NOTICE_ECM_BLOCK_OTHERS", null);
                ecmDialogIntent.addFlags(268435456);
                this.this$0.mContext.startActivity(ecmDialogIntent);
                return;
            }
            this.this$0.changeAirplaneModeSystemSetting(on);
        }

        protected void changeStateFromPress(boolean buttonOn) {
            if (this.this$0.mHasTelephony && !Boolean.parseBoolean(SystemProperties.get("ril.cdma.inecmmode"))) {
                this.mState = buttonOn ? State.TurningOn : State.TurningOff;
                this.this$0.mAirplaneState = this.mState;
            }
        }

        public boolean showDuringKeyguard() {
            return GlobalActions.SHOW_SILENT_TOGGLE;
        }

        public boolean showBeforeProvisioning() {
            return false;
        }
    }

    /* renamed from: com.android.server.policy.GlobalActions.6 */
    class AnonymousClass6 implements OnItemLongClickListener {
        final /* synthetic */ GlobalActions this$0;

        AnonymousClass6(GlobalActions this$0) {
            this.this$0 = this$0;
        }

        public boolean onItemLongClick(AdapterView<?> adapterView, View view, int position, long id) {
            Action action = this.this$0.mAdapter.getItem(position);
            if (action instanceof LongPressAction) {
                return ((LongPressAction) action).onLongPress();
            }
            return false;
        }
    }

    /* renamed from: com.android.server.policy.GlobalActions.7 */
    class AnonymousClass7 extends SinglePressAction {
        final /* synthetic */ GlobalActions this$0;

        AnonymousClass7(GlobalActions this$0, int $anonymous0, int $anonymous1) {
            this.this$0 = this$0;
            super($anonymous0, $anonymous1);
        }

        public void onPress() {
            Intent intent = new Intent("android.settings.SETTINGS");
            intent.addFlags(335544320);
            this.this$0.mContext.startActivity(intent);
        }

        public boolean showDuringKeyguard() {
            return GlobalActions.SHOW_SILENT_TOGGLE;
        }

        public boolean showBeforeProvisioning() {
            return GlobalActions.SHOW_SILENT_TOGGLE;
        }
    }

    /* renamed from: com.android.server.policy.GlobalActions.8 */
    class AnonymousClass8 extends SinglePressAction {
        final /* synthetic */ GlobalActions this$0;

        AnonymousClass8(GlobalActions this$0, int $anonymous0, int $anonymous1) {
            this.this$0 = this$0;
            super($anonymous0, $anonymous1);
        }

        public void onPress() {
            Intent intent = new Intent("android.intent.action.ASSIST");
            intent.addFlags(335544320);
            this.this$0.mContext.startActivity(intent);
        }

        public boolean showDuringKeyguard() {
            return GlobalActions.SHOW_SILENT_TOGGLE;
        }

        public boolean showBeforeProvisioning() {
            return GlobalActions.SHOW_SILENT_TOGGLE;
        }
    }

    /* renamed from: com.android.server.policy.GlobalActions.9 */
    class AnonymousClass9 extends SinglePressAction {
        final /* synthetic */ GlobalActions this$0;

        AnonymousClass9(GlobalActions this$0, int $anonymous0, int $anonymous1) {
            this.this$0 = this$0;
            super($anonymous0, $anonymous1);
        }

        public void onPress() {
            Intent intent = new Intent("android.intent.action.VOICE_ASSIST");
            intent.addFlags(335544320);
            this.this$0.mContext.startActivity(intent);
        }

        public boolean showDuringKeyguard() {
            return GlobalActions.SHOW_SILENT_TOGGLE;
        }

        public boolean showBeforeProvisioning() {
            return GlobalActions.SHOW_SILENT_TOGGLE;
        }
    }

    private interface LongPressAction extends Action {
        boolean onLongPress();
    }

    private class BugReportAction extends SinglePressAction implements LongPressAction {
        final /* synthetic */ GlobalActions this$0;

        /* renamed from: com.android.server.policy.GlobalActions.BugReportAction.1 */
        class AnonymousClass1 implements Runnable {
            final /* synthetic */ BugReportAction this$1;

            AnonymousClass1(BugReportAction this$1) {
                this.this$1 = this$1;
            }

            public void run() {
                try {
                    MetricsLogger.action(this.this$1.this$0.mContext, 292);
                    ActivityManagerNative.getDefault().requestBugReport(GlobalActions.MESSAGE_REFRESH);
                } catch (RemoteException e) {
                }
            }
        }

        public BugReportAction(GlobalActions this$0) {
            this.this$0 = this$0;
            super(17302357, 17039660);
        }

        public void onPress() {
            if (!ActivityManager.isUserAMonkey()) {
                this.this$0.mHandler.postDelayed(new AnonymousClass1(this), 500);
            }
        }

        public boolean onLongPress() {
            if (ActivityManager.isUserAMonkey()) {
                return false;
            }
            try {
                MetricsLogger.action(this.this$0.mContext, 293);
                ActivityManagerNative.getDefault().requestBugReport(GlobalActions.MESSAGE_DISMISS);
            } catch (RemoteException e) {
            }
            return false;
        }

        public boolean showDuringKeyguard() {
            return GlobalActions.SHOW_SILENT_TOGGLE;
        }

        public boolean showBeforeProvisioning() {
            return false;
        }

        public String getStatus() {
            Context -get4 = this.this$0.mContext;
            Object[] objArr = new Object[GlobalActions.MESSAGE_SHOW];
            objArr[GlobalActions.MESSAGE_DISMISS] = VERSION.RELEASE;
            objArr[GlobalActions.MESSAGE_REFRESH] = Build.ID;
            return -get4.getString(17039666, objArr);
        }
    }

    private static final class GlobalActionsDialog extends Dialog implements DialogInterface {
        private final MyAdapter mAdapter;
        private final AlertController mAlert;
        private boolean mCancelOnUp;
        private final Context mContext;
        private EnableAccessibilityController mEnableAccessibilityController;
        private boolean mIntercepted;
        private final int mWindowTouchSlop;

        /* renamed from: com.android.server.policy.GlobalActions.GlobalActionsDialog.1 */
        class AnonymousClass1 implements Runnable {
            final /* synthetic */ GlobalActionsDialog this$1;

            AnonymousClass1(GlobalActionsDialog this$1) {
                this.this$1 = this$1;
            }

            public void run() {
                this.this$1.dismiss();
            }
        }

        public GlobalActionsDialog(Context context, AlertParams params) {
            super(context, getDialogTheme(context));
            this.mContext = getContext();
            this.mAlert = new AlertController(this.mContext, this, getWindow());
            this.mAdapter = (MyAdapter) params.mAdapter;
            this.mWindowTouchSlop = ViewConfiguration.get(context).getScaledWindowTouchSlop();
            params.apply(this.mAlert);
        }

        private static int getDialogTheme(Context context) {
            TypedValue outValue = new TypedValue();
            context.getTheme().resolveAttribute(16843529, outValue, GlobalActions.SHOW_SILENT_TOGGLE);
            return outValue.resourceId;
        }

        protected void onStart() {
            if (EnableAccessibilityController.canEnableAccessibilityViaGesture(this.mContext)) {
                this.mEnableAccessibilityController = new EnableAccessibilityController(this.mContext, new AnonymousClass1(this));
                super.setCanceledOnTouchOutside(false);
            } else {
                this.mEnableAccessibilityController = null;
                super.setCanceledOnTouchOutside(GlobalActions.SHOW_SILENT_TOGGLE);
            }
            super.onStart();
        }

        protected void onStop() {
            if (this.mEnableAccessibilityController != null) {
                this.mEnableAccessibilityController.onDestroy();
            }
            super.onStop();
        }

        public boolean dispatchTouchEvent(MotionEvent event) {
            if (this.mEnableAccessibilityController != null) {
                int action = event.getActionMasked();
                if (action == 0) {
                    View decor = getWindow().getDecorView();
                    int eventX = (int) event.getX();
                    int eventY = (int) event.getY();
                    if (eventX >= (-this.mWindowTouchSlop) && eventY >= (-this.mWindowTouchSlop) && eventX < decor.getWidth() + this.mWindowTouchSlop) {
                        if (eventY >= decor.getHeight() + this.mWindowTouchSlop) {
                        }
                    }
                    this.mCancelOnUp = GlobalActions.SHOW_SILENT_TOGGLE;
                }
                try {
                    if (this.mIntercepted) {
                        boolean onTouchEvent = this.mEnableAccessibilityController.onTouchEvent(event);
                        if (action == GlobalActions.MESSAGE_REFRESH) {
                            if (this.mCancelOnUp) {
                                cancel();
                            }
                            this.mCancelOnUp = false;
                            this.mIntercepted = false;
                        }
                        return onTouchEvent;
                    }
                    this.mIntercepted = this.mEnableAccessibilityController.onInterceptTouchEvent(event);
                    if (this.mIntercepted) {
                        long now = SystemClock.uptimeMillis();
                        event = MotionEvent.obtain(now, now, 3, 0.0f, 0.0f, GlobalActions.MESSAGE_DISMISS);
                        event.setSource(4098);
                        this.mCancelOnUp = GlobalActions.SHOW_SILENT_TOGGLE;
                    }
                    if (action == GlobalActions.MESSAGE_REFRESH) {
                        if (this.mCancelOnUp) {
                            cancel();
                        }
                        this.mCancelOnUp = false;
                        this.mIntercepted = false;
                    }
                } catch (Throwable th) {
                    if (action == GlobalActions.MESSAGE_REFRESH) {
                        if (this.mCancelOnUp) {
                            cancel();
                        }
                        this.mCancelOnUp = false;
                        this.mIntercepted = false;
                    }
                }
            }
            return super.dispatchTouchEvent(event);
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
                for (int i = GlobalActions.MESSAGE_DISMISS; i < this.mAdapter.getCount(); i += GlobalActions.MESSAGE_REFRESH) {
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
                return GlobalActions.SHOW_SILENT_TOGGLE;
            }
            return super.onKeyDown(keyCode, event);
        }

        public boolean onKeyUp(int keyCode, KeyEvent event) {
            if (this.mAlert.onKeyUp(keyCode, event)) {
                return GlobalActions.SHOW_SILENT_TOGGLE;
            }
            return super.onKeyUp(keyCode, event);
        }
    }

    private class MyAdapter extends BaseAdapter {
        final /* synthetic */ GlobalActions this$0;

        /* synthetic */ MyAdapter(GlobalActions this$0, MyAdapter myAdapter) {
            this(this$0);
        }

        private MyAdapter(GlobalActions this$0) {
            this.this$0 = this$0;
        }

        public int getCount() {
            int count = GlobalActions.MESSAGE_DISMISS;
            for (int i = GlobalActions.MESSAGE_DISMISS; i < this.this$0.mItems.size(); i += GlobalActions.MESSAGE_REFRESH) {
                Action action = (Action) this.this$0.mItems.get(i);
                if ((!this.this$0.mKeyguardShowing || action.showDuringKeyguard()) && (this.this$0.mDeviceProvisioned || action.showBeforeProvisioning())) {
                    count += GlobalActions.MESSAGE_REFRESH;
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

        public /* bridge */ /* synthetic */ Object m0getItem(int position) {
            return getItem(position);
        }

        public Action getItem(int position) {
            int filteredPos = GlobalActions.MESSAGE_DISMISS;
            for (int i = GlobalActions.MESSAGE_DISMISS; i < this.this$0.mItems.size(); i += GlobalActions.MESSAGE_REFRESH) {
                Action action = (Action) this.this$0.mItems.get(i);
                if ((!this.this$0.mKeyguardShowing || action.showDuringKeyguard()) && (this.this$0.mDeviceProvisioned || action.showBeforeProvisioning())) {
                    if (filteredPos == position) {
                        return action;
                    }
                    filteredPos += GlobalActions.MESSAGE_REFRESH;
                }
            }
            throw new IllegalArgumentException("position " + position + " out of range of showable actions" + ", filtered count=" + getCount() + ", keyguardshowing=" + this.this$0.mKeyguardShowing + ", provisioned=" + this.this$0.mDeviceProvisioned);
        }

        public long getItemId(int position) {
            return (long) position;
        }

        public View getView(int position, View convertView, ViewGroup parent) {
            return getItem(position).create(this.this$0.mContext, convertView, parent, LayoutInflater.from(this.this$0.mContext));
        }
    }

    private final class PowerAction extends SinglePressAction implements LongPressAction {
        final /* synthetic */ GlobalActions this$0;

        /* synthetic */ PowerAction(GlobalActions this$0, PowerAction powerAction) {
            this(this$0);
        }

        private PowerAction(GlobalActions this$0) {
            this.this$0 = this$0;
            super(17301552, 17039658);
        }

        public boolean onLongPress() {
            if (((UserManager) this.this$0.mContext.getSystemService("user")).hasUserRestriction("no_safe_boot")) {
                return false;
            }
            this.this$0.mWindowManagerFuncs.rebootSafeMode(GlobalActions.SHOW_SILENT_TOGGLE);
            return GlobalActions.SHOW_SILENT_TOGGLE;
        }

        public boolean showDuringKeyguard() {
            return GlobalActions.SHOW_SILENT_TOGGLE;
        }

        public boolean showBeforeProvisioning() {
            return GlobalActions.SHOW_SILENT_TOGGLE;
        }

        public void onPress() {
            this.this$0.mWindowManagerFuncs.shutdown(GlobalActions.SHOW_SILENT_TOGGLE);
        }
    }

    private class SilentModeToggleAction extends ToggleAction {
        final /* synthetic */ GlobalActions this$0;

        public SilentModeToggleAction(GlobalActions this$0) {
            this.this$0 = this$0;
            super(17302261, 17302260, 17039667, 17039668, 17039669);
        }

        void onToggle(boolean on) {
            if (on) {
                this.this$0.mAudioManager.setRingerMode(GlobalActions.MESSAGE_DISMISS);
            } else {
                this.this$0.mAudioManager.setRingerMode(GlobalActions.MESSAGE_SHOW);
            }
        }

        public boolean showDuringKeyguard() {
            return GlobalActions.SHOW_SILENT_TOGGLE;
        }

        public boolean showBeforeProvisioning() {
            return false;
        }
    }

    private static class SilentModeTriStateAction implements Action, View.OnClickListener {
        private final int[] ITEM_IDS;
        private final AudioManager mAudioManager;
        private final Context mContext;
        private final Handler mHandler;

        SilentModeTriStateAction(Context context, AudioManager audioManager, Handler handler) {
            this.ITEM_IDS = new int[]{16909153, 16909154, 16909155};
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
            View v = inflater.inflate(17367143, parent, false);
            int selectedIndex = ringerModeToIndex(this.mAudioManager.getRingerMode());
            for (int i = GlobalActions.MESSAGE_DISMISS; i < 3; i += GlobalActions.MESSAGE_REFRESH) {
                boolean z;
                View itemView = v.findViewById(this.ITEM_IDS[i]);
                if (selectedIndex == i) {
                    z = GlobalActions.SHOW_SILENT_TOGGLE;
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
            return GlobalActions.SHOW_SILENT_TOGGLE;
        }

        public boolean showBeforeProvisioning() {
            return false;
        }

        public boolean isEnabled() {
            return GlobalActions.SHOW_SILENT_TOGGLE;
        }

        void willCreate() {
        }

        public void onClick(View v) {
            if (v.getTag() instanceof Integer) {
                this.mAudioManager.setRingerMode(indexToRingerMode(((Integer) v.getTag()).intValue()));
                this.mHandler.sendEmptyMessageDelayed(GlobalActions.MESSAGE_DISMISS, 300);
            }
        }
    }

    public GlobalActions(Context context, WindowManagerFuncs windowManagerFuncs) {
        boolean z = false;
        this.mKeyguardShowing = false;
        this.mDeviceProvisioned = false;
        this.mAirplaneState = State.Off;
        this.mIsWaitingForEcmExit = false;
        this.mBroadcastReceiver = new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                if ("android.intent.action.CLOSE_SYSTEM_DIALOGS".equals(action) || "android.intent.action.SCREEN_OFF".equals(action)) {
                    if (!PhoneWindowManager.SYSTEM_DIALOG_REASON_GLOBAL_ACTIONS.equals(intent.getStringExtra(PhoneWindowManager.SYSTEM_DIALOG_REASON_KEY))) {
                        GlobalActions.this.mHandler.sendEmptyMessage(GlobalActions.MESSAGE_DISMISS);
                    }
                } else if ("android.intent.action.EMERGENCY_CALLBACK_MODE_CHANGED".equals(action) && !intent.getBooleanExtra("PHONE_IN_ECM_STATE", false) && GlobalActions.this.mIsWaitingForEcmExit) {
                    GlobalActions.this.mIsWaitingForEcmExit = false;
                    GlobalActions.this.changeAirplaneModeSystemSetting(GlobalActions.SHOW_SILENT_TOGGLE);
                }
            }
        };
        this.mPhoneStateListener = new PhoneStateListener() {
            public void onServiceStateChanged(ServiceState serviceState) {
                if (GlobalActions.this.mHasTelephony) {
                    GlobalActions.this.mAirplaneState = serviceState.getState() == 3 ? GlobalActions.SHOW_SILENT_TOGGLE : false ? State.On : State.Off;
                    GlobalActions.this.mAirplaneModeOn.updateState(GlobalActions.this.mAirplaneState);
                    GlobalActions.this.mAdapter.notifyDataSetChanged();
                }
            }
        };
        this.mRingerModeReceiver = null;
        this.mAirplaneModeObserver = new AnonymousClass3(new Handler());
        this.mHandler = new Handler() {
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case GlobalActions.MESSAGE_DISMISS /*0*/:
                        if (GlobalActions.this.mDialog != null) {
                            GlobalActions.this.mDialog.dismiss();
                            GlobalActions.this.mDialog = null;
                        }
                    case GlobalActions.MESSAGE_REFRESH /*1*/:
                        GlobalActions.this.refreshSilentMode();
                        GlobalActions.this.mAdapter.notifyDataSetChanged();
                    case GlobalActions.MESSAGE_SHOW /*2*/:
                        GlobalActions.this.handleShow();
                    default:
                }
            }
        };
        this.mContext = context;
        this.mWindowManagerFuncs = windowManagerFuncs;
        this.mAudioManager = (AudioManager) this.mContext.getSystemService("audio");
        this.mDreamManager = Stub.asInterface(ServiceManager.getService("dreams"));
        IntentFilter filter = new IntentFilter();
        filter.addAction("android.intent.action.CLOSE_SYSTEM_DIALOGS");
        filter.addAction("android.intent.action.SCREEN_OFF");
        filter.addAction("android.intent.action.EMERGENCY_CALLBACK_MODE_CHANGED");
        context.registerReceiver(this.mBroadcastReceiver, filter);
        this.mHasTelephony = ((ConnectivityManager) context.getSystemService("connectivity")).isNetworkSupported(MESSAGE_DISMISS);
        ((TelephonyManager) context.getSystemService("phone")).listen(this.mPhoneStateListener, MESSAGE_REFRESH);
        this.mContext.getContentResolver().registerContentObserver(Global.getUriFor("airplane_mode_on"), SHOW_SILENT_TOGGLE, this.mAirplaneModeObserver);
        Vibrator vibrator = (Vibrator) this.mContext.getSystemService("vibrator");
        this.mHasVibrator = vibrator != null ? vibrator.hasVibrator() : false;
        if (!this.mContext.getResources().getBoolean(17956995)) {
            z = SHOW_SILENT_TOGGLE;
        }
        this.mShowSilentToggle = z;
    }

    public void showDialog(boolean keyguardShowing, boolean isDeviceProvisioned) {
        this.mKeyguardShowing = keyguardShowing;
        this.mDeviceProvisioned = isDeviceProvisioned;
        if (this.mDialog != null) {
            this.mDialog.dismiss();
            this.mDialog = null;
            this.mHandler.sendEmptyMessage(MESSAGE_SHOW);
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
        if (this.mAdapter.getCount() == MESSAGE_REFRESH && (this.mAdapter.getItem((int) MESSAGE_DISMISS) instanceof SinglePressAction) && !(this.mAdapter.getItem((int) MESSAGE_DISMISS) instanceof LongPressAction)) {
            ((SinglePressAction) this.mAdapter.getItem((int) MESSAGE_DISMISS)).onPress();
            return;
        }
        LayoutParams attrs = this.mDialog.getWindow().getAttributes();
        attrs.setTitle(TAG);
        this.mDialog.getWindow().setAttributes(attrs);
        this.mDialog.show();
        this.mDialog.getWindow().getDecorView().setSystemUiVisibility(DumpState.DUMP_INSTALLS);
    }

    private GlobalActionsDialog createDialog() {
        if (this.mHasVibrator) {
            this.mSilentModeAction = new SilentModeTriStateAction(this.mContext, this.mAudioManager, this.mHandler);
        } else {
            this.mSilentModeAction = new SilentModeToggleAction(this);
        }
        this.mAirplaneModeOn = new AnonymousClass5(this, 17302353, 17302355, 17039670, 17039671, 17039672);
        onAirplaneModeChanged();
        this.mItems = new ArrayList();
        String[] defaultActions = this.mContext.getResources().getStringArray(17236030);
        ArraySet<String> addedKeys = new ArraySet();
        for (int i = MESSAGE_DISMISS; i < defaultActions.length; i += MESSAGE_REFRESH) {
            String actionKey = defaultActions[i];
            if (!addedKeys.contains(actionKey)) {
                if (GLOBAL_ACTION_KEY_POWER.equals(actionKey)) {
                    this.mItems.add(new PowerAction());
                } else if (GLOBAL_ACTION_KEY_REBOOT.equals(actionKey)) {
                    HwPolicyFactory.addRebootMenu(this.mItems);
                } else if (GLOBAL_ACTION_KEY_AIRPLANE.equals(actionKey)) {
                    this.mItems.add(this.mAirplaneModeOn);
                } else if (GLOBAL_ACTION_KEY_BUGREPORT.equals(actionKey)) {
                    if (Global.getInt(this.mContext.getContentResolver(), "bugreport_in_power_menu", MESSAGE_DISMISS) != 0 && isCurrentUserOwner()) {
                        this.mItems.add(new BugReportAction(this));
                    }
                } else if (GLOBAL_ACTION_KEY_SILENT.equals(actionKey)) {
                    if (this.mShowSilentToggle) {
                        this.mItems.add(this.mSilentModeAction);
                    }
                } else if (GLOBAL_ACTION_KEY_USERS.equals(actionKey)) {
                    if (SystemProperties.getBoolean("fw.power_user_switcher", false)) {
                        addUsersToMenu(this.mItems);
                    }
                } else if (GLOBAL_ACTION_KEY_SETTINGS.equals(actionKey)) {
                    this.mItems.add(getSettingsAction());
                } else if (GLOBAL_ACTION_KEY_LOCKDOWN.equals(actionKey)) {
                    this.mItems.add(getLockdownAction());
                } else if (GLOBAL_ACTION_KEY_VOICEASSIST.equals(actionKey)) {
                    this.mItems.add(getVoiceAssistAction());
                } else if (GLOBAL_ACTION_KEY_ASSIST.equals(actionKey)) {
                    this.mItems.add(getAssistAction());
                } else {
                    Log.e(TAG, "Invalid global action key " + actionKey);
                }
                addedKeys.add(actionKey);
            }
        }
        this.mAdapter = new MyAdapter();
        AlertParams params = new AlertParams(this.mContext);
        params.mAdapter = this.mAdapter;
        params.mOnClickListener = this;
        params.mForceInverseBackground = SHOW_SILENT_TOGGLE;
        GlobalActionsDialog dialog = new GlobalActionsDialog(this.mContext, params);
        dialog.setCanceledOnTouchOutside(false);
        dialog.getListView().setItemsCanFocus(SHOW_SILENT_TOGGLE);
        dialog.getListView().setLongClickable(SHOW_SILENT_TOGGLE);
        dialog.getListView().setOnItemLongClickListener(new AnonymousClass6(this));
        dialog.getWindow().setType(2009);
        dialog.setOnDismissListener(this);
        return dialog;
    }

    private Action getSettingsAction() {
        return new AnonymousClass7(this, 17302552, 17039673);
    }

    private Action getAssistAction() {
        return new AnonymousClass8(this, 17302246, 17039674);
    }

    private Action getVoiceAssistAction() {
        return new AnonymousClass9(this, 17302573, 17039675);
    }

    private Action getLockdownAction() {
        return new AnonymousClass10(17301551, 17039676);
    }

    private UserInfo getCurrentUser() {
        try {
            return ActivityManagerNative.getDefault().getCurrentUser();
        } catch (RemoteException e) {
            return null;
        }
    }

    private boolean isCurrentUserOwner() {
        UserInfo currentUser = getCurrentUser();
        return currentUser != null ? currentUser.isPrimary() : SHOW_SILENT_TOGGLE;
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void addUsersToMenu(ArrayList<Action> items) {
        UserManager um = (UserManager) this.mContext.getSystemService("user");
        if (um.isUserSwitcherEnabled()) {
            List<UserInfo> users = um.getUsers();
            UserInfo currentUser = getCurrentUser();
            for (UserInfo user : users) {
                if (user.supportsSwitchToByUser()) {
                    boolean isCurrentUser;
                    Drawable createFromPath;
                    if (currentUser != null) {
                        if (currentUser.id == user.id) {
                        }
                        isCurrentUser = false;
                        if (user.iconPath == null) {
                            createFromPath = Drawable.createFromPath(user.iconPath);
                        } else {
                            createFromPath = null;
                        }
                        items.add(new AnonymousClass11(17302445, createFromPath, (user.name == null ? user.name : "Primary") + (isCurrentUser ? " \u2714" : ""), user));
                    }
                    isCurrentUser = SHOW_SILENT_TOGGLE;
                    if (user.iconPath == null) {
                        createFromPath = null;
                    } else {
                        createFromPath = Drawable.createFromPath(user.iconPath);
                    }
                    if (user.name == null) {
                    }
                    if (isCurrentUser) {
                    }
                    items.add(new AnonymousClass11(17302445, createFromPath, (user.name == null ? user.name : "Primary") + (isCurrentUser ? " \u2714" : ""), user));
                }
            }
        }
    }

    private void prepareDialog() {
        boolean airplaneModeOn = SHOW_SILENT_TOGGLE;
        refreshSilentMode();
        if (System.getInt(this.mContext.getContentResolver(), "airplane_mode_on", MESSAGE_DISMISS) != MESSAGE_REFRESH) {
            airplaneModeOn = false;
        }
        this.mAirplaneState = airplaneModeOn ? State.On : State.Off;
        this.mAirplaneModeOn.updateState(this.mAirplaneState);
        this.mAdapter.notifyDataSetChanged();
        this.mDialog.getWindow().setType(2009);
        if (this.mShowSilentToggle) {
            IntentFilter filter = new IntentFilter("android.media.RINGER_MODE_CHANGED");
            Context context = this.mContext;
            BroadcastReceiver anonymousClass12 = new BroadcastReceiver() {
                public void onReceive(Context context, Intent intent) {
                    String action = intent.getAction();
                    if (action != null && "android.media.RINGER_MODE_CHANGED".equals(action)) {
                        GlobalActions.this.mHandler.sendEmptyMessage(GlobalActions.MESSAGE_REFRESH);
                    }
                }
            };
            this.mRingerModeReceiver = anonymousClass12;
            context.registerReceiver(anonymousClass12, filter);
        }
    }

    private void refreshSilentMode() {
        if (!this.mHasVibrator) {
            ((ToggleAction) this.mSilentModeAction).updateState(this.mAudioManager.getRingerMode() != MESSAGE_SHOW ? SHOW_SILENT_TOGGLE : false ? State.On : State.Off);
        }
    }

    public void onDismiss(DialogInterface dialog) {
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
        boolean airplaneModeOn = SHOW_SILENT_TOGGLE;
        if (!this.mHasTelephony) {
            if (Global.getInt(this.mContext.getContentResolver(), "airplane_mode_on", MESSAGE_DISMISS) != MESSAGE_REFRESH) {
                airplaneModeOn = false;
            }
            this.mAirplaneState = airplaneModeOn ? State.On : State.Off;
            this.mAirplaneModeOn.updateState(this.mAirplaneState);
        }
    }

    private void changeAirplaneModeSystemSetting(boolean on) {
        Global.putInt(this.mContext.getContentResolver(), "airplane_mode_on", on ? MESSAGE_REFRESH : MESSAGE_DISMISS);
        Intent intent = new Intent("android.intent.action.AIRPLANE_MODE");
        intent.addFlags(536870912);
        intent.putExtra(AudioService.CONNECT_INTENT_KEY_STATE, on);
        this.mContext.sendBroadcastAsUser(intent, UserHandle.ALL);
        if (!this.mHasTelephony) {
            State state;
            if (on) {
                state = State.On;
            } else {
                state = State.Off;
            }
            this.mAirplaneState = state;
        }
    }
}
