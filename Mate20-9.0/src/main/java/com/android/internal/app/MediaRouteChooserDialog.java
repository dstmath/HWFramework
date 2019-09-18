package com.android.internal.app;

import android.app.Dialog;
import android.content.Context;
import android.media.MediaRouter;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import java.util.Comparator;

public class MediaRouteChooserDialog extends Dialog {
    private RouteAdapter mAdapter;
    private boolean mAttachedToWindow;
    private final MediaRouterCallback mCallback = new MediaRouterCallback();
    private Button mExtendedSettingsButton;
    private View.OnClickListener mExtendedSettingsClickListener;
    private ListView mListView;
    private int mRouteTypes;
    /* access modifiers changed from: private */
    public final MediaRouter mRouter;

    private final class MediaRouterCallback extends MediaRouter.SimpleCallback {
        private MediaRouterCallback() {
        }

        public void onRouteAdded(MediaRouter router, MediaRouter.RouteInfo info) {
            MediaRouteChooserDialog.this.refreshRoutes();
        }

        public void onRouteRemoved(MediaRouter router, MediaRouter.RouteInfo info) {
            MediaRouteChooserDialog.this.refreshRoutes();
        }

        public void onRouteChanged(MediaRouter router, MediaRouter.RouteInfo info) {
            MediaRouteChooserDialog.this.refreshRoutes();
        }

        public void onRouteSelected(MediaRouter router, int type, MediaRouter.RouteInfo info) {
            MediaRouteChooserDialog.this.dismiss();
        }
    }

    private final class RouteAdapter extends ArrayAdapter<MediaRouter.RouteInfo> implements AdapterView.OnItemClickListener {
        private final LayoutInflater mInflater;

        public RouteAdapter(Context context) {
            super(context, 0);
            this.mInflater = LayoutInflater.from(context);
        }

        public void update() {
            clear();
            int count = MediaRouteChooserDialog.this.mRouter.getRouteCount();
            for (int i = 0; i < count; i++) {
                MediaRouter.RouteInfo route = MediaRouteChooserDialog.this.mRouter.getRouteAt(i);
                if (MediaRouteChooserDialog.this.onFilterRoute(route)) {
                    add(route);
                }
            }
            sort(RouteComparator.sInstance);
            notifyDataSetChanged();
        }

        public boolean areAllItemsEnabled() {
            return false;
        }

        public boolean isEnabled(int position) {
            return ((MediaRouter.RouteInfo) getItem(position)).isEnabled();
        }

        public View getView(int position, View convertView, ViewGroup parent) {
            View view = convertView;
            if (view == null) {
                view = this.mInflater.inflate(17367178, parent, false);
            }
            MediaRouter.RouteInfo route = (MediaRouter.RouteInfo) getItem(position);
            TextView text2 = (TextView) view.findViewById(16908309);
            ((TextView) view.findViewById(16908308)).setText(route.getName());
            CharSequence description = route.getDescription();
            if (TextUtils.isEmpty(description)) {
                text2.setVisibility(8);
                text2.setText("");
            } else {
                text2.setVisibility(0);
                text2.setText(description);
            }
            view.setEnabled(route.isEnabled());
            return view;
        }

        public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
            MediaRouter.RouteInfo route = (MediaRouter.RouteInfo) getItem(position);
            if (route.isEnabled()) {
                route.select();
                MediaRouteChooserDialog.this.dismiss();
            }
        }
    }

    private static final class RouteComparator implements Comparator<MediaRouter.RouteInfo> {
        public static final RouteComparator sInstance = new RouteComparator();

        private RouteComparator() {
        }

        public int compare(MediaRouter.RouteInfo lhs, MediaRouter.RouteInfo rhs) {
            return lhs.getName().toString().compareTo(rhs.getName().toString());
        }
    }

    public MediaRouteChooserDialog(Context context, int theme) {
        super(context, theme);
        this.mRouter = (MediaRouter) context.getSystemService("media_router");
    }

    public int getRouteTypes() {
        return this.mRouteTypes;
    }

    public void setRouteTypes(int types) {
        if (this.mRouteTypes != types) {
            this.mRouteTypes = types;
            if (this.mAttachedToWindow) {
                this.mRouter.removeCallback(this.mCallback);
                this.mRouter.addCallback(types, this.mCallback, 1);
            }
            refreshRoutes();
        }
    }

    public void setExtendedSettingsClickListener(View.OnClickListener listener) {
        if (listener != this.mExtendedSettingsClickListener) {
            this.mExtendedSettingsClickListener = listener;
            updateExtendedSettingsButton();
        }
    }

    public boolean onFilterRoute(MediaRouter.RouteInfo route) {
        return !route.isDefault() && route.isEnabled() && route.matchesTypes(this.mRouteTypes);
    }

    /* access modifiers changed from: protected */
    public void onCreate(Bundle savedInstanceState) {
        int i;
        int i2;
        super.onCreate(savedInstanceState);
        getWindow().requestFeature(3);
        setContentView(17367176);
        if (this.mRouteTypes == 4) {
            i = 17040415;
        } else {
            i = 17040414;
        }
        setTitle(i);
        Window window = getWindow();
        if (isLightTheme(getContext())) {
            i2 = 17302616;
        } else {
            i2 = 17302615;
        }
        window.setFeatureDrawableResource(3, i2);
        this.mAdapter = new RouteAdapter(getContext());
        this.mListView = (ListView) findViewById(16909077);
        this.mListView.setAdapter(this.mAdapter);
        this.mListView.setOnItemClickListener(this.mAdapter);
        this.mListView.setEmptyView(findViewById(16908292));
        this.mExtendedSettingsButton = (Button) findViewById(16909076);
        updateExtendedSettingsButton();
    }

    private void updateExtendedSettingsButton() {
        if (this.mExtendedSettingsButton != null) {
            this.mExtendedSettingsButton.setOnClickListener(this.mExtendedSettingsClickListener);
            this.mExtendedSettingsButton.setVisibility(this.mExtendedSettingsClickListener != null ? 0 : 8);
        }
    }

    public void onAttachedToWindow() {
        super.onAttachedToWindow();
        this.mAttachedToWindow = true;
        this.mRouter.addCallback(this.mRouteTypes, this.mCallback, 1);
        refreshRoutes();
    }

    public void onDetachedFromWindow() {
        this.mAttachedToWindow = false;
        this.mRouter.removeCallback(this.mCallback);
        super.onDetachedFromWindow();
    }

    public void refreshRoutes() {
        if (this.mAttachedToWindow) {
            this.mAdapter.update();
        }
    }

    static boolean isLightTheme(Context context) {
        TypedValue value = new TypedValue();
        return context.getTheme().resolveAttribute(17891413, value, true) && value.data != 0;
    }
}
