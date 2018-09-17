package com.huawei.android.pushselfshow.utils.a;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import com.huawei.android.pushagent.a.a.c;
import com.huawei.android.pushselfshow.richpush.favorites.e;
import com.huawei.android.pushselfshow.richpush.provider.RichMediaProvider.a;
import com.huawei.systemmanager.rainbow.comm.request.util.RainbowRequestBasic.CheckVersionField;
import java.util.ArrayList;

public class d {
    public static ArrayList a(Context context, String str) {
        ArrayList arrayList = new ArrayList();
        String str2 = "";
        String[] strArr = null;
        Cursor cursor = null;
        if (str != null) {
            str2 = "SELECT pushmsg._id,pushmsg.msg,pushmsg.token,pushmsg.url,notify.bmp  FROM pushmsg LEFT OUTER JOIN notify ON pushmsg.url = notify.url and pushmsg.url = ? order by pushmsg._id desc";
            strArr = new String[]{str};
        } else {
            str2 = "SELECT pushmsg._id,pushmsg.msg,pushmsg.token,pushmsg.url,notify.bmp  FROM pushmsg LEFT OUTER JOIN notify ON pushmsg.url = notify.url order by pushmsg._id desc limit 1000;";
        }
        try {
            cursor = e.a().a(context, a.f, str2, strArr);
        } catch (Throwable e) {
            c.d("PushSelfShowLog", e.toString(), e);
        }
        if (cursor != null) {
            while (cursor.moveToNext()) {
                try {
                    int i = cursor.getInt(0);
                    byte[] blob = cursor.getBlob(1);
                    if (blob != null) {
                        com.huawei.android.pushselfshow.c.a aVar = new com.huawei.android.pushselfshow.c.a(blob, " ".getBytes("UTF-8"));
                        if (!aVar.b()) {
                            c.a("PushSelfShowLog", "parseMessage failed");
                        }
                        cursor.getString(3);
                        e eVar = new e();
                        eVar.a(i);
                        eVar.a(aVar);
                        arrayList.add(eVar);
                    } else {
                        c.d("PushSelfShowLog", "msg is null");
                    }
                } catch (Throwable e2) {
                    c.d("TAG", "query favo error " + e2.toString(), e2);
                } finally {
                    cursor.close();
                }
            }
            c.e("PushSelfShowLog", "query favo size is " + arrayList.size());
            return arrayList;
        }
        c.a("PushSelfShowLog", "cursor is null.");
        return arrayList;
    }

    public static void a(Context context, int i) {
        try {
            i iVar = new i();
            iVar.a(a.g);
            iVar.a("pushmsg");
            iVar.b("_id = ?");
            iVar.a(new String[]{String.valueOf(i)});
            e.a().a(context, iVar);
        } catch (Throwable e) {
            c.d("PushSelfShowLog", e.toString(), e);
        }
    }

    public static boolean a(Context context, String str, com.huawei.android.pushselfshow.c.a aVar) {
        if (context == null || str == null || aVar == null) {
            try {
                c.e("PushSelfShowLog", "insertPushMsginfo ilegle param");
                return false;
            } catch (Throwable e) {
                c.e("PushSelfShowLog", "insertBmpinfo error", e);
                return false;
            }
        }
        ContentValues contentValues = new ContentValues();
        contentValues.put(CheckVersionField.CHECK_VERSION_SERVER_URL, str);
        contentValues.put("msg", aVar.c());
        contentValues.put("token", " ".getBytes("UTF-8"));
        c.a("PushSelfShowLog", "insertPushMsginfo select url is %s ,rpl is %s", str, aVar.x());
        ArrayList a = a(context, str);
        String x = aVar.x();
        int i = 0;
        while (i < a.size()) {
            if (((e) a.get(i)).b() != null && x.equals(((e) a.get(i)).b().x())) {
                c.a("PushSelfShowLog", x + " already exist");
                return true;
            }
            i++;
        }
        c.e("PushSelfShowLog", "insertPushMsginfo " + contentValues.toString());
        e.a().a(context, a.e, "pushmsg", contentValues);
        return true;
    }
}
