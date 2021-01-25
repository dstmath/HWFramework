package ohos.agp.components;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Predicate;
import ohos.aafwk.utils.log.LogDomain;
import ohos.agp.colors.RgbPalette;
import ohos.agp.components.Component;
import ohos.agp.components.Text;
import ohos.agp.components.element.Element;
import ohos.agp.components.element.ShapeElement;
import ohos.agp.styles.Style;
import ohos.agp.styles.Value;
import ohos.agp.styles.attributes.TextViewAttrsConstants;
import ohos.agp.text.Font;
import ohos.agp.text.RichText;
import ohos.agp.utils.Color;
import ohos.app.Context;
import ohos.global.resource.NotExistException;
import ohos.global.resource.ResourceManager;
import ohos.global.resource.WrongTypeException;
import ohos.hiviewdfx.HiLog;
import ohos.hiviewdfx.HiLogLabel;
import ohos.miscservices.inputmethod.DefaultInputDataChannelImpl;
import ohos.miscservices.inputmethod.EditingCapability;
import ohos.miscservices.inputmethod.EditingText;
import ohos.miscservices.inputmethod.InputDataChannel;
import ohos.multimodalinput.event.KeyBoardEvent;
import ohos.multimodalinput.event.KeyEvent;

public class Text extends Component {
    public static final int AUTO_SCROLLING_FOREVER = -1;
    private static final int ELEMENT_BOTTOM = 3;
    private static final int ELEMENT_END = 2;
    private static final int ELEMENT_INDEX_BOTTOM = 3;
    private static final int ELEMENT_INDEX_END = 5;
    private static final int ELEMENT_INDEX_LEFT = 0;
    private static final int ELEMENT_INDEX_RIGHT = 2;
    private static final int ELEMENT_INDEX_START = 4;
    private static final int ELEMENT_INDEX_TOP = 1;
    private static final int ELEMENT_START = 0;
    private static final int ELEMENT_TOP = 1;
    private static final HiLogLabel TAG = new HiLogLabel(3, (int) LogDomain.END, "AGP_VIEW");
    private static Map<String, BiConsumer<Text, Value>> sStyleMethodMap = new HashMap<String, BiConsumer<Text, Value>>() {
        /* class ohos.agp.components.Text.AnonymousClass1 */

        {
            put(TextViewAttrsConstants.ELEMENT_LEFT, $$Lambda$Text$1$NzBGHfqAPsEUBe1veGtwWMF3kH0.INSTANCE);
            put(TextViewAttrsConstants.ELEMENT_TOP, $$Lambda$Text$1$rgyTu795Fg8GNjihUQtkoKCJVQ.INSTANCE);
            put(TextViewAttrsConstants.ELEMENT_RIGHT, $$Lambda$Text$1$TnLcJ0kie0dzPXUvG7RXRu9xv6U.INSTANCE);
            put(TextViewAttrsConstants.ELEMENT_BOTTOM, $$Lambda$Text$1$4xPEyZgSYM1EOeOcL73q0EDHIQ0.INSTANCE);
            put(TextViewAttrsConstants.ELEMENT_START, $$Lambda$Text$1$w6BdDIb7RKAppOiSKWCf7Rr9Ug.INSTANCE);
            put(TextViewAttrsConstants.ELEMENT_END, $$Lambda$Text$1$naYD7sFzHxEX2vMuzzddx8AmsJE.INSTANCE);
            put(TextViewAttrsConstants.TEXT_FONT, $$Lambda$Text$1$2ZQr5R6a5vEJlNdnvcokaJcEA0.INSTANCE);
            put(TextViewAttrsConstants.TEXT_WEIGHT, $$Lambda$Text$1$_vakBvz84yE_PNTVDGM_FucMufo.INSTANCE);
            put(TextViewAttrsConstants.ITALIC, $$Lambda$Text$1$Wp6pgYoN69GCb5uYppgXgxWAR4.INSTANCE);
            put(TextViewAttrsConstants.IME_OPTION, $$Lambda$Text$1$yW92LCguMkWWqcX6K4Gc71kYVg.INSTANCE);
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
    };
    private boolean adjustInputPanel;
    private Element[] mAttrElements;
    private Element mCursorElement;
    private Element[] mElements;
    private boolean mElementsRelative;
    private Font mFont;
    private int mImeOption;
    private TextViewInputDataChannel mInputDataChannel;
    private Component.LayoutDirection mLastLayoutDirection;
    private EditorActionListener mOnEditorActionListener;
    private RichText mRichText;
    private final TextWatchersHandler mTextWatchersHandler;

    public interface EditorActionListener {
        boolean onEditorAction(int i);
    }

    public interface TextObserver {
        void onTextChanged(String str, int i, int i2, int i3);
    }

    private native void nativeAppend(long j, String str);

    private native void nativeDeleteText(long j, int i, boolean z, int i2);

    private native boolean nativeGetAutoFontSize(long j);

    private native int nativeGetCompoundDrawablesPadding(long j);

    private native String nativeGetEditableText(long j);

    private native int nativeGetEllipsize(long j);

    private native String nativeGetHint(long j);

    private native int nativeGetHintTextColor(long j);

    private native float nativeGetLineSpacingExtra(long j);

    private native float nativeGetLineSpacingMultiplier(long j);

    private native int nativeGetMarqueeCount(long j);

    private native long nativeGetMarqueeDuration(long j);

    private native int nativeGetMaxHeight(long j);

    private native int nativeGetMaxLines(long j);

    private native int nativeGetMaxWidth(long j);

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

    private native void nativeSetCompoundDrawables(long j, long[] jArr);

    private native void nativeSetCompoundDrawablesPadding(long j, int i);

    private native void nativeSetCompoundDrawablesRelative(long j, long[] jArr);

    private native void nativeSetCursorVisible(long j, boolean z);

    private native void nativeSetEllipsize(long j, int i);

    private native void nativeSetHint(long j, String str);

    private native void nativeSetHintTextColor(long j, int i);

    private native void nativeSetLineSpacing(long j, float f, float f2);

    private native void nativeSetMarqueeCount(long j, int i);

    private native void nativeSetMarqueeDuration(long j, long j2);

    private native void nativeSetMaxHeight(long j, int i);

    private native void nativeSetMaxLines(long j, int i);

    private native void nativeSetMaxWidth(long j, int i);

    private native void nativeSetMultipleLine(long j, boolean z);

    private native void nativeSetRichText(long j, long j2);

    private native void nativeSetScrollable(long j, boolean z);

    private native void nativeSetText(long j, String str);

    private native void nativeSetTextAlignment(long j, int i);

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
        
        private int enumInt;

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
    public class TextViewInputDataChannel extends DefaultInputDataChannelImpl {
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

        public boolean selectText(int i, int i2) {
            Text.this.insert("", i);
            return false;
        }

        public boolean insertText(String str) {
            return Text.this.nativeInputDataChannelInsertText(this.mNativeInputDataChannelPtr, str);
        }

        public boolean deleteBackward(int i) {
            return Text.this.nativeInputDataChannelDeleteBackward(this.mNativeInputDataChannelPtr, i);
        }

        public boolean deleteForward(int i) {
            return Text.this.nativeInputDataChannelDeleteForward(this.mNativeInputDataChannelPtr, i);
        }

        public String getForward(int i) {
            return Text.this.nativeInputDataChannelGetTextBeforeCursor(this.mNativeInputDataChannelPtr, i);
        }

        public String getBackward(int i) {
            return Text.this.nativeInputDataChannelGetTextAfterCursor(this.mNativeInputDataChannelPtr, i);
        }

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

        public EditingText subscribeEditingText(EditingCapability editingCapability) {
            EditingText editingText = new EditingText();
            editingText.setTextContent(this.mParent.getEditableText());
            editingText.setPrompt(this.mParent.getHint());
            return editingText;
        }

        public boolean sendKeyFunction(int i) {
            if (this.mParent.mOnEditorActionListener != null) {
                return this.mParent.mOnEditorActionListener.onEditorAction(i);
            }
            return Text.super.sendKeyFunction(i);
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

    public Text(Context context) {
        this(context, null);
    }

    public Text(Context context, AttrSet attrSet) {
        this(context, attrSet, "TextViewDefaultStyle");
    }

    public Text(Context context, AttrSet attrSet, String str) {
        super(context, attrSet, str);
        this.mFont = Font.DEFAULT;
        this.mTextWatchersHandler = new TextWatchersHandler();
        this.mElementsRelative = false;
        this.mRichText = null;
        this.adjustInputPanel = false;
        this.mCursorElement = new ShapeElement();
        Element element = this.mCursorElement;
        if (element instanceof ShapeElement) {
            ((ShapeElement) element).setRgbColor(RgbPalette.BLUE);
        }
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
            this.mAttrsConstants = new TextViewAttrsConstants();
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
        for (Map.Entry<String, BiConsumer<Text, Value>> entry : sStyleMethodMap.entrySet()) {
            if (style.hasProperty(entry.getKey())) {
                Optional.ofNullable(sStyleMethodMap.get(entry.getKey())).ifPresent(new Consumer(style, entry) {
                    /* class ohos.agp.components.$$Lambda$Text$Ec5D2YOozCn2U6a0Uy_wkVm0l54 */
                    private final /* synthetic */ Style f$1;
                    private final /* synthetic */ Map.Entry f$2;

                    {
                        this.f$1 = r2;
                        this.f$2 = r3;
                    }

                    @Override // java.util.function.Consumer
                    public final void accept(Object obj) {
                        Text.this.lambda$applyStyleImplementation$0$Text(this.f$1, this.f$2, (BiConsumer) obj);
                    }
                });
            }
        }
        applyCompoundElementsFromAttr();
    }

    public /* synthetic */ void lambda$applyStyleImplementation$0$Text(Style style, Map.Entry entry, BiConsumer biConsumer) {
        biConsumer.accept(this, style.getPropertyValue((String) entry.getKey()));
    }

    private synchronized void init() {
        this.mAttrElements = new Element[]{null, null, null, null, null, null};
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
        Arrays.fill(this.mAttrElements, (Object) null);
    }

    private static class TextWatchersHandler {
        private List<TextObserver> mTextWatchers;

        private TextWatchersHandler() {
            this.mTextWatchers = new ArrayList();
        }

        /* access modifiers changed from: package-private */
        public void addWatcher(TextObserver textObserver) {
            this.mTextWatchers.add(textObserver);
        }

        /* access modifiers changed from: package-private */
        public void removeWatcher(TextObserver textObserver) {
            this.mTextWatchers.remove(textObserver);
        }

        /* access modifiers changed from: package-private */
        public int getWatchersCount() {
            return this.mTextWatchers.size();
        }

        /* access modifiers changed from: package-private */
        public void onTextChanged(String str, int i, int i2, int i3) {
            List<TextObserver> list = this.mTextWatchers;
            if (list == null || list.size() == 0) {
                HiLog.error(Text.TAG, "mTextWatchers is null, or size is 0.", new Object[0]);
                return;
            }
            for (TextObserver textObserver : this.mTextWatchers) {
                textObserver.onTextChanged(str, i, i2, i3);
            }
        }
    }

    public void addTextObserver(TextObserver textObserver) {
        if (this.mTextWatchersHandler.getWatchersCount() == 0) {
            nativeSetTextWatchersHandler(this.mNativeViewPtr, this.mTextWatchersHandler);
        }
        this.mTextWatchersHandler.addWatcher(textObserver);
    }

    public void removeTextObserver(TextObserver textObserver) {
        this.mTextWatchersHandler.removeWatcher(textObserver);
        if (this.mTextWatchersHandler.getWatchersCount() == 0) {
            nativeSetTextWatchersHandler(this.mNativeViewPtr, null);
        }
    }

    public void setEditorActionListener(EditorActionListener editorActionListener) {
        this.mOnEditorActionListener = editorActionListener;
    }

    public void setFont(Font font) {
        if (font == null) {
            HiLog.warn(TAG, "invalid typeface", new Object[0]);
            return;
        }
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

    public void setAutoFontSizeRule(int i, int i2, int i3) {
        nativeSetAutoFontSizeConfiguration(this.mNativeViewPtr, i, i2, i3);
    }

    public void setAutoFontSizeRule(int[] iArr) {
        nativeSetAutoFontSizePreSet(this.mNativeViewPtr, iArr);
    }

    public boolean isAutoFontSize() {
        return nativeGetAutoFontSize(this.mNativeViewPtr);
    }

    public void setAutoScrollingDuration(long j) {
        if (j >= 0) {
            nativeSetMarqueeDuration(this.mNativeViewPtr, j);
        }
    }

    public long getAutoScrollingDuration() {
        return nativeGetMarqueeDuration(this.mNativeViewPtr);
    }

    public void setAutoScrollingCount(int i) {
        if (i >= 1 || i == -1) {
            nativeSetMarqueeCount(this.mNativeViewPtr, i);
        }
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
        Element element5 = isLayoutRtl() ? element3 : element;
        Element element6 = isLayoutRtl() ? element : element3;
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
        this.mElementsRelative = false;
    }

    public Element[] getAroundElements() {
        Element element = this.mElements[isLayoutRtl() ? (char) 2 : 0];
        Element[] elementArr = this.mElements;
        return new Element[]{element, elementArr[1], elementArr[isLayoutRtl() ? (char) 0 : 2], this.mElements[3]};
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
        this.mElementsRelative = true;
    }

    public Element[] getAroundElementsRelative() {
        return (Element[]) this.mElements.clone();
    }

    public void setAroundElementsPadding(int i) {
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

    public String getEditableText() {
        return nativeGetEditableText(this.mNativeViewPtr);
    }

    public void setTextSize(int i) {
        if (i >= 0) {
            nativeSetTextSize(this.mNativeViewPtr, i);
        }
    }

    public int getTextSize() {
        return nativeGetTextSize(this.mNativeViewPtr);
    }

    public void setTextColor(Color color) {
        nativeSetTextColor(this.mNativeViewPtr, color.getValue());
    }

    public Color getTextColor() {
        return new Color(nativeGetTextColor(this.mNativeViewPtr));
    }

    public void setHintTextColor(Color color) {
        nativeSetHintTextColor(this.mNativeViewPtr, color.getValue());
    }

    public Color getHintTextColor() {
        return new Color(nativeGetHintTextColor(this.mNativeViewPtr));
    }

    public void setTextAlignment(int i) {
        nativeSetTextAlignment(this.mNativeViewPtr, i);
    }

    public int getTextAlignment() {
        return nativeGetTextAlignment(this.mNativeViewPtr);
    }

    public void setCursorVisible(boolean z) {
        nativeSetCursorVisible(this.mNativeViewPtr, z);
    }

    public boolean isCursorVisible() {
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

    public void insert(String str, int i) {
        nativeInsertText(this.mNativeViewPtr, str, i);
    }

    public void delete(int i) {
        delete(i, true);
    }

    public void delete(int i, boolean z) {
        delete(i, z, -1);
    }

    public void delete(int i, boolean z, int i2) {
        nativeDeleteText(this.mNativeViewPtr, i, z, i2);
    }

    public void setMultipleLine(boolean z) {
        nativeSetMultipleLine(this.mNativeViewPtr, z);
    }

    public boolean isMultipleLine() {
        return nativeIsMultipleLine(this.mNativeViewPtr);
    }

    public void setMaxLines(int i) {
        nativeSetMaxLines(this.mNativeViewPtr, i);
    }

    public int getMaxLines() {
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

    public float getLineSpacingMultiplier() {
        return nativeGetLineSpacingMultiplier(this.mNativeViewPtr);
    }

    public float getLineSpacingExtra() {
        return nativeGetLineSpacingExtra(this.mNativeViewPtr);
    }

    public boolean isAdjustInputPanel() {
        return this.adjustInputPanel;
    }

    public void setAdjustInputPanel(boolean z) {
        this.adjustInputPanel = z;
    }

    public void setMaxHeight(int i) {
        nativeSetMaxHeight(this.mNativeViewPtr, i);
    }

    public int getMaxHeight() {
        return nativeGetMaxHeight(this.mNativeViewPtr);
    }

    public void setMaxWidth(int i) {
        nativeSetMaxWidth(this.mNativeViewPtr, i);
    }

    public int getMaxWidth() {
        return nativeGetMaxWidth(this.mNativeViewPtr);
    }

    /* access modifiers changed from: protected */
    @Override // ohos.agp.components.Component
    public void onRtlPropertiesChanged(Component.LayoutDirection layoutDirection) {
        super.onRtlPropertiesChanged(layoutDirection);
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
}
