package android.rms.iaware;

import android.util.AtomicFile;
import java.io.BufferedReader;
import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class AtomicFileUtils {
    private static final String TAG = "AtomicFileUtils";
    private AtomicFile mAtomicFile;

    public AtomicFileUtils(File file) {
        if (file != null) {
            this.mAtomicFile = new AtomicFile(file);
        }
    }

    public List<String> readFileLines(int maxSize) {
        List<String> result = new ArrayList<>();
        AtomicFile atomicFile = this.mAtomicFile;
        if (atomicFile == null) {
            return result;
        }
        FileInputStream stream = null;
        BufferedReader br = null;
        try {
            stream = atomicFile.openRead();
            if (stream.available() > maxSize) {
                AwareLog.e(TAG, "readFileLines file size is more than maxSize!");
                closeStream(null);
                closeStream(stream);
                return result;
            }
            AwareLog.d(TAG, "readFileLines path:" + this.mAtomicFile.getBaseFile().toString());
            br = new BufferedReader(new InputStreamReader(stream, "utf-8"));
            while (true) {
                String tmp = br.readLine();
                if (tmp == null) {
                    break;
                }
                result.add(tmp);
            }
            closeStream(br);
            closeStream(stream);
            AwareLog.d(TAG, "readFileLines result: " + result);
            return result;
        } catch (FileNotFoundException e) {
            AwareLog.e(TAG, "readFileLines file not exist: " + this.mAtomicFile);
        } catch (IOException e2) {
            AwareLog.e(TAG, "readFileLines catch IOException!");
        } catch (Throwable th) {
            closeStream(null);
            closeStream(null);
            throw th;
        }
    }

    private void closeStream(Closeable io) {
        if (io != null) {
            try {
                io.close();
            } catch (IOException e) {
                AwareLog.e(TAG, "close exception!");
            }
        }
    }

    public void writeFileLines(List<String> lines) {
        if (this.mAtomicFile != null) {
            try {
                AwareLog.d(TAG, "writeFileLines lines: " + lines);
                FileOutputStream fos = this.mAtomicFile.startWrite();
                StringBuffer buf = new StringBuffer();
                for (String line : lines) {
                    buf.append(line);
                    buf.append(System.lineSeparator());
                }
                fos.write(buf.toString().getBytes("utf-8"));
                this.mAtomicFile.finishWrite(fos);
            } catch (IOException e) {
                AwareLog.e(TAG, "writeFileLines catch IOException");
                this.mAtomicFile.failWrite(null);
            }
        }
    }

    public void writeFileLine(StringBuilder line) {
        if (this.mAtomicFile != null && line != null) {
            FileOutputStream fos = null;
            try {
                AwareLog.d(TAG, "writeFileLine line: " + ((Object) line));
                fos = this.mAtomicFile.startWrite();
                line.append(System.lineSeparator());
                fos.write(line.toString().getBytes("utf-8"));
                this.mAtomicFile.finishWrite(fos);
            } catch (IOException e) {
                AwareLog.e(TAG, "writeFileLine catch IOException");
                this.mAtomicFile.failWrite(fos);
            }
        }
    }

    public void deleteFile() {
        AtomicFile atomicFile = this.mAtomicFile;
        if (atomicFile != null && atomicFile.exists()) {
            AwareLog.i(TAG, "delete file path:" + this.mAtomicFile.getBaseFile().toString());
            this.mAtomicFile.delete();
        }
    }
}
