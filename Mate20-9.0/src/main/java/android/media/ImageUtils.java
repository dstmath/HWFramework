package android.media;

import android.graphics.ImageFormat;
import android.media.Image;
import android.util.Size;
import java.nio.ByteBuffer;
import libcore.io.Memory;

class ImageUtils {
    ImageUtils() {
    }

    public static int getNumPlanesForFormat(int format) {
        switch (format) {
            case 1:
            case 2:
            case 3:
            case 4:
            case 20:
            case 32:
            case 36:
            case 37:
            case 38:
            case 256:
            case 257:
            case 4098:
            case ImageFormat.Y8:
            case ImageFormat.Y16:
            case ImageFormat.DEPTH16:
                return 1;
            case 16:
                return 2;
            case 17:
            case 35:
            case ImageFormat.YV12:
                return 3;
            case 34:
                return 0;
            default:
                throw new UnsupportedOperationException(String.format("Invalid format specified %d", new Object[]{Integer.valueOf(format)}));
        }
    }

    public static void imageCopy(Image src, Image dst) {
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
                                if (row == effectivePlaneSize.getHeight() - 1) {
                                    int remainingBytes = srcBuffer.remaining() - srcOffset;
                                    if (srcByteCount > remainingBytes) {
                                        srcByteCount = remainingBytes;
                                    }
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
        switch (format) {
            case 1:
            case 2:
                estimatedBytePerPixel = 4.0d;
                break;
            case 3:
                estimatedBytePerPixel = 3.0d;
                break;
            case 4:
            case 16:
            case 20:
            case 32:
            case 36:
            case 4098:
            case ImageFormat.Y16:
            case ImageFormat.DEPTH16:
                estimatedBytePerPixel = 2.0d;
                break;
            case 17:
            case 34:
            case 35:
            case 38:
            case ImageFormat.YV12:
                estimatedBytePerPixel = 1.5d;
                break;
            case 37:
                estimatedBytePerPixel = 1.25d;
                break;
            case 256:
            case 257:
                estimatedBytePerPixel = 0.3d;
                break;
            case ImageFormat.Y8:
                estimatedBytePerPixel = 1.0d;
                break;
            default:
                throw new UnsupportedOperationException(String.format("Invalid format specified %d", new Object[]{Integer.valueOf(format)}));
        }
        return (int) (((double) (width * height)) * estimatedBytePerPixel * ((double) numImages));
    }

    private static Size getEffectivePlaneSizeForImage(Image image, int planeIdx) {
        switch (image.getFormat()) {
            case 1:
            case 2:
            case 3:
            case 4:
            case 20:
            case 32:
            case 37:
            case 38:
            case 256:
            case 4098:
            case ImageFormat.Y8:
            case ImageFormat.Y16:
                return new Size(image.getWidth(), image.getHeight());
            case 16:
                if (planeIdx == 0) {
                    return new Size(image.getWidth(), image.getHeight());
                }
                return new Size(image.getWidth(), image.getHeight() / 2);
            case 17:
            case 35:
            case ImageFormat.YV12:
                if (planeIdx == 0) {
                    return new Size(image.getWidth(), image.getHeight());
                }
                return new Size(image.getWidth() / 2, image.getHeight() / 2);
            case 34:
                return new Size(0, 0);
            default:
                throw new UnsupportedOperationException(String.format("Invalid image format %d", new Object[]{Integer.valueOf(image.getFormat())}));
        }
    }

    private static void directByteBufferCopy(ByteBuffer srcBuffer, int srcOffset, ByteBuffer dstBuffer, int dstOffset, int srcByteCount) {
        Memory.memmove(dstBuffer, dstOffset, srcBuffer, srcOffset, (long) srcByteCount);
    }
}
