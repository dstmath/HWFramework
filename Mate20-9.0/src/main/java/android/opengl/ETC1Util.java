package android.opengl;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class ETC1Util {

    public static class ETC1Texture {
        private ByteBuffer mData;
        private int mHeight;
        private int mWidth;

        public ETC1Texture(int width, int height, ByteBuffer data) {
            this.mWidth = width;
            this.mHeight = height;
            this.mData = data;
        }

        public int getWidth() {
            return this.mWidth;
        }

        public int getHeight() {
            return this.mHeight;
        }

        public ByteBuffer getData() {
            return this.mData;
        }
    }

    public static void loadTexture(int target, int level, int border, int fallbackFormat, int fallbackType, InputStream input) throws IOException {
        loadTexture(target, level, border, fallbackFormat, fallbackType, createTexture(input));
    }

    public static void loadTexture(int target, int level, int border, int fallbackFormat, int fallbackType, ETC1Texture texture) {
        int i = fallbackType;
        int i2 = fallbackFormat;
        if (i2 != 6407) {
            throw new IllegalArgumentException("fallbackFormat must be GL_RGB");
        } else if (i == 33635 || i == 5121) {
            int width = texture.getWidth();
            int height = texture.getHeight();
            ByteBuffer data = texture.getData();
            if (isETC1Supported()) {
                GLES10.glCompressedTexImage2D(target, level, 36196, width, height, border, data.remaining(), data);
                ByteBuffer byteBuffer = data;
                return;
            }
            int pixelSize = i != 5121 ? 2 : 3;
            int stride = pixelSize * width;
            Buffer decodedData = ByteBuffer.allocateDirect(stride * height).order(ByteOrder.nativeOrder());
            ETC1.decodeImage(data, decodedData, width, height, pixelSize, stride);
            ByteBuffer byteBuffer2 = data;
            GLES10.glTexImage2D(target, level, i2, width, height, border, i2, i, decodedData);
        } else {
            throw new IllegalArgumentException("Unsupported fallbackType");
        }
    }

    public static boolean isETC1Supported() {
        int[] results = new int[20];
        GLES10.glGetIntegerv(34466, results, 0);
        int numFormats = results[0];
        if (numFormats > results.length) {
            results = new int[numFormats];
        }
        GLES10.glGetIntegerv(34467, results, 0);
        for (int i = 0; i < numFormats; i++) {
            if (results[i] == 36196) {
                return true;
            }
        }
        return false;
    }

    public static ETC1Texture createTexture(InputStream input) throws IOException {
        byte[] ioBuffer = new byte[4096];
        if (input.read(ioBuffer, 0, 16) == 16) {
            ByteBuffer headerBuffer = ByteBuffer.allocateDirect(16).order(ByteOrder.nativeOrder());
            headerBuffer.put(ioBuffer, 0, 16).position(0);
            if (ETC1.isValid(headerBuffer)) {
                int width = ETC1.getWidth(headerBuffer);
                int height = ETC1.getHeight(headerBuffer);
                int encodedSize = ETC1.getEncodedDataSize(width, height);
                ByteBuffer dataBuffer = ByteBuffer.allocateDirect(encodedSize).order(ByteOrder.nativeOrder());
                int i = 0;
                while (i < encodedSize) {
                    int chunkSize = Math.min(ioBuffer.length, encodedSize - i);
                    if (input.read(ioBuffer, 0, chunkSize) == chunkSize) {
                        dataBuffer.put(ioBuffer, 0, chunkSize);
                        i += chunkSize;
                    } else {
                        throw new IOException("Unable to read PKM file data.");
                    }
                }
                dataBuffer.position(0);
                return new ETC1Texture(width, height, dataBuffer);
            }
            throw new IOException("Not a PKM file.");
        }
        throw new IOException("Unable to read PKM file header.");
    }

    public static ETC1Texture compressTexture(Buffer input, int width, int height, int pixelSize, int stride) {
        ByteBuffer compressedImage = ByteBuffer.allocateDirect(ETC1.getEncodedDataSize(width, height)).order(ByteOrder.nativeOrder());
        ETC1.encodeImage(input, width, height, pixelSize, stride, compressedImage);
        return new ETC1Texture(width, height, compressedImage);
    }

    public static void writeTexture(ETC1Texture texture, OutputStream output) throws IOException {
        ByteBuffer dataBuffer = texture.getData();
        int originalPosition = dataBuffer.position();
        try {
            int width = texture.getWidth();
            int height = texture.getHeight();
            ByteBuffer header = ByteBuffer.allocateDirect(16).order(ByteOrder.nativeOrder());
            ETC1.formatHeader(header, width, height);
            byte[] ioBuffer = new byte[4096];
            header.get(ioBuffer, 0, 16);
            output.write(ioBuffer, 0, 16);
            int encodedSize = ETC1.getEncodedDataSize(width, height);
            int i = 0;
            while (i < encodedSize) {
                int chunkSize = Math.min(ioBuffer.length, encodedSize - i);
                dataBuffer.get(ioBuffer, 0, chunkSize);
                output.write(ioBuffer, 0, chunkSize);
                i += chunkSize;
            }
        } finally {
            dataBuffer.position(originalPosition);
        }
    }
}
