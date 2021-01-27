package ohos.media.image.exifadapter;

import android.media.ExifInterface;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileDescriptor;
import java.io.IOException;
import java.io.InputStream;

public class ExifAdapter {
    private final ExifInterface exifInterface;

    public ExifAdapter(String str) throws IOException {
        this.exifInterface = new ExifInterface(str);
    }

    public ExifAdapter(InputStream inputStream) throws IOException {
        this.exifInterface = new ExifInterface(inputStream);
    }

    public ExifAdapter(byte[] bArr, int i, int i2) throws IOException {
        this.exifInterface = new ExifInterface(new ByteArrayInputStream(bArr, i, i2));
    }

    public ExifAdapter(File file) throws IOException {
        this.exifInterface = new ExifInterface(file);
    }

    public ExifAdapter(FileDescriptor fileDescriptor) throws IOException {
        this.exifInterface = new ExifInterface(fileDescriptor);
    }

    public String getImagePropertyString(String str) {
        return this.exifInterface.getAttribute(str);
    }

    public int getImagePropertyInt(String str, int i) {
        return this.exifInterface.getAttributeInt(str, i);
    }

    public byte[] getThumbnailBytes() {
        return this.exifInterface.getThumbnailBytes();
    }

    public byte[] getThumbnail() {
        return this.exifInterface.getThumbnail();
    }

    public double getImagePropertyDouble(String str, double d) {
        return this.exifInterface.getAttributeDouble(str, d);
    }

    public void setImageProperty(String str, String str2) {
        this.exifInterface.setAttribute(str, str2);
    }

    public void saveAttributes() throws IOException {
        try {
            this.exifInterface.saveAttributes();
        } catch (IOException unused) {
            throw new IOException("only supports JPEG formate for changing and saving Exif properties");
        }
    }
}
