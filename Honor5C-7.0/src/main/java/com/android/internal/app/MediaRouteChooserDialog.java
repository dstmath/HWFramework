package com.android.internal.app;

import android.app.Dialog;
import android.content.Context;
import android.media.MediaRouter;
import android.media.MediaRouter.RouteInfo;
import android.media.MediaRouter.SimpleCallback;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import com.android.internal.R;
import java.util.Comparator;

public class MediaRouteChooserDialog extends Dialog {
    private RouteAdapter mAdapter;
    private boolean mAttachedToWindow;
    private final MediaRouterCallback mCallback;
    private Button mExtendedSettingsButton;
    private OnClickListener mExtendedSettingsClickListener;
    private ListView mListView;
    private int mRouteTypes;
    private final MediaRouter mRouter;

    private final class MediaRouterCallback extends SimpleCallback {
        private MediaRouterCallback() {
        }

        public void onRouteAdded(MediaRouter router, RouteInfo info) {
            MediaRouteChooserDialog.this.refreshRoutes();
        }

        public void onRouteRemoved(MediaRouter router, RouteInfo info) {
            MediaRouteChooserDialog.this.refreshRoutes();
        }

        public void onRouteChanged(MediaRouter router, RouteInfo info) {
            MediaRouteChooserDialog.this.refreshRoutes();
        }

        public void onRouteSelected(MediaRouter router, int type, RouteInfo info) {
            MediaRouteChooserDialog.this.dismiss();
        }
    }

    private final class RouteAdapter extends ArrayAdapter<RouteInfo> implements OnItemClickListener {
        private final LayoutInflater mInflater;

        public RouteAdapter(Context context) {
            super(context, 0);
            this.mInflater = LayoutInflater.from(context);
        }

        public void update() {
            clear();
            int count = MediaRouteChooserDialog.this.mRouter.getRouteCount();
            for (int i = 0; i < count; i++) {
                RouteInfo route = MediaRouteChooserDialog.this.mRouter.getRouteAt(i);
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
            return ((RouteInfo) getItem(position)).isEnabled();
        }

        public View getView(int position, View convertView, ViewGroup parent) {
            View view = convertView;
            if (convertView == null) {
                view = this.mInflater.inflate((int) R.layout.media_route_list_item, parent, false);
            }
            RouteInfo route = (RouteInfo) getItem(position);
            TextView text2 = (TextView) view.findViewById(R.id.text2);
            ((TextView) view.findViewById(R.id.text1)).setText(route.getName());
            CharSequence description = route.getDescription();
            if (TextUtils.isEmpty(description)) {
                text2.setVisibility(8);
                text2.setText((CharSequence) "");
            } else {
                text2.setVisibility(0);
                text2.setText(description);
            }
            view.setEnabled(route.isEnabled());
            return view;
        }

        public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
            RouteInfo route = (RouteInfo) getItem(position);
            if (route.isEnabled()) {
                route.select();
                MediaRouteChooserDialog.this.dismiss();
            }
        }
    }

    private static final class RouteComparator implements Comparator<RouteInfo> {
        public static final RouteComparator sInstance = null;

        static {
            /* JADX: method processing error */
/*
            Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.internal.app.MediaRouteChooserDialog.RouteComparator.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:263)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.internal.app.MediaRouteChooserDialog.RouteComparator.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 6 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 7 more
*/
            /*
            // Can't load method instructions.
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.internal.app.MediaRouteChooserDialog.RouteComparator.<clinit>():void");
        }

        private RouteComparator() {
        }

        public int compare(RouteInfo lhs, RouteInfo rhs) {
            return lhs.getName().toString().compareTo(rhs.getName().toString());
        }
    }

    public MediaRouteChooserDialog(Context context, int theme) {
        super(context, theme);
        this.mRouter = (MediaRouter) context.getSystemService("media_router");
        this.mCallback = new MediaRouterCallback();
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

    public void setExtendedSettingsClickListener(OnClickListener listener) {
        if (listener != this.mExtendedSettingsClickListener) {
            this.mExtendedSettingsClickListener = listener;
            updateExtendedSettingsButton();
        }
    }

    public boolean onFilterRoute(RouteInfo route) {
        return (route.isDefault() || !route.isEnabled()) ? false : route.matchesTypes(this.mRouteTypes);
    }

    protected void onCreate(Bundle savedInstanceState) {
        int i;
        super.onCreate(savedInstanceState);
        getWindow().requestFeature(3);
        setContentView(R.layout.media_route_chooser_dialog);
        if (this.mRouteTypes == 4) {
            i = R.string.media_route_chooser_title_for_remote_display;
        } else {
            i = R.string.media_route_chooser_title;
        }
        setTitle(i);
        getWindow().setFeatureDrawableResource(3, R.drawable.ic_media_route_off_holo_dark);
        this.mAdapter = new RouteAdapter(getContext());
        this.mListView = (ListView) findViewById(R.id.media_route_list);
        this.mListView.setAdapter(this.mAdapter);
        this.mListView.setOnItemClickListener(this.mAdapter);
        this.mListView.setEmptyView(findViewById(R.id.empty));
        this.mExtendedSettingsButton = (Button) findViewById(R.id.media_route_extended_settings_button);
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
}
