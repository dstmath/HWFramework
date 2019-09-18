package huawei.android.widget.pattern;

import android.content.Context;
import android.content.res.Resources;
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
    public static final int ELE_RIGHT_TXT = 2;
    public static final int ELE_TITLE_TXT = 1;
    private final int PADDING_L;
    private final int PADDING_M;
    private Context mContext;
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
        this.PADDING_L = ResLoaderUtil.getDimensionPixelSize(getContext(), "padding_l");
        this.PADDING_M = ResLoaderUtil.getDimensionPixelSize(getContext(), "padding_m");
        setOrientation(1);
        this.mContext = ResLoader.getInstance().getContext(context);
        ResLoaderUtil.getLayout(this.mContext, "hwpattern_search_history", this, true);
        this.mTitleTv = (HwTextView) findViewById(ResLoaderUtil.getViewId(getContext(), "search_history_title"));
        this.mRightTv = (HwTextView) findViewById(ResLoaderUtil.getViewId(getContext(), "search_history_right_text"));
        this.mTagLayout = (HwFlowTagLayout) findViewById(ResLoaderUtil.getViewId(getContext(), "hwpattern_taglayout"));
        ResLoader resLoader = ResLoader.getInstance();
        Resources resources = resLoader.getResources(this.mContext);
        TypedArray ta = resLoader.getTheme(context).obtainStyledAttributes(attrs, resLoader.getIdentifierArray(context, ResLoaderUtil.STAYLEABLE, "HwSearchHistory"), defStyleAttr, defStyleRes);
        this.mTagBgId = ta.getResourceId(resLoader.getIdentifier(context, ResLoaderUtil.STAYLEABLE, "HwSearchHistory_hwSearchHistoryTagBg"), 0);
        ta.recycle();
    }

    public void setData(List<String> data, final OnTagClickListener onTagClickListener) {
        int textSize = ResLoaderUtil.getDimensionPixelSize(getContext(), "emui_master_body_1");
        int tagHeight = ResLoaderUtil.getDimensionPixelSize(getContext(), "search_history_tag_height");
        clearData();
        if (data != null && !data.isEmpty()) {
            int dataNum = data.size();
            for (int i = 0; i < dataNum; i++) {
                TextView tv = new TextView(getContext());
                tv.setLines(1);
                tv.setSingleLine(true);
                tv.setEllipsize(TextUtils.TruncateAt.END);
                tv.setText(data.get(i));
                tv.setTextSize(0, (float) textSize);
                tv.setHeight(tagHeight);
                tv.setGravity(16);
                tv.setBackgroundResource(this.mTagBgId);
                final int index = i;
                final String tag = data.get(i);
                tv.setOnClickListener(new View.OnClickListener() {
                    public void onClick(View v) {
                        if (onTagClickListener != null) {
                            onTagClickListener.onClick(index, tag);
                        }
                    }
                });
                if (this.mTagLayout != null) {
                    this.mTagLayout.addView(tv);
                }
            }
        }
    }

    public void clearData() {
        if (this.mTagLayout != null) {
            this.mTagLayout.removeAllViews();
        }
    }

    public void setElementText(String text, int elementTag) {
        switch (elementTag) {
            case 1:
                if (this.mTitleTv != null) {
                    this.mTitleTv.setText(text);
                    return;
                }
                return;
            case 2:
                if (this.mRightTv != null) {
                    this.mRightTv.setText(text);
                    return;
                }
                return;
            default:
                return;
        }
    }

    public void setRightTextClick(View.OnClickListener clickEvent) {
        if (this.mRightTv != null) {
            this.mRightTv.setOnClickListener(clickEvent);
        }
    }

    /* access modifiers changed from: protected */
    public void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed, l, t, r, b);
        int hasNoUseWidth = getMeasuredWidth() - ((2 * this.PADDING_L) + this.PADDING_M);
        float titleWidth = this.mTitleTv.getPaint().measureText(this.mTitleTv.getText().toString());
        int settingMinWidth = hasNoUseWidth / 3;
        if (titleWidth < ((float) (hasNoUseWidth - settingMinWidth))) {
            this.mRightTv.setMaxWidth(hasNoUseWidth - ((int) titleWidth));
        } else {
            this.mRightTv.setMaxWidth(settingMinWidth);
        }
    }
}
