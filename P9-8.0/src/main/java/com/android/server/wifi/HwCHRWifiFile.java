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
    /* JADX WARNING: Removed duplicated region for block: B:35:0x0069 A:{SYNTHETIC, Splitter: B:35:0x0069} */
    /* JADX WARNING: Removed duplicated region for block: B:29:0x0057 A:{SYNTHETIC, Splitter: B:29:0x0057} */
    /* JADX WARNING: Removed duplicated region for block: B:20:0x003b A:{SYNTHETIC, Splitter: B:20:0x003b} */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public static byte[] getDevFileResult(String fileName) {
        Throwable th;
        FileInputStream fin = null;
        byte[] buffer = new byte[4];
        try {
            FileInputStream fin2 = new FileInputStream(fileName);
            try {
                int length = fin2.read(buffer);
                fin2.close();
                if (length != 4) {
                    Log.e("HwCHRWifiFile", "getDevFileResult read length is not right");
                    buffer = null;
                }
                if (fin2 != null) {
                    try {
                        fin2.close();
                    } catch (Exception e) {
                        Log.e("HwCHRWifiFile", "getDevFileResult throw close exception");
                    }
                }
                fin = fin2;
            } catch (FileNotFoundException e2) {
                fin = fin2;
                Log.e("HwCHRWifiFile", "getDevFileResult throw FileNotFoundException");
                buffer = null;
                if (fin != null) {
                    try {
                        fin.close();
                    } catch (Exception e3) {
                        Log.e("HwCHRWifiFile", "getDevFileResult throw close exception");
                    }
                }
                return buffer;
            } catch (IOException e4) {
                fin = fin2;
                try {
                    Log.e("HwCHRWifiFile", "getDevFileResult throw IOException");
                    buffer = null;
                    if (fin != null) {
                        try {
                            fin.close();
                        } catch (Exception e5) {
                            Log.e("HwCHRWifiFile", "getDevFileResult throw close exception");
                        }
                    }
                    return buffer;
                } catch (Throwable th2) {
                    th = th2;
                    if (fin != null) {
                        try {
                            fin.close();
                        } catch (Exception e6) {
                            Log.e("HwCHRWifiFile", "getDevFileResult throw close exception");
                        }
                    }
                    throw th;
                }
            } catch (Throwable th3) {
                th = th3;
                fin = fin2;
                if (fin != null) {
                }
                throw th;
            }
        } catch (FileNotFoundException e7) {
            Log.e("HwCHRWifiFile", "getDevFileResult throw FileNotFoundException");
            buffer = null;
            if (fin != null) {
            }
            return buffer;
        } catch (IOException e8) {
            Log.e("HwCHRWifiFile", "getDevFileResult throw IOException");
            buffer = null;
            if (fin != null) {
            }
            return buffer;
        }
        return buffer;
    }

    /* JADX WARNING: Removed duplicated region for block: B:40:0x009b A:{SYNTHETIC, Splitter: B:40:0x009b} */
    /* JADX WARNING: Removed duplicated region for block: B:43:0x00a0 A:{Catch:{ Exception -> 0x00a4 }} */
    /* JADX WARNING: Removed duplicated region for block: B:33:0x008d A:{SYNTHETIC, Splitter: B:33:0x008d} */
    /* JADX WARNING: Removed duplicated region for block: B:36:0x0092 A:{Catch:{ Exception -> 0x0096 }} */
    /* JADX WARNING: Removed duplicated region for block: B:24:0x0065 A:{SYNTHETIC, Splitter: B:24:0x0065} */
    /* JADX WARNING: Removed duplicated region for block: B:27:0x006a A:{Catch:{ Exception -> 0x006e }} */
    /* JADX WARNING: Removed duplicated region for block: B:40:0x009b A:{SYNTHETIC, Splitter: B:40:0x009b} */
    /* JADX WARNING: Removed duplicated region for block: B:43:0x00a0 A:{Catch:{ Exception -> 0x00a4 }} */
    /* JADX WARNING: Removed duplicated region for block: B:33:0x008d A:{SYNTHETIC, Splitter: B:33:0x008d} */
    /* JADX WARNING: Removed duplicated region for block: B:36:0x0092 A:{Catch:{ Exception -> 0x0096 }} */
    /* JADX WARNING: Removed duplicated region for block: B:24:0x0065 A:{SYNTHETIC, Splitter: B:24:0x0065} */
    /* JADX WARNING: Removed duplicated region for block: B:27:0x006a A:{Catch:{ Exception -> 0x006e }} */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public static List<String> getFileResult(String fileName) {
        FileNotFoundException e;
        IOException e2;
        Throwable th;
        List<String> result = new ArrayList();
        FileInputStream f = null;
        BufferedReader dr = null;
        try {
            BufferedReader dr2;
            FileInputStream f2 = new FileInputStream(fileName);
            try {
                dr2 = new BufferedReader(new InputStreamReader(f2, "US-ASCII"));
            } catch (FileNotFoundException e3) {
                e = e3;
                f = f2;
                Log.e("HwCHRWifiFile", "getFileResult throw exception" + e);
                if (dr != null) {
                }
                if (f != null) {
                }
                return result;
            } catch (IOException e4) {
                e2 = e4;
                f = f2;
                try {
                    Log.e("HwCHRWifiFile", "getFileResult throw exception" + e2);
                    if (dr != null) {
                    }
                    if (f != null) {
                    }
                    return result;
                } catch (Throwable th2) {
                    th = th2;
                    if (dr != null) {
                        try {
                            dr.close();
                        } catch (Exception e5) {
                            throw th;
                        }
                    }
                    if (f != null) {
                        f.close();
                    }
                    throw th;
                }
            } catch (Throwable th3) {
                th = th3;
                f = f2;
                if (dr != null) {
                }
                if (f != null) {
                }
                throw th;
            }
            try {
                for (String line = dr2.readLine(); line != null; line = dr2.readLine()) {
                    line = line.trim();
                    if (!line.equals("")) {
                        result.add(line);
                    }
                }
                dr2.close();
                f2.close();
                if (dr2 != null) {
                    try {
                        dr2.close();
                    } catch (Exception e6) {
                    }
                }
                if (f2 != null) {
                    f2.close();
                }
                f = f2;
            } catch (FileNotFoundException e7) {
                e = e7;
                dr = dr2;
                f = f2;
                Log.e("HwCHRWifiFile", "getFileResult throw exception" + e);
                if (dr != null) {
                    try {
                        dr.close();
                    } catch (Exception e8) {
                    }
                }
                if (f != null) {
                    f.close();
                }
                return result;
            } catch (IOException e9) {
                e2 = e9;
                dr = dr2;
                f = f2;
                Log.e("HwCHRWifiFile", "getFileResult throw exception" + e2);
                if (dr != null) {
                    try {
                        dr.close();
                    } catch (Exception e10) {
                    }
                }
                if (f != null) {
                    f.close();
                }
                return result;
            } catch (Throwable th4) {
                th = th4;
                dr = dr2;
                f = f2;
                if (dr != null) {
                }
                if (f != null) {
                }
                throw th;
            }
        } catch (FileNotFoundException e11) {
            e = e11;
            Log.e("HwCHRWifiFile", "getFileResult throw exception" + e);
            if (dr != null) {
            }
            if (f != null) {
            }
            return result;
        } catch (IOException e12) {
            e2 = e12;
            Log.e("HwCHRWifiFile", "getFileResult throw exception" + e2);
            if (dr != null) {
            }
            if (f != null) {
            }
            return result;
        }
        return result;
    }
}
