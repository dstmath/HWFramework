package com.android.server.autofill.ui;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.graphics.Point;
import android.graphics.Rect;
import android.os.Handler;
import android.service.autofill.Dataset;
import android.service.autofill.FillResponse;
import android.text.TextUtils;
import android.util.Slog;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.MeasureSpec;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.WindowManager.BadTokenException;
import android.view.WindowManager.LayoutParams;
import android.view.accessibility.AccessibilityManager;
import android.view.autofill.AutofillId;
import android.view.autofill.AutofillValue;
import android.view.autofill.IAutofillWindowPresenter;
import android.view.autofill.IAutofillWindowPresenter.Stub;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Filter;
import android.widget.Filter.FilterResults;
import android.widget.Filterable;
import android.widget.ListView;
import android.widget.RemoteViews;
import android.widget.RemoteViews.OnClickHandler;
import com.android.server.UiThread;
import com.android.server.autofill.Helper;
import com.android.server.autofill.ui.-$Lambda$DgpgpA67BLtfXEF5MAIi1KCU694.AnonymousClass2;
import com.android.server.autofill.ui.-$Lambda$DgpgpA67BLtfXEF5MAIi1KCU694.AnonymousClass3;
import com.android.server.autofill.ui.-$Lambda$DgpgpA67BLtfXEF5MAIi1KCU694.AnonymousClass4;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import libcore.util.Objects;

final class FillUi {
    private static final String TAG = "FillUi";
    private static final int VISIBLE_OPTIONS_MAX_COUNT = 3;
    private static final TypedValue sTempTypedValue = new TypedValue();
    private final ItemsAdapter mAdapter;
    private AnnounceFilterResult mAnnounceFilterResult;
    private final Callback mCallback;
    private int mContentHeight;
    private int mContentWidth;
    private final Context mContext;
    private boolean mDestroyed;
    private String mFilterText;
    private final ListView mListView;
    private final Point mTempPoint = new Point();
    private final AnchoredWindow mWindow;
    private final AutofillWindowPresenter mWindowPresenter = new AutofillWindowPresenter(this, null);

    interface Callback {
        void onCanceled();

        void onDatasetPicked(Dataset dataset);

        void onDestroy();

        void onResponsePicked(FillResponse fillResponse);

        void requestHideFillUi();

        void requestShowFillUi(int i, int i2, IAutofillWindowPresenter iAutofillWindowPresenter);

        void startIntentSender(IntentSender intentSender);
    }

    final class AnchoredWindow implements OnTouchListener {
        private final View mContentView;
        private final OverlayControl mOverlayControl;
        private boolean mShowing;
        private final WindowManager mWm;

        AnchoredWindow(View contentView, OverlayControl overlayControl) {
            this.mWm = (WindowManager) contentView.getContext().getSystemService(WindowManager.class);
            this.mContentView = contentView;
            this.mOverlayControl = overlayControl;
        }

        public void show(LayoutParams params) {
            if (Helper.sVerbose) {
                Slog.v(FillUi.TAG, "show(): showing=" + this.mShowing + ", params=" + params);
            }
            try {
                if (this.mShowing) {
                    this.mWm.updateViewLayout(this.mContentView, params);
                    return;
                }
                params.accessibilityTitle = this.mContentView.getContext().getString(17039650);
                this.mWm.addView(this.mContentView, params);
                this.mContentView.setOnTouchListener(this);
                this.mOverlayControl.hideOverlays();
                this.mShowing = true;
            } catch (BadTokenException e) {
                if (Helper.sDebug) {
                    Slog.d(FillUi.TAG, "Filed with with token " + params.token + " gone.");
                }
                FillUi.this.mCallback.onDestroy();
            } catch (IllegalStateException e2) {
                Slog.e(FillUi.TAG, "Exception showing window " + params, e2);
                FillUi.this.mCallback.onDestroy();
            }
        }

        /* renamed from: hide */
        void -com_android_server_autofill_ui_FillUi$AutofillWindowPresenter-mthref-0() {
            try {
                if (this.mShowing) {
                    this.mContentView.setOnTouchListener(null);
                    this.mWm.removeView(this.mContentView);
                    this.mShowing = false;
                }
                this.mOverlayControl.showOverlays();
            } catch (IllegalStateException e) {
                Slog.e(FillUi.TAG, "Exception hiding window ", e);
                FillUi.this.mCallback.onDestroy();
                this.mOverlayControl.showOverlays();
            } catch (Throwable th) {
                this.mOverlayControl.showOverlays();
                throw th;
            }
        }

        public boolean onTouch(View view, MotionEvent event) {
            if (view != this.mContentView || event.getAction() != 4) {
                return false;
            }
            FillUi.this.mCallback.onCanceled();
            return true;
        }
    }

    private final class AnnounceFilterResult implements Runnable {
        private static final int SEARCH_RESULT_ANNOUNCEMENT_DELAY = 1000;

        /* synthetic */ AnnounceFilterResult(FillUi this$0, AnnounceFilterResult -this1) {
            this();
        }

        private AnnounceFilterResult() {
        }

        public void post() {
            remove();
            FillUi.this.mListView.postDelayed(this, 1000);
        }

        public void remove() {
            FillUi.this.mListView.removeCallbacks(this);
        }

        public void run() {
            String text;
            int count = FillUi.this.mListView.getAdapter().getCount();
            if (count <= 0) {
                text = FillUi.this.mContext.getString(17039651);
            } else {
                text = FillUi.this.mContext.getResources().getQuantityString(18153472, count, new Object[]{Integer.valueOf(count)});
            }
            FillUi.this.mListView.announceForAccessibility(text);
        }
    }

    private final class AutofillWindowPresenter extends Stub {
        /* synthetic */ AutofillWindowPresenter(FillUi this$0, AutofillWindowPresenter -this1) {
            this();
        }

        private AutofillWindowPresenter() {
        }

        public void show(LayoutParams p, Rect transitionEpicenter, boolean fitsSystemWindows, int layoutDirection) {
            if (Helper.sVerbose) {
                Slog.v(FillUi.TAG, "AutofillWindowPresenter.show(): fit=" + fitsSystemWindows + ", epicenter=" + transitionEpicenter + ", dir=" + layoutDirection + ", params=" + p);
            }
            UiThread.getHandler().post(new AnonymousClass3(this, p));
        }

        /* synthetic */ void lambda$-com_android_server_autofill_ui_FillUi$AutofillWindowPresenter_13158(LayoutParams p) {
            FillUi.this.mWindow.show(p);
        }

        public void hide(Rect transitionEpicenter) {
            Handler handler = UiThread.getHandler();
            AnchoredWindow -get3 = FillUi.this.mWindow;
            -get3.getClass();
            handler.post(new com.android.server.autofill.ui.-$Lambda$DgpgpA67BLtfXEF5MAIi1KCU694.AnonymousClass1(-get3));
        }
    }

    private final class ItemsAdapter extends BaseAdapter implements Filterable {
        private final List<ViewItem> mAllItems;
        private final List<ViewItem> mFilteredItems = new ArrayList();

        ItemsAdapter(List<ViewItem> items) {
            this.mAllItems = Collections.unmodifiableList(new ArrayList(items));
            this.mFilteredItems.addAll(items);
        }

        public Filter getFilter() {
            return new Filter() {
                protected FilterResults performFiltering(CharSequence constraint) {
                    FilterResults results = new FilterResults();
                    if (TextUtils.isEmpty(constraint)) {
                        results.values = ItemsAdapter.this.mAllItems;
                        results.count = ItemsAdapter.this.mAllItems.size();
                        return results;
                    }
                    List<ViewItem> filteredItems = new ArrayList();
                    String constraintLowerCase = constraint.toString().toLowerCase();
                    int itemCount = ItemsAdapter.this.mAllItems.size();
                    for (int i = 0; i < itemCount; i++) {
                        ViewItem item = (ViewItem) ItemsAdapter.this.mAllItems.get(i);
                        String value = item.getValue();
                        if (value == null || value.toLowerCase().startsWith(constraintLowerCase)) {
                            filteredItems.add(item);
                        }
                    }
                    results.values = filteredItems;
                    results.count = filteredItems.size();
                    return results;
                }

                protected void publishResults(CharSequence constraint, FilterResults results) {
                    int oldItemCount = ItemsAdapter.this.mFilteredItems.size();
                    ItemsAdapter.this.mFilteredItems.clear();
                    ItemsAdapter.this.mFilteredItems.addAll(results.values);
                    if (oldItemCount != ItemsAdapter.this.mFilteredItems.size()) {
                        FillUi.this.announceSearchResultIfNeeded();
                    }
                    ItemsAdapter.this.notifyDataSetChanged();
                }
            };
        }

        public int getCount() {
            return this.mFilteredItems.size();
        }

        public ViewItem getItem(int position) {
            return (ViewItem) this.mFilteredItems.get(position);
        }

        public long getItemId(int position) {
            return (long) position;
        }

        public View getView(int position, View convertView, ViewGroup parent) {
            return getItem(position).getView();
        }
    }

    private static class ViewItem {
        private final Dataset mDataset;
        private final String mValue;
        private final View mView;

        ViewItem(Dataset dataset, String value, View view) {
            this.mDataset = dataset;
            this.mValue = value;
            this.mView = view;
        }

        public View getView() {
            return this.mView;
        }

        public Dataset getDataset() {
            return this.mDataset;
        }

        public String getValue() {
            return this.mValue;
        }

        public String toString() {
            return this.mValue;
        }
    }

    FillUi(Context context, FillResponse response, AutofillId focusedViewId, String filterText, OverlayControl overlayControl, Callback callback) {
        this.mContext = context;
        this.mCallback = callback;
        ViewGroup decor = (ViewGroup) LayoutInflater.from(context).inflate(17367100, null);
        OnClickHandler interceptionHandler = new OnClickHandler() {
            public boolean onClickHandler(View view, PendingIntent pendingIntent, Intent fillInIntent) {
                if (pendingIntent != null) {
                    FillUi.this.mCallback.startIntentSender(pendingIntent.getIntentSender());
                }
                return true;
            }
        };
        if (response.getAuthentication() != null) {
            this.mListView = null;
            this.mAdapter = null;
            try {
                View content = response.getPresentation().apply(context, decor, interceptionHandler);
                decor.addView(content);
                Point maxSize = this.mTempPoint;
                resolveMaxWindowSize(context, maxSize);
                decor.measure(MeasureSpec.makeMeasureSpec(maxSize.x, Integer.MIN_VALUE), MeasureSpec.makeMeasureSpec(maxSize.y, Integer.MIN_VALUE));
                decor.setOnClickListener(new AnonymousClass2(this, response));
                this.mContentWidth = content.getMeasuredWidth();
                this.mContentHeight = content.getMeasuredHeight();
                this.mWindow = new AnchoredWindow(decor, overlayControl);
                this.mCallback.requestShowFillUi(this.mContentWidth, this.mContentHeight, this.mWindowPresenter);
            } catch (RuntimeException e) {
                callback.onCanceled();
                Slog.e(TAG, "Error inflating remote views", e);
                this.mWindow = null;
                return;
            }
        }
        int datasetCount = response.getDatasets().size();
        ArrayList<ViewItem> items = new ArrayList(datasetCount);
        for (int i = 0; i < datasetCount; i++) {
            Dataset dataset = (Dataset) response.getDatasets().get(i);
            int index = dataset.getFieldIds().indexOf(focusedViewId);
            if (index >= 0) {
                RemoteViews presentation = dataset.getFieldPresentation(index);
                try {
                    if (Helper.sVerbose) {
                        Slog.v(TAG, "setting remote view for " + focusedViewId);
                    }
                    View view = presentation.apply(context, null, interceptionHandler);
                    AutofillValue value = (AutofillValue) dataset.getFieldValues().get(index);
                    String valueText = null;
                    if (value != null && value.isText() && dataset.getAuthentication() == null) {
                        valueText = value.getTextValue().toString().toLowerCase();
                    }
                    items.add(new ViewItem(dataset, valueText, view));
                } catch (RuntimeException e2) {
                    Slog.e(TAG, "Error inflating remote views", e2);
                }
            }
        }
        this.mAdapter = new ItemsAdapter(items);
        this.mListView = (ListView) decor.findViewById(16908736);
        this.mListView.setAdapter(this.mAdapter);
        this.mListView.setVisibility(0);
        this.mListView.setOnItemClickListener(new -$Lambda$DgpgpA67BLtfXEF5MAIi1KCU694(this));
        if (filterText == null) {
            this.mFilterText = null;
        } else {
            this.mFilterText = filterText.toLowerCase();
        }
        applyNewFilterText();
        this.mWindow = new AnchoredWindow(decor, overlayControl);
    }

    /* synthetic */ void lambda$-com_android_server_autofill_ui_FillUi_5115(FillResponse response, View v) {
        this.mCallback.onResponsePicked(response);
    }

    /* synthetic */ void lambda$-com_android_server_autofill_ui_FillUi_7197(AdapterView adapterView, View view, int position, long id) {
        this.mCallback.onDatasetPicked(this.mAdapter.getItem(position).getDataset());
    }

    private void applyNewFilterText() {
        this.mAdapter.getFilter().filter(this.mFilterText, new AnonymousClass4(this.mAdapter.getCount(), this));
    }

    /* synthetic */ void lambda$-com_android_server_autofill_ui_FillUi_7794(int oldCount, int count) {
        if (!this.mDestroyed) {
            if (count <= 0) {
                if (Helper.sDebug) {
                    Slog.d(TAG, "No dataset matches filter: " + this.mFilterText);
                }
                this.mCallback.requestHideFillUi();
            } else {
                if (updateContentSize()) {
                    this.mCallback.requestShowFillUi(this.mContentWidth, this.mContentHeight, this.mWindowPresenter);
                }
                if (this.mAdapter.getCount() > 3) {
                    this.mListView.setVerticalScrollBarEnabled(true);
                    this.mListView.onVisibilityAggregated(true);
                } else {
                    this.mListView.setVerticalScrollBarEnabled(false);
                }
                if (this.mAdapter.getCount() != oldCount) {
                    this.mListView.requestLayout();
                }
            }
        }
    }

    public void setFilterText(String filterText) {
        throwIfDestroyed();
        if (this.mAdapter != null) {
            if (filterText == null) {
                filterText = null;
            } else {
                filterText = filterText.toLowerCase();
            }
            if (!Objects.equal(this.mFilterText, filterText)) {
                this.mFilterText = filterText;
                applyNewFilterText();
            }
        }
    }

    public void destroy() {
        throwIfDestroyed();
        this.mCallback.onDestroy();
        this.mCallback.requestHideFillUi();
        this.mDestroyed = true;
    }

    private boolean updateContentSize() {
        if (this.mAdapter == null) {
            return false;
        }
        boolean changed = false;
        if (this.mAdapter.getCount() <= 0) {
            if (this.mContentWidth != 0) {
                this.mContentWidth = 0;
                changed = true;
            }
            if (this.mContentHeight != 0) {
                this.mContentHeight = 0;
                changed = true;
            }
            return changed;
        }
        Point maxSize = this.mTempPoint;
        resolveMaxWindowSize(this.mContext, maxSize);
        this.mContentWidth = 0;
        this.mContentHeight = 0;
        int widthMeasureSpec = MeasureSpec.makeMeasureSpec(maxSize.x, Integer.MIN_VALUE);
        int heightMeasureSpec = MeasureSpec.makeMeasureSpec(maxSize.y, Integer.MIN_VALUE);
        int itemCount = this.mAdapter.getCount();
        for (int i = 0; i < itemCount; i++) {
            View view = this.mAdapter.getItem(i).getView();
            view.measure(widthMeasureSpec, heightMeasureSpec);
            int newContentWidth = Math.max(this.mContentWidth, Math.min(view.getMeasuredWidth(), maxSize.x));
            if (newContentWidth != this.mContentWidth) {
                this.mContentWidth = newContentWidth;
                changed = true;
            }
            if (i < 3) {
                int newContentHeight = this.mContentHeight + Math.min(view.getMeasuredHeight(), maxSize.y);
                if (newContentHeight != this.mContentHeight) {
                    this.mContentHeight = newContentHeight;
                    changed = true;
                }
            }
        }
        return changed;
    }

    private void throwIfDestroyed() {
        if (this.mDestroyed) {
            throw new IllegalStateException("cannot interact with a destroyed instance");
        }
    }

    private static void resolveMaxWindowSize(Context context, Point outPoint) {
        context.getDisplay().getSize(outPoint);
        TypedValue typedValue = sTempTypedValue;
        context.getTheme().resolveAttribute(17891344, typedValue, true);
        outPoint.x = (int) typedValue.getFraction((float) outPoint.x, (float) outPoint.x);
        context.getTheme().resolveAttribute(17891343, typedValue, true);
        outPoint.y = (int) typedValue.getFraction((float) outPoint.y, (float) outPoint.y);
    }

    public void dump(PrintWriter pw, String prefix) {
        boolean z;
        pw.print(prefix);
        pw.print("mCallback: ");
        pw.println(this.mCallback != null);
        pw.print(prefix);
        pw.print("mListView: ");
        pw.println(this.mListView);
        pw.print(prefix);
        pw.print("mAdapter: ");
        if (this.mAdapter != null) {
            z = true;
        } else {
            z = false;
        }
        pw.println(z);
        pw.print(prefix);
        pw.print("mFilterText: ");
        pw.println(this.mFilterText);
        pw.print(prefix);
        pw.print("mContentWidth: ");
        pw.println(this.mContentWidth);
        pw.print(prefix);
        pw.print("mContentHeight: ");
        pw.println(this.mContentHeight);
        pw.print(prefix);
        pw.print("mDestroyed: ");
        pw.println(this.mDestroyed);
        pw.print(prefix);
        pw.print("mWindow: ");
        if (this.mWindow == null) {
            pw.println("N/A");
            return;
        }
        String prefix2 = prefix + "  ";
        pw.println();
        pw.print(prefix2);
        pw.print("showing: ");
        pw.println(this.mWindow.mShowing);
        pw.print(prefix2);
        pw.print("view: ");
        pw.println(this.mWindow.mContentView);
        pw.print(prefix2);
        pw.print("screen coordinates: ");
        if (this.mWindow.mContentView == null) {
            pw.println("N/A");
            return;
        }
        int[] coordinates = this.mWindow.mContentView.getLocationOnScreen();
        pw.print(coordinates[0]);
        pw.print("x");
        pw.println(coordinates[1]);
    }

    private void announceSearchResultIfNeeded() {
        if (AccessibilityManager.getInstance(this.mContext).isEnabled()) {
            if (this.mAnnounceFilterResult == null) {
                this.mAnnounceFilterResult = new AnnounceFilterResult(this, null);
            }
            this.mAnnounceFilterResult.post();
        }
    }
}
