package com.huawei.android.pushselfshow.d;

import android.R.drawable;
import android.annotation.SuppressLint;
import android.app.Notification.Builder;
import android.content.Context;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Icon;
import android.os.Build.VERSION;
import com.huawei.android.pushagent.a.a.c;
import com.huawei.android.pushselfshow.utils.a;
import com.huawei.android.pushselfshow.utils.d;

public class b {
    @SuppressLint({"InlinedApi"})
    private static float a(Context context) {
        float a = (float) a.a(context, 48.0f);
        try {
            float dimension = context.getResources().getDimension(17104901);
            if (dimension > 0.0f && a > dimension) {
                a = dimension;
            }
        } catch (Exception e) {
            c.c("PushSelfShowLog", e.toString());
        }
        c.a("PushSelfShowLog", "getRescaleBitmapSize:" + a);
        return a;
    }

    public static int a(Context context, com.huawei.android.pushselfshow.c.a aVar) {
        int i = 0;
        if (context == null || aVar == null) {
            c.b("PushSelfShowLog", "enter getSmallIconId, context or msg is null");
            return 0;
        }
        if ("com.huawei.android.pushagent".equals(aVar.k())) {
            i = d.g(context, "hwpush_status_icon");
        }
        if (i == 0) {
            i = context.getApplicationInfo().icon;
        }
        if (i == 0) {
            i = context.getResources().getIdentifier("btn_star_big_on", "drawable", "android");
            c.a("PushSelfShowLog", "icon is btn_star_big_on ");
            if (i == 0) {
                i = 17301651;
                c.a("PushSelfShowLog", "icon is sym_def_app_icon ");
            }
        }
        return i;
    }

    public static int a(Context context, String str, String str2, Object obj) {
        try {
            String str3 = context.getPackageName() + ".R";
            c.a("PushSelfShowLog", "try to refrect " + str3 + " typeName is " + str2);
            Class[] classes = Class.forName(str3).getClasses();
            c.a("PushSelfShowLog", "sonClassArr length " + classes.length);
            Class cls = null;
            for (Class cls2 : classes) {
                c.a("PushSelfShowLog", "sonTypeClass,query sonclass is  %s", cls2.getName().substring(str3.length() + 1) + " sonClass.getName() is" + cls2.getName());
                if (str2.equals(cls2.getName().substring(str3.length() + 1))) {
                    cls = cls2;
                    break;
                }
            }
            if (cls == null) {
                c.a("PushSelfShowLog", "sonTypeClass is null");
                String str4 = context.getPackageName() + ".R$" + str2;
                c.a("PushSelfShowLog", "try to refrect 2 " + str4 + " typeName is " + str2);
                c.a("PushSelfShowLog", " refect res id 2 is %s", "" + Class.forName(str4).getField(str).getInt(obj));
                return Class.forName(str4).getField(str).getInt(obj);
            }
            c.a("PushSelfShowLog", " refect res id is %s", "" + cls.getField(str).getInt(obj));
            return cls.getField(str).getInt(obj);
        } catch (Throwable e) {
            c.d("PushSelfShowLog", "ClassNotFound failed,", e);
            return 0;
        } catch (Throwable e2) {
            c.d("PushSelfShowLog", "NoSuchFieldException failed,", e2);
            return 0;
        } catch (Throwable e22) {
            c.d("PushSelfShowLog", "IllegalAccessException failed,", e22);
            return 0;
        } catch (Throwable e222) {
            c.d("PushSelfShowLog", "IllegalArgumentException failed,", e222);
            return 0;
        } catch (Throwable e2222) {
            c.d("PushSelfShowLog", "IndexOutOfBoundsException failed,", e2222);
            return 0;
        } catch (Throwable e22222) {
            c.d("PushSelfShowLog", "  failed,", e22222);
            return 0;
        }
    }

    public static void a(Context context, Builder builder, com.huawei.android.pushselfshow.c.a aVar) {
        if (context == null || builder == null || aVar == null) {
            c.d("PushSelfShowLog", "msg is null");
            return;
        }
        if (d(context, aVar)) {
            c.b("PushSelfShowLog", "get small icon from " + aVar.k());
            Icon c = c(context, aVar);
            if (c == null) {
                builder.setSmallIcon(a(context, aVar));
            } else {
                builder.setSmallIcon(c);
            }
        } else {
            builder.setSmallIcon(a(context, aVar));
        }
    }

    public static Bitmap b(Context context, com.huawei.android.pushselfshow.c.a aVar) {
        if (context == null || aVar == null) {
            return null;
        }
        Bitmap bitmap = null;
        try {
            if (aVar.o() != null && aVar.o().length() > 0) {
                com.huawei.android.pushselfshow.utils.c.a aVar2 = new com.huawei.android.pushselfshow.utils.c.a();
                int i = 0;
                if (!aVar.o().equals("" + aVar.a())) {
                    i = a(context, aVar.o(), "drawable", new drawable());
                    if (i == 0) {
                        i = context.getResources().getIdentifier(aVar.o(), "drawable", "android");
                    }
                    c.a("PushSelfShowLog", "msg.notifyIcon is " + aVar.o() + ",and defaultIcon is " + i);
                }
                if (i == 0) {
                    Bitmap a = aVar2.a(context, aVar.o());
                    c.a("PushSelfShowLog", "get bitmap from new downloaded ");
                    if (a != null) {
                        c.a("PushSelfShowLog", "height:" + a.getHeight() + ",width:" + a.getWidth());
                        float a2 = a(context);
                        a = aVar2.a(context, a, a2, a2);
                    }
                    if (a != null) {
                        bitmap = a;
                    }
                } else {
                    bitmap = BitmapFactory.decodeResource(context.getResources(), i);
                }
            }
            if (com.huawei.android.pushagent.a.a.a.a() < 11) {
                if (bitmap == null) {
                    if (!"com.huawei.android.pushagent".equals(aVar.k())) {
                        c.b("PushSelfShowLog", "get left bitmap from " + aVar.k());
                        bitmap = ((BitmapDrawable) context.getPackageManager().getApplicationIcon(aVar.k())).getBitmap();
                    }
                }
                return bitmap;
            }
            c.b("PushSelfShowLog", "huawei phone, and emui5.0, need not show large icon.");
            return bitmap;
        } catch (Throwable e) {
            c.d("PushSelfShowLog", "" + e.toString(), e);
        } catch (Throwable e2) {
            c.d("PushSelfShowLog", "" + e2.toString(), e2);
        }
    }

    public static Icon c(Context context, com.huawei.android.pushselfshow.c.a aVar) {
        if (context == null || aVar == null) {
            c.d("PushSelfShowLog", "getSmallIcon, context is null");
            return null;
        } else if (VERSION.SDK_INT >= 23) {
            try {
                return Icon.createWithResource(aVar.k(), context.getPackageManager().getApplicationInfo(aVar.k(), 0).icon);
            } catch (NameNotFoundException e) {
                c.d("PushSelfShowLog", e.toString());
            } catch (Throwable e2) {
                c.d("PushSelfShowLog", e2.toString(), e2);
            }
        } else {
            c.b("PushSelfShowLog", "getSmallIcon failed, Build.VERSION less than 23");
            return null;
        }
        return null;
    }

    private static boolean d(Context context, com.huawei.android.pushselfshow.c.a aVar) {
        return !"com.huawei.android.pushagent".equals(aVar.k()) && VERSION.SDK_INT >= 23 && (a.e(context) || a.f(context));
    }
}
