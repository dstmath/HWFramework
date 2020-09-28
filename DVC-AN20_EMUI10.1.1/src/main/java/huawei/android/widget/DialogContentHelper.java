package huawei.android.widget;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ScrollView;
import android.widget.Space;
import android.widget.TextView;
import com.android.internal.app.AlertController;
import huawei.android.widget.loader.ResLoader;
import huawei.android.widget.loader.ResLoaderUtil;
import java.util.ArrayList;
import java.util.List;

public class DialogContentHelper {
    private static final int DEFAULT_TEXT_SIZE = 15;
    private static final int GAP_16 = 16;
    private static final int GAP_24 = 24;
    public static final int LIST_ITEM_1 = 0;
    public static final int LIST_ITEM_2 = 3;
    public static final int LIST_ITEM_MULTIPLE_CHOICE = 2;
    public static final int LIST_ITEM_SINGLE_CHOICE = 1;
    private static final int NESTED_LIST_HEIGHT = 200;
    private static final String TAG = "DialogContentHelper";
    private static final int VIEW_LISTS_INITIAL_CAPACITY = 10;
    private LinearLayout mContainer;
    private Context mContext;
    private float mDensity;
    private ViewGroup.LayoutParams mGapParams;
    private LayoutInflater mInflater;
    private boolean mIsHasHat;
    private boolean mIsHasShoe;
    private int mListItemLayout;
    private int mListTwoLinesItemLayout;
    private int mMultiChoiceItemLayout;
    private ScrollView mScrollView;
    private int mSingleChoiceItemLayout;
    private ViewGroup.LayoutParams mTopBottomGapParams;
    private List<Dex> mViewlists;

    public DialogContentHelper(Context context) {
        this(null, false, false, context);
    }

    public DialogContentHelper(boolean isHasHat, boolean isHasShoe, Context context) {
        this(null, isHasHat, isHasShoe, context);
    }

    public DialogContentHelper(List<Dex> viewlists, Context context) {
        this(viewlists, false, false, context);
    }

    public DialogContentHelper(List<Dex> viewlists, boolean isHasHat, boolean isHasShoe, Context context) {
        this.mViewlists = viewlists;
        this.mIsHasHat = isHasHat;
        this.mIsHasShoe = isHasShoe;
        this.mContext = context;
        init();
    }

    private static int getIdentifier(Context context, String type, String name) {
        int id = ResLoader.getInstance().getIdentifier(context, type, name);
        if (id == 0) {
            Log.w(TAG, "resources is not found");
        }
        return id;
    }

    private void init() {
        if (this.mViewlists == null) {
            this.mViewlists = new ArrayList((int) VIEW_LISTS_INITIAL_CAPACITY);
        }
        Context context = this.mContext;
        if (context != null) {
            this.mInflater = LayoutInflater.from(context);
            this.mDensity = this.mContext.getResources().getDisplayMetrics().density;
            this.mTopBottomGapParams = new ViewGroup.LayoutParams(-1, this.mIsHasHat ? (int) (this.mDensity * 16.0f) : (int) (this.mDensity * 24.0f));
            this.mGapParams = new ViewGroup.LayoutParams(-1, (int) (this.mDensity * 16.0f));
            this.mScrollView = new ScrollView(this.mContext);
            this.mContainer = new LinearLayout(this.mContext);
            this.mContainer.setOrientation(1);
            this.mMultiChoiceItemLayout = getIdentifier(this.mContext, ResLoaderUtil.LAYOUT, "select_dialog_multichoice_emui");
            this.mSingleChoiceItemLayout = getIdentifier(this.mContext, ResLoaderUtil.LAYOUT, "select_dialog_singlechoice_emui");
            this.mListItemLayout = getIdentifier(this.mContext, ResLoaderUtil.LAYOUT, "select_dialog_item_emui");
            this.mListTwoLinesItemLayout = getIdentifier(this.mContext, ResLoaderUtil.LAYOUT, "dialog_list_twolines_emui");
            return;
        }
        throw new RuntimeException("Context can not be null");
    }

    public static class Dex {
        public static final int DIALOG_BODY_EDIT = 2;
        public static final int DIALOG_BODY_IGNORECHECKBOX = 3;
        public static final int DIALOG_BODY_LIST = 4;
        public static final int DIALOG_BODY_TEXT = 1;
        public static final int DIALOG_BODY_TWO_IMAGES = 5;
        public static final int DIALOG_BODY_View = 6;
        private int mTag;
        private View mViews;

        public Dex(View views, int tag) {
            this.mViews = views;
            this.mTag = tag;
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private boolean isOutRange(int judge) {
            return judge < 1 || judge > 6;
        }
    }

    private class DialogRecycleListView extends AlertController.RecycleListView {
        DialogRecycleListView(Context context) {
            super(context);
        }

        DialogRecycleListView(Context context, AttributeSet attrs) {
            super(context, attrs);
        }

        public boolean onTouchEvent(MotionEvent motionEvent) {
            int action = motionEvent.getAction();
            if (action != 0) {
                if (action == 1) {
                    DialogContentHelper.this.mScrollView.requestDisallowInterceptTouchEvent(false);
                } else if (action == 2) {
                    DialogContentHelper.this.mScrollView.requestDisallowInterceptTouchEvent(true);
                }
            }
            return DialogContentHelper.super.onTouchEvent(motionEvent);
        }
    }

    public DialogContentHelper addLists(List<Dex> list) {
        for (Dex dex : list) {
            if (dex.isOutRange(dex.mTag)) {
                throw new RuntimeException("invalid Data");
            }
        }
        this.mViewlists.addAll(list);
        return this;
    }

    public DialogContentHelper addData(Dex dex) {
        if (!dex.isOutRange(dex.mTag)) {
            this.mViewlists.add(dex);
            return this;
        }
        throw new RuntimeException("invalid Data");
    }

    public DialogContentHelper addData(View view, int tag) {
        return addData(new Dex(view, tag));
    }

    public DialogContentHelper insertEditText() {
        EditText editText = new EditText(this.mContext);
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(-1, -2);
        float f = this.mDensity;
        layoutParams.setMargins((int) (f * 16.0f), 0, (int) (f * 16.0f), 0);
        editText.setLayoutParams(layoutParams);
        return addData(editText, 2);
    }

    public DialogContentHelper insertBodyText(CharSequence longText) {
        TextView textView = new TextView(this.mContext);
        ViewGroup.LayoutParams layoutParams = new ViewGroup.LayoutParams(-1, -2);
        float f = this.mDensity;
        textView.setPadding((int) (f * 16.0f), 0, (int) (f * 16.0f), 0);
        textView.setLayoutParams(layoutParams);
        textView.setTextSize(2, 15.0f);
        textView.setLineSpacing(0.0f, this.mContext.getResources().getFloat(getIdentifier(this.mContext, ResLoaderUtil.DIMEN, "text_default_spacing_emui")));
        textView.setText(longText);
        return addData(textView, 1);
    }

    /* JADX DEBUG: Multi-variable search result rejected for r4v0, resolved type: huawei.android.widget.DialogContentHelper */
    /* JADX WARN: Multi-variable type inference failed */
    /* JADX WARN: Type inference failed for: r0v0, types: [huawei.android.widget.DialogContentHelper$DialogRecycleListView, android.view.View] */
    public DialogContentHelper insertLists(BaseAdapter adapter, AdapterView.OnItemClickListener onClickListener) {
        ?? dialogRecycleListView = new DialogRecycleListView(this.mContext);
        dialogRecycleListView.setLayoutParams(new ViewGroup.LayoutParams(-1, (int) (this.mDensity * 200.0f)));
        dialogRecycleListView.setAdapter(adapter);
        if (onClickListener != null) {
            dialogRecycleListView.setOnItemClickListener(onClickListener);
        }
        return addData(dialogRecycleListView, 4);
    }

    /* JADX DEBUG: Multi-variable search result rejected for r8v0, resolved type: huawei.android.widget.DialogContentHelper */
    /* JADX WARN: Multi-variable type inference failed */
    /* JADX WARN: Type inference failed for: r1v0, types: [huawei.android.widget.DialogContentHelper$DialogRecycleListView, android.view.View, android.widget.ListView] */
    public DialogContentHelper insertLists(CharSequence[] text, int[] checkItems, AdapterView.OnItemClickListener onClickListener, int mode) {
        int listLayoutRes = -1;
        ?? dialogRecycleListView = new DialogRecycleListView(this.mContext);
        if (mode == 0) {
            listLayoutRes = this.mListItemLayout;
        } else if (mode == 1) {
            listLayoutRes = this.mSingleChoiceItemLayout;
            if (checkItems == null || checkItems.length <= 1) {
                dialogRecycleListView.setChoiceMode(1);
            } else {
                throw new RuntimeException("you can not select more than one item");
            }
        } else if (mode == 2) {
            listLayoutRes = this.mMultiChoiceItemLayout;
            dialogRecycleListView.setChoiceMode(2);
        } else if (mode == 3) {
            listLayoutRes = this.mListTwoLinesItemLayout;
        }
        ListAdapter adapter = new ArrayAdapter(this.mContext, listLayoutRes, 16908308, text);
        dialogRecycleListView.setLayoutParams(new ViewGroup.LayoutParams(-1, (int) (this.mDensity * 200.0f)));
        dialogRecycleListView.setAdapter(adapter);
        if (checkItems != null) {
            for (int i : checkItems) {
                dialogRecycleListView.setItemChecked(i, true);
            }
        }
        if (onClickListener != null) {
            dialogRecycleListView.setOnItemClickListener(onClickListener);
        }
        return addData(dialogRecycleListView, 4);
    }

    public DialogContentHelper insertIgnoreCheckBox(CharSequence ignoreCheckBoxHint, View.OnClickListener onClickListener) {
        IgnoreCheckBox view = new IgnoreCheckBox(this.mContext);
        LinearLayout.LayoutParams ignoreCheckBoxParams = new LinearLayout.LayoutParams(-1, -2);
        float f = this.mDensity;
        ignoreCheckBoxParams.setMargins((int) (f * 16.0f), 0, (int) (f * 16.0f), 0);
        view.setLayoutParams(ignoreCheckBoxParams);
        view.setOnClickListener(onClickListener);
        view.setText(ignoreCheckBoxHint);
        return addData(view, 3);
    }

    public DialogContentHelper insertTwoImages(Drawable image1, CharSequence text1, Drawable image2, CharSequence text2) {
        View view = this.mInflater.inflate(getIdentifier(this.mContext, ResLoaderUtil.LAYOUT, "dialog_two_images_emui"), (ViewGroup) this.mContainer, false);
        ((ImageView) view.findViewById(getIdentifier(this.mContext, ResLoaderUtil.ID, "image1"))).setImageDrawable(image1);
        ((ImageView) view.findViewById(getIdentifier(this.mContext, ResLoaderUtil.ID, "image2"))).setImageDrawable(image2);
        ((TextView) view.findViewById(getIdentifier(this.mContext, ResLoaderUtil.ID, "text1"))).setText(text1);
        ((TextView) view.findViewById(getIdentifier(this.mContext, ResLoaderUtil.ID, "text2"))).setText(text2);
        return addData(view, 5);
    }

    public DialogContentHelper insertView(View view, View.OnClickListener onClickListener) {
        view.setOnClickListener(onClickListener);
        return addData(view, 6);
    }

    public DialogContentHelper beginLayout() {
        this.mViewlists.clear();
        this.mContainer.removeAllViews();
        this.mScrollView.removeAllViews();
        return this;
    }

    public View endLayout() {
        this.mContainer.addView(new Space(this.mContext), this.mTopBottomGapParams);
        int lastFormatTag = -1;
        int size = this.mViewlists.size();
        for (int i = 0; i < size; i++) {
            Dex dex = this.mViewlists.get(i);
            if (!(lastFormatTag == -1 || (dex.mTag == 3 && lastFormatTag == 3))) {
                this.mContainer.addView(new Space(this.mContext), this.mGapParams);
            }
            this.mContainer.addView(dex.mViews);
            lastFormatTag = dex.mTag;
        }
        if (!this.mIsHasShoe) {
            this.mContainer.addView(new Space(this.mContext), this.mTopBottomGapParams);
        }
        this.mScrollView.addView(this.mContainer);
        return this.mScrollView;
    }
}
