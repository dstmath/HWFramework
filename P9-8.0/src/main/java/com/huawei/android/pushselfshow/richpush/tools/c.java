package com.huawei.android.pushselfshow.richpush.tools;

import android.content.Context;
import com.huawei.android.pushselfshow.utils.a;
import java.io.File;
import java.io.FileOutputStream;

public class c {
    private String a;
    private Context b;

    public c(Context context, String str) {
        this.a = str;
        this.b = context;
    }

    private String b() {
        return "﻿<!DOCTYPE html>\t\t<html>\t\t   <head>\t\t     <meta charset=\"utf-8\">\t\t     <title></title>\t\t     <style type=\"text/css\">\t\t\t\t html { height:100%;}\t\t\t\t body { height:100%; text-align:center;}\t    \t    .centerDiv { display:inline-block; zoom:1; *display:inline; vertical-align:top; text-align:left; width:200px; padding:10px;margin-top:100px;}\t\t\t   .hiddenDiv { height:100%; overflow:hidden; display:inline-block; width:1px; overflow:hidden; margin-left:-1px; zoom:1; *display:inline; *margin-top:-1px; _margin-top:0; vertical-align:middle;}\t\t  \t</style>    \t  </head>\t\t <body>\t\t\t<div id =\"container\" class=\"centerDiv\">";
    }

    private String c() {
        return "﻿\t\t</div>  \t\t<div class=\"hiddenDiv\"></div>\t  </body>   </html>";
    }

    /* JADX WARNING: Removed duplicated region for block: B:57:0x0153 A:{SYNTHETIC, Splitter: B:57:0x0153} */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public String a() {
        Throwable e;
        Throwable th;
        FileOutputStream fileOutputStream = null;
        if (this.b != null) {
            String str = b() + this.a + c();
            String str2 = this.b.getFilesDir().getPath() + File.separator + "PushService" + File.separator + "richpush";
            String str3 = "error.html";
            File file = new File(str2);
            File file2 = new File(str2 + File.separator + str3);
            try {
                if (!file.exists()) {
                    com.huawei.android.pushagent.a.a.c.a("PushSelfShowLog", "Create the path:" + str2);
                    if (!file.mkdirs()) {
                        com.huawei.android.pushagent.a.a.c.a("PushSelfShowLog", "!path.mkdirs()");
                        if (fileOutputStream != null) {
                            try {
                                fileOutputStream.close();
                            } catch (Throwable e2) {
                                com.huawei.android.pushagent.a.a.c.a("PushSelfShowLog", "stream.close() error ", e2);
                            }
                        }
                        return null;
                    }
                }
                if (file2.exists()) {
                    a.a(file2);
                }
                com.huawei.android.pushagent.a.a.c.a("PushSelfShowLog", "Create the file:" + str3);
                if (file2.createNewFile()) {
                    FileOutputStream fileOutputStream2 = new FileOutputStream(file2);
                    try {
                        fileOutputStream2.write(str.getBytes("UTF-8"));
                        if (fileOutputStream2 != null) {
                            try {
                                fileOutputStream2.close();
                            } catch (Throwable e3) {
                                com.huawei.android.pushagent.a.a.c.a("PushSelfShowLog", "stream.close() error ", e3);
                            }
                        }
                        return file2.getAbsolutePath();
                    } catch (Exception e4) {
                        e3 = e4;
                        fileOutputStream = fileOutputStream2;
                        try {
                            com.huawei.android.pushagent.a.a.c.a("PushSelfShowLog", "Create html error ", e3);
                            if (fileOutputStream != null) {
                                try {
                                    fileOutputStream.close();
                                } catch (Throwable e5) {
                                    com.huawei.android.pushagent.a.a.c.a("PushSelfShowLog", "stream.close() error ", e5);
                                }
                            }
                            return null;
                        } catch (Throwable th2) {
                            th = th2;
                            if (fileOutputStream != null) {
                            }
                            throw th;
                        }
                    } catch (Throwable th3) {
                        th = th3;
                        fileOutputStream = fileOutputStream2;
                        if (fileOutputStream != null) {
                            try {
                                fileOutputStream.close();
                            } catch (Throwable e6) {
                                com.huawei.android.pushagent.a.a.c.a("PushSelfShowLog", "stream.close() error ", e6);
                            }
                        }
                        throw th;
                    }
                }
                com.huawei.android.pushagent.a.a.c.a("PushSelfShowLog", "!file.createNewFile()");
                if (fileOutputStream != null) {
                    try {
                        fileOutputStream.close();
                    } catch (Throwable e22) {
                        com.huawei.android.pushagent.a.a.c.a("PushSelfShowLog", "stream.close() error ", e22);
                    }
                }
                return null;
            } catch (Exception e7) {
                e3 = e7;
            }
        } else {
            com.huawei.android.pushagent.a.a.c.d("PushSelfShowLog", "CreateHtmlFile fail ,context is null");
            return null;
        }
    }
}
