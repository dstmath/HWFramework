package android.accounts;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;

public class CantAddAccountActivity extends Activity {
    public static final String EXTRA_ERROR_CODE = "android.accounts.extra.ERROR_CODE";

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(17367094);
    }

    public void onCancelButtonClicked(View view) {
        onBackPressed();
    }
}
