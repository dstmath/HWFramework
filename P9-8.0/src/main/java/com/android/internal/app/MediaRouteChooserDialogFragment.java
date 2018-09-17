package com.android.internal.app;

import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.os.Bundle;
import android.view.View.OnClickListener;
import com.android.internal.R;

public class MediaRouteChooserDialogFragment extends DialogFragment {
    private final String ARGUMENT_ROUTE_TYPES = "routeTypes";
    private OnClickListener mExtendedSettingsClickListener;

    public MediaRouteChooserDialogFragment() {
        setCancelable(true);
        setStyle(0, R.style.Theme_DeviceDefault_Dialog);
    }

    public int getRouteTypes() {
        Bundle args = getArguments();
        return args != null ? args.getInt("routeTypes") : 0;
    }

    public void setRouteTypes(int types) {
        if (types != getRouteTypes()) {
            Bundle args = getArguments();
            if (args == null) {
                args = new Bundle();
            }
            args.putInt("routeTypes", types);
            setArguments(args);
            MediaRouteChooserDialog dialog = (MediaRouteChooserDialog) getDialog();
            if (dialog != null) {
                dialog.setRouteTypes(types);
            }
        }
    }

    public void setExtendedSettingsClickListener(OnClickListener listener) {
        if (listener != this.mExtendedSettingsClickListener) {
            this.mExtendedSettingsClickListener = listener;
            MediaRouteChooserDialog dialog = (MediaRouteChooserDialog) getDialog();
            if (dialog != null) {
                dialog.setExtendedSettingsClickListener(listener);
            }
        }
    }

    public MediaRouteChooserDialog onCreateChooserDialog(Context context, Bundle savedInstanceState) {
        return new MediaRouteChooserDialog(context, getTheme());
    }

    public Dialog onCreateDialog(Bundle savedInstanceState) {
        MediaRouteChooserDialog dialog = onCreateChooserDialog(getActivity(), savedInstanceState);
        dialog.setRouteTypes(getRouteTypes());
        dialog.setExtendedSettingsClickListener(this.mExtendedSettingsClickListener);
        return dialog;
    }
}
