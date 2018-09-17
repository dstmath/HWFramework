package com.android.internal.app;

import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.os.Bundle;
import com.android.internal.R;

public class MediaRouteControllerDialogFragment extends DialogFragment {
    public MediaRouteControllerDialogFragment() {
        setCancelable(true);
        setStyle(0, R.style.Theme_DeviceDefault_Dialog);
    }

    public MediaRouteControllerDialog onCreateControllerDialog(Context context, Bundle savedInstanceState) {
        return new MediaRouteControllerDialog(context, getTheme());
    }

    public Dialog onCreateDialog(Bundle savedInstanceState) {
        return onCreateControllerDialog(getContext(), savedInstanceState);
    }
}
