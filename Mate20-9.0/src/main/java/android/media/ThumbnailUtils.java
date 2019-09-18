package android.media;

import android.app.backup.FullBackup;
import android.content.ContentResolver;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;
import android.media.MediaFile;
import android.media.MediaMetadataRetriever;
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

        private SizedThumbnailBitmap() {
        }
    }

    public static Bitmap createImageThumbnail(String filePath, int kind) {
        int targetSize;
        int maxPixels;
        String str = filePath;
        int i = kind;
        boolean wantMini = i == 1;
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
        MediaFile.MediaFileType fileType = MediaFile.getFileType(filePath);
        if (fileType != null) {
            if (fileType.fileType == 34 || MediaFile.isRawImageFileType(fileType.fileType)) {
                createThumbnailFromEXIF(str, targetSize, maxPixels, sizedThumbnailBitmap);
                bitmap = sizedThumbnailBitmap.mBitmap;
            } else if (fileType.fileType == 40) {
                bitmap = createThumbnailFromMetadataRetriever(str, targetSize, maxPixels);
            }
        }
        if (bitmap == null) {
            FileInputStream stream = null;
            try {
                FileInputStream stream2 = new FileInputStream(str);
                FileDescriptor fd = stream2.getFD();
                BitmapFactory.Options options = new BitmapFactory.Options();
                options.inSampleSize = 1;
                options.inJustDecodeBounds = true;
                BitmapFactory.decodeFileDescriptor(fd, null, options);
                if (!options.mCancel && options.outWidth != -1) {
                    if (options.outHeight != -1) {
                        options.inSampleSize = computeSampleSize(options, targetSize, maxPixels);
                        options.inJustDecodeBounds = false;
                        options.inThumbnailMode = true;
                        options.inDither = false;
                        options.inPreferredConfig = Bitmap.Config.ARGB_8888;
                        bitmap = BitmapFactory.decodeFileDescriptor(fd, null, options);
                        try {
                            stream2.close();
                        } catch (IOException ex) {
                            IOException iOException = ex;
                            Log.e(TAG, "", ex);
                        }
                    }
                }
                try {
                    stream2.close();
                } catch (IOException ex2) {
                    IOException iOException2 = ex2;
                    Log.e(TAG, "", ex2);
                }
                return null;
            } catch (IOException ex3) {
                Log.e(TAG, "", ex3);
                if (stream != null) {
                    stream.close();
                }
            } catch (OutOfMemoryError oom) {
                Log.e(TAG, "Unable to decode file OutOfMemoryError.", oom);
                if (stream != null) {
                    stream.close();
                }
            } catch (Throwable th) {
                Throwable th2 = th;
                if (stream != null) {
                    try {
                        stream.close();
                    } catch (IOException ex4) {
                        IOException iOException3 = ex4;
                        Log.e(TAG, "", ex4);
                    }
                }
                throw th2;
            }
        }
        if (i == 3) {
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
            retriever.release();
        } catch (RuntimeException e3) {
            retriever.release();
        } catch (Throwable th) {
            try {
                retriever.release();
            } catch (RuntimeException e4) {
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
        return transform(matrix, source, width, height, 1 | options);
    }

    private static int computeSampleSize(BitmapFactory.Options options, int minSideLength, int maxNumOfPixels) {
        int initialSize = computeInitialSampleSize(options, minSideLength, maxNumOfPixels);
        if (initialSize > 8) {
            return 8 * ((initialSize + 7) / 8);
        }
        int roundedSize = 1;
        while (roundedSize < initialSize) {
            roundedSize <<= 1;
        }
        return roundedSize;
    }

    private static int computeInitialSampleSize(BitmapFactory.Options options, int minSideLength, int maxNumOfPixels) {
        int upperBound;
        double w = (double) options.outWidth;
        double h = (double) options.outHeight;
        int lowerBound = maxNumOfPixels == -1 ? 1 : (int) Math.ceil(Math.sqrt((w * h) / ((double) maxNumOfPixels)));
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

    private static Bitmap makeBitmap(int minSideLength, int maxNumOfPixels, Uri uri, ContentResolver cr, ParcelFileDescriptor pfd, BitmapFactory.Options options) {
        if (pfd == null) {
            try {
                pfd = makeInputStream(uri, cr);
            } catch (OutOfMemoryError ex) {
                Log.e(TAG, "Got oom exception ", ex);
                closeSilently(pfd);
                return null;
            } catch (Throwable th) {
                closeSilently(pfd);
                throw th;
            }
        }
        if (pfd == null) {
            closeSilently(pfd);
            return null;
        }
        if (options == null) {
            options = new BitmapFactory.Options();
        }
        FileDescriptor fd = pfd.getFileDescriptor();
        options.inSampleSize = 1;
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFileDescriptor(fd, null, options);
        if (!options.mCancel && options.outWidth != -1) {
            if (options.outHeight != -1) {
                options.inSampleSize = computeSampleSize(options, minSideLength, maxNumOfPixels);
                options.inJustDecodeBounds = false;
                options.inDither = false;
                options.inPreferredConfig = Bitmap.Config.ARGB_8888;
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
        Bitmap b1;
        Matrix scaler2 = scaler;
        Bitmap bitmap = source;
        int i = targetWidth;
        int i2 = targetHeight;
        boolean z = true;
        boolean scaleUp = (options & 1) != 0;
        if ((options & 2) == 0) {
            z = false;
        }
        boolean recycle = z;
        int deltaX = source.getWidth() - i;
        int deltaY = source.getHeight() - i2;
        if (scaleUp || (deltaX >= 0 && deltaY >= 0)) {
            float bitmapWidthF = (float) source.getWidth();
            float bitmapHeightF = (float) source.getHeight();
            if (bitmapWidthF / bitmapHeightF > ((float) i) / ((float) i2)) {
                float scale = ((float) i2) / bitmapHeightF;
                if (scale < 0.9f || scale > 1.0f) {
                    scaler2.setScale(scale, scale);
                } else {
                    scaler2 = null;
                }
            } else {
                float scale2 = ((float) i) / bitmapWidthF;
                if (scale2 < 0.9f || scale2 > 1.0f) {
                    scaler2.setScale(scale2, scale2);
                } else {
                    scaler2 = null;
                }
            }
            Matrix scaler3 = scaler2;
            if (scaler3 != null) {
                float f = bitmapHeightF;
                b1 = Bitmap.createBitmap(bitmap, 0, 0, source.getWidth(), source.getHeight(), scaler3, true);
            } else {
                b1 = bitmap;
            }
            if (recycle && b1 != bitmap) {
                source.recycle();
            }
            Bitmap b2 = Bitmap.createBitmap(b1, Math.max(0, b1.getWidth() - i) / 2, Math.max(0, b1.getHeight() - i2) / 2, i, i2);
            if (b2 != b1 && (recycle || b1 != bitmap)) {
                b1.recycle();
            }
            return b2;
        }
        Bitmap b22 = Bitmap.createBitmap(i, i2, Bitmap.Config.ARGB_8888);
        Canvas c = new Canvas(b22);
        int deltaXHalf = Math.max(0, deltaX / 2);
        int deltaYHalf = Math.max(0, deltaY / 2);
        Rect src = new Rect(deltaXHalf, deltaYHalf, Math.min(i, source.getWidth()) + deltaXHalf, Math.min(i2, source.getHeight()) + deltaYHalf);
        int dstX = (i - src.width()) / 2;
        int dstY = (i2 - src.height()) / 2;
        int i3 = deltaXHalf;
        int i4 = deltaYHalf;
        c.drawBitmap(bitmap, src, new Rect(dstX, dstY, i - dstX, i2 - dstY), (Paint) null);
        if (recycle) {
            source.recycle();
        }
        c.setBitmap(null);
        return b22;
    }

    private static void createThumbnailFromEXIF(String filePath, int targetSize, int maxPixels, SizedThumbnailBitmap sizedThumbBitmap) {
        if (filePath != null) {
            byte[] thumbData = null;
            try {
                thumbData = new ExifInterface(filePath).getThumbnail();
            } catch (IOException ex) {
                Log.w(TAG, ex);
            }
            BitmapFactory.Options fullOptions = new BitmapFactory.Options();
            BitmapFactory.Options exifOptions = new BitmapFactory.Options();
            int exifThumbWidth = 0;
            if (thumbData != null) {
                exifOptions.inJustDecodeBounds = true;
                BitmapFactory.decodeByteArray(thumbData, 0, thumbData.length, exifOptions);
                exifOptions.inSampleSize = computeSampleSize(exifOptions, targetSize, maxPixels);
                exifThumbWidth = exifOptions.outWidth / exifOptions.inSampleSize;
            }
            fullOptions.inJustDecodeBounds = true;
            BitmapFactory.decodeFile(filePath, fullOptions);
            fullOptions.inSampleSize = computeSampleSize(fullOptions, targetSize, maxPixels);
            int fullThumbWidth = fullOptions.outWidth / fullOptions.inSampleSize;
            if (thumbData == null || exifThumbWidth < fullThumbWidth) {
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

    private static Bitmap createThumbnailFromMetadataRetriever(String filePath, int targetSize, int maxPixels) {
        if (filePath == null) {
            return null;
        }
        Bitmap thumbnail = null;
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        try {
            retriever.setDataSource(filePath);
            MediaMetadataRetriever.BitmapParams params = new MediaMetadataRetriever.BitmapParams();
            params.setPreferredConfig(Bitmap.Config.ARGB_8888);
            thumbnail = retriever.getThumbnailImageAtIndex(-1, params, targetSize, maxPixels);
        } catch (RuntimeException e) {
        } catch (Throwable th) {
            retriever.release();
            throw th;
        }
        retriever.release();
        return thumbnail;
    }
}
