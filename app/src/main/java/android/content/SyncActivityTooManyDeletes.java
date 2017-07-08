package android.content;

import android.R;
import android.accounts.Account;
import android.app.Activity;
import android.net.ProxyInfo;
import android.os.Bundle;
import android.provider.ContactsContract.Directory;
import android.rms.iaware.DataContract.Apps.LaunchMode;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;

public class SyncActivityTooManyDeletes extends Activity implements OnItemClickListener {
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
        this.mAccount = (Account) extras.getParcelable(ContentResolver.SYNC_EXTRAS_ACCOUNT);
        this.mAuthority = extras.getString(Directory.DIRECTORY_AUTHORITY);
        this.mProvider = extras.getString(LaunchMode.PROVIDER);
        ListAdapter adapter = new ArrayAdapter(this, R.layout.simple_list_item_1, R.id.text1, new CharSequence[]{getResources().getText(17040516), getResources().getText(17040517), getResources().getText(17040518)});
        ListView listView = new ListView(this);
        listView.setAdapter(adapter);
        listView.setItemsCanFocus(true);
        listView.setOnItemClickListener(this);
        TextView textView = new TextView(this);
        CharSequence tooManyDeletesDescFormat = getResources().getText(17040515);
        if (this.mAccount != null) {
            textView.setText(String.format(tooManyDeletesDescFormat.toString(), new Object[]{Long.valueOf(this.mNumDeletes), this.mProvider, this.mAccount.name}));
        } else {
            textView.setText(String.format(tooManyDeletesDescFormat.toString(), new Object[]{Long.valueOf(this.mNumDeletes), this.mProvider, ProxyInfo.LOCAL_EXCL_LIST}));
        }
        LinearLayout ll = new LinearLayout(this);
        ll.setOrientation(1);
        LayoutParams lp = new LayoutParams(-1, -2, 0.0f);
        ll.addView(textView, lp);
        ll.addView(listView, lp);
        setContentView((View) ll);
    }

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
        extras.putBoolean(ContentResolver.SYNC_EXTRAS_MANUAL, true);
        extras.putBoolean(ContentResolver.SYNC_EXTRAS_EXPEDITED, true);
        extras.putBoolean(ContentResolver.SYNC_EXTRAS_UPLOAD, true);
        ContentResolver.requestSync(this.mAccount, this.mAuthority, extras);
    }

    private void startSyncUndoDeletes() {
        Bundle extras = new Bundle();
        extras.putBoolean(ContentResolver.SYNC_EXTRAS_DISCARD_LOCAL_DELETIONS, true);
        extras.putBoolean(ContentResolver.SYNC_EXTRAS_MANUAL, true);
        extras.putBoolean(ContentResolver.SYNC_EXTRAS_EXPEDITED, true);
        extras.putBoolean(ContentResolver.SYNC_EXTRAS_UPLOAD, true);
        ContentResolver.requestSync(this.mAccount, this.mAuthority, extras);
    }
}
