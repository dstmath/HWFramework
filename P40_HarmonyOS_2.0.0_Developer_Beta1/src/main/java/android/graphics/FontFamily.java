package android.graphics;

import android.annotation.UnsupportedAppUsage;
import android.content.res.AssetManager;
import android.graphics.fonts.FontVariationAxis;
import android.telephony.SmsManager;
import android.text.TextUtils;
import android.util.Log;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import libcore.util.NativeAllocationRegistry;

@Deprecated
public class FontFamily {
    private static String TAG = "FontFamily";
    private static final NativeAllocationRegistry sBuilderRegistry = NativeAllocationRegistry.createMalloced(FontFamily.class.getClassLoader(), nGetBuilderReleaseFunc());
    private static final NativeAllocationRegistry sFamilyRegistry = NativeAllocationRegistry.createMalloced(FontFamily.class.getClassLoader(), nGetFamilyReleaseFunc());
    private long mBuilderPtr;
    private Runnable mNativeBuilderCleaner;
    @UnsupportedAppUsage(trackingBug = 123768928)
    public long mNativePtr;

    private static native void nAddAxisValue(long j, int i, float f);

    private static native boolean nAddFont(long j, ByteBuffer byteBuffer, int i, int i2, int i3);

    private static native boolean nAddFontFromAssetManager(long j, AssetManager assetManager, String str, int i, boolean z, int i2, int i3, int i4);

    private static native boolean nAddFontWeightStyle(long j, ByteBuffer byteBuffer, int i, int i2, int i3);

    private static native long nCreateFamily(long j);

    private static native long nGetBuilderReleaseFunc();

    private static native long nGetFamilyReleaseFunc();

    private static native long nInitBuilder(String str, int i);

    @UnsupportedAppUsage(trackingBug = 123768928)
    public FontFamily() {
        this.mBuilderPtr = nInitBuilder(null, 0);
        this.mNativeBuilderCleaner = sBuilderRegistry.registerNativeAllocation(this, this.mBuilderPtr);
    }

    @UnsupportedAppUsage(trackingBug = 123768928)
    public FontFamily(String[] langs, int variant) {
        String langsString;
        if (langs == null || langs.length == 0) {
            langsString = null;
        } else if (langs.length == 1) {
            langsString = langs[0];
        } else {
            langsString = TextUtils.join(SmsManager.REGEX_PREFIX_DELIMITER, langs);
        }
        this.mBuilderPtr = nInitBuilder(langsString, variant);
        this.mNativeBuilderCleaner = sBuilderRegistry.registerNativeAllocation(this, this.mBuilderPtr);
    }

    @UnsupportedAppUsage(trackingBug = 123768928)
    public boolean freeze() {
        long j = this.mBuilderPtr;
        if (j != 0) {
            this.mNativePtr = nCreateFamily(j);
            this.mNativeBuilderCleaner.run();
            this.mBuilderPtr = 0;
            long j2 = this.mNativePtr;
            if (j2 != 0) {
                sFamilyRegistry.registerNativeAllocation(this, j2);
            }
            return this.mNativePtr != 0;
        }
        throw new IllegalStateException("This FontFamily is already frozen");
    }

    @UnsupportedAppUsage(trackingBug = 123768928)
    public void abortCreation() {
        if (this.mBuilderPtr != 0) {
            this.mNativeBuilderCleaner.run();
            this.mBuilderPtr = 0;
            return;
        }
        throw new IllegalStateException("This FontFamily is already frozen or abandoned");
    }

    /* JADX WARNING: Code restructure failed: missing block: B:19:0x0050, code lost:
        r0 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:22:?, code lost:
        r0.close();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:23:0x0056, code lost:
        r0 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:24:0x0057, code lost:
        r0.addSuppressed(r0);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:25:0x005b, code lost:
        throw r0;
     */
    @UnsupportedAppUsage(trackingBug = 123768928)
    public boolean addFont(String path, int ttcIndex, FontVariationAxis[] axes, int weight, int italic) {
        if (this.mBuilderPtr != 0) {
            try {
                FileInputStream file = new FileInputStream(path);
                FileChannel fileChannel = file.getChannel();
                ByteBuffer fontBuffer = fileChannel.map(FileChannel.MapMode.READ_ONLY, 0, fileChannel.size());
                if (axes != null) {
                    for (FontVariationAxis axis : axes) {
                        nAddAxisValue(this.mBuilderPtr, axis.getOpenTypeTagValue(), axis.getStyleValue());
                    }
                }
                boolean nAddFont = nAddFont(this.mBuilderPtr, fontBuffer, ttcIndex, weight, italic);
                file.close();
                return nAddFont;
            } catch (IOException e) {
                Log.e(TAG, "Error mapping font file " + path);
                return false;
            }
        } else {
            throw new IllegalStateException("Unable to call addFont after freezing.");
        }
    }

    @UnsupportedAppUsage(trackingBug = 123768928)
    public boolean addFontFromBuffer(ByteBuffer font, int ttcIndex, FontVariationAxis[] axes, int weight, int italic) {
        if (this.mBuilderPtr != 0) {
            if (axes != null) {
                for (FontVariationAxis axis : axes) {
                    nAddAxisValue(this.mBuilderPtr, axis.getOpenTypeTagValue(), axis.getStyleValue());
                }
            }
            return nAddFontWeightStyle(this.mBuilderPtr, font, ttcIndex, weight, italic);
        }
        throw new IllegalStateException("Unable to call addFontWeightStyle after freezing.");
    }

    @UnsupportedAppUsage(trackingBug = 123768928)
    public boolean addFontFromAssetManager(AssetManager mgr, String path, int cookie, boolean isAsset, int ttcIndex, int weight, int isItalic, FontVariationAxis[] axes) {
        if (this.mBuilderPtr != 0) {
            if (axes != null) {
                for (FontVariationAxis axis : axes) {
                    nAddAxisValue(this.mBuilderPtr, axis.getOpenTypeTagValue(), axis.getStyleValue());
                }
            }
            return nAddFontFromAssetManager(this.mBuilderPtr, mgr, path, cookie, isAsset, ttcIndex, weight, isItalic);
        }
        throw new IllegalStateException("Unable to call addFontFromAsset after freezing.");
    }

    private static boolean nAddFont(long builderPtr, ByteBuffer font, int ttcIndex) {
        return nAddFont(builderPtr, font, ttcIndex, -1, -1);
    }
}
