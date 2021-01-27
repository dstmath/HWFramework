package ohos.agp.styles;

import java.util.Optional;
import java.util.function.Consumer;
import ohos.agp.colors.RgbColor;
import ohos.agp.components.Attr;
import ohos.agp.components.AttrSet;
import ohos.agp.utils.MemoryCleaner;
import ohos.agp.utils.MemoryCleanerRegistry;
import ohos.hiviewdfx.HiLog;
import ohos.hiviewdfx.HiLogLabel;

public class Style {
    public static final String STYLE_FROM_ATTRSET = "from AttrSet ";
    private static final HiLogLabel TAG = new HiLogLabel(3, 0, "AGP_Style");
    protected final long mNativeStylePtr;

    private native Value nativeGetPropertyValue(long j, String str);

    private native long nativeGetStyleHandle(String str);

    private native String nativeGetStyleName(long j);

    private native boolean nativeHasProperty(long j, String str);

    private native boolean nativeRemoveProperty(long j, String str);

    private native void nativeSetBoolProperty(long j, String str, boolean z);

    private native void nativeSetColorProperty(long j, String str, int i);

    private native void nativeSetDoubleProperty(long j, String str, double d);

    private native void nativeSetElementProperty(long j, String str, long j2);

    private native void nativeSetIntegerProperty(long j, String str, int i);

    private native void nativeSetLongProperty(long j, String str, long j2);

    private native void nativeSetStringProperty(long j, String str, String str2);

    private native void nativeStyleMerge(long j, long j2);

    public void release() {
    }

    protected static class StyleCleaner implements MemoryCleaner {
        private long mNativePtr;

        private native void nativeStyleRelease(long j);

        StyleCleaner(long j) {
            this.mNativePtr = j;
        }

        @Override // ohos.agp.utils.MemoryCleaner
        public void run() {
            long j = this.mNativePtr;
            if (j != 0) {
                nativeStyleRelease(j);
                this.mNativePtr = 0;
            }
        }
    }

    public Style(String str) {
        this.mNativeStylePtr = nativeGetStyleHandle(str);
        MemoryCleanerRegistry.getInstance().register(this, new StyleCleaner(this.mNativeStylePtr));
    }

    public long getNativeStylePtr() {
        return this.mNativeStylePtr;
    }

    public String getName() {
        return nativeGetStyleName(this.mNativeStylePtr);
    }

    public static Style fromAttrSet(AttrSet attrSet) {
        Optional<String> style = attrSet.getStyle();
        int length = attrSet.getLength();
        Style style2 = new Style(STYLE_FROM_ATTRSET + style.orElse(""));
        for (int i = 0; i < length; i++) {
            attrSet.getAttr(i).ifPresent(new Consumer() {
                /* class ohos.agp.styles.$$Lambda$Style$mv_vs6497OKqGhJ6FD5eKJXw9g */

                @Override // java.util.function.Consumer
                public final void accept(Object obj) {
                    Style.lambda$fromAttrSet$0(Style.this, (Attr) obj);
                }
            });
        }
        return style2;
    }

    static /* synthetic */ void lambda$fromAttrSet$0(Style style, Attr attr) {
        Value value;
        switch (attr.getType()) {
            case INT:
                value = new Value(attr.getIntegerValue());
                break;
            case LONG:
                value = new Value(attr.getLongValue());
                break;
            case FLOAT:
                value = new Value((double) attr.getFloatValue());
                break;
            case BOOLEAN:
                value = new Value(attr.getBoolValue());
                break;
            case STRING:
                value = new Value(attr.getStringValue());
                break;
            case ELEMENT:
                value = new Value(attr.getElement());
                break;
            case DIMENSION:
                value = new Value(attr.getDimensionValue());
                break;
            case COLOR:
                value = new Value(RgbColor.fromArgbInt(attr.getColorValue().getValue()));
                break;
            default:
                return;
        }
        style.setPropertyValue(attr.getName(), value);
    }

    public boolean hasProperty(String str) {
        return nativeHasProperty(this.mNativeStylePtr, str);
    }

    public Value getPropertyValue(String str) {
        return nativeGetPropertyValue(this.mNativeStylePtr, str);
    }

    public void setPropertyValue(String str, Value value) {
        if (!value.isEmpty()) {
            switch (value.getType()) {
                case 0:
                    nativeSetBoolProperty(this.mNativeStylePtr, str, value.asBool());
                    return;
                case 1:
                    nativeSetDoubleProperty(this.mNativeStylePtr, str, value.asDouble());
                    return;
                case 2:
                    nativeSetElementProperty(this.mNativeStylePtr, str, value.asElement().getNativeElementPtr());
                    return;
                case 3:
                    nativeSetIntegerProperty(this.mNativeStylePtr, str, value.asInteger());
                    return;
                case 4:
                    nativeSetLongProperty(this.mNativeStylePtr, str, value.asLong());
                    return;
                case 5:
                    nativeSetColorProperty(this.mNativeStylePtr, str, value.asColor().asRgbaInt());
                    return;
                case 6:
                    nativeSetStringProperty(this.mNativeStylePtr, str, value.asString());
                    return;
                default:
                    HiLog.debug(TAG, "Unknown value type.", new Object[0]);
                    return;
            }
        }
    }

    public boolean removeProperty(String str) {
        return nativeRemoveProperty(this.mNativeStylePtr, str);
    }

    public Style merge(Style style) {
        nativeStyleMerge(this.mNativeStylePtr, style.mNativeStylePtr);
        return this;
    }
}
