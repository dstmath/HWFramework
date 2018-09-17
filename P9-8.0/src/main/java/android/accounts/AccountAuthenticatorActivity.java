package android.accounts;

import android.app.Activity;
import android.os.Bundle;

public class AccountAuthenticatorActivity extends Activity {
    private AccountAuthenticatorResponse mAccountAuthenticatorResponse = null;
    private Bundle mResultBundle = null;

    public final void setAccountAuthenticatorResult(Bundle result) {
        this.mResultBundle = result;
    }

    protected void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        this.mAccountAuthenticatorResponse = (AccountAuthenticatorResponse) getIntent().getParcelableExtra(AccountManager.KEY_ACCOUNT_AUTHENTICATOR_RESPONSE);
        if (this.mAccountAuthenticatorResponse != null) {
            this.mAccountAuthenticatorResponse.onRequestContinued();
        }
    }

    public void finish() {
        if (this.mAccountAuthenticatorResponse != null) {
            if (this.mResultBundle != null) {
                this.mAccountAuthenticatorResponse.onResult(this.mResultBundle);
            } else {
                this.mAccountAuthenticatorResponse.onError(4, "canceled");
            }
            this.mAccountAuthenticatorResponse = null;
        }
        super.finish();
    }
}
