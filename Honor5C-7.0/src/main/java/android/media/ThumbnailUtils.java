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
import android.provider.DocumentsContract.Document;
import android.security.keymaster.KeymasterDefs;
import android.speech.tts.TextToSpeech.Engine;
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
        IOException ex;
        OutOfMemoryError oom;
        Throwable th;
        boolean wantMini = kind == OPTIONS_SCALE_UP;
        if (wantMini) {
            targetSize = TARGET_SIZE_MINI_THUMBNAIL;
        } else {
            targetSize = TARGET_SIZE_MICRO_THUMBNAIL;
        }
        if (wantMini) {
            maxPixels = MAX_NUM_PIXELS_THUMBNAIL;
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
            FileInputStream fileInputStream = null;
            try {
                FileInputStream stream = new FileInputStream(filePath);
                try {
                    FileDescriptor fd = stream.getFD();
                    Options options = new Options();
                    options.inSampleSize = OPTIONS_SCALE_UP;
                    options.inJustDecodeBounds = true;
                    BitmapFactory.decodeFileDescriptor(fd, null, options);
                    if (!(options.mCancel || options.outWidth == UNCONSTRAINED)) {
                        if (options.outHeight != UNCONSTRAINED) {
                            options.inSampleSize = computeSampleSize(options, targetSize, maxPixels);
                            options.inJustDecodeBounds = false;
                            options.inThumbnailMode = true;
                            options.inDither = false;
                            options.inPreferredConfig = Config.ARGB_8888;
                            bitmap = BitmapFactory.decodeFileDescriptor(fd, null, options);
                            if (stream != null) {
                                try {
                                    stream.close();
                                } catch (IOException ex2) {
                                    Log.e(TAG, ProxyInfo.LOCAL_EXCL_LIST, ex2);
                                }
                            }
                        }
                    }
                    if (stream != null) {
                        try {
                            stream.close();
                        } catch (IOException ex22) {
                            Log.e(TAG, ProxyInfo.LOCAL_EXCL_LIST, ex22);
                        }
                    }
                    return null;
                } catch (IOException e) {
                    ex22 = e;
                    fileInputStream = stream;
                    Log.e(TAG, ProxyInfo.LOCAL_EXCL_LIST, ex22);
                    if (fileInputStream != null) {
                        try {
                            fileInputStream.close();
                        } catch (IOException ex222) {
                            Log.e(TAG, ProxyInfo.LOCAL_EXCL_LIST, ex222);
                        }
                    }
                    if (kind == 3) {
                        bitmap = extractThumbnail(bitmap, TARGET_SIZE_MICRO_THUMBNAIL, TARGET_SIZE_MICRO_THUMBNAIL, OPTIONS_RECYCLE_INPUT);
                    }
                    return bitmap;
                } catch (OutOfMemoryError e2) {
                    oom = e2;
                    fileInputStream = stream;
                    try {
                        Log.e(TAG, "Unable to decode file " + filePath + ". OutOfMemoryError.", oom);
                        if (fileInputStream != null) {
                            try {
                                fileInputStream.close();
                            } catch (IOException ex2222) {
                                Log.e(TAG, ProxyInfo.LOCAL_EXCL_LIST, ex2222);
                            }
                        }
                        if (kind == 3) {
                            bitmap = extractThumbnail(bitmap, TARGET_SIZE_MICRO_THUMBNAIL, TARGET_SIZE_MICRO_THUMBNAIL, OPTIONS_RECYCLE_INPUT);
                        }
                        return bitmap;
                    } catch (Throwable th2) {
                        th = th2;
                        if (fileInputStream != null) {
                            try {
                                fileInputStream.close();
                            } catch (IOException ex22222) {
                                Log.e(TAG, ProxyInfo.LOCAL_EXCL_LIST, ex22222);
                            }
                        }
                        throw th;
                    }
                } catch (Throwable th3) {
                    th = th3;
                    fileInputStream = stream;
                    if (fileInputStream != null) {
                        fileInputStream.close();
                    }
                    throw th;
                }
            } catch (IOException e3) {
                ex22222 = e3;
                Log.e(TAG, ProxyInfo.LOCAL_EXCL_LIST, ex22222);
                if (fileInputStream != null) {
                    fileInputStream.close();
                }
                if (kind == 3) {
                    bitmap = extractThumbnail(bitmap, TARGET_SIZE_MICRO_THUMBNAIL, TARGET_SIZE_MICRO_THUMBNAIL, OPTIONS_RECYCLE_INPUT);
                }
                return bitmap;
            } catch (OutOfMemoryError e4) {
                oom = e4;
                Log.e(TAG, "Unable to decode file " + filePath + ". OutOfMemoryError.", oom);
                if (fileInputStream != null) {
                    fileInputStream.close();
                }
                if (kind == 3) {
                    bitmap = extractThumbnail(bitmap, TARGET_SIZE_MICRO_THUMBNAIL, TARGET_SIZE_MICRO_THUMBNAIL, OPTIONS_RECYCLE_INPUT);
                }
                return bitmap;
            }
        }
        if (kind == 3) {
            bitmap = extractThumbnail(bitmap, TARGET_SIZE_MICRO_THUMBNAIL, TARGET_SIZE_MICRO_THUMBNAIL, OPTIONS_RECYCLE_INPUT);
        }
        return bitmap;
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
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
        } catch (RuntimeException e3) {
            try {
                retriever.release();
            } catch (RuntimeException e4) {
            }
        } catch (Throwable th) {
            try {
                retriever.release();
            } catch (RuntimeException e5) {
            }
        }
        if (bitmap == null) {
            return null;
        }
        if (kind == OPTIONS_SCALE_UP) {
            int width = bitmap.getWidth();
            int height = bitmap.getHeight();
            int max = Math.max(width, height);
            if (max > Document.FLAG_VIRTUAL_DOCUMENT) {
                float scale = 512.0f / ((float) max);
                bitmap = Bitmap.createScaledBitmap(bitmap, Math.round(((float) width) * scale), Math.round(((float) height) * scale), true);
            }
        } else if (kind == 3) {
            bitmap = extractThumbnail(bitmap, TARGET_SIZE_MICRO_THUMBNAIL, TARGET_SIZE_MICRO_THUMBNAIL, OPTIONS_RECYCLE_INPUT);
        }
        return bitmap;
    }

    public static Bitmap extractThumbnail(Bitmap source, int width, int height) {
        return extractThumbnail(source, width, height, OPTIONS_NONE);
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
        return transform(matrix, source, width, height, options | OPTIONS_SCALE_UP);
    }

    private static int computeSampleSize(Options options, int minSideLength, int maxNumOfPixels) {
        int initialSize = computeInitialSampleSize(options, minSideLength, maxNumOfPixels);
        if (initialSize > 8) {
            return ((initialSize + 7) / 8) * 8;
        }
        int roundedSize = OPTIONS_SCALE_UP;
        while (roundedSize < initialSize) {
            roundedSize <<= OPTIONS_SCALE_UP;
        }
        return roundedSize;
    }

    private static int computeInitialSampleSize(Options options, int minSideLength, int maxNumOfPixels) {
        int lowerBound;
        int upperBound;
        double w = (double) options.outWidth;
        double h = (double) options.outHeight;
        if (maxNumOfPixels == UNCONSTRAINED) {
            lowerBound = OPTIONS_SCALE_UP;
        } else {
            lowerBound = (int) Math.ceil(Math.sqrt((w * h) / ((double) maxNumOfPixels)));
        }
        if (minSideLength == UNCONSTRAINED) {
            upperBound = KeymasterDefs.KM_ALGORITHM_HMAC;
        } else {
            upperBound = (int) Math.min(Math.floor(w / ((double) minSideLength)), Math.floor(h / ((double) minSideLength)));
        }
        if (upperBound < lowerBound) {
            return lowerBound;
        }
        if (maxNumOfPixels == UNCONSTRAINED && minSideLength == UNCONSTRAINED) {
            return OPTIONS_SCALE_UP;
        }
        if (minSideLength == UNCONSTRAINED) {
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
        options.inSampleSize = OPTIONS_SCALE_UP;
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFileDescriptor(fd, null, options);
        if (!(options.mCancel || options.outWidth == UNCONSTRAINED)) {
            if (options.outHeight != UNCONSTRAINED) {
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
        boolean scaleUp = (options & OPTIONS_SCALE_UP) != 0;
        boolean recycle = (options & OPTIONS_RECYCLE_INPUT) != 0;
        int deltaX = source.getWidth() - targetWidth;
        int deltaY = source.getHeight() - targetHeight;
        if (scaleUp || (deltaX >= 0 && deltaY >= 0)) {
            Bitmap b1;
            Bitmap b2;
            float bitmapWidthF = (float) source.getWidth();
            float bitmapHeightF = (float) source.getHeight();
            float scale;
            if (bitmapWidthF / bitmapHeightF > ((float) targetWidth) / ((float) targetHeight)) {
                scale = ((float) targetHeight) / bitmapHeightF;
                if (scale < 0.9f || scale > Engine.DEFAULT_VOLUME) {
                    scaler.setScale(scale, scale);
                } else {
                    scaler = null;
                }
            } else {
                scale = ((float) targetWidth) / bitmapWidthF;
                if (scale < 0.9f || scale > Engine.DEFAULT_VOLUME) {
                    scaler.setScale(scale, scale);
                } else {
                    scaler = null;
                }
            }
            if (scaler != null) {
                b1 = Bitmap.createBitmap(source, (int) OPTIONS_NONE, (int) OPTIONS_NONE, source.getWidth(), source.getHeight(), scaler, true);
            } else {
                b1 = source;
            }
            if (recycle && b1 != source) {
                source.recycle();
            }
            b2 = Bitmap.createBitmap(b1, Math.max(OPTIONS_NONE, b1.getWidth() - targetWidth) / OPTIONS_RECYCLE_INPUT, Math.max(OPTIONS_NONE, b1.getHeight() - targetHeight) / OPTIONS_RECYCLE_INPUT, targetWidth, targetHeight);
            if (b2 != b1 && (recycle || b1 != source)) {
                b1.recycle();
            }
            return b2;
        }
        b2 = Bitmap.createBitmap(targetWidth, targetHeight, Config.ARGB_8888);
        Canvas c = new Canvas(b2);
        int deltaXHalf = Math.max(OPTIONS_NONE, deltaX / OPTIONS_RECYCLE_INPUT);
        int deltaYHalf = Math.max(OPTIONS_NONE, deltaY / OPTIONS_RECYCLE_INPUT);
        Rect rect = new Rect(deltaXHalf, deltaYHalf, Math.min(targetWidth, source.getWidth()) + deltaXHalf, Math.min(targetHeight, source.getHeight()) + deltaYHalf);
        int dstX = (targetWidth - rect.width()) / OPTIONS_RECYCLE_INPUT;
        int dstY = (targetHeight - rect.height()) / OPTIONS_RECYCLE_INPUT;
        c.drawBitmap(source, rect, new Rect(dstX, dstY, targetWidth - dstX, targetHeight - dstY), null);
        if (recycle) {
            source.recycle();
        }
        c.setBitmap(null);
        return b2;
    }

    private static void createThumbnailFromEXIF(String filePath, int targetSize, int maxPixels, SizedThumbnailBitmap sizedThumbBitmap) {
        IOException ex;
        int fullThumbWidth;
        if (filePath != null) {
            Options fullOptions;
            Options exifOptions;
            int exifThumbWidth;
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
                    exifThumbWidth = OPTIONS_NONE;
                    if (thumbData != null) {
                        exifOptions.inJustDecodeBounds = true;
                        BitmapFactory.decodeByteArray(thumbData, OPTIONS_NONE, thumbData.length, exifOptions);
                        exifOptions.inSampleSize = computeSampleSize(exifOptions, targetSize, maxPixels);
                        exifThumbWidth = exifOptions.outWidth / exifOptions.inSampleSize;
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
                exifThumbWidth = OPTIONS_NONE;
                if (thumbData != null) {
                    exifOptions.inJustDecodeBounds = true;
                    BitmapFactory.decodeByteArray(thumbData, OPTIONS_NONE, thumbData.length, exifOptions);
                    exifOptions.inSampleSize = computeSampleSize(exifOptions, targetSize, maxPixels);
                    exifThumbWidth = exifOptions.outWidth / exifOptions.inSampleSize;
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
            exifThumbWidth = OPTIONS_NONE;
            if (thumbData != null) {
                exifOptions.inJustDecodeBounds = true;
                BitmapFactory.decodeByteArray(thumbData, OPTIONS_NONE, thumbData.length, exifOptions);
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
                sizedThumbBitmap.mBitmap = BitmapFactory.decodeByteArray(thumbData, OPTIONS_NONE, thumbData.length, exifOptions);
                if (sizedThumbBitmap.mBitmap != null) {
                    sizedThumbBitmap.mThumbnailData = thumbData;
                    sizedThumbBitmap.mThumbnailWidth = width;
                    sizedThumbBitmap.mThumbnailHeight = height;
                }
            }
        }
    }
}
