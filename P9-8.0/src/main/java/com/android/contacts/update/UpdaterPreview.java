package com.android.contacts.update;

import android.app.Activity;
import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.os.Bundle;
import android.text.SpannableStringBuilder;
import android.text.style.RelativeSizeSpan;
import com.android.contacts.external.separated.ISeparatedResourceUtils;
import com.android.contacts.external.separated.SeparatedResourceUtils;
import com.android.contacts.hap.CommonUtilMethods;
import com.android.contacts.hap.EmuiFeatureManager;
import com.android.contacts.hap.numbermark.NumberMarkManager;
import com.android.contacts.hap.util.MultiUsersUtils;

public class UpdaterPreview extends Activity {
    private CancelListener mCancelListener = new CancelListener(this, null);
    private ISeparatedResourceUtils mISeparatedResourceUtils;
    private PositiveListener mPositiveListener = new PositiveListener(this, null);
    private Updater updater;

    private class CancelListener implements OnClickListener, OnCancelListener {
        private CancelListener() {
        }

        /* synthetic */ CancelListener(UpdaterPreview x0, AnonymousClass1 x1) {
            this();
        }

        public void onClick(DialogInterface dialog, int which) {
            UpdaterPreview.this.finish();
        }

        public void onCancel(DialogInterface dialog) {
            UpdaterPreview.this.finish();
        }
    }

    private class PositiveListener implements OnClickListener {
        private PositiveListener() {
        }

        /* synthetic */ PositiveListener(UpdaterPreview x0, AnonymousClass1 x1) {
            this();
        }

        public void onClick(DialogInterface dialog, int which) {
            UpdaterPreview.this.updateYPItem(1);
            NumberMarkManager.enableCloudMark(true, UpdaterPreview.this);
            UpdaterPreview.this.finish();
        }
    }

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent intent = getIntent();
        this.mISeparatedResourceUtils = new SeparatedResourceUtils();
        int fileId = intent.getIntExtra("fileId", 1);
        if (intent.getBooleanExtra("com.huawei.contacts.AUTO.UPDATE", false)) {
            updateFileItem(fileId, intent.getIntExtra("fileIiem", 2));
            finish();
            return;
        }
        this.updater = (Updater) UpdateHelper.getUpdaterInstance(fileId, this);
        showDialog(1);
    }

    private void updateFileItem(int fileId, int item) {
        switch (fileId) {
            case DownloadService.MSG_PROCESS /*1*/:
                updateNLItem(item);
                return;
            case DownloadService.MSG_OK /*3*/:
                updateCCItem(item);
                return;
            case DownloadService.MSG_CANCEL /*4*/:
                updateYPItem(item);
                return;
            default:
                updateNLItem(item);
                updateCCItem(item);
                updateYPItem(item);
                return;
        }
    }

    private void updateCCItem(int item) {
        if (EmuiFeatureManager.isCamcardEnabled()) {
            this.updater = (Updater) UpdateHelper.getUpdaterInstance(3, this);
            this.updater.setItem(item);
        }
    }

    private void updateYPItem(int item) {
        if (!EmuiFeatureManager.isSuperSaverMode() && !CommonUtilMethods.isSimplifiedModeEnabled()) {
            this.updater = (Updater) UpdateHelper.getUpdaterInstance(4, this);
            this.updater.setItem(item);
        }
    }

    private void updateNLItem(int item) {
        if (!EmuiFeatureManager.isSuperSaverMode() && !CommonUtilMethods.isSimplifiedModeEnabled() && MultiUsersUtils.isCurrentUserOwner() && EmuiFeatureManager.isSystemVoiceCapable()) {
            this.updater = (Updater) UpdateHelper.getUpdaterInstance(1, this);
            this.updater.setItem(item);
        }
    }

    protected Dialog onCreateDialog(int id, Bundle args) {
        if (this.updater == null) {
            return null;
        }
        Builder builder = new Builder(this).setTitle(this.updater.getTitle()).setMessage(this.updater.getMessage()).setOnCancelListener(this.mCancelListener).setNegativeButton(this.updater.getNegativeString(), this.mCancelListener).setPositiveButton(this.updater.getPositiveString(), this.mPositiveListener);
        final int item = this.updater.getItem();
        CharSequence[] items = new CharSequence[3];
        SpannableStringBuilder stringBuilder = new SpannableStringBuilder();
        ISeparatedResourceUtils iSeparatedResourceUtils = this.mISeparatedResourceUtils;
        Context applicationContext = getApplicationContext();
        ISeparatedResourceUtils iSeparatedResourceUtils2 = this.mISeparatedResourceUtils;
        stringBuilder.append(iSeparatedResourceUtils.getString(applicationContext, 11));
        stringBuilder.append(10);
        int indexToBeSpanned = stringBuilder.length();
        iSeparatedResourceUtils = this.mISeparatedResourceUtils;
        applicationContext = getApplicationContext();
        iSeparatedResourceUtils2 = this.mISeparatedResourceUtils;
        stringBuilder.append(iSeparatedResourceUtils.getString(applicationContext, 12));
        stringBuilder.setSpan(new RelativeSizeSpan(0.8f), indexToBeSpanned, stringBuilder.length(), 33);
        items[0] = stringBuilder;
        ISeparatedResourceUtils iSeparatedResourceUtils3 = this.mISeparatedResourceUtils;
        Context applicationContext2 = getApplicationContext();
        ISeparatedResourceUtils iSeparatedResourceUtils4 = this.mISeparatedResourceUtils;
        items[1] = iSeparatedResourceUtils3.getString(applicationContext2, 13);
        iSeparatedResourceUtils3 = this.mISeparatedResourceUtils;
        applicationContext2 = getApplicationContext();
        iSeparatedResourceUtils4 = this.mISeparatedResourceUtils;
        items[2] = iSeparatedResourceUtils3.getString(applicationContext2, 14);
        builder.setSingleChoiceItems(items, item, new OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                if (item != which) {
                    UpdaterPreview.this.updater.setItem(which, true);
                }
                UpdaterPreview.this.finish();
            }
        });
        return builder.create();
    }

    public void finish() {
        removeDialog(1);
        super.finish();
        overridePendingTransition(0, 0);
    }
}
