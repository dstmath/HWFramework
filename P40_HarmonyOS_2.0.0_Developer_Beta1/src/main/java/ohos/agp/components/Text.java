package ohos.agp.components;

import java.io.IOException;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Predicate;
import ohos.aafwk.utils.log.LogDomain;
import ohos.agp.colors.RgbPalette;
import ohos.agp.components.Component;
import ohos.agp.components.ComponentObserverHandler;
import ohos.agp.components.Text;
import ohos.agp.components.element.Element;
import ohos.agp.components.element.ShapeElement;
import ohos.agp.styles.Style;
import ohos.agp.styles.Value;
import ohos.agp.styles.attributes.TextAttrsConstants;
import ohos.agp.text.Font;
import ohos.agp.text.RichText;
import ohos.agp.utils.Color;
import ohos.agp.utils.TextTool;
import ohos.app.Context;
import ohos.global.resource.NotExistException;
import ohos.global.resource.ResourceManager;
import ohos.global.resource.WrongTypeException;
import ohos.hiviewdfx.HiLog;
import ohos.hiviewdfx.HiLogLabel;
import ohos.miscservices.inputmethod.EditingCapability;
import ohos.miscservices.inputmethod.EditingText;
import ohos.miscservices.inputmethod.InputDataChannel;
import ohos.multimodalinput.event.KeyBoardEvent;
import ohos.multimodalinput.event.KeyEvent;

public class Text extends Component {
    public static final int AUTO_CURSOR_POSITION = -1;
    public static final int AUTO_SCROLLING_FOREVER = -1;
    private static final int ELEMENT_BOTTOM = 3;
    private static final int ELEMENT_CURSOR_BUBBLE = 6;
    private static final int ELEMENT_END = 2;
    private static final int ELEMENT_INDEX_BOTTOM = 3;
    private static final int ELEMENT_INDEX_END = 5;
    private static final int ELEMENT_INDEX_LEFT = 0;
    private static final int ELEMENT_INDEX_RIGHT = 2;
    private static final int ELEMENT_INDEX_START = 4;
    private static final int ELEMENT_INDEX_TOP = 1;
    private static final int ELEMENT_SELECTION_LEFT_BUBBLE = 7;
    private static final int ELEMENT_SELECTION_RIGHT_BUBBLE = 8;
    private static final int ELEMENT_START = 0;
    private static final int ELEMENT_TOP = 1;
    private static final Map<String, BiConsumer<Text, Value>> STYLE_METHOD_MAP = new LinkedHashMap<String, BiConsumer<Text, Value>>() {
        /* class ohos.agp.components.Text.AnonymousClass1 */

        {
            put(TextAttrsConstants.ELEMENT_LEFT, $$Lambda$Text$1$NzBGHfqAPsEUBe1veGtwWMF3kH0.INSTANCE);
            put(TextAttrsConstants.ELEMENT_TOP, $$Lambda$Text$1$rgyTu795Fg8GNjihUQtkoKCJVQ.INSTANCE);
            put(TextAttrsConstants.ELEMENT_RIGHT, $$Lambda$Text$1$TnLcJ0kie0dzPXUvG7RXRu9xv6U.INSTANCE);
            put(TextAttrsConstants.ELEMENT_BOTTOM, $$Lambda$Text$1$4xPEyZgSYM1EOeOcL73q0EDHIQ0.INSTANCE);
            put(TextAttrsConstants.ELEMENT_START, $$Lambda$Text$1$w6BdDIb7RKAppOiSKWCf7Rr9Ug.INSTANCE);
            put(TextAttrsConstants.ELEMENT_END, $$Lambda$Text$1$naYD7sFzHxEX2vMuzzddx8AmsJE.INSTANCE);
            put(TextAttrsConstants.ELEMENT_CURSOR_BUBBLE, $$Lambda$Text$1$2ZQr5R6a5vEJlNdnvcokaJcEA0.INSTANCE);
            put(TextAttrsConstants.ELEMENT_SELECTION_LEFT_BUBBLE, $$Lambda$Text$1$_vakBvz84yE_PNTVDGM_FucMufo.INSTANCE);
            put(TextAttrsConstants.ELEMENT_SELECTION_RIGHT_BUBBLE, $$Lambda$Text$1$Wp6pgYoN69GCb5uYppgXgxWAR4.INSTANCE);
            put(TextAttrsConstants.TEXT_WEIGHT, $$Lambda$Text$1$yW92LCguMkWWqcX6K4Gc71kYVg.INSTANCE);
            put(TextAttrsConstants.ITALIC, $$Lambda$Text$1$qWO7W0KNKKmTvwWz7jlcN2MS6fY.INSTANCE);
            put(TextAttrsConstants.TEXT_FONT, $$Lambda$Text$1$6TVR3fzgOtMrcXg1FXtPA6Gpluw.INSTANCE);
            put(TextAttrsConstants.INPUT_ENTER_KEY_TYPE, $$Lambda$Text$1$V87CoYbheWo9_pdaz581RDDFrhg.INSTANCE);
            put(TextAttrsConstants.AUTO_SCROLLING_DURATION, $$Lambda$Text$1$QDCHcoNhk0Jh1D2gkjG6mRm7ZQ.INSTANCE);
            put(TextAttrsConstants.AUTO_SCROLLING_COUNT, $$Lambda$Text$1$I2d86JY8q__xunm616avwihAzg8.INSTANCE);
            put(TextAttrsConstants.TRUNCATION_MODE, $$Lambda$Text$1$W3P3_RjQ2EoE3J_zzQiwfEopIIU.INSTANCE);
            put(TextAttrsConstants.PADDING_FOR_TEXT, $$Lambda$Text$1$UQglRzRbocOsR7wvrL4lspdAmIs.INSTANCE);
        }

        static /* synthetic */ void lambda$new$0(Text text, Value value) {
            text.mAttrElements[0] = value.asElement();
        }

        static /* synthetic */ void lambda$new$1(Text text, Value value) {
            text.mAttrElements[1] = value.asElement();
        }

        static /* synthetic */ void lambda$new$2(Text text, Value value) {
            text.mAttrElements[2] = value.asElement();
        }

        static /* synthetic */ void lambda$new$3(Text text, Value value) {
            text.mAttrElements[3] = value.asElement();
        }

        static /* synthetic */ void lambda$new$4(Text text, Value value) {
            text.mAttrElements[4] = value.asElement();
        }

        static /* synthetic */ void lambda$new$5(Text text, Value value) {
            text.mAttrElements[5] = value.asElement();
        }

        static /* synthetic */ void lambda$new$6(Text text, Value value) {
            text.mAttrElements[6] = value.asElement();
        }

        static /* synthetic */ void lambda$new$7(Text text, Value value) {
            text.mAttrElements[7] = value.asElement();
        }

        static /* synthetic */ void lambda$new$8(Text text, Value value) {
            text.mAttrElements[8] = value.asElement();
        }

        static /* synthetic */ void lambda$new$15(Text text, Value value) {
            try {
                text.setTruncationMode(TruncationMode.valueOf(value.asString().toUpperCase(Locale.ENGLISH)));
            } catch (IllegalArgumentException unused) {
                HiLog.error(Text.TAG, "wrong parameter in truncation mode", new Object[0]);
            }
        }
    };
    private static final HiLogLabel TAG = new HiLogLabel(3, (int) LogDomain.END, "AGP_VIEW");
    private static final int TEXTCOLOR_CALLBACK_PARAM_NUM = 2;
    private static final int TEXTSIZE_CALLBACK_PARAM_NUM = 2;
    private boolean adjustInputPanel;
    private Element[] mAttrElements;
    private Element mCursorBubbleElement;
    private Element mCursorElement;
    private Element[] mElements;
    private boolean mElementsRelative;
    private Font mFont;
    private int mImeOption;
    private TextViewInputDataChannel mInputDataChannel;
    private Component.LayoutDirection mLastLayoutDirection;
    private EditorActionListener mOnEditorActionListener;
    private RichText mRichText;
    private Element mSelectionLeftBubbleElement;
    private Element mSelectionRightBubbleElement;
    private final TextColorObserversHandler mTextColorObserversHandler;
    private final TextSizeObserversHandler mTextSizeObserversHandler;
    private final TextWatchersHandler mTextWatchersHandler;

    public interface EditorActionListener {
        boolean onTextEditorAction(int i);
    }

    public interface TextColorObserver extends ComponentObserverHandler.Observer {
        void onTextColorChanged(int i, int i2);
    }

    public interface TextObserver extends ComponentObserverHandler.Observer {
        void onTextUpdated(String str, int i, int i2, int i3);
    }

    public interface TextSizeObserver extends ComponentObserverHandler.Observer {
        void onTextSizeChanged(int i, int i2);
    }

    private int convertToPx(int i, float f) {
        return (int) ((((float) i) * f) + 0.5f);
    }

    static /* synthetic */ boolean lambda$setFont$0(Font font) {
        return font != null;
    }

    private native void nativeAppend(long j, String str);

    private native void nativeDeleteText(long j, int i, boolean z, int i2);

    private native boolean nativeGetAutoFontSize(long j);

    private native int nativeGetBubbleHeight(long j);

    private native int nativeGetBubbleWidth(long j);

    private native int nativeGetCompoundDrawablesPadding(long j);

    private native String nativeGetEditableText(long j);

    private native int nativeGetEllipsize(long j);

    private native int nativeGetFadeEffectBoundaryWidth(long j);

    private native String nativeGetHint(long j);

    private native int nativeGetHintTextColor(long j);

    private native float nativeGetLineSpacingExtra(long j);

    private native float nativeGetLineSpacingMultiplier(long j);

    private native int nativeGetMarqueeCount(long j);

    private native long nativeGetMarqueeDuration(long j);

    private native int nativeGetMaxHeight(long j);

    private native int nativeGetMaxLines(long j);

    private native int nativeGetMaxWidth(long j);

    private native boolean nativeGetPaddingForText(long j);

    private native int nativeGetSelectionColor(long j);

    private native int nativeGetSelectionLeftBubbleHeight(long j);

    private native int nativeGetSelectionLeftBubbleWidth(long j);

    private native int nativeGetSelectionRightBubbleHeight(long j);

    private native int nativeGetSelectionRightBubbleWidth(long j);

    private native String nativeGetText(long j);

    private native int nativeGetTextAlignment(long j);

    private native int nativeGetTextColor(long j);

    private native int nativeGetTextInputType(long j);

    private native int nativeGetTextSize(long j);

    private native long nativeGetTextViewHandle();

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private native boolean nativeInputDataChannelDeleteBackward(long j, int i);

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private native boolean nativeInputDataChannelDeleteForward(long j, int i);

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private native long nativeInputDataChannelGetHandle(long j);

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private native String nativeInputDataChannelGetTextAfterCursor(long j, int i);

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private native String nativeInputDataChannelGetTextBeforeCursor(long j, int i);

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private native boolean nativeInputDataChannelInsertText(long j, String str);

    private native void nativeInsertText(long j, String str, int i);

    private native boolean nativeIsCursorVisible(long j);

    private native boolean nativeIsMultipleLine(long j);

    private native boolean nativeIsScrollable(long j);

    private native int nativeLength(long j);

    private native void nativeSetAutoFontSize(long j, boolean z, boolean z2);

    private native void nativeSetAutoFontSizeConfiguration(long j, int i, int i2, int i3);

    private native void nativeSetAutoFontSizePreSet(long j, int[] iArr);

    private native void nativeSetBubbleSize(long j, int i, int i2);

    private native void nativeSetCompoundDrawables(long j, long[] jArr);

    private native void nativeSetCompoundDrawablesPadding(long j, int i);

    private native void nativeSetCompoundDrawablesRelative(long j, long[] jArr);

    private native void nativeSetCursorBubbleElement(long j, long j2);

    private native void nativeSetCursorVisible(long j, boolean z);

    private native void nativeSetEllipsize(long j, int i);

    private native void nativeSetFadeEffectBoundaryWidth(long j, int i);

    private native void nativeSetHint(long j, String str);

    private native void nativeSetHintTextColor(long j, int i);

    private native void nativeSetLeftBubbleSize(long j, int i, int i2);

    private native void nativeSetLineSpacing(long j, float f, float f2);

    private native void nativeSetMarqueeCount(long j, int i);

    private native void nativeSetMarqueeDuration(long j, long j2);

    private native void nativeSetMaxHeight(long j, int i);

    private native void nativeSetMaxLines(long j, int i);

    private native void nativeSetMaxWidth(long j, int i);

    private native void nativeSetMultipleLine(long j, boolean z);

    private native void nativeSetPaddingForText(long j, boolean z);

    private native void nativeSetRichText(long j, long j2);

    private native void nativeSetRightBubbleSize(long j, int i, int i2);

    private native void nativeSetScrollable(long j, boolean z);

    private native void nativeSetSelectionColor(long j, int i);

    private native void nativeSetSelectionLeftBubbleElement(long j, long j2);

    private native void nativeSetSelectionRightBubbleElement(long j, long j2);

    private native void nativeSetText(long j, String str);

    private native boolean nativeSetTextAlignment(long j, int i);

    private native void nativeSetTextColor(long j, int i);

    private native void nativeSetTextCursorDrawable(long j, long j2);

    private native void nativeSetTextInputType(long j, int i);

    private native void nativeSetTextSize(long j, int i);

    private native void nativeSetTextWatchersHandler(long j, TextWatchersHandler textWatchersHandler);

    private native void nativeSetTypeface(long j, long j2);

    private native void nativeStartMarquee(long j);

    private native void nativeStopMarquee(long j);

    public enum TruncationMode {
        NONE(0),
        ELLIPSIS_AT_START(1),
        ELLIPSIS_AT_MIDDLE(2),
        ELLIPSIS_AT_END(3),
        AUTO_SCROLLING(4);
        
        private final int enumInt;

        private TruncationMode(int i) {
            this.enumInt = i;
        }

        public int value() {
            return this.enumInt;
        }

        public static TruncationMode getByInt(int i) {
            return (TruncationMode) Arrays.stream(values()).filter(new Predicate(i) {
                /* class ohos.agp.components.$$Lambda$Text$TruncationMode$bRMtM2MUyjYxbvDr6eB3B8HCI */
                private final /* synthetic */ int f$0;

                {
                    this.f$0 = r1;
                }

                @Override // java.util.function.Predicate
                public final boolean test(Object obj) {
                    return Text.TruncationMode.lambda$getByInt$0(this.f$0, (Text.TruncationMode) obj);
                }
            }).findAny().orElse(NONE);
        }

        static /* synthetic */ boolean lambda$getByInt$0(int i, TruncationMode truncationMode) {
            return truncationMode.value() == i;
        }
    }

    /* access modifiers changed from: private */
    public class TextViewInputDataChannel extends TextInputDataChannel {
        private int deleteLength = 1;
        private KeyBoardEvent latestKeyBoardEvent = null;
        private long mNativeInputDataChannelPtr = 0;
        private final Text mParent;

        TextViewInputDataChannel(Text text) {
            createNativePtr();
            this.mParent = text;
        }

        private void createNativePtr() {
            if (this.mNativeInputDataChannelPtr == 0) {
                Text text = Text.this;
                this.mNativeInputDataChannelPtr = text.nativeInputDataChannelGetHandle(text.mNativeViewPtr);
            }
        }

        @Override // ohos.agp.components.TextInputDataChannel
        public boolean selectText(int i, int i2) {
            Text.this.insert("", i);
            return false;
        }

        @Override // ohos.agp.components.TextInputDataChannel
        public boolean insertText(String str) {
            return Text.this.nativeInputDataChannelInsertText(this.mNativeInputDataChannelPtr, str);
        }

        @Override // ohos.agp.components.TextInputDataChannel
        public boolean deleteBackward(int i) {
            return Text.this.nativeInputDataChannelDeleteBackward(this.mNativeInputDataChannelPtr, i);
        }

        @Override // ohos.agp.components.TextInputDataChannel
        public boolean deleteForward(int i) {
            return Text.this.nativeInputDataChannelDeleteForward(this.mNativeInputDataChannelPtr, i);
        }

        @Override // ohos.agp.components.TextInputDataChannel
        public String getForward(int i) {
            return Text.this.nativeInputDataChannelGetTextBeforeCursor(this.mNativeInputDataChannelPtr, i);
        }

        @Override // ohos.agp.components.TextInputDataChannel
        public String getBackward(int i) {
            return Text.this.nativeInputDataChannelGetTextAfterCursor(this.mNativeInputDataChannelPtr, i);
        }

        @Override // ohos.agp.components.TextInputDataChannel
        public boolean sendKeyEvent(KeyEvent keyEvent) {
            Text text = this.mParent;
            if (text == null) {
                return false;
            }
            if (text.mKeyEventListener == null) {
                return handleKeyEvent(keyEvent);
            }
            if (this.mParent.mKeyEventListener.onKeyEvent(this.mParent, keyEvent)) {
                return true;
            }
            return handleKeyEvent(keyEvent);
        }

        @Override // ohos.agp.components.TextInputDataChannel
        public EditingText subscribeEditingText(EditingCapability editingCapability) {
            EditingText editingText = new EditingText();
            editingText.setTextContent(this.mParent.getEditableString());
            editingText.setPrompt(this.mParent.getHint());
            return editingText;
        }

        @Override // ohos.agp.components.TextInputDataChannel
        public boolean sendKeyFunction(int i) {
            if (this.mParent.mOnEditorActionListener != null) {
                return this.mParent.mOnEditorActionListener.onTextEditorAction(i);
            }
            return super.sendKeyFunction(i);
        }

        private boolean handleKeyEvent(KeyEvent keyEvent) {
            if (keyEvent instanceof KeyBoardEvent) {
                return handleKeyBoardEvent((KeyBoardEvent) keyEvent);
            }
            return false;
        }

        private boolean handleKeyBoardEvent(KeyBoardEvent keyBoardEvent) {
            HiLog.debug(Text.TAG, "KeyBoardEvent is %{public}d %{public}d %{public}d", new Object[]{Integer.valueOf(keyBoardEvent.getKeyCode()), Long.valueOf(keyBoardEvent.getOccurredTime()), Long.valueOf(keyBoardEvent.getKeyDownDuration())});
            if (keyBoardEvent.getKeyCode() != 2055 || !keyBoardEvent.isKeyDown()) {
                return false;
            }
            KeyBoardEvent keyBoardEvent2 = this.latestKeyBoardEvent;
            if (keyBoardEvent2 != null && keyBoardEvent2.getKeyCode() == keyBoardEvent.getKeyCode() && this.latestKeyBoardEvent.getOccurredTime() == keyBoardEvent.getOccurredTime()) {
                this.deleteLength++;
            } else {
                this.deleteLength = 1;
            }
            this.latestKeyBoardEvent = keyBoardEvent;
            HiLog.debug(Text.TAG, "delete length is %{public}d", new Object[]{Integer.valueOf(this.deleteLength)});
            return deleteBackward(this.deleteLength);
        }
    }

    private static class TextSizeObserversHandler extends ComponentObserverHandler<TextSizeObserver> {
        private TextSizeObserversHandler() {
        }

        @Override // ohos.agp.components.ComponentObserverHandler
        public void onChange(int[] iArr) {
            super.onChange(iArr);
            if (iArr.length != 2) {
                HiLog.error(Text.TAG, "Illegal return params, should be size %{public}d.", new Object[]{2});
                return;
            }
            for (TextSizeObserver textSizeObserver : this.mObservers) {
                textSizeObserver.onTextSizeChanged(iArr[0], iArr[1]);
            }
        }
    }

    private static class TextColorObserversHandler extends ComponentObserverHandler<TextColorObserver> {
        private TextColorObserversHandler() {
        }

        @Override // ohos.agp.components.ComponentObserverHandler
        public void onChange(int[] iArr) {
            super.onChange(iArr);
            if (iArr.length != 2) {
                HiLog.error(Text.TAG, "Illegal return params, should be size %{public}d", new Object[]{2});
                return;
            }
            for (TextColorObserver textColorObserver : this.mObservers) {
                textColorObserver.onTextColorChanged(iArr[0], iArr[1]);
            }
        }
    }

    public Text(Context context) {
        this(context, null);
    }

    public Text(Context context, AttrSet attrSet) {
        this(context, attrSet, "TextViewDefaultStyle");
    }

    public Text(Context context, AttrSet attrSet, String str) {
        super(context, attrSet, str);
        this.mTextColorObserversHandler = new TextColorObserversHandler();
        this.mTextSizeObserversHandler = new TextSizeObserversHandler();
        this.mFont = Font.DEFAULT;
        this.mTextWatchersHandler = new TextWatchersHandler();
        this.mElementsRelative = false;
        this.mRichText = null;
        this.adjustInputPanel = false;
        this.mCursorElement = new ShapeElement();
        ((ShapeElement) this.mCursorElement).setRgbColor(RgbPalette.BLUE);
        setCursorElement(this.mCursorElement);
        this.mLastLayoutDirection = getLayoutDirectionResolved();
    }

    /* access modifiers changed from: protected */
    @Override // ohos.agp.components.Component
    public void createNativePtr() {
        if (this.mNativeViewPtr == 0) {
            this.mNativeViewPtr = nativeGetTextViewHandle();
        }
    }

    /* access modifiers changed from: protected */
    @Override // ohos.agp.components.Component
    public Style convertAttrToStyle(AttrSet attrSet) {
        if (this.mAttrsConstants == null) {
            this.mAttrsConstants = AttrHelper.getTextAttrsConstants();
        }
        return super.convertAttrToStyle(attrSet);
    }

    @Override // ohos.agp.components.Component
    public void applyStyle(Style style) {
        super.applyStyle(style);
        applyStyleImplementation(style);
    }

    private void applyStyleImplementation(Style style) {
        init();
        for (Map.Entry<String, BiConsumer<Text, Value>> entry : STYLE_METHOD_MAP.entrySet()) {
            if (style.hasProperty(entry.getKey())) {
                entry.getValue().accept(this, style.getPropertyValue(entry.getKey()));
            }
        }
        applyCompoundElementsFromAttr();
    }

    private synchronized void init() {
        this.mAttrElements = new Element[]{null, null, null, null, null, null, null, null, null};
        if (this.mElements == null) {
            this.mElements = new Element[]{null, null, null, null};
        }
    }

    private void applyCompoundElementsFromAttr() {
        Element[] elementArr = this.mAttrElements;
        if (elementArr[4] != null || elementArr[5] != null) {
            Element[] elementArr2 = this.mAttrElements;
            setAroundElementsRelative(elementArr2[4], elementArr2[1], elementArr2[5], elementArr2[3]);
        } else if (!(elementArr[0] == null && elementArr[1] == null && elementArr[2] == null && elementArr[3] == null)) {
            Element[] elementArr3 = this.mAttrElements;
            setAroundElements(elementArr3[0], elementArr3[1], elementArr3[2], elementArr3[3]);
        }
        Element[] elementArr4 = this.mAttrElements;
        if (elementArr4[6] != null) {
            setBubbleElement(elementArr4[6]);
        }
        Element[] elementArr5 = this.mAttrElements;
        if (elementArr5[7] != null) {
            setSelectionLeftBubbleElement(elementArr5[7]);
        }
        Element[] elementArr6 = this.mAttrElements;
        if (elementArr6[8] != null) {
            setSelectionRightBubbleElement(elementArr6[8]);
        }
        Arrays.fill(this.mAttrElements, (Object) null);
    }

    private static class TextWatchersHandler extends ComponentObserverHandler<TextObserver> {
        private TextWatchersHandler() {
        }

        /* access modifiers changed from: package-private */
        public void onTextChanged(String str, int i, int i2, int i3) {
            if (this.mObservers.size() == 0) {
                HiLog.error(Text.TAG, "mTextWatchers is null, or size is 0.", new Object[0]);
                return;
            }
            for (TextObserver textObserver : this.mObservers) {
                textObserver.onTextUpdated(str, i, i2, i3);
            }
        }

        @Override // ohos.agp.components.ComponentObserverHandler
        public void onChange(int[] iArr) {
            super.onChange(iArr);
        }
    }

    public void addTextObserver(TextObserver textObserver) {
        if (this.mTextWatchersHandler.getObserversCount() == 0) {
            nativeSetTextWatchersHandler(this.mNativeViewPtr, this.mTextWatchersHandler);
        }
        this.mTextWatchersHandler.addObserver(textObserver);
    }

    public void removeTextObserver(TextObserver textObserver) {
        this.mTextWatchersHandler.removeObserver(textObserver);
        if (this.mTextWatchersHandler.getObserversCount() == 0) {
            nativeSetTextWatchersHandler(this.mNativeViewPtr, null);
        }
    }

    public void setEditorActionListener(EditorActionListener editorActionListener) {
        this.mOnEditorActionListener = editorActionListener;
    }

    public void setFont(Font font) {
        validateParam(font, $$Lambda$Text$qksZCSi6G_hQduIVdUePXkLBWws.INSTANCE, "The font must have valid value!");
        long nativeTypefacePtr = font.convertToTypeface().getNativeTypefacePtr();
        if (nativeTypefacePtr == 0) {
            HiLog.warn(TAG, "invalid typeface", new Object[0]);
        } else if (!font.convertToTypeface().equals(this.mFont.convertToTypeface())) {
            nativeSetTypeface(this.mNativeViewPtr, nativeTypefacePtr);
            this.mFont = font;
        }
    }

    public Font getFont() {
        if (this.mFont == null) {
            this.mFont = Font.DEFAULT;
        }
        return this.mFont;
    }

    public void setTruncationMode(TruncationMode truncationMode) {
        nativeSetEllipsize(this.mNativeViewPtr, truncationMode.value());
    }

    public TruncationMode getTruncationMode() {
        return TruncationMode.getByInt(nativeGetEllipsize(this.mNativeViewPtr));
    }

    public void setInputMethodOption(int i) {
        this.mImeOption = i;
    }

    public int getInputMethodOption() {
        return this.mImeOption;
    }

    static /* synthetic */ boolean lambda$setAutoFontSizeRule$1(Integer num) {
        return num.intValue() >= 0;
    }

    public void setAutoFontSizeRule(int i, int i2, int i3) {
        validateParam(Integer.valueOf(i2), $$Lambda$Text$izsIgzPfirhvsxqHsa1hNFKCQc.INSTANCE, "Max font size must be positive");
        validateParam(Integer.valueOf(i), new Predicate(i2) {
            /* class ohos.agp.components.$$Lambda$Text$B0WComd7FrCiMp3fQqD3DbCKTc */
            private final /* synthetic */ int f$0;

            {
                this.f$0 = r1;
            }

            @Override // java.util.function.Predicate
            public final boolean test(Object obj) {
                return Text.lambda$setAutoFontSizeRule$2(this.f$0, (Integer) obj);
            }
        }, "Min font size must be positive and cannot exceed the size of the max");
        validateParam(Integer.valueOf(i3), $$Lambda$Text$RdiX4vpSWf4MkKXf26rXK8Bg.INSTANCE, "Step must be positive");
        nativeSetAutoFontSizeConfiguration(this.mNativeViewPtr, i, i2, i3);
    }

    static /* synthetic */ boolean lambda$setAutoFontSizeRule$2(int i, Integer num) {
        return num.intValue() >= 0 && num.intValue() <= i;
    }

    static /* synthetic */ boolean lambda$setAutoFontSizeRule$3(Integer num) {
        return num.intValue() > 0;
    }

    public void setAutoFontSizeRule(int[] iArr) {
        nativeSetAutoFontSizePreSet(this.mNativeViewPtr, iArr);
    }

    public boolean isAutoFontSize() {
        return nativeGetAutoFontSize(this.mNativeViewPtr);
    }

    static /* synthetic */ boolean lambda$setAutoScrollingDuration$4(Long l) {
        return l.longValue() >= 0;
    }

    public void setAutoScrollingDuration(long j) {
        validateParam(Long.valueOf(j), $$Lambda$Text$iSCfz6zP1e3_Uq6CLtABs4FP6pA.INSTANCE, "The duration must be non negative");
        nativeSetMarqueeDuration(this.mNativeViewPtr, j);
    }

    public long getAutoScrollingDuration() {
        return nativeGetMarqueeDuration(this.mNativeViewPtr);
    }

    static /* synthetic */ boolean lambda$setAutoScrollingCount$5(int i, Integer num) {
        return num.intValue() >= 1 || i == -1;
    }

    public void setAutoScrollingCount(int i) {
        validateParam(Integer.valueOf(i), new Predicate(i) {
            /* class ohos.agp.components.$$Lambda$Text$RpOg25Iy7AtC17W_TrC_9PeUKRo */
            private final /* synthetic */ int f$0;

            {
                this.f$0 = r1;
            }

            @Override // java.util.function.Predicate
            public final boolean test(Object obj) {
                return Text.lambda$setAutoScrollingCount$5(this.f$0, (Integer) obj);
            }
        }, "The Auto Scrolling Count must be more than or equal to 1, or AUTO_SCROLLING_FOREVER");
        nativeSetMarqueeCount(this.mNativeViewPtr, i);
    }

    public int getAutoScrollingCount() {
        return nativeGetMarqueeCount(this.mNativeViewPtr);
    }

    public void startAutoScrolling() {
        nativeStartMarquee(this.mNativeViewPtr);
    }

    public void stopAutoScrolling() {
        nativeStopMarquee(this.mNativeViewPtr);
    }

    public void setCursorElement(Element element) {
        this.mCursorElement = element;
        nativeSetTextCursorDrawable(this.mNativeViewPtr, element.getNativeElementPtr());
    }

    public Element getCursorElement() {
        return this.mCursorElement;
    }

    public void setAroundElements(Element element, Element element2, Element element3, Element element4) {
        long j;
        long j2;
        long j3;
        Element element5 = isRtl() ? element3 : element;
        Element element6 = isRtl() ? element : element3;
        Element[] elementArr = this.mElements;
        System.arraycopy(new Element[]{element5, element2, element6, element4}, 0, elementArr, 0, elementArr.length);
        long j4 = 0;
        if (element == null) {
            j = 0;
        } else {
            j = element.getNativeElementPtr();
        }
        if (element2 == null) {
            j2 = 0;
        } else {
            j2 = element2.getNativeElementPtr();
        }
        if (element3 == null) {
            j3 = 0;
        } else {
            j3 = element3.getNativeElementPtr();
        }
        if (element4 != null) {
            j4 = element4.getNativeElementPtr();
        }
        nativeSetCompoundDrawables(this.mNativeViewPtr, new long[]{j, j2, j3, j4});
        if (element == null && element3 == null) {
            unsubscribeRtlPropertiesChangedCallback();
        } else {
            subscribeRtlPropertiesChangedCallback();
        }
        this.mElementsRelative = false;
    }

    public Element[] getAroundElements() {
        Element element = this.mElements[isRtl() ? (char) 2 : 0];
        Element[] elementArr = this.mElements;
        return new Element[]{element, elementArr[1], elementArr[isRtl() ? (char) 0 : 2], this.mElements[3]};
    }

    public Element getLeftElement() {
        return this.mElements[isRtl() ? (char) 2 : 0];
    }

    public Element getRightElement() {
        return this.mElements[isRtl() ? (char) 0 : 2];
    }

    public Element getTopElement() {
        return this.mElements[1];
    }

    public Element getBottonElement() {
        return this.mElements[3];
    }

    public Element getStartElement() {
        return this.mElements[0];
    }

    public Element getEndElement() {
        return this.mElements[2];
    }

    public void setAroundElementsRelative(Element element, Element element2, Element element3, Element element4) {
        long j;
        long j2;
        long j3;
        Element[] elementArr = this.mElements;
        System.arraycopy(new Element[]{element, element2, element3, element4}, 0, elementArr, 0, elementArr.length);
        long j4 = 0;
        if (element == null) {
            j = 0;
        } else {
            j = element.getNativeElementPtr();
        }
        if (element2 == null) {
            j2 = 0;
        } else {
            j2 = element2.getNativeElementPtr();
        }
        if (element3 == null) {
            j3 = 0;
        } else {
            j3 = element3.getNativeElementPtr();
        }
        if (element4 != null) {
            j4 = element4.getNativeElementPtr();
        }
        nativeSetCompoundDrawablesRelative(this.mNativeViewPtr, new long[]{j, j2, j3, j4});
        unsubscribeRtlPropertiesChangedCallback();
        this.mElementsRelative = true;
    }

    public Element[] getAroundElementsRelative() {
        return (Element[]) this.mElements.clone();
    }

    static /* synthetic */ boolean lambda$setAroundElementsPadding$6(Integer num) {
        return num.intValue() >= 0;
    }

    public void setAroundElementsPadding(int i) {
        validateParam(Integer.valueOf(i), $$Lambda$Text$hkUWJebwncd011aZHCWO4W7JlMQ.INSTANCE, "Padding values should be non negative.");
        nativeSetCompoundDrawablesPadding(this.mNativeViewPtr, i);
    }

    public int getAroundElementsPadding() {
        return nativeGetCompoundDrawablesPadding(this.mNativeViewPtr);
    }

    public void setTextInputType(int i) {
        HiLog.debug(TAG, "TextView setTextInputType called, inputType=%{public}d", new Object[]{Integer.valueOf(i)});
        nativeSetTextInputType(this.mNativeViewPtr, i);
    }

    public int getTextInputType() {
        int nativeGetTextInputType = nativeGetTextInputType(this.mNativeViewPtr);
        HiLog.debug(TAG, "TextView getTextInputType called, inputType=%{public}d", new Object[]{Integer.valueOf(nativeGetTextInputType)});
        return nativeGetTextInputType;
    }

    public void setRichText(RichText richText) {
        this.mRichText = richText;
        if (this.mRichText != null) {
            nativeSetRichText(this.mNativeViewPtr, this.mRichText.getNativeRichText());
        } else {
            nativeSetRichText(this.mNativeViewPtr, 0);
        }
    }

    public RichText getRichText() {
        return this.mRichText;
    }

    public void setText(String str) {
        long j = this.mNativeViewPtr;
        if (str == null) {
            str = "";
        }
        nativeSetText(j, str);
    }

    public void setText(int i) {
        HiLog.debug(TAG, " setText using resId: %{public}d", new Object[]{Integer.valueOf(i)});
        if (this.mContext == null) {
            HiLog.error(TAG, " mContext is null!", new Object[0]);
            return;
        }
        ResourceManager resourceManager = this.mContext.getResourceManager();
        if (resourceManager == null) {
            HiLog.error(TAG, " Fail to get resource manager!", new Object[0]);
            return;
        }
        try {
            setText(resourceManager.getElement(i).getString());
        } catch (IOException | NotExistException | WrongTypeException unused) {
            HiLog.error(TAG, " Fail to get text source", new Object[0]);
        }
    }

    public String getText() {
        return nativeGetText(this.mNativeViewPtr);
    }

    public void setHint(String str) {
        nativeSetHint(this.mNativeViewPtr, str);
    }

    public String getHint() {
        return nativeGetHint(this.mNativeViewPtr);
    }

    public String getEditableString() {
        return nativeGetEditableText(this.mNativeViewPtr);
    }

    public void setTextSize(int i) {
        if (TextTool.validateTextSizeParam(i)) {
            nativeSetTextSize(this.mNativeViewPtr, i);
        }
    }

    public void setTextSize(int i, TextSizeType textSizeType) {
        if (TextSizeType.PX.equals(textSizeType)) {
            setTextSize(i);
        } else if (!TextSizeType.VP.equals(textSizeType) && !TextSizeType.FP.equals(textSizeType)) {
            HiLog.error(TAG, "do not support this type of text size", new Object[0]);
        } else if (this.mContext == null) {
            HiLog.error(TAG, "context is null", new Object[0]);
        } else {
            float density = AttrHelper.getDensity(this.mContext);
            if (density == 0.0f) {
                setTextSize(i);
            } else {
                setTextSize(convertToPx(i, density));
            }
        }
    }

    public int getTextSize() {
        return nativeGetTextSize(this.mNativeViewPtr);
    }

    public int getTextSize(TextSizeType textSizeType) {
        int nativeGetTextSize = nativeGetTextSize(this.mNativeViewPtr);
        if (TextSizeType.VP.equals(textSizeType) || TextSizeType.FP.equals(textSizeType)) {
            return convertFromPx(nativeGetTextSize);
        }
        return nativeGetTextSize;
    }

    private int convertFromPx(int i) {
        float density = AttrHelper.getDensity(this.mContext);
        return density == 0.0f ? i : (int) ((((float) i) / density) + 0.5f);
    }

    public enum TextSizeType {
        VP(0),
        FP(1),
        PX(2);
        
        private int textSizeTypeNum;

        private TextSizeType(int i) {
            this.textSizeTypeNum = i;
        }

        public int textSizeTypeValue() {
            return this.textSizeTypeNum;
        }

        public static TextSizeType getTextSizeType(int i) {
            if (i == 0) {
                return VP;
            }
            if (i == 1) {
                return FP;
            }
            if (i != 2) {
                return null;
            }
            return PX;
        }
    }

    public void setTextColor(Color color) {
        nativeSetTextColor(this.mNativeViewPtr, color.getValue());
    }

    public Color getTextColor() {
        return new Color(nativeGetTextColor(this.mNativeViewPtr));
    }

    public void setHintColor(Color color) {
        nativeSetHintTextColor(this.mNativeViewPtr, color.getValue());
    }

    public Color getHintColor() {
        return new Color(nativeGetHintTextColor(this.mNativeViewPtr));
    }

    public void setTextAlignment(int i) {
        if (!nativeSetTextAlignment(this.mNativeViewPtr, i)) {
            throw new IllegalArgumentException("The text alignment value should corresponds to alignment modes");
        }
    }

    public int getTextAlignment() {
        return nativeGetTextAlignment(this.mNativeViewPtr);
    }

    public void setTextCursorVisible(boolean z) {
        nativeSetCursorVisible(this.mNativeViewPtr, z);
    }

    public boolean isTextCursorVisible() {
        return nativeIsCursorVisible(this.mNativeViewPtr);
    }

    public int length() {
        return nativeLength(this.mNativeViewPtr);
    }

    public void append(String str) {
        nativeAppend(this.mNativeViewPtr, str);
    }

    public void insert(String str) {
        insert(str, -1);
    }

    static /* synthetic */ boolean lambda$insert$7(Integer num) {
        return num.intValue() >= 0 || num.intValue() == -1;
    }

    public void insert(String str, int i) {
        validateParam(Integer.valueOf(i), $$Lambda$Text$x5gaHD64TqSy8KRLPR76inTs_c.INSTANCE, "The position must be non negative or AUTO_CURSOR_POSITION");
        nativeInsertText(this.mNativeViewPtr, str, i);
    }

    public void delete(int i) {
        delete(i, true);
    }

    public void delete(int i, boolean z) {
        delete(i, z, -1);
    }

    static /* synthetic */ boolean lambda$delete$8(Integer num) {
        return num.intValue() >= 0;
    }

    public void delete(int i, boolean z, int i2) {
        validateParam(Integer.valueOf(i), $$Lambda$Text$1ceGRnOPvP7NxaoNWUl1wThfVkM.INSTANCE, "The length size must be non negative");
        validateParam(Integer.valueOf(i2), $$Lambda$Text$irhXp9co2vfZ1nDOtY8gqfI0uhI.INSTANCE, "The position must be non negative or AUTO_CURSOR_POSITION");
        nativeDeleteText(this.mNativeViewPtr, i, z, i2);
    }

    static /* synthetic */ boolean lambda$delete$9(Integer num) {
        return num.intValue() >= 0 || num.intValue() == -1;
    }

    public void setMultipleLine(boolean z) {
        nativeSetMultipleLine(this.mNativeViewPtr, z);
    }

    public boolean isMultipleLine() {
        return nativeIsMultipleLine(this.mNativeViewPtr);
    }

    static /* synthetic */ boolean lambda$setMaxTextLines$10(Integer num) {
        return num.intValue() > 0;
    }

    public void setMaxTextLines(int i) {
        validateParam(Integer.valueOf(i), $$Lambda$Text$kvZw9ZTHeYtLRMxkMHUiQtSqmxE.INSTANCE, "The value should be positive.");
        nativeSetMaxLines(this.mNativeViewPtr, i);
    }

    public int getMaxTextLines() {
        return nativeGetMaxLines(this.mNativeViewPtr);
    }

    public void setScrollable(boolean z) {
        nativeSetScrollable(this.mNativeViewPtr, z);
    }

    public boolean isScrollable() {
        return nativeIsScrollable(this.mNativeViewPtr);
    }

    public void setAutoFontSize(boolean z) {
        setAutoFontSize(z, false);
    }

    public void setAutoFontSize(boolean z, boolean z2) {
        nativeSetAutoFontSize(this.mNativeViewPtr, z, z2);
    }

    public void setLineSpacing(float f, float f2) {
        nativeSetLineSpacing(this.mNativeViewPtr, f, f2);
    }

    public float getNumOfFontHeight() {
        return nativeGetLineSpacingMultiplier(this.mNativeViewPtr);
    }

    public float getAdditionalLineSpacing() {
        return nativeGetLineSpacingExtra(this.mNativeViewPtr);
    }

    public boolean isAdjustInputPanel() {
        return this.adjustInputPanel;
    }

    public void setAdjustInputPanel(boolean z) {
        this.adjustInputPanel = z;
    }

    static /* synthetic */ boolean lambda$setMaxTextHeight$11(Integer num) {
        return num.intValue() >= 0;
    }

    public void setMaxTextHeight(int i) {
        validateParam(Integer.valueOf(i), $$Lambda$Text$wEYzpMmXDDnslEV27RNGueSooZQ.INSTANCE, "Max text height size must be non negative");
        nativeSetMaxHeight(this.mNativeViewPtr, i);
    }

    public int getMaxTextHeight() {
        return nativeGetMaxHeight(this.mNativeViewPtr);
    }

    static /* synthetic */ boolean lambda$setMaxTextWidth$12(Integer num) {
        return num.intValue() >= 0;
    }

    public void setMaxTextWidth(int i) {
        validateParam(Integer.valueOf(i), $$Lambda$Text$5IRWYv27WZloSc3BWq16svdsnk.INSTANCE, "Max text width size must be non negative");
        nativeSetMaxWidth(this.mNativeViewPtr, i);
    }

    public int getMaxTextWidth() {
        return nativeGetMaxWidth(this.mNativeViewPtr);
    }

    /* access modifiers changed from: protected */
    @Override // ohos.agp.components.Component
    public void onRtlChanged(Component.LayoutDirection layoutDirection) {
        super.onRtlChanged(layoutDirection);
        if (this.mLastLayoutDirection != layoutDirection) {
            this.mLastLayoutDirection = layoutDirection;
            Element[] elementArr = this.mElements;
            if (elementArr != null && !this.mElementsRelative) {
                Element element = elementArr[0];
                elementArr[0] = elementArr[2];
                elementArr[2] = element;
            }
        }
    }

    public InputDataChannel getInputDataChannel() {
        if (this.mInputDataChannel == null) {
            this.mInputDataChannel = new TextViewInputDataChannel(this);
        }
        HiLog.debug(TAG, "TextView get input data channel instance success.", new Object[0]);
        return this.mInputDataChannel;
    }

    public void addTextSizeObserver(TextSizeObserver textSizeObserver) {
        if (this.mTextSizeObserversHandler.getObserversCount() == 0) {
            addObserverHandler(this.mTextSizeObserversHandler);
        }
        this.mTextSizeObserversHandler.addObserver(textSizeObserver);
    }

    public void removeSizeTextObserver(TextSizeObserver textSizeObserver) {
        this.mTextSizeObserversHandler.removeObserver(textSizeObserver);
        if (this.mTextSizeObserversHandler.getObserversCount() == 0) {
            removeObserverHandler(this.mTextSizeObserversHandler);
        }
    }

    public void addTextColorObserver(TextColorObserver textColorObserver) {
        if (this.mTextColorObserversHandler.getObserversCount() == 0) {
            addObserverHandler(this.mTextColorObserversHandler);
        }
        this.mTextColorObserversHandler.addObserver(textColorObserver);
    }

    public void removeColorTextObserver(TextColorObserver textColorObserver) {
        this.mTextColorObserversHandler.removeObserver(textColorObserver);
        if (this.mTextColorObserversHandler.getObserversCount() == 0) {
            removeObserverHandler(this.mTextColorObserversHandler);
        }
    }

    public void setBubbleSize(int i, int i2) {
        nativeSetBubbleSize(this.mNativeViewPtr, i, i2);
    }

    public void setBubbleWidth(int i) {
        setBubbleSize(i, getBubbleHeight());
    }

    public void setBubbleHeight(int i) {
        setBubbleSize(getBubbleWidth(), i);
    }

    public int getBubbleWidth() {
        return nativeGetBubbleWidth(this.mNativeViewPtr);
    }

    public int getBubbleHeight() {
        return nativeGetBubbleHeight(this.mNativeViewPtr);
    }

    public void setLeftBubbleSize(int i, int i2) {
        nativeSetLeftBubbleSize(this.mNativeViewPtr, i, i2);
    }

    public void setLeftBubbleWidth(int i) {
        setLeftBubbleSize(i, getSelectionLeftBubbleHeight());
    }

    public void setLeftBubbleHeight(int i) {
        setLeftBubbleSize(getSelectionLeftBubbleWidth(), i);
    }

    public int getSelectionLeftBubbleWidth() {
        return nativeGetSelectionLeftBubbleWidth(this.mNativeViewPtr);
    }

    public int getSelectionLeftBubbleHeight() {
        return nativeGetSelectionLeftBubbleHeight(this.mNativeViewPtr);
    }

    public void setRightBubbleSize(int i, int i2) {
        nativeSetRightBubbleSize(this.mNativeViewPtr, i, i2);
    }

    public void setRightBubbleWidth(int i) {
        setRightBubbleSize(i, getSelectionRightBubbleHeight());
    }

    public void setRightBubbleHeight(int i) {
        setRightBubbleSize(getSelectionRightBubbleWidth(), i);
    }

    public int getSelectionRightBubbleWidth() {
        return nativeGetSelectionRightBubbleWidth(this.mNativeViewPtr);
    }

    public int getSelectionRightBubbleHeight() {
        return nativeGetSelectionRightBubbleHeight(this.mNativeViewPtr);
    }

    public void setSelectionColor(Color color) {
        nativeSetSelectionColor(this.mNativeViewPtr, color.getValue());
    }

    public Color getSelectionColor() {
        return new Color(nativeGetSelectionColor(this.mNativeViewPtr));
    }

    public void setBubbleElement(Element element) {
        this.mCursorBubbleElement = element;
        nativeSetCursorBubbleElement(this.mNativeViewPtr, element.getNativeElementPtr());
    }

    public Element getBubbleElement() {
        return this.mCursorBubbleElement;
    }

    public void setSelectionLeftBubbleElement(Element element) {
        this.mSelectionLeftBubbleElement = element;
        nativeSetSelectionLeftBubbleElement(this.mNativeViewPtr, element.getNativeElementPtr());
    }

    public Element getSelectionLeftBubbleElement() {
        return this.mSelectionLeftBubbleElement;
    }

    public void setSelectionRightBubbleElement(Element element) {
        this.mSelectionRightBubbleElement = element;
        nativeSetSelectionRightBubbleElement(this.mNativeViewPtr, element.getNativeElementPtr());
    }

    public Element getSelectionRightBubbleElement() {
        return this.mSelectionRightBubbleElement;
    }

    @Override // ohos.agp.components.Component
    public void setFadeEffectBoundaryWidth(int i) {
        nativeSetFadeEffectBoundaryWidth(this.mNativeViewPtr, i);
    }

    @Override // ohos.agp.components.Component
    public int getFadeEffectBoundaryWidth() {
        return nativeGetFadeEffectBoundaryWidth(this.mNativeViewPtr);
    }

    public void setPaddingForText(boolean z) {
        nativeSetPaddingForText(this.mNativeViewPtr, z);
    }

    public boolean getPaddingForText() {
        return nativeGetPaddingForText(this.mNativeViewPtr);
    }
}
