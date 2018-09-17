package dalvik.system.profiler;

import android.icu.text.PluralRules;
import java.io.BufferedInputStream;
import java.io.Closeable;
import java.io.DataInputStream;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

public final class HprofBinaryToAscii {
    public static void main(String[] args) {
        System.exit(convert(args) ? 0 : 1);
    }

    private static boolean convert(String[] args) {
        if (args.length != 1) {
            usage("binary hprof file argument expected");
            return false;
        }
        File file = new File(args[0]);
        if (!file.exists()) {
            usage("file " + file + " does not exist");
            return false;
        } else if (startsWithMagic(file)) {
            try {
                return write(readHprof(file));
            } catch (IOException e) {
                System.out.println("Problem reading binary hprof data from " + file + PluralRules.KEYWORD_RULE_SEPARATOR + e.getMessage());
                return false;
            }
        } else {
            try {
                return write(readSnapshot(file));
            } catch (IOException e2) {
                System.out.println("Problem reading snapshot containing binary hprof data from " + file + PluralRules.KEYWORD_RULE_SEPARATOR + e2.getMessage());
                return false;
            }
        }
    }

    private static boolean startsWithMagic(File file) {
        Throwable th;
        boolean z = false;
        DataInputStream inputStream = null;
        try {
            DataInputStream inputStream2 = new DataInputStream(new BufferedInputStream(new FileInputStream(file)));
            try {
                if (BinaryHprof.readMagic(inputStream2) != null) {
                    z = true;
                }
                closeQuietly(inputStream2);
                return z;
            } catch (IOException e) {
                inputStream = inputStream2;
            } catch (Throwable th2) {
                th = th2;
                inputStream = inputStream2;
                closeQuietly(inputStream);
                throw th;
            }
        } catch (IOException e2) {
            closeQuietly(inputStream);
            return false;
        } catch (Throwable th3) {
            th = th3;
            closeQuietly(inputStream);
            throw th;
        }
    }

    private static HprofData readHprof(File file) throws IOException {
        Throwable th;
        InputStream inputStream = null;
        try {
            InputStream inputStream2 = new BufferedInputStream(new FileInputStream(file));
            try {
                HprofData read = read(inputStream2);
                closeQuietly(inputStream2);
                return read;
            } catch (Throwable th2) {
                th = th2;
                inputStream = inputStream2;
                closeQuietly(inputStream);
                throw th;
            }
        } catch (Throwable th3) {
            th = th3;
            closeQuietly(inputStream);
            throw th;
        }
    }

    private static HprofData readSnapshot(File file) throws IOException {
        Throwable th;
        Closeable closeable = null;
        try {
            InputStream inputStream = new BufferedInputStream(new FileInputStream(file));
            while (true) {
                try {
                    int ch = inputStream.read();
                    if (ch == -1) {
                        throw new EOFException("Could not find expected header");
                    } else if (ch == 10 && inputStream.read() == 10) {
                        HprofData read = read(inputStream);
                        closeQuietly(inputStream);
                        return read;
                    }
                } catch (Throwable th2) {
                    th = th2;
                    closeable = inputStream;
                    closeQuietly(closeable);
                    throw th;
                }
            }
        } catch (Throwable th3) {
            th = th3;
            closeQuietly(closeable);
            throw th;
        }
    }

    private static HprofData read(InputStream inputStream) throws IOException {
        BinaryHprofReader reader = new BinaryHprofReader(inputStream);
        reader.setStrict(false);
        reader.read();
        return reader.getHprofData();
    }

    private static void closeQuietly(Closeable c) {
        if (c != null) {
            try {
                c.close();
            } catch (IOException e) {
            }
        }
    }

    private static boolean write(HprofData hprofData) {
        try {
            AsciiHprofWriter.write(hprofData, System.out);
            return true;
        } catch (IOException e) {
            System.out.println("Problem writing ASCII hprof data: " + e.getMessage());
            return false;
        }
    }

    private static void usage(String error) {
        System.out.print("ERROR: ");
        System.out.println(error);
        System.out.println();
        System.out.println("usage: HprofBinaryToAscii <binary-hprof-file>");
        System.out.println();
        System.out.println("Reads a binary hprof file and print it in ASCII format");
    }
}
