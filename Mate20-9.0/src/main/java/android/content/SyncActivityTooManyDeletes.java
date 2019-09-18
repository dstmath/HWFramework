package android.content;

import android.accounts.Account;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.android.internal.R;

public class SyncActivityTooManyDeletes extends Activity implements View.OnClickListener {
    final DialogInterface.OnDismissListener dismissListener = new DialogInterface.OnDismissListener() {
        public void onDismiss(DialogInterface dialog) {
            SyncActivityTooManyDeletes.this.finish();
        }
    };
    private Account mAccount;
    private String mAuthority;
    private long mNumDeletes;
    private String mProvider;

    /* access modifiers changed from: protected */
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle extras = getIntent().getExtras();
        if (extras == null) {
            finish();
            return;
        }
        this.mNumDeletes = extras.getLong("numDeletes");
        this.mAccount = (Account) extras.getParcelable("account");
        this.mAuthority = extras.getString("authority");
        this.mProvider = extras.getString("provider");
        CharSequence[] options = {getResources().getText(R.string.sync_really_delete), getResources().getText(R.string.sync_undo_deletes), getResources().getText(R.string.sync_do_nothing)};
        TextView textView = new TextView(this);
        CharSequence tooManyDeletesDescFormat = getResources().getText(R.string.sync_too_many_deletes_desc);
        if (this.mAccount != null) {
            textView.setText(String.format(tooManyDeletesDescFormat.toString(), new Object[]{Long.valueOf(this.mNumDeletes), this.mProvider, this.mAccount.name}));
        } else {
            textView.setText(String.format(tooManyDeletesDescFormat.toString(), new Object[]{Long.valueOf(this.mNumDeletes), this.mProvider, ""}));
        }
        LinearLayout ll = new LinearLayout(this);
        ll.setOrientation(1);
        LinearLayout.LayoutParams textLp = new LinearLayout.LayoutParams(-1, -2);
        textLp.setMargins(dip2px(this, 16.0f), dip2px(this, 18.0f), dip2px(this, 16.0f), 0);
        ll.addView(textView, textLp);
        LinearLayout.LayoutParams layoutParams = textLp;
        LinearLayout ll2 = ll;
        addBtnToRootView(ll, dip2px(this, 16.0f), dip2px(this, 16.0f), dip2px(this, 16.0f), 0, options[0], 0);
        LinearLayout linearLayout = ll2;
        addBtnToRootView(linearLayout, dip2px(this, 16.0f), dip2px(this, 16.0f), dip2px(this, 16.0f), 0, options[1], 1);
        addBtnToRootView(linearLayout, dip2px(this, 16.0f), dip2px(this, 16.0f), dip2px(this, 16.0f), dip2px(this, 16.0f), options[2], 2);
        getWindow().setBackgroundDrawableResource(17170445);
        AlertDialog dialog = new AlertDialog.Builder(this, 33947691).setTitle((CharSequence) getTitle().toString()).setView((View) ll2).create();
        dialog.setCanceledOnTouchOutside(false);
        dialog.setOnDismissListener(this.dismissListener);
        dialog.show();
    }

    private void addBtnToRootView(LinearLayout root, int l, int u, int r, int b, CharSequence text, int id) {
        Button btn = new Button(this);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(-1, -2);
        lp.setMargins(l, u, r, b);
        btn.setLayoutParams(lp);
        btn.setText(text);
        btn.setId(id);
        btn.setOnClickListener(this);
        root.addView(btn);
    }

    public void onClick(View v) {
        switch (v.getId()) {
            case 0:
                startSyncReallyDelete();
                break;
            case 1:
                startSyncUndoDeletes();
                break;
        }
        finish();
    }

    private int dip2px(Context context, float dpValue) {
        return (int) ((dpValue * context.getResources().getDisplayMetrics().density) + 0.5f);
    }

    private void startSyncReallyDelete() {
        Bundle extras = new Bundle();
        extras.putBoolean(ContentResolver.SYNC_EXTRAS_OVERRIDE_TOO_MANY_DELETIONS, true);
        extras.putBoolean("force", true);
        extras.putBoolean(ContentResolver.SYNC_EXTRAS_EXPEDITED, true);
        extras.putBoolean(ContentResolver.SYNC_EXTRAS_UPLOAD, true);
        ContentResolver.requestSync(this.mAccount, this.mAuthority, extras);
    }

    private void startSyncUndoDeletes() {
        Bundle extras = new Bundle();
        extras.putBoolean(ContentResolver.SYNC_EXTRAS_DISCARD_LOCAL_DELETIONS, true);
        extras.putBoolean("force", true);
        extras.putBoolean(ContentResolver.SYNC_EXTRAS_EXPEDITED, true);
        extras.putBoolean(ContentResolver.SYNC_EXTRAS_UPLOAD, true);
        ContentResolver.requestSync(this.mAccount, this.mAuthority, extras);
    }
}
