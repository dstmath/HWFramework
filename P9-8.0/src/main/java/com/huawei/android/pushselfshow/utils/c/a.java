package com.huawei.android.pushselfshow.utils.c;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.BitmapFactory.Options;
import android.graphics.Matrix;
import com.huawei.android.pushagent.a.a.c;
import com.huawei.android.pushselfshow.utils.b.b;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;

public class a {
    public Bitmap a(Context context, Bitmap bitmap, float f, float f2) {
        try {
            int width = bitmap.getWidth();
            int height = bitmap.getHeight();
            float f3 = f / ((float) width);
            float f4 = f2 / ((float) height);
            Matrix matrix = new Matrix();
            matrix.postScale(f3, f4);
            Bitmap createBitmap = Bitmap.createBitmap(bitmap, 0, 0, width, height, matrix, true);
            if (createBitmap != null) {
                c.a("PushSelfShowLog", "reScaleBitmap success");
                return createBitmap;
            }
        } catch (Throwable e) {
            c.d("PushSelfShowLog", "reScaleBitmap fail ,error ：" + e, e);
        }
        return bitmap;
    }

    /* JADX WARNING: Removed duplicated region for block: B:45:0x018f A:{SYNTHETIC, Splitter: B:45:0x018f} */
    /* JADX WARNING: Removed duplicated region for block: B:18:0x00e7 A:{SYNTHETIC, Splitter: B:18:0x00e7} */
    /* JADX WARNING: Removed duplicated region for block: B:45:0x018f A:{SYNTHETIC, Splitter: B:45:0x018f} */
    /* JADX WARNING: Removed duplicated region for block: B:18:0x00e7 A:{SYNTHETIC, Splitter: B:18:0x00e7} */
    /* JADX WARNING: Removed duplicated region for block: B:52:0x019b A:{SYNTHETIC, Splitter: B:52:0x019b} */
    /* JADX WARNING: Removed duplicated region for block: B:45:0x018f A:{SYNTHETIC, Splitter: B:45:0x018f} */
    /* JADX WARNING: Removed duplicated region for block: B:18:0x00e7 A:{SYNTHETIC, Splitter: B:18:0x00e7} */
    /* JADX WARNING: Removed duplicated region for block: B:52:0x019b A:{SYNTHETIC, Splitter: B:52:0x019b} */
    /* JADX WARNING: Removed duplicated region for block: B:52:0x019b A:{SYNTHETIC, Splitter: B:52:0x019b} */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public Bitmap a(Context context, String str) {
        Throwable e;
        Throwable th;
        Bitmap bitmap = null;
        InputStream inputStream = null;
        File file = null;
        try {
            String str2 = "image" + System.currentTimeMillis();
            String a = b.a(context);
            File file2 = new File(a);
            if (!file2.exists()) {
                c.a("PushSelfShowLog", "mkdir: " + file2.getAbsolutePath());
                if (!file2.mkdirs()) {
                    c.a("PushSelfShowLog", "file mkdir failed ,path is " + file2.getPath());
                }
            }
            String str3 = a + File.separator + str2;
            c.a("PushSelfShowLog", "try to download image to " + str3);
            if (new b().b(context, str, str3)) {
                c.a("PushSelfShowLog", "download successed");
                Options options = new Options();
                options.inDither = false;
                options.inPurgeable = true;
                options.inSampleSize = 1;
                options.inPreferredConfig = Config.RGB_565;
                File file3 = new File(str3);
                try {
                    try {
                        InputStream fileInputStream = new FileInputStream(file3);
                        try {
                            bitmap = BitmapFactory.decodeStream(fileInputStream, null, options);
                            file = file3;
                            inputStream = fileInputStream;
                        } catch (Exception e2) {
                            e = e2;
                            file = file3;
                            inputStream = fileInputStream;
                            try {
                                c.d("PushSelfShowLog", "getRemoteImage  failed  ,errorinfo is " + e.toString(), e);
                                if (inputStream != null) {
                                }
                                if (file != null) {
                                }
                                return bitmap;
                            } catch (Throwable th2) {
                                th = th2;
                                if (inputStream != null) {
                                }
                                c.a("PushSelfShowLog", "image delete success");
                                throw th;
                            }
                        } catch (Throwable th3) {
                            th = th3;
                            file = file3;
                            inputStream = fileInputStream;
                            if (inputStream != null) {
                            }
                            c.a("PushSelfShowLog", "image delete success");
                            throw th;
                        }
                    } catch (Exception e3) {
                        e = e3;
                        file = file3;
                        c.d("PushSelfShowLog", "getRemoteImage  failed  ,errorinfo is " + e.toString(), e);
                        if (inputStream != null) {
                            inputStream.close();
                        }
                        if (file != null) {
                            try {
                                if (file.isFile() && file.delete()) {
                                    c.a("PushSelfShowLog", "image delete success");
                                }
                            } catch (Throwable e4) {
                                c.d("PushSelfShowLog", "is.close() error" + e4.toString(), e4);
                            }
                        }
                        return bitmap;
                    } catch (Throwable th4) {
                        th = th4;
                        file = file3;
                        if (inputStream != null) {
                            try {
                                inputStream.close();
                            } catch (Throwable e5) {
                                c.d("PushSelfShowLog", "is.close() error" + e5.toString(), e5);
                                throw th;
                            }
                        }
                        if (file != null && file.isFile() && file.delete()) {
                            c.a("PushSelfShowLog", "image delete success");
                        }
                        throw th;
                    }
                } catch (Exception e6) {
                    e4 = e6;
                    file = file3;
                    c.d("PushSelfShowLog", "getRemoteImage  failed  ,errorinfo is " + e4.toString(), e4);
                    if (inputStream != null) {
                    }
                    if (file != null) {
                    }
                    return bitmap;
                } catch (Throwable th5) {
                    th = th5;
                    file = file3;
                    if (inputStream != null) {
                    }
                    c.a("PushSelfShowLog", "image delete success");
                    throw th;
                }
            }
            c.a("PushSelfShowLog", "download failed");
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (Throwable e42) {
                    c.d("PushSelfShowLog", "is.close() error" + e42.toString(), e42);
                }
            }
            if (file != null && file.isFile() && file.delete()) {
                c.a("PushSelfShowLog", "image delete success");
            }
        } catch (Exception e7) {
            e42 = e7;
            c.d("PushSelfShowLog", "getRemoteImage  failed  ,errorinfo is " + e42.toString(), e42);
            if (inputStream != null) {
            }
            if (file != null) {
            }
            return bitmap;
        }
        return bitmap;
    }
}
