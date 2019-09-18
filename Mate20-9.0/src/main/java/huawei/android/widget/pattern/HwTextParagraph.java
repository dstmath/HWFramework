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
    private Context mContext;
    private int marginL;
    private int marginM;
    private int marginS;
    private int marginXL;
    private int[][] margins;
    private final int tyleSize;

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
        this.tyleSize = 5;
        this.marginXL = 0;
        this.marginL = 0;
        this.marginM = 0;
        this.marginS = 0;
        this.mContext = context;
        this.marginXL = ResLoaderUtil.getDimensionPixelSize(this.mContext, "margin_xl");
        this.marginL = ResLoaderUtil.getDimensionPixelSize(this.mContext, "margin_l");
        this.marginM = ResLoaderUtil.getDimensionPixelSize(this.mContext, "margin_m");
        this.marginS = ResLoaderUtil.getDimensionPixelSize(this.mContext, "margin_s");
        this.margins = new int[][]{new int[]{this.marginXL, this.marginL, this.marginL, this.marginL, this.marginL}, new int[]{this.marginL, this.marginXL, this.marginL, this.marginL, this.marginL}, new int[]{this.marginL, this.marginL, this.marginM, this.marginM, this.marginM}, new int[]{this.marginL, this.marginL, this.marginM, this.marginS, this.marginS}, new int[]{this.marginL, this.marginL, this.marginM, this.marginS, 0}};
    }

    public void setData(List<HwTextParagraphBean> datas) {
        int tvId;
        boolean z;
        int i;
        int imgId;
        View view;
        int imgId2;
        List<HwTextParagraphBean> list = datas;
        int textSizeTitle = ResLoaderUtil.getDimensionPixelSize(this.mContext, "emui_master_title_1");
        int textSizeSubtitle = ResLoaderUtil.getDimensionPixelSize(this.mContext, "emui_master_subtitle");
        int textSizeBody = ResLoaderUtil.getDimensionPixelSize(this.mContext, "emui_master_body_2");
        int colorGray9 = ResLoaderUtil.getColor(this.mContext, "emui_color_gray_9");
        int colorGray7 = ResLoaderUtil.getColor(this.mContext, "emui_color_gray_7");
        boolean z2 = false;
        setPadding(this.marginL, 0, this.marginL, 0);
        setOrientation(1);
        Typeface typeface = Typeface.create("HwChinese-medium", 0);
        int dataSize = datas.size();
        int tvId2 = ResLoaderUtil.getViewId(this.mContext, "hwpattern_textparagraph_text");
        int imgId3 = ResLoaderUtil.getViewId(this.mContext, "hwpattern_textparagraph_img_dot");
        int i2 = 0;
        while (true) {
            int i3 = i2;
            if (i3 < dataSize) {
                View view2 = ResLoaderUtil.getLayout(this.mContext, "hwpattern_textparagraph_layout", null, z2);
                if (view2 != null) {
                    TextView textView = (TextView) view2.findViewById(tvId2);
                    ImageView dotImg = (ImageView) view2.findViewById(imgId3);
                    int type = list.get(i3).getType();
                    if (textView != null) {
                        View view3 = view2;
                        textView.setText(list.get(i3).getText());
                        switch (type) {
                            case 0:
                                imgId2 = imgId3;
                                textView.setTextSize(0, (float) textSizeTitle);
                                textView.setTypeface(typeface);
                                textView.setTextColor(colorGray9);
                                break;
                            case 1:
                                imgId2 = imgId3;
                                textView.setTextSize(0, (float) textSizeSubtitle);
                                textView.setTypeface(typeface);
                                textView.setTextColor(colorGray9);
                                break;
                            case 2:
                                imgId2 = imgId3;
                                textView.setTextSize(0, (float) textSizeSubtitle);
                                textView.setTextColor(colorGray9);
                                break;
                            case 3:
                                imgId2 = imgId3;
                                textView.setTextSize(0, (float) textSizeBody);
                                textView.setTextColor(colorGray9);
                                if (dotImg != null) {
                                    dotImg.setVisibility(0);
                                    break;
                                }
                                break;
                            case 4:
                                imgId2 = imgId3;
                                textView.setTextSize(0, (float) textSizeBody);
                                textView.setTextColor(colorGray7);
                                break;
                            default:
                                imgId2 = imgId3;
                                break;
                        }
                        if (i3 + 1 < datas.size()) {
                            int nextTypt = list.get(i3 + 1).getType();
                            ImageView imageView = dotImg;
                            TextView textView2 = textView;
                            view = view3;
                            i = i3;
                            imgId = imgId2;
                            z = false;
                            int imgId4 = nextTypt;
                            tvId = tvId2;
                            setMargins(textView, 0, 0, 0, getMargins(type, nextTypt));
                        } else {
                            TextView textView3 = textView;
                            tvId = tvId2;
                            view = view3;
                            imgId = imgId2;
                            z = false;
                            i = i3;
                        }
                    } else {
                        TextView textView4 = textView;
                        view = view2;
                        i = i3;
                        imgId = imgId3;
                        tvId = tvId2;
                        z = false;
                    }
                    addView(view);
                } else {
                    i = i3;
                    imgId = imgId3;
                    tvId = tvId2;
                    z = z2;
                }
                i2 = i + 1;
                imgId3 = imgId;
                z2 = z;
                tvId2 = tvId;
            } else {
                int i4 = tvId2;
                return;
            }
        }
    }

    private void setMargins(View v, int l, int t, int r, int b) {
        if (v != null && (v.getLayoutParams() instanceof ViewGroup.MarginLayoutParams)) {
            ((ViewGroup.MarginLayoutParams) v.getLayoutParams()).setMargins(l, t, r, b);
            v.requestLayout();
        }
    }

    private int getMargins(int i, int j) {
        if (i >= 5 || j >= 5) {
            return 0;
        }
        return this.margins[i][j];
    }
}
