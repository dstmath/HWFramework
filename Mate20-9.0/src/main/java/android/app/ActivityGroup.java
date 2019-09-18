package android.app;

import android.content.Intent;
import android.os.Bundle;
import java.util.HashMap;

@Deprecated
public class ActivityGroup extends Activity {
    static final String PARENT_NON_CONFIG_INSTANCE_KEY = "android:parent_non_config_instance";
    private static final String STATES_KEY = "android:states";
    protected LocalActivityManager mLocalActivityManager;

    public ActivityGroup() {
        this(true);
    }

    public ActivityGroup(boolean singleActivityMode) {
        this.mLocalActivityManager = new LocalActivityManager(this, singleActivityMode);
    }

    /* access modifiers changed from: protected */
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.mLocalActivityManager.dispatchCreate(savedInstanceState != null ? savedInstanceState.getBundle(STATES_KEY) : null);
    }

    /* access modifiers changed from: protected */
    public void onResume() {
        super.onResume();
        this.mLocalActivityManager.dispatchResume();
    }

    /* access modifiers changed from: protected */
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        Bundle state = this.mLocalActivityManager.saveInstanceState();
        if (state != null) {
            outState.putBundle(STATES_KEY, state);
        }
    }

    /* access modifiers changed from: protected */
    public void onPause() {
        super.onPause();
        this.mLocalActivityManager.dispatchPause(isFinishing());
    }

    /* access modifiers changed from: protected */
    public void onStop() {
        super.onStop();
        this.mLocalActivityManager.dispatchStop();
    }

    /* access modifiers changed from: protected */
    public void onDestroy() {
        super.onDestroy();
        this.mLocalActivityManager.dispatchDestroy(isFinishing());
    }

    public HashMap<String, Object> onRetainNonConfigurationChildInstances() {
        return this.mLocalActivityManager.dispatchRetainNonConfigurationInstance();
    }

    public Activity getCurrentActivity() {
        return this.mLocalActivityManager.getCurrentActivity();
    }

    public final LocalActivityManager getLocalActivityManager() {
        return this.mLocalActivityManager;
    }

    /* access modifiers changed from: package-private */
    public void dispatchActivityResult(String who, int requestCode, int resultCode, Intent data, String reason) {
        if (who != null) {
            Activity act = this.mLocalActivityManager.getActivity(who);
            if (act != null) {
                act.onActivityResult(requestCode, resultCode, data);
                return;
            }
        }
        super.dispatchActivityResult(who, requestCode, resultCode, data, reason);
    }
}
