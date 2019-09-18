package android.accounts;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import com.android.internal.R;

public class CantAddAccountActivity extends Activity {
    public static final String EXTRA_ERROR_CODE = "android.accounts.extra.ERROR_CODE";

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView((int) R.layout.app_not_authorized);
    }

    public void onCancelButtonClicked(View view) {
        onBackPressed();
    }
}
