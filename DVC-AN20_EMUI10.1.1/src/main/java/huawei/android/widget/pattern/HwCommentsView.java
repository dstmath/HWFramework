package huawei.android.widget.pattern;

import android.content.Context;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import com.huawei.uikit.effect.BuildConfig;
import huawei.android.widget.EditText;
import huawei.android.widget.HwTextView;
import huawei.android.widget.HwWidgetUtils;
import huawei.android.widget.loader.ResLoaderUtil;
import java.util.ArrayList;
import java.util.List;

public class HwCommentsView extends LinearLayout {
    public static final int ELE_ADD_ICON = 1;
    public static final int ELE_EMOJI_ICON = 2;
    public static final int ELE_MESSAGE_ICON = 4;
    public static final int ELE_SEND_ICON = 3;
    private static final int LINES_DIV = 4;
    private static final int LIST_LENGTH = 10;
    private static final int MAX_ATTACHMENT_VIEW_SIZE = 5;
    private static final int MAX_LINES = 2;
    private ImageView mAddIv;
    private LinearLayout mAttachmentLayout;
    private int mAttachmentMargin;
    private int mAttachmentTextColor;
    private int mAttachmentTextMargin;
    private int mAttachmentWidth;
    private int mClickableColor;
    private Context mContext;
    private EditText mEdit;
    private ImageView mEmojiIv;
    private int mImageWidth;
    private ImageView mMessageIv;
    private int mScreenWidth;
    private ImageView mSendIv;
    private Typeface mTypeface;
    private int mUnClickableColor;

    public HwCommentsView(Context context) {
        this(context, null);
    }

    public HwCommentsView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public HwCommentsView(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public HwCommentsView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        this.mContext = context;
        initView();
    }

    private void editAddListener() {
        EditText editText = this.mEdit;
        if (editText != null) {
            editText.addTextChangedListener(new TextWatcher() {
                /* class huawei.android.widget.pattern.HwCommentsView.AnonymousClass1 */

                public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                }

                public void onTextChanged(CharSequence s, int start, int before, int count) {
                }

                public void afterTextChanged(Editable s) {
                    if (HwCommentsView.this.mSendIv != null) {
                        if (s.length() != 0) {
                            if (HwCommentsView.this.mClickableColor != 0) {
                                HwCommentsView.this.mSendIv.setColorFilter(HwCommentsView.this.mClickableColor);
                            }
                            HwCommentsView.this.mSendIv.setClickable(true);
                            return;
                        }
                        if (HwCommentsView.this.mUnClickableColor != 0) {
                            HwCommentsView.this.mSendIv.setColorFilter(HwCommentsView.this.mUnClickableColor);
                        }
                        HwCommentsView.this.mSendIv.setClickable(false);
                    }
                }
            });
        }
    }

    private void setLeftAndRightPadding() {
        EditText editText;
        int margin = ResLoaderUtil.getDimensionPixelSize(this.mContext, "margin_m");
        int mEmojiIvVisibility = 8;
        int mAddIvVisibility = 8;
        ImageView imageView = this.mEmojiIv;
        if (imageView != null) {
            mEmojiIvVisibility = imageView.getVisibility();
        }
        ImageView imageView2 = this.mAddIv;
        if (imageView2 != null) {
            mAddIvVisibility = imageView2.getVisibility();
        }
        if (mAddIvVisibility == 8 || mEmojiIvVisibility == 8) {
            EditText editText2 = this.mEdit;
            if (editText2 != null) {
                LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) editText2.getLayoutParams();
                params.setMarginStart(0);
                this.mEdit.setLayoutParams(params);
            }
        } else {
            LinearLayout.LayoutParams params2 = (LinearLayout.LayoutParams) this.mAddIv.getLayoutParams();
            params2.setMarginEnd(margin);
            this.mAddIv.setLayoutParams(params2);
        }
        if (mAddIvVisibility == 8 && mEmojiIvVisibility == 8 && (editText = this.mEdit) != null) {
            LinearLayout.LayoutParams params3 = (LinearLayout.LayoutParams) editText.getLayoutParams();
            params3.setMarginStart(margin);
            this.mEdit.setLayoutParams(params3);
        }
        ImageView imageView3 = this.mMessageIv;
        if (imageView3 != null && imageView3.getVisibility() != 8) {
            LinearLayout.LayoutParams params4 = (LinearLayout.LayoutParams) this.mMessageIv.getLayoutParams();
            params4.setMarginEnd(margin);
            this.mMessageIv.setLayoutParams(params4);
        }
    }

    private void initView() {
        this.mAttachmentMargin = ResLoaderUtil.getDimensionPixelSize(this.mContext, "margin_l");
        this.mScreenWidth = ResLoaderUtil.getResources(this.mContext).getDisplayMetrics().widthPixels;
        this.mAttachmentWidth = (this.mScreenWidth - (this.mAttachmentMargin * 2)) / 4;
        this.mImageWidth = ResLoaderUtil.getDimensionPixelSize(this.mContext, "hwpattern_commentsview_image_width_s");
        this.mAttachmentTextMargin = ResLoaderUtil.getDimensionPixelSize(this.mContext, "margin_s");
        this.mTypeface = Typeface.create("HwChinese-medium", 0);
        this.mClickableColor = ResLoaderUtil.getColor(this.mContext, "emui_primary");
        this.mUnClickableColor = ResLoaderUtil.getColor(this.mContext, "emui_color_gray_5");
        this.mAttachmentTextColor = ResLoaderUtil.getColor(this.mContext, "emui_color_gray_10");
        ResLoaderUtil.getLayout(this.mContext, "hwpattern_hwcommentsview_layout", this, true);
        this.mEdit = (EditText) findViewById(ResLoaderUtil.getViewId(this.mContext, "hwpattern_commentsview_edit"));
        this.mAddIv = (ImageView) findViewById(ResLoaderUtil.getViewId(this.mContext, "hwpattern_commentsview_add_image"));
        this.mSendIv = (ImageView) findViewById(ResLoaderUtil.getViewId(this.mContext, "hwpattern_commentsview_send_image"));
        this.mEmojiIv = (ImageView) findViewById(ResLoaderUtil.getViewId(this.mContext, "hwpattern_commentsview_emoji_image"));
        this.mMessageIv = (ImageView) findViewById(ResLoaderUtil.getViewId(this.mContext, "hwpattern_commentsview_message_image"));
        this.mAttachmentLayout = (LinearLayout) findViewById(ResLoaderUtil.getViewId(this.mContext, "hwpattern_commentsview_attachment_layout"));
        ImageView imageView = this.mAddIv;
        if (imageView != null) {
            imageView.setOnClickListener(new View.OnClickListener() {
                /* class huawei.android.widget.pattern.HwCommentsView.AnonymousClass2 */

                public void onClick(View view) {
                    if (HwCommentsView.this.mAttachmentLayout == null) {
                        return;
                    }
                    if (HwCommentsView.this.mAttachmentLayout.getVisibility() == 8) {
                        HwCommentsView.this.mAttachmentLayout.setVisibility(0);
                    } else if (HwCommentsView.this.mAttachmentLayout.getVisibility() == 0) {
                        HwCommentsView.this.mAttachmentLayout.setVisibility(8);
                    }
                }
            });
        }
        int i = this.mClickableColor;
        if (i != 0) {
            ImageView imageView2 = this.mMessageIv;
            if (imageView2 != null) {
                imageView2.setColorFilter(i);
            }
            ImageView imageView3 = this.mEmojiIv;
            if (imageView3 != null) {
                imageView3.setColorFilter(this.mClickableColor);
            }
            ImageView imageView4 = this.mAddIv;
            if (imageView4 != null) {
                imageView4.setColorFilter(this.mClickableColor);
            }
        }
        ImageView imageView5 = this.mEmojiIv;
        if (imageView5 != null) {
            imageView5.setBackground(HwWidgetUtils.getHwAnimatedGradientDrawable(this.mContext, 0));
        }
        ImageView imageView6 = this.mMessageIv;
        if (imageView6 != null) {
            imageView6.setBackground(HwWidgetUtils.getHwAnimatedGradientDrawable(this.mContext, 0));
        }
        ImageView imageView7 = this.mAddIv;
        if (imageView7 != null) {
            imageView7.setBackground(HwWidgetUtils.getHwAnimatedGradientDrawable(this.mContext, 0));
        }
        ImageView imageView8 = this.mSendIv;
        if (imageView8 != null) {
            imageView8.setBackground(HwWidgetUtils.getHwAnimatedGradientDrawable(this.mContext, 0));
            this.mSendIv.setClickable(false);
        }
        setLeftAndRightPadding();
        editAddListener();
    }

    public void setEditHint(CharSequence text) {
        EditText editText = this.mEdit;
        if (editText != null) {
            editText.setHint(text);
        }
    }

    public void setElementOnClickListener(View.OnClickListener listener, int elementTag) {
        ImageView imageView;
        if (elementTag == 2) {
            ImageView imageView2 = this.mEmojiIv;
            if (imageView2 != null) {
                imageView2.setOnClickListener(listener);
            }
        } else if (elementTag == 3) {
            ImageView imageView3 = this.mSendIv;
            if (imageView3 != null) {
                imageView3.setOnClickListener(listener);
            }
        } else if (elementTag == 4 && (imageView = this.mMessageIv) != null) {
            imageView.setOnClickListener(listener);
        }
    }

    public void setElementVisibility(int visibility, int elementTag) {
        ImageView imageView;
        if (elementTag == 1) {
            ImageView imageView2 = this.mAddIv;
            if (imageView2 != null) {
                imageView2.setVisibility(visibility);
            }
        } else if (elementTag == 2) {
            ImageView imageView3 = this.mEmojiIv;
            if (imageView3 != null) {
                imageView3.setVisibility(visibility);
            }
        } else if (elementTag == 4 && (imageView = this.mMessageIv) != null) {
            imageView.setVisibility(visibility);
        }
        setLeftAndRightPadding();
    }

    public void setElementImageDrawable(Drawable image, int elementTag) {
        ImageView imageView;
        if (elementTag == 1) {
            ImageView imageView2 = this.mAddIv;
            if (imageView2 != null) {
                imageView2.setImageDrawable(image);
            }
        } else if (elementTag == 2) {
            ImageView imageView3 = this.mEmojiIv;
            if (imageView3 != null) {
                imageView3.setImageDrawable(image);
            }
        } else if (elementTag == 3) {
            ImageView imageView4 = this.mSendIv;
            if (imageView4 != null) {
                imageView4.setImageDrawable(image);
            }
        } else if (elementTag == 4 && (imageView = this.mMessageIv) != null) {
            imageView.setImageDrawable(image);
        }
    }

    public void setElementImageResource(int resId, int elementTag) {
        ImageView imageView;
        if (elementTag == 1) {
            ImageView imageView2 = this.mAddIv;
            if (imageView2 != null) {
                imageView2.setImageResource(resId);
            }
        } else if (elementTag == 2) {
            ImageView imageView3 = this.mEmojiIv;
            if (imageView3 != null) {
                imageView3.setImageResource(resId);
            }
        } else if (elementTag == 3) {
            ImageView imageView4 = this.mSendIv;
            if (imageView4 != null) {
                imageView4.setImageResource(resId);
            }
        } else if (elementTag == 4 && (imageView = this.mMessageIv) != null) {
            imageView.setImageResource(resId);
        }
    }

    public void setAttachmentViewDrawable(List<Drawable> imageList, List<CharSequence> textList, List<View.OnClickListener> listenerList) {
        List<Drawable> myImageList;
        List<CharSequence> myTextList;
        List<View.OnClickListener> myListenerList;
        float textSize;
        boolean isTwoLineText;
        int realAttachmentWidth;
        ImageView imageView;
        List<CharSequence> myTextList2;
        List<Drawable> myImageList2;
        int maxPosition;
        int index;
        ImageView imageView2;
        HwTextView mAttachmentTv;
        int realAttachmentWidth2;
        if (imageList == null) {
            myImageList = new ArrayList<>((int) LIST_LENGTH);
        } else {
            myImageList = imageList;
        }
        if (textList == null) {
            myTextList = new ArrayList<>((int) LIST_LENGTH);
        } else {
            myTextList = textList;
        }
        if (listenerList == null) {
            myListenerList = new ArrayList<>((int) LIST_LENGTH);
        } else {
            myListenerList = listenerList;
        }
        int textListSize = myTextList.size();
        int imageListSize = myImageList.size();
        int itemSize = imageListSize > textListSize ? imageListSize : textListSize;
        int itemSize2 = itemSize > 5 ? 5 : itemSize;
        int maxTextWidth = 0;
        int maxPosition2 = 0;
        float textSize2 = (float) ResLoaderUtil.getDimensionPixelSize(this.mContext, "emui_master_caption_2");
        HwTextView textView = new HwTextView(this.mContext);
        textView.setTextSize(0, textSize2);
        textView.setTypeface(this.mTypeface);
        for (int i = 0; i < textListSize; i++) {
            int textWidth = (int) textView.getPaint().measureText(myTextList.get(i).toString());
            if (textWidth > maxTextWidth) {
                maxTextWidth = textWidth;
                maxPosition2 = i;
            }
        }
        int attachmentViewWidth = (this.mAttachmentTextMargin * 2) + maxTextWidth;
        int i2 = this.mAttachmentWidth;
        if (attachmentViewWidth <= i2) {
            realAttachmentWidth = this.mAttachmentWidth;
            isTwoLineText = false;
            textSize = textSize2;
        } else if (attachmentViewWidth <= i2 || ((float) attachmentViewWidth) * textSize2 > ((float) this.mScreenWidth)) {
            if (attachmentViewWidth <= this.mAttachmentWidth || ((float) attachmentViewWidth) * textSize2 <= ((float) this.mScreenWidth)) {
                realAttachmentWidth2 = 0;
            } else {
                textSize2 = (float) ResLoaderUtil.getDimensionPixelSize(this.mContext, "hwpattern_commentsview_attachment_textsize");
                textView.setTextSize(0, textSize2);
                if (maxPosition2 >= 0 && maxPosition2 < myTextList.size()) {
                    maxTextWidth = (int) textView.getPaint().measureText(myTextList.get(maxPosition2).toString());
                }
                attachmentViewWidth = maxTextWidth + (this.mAttachmentTextMargin * 2);
                int i3 = this.mAttachmentWidth;
                if (attachmentViewWidth <= i3) {
                    realAttachmentWidth = this.mAttachmentWidth;
                    isTwoLineText = false;
                    textSize = textSize2;
                } else if (attachmentViewWidth > i3 && ((float) attachmentViewWidth) * textSize2 <= ((float) this.mScreenWidth)) {
                    realAttachmentWidth = attachmentViewWidth;
                    isTwoLineText = false;
                    textSize = textSize2;
                } else if (attachmentViewWidth > this.mAttachmentWidth) {
                    int i4 = this.mScreenWidth;
                    realAttachmentWidth2 = 0;
                    if (((float) attachmentViewWidth) * textSize2 > ((float) i4)) {
                        realAttachmentWidth = i4 / itemSize2;
                        isTwoLineText = true;
                        textSize = textSize2;
                    }
                } else {
                    realAttachmentWidth2 = 0;
                }
            }
            textSize = textSize2;
            realAttachmentWidth = realAttachmentWidth2;
            isTwoLineText = false;
        } else {
            realAttachmentWidth = attachmentViewWidth;
            isTwoLineText = false;
            textSize = textSize2;
        }
        int index2 = 0;
        while (index2 < itemSize2) {
            if (index2 < 0 || index2 >= imageListSize) {
                imageView = createImageView(null);
            } else {
                imageView = createImageView(myImageList.get(index2));
            }
            if (index2 < textListSize) {
                maxPosition = maxPosition2;
                myImageList2 = myImageList;
                imageView2 = imageView;
                myTextList2 = myTextList;
                index = index2;
                mAttachmentTv = createTextView(this.mContext, myTextList.get(index2).toString(), realAttachmentWidth, textSize, isTwoLineText);
            } else {
                maxPosition = maxPosition2;
                myImageList2 = myImageList;
                myTextList2 = myTextList;
                imageView2 = imageView;
                index = index2;
                mAttachmentTv = createTextView(this.mContext, BuildConfig.FLAVOR, realAttachmentWidth, textSize, isTwoLineText);
            }
            LinearLayout innerLayout = createInnerLayout();
            innerLayout.addView(imageView2);
            innerLayout.addView(mAttachmentTv);
            RelativeLayout outerLayout = new RelativeLayout(this.mContext);
            RelativeLayout.LayoutParams outerParams = new RelativeLayout.LayoutParams(-2, -2);
            if (itemSize2 < 5) {
                if (isTwoLineText) {
                    outerParams.width = realAttachmentWidth;
                } else {
                    outerParams.width = realAttachmentWidth;
                    outerParams.height = ResLoaderUtil.getDimensionPixelSize(this.mContext, "hwpattern_commentsview_attachment_height");
                }
                if (index == 0) {
                    outerParams.setMarginStart((this.mScreenWidth - (realAttachmentWidth * itemSize2)) / 2);
                }
                if (index == itemSize2 - 1) {
                    outerParams.setMarginEnd((this.mScreenWidth - (realAttachmentWidth * itemSize2)) / 2);
                }
            } else if (itemSize2 == 5) {
                if (isTwoLineText) {
                    outerParams.width = this.mScreenWidth / 5;
                } else {
                    outerParams.width = this.mScreenWidth / 5;
                    outerParams.height = ResLoaderUtil.getDimensionPixelSize(this.mContext, "hwpattern_commentsview_attachment_height");
                }
            }
            outerLayout.setClickable(true);
            outerLayout.setLayoutParams(outerParams);
            outerLayout.setBackground(HwWidgetUtils.getHwAnimatedGradientDrawable(this.mContext, 0));
            outerLayout.addView(innerLayout);
            if (index >= 0 && index < myListenerList.size()) {
                outerLayout.setOnClickListener(myListenerList.get(index));
            }
            LinearLayout linearLayout = this.mAttachmentLayout;
            if (linearLayout != null) {
                linearLayout.addView(outerLayout);
            }
            index2 = index + 1;
            maxPosition2 = maxPosition;
            myImageList = myImageList2;
            myTextList = myTextList2;
        }
    }

    private ImageView createImageView(Drawable drawable) {
        ImageView imageView = new ImageView(this.mContext);
        int i = this.mImageWidth;
        LinearLayout.LayoutParams imageParams = new LinearLayout.LayoutParams(i, i);
        imageParams.gravity = 1;
        imageView.setLayoutParams(imageParams);
        imageView.setImageDrawable(drawable);
        int i2 = this.mClickableColor;
        if (i2 != 0) {
            imageView.setColorFilter(i2);
        }
        return imageView;
    }

    private HwTextView createTextView(Context context, String text, int realAttachmentWidth, float textSize, boolean isTwoLineText) {
        HwTextView textView = new HwTextView(context);
        textView.setTextSize(0, textSize);
        textView.setTypeface(this.mTypeface);
        int i = this.mAttachmentTextColor;
        if (i != 0) {
            textView.setTextColor(i);
        }
        if (isTwoLineText) {
            textView.setText(text + System.lineSeparator());
        } else {
            textView.setText(text);
        }
        LinearLayout.LayoutParams textParams = new LinearLayout.LayoutParams(realAttachmentWidth - (this.mAttachmentTextMargin * 2), -2);
        textParams.setMargins(0, ResLoaderUtil.getDimensionPixelSize(this.mContext, "margin_xs"), 0, 0);
        textParams.gravity = 1;
        textView.setLayoutParams(textParams);
        textView.setGravity(17);
        textView.setMaxLines(2);
        textView.setEllipsize(TextUtils.TruncateAt.END);
        return textView;
    }

    private LinearLayout createInnerLayout() {
        LinearLayout innerLayout = new LinearLayout(this.mContext);
        innerLayout.setOrientation(1);
        RelativeLayout.LayoutParams innerParams = new RelativeLayout.LayoutParams(-2, -2);
        innerParams.setMargins(0, ResLoaderUtil.getDimensionPixelSize(this.mContext, "hwpattern_commentsview_margin"), 0, 0);
        innerParams.addRule(14);
        innerLayout.setLayoutParams(innerParams);
        return innerLayout;
    }
}
