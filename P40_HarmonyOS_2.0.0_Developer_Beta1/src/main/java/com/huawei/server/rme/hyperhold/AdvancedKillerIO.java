package com.huawei.server.rme.hyperhold;

import android.os.FileUtils;
import android.util.Slog;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

public class AdvancedKillerIO {
    private final int PRIVILEGE = 504;

    public interface FileLineProcessor<T> {
        void processFileLine(String str, T t);
    }

    public interface StructEncoder<T> {
        byte[] getBytes(T t);
    }

    /* JADX WARNING: Code restructure failed: missing block: B:17:0x0035, code lost:
        r4 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:18:0x0036, code lost:
        $closeResource(r3, r1);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:19:0x0039, code lost:
        throw r4;
     */
    public <T> void readDataFile(String filename, T dataStructure, FileLineProcessor<T> lineProcessor) {
        File dataFile = new File(filename);
        if (!dataFile.exists() || !dataFile.canRead() || !dataFile.isFile()) {
            errorPrint("AkRead", "Can't access file: " + filename);
            return;
        }
        try {
            BufferedReader br = new BufferedReader(new FileReader(dataFile));
            while (true) {
                String line = br.readLine();
                if (line != null) {
                    lineProcessor.processFileLine(line, dataStructure);
                } else {
                    $closeResource(null, br);
                    return;
                }
            }
        } catch (IOException ex) {
            errorPrint("AkRead", "Internal exception for: " + filename + " " + ex);
        }
    }

    private static /* synthetic */ void $closeResource(Throwable x0, AutoCloseable x1) {
        if (x0 != null) {
            try {
                x1.close();
            } catch (Throwable th) {
                x0.addSuppressed(th);
            }
        } else {
            x1.close();
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:33:0x0098, code lost:
        r0 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:34:0x0099, code lost:
        $closeResource(r0, r0);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:35:0x009e, code lost:
        throw r0;
     */
    /* JADX WARNING: Removed duplicated region for block: B:44:0x00be A[SYNTHETIC, Splitter:B:44:0x00be] */
    /* JADX WARNING: Removed duplicated region for block: B:46:0x00d0 A[Catch:{ IOException -> 0x00d8 }] */
    /* JADX WARNING: Removed duplicated region for block: B:53:0x00ec A[SYNTHETIC, Splitter:B:53:0x00ec] */
    /* JADX WARNING: Removed duplicated region for block: B:55:0x00fe A[Catch:{ IOException -> 0x0106 }] */
    public <T> void writeDataFile(String filename, T dataStructure, StructEncoder<T> structEncoder) {
        StringBuilder sb;
        StringBuilder sb2;
        String str;
        String str2;
        if (dataStructure != null) {
            File dataFile = new File(filename);
            String tempFileName = filename + "_temp";
            File dataTempFile = new File(tempFileName);
            try {
                if (dataTempFile.createNewFile()) {
                    FileUtils.setPermissions(tempFileName, 504, -1, -1);
                }
                boolean successWrite = false;
                try {
                    FileOutputStream stream = new FileOutputStream(dataTempFile);
                    stream.write(structEncoder.getBytes(dataStructure));
                    successWrite = true;
                    try {
                        $closeResource(null, stream);
                        if (1 != 0) {
                            try {
                                Files.move(dataTempFile.toPath(), dataFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
                            } catch (IOException e) {
                                if (1 != 0) {
                                    sb2 = new StringBuilder();
                                    sb2.append("Failed to move file in right place ");
                                    sb2.append(filename);
                                    str2 = sb2.toString();
                                    errorPrint("AkWrite", str2);
                                }
                                sb = new StringBuilder();
                                sb.append("Failed to remove temporary file ");
                                sb.append(tempFileName);
                                str2 = sb.toString();
                                errorPrint("AkWrite", str2);
                            }
                        } else {
                            Files.delete(dataTempFile.toPath());
                        }
                    } catch (IOException e2) {
                        try {
                            errorPrint("AkWrite", "Failed to write file " + tempFileName);
                            if (!successWrite) {
                                try {
                                    Files.move(dataTempFile.toPath(), dataFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
                                } catch (IOException e3) {
                                    if (successWrite) {
                                        sb2 = new StringBuilder();
                                        sb2.append("Failed to move file in right place ");
                                        sb2.append(filename);
                                        str2 = sb2.toString();
                                        errorPrint("AkWrite", str2);
                                    }
                                    sb = new StringBuilder();
                                    sb.append("Failed to remove temporary file ");
                                    sb.append(tempFileName);
                                    str2 = sb.toString();
                                    errorPrint("AkWrite", str2);
                                }
                            } else {
                                Files.delete(dataTempFile.toPath());
                            }
                        } catch (Throwable th) {
                            ex = th;
                            if (!successWrite) {
                                try {
                                    Files.move(dataTempFile.toPath(), dataFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
                                } catch (IOException e4) {
                                    if (successWrite) {
                                        str = "Failed to move file in right place " + filename;
                                    } else {
                                        str = "Failed to remove temporary file " + tempFileName;
                                    }
                                    errorPrint("AkWrite", str);
                                }
                            } else {
                                Files.delete(dataTempFile.toPath());
                            }
                            throw ex;
                        }
                    }
                } catch (IOException e5) {
                    errorPrint("AkWrite", "Failed to write file " + tempFileName);
                    if (!successWrite) {
                    }
                } catch (Throwable th2) {
                    ex = th2;
                    if (!successWrite) {
                    }
                    throw ex;
                }
            } catch (IOException e6) {
                errorPrint("AkWrite", "Failed to create file " + tempFileName);
            }
        }
    }

    private void errorPrint(String tag, String message) {
        Slog.e("AdvancedKillerIO", tag + " " + message);
    }
}
