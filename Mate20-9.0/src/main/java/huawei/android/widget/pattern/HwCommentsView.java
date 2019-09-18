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
    private int attachment_margin;
    private int attachment_text_margin;
    private int attachment_width;
    private int image_width;
    private ImageView mAddIv;
    /* access modifiers changed from: private */
    public LinearLayout mAttachmentLayout;
    private int mAttachmentTextColor;
    /* access modifiers changed from: private */
    public int mClickableColor;
    private Context mContext;
    private EditText mEdit;
    private ImageView mEmojiIv;
    private ImageView mMessageIv;
    /* access modifiers changed from: private */
    public ImageView mSendIv;
    /* access modifiers changed from: private */
    public int mUnClickableColor;
    private final int maxAttachmentViewSize;
    private int screen_width;
    private Typeface typeface;

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
        this.maxAttachmentViewSize = 5;
        this.mContext = context;
        initView();
    }

    private void editAddListener() {
        if (this.mEdit != null) {
            this.mEdit.addTextChangedListener(new TextWatcher() {
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
                        } else {
                            if (HwCommentsView.this.mUnClickableColor != 0) {
                                HwCommentsView.this.mSendIv.setColorFilter(HwCommentsView.this.mUnClickableColor);
                            }
                            HwCommentsView.this.mSendIv.setClickable(false);
                        }
                    }
                }
            });
        }
    }

    private void setLeftAndRightPadding() {
        int margin = ResLoaderUtil.getDimensionPixelSize(this.mContext, "margin_m");
        int mEmojiIvVisibility = 8;
        int mAddIvVisibility = 8;
        if (this.mEmojiIv != null) {
            mEmojiIvVisibility = this.mEmojiIv.getVisibility();
        }
        if (this.mAddIv != null) {
            mAddIvVisibility = this.mAddIv.getVisibility();
        }
        if (mAddIvVisibility != 8 && mEmojiIvVisibility != 8) {
            LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) this.mAddIv.getLayoutParams();
            params.setMarginEnd(margin);
            this.mAddIv.setLayoutParams(params);
        } else if (this.mEdit != null) {
            LinearLayout.LayoutParams params2 = (LinearLayout.LayoutParams) this.mEdit.getLayoutParams();
            params2.setMarginStart(0);
            this.mEdit.setLayoutParams(params2);
        }
        if (mAddIvVisibility == 8 && mEmojiIvVisibility == 8 && this.mEdit != null) {
            LinearLayout.LayoutParams params3 = (LinearLayout.LayoutParams) this.mEdit.getLayoutParams();
            params3.setMarginStart(margin);
            this.mEdit.setLayoutParams(params3);
        }
        if (this.mMessageIv != null && this.mMessageIv.getVisibility() != 8) {
            LinearLayout.LayoutParams params4 = (LinearLayout.LayoutParams) this.mMessageIv.getLayoutParams();
            params4.setMarginEnd(margin);
            this.mMessageIv.setLayoutParams(params4);
        }
    }

    private void initView() {
        this.attachment_margin = ResLoaderUtil.getDimensionPixelSize(this.mContext, "margin_l");
        this.screen_width = ResLoaderUtil.getResources(this.mContext).getDisplayMetrics().widthPixels;
        this.attachment_width = (this.screen_width - (this.attachment_margin * 2)) / 4;
        this.image_width = ResLoaderUtil.getDimensionPixelSize(this.mContext, "hwpattern_commentsview_image_width_s");
        this.attachment_text_margin = ResLoaderUtil.getDimensionPixelSize(this.mContext, "margin_s");
        this.typeface = Typeface.create("HwChinese-medium", 0);
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
        if (this.mAddIv != null) {
            this.mAddIv.setOnClickListener(new View.OnClickListener() {
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
        if (this.mClickableColor != 0) {
            if (this.mMessageIv != null) {
                this.mMessageIv.setColorFilter(this.mClickableColor);
            }
            if (this.mEmojiIv != null) {
                this.mEmojiIv.setColorFilter(this.mClickableColor);
            }
            if (this.mAddIv != null) {
                this.mAddIv.setColorFilter(this.mClickableColor);
            }
        }
        if (this.mEmojiIv != null) {
            this.mEmojiIv.setBackground(HwWidgetUtils.getHwAnimatedGradientDrawable(this.mContext, 0));
        }
        if (this.mMessageIv != null) {
            this.mMessageIv.setBackground(HwWidgetUtils.getHwAnimatedGradientDrawable(this.mContext, 0));
        }
        if (this.mAddIv != null) {
            this.mAddIv.setBackground(HwWidgetUtils.getHwAnimatedGradientDrawable(this.mContext, 0));
        }
        if (this.mSendIv != null) {
            this.mSendIv.setBackground(HwWidgetUtils.getHwAnimatedGradientDrawable(this.mContext, 0));
            this.mSendIv.setClickable(false);
        }
        setLeftAndRightPadding();
        editAddListener();
    }

    public void setEditHint(CharSequence text) {
        if (this.mEdit != null) {
            this.mEdit.setHint(text);
        }
    }

    public void setElementOnClickListener(View.OnClickListener listener, int elementTag) {
        switch (elementTag) {
            case 2:
                if (this.mEmojiIv != null) {
                    this.mEmojiIv.setOnClickListener(listener);
                    return;
                }
                return;
            case 3:
                if (this.mSendIv != null) {
                    this.mSendIv.setOnClickListener(listener);
                    return;
                }
                return;
            case 4:
                if (this.mMessageIv != null) {
                    this.mMessageIv.setOnClickListener(listener);
                    return;
                }
                return;
            default:
                return;
        }
    }

    public void setElementVisibility(int visibility, int elementTag) {
        if (elementTag != 4) {
            switch (elementTag) {
                case 1:
                    if (this.mAddIv != null) {
                        this.mAddIv.setVisibility(visibility);
                        break;
                    }
                    break;
                case 2:
                    if (this.mEmojiIv != null) {
                        this.mEmojiIv.setVisibility(visibility);
                        break;
                    }
                    break;
            }
        } else if (this.mMessageIv != null) {
            this.mMessageIv.setVisibility(visibility);
        }
        setLeftAndRightPadding();
    }

    public void setElementImageDrawable(Drawable image, int elementTag) {
        switch (elementTag) {
            case 1:
                if (this.mAddIv != null) {
                    this.mAddIv.setImageDrawable(image);
                    return;
                }
                return;
            case 2:
                if (this.mEmojiIv != null) {
                    this.mEmojiIv.setImageDrawable(image);
                    return;
                }
                return;
            case 3:
                if (this.mSendIv != null) {
                    this.mSendIv.setImageDrawable(image);
                    return;
                }
                return;
            case 4:
                if (this.mMessageIv != null) {
                    this.mMessageIv.setImageDrawable(image);
                    return;
                }
                return;
            default:
                return;
        }
    }

    public void setElementImageResource(int resId, int elementTag) {
        switch (elementTag) {
            case 1:
                if (this.mAddIv != null) {
                    this.mAddIv.setImageResource(resId);
                    return;
                }
                return;
            case 2:
                if (this.mEmojiIv != null) {
                    this.mEmojiIv.setImageResource(resId);
                    return;
                }
                return;
            case 3:
                if (this.mSendIv != null) {
                    this.mSendIv.setImageResource(resId);
                    return;
                }
                return;
            case 4:
                if (this.mMessageIv != null) {
                    this.mMessageIv.setImageResource(resId);
                    return;
                }
                return;
            default:
                return;
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:53:0x0105  */
    /* JADX WARNING: Removed duplicated region for block: B:88:0x01e7 A[SYNTHETIC] */
    public void setAttachmentViewDrawable(List<Drawable> imageList, List<CharSequence> textList, List<View.OnClickListener> listenerList) {
        List<Drawable> imageList2;
        List<CharSequence> textList2;
        List<View.OnClickListener> listenerList2;
        float textSize;
        boolean isTwoLineText;
        int realAttachmentWidth;
        int i;
        int i2;
        ImageView imageView;
        int maxPosition;
        List<CharSequence> textList3;
        List<Drawable> imageList3;
        int i3;
        ImageView imageView2;
        HwTextView mAttachmentTv;
        if (imageList == null) {
            imageList2 = new ArrayList<>();
        } else {
            imageList2 = imageList;
        }
        if (textList == null) {
            textList2 = new ArrayList<>();
        } else {
            textList2 = textList;
        }
        if (listenerList == null) {
            listenerList2 = new ArrayList<>();
        } else {
            listenerList2 = listenerList;
        }
        int textListSize = textList2.size();
        int imageListSize = imageList2.size();
        int itemSize = imageListSize > textListSize ? imageListSize : textListSize;
        int itemSize2 = itemSize > 5 ? 5 : itemSize;
        int realAttachmentWidth2 = 0;
        float textSize2 = (float) ResLoaderUtil.getDimensionPixelSize(this.mContext, "emui_master_caption_2");
        HwTextView textView = new HwTextView(this.mContext);
        textView.setTextSize(0, textSize2);
        textView.setTypeface(this.typeface);
        int maxPosition2 = 0;
        int maxTextWidth = 0;
        for (int i4 = 0; i4 < textListSize; i4++) {
            int textWidth = (int) textView.getPaint().measureText(textList2.get(i4).toString());
            if (textWidth > maxTextWidth) {
                maxTextWidth = textWidth;
                maxPosition2 = i4;
            }
        }
        int attachmentViewWidth = (this.attachment_text_margin * 2) + maxTextWidth;
        if (attachmentViewWidth <= this.attachment_width) {
            realAttachmentWidth2 = this.attachment_width;
        } else if (attachmentViewWidth > this.attachment_width && ((float) attachmentViewWidth) * textSize2 <= ((float) this.screen_width)) {
            realAttachmentWidth2 = attachmentViewWidth;
        } else if (attachmentViewWidth > this.attachment_width && ((float) attachmentViewWidth) * textSize2 > ((float) this.screen_width)) {
            textSize2 = (float) ResLoaderUtil.getDimensionPixelSize(this.mContext, "hwpattern_commentsview_attachment_textsize");
            textView.setTextSize(0, textSize2);
            maxTextWidth = (int) textView.getPaint().measureText(textList2.get(maxPosition2).toString());
            attachmentViewWidth = maxTextWidth + (this.attachment_text_margin * 2);
            if (attachmentViewWidth <= this.attachment_width) {
                realAttachmentWidth2 = this.attachment_width;
            } else if (attachmentViewWidth > this.attachment_width && ((float) attachmentViewWidth) * textSize2 <= ((float) this.screen_width)) {
                realAttachmentWidth2 = attachmentViewWidth;
            } else if (attachmentViewWidth > this.attachment_width && ((float) attachmentViewWidth) * textSize2 > ((float) this.screen_width)) {
                isTwoLineText = true;
                int i5 = maxTextWidth;
                textSize = textSize2;
                realAttachmentWidth = this.screen_width / itemSize2;
                int realAttachmentWidth3 = attachmentViewWidth;
                i = 0;
                while (true) {
                    i2 = i;
                    if (i2 >= itemSize2) {
                        if (i2 < imageListSize) {
                            imageView = createImageView(imageList2.get(i2));
                        } else {
                            imageView = createImageView(null);
                        }
                        ImageView imageView3 = imageView;
                        if (i2 < textListSize) {
                            Context context = this.mContext;
                            String charSequence = textList2.get(i2).toString();
                            imageList3 = imageList2;
                            imageView2 = imageView3;
                            textList3 = textList2;
                            i3 = i2;
                            maxPosition = maxPosition2;
                            mAttachmentTv = createTextView(context, charSequence, realAttachmentWidth, textSize, isTwoLineText);
                        } else {
                            maxPosition = maxPosition2;
                            imageList3 = imageList2;
                            textList3 = textList2;
                            imageView2 = imageView3;
                            i3 = i2;
                            mAttachmentTv = createTextView(this.mContext, "", realAttachmentWidth, textSize, isTwoLineText);
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
                            if (i3 == 0) {
                                outerParams.setMarginStart((this.screen_width - (realAttachmentWidth * itemSize2)) / 2);
                            }
                            if (i3 == itemSize2 - 1) {
                                outerParams.setMarginEnd((this.screen_width - (realAttachmentWidth * itemSize2)) / 2);
                            }
                        } else if (itemSize2 == 5) {
                            if (isTwoLineText) {
                                outerParams.width = this.screen_width / 5;
                            } else {
                                outerParams.width = this.screen_width / 5;
                                outerParams.height = ResLoaderUtil.getDimensionPixelSize(this.mContext, "hwpattern_commentsview_attachment_height");
                            }
                        }
                        outerLayout.setClickable(true);
                        outerLayout.setLayoutParams(outerParams);
                        outerLayout.setBackground(HwWidgetUtils.getHwAnimatedGradientDrawable(this.mContext, 0));
                        outerLayout.addView(innerLayout);
                        if (i3 < listenerList2.size()) {
                            outerLayout.setOnClickListener(listenerList2.get(i3));
                        }
                        if (this.mAttachmentLayout != null) {
                            this.mAttachmentLayout.addView(outerLayout);
                        }
                        i = i3 + 1;
                        imageList2 = imageList3;
                        textList2 = textList3;
                        maxPosition2 = maxPosition;
                    } else {
                        List<Drawable> list = imageList2;
                        List<CharSequence> list2 = textList2;
                        return;
                    }
                }
            }
        }
        realAttachmentWidth = realAttachmentWidth2;
        isTwoLineText = false;
        textSize = textSize2;
        i = 0;
        while (true) {
            i2 = i;
            if (i2 >= itemSize2) {
            }
            i = i3 + 1;
            imageList2 = imageList3;
            textList2 = textList3;
            maxPosition2 = maxPosition;
        }
    }

    private ImageView createImageView(Drawable drawable) {
        ImageView imageView = new ImageView(this.mContext);
        LinearLayout.LayoutParams imageParams = new LinearLayout.LayoutParams(this.image_width, this.image_width);
        imageParams.gravity = 1;
        imageView.setLayoutParams(imageParams);
        imageView.setImageDrawable(drawable);
        if (this.mClickableColor != 0) {
            imageView.setColorFilter(this.mClickableColor);
        }
        return imageView;
    }

    private HwTextView createTextView(Context context, String text, int realAttachmentWidth, float textSize, boolean isTwoLineText) {
        HwTextView textView = new HwTextView(context);
        textView.setTextSize(0, textSize);
        textView.setTypeface(this.typeface);
        if (this.mAttachmentTextColor != 0) {
            textView.setTextColor(this.mAttachmentTextColor);
        }
        if (isTwoLineText) {
            textView.setText(text + "\n");
        } else {
            textView.setText(text);
        }
        LinearLayout.LayoutParams textParams = new LinearLayout.LayoutParams(realAttachmentWidth - (this.attachment_text_margin * 2), -2);
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
