package ohos.agp.image;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Optional;
import ohos.aafwk.utils.log.LogDomain;
import ohos.app.Context;
import ohos.global.resource.NotExistException;
import ohos.global.resource.RawFileEntry;
import ohos.global.resource.Resource;
import ohos.global.resource.ResourceManager;
import ohos.global.resource.WrongTypeException;
import ohos.hiviewdfx.HiLog;
import ohos.hiviewdfx.HiLogLabel;
import ohos.media.image.ImageSource;
import ohos.media.image.PixelMap;
import ohos.media.image.common.PixelFormat;
import ohos.media.image.common.Rect;
import ohos.media.image.common.Size;

public class PixelMapFactory {
    private static final HiLogLabel TAG = new HiLogLabel(3, (int) LogDomain.END, "PixelMapFactory");

    public static PixelMap createFromPath(String str) {
        if (!new File(str).exists()) {
            return null;
        }
        ImageSource.SourceOptions sourceOptions = new ImageSource.SourceOptions();
        sourceOptions.formatHint = "image/png";
        ImageSource create = ImageSource.create(str, sourceOptions);
        if (create == null) {
            return null;
        }
        ImageSource.DecodingOptions decodingOptions = new ImageSource.DecodingOptions();
        decodingOptions.desiredSize = new Size(0, 0);
        decodingOptions.desiredRegion = new Rect(0, 0, 0, 0);
        decodingOptions.desiredPixelFormat = PixelFormat.RGBA_8888;
        return create.createPixelmap(decodingOptions);
    }

    /* JADX WARNING: Code restructure failed: missing block: B:34:0x00be, code lost:
        r0 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:35:0x00bf, code lost:
        if (r4 != null) goto L_0x00c1;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:37:?, code lost:
        r4.close();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:38:0x00c5, code lost:
        r4 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:39:0x00c6, code lost:
        r5.addSuppressed(r4);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:40:0x00c9, code lost:
        throw r0;
     */
    public static Optional<PixelMap> createFromResourceId(Context context, int i) {
        HiLog.debug(TAG, "createFromResourceId using resId: %{public}d", new Object[]{Integer.valueOf(i)});
        if (context == null) {
            HiLog.error(TAG, "context is null!", new Object[0]);
            return Optional.empty();
        }
        ResourceManager resourceManager = context.getResourceManager();
        if (resourceManager == null) {
            HiLog.error(TAG, "Fail to get resource manager!", new Object[0]);
            return Optional.empty();
        }
        try {
            String mediaPath = resourceManager.getMediaPath(i);
            if (mediaPath == null) {
                HiLog.error(TAG, "resourcePath is null", new Object[0]);
                return Optional.empty();
            }
            RawFileEntry rawFileEntry = resourceManager.getRawFileEntry(mediaPath);
            if (rawFileEntry == null) {
                HiLog.error(TAG, "rawFileEntry is null", new Object[0]);
                return Optional.empty();
            }
            try {
                Resource openRawFile = rawFileEntry.openRawFile();
                if (openRawFile == null) {
                    HiLog.error(TAG, "resource is null", new Object[0]);
                    Optional<PixelMap> empty = Optional.empty();
                    if (openRawFile != null) {
                        openRawFile.close();
                    }
                    return empty;
                }
                ImageSource.SourceOptions sourceOptions = new ImageSource.SourceOptions();
                sourceOptions.formatHint = "image/png";
                ImageSource create = ImageSource.create(openRawFile, sourceOptions);
                if (create == null) {
                    HiLog.error(TAG, "create ImageSource is null", new Object[0]);
                    Optional<PixelMap> empty2 = Optional.empty();
                    openRawFile.close();
                    return empty2;
                }
                ImageSource.DecodingOptions decodingOptions = new ImageSource.DecodingOptions();
                decodingOptions.desiredSize = new Size(0, 0);
                decodingOptions.desiredRegion = new Rect(0, 0, 0, 0);
                decodingOptions.desiredPixelFormat = PixelFormat.RGBA_8888;
                Optional<PixelMap> ofNullable = Optional.ofNullable(create.createPixelmap(decodingOptions));
                openRawFile.close();
                return ofNullable;
            } catch (IOException unused) {
                HiLog.error(TAG, "Fail to create image using resource id", new Object[0]);
                return Optional.empty();
            }
        } catch (IOException | NotExistException | WrongTypeException unused2) {
            HiLog.error(TAG, "can't find image resource id", new Object[0]);
            return Optional.empty();
        }
    }

    public static ImageSource createGifFromPath(String str) {
        if (!new File(str).exists()) {
            return null;
        }
        ImageSource.SourceOptions sourceOptions = new ImageSource.SourceOptions();
        sourceOptions.formatHint = "image/gif";
        return ImageSource.create(str, sourceOptions);
    }

    public static ImageSource createGifFromInputStream(InputStream inputStream) {
        ImageSource.SourceOptions sourceOptions = new ImageSource.SourceOptions();
        sourceOptions.formatHint = "image/gif";
        return ImageSource.create(inputStream, sourceOptions);
    }

    public static int getGifDuration(ImageSource imageSource) {
        if (imageSource == null || imageSource.getSourceInfo() == null) {
            return 0;
        }
        int i = 0;
        for (int i2 = 0; i2 < imageSource.getSourceInfo().topLevelImageNum; i2++) {
            i += imageSource.getImagePropertyInt(i2, "GIFDelayTime", 0);
        }
        return i;
    }

    public static int getGifWidth(ImageSource imageSource) {
        if (imageSource == null || imageSource.getSourceInfo() == null) {
            return 0;
        }
        return imageSource.getImageInfo().size.width;
    }

    public static int getGifHeight(ImageSource imageSource) {
        if (imageSource == null || imageSource.getSourceInfo() == null) {
            return 0;
        }
        return imageSource.getImageInfo().size.height;
    }

    public static PixelMap getGifIndexPixelMap(ImageSource imageSource, long j, long j2) {
        boolean z;
        if (imageSource == null) {
            return null;
        }
        int i = imageSource.getSourceInfo().topLevelImageNum;
        long j3 = 0;
        for (int i2 = 0; i2 < i; i2++) {
            j3 += (long) imageSource.getImagePropertyInt(i2, "GIFDelayTime", 0);
        }
        if (j3 == 0) {
            return null;
        }
        int i3 = (int) ((j - j2) % j3);
        int i4 = 0;
        int i5 = 0;
        while (true) {
            if (i4 >= i) {
                z = false;
                i4 = 0;
                break;
            }
            i5 += imageSource.getImagePropertyInt(i4, "GIFDelayTime", 0);
            if (i5 >= i3) {
                z = true;
                break;
            }
            i4++;
        }
        if (!z) {
            i4 = i - 1;
        }
        ImageSource.DecodingOptions decodingOptions = new ImageSource.DecodingOptions();
        decodingOptions.desiredSize = new Size(0, 0);
        decodingOptions.desiredRegion = new Rect(0, 0, 0, 0);
        decodingOptions.desiredPixelFormat = PixelFormat.RGBA_8888;
        return imageSource.createPixelmap(i4, decodingOptions);
    }

    public static PixelMap createGifPixelmap(int i, ImageSource imageSource) {
        if (imageSource == null) {
            return null;
        }
        ImageSource.DecodingOptions decodingOptions = new ImageSource.DecodingOptions();
        decodingOptions.desiredSize = new Size(0, 0);
        decodingOptions.desiredRegion = new Rect(0, 0, 0, 0);
        decodingOptions.desiredPixelFormat = PixelFormat.RGBA_8888;
        return imageSource.createPixelmap(i, decodingOptions);
    }
}
