package com.android.server.os;

import android.content.Context;
import com.android.server.SystemService;

public class BugreportManagerService extends SystemService {
    private static final String TAG = "BugreportManagerService";
    private BugreportManagerServiceImpl mService;

    public BugreportManagerService(Context context) {
        super(context);
    }

    @Override // com.android.server.SystemService
    public void onStart() {
        this.mService = new BugreportManagerServiceImpl(getContext());
        publishBinderService("bugreport", this.mService);
    }
}
