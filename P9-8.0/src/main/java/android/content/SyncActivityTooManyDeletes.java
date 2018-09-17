package android.content;

import android.R;
import android.accounts.Account;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.DialogInterface.OnDismissListener;
import android.net.ProxyInfo;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.TextView;

public class SyncActivityTooManyDeletes extends Activity implements OnClickListener {
    final OnDismissListener dismissListener = new OnDismissListener() {
        public void onDismiss(DialogInterface dialog) {
            SyncActivityTooManyDeletes.this.finish();
        }
    };
    private Account mAccount;
    private String mAuthority;
    private long mNumDeletes;
    private String mProvider;

    protected void onCreate(Bundle savedInstanceState) {
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
        CharSequence[] options = new CharSequence[]{getResources().getText(17041096), getResources().getText(17041099), getResources().getText(17041095)};
        TextView textView = new TextView(this);
        CharSequence tooManyDeletesDescFormat = getResources().getText(17041098);
        if (this.mAccount != null) {
            textView.setText(String.format(tooManyDeletesDescFormat.toString(), new Object[]{Long.valueOf(this.mNumDeletes), this.mProvider, this.mAccount.name}));
        } else {
            textView.setText(String.format(tooManyDeletesDescFormat.toString(), new Object[]{Long.valueOf(this.mNumDeletes), this.mProvider, ProxyInfo.LOCAL_EXCL_LIST}));
        }
        View ll = new LinearLayout(this);
        ll.setOrientation(1);
        LayoutParams textLp = new LayoutParams(-1, -2);
        textLp.setMargins(dip2px(this, 16.0f), dip2px(this, 18.0f), dip2px(this, 16.0f), 0);
        ll.addView(textView, textLp);
        addBtnToRootView(ll, dip2px(this, 16.0f), dip2px(this, 16.0f), dip2px(this, 16.0f), 0, options[0], 0);
        addBtnToRootView(ll, dip2px(this, 16.0f), dip2px(this, 16.0f), dip2px(this, 16.0f), 0, options[1], 1);
        addBtnToRootView(ll, dip2px(this, 16.0f), dip2px(this, 16.0f), dip2px(this, 16.0f), dip2px(this, 16.0f), options[2], 2);
        getWindow().setBackgroundDrawableResource(R.color.transparent);
        AlertDialog dialog = new Builder(this, 33947691).setTitle(getTitle().toString()).setView(ll).create();
        dialog.setCanceledOnTouchOutside(false);
        dialog.setOnDismissListener(this.dismissListener);
        dialog.show();
    }

    private void addBtnToRootView(LinearLayout root, int l, int u, int r, int b, CharSequence text, int id) {
        Button btn = new Button(this);
        LayoutParams lp = new LayoutParams(-1, -2);
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
