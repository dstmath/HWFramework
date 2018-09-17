package com.android.contacts.locationdataupdate;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

public class LocationDataUpdater extends Activity {
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        startActivity(createRedirectIntent());
        finish();
    }

    private Intent createRedirectIntent() {
        Intent intent = new Intent();
        intent.setAction("com.android.contacts.action.UPDATE");
        intent.setPackage(getPackageName());
        return intent;
    }
}
