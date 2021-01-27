package android.graphics.fonts;

import android.graphics.fonts.Font;
import android.util.Log;
import com.android.internal.util.Preconditions;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import libcore.util.NativeAllocationRegistry;

public final class FontFamily {
    public static final int HW_FONT_OVERLARY = 2;
    public static final int HW_HOOK_OVERLARY = -1;
    private static final String TAG = "FontFamily";
    private final ArrayList<Font> mFonts;
    public boolean mIsHook;
    private final long mNativePtr;

    private static native void nHwAddFont(long j, long j2);

    private static native void nResetCoverage(long j);

    private static native void nResetFont(long j);

    private static native void nSetHwFontFamilyType(long j, int i);

    public static final class Builder {
        private static final NativeAllocationRegistry sFamilyRegistory = NativeAllocationRegistry.createMalloced(FontFamily.class.getClassLoader(), nGetReleaseNativeFamily());
        private final ArrayList<Font> mFonts = new ArrayList<>();
        private final HashSet<Integer> mStyleHashSet = new HashSet<>();

        private static native void nAddFont(long j, long j2);

        private static native long nBuild(long j, String str, int i, boolean z);

        private static native long nGetReleaseNativeFamily();

        private static native long nInitBuilder();

        public Builder(Font font) {
            Preconditions.checkNotNull(font, "font can not be null");
            this.mStyleHashSet.add(Integer.valueOf(makeStyleIdentifier(font)));
            this.mFonts.add(font);
        }

        public Builder addFont(Font font) {
            Preconditions.checkNotNull(font, "font can not be null");
            if (this.mStyleHashSet.add(Integer.valueOf(makeStyleIdentifier(font)))) {
                this.mFonts.add(font);
                return this;
            }
            throw new IllegalArgumentException(font + " has already been added");
        }

        public FontFamily build() {
            return build("", 0, true);
        }

        public FontFamily build(String langTags, int variant, boolean isCustomFallback) {
            long builderPtr = nInitBuilder();
            for (int i = 0; i < this.mFonts.size(); i++) {
                nAddFont(builderPtr, this.mFonts.get(i).getNativePtr());
            }
            long ptr = nBuild(builderPtr, langTags, variant, isCustomFallback);
            FontFamily family = new FontFamily(this.mFonts, ptr);
            sFamilyRegistory.registerNativeAllocation(family, ptr);
            return family;
        }

        private static int makeStyleIdentifier(Font font) {
            return font.getStyle().getWeight() | (font.getStyle().getSlant() << 16);
        }
    }

    private FontFamily(ArrayList<Font> fonts, long ptr) {
        this.mIsHook = false;
        this.mFonts = fonts;
        this.mNativePtr = ptr;
    }

    public Font getFont(int index) {
        return this.mFonts.get(index);
    }

    public int getSize() {
        return this.mFonts.size();
    }

    public long getNativePtr() {
        return this.mNativePtr;
    }

    public void buildHwFonts(File fontFile, int weight) {
        ArrayList<Font> arrayList;
        Font font = null;
        try {
            Font.Builder builder = new Font.Builder(fontFile);
            if (weight != 0) {
                builder.setWeight(weight);
            }
            font = builder.build();
        } catch (IOException e) {
            Log.i(TAG, "build huawei fonts error : IOException");
        } catch (Exception e2) {
            Log.i(TAG, "build huawei fonts error : Exception");
        }
        if (font != null && (arrayList = this.mFonts) != null) {
            arrayList.add(font);
        }
    }

    public void buildHwFontFamily() {
        ArrayList<Font> arrayList = this.mFonts;
        if (arrayList != null) {
            int size = arrayList.size();
            for (int i = 0; i < size; i++) {
                nHwAddFont(this.mNativePtr, this.mFonts.get(i).getNativePtr());
            }
        }
    }

    public void setHookFlag(boolean ishook) {
        this.mIsHook = ishook;
    }

    public void resetCoverage() {
        nResetCoverage(this.mNativePtr);
    }

    public void resetFont() {
        ArrayList<Font> arrayList = this.mFonts;
        if (arrayList != null) {
            arrayList.clear();
        }
        nResetFont(this.mNativePtr);
    }

    public void setHwFontFamilyType(int type) {
        nSetHwFontFamilyType(this.mNativePtr, type);
    }
}
