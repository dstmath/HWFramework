package com.android.internal.app;

import android.accessibilityservice.AccessibilityServiceInfo;
import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.provider.Settings;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.view.accessibility.AccessibilityManager;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;
import com.android.internal.widget.ResolverDrawerLayout;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class AccessibilityButtonChooserActivity extends Activity {
    private static final String MAGNIFICATION_COMPONENT_ID = "com.android.server.accessibility.MagnificationController";
    private AccessibilityButtonTarget mMagnificationTarget = null;
    /* access modifiers changed from: private */
    public List<AccessibilityButtonTarget> mTargets = null;

    private static class AccessibilityButtonTarget {
        public Drawable mDrawable;
        public String mId;
        public CharSequence mLabel;

        public AccessibilityButtonTarget(Context context, AccessibilityServiceInfo serviceInfo) {
            this.mId = serviceInfo.getComponentName().flattenToString();
            this.mLabel = serviceInfo.getResolveInfo().loadLabel(context.getPackageManager());
            this.mDrawable = serviceInfo.getResolveInfo().loadIcon(context.getPackageManager());
        }

        public AccessibilityButtonTarget(Context context, String id, int labelResId, int iconRes) {
            this.mId = id;
            this.mLabel = context.getText(labelResId);
            this.mDrawable = context.getDrawable(iconRes);
        }

        public String getId() {
            return this.mId;
        }

        public CharSequence getLabel() {
            return this.mLabel;
        }

        public Drawable getDrawable() {
            return this.mDrawable;
        }
    }

    private class TargetAdapter extends BaseAdapter {
        private TargetAdapter() {
        }

        public int getCount() {
            return AccessibilityButtonChooserActivity.this.mTargets.size();
        }

        public Object getItem(int position) {
            return null;
        }

        public long getItemId(int position) {
            return (long) position;
        }

        public View getView(int position, View convertView, ViewGroup parent) {
            View root = AccessibilityButtonChooserActivity.this.getLayoutInflater().inflate(17367065, parent, false);
            AccessibilityButtonTarget target = (AccessibilityButtonTarget) AccessibilityButtonChooserActivity.this.mTargets.get(position);
            ((ImageView) root.findViewById(16908674)).setImageDrawable(target.getDrawable());
            ((TextView) root.findViewById(16908675)).setText(target.getLabel());
            return root;
        }
    }

    /* access modifiers changed from: protected */
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(17367064);
        ResolverDrawerLayout rdl = (ResolverDrawerLayout) findViewById(16908832);
        if (rdl != null) {
            rdl.setOnDismissedListener(new ResolverDrawerLayout.OnDismissedListener() {
                public final void onDismissed() {
                    AccessibilityButtonChooserActivity.this.finish();
                }
            });
        }
        if (TextUtils.isEmpty(Settings.Secure.getString(getContentResolver(), "accessibility_button_target_component"))) {
            ((TextView) findViewById(16908673)).setVisibility(0);
        }
        this.mMagnificationTarget = new AccessibilityButtonTarget(this, MAGNIFICATION_COMPONENT_ID, 17039530, 17302259);
        this.mTargets = getServiceAccessibilityButtonTargets(this);
        if (Settings.Secure.getInt(getContentResolver(), "accessibility_display_magnification_navbar_enabled", 0) == 1) {
            this.mTargets.add(this.mMagnificationTarget);
        }
        if (this.mTargets.size() < 2) {
            finish();
        }
        GridView gridview = (GridView) findViewById(16908672);
        gridview.setAdapter(new TargetAdapter());
        gridview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public final void onItemClick(AdapterView adapterView, View view, int i, long j) {
                AccessibilityButtonChooserActivity.this.onTargetSelected(AccessibilityButtonChooserActivity.this.mTargets.get(i));
            }
        });
    }

    private static List<AccessibilityButtonTarget> getServiceAccessibilityButtonTargets(Context context) {
        List<AccessibilityServiceInfo> services = ((AccessibilityManager) context.getSystemService("accessibility")).getEnabledAccessibilityServiceList(-1);
        if (services == null) {
            return Collections.emptyList();
        }
        ArrayList<AccessibilityButtonTarget> targets = new ArrayList<>(services.size());
        for (AccessibilityServiceInfo info : services) {
            if ((info.flags & 256) != 0) {
                targets.add(new AccessibilityButtonTarget(context, info));
            }
        }
        return targets;
    }

    /* access modifiers changed from: private */
    public void onTargetSelected(AccessibilityButtonTarget target) {
        Settings.Secure.putString(getContentResolver(), "accessibility_button_target_component", target.getId());
        finish();
    }
}
