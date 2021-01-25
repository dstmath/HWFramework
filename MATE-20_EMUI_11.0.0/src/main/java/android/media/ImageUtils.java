package android.media;

import android.graphics.ImageFormat;
import android.media.Image;
import android.util.Size;
import java.nio.ByteBuffer;
import libcore.io.Memory;

/* access modifiers changed from: package-private */
public class ImageUtils {
    ImageUtils() {
    }

    public static int getNumPlanesForFormat(int format) {
        if (!(format == 1 || format == 2 || format == 3 || format == 4)) {
            if (format == 16) {
                return 2;
            }
            if (format != 17) {
                if (!(format == 256 || format == 257)) {
                    switch (format) {
                        case 20:
                        case 32:
                        case 4098:
                        case ImageFormat.Y8 /* 538982489 */:
                        case ImageFormat.Y16 /* 540422489 */:
                        case ImageFormat.DEPTH16 /* 1144402265 */:
                        case ImageFormat.HEIC /* 1212500294 */:
                        case ImageFormat.DEPTH_JPEG /* 1768253795 */:
                            break;
                        case ImageFormat.YV12 /* 842094169 */:
                            break;
                        default:
                            switch (format) {
                                case 34:
                                    return 0;
                                case 35:
                                    break;
                                case 36:
                                case 37:
                                case 38:
                                    break;
                                default:
                                    throw new UnsupportedOperationException(String.format("Invalid format specified %d", Integer.valueOf(format)));
                            }
                    }
                }
            }
            return 3;
        }
        return 1;
    }

    public static void imageCopy(Image src, Image dst) {
        int remainingBytes;
        Image image = src;
        if (image == null || dst == null) {
            throw new IllegalArgumentException("Images should be non-null");
        } else if (src.getFormat() != dst.getFormat()) {
            throw new IllegalArgumentException("Src and dst images should have the same format");
        } else if (src.getFormat() == 34 || dst.getFormat() == 34) {
            throw new IllegalArgumentException("PRIVATE format images are not copyable");
        } else if (src.getFormat() == 36) {
            throw new IllegalArgumentException("Copy of RAW_OPAQUE format has not been implemented");
        } else if (src.getFormat() == 4098) {
            throw new IllegalArgumentException("Copy of RAW_DEPTH format has not been implemented");
        } else if (dst.getOwner() instanceof ImageWriter) {
            Size srcSize = new Size(src.getWidth(), src.getHeight());
            Size dstSize = new Size(dst.getWidth(), dst.getHeight());
            if (srcSize.equals(dstSize)) {
                Image.Plane[] srcPlanes = src.getPlanes();
                Image.Plane[] dstPlanes = dst.getPlanes();
                int i = 0;
                while (i < srcPlanes.length) {
                    int srcRowStride = srcPlanes[i].getRowStride();
                    int dstRowStride = dstPlanes[i].getRowStride();
                    ByteBuffer srcBuffer = srcPlanes[i].getBuffer();
                    ByteBuffer dstBuffer = dstPlanes[i].getBuffer();
                    if (!srcBuffer.isDirect() || !dstBuffer.isDirect()) {
                        throw new IllegalArgumentException("Source and destination ByteBuffers must be direct byteBuffer!");
                    } else if (srcPlanes[i].getPixelStride() == dstPlanes[i].getPixelStride()) {
                        int srcPos = srcBuffer.position();
                        srcBuffer.rewind();
                        dstBuffer.rewind();
                        if (srcRowStride == dstRowStride) {
                            dstBuffer.put(srcBuffer);
                        } else {
                            int srcOffset = srcBuffer.position();
                            int dstOffset = dstBuffer.position();
                            Size effectivePlaneSize = getEffectivePlaneSizeForImage(image, i);
                            int srcByteCount = effectivePlaneSize.getWidth() * srcPlanes[i].getPixelStride();
                            for (int row = 0; row < effectivePlaneSize.getHeight(); row++) {
                                if (row == effectivePlaneSize.getHeight() - 1 && srcByteCount > (remainingBytes = srcBuffer.remaining() - srcOffset)) {
                                    srcByteCount = remainingBytes;
                                }
                                directByteBufferCopy(srcBuffer, srcOffset, dstBuffer, dstOffset, srcByteCount);
                                srcOffset += srcRowStride;
                                dstOffset += dstRowStride;
                            }
                        }
                        srcBuffer.position(srcPos);
                        dstBuffer.rewind();
                        i++;
                        image = src;
                    } else {
                        throw new IllegalArgumentException("Source plane image pixel stride " + srcPlanes[i].getPixelStride() + " must be same as destination image pixel stride " + dstPlanes[i].getPixelStride());
                    }
                }
                return;
            }
            throw new IllegalArgumentException("source image size " + srcSize + " is different with destination image size " + dstSize);
        } else {
            throw new IllegalArgumentException("Destination image is not from ImageWriter. Only the images from ImageWriter are writable");
        }
    }

    public static int getEstimatedNativeAllocBytes(int width, int height, int format, int numImages) {
        double estimatedBytePerPixel;
        if (format == 1 || format == 2) {
            estimatedBytePerPixel = 4.0d;
        } else if (format != 3) {
            if (!(format == 4 || format == 16)) {
                if (format != 17) {
                    if (!(format == 256 || format == 257)) {
                        switch (format) {
                            case 20:
                            case 32:
                            case 4098:
                            case ImageFormat.Y16 /* 540422489 */:
                            case ImageFormat.DEPTH16 /* 1144402265 */:
                                break;
                            case ImageFormat.Y8 /* 538982489 */:
                                estimatedBytePerPixel = 1.0d;
                                break;
                            case ImageFormat.YV12 /* 842094169 */:
                                break;
                            case ImageFormat.HEIC /* 1212500294 */:
                            case ImageFormat.DEPTH_JPEG /* 1768253795 */:
                                break;
                            default:
                                switch (format) {
                                    case 34:
                                    case 35:
                                    case 38:
                                        break;
                                    case 36:
                                        break;
                                    case 37:
                                        estimatedBytePerPixel = 1.25d;
                                        break;
                                    default:
                                        throw new UnsupportedOperationException(String.format("Invalid format specified %d", Integer.valueOf(format)));
                                }
                        }
                    }
                    estimatedBytePerPixel = 0.3d;
                }
                estimatedBytePerPixel = 1.5d;
            }
            estimatedBytePerPixel = 2.0d;
        } else {
            estimatedBytePerPixel = 3.0d;
        }
        return (int) (((double) (width * height)) * estimatedBytePerPixel * ((double) numImages));
    }

    private static Size getEffectivePlaneSizeForImage(Image image, int planeIdx) {
        int format = image.getFormat();
        if (!(format == 1 || format == 2 || format == 3 || format == 4)) {
            if (format != 16) {
                if (format != 17) {
                    if (format == 34) {
                        return new Size(0, 0);
                    }
                    if (format != 35) {
                        if (!(format == 37 || format == 38)) {
                            switch (format) {
                                case 20:
                                case 32:
                                case 256:
                                case 4098:
                                case ImageFormat.Y8 /* 538982489 */:
                                case ImageFormat.Y16 /* 540422489 */:
                                case ImageFormat.HEIC /* 1212500294 */:
                                    break;
                                case ImageFormat.YV12 /* 842094169 */:
                                    break;
                                default:
                                    throw new UnsupportedOperationException(String.format("Invalid image format %d", Integer.valueOf(image.getFormat())));
                            }
                        }
                    }
                }
                if (planeIdx == 0) {
                    return new Size(image.getWidth(), image.getHeight());
                }
                return new Size(image.getWidth() / 2, image.getHeight() / 2);
            } else if (planeIdx == 0) {
                return new Size(image.getWidth(), image.getHeight());
            } else {
                return new Size(image.getWidth(), image.getHeight() / 2);
            }
        }
        return new Size(image.getWidth(), image.getHeight());
    }

    private static void directByteBufferCopy(ByteBuffer srcBuffer, int srcOffset, ByteBuffer dstBuffer, int dstOffset, int srcByteCount) {
        Memory.memmove(dstBuffer, dstOffset, srcBuffer, srcOffset, (long) srcByteCount);
    }
}
