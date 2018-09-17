package com.android.internal.app;

import android.app.Activity;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.FragmentManager;
import android.content.Context;
import android.media.MediaRouter;
import android.media.MediaRouter.RouteInfo;
import android.util.Log;
import android.view.View.OnClickListener;
import com.android.internal.R;

public abstract class MediaRouteDialogPresenter {
    private static final String CHOOSER_FRAGMENT_TAG = "android.app.MediaRouteButton:MediaRouteChooserDialogFragment";
    private static final String CONTROLLER_FRAGMENT_TAG = "android.app.MediaRouteButton:MediaRouteControllerDialogFragment";
    private static final String TAG = "MediaRouter";

    public static DialogFragment showDialogFragment(Activity activity, int routeTypes, OnClickListener extendedSettingsClickListener) {
        MediaRouter router = (MediaRouter) activity.getSystemService("media_router");
        FragmentManager fm = activity.getFragmentManager();
        RouteInfo route = router.getSelectedRoute();
        if (route.isDefault() || (route.matchesTypes(routeTypes) ^ 1) != 0) {
            if (fm.findFragmentByTag(CHOOSER_FRAGMENT_TAG) != null) {
                Log.w(TAG, "showDialog(): Route chooser dialog already showing!");
                return null;
            }
            MediaRouteChooserDialogFragment f = new MediaRouteChooserDialogFragment();
            f.setRouteTypes(routeTypes);
            f.setExtendedSettingsClickListener(extendedSettingsClickListener);
            f.show(fm, CHOOSER_FRAGMENT_TAG);
            return f;
        } else if (fm.findFragmentByTag(CONTROLLER_FRAGMENT_TAG) != null) {
            Log.w(TAG, "showDialog(): Route controller dialog already showing!");
            return null;
        } else {
            MediaRouteControllerDialogFragment f2 = new MediaRouteControllerDialogFragment();
            f2.show(fm, CONTROLLER_FRAGMENT_TAG);
            return f2;
        }
    }

    public static Dialog createDialog(Context context, int routeTypes, OnClickListener extendedSettingsClickListener) {
        RouteInfo route = ((MediaRouter) context.getSystemService("media_router")).getSelectedRoute();
        if (!route.isDefault() && (route.matchesTypes(routeTypes) ^ 1) == 0) {
            return new MediaRouteControllerDialog(context, R.style.Theme_DeviceDefault_Dialog);
        }
        MediaRouteChooserDialog d = new MediaRouteChooserDialog(context, R.style.Theme_DeviceDefault_Dialog);
        d.setRouteTypes(routeTypes);
        d.setExtendedSettingsClickListener(extendedSettingsClickListener);
        return d;
    }
}
