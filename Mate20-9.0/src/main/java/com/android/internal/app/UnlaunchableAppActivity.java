package com.android.internal.app;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.os.Bundle;
import android.os.UserHandle;
import android.os.UserManager;
import android.util.Log;

public class UnlaunchableAppActivity extends Activity implements DialogInterface.OnDismissListener, DialogInterface.OnClickListener {
    private static final String EXTRA_UNLAUNCHABLE_REASON = "unlaunchable_reason";
    private static final String TAG = "UnlaunchableAppActivity";
    private static final int UNLAUNCHABLE_REASON_QUIET_MODE = 1;
    private int mReason;
    private IntentSender mTarget;
    private int mUserId;

    /* access modifiers changed from: protected */
    public void onCreate(Bundle savedInstanceState) {
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
            String dialogTitle = getResources().getString(17041424);
            String dialogMessage = getResources().getString(17041423);
            getWindow().setBackgroundDrawableResource(17170445);
            AlertDialog.Builder builder = new AlertDialog.Builder(this).setTitle(dialogTitle).setMessage(dialogMessage).setOnDismissListener(this);
            if (this.mReason == 1) {
                builder.setPositiveButton(17041425, this).setNegativeButton(17039360, null);
            } else {
                builder.setPositiveButton(17039370, null);
            }
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
        if (this.mReason == 1 && which == -1) {
            UserManager.get(this).requestQuietModeEnabled(false, UserHandle.of(this.mUserId), this.mTarget);
        }
    }

    private static final Intent createBaseIntent() {
        Intent intent = new Intent();
        intent.setComponent(new ComponentName("android", UnlaunchableAppActivity.class.getName()));
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
