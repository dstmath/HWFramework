package huawei.android.widget.pattern;

import android.content.Context;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import huawei.android.widget.loader.ResLoaderUtil;
import java.util.List;

public class HwTextParagraph extends LinearLayout {
    private static final int TYPE_SIZE = 5;
    private Context mContext;
    private int mMarginL;
    private int mMarginM;
    private int mMarginS;
    private int mMarginXL;
    private int[][] mMarginsArray;

    public HwTextParagraph(Context context) {
        this(context, null);
    }

    public HwTextParagraph(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public HwTextParagraph(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public HwTextParagraph(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        this.mMarginXL = 0;
        this.mMarginL = 0;
        this.mMarginM = 0;
        this.mMarginS = 0;
        this.mContext = context;
        this.mMarginXL = ResLoaderUtil.getDimensionPixelSize(this.mContext, "margin_xl");
        this.mMarginL = ResLoaderUtil.getDimensionPixelSize(this.mContext, "margin_l");
        this.mMarginM = ResLoaderUtil.getDimensionPixelSize(this.mContext, "margin_m");
        this.mMarginS = ResLoaderUtil.getDimensionPixelSize(this.mContext, "margin_s");
        int i = this.mMarginXL;
        int i2 = this.mMarginL;
        int[] iArr = {i, i2, i2, i2, i2};
        int[] iArr2 = {i2, i, i2, i2, i2};
        int i3 = this.mMarginM;
        int i4 = this.mMarginS;
        this.mMarginsArray = new int[][]{iArr, iArr2, new int[]{i2, i2, i3, i3, i3}, new int[]{i2, i2, i3, i4, i4}, new int[]{i2, i2, i3, i4, 0}};
    }

    public void setData(List<HwTextParagraphBean> datas) {
        boolean z;
        int type;
        ImageView dotImg;
        List<HwTextParagraphBean> list = datas;
        int i = this.mMarginL;
        boolean z2 = false;
        setPadding(i, 0, i, 0);
        setOrientation(1);
        Typeface typeface = Typeface.create("HwChinese-medium", 0);
        int dataSize = datas.size();
        int tvId = ResLoaderUtil.getViewId(this.mContext, "hwpattern_textparagraph_text");
        int imgId = ResLoaderUtil.getViewId(this.mContext, "hwpattern_textparagraph_img_dot");
        int i2 = 0;
        while (i2 < dataSize) {
            View view = ResLoaderUtil.getLayout(this.mContext, "hwpattern_textparagraph_layout", null, z2);
            if (view != null) {
                TextView textView = (TextView) view.findViewById(tvId);
                ImageView dotImg2 = (ImageView) view.findViewById(imgId);
                int type2 = list.get(i2).getType();
                if (textView != null) {
                    textView.setText(list.get(i2).getText());
                    setTextParagraphTextProp(textView, typeface, type2);
                    if (i2 + 1 < datas.size()) {
                        type = type2;
                        dotImg = dotImg2;
                        setMargins(textView, 0, 0, 0, getMargins(type2, list.get(i2 + 1).getType()));
                    } else {
                        type = type2;
                        dotImg = dotImg2;
                    }
                } else {
                    type = type2;
                    dotImg = dotImg2;
                }
                if (dotImg == null || type != 3) {
                    z = false;
                } else {
                    z = false;
                    dotImg.setVisibility(0);
                }
                addView(view);
            } else {
                z = z2;
            }
            i2++;
            list = datas;
            z2 = z;
        }
    }

    private void setTextParagraphTextProp(TextView textView, Typeface typeface, int type) {
        if (textView != null) {
            int textSizeTitle = ResLoaderUtil.getDimensionPixelSize(this.mContext, "emui_master_title_1");
            int textSizeSubtitle = ResLoaderUtil.getDimensionPixelSize(this.mContext, "emui_master_subtitle");
            int textSizeBody = ResLoaderUtil.getDimensionPixelSize(this.mContext, "emui_master_body_2");
            int colorGray9 = ResLoaderUtil.getColor(this.mContext, "emui_color_gray_9");
            int colorGray7 = ResLoaderUtil.getColor(this.mContext, "emui_color_gray_7");
            if (type == 0) {
                textView.setTextSize(0, (float) textSizeTitle);
                textView.setTypeface(typeface);
                textView.setTextColor(colorGray9);
            } else if (type == 1) {
                textView.setTextSize(0, (float) textSizeSubtitle);
                textView.setTypeface(typeface);
                textView.setTextColor(colorGray9);
            } else if (type == 2) {
                textView.setTextSize(0, (float) textSizeSubtitle);
                textView.setTextColor(colorGray9);
            } else if (type == 3) {
                textView.setTextSize(0, (float) textSizeBody);
                textView.setTextColor(colorGray9);
            } else if (type == 4) {
                textView.setTextSize(0, (float) textSizeBody);
                textView.setTextColor(colorGray7);
            }
        }
    }

    private void setMargins(View view, int left, int top, int right, int bottom) {
        if (view != null && (view.getLayoutParams() instanceof ViewGroup.MarginLayoutParams)) {
            ((ViewGroup.MarginLayoutParams) view.getLayoutParams()).setMargins(left, top, right, bottom);
            view.requestLayout();
        }
    }

    private int getMargins(int type, int nextType) {
        if (type >= 5 || nextType >= 5) {
            return 0;
        }
        return this.mMarginsArray[type][nextType];
    }
}
