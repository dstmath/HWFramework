package android.content;

import android.accounts.Account;
import android.app.Activity;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;
import com.android.internal.R;

public class SyncActivityTooManyDeletes extends Activity implements AdapterView.OnItemClickListener {
    private Account mAccount;
    private String mAuthority;
    private long mNumDeletes;
    private String mProvider;

    /* access modifiers changed from: protected */
    @Override // android.app.Activity
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle extras = getIntent().getExtras();
        if (extras == null) {
            finish();
            return;
        }
        this.mNumDeletes = extras.getLong("numDeletes");
        this.mAccount = (Account) extras.getParcelable("account");
        this.mAuthority = extras.getString(ContactsContract.Directory.DIRECTORY_AUTHORITY);
        this.mProvider = extras.getString("provider");
        ListAdapter adapter = new ArrayAdapter(this, 17367043, 16908308, new CharSequence[]{getResources().getText(R.string.sync_really_delete), getResources().getText(R.string.sync_undo_deletes), getResources().getText(R.string.sync_do_nothing)});
        ListView listView = new ListView(this);
        listView.setAdapter(adapter);
        listView.setItemsCanFocus(true);
        listView.setOnItemClickListener(this);
        TextView textView = new TextView(this);
        CharSequence tooManyDeletesDescFormat = getResources().getText(R.string.sync_too_many_deletes_desc);
        if (this.mAccount != null) {
            textView.setText(String.format(tooManyDeletesDescFormat.toString(), Long.valueOf(this.mNumDeletes), this.mProvider, this.mAccount.name));
        } else {
            textView.setText(String.format(tooManyDeletesDescFormat.toString(), Long.valueOf(this.mNumDeletes), this.mProvider, ""));
        }
        LinearLayout ll = new LinearLayout(this);
        ll.setOrientation(1);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(-1, -2, 0.0f);
        ll.addView(textView, lp);
        ll.addView(listView, lp);
        setContentView(ll);
    }

    @Override // android.widget.AdapterView.OnItemClickListener
    public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
        if (position == 0) {
            startSyncReallyDelete();
        } else if (position == 1) {
            startSyncUndoDeletes();
        }
        finish();
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
