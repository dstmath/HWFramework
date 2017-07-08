package android.widget;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.database.DataSetObserver;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.ActionProvider;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.AccessibilityDelegate;
import android.view.View.MeasureSpec;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.view.accessibility.AccessibilityNodeInfo;
import android.view.inputmethod.EditorInfo;
import android.widget.ActivityChooserModel.ActivityChooserModelClient;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.PopupWindow.OnDismissListener;
import com.android.internal.R;
import com.android.internal.os.HwBootFail;
import com.android.internal.util.Protocol;
import com.android.internal.view.menu.ShowableListMenu;
import huawei.cust.HwCfgFilePolicy;

public class ActivityChooserView extends ViewGroup implements ActivityChooserModelClient {
    private static final String LOG_TAG = "ActivityChooserView";
    private final LinearLayout mActivityChooserContent;
    private final Drawable mActivityChooserContentBackground;
    private final ActivityChooserViewAdapter mAdapter;
    private final Callbacks mCallbacks;
    private int mDefaultActionButtonContentDescription;
    private final FrameLayout mDefaultActivityButton;
    private final ImageView mDefaultActivityButtonImage;
    private final FrameLayout mExpandActivityOverflowButton;
    private final ImageView mExpandActivityOverflowButtonImage;
    private int mInitialActivityCount;
    private boolean mIsAttachedToWindow;
    private boolean mIsSelectingDefaultActivity;
    private final int mListPopupMaxWidth;
    private ListPopupWindow mListPopupWindow;
    private final DataSetObserver mModelDataSetOberver;
    private OnDismissListener mOnDismissListener;
    private final OnGlobalLayoutListener mOnGlobalLayoutListener;
    ActionProvider mProvider;

    /* renamed from: android.widget.ActivityChooserView.4 */
    class AnonymousClass4 extends ForwardingListener {
        AnonymousClass4(View $anonymous0) {
            super($anonymous0);
        }

        public ShowableListMenu getPopup() {
            return ActivityChooserView.this.getListPopupWindow();
        }

        protected boolean onForwardingStarted() {
            ActivityChooserView.this.showPopup();
            return true;
        }

        protected boolean onForwardingStopped() {
            ActivityChooserView.this.dismissPopup();
            return true;
        }
    }

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
            this.mMaxActivityCount = MAX_ACTIVITY_COUNT_DEFAULT;
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
            if (this.mShowFooterView && position == getCount() - 1) {
                return ITEM_VIEW_TYPE_FOOTER;
            }
            return ITEM_VIEW_TYPE_ACTIVITY;
        }

        public int getViewTypeCount() {
            return ITEM_VIEW_TYPE_COUNT;
        }

        public int getCount() {
            int activityCount = this.mDataModel.getActivityCount();
            if (!(this.mShowDefaultActivity || this.mDataModel.getDefaultActivity() == null)) {
                activityCount--;
            }
            int count = Math.min(activityCount, this.mMaxActivityCount);
            if (this.mShowFooterView) {
                return count + ITEM_VIEW_TYPE_FOOTER;
            }
            return count;
        }

        public Object getItem(int position) {
            switch (getItemViewType(position)) {
                case ITEM_VIEW_TYPE_ACTIVITY /*0*/:
                    if (!(this.mShowDefaultActivity || this.mDataModel.getDefaultActivity() == null)) {
                        position += ITEM_VIEW_TYPE_FOOTER;
                    }
                    return this.mDataModel.getActivity(position);
                case ITEM_VIEW_TYPE_FOOTER /*1*/:
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
                case ITEM_VIEW_TYPE_ACTIVITY /*0*/:
                    if (convertView == null || convertView.getId() != R.id.list_item) {
                        convertView = LayoutInflater.from(ActivityChooserView.this.getContext()).inflate((int) R.layout.activity_chooser_view_list_item, parent, false);
                    }
                    PackageManager packageManager = ActivityChooserView.this.mContext.getPackageManager();
                    ResolveInfo activity = (ResolveInfo) getItem(position);
                    ((ImageView) convertView.findViewById(R.id.icon)).setImageDrawable(activity.loadIcon(packageManager));
                    ((TextView) convertView.findViewById(R.id.title)).setText(activity.loadLabel(packageManager));
                    if (this.mShowDefaultActivity && position == 0 && this.mHighlightDefaultActivity) {
                        convertView.setActivated(true);
                    } else {
                        convertView.setActivated(false);
                    }
                    return convertView;
                case ITEM_VIEW_TYPE_FOOTER /*1*/:
                    if (convertView == null || convertView.getId() != ITEM_VIEW_TYPE_FOOTER) {
                        convertView = LayoutInflater.from(ActivityChooserView.this.getContext()).inflate((int) R.layout.activity_chooser_view_list_item, parent, false);
                        convertView.setId(ITEM_VIEW_TYPE_FOOTER);
                        ((TextView) convertView.findViewById(R.id.title)).setText(ActivityChooserView.this.mContext.getString(R.string.activity_chooser_view_see_all));
                    }
                    return convertView;
                default:
                    throw new IllegalArgumentException();
            }
        }

        public int measureContentWidth() {
            int oldMaxActivityCount = this.mMaxActivityCount;
            this.mMaxActivityCount = MAX_ACTIVITY_COUNT_UNLIMITED;
            int contentWidth = ITEM_VIEW_TYPE_ACTIVITY;
            View itemView = null;
            int widthMeasureSpec = MeasureSpec.makeMeasureSpec(ITEM_VIEW_TYPE_ACTIVITY, ITEM_VIEW_TYPE_ACTIVITY);
            int heightMeasureSpec = MeasureSpec.makeMeasureSpec(ITEM_VIEW_TYPE_ACTIVITY, ITEM_VIEW_TYPE_ACTIVITY);
            int count = getCount();
            for (int i = ITEM_VIEW_TYPE_ACTIVITY; i < count; i += ITEM_VIEW_TYPE_FOOTER) {
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

    private class Callbacks implements OnItemClickListener, OnClickListener, OnLongClickListener, OnDismissListener {
        private Callbacks() {
        }

        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            switch (((ActivityChooserViewAdapter) parent.getAdapter()).getItemViewType(position)) {
                case HwCfgFilePolicy.GLOBAL /*0*/:
                    ActivityChooserView.this.dismissPopup();
                    if (!ActivityChooserView.this.mIsSelectingDefaultActivity) {
                        if (!ActivityChooserView.this.mAdapter.getShowDefaultActivity()) {
                            position++;
                        }
                        Intent launchIntent = ActivityChooserView.this.mAdapter.getDataModel().chooseActivity(position);
                        if (launchIntent != null) {
                            launchIntent.addFlags(Protocol.BASE_CONNECTIVITY_MANAGER);
                            startActivity(launchIntent, ActivityChooserView.this.mAdapter.getDataModel().getActivity(position));
                        }
                    } else if (position > 0) {
                        ActivityChooserView.this.mAdapter.getDataModel().setDefaultActivity(position);
                    }
                case HwCfgFilePolicy.EMUI /*1*/:
                    ActivityChooserView.this.showPopupUnchecked(HwBootFail.STAGE_BOOT_SUCCESS);
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
                    launchIntent.addFlags(Protocol.BASE_CONNECTIVITY_MANAGER);
                    startActivity(launchIntent, defaultActivity);
                }
            } else if (view == ActivityChooserView.this.mExpandActivityOverflowButton) {
                ActivityChooserView.this.mIsSelectingDefaultActivity = false;
                ActivityChooserView.this.showPopupUnchecked(ActivityChooserView.this.mInitialActivityCount);
            } else {
                throw new IllegalArgumentException();
            }
        }

        public boolean onLongClick(View view) {
            if (view == ActivityChooserView.this.mDefaultActivityButton) {
                if (ActivityChooserView.this.mAdapter.getCount() > 0) {
                    ActivityChooserView.this.mIsSelectingDefaultActivity = true;
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
                CharSequence message = ActivityChooserView.this.mContext.getString(R.string.activitychooserview_choose_application_error, new Object[]{appLabel});
                Log.e(ActivityChooserView.LOG_TAG, message);
                Toast.makeText(ActivityChooserView.this.mContext, message, 0).show();
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
        this.mOnGlobalLayoutListener = new OnGlobalLayoutListener() {
            public void onGlobalLayout() {
                if (!ActivityChooserView.this.isShowingPopup()) {
                    return;
                }
                if (ActivityChooserView.this.isShown()) {
                    ActivityChooserView.this.getListPopupWindow().show();
                    if (ActivityChooserView.this.mProvider != null) {
                        ActivityChooserView.this.mProvider.subUiVisibilityChanged(true);
                        return;
                    }
                    return;
                }
                ActivityChooserView.this.getListPopupWindow().dismiss();
            }
        };
        this.mInitialActivityCount = 4;
        TypedArray attributesArray = context.obtainStyledAttributes(attrs, R.styleable.ActivityChooserView, defStyleAttr, defStyleRes);
        this.mInitialActivityCount = attributesArray.getInt(0, 4);
        Drawable expandActivityOverflowButtonDrawable = attributesArray.getDrawable(1);
        attributesArray.recycle();
        LayoutInflater.from(this.mContext).inflate((int) R.layout.activity_chooser_view, (ViewGroup) this, true);
        this.mCallbacks = new Callbacks();
        this.mActivityChooserContent = (LinearLayout) findViewById(R.id.activity_chooser_view_content);
        this.mActivityChooserContentBackground = this.mActivityChooserContent.getBackground();
        this.mDefaultActivityButton = (FrameLayout) findViewById(R.id.default_activity_button);
        this.mDefaultActivityButton.setOnClickListener(this.mCallbacks);
        this.mDefaultActivityButton.setOnLongClickListener(this.mCallbacks);
        this.mDefaultActivityButtonImage = (ImageView) this.mDefaultActivityButton.findViewById(R.id.image);
        FrameLayout expandButton = (FrameLayout) findViewById(R.id.expand_activities_button);
        expandButton.setOnClickListener(this.mCallbacks);
        expandButton.setAccessibilityDelegate(new AccessibilityDelegate() {
            public void onInitializeAccessibilityNodeInfo(View host, AccessibilityNodeInfo info) {
                super.onInitializeAccessibilityNodeInfo(host, info);
                info.setCanOpenPopup(true);
            }
        });
        expandButton.setOnTouchListener(new AnonymousClass4(expandButton));
        this.mExpandActivityOverflowButton = expandButton;
        this.mExpandActivityOverflowButtonImage = (ImageView) expandButton.findViewById(R.id.image);
        this.mExpandActivityOverflowButtonImage.setImageDrawable(expandActivityOverflowButtonDrawable);
        this.mAdapter = new ActivityChooserViewAdapter();
        this.mAdapter.registerDataSetObserver(new DataSetObserver() {
            public void onChanged() {
                super.onChanged();
                ActivityChooserView.this.updateAppearance();
            }
        });
        Resources resources = context.getResources();
        this.mListPopupMaxWidth = Math.max(resources.getDisplayMetrics().widthPixels / 2, resources.getDimensionPixelSize(R.dimen.config_prefDialogWidth));
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

    private void showPopupUnchecked(int maxActivityCount) {
        if (this.mAdapter.getDataModel() == null) {
            throw new IllegalStateException("No data model. Did you call #setDataModel?");
        }
        getViewTreeObserver().addOnGlobalLayoutListener(this.mOnGlobalLayoutListener);
        boolean defaultActivityButtonShown = this.mDefaultActivityButton.getVisibility() == 0;
        int activityCount = this.mAdapter.getActivityCount();
        int maxActivityCountOffset = defaultActivityButtonShown ? 1 : 0;
        if (maxActivityCount == HwBootFail.STAGE_BOOT_SUCCESS || activityCount <= maxActivityCount + maxActivityCountOffset) {
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
            popupWindow.getListView().setContentDescription(this.mContext.getString(R.string.activitychooserview_choose_application));
        }
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

    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        ActivityChooserModel dataModel = this.mAdapter.getDataModel();
        if (dataModel != null) {
            dataModel.registerObserver(this.mModelDataSetOberver);
        }
        this.mIsAttachedToWindow = true;
    }

    protected void onDetachedFromWindow() {
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

    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        View child = this.mActivityChooserContent;
        if (this.mDefaultActivityButton.getVisibility() != 0) {
            heightMeasureSpec = MeasureSpec.makeMeasureSpec(MeasureSpec.getSize(heightMeasureSpec), EditorInfo.IME_FLAG_NO_ENTER_ACTION);
        }
        measureChild(child, widthMeasureSpec, heightMeasureSpec);
        setMeasuredDimension(child.getMeasuredWidth(), child.getMeasuredHeight());
    }

    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        this.mActivityChooserContent.layout(0, 0, right - left, bottom - top);
        if (!isShowingPopup()) {
            dismissPopup();
        }
    }

    public ActivityChooserModel getDataModel() {
        return this.mAdapter.getDataModel();
    }

    public void setOnDismissListener(OnDismissListener listener) {
        this.mOnDismissListener = listener;
    }

    public void setInitialActivityCount(int itemCount) {
        this.mInitialActivityCount = itemCount;
    }

    public void setDefaultActionButtonContentDescription(int resourceId) {
        this.mDefaultActionButtonContentDescription = resourceId;
    }

    private ListPopupWindow getListPopupWindow() {
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

    private void updateAppearance() {
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
