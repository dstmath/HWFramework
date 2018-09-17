package com.android.contacts.update;

import android.app.Activity;
import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.DialogInterface.OnClickListener;
import android.os.Bundle;
import android.text.SpannableStringBuilder;
import android.text.style.RelativeSizeSpan;

public class UpdaterPreview extends Activity {
    private CancelListener mCancelListener;
    private Updater updater;

    /* renamed from: com.android.contacts.update.UpdaterPreview.1 */
    class AnonymousClass1 implements OnClickListener {
        final /* synthetic */ int val$item;

        AnonymousClass1(int val$item) {
            this.val$item = val$item;
        }

        public void onClick(DialogInterface dialog, int which) {
            if (this.val$item != which) {
                UpdaterPreview.this.updater.setItem(which, true);
            }
            UpdaterPreview.this.finish();
        }
    }

    private class CancelListener implements OnClickListener, OnCancelListener {
        private CancelListener() {
        }

        public void onClick(DialogInterface dialog, int which) {
            UpdaterPreview.this.finish();
        }

        public void onCancel(DialogInterface dialog) {
            UpdaterPreview.this.finish();
        }
    }

    public UpdaterPreview() {
        this.mCancelListener = new CancelListener();
    }

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.updater = (Updater) UpdateHelper.getUpdaterInstance(getIntent().getIntExtra("fileId", 1), this);
        showDialog(1);
    }

    protected Dialog onCreateDialog(int id, Bundle args) {
        if (this.updater == null) {
            return null;
        }
        Builder builder = new Builder(this).setTitle(this.updater.getTitle()).setOnCancelListener(this.mCancelListener).setNegativeButton(17039360, this.mCancelListener);
        int item = this.updater.getItem();
        CharSequence[] items = new CharSequence[3];
        SpannableStringBuilder stringBuilder = new SpannableStringBuilder();
        stringBuilder.append(getString(2131428752));
        stringBuilder.append('\n');
        int indexToBeSpanned = stringBuilder.length();
        stringBuilder.append(getString(2131428753));
        stringBuilder.setSpan(new RelativeSizeSpan(0.8f), indexToBeSpanned, stringBuilder.length(), 33);
        items[0] = stringBuilder;
        items[1] = getString(2131428754);
        items[2] = getString(2131429134);
        builder.setSingleChoiceItems(items, item, new AnonymousClass1(item));
        return builder.create();
    }

    public void finish() {
        removeDialog(1);
        super.finish();
        overridePendingTransition(0, 0);
    }
}
