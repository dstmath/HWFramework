package com.android.server.autofill.ui;

import android.app.PendingIntent;
import android.common.HwFrameworkFactory;
import android.content.Context;
import android.content.IntentSender;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.hardware.biometrics.face.V1_0.FaceAcquiredInfo;
import android.os.Handler;
import android.service.autofill.Dataset;
import android.service.autofill.FillResponse;
import android.text.TextUtils;
import android.util.Slog;
import android.util.TypedValue;
import android.view.ContextThemeWrapper;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.accessibility.AccessibilityManager;
import android.view.autofill.AutofillId;
import android.view.autofill.AutofillValue;
import android.view.autofill.IAutofillWindowPresenter;
import android.view.autofill.IHwAutofillHelper;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.RemoteViews;
import android.widget.TextView;
import com.android.server.UiThread;
import com.android.server.autofill.AutofillManagerService;
import com.android.server.autofill.Helper;
import com.android.server.autofill.ui.FillUi;
import com.android.server.pm.PackageManagerService;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/* access modifiers changed from: package-private */
public final class FillUi {
    private static final String TAG = "FillUi";
    private static final int THEME_ID_DARK = 16974813;
    private static final int THEME_ID_LIGHT = 16974822;
    private static boolean sIsHwAutofillService = false;
    private static final TypedValue sTempTypedValue = new TypedValue();
    private final ItemsAdapter mAdapter;
    private AnnounceFilterResult mAnnounceFilterResult;
    private final Callback mCallback;
    private int mContentHeight;
    private int mContentWidth;
    private final Context mContext;
    private int mDecorPaddingHeight;
    private int mDecorPaddingWidth;
    private boolean mDestroyed;
    private String mFilterText;
    private final View mFooter;
    private final boolean mFullScreen;
    private final View mHeader;
    private IHwAutofillHelper mHwAutofillHelper = HwFrameworkFactory.getHwAutofillHelper();
    private final ListView mListView;
    private final Point mTempPoint = new Point();
    private int mThemeId;
    private final int mVisibleDatasetsMaxCount;
    private final AnchoredWindow mWindow;
    private final AutofillWindowPresenter mWindowPresenter = new AutofillWindowPresenter();

    /* access modifiers changed from: package-private */
    public interface Callback {
        void dispatchUnhandledKey(KeyEvent keyEvent);

        void onCanceled();

        void onDatasetPicked(Dataset dataset);

        void onDestroy();

        void onResponsePicked(FillResponse fillResponse);

        void requestHideFillUi();

        void requestShowFillUi(int i, int i2, IAutofillWindowPresenter iAutofillWindowPresenter);

        void startIntentSender(IntentSender intentSender);
    }

    public static boolean isFullScreen(Context context) {
        if (Helper.sFullScreenMode == null) {
            return context.getPackageManager().hasSystemFeature("android.software.leanback");
        }
        if (Helper.sVerbose) {
            Slog.v(TAG, "forcing full-screen mode to " + Helper.sFullScreenMode);
        }
        return Helper.sFullScreenMode.booleanValue();
    }

    /* JADX INFO: Multiple debug info for r5v10 'filterPattern'  java.util.regex.Pattern: [D('presentation' android.widget.RemoteViews), D('filterPattern' java.util.regex.Pattern)] */
    FillUi(Context context, FillResponse response, AutofillId focusedViewId, String filterText, OverlayControl overlayControl, CharSequence serviceLabel, Drawable serviceIcon, boolean nightMode, Callback callback) {
        ViewGroup decor;
        RemoteViews.OnClickHandler footerContainer;
        RemoteViews footerPresentation;
        RemoteViews headerPresentation;
        RemoteViews.OnClickHandler clickBlocker;
        RuntimeException e;
        Pattern filterPattern;
        boolean filterable;
        int i;
        if (Helper.sVerbose) {
            Slog.v(TAG, "nightMode: " + nightMode);
        }
        this.mThemeId = nightMode ? THEME_ID_DARK : THEME_ID_LIGHT;
        int themeID = context.getResources().getIdentifier("androidhwext:style/Theme.Emui", null, null);
        if (themeID != 0) {
            this.mThemeId = themeID;
        }
        this.mCallback = callback;
        this.mFullScreen = isFullScreen(context);
        this.mContext = new ContextThemeWrapper(context, this.mThemeId);
        LayoutInflater inflater = LayoutInflater.from(this.mContext);
        RemoteViews headerPresentation2 = response.getHeader();
        RemoteViews footerPresentation2 = response.getFooter();
        if (this.mFullScreen) {
            decor = (ViewGroup) inflater.inflate(17367101, (ViewGroup) null);
        } else if (headerPresentation2 == null && footerPresentation2 == null) {
            decor = (ViewGroup) inflater.inflate(17367100, (ViewGroup) null);
        } else {
            decor = (ViewGroup) inflater.inflate(17367102, (ViewGroup) null);
        }
        decor.setClipToOutline(true);
        TextView titleView = (TextView) decor.findViewById(16908783);
        if (titleView != null) {
            titleView.setText(this.mContext.getString(17039696, serviceLabel));
        }
        ImageView iconView = (ImageView) decor.findViewById(16908780);
        if (iconView != null) {
            iconView.setImageDrawable(serviceIcon);
        }
        if (this.mFullScreen) {
            Point outPoint = this.mTempPoint;
            this.mContext.getDisplay().getSize(outPoint);
            this.mContentWidth = -1;
            this.mContentHeight = outPoint.y / 2;
            if (Helper.sVerbose) {
                Slog.v(TAG, "initialized fillscreen LayoutParams " + this.mContentWidth + "," + this.mContentHeight);
            }
        }
        decor.addOnUnhandledKeyEventListener(new View.OnUnhandledKeyEventListener() {
            /* class com.android.server.autofill.ui.$$Lambda$FillUi$FY016gv4LQ5AA6yOkKTH3EM5zaM */

            @Override // android.view.View.OnUnhandledKeyEventListener
            public final boolean onUnhandledKeyEvent(View view, KeyEvent keyEvent) {
                return FillUi.this.lambda$new$0$FillUi(view, keyEvent);
            }
        });
        if (AutofillManagerService.getVisibleDatasetsMaxCount() > 0) {
            this.mVisibleDatasetsMaxCount = AutofillManagerService.getVisibleDatasetsMaxCount();
            if (Helper.sVerbose) {
                Slog.v(TAG, "overriding maximum visible datasets to " + this.mVisibleDatasetsMaxCount);
            }
        } else {
            this.mVisibleDatasetsMaxCount = this.mContext.getResources().getInteger(17694724);
        }
        RemoteViews.OnClickHandler interceptionHandler = new RemoteViews.OnClickHandler() {
            /* class com.android.server.autofill.ui.$$Lambda$FillUi$QXIyKJs3cMApGd5ifauQkxdpdqk */

            public final boolean onClickHandler(View view, PendingIntent pendingIntent, RemoteViews.RemoteResponse remoteResponse) {
                return FillUi.this.lambda$new$1$FillUi(view, pendingIntent, remoteResponse);
            }
        };
        if (response.getAuthentication() != null) {
            this.mHeader = null;
            this.mListView = null;
            this.mFooter = null;
            this.mAdapter = null;
            ViewGroup container = (ViewGroup) decor.findViewById(16908782);
            try {
                View content = response.getPresentation().applyWithTheme(this.mContext, decor, interceptionHandler, this.mThemeId);
                container.addView(content);
                container.setFocusable(true);
                container.setOnClickListener(new View.OnClickListener(response) {
                    /* class com.android.server.autofill.ui.$$Lambda$FillUi$h0jT24YuSGGDnoZ6Tf22n1QRkO8 */
                    private final /* synthetic */ FillResponse f$1;

                    {
                        this.f$1 = r2;
                    }

                    @Override // android.view.View.OnClickListener
                    public final void onClick(View view) {
                        FillUi.this.lambda$new$2$FillUi(this.f$1, view);
                    }
                });
                if (!this.mFullScreen) {
                    Point maxSize = this.mTempPoint;
                    resolveMaxWindowSize(this.mContext, maxSize);
                    ViewGroup.LayoutParams layoutParams = content.getLayoutParams();
                    if (this.mFullScreen) {
                        i = maxSize.x;
                    } else {
                        i = -2;
                    }
                    layoutParams.width = i;
                    content.getLayoutParams().height = -2;
                    decor.measure(View.MeasureSpec.makeMeasureSpec(maxSize.x, Integer.MIN_VALUE), View.MeasureSpec.makeMeasureSpec(maxSize.y, Integer.MIN_VALUE));
                    this.mContentWidth = content.getMeasuredWidth();
                    this.mContentHeight = content.getMeasuredHeight();
                }
                this.mWindow = new AnchoredWindow(decor, overlayControl);
                requestShowFillUi();
            } catch (RuntimeException e2) {
                callback.onCanceled();
                Slog.e(TAG, "Error inflating remote views", e2);
                this.mWindow = null;
            }
        } else {
            int datasetCount = response.getDatasets().size();
            if (Helper.sVerbose) {
                Slog.v(TAG, "Number datasets: " + datasetCount + " max visible: " + this.mVisibleDatasetsMaxCount);
            }
            RemoteViews.OnClickHandler clickBlocker2 = null;
            if (headerPresentation2 != null) {
                clickBlocker2 = newClickBlocker();
                this.mHeader = headerPresentation2.applyWithTheme(this.mContext, null, clickBlocker2, this.mThemeId);
                LinearLayout headerContainer = (LinearLayout) decor.findViewById(16908779);
                if (Helper.sVerbose) {
                    Slog.v(TAG, "adding header");
                }
                headerContainer.addView(this.mHeader);
                headerContainer.setVisibility(0);
            } else {
                this.mHeader = null;
            }
            if (footerPresentation2 != null) {
                LinearLayout footerContainer2 = (LinearLayout) decor.findViewById(16908778);
                if (footerContainer2 != null) {
                    clickBlocker2 = clickBlocker2 == null ? newClickBlocker() : clickBlocker2;
                    this.mFooter = footerPresentation2.applyWithTheme(this.mContext, null, clickBlocker2, this.mThemeId);
                    if (Helper.sVerbose) {
                        Slog.v(TAG, "adding footer");
                    }
                    footerContainer2.addView(this.mFooter);
                    footerContainer2.setVisibility(0);
                } else {
                    this.mFooter = null;
                }
                footerContainer = clickBlocker2;
            } else {
                this.mFooter = null;
                footerContainer = clickBlocker2;
            }
            RemoteViews.OnClickHandler clickBlocker3 = this.mHwAutofillHelper;
            sIsHwAutofillService = clickBlocker3 != null && clickBlocker3.isHwAutofillService(this.mContext);
            ArrayList<ViewItem> items = new ArrayList<>(datasetCount);
            int i2 = 0;
            while (i2 < datasetCount) {
                Dataset dataset = (Dataset) response.getDatasets().get(i2);
                int index = dataset.getFieldIds().indexOf(focusedViewId);
                if (index >= 0) {
                    clickBlocker = footerContainer;
                    RemoteViews presentation = dataset.getFieldPresentation(index);
                    if (presentation == null) {
                        StringBuilder sb = new StringBuilder();
                        headerPresentation = headerPresentation2;
                        sb.append("not displaying UI on field ");
                        sb.append(focusedViewId);
                        sb.append(" because service didn't provide a presentation for it on ");
                        sb.append(dataset);
                        Slog.w(TAG, sb.toString());
                        footerPresentation = footerPresentation2;
                    } else {
                        headerPresentation = headerPresentation2;
                        try {
                            if (Helper.sVerbose) {
                                try {
                                    Slog.v(TAG, "setting remote view for " + focusedViewId);
                                } catch (RuntimeException e3) {
                                    e = e3;
                                    footerPresentation = footerPresentation2;
                                }
                            }
                            footerPresentation = footerPresentation2;
                            try {
                                View view = presentation.applyWithTheme(this.mContext, null, interceptionHandler, this.mThemeId);
                                Dataset.DatasetFieldFilter filter = dataset.getFilter(index);
                                String valueText = null;
                                if (filter == null) {
                                    AutofillValue value = (AutofillValue) dataset.getFieldValues().get(index);
                                    if (value != null && value.isText()) {
                                        valueText = value.getTextValue().toString().toLowerCase();
                                    }
                                    filterPattern = null;
                                    filterable = true;
                                } else {
                                    filterPattern = filter.pattern;
                                    if (filterPattern == null) {
                                        if (Helper.sVerbose) {
                                            Slog.v(TAG, "Explicitly disabling filter at id " + focusedViewId + " for dataset #" + index);
                                        }
                                        filterable = false;
                                    } else {
                                        filterable = true;
                                    }
                                }
                                items.add(new ViewItem(dataset, filterPattern, filterable, valueText, view));
                            } catch (RuntimeException e4) {
                                e = e4;
                                Slog.e(TAG, "Error inflating remote views", e);
                                i2++;
                                datasetCount = datasetCount;
                                footerContainer = clickBlocker;
                                headerPresentation2 = headerPresentation;
                                footerPresentation2 = footerPresentation;
                            }
                        } catch (RuntimeException e5) {
                            e = e5;
                            footerPresentation = footerPresentation2;
                            Slog.e(TAG, "Error inflating remote views", e);
                            i2++;
                            datasetCount = datasetCount;
                            footerContainer = clickBlocker;
                            headerPresentation2 = headerPresentation;
                            footerPresentation2 = footerPresentation;
                        }
                    }
                } else {
                    clickBlocker = footerContainer;
                    headerPresentation = headerPresentation2;
                    footerPresentation = footerPresentation2;
                }
                i2++;
                datasetCount = datasetCount;
                footerContainer = clickBlocker;
                headerPresentation2 = headerPresentation;
                footerPresentation2 = footerPresentation;
            }
            this.mAdapter = new ItemsAdapter(items);
            this.mListView = (ListView) decor.findViewById(16908781);
            this.mListView.setAdapter((ListAdapter) this.mAdapter);
            this.mListView.setVisibility(0);
            this.mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                /* class com.android.server.autofill.ui.$$Lambda$FillUi$TUHYXtyYjvn8kBKxh1vyXjC9x84 */

                @Override // android.widget.AdapterView.OnItemClickListener
                public final void onItemClick(AdapterView adapterView, View view, int i, long j) {
                    FillUi.this.lambda$new$3$FillUi(adapterView, view, i, j);
                }
            });
            IHwAutofillHelper iHwAutofillHelper = this.mHwAutofillHelper;
            if (iHwAutofillHelper != null && iHwAutofillHelper.isHwAutofillService(this.mContext)) {
                FrameLayout layout = (FrameLayout) decor.findViewById(16908782);
                layout.setBackgroundColor(0);
                layout.setBackgroundResource(33751603);
                this.mListView.setDivider(null);
                this.mDecorPaddingWidth = layout.getPaddingLeft() + layout.getPaddingRight();
                this.mDecorPaddingHeight = layout.getPaddingTop() + layout.getPaddingBottom();
            }
            if (filterText == null) {
                this.mFilterText = null;
            } else {
                this.mFilterText = filterText.toLowerCase();
            }
            applyNewFilterText();
            this.mWindow = new AnchoredWindow(decor, overlayControl);
        }
    }

    public /* synthetic */ boolean lambda$new$0$FillUi(View view, KeyEvent event) {
        int keyCode = event.getKeyCode();
        if (keyCode == 4 || keyCode == 66 || keyCode == 111) {
            return false;
        }
        switch (keyCode) {
            case FaceAcquiredInfo.FACE_OBSCURED /* 19 */:
            case 20:
            case 21:
            case FaceAcquiredInfo.VENDOR /* 22 */:
            case 23:
                return false;
            default:
                this.mCallback.dispatchUnhandledKey(event);
                return true;
        }
    }

    public /* synthetic */ boolean lambda$new$1$FillUi(View view, PendingIntent pendingIntent, RemoteViews.RemoteResponse r) {
        if (pendingIntent == null) {
            return true;
        }
        this.mCallback.startIntentSender(pendingIntent.getIntentSender());
        return true;
    }

    public /* synthetic */ void lambda$new$2$FillUi(FillResponse response, View v) {
        this.mCallback.onResponsePicked(response);
    }

    public /* synthetic */ void lambda$new$3$FillUi(AdapterView adapter, View view, int position, long id) {
        this.mCallback.onDatasetPicked(this.mAdapter.getItem(position).dataset);
    }

    /* access modifiers changed from: package-private */
    public void requestShowFillUi() {
        this.mCallback.requestShowFillUi(this.mContentWidth, this.mContentHeight, this.mWindowPresenter);
    }

    private RemoteViews.OnClickHandler newClickBlocker() {
        return $$Lambda$FillUi$9_pWdpF2p3GasqWcPNtfp8BoGZs.INSTANCE;
    }

    static /* synthetic */ boolean lambda$newClickBlocker$4(View view, PendingIntent pendingIntent, RemoteViews.RemoteResponse response) {
        if (!Helper.sVerbose) {
            return true;
        }
        Slog.v(TAG, "Ignoring click on " + view);
        return true;
    }

    private void applyNewFilterText() {
        this.mAdapter.getFilter().filter(this.mFilterText, new Filter.FilterListener(this.mAdapter.getCount()) {
            /* class com.android.server.autofill.ui.$$Lambda$FillUi$FzsKmFVepz197dqO8bth9Hmkl4 */
            private final /* synthetic */ int f$1;

            {
                this.f$1 = r2;
            }

            @Override // android.widget.Filter.FilterListener
            public final void onFilterComplete(int i) {
                FillUi.this.lambda$applyNewFilterText$5$FillUi(this.f$1, i);
            }
        });
    }

    public /* synthetic */ void lambda$applyNewFilterText$5$FillUi(int oldCount, int count) {
        if (!this.mDestroyed) {
            int size = 0;
            if (count <= 0) {
                if (Helper.sDebug) {
                    String str = this.mFilterText;
                    if (str != null) {
                        size = str.length();
                    }
                    Slog.d(TAG, "No dataset matches filter with " + size + " chars");
                }
                this.mCallback.requestHideFillUi();
                return;
            }
            if (updateContentSize()) {
                requestShowFillUi();
            }
            if (this.mAdapter.getCount() > this.mVisibleDatasetsMaxCount) {
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

    public void setFilterText(String filterText) {
        String filterText2;
        throwIfDestroyed();
        if (this.mAdapter != null) {
            if (filterText == null) {
                filterText2 = null;
            } else {
                filterText2 = filterText.toLowerCase();
            }
            if (!Objects.equals(this.mFilterText, filterText2)) {
                this.mFilterText = filterText2;
                applyNewFilterText();
            }
        } else if (TextUtils.isEmpty(filterText)) {
            requestShowFillUi();
        } else {
            this.mCallback.requestHideFillUi();
        }
    }

    public void destroy(boolean notifyClient) {
        throwIfDestroyed();
        AnchoredWindow anchoredWindow = this.mWindow;
        if (anchoredWindow != null) {
            anchoredWindow.hide(false);
        }
        this.mCallback.onDestroy();
        if (notifyClient) {
            this.mCallback.requestHideFillUi();
        }
        this.mDestroyed = true;
    }

    private boolean updateContentSize() {
        ItemsAdapter itemsAdapter = this.mAdapter;
        if (itemsAdapter == null) {
            return false;
        }
        if (this.mFullScreen) {
            return true;
        }
        boolean changed = false;
        if (itemsAdapter.getCount() <= 0) {
            if (this.mContentWidth != 0) {
                this.mContentWidth = 0;
                changed = true;
            }
            if (this.mContentHeight == 0) {
                return changed;
            }
            this.mContentHeight = 0;
            return true;
        }
        Point maxSize = this.mTempPoint;
        resolveMaxWindowSize(this.mContext, maxSize);
        this.mContentWidth = 0;
        this.mContentHeight = 0;
        int widthMeasureSpec = View.MeasureSpec.makeMeasureSpec(maxSize.x, Integer.MIN_VALUE);
        int heightMeasureSpec = View.MeasureSpec.makeMeasureSpec(maxSize.y, Integer.MIN_VALUE);
        int itemCount = this.mAdapter.getCount();
        View view = this.mHeader;
        if (view != null) {
            view.measure(widthMeasureSpec, heightMeasureSpec);
            changed = false | updateWidth(this.mHeader, maxSize) | updateHeight(this.mHeader, maxSize);
        }
        for (int i = 0; i < itemCount; i++) {
            View view2 = this.mAdapter.getItem(i).view;
            view2.measure(widthMeasureSpec, heightMeasureSpec);
            changed |= updateWidth(view2, maxSize);
            if (i < this.mVisibleDatasetsMaxCount) {
                changed |= updateHeight(view2, maxSize);
            }
        }
        View view3 = this.mFooter;
        if (view3 == null) {
            return changed;
        }
        view3.measure(widthMeasureSpec, heightMeasureSpec);
        return changed | updateWidth(this.mFooter, maxSize) | updateHeight(this.mFooter, maxSize);
    }

    private boolean updateWidth(View view, Point maxSize) {
        int newContentWidth = Math.max(this.mContentWidth, Math.min(view.getMeasuredWidth(), maxSize.x));
        if (newContentWidth == this.mContentWidth) {
            return false;
        }
        this.mContentWidth = newContentWidth;
        return true;
    }

    private boolean updateHeight(View view, Point maxSize) {
        int clampedMeasuredHeight = Math.min(view.getMeasuredHeight(), maxSize.y);
        int i = this.mContentHeight;
        int newContentHeight = i + clampedMeasuredHeight;
        if (newContentHeight == i) {
            return false;
        }
        this.mContentHeight = newContentHeight;
        return true;
    }

    private void throwIfDestroyed() {
        if (this.mDestroyed) {
            throw new IllegalStateException("cannot interact with a destroyed instance");
        }
    }

    private static void resolveMaxWindowSize(Context context, Point outPoint) {
        context.getDisplay().getSize(outPoint);
        TypedValue typedValue = sTempTypedValue;
        context.getTheme().resolveAttribute(17956884, typedValue, true);
        outPoint.x = (int) typedValue.getFraction((float) outPoint.x, (float) outPoint.x);
        context.getTheme().resolveAttribute(17956883, typedValue, true);
        outPoint.y = (int) typedValue.getFraction((float) outPoint.y, (float) outPoint.y);
    }

    /* access modifiers changed from: private */
    public static class ViewItem {
        public final Dataset dataset;
        public final Pattern filter;
        public final boolean filterable;
        public final String value;
        public final View view;

        ViewItem(Dataset dataset2, Pattern filter2, boolean filterable2, String value2, View view2) {
            this.dataset = dataset2;
            this.value = value2;
            this.view = view2;
            this.filter = filter2;
            this.filterable = filterable2;
        }

        public boolean matches(CharSequence filterText) {
            if (TextUtils.isEmpty(filterText)) {
                return true;
            }
            if (!this.filterable) {
                return false;
            }
            String constraintLowerCase = filterText.toString().toLowerCase();
            Pattern pattern = this.filter;
            if (pattern != null) {
                return pattern.matcher(constraintLowerCase).matches();
            }
            if (this.value == null) {
                if (this.dataset.getAuthentication() == null) {
                    return true;
                }
                return false;
            } else if (FillUi.sIsHwAutofillService) {
                return this.value.toLowerCase().contains(constraintLowerCase);
            } else {
                return this.value.toLowerCase().startsWith(constraintLowerCase);
            }
        }

        public String toString() {
            StringBuilder builder = new StringBuilder("ViewItem:[view=").append(this.view.getAutofillId());
            Dataset dataset2 = this.dataset;
            String datasetId = dataset2 == null ? null : dataset2.getId();
            if (datasetId != null) {
                builder.append(", dataset=");
                builder.append(datasetId);
            }
            if (this.value != null) {
                builder.append(", value=");
                builder.append(this.value.length());
                builder.append("_chars");
            }
            if (this.filterable) {
                builder.append(", filterable");
            }
            if (this.filter != null) {
                builder.append(", filter=");
                builder.append(this.filter.pattern().length());
                builder.append("_chars");
            }
            builder.append(']');
            return builder.toString();
        }
    }

    /* access modifiers changed from: private */
    public final class AutofillWindowPresenter extends IAutofillWindowPresenter.Stub {
        private AutofillWindowPresenter() {
        }

        public void show(WindowManager.LayoutParams p, Rect transitionEpicenter, boolean fitsSystemWindows, int layoutDirection) {
            if (Helper.sVerbose) {
                Slog.v(FillUi.TAG, "AutofillWindowPresenter.show(): fit=" + fitsSystemWindows + ", params=" + Helper.paramsToString(p));
            }
            if (FillUi.this.mHwAutofillHelper != null && FillUi.this.mHwAutofillHelper.isHwAutofillService(FillUi.this.mContext)) {
                p.width += FillUi.this.mDecorPaddingWidth;
                p.height += FillUi.this.mDecorPaddingHeight;
            }
            UiThread.getHandler().post(new Runnable(p) {
                /* class com.android.server.autofill.ui.$$Lambda$FillUi$AutofillWindowPresenter$N4xQe2B0oe5MBiqZlsy3Lb7vZTg */
                private final /* synthetic */ WindowManager.LayoutParams f$1;

                {
                    this.f$1 = r2;
                }

                @Override // java.lang.Runnable
                public final void run() {
                    FillUi.AutofillWindowPresenter.this.lambda$show$0$FillUi$AutofillWindowPresenter(this.f$1);
                }
            });
        }

        public /* synthetic */ void lambda$show$0$FillUi$AutofillWindowPresenter(WindowManager.LayoutParams p) {
            FillUi.this.mWindow.show(p);
        }

        public void hide(Rect transitionEpicenter) {
            Handler handler = UiThread.getHandler();
            AnchoredWindow anchoredWindow = FillUi.this.mWindow;
            Objects.requireNonNull(anchoredWindow);
            handler.post(new Runnable() {
                /* class com.android.server.autofill.ui.$$Lambda$E4J3bUcyqJNd4ZlExSBhwy8Tx4 */

                @Override // java.lang.Runnable
                public final void run() {
                    FillUi.AnchoredWindow.this.hide();
                }
            });
        }
    }

    /* access modifiers changed from: package-private */
    public final class AnchoredWindow {
        private final View mContentView;
        private final OverlayControl mOverlayControl;
        private WindowManager.LayoutParams mShowParams;
        private boolean mShowing;
        private final WindowManager mWm;

        AnchoredWindow(View contentView, OverlayControl overlayControl) {
            this.mWm = (WindowManager) contentView.getContext().getSystemService(WindowManager.class);
            this.mContentView = contentView;
            this.mOverlayControl = overlayControl;
        }

        public void show(WindowManager.LayoutParams params) {
            this.mShowParams = params;
            if (Helper.sVerbose) {
                Slog.v(FillUi.TAG, "show(): showing=" + this.mShowing + ", params=" + Helper.paramsToString(params));
            }
            try {
                params.packageName = PackageManagerService.PLATFORM_PACKAGE_NAME;
                params.setTitle("Autofill UI");
                if (!this.mShowing) {
                    params.accessibilityTitle = this.mContentView.getContext().getString(17039668);
                    this.mWm.addView(this.mContentView, params);
                    this.mOverlayControl.hideOverlays();
                    this.mShowing = true;
                    return;
                }
                this.mWm.updateViewLayout(this.mContentView, params);
            } catch (WindowManager.BadTokenException e) {
                if (Helper.sDebug) {
                    Slog.d(FillUi.TAG, "Filed with with token " + params.token + " gone.");
                }
                FillUi.this.mCallback.onDestroy();
            } catch (IllegalStateException e2) {
                Slog.wtf(FillUi.TAG, "Exception showing window " + params, e2);
                FillUi.this.mCallback.onDestroy();
            }
        }

        /* access modifiers changed from: package-private */
        public void hide() {
            hide(true);
        }

        /* access modifiers changed from: package-private */
        public void hide(boolean destroyCallbackOnError) {
            try {
                if (this.mShowing) {
                    this.mWm.removeView(this.mContentView);
                    this.mShowing = false;
                }
            } catch (IllegalStateException e) {
                Slog.e(FillUi.TAG, "Exception hiding window ", e);
                if (destroyCallbackOnError) {
                    FillUi.this.mCallback.onDestroy();
                }
            } catch (Throwable th) {
                this.mOverlayControl.showOverlays();
                throw th;
            }
            this.mOverlayControl.showOverlays();
        }
    }

    public void dump(PrintWriter pw, String prefix) {
        pw.print(prefix);
        pw.print("mCallback: ");
        pw.println(this.mCallback != null);
        pw.print(prefix);
        pw.print("mFullScreen: ");
        pw.println(this.mFullScreen);
        pw.print(prefix);
        pw.print("mVisibleDatasetsMaxCount: ");
        pw.println(this.mVisibleDatasetsMaxCount);
        if (this.mHeader != null) {
            pw.print(prefix);
            pw.print("mHeader: ");
            pw.println(this.mHeader);
        }
        if (this.mListView != null) {
            pw.print(prefix);
            pw.print("mListView: ");
            pw.println(this.mListView);
        }
        if (this.mFooter != null) {
            pw.print(prefix);
            pw.print("mFooter: ");
            pw.println(this.mFooter);
        }
        if (this.mAdapter != null) {
            pw.print(prefix);
            pw.print("mAdapter: ");
            pw.println(this.mAdapter);
        }
        if (this.mFilterText != null) {
            pw.print(prefix);
            pw.print("mFilterText: ");
            Helper.printlnRedactedText(pw, this.mFilterText);
        }
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
        pw.print("theme id: ");
        pw.print(this.mThemeId);
        int i = this.mThemeId;
        if (i == THEME_ID_DARK) {
            pw.println(" (dark)");
        } else if (i != THEME_ID_LIGHT) {
            pw.println("(UNKNOWN_MODE)");
        } else {
            pw.println(" (light)");
        }
        if (this.mWindow != null) {
            pw.print(prefix);
            pw.print("mWindow: ");
            String prefix2 = prefix + "  ";
            pw.println();
            pw.print(prefix2);
            pw.print("showing: ");
            pw.println(this.mWindow.mShowing);
            pw.print(prefix2);
            pw.print("view: ");
            pw.println(this.mWindow.mContentView);
            if (this.mWindow.mShowParams != null) {
                pw.print(prefix2);
                pw.print("params: ");
                pw.println(this.mWindow.mShowParams);
            }
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
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void announceSearchResultIfNeeded() {
        if (AccessibilityManager.getInstance(this.mContext).isEnabled()) {
            if (this.mAnnounceFilterResult == null) {
                this.mAnnounceFilterResult = new AnnounceFilterResult();
            }
            this.mAnnounceFilterResult.post();
        }
    }

    /* access modifiers changed from: private */
    public final class ItemsAdapter extends BaseAdapter implements Filterable {
        private final List<ViewItem> mAllItems;
        private final List<ViewItem> mFilteredItems = new ArrayList();

        ItemsAdapter(List<ViewItem> items) {
            this.mAllItems = Collections.unmodifiableList(new ArrayList(items));
            this.mFilteredItems.addAll(items);
        }

        @Override // android.widget.Filterable
        public Filter getFilter() {
            return new Filter() {
                /* class com.android.server.autofill.ui.FillUi.ItemsAdapter.AnonymousClass1 */

                /* access modifiers changed from: protected */
                @Override // android.widget.Filter
                public Filter.FilterResults performFiltering(CharSequence filterText) {
                    List<ViewItem> filtered = (List) ItemsAdapter.this.mAllItems.stream().filter(new Predicate(filterText) {
                        /* class com.android.server.autofill.ui.$$Lambda$FillUi$ItemsAdapter$1$8s9zobTvKJVJjInaObtlx2flLMc */
                        private final /* synthetic */ CharSequence f$0;

                        {
                            this.f$0 = r1;
                        }

                        @Override // java.util.function.Predicate
                        public final boolean test(Object obj) {
                            return ((FillUi.ViewItem) obj).matches(this.f$0);
                        }
                    }).collect(Collectors.toList());
                    Filter.FilterResults results = new Filter.FilterResults();
                    results.values = filtered;
                    results.count = filtered.size();
                    return results;
                }

                /* access modifiers changed from: protected */
                @Override // android.widget.Filter
                public void publishResults(CharSequence constraint, Filter.FilterResults results) {
                    int oldItemCount = ItemsAdapter.this.mFilteredItems.size();
                    ItemsAdapter.this.mFilteredItems.clear();
                    if (results.count > 0) {
                        ItemsAdapter.this.mFilteredItems.addAll((List) results.values);
                    }
                    if (oldItemCount != ItemsAdapter.this.mFilteredItems.size()) {
                        FillUi.this.announceSearchResultIfNeeded();
                    }
                    ItemsAdapter.this.notifyDataSetChanged();
                }
            };
        }

        @Override // android.widget.Adapter
        public int getCount() {
            return this.mFilteredItems.size();
        }

        @Override // android.widget.Adapter
        public ViewItem getItem(int position) {
            return this.mFilteredItems.get(position);
        }

        @Override // android.widget.Adapter
        public long getItemId(int position) {
            return (long) position;
        }

        @Override // android.widget.Adapter
        public View getView(int position, View convertView, ViewGroup parent) {
            return getItem(position).view;
        }

        @Override // java.lang.Object
        public String toString() {
            return "ItemsAdapter: [all=" + this.mAllItems + ", filtered=" + this.mFilteredItems + "]";
        }
    }

    /* access modifiers changed from: private */
    public final class AnnounceFilterResult implements Runnable {
        private static final int SEARCH_RESULT_ANNOUNCEMENT_DELAY = 1000;

        private AnnounceFilterResult() {
        }

        public void post() {
            remove();
            FillUi.this.mListView.postDelayed(this, 1000);
        }

        public void remove() {
            FillUi.this.mListView.removeCallbacks(this);
        }

        @Override // java.lang.Runnable
        public void run() {
            String text;
            int count = FillUi.this.mListView.getAdapter().getCount();
            if (count <= 0) {
                text = FillUi.this.mContext.getString(17039669);
            } else {
                text = FillUi.this.mContext.getResources().getQuantityString(18153472, count, Integer.valueOf(count));
            }
            FillUi.this.mListView.announceForAccessibility(text);
        }
    }
}
