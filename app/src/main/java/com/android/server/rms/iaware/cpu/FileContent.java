package com.android.server.rms.iaware.cpu;

import android.rms.iaware.AwareLog;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;

public class FileContent {
    private static final String TAG = "FileContent";

    public static int getFileContent(String filePath) {
        Throwable th;
        int ret = -1;
        if (filePath == null) {
            return ret;
        }
        File file = new File(filePath);
        if (!file.exists() || !file.canRead()) {
            return ret;
        }
        FileInputStream fileInputStream = null;
        InputStreamReader inputStreamReader = null;
        BufferedReader bufferedReader = null;
        String content = null;
        try {
            InputStreamReader inputStreamReader2;
            FileInputStream input = new FileInputStream(filePath);
            try {
                inputStreamReader2 = new InputStreamReader(input, "UTF-8");
            } catch (FileNotFoundException e) {
                fileInputStream = input;
                AwareLog.e(TAG, "exception file not found, file path: " + filePath);
                closeBufferedReader(bufferedReader);
                closeInputStreamReader(inputStreamReader);
                closeFileInputStream(fileInputStream);
                if (content == null) {
                    return ret;
                }
                try {
                    ret = Integer.parseInt(content.trim());
                } catch (NumberFormatException e2) {
                    AwareLog.e(TAG, "itemValue string to int error!");
                }
                return ret;
            } catch (UnsupportedEncodingException e3) {
                fileInputStream = input;
                AwareLog.e(TAG, "UnsupportedEncodingException ");
                closeBufferedReader(bufferedReader);
                closeInputStreamReader(inputStreamReader);
                closeFileInputStream(fileInputStream);
                if (content == null) {
                    return ret;
                }
                ret = Integer.parseInt(content.trim());
                return ret;
            } catch (IOException e4) {
                fileInputStream = input;
                try {
                    AwareLog.e(TAG, "IOException");
                    closeBufferedReader(bufferedReader);
                    closeInputStreamReader(inputStreamReader);
                    closeFileInputStream(fileInputStream);
                    if (content == null) {
                        return ret;
                    }
                    ret = Integer.parseInt(content.trim());
                    return ret;
                } catch (Throwable th2) {
                    th = th2;
                    closeBufferedReader(bufferedReader);
                    closeInputStreamReader(inputStreamReader);
                    closeFileInputStream(fileInputStream);
                    throw th;
                }
            } catch (Throwable th3) {
                th = th3;
                fileInputStream = input;
                closeBufferedReader(bufferedReader);
                closeInputStreamReader(inputStreamReader);
                closeFileInputStream(fileInputStream);
                throw th;
            }
            try {
                BufferedReader bufReader = new BufferedReader(inputStreamReader2);
                try {
                    content = bufReader.readLine();
                    closeBufferedReader(bufReader);
                    closeInputStreamReader(inputStreamReader2);
                    closeFileInputStream(input);
                    fileInputStream = input;
                } catch (FileNotFoundException e5) {
                    bufferedReader = bufReader;
                    inputStreamReader = inputStreamReader2;
                    fileInputStream = input;
                    AwareLog.e(TAG, "exception file not found, file path: " + filePath);
                    closeBufferedReader(bufferedReader);
                    closeInputStreamReader(inputStreamReader);
                    closeFileInputStream(fileInputStream);
                    if (content == null) {
                        return ret;
                    }
                    ret = Integer.parseInt(content.trim());
                    return ret;
                } catch (UnsupportedEncodingException e6) {
                    bufferedReader = bufReader;
                    inputStreamReader = inputStreamReader2;
                    fileInputStream = input;
                    AwareLog.e(TAG, "UnsupportedEncodingException ");
                    closeBufferedReader(bufferedReader);
                    closeInputStreamReader(inputStreamReader);
                    closeFileInputStream(fileInputStream);
                    if (content == null) {
                        return ret;
                    }
                    ret = Integer.parseInt(content.trim());
                    return ret;
                } catch (IOException e7) {
                    bufferedReader = bufReader;
                    inputStreamReader = inputStreamReader2;
                    fileInputStream = input;
                    AwareLog.e(TAG, "IOException");
                    closeBufferedReader(bufferedReader);
                    closeInputStreamReader(inputStreamReader);
                    closeFileInputStream(fileInputStream);
                    if (content == null) {
                        return ret;
                    }
                    ret = Integer.parseInt(content.trim());
                    return ret;
                } catch (Throwable th4) {
                    th = th4;
                    bufferedReader = bufReader;
                    inputStreamReader = inputStreamReader2;
                    fileInputStream = input;
                    closeBufferedReader(bufferedReader);
                    closeInputStreamReader(inputStreamReader);
                    closeFileInputStream(fileInputStream);
                    throw th;
                }
            } catch (FileNotFoundException e8) {
                inputStreamReader = inputStreamReader2;
                fileInputStream = input;
                AwareLog.e(TAG, "exception file not found, file path: " + filePath);
                closeBufferedReader(bufferedReader);
                closeInputStreamReader(inputStreamReader);
                closeFileInputStream(fileInputStream);
                if (content == null) {
                    return ret;
                }
                ret = Integer.parseInt(content.trim());
                return ret;
            } catch (UnsupportedEncodingException e9) {
                inputStreamReader = inputStreamReader2;
                fileInputStream = input;
                AwareLog.e(TAG, "UnsupportedEncodingException ");
                closeBufferedReader(bufferedReader);
                closeInputStreamReader(inputStreamReader);
                closeFileInputStream(fileInputStream);
                if (content == null) {
                    return ret;
                }
                ret = Integer.parseInt(content.trim());
                return ret;
            } catch (IOException e10) {
                inputStreamReader = inputStreamReader2;
                fileInputStream = input;
                AwareLog.e(TAG, "IOException");
                closeBufferedReader(bufferedReader);
                closeInputStreamReader(inputStreamReader);
                closeFileInputStream(fileInputStream);
                if (content == null) {
                    return ret;
                }
                ret = Integer.parseInt(content.trim());
                return ret;
            } catch (Throwable th5) {
                th = th5;
                inputStreamReader = inputStreamReader2;
                fileInputStream = input;
                closeBufferedReader(bufferedReader);
                closeInputStreamReader(inputStreamReader);
                closeFileInputStream(fileInputStream);
                throw th;
            }
        } catch (FileNotFoundException e11) {
            AwareLog.e(TAG, "exception file not found, file path: " + filePath);
            closeBufferedReader(bufferedReader);
            closeInputStreamReader(inputStreamReader);
            closeFileInputStream(fileInputStream);
            if (content == null) {
                return ret;
            }
            ret = Integer.parseInt(content.trim());
            return ret;
        } catch (UnsupportedEncodingException e12) {
            AwareLog.e(TAG, "UnsupportedEncodingException ");
            closeBufferedReader(bufferedReader);
            closeInputStreamReader(inputStreamReader);
            closeFileInputStream(fileInputStream);
            if (content == null) {
                return ret;
            }
            ret = Integer.parseInt(content.trim());
            return ret;
        } catch (IOException e13) {
            AwareLog.e(TAG, "IOException");
            closeBufferedReader(bufferedReader);
            closeInputStreamReader(inputStreamReader);
            closeFileInputStream(fileInputStream);
            if (content == null) {
                return ret;
            }
            ret = Integer.parseInt(content.trim());
            return ret;
        }
        if (content == null) {
            return ret;
        }
        ret = Integer.parseInt(content.trim());
        return ret;
    }

    public static void closeBufferedReader(BufferedReader br) {
        if (br != null) {
            try {
                br.close();
            } catch (IOException e) {
                AwareLog.e(TAG, "closeBufferedReader exception " + e.getMessage());
            }
        }
    }

    public static void closeInputStreamReader(InputStreamReader isr) {
        if (isr != null) {
            try {
                isr.close();
            } catch (IOException e) {
                AwareLog.e(TAG, "closeInputStreamReader exception " + e.getMessage());
            }
        }
    }

    public static void closeFileInputStream(FileInputStream fis) {
        if (fis != null) {
            try {
                fis.close();
            } catch (IOException e) {
                AwareLog.e(TAG, "closeFileInputStream exception " + e.getMessage());
            }
        }
    }
}
