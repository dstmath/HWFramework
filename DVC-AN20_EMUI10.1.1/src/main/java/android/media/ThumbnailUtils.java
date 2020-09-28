package android.media;

import android.annotation.UnsupportedAppUsage;
import android.content.ContentResolver;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.ImageDecoder;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.CancellationSignal;
import android.os.Environment;
import android.os.ParcelFileDescriptor;
import android.provider.MediaStore;
import android.util.Log;
import android.util.Size;
import com.android.internal.util.ArrayUtils;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Objects;
import java.util.function.ToIntFunction;
import libcore.io.IoUtils;

public class ThumbnailUtils {
    private static final int OPTIONS_NONE = 0;
    public static final int OPTIONS_RECYCLE_INPUT = 2;
    private static final int OPTIONS_SCALE_UP = 1;
    private static final String TAG = "ThumbnailUtils";
    @UnsupportedAppUsage
    @Deprecated
    public static final int TARGET_SIZE_MICRO_THUMBNAIL = 96;

    private static Size convertKind(int kind) {
        if (kind == 3) {
            return Point.convert(MediaStore.ThumbnailConstants.MICRO_SIZE);
        }
        if (kind == 2) {
            return Point.convert(MediaStore.ThumbnailConstants.FULL_SCREEN_SIZE);
        }
        if (kind == 1) {
            return Point.convert(MediaStore.ThumbnailConstants.MINI_SIZE);
        }
        throw new IllegalArgumentException("Unsupported kind: " + kind);
    }

    /* access modifiers changed from: private */
    public static class Resizer implements ImageDecoder.OnHeaderDecodedListener {
        private final CancellationSignal signal;
        private final Size size;

        public Resizer(Size size2, CancellationSignal signal2) {
            this.size = size2;
            this.signal = signal2;
        }

        @Override // android.graphics.ImageDecoder.OnHeaderDecodedListener
        public void onHeaderDecoded(ImageDecoder decoder, ImageDecoder.ImageInfo info, ImageDecoder.Source source) {
            CancellationSignal cancellationSignal = this.signal;
            if (cancellationSignal != null) {
                cancellationSignal.throwIfCanceled();
            }
            decoder.setAllocator(1);
            int sample = Math.max(info.getSize().getWidth() / this.size.getWidth(), info.getSize().getHeight() / this.size.getHeight());
            if (sample > 1) {
                decoder.setTargetSampleSize(sample);
            }
        }
    }

    @Deprecated
    public static Bitmap createAudioThumbnail(String filePath, int kind) {
        try {
            return createAudioThumbnail(new File(filePath), convertKind(kind), null);
        } catch (IOException e) {
            Log.w(TAG, e);
            return null;
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:40:0x00bc, code lost:
        r3 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:41:0x00bd, code lost:
        $closeResource(r2, r1);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:42:0x00c0, code lost:
        throw r3;
     */
    public static Bitmap createAudioThumbnail(File file, Size size, CancellationSignal signal) throws IOException {
        if (signal != null) {
            signal.throwIfCanceled();
        }
        Resizer resizer = new Resizer(size, signal);
        try {
            MediaMetadataRetriever retriever = new MediaMetadataRetriever();
            retriever.setDataSource(file.getAbsolutePath());
            byte[] raw = retriever.getEmbeddedPicture();
            if (raw != null) {
                Bitmap decodeBitmap = ImageDecoder.decodeBitmap(ImageDecoder.createSource(raw), resizer);
                $closeResource(null, retriever);
                return decodeBitmap;
            }
            $closeResource(null, retriever);
            if (!"unknown".equals(Environment.getExternalStorageState(file))) {
                File parent = file.getParentFile();
                File grandParent = parent != null ? parent.getParentFile() : null;
                if (parent != null && parent.getName().equals(Environment.DIRECTORY_DOWNLOADS)) {
                    throw new IOException("No thumbnails in Downloads directories");
                } else if (grandParent == null || !"unknown".equals(Environment.getExternalStorageState(grandParent))) {
                    File bestFile = (File) Arrays.asList(ArrayUtils.defeatNullable(file.getParentFile().listFiles($$Lambda$ThumbnailUtils$P13h9YbyD69p6ss1gYpoef43_MU.INSTANCE))).stream().max(new Comparator($$Lambda$ThumbnailUtils$qOH5vebuTwPi2G92PTa6rgwKGoc.INSTANCE) {
                        /* class android.media.$$Lambda$ThumbnailUtils$HhGKNQZck57eO__Paj6KyQm6lCk */
                        private final /* synthetic */ ToIntFunction f$0;

                        {
                            this.f$0 = r1;
                        }

                        @Override // java.util.Comparator
                        public final int compare(Object obj, Object obj2) {
                            return ThumbnailUtils.lambda$createAudioThumbnail$2(this.f$0, (File) obj, (File) obj2);
                        }
                    }).orElse(null);
                    if (bestFile != null) {
                        if (signal != null) {
                            signal.throwIfCanceled();
                        }
                        return ImageDecoder.decodeBitmap(ImageDecoder.createSource(bestFile), resizer);
                    }
                    throw new IOException("No album art found");
                } else {
                    throw new IOException("No thumbnails in top-level directories");
                }
            } else {
                throw new IOException("No embedded album art found");
            }
        } catch (RuntimeException e) {
            throw new IOException("Failed to create thumbnail", e);
        }
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

    static /* synthetic */ boolean lambda$createAudioThumbnail$0(File dir, String name) {
        String lower = name.toLowerCase();
        return lower.endsWith(".jpg") || lower.endsWith(".png");
    }

    static /* synthetic */ int lambda$createAudioThumbnail$1(File f) {
        String lower = f.getName().toLowerCase();
        if (lower.equals("albumart.jpg")) {
            return 4;
        }
        if (lower.startsWith("albumart") && lower.endsWith(".jpg")) {
            return 3;
        }
        if (lower.contains("albumart") && lower.endsWith(".jpg")) {
            return 2;
        }
        if (lower.endsWith(".jpg")) {
            return 1;
        }
        return 0;
    }

    static /* synthetic */ int lambda$createAudioThumbnail$2(ToIntFunction score, File a, File b) {
        return score.applyAsInt(a) - score.applyAsInt(b);
    }

    @Deprecated
    public static Bitmap createImageThumbnail(String filePath, int kind) {
        try {
            return createImageThumbnail(new File(filePath), convertKind(kind), null);
        } catch (IOException e) {
            Log.w(TAG, e);
            return null;
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:50:0x00ec, code lost:
        r0 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:51:0x00ed, code lost:
        $closeResource(r0, r0);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:52:0x00f1, code lost:
        throw r0;
     */
    public static Bitmap createImageThumbnail(File file, Size size, CancellationSignal signal) throws IOException {
        ExifInterface exif;
        byte[] raw;
        if (signal != null) {
            signal.throwIfCanceled();
        }
        Resizer resizer = new Resizer(size, signal);
        String mimeType = MediaFile.getMimeTypeForFile(file.getName());
        Bitmap bitmap = null;
        int orientation = 0;
        if (MediaFile.isExifMimeType(mimeType)) {
            ExifInterface exif2 = new ExifInterface(file);
            int attributeInt = exif2.getAttributeInt(ExifInterface.TAG_ORIENTATION, 0);
            if (attributeInt == 3) {
                orientation = 180;
                exif = exif2;
            } else if (attributeInt == 6) {
                orientation = 90;
                exif = exif2;
            } else if (attributeInt != 8) {
                exif = exif2;
            } else {
                orientation = 270;
                exif = exif2;
            }
        } else {
            exif = null;
        }
        if (mimeType.equals("image/heif") || mimeType.equals("image/heif-sequence") || mimeType.equals("image/heic") || mimeType.equals("image/heic-sequence")) {
            try {
                MediaMetadataRetriever retriever = new MediaMetadataRetriever();
                retriever.setDataSource(file.getAbsolutePath());
                bitmap = retriever.getThumbnailImageAtIndex(-1, new MediaMetadataRetriever.BitmapParams(), size.getWidth(), size.getWidth() * size.getHeight());
                $closeResource(null, retriever);
            } catch (RuntimeException e) {
                throw new IOException("Failed to create thumbnail", e);
            }
        }
        if (!(bitmap != null || exif == null || (raw = exif.getThumbnailBytes()) == null)) {
            try {
                bitmap = ImageDecoder.decodeBitmap(ImageDecoder.createSource(raw), resizer);
            } catch (ImageDecoder.DecodeException e2) {
                Log.w(TAG, e2);
            }
        }
        if (signal != null) {
            signal.throwIfCanceled();
        }
        if (bitmap == null) {
            return ImageDecoder.decodeBitmap(ImageDecoder.createSource(file), resizer);
        }
        if (orientation == 0) {
            return bitmap;
        }
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        Matrix m = new Matrix();
        m.setRotate((float) orientation, (float) (width / 2), (float) (height / 2));
        return Bitmap.createBitmap(bitmap, 0, 0, width, height, m, false);
    }

    @Deprecated
    public static Bitmap createVideoThumbnail(String filePath, int kind) {
        try {
            return createVideoThumbnail(new File(filePath), convertKind(kind), null);
        } catch (IOException e) {
            Log.w(TAG, e);
            return null;
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:25:0x008b, code lost:
        r0 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:26:0x008c, code lost:
        $closeResource(r0, r0);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:27:0x0090, code lost:
        throw r0;
     */
    public static Bitmap createVideoThumbnail(File file, Size size, CancellationSignal signal) throws IOException {
        if (signal != null) {
            signal.throwIfCanceled();
        }
        Resizer resizer = new Resizer(size, signal);
        try {
            MediaMetadataRetriever mmr = new MediaMetadataRetriever();
            mmr.setDataSource(file.getAbsolutePath());
            byte[] raw = mmr.getEmbeddedPicture();
            if (raw != null) {
                Bitmap decodeBitmap = ImageDecoder.decodeBitmap(ImageDecoder.createSource(raw), resizer);
                $closeResource(null, mmr);
                return decodeBitmap;
            }
            int width = Integer.parseInt(mmr.extractMetadata(18));
            int height = Integer.parseInt(mmr.extractMetadata(19));
            Long.parseLong(mmr.extractMetadata(9));
            if (size.getWidth() <= width || size.getHeight() <= height) {
                Bitmap bitmap = (Bitmap) Objects.requireNonNull(mmr.getScaledFrameAtTime(-1, 2, size.getWidth(), size.getHeight()));
                $closeResource(null, mmr);
                return bitmap;
            }
            Bitmap bitmap2 = (Bitmap) Objects.requireNonNull(mmr.getFrameAtTime(-1, 2));
            $closeResource(null, mmr);
            return bitmap2;
        } catch (RuntimeException e) {
            throw new IOException("Failed to create thumbnail", e);
        }
    }

    public static Bitmap extractThumbnail(Bitmap source, int width, int height) {
        return extractThumbnail(source, width, height, 0);
    }

    public static Bitmap extractThumbnail(Bitmap source, int width, int height, int options) {
        float scale;
        if (source == null) {
            return null;
        }
        if (source.getWidth() < source.getHeight()) {
            scale = ((float) width) / ((float) source.getWidth());
        } else {
            scale = ((float) height) / ((float) source.getHeight());
        }
        Matrix matrix = new Matrix();
        matrix.setScale(scale, scale);
        return transform(matrix, source, width, height, options | 1);
    }

    @UnsupportedAppUsage(maxTargetSdk = 28, trackingBug = 115609023)
    @Deprecated
    private static int computeSampleSize(BitmapFactory.Options options, int minSideLength, int maxNumOfPixels) {
        return 1;
    }

    @UnsupportedAppUsage(maxTargetSdk = 28, trackingBug = 115609023)
    @Deprecated
    private static int computeInitialSampleSize(BitmapFactory.Options options, int minSideLength, int maxNumOfPixels) {
        return 1;
    }

    @UnsupportedAppUsage(maxTargetSdk = 28, trackingBug = 115609023)
    @Deprecated
    private static void closeSilently(ParcelFileDescriptor c) {
        IoUtils.closeQuietly(c);
    }

    @UnsupportedAppUsage(maxTargetSdk = 28, trackingBug = 115609023)
    @Deprecated
    private static ParcelFileDescriptor makeInputStream(Uri uri, ContentResolver cr) {
        try {
            return cr.openFileDescriptor(uri, "r");
        } catch (IOException e) {
            return null;
        }
    }

    @UnsupportedAppUsage(maxTargetSdk = 28, trackingBug = 115609023)
    @Deprecated
    private static Bitmap transform(Matrix scaler, Bitmap source, int targetWidth, int targetHeight, int options) {
        Matrix scaler2;
        Bitmap b1;
        Matrix scaler3 = scaler;
        boolean recycle = true;
        boolean scaleUp = (options & 1) != 0;
        if ((options & 2) == 0) {
            recycle = false;
        }
        int deltaX = source.getWidth() - targetWidth;
        int deltaY = source.getHeight() - targetHeight;
        if (scaleUp || (deltaX >= 0 && deltaY >= 0)) {
            float bitmapWidthF = (float) source.getWidth();
            float bitmapHeightF = (float) source.getHeight();
            if (bitmapWidthF / bitmapHeightF > ((float) targetWidth) / ((float) targetHeight)) {
                float scale = ((float) targetHeight) / bitmapHeightF;
                if (scale < 0.9f || scale > 1.0f) {
                    scaler3.setScale(scale, scale);
                } else {
                    scaler3 = null;
                }
                scaler2 = scaler3;
            } else {
                float scale2 = ((float) targetWidth) / bitmapWidthF;
                if (scale2 < 0.9f || scale2 > 1.0f) {
                    scaler3.setScale(scale2, scale2);
                    scaler2 = scaler3;
                } else {
                    scaler2 = null;
                }
            }
            if (scaler2 != null) {
                b1 = Bitmap.createBitmap(source, 0, 0, source.getWidth(), source.getHeight(), scaler2, true);
            } else {
                b1 = source;
            }
            if (recycle && b1 != source) {
                source.recycle();
            }
            Bitmap b2 = Bitmap.createBitmap(b1, Math.max(0, b1.getWidth() - targetWidth) / 2, Math.max(0, b1.getHeight() - targetHeight) / 2, targetWidth, targetHeight);
            if (b2 != b1 && (recycle || b1 != source)) {
                b1.recycle();
            }
            return b2;
        }
        Bitmap b22 = Bitmap.createBitmap(targetWidth, targetHeight, Bitmap.Config.ARGB_8888);
        Canvas c = new Canvas(b22);
        int deltaXHalf = Math.max(0, deltaX / 2);
        int deltaYHalf = Math.max(0, deltaY / 2);
        Rect src = new Rect(deltaXHalf, deltaYHalf, Math.min(targetWidth, source.getWidth()) + deltaXHalf, Math.min(targetHeight, source.getHeight()) + deltaYHalf);
        int dstX = (targetWidth - src.width()) / 2;
        int dstY = (targetHeight - src.height()) / 2;
        c.drawBitmap(source, src, new Rect(dstX, dstY, targetWidth - dstX, targetHeight - dstY), (Paint) null);
        if (recycle) {
            source.recycle();
        }
        c.setBitmap(null);
        return b22;
    }

    @Deprecated
    private static class SizedThumbnailBitmap {
        public Bitmap mBitmap;
        public byte[] mThumbnailData;
        public int mThumbnailHeight;
        public int mThumbnailWidth;

        private SizedThumbnailBitmap() {
        }
    }

    @UnsupportedAppUsage(maxTargetSdk = 28, trackingBug = 115609023)
    @Deprecated
    private static void createThumbnailFromEXIF(String filePath, int targetSize, int maxPixels, SizedThumbnailBitmap sizedThumbBitmap) {
    }
}
