package ohos.agp.text;

import ohos.agp.utils.MemoryCleanerRegistry;
import ohos.agp.utils.NativeMemoryCleanerHelper;

public class RichTextBuilder {
    private final long mNativeBuilder;

    private native void nativeAddText(long j, String str);

    private native long nativeBuild(long j);

    private native long nativeCreateRichTextBuilder(long j, float[] fArr, int[] iArr, boolean[] zArr);

    private native void nativePopStyle(long j);

    private native void nativePushStyle(long j, long j2, float[] fArr, int[] iArr, boolean[] zArr);

    private static class RichTextBuilderCleaner extends NativeMemoryCleanerHelper {
        private native void nativeBuilderRelease(long j);

        public RichTextBuilderCleaner(long j) {
            super(j);
        }

        /* access modifiers changed from: protected */
        @Override // ohos.agp.utils.NativeMemoryCleanerHelper
        public void releaseNativeMemory(long j) {
            nativeBuilderRelease(j);
        }
    }

    public RichTextBuilder() {
        this(new TextForm());
    }

    public RichTextBuilder(TextForm textForm) {
        int defaultDirtyFlag = textForm.getDefaultDirtyFlag();
        int textSize = textForm.getTextSize();
        int textColor = textForm.getTextColor();
        Font textFont = textForm.getTextFont();
        float scaleX = textForm.getScaleX();
        boolean strikethrough = textForm.getStrikethrough();
        int textBackgroundColor = textForm.getTextBackgroundColor();
        float lineHeight = textForm.getLineHeight();
        boolean underline = textForm.getUnderline();
        float relativeTextSize = textForm.getRelativeTextSize();
        float[] fArr = {scaleX, lineHeight, relativeTextSize};
        this.mNativeBuilder = nativeCreateRichTextBuilder(textFont.convertToTypeface().getNativeTypefacePtr(), fArr, new int[]{defaultDirtyFlag, textSize, textColor, textBackgroundColor}, new boolean[]{strikethrough, underline, textForm.getSuperscript(), textForm.getSubscript()});
        MemoryCleanerRegistry.getInstance().register(this, new RichTextBuilderCleaner(this.mNativeBuilder));
    }

    public RichTextBuilder addText(String str) {
        nativeAddText(this.mNativeBuilder, str);
        return this;
    }

    public RichTextBuilder mergeForm(TextForm textForm) {
        int dirtyFlag = textForm.getDirtyFlag();
        int textSize = textForm.getTextSize();
        int textColor = textForm.getTextColor();
        Font textFont = textForm.getTextFont();
        float scaleX = textForm.getScaleX();
        boolean strikethrough = textForm.getStrikethrough();
        int textBackgroundColor = textForm.getTextBackgroundColor();
        float lineHeight = textForm.getLineHeight();
        boolean underline = textForm.getUnderline();
        float relativeTextSize = textForm.getRelativeTextSize();
        boolean[] zArr = {strikethrough, underline, textForm.getSuperscript(), textForm.getSubscript()};
        nativePushStyle(this.mNativeBuilder, textFont.convertToTypeface().getNativeTypefacePtr(), new float[]{scaleX, lineHeight, relativeTextSize}, new int[]{dirtyFlag, textSize, textColor, textBackgroundColor}, zArr);
        return this;
    }

    public RichTextBuilder revertForm() {
        nativePopStyle(this.mNativeBuilder);
        return this;
    }

    public RichText build() {
        return new RichText(nativeBuild(this.mNativeBuilder));
    }
}
