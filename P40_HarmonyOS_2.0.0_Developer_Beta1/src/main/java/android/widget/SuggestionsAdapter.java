package android.widget;

import android.app.SearchManager;
import android.app.SearchableInfo;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.net.wifi.WifiEnterpriseConfig;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.style.TextAppearanceSpan;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import com.android.internal.R;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.WeakHashMap;

/* access modifiers changed from: package-private */
public class SuggestionsAdapter extends ResourceCursorAdapter implements View.OnClickListener {
    private static final boolean DBG = false;
    private static final long DELETE_KEY_POST_DELAY = 500;
    static final int INVALID_INDEX = -1;
    private static final String LOG_TAG = "SuggestionsAdapter";
    private static final int QUERY_LIMIT = 50;
    static final int REFINE_ALL = 2;
    static final int REFINE_BY_ENTRY = 1;
    static final int REFINE_NONE = 0;
    private boolean mClosed = false;
    private final int mCommitIconResId;
    private int mFlagsCol = -1;
    private int mIconName1Col = -1;
    private int mIconName2Col = -1;
    private final WeakHashMap<String, Drawable.ConstantState> mOutsideDrawablesCache;
    private final Context mProviderContext;
    private int mQueryRefinement = 1;
    private final SearchManager mSearchManager = ((SearchManager) this.mContext.getSystemService("search"));
    private final SearchView mSearchView;
    private final SearchableInfo mSearchable;
    private int mText1Col = -1;
    private int mText2Col = -1;
    private int mText2UrlCol = -1;
    private ColorStateList mUrlColor;

    public SuggestionsAdapter(Context context, SearchView searchView, SearchableInfo searchable, WeakHashMap<String, Drawable.ConstantState> outsideDrawablesCache) {
        super(context, searchView.getSuggestionRowLayout(), (Cursor) null, true);
        this.mSearchView = searchView;
        this.mSearchable = searchable;
        this.mCommitIconResId = searchView.getSuggestionCommitIconResId();
        this.mProviderContext = this.mSearchable.getProviderContext(this.mContext, this.mSearchable.getActivityContext(this.mContext));
        this.mOutsideDrawablesCache = outsideDrawablesCache;
        getFilter().setDelayer(new Filter.Delayer() {
            /* class android.widget.SuggestionsAdapter.AnonymousClass1 */
            private int mPreviousLength = 0;

            @Override // android.widget.Filter.Delayer
            public long getPostingDelay(CharSequence constraint) {
                long delay = 0;
                if (constraint == null) {
                    return 0;
                }
                if (constraint.length() < this.mPreviousLength) {
                    delay = SuggestionsAdapter.DELETE_KEY_POST_DELAY;
                }
                this.mPreviousLength = constraint.length();
                return delay;
            }
        });
    }

    public void setQueryRefinement(int refineWhat) {
        this.mQueryRefinement = refineWhat;
    }

    public int getQueryRefinement() {
        return this.mQueryRefinement;
    }

    @Override // android.widget.CursorAdapter, android.widget.BaseAdapter, android.widget.Adapter
    public boolean hasStableIds() {
        return false;
    }

    @Override // android.widget.CursorAdapter, android.widget.CursorFilter.CursorFilterClient
    public Cursor runQueryOnBackgroundThread(CharSequence constraint) {
        String query = constraint == null ? "" : constraint.toString();
        if (this.mSearchView.getVisibility() != 0 || this.mSearchView.getWindowVisibility() != 0) {
            return null;
        }
        try {
            Cursor cursor = this.mSearchManager.getSuggestions(this.mSearchable, query, 50);
            if (cursor != null) {
                cursor.getCount();
                return cursor;
            }
        } catch (RuntimeException e) {
            Log.w(LOG_TAG, "Search suggestions query threw an exception.", e);
        }
        return null;
    }

    public void close() {
        changeCursor(null);
        this.mClosed = true;
    }

    @Override // android.widget.BaseAdapter
    public void notifyDataSetChanged() {
        super.notifyDataSetChanged();
        updateSpinnerState(getCursor());
    }

    @Override // android.widget.BaseAdapter
    public void notifyDataSetInvalidated() {
        super.notifyDataSetInvalidated();
        updateSpinnerState(getCursor());
    }

    private void updateSpinnerState(Cursor cursor) {
        Bundle extras = cursor != null ? cursor.getExtras() : null;
        if (extras != null && !extras.getBoolean(SearchManager.CURSOR_EXTRA_KEY_IN_PROGRESS)) {
        }
    }

    @Override // android.widget.CursorAdapter, android.widget.CursorFilter.CursorFilterClient
    public void changeCursor(Cursor c) {
        if (this.mClosed) {
            Log.w(LOG_TAG, "Tried to change cursor after adapter was closed.");
            if (c != null) {
                c.close();
                return;
            }
            return;
        }
        try {
            super.changeCursor(c);
            if (c != null) {
                this.mText1Col = c.getColumnIndex(SearchManager.SUGGEST_COLUMN_TEXT_1);
                this.mText2Col = c.getColumnIndex(SearchManager.SUGGEST_COLUMN_TEXT_2);
                this.mText2UrlCol = c.getColumnIndex(SearchManager.SUGGEST_COLUMN_TEXT_2_URL);
                this.mIconName1Col = c.getColumnIndex(SearchManager.SUGGEST_COLUMN_ICON_1);
                this.mIconName2Col = c.getColumnIndex(SearchManager.SUGGEST_COLUMN_ICON_2);
                this.mFlagsCol = c.getColumnIndex(SearchManager.SUGGEST_COLUMN_FLAGS);
            }
        } catch (Exception e) {
            Log.e(LOG_TAG, "error changing cursor and caching columns", e);
        }
    }

    @Override // android.widget.ResourceCursorAdapter, android.widget.CursorAdapter
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        View v = super.newView(context, cursor, parent);
        v.setTag(new ChildViewCache(v));
        ((ImageView) v.findViewById(R.id.edit_query)).setImageResource(this.mCommitIconResId);
        return v;
    }

    /* access modifiers changed from: private */
    public static final class ChildViewCache {
        public final ImageView mIcon1;
        public final ImageView mIcon2;
        public final ImageView mIconRefine;
        public final TextView mText1;
        public final TextView mText2;

        public ChildViewCache(View v) {
            this.mText1 = (TextView) v.findViewById(16908308);
            this.mText2 = (TextView) v.findViewById(16908309);
            this.mIcon1 = (ImageView) v.findViewById(16908295);
            this.mIcon2 = (ImageView) v.findViewById(16908296);
            this.mIconRefine = (ImageView) v.findViewById(R.id.edit_query);
        }
    }

    @Override // android.widget.CursorAdapter
    public void bindView(View view, Context context, Cursor cursor) {
        CharSequence text2;
        ChildViewCache views = (ChildViewCache) view.getTag();
        int flags = 0;
        int i = this.mFlagsCol;
        if (i != -1) {
            flags = cursor.getInt(i);
        }
        if (views.mText1 != null) {
            setViewText(views.mText1, getStringOrNull(cursor, this.mText1Col));
        }
        if (views.mText2 != null) {
            CharSequence text22 = getStringOrNull(cursor, this.mText2UrlCol);
            if (text22 != null) {
                text2 = formatUrl(context, text22);
            } else {
                text2 = getStringOrNull(cursor, this.mText2Col);
            }
            if (TextUtils.isEmpty(text2)) {
                if (views.mText1 != null) {
                    views.mText1.setSingleLine(false);
                    views.mText1.setMaxLines(2);
                }
            } else if (views.mText1 != null) {
                views.mText1.setSingleLine(true);
                views.mText1.setMaxLines(1);
            }
            setViewText(views.mText2, text2);
        }
        if (views.mIcon1 != null) {
            setViewDrawable(views.mIcon1, getIcon1(cursor), 4);
        }
        if (views.mIcon2 != null) {
            setViewDrawable(views.mIcon2, getIcon2(cursor), 8);
        }
        int i2 = this.mQueryRefinement;
        if (i2 == 2 || (i2 == 1 && (flags & 1) != 0)) {
            views.mIconRefine.setVisibility(0);
            views.mIconRefine.setTag(views.mText1.getText());
            views.mIconRefine.setOnClickListener(this);
            return;
        }
        views.mIconRefine.setVisibility(8);
    }

    @Override // android.view.View.OnClickListener
    public void onClick(View v) {
        Object tag = v.getTag();
        if (tag instanceof CharSequence) {
            this.mSearchView.onQueryRefine((CharSequence) tag);
        }
    }

    private CharSequence formatUrl(Context context, CharSequence url) {
        if (this.mUrlColor == null) {
            TypedValue colorValue = new TypedValue();
            context.getTheme().resolveAttribute(R.attr.textColorSearchUrl, colorValue, true);
            this.mUrlColor = context.getColorStateList(colorValue.resourceId);
        }
        SpannableString text = new SpannableString(url);
        text.setSpan(new TextAppearanceSpan(null, 0, 0, this.mUrlColor, null), 0, url.length(), 33);
        return text;
    }

    private void setViewText(TextView v, CharSequence text) {
        v.setText(text);
        if (TextUtils.isEmpty(text)) {
            v.setVisibility(8);
        } else {
            v.setVisibility(0);
        }
    }

    private Drawable getIcon1(Cursor cursor) {
        int i = this.mIconName1Col;
        if (i == -1) {
            return null;
        }
        Drawable drawable = getDrawableFromResourceValue(cursor.getString(i));
        if (drawable != null) {
            return drawable;
        }
        return getDefaultIcon1(cursor);
    }

    private Drawable getIcon2(Cursor cursor) {
        int i = this.mIconName2Col;
        if (i == -1) {
            return null;
        }
        return getDrawableFromResourceValue(cursor.getString(i));
    }

    private void setViewDrawable(ImageView v, Drawable drawable, int nullVisibility) {
        v.setImageDrawable(drawable);
        if (drawable == null) {
            v.setVisibility(nullVisibility);
            return;
        }
        v.setVisibility(0);
        drawable.setVisible(false, false);
        drawable.setVisible(true, false);
    }

    @Override // android.widget.CursorAdapter, android.widget.CursorFilter.CursorFilterClient
    public CharSequence convertToString(Cursor cursor) {
        String text1;
        String data;
        if (cursor == null) {
            return null;
        }
        String query = getColumnString(cursor, SearchManager.SUGGEST_COLUMN_QUERY);
        if (query != null) {
            return query;
        }
        if (this.mSearchable.shouldRewriteQueryFromData() && (data = getColumnString(cursor, SearchManager.SUGGEST_COLUMN_INTENT_DATA)) != null) {
            return data;
        }
        if (!this.mSearchable.shouldRewriteQueryFromText() || (text1 = getColumnString(cursor, SearchManager.SUGGEST_COLUMN_TEXT_1)) == null) {
            return null;
        }
        return text1;
    }

    @Override // android.widget.CursorAdapter, android.widget.Adapter
    public View getView(int position, View convertView, ViewGroup parent) {
        try {
            return super.getView(position, convertView, parent);
        } catch (RuntimeException e) {
            Log.w(LOG_TAG, "Search suggestions cursor threw exception.", e);
            View v = newView(this.mContext, this.mCursor, parent);
            if (v != null) {
                ((ChildViewCache) v.getTag()).mText1.setText(e.toString());
            }
            return v;
        }
    }

    @Override // android.widget.CursorAdapter, android.widget.BaseAdapter, android.widget.SpinnerAdapter
    public View getDropDownView(int position, View convertView, ViewGroup parent) {
        try {
            return super.getDropDownView(position, convertView, parent);
        } catch (RuntimeException e) {
            Log.w(LOG_TAG, "Search suggestions cursor threw exception.", e);
            View v = newDropDownView(this.mDropDownContext == null ? this.mContext : this.mDropDownContext, this.mCursor, parent);
            if (v != null) {
                ((ChildViewCache) v.getTag()).mText1.setText(e.toString());
            }
            return v;
        }
    }

    private Drawable getDrawableFromResourceValue(String drawableId) {
        if (drawableId == null || drawableId.length() == 0 || WifiEnterpriseConfig.ENGINE_DISABLE.equals(drawableId)) {
            return null;
        }
        try {
            int resourceId = Integer.parseInt(drawableId);
            String drawableUri = "android.resource://" + this.mProviderContext.getPackageName() + "/" + resourceId;
            Drawable drawable = checkIconCache(drawableUri);
            if (drawable != null) {
                return drawable;
            }
            Drawable drawable2 = this.mProviderContext.getDrawable(resourceId);
            storeInIconCache(drawableUri, drawable2);
            return drawable2;
        } catch (NumberFormatException e) {
            Drawable drawable3 = checkIconCache(drawableId);
            if (drawable3 != null) {
                return drawable3;
            }
            Drawable drawable4 = getDrawable(Uri.parse(drawableId));
            storeInIconCache(drawableId, drawable4);
            return drawable4;
        } catch (Resources.NotFoundException e2) {
            Log.w(LOG_TAG, "Icon resource not found: " + drawableId);
            return null;
        }
    }

    private Drawable getDrawable(Uri uri) {
        try {
            if (ContentResolver.SCHEME_ANDROID_RESOURCE.equals(uri.getScheme())) {
                ContentResolver.OpenResourceIdResult r = this.mProviderContext.getContentResolver().getResourceId(uri);
                try {
                    return r.r.getDrawable(r.id, this.mProviderContext.getTheme());
                } catch (Resources.NotFoundException e) {
                    throw new FileNotFoundException("Resource does not exist: " + uri);
                }
            } else {
                InputStream stream = this.mProviderContext.getContentResolver().openInputStream(uri);
                if (stream != null) {
                    try {
                        return Drawable.createFromStream(stream, null);
                    } finally {
                        try {
                            stream.close();
                        } catch (IOException ex) {
                            Log.e(LOG_TAG, "Error closing icon stream for " + uri, ex);
                        }
                    }
                } else {
                    throw new FileNotFoundException("Failed to open " + uri);
                }
            }
        } catch (FileNotFoundException fnfe) {
            Log.w(LOG_TAG, "Icon not found: " + uri + ", " + fnfe.getMessage());
            return null;
        }
    }

    private Drawable checkIconCache(String resourceUri) {
        Drawable.ConstantState cached = this.mOutsideDrawablesCache.get(resourceUri);
        if (cached == null) {
            return null;
        }
        return cached.newDrawable();
    }

    private void storeInIconCache(String resourceUri, Drawable drawable) {
        if (drawable != null) {
            this.mOutsideDrawablesCache.put(resourceUri, drawable.getConstantState());
        }
    }

    private Drawable getDefaultIcon1(Cursor cursor) {
        Drawable drawable = getActivityIconWithCache(this.mSearchable.getSearchActivity());
        if (drawable != null) {
            return drawable;
        }
        return this.mContext.getPackageManager().getDefaultActivityIcon();
    }

    private Drawable getActivityIconWithCache(ComponentName component) {
        String componentIconKey = component.flattenToShortString();
        Drawable.ConstantState toCache = null;
        if (this.mOutsideDrawablesCache.containsKey(componentIconKey)) {
            Drawable.ConstantState cached = this.mOutsideDrawablesCache.get(componentIconKey);
            if (cached == null) {
                return null;
            }
            return cached.newDrawable(this.mProviderContext.getResources());
        }
        Drawable drawable = getActivityIcon(component);
        if (drawable != null) {
            toCache = drawable.getConstantState();
        }
        this.mOutsideDrawablesCache.put(componentIconKey, toCache);
        return drawable;
    }

    private Drawable getActivityIcon(ComponentName component) {
        PackageManager pm = this.mContext.getPackageManager();
        try {
            ActivityInfo activityInfo = pm.getActivityInfo(component, 128);
            int iconId = activityInfo.getIconResource();
            if (iconId == 0) {
                return null;
            }
            Drawable drawable = pm.getDrawable(component.getPackageName(), iconId, activityInfo.applicationInfo);
            if (drawable != null) {
                return drawable;
            }
            Log.w(LOG_TAG, "Invalid icon resource " + iconId + " for " + component.flattenToShortString());
            return null;
        } catch (PackageManager.NameNotFoundException ex) {
            Log.w(LOG_TAG, ex.toString());
            return null;
        }
    }

    public static String getColumnString(Cursor cursor, String columnName) {
        return getStringOrNull(cursor, cursor.getColumnIndex(columnName));
    }

    private static String getStringOrNull(Cursor cursor, int col) {
        if (col == -1) {
            return null;
        }
        try {
            return cursor.getString(col);
        } catch (Exception e) {
            Log.e(LOG_TAG, "unexpected error retrieving valid column from cursor, did the remote process die?", e);
            return null;
        }
    }
}
