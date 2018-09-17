package com.android.internal.app;

import android.app.Activity;
import android.app.AlertDialog.Builder;
import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.DialogInterface.OnDismissListener;
import android.content.Intent;
import android.content.IntentSender;
import android.content.IntentSender.SendIntentException;
import android.os.Bundle;
import android.os.UserManager;
import android.service.notification.ZenModeConfig;
import android.util.Log;
import com.android.internal.R;

public class UnlaunchableAppActivity extends Activity implements OnDismissListener, OnClickListener {
    private static final String EXTRA_UNLAUNCHABLE_REASON = "unlaunchable_reason";
    private static final String TAG = "UnlaunchableAppActivity";
    private static final int UNLAUNCHABLE_REASON_QUIET_MODE = 1;
    private int mReason;
    private IntentSender mTarget;
    private int mUserId;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(1);
        Intent intent = getIntent();
        this.mReason = intent.getIntExtra(EXTRA_UNLAUNCHABLE_REASON, -1);
        this.mUserId = intent.getIntExtra("android.intent.extra.user_handle", -10000);
        this.mTarget = (IntentSender) intent.getParcelableExtra("android.intent.extra.INTENT");
        if (this.mUserId == -10000) {
            Log.wtf(TAG, "Invalid user id: " + this.mUserId + ". Stopping.");
            finish();
        } else if (this.mReason == 1) {
            String dialogTitle = getResources().getString(R.string.work_mode_off_title);
            String dialogMessage = getResources().getString(R.string.work_mode_off_message);
            getWindow().setBackgroundDrawableResource(R.color.transparent);
            Builder builder = new Builder(this).setTitle(dialogTitle).setMessage(dialogMessage).setOnDismissListener(this);
            if (this.mReason == 1) {
                builder.setPositiveButton(R.string.work_mode_turn_on, this).setNegativeButton(R.string.cancel, null);
            } else {
                builder.setPositiveButton(R.string.ok, null);
            }
            builder.create();
            builder.show();
        } else {
            Log.wtf(TAG, "Invalid unlaunchable type: " + this.mReason);
            finish();
        }
    }

    public void onDismiss(DialogInterface dialog) {
        finish();
    }

    public void onClick(DialogInterface dialog, int which) {
        if (this.mReason == 1 && which == -1 && UserManager.get(this).trySetQuietModeDisabled(this.mUserId, this.mTarget) && this.mTarget != null) {
            try {
                startIntentSenderForResult(this.mTarget, -1, null, 0, 0, 0);
            } catch (SendIntentException e) {
            }
        }
    }

    private static final Intent createBaseIntent() {
        Intent intent = new Intent();
        intent.setComponent(new ComponentName(ZenModeConfig.SYSTEM_AUTHORITY, UnlaunchableAppActivity.class.getName()));
        intent.setFlags(276824064);
        return intent;
    }

    public static Intent createInQuietModeDialogIntent(int userId) {
        Intent intent = createBaseIntent();
        intent.putExtra(EXTRA_UNLAUNCHABLE_REASON, 1);
        intent.putExtra("android.intent.extra.user_handle", userId);
        return intent;
    }

    public static Intent createInQuietModeDialogIntent(int userId, IntentSender target) {
        Intent intent = createInQuietModeDialogIntent(userId);
        intent.putExtra("android.intent.extra.INTENT", target);
        return intent;
    }
}
