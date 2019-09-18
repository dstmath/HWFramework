package android.widget;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.database.DataSetObserver;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.ActionProvider;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.accessibility.AccessibilityNodeInfo;
import android.widget.ActivityChooserModel;
import android.widget.AdapterView;
import android.widget.PopupWindow;
import com.android.internal.R;
import com.android.internal.view.menu.ShowableListMenu;

public class ActivityChooserView extends ViewGroup implements ActivityChooserModel.ActivityChooserModelClient {
    private static final String LOG_TAG = "ActivityChooserView";
    private final LinearLayout mActivityChooserContent;
    private final Drawable mActivityChooserContentBackground;
    /* access modifiers changed from: private */
    public final ActivityChooserViewAdapter mAdapter;
    private final Callbacks mCallbacks;
    private int mDefaultActionButtonContentDescription;
    /* access modifiers changed from: private */
    public final FrameLayout mDefaultActivityButton;
    private final ImageView mDefaultActivityButtonImage;
    /* access modifiers changed from: private */
    public final FrameLayout mExpandActivityOverflowButton;
    private final ImageView mExpandActivityOverflowButtonImage;
    /* access modifiers changed from: private */
    public int mInitialActivityCount;
    private boolean mIsAttachedToWindow;
    /* access modifiers changed from: private */
    public boolean mIsSelectingDefaultActivity;
    private final int mListPopupMaxWidth;
    private ListPopupWindow mListPopupWindow;
    /* access modifiers changed from: private */
    public final DataSetObserver mModelDataSetOberver;
    /* access modifiers changed from: private */
    public PopupWindow.OnDismissListener mOnDismissListener;
    private final ViewTreeObserver.OnGlobalLayoutListener mOnGlobalLayoutListener;
    ActionProvider mProvider;

    private class ActivityChooserViewAdapter extends BaseAdapter {
        private static final int ITEM_VIEW_TYPE_ACTIVITY = 0;
        private static final int ITEM_VIEW_TYPE_COUNT = 3;
        private static final int ITEM_VIEW_TYPE_FOOTER = 1;
        public static final int MAX_ACTIVITY_COUNT_DEFAULT = 4;
        public static final int MAX_ACTIVITY_COUNT_UNLIMITED = Integer.MAX_VALUE;
        private ActivityChooserModel mDataModel;
        private boolean mHighlightDefaultActivity;
        private int mMaxActivityCount;
        private boolean mShowDefaultActivity;
        private boolean mShowFooterView;

        private ActivityChooserViewAdapter() {
            this.mMaxActivityCount = 4;
        }

        public void setDataModel(ActivityChooserModel dataModel) {
            ActivityChooserModel oldDataModel = ActivityChooserView.this.mAdapter.getDataModel();
            if (oldDataModel != null && ActivityChooserView.this.isShown()) {
                oldDataModel.unregisterObserver(ActivityChooserView.this.mModelDataSetOberver);
            }
            this.mDataModel = dataModel;
            if (dataModel != null && ActivityChooserView.this.isShown()) {
                dataModel.registerObserver(ActivityChooserView.this.mModelDataSetOberver);
            }
            notifyDataSetChanged();
        }

        public int getItemViewType(int position) {
            if (!this.mShowFooterView || position != getCount() - 1) {
                return 0;
            }
            return 1;
        }

        public int getViewTypeCount() {
            return 3;
        }

        public int getCount() {
            int activityCount = this.mDataModel.getActivityCount();
            if (!this.mShowDefaultActivity && this.mDataModel.getDefaultActivity() != null) {
                activityCount--;
            }
            int count = Math.min(activityCount, this.mMaxActivityCount);
            if (this.mShowFooterView) {
                return count + 1;
            }
            return count;
        }

        public Object getItem(int position) {
            switch (getItemViewType(position)) {
                case 0:
                    if (!this.mShowDefaultActivity && this.mDataModel.getDefaultActivity() != null) {
                        position++;
                    }
                    return this.mDataModel.getActivity(position);
                case 1:
                    return null;
                default:
                    throw new IllegalArgumentException();
            }
        }

        public long getItemId(int position) {
            return (long) position;
        }

        public View getView(int position, View convertView, ViewGroup parent) {
            switch (getItemViewType(position)) {
                case 0:
                    if (convertView == null || convertView.getId() != 16909053) {
                        convertView = LayoutInflater.from(ActivityChooserView.this.getContext()).inflate(17367076, parent, false);
                    }
                    PackageManager packageManager = ActivityChooserView.this.mContext.getPackageManager();
                    ResolveInfo activity = (ResolveInfo) getItem(position);
                    ((ImageView) convertView.findViewById(16908294)).setImageDrawable(activity.loadIcon(packageManager));
                    ((TextView) convertView.findViewById(16908310)).setText(activity.loadLabel(packageManager));
                    if (!this.mShowDefaultActivity || position != 0 || !this.mHighlightDefaultActivity) {
                        convertView.setActivated(false);
                    } else {
                        convertView.setActivated(true);
                    }
                    return convertView;
                case 1:
                    if (convertView == null || convertView.getId() != 1) {
                        convertView = LayoutInflater.from(ActivityChooserView.this.getContext()).inflate(17367076, parent, false);
                        convertView.setId(1);
                        ((TextView) convertView.findViewById(16908310)).setText((CharSequence) ActivityChooserView.this.mContext.getString(17039542));
                    }
                    return convertView;
                default:
                    throw new IllegalArgumentException();
            }
        }

        public int measureContentWidth() {
            int oldMaxActivityCount = this.mMaxActivityCount;
            this.mMaxActivityCount = Integer.MAX_VALUE;
            int contentWidth = 0;
            View itemView = null;
            int widthMeasureSpec = View.MeasureSpec.makeMeasureSpec(0, 0);
            int heightMeasureSpec = View.MeasureSpec.makeMeasureSpec(0, 0);
            int count = getCount();
            for (int i = 0; i < count; i++) {
                itemView = getView(i, itemView, null);
                itemView.measure(widthMeasureSpec, heightMeasureSpec);
                contentWidth = Math.max(contentWidth, itemView.getMeasuredWidth());
            }
            this.mMaxActivityCount = oldMaxActivityCount;
            return contentWidth;
        }

        public void setMaxActivityCount(int maxActivityCount) {
            if (this.mMaxActivityCount != maxActivityCount) {
                this.mMaxActivityCount = maxActivityCount;
                notifyDataSetChanged();
            }
        }

        public ResolveInfo getDefaultActivity() {
            return this.mDataModel.getDefaultActivity();
        }

        public void setShowFooterView(boolean showFooterView) {
            if (this.mShowFooterView != showFooterView) {
                this.mShowFooterView = showFooterView;
                notifyDataSetChanged();
            }
        }

        public int getActivityCount() {
            return this.mDataModel.getActivityCount();
        }

        public int getHistorySize() {
            return this.mDataModel.getHistorySize();
        }

        public ActivityChooserModel getDataModel() {
            return this.mDataModel;
        }

        public void setShowDefaultActivity(boolean showDefaultActivity, boolean highlightDefaultActivity) {
            if (this.mShowDefaultActivity != showDefaultActivity || this.mHighlightDefaultActivity != highlightDefaultActivity) {
                this.mShowDefaultActivity = showDefaultActivity;
                this.mHighlightDefaultActivity = highlightDefaultActivity;
                notifyDataSetChanged();
            }
        }

        public boolean getShowDefaultActivity() {
            return this.mShowDefaultActivity;
        }
    }

    private class Callbacks implements AdapterView.OnItemClickListener, View.OnClickListener, View.OnLongClickListener, PopupWindow.OnDismissListener {
        private Callbacks() {
        }

        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            switch (((ActivityChooserViewAdapter) parent.getAdapter()).getItemViewType(position)) {
                case 0:
                    ActivityChooserView.this.dismissPopup();
                    if (!ActivityChooserView.this.mIsSelectingDefaultActivity) {
                        int position2 = ActivityChooserView.this.mAdapter.getShowDefaultActivity() ? position : position + 1;
                        Intent launchIntent = ActivityChooserView.this.mAdapter.getDataModel().chooseActivity(position2);
                        if (launchIntent != null) {
                            launchIntent.addFlags(524288);
                            startActivity(launchIntent, ActivityChooserView.this.mAdapter.getDataModel().getActivity(position2));
                            return;
                        }
                        return;
                    } else if (position > 0) {
                        ActivityChooserView.this.mAdapter.getDataModel().setDefaultActivity(position);
                        return;
                    } else {
                        return;
                    }
                case 1:
                    ActivityChooserView.this.showPopupUnchecked(Integer.MAX_VALUE);
                    return;
                default:
                    throw new IllegalArgumentException();
            }
        }

        public void onClick(View view) {
            if (view == ActivityChooserView.this.mDefaultActivityButton) {
                ActivityChooserView.this.dismissPopup();
                ResolveInfo defaultActivity = ActivityChooserView.this.mAdapter.getDefaultActivity();
                Intent launchIntent = ActivityChooserView.this.mAdapter.getDataModel().chooseActivity(ActivityChooserView.this.mAdapter.getDataModel().getActivityIndex(defaultActivity));
                if (launchIntent != null) {
                    launchIntent.addFlags(524288);
                    startActivity(launchIntent, defaultActivity);
                }
            } else if (view == ActivityChooserView.this.mExpandActivityOverflowButton) {
                boolean unused = ActivityChooserView.this.mIsSelectingDefaultActivity = false;
                ActivityChooserView.this.showPopupUnchecked(ActivityChooserView.this.mInitialActivityCount);
            } else {
                throw new IllegalArgumentException();
            }
        }

        public boolean onLongClick(View view) {
            if (view == ActivityChooserView.this.mDefaultActivityButton) {
                if (ActivityChooserView.this.mAdapter.getCount() > 0) {
                    boolean unused = ActivityChooserView.this.mIsSelectingDefaultActivity = true;
                    ActivityChooserView.this.showPopupUnchecked(ActivityChooserView.this.mInitialActivityCount);
                }
                return true;
            }
            throw new IllegalArgumentException();
        }

        public void onDismiss() {
            notifyOnDismissListener();
            if (ActivityChooserView.this.mProvider != null) {
                ActivityChooserView.this.mProvider.subUiVisibilityChanged(false);
            }
        }

        private void notifyOnDismissListener() {
            if (ActivityChooserView.this.mOnDismissListener != null) {
                ActivityChooserView.this.mOnDismissListener.onDismiss();
            }
        }

        private void startActivity(Intent intent, ResolveInfo resolveInfo) {
            try {
                ActivityChooserView.this.mContext.startActivity(intent);
            } catch (RuntimeException e) {
                CharSequence appLabel = resolveInfo.loadLabel(ActivityChooserView.this.mContext.getPackageManager());
                String message = ActivityChooserView.this.mContext.getString(17039548, new Object[]{appLabel});
                Log.e(ActivityChooserView.LOG_TAG, message);
                Toast.makeText(ActivityChooserView.this.mContext, (CharSequence) message, 0).show();
            }
        }
    }

    public ActivityChooserView(Context context) {
        this(context, null);
    }

    public ActivityChooserView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ActivityChooserView(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public ActivityChooserView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        this.mModelDataSetOberver = new DataSetObserver() {
            public void onChanged() {
                super.onChanged();
                ActivityChooserView.this.mAdapter.notifyDataSetChanged();
            }

            public void onInvalidated() {
                super.onInvalidated();
                ActivityChooserView.this.mAdapter.notifyDataSetInvalidated();
            }
        };
        this.mOnGlobalLayoutListener = new ViewTreeObserver.OnGlobalLayoutListener() {
            public void onGlobalLayout() {
                if (!ActivityChooserView.this.isShowingPopup()) {
                    return;
                }
                if (!ActivityChooserView.this.isShown()) {
                    ActivityChooserView.this.getListPopupWindow().dismiss();
                    return;
                }
                ActivityChooserView.this.getListPopupWindow().show();
                if (ActivityChooserView.this.mProvider != null) {
                    ActivityChooserView.this.mProvider.subUiVisibilityChanged(true);
                }
            }
        };
        this.mInitialActivityCount = 4;
        TypedArray attributesArray = context.obtainStyledAttributes(attrs, R.styleable.ActivityChooserView, defStyleAttr, defStyleRes);
        this.mInitialActivityCount = attributesArray.getInt(1, 4);
        Drawable expandActivityOverflowButtonDrawable = attributesArray.getDrawable(0);
        attributesArray.recycle();
        LayoutInflater.from(this.mContext).inflate(17367075, (ViewGroup) this, true);
        this.mCallbacks = new Callbacks();
        this.mActivityChooserContent = (LinearLayout) findViewById(16908706);
        this.mActivityChooserContentBackground = this.mActivityChooserContent.getBackground();
        this.mDefaultActivityButton = (FrameLayout) findViewById(16908858);
        this.mDefaultActivityButton.setOnClickListener(this.mCallbacks);
        this.mDefaultActivityButton.setOnLongClickListener(this.mCallbacks);
        this.mDefaultActivityButtonImage = (ImageView) this.mDefaultActivityButton.findViewById(16908982);
        FrameLayout expandButton = (FrameLayout) findViewById(16908880);
        expandButton.setOnClickListener(this.mCallbacks);
        expandButton.setAccessibilityDelegate(new View.AccessibilityDelegate() {
            public void onInitializeAccessibilityNodeInfo(View host, AccessibilityNodeInfo info) {
                super.onInitializeAccessibilityNodeInfo(host, info);
                info.setCanOpenPopup(true);
            }
        });
        expandButton.setOnTouchListener(new ForwardingListener(expandButton) {
            public ShowableListMenu getPopup() {
                return ActivityChooserView.this.getListPopupWindow();
            }

            /* access modifiers changed from: protected */
            public boolean onForwardingStarted() {
                ActivityChooserView.this.showPopup();
                return true;
            }

            /* access modifiers changed from: protected */
            public boolean onForwardingStopped() {
                ActivityChooserView.this.dismissPopup();
                return true;
            }
        });
        this.mExpandActivityOverflowButton = expandButton;
        this.mExpandActivityOverflowButtonImage = (ImageView) expandButton.findViewById(16908982);
        this.mExpandActivityOverflowButtonImage.setImageDrawable(expandActivityOverflowButtonDrawable);
        this.mAdapter = new ActivityChooserViewAdapter();
        this.mAdapter.registerDataSetObserver(new DataSetObserver() {
            public void onChanged() {
                super.onChanged();
                ActivityChooserView.this.updateAppearance();
            }
        });
        Resources resources = context.getResources();
        this.mListPopupMaxWidth = Math.max(resources.getDisplayMetrics().widthPixels / 2, resources.getDimensionPixelSize(17104972));
    }

    public void setActivityChooserModel(ActivityChooserModel dataModel) {
        this.mAdapter.setDataModel(dataModel);
        if (isShowingPopup()) {
            dismissPopup();
            showPopup();
        }
    }

    public void setExpandActivityOverflowButtonDrawable(Drawable drawable) {
        this.mExpandActivityOverflowButtonImage.setImageDrawable(drawable);
    }

    public void setExpandActivityOverflowButtonContentDescription(int resourceId) {
        this.mExpandActivityOverflowButtonImage.setContentDescription(this.mContext.getString(resourceId));
    }

    public void setProvider(ActionProvider provider) {
        this.mProvider = provider;
    }

    public boolean showPopup() {
        if (isShowingPopup() || !this.mIsAttachedToWindow) {
            return false;
        }
        this.mIsSelectingDefaultActivity = false;
        showPopupUnchecked(this.mInitialActivityCount);
        return true;
    }

    /* access modifiers changed from: private */
    public void showPopupUnchecked(int maxActivityCount) {
        if (this.mAdapter.getDataModel() != null) {
            getViewTreeObserver().addOnGlobalLayoutListener(this.mOnGlobalLayoutListener);
            boolean defaultActivityButtonShown = this.mDefaultActivityButton.getVisibility() == 0;
            int activityCount = this.mAdapter.getActivityCount();
            int maxActivityCountOffset = defaultActivityButtonShown ? 1 : 0;
            if (maxActivityCount == Integer.MAX_VALUE || activityCount <= maxActivityCount + maxActivityCountOffset) {
                this.mAdapter.setShowFooterView(false);
                this.mAdapter.setMaxActivityCount(maxActivityCount);
            } else {
                this.mAdapter.setShowFooterView(true);
                this.mAdapter.setMaxActivityCount(maxActivityCount - 1);
            }
            ListPopupWindow popupWindow = getListPopupWindow();
            if (!popupWindow.isShowing()) {
                if (this.mIsSelectingDefaultActivity || !defaultActivityButtonShown) {
                    this.mAdapter.setShowDefaultActivity(true, defaultActivityButtonShown);
                } else {
                    this.mAdapter.setShowDefaultActivity(false, false);
                }
                popupWindow.setContentWidth(Math.min(this.mAdapter.measureContentWidth(), this.mListPopupMaxWidth));
                popupWindow.show();
                if (this.mProvider != null) {
                    this.mProvider.subUiVisibilityChanged(true);
                }
                popupWindow.getListView().setContentDescription(this.mContext.getString(17039547));
                popupWindow.getListView().setSelector((Drawable) new ColorDrawable(0));
                return;
            }
            return;
        }
        throw new IllegalStateException("No data model. Did you call #setDataModel?");
    }

    public boolean dismissPopup() {
        if (isShowingPopup()) {
            getListPopupWindow().dismiss();
            ViewTreeObserver viewTreeObserver = getViewTreeObserver();
            if (viewTreeObserver.isAlive()) {
                viewTreeObserver.removeOnGlobalLayoutListener(this.mOnGlobalLayoutListener);
            }
        }
        return true;
    }

    public boolean isShowingPopup() {
        return getListPopupWindow().isShowing();
    }

    /* access modifiers changed from: protected */
    public void onAttachedToWindow() {
        super.onAttachedToWindow();
        ActivityChooserModel dataModel = this.mAdapter.getDataModel();
        if (dataModel != null) {
            dataModel.registerObserver(this.mModelDataSetOberver);
        }
        this.mIsAttachedToWindow = true;
    }

    /* access modifiers changed from: protected */
    public void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        ActivityChooserModel dataModel = this.mAdapter.getDataModel();
        if (dataModel != null) {
            dataModel.unregisterObserver(this.mModelDataSetOberver);
        }
        ViewTreeObserver viewTreeObserver = getViewTreeObserver();
        if (viewTreeObserver.isAlive()) {
            viewTreeObserver.removeOnGlobalLayoutListener(this.mOnGlobalLayoutListener);
        }
        if (isShowingPopup()) {
            dismissPopup();
        }
        this.mIsAttachedToWindow = false;
    }

    /* access modifiers changed from: protected */
    public void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        View child = this.mActivityChooserContent;
        if (this.mDefaultActivityButton.getVisibility() != 0) {
            heightMeasureSpec = View.MeasureSpec.makeMeasureSpec(View.MeasureSpec.getSize(heightMeasureSpec), 1073741824);
        }
        measureChild(child, widthMeasureSpec, heightMeasureSpec);
        setMeasuredDimension(child.getMeasuredWidth(), child.getMeasuredHeight());
    }

    /* access modifiers changed from: protected */
    public void onLayout(boolean changed, int left, int top, int right, int bottom) {
        this.mActivityChooserContent.layout(0, 0, right - left, bottom - top);
        if (!isShowingPopup()) {
            dismissPopup();
        }
    }

    public ActivityChooserModel getDataModel() {
        return this.mAdapter.getDataModel();
    }

    public void setOnDismissListener(PopupWindow.OnDismissListener listener) {
        this.mOnDismissListener = listener;
    }

    public void setInitialActivityCount(int itemCount) {
        this.mInitialActivityCount = itemCount;
    }

    public void setDefaultActionButtonContentDescription(int resourceId) {
        this.mDefaultActionButtonContentDescription = resourceId;
    }

    /* access modifiers changed from: private */
    public ListPopupWindow getListPopupWindow() {
        if (this.mListPopupWindow == null) {
            this.mListPopupWindow = new ListPopupWindow(getContext());
            this.mListPopupWindow.setAdapter(this.mAdapter);
            this.mListPopupWindow.setAnchorView(this);
            this.mListPopupWindow.setModal(true);
            this.mListPopupWindow.setOnItemClickListener(this.mCallbacks);
            this.mListPopupWindow.setOnDismissListener(this.mCallbacks);
        }
        return this.mListPopupWindow;
    }

    /* access modifiers changed from: private */
    public void updateAppearance() {
        if (this.mAdapter.getCount() > 0) {
            this.mExpandActivityOverflowButton.setEnabled(true);
        } else {
            this.mExpandActivityOverflowButton.setEnabled(false);
        }
        int activityCount = this.mAdapter.getActivityCount();
        int historySize = this.mAdapter.getHistorySize();
        if (activityCount == 1 || (activityCount > 1 && historySize > 0)) {
            this.mDefaultActivityButton.setVisibility(0);
            ResolveInfo activity = this.mAdapter.getDefaultActivity();
            PackageManager packageManager = this.mContext.getPackageManager();
            this.mDefaultActivityButtonImage.setImageDrawable(activity.loadIcon(packageManager));
            if (this.mDefaultActionButtonContentDescription != 0) {
                CharSequence label = activity.loadLabel(packageManager);
                this.mDefaultActivityButton.setContentDescription(this.mContext.getString(this.mDefaultActionButtonContentDescription, new Object[]{label}));
            }
        } else {
            this.mDefaultActivityButton.setVisibility(8);
        }
        if (this.mDefaultActivityButton.getVisibility() == 0) {
            this.mActivityChooserContent.setBackground(this.mActivityChooserContentBackground);
        } else {
            this.mActivityChooserContent.setBackground(null);
        }
    }
}
