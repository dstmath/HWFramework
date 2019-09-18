package com.android.server.am;

import android.app.AlertDialog;
import android.content.Context;
import android.content.pm.UserInfo;
import android.content.res.Resources;
import android.os.Handler;
import android.os.Message;
import android.os.UserManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.WindowManager;
import android.widget.TextView;
import com.android.internal.annotations.GuardedBy;

class UserSwitchingDialog extends AlertDialog implements ViewTreeObserver.OnWindowShownListener {
    private static final int MSG_START_USER = 1;
    private static final String TAG = "ActivityManagerUserSwitchingDialog";
    private static final int WINDOW_SHOWN_TIMEOUT_MS = 3000;
    protected final Context mContext;
    private final Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            if (msg.what == 1) {
                UserSwitchingDialog.this.startUser();
            }
        }
    };
    protected final UserInfo mNewUser;
    protected final UserInfo mOldUser;
    private final ActivityManagerService mService;
    @GuardedBy("this")
    private boolean mStartedUser;
    private final String mSwitchingFromSystemUserMessage;
    private final String mSwitchingToSystemUserMessage;
    private final int mUserId;

    public UserSwitchingDialog(ActivityManagerService service, Context context, UserInfo oldUser, UserInfo newUser, boolean aboveSystem, String switchingFromSystemUserMessage, String switchingToSystemUserMessage) {
        super(context, context.getResources().getIdentifier("androidhwext:style/Theme.Emui.Dialog.Alert", null, null));
        this.mContext = context;
        this.mService = service;
        this.mUserId = newUser.id;
        if (newUser.isGuest()) {
            newUser.name = getContext().getString(33685844);
        }
        this.mOldUser = oldUser;
        this.mNewUser = newUser;
        this.mSwitchingFromSystemUserMessage = switchingFromSystemUserMessage;
        this.mSwitchingToSystemUserMessage = switchingToSystemUserMessage;
        inflateContent();
        if (aboveSystem) {
            getWindow().setType(2010);
        }
        WindowManager.LayoutParams attrs = getWindow().getAttributes();
        attrs.privateFlags = 272;
        getWindow().setAttributes(attrs);
    }

    /* access modifiers changed from: package-private */
    public void inflateContent() {
        String viewMessage;
        setCancelable(false);
        Resources res = getContext().getResources();
        View view = LayoutInflater.from(getContext()).inflate(17367327, null);
        String viewMessage2 = null;
        if (UserManager.isSplitSystemUser() && this.mNewUser.id == 0) {
            viewMessage = res.getString(17041312, new Object[]{this.mOldUser.name});
        } else if (!UserManager.isDeviceInDemoMode(this.mContext)) {
            if (this.mOldUser.id == 0) {
                viewMessage2 = this.mSwitchingFromSystemUserMessage;
            } else if (this.mNewUser.id == 0) {
                viewMessage2 = this.mSwitchingToSystemUserMessage;
            }
            if (viewMessage2 == null) {
                viewMessage = res.getString(17041315, new Object[]{this.mNewUser.name});
            } else {
                viewMessage = viewMessage2;
            }
        } else if (this.mOldUser.isDemo()) {
            viewMessage = res.getString(17039928);
        } else {
            viewMessage = res.getString(17039929);
        }
        ((TextView) view.findViewById(16908299)).setText(viewMessage);
        setView(view);
    }

    public void show() {
        super.show();
        View decorView = getWindow().getDecorView();
        if (decorView != null) {
            decorView.getViewTreeObserver().addOnWindowShownListener(this);
        }
        this.mHandler.sendMessageDelayed(this.mHandler.obtainMessage(1), 3000);
    }

    public void onWindowShown() {
        startUser();
    }

    /* access modifiers changed from: package-private */
    public void startUser() {
        synchronized (this) {
            if (!this.mStartedUser) {
                this.mService.mUserController.startUserInForeground(this.mUserId);
                dismiss();
                this.mStartedUser = true;
                View decorView = getWindow().getDecorView();
                if (decorView != null) {
                    decorView.getViewTreeObserver().removeOnWindowShownListener(this);
                }
                this.mHandler.removeMessages(1);
            }
        }
    }
}
