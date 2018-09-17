package android.app;

import android.R;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ComponentInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.pm.ResolveInfo.DisplayNameComparator;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.PaintFlagsDrawFilter;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.PaintDrawable;
import android.net.wifi.WifiEnterpriseConfig;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.Filter;
import android.widget.Filter.FilterResults;
import android.widget.Filterable;
import android.widget.ListView;
import android.widget.TextView;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public abstract class LauncherActivity extends ListActivity {
    IconResizer mIconResizer;
    Intent mIntent;
    PackageManager mPackageManager;

    private class ActivityAdapter extends BaseAdapter implements Filterable {
        private final Object lock;
        protected List<ListItem> mActivitiesList;
        private Filter mFilter;
        protected final IconResizer mIconResizer;
        protected final LayoutInflater mInflater;
        private ArrayList<ListItem> mOriginalValues;
        private final boolean mShowIcons;

        private class ArrayFilter extends Filter {
            private ArrayFilter() {
            }

            protected FilterResults performFiltering(CharSequence prefix) {
                FilterResults results = new FilterResults();
                if (ActivityAdapter.this.mOriginalValues == null) {
                    synchronized (ActivityAdapter.this.lock) {
                        ActivityAdapter.this.mOriginalValues = new ArrayList(ActivityAdapter.this.mActivitiesList);
                    }
                }
                if (prefix == null || prefix.length() == 0) {
                    synchronized (ActivityAdapter.this.lock) {
                        ArrayList<ListItem> list = new ArrayList(ActivityAdapter.this.mOriginalValues);
                        results.values = list;
                        results.count = list.size();
                    }
                } else {
                    String prefixString = prefix.toString().toLowerCase();
                    ArrayList<ListItem> values = ActivityAdapter.this.mOriginalValues;
                    int count = values.size();
                    ArrayList<ListItem> newValues = new ArrayList(count);
                    for (int i = 0; i < count; i++) {
                        ListItem item = (ListItem) values.get(i);
                        for (String word : item.label.toString().toLowerCase().split(WifiEnterpriseConfig.CA_CERT_ALIAS_DELIMITER)) {
                            if (word.startsWith(prefixString)) {
                                newValues.add(item);
                                break;
                            }
                        }
                    }
                    results.values = newValues;
                    results.count = newValues.size();
                }
                return results;
            }

            protected void publishResults(CharSequence constraint, FilterResults results) {
                ActivityAdapter.this.mActivitiesList = (List) results.values;
                if (results.count > 0) {
                    ActivityAdapter.this.notifyDataSetChanged();
                } else {
                    ActivityAdapter.this.notifyDataSetInvalidated();
                }
            }
        }

        public ActivityAdapter(IconResizer resizer) {
            this.lock = new Object();
            this.mIconResizer = resizer;
            this.mInflater = (LayoutInflater) LauncherActivity.this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            this.mShowIcons = LauncherActivity.this.onEvaluateShowIcons();
            this.mActivitiesList = LauncherActivity.this.makeListItems();
        }

        public Intent intentForPosition(int position) {
            if (this.mActivitiesList == null) {
                return null;
            }
            Intent intent = new Intent(LauncherActivity.this.mIntent);
            ListItem item = (ListItem) this.mActivitiesList.get(position);
            intent.setClassName(item.packageName, item.className);
            if (item.extras != null) {
                intent.putExtras(item.extras);
            }
            return intent;
        }

        public ListItem itemForPosition(int position) {
            if (this.mActivitiesList == null) {
                return null;
            }
            return (ListItem) this.mActivitiesList.get(position);
        }

        public int getCount() {
            return this.mActivitiesList != null ? this.mActivitiesList.size() : 0;
        }

        public Object getItem(int position) {
            return Integer.valueOf(position);
        }

        public long getItemId(int position) {
            return (long) position;
        }

        public View getView(int position, View convertView, ViewGroup parent) {
            View view;
            if (convertView == null) {
                view = this.mInflater.inflate(17367076, parent, false);
            } else {
                view = convertView;
            }
            bindView(view, (ListItem) this.mActivitiesList.get(position));
            return view;
        }

        private void bindView(View view, ListItem item) {
            TextView text = (TextView) view;
            text.setText(item.label);
            if (this.mShowIcons) {
                if (item.icon == null) {
                    item.icon = this.mIconResizer.createIconThumbnail(item.resolveInfo.loadIcon(LauncherActivity.this.getPackageManager()));
                }
                text.setCompoundDrawablesWithIntrinsicBounds(item.icon, null, null, null);
            }
        }

        public Filter getFilter() {
            if (this.mFilter == null) {
                this.mFilter = new ArrayFilter();
            }
            return this.mFilter;
        }
    }

    public class IconResizer {
        private Canvas mCanvas;
        private int mIconHeight;
        private int mIconWidth;
        private final Rect mOldBounds;

        public IconResizer() {
            this.mIconWidth = -1;
            this.mIconHeight = -1;
            this.mOldBounds = new Rect();
            this.mCanvas = new Canvas();
            this.mCanvas.setDrawFilter(new PaintFlagsDrawFilter(4, 2));
            int dimension = (int) LauncherActivity.this.getResources().getDimension(R.dimen.app_icon_size);
            this.mIconHeight = dimension;
            this.mIconWidth = dimension;
        }

        public Drawable createIconThumbnail(Drawable icon) {
            int width = this.mIconWidth;
            int height = this.mIconHeight;
            int iconWidth = icon.getIntrinsicWidth();
            int iconHeight = icon.getIntrinsicHeight();
            if (icon instanceof PaintDrawable) {
                PaintDrawable painter = (PaintDrawable) icon;
                painter.setIntrinsicWidth(width);
                painter.setIntrinsicHeight(height);
            }
            if (width <= 0 || height <= 0) {
                return icon;
            }
            Bitmap thumb;
            Canvas canvas;
            int x;
            int y;
            if (width < iconWidth || height < iconHeight) {
                float ratio = ((float) iconWidth) / ((float) iconHeight);
                if (iconWidth > iconHeight) {
                    height = (int) (((float) width) / ratio);
                } else if (iconHeight > iconWidth) {
                    width = (int) (((float) height) * ratio);
                }
                thumb = Bitmap.createBitmap(this.mIconWidth, this.mIconHeight, icon.getOpacity() != -1 ? Config.ARGB_8888 : Config.RGB_565);
                canvas = this.mCanvas;
                canvas.setBitmap(thumb);
                this.mOldBounds.set(icon.getBounds());
                x = (this.mIconWidth - width) / 2;
                y = (this.mIconHeight - height) / 2;
                icon.setBounds(x, y, x + width, y + height);
                icon.draw(canvas);
                icon.setBounds(this.mOldBounds);
                icon = new BitmapDrawable(LauncherActivity.this.getResources(), thumb);
                canvas.setBitmap(null);
                return icon;
            } else if (iconWidth >= width || iconHeight >= height) {
                return icon;
            } else {
                thumb = Bitmap.createBitmap(this.mIconWidth, this.mIconHeight, Config.ARGB_8888);
                canvas = this.mCanvas;
                canvas.setBitmap(thumb);
                this.mOldBounds.set(icon.getBounds());
                x = (width - iconWidth) / 2;
                y = (height - iconHeight) / 2;
                icon.setBounds(x, y, x + iconWidth, y + iconHeight);
                icon.draw(canvas);
                icon.setBounds(this.mOldBounds);
                icon = new BitmapDrawable(LauncherActivity.this.getResources(), thumb);
                canvas.setBitmap(null);
                return icon;
            }
        }
    }

    public static class ListItem {
        public String className;
        public Bundle extras;
        public Drawable icon;
        public CharSequence label;
        public String packageName;
        public ResolveInfo resolveInfo;

        ListItem(PackageManager pm, ResolveInfo resolveInfo, IconResizer resizer) {
            this.resolveInfo = resolveInfo;
            this.label = resolveInfo.loadLabel(pm);
            ComponentInfo ci = resolveInfo.activityInfo;
            if (ci == null) {
                ci = resolveInfo.serviceInfo;
            }
            if (this.label == null && ci != null) {
                this.label = resolveInfo.activityInfo.name;
            }
            if (resizer != null) {
                this.icon = resizer.createIconThumbnail(resolveInfo.loadIcon(pm));
            }
            this.packageName = ci.applicationInfo.packageName;
            this.className = ci.name;
        }
    }

    protected void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        this.mPackageManager = getPackageManager();
        if (!this.mPackageManager.hasSystemFeature(PackageManager.FEATURE_WATCH)) {
            requestWindowFeature(5);
            setProgressBarIndeterminateVisibility(true);
        }
        onSetContentView();
        this.mIconResizer = new IconResizer();
        this.mIntent = new Intent(getTargetIntent());
        this.mIntent.setComponent(null);
        this.mAdapter = new ActivityAdapter(this.mIconResizer);
        setListAdapter(this.mAdapter);
        getListView().setTextFilterEnabled(true);
        updateAlertTitle();
        updateButtonText();
        if (!this.mPackageManager.hasSystemFeature(PackageManager.FEATURE_WATCH)) {
            setProgressBarIndeterminateVisibility(false);
        }
    }

    private void updateAlertTitle() {
        TextView alertTitle = (TextView) findViewById(16909084);
        if (alertTitle != null) {
            alertTitle.setText(getTitle());
        }
    }

    private void updateButtonText() {
        Button cancelButton = (Button) findViewById(R.id.button1);
        if (cancelButton != null) {
            cancelButton.setOnClickListener(new OnClickListener() {
                public void onClick(View v) {
                    LauncherActivity.this.finish();
                }
            });
        }
    }

    public void setTitle(CharSequence title) {
        super.setTitle(title);
        updateAlertTitle();
    }

    public void setTitle(int titleId) {
        super.setTitle(titleId);
        updateAlertTitle();
    }

    protected void onSetContentView() {
        setContentView(17367075);
    }

    protected void onListItemClick(ListView l, View v, int position, long id) {
        startActivity(intentForPosition(position));
    }

    protected Intent intentForPosition(int position) {
        return this.mAdapter.intentForPosition(position);
    }

    protected ListItem itemForPosition(int position) {
        return this.mAdapter.itemForPosition(position);
    }

    protected Intent getTargetIntent() {
        return new Intent();
    }

    protected List<ResolveInfo> onQueryPackageManager(Intent queryIntent) {
        return this.mPackageManager.queryIntentActivities(queryIntent, 0);
    }

    protected void onSortResultList(List<ResolveInfo> results) {
        Collections.sort(results, new DisplayNameComparator(this.mPackageManager));
    }

    public List<ListItem> makeListItems() {
        List<ResolveInfo> list = onQueryPackageManager(this.mIntent);
        onSortResultList(list);
        ArrayList<ListItem> result = new ArrayList(list.size());
        int listSize = list.size();
        for (int i = 0; i < listSize; i++) {
            result.add(new ListItem(this.mPackageManager, (ResolveInfo) list.get(i), null));
        }
        return result;
    }

    protected boolean onEvaluateShowIcons() {
        return true;
    }
}
