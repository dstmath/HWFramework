package com.android.server.wifi;

import android.util.Log;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class HwCHRWifiFile {
    public static List<String> getFileResult(String fileName) {
        FileNotFoundException e;
        IOException e2;
        Throwable th;
        List<String> result = new ArrayList();
        FileInputStream fileInputStream = null;
        BufferedReader bufferedReader = null;
        try {
            BufferedReader dr;
            FileInputStream f = new FileInputStream(fileName);
            try {
                dr = new BufferedReader(new InputStreamReader(f, "US-ASCII"));
            } catch (FileNotFoundException e3) {
                e = e3;
                fileInputStream = f;
                Log.e("HwCHRWifiFile", "getFileResult throw exception" + e);
                if (bufferedReader != null) {
                    try {
                        bufferedReader.close();
                    } catch (Exception e4) {
                    }
                }
                if (fileInputStream != null) {
                    fileInputStream.close();
                }
                return result;
            } catch (IOException e5) {
                e2 = e5;
                fileInputStream = f;
                try {
                    Log.e("HwCHRWifiFile", "getFileResult throw exception" + e2);
                    if (bufferedReader != null) {
                        try {
                            bufferedReader.close();
                        } catch (Exception e6) {
                        }
                    }
                    if (fileInputStream != null) {
                        fileInputStream.close();
                    }
                    return result;
                } catch (Throwable th2) {
                    th = th2;
                    if (bufferedReader != null) {
                        try {
                            bufferedReader.close();
                        } catch (Exception e7) {
                            throw th;
                        }
                    }
                    if (fileInputStream != null) {
                        fileInputStream.close();
                    }
                    throw th;
                }
            } catch (Throwable th3) {
                th = th3;
                fileInputStream = f;
                if (bufferedReader != null) {
                    bufferedReader.close();
                }
                if (fileInputStream != null) {
                    fileInputStream.close();
                }
                throw th;
            }
            try {
                for (String line = dr.readLine(); line != null; line = dr.readLine()) {
                    line = line.trim();
                    if (!line.equals("")) {
                        result.add(line);
                    }
                }
                dr.close();
                f.close();
                if (dr != null) {
                    try {
                        dr.close();
                    } catch (Exception e8) {
                    }
                }
                if (f != null) {
                    f.close();
                }
                fileInputStream = f;
            } catch (FileNotFoundException e9) {
                e = e9;
                bufferedReader = dr;
                fileInputStream = f;
                Log.e("HwCHRWifiFile", "getFileResult throw exception" + e);
                if (bufferedReader != null) {
                    bufferedReader.close();
                }
                if (fileInputStream != null) {
                    fileInputStream.close();
                }
                return result;
            } catch (IOException e10) {
                e2 = e10;
                bufferedReader = dr;
                fileInputStream = f;
                Log.e("HwCHRWifiFile", "getFileResult throw exception" + e2);
                if (bufferedReader != null) {
                    bufferedReader.close();
                }
                if (fileInputStream != null) {
                    fileInputStream.close();
                }
                return result;
            } catch (Throwable th4) {
                th = th4;
                bufferedReader = dr;
                fileInputStream = f;
                if (bufferedReader != null) {
                    bufferedReader.close();
                }
                if (fileInputStream != null) {
                    fileInputStream.close();
                }
                throw th;
            }
        } catch (FileNotFoundException e11) {
            e = e11;
            Log.e("HwCHRWifiFile", "getFileResult throw exception" + e);
            if (bufferedReader != null) {
                bufferedReader.close();
            }
            if (fileInputStream != null) {
                fileInputStream.close();
            }
            return result;
        } catch (IOException e12) {
            e2 = e12;
            Log.e("HwCHRWifiFile", "getFileResult throw exception" + e2);
            if (bufferedReader != null) {
                bufferedReader.close();
            }
            if (fileInputStream != null) {
                fileInputStream.close();
            }
            return result;
        }
        return result;
    }
}
