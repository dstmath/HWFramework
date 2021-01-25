package huawei.android.widget.pattern;

import android.content.Context;
import android.content.res.TypedArray;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import huawei.android.widget.HwTextView;
import huawei.android.widget.loader.ResLoader;
import huawei.android.widget.loader.ResLoaderUtil;
import java.util.List;

public class HwSearchHistory extends LinearLayout {
    private static final int DOUBLE_SIZE = 2;
    public static final int ELE_RIGHT_TXT = 2;
    public static final int ELE_TITLE_TXT = 1;
    private static final int ONE_THIRD_SIZE = 3;
    private Context mContext;
    private final int mPaddingL;
    private final int mPaddingM;
    private HwTextView mRightTv;
    private int mTagBgId;
    private HwFlowTagLayout mTagLayout;
    private HwTextView mTitleTv;

    public interface OnTagClickListener {
        void onClick(int i, String str);
    }

    public HwSearchHistory(Context context) {
        this(context, null);
    }

    public HwSearchHistory(Context context, AttributeSet attrs) {
        this(context, attrs, ResLoader.getInstance().getIdentifier(context, "attr", "hwSearchHistoryStyle"));
    }

    public HwSearchHistory(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public HwSearchHistory(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        this.mPaddingL = ResLoaderUtil.getDimensionPixelSize(getContext(), "padding_l");
        this.mPaddingM = ResLoaderUtil.getDimensionPixelSize(getContext(), "padding_m");
        setOrientation(1);
        this.mContext = ResLoader.getInstance().getContext(context);
        ResLoaderUtil.getLayout(this.mContext, "hwpattern_search_history", this, true);
        this.mTitleTv = (HwTextView) findViewById(ResLoaderUtil.getViewId(getContext(), "search_history_title"));
        this.mRightTv = (HwTextView) findViewById(ResLoaderUtil.getViewId(getContext(), "search_history_right_text"));
        this.mTagLayout = (HwFlowTagLayout) findViewById(ResLoaderUtil.getViewId(getContext(), "hwpattern_taglayout"));
        ResLoader resLoader = ResLoader.getInstance();
        resLoader.getResources(this.mContext);
        TypedArray ta = resLoader.getTheme(context).obtainStyledAttributes(attrs, resLoader.getIdentifierArray(context, ResLoaderUtil.STAYLEABLE, "HwSearchHistory"), defStyleAttr, defStyleRes);
        this.mTagBgId = ta.getResourceId(resLoader.getIdentifier(context, ResLoaderUtil.STAYLEABLE, "HwSearchHistory_hwSearchHistoryTagBg"), 0);
        ta.recycle();
    }

    public void setData(List<String> data, final OnTagClickListener onTagClickListener) {
        int textSize = ResLoaderUtil.getDimensionPixelSize(getContext(), "emui_master_body_1");
        int tagHeight = ResLoaderUtil.getDimensionPixelSize(getContext(), "search_history_tag_height");
        clearData();
        if (!(data == null || data.isEmpty())) {
            int dataNum = data.size();
            for (final int i = 0; i < dataNum; i++) {
                TextView textView = new TextView(getContext());
                textView.setLines(1);
                textView.setSingleLine(true);
                textView.setEllipsize(TextUtils.TruncateAt.END);
                textView.setText(data.get(i));
                textView.setTextSize(0, (float) textSize);
                textView.setHeight(tagHeight);
                textView.setGravity(16);
                textView.setBackgroundResource(this.mTagBgId);
                final String tag = data.get(i);
                textView.setOnClickListener(new View.OnClickListener() {
                    /* class huawei.android.widget.pattern.HwSearchHistory.AnonymousClass1 */

                    @Override // android.view.View.OnClickListener
                    public void onClick(View v) {
                        OnTagClickListener onTagClickListener = onTagClickListener;
                        if (onTagClickListener != null) {
                            onTagClickListener.onClick(i, tag);
                        }
                    }
                });
                HwFlowTagLayout hwFlowTagLayout = this.mTagLayout;
                if (hwFlowTagLayout != null) {
                    hwFlowTagLayout.addView(textView);
                }
            }
        }
    }

    public void clearData() {
        HwFlowTagLayout hwFlowTagLayout = this.mTagLayout;
        if (hwFlowTagLayout != null) {
            hwFlowTagLayout.removeAllViews();
        }
    }

    public void setElementText(String text, int elementTag) {
        HwTextView hwTextView;
        if (elementTag == 1) {
            HwTextView hwTextView2 = this.mTitleTv;
            if (hwTextView2 != null) {
                hwTextView2.setText(text);
            }
        } else if (elementTag == 2 && (hwTextView = this.mRightTv) != null) {
            hwTextView.setText(text);
        }
    }

    public void setRightTextClick(View.OnClickListener clickEvent) {
        HwTextView hwTextView = this.mRightTv;
        if (hwTextView != null) {
            hwTextView.setOnClickListener(clickEvent);
        }
    }

    /* access modifiers changed from: protected */
    @Override // android.widget.LinearLayout, android.view.View, android.view.ViewGroup
    public void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed, l, t, r, b);
        int hasNoUseWidth = getMeasuredWidth() - ((this.mPaddingL * 2) + this.mPaddingM);
        float titleWidth = this.mTitleTv.getPaint().measureText(this.mTitleTv.getText().toString());
        int settingMinWidth = hasNoUseWidth / 3;
        if (titleWidth < ((float) (hasNoUseWidth - settingMinWidth))) {
            this.mRightTv.setMaxWidth(hasNoUseWidth - ((int) titleWidth));
        } else {
            this.mRightTv.setMaxWidth(settingMinWidth);
        }
    }
}
