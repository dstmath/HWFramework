package ohos.agp.components;

import ohos.agp.components.element.Element;
import ohos.agp.styles.Style;
import ohos.agp.styles.attributes.TextFieldAttrsConstants;
import ohos.app.Context;

public class TextField extends Text {
    private Element mBasementElement;
    private CursorChangedListener mOnCursorChangeListener;

    /* access modifiers changed from: protected */
    public interface CursorChangedListener {
        void onCursorChange(TextField textField, int i, int i2);
    }

    private native long nativeGetEditTextHandle();

    private native void nativeSetBasement(long j, long j2);

    private native void nativeSetCursorChangeListener(long j, CursorChangedListener cursorChangedListener);

    public /* synthetic */ void lambda$setCursorChangedListener$0$TextField(TextField textField, int i, int i2) {
        getInputDataChannel().selectText(i, i2);
    }

    /* access modifiers changed from: protected */
    public void setCursorChangedListener() {
        setCursorChangedListener(new CursorChangedListener() {
            /* class ohos.agp.components.$$Lambda$TextField$u_PV6htTG5IAsCfdyiVajyxXE0U */

            @Override // ohos.agp.components.TextField.CursorChangedListener
            public final void onCursorChange(TextField textField, int i, int i2) {
                TextField.this.lambda$setCursorChangedListener$0$TextField(textField, i, i2);
            }
        });
    }

    /* access modifiers changed from: protected */
    public void setCursorChangedListener(CursorChangedListener cursorChangedListener) {
        this.mOnCursorChangeListener = cursorChangedListener;
        nativeSetCursorChangeListener(this.mNativeViewPtr, cursorChangedListener);
    }

    public TextField(Context context) {
        this(context, null);
    }

    public TextField(Context context, AttrSet attrSet) {
        this(context, attrSet, "EditTextDefaultStyle");
    }

    public TextField(Context context, AttrSet attrSet, String str) {
        super(context, attrSet, str);
    }

    /* access modifiers changed from: protected */
    @Override // ohos.agp.components.Text, ohos.agp.components.Component
    public Style convertAttrToStyle(AttrSet attrSet) {
        if (this.mAttrsConstants == null) {
            this.mAttrsConstants = AttrHelper.getTextFieldAttrsConstants();
        }
        return super.convertAttrToStyle(attrSet);
    }

    /* access modifiers changed from: protected */
    @Override // ohos.agp.components.Text, ohos.agp.components.Component
    public void createNativePtr() {
        if (this.mNativeViewPtr == 0) {
            this.mNativeViewPtr = nativeGetEditTextHandle();
        }
    }

    @Override // ohos.agp.components.Text, ohos.agp.components.Component
    public void applyStyle(Style style) {
        super.applyStyle(style);
        if (style.hasProperty(TextFieldAttrsConstants.BASEMENT)) {
            setBasement(style.getPropertyValue(TextFieldAttrsConstants.BASEMENT).asElement());
        }
    }

    public void setBasement(Element element) {
        this.mBasementElement = element;
        nativeSetBasement(this.mNativeViewPtr, element == null ? 0 : element.getNativeElementPtr());
    }

    public Element getBasement() {
        return this.mBasementElement;
    }
}
