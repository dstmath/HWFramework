package com.android.internal.app;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;

public class ResolverTargetActionsDialogFragment extends DialogFragment implements DialogInterface.OnClickListener {
    private static final int APP_INFO_INDEX = 1;
    private static final String NAME_KEY = "componentName";
    private static final String PINNED_KEY = "pinned";
    private static final String TITLE_KEY = "title";
    private static final int TOGGLE_PIN_INDEX = 0;

    public ResolverTargetActionsDialogFragment() {
    }

    public ResolverTargetActionsDialogFragment(CharSequence title, ComponentName name, boolean pinned) {
        Bundle args = new Bundle();
        args.putCharSequence("title", title);
        args.putParcelable(NAME_KEY, name);
        args.putBoolean(PINNED_KEY, pinned);
        setArguments(args);
    }

    public Dialog onCreateDialog(Bundle savedInstanceState) {
        int itemRes;
        Bundle args = getArguments();
        if (args.getBoolean(PINNED_KEY, false)) {
            itemRes = 17236075;
        } else {
            itemRes = 17236074;
        }
        return new AlertDialog.Builder(getContext()).setCancelable(true).setItems(itemRes, this).setTitle(args.getCharSequence("title")).create();
    }

    public void onClick(DialogInterface dialog, int which) {
        ComponentName name = (ComponentName) getArguments().getParcelable(NAME_KEY);
        switch (which) {
            case 0:
                SharedPreferences sp = ChooserActivity.getPinnedSharedPrefs(getContext());
                String key = name.flattenToString();
                if (sp.getBoolean(name.flattenToString(), false)) {
                    sp.edit().remove(key).apply();
                } else {
                    sp.edit().putBoolean(key, true).apply();
                }
                getActivity().recreate();
                break;
            case 1:
                startActivity(new Intent().setAction("android.settings.APPLICATION_DETAILS_SETTINGS").setData(Uri.fromParts("package", name.getPackageName(), null)).addFlags(524288));
                break;
        }
        dismiss();
    }
}
