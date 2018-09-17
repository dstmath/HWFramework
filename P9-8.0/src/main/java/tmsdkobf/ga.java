package tmsdkobf;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

public final class ga {
    /* JADX WARNING: Removed duplicated region for block: B:71:0x011f  */
    /* JADX WARNING: Removed duplicated region for block: B:4:0x0019  */
    /* JADX WARNING: Removed duplicated region for block: B:72:0x0123  */
    /* JADX WARNING: Removed duplicated region for block: B:80:? A:{SYNTHETIC, RETURN, ORIG_RETURN} */
    /* JADX WARNING: Removed duplicated region for block: B:4:0x0019  */
    /* JADX WARNING: Removed duplicated region for block: B:71:0x011f  */
    /* JADX WARNING: Removed duplicated region for block: B:80:? A:{SYNTHETIC, RETURN, ORIG_RETURN} */
    /* JADX WARNING: Removed duplicated region for block: B:72:0x0123  */
    /* JADX WARNING: Removed duplicated region for block: B:71:0x011f  */
    /* JADX WARNING: Removed duplicated region for block: B:4:0x0019  */
    /* JADX WARNING: Removed duplicated region for block: B:72:0x0123  */
    /* JADX WARNING: Removed duplicated region for block: B:80:? A:{SYNTHETIC, RETURN, ORIG_RETURN} */
    /* JADX WARNING: Removed duplicated region for block: B:66:0x0115 A:{SYNTHETIC, Splitter: B:66:0x0115} */
    /* JADX WARNING: Removed duplicated region for block: B:18:0x004e A:{SYNTHETIC, Splitter: B:18:0x004e} */
    /* JADX WARNING: Removed duplicated region for block: B:4:0x0019  */
    /* JADX WARNING: Removed duplicated region for block: B:71:0x011f  */
    /* JADX WARNING: Removed duplicated region for block: B:80:? A:{SYNTHETIC, RETURN, ORIG_RETURN} */
    /* JADX WARNING: Removed duplicated region for block: B:72:0x0123  */
    /* JADX WARNING: Removed duplicated region for block: B:51:0x00f5 A:{SYNTHETIC, Splitter: B:51:0x00f5} */
    /* JADX WARNING: Removed duplicated region for block: B:71:0x011f  */
    /* JADX WARNING: Removed duplicated region for block: B:4:0x0019  */
    /* JADX WARNING: Removed duplicated region for block: B:72:0x0123  */
    /* JADX WARNING: Removed duplicated region for block: B:80:? A:{SYNTHETIC, RETURN, ORIG_RETURN} */
    /* JADX WARNING: Removed duplicated region for block: B:59:0x0106 A:{SYNTHETIC, Splitter: B:59:0x0106} */
    /* JADX WARNING: Removed duplicated region for block: B:4:0x0019  */
    /* JADX WARNING: Removed duplicated region for block: B:71:0x011f  */
    /* JADX WARNING: Removed duplicated region for block: B:80:? A:{SYNTHETIC, RETURN, ORIG_RETURN} */
    /* JADX WARNING: Removed duplicated region for block: B:72:0x0123  */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public static long Q() {
        IOException e;
        FileNotFoundException e2;
        Throwable th;
        NumberFormatException e3;
        long j = 0;
        File file = new File("/proc/meminfo");
        DataInputStream dataInputStream = null;
        if (file.exists()) {
            try {
                DataInputStream dataInputStream2 = new DataInputStream(new FileInputStream(file));
                try {
                    String readLine = dataInputStream2.readLine();
                    String readLine2 = dataInputStream2.readLine();
                    String readLine3 = dataInputStream2.readLine();
                    String readLine4 = dataInputStream2.readLine();
                    if (readLine == null || readLine2 == null || readLine3 == null || readLine4 == null) {
                        throw new IOException("/proc/meminfo is error!");
                    }
                    readLine = readLine.trim();
                    readLine2 = readLine2.trim();
                    readLine3 = readLine3.trim();
                    readLine4 = readLine4.trim();
                    String[] split = readLine.split("[\\s]+");
                    String[] split2 = readLine2.split("[\\s]+");
                    String[] split3 = readLine3.split("[\\s]+");
                    String[] split4 = readLine4.split("[\\s]+");
                    if (split2 != null && split2.length > 1) {
                        j = 0 + Long.parseLong(split2[1]);
                    }
                    if (split3 != null && split3.length > 1) {
                        j += Long.parseLong(split3[1]);
                    }
                    if (split4 != null && split4.length > 1) {
                        j += Long.parseLong(split4[1]);
                    }
                    if (dataInputStream2 == null) {
                        dataInputStream = dataInputStream2;
                    } else {
                        try {
                            dataInputStream2.close();
                        } catch (IOException e4) {
                            e4.printStackTrace();
                        }
                    }
                } catch (FileNotFoundException e5) {
                    e2 = e5;
                    dataInputStream = dataInputStream2;
                    try {
                        e2.printStackTrace();
                        if (dataInputStream != null) {
                            try {
                                dataInputStream.close();
                            } catch (IOException e42) {
                                e42.printStackTrace();
                            }
                        }
                        if (j <= 0) {
                        }
                        if ((j <= 0 ? 1 : null) == null) {
                        }
                    } catch (Throwable th2) {
                        th = th2;
                        if (dataInputStream != null) {
                        }
                        throw th;
                    }
                } catch (IOException e6) {
                    e42 = e6;
                    dataInputStream = dataInputStream2;
                    e42.printStackTrace();
                    if (dataInputStream != null) {
                        try {
                            dataInputStream.close();
                        } catch (IOException e422) {
                            e422.printStackTrace();
                        }
                    }
                    if (j <= 0) {
                    }
                    if ((j <= 0 ? 1 : null) == null) {
                    }
                } catch (NumberFormatException e7) {
                    e3 = e7;
                    dataInputStream = dataInputStream2;
                    e3.printStackTrace();
                    if (dataInputStream != null) {
                        try {
                            dataInputStream.close();
                        } catch (IOException e4222) {
                            e4222.printStackTrace();
                        }
                    }
                    if (j <= 0) {
                    }
                    if ((j <= 0 ? 1 : null) == null) {
                    }
                } catch (Throwable th3) {
                    th = th3;
                    dataInputStream = dataInputStream2;
                    if (dataInputStream != null) {
                        try {
                            dataInputStream.close();
                        } catch (IOException e8) {
                            e8.printStackTrace();
                        }
                    }
                    throw th;
                }
            } catch (FileNotFoundException e9) {
                e2 = e9;
                e2.printStackTrace();
                if (dataInputStream != null) {
                }
                if (j <= 0) {
                }
                if ((j <= 0 ? 1 : null) == null) {
                }
            } catch (IOException e10) {
                e4222 = e10;
                e4222.printStackTrace();
                if (dataInputStream != null) {
                }
                if (j <= 0) {
                }
                if ((j <= 0 ? 1 : null) == null) {
                }
            } catch (NumberFormatException e11) {
                e3 = e11;
                e3.printStackTrace();
                if (dataInputStream != null) {
                }
                if (j <= 0) {
                }
                if ((j <= 0 ? 1 : null) == null) {
                }
            }
        }
        return (j <= 0 ? 1 : null) == null ? j : 1;
    }
}
