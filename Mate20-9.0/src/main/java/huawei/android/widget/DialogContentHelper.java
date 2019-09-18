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
    private static final int GAP_16 = 16;
    private static final int GAP_24 = 24;
    public static final int LIST_ITEM_1 = 0;
    public static final int LIST_ITEM_2 = 3;
    public static final int LIST_ITEM_MULTIPLE_CHOICE = 2;
    public static final int LIST_ITEM_SINGLE_CHOICE = 1;
    private static final int NESTED_LIST_HEIGHT = 200;
    private static final String TAG = "DialogContentHelper";
    private LinearLayout mContainer;
    private Context mContext;
    private float mDensity;
    private ViewGroup.LayoutParams mGapParams;
    private boolean mHasHat;
    private boolean mHasShoe;
    private LayoutInflater mInflater;
    private int mListItemLayout;
    private int mListTwoLinesItemLayout;
    private int mMultiChoiceItemLayout;
    /* access modifiers changed from: private */
    public ScrollView mScrollView;
    private int mSingleChoiceItemLayout;
    private ViewGroup.LayoutParams mTopBottomGapParams;
    private List<Dex> mViewlists;

    public static class Dex {
        public static final int DIALOG_BODY_EDIT = 2;
        public static final int DIALOG_BODY_IGNORECHECKBOX = 3;
        public static final int DIALOG_BODY_LIST = 4;
        public static final int DIALOG_BODY_TEXT = 1;
        public static final int DIALOG_BODY_TWO_IMAGES = 5;
        public static final int DIALOG_BODY_View = 6;
        /* access modifiers changed from: private */
        public int mTag;
        /* access modifiers changed from: private */
        public View mViews;

        public Dex(View views, int tag) {
            this.mViews = views;
            this.mTag = tag;
        }

        /* access modifiers changed from: private */
        public boolean isOutRange(int judge) {
            return judge < 1 || judge > 6;
        }
    }

    private class RListView extends AlertController.RecycleListView {
        public RListView(Context context) {
            super(context);
        }

        public RListView(Context context, AttributeSet attrs) {
            super(context, attrs);
        }

        public boolean onTouchEvent(MotionEvent ev) {
            switch (ev.getAction()) {
                case 1:
                    DialogContentHelper.this.mScrollView.requestDisallowInterceptTouchEvent(false);
                    break;
                case 2:
                    DialogContentHelper.this.mScrollView.requestDisallowInterceptTouchEvent(true);
                    break;
            }
            return DialogContentHelper.super.onTouchEvent(ev);
        }
    }

    private static int getIdentifier(Context context, String type, String name) {
        int id = ResLoader.getInstance().getIdentifier(context, type, name);
        if (id == 0) {
            Log.w(TAG, "resources is not found");
        }
        return id;
    }

    public DialogContentHelper(Context context) {
        this(null, false, false, context);
    }

    public DialogContentHelper(boolean hasHat, boolean hasShoe, Context context) {
        this(null, hasHat, hasShoe, context);
    }

    public DialogContentHelper(List<Dex> viewlists, Context context) {
        this(viewlists, false, false, context);
    }

    public DialogContentHelper(List<Dex> viewlists, boolean hasHat, boolean hasShoe, Context context) {
        this.mViewlists = viewlists;
        this.mHasHat = hasHat;
        this.mHasShoe = hasShoe;
        this.mContext = context;
        init();
    }

    private void init() {
        int gap_height;
        if (this.mViewlists == null) {
            this.mViewlists = new ArrayList();
        }
        if (this.mContext != null) {
            this.mInflater = LayoutInflater.from(this.mContext);
            this.mDensity = this.mContext.getResources().getDisplayMetrics().density;
            if (this.mHasHat) {
                gap_height = (int) (this.mDensity * 16.0f);
            } else {
                gap_height = (int) (24.0f * this.mDensity);
            }
            this.mTopBottomGapParams = new ViewGroup.LayoutParams(-1, gap_height);
            this.mGapParams = new ViewGroup.LayoutParams(-1, (int) (16.0f * this.mDensity));
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

    public DialogContentHelper addLists(List<Dex> list) {
        for (Dex dex : list) {
            if (dex.isOutRange(dex.mTag)) {
                throw new RuntimeException("invalid Data");
            }
        }
        this.mViewlists.addAll(list);
        return this;
    }

    public DialogContentHelper addData(Dex d) {
        if (!d.isOutRange(d.mTag)) {
            this.mViewlists.add(d);
            return this;
        }
        throw new RuntimeException("invalid Data");
    }

    public DialogContentHelper addData(View view, int tag) {
        return addData(new Dex(view, tag));
    }

    public DialogContentHelper insertEditText() {
        EditText et = new EditText(this.mContext);
        LinearLayout.LayoutParams etParams = new LinearLayout.LayoutParams(-1, -2);
        etParams.setMargins((int) (this.mDensity * 16.0f), 0, (int) (16.0f * this.mDensity), 0);
        et.setLayoutParams(etParams);
        return addData(et, 2);
    }

    public DialogContentHelper insertBodyText(CharSequence longText) {
        TextView tv = new TextView(this.mContext);
        ViewGroup.LayoutParams tvParams = new ViewGroup.LayoutParams(-1, -2);
        tv.setPadding((int) (this.mDensity * 16.0f), 0, (int) (16.0f * this.mDensity), 0);
        tv.setLayoutParams(tvParams);
        tv.setTextSize(2, 15.0f);
        tv.setLineSpacing(0.0f, this.mContext.getResources().getFloat(getIdentifier(this.mContext, ResLoaderUtil.DIMEN, "text_default_spacing_emui")));
        tv.setText(longText);
        return addData(tv, 1);
    }

    /* JADX WARNING: type inference failed for: r0v0, types: [huawei.android.widget.DialogContentHelper$RListView, android.view.View, android.widget.ListView] */
    public DialogContentHelper insertLists(BaseAdapter adapter, AdapterView.OnItemClickListener onClickListener) {
        ? rListView = new RListView(this.mContext);
        rListView.setLayoutParams(new ViewGroup.LayoutParams(-1, (int) (200.0f * this.mDensity)));
        rListView.setAdapter(adapter);
        if (onClickListener != null) {
            rListView.setOnItemClickListener(onClickListener);
        }
        return addData(rListView, 4);
    }

    /* JADX WARNING: type inference failed for: r1v0, types: [huawei.android.widget.DialogContentHelper$RListView, android.view.View, android.widget.ListView] */
    public DialogContentHelper insertLists(CharSequence[] c, int[] checkItems, AdapterView.OnItemClickListener onClickListener, int mode) {
        int listLayoutRes = -1;
        ? rListView = new RListView(this.mContext);
        switch (mode) {
            case 0:
                listLayoutRes = this.mListItemLayout;
                break;
            case 1:
                listLayoutRes = this.mSingleChoiceItemLayout;
                if (checkItems == null || checkItems.length <= 1) {
                    rListView.setChoiceMode(1);
                    break;
                } else {
                    throw new RuntimeException("you can not select more than one item");
                }
            case 2:
                listLayoutRes = this.mMultiChoiceItemLayout;
                rListView.setChoiceMode(2);
                break;
            case 3:
                listLayoutRes = this.mListTwoLinesItemLayout;
                break;
        }
        ListAdapter adapter = new ArrayAdapter(this.mContext, listLayoutRes, 16908308, c);
        rListView.setLayoutParams(new ViewGroup.LayoutParams(-1, (int) (200.0f * this.mDensity)));
        rListView.setAdapter(adapter);
        if (checkItems != null) {
            for (int i : checkItems) {
                rListView.setItemChecked(i, true);
            }
        }
        if (onClickListener != null) {
            rListView.setOnItemClickListener(onClickListener);
        }
        return addData(rListView, 4);
    }

    public DialogContentHelper insertIgnoreCheckBox(CharSequence ignoreCheckBoxHint, View.OnClickListener onClickListener) {
        IgnoreCheckBox view = new IgnoreCheckBox(this.mContext);
        LinearLayout.LayoutParams ignoreCheckBoxParams = new LinearLayout.LayoutParams(-1, -2);
        ignoreCheckBoxParams.setMargins((int) (this.mDensity * 16.0f), 0, (int) (16.0f * this.mDensity), 0);
        view.setLayoutParams(ignoreCheckBoxParams);
        view.setOnClickListener(onClickListener);
        view.setText(ignoreCheckBoxHint);
        return addData(view, 3);
    }

    public DialogContentHelper insertTwoImages(Drawable image1, CharSequence text1, Drawable image2, CharSequence text2) {
        View view = this.mInflater.inflate(getIdentifier(this.mContext, ResLoaderUtil.LAYOUT, "dialog_two_images_emui"), this.mContainer, false);
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
            Dex d = this.mViewlists.get(i);
            if (!(lastFormatTag == -1 || (3 == d.mTag && 3 == lastFormatTag))) {
                this.mContainer.addView(new Space(this.mContext), this.mGapParams);
            }
            this.mContainer.addView(d.mViews);
            lastFormatTag = d.mTag;
        }
        if (this.mHasShoe == 0) {
            this.mContainer.addView(new Space(this.mContext), this.mTopBottomGapParams);
        }
        this.mScrollView.addView(this.mContainer);
        return this.mScrollView;
    }
}
