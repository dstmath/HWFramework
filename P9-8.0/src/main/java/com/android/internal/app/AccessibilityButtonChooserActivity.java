package com.android.internal.app;

import android.accessibilityservice.AccessibilityServiceInfo;
import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.provider.Settings.Secure;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.view.accessibility.AccessibilityManager;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;
import com.android.internal.R;
import com.android.internal.app.-$Lambda$0r_TDm8lcq9IOpwsSKOm5X0EL-Q.AnonymousClass1;
import com.android.internal.widget.ResolverDrawerLayout;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class AccessibilityButtonChooserActivity extends Activity {
    private static final String MAGNIFICATION_COMPONENT_ID = "com.android.server.accessibility.MagnificationController";
    private AccessibilityButtonTarget mMagnificationTarget = null;
    private List<AccessibilityButtonTarget> mTargets = null;

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
        /* synthetic */ TargetAdapter(AccessibilityButtonChooserActivity this$0, TargetAdapter -this1) {
            this();
        }

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
            View root = AccessibilityButtonChooserActivity.this.getLayoutInflater().inflate((int) R.layout.accessibility_button_chooser_item, parent, false);
            AccessibilityButtonTarget target = (AccessibilityButtonTarget) AccessibilityButtonChooserActivity.this.mTargets.get(position);
            TextView labelView = (TextView) root.findViewById(R.id.accessibility_button_target_label);
            ((ImageView) root.findViewById(R.id.accessibility_button_target_icon)).setImageDrawable(target.getDrawable());
            labelView.setText(target.getLabel());
            return root;
        }
    }

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.accessibility_button_chooser);
        ResolverDrawerLayout rdl = (ResolverDrawerLayout) findViewById(R.id.contentPanel);
        if (rdl != null) {
            rdl.setOnDismissedListener(new AnonymousClass1(this));
        }
        if (TextUtils.isEmpty(Secure.getString(getContentResolver(), Secure.ACCESSIBILITY_BUTTON_TARGET_COMPONENT))) {
            ((TextView) findViewById(R.id.accessibility_button_prompt)).setVisibility(0);
        }
        this.mMagnificationTarget = new AccessibilityButtonTarget(this, MAGNIFICATION_COMPONENT_ID, R.string.accessibility_magnification_chooser_text, R.drawable.ic_accessibility_magnification);
        this.mTargets = getServiceAccessibilityButtonTargets(this);
        if (Secure.getInt(getContentResolver(), Secure.ACCESSIBILITY_DISPLAY_MAGNIFICATION_NAVBAR_ENABLED, 0) == 1) {
            this.mTargets.add(this.mMagnificationTarget);
        }
        if (this.mTargets.size() < 2) {
            finish();
        }
        GridView gridview = (GridView) findViewById(R.id.accessibility_button_chooser_grid);
        gridview.setAdapter(new TargetAdapter(this, null));
        gridview.setOnItemClickListener(new -$Lambda$0r_TDm8lcq9IOpwsSKOm5X0EL-Q(this));
    }

    /* synthetic */ void -com_android_internal_app_AccessibilityButtonChooserActivity-mthref-0() {
        finish();
    }

    /* synthetic */ void lambda$-com_android_internal_app_AccessibilityButtonChooserActivity_3326(AdapterView adapterView, View view, int position, long id) {
        onTargetSelected((AccessibilityButtonTarget) this.mTargets.get(position));
    }

    private static List<AccessibilityButtonTarget> getServiceAccessibilityButtonTargets(Context context) {
        List<AccessibilityServiceInfo> services = ((AccessibilityManager) context.getSystemService("accessibility")).getEnabledAccessibilityServiceList(-1);
        if (services == null) {
            return Collections.emptyList();
        }
        ArrayList<AccessibilityButtonTarget> targets = new ArrayList(services.size());
        for (AccessibilityServiceInfo info : services) {
            if ((info.flags & 256) != 0) {
                targets.add(new AccessibilityButtonTarget(context, info));
            }
        }
        return targets;
    }

    private void onTargetSelected(AccessibilityButtonTarget target) {
        Secure.putString(getContentResolver(), Secure.ACCESSIBILITY_BUTTON_TARGET_COMPONENT, target.getId());
        finish();
    }
}
