package android.app;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ComponentInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.PaintFlagsDrawFilter;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.PaintDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ListView;
import android.widget.TextView;
import com.android.internal.R;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public abstract class LauncherActivity extends ListActivity {
    IconResizer mIconResizer;
    Intent mIntent;
    PackageManager mPackageManager;

    private class ActivityAdapter extends BaseAdapter implements Filterable {
        /* access modifiers changed from: private */
        public final Object lock = new Object();
        protected List<ListItem> mActivitiesList;
        private Filter mFilter;
        protected final IconResizer mIconResizer;
        protected final LayoutInflater mInflater;
        /* access modifiers changed from: private */
        public ArrayList<ListItem> mOriginalValues;
        private final boolean mShowIcons;

        private class ArrayFilter extends Filter {
            private ArrayFilter() {
            }

            /* access modifiers changed from: protected */
            public Filter.FilterResults performFiltering(CharSequence prefix) {
                Filter.FilterResults results = new Filter.FilterResults();
                if (ActivityAdapter.this.mOriginalValues == null) {
                    synchronized (ActivityAdapter.this.lock) {
                        ArrayList unused = ActivityAdapter.this.mOriginalValues = new ArrayList(ActivityAdapter.this.mActivitiesList);
                    }
                }
                if (prefix == null || prefix.length() == 0) {
                    synchronized (ActivityAdapter.this.lock) {
                        ArrayList<ListItem> list = new ArrayList<>(ActivityAdapter.this.mOriginalValues);
                        results.values = list;
                        results.count = list.size();
                    }
                } else {
                    String prefixString = prefix.toString().toLowerCase();
                    ArrayList<ListItem> values = ActivityAdapter.this.mOriginalValues;
                    int count = values.size();
                    ArrayList<ListItem> newValues = new ArrayList<>(count);
                    for (int i = 0; i < count; i++) {
                        ListItem item = values.get(i);
                        String[] words = item.label.toString().toLowerCase().split(" ");
                        int wordCount = words.length;
                        int k = 0;
                        while (true) {
                            if (k >= wordCount) {
                                break;
                            } else if (words[k].startsWith(prefixString)) {
                                newValues.add(item);
                                break;
                            } else {
                                k++;
                            }
                        }
                    }
                    results.values = newValues;
                    results.count = newValues.size();
                }
                return results;
            }

            /* access modifiers changed from: protected */
            public void publishResults(CharSequence constraint, Filter.FilterResults results) {
                ActivityAdapter.this.mActivitiesList = (List) results.values;
                if (results.count > 0) {
                    ActivityAdapter.this.notifyDataSetChanged();
                } else {
                    ActivityAdapter.this.notifyDataSetInvalidated();
                }
            }
        }

        public ActivityAdapter(IconResizer resizer) {
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
            ListItem item = this.mActivitiesList.get(position);
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
            return this.mActivitiesList.get(position);
        }

        public int getCount() {
            if (this.mActivitiesList != null) {
                return this.mActivitiesList.size();
            }
            return 0;
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
                view = this.mInflater.inflate(R.layout.activity_list_item_2, parent, false);
            } else {
                view = convertView;
            }
            bindView(view, this.mActivitiesList.get(position));
            return view;
        }

        private void bindView(View view, ListItem item) {
            TextView text = (TextView) view;
            text.setText(item.label);
            if (this.mShowIcons) {
                if (item.icon == null) {
                    item.icon = this.mIconResizer.createIconThumbnail(item.resolveInfo.loadIcon(LauncherActivity.this.getPackageManager()));
                }
                text.setCompoundDrawablesRelativeWithIntrinsicBounds(item.icon, null, null, null);
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
        private Canvas mCanvas = new Canvas();
        private int mIconHeight = -1;
        private int mIconWidth = -1;
        private final Rect mOldBounds = new Rect();

        public IconResizer() {
            this.mCanvas.setDrawFilter(new PaintFlagsDrawFilter(4, 2));
            int dimension = (int) LauncherActivity.this.getResources().getDimension(17104896);
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
            if (width < iconWidth || height < iconHeight) {
                float ratio = ((float) iconWidth) / ((float) iconHeight);
                if (iconWidth > iconHeight) {
                    height = (int) (((float) width) / ratio);
                } else if (iconHeight > iconWidth) {
                    width = (int) (((float) height) * ratio);
                }
                Bitmap thumb = Bitmap.createBitmap(this.mIconWidth, this.mIconHeight, icon.getOpacity() != -1 ? Bitmap.Config.ARGB_8888 : Bitmap.Config.RGB_565);
                Canvas canvas = this.mCanvas;
                canvas.setBitmap(thumb);
                this.mOldBounds.set(icon.getBounds());
                int x = (this.mIconWidth - width) / 2;
                int y = (this.mIconHeight - height) / 2;
                icon.setBounds(x, y, x + width, y + height);
                icon.draw(canvas);
                icon.setBounds(this.mOldBounds);
                Drawable icon2 = new BitmapDrawable(LauncherActivity.this.getResources(), thumb);
                canvas.setBitmap(null);
                return icon2;
            } else if (iconWidth >= width || iconHeight >= height) {
                return icon;
            } else {
                Bitmap thumb2 = Bitmap.createBitmap(this.mIconWidth, this.mIconHeight, Bitmap.Config.ARGB_8888);
                Canvas canvas2 = this.mCanvas;
                canvas2.setBitmap(thumb2);
                this.mOldBounds.set(icon.getBounds());
                int x2 = (width - iconWidth) / 2;
                int y2 = (height - iconHeight) / 2;
                icon.setBounds(x2, y2, x2 + iconWidth, y2 + iconHeight);
                icon.draw(canvas2);
                icon.setBounds(this.mOldBounds);
                Drawable icon3 = new BitmapDrawable(LauncherActivity.this.getResources(), thumb2);
                canvas2.setBitmap(null);
                return icon3;
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

        ListItem(PackageManager pm, ResolveInfo resolveInfo2, IconResizer resizer) {
            this.resolveInfo = resolveInfo2;
            this.label = resolveInfo2.loadLabel(pm);
            ComponentInfo ci = resolveInfo2.activityInfo;
            ci = ci == null ? resolveInfo2.serviceInfo : ci;
            if (this.label == null && ci != null) {
                this.label = resolveInfo2.activityInfo.name;
            }
            if (resizer != null) {
                this.icon = resizer.createIconThumbnail(resolveInfo2.loadIcon(pm));
            }
            this.packageName = ci.applicationInfo.packageName;
            this.className = ci.name;
        }

        public ListItem() {
        }
    }

    /* access modifiers changed from: protected */
    public void onCreate(Bundle icicle) {
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
        TextView alertTitle = (TextView) findViewById(R.id.alertTitle);
        if (alertTitle != null) {
            alertTitle.setText(getTitle());
        }
    }

    private void updateButtonText() {
        Button cancelButton = (Button) findViewById(16908313);
        if (cancelButton != null) {
            cancelButton.setOnClickListener(new View.OnClickListener() {
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

    /* access modifiers changed from: protected */
    public void onSetContentView() {
        setContentView((int) R.layout.activity_list);
    }

    /* access modifiers changed from: protected */
    public void onListItemClick(ListView l, View v, int position, long id) {
        startActivity(intentForPosition(position));
    }

    /* access modifiers changed from: protected */
    public Intent intentForPosition(int position) {
        return ((ActivityAdapter) this.mAdapter).intentForPosition(position);
    }

    /* access modifiers changed from: protected */
    public ListItem itemForPosition(int position) {
        return ((ActivityAdapter) this.mAdapter).itemForPosition(position);
    }

    /* access modifiers changed from: protected */
    public Intent getTargetIntent() {
        return new Intent();
    }

    /* access modifiers changed from: protected */
    public List<ResolveInfo> onQueryPackageManager(Intent queryIntent) {
        return this.mPackageManager.queryIntentActivities(queryIntent, 0);
    }

    /* access modifiers changed from: protected */
    public void onSortResultList(List<ResolveInfo> results) {
        Collections.sort(results, new ResolveInfo.DisplayNameComparator(this.mPackageManager));
    }

    public List<ListItem> makeListItems() {
        List<ResolveInfo> list = onQueryPackageManager(this.mIntent);
        onSortResultList(list);
        ArrayList<ListItem> result = new ArrayList<>(list.size());
        int listSize = list.size();
        for (int i = 0; i < listSize; i++) {
            result.add(new ListItem(this.mPackageManager, list.get(i), null));
        }
        return result;
    }

    /* access modifiers changed from: protected */
    public boolean onEvaluateShowIcons() {
        return true;
    }
}
