package android.media;

import android.app.backup.FullBackup;
import android.content.ContentResolver;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.BitmapFactory.Options;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.media.MediaFile.MediaFileType;
import android.net.ProxyInfo;
import android.net.Uri;
import android.os.ParcelFileDescriptor;
import android.util.Log;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.IOException;

public class ThumbnailUtils {
    private static final int MAX_NUM_PIXELS_MICRO_THUMBNAIL = 19200;
    private static final int MAX_NUM_PIXELS_THUMBNAIL = 196608;
    private static final int OPTIONS_NONE = 0;
    public static final int OPTIONS_RECYCLE_INPUT = 2;
    private static final int OPTIONS_SCALE_UP = 1;
    private static final String TAG = "ThumbnailUtils";
    public static final int TARGET_SIZE_MICRO_THUMBNAIL = 96;
    public static final int TARGET_SIZE_MINI_THUMBNAIL = 320;
    private static final int UNCONSTRAINED = -1;

    private static class SizedThumbnailBitmap {
        public Bitmap mBitmap;
        public byte[] mThumbnailData;
        public int mThumbnailHeight;
        public int mThumbnailWidth;

        /* synthetic */ SizedThumbnailBitmap(SizedThumbnailBitmap -this0) {
            this();
        }

        private SizedThumbnailBitmap() {
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:41:0x008c  */
    /* JADX WARNING: Removed duplicated region for block: B:41:0x008c  */
    /* JADX WARNING: Removed duplicated region for block: B:59:0x00ed A:{SYNTHETIC, Splitter: B:59:0x00ed} */
    /* JADX WARNING: Removed duplicated region for block: B:41:0x008c  */
    /* JADX WARNING: Removed duplicated region for block: B:51:0x00d2 A:{SYNTHETIC, Splitter: B:51:0x00d2} */
    /* JADX WARNING: Removed duplicated region for block: B:41:0x008c  */
    /* JADX WARNING: Removed duplicated region for block: B:65:0x00ff A:{SYNTHETIC, Splitter: B:65:0x00ff} */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public static Bitmap createImageThumbnail(String filePath, int kind) {
        int targetSize;
        int maxPixels;
        IOException ex;
        OutOfMemoryError oom;
        Throwable th;
        boolean wantMini = kind == 1;
        if (wantMini) {
            targetSize = 320;
        } else {
            targetSize = 96;
        }
        if (wantMini) {
            maxPixels = 196608;
        } else {
            maxPixels = MAX_NUM_PIXELS_MICRO_THUMBNAIL;
        }
        SizedThumbnailBitmap sizedThumbnailBitmap = new SizedThumbnailBitmap();
        Bitmap bitmap = null;
        MediaFileType fileType = MediaFile.getFileType(filePath);
        if (fileType != null && (fileType.fileType == 34 || MediaFile.isRawImageFileType(fileType.fileType))) {
            createThumbnailFromEXIF(filePath, targetSize, maxPixels, sizedThumbnailBitmap);
            bitmap = sizedThumbnailBitmap.mBitmap;
        }
        if (bitmap == null) {
            FileInputStream stream = null;
            try {
                FileInputStream stream2 = new FileInputStream(filePath);
                try {
                    FileDescriptor fd = stream2.getFD();
                    Options options = new Options();
                    options.inSampleSize = 1;
                    options.inJustDecodeBounds = true;
                    BitmapFactory.decodeFileDescriptor(fd, null, options);
                    if (!(options.mCancel || options.outWidth == -1)) {
                        if (options.outHeight != -1) {
                            options.inSampleSize = computeSampleSize(options, targetSize, maxPixels);
                            options.inJustDecodeBounds = false;
                            options.inThumbnailMode = true;
                            options.inDither = false;
                            options.inPreferredConfig = Config.ARGB_8888;
                            bitmap = BitmapFactory.decodeFileDescriptor(fd, null, options);
                            if (stream2 != null) {
                                try {
                                    stream2.close();
                                } catch (IOException ex2) {
                                    Log.e(TAG, ProxyInfo.LOCAL_EXCL_LIST, ex2);
                                }
                            }
                        }
                    }
                    if (stream2 != null) {
                        try {
                            stream2.close();
                        } catch (IOException ex22) {
                            Log.e(TAG, ProxyInfo.LOCAL_EXCL_LIST, ex22);
                        }
                    }
                    return null;
                } catch (IOException e) {
                    ex22 = e;
                    stream = stream2;
                    Log.e(TAG, ProxyInfo.LOCAL_EXCL_LIST, ex22);
                    if (stream != null) {
                        try {
                            stream.close();
                        } catch (IOException ex222) {
                            Log.e(TAG, ProxyInfo.LOCAL_EXCL_LIST, ex222);
                        }
                    }
                    if (kind == 3) {
                    }
                    return bitmap;
                } catch (OutOfMemoryError e2) {
                    oom = e2;
                    stream = stream2;
                    try {
                        Log.e(TAG, "Unable to decode file " + filePath + ". OutOfMemoryError.", oom);
                        if (stream != null) {
                            try {
                                stream.close();
                            } catch (IOException ex2222) {
                                Log.e(TAG, ProxyInfo.LOCAL_EXCL_LIST, ex2222);
                            }
                        }
                        if (kind == 3) {
                        }
                        return bitmap;
                    } catch (Throwable th2) {
                        th = th2;
                        if (stream != null) {
                        }
                        throw th;
                    }
                } catch (Throwable th3) {
                    th = th3;
                    stream = stream2;
                    if (stream != null) {
                        try {
                            stream.close();
                        } catch (IOException ex22222) {
                            Log.e(TAG, ProxyInfo.LOCAL_EXCL_LIST, ex22222);
                        }
                    }
                    throw th;
                }
            } catch (IOException e3) {
                ex22222 = e3;
                Log.e(TAG, ProxyInfo.LOCAL_EXCL_LIST, ex22222);
                if (stream != null) {
                }
                if (kind == 3) {
                }
                return bitmap;
            } catch (OutOfMemoryError e4) {
                oom = e4;
                Log.e(TAG, "Unable to decode file " + filePath + ". OutOfMemoryError.", oom);
                if (stream != null) {
                }
                if (kind == 3) {
                }
                return bitmap;
            }
        }
        if (kind == 3) {
            bitmap = extractThumbnail(bitmap, 96, 96, 2);
        }
        return bitmap;
    }

    public static Bitmap createVideoThumbnail(String filePath, int kind) {
        if (filePath == null) {
            return null;
        }
        Bitmap bitmap = null;
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        try {
            retriever.setDataSource(filePath);
            bitmap = retriever.getFrameAtTime(-1);
            try {
                retriever.release();
            } catch (RuntimeException e) {
            }
        } catch (IllegalArgumentException e2) {
            try {
                retriever.release();
            } catch (RuntimeException e3) {
            }
        } catch (RuntimeException e4) {
            try {
                retriever.release();
            } catch (RuntimeException e5) {
            }
        } catch (Throwable th) {
            try {
                retriever.release();
            } catch (RuntimeException e6) {
            }
            throw th;
        }
        if (bitmap == null) {
            return null;
        }
        if (kind == 1) {
            int width = bitmap.getWidth();
            int height = bitmap.getHeight();
            int max = Math.max(width, height);
            if (max > 512) {
                float scale = 512.0f / ((float) max);
                bitmap = Bitmap.createScaledBitmap(bitmap, Math.round(((float) width) * scale), Math.round(((float) height) * scale), true);
            }
        } else if (kind == 3) {
            bitmap = extractThumbnail(bitmap, 96, 96, 2);
        }
        return bitmap;
    }

    public static Bitmap extractThumbnail(Bitmap source, int width, int height) {
        return extractThumbnail(source, width, height, 0);
    }

    public static Bitmap extractThumbnail(Bitmap source, int width, int height, int options) {
        if (source == null) {
            return null;
        }
        float scale;
        if (source.getWidth() < source.getHeight()) {
            scale = ((float) width) / ((float) source.getWidth());
        } else {
            scale = ((float) height) / ((float) source.getHeight());
        }
        Matrix matrix = new Matrix();
        matrix.setScale(scale, scale);
        return transform(matrix, source, width, height, options | 1);
    }

    private static int computeSampleSize(Options options, int minSideLength, int maxNumOfPixels) {
        int initialSize = computeInitialSampleSize(options, minSideLength, maxNumOfPixels);
        if (initialSize > 8) {
            return ((initialSize + 7) / 8) * 8;
        }
        int roundedSize = 1;
        while (roundedSize < initialSize) {
            roundedSize <<= 1;
        }
        return roundedSize;
    }

    private static int computeInitialSampleSize(Options options, int minSideLength, int maxNumOfPixels) {
        int lowerBound;
        int upperBound;
        double w = (double) options.outWidth;
        double h = (double) options.outHeight;
        if (maxNumOfPixels == -1) {
            lowerBound = 1;
        } else {
            lowerBound = (int) Math.ceil(Math.sqrt((w * h) / ((double) maxNumOfPixels)));
        }
        if (minSideLength == -1) {
            upperBound = 128;
        } else {
            upperBound = (int) Math.min(Math.floor(w / ((double) minSideLength)), Math.floor(h / ((double) minSideLength)));
        }
        if (upperBound < lowerBound) {
            return lowerBound;
        }
        if (maxNumOfPixels == -1 && minSideLength == -1) {
            return 1;
        }
        if (minSideLength == -1) {
            return lowerBound;
        }
        return upperBound;
    }

    private static Bitmap makeBitmap(int minSideLength, int maxNumOfPixels, Uri uri, ContentResolver cr, ParcelFileDescriptor pfd, Options options) {
        if (pfd == null) {
            try {
                pfd = makeInputStream(uri, cr);
            } catch (OutOfMemoryError ex) {
                Log.e(TAG, "Got oom exception ", ex);
                return null;
            } finally {
                closeSilently(pfd);
            }
        }
        if (pfd == null) {
            closeSilently(pfd);
            return null;
        }
        if (options == null) {
            options = new Options();
        }
        FileDescriptor fd = pfd.getFileDescriptor();
        options.inSampleSize = 1;
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFileDescriptor(fd, null, options);
        if (!(options.mCancel || options.outWidth == -1)) {
            if (options.outHeight != -1) {
                options.inSampleSize = computeSampleSize(options, minSideLength, maxNumOfPixels);
                options.inJustDecodeBounds = false;
                options.inDither = false;
                options.inPreferredConfig = Config.ARGB_8888;
                Bitmap b = BitmapFactory.decodeFileDescriptor(fd, null, options);
                closeSilently(pfd);
                return b;
            }
        }
        closeSilently(pfd);
        return null;
    }

    private static void closeSilently(ParcelFileDescriptor c) {
        if (c != null) {
            try {
                c.close();
            } catch (Throwable th) {
            }
        }
    }

    private static ParcelFileDescriptor makeInputStream(Uri uri, ContentResolver cr) {
        try {
            return cr.openFileDescriptor(uri, FullBackup.ROOT_TREE_TOKEN);
        } catch (IOException e) {
            return null;
        }
    }

    private static Bitmap transform(Matrix scaler, Bitmap source, int targetWidth, int targetHeight, int options) {
        boolean scaleUp = (options & 1) != 0;
        boolean recycle = (options & 2) != 0;
        int deltaX = source.getWidth() - targetWidth;
        int deltaY = source.getHeight() - targetHeight;
        Bitmap b2;
        if (scaleUp || (deltaX >= 0 && deltaY >= 0)) {
            Bitmap b1;
            float bitmapWidthF = (float) source.getWidth();
            float bitmapHeightF = (float) source.getHeight();
            float scale;
            if (bitmapWidthF / bitmapHeightF > ((float) targetWidth) / ((float) targetHeight)) {
                scale = ((float) targetHeight) / bitmapHeightF;
                if (scale < 0.9f || scale > 1.0f) {
                    scaler.setScale(scale, scale);
                } else {
                    scaler = null;
                }
            } else {
                scale = ((float) targetWidth) / bitmapWidthF;
                if (scale < 0.9f || scale > 1.0f) {
                    scaler.setScale(scale, scale);
                } else {
                    scaler = null;
                }
            }
            if (scaler != null) {
                b1 = Bitmap.createBitmap(source, 0, 0, source.getWidth(), source.getHeight(), scaler, true);
            } else {
                b1 = source;
            }
            if (recycle && b1 != source) {
                source.recycle();
            }
            b2 = Bitmap.createBitmap(b1, Math.max(0, b1.getWidth() - targetWidth) / 2, Math.max(0, b1.getHeight() - targetHeight) / 2, targetWidth, targetHeight);
            if (b2 != b1 && (recycle || b1 != source)) {
                b1.recycle();
            }
            return b2;
        }
        b2 = Bitmap.createBitmap(targetWidth, targetHeight, Config.ARGB_8888);
        Canvas c = new Canvas(b2);
        int deltaXHalf = Math.max(0, deltaX / 2);
        int deltaYHalf = Math.max(0, deltaY / 2);
        Rect rect = new Rect(deltaXHalf, deltaYHalf, Math.min(targetWidth, source.getWidth()) + deltaXHalf, Math.min(targetHeight, source.getHeight()) + deltaYHalf);
        int dstX = (targetWidth - rect.width()) / 2;
        int dstY = (targetHeight - rect.height()) / 2;
        c.drawBitmap(source, rect, new Rect(dstX, dstY, targetWidth - dstX, targetHeight - dstY), null);
        if (recycle) {
            source.recycle();
        }
        c.setBitmap(null);
        return b2;
    }

    /* JADX WARNING: Removed duplicated region for block: B:10:0x001d  */
    /* JADX WARNING: Removed duplicated region for block: B:10:0x001d  */
    /* JADX WARNING: Removed duplicated region for block: B:13:0x0045 A:{SKIP} */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private static void createThumbnailFromEXIF(String filePath, int targetSize, int maxPixels, SizedThumbnailBitmap sizedThumbBitmap) {
        IOException ex;
        Options fullOptions;
        int exifThumbWidth;
        int fullThumbWidth;
        if (filePath != null) {
            Options exifOptions;
            byte[] thumbData = null;
            try {
                ExifInterface exif = new ExifInterface(filePath);
                try {
                    thumbData = exif.getThumbnail();
                    ExifInterface exifInterface = exif;
                } catch (IOException e) {
                    ex = e;
                    Log.w(TAG, ex);
                    fullOptions = new Options();
                    exifOptions = new Options();
                    exifThumbWidth = 0;
                    if (thumbData != null) {
                    }
                    fullOptions.inJustDecodeBounds = true;
                    BitmapFactory.decodeFile(filePath, fullOptions);
                    fullOptions.inSampleSize = computeSampleSize(fullOptions, targetSize, maxPixels);
                    fullThumbWidth = fullOptions.outWidth / fullOptions.inSampleSize;
                    if (thumbData != null) {
                    }
                    fullOptions.inJustDecodeBounds = false;
                    sizedThumbBitmap.mBitmap = BitmapFactory.decodeFile(filePath, fullOptions);
                }
            } catch (IOException e2) {
                ex = e2;
                Log.w(TAG, ex);
                fullOptions = new Options();
                exifOptions = new Options();
                exifThumbWidth = 0;
                if (thumbData != null) {
                }
                fullOptions.inJustDecodeBounds = true;
                BitmapFactory.decodeFile(filePath, fullOptions);
                fullOptions.inSampleSize = computeSampleSize(fullOptions, targetSize, maxPixels);
                fullThumbWidth = fullOptions.outWidth / fullOptions.inSampleSize;
                if (thumbData != null) {
                }
                fullOptions.inJustDecodeBounds = false;
                sizedThumbBitmap.mBitmap = BitmapFactory.decodeFile(filePath, fullOptions);
            }
            fullOptions = new Options();
            exifOptions = new Options();
            exifThumbWidth = 0;
            if (thumbData != null) {
                exifOptions.inJustDecodeBounds = true;
                BitmapFactory.decodeByteArray(thumbData, 0, thumbData.length, exifOptions);
                exifOptions.inSampleSize = computeSampleSize(exifOptions, targetSize, maxPixels);
                exifThumbWidth = exifOptions.outWidth / exifOptions.inSampleSize;
            }
            fullOptions.inJustDecodeBounds = true;
            BitmapFactory.decodeFile(filePath, fullOptions);
            fullOptions.inSampleSize = computeSampleSize(fullOptions, targetSize, maxPixels);
            fullThumbWidth = fullOptions.outWidth / fullOptions.inSampleSize;
            if (thumbData != null || exifThumbWidth < fullThumbWidth) {
                fullOptions.inJustDecodeBounds = false;
                sizedThumbBitmap.mBitmap = BitmapFactory.decodeFile(filePath, fullOptions);
            } else {
                int width = exifOptions.outWidth;
                int height = exifOptions.outHeight;
                exifOptions.inJustDecodeBounds = false;
                sizedThumbBitmap.mBitmap = BitmapFactory.decodeByteArray(thumbData, 0, thumbData.length, exifOptions);
                if (sizedThumbBitmap.mBitmap != null) {
                    sizedThumbBitmap.mThumbnailData = thumbData;
                    sizedThumbBitmap.mThumbnailWidth = width;
                    sizedThumbBitmap.mThumbnailHeight = height;
                }
            }
        }
    }
}
