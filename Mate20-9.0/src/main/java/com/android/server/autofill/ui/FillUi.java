package com.android.server.autofill.ui;

import android.app.PendingIntent;
import android.common.HwFrameworkFactory;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
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
import android.widget.ListView;
import android.widget.RemoteViews;
import android.widget.TextView;
import com.android.server.UiThread;
import com.android.server.autofill.Helper;
import com.android.server.autofill.ui.FillUi;
import com.android.server.backup.internal.BackupHandler;
import com.android.server.pm.PackageManagerService;
import com.android.server.wm.WindowManagerService;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.regex.Pattern;

final class FillUi {
    private static final String TAG = "FillUi";
    /* access modifiers changed from: private */
    public static boolean sIsHwAutofillService = false;
    private static final TypedValue sTempTypedValue = new TypedValue();
    private int THEME_ID = 16974777;
    private final ItemsAdapter mAdapter;
    private AnnounceFilterResult mAnnounceFilterResult;
    /* access modifiers changed from: private */
    public final Callback mCallback;
    private int mContentHeight;
    private int mContentWidth;
    /* access modifiers changed from: private */
    public final Context mContext;
    /* access modifiers changed from: private */
    public int mDecorPaddingHeight;
    /* access modifiers changed from: private */
    public int mDecorPaddingWidth;
    private boolean mDestroyed;
    private String mFilterText;
    private final View mFooter;
    private final boolean mFullScreen;
    private final View mHeader;
    /* access modifiers changed from: private */
    public IHwAutofillHelper mHwAutofillHelper = HwFrameworkFactory.getHwAutofillHelper();
    /* access modifiers changed from: private */
    public final ListView mListView;
    private final Point mTempPoint = new Point();
    private final int mVisibleDatasetsMaxCount;
    /* access modifiers changed from: private */
    public final AnchoredWindow mWindow;
    private final AutofillWindowPresenter mWindowPresenter = new AutofillWindowPresenter();

    final class AnchoredWindow {
        /* access modifiers changed from: private */
        public final View mContentView;
        private final OverlayControl mOverlayControl;
        /* access modifiers changed from: private */
        public WindowManager.LayoutParams mShowParams;
        /* access modifiers changed from: private */
        public boolean mShowing;
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
                    params.accessibilityTitle = this.mContentView.getContext().getString(17039659);
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
                Slog.e(FillUi.TAG, "Exception showing window " + params, e2);
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

    private final class AnnounceFilterResult implements Runnable {
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

        public void run() {
            String text;
            int count = FillUi.this.mListView.getAdapter().getCount();
            if (count <= 0) {
                text = FillUi.this.mContext.getString(17039660);
            } else {
                text = FillUi.this.mContext.getResources().getQuantityString(18153472, count, new Object[]{Integer.valueOf(count)});
            }
            FillUi.this.mListView.announceForAccessibility(text);
        }
    }

    private final class AutofillWindowPresenter extends IAutofillWindowPresenter.Stub {
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
                private final /* synthetic */ WindowManager.LayoutParams f$1;

                {
                    this.f$1 = r2;
                }

                public final void run() {
                    FillUi.this.mWindow.show(this.f$1);
                }
            });
        }

        public void hide(Rect transitionEpicenter) {
            Handler handler = UiThread.getHandler();
            AnchoredWindow access$700 = FillUi.this.mWindow;
            Objects.requireNonNull(access$700);
            handler.post(new Runnable() {
                public final void run() {
                    FillUi.AnchoredWindow.this.hide();
                }
            });
        }
    }

    interface Callback {
        void dispatchUnhandledKey(KeyEvent keyEvent);

        void onCanceled();

        void onDatasetPicked(Dataset dataset);

        void onDestroy();

        void onResponsePicked(FillResponse fillResponse);

        void requestHideFillUi();

        void requestShowFillUi(int i, int i2, IAutofillWindowPresenter iAutofillWindowPresenter);

        void startIntentSender(IntentSender intentSender);
    }

    private final class ItemsAdapter extends BaseAdapter implements Filterable {
        /* access modifiers changed from: private */
        public final List<ViewItem> mAllItems;
        /* access modifiers changed from: private */
        public final List<ViewItem> mFilteredItems = new ArrayList();

        ItemsAdapter(List<ViewItem> items) {
            this.mAllItems = Collections.unmodifiableList(new ArrayList(items));
            this.mFilteredItems.addAll(items);
        }

        public Filter getFilter() {
            return new Filter() {
                /* access modifiers changed from: protected */
                public Filter.FilterResults performFiltering(CharSequence filterText) {
                    List<ViewItem> filtered = (List) ItemsAdapter.this.mAllItems.stream().filter(
                    /*  JADX ERROR: Method code generation error
                        jadx.core.utils.exceptions.CodegenException: Error generate insn: 0x001b: CHECK_CAST  (r0v5 'filtered' java.util.List<com.android.server.autofill.ui.FillUi$ViewItem>) = (java.util.List) (wrap: java.lang.Object
                          0x0017: INVOKE  (r0v4 java.lang.Object) = (wrap: java.util.stream.Stream
                          0x000f: INVOKE  (r0v3 java.util.stream.Stream) = (wrap: java.util.stream.Stream
                          0x0006: INVOKE  (r0v2 java.util.stream.Stream) = (wrap: java.util.List
                          0x0002: INVOKE  (r0v1 java.util.List) = (wrap: com.android.server.autofill.ui.FillUi$ItemsAdapter
                          0x0000: IGET  (r0v0 com.android.server.autofill.ui.FillUi$ItemsAdapter) = (r3v0 'this' com.android.server.autofill.ui.FillUi$ItemsAdapter$1 A[THIS]) com.android.server.autofill.ui.FillUi.ItemsAdapter.1.this$1 com.android.server.autofill.ui.FillUi$ItemsAdapter) com.android.server.autofill.ui.FillUi.ItemsAdapter.access$1200(com.android.server.autofill.ui.FillUi$ItemsAdapter):java.util.List type: STATIC) java.util.List.stream():java.util.stream.Stream type: INTERFACE), (wrap: com.android.server.autofill.ui.-$$Lambda$FillUi$ItemsAdapter$1$8s9zobTvKJVJjInaObtlx2flLMc
                          0x000c: CONSTRUCTOR  (r1v0 com.android.server.autofill.ui.-$$Lambda$FillUi$ItemsAdapter$1$8s9zobTvKJVJjInaObtlx2flLMc) = (r4v0 'filterText' java.lang.CharSequence) com.android.server.autofill.ui.-$$Lambda$FillUi$ItemsAdapter$1$8s9zobTvKJVJjInaObtlx2flLMc.<init>(java.lang.CharSequence):void CONSTRUCTOR) java.util.stream.Stream.filter(java.util.function.Predicate):java.util.stream.Stream type: INTERFACE), (wrap: java.util.stream.Collector
                          0x0013: INVOKE  (r1v1 java.util.stream.Collector) =  java.util.stream.Collectors.toList():java.util.stream.Collector type: STATIC) java.util.stream.Stream.collect(java.util.stream.Collector):java.lang.Object type: INTERFACE) in method: com.android.server.autofill.ui.FillUi.ItemsAdapter.1.performFiltering(java.lang.CharSequence):android.widget.Filter$FilterResults, dex: services_classes.dex
                        	at jadx.core.codegen.InsnGen.makeInsn(InsnGen.java:245)
                        	at jadx.core.codegen.InsnGen.makeInsn(InsnGen.java:210)
                        	at jadx.core.codegen.RegionGen.makeSimpleBlock(RegionGen.java:109)
                        	at jadx.core.codegen.RegionGen.makeRegion(RegionGen.java:55)
                        	at jadx.core.codegen.RegionGen.makeSimpleRegion(RegionGen.java:92)
                        	at jadx.core.codegen.RegionGen.makeRegion(RegionGen.java:58)
                        	at jadx.core.codegen.MethodGen.addRegionInsns(MethodGen.java:211)
                        	at jadx.core.codegen.MethodGen.addInstructions(MethodGen.java:204)
                        	at jadx.core.codegen.ClassGen.addMethod(ClassGen.java:317)
                        	at jadx.core.codegen.ClassGen.addMethods(ClassGen.java:263)
                        	at jadx.core.codegen.ClassGen.addClassBody(ClassGen.java:226)
                        	at jadx.core.codegen.InsnGen.inlineAnonymousConstructor(InsnGen.java:665)
                        	at jadx.core.codegen.InsnGen.makeConstructor(InsnGen.java:596)
                        	at jadx.core.codegen.InsnGen.makeInsnBody(InsnGen.java:353)
                        	at jadx.core.codegen.InsnGen.makeInsn(InsnGen.java:220)
                        	at jadx.core.codegen.InsnGen.addArg(InsnGen.java:108)
                        	at jadx.core.codegen.InsnGen.makeInsnBody(InsnGen.java:303)
                        	at jadx.core.codegen.InsnGen.makeInsn(InsnGen.java:239)
                        	at jadx.core.codegen.InsnGen.makeInsn(InsnGen.java:210)
                        	at jadx.core.codegen.RegionGen.makeSimpleBlock(RegionGen.java:109)
                        	at jadx.core.codegen.RegionGen.makeRegion(RegionGen.java:55)
                        	at jadx.core.codegen.RegionGen.makeSimpleRegion(RegionGen.java:92)
                        	at jadx.core.codegen.RegionGen.makeRegion(RegionGen.java:58)
                        	at jadx.core.codegen.MethodGen.addRegionInsns(MethodGen.java:211)
                        	at jadx.core.codegen.MethodGen.addInstructions(MethodGen.java:204)
                        	at jadx.core.codegen.ClassGen.addMethod(ClassGen.java:317)
                        	at jadx.core.codegen.ClassGen.addMethods(ClassGen.java:263)
                        	at jadx.core.codegen.ClassGen.addClassBody(ClassGen.java:226)
                        	at jadx.core.codegen.ClassGen.addClassCode(ClassGen.java:111)
                        	at jadx.core.codegen.ClassGen.addInnerClasses(ClassGen.java:238)
                        	at jadx.core.codegen.ClassGen.addClassBody(ClassGen.java:225)
                        	at jadx.core.codegen.ClassGen.addClassCode(ClassGen.java:111)
                        	at jadx.core.codegen.ClassGen.makeClass(ClassGen.java:77)
                        	at jadx.core.codegen.CodeGen.wrapCodeGen(CodeGen.java:44)
                        	at jadx.core.codegen.CodeGen.generateJavaCode(CodeGen.java:33)
                        	at jadx.core.codegen.CodeGen.generate(CodeGen.java:21)
                        	at jadx.core.ProcessClass.generateCode(ProcessClass.java:61)
                        	at jadx.core.dex.nodes.ClassNode.decompile(ClassNode.java:273)
                        Caused by: jadx.core.utils.exceptions.CodegenException: Error generate insn: 0x0017: INVOKE  (r0v4 java.lang.Object) = (wrap: java.util.stream.Stream
                          0x000f: INVOKE  (r0v3 java.util.stream.Stream) = (wrap: java.util.stream.Stream
                          0x0006: INVOKE  (r0v2 java.util.stream.Stream) = (wrap: java.util.List
                          0x0002: INVOKE  (r0v1 java.util.List) = (wrap: com.android.server.autofill.ui.FillUi$ItemsAdapter
                          0x0000: IGET  (r0v0 com.android.server.autofill.ui.FillUi$ItemsAdapter) = (r3v0 'this' com.android.server.autofill.ui.FillUi$ItemsAdapter$1 A[THIS]) com.android.server.autofill.ui.FillUi.ItemsAdapter.1.this$1 com.android.server.autofill.ui.FillUi$ItemsAdapter) com.android.server.autofill.ui.FillUi.ItemsAdapter.access$1200(com.android.server.autofill.ui.FillUi$ItemsAdapter):java.util.List type: STATIC) java.util.List.stream():java.util.stream.Stream type: INTERFACE), (wrap: com.android.server.autofill.ui.-$$Lambda$FillUi$ItemsAdapter$1$8s9zobTvKJVJjInaObtlx2flLMc
                          0x000c: CONSTRUCTOR  (r1v0 com.android.server.autofill.ui.-$$Lambda$FillUi$ItemsAdapter$1$8s9zobTvKJVJjInaObtlx2flLMc) = (r4v0 'filterText' java.lang.CharSequence) com.android.server.autofill.ui.-$$Lambda$FillUi$ItemsAdapter$1$8s9zobTvKJVJjInaObtlx2flLMc.<init>(java.lang.CharSequence):void CONSTRUCTOR) java.util.stream.Stream.filter(java.util.function.Predicate):java.util.stream.Stream type: INTERFACE), (wrap: java.util.stream.Collector
                          0x0013: INVOKE  (r1v1 java.util.stream.Collector) =  java.util.stream.Collectors.toList():java.util.stream.Collector type: STATIC) java.util.stream.Stream.collect(java.util.stream.Collector):java.lang.Object type: INTERFACE in method: com.android.server.autofill.ui.FillUi.ItemsAdapter.1.performFiltering(java.lang.CharSequence):android.widget.Filter$FilterResults, dex: services_classes.dex
                        	at jadx.core.codegen.InsnGen.makeInsn(InsnGen.java:245)
                        	at jadx.core.codegen.InsnGen.addArg(InsnGen.java:108)
                        	at jadx.core.codegen.InsnGen.makeInsnBody(InsnGen.java:280)
                        	at jadx.core.codegen.InsnGen.makeInsn(InsnGen.java:239)
                        	... 37 more
                        Caused by: jadx.core.utils.exceptions.CodegenException: Error generate insn: 0x000f: INVOKE  (r0v3 java.util.stream.Stream) = (wrap: java.util.stream.Stream
                          0x0006: INVOKE  (r0v2 java.util.stream.Stream) = (wrap: java.util.List
                          0x0002: INVOKE  (r0v1 java.util.List) = (wrap: com.android.server.autofill.ui.FillUi$ItemsAdapter
                          0x0000: IGET  (r0v0 com.android.server.autofill.ui.FillUi$ItemsAdapter) = (r3v0 'this' com.android.server.autofill.ui.FillUi$ItemsAdapter$1 A[THIS]) com.android.server.autofill.ui.FillUi.ItemsAdapter.1.this$1 com.android.server.autofill.ui.FillUi$ItemsAdapter) com.android.server.autofill.ui.FillUi.ItemsAdapter.access$1200(com.android.server.autofill.ui.FillUi$ItemsAdapter):java.util.List type: STATIC) java.util.List.stream():java.util.stream.Stream type: INTERFACE), (wrap: com.android.server.autofill.ui.-$$Lambda$FillUi$ItemsAdapter$1$8s9zobTvKJVJjInaObtlx2flLMc
                          0x000c: CONSTRUCTOR  (r1v0 com.android.server.autofill.ui.-$$Lambda$FillUi$ItemsAdapter$1$8s9zobTvKJVJjInaObtlx2flLMc) = (r4v0 'filterText' java.lang.CharSequence) com.android.server.autofill.ui.-$$Lambda$FillUi$ItemsAdapter$1$8s9zobTvKJVJjInaObtlx2flLMc.<init>(java.lang.CharSequence):void CONSTRUCTOR) java.util.stream.Stream.filter(java.util.function.Predicate):java.util.stream.Stream type: INTERFACE in method: com.android.server.autofill.ui.FillUi.ItemsAdapter.1.performFiltering(java.lang.CharSequence):android.widget.Filter$FilterResults, dex: services_classes.dex
                        	at jadx.core.codegen.InsnGen.makeInsn(InsnGen.java:245)
                        	at jadx.core.codegen.InsnGen.addArg(InsnGen.java:108)
                        	at jadx.core.codegen.InsnGen.addArgDot(InsnGen.java:91)
                        	at jadx.core.codegen.InsnGen.makeInvoke(InsnGen.java:686)
                        	at jadx.core.codegen.InsnGen.makeInsnBody(InsnGen.java:357)
                        	at jadx.core.codegen.InsnGen.makeInsn(InsnGen.java:220)
                        	... 40 more
                        Caused by: jadx.core.utils.exceptions.CodegenException: Error generate insn: 0x000c: CONSTRUCTOR  (r1v0 com.android.server.autofill.ui.-$$Lambda$FillUi$ItemsAdapter$1$8s9zobTvKJVJjInaObtlx2flLMc) = (r4v0 'filterText' java.lang.CharSequence) com.android.server.autofill.ui.-$$Lambda$FillUi$ItemsAdapter$1$8s9zobTvKJVJjInaObtlx2flLMc.<init>(java.lang.CharSequence):void CONSTRUCTOR in method: com.android.server.autofill.ui.FillUi.ItemsAdapter.1.performFiltering(java.lang.CharSequence):android.widget.Filter$FilterResults, dex: services_classes.dex
                        	at jadx.core.codegen.InsnGen.makeInsn(InsnGen.java:245)
                        	at jadx.core.codegen.InsnGen.addArg(InsnGen.java:108)
                        	at jadx.core.codegen.InsnGen.generateMethodArguments(InsnGen.java:776)
                        	at jadx.core.codegen.InsnGen.makeInvoke(InsnGen.java:717)
                        	at jadx.core.codegen.InsnGen.makeInsnBody(InsnGen.java:357)
                        	at jadx.core.codegen.InsnGen.makeInsn(InsnGen.java:220)
                        	... 45 more
                        Caused by: jadx.core.utils.exceptions.JadxRuntimeException: Expected class to be processed at this point, class: com.android.server.autofill.ui.-$$Lambda$FillUi$ItemsAdapter$1$8s9zobTvKJVJjInaObtlx2flLMc, state: NOT_LOADED
                        	at jadx.core.dex.nodes.ClassNode.ensureProcessed(ClassNode.java:260)
                        	at jadx.core.codegen.InsnGen.makeConstructor(InsnGen.java:595)
                        	at jadx.core.codegen.InsnGen.makeInsnBody(InsnGen.java:353)
                        	at jadx.core.codegen.InsnGen.makeInsn(InsnGen.java:220)
                        	... 50 more
                        */
                    /*
                        this = this;
                        com.android.server.autofill.ui.FillUi$ItemsAdapter r0 = com.android.server.autofill.ui.FillUi.ItemsAdapter.this
                        java.util.List r0 = r0.mAllItems
                        java.util.stream.Stream r0 = r0.stream()
                        com.android.server.autofill.ui.-$$Lambda$FillUi$ItemsAdapter$1$8s9zobTvKJVJjInaObtlx2flLMc r1 = new com.android.server.autofill.ui.-$$Lambda$FillUi$ItemsAdapter$1$8s9zobTvKJVJjInaObtlx2flLMc
                        r1.<init>(r4)
                        java.util.stream.Stream r0 = r0.filter(r1)
                        java.util.stream.Collector r1 = java.util.stream.Collectors.toList()
                        java.lang.Object r0 = r0.collect(r1)
                        java.util.List r0 = (java.util.List) r0
                        android.widget.Filter$FilterResults r1 = new android.widget.Filter$FilterResults
                        r1.<init>()
                        r1.values = r0
                        int r2 = r0.size()
                        r1.count = r2
                        return r1
                    */
                    throw new UnsupportedOperationException("Method not decompiled: com.android.server.autofill.ui.FillUi.ItemsAdapter.AnonymousClass1.performFiltering(java.lang.CharSequence):android.widget.Filter$FilterResults");
                }

                /* access modifiers changed from: protected */
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

        public int getCount() {
            return this.mFilteredItems.size();
        }

        public ViewItem getItem(int position) {
            return this.mFilteredItems.get(position);
        }

        public long getItemId(int position) {
            return (long) position;
        }

        public View getView(int position, View convertView, ViewGroup parent) {
            return getItem(position).view;
        }

        public String toString() {
            return "ItemsAdapter: [all=" + this.mAllItems + ", filtered=" + this.mFilteredItems + "]";
        }
    }

    private static class ViewItem {
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
            boolean z = true;
            if (TextUtils.isEmpty(filterText)) {
                return true;
            }
            if (!this.filterable) {
                return false;
            }
            String constraintLowerCase = filterText.toString().toLowerCase();
            if (this.filter != null) {
                return this.filter.matcher(constraintLowerCase).matches();
            }
            if (this.value != null) {
                z = FillUi.sIsHwAutofillService ? this.value.toLowerCase().contains(constraintLowerCase) : this.value.toLowerCase().startsWith(constraintLowerCase);
            } else if (this.dataset.getAuthentication() != null) {
                z = false;
            }
            return z;
        }

        public String toString() {
            StringBuilder builder = new StringBuilder("ViewItem:[view=").append(this.view.getAutofillId());
            String datasetId = this.dataset == null ? null : this.dataset.getId();
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

    public static boolean isFullScreen(Context context) {
        if (Helper.sFullScreenMode == null) {
            return context.getPackageManager().hasSystemFeature("android.software.leanback");
        }
        if (Helper.sVerbose) {
            Slog.v(TAG, "forcing full-screen mode to " + Helper.sFullScreenMode);
        }
        return Helper.sFullScreenMode.booleanValue();
    }

    FillUi(Context context, FillResponse response, AutofillId focusedViewId, String filterText, OverlayControl overlayControl, CharSequence serviceLabel, Drawable serviceIcon, Callback callback) {
        ViewGroup decor;
        AnonymousClass1 r30;
        RemoteViews footerPresentation;
        RemoteViews headerPresentation;
        int datasetCount;
        AnonymousClass1 r302;
        Pattern filterPattern;
        int i;
        AutofillId autofillId = focusedViewId;
        OverlayControl overlayControl2 = overlayControl;
        this.mCallback = callback;
        this.mFullScreen = isFullScreen(context);
        this.mContext = new ContextThemeWrapper(context, this.THEME_ID);
        this.THEME_ID = this.mContext.getResources().getIdentifier("androidhwext:style/Theme.Emui", null, null);
        this.mContext.setTheme(this.THEME_ID);
        LayoutInflater inflater = LayoutInflater.from(this.mContext);
        RemoteViews headerPresentation2 = response.getHeader();
        RemoteViews footerPresentation2 = response.getFooter();
        if (this.mFullScreen) {
            decor = (ViewGroup) inflater.inflate(17367101, null);
        } else if (headerPresentation2 == null && footerPresentation2 == null) {
            decor = (ViewGroup) inflater.inflate(17367100, null);
        } else {
            decor = (ViewGroup) inflater.inflate(17367102, null);
        }
        ViewGroup decor2 = decor;
        TextView titleView = (TextView) decor2.findViewById(16908755);
        if (titleView != null) {
            titleView.setText(this.mContext.getString(17039682, new Object[]{serviceLabel}));
        }
        ImageView iconView = (ImageView) decor2.findViewById(16908752);
        if (iconView != null) {
            iconView.setImageDrawable(serviceIcon);
        } else {
            Drawable drawable = serviceIcon;
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
        decor2.addOnUnhandledKeyEventListener(new View.OnUnhandledKeyEventListener() {
            public final boolean onUnhandledKeyEvent(View view, KeyEvent keyEvent) {
                return FillUi.lambda$new$0(FillUi.this, view, keyEvent);
            }
        });
        if (Helper.sVisibleDatasetsMaxCount > 0) {
            this.mVisibleDatasetsMaxCount = Helper.sVisibleDatasetsMaxCount;
            if (Helper.sVerbose) {
                Slog.v(TAG, "overriding maximum visible datasets to " + this.mVisibleDatasetsMaxCount);
            }
        } else {
            this.mVisibleDatasetsMaxCount = this.mContext.getResources().getInteger(17694724);
        }
        AnonymousClass1 r4 = new RemoteViews.OnClickHandler() {
            public boolean onClickHandler(View view, PendingIntent pendingIntent, Intent fillInIntent) {
                if (pendingIntent != null) {
                    FillUi.this.mCallback.startIntentSender(pendingIntent.getIntentSender());
                }
                return true;
            }
        };
        if (response.getAuthentication() != null) {
            this.mHeader = null;
            this.mListView = null;
            this.mFooter = null;
            this.mAdapter = null;
            ViewGroup container = (ViewGroup) decor2.findViewById(16908754);
            try {
                response.getPresentation().setApplyTheme(this.THEME_ID);
                View content = response.getPresentation().apply(this.mContext, decor2, r4);
                container.addView(content);
                container.setFocusable(true);
                LayoutInflater layoutInflater = inflater;
                container.setOnClickListener(new View.OnClickListener(response) {
                    private final /* synthetic */ FillResponse f$1;

                    {
                        this.f$1 = r2;
                    }

                    public final void onClick(View view) {
                        FillUi.this.mCallback.onResponsePicked(this.f$1);
                    }
                });
                if (!this.mFullScreen) {
                    Point maxSize = this.mTempPoint;
                    resolveMaxWindowSize(this.mContext, maxSize);
                    ViewGroup.LayoutParams layoutParams = content.getLayoutParams();
                    TextView textView = titleView;
                    ViewGroup viewGroup = container;
                    if (this.mFullScreen) {
                        i = maxSize.x;
                    } else {
                        i = -2;
                    }
                    layoutParams.width = i;
                    content.getLayoutParams().height = -2;
                    decor2.measure(View.MeasureSpec.makeMeasureSpec(maxSize.x, Integer.MIN_VALUE), View.MeasureSpec.makeMeasureSpec(maxSize.y, Integer.MIN_VALUE));
                    this.mContentWidth = content.getMeasuredWidth();
                    this.mContentHeight = content.getMeasuredHeight();
                } else {
                    ViewGroup viewGroup2 = container;
                }
                this.mWindow = new AnchoredWindow(decor2, overlayControl2);
                requestShowFillUi();
                AnonymousClass1 r303 = r4;
                RemoteViews remoteViews = headerPresentation2;
                RemoteViews remoteViews2 = footerPresentation2;
            } catch (RuntimeException e) {
                LayoutInflater layoutInflater2 = inflater;
                TextView textView2 = titleView;
                ViewGroup viewGroup3 = container;
                FillResponse fillResponse = response;
                callback.onCanceled();
                Slog.e(TAG, "Error inflating remote views", e);
                this.mWindow = null;
            }
        } else {
            TextView textView3 = titleView;
            FillResponse fillResponse2 = response;
            int datasetCount2 = response.getDatasets().size();
            if (Helper.sVerbose) {
                Slog.v(TAG, "Number datasets: " + datasetCount2 + " max visible: " + this.mVisibleDatasetsMaxCount);
            }
            RemoteViews.OnClickHandler clickBlocker = null;
            if (headerPresentation2 != null) {
                clickBlocker = newClickBlocker();
                headerPresentation2.setApplyTheme(this.THEME_ID);
                this.mHeader = headerPresentation2.apply(this.mContext, null, clickBlocker);
                LinearLayout headerContainer = (LinearLayout) decor2.findViewById(16908751);
                if (Helper.sVerbose) {
                    Slog.v(TAG, "adding header");
                }
                headerContainer.addView(this.mHeader);
                headerContainer.setVisibility(0);
            } else {
                this.mHeader = null;
            }
            if (footerPresentation2 != null) {
                LinearLayout footerContainer = (LinearLayout) decor2.findViewById(16908750);
                if (footerContainer != null) {
                    clickBlocker = clickBlocker == null ? newClickBlocker() : clickBlocker;
                    footerPresentation2.setApplyTheme(this.THEME_ID);
                    this.mFooter = footerPresentation2.apply(this.mContext, null, clickBlocker);
                    if (Helper.sVerbose) {
                        Slog.v(TAG, "adding footer");
                    }
                    footerContainer.addView(this.mFooter);
                    footerContainer.setVisibility(0);
                } else {
                    this.mFooter = null;
                }
            } else {
                this.mFooter = null;
            }
            sIsHwAutofillService = this.mHwAutofillHelper != null && this.mHwAutofillHelper.isHwAutofillService(this.mContext);
            ArrayList<ViewItem> items = new ArrayList<>(datasetCount2);
            int i2 = 0;
            while (true) {
                int i3 = i2;
                if (i3 >= datasetCount2) {
                    break;
                }
                Dataset dataset = (Dataset) response.getDatasets().get(i3);
                int index = dataset.getFieldIds().indexOf(autofillId);
                if (index >= 0) {
                    datasetCount = datasetCount2;
                    RemoteViews presentation = dataset.getFieldPresentation(index);
                    if (presentation == null) {
                        headerPresentation = headerPresentation2;
                        StringBuilder sb = new StringBuilder();
                        footerPresentation = footerPresentation2;
                        sb.append("not displaying UI on field ");
                        sb.append(autofillId);
                        sb.append(" because service didn't provide a presentation for it on ");
                        sb.append(dataset);
                        Slog.w(TAG, sb.toString());
                        r30 = r4;
                    } else {
                        headerPresentation = headerPresentation2;
                        footerPresentation = footerPresentation2;
                        try {
                            if (Helper.sVerbose) {
                                try {
                                    Slog.v(TAG, "setting remote view for " + autofillId);
                                } catch (RuntimeException e2) {
                                    e = e2;
                                    r302 = r4;
                                    RemoteViews remoteViews3 = presentation;
                                }
                            }
                            presentation.setApplyTheme(this.THEME_ID);
                            View view = presentation.apply(this.mContext, null, r4);
                            Dataset.DatasetFieldFilter filter = dataset.getFilter(index);
                            String valueText = null;
                            boolean filterable = true;
                            if (filter == null) {
                                r30 = r4;
                                AutofillValue value = (AutofillValue) dataset.getFieldValues().get(index);
                                if (value == null || !value.isText()) {
                                } else {
                                    RemoteViews remoteViews4 = presentation;
                                    valueText = value.getTextValue().toString().toLowerCase();
                                }
                                Dataset.DatasetFieldFilter datasetFieldFilter = filter;
                                filterPattern = null;
                            } else {
                                r30 = r4;
                                RemoteViews remoteViews5 = presentation;
                                filterPattern = filter.pattern;
                                if (filterPattern == null) {
                                    if (Helper.sVerbose) {
                                        StringBuilder sb2 = new StringBuilder();
                                        Dataset.DatasetFieldFilter datasetFieldFilter2 = filter;
                                        sb2.append("Explicitly disabling filter at id ");
                                        sb2.append(autofillId);
                                        sb2.append(" for dataset #");
                                        sb2.append(index);
                                        Slog.v(TAG, sb2.toString());
                                    }
                                    filterable = false;
                                }
                            }
                            ViewItem viewItem = new ViewItem(dataset, filterPattern, filterable, valueText, view);
                            items.add(viewItem);
                        } catch (RuntimeException e3) {
                            e = e3;
                            r302 = r4;
                            RemoteViews remoteViews6 = presentation;
                            Slog.e(TAG, "Error inflating remote views", e);
                            i2 = i3 + 1;
                            datasetCount2 = datasetCount;
                            headerPresentation2 = headerPresentation;
                            footerPresentation2 = footerPresentation;
                            r4 = r30;
                            Callback callback2 = callback;
                            FillResponse fillResponse3 = response;
                        }
                    }
                } else {
                    r30 = r4;
                    datasetCount = datasetCount2;
                    headerPresentation = headerPresentation2;
                    footerPresentation = footerPresentation2;
                }
                i2 = i3 + 1;
                datasetCount2 = datasetCount;
                headerPresentation2 = headerPresentation;
                footerPresentation2 = footerPresentation;
                r4 = r30;
                Callback callback22 = callback;
                FillResponse fillResponse32 = response;
            }
            AnonymousClass1 r304 = r4;
            int i4 = datasetCount2;
            RemoteViews remoteViews7 = headerPresentation2;
            RemoteViews remoteViews8 = footerPresentation2;
            this.mAdapter = new ItemsAdapter(items);
            this.mListView = (ListView) decor2.findViewById(16908753);
            this.mListView.setAdapter(this.mAdapter);
            this.mListView.setVisibility(0);
            this.mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                public final void onItemClick(AdapterView adapterView, View view, int i, long j) {
                    FillUi.this.mCallback.onDatasetPicked(FillUi.this.mAdapter.getItem(i).dataset);
                }
            });
            if (this.mHwAutofillHelper != null && this.mHwAutofillHelper.isHwAutofillService(this.mContext)) {
                FrameLayout layout = (FrameLayout) decor2.findViewById(16908754);
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
            this.mWindow = new AnchoredWindow(decor2, overlayControl2);
        }
    }

    public static /* synthetic */ boolean lambda$new$0(FillUi fillUi, View view, KeyEvent event) {
        int keyCode = event.getKeyCode();
        if (!(keyCode == 4 || keyCode == 66 || keyCode == 111)) {
            switch (keyCode) {
                case WindowManagerService.H.REPORT_WINDOWS_CHANGE /*19*/:
                case 20:
                case BackupHandler.MSG_OP_COMPLETE /*21*/:
                case WindowManagerService.H.REPORT_HARD_KEYBOARD_STATUS_CHANGE /*22*/:
                case WindowManagerService.H.BOOT_TIMEOUT /*23*/:
                    break;
                default:
                    fillUi.mCallback.dispatchUnhandledKey(event);
                    return true;
            }
        }
        return false;
    }

    /* access modifiers changed from: package-private */
    public void requestShowFillUi() {
        this.mCallback.requestShowFillUi(this.mContentWidth, this.mContentHeight, this.mWindowPresenter);
    }

    private RemoteViews.OnClickHandler newClickBlocker() {
        return new RemoteViews.OnClickHandler() {
            public boolean onClickHandler(View view, PendingIntent pendingIntent, Intent fillInIntent) {
                if (Helper.sVerbose) {
                    Slog.v(FillUi.TAG, "Ignoring click on " + view);
                }
                return true;
            }
        };
    }

    private void applyNewFilterText() {
        this.mAdapter.getFilter().filter(this.mFilterText, new Filter.FilterListener(this.mAdapter.getCount()) {
            private final /* synthetic */ int f$1;

            {
                this.f$1 = r2;
            }

            public final void onFilterComplete(int i) {
                FillUi.lambda$applyNewFilterText$3(FillUi.this, this.f$1, i);
            }
        });
    }

    public static /* synthetic */ void lambda$applyNewFilterText$3(FillUi fillUi, int oldCount, int count) {
        if (!fillUi.mDestroyed) {
            int size = 0;
            if (count <= 0) {
                if (Helper.sDebug) {
                    if (fillUi.mFilterText != null) {
                        size = fillUi.mFilterText.length();
                    }
                    Slog.d(TAG, "No dataset matches filter with " + size + " chars");
                }
                fillUi.mCallback.requestHideFillUi();
            } else {
                if (fillUi.updateContentSize()) {
                    fillUi.requestShowFillUi();
                }
                if (fillUi.mAdapter.getCount() > fillUi.mVisibleDatasetsMaxCount) {
                    fillUi.mListView.setVerticalScrollBarEnabled(true);
                    fillUi.mListView.onVisibilityAggregated(true);
                } else {
                    fillUi.mListView.setVerticalScrollBarEnabled(false);
                }
                if (fillUi.mAdapter.getCount() != oldCount) {
                    fillUi.mListView.requestLayout();
                }
            }
        }
    }

    public void setFilterText(String filterText) {
        String filterText2;
        throwIfDestroyed();
        if (this.mAdapter == null) {
            if (TextUtils.isEmpty(filterText)) {
                requestShowFillUi();
            } else {
                this.mCallback.requestHideFillUi();
            }
            return;
        }
        if (filterText == null) {
            filterText2 = null;
        } else {
            filterText2 = filterText.toLowerCase();
        }
        if (!Objects.equals(this.mFilterText, filterText2)) {
            this.mFilterText = filterText2;
            applyNewFilterText();
        }
    }

    public void destroy(boolean notifyClient) {
        throwIfDestroyed();
        if (this.mWindow != null) {
            this.mWindow.hide(false);
        }
        this.mCallback.onDestroy();
        if (notifyClient) {
            this.mCallback.requestHideFillUi();
        }
        this.mDestroyed = true;
    }

    private boolean updateContentSize() {
        boolean changed;
        if (this.mAdapter == null) {
            return false;
        }
        if (this.mFullScreen) {
            return true;
        }
        boolean changed2 = false;
        if (this.mAdapter.getCount() <= 0) {
            if (this.mContentWidth != 0) {
                this.mContentWidth = 0;
                changed2 = true;
            }
            if (this.mContentHeight != 0) {
                this.mContentHeight = 0;
                changed2 = true;
            }
            return changed2;
        }
        Point maxSize = this.mTempPoint;
        resolveMaxWindowSize(this.mContext, maxSize);
        this.mContentWidth = 0;
        this.mContentHeight = 0;
        int widthMeasureSpec = View.MeasureSpec.makeMeasureSpec(maxSize.x, Integer.MIN_VALUE);
        int heightMeasureSpec = View.MeasureSpec.makeMeasureSpec(maxSize.y, Integer.MIN_VALUE);
        int itemCount = this.mAdapter.getCount();
        if (this.mHeader != null) {
            this.mHeader.measure(widthMeasureSpec, heightMeasureSpec);
            changed2 = false | updateWidth(this.mHeader, maxSize) | updateHeight(this.mHeader, maxSize);
        }
        for (int i = 0; i < itemCount; i++) {
            View view = this.mAdapter.getItem(i).view;
            view.measure(widthMeasureSpec, heightMeasureSpec);
            changed |= updateWidth(view, maxSize);
            if (i < this.mVisibleDatasetsMaxCount) {
                changed |= updateHeight(view, maxSize);
            }
        }
        if (this.mFooter != null) {
            this.mFooter.measure(widthMeasureSpec, heightMeasureSpec);
            changed = changed | updateWidth(this.mFooter, maxSize) | updateHeight(this.mFooter, maxSize);
        }
        return changed;
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
        int newContentHeight = this.mContentHeight + Math.min(view.getMeasuredHeight(), maxSize.y);
        if (newContentHeight == this.mContentHeight) {
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
        context.getTheme().resolveAttribute(17891345, typedValue, true);
        outPoint.x = (int) typedValue.getFraction((float) outPoint.x, (float) outPoint.x);
        context.getTheme().resolveAttribute(17891344, typedValue, true);
        outPoint.y = (int) typedValue.getFraction((float) outPoint.y, (float) outPoint.y);
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
    public void announceSearchResultIfNeeded() {
        if (AccessibilityManager.getInstance(this.mContext).isEnabled()) {
            if (this.mAnnounceFilterResult == null) {
                this.mAnnounceFilterResult = new AnnounceFilterResult();
            }
            this.mAnnounceFilterResult.post();
        }
    }
}
