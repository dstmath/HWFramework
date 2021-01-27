package android.nfc;

import android.annotation.UnsupportedAppUsage;
import android.content.Context;

public final class NfcManager {
    private NfcAdapter mAdapter;
    private Context mContext;

    @UnsupportedAppUsage(maxTargetSdk = 28, trackingBug = 115609023)
    public NfcManager(Context context) {
        NfcAdapter adapter;
        this.mContext = context.getApplicationContext();
        Context context2 = this.mContext;
        if (context2 != null) {
            try {
                adapter = NfcAdapter.getNfcAdapter(context2);
            } catch (UnsupportedOperationException e) {
                adapter = null;
            }
            this.mAdapter = adapter;
            return;
        }
        throw new IllegalArgumentException("context not associated with any application (using a mock context?)");
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
