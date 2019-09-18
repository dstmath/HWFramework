package com.android.internal.app;

import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.os.Bundle;
import android.view.View;

public class MediaRouteChooserDialogFragment extends DialogFragment {
    private final String ARGUMENT_ROUTE_TYPES = "routeTypes";
    private View.OnClickListener mExtendedSettingsClickListener;

    public MediaRouteChooserDialogFragment() {
        int theme;
        if (MediaRouteChooserDialog.isLightTheme(getContext())) {
            theme = 16974130;
        } else {
            theme = 16974126;
        }
        setCancelable(true);
        setStyle(0, theme);
    }

    public int getRouteTypes() {
        Bundle args = getArguments();
        if (args != null) {
            return args.getInt("routeTypes");
        }
        return 0;
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

    public void setExtendedSettingsClickListener(View.OnClickListener listener) {
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
