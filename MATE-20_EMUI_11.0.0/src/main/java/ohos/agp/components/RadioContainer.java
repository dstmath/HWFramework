package ohos.agp.components;

import java.util.function.Consumer;
import ohos.agp.styles.attributes.RadioContainerAttrsConstants;
import ohos.app.Context;

public class RadioContainer extends DirectionalLayout {
    protected CheckedStateChangedListener mCheckedListener;

    public interface CheckedStateChangedListener {
        void onCheckedChanged(RadioContainer radioContainer, int i);
    }

    private native long nativeGetRadioGroupHandle();

    private native void nativeRadioGroupClear(long j);

    private native int nativeRadioGroupGetCheckedId(long j);

    private native void nativeRadioGroupSetChecked(long j, int i);

    private native void nativeSetRadioGroupCallback(long j, CheckedStateChangedListener checkedStateChangedListener);

    public RadioContainer(Context context) {
        this(context, null);
    }

    public RadioContainer(Context context, AttrSet attrSet) {
        this(context, attrSet, "RadioGroupDefaultStyle");
    }

    public RadioContainer(Context context, AttrSet attrSet, String str) {
        super(context, attrSet, str);
        this.mCheckedListener = null;
        AttrSet mergeStyle = AttrHelper.mergeStyle(context, attrSet, 0);
        for (int i = 0; i < mergeStyle.getLength(); i++) {
            mergeStyle.getAttr(i).ifPresent(new Consumer(context) {
                /* class ohos.agp.components.$$Lambda$RadioContainer$pOKvrTRZcgOLZyh2d0aUvillvMk */
                private final /* synthetic */ Context f$1;

                {
                    this.f$1 = r2;
                }

                @Override // java.util.function.Consumer
                public final void accept(Object obj) {
                    RadioContainer.this.lambda$new$0$RadioContainer(this.f$1, (Attr) obj);
                }
            });
        }
    }

    public /* synthetic */ void lambda$new$0$RadioContainer(Context context, Attr attr) {
        AttrWrapper attrWrapper = new AttrWrapper(context, attr);
        String name = attr.getName();
        if (((name.hashCode() == 1484831434 && name.equals(RadioContainerAttrsConstants.CHECKED_BUTTON)) ? (char) 0 : 65535) == 0) {
            check(attrWrapper.getIntegerValue());
        }
    }

    /* access modifiers changed from: protected */
    @Override // ohos.agp.components.LinearLayout, ohos.agp.components.ComponentContainer, ohos.agp.components.Component
    public void createNativePtr() {
        if (this.mNativeViewPtr == 0) {
            this.mNativeViewPtr = nativeGetRadioGroupHandle();
        }
    }

    public void check(int i) {
        nativeRadioGroupSetChecked(this.mNativeViewPtr, i);
    }

    public int getCheckedRadioButtonId() {
        return nativeRadioGroupGetCheckedId(this.mNativeViewPtr);
    }

    public void clearCheck() {
        nativeRadioGroupClear(this.mNativeViewPtr);
    }

    public void setCheckedStateChangedListener(CheckedStateChangedListener checkedStateChangedListener) {
        this.mCheckedListener = checkedStateChangedListener;
        nativeSetRadioGroupCallback(this.mNativeViewPtr, checkedStateChangedListener);
    }
}
