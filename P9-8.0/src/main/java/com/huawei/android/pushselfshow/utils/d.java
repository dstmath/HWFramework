package com.huawei.android.pushselfshow.utils;

import android.content.Context;
import com.huawei.android.pushagent.a.a.c;
import java.lang.reflect.Field;

public class d {
    public static int a(Context context, String str) {
        return a(context, "string", str);
    }

    public static int a(Context context, String str, String str2) {
        try {
            int identifier = context.getResources().getIdentifier(str2, str, context.getPackageName());
            if (identifier == 0) {
                Field field = Class.forName(context.getPackageName() + ".R$" + str).getField(str2);
                identifier = Integer.parseInt(field.get(field.getName()).toString());
                if (identifier == 0) {
                    c.b("ResourceLoader", "Error-resourceType=" + str + "--resourceName=" + str2 + "--resourceId =" + identifier);
                }
            }
            return identifier;
        } catch (Throwable e) {
            c.d("ResourceLoader", "!!!! ResourceLoader: ClassNotFoundException-resourceType=" + str + "--resourceName=" + str2, e);
            return 0;
        } catch (Throwable e2) {
            c.d("ResourceLoader", "!!!! ResourceLoader: NoSuchFieldException-resourceType=" + str + "--resourceName=" + str2, e2);
            return 0;
        } catch (Throwable e22) {
            c.d("ResourceLoader", "!!!! ResourceLoader: NumberFormatException-resourceType=" + str + "--resourceName=" + str2, e22);
            return 0;
        } catch (Throwable e222) {
            c.d("ResourceLoader", "!!!! ResourceLoader: IllegalAccessException-resourceType=" + str + "--resourceName=" + str2, e222);
            return 0;
        } catch (Throwable e2222) {
            c.d("ResourceLoader", "!!!! ResourceLoader: IllegalArgumentException-resourceType=" + str + "--resourceName=" + str2, e2222);
            return 0;
        }
    }

    public static int b(Context context, String str) {
        return a(context, "plurals", str);
    }

    public static int c(Context context, String str) {
        return a(context, "layout", str);
    }

    public static int d(Context context, String str) {
        return a(context, "menu", str);
    }

    public static int e(Context context, String str) {
        return a(context, "id", str);
    }

    public static int f(Context context, String str) {
        return a(context, "color", str);
    }

    public static int g(Context context, String str) {
        return a(context, "drawable", str);
    }
}
