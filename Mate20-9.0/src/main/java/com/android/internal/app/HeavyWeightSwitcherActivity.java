package com.android.internal.app;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.IntentSender;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.RemoteException;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

public class HeavyWeightSwitcherActivity extends Activity {
    public static final String KEY_CUR_APP = "cur_app";
    public static final String KEY_CUR_TASK = "cur_task";
    public static final String KEY_HAS_RESULT = "has_result";
    public static final String KEY_INTENT = "intent";
    public static final String KEY_NEW_APP = "new_app";
    String mCurApp;
    int mCurTask;
    boolean mHasResult;
    String mNewApp;
    IntentSender mStartIntent;
    private View.OnClickListener mSwitchNewListener = new View.OnClickListener() {
        public void onClick(View v) {
            try {
                ActivityManager.getService().finishHeavyWeightApp();
            } catch (RemoteException e) {
            }
            try {
                if (HeavyWeightSwitcherActivity.this.mHasResult) {
                    HeavyWeightSwitcherActivity.this.startIntentSenderForResult(HeavyWeightSwitcherActivity.this.mStartIntent, -1, null, 33554432, 33554432, 0);
                } else {
                    HeavyWeightSwitcherActivity.this.startIntentSenderForResult(HeavyWeightSwitcherActivity.this.mStartIntent, -1, null, 0, 0, 0);
                }
            } catch (IntentSender.SendIntentException ex) {
                Log.w("HeavyWeightSwitcherActivity", "Failure starting", ex);
            }
            HeavyWeightSwitcherActivity.this.finish();
        }
    };
    private View.OnClickListener mSwitchOldListener = new View.OnClickListener() {
        public void onClick(View v) {
            try {
                ActivityManager.getService().moveTaskToFront(HeavyWeightSwitcherActivity.this.mCurTask, 0, null);
            } catch (RemoteException e) {
            }
            HeavyWeightSwitcherActivity.this.finish();
        }
    };

    /* access modifiers changed from: protected */
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(1);
        this.mStartIntent = (IntentSender) getIntent().getParcelableExtra(KEY_INTENT);
        this.mHasResult = getIntent().getBooleanExtra(KEY_HAS_RESULT, false);
        this.mCurApp = getIntent().getStringExtra(KEY_CUR_APP);
        this.mCurTask = getIntent().getIntExtra(KEY_CUR_TASK, 0);
        this.mNewApp = getIntent().getStringExtra(KEY_NEW_APP);
        setContentView(17367153);
        setIconAndText(16909159, 16909157, 0, this.mCurApp, this.mNewApp, 17040616, 0);
        setIconAndText(16909119, 16909117, 16909118, this.mNewApp, this.mCurApp, 17040554, 17040555);
        findViewById(16909392).setOnClickListener(this.mSwitchOldListener);
        findViewById(16909391).setOnClickListener(this.mSwitchNewListener);
    }

    /* access modifiers changed from: package-private */
    public void setText(int id, CharSequence text) {
        ((TextView) findViewById(id)).setText(text);
    }

    /* access modifiers changed from: package-private */
    public void setDrawable(int id, Drawable dr) {
        if (dr != null) {
            ((ImageView) findViewById(id)).setImageDrawable(dr);
        }
    }

    /* access modifiers changed from: package-private */
    public void setIconAndText(int iconId, int actionId, int descriptionId, String packageName, String otherPackageName, int actionStr, int descriptionStr) {
        CharSequence appName = packageName;
        Drawable appIcon = null;
        if (packageName != null) {
            try {
                ApplicationInfo info = getPackageManager().getApplicationInfo(packageName, 0);
                appName = info.loadLabel(getPackageManager());
                appIcon = info.loadIcon(getPackageManager());
            } catch (PackageManager.NameNotFoundException e) {
            }
        }
        setDrawable(iconId, appIcon);
        setText(actionId, getString(actionStr, new Object[]{appName}));
        if (descriptionId != 0) {
            CharSequence otherAppName = otherPackageName;
            if (otherPackageName != null) {
                try {
                    otherAppName = getPackageManager().getApplicationInfo(otherPackageName, 0).loadLabel(getPackageManager());
                } catch (PackageManager.NameNotFoundException e2) {
                }
            }
            setText(descriptionId, getString(descriptionStr, new Object[]{otherAppName}));
        }
    }
}
