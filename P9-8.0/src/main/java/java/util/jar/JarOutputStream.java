package java.util.jar;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class JarOutputStream extends ZipOutputStream {
    private static final int JAR_MAGIC = 51966;
    private boolean firstEntry = true;

    public JarOutputStream(OutputStream out, Manifest man) throws IOException {
        super(out);
        if (man == null) {
            throw new NullPointerException("man");
        }
        putNextEntry(new ZipEntry(JarFile.MANIFEST_NAME));
        man.write(new BufferedOutputStream(this));
        closeEntry();
    }

    public JarOutputStream(OutputStream out) throws IOException {
        super(out);
    }

    public void putNextEntry(ZipEntry ze) throws IOException {
        if (this.firstEntry) {
            byte[] edata = ze.getExtra();
            if (edata == null || (hasMagic(edata) ^ 1) != 0) {
                if (edata == null) {
                    edata = new byte[4];
                } else {
                    byte[] tmp = new byte[(edata.length + 4)];
                    System.arraycopy(edata, 0, tmp, 4, edata.length);
                    edata = tmp;
                }
                set16(edata, 0, JAR_MAGIC);
                set16(edata, 2, 0);
                ze.setExtra(edata);
            }
            this.firstEntry = false;
        }
        super.putNextEntry(ze);
    }

    private static boolean hasMagic(byte[] edata) {
        int i = 0;
        while (i < edata.length) {
            try {
                if (get16(edata, i) == JAR_MAGIC) {
                    return true;
                }
                i += get16(edata, i + 2) + 4;
            } catch (ArrayIndexOutOfBoundsException e) {
            }
        }
        return false;
    }

    private static int get16(byte[] b, int off) {
        return Byte.toUnsignedInt(b[off]) | (Byte.toUnsignedInt(b[off + 1]) << 8);
    }

    private static void set16(byte[] b, int off, int value) {
        b[off + 0] = (byte) value;
        b[off + 1] = (byte) (value >> 8);
    }
}
