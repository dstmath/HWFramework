package android.rms.iaware;

import android.util.AtomicFile;
import java.io.BufferedReader;
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
        if (this.mAtomicFile == null) {
            return result;
        }
        FileInputStream stream = null;
        BufferedReader br = null;
        try {
            stream = this.mAtomicFile.openRead();
            if (stream.available() > maxSize) {
                AwareLog.e(TAG, "readFileLines file size is more than maxSize!");
                if (br != null) {
                    try {
                        br.close();
                    } catch (IOException e) {
                        AwareLog.e(TAG, "close catch IOException!");
                    }
                }
                if (stream != null) {
                    try {
                        stream.close();
                    } catch (IOException e2) {
                        AwareLog.e(TAG, "readFileLines stream catch IOException in finally!");
                    }
                }
                return result;
            }
            AwareLog.d(TAG, "readFileLines path:" + this.mAtomicFile.getBaseFile().toString());
            BufferedReader br2 = new BufferedReader(new InputStreamReader(stream, "utf-8"));
            while (true) {
                String readLine = br2.readLine();
                String tmp = readLine;
                if (readLine != null) {
                    result.add(tmp);
                } else {
                    try {
                        break;
                    } catch (IOException e3) {
                        AwareLog.e(TAG, "close catch IOException!");
                    }
                }
            }
            br2.close();
            if (stream != null) {
                try {
                    stream.close();
                } catch (IOException e4) {
                    AwareLog.e(TAG, "readFileLines stream catch IOException in finally!");
                }
            }
            AwareLog.d(TAG, "readFileLines result: " + result);
            return result;
        } catch (FileNotFoundException e5) {
            AwareLog.e(TAG, "readFileLines file not exist: " + this.mAtomicFile);
            if (br != null) {
                try {
                    br.close();
                } catch (IOException e6) {
                    AwareLog.e(TAG, "close catch IOException!");
                }
            }
            if (stream != null) {
                stream.close();
            }
        } catch (IOException e7) {
            AwareLog.e(TAG, "readFileLines catch IOException!");
            if (br != null) {
                try {
                    br.close();
                } catch (IOException e8) {
                    AwareLog.e(TAG, "close catch IOException!");
                }
            }
            if (stream != null) {
                stream.close();
            }
        } catch (Throwable th) {
            if (br != null) {
                try {
                    br.close();
                } catch (IOException e9) {
                    AwareLog.e(TAG, "close catch IOException!");
                }
            }
            if (stream != null) {
                try {
                    stream.close();
                } catch (IOException e10) {
                    AwareLog.e(TAG, "readFileLines stream catch IOException in finally!");
                }
            }
            throw th;
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
                    buf.append("\n");
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
                AwareLog.d(TAG, "writeFileLine line: " + line);
                fos = this.mAtomicFile.startWrite();
                line.append("\n");
                fos.write(line.toString().getBytes("utf-8"));
                this.mAtomicFile.finishWrite(fos);
            } catch (IOException e) {
                AwareLog.e(TAG, "writeFileLine catch IOException");
                this.mAtomicFile.failWrite(fos);
            }
        }
    }

    public void deleteFile() {
        if (this.mAtomicFile != null && this.mAtomicFile.exists()) {
            AwareLog.i(TAG, "delete file path:" + this.mAtomicFile.getBaseFile().toString());
            this.mAtomicFile.delete();
        }
    }
}
