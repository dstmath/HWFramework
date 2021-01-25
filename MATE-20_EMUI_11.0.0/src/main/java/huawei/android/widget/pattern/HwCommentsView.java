package huawei.android.widget.pattern;

import android.content.Context;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
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
    private int mMaxPosition;
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

                @Override // android.text.TextWatcher
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                }

                @Override // android.text.TextWatcher
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                }

                @Override // android.text.TextWatcher
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
                ViewGroup.LayoutParams paramsTemp = editText2.getLayoutParams();
                if (paramsTemp instanceof LinearLayout.LayoutParams) {
                    LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) paramsTemp;
                    params.setMarginStart(0);
                    this.mEdit.setLayoutParams(params);
                }
            }
        } else {
            ViewGroup.LayoutParams paramsTemp2 = this.mAddIv.getLayoutParams();
            if (paramsTemp2 instanceof LinearLayout.LayoutParams) {
                LinearLayout.LayoutParams params2 = (LinearLayout.LayoutParams) paramsTemp2;
                params2.setMarginEnd(margin);
                this.mAddIv.setLayoutParams(params2);
            }
        }
        if (mAddIvVisibility == 8 && mEmojiIvVisibility == 8 && (editText = this.mEdit) != null) {
            ViewGroup.LayoutParams paramsTemp3 = editText.getLayoutParams();
            if (paramsTemp3 instanceof LinearLayout.LayoutParams) {
                LinearLayout.LayoutParams params3 = (LinearLayout.LayoutParams) paramsTemp3;
                params3.setMarginStart(margin);
                this.mEdit.setLayoutParams(params3);
            }
        }
        ImageView imageView3 = this.mMessageIv;
        if (imageView3 != null && imageView3.getVisibility() != 8) {
            ViewGroup.LayoutParams paramsTemp4 = this.mMessageIv.getLayoutParams();
            if (paramsTemp4 instanceof LinearLayout.LayoutParams) {
                LinearLayout.LayoutParams params4 = (LinearLayout.LayoutParams) paramsTemp4;
                params4.setMarginEnd(margin);
                this.mMessageIv.setLayoutParams(params4);
            }
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
        if (this.mClickableColor != 0) {
            setViewColorFilter(this.mMessageIv);
            setViewColorFilter(this.mEmojiIv);
            setViewColorFilter(this.mAddIv);
        }
        setViewHwAnimatedGradientDrawable(this.mEmojiIv);
        setViewHwAnimatedGradientDrawable(this.mMessageIv);
        setViewHwAnimatedGradientDrawable(this.mAddIv);
        setViewHwAnimatedGradientDrawable(this.mSendIv);
        ImageView imageView = this.mSendIv;
        if (imageView != null) {
            imageView.setClickable(false);
        }
        ImageView imageView2 = this.mAddIv;
        if (!(imageView2 == null || this.mAttachmentLayout == null)) {
            imageView2.setOnClickListener(new View.OnClickListener() {
                /* class huawei.android.widget.pattern.HwCommentsView.AnonymousClass2 */

                @Override // android.view.View.OnClickListener
                public void onClick(View view) {
                    if (HwCommentsView.this.mAttachmentLayout.getVisibility() == 8) {
                        HwCommentsView.this.mAttachmentLayout.setVisibility(0);
                    } else if (HwCommentsView.this.mAttachmentLayout.getVisibility() == 0) {
                        HwCommentsView.this.mAttachmentLayout.setVisibility(8);
                    }
                }
            });
        }
        setLeftAndRightPadding();
        editAddListener();
    }

    private void setViewColorFilter(ImageView view) {
        if (view != null) {
            view.setColorFilter(this.mClickableColor);
        }
    }

    private void setViewHwAnimatedGradientDrawable(View view) {
        if (view != null) {
            view.setBackground(HwWidgetUtils.getHwAnimatedGradientDrawable(this.mContext, 0));
        }
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
        int realAttachmentWidth;
        boolean isTwoLineText;
        float textSize;
        List<View.OnClickListener> myListenerList;
        if (this.mAttachmentLayout != null) {
            List<Drawable> myImageList = getImageList(imageList);
            List<CharSequence> myTextList = getTextList(textList);
            int itemSize = getItemSize(myTextList.size(), myImageList.size());
            float textSize2 = (float) ResLoaderUtil.getDimensionPixelSize(this.mContext, "emui_master_caption_2");
            HwTextView textView = new HwTextView(this.mContext);
            textView.setTypeface(this.mTypeface);
            textView.setTextSize(0, textSize2);
            int maxTextWidth = getMaxTextWidth(textView, myTextList);
            int attachmentViewWidth = (this.mAttachmentTextMargin * 2) + maxTextWidth;
            int i = this.mAttachmentWidth;
            if (attachmentViewWidth <= i) {
                textSize = textSize2;
                realAttachmentWidth = this.mAttachmentWidth;
                isTwoLineText = false;
            } else if (attachmentViewWidth <= i || ((float) attachmentViewWidth) * textSize2 > ((float) this.mScreenWidth)) {
                if (attachmentViewWidth > this.mAttachmentWidth && ((float) attachmentViewWidth) * textSize2 > ((float) this.mScreenWidth)) {
                    textSize2 = (float) ResLoaderUtil.getDimensionPixelSize(this.mContext, "hwpattern_commentsview_attachment_textsize");
                    textView.setTextSize(0, textSize2);
                    maxTextWidth = (int) textView.getPaint().measureText(myTextList.get(this.mMaxPosition).toString());
                    attachmentViewWidth = maxTextWidth + (this.mAttachmentTextMargin * 2);
                    int realAttachmentWidth2 = this.mAttachmentWidth;
                    if (attachmentViewWidth <= realAttachmentWidth2) {
                        textSize = textSize2;
                        realAttachmentWidth = this.mAttachmentWidth;
                        isTwoLineText = false;
                    } else if (attachmentViewWidth > realAttachmentWidth2 && ((float) attachmentViewWidth) * textSize2 <= ((float) this.mScreenWidth)) {
                        textSize = textSize2;
                        realAttachmentWidth = attachmentViewWidth;
                        isTwoLineText = false;
                    } else if (attachmentViewWidth > this.mAttachmentWidth) {
                        int i2 = this.mScreenWidth;
                        if (((float) attachmentViewWidth) * textSize2 > ((float) i2)) {
                            isTwoLineText = true;
                            realAttachmentWidth = i2 / itemSize;
                            textSize = textSize2;
                        }
                    }
                }
                textSize = textSize2;
                isTwoLineText = false;
                realAttachmentWidth = 0;
            } else {
                textSize = textSize2;
                realAttachmentWidth = attachmentViewWidth;
                isTwoLineText = false;
            }
            List<View.OnClickListener> myListenerList2 = getListenerList(listenerList);
            for (int index = 0; index < itemSize; index++) {
                ImageView imageView = createImageView(myImageList, index);
                HwTextView hwTextView = createTextView(myTextList, index, realAttachmentWidth, textSize, isTwoLineText);
                LinearLayout innerLayout = createInnerLayout();
                innerLayout.addView(imageView);
                innerLayout.addView(hwTextView);
                RelativeLayout outerLayout = createOuterLayout(index, realAttachmentWidth, itemSize, isTwoLineText);
                outerLayout.addView(innerLayout);
                if (index < myListenerList2.size()) {
                    myListenerList = myListenerList2;
                    outerLayout.setOnClickListener(myListenerList.get(index));
                } else {
                    myListenerList = myListenerList2;
                }
                this.mAttachmentLayout.addView(outerLayout);
                myListenerList2 = myListenerList;
                myImageList = myImageList;
            }
        }
    }

    private int getMaxTextWidth(HwTextView textView, List<CharSequence> textList) {
        int maxTextWidth = 0;
        this.mMaxPosition = 0;
        int textListSize = textList.size();
        for (int i = 0; i < textListSize; i++) {
            int textWidth = (int) textView.getPaint().measureText(textList.get(i).toString());
            if (textWidth > maxTextWidth) {
                maxTextWidth = textWidth;
                this.mMaxPosition = i;
            }
        }
        return maxTextWidth;
    }

    private List<View.OnClickListener> getListenerList(List<View.OnClickListener> listenerList) {
        if (listenerList == null) {
            return new ArrayList<>((int) LIST_LENGTH);
        }
        return listenerList;
    }

    private List<CharSequence> getTextList(List<CharSequence> textList) {
        if (textList == null) {
            return new ArrayList<>((int) LIST_LENGTH);
        }
        return textList;
    }

    private List<Drawable> getImageList(List<Drawable> imageList) {
        if (imageList == null) {
            return new ArrayList<>((int) LIST_LENGTH);
        }
        return imageList;
    }

    private int getItemSize(int textListSize, int imageListSize) {
        int itemSize = imageListSize > textListSize ? imageListSize : textListSize;
        int itemSize2 = 5;
        if (itemSize <= 5) {
            itemSize2 = itemSize;
        }
        return itemSize2;
    }

    private RelativeLayout createOuterLayout(int index, int realAttachmentWidth, int itemSize, boolean isTwoLineText) {
        RelativeLayout outerLayout = new RelativeLayout(this.mContext);
        RelativeLayout.LayoutParams outerParams = new RelativeLayout.LayoutParams(-2, -2);
        if (itemSize < 5) {
            outerParams.width = realAttachmentWidth;
            if (!isTwoLineText) {
                outerParams.height = ResLoaderUtil.getDimensionPixelSize(this.mContext, "hwpattern_commentsview_attachment_height");
            }
            if (index == 0) {
                outerParams.setMarginStart((this.mScreenWidth - (realAttachmentWidth * itemSize)) / 2);
            }
            if (index == itemSize - 1) {
                outerParams.setMarginEnd((this.mScreenWidth - (realAttachmentWidth * itemSize)) / 2);
            }
        } else if (itemSize == 5) {
            outerParams.width = this.mScreenWidth / 5;
            if (!isTwoLineText) {
                outerParams.height = ResLoaderUtil.getDimensionPixelSize(this.mContext, "hwpattern_commentsview_attachment_height");
            }
        }
        outerLayout.setClickable(true);
        outerLayout.setLayoutParams(outerParams);
        outerLayout.setBackground(HwWidgetUtils.getHwAnimatedGradientDrawable(this.mContext, 0));
        return outerLayout;
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

    private ImageView createImageView(List<Drawable> imageList, int index) {
        if (index < imageList.size()) {
            return createImageView(imageList.get(index));
        }
        return createImageView(null);
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

    private HwTextView createTextView(List<CharSequence> textList, int index, int realAttachmentWidth, float textSize, boolean isTwoLineText) {
        if (index < textList.size()) {
            return createTextView(this.mContext, textList.get(index).toString(), realAttachmentWidth, textSize, isTwoLineText);
        }
        return createTextView(this.mContext, "", realAttachmentWidth, textSize, isTwoLineText);
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
