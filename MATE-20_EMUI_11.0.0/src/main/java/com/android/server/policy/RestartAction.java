package com.android.server.policy;

import android.content.Context;
import android.os.UserManager;
import com.android.internal.globalactions.LongPressAction;
import com.android.internal.globalactions.SinglePressAction;
import com.android.server.policy.WindowManagerPolicy;

public final class RestartAction extends SinglePressAction implements LongPressAction {
    private final Context mContext;
    private final WindowManagerPolicy.WindowManagerFuncs mWindowManagerFuncs;

    public RestartAction(Context context, WindowManagerPolicy.WindowManagerFuncs windowManagerFuncs) {
        super(17302799, 17040209);
        this.mContext = context;
        this.mWindowManagerFuncs = windowManagerFuncs;
    }

    public boolean onLongPress() {
        if (((UserManager) this.mContext.getSystemService(UserManager.class)).hasUserRestriction("no_safe_boot")) {
            return false;
        }
        this.mWindowManagerFuncs.rebootSafeMode(true);
        return true;
    }

    public boolean showDuringKeyguard() {
        return true;
    }

    public boolean showBeforeProvisioning() {
        return true;
    }

    public void onPress() {
        this.mWindowManagerFuncs.reboot(false);
    }
}
