package com.android.internal.app;

import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.DialogInterface.OnDismissListener;
import android.content.DialogInterface.OnKeyListener;
import android.content.DialogInterface.OnMultiChoiceClickListener;
import android.content.res.TypedArray;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.ViewParent;
import android.view.ViewStub;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckedTextView;
import android.widget.CursorAdapter;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import com.android.internal.R;
import com.android.internal.util.Protocol;
import com.huawei.hwperformance.HwPerformance;
import com.huawei.pgmng.plug.PGSdk;
import java.lang.ref.WeakReference;

public class AlertController {
    private ListAdapter mAdapter;
    private int mAlertDialogLayout;
    private final OnClickListener mButtonHandler;
    private Button mButtonNegative;
    private Message mButtonNegativeMessage;
    private CharSequence mButtonNegativeText;
    private Button mButtonNeutral;
    private Message mButtonNeutralMessage;
    private CharSequence mButtonNeutralText;
    private int mButtonPanelLayoutHint;
    private int mButtonPanelSideLayout;
    private Button mButtonPositive;
    private Message mButtonPositiveMessage;
    private CharSequence mButtonPositiveText;
    private int mCheckedItem;
    private final Context mContext;
    private View mCustomTitleView;
    private final DialogInterface mDialogInterface;
    private boolean mForceInverseBackground;
    private Handler mHandler;
    private Drawable mIcon;
    private int mIconId;
    private ImageView mIconView;
    private int mListItemLayout;
    private int mListLayout;
    private ListView mListView;
    private CharSequence mMessage;
    private TextView mMessageView;
    private int mMultiChoiceItemLayout;
    private ScrollView mScrollView;
    private boolean mShowTitle;
    private int mSingleChoiceItemLayout;
    private CharSequence mTitle;
    private TextView mTitleView;
    private View mView;
    private int mViewLayoutResId;
    private int mViewSpacingBottom;
    private int mViewSpacingLeft;
    private int mViewSpacingRight;
    private boolean mViewSpacingSpecified;
    private int mViewSpacingTop;
    private final Window mWindow;

    public static class AlertParams {
        public ListAdapter mAdapter;
        public boolean mCancelable;
        public int mCheckedItem;
        public boolean[] mCheckedItems;
        public final Context mContext;
        public Cursor mCursor;
        public View mCustomTitleView;
        public boolean mForceInverseBackground;
        public Drawable mIcon;
        public int mIconAttrId;
        public int mIconId;
        public final LayoutInflater mInflater;
        public String mIsCheckedColumn;
        public boolean mIsMultiChoice;
        public boolean mIsSingleChoice;
        public CharSequence[] mItems;
        public String mLabelColumn;
        public CharSequence mMessage;
        public DialogInterface.OnClickListener mNegativeButtonListener;
        public CharSequence mNegativeButtonText;
        public DialogInterface.OnClickListener mNeutralButtonListener;
        public CharSequence mNeutralButtonText;
        public OnCancelListener mOnCancelListener;
        public OnMultiChoiceClickListener mOnCheckboxClickListener;
        public DialogInterface.OnClickListener mOnClickListener;
        public OnDismissListener mOnDismissListener;
        public OnItemSelectedListener mOnItemSelectedListener;
        public OnKeyListener mOnKeyListener;
        public OnPrepareListViewListener mOnPrepareListViewListener;
        public DialogInterface.OnClickListener mPositiveButtonListener;
        public CharSequence mPositiveButtonText;
        public boolean mRecycleOnMeasure;
        public CharSequence mTitle;
        public View mView;
        public int mViewLayoutResId;
        public int mViewSpacingBottom;
        public int mViewSpacingLeft;
        public int mViewSpacingRight;
        public boolean mViewSpacingSpecified;
        public int mViewSpacingTop;

        /* renamed from: com.android.internal.app.AlertController.AlertParams.1 */
        class AnonymousClass1 extends ArrayAdapter<CharSequence> {
            final /* synthetic */ RecycleListView val$listView;

            AnonymousClass1(Context $anonymous0, int $anonymous1, int $anonymous2, CharSequence[] $anonymous3, RecycleListView val$listView) {
                this.val$listView = val$listView;
                super($anonymous0, $anonymous1, $anonymous2, (Object[]) $anonymous3);
            }

            public View getView(int position, View convertView, ViewGroup parent) {
                View view = super.getView(position, convertView, parent);
                if (AlertParams.this.mCheckedItems != null && AlertParams.this.mCheckedItems[position]) {
                    this.val$listView.setItemChecked(position, true);
                }
                return view;
            }
        }

        /* renamed from: com.android.internal.app.AlertController.AlertParams.2 */
        class AnonymousClass2 extends CursorAdapter {
            private final int mIsCheckedIndex;
            private final int mLabelIndex;
            final /* synthetic */ AlertController val$dialog;
            final /* synthetic */ RecycleListView val$listView;

            AnonymousClass2(Context $anonymous0, Cursor $anonymous1, boolean $anonymous2, RecycleListView val$listView, AlertController val$dialog) {
                this.val$listView = val$listView;
                this.val$dialog = val$dialog;
                super($anonymous0, $anonymous1, $anonymous2);
                Cursor cursor = getCursor();
                this.mLabelIndex = cursor.getColumnIndexOrThrow(AlertParams.this.mLabelColumn);
                this.mIsCheckedIndex = cursor.getColumnIndexOrThrow(AlertParams.this.mIsCheckedColumn);
            }

            public void bindView(View view, Context context, Cursor cursor) {
                boolean z = true;
                ((CheckedTextView) view.findViewById(R.id.text1)).setText(cursor.getString(this.mLabelIndex));
                RecycleListView recycleListView = this.val$listView;
                int position = cursor.getPosition();
                if (cursor.getInt(this.mIsCheckedIndex) != 1) {
                    z = false;
                }
                recycleListView.setItemChecked(position, z);
            }

            public View newView(Context context, Cursor cursor, ViewGroup parent) {
                return AlertParams.this.mInflater.inflate(this.val$dialog.mMultiChoiceItemLayout, parent, false);
            }
        }

        /* renamed from: com.android.internal.app.AlertController.AlertParams.3 */
        class AnonymousClass3 implements OnItemClickListener {
            final /* synthetic */ AlertController val$dialog;

            AnonymousClass3(AlertController val$dialog) {
                this.val$dialog = val$dialog;
            }

            public void onItemClick(AdapterView<?> adapterView, View v, int position, long id) {
                AlertParams.this.mOnClickListener.onClick(this.val$dialog.mDialogInterface, position);
                if (!AlertParams.this.mIsSingleChoice) {
                    this.val$dialog.mDialogInterface.dismiss();
                }
            }
        }

        /* renamed from: com.android.internal.app.AlertController.AlertParams.4 */
        class AnonymousClass4 implements OnItemClickListener {
            final /* synthetic */ AlertController val$dialog;
            final /* synthetic */ RecycleListView val$listView;

            AnonymousClass4(RecycleListView val$listView, AlertController val$dialog) {
                this.val$listView = val$listView;
                this.val$dialog = val$dialog;
            }

            public void onItemClick(AdapterView<?> adapterView, View v, int position, long id) {
                if (AlertParams.this.mCheckedItems != null) {
                    AlertParams.this.mCheckedItems[position] = this.val$listView.isItemChecked(position);
                }
                AlertParams.this.mOnCheckboxClickListener.onClick(this.val$dialog.mDialogInterface, position, this.val$listView.isItemChecked(position));
            }
        }

        public interface OnPrepareListViewListener {
            void onPrepareListView(ListView listView);
        }

        public AlertParams(Context context) {
            this.mIconId = 0;
            this.mIconAttrId = 0;
            this.mViewSpacingSpecified = false;
            this.mCheckedItem = -1;
            this.mRecycleOnMeasure = true;
            this.mContext = context;
            this.mCancelable = true;
            this.mInflater = (LayoutInflater) context.getSystemService("layout_inflater");
        }

        public void apply(AlertController dialog) {
            if (this.mCustomTitleView != null) {
                dialog.setCustomTitle(this.mCustomTitleView);
            } else {
                if (this.mTitle != null) {
                    dialog.setTitle(this.mTitle);
                }
                if (this.mIcon != null) {
                    dialog.setIcon(this.mIcon);
                }
                if (this.mIconId != 0) {
                    dialog.setIcon(this.mIconId);
                }
                if (this.mIconAttrId != 0) {
                    dialog.setIcon(dialog.getIconAttributeResId(this.mIconAttrId));
                }
            }
            if (this.mMessage != null) {
                dialog.setMessage(this.mMessage);
            }
            if (this.mPositiveButtonText != null) {
                dialog.setButton(-1, this.mPositiveButtonText, this.mPositiveButtonListener, null);
            }
            if (this.mNegativeButtonText != null) {
                dialog.setButton(-2, this.mNegativeButtonText, this.mNegativeButtonListener, null);
            }
            if (this.mNeutralButtonText != null) {
                dialog.setButton(-3, this.mNeutralButtonText, this.mNeutralButtonListener, null);
            }
            if (this.mForceInverseBackground) {
                dialog.setInverseBackgroundForced(true);
            }
            if (this.mItems == null && this.mCursor == null) {
                if (this.mAdapter != null) {
                }
                if (this.mView == null) {
                    if (this.mViewSpacingSpecified) {
                        dialog.setView(this.mView);
                    }
                    dialog.setView(this.mView, this.mViewSpacingLeft, this.mViewSpacingTop, this.mViewSpacingRight, this.mViewSpacingBottom);
                } else if (this.mViewLayoutResId != 0) {
                    dialog.setView(this.mViewLayoutResId);
                }
            }
            createListView(dialog);
            if (this.mView == null) {
                if (this.mViewLayoutResId != 0) {
                    dialog.setView(this.mViewLayoutResId);
                }
            } else if (this.mViewSpacingSpecified) {
                dialog.setView(this.mView);
            } else {
                dialog.setView(this.mView, this.mViewSpacingLeft, this.mViewSpacingTop, this.mViewSpacingRight, this.mViewSpacingBottom);
            }
        }

        private void createListView(AlertController dialog) {
            ListAdapter adapter;
            RecycleListView listView = (RecycleListView) this.mInflater.inflate(dialog.mListLayout, null);
            if (!this.mIsMultiChoice) {
                int layout;
                if (this.mIsSingleChoice) {
                    layout = dialog.mSingleChoiceItemLayout;
                } else {
                    layout = dialog.mListItemLayout;
                }
                if (this.mCursor != null) {
                    adapter = new SimpleCursorAdapter(this.mContext, layout, this.mCursor, new String[]{this.mLabelColumn}, new int[]{R.id.text1});
                } else if (this.mAdapter != null) {
                    adapter = this.mAdapter;
                } else {
                    adapter = new CheckedItemAdapter(this.mContext, layout, R.id.text1, this.mItems);
                }
            } else if (this.mCursor == null) {
                adapter = new AnonymousClass1(this.mContext, dialog.mMultiChoiceItemLayout, R.id.text1, this.mItems, listView);
            } else {
                ListAdapter anonymousClass2 = new AnonymousClass2(this.mContext, this.mCursor, false, listView, dialog);
            }
            if (this.mOnPrepareListViewListener != null) {
                this.mOnPrepareListViewListener.onPrepareListView(listView);
            }
            dialog.mAdapter = adapter;
            dialog.mCheckedItem = this.mCheckedItem;
            if (this.mOnClickListener != null) {
                listView.setOnItemClickListener(new AnonymousClass3(dialog));
            } else if (this.mOnCheckboxClickListener != null) {
                listView.setOnItemClickListener(new AnonymousClass4(listView, dialog));
            }
            if (this.mOnItemSelectedListener != null) {
                listView.setOnItemSelectedListener(this.mOnItemSelectedListener);
            }
            if (this.mIsSingleChoice) {
                listView.setChoiceMode(1);
            } else if (this.mIsMultiChoice) {
                listView.setChoiceMode(2);
            }
            listView.mRecycleOnMeasure = this.mRecycleOnMeasure;
            dialog.mListView = listView;
        }
    }

    private static final class ButtonHandler extends Handler {
        private static final int MSG_DISMISS_DIALOG = 1;
        private WeakReference<DialogInterface> mDialog;

        public ButtonHandler(DialogInterface dialog) {
            this.mDialog = new WeakReference(dialog);
        }

        public void handleMessage(Message msg) {
            switch (msg.what) {
                case HwPerformance.REQUEST_INPUT_INVALID /*-3*/:
                case HwPerformance.REQUEST_PLATFORM_NOTSUPPORT /*-2*/:
                case PGSdk.TYPE_UNKNOW /*-1*/:
                    ((DialogInterface.OnClickListener) msg.obj).onClick((DialogInterface) this.mDialog.get(), msg.what);
                case MSG_DISMISS_DIALOG /*1*/:
                    ((DialogInterface) msg.obj).dismiss();
                default:
            }
        }
    }

    private static class CheckedItemAdapter extends ArrayAdapter<CharSequence> {
        public CheckedItemAdapter(Context context, int resource, int textViewResourceId, CharSequence[] objects) {
            super(context, resource, textViewResourceId, (Object[]) objects);
        }

        public boolean hasStableIds() {
            return true;
        }

        public long getItemId(int position) {
            return (long) position;
        }
    }

    public static class RecycleListView extends ListView {
        private final int mPaddingBottomNoButtons;
        private final int mPaddingTopNoTitle;
        boolean mRecycleOnMeasure;

        public RecycleListView(Context context) {
            this(context, null);
        }

        public RecycleListView(Context context, AttributeSet attrs) {
            super(context, attrs);
            this.mRecycleOnMeasure = true;
            TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.RecycleListView);
            this.mPaddingBottomNoButtons = ta.getDimensionPixelOffset(0, -1);
            this.mPaddingTopNoTitle = ta.getDimensionPixelOffset(1, -1);
        }

        public void setHasDecor(boolean hasTitle, boolean hasButtons) {
            if (!hasButtons || !hasTitle) {
                setPadding(getPaddingLeft(), hasTitle ? getPaddingTop() : this.mPaddingTopNoTitle, getPaddingRight(), hasButtons ? getPaddingBottom() : this.mPaddingBottomNoButtons);
            }
        }

        protected boolean recycleOnMeasure() {
            return this.mRecycleOnMeasure;
        }

        protected Drawable initializeVariousScrollIndicators(Context context) {
            return super.initializeVariousScrollIndicators(context);
        }

        protected boolean reSizeScrollIndicators(int[] xy) {
            return super.reSizeScrollIndicators(xy);
        }
    }

    private static boolean shouldCenterSingleButton(Context context) {
        TypedValue outValue = new TypedValue();
        context.getTheme().resolveAttribute(R.attr.alertDialogCenterButtons, outValue, true);
        if (outValue.data != 0) {
            return true;
        }
        return false;
    }

    public AlertController(Context context, DialogInterface di, Window window) {
        this.mViewSpacingSpecified = false;
        this.mIconId = 0;
        this.mCheckedItem = -1;
        this.mButtonPanelLayoutHint = 0;
        this.mButtonHandler = new OnClickListener() {
            public void onClick(View v) {
                Message obtain;
                if (v == AlertController.this.mButtonPositive && AlertController.this.mButtonPositiveMessage != null) {
                    obtain = Message.obtain(AlertController.this.mButtonPositiveMessage);
                } else if (v == AlertController.this.mButtonNegative && AlertController.this.mButtonNegativeMessage != null) {
                    obtain = Message.obtain(AlertController.this.mButtonNegativeMessage);
                } else if (v != AlertController.this.mButtonNeutral || AlertController.this.mButtonNeutralMessage == null) {
                    obtain = null;
                } else {
                    obtain = Message.obtain(AlertController.this.mButtonNeutralMessage);
                }
                if (obtain != null) {
                    obtain.sendToTarget();
                }
                AlertController.this.mHandler.obtainMessage(1, AlertController.this.mDialogInterface).sendToTarget();
            }
        };
        this.mContext = context;
        this.mDialogInterface = di;
        this.mWindow = window;
        this.mHandler = new ButtonHandler(di);
        TypedArray a = context.obtainStyledAttributes(null, R.styleable.AlertDialog, R.attr.alertDialogStyle, 0);
        this.mAlertDialogLayout = a.getResourceId(10, R.layout.alert_dialog);
        this.mButtonPanelSideLayout = a.getResourceId(11, 0);
        this.mListLayout = a.getResourceId(12, R.layout.select_dialog);
        this.mMultiChoiceItemLayout = a.getResourceId(13, R.layout.select_dialog_multichoice);
        this.mSingleChoiceItemLayout = a.getResourceId(14, R.layout.select_dialog_singlechoice);
        this.mListItemLayout = a.getResourceId(15, R.layout.select_dialog_item);
        this.mShowTitle = a.getBoolean(18, true);
        a.recycle();
        window.requestFeature(1);
    }

    static boolean canTextInput(View v) {
        if (v.onCheckIsTextEditor()) {
            return true;
        }
        if (!(v instanceof ViewGroup)) {
            return false;
        }
        ViewGroup vg = (ViewGroup) v;
        int i = vg.getChildCount();
        while (i > 0) {
            i--;
            if (canTextInput(vg.getChildAt(i))) {
                return true;
            }
        }
        return false;
    }

    public void installContent() {
        this.mWindow.setContentView(selectContentView());
        setupView();
    }

    private int selectContentView() {
        if (this.mButtonPanelSideLayout == 0) {
            return this.mAlertDialogLayout;
        }
        if (this.mButtonPanelLayoutHint == 1) {
            return this.mButtonPanelSideLayout;
        }
        return this.mAlertDialogLayout;
    }

    public void setTitle(CharSequence title) {
        this.mTitle = title;
        if (this.mTitleView != null) {
            this.mTitleView.setText(title);
        }
    }

    public void setCustomTitle(View customTitleView) {
        this.mCustomTitleView = customTitleView;
    }

    public void setMessage(CharSequence message) {
        this.mMessage = message;
        if (this.mMessageView != null) {
            this.mMessageView.setText(message);
        }
    }

    public void setView(int layoutResId) {
        this.mView = null;
        this.mViewLayoutResId = layoutResId;
        this.mViewSpacingSpecified = false;
    }

    public void setView(View view) {
        this.mView = view;
        this.mViewLayoutResId = 0;
        this.mViewSpacingSpecified = false;
    }

    public void setView(View view, int viewSpacingLeft, int viewSpacingTop, int viewSpacingRight, int viewSpacingBottom) {
        this.mView = view;
        this.mViewLayoutResId = 0;
        this.mViewSpacingSpecified = true;
        this.mViewSpacingLeft = viewSpacingLeft;
        this.mViewSpacingTop = viewSpacingTop;
        this.mViewSpacingRight = viewSpacingRight;
        this.mViewSpacingBottom = viewSpacingBottom;
    }

    public void setButtonPanelLayoutHint(int layoutHint) {
        this.mButtonPanelLayoutHint = layoutHint;
    }

    public void setButton(int whichButton, CharSequence text, DialogInterface.OnClickListener listener, Message msg) {
        if (msg == null && listener != null) {
            msg = this.mHandler.obtainMessage(whichButton, listener);
        }
        switch (whichButton) {
            case HwPerformance.REQUEST_INPUT_INVALID /*-3*/:
                this.mButtonNeutralText = text;
                this.mButtonNeutralMessage = msg;
            case HwPerformance.REQUEST_PLATFORM_NOTSUPPORT /*-2*/:
                this.mButtonNegativeText = text;
                this.mButtonNegativeMessage = msg;
            case PGSdk.TYPE_UNKNOW /*-1*/:
                this.mButtonPositiveText = text;
                this.mButtonPositiveMessage = msg;
            default:
                throw new IllegalArgumentException("Button does not exist");
        }
    }

    public void setIcon(int resId) {
        this.mIcon = null;
        this.mIconId = resId;
        if (this.mIconView == null) {
            return;
        }
        if (resId != 0) {
            this.mIconView.setVisibility(0);
            this.mIconView.setImageResource(this.mIconId);
            return;
        }
        this.mIconView.setVisibility(8);
    }

    public void setIcon(Drawable icon) {
        this.mIcon = icon;
        this.mIconId = 0;
        if (this.mIconView == null) {
            return;
        }
        if (icon != null) {
            this.mIconView.setVisibility(0);
            this.mIconView.setImageDrawable(icon);
            return;
        }
        this.mIconView.setVisibility(8);
    }

    public int getIconAttributeResId(int attrId) {
        TypedValue out = new TypedValue();
        this.mContext.getTheme().resolveAttribute(attrId, out, true);
        return out.resourceId;
    }

    public void setInverseBackgroundForced(boolean forceInverseBackground) {
        this.mForceInverseBackground = forceInverseBackground;
    }

    public ListView getListView() {
        return this.mListView;
    }

    public Button getButton(int whichButton) {
        switch (whichButton) {
            case HwPerformance.REQUEST_INPUT_INVALID /*-3*/:
                return this.mButtonNeutral;
            case HwPerformance.REQUEST_PLATFORM_NOTSUPPORT /*-2*/:
                return this.mButtonNegative;
            case PGSdk.TYPE_UNKNOW /*-1*/:
                return this.mButtonPositive;
            default:
                return null;
        }
    }

    public boolean onKeyDown(int keyCode, KeyEvent event) {
        return this.mScrollView != null ? this.mScrollView.executeKeyEvent(event) : false;
    }

    public boolean onKeyUp(int keyCode, KeyEvent event) {
        return this.mScrollView != null ? this.mScrollView.executeKeyEvent(event) : false;
    }

    private ViewGroup resolvePanel(View customPanel, View defaultPanel) {
        if (customPanel == null) {
            if (defaultPanel instanceof ViewStub) {
                defaultPanel = ((ViewStub) defaultPanel).inflate();
            }
            return (ViewGroup) defaultPanel;
        }
        if (defaultPanel != null) {
            ViewParent parent = defaultPanel.getParent();
            if (parent instanceof ViewGroup) {
                ((ViewGroup) parent).removeView(defaultPanel);
            }
        }
        if (customPanel instanceof ViewStub) {
            customPanel = ((ViewStub) customPanel).inflate();
        }
        return (ViewGroup) customPanel;
    }

    protected void setupView() {
        View spacer;
        View parentPanel = this.mWindow.findViewById(R.id.parentPanel);
        View defaultTopPanel = parentPanel.findViewById(R.id.topPanel);
        View defaultContentPanel = parentPanel.findViewById(R.id.contentPanel);
        View defaultButtonPanel = parentPanel.findViewById(R.id.buttonPanel);
        ViewGroup customPanel = (ViewGroup) parentPanel.findViewById(R.id.customPanel);
        setupCustomContent(customPanel);
        View customTopPanel = customPanel.findViewById(R.id.topPanel);
        View customContentPanel = customPanel.findViewById(R.id.contentPanel);
        View customButtonPanel = customPanel.findViewById(R.id.buttonPanel);
        ViewGroup topPanel = resolvePanel(customTopPanel, defaultTopPanel);
        ViewGroup contentPanel = resolvePanel(customContentPanel, defaultContentPanel);
        ViewGroup buttonPanel = resolvePanel(customButtonPanel, defaultButtonPanel);
        setupContent(contentPanel);
        setupButtons(buttonPanel);
        setupTitle(topPanel);
        boolean hasCustomPanel = customPanel != null ? customPanel.getVisibility() != 8 : false;
        boolean hasTopPanel = topPanel != null ? topPanel.getVisibility() != 8 : false;
        boolean hasButtonPanel = buttonPanel != null ? buttonPanel.getVisibility() != 8 : false;
        if (!hasButtonPanel) {
            if (contentPanel != null) {
                spacer = contentPanel.findViewById(R.id.textSpacerNoButtons);
                if (spacer != null) {
                    spacer.setVisibility(0);
                }
            }
            this.mWindow.setCloseOnTouchOutsideIfNotSet(true);
        }
        if (hasTopPanel) {
            if (this.mScrollView != null) {
                this.mScrollView.setClipToPadding(true);
            }
            View divider = null;
            if (this.mMessage == null && this.mListView == null && !hasCustomPanel) {
                divider = topPanel.findViewById(R.id.titleDividerTop);
            } else {
                if (!hasCustomPanel) {
                    divider = topPanel.findViewById(R.id.titleDividerNoCustom);
                }
                if (divider == null) {
                    divider = topPanel.findViewById(R.id.titleDivider);
                }
            }
            if (divider != null) {
                divider.setVisibility(0);
            }
        } else if (contentPanel != null) {
            spacer = contentPanel.findViewById(R.id.textSpacerNoTitle);
            if (spacer != null) {
                spacer.setVisibility(0);
            }
        }
        if (this.mListView instanceof RecycleListView) {
            ((RecycleListView) this.mListView).setHasDecor(hasTopPanel, hasButtonPanel);
        }
        setHuaweiScrollIndicators(hasCustomPanel, hasTopPanel, hasButtonPanel);
        TypedArray a = this.mContext.obtainStyledAttributes(null, R.styleable.AlertDialog, R.attr.alertDialogStyle, 0);
        setBackground(a, topPanel, contentPanel, customPanel, buttonPanel, hasTopPanel, hasCustomPanel, hasButtonPanel);
        a.recycle();
    }

    private void setupCustomContent(ViewGroup customPanel) {
        View view;
        boolean hasCustomView = false;
        if (this.mView != null) {
            view = this.mView;
        } else if (this.mViewLayoutResId != 0) {
            view = LayoutInflater.from(this.mContext).inflate(this.mViewLayoutResId, customPanel, false);
        } else {
            view = null;
        }
        if (view != null) {
            hasCustomView = true;
        }
        if (!(hasCustomView && canTextInput(view))) {
            this.mWindow.setFlags(Protocol.BASE_WIFI, Protocol.BASE_WIFI);
        }
        if (hasCustomView) {
            FrameLayout custom = (FrameLayout) this.mWindow.findViewById(R.id.custom);
            custom.addView(view, new LayoutParams(-1, -1));
            if (this.mViewSpacingSpecified) {
                custom.setPadding(this.mViewSpacingLeft, this.mViewSpacingTop, this.mViewSpacingRight, this.mViewSpacingBottom);
            }
            if (this.mListView != null) {
                ((LinearLayout.LayoutParams) customPanel.getLayoutParams()).weight = 0.0f;
                return;
            }
            return;
        }
        customPanel.setVisibility(8);
    }

    private void setupTitle(ViewGroup topPanel) {
        boolean hasTextTitle = false;
        if (this.mCustomTitleView == null || !this.mShowTitle) {
            this.mIconView = (ImageView) this.mWindow.findViewById(R.id.icon);
            if (!TextUtils.isEmpty(this.mTitle)) {
                hasTextTitle = true;
            }
            if (hasTextTitle && this.mShowTitle) {
                this.mTitleView = (TextView) this.mWindow.findViewById(R.id.alertTitle);
                this.mTitleView.setText(this.mTitle);
                if (this.mIconId != 0) {
                    this.mIconView.setImageResource(this.mIconId);
                    return;
                } else if (this.mIcon != null) {
                    this.mIconView.setImageDrawable(this.mIcon);
                    return;
                } else {
                    this.mTitleView.setPadding(this.mIconView.getPaddingLeft(), this.mIconView.getPaddingTop(), this.mIconView.getPaddingRight(), this.mIconView.getPaddingBottom());
                    this.mIconView.setVisibility(8);
                    return;
                }
            }
            this.mWindow.findViewById(R.id.title_template).setVisibility(8);
            this.mIconView.setVisibility(8);
            topPanel.setVisibility(8);
            return;
        }
        topPanel.addView(this.mCustomTitleView, 0, new LinearLayout.LayoutParams(-1, -1));
        this.mWindow.findViewById(R.id.title_template).setVisibility(8);
    }

    private void setupContent(ViewGroup contentPanel) {
        this.mScrollView = (ScrollView) contentPanel.findViewById(R.id.scrollView);
        this.mScrollView.setFocusable(false);
        this.mMessageView = (TextView) contentPanel.findViewById(R.id.message);
        if (this.mMessageView != null) {
            if (this.mMessage != null) {
                this.mMessageView.setText(this.mMessage);
            } else {
                this.mMessageView.setVisibility(8);
                this.mScrollView.removeView(this.mMessageView);
                if (this.mListView != null) {
                    ViewGroup scrollParent = (ViewGroup) this.mScrollView.getParent();
                    int childIndex = scrollParent.indexOfChild(this.mScrollView);
                    scrollParent.removeViewAt(childIndex);
                    scrollParent.addView(this.mListView, childIndex, new LayoutParams(-1, -1));
                } else {
                    contentPanel.setVisibility(8);
                }
            }
        }
    }

    private static void manageScrollIndicators(View v, View upIndicator, View downIndicator) {
        int i = 0;
        if (upIndicator != null) {
            int i2;
            if (v.canScrollVertically(-1)) {
                i2 = 0;
            } else {
                i2 = 4;
            }
            upIndicator.setVisibility(i2);
        }
        if (downIndicator != null) {
            if (!v.canScrollVertically(1)) {
                i = 4;
            }
            downIndicator.setVisibility(i);
        }
    }

    private void setupButtons(ViewGroup buttonPanel) {
        boolean hasButtons = true;
        int whichButtons = 0;
        this.mButtonPositive = (Button) buttonPanel.findViewById(R.id.button1);
        this.mButtonPositive.setOnClickListener(this.mButtonHandler);
        if (TextUtils.isEmpty(this.mButtonPositiveText)) {
            this.mButtonPositive.setVisibility(8);
        } else {
            this.mButtonPositive.setText(this.mButtonPositiveText);
            this.mButtonPositive.setVisibility(0);
            whichButtons = 1;
        }
        this.mButtonNegative = (Button) buttonPanel.findViewById(R.id.button2);
        this.mButtonNegative.setOnClickListener(this.mButtonHandler);
        if (TextUtils.isEmpty(this.mButtonNegativeText)) {
            this.mButtonNegative.setVisibility(8);
        } else {
            this.mButtonNegative.setText(this.mButtonNegativeText);
            this.mButtonNegative.setVisibility(0);
            whichButtons |= 2;
        }
        this.mButtonNeutral = (Button) buttonPanel.findViewById(R.id.button3);
        this.mButtonNeutral.setOnClickListener(this.mButtonHandler);
        if (TextUtils.isEmpty(this.mButtonNeutralText)) {
            this.mButtonNeutral.setVisibility(8);
        } else {
            this.mButtonNeutral.setText(this.mButtonNeutralText);
            this.mButtonNeutral.setVisibility(0);
            whichButtons |= 4;
        }
        if (shouldCenterSingleButton(this.mContext)) {
            if (whichButtons == 1) {
                centerButton(this.mButtonPositive);
            } else if (whichButtons == 2) {
                centerButton(this.mButtonNegative);
            } else if (whichButtons == 4) {
                centerButton(this.mButtonNeutral);
            }
        }
        if (whichButtons == 0) {
            hasButtons = false;
        }
        if (!hasButtons) {
            buttonPanel.setVisibility(8);
        }
    }

    private void centerButton(Button button) {
        LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) button.getLayoutParams();
        params.gravity = 1;
        params.weight = 0.5f;
        button.setLayoutParams(params);
        View leftSpacer = this.mWindow.findViewById(R.id.leftSpacer);
        if (leftSpacer != null) {
            leftSpacer.setVisibility(0);
        }
        View rightSpacer = this.mWindow.findViewById(R.id.rightSpacer);
        if (rightSpacer != null) {
            rightSpacer.setVisibility(0);
        }
    }

    protected void setBackground(TypedArray a, View topPanel, View contentPanel, View customPanel, View buttonPanel, boolean hasTitle, boolean hasCustomView, boolean hasButtons) {
        int fullDark = 0;
        int topDark = 0;
        int centerDark = 0;
        int bottomDark = 0;
        int fullBright = 0;
        int topBright = 0;
        int centerBright = 0;
        int bottomBright = 0;
        int bottomMedium = 0;
        if (a.getBoolean(19, true)) {
            fullDark = R.drawable.popup_full_dark;
            topDark = R.drawable.popup_top_dark;
            centerDark = R.drawable.popup_center_dark;
            bottomDark = R.drawable.popup_bottom_dark;
            fullBright = R.drawable.popup_full_bright;
            topBright = R.drawable.popup_top_bright;
            centerBright = R.drawable.popup_center_bright;
            bottomBright = R.drawable.popup_bottom_bright;
            bottomMedium = R.drawable.popup_bottom_medium;
        }
        topBright = a.getResourceId(5, topBright);
        topDark = a.getResourceId(1, topDark);
        centerBright = a.getResourceId(6, centerBright);
        centerDark = a.getResourceId(2, centerDark);
        View[] views = new View[4];
        boolean[] light = new boolean[4];
        View lastView = null;
        boolean lastLight = false;
        int pos = 0;
        if (hasTitle) {
            views[0] = topPanel;
            light[0] = false;
            pos = 1;
        }
        if (contentPanel.getVisibility() == 8) {
            contentPanel = null;
        }
        views[pos] = contentPanel;
        light[pos] = this.mListView != null;
        pos++;
        if (hasCustomView) {
            views[pos] = customPanel;
            light[pos] = this.mForceInverseBackground;
            pos++;
        }
        if (hasButtons) {
            views[pos] = buttonPanel;
            light[pos] = true;
        }
        boolean setView = false;
        for (pos = 0; pos < views.length; pos++) {
            int i;
            View v = views[pos];
            if (v != null) {
                if (lastView != null) {
                    if (setView) {
                        lastView.setBackgroundResource(lastLight ? centerBright : centerDark);
                    } else {
                        if (lastLight) {
                            i = topBright;
                        } else {
                            i = topDark;
                        }
                        lastView.setBackgroundResource(i);
                    }
                    setView = true;
                }
                lastView = v;
                lastLight = light[pos];
            }
        }
        if (lastView != null) {
            if (setView) {
                i = lastLight ? hasButtons ? a.getResourceId(8, bottomMedium) : a.getResourceId(7, bottomBright) : a.getResourceId(3, bottomDark);
                lastView.setBackgroundResource(i);
            } else {
                lastView.setBackgroundResource(lastLight ? a.getResourceId(4, fullBright) : a.getResourceId(0, fullDark));
            }
        }
        ListView listView = this.mListView;
        if (listView != null && this.mAdapter != null) {
            listView.setAdapter(this.mAdapter);
            int checkedItem = this.mCheckedItem;
            if (checkedItem > -1) {
                listView.setItemChecked(checkedItem, true);
                listView.setSelection(checkedItem);
            }
        }
    }

    protected Window getWindow() {
        return this.mWindow;
    }

    protected CharSequence getButtonPositiveText() {
        return this.mButtonPositiveText;
    }

    protected CharSequence getButtonNegativeText() {
        return this.mButtonNegativeText;
    }

    protected CharSequence getButtonNeutralText() {
        return this.mButtonNeutralText;
    }

    protected boolean hasTextTitle() {
        return (this.mCustomTitleView == null && TextUtils.isEmpty(this.mTitle)) ? false : true;
    }

    protected void setHuaweiScrollIndicators(boolean hasCustomPanel, boolean hasTopPanel, boolean hasButtonPanel) {
        int i = 0;
        if (!hasCustomPanel) {
            View content = this.mListView != null ? this.mListView : this.mScrollView;
            if (content != null) {
                int i2;
                if (hasTopPanel) {
                    i2 = 1;
                } else {
                    i2 = 0;
                }
                if (hasButtonPanel) {
                    i = 2;
                }
                content.setScrollIndicators(i2 | i, 3);
            }
        }
    }

    protected ScrollView getScrollView() {
        return this.mScrollView;
    }

    public void setMessageNotScrolling() {
    }
}
