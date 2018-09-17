package com.android.internal.app;

import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.provider.Telephony.Sms.Intents;
import com.android.internal.R;

public class ResolverTargetActionsDialogFragment extends DialogFragment implements OnClickListener {
    private static final int APP_INFO_INDEX = 1;
    private static final String NAME_KEY = "componentName";
    private static final String PINNED_KEY = "pinned";
    private static final String TITLE_KEY = "title";
    private static final int TOGGLE_PIN_INDEX = 0;

    public ResolverTargetActionsDialogFragment(CharSequence title, ComponentName name, boolean pinned) {
        Bundle args = new Bundle();
        args.putCharSequence("title", title);
        args.putParcelable(NAME_KEY, name);
        args.putBoolean("pinned", pinned);
        setArguments(args);
    }

    public Dialog onCreateDialog(Bundle savedInstanceState) {
        int itemRes;
        Bundle args = getArguments();
        if (args.getBoolean("pinned", false)) {
            itemRes = R.array.resolver_target_actions_unpin;
        } else {
            itemRes = R.array.resolver_target_actions_pin;
        }
        return new Builder(getContext()).setCancelable(true).setItems(itemRes, this).setTitle(args.getCharSequence("title")).create();
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
                startActivity(new Intent().setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).setData(Uri.fromParts(Intents.EXTRA_PACKAGE_NAME, name.getPackageName(), null)).addFlags(524288));
                break;
        }
        dismiss();
    }
}
