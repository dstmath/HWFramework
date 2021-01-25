package android.graphics.fonts;

import android.content.res.AssetManager;
import android.content.res.Resources;
import android.os.LocaleList;
import android.os.ParcelFileDescriptor;
import android.util.TypedValue;
import com.android.internal.util.Preconditions;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.Arrays;
import java.util.Objects;
import libcore.util.NativeAllocationRegistry;

public final class Font {
    private static final int NOT_SPECIFIED = -1;
    private static final int STYLE_ITALIC = 1;
    private static final int STYLE_NORMAL = 0;
    private static final String TAG = "Font";
    private final FontVariationAxis[] mAxes;
    private final ByteBuffer mBuffer;
    private final File mFile;
    private final FontStyle mFontStyle;
    private final String mLocaleList;
    private final long mNativePtr;
    private final int mTtcIndex;

    public static final class Builder {
        private static final NativeAllocationRegistry sAssetByteBufferRegistry = NativeAllocationRegistry.createMalloced(ByteBuffer.class.getClassLoader(), nGetReleaseNativeAssetFunc());
        private static final NativeAllocationRegistry sFontRegistry = NativeAllocationRegistry.createMalloced(Font.class.getClassLoader(), nGetReleaseNativeFont());
        private FontVariationAxis[] mAxes;
        private ByteBuffer mBuffer;
        private IOException mException;
        private File mFile;
        private int mItalic;
        private String mLocaleList;
        private int mTtcIndex;
        private int mWeight;

        private static native void nAddAxis(long j, int i, float f);

        private static native long nBuild(long j, ByteBuffer byteBuffer, String str, int i, boolean z, int i2);

        private static native ByteBuffer nGetAssetBuffer(long j);

        private static native long nGetNativeAsset(AssetManager assetManager, String str, boolean z, int i);

        private static native long nGetReleaseNativeAssetFunc();

        private static native long nGetReleaseNativeFont();

        private static native long nInitBuilder();

        public Builder(ByteBuffer buffer) {
            this.mLocaleList = "";
            this.mWeight = -1;
            this.mItalic = -1;
            this.mTtcIndex = 0;
            this.mAxes = null;
            Preconditions.checkNotNull(buffer, "buffer can not be null");
            if (buffer.isDirect()) {
                this.mBuffer = buffer;
                return;
            }
            throw new IllegalArgumentException("Only direct buffer can be used as the source of font data.");
        }

        public Builder(ByteBuffer buffer, File path, String localeList) {
            this(buffer);
            this.mFile = path;
            this.mLocaleList = localeList;
        }

        /* JADX WARNING: Code restructure failed: missing block: B:10:0x0036, code lost:
            $closeResource(r0, r1);
         */
        /* JADX WARNING: Code restructure failed: missing block: B:11:0x0039, code lost:
            throw r2;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:9:0x0035, code lost:
            r2 = move-exception;
         */
        public Builder(File path) {
            this.mLocaleList = "";
            this.mWeight = -1;
            this.mItalic = -1;
            this.mTtcIndex = 0;
            this.mAxes = null;
            Preconditions.checkNotNull(path, "path can not be null");
            try {
                FileInputStream fis = new FileInputStream(path);
                FileChannel fc = fis.getChannel();
                this.mBuffer = fc.map(FileChannel.MapMode.READ_ONLY, 0, fc.size());
                $closeResource(null, fis);
            } catch (IOException e) {
                this.mException = e;
            }
            this.mFile = path;
        }

        private static /* synthetic */ void $closeResource(Throwable x0, AutoCloseable x1) {
            if (x0 != null) {
                try {
                    x1.close();
                } catch (Throwable th) {
                    x0.addSuppressed(th);
                }
            } else {
                x1.close();
            }
        }

        public Builder(ParcelFileDescriptor fd) {
            this(fd, 0, -1);
        }

        public Builder(ParcelFileDescriptor fd, long offset, long size) {
            IOException e;
            Throwable th;
            this.mLocaleList = "";
            this.mWeight = -1;
            this.mItalic = -1;
            this.mTtcIndex = 0;
            this.mAxes = null;
            try {
                FileInputStream fis = new FileInputStream(fd.getFileDescriptor());
                try {
                    FileChannel fc = fis.getChannel();
                    try {
                        this.mBuffer = fc.map(FileChannel.MapMode.READ_ONLY, offset, size == -1 ? fc.size() - offset : size);
                        try {
                            $closeResource(null, fis);
                        } catch (IOException e2) {
                            e = e2;
                            this.mException = e;
                        }
                    } catch (Throwable th2) {
                        th = th2;
                        try {
                            throw th;
                        } catch (Throwable th3) {
                            $closeResource(th, fis);
                            throw th3;
                        }
                    }
                } catch (Throwable th4) {
                    th = th4;
                    throw th;
                }
            } catch (IOException e3) {
                e = e3;
                this.mException = e;
            }
        }

        public Builder(AssetManager am, String path) {
            this(am, path, true, 0);
        }

        public Builder(AssetManager am, String path, boolean isAsset, int cookie) {
            this.mLocaleList = "";
            this.mWeight = -1;
            this.mItalic = -1;
            this.mTtcIndex = 0;
            this.mAxes = null;
            long nativeAsset = nGetNativeAsset(am, path, isAsset, cookie);
            if (nativeAsset == 0) {
                this.mException = new FileNotFoundException("Unable to open " + path);
                return;
            }
            ByteBuffer b = nGetAssetBuffer(nativeAsset);
            sAssetByteBufferRegistry.registerNativeAllocation(b, nativeAsset);
            if (b == null) {
                this.mException = new FileNotFoundException(path + " not found");
                return;
            }
            this.mBuffer = b;
        }

        public Builder(Resources res, int resId) {
            this.mLocaleList = "";
            this.mWeight = -1;
            this.mItalic = -1;
            this.mTtcIndex = 0;
            this.mAxes = null;
            TypedValue value = new TypedValue();
            res.getValue(resId, value, true);
            if (value.string == null) {
                this.mException = new FileNotFoundException(resId + " not found");
                return;
            }
            String str = value.string.toString();
            if (str.toLowerCase().endsWith(".xml")) {
                this.mException = new FileNotFoundException(resId + " must be font file.");
                return;
            }
            long nativeAsset = nGetNativeAsset(res.getAssets(), str, false, value.assetCookie);
            if (nativeAsset == 0) {
                this.mException = new FileNotFoundException("Unable to open " + str);
                return;
            }
            ByteBuffer b = nGetAssetBuffer(nativeAsset);
            sAssetByteBufferRegistry.registerNativeAllocation(b, nativeAsset);
            if (b == null) {
                this.mException = new FileNotFoundException(str + " not found");
                return;
            }
            this.mBuffer = b;
        }

        public Builder setWeight(int weight) {
            boolean z = true;
            if (1 > weight || weight > 1000) {
                z = false;
            }
            Preconditions.checkArgument(z);
            this.mWeight = weight;
            return this;
        }

        public Builder setSlant(int slant) {
            this.mItalic = slant == 0 ? 0 : 1;
            return this;
        }

        public Builder setTtcIndex(int ttcIndex) {
            this.mTtcIndex = ttcIndex;
            return this;
        }

        public Builder setFontVariationSettings(String variationSettings) {
            this.mAxes = FontVariationAxis.fromFontVariationSettings(variationSettings);
            return this;
        }

        public Builder setFontVariationSettings(FontVariationAxis[] axes) {
            this.mAxes = axes == null ? null : (FontVariationAxis[]) axes.clone();
            return this;
        }

        public Font build() throws IOException {
            IOException iOException = this.mException;
            if (iOException == null) {
                if (this.mWeight == -1 || this.mItalic == -1) {
                    int packed = FontFileUtil.analyzeStyle(this.mBuffer, this.mTtcIndex, this.mAxes);
                    if (FontFileUtil.isSuccess(packed)) {
                        if (this.mWeight == -1) {
                            this.mWeight = FontFileUtil.unpackWeight(packed);
                        }
                        if (this.mItalic == -1) {
                            this.mItalic = FontFileUtil.unpackItalic(packed) ? 1 : 0;
                        }
                    } else {
                        this.mWeight = 400;
                        this.mItalic = 0;
                    }
                }
                int slant = 1;
                this.mWeight = Math.max(1, Math.min(1000, this.mWeight));
                boolean italic = this.mItalic == 1;
                if (this.mItalic != 1) {
                    slant = 0;
                }
                long builderPtr = nInitBuilder();
                FontVariationAxis[] fontVariationAxisArr = this.mAxes;
                if (fontVariationAxisArr != null) {
                    for (FontVariationAxis axis : fontVariationAxisArr) {
                        nAddAxis(builderPtr, axis.getOpenTypeTagValue(), axis.getStyleValue());
                    }
                }
                ByteBuffer readonlyBuffer = this.mBuffer.asReadOnlyBuffer();
                File file = this.mFile;
                long ptr = nBuild(builderPtr, readonlyBuffer, file == null ? "" : file.getAbsolutePath(), this.mWeight, italic, this.mTtcIndex);
                Font font = new Font(ptr, readonlyBuffer, this.mFile, new FontStyle(this.mWeight, slant), this.mTtcIndex, this.mAxes, this.mLocaleList);
                sFontRegistry.registerNativeAllocation(font, ptr);
                return font;
            }
            throw new IOException("Failed to read font contents", iOException);
        }
    }

    private Font(long nativePtr, ByteBuffer buffer, File file, FontStyle fontStyle, int ttcIndex, FontVariationAxis[] axes, String localeList) {
        this.mBuffer = buffer;
        this.mFile = file;
        this.mFontStyle = fontStyle;
        this.mNativePtr = nativePtr;
        this.mTtcIndex = ttcIndex;
        this.mAxes = axes;
        this.mLocaleList = localeList;
    }

    public ByteBuffer getBuffer() {
        return this.mBuffer;
    }

    public File getFile() {
        return this.mFile;
    }

    public FontStyle getStyle() {
        return this.mFontStyle;
    }

    public int getTtcIndex() {
        return this.mTtcIndex;
    }

    public FontVariationAxis[] getAxes() {
        FontVariationAxis[] fontVariationAxisArr = this.mAxes;
        if (fontVariationAxisArr == null) {
            return null;
        }
        return (FontVariationAxis[]) fontVariationAxisArr.clone();
    }

    public LocaleList getLocaleList() {
        return LocaleList.forLanguageTags(this.mLocaleList);
    }

    public long getNativePtr() {
        return this.mNativePtr;
    }

    public boolean equals(Object o) {
        if (o == this) {
            return true;
        }
        if (o == null || !(o instanceof Font)) {
            return false;
        }
        Font f = (Font) o;
        if (!this.mFontStyle.equals(f.mFontStyle) || f.mTtcIndex != this.mTtcIndex || !Arrays.equals(f.mAxes, this.mAxes) || !f.mBuffer.equals(this.mBuffer)) {
            return false;
        }
        return true;
    }

    public int hashCode() {
        return Objects.hash(this.mFontStyle, Integer.valueOf(this.mTtcIndex), Integer.valueOf(Arrays.hashCode(this.mAxes)), this.mBuffer);
    }

    public String toString() {
        return "Font {path=" + this.mFile + ", style=" + this.mFontStyle + ", ttcIndex=" + this.mTtcIndex + ", axes=" + FontVariationAxis.toFontVariationSettings(this.mAxes) + ", localeList=" + this.mLocaleList + ", buffer=" + this.mBuffer + "}";
    }
}
