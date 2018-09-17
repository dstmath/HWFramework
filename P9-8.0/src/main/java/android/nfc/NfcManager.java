package android.nfc;

import android.content.Context;

public final class NfcManager {
    private NfcAdapter mAdapter;
    private Context mContext;

    public NfcManager(Context context) {
        this.mContext = context.getApplicationContext();
        if (this.mContext == null) {
            throw new IllegalArgumentException("context not associated with any application (using a mock context?)");
        }
        NfcAdapter adapter;
        try {
            adapter = NfcAdapter.getNfcAdapter(this.mContext);
        } catch (UnsupportedOperationException e) {
            adapter = null;
        }
        this.mAdapter = adapter;
    }

    public NfcAdapter getDefaultAdapter() {
        if (this.mAdapter == null) {
            try {
                this.mAdapter = NfcAdapter.getNfcAdapter(this.mContext);
            } catch (UnsupportedOperationException e) {
                this.mAdapter = null;
            }
        }
        return this.mAdapter;
    }
}
