package com.huawei.android.pushselfshow.permission;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Build.VERSION;
import android.os.Bundle;
import com.huawei.android.pushagent.a.a.a;
import com.huawei.android.pushagent.a.a.c;
import com.huawei.android.pushselfshow.SelfShowReceiver;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class RequestPermissionsActivity extends Activity {
    private static final String[] a = new String[]{"android.permission.READ_PHONE_STATE"};
    private static List c = new ArrayList();
    private static boolean d = false;
    private String[] b = new String[0];

    public static void a(Context context, Intent intent) {
        c.b("PushSelfShowLog", "enter startPermissionActivity");
        if (context != null) {
            Intent intent2 = new Intent(context.getApplicationContext(), RequestPermissionsActivity.class);
            intent2.addFlags(276824064);
            if (intent != null) {
                intent2.putExtra("previous_intent", intent);
            }
            try {
                context.startActivity(intent2);
            } catch (Throwable e) {
                c.d("PushSelfShowLog", e.toString(), e);
            }
        }
    }

    private static void a(boolean z) {
        d = z;
    }

    public static boolean a(Context context) {
        if (VERSION.SDK_INT < 23) {
            return false;
        }
        if (!a.a(context, context.getPackageName()) || !a.e()) {
            return !a(context, a);
        } else {
            c.a("PushSelfShowLog", "It is system app, no need request permission.");
            return false;
        }
    }

    protected static boolean a(Context context, String[] -l_2_R) {
        if (context == null || -l_2_R == null || -l_2_R.length == 0) {
            return false;
        }
        int length = -l_2_R.length;
        int i = 0;
        while (i < length) {
            String str = -l_2_R[i];
            if (context.checkSelfPermission(str) == 0) {
                i++;
            } else {
                c.a("PushSelfShowLog", str + " need request");
                return false;
            }
        }
        return true;
    }

    private boolean a(String str) {
        return Arrays.asList(a()).contains(str);
    }

    private boolean a(String[] strArr) {
        int i = 0;
        while (i < strArr.length) {
            if (checkSelfPermission(strArr[i]) != 0 && a(strArr[i])) {
                c.b("PushSelfShowLog", "request permissions failed:" + strArr[i]);
                return false;
            }
            i++;
        }
        c.b("PushSelfShowLog", "request all permissions success:");
        return true;
    }

    private boolean a(String[] strArr, int[] iArr) {
        int i = 0;
        while (i < strArr.length) {
            if (iArr[i] != 0 && a(strArr[i])) {
                c.a("PushSelfShowLog", "request permissions failed:" + strArr[i]);
                return false;
            }
            i++;
        }
        c.a("PushSelfShowLog", "request all permissions success:");
        return true;
    }

    private void b(String[] strArr) {
        if (d) {
            c.b("PushSelfShowLog", "has Start PermissionActivity, do nothing");
            finish();
            return;
        }
        a(true);
        try {
            Intent intent = new Intent("huawei.intent.action.REQUEST_PERMISSIONS");
            intent.setPackage("com.huawei.systemmanager");
            intent.putExtra("KEY_HW_PERMISSION_ARRAY", strArr);
            intent.putExtra("KEY_HW_PERMISSION_PKG", getPackageName());
            if (com.huawei.android.pushselfshow.utils.a.a((Context) this, "com.huawei.systemmanager", intent).booleanValue()) {
                try {
                    c.b("PushSelfShowLog", "checkAndRequestPermission: systemmanager permission activity is exist");
                    startActivityForResult(intent, 1357);
                } catch (Throwable e) {
                    c.d("PushSelfShowLog", "checkAndRequestPermission: Exception", e);
                    requestPermissions(strArr, 1357);
                }
            } else {
                c.b("PushSelfShowLog", "checkAndRequestPermission: systemmanager permission activity is not exist");
                requestPermissions(strArr, 1357);
            }
        } catch (Throwable e2) {
            c.d("PushSelfShowLog", e2.toString(), e2);
        }
    }

    private void c() {
        ArrayList arrayList = new ArrayList();
        for (String str : b()) {
            if (checkSelfPermission(str) != 0) {
                arrayList.add(str);
            }
        }
        if (arrayList.size() == 0) {
            c.a("PushSelfShowLog", "unsatisfiedPermissions size is 0, finish");
            finish();
            return;
        }
        this.b = (String[]) arrayList.toArray(new String[arrayList.size()]);
        b(this.b);
    }

    protected String[] a() {
        return a;
    }

    protected String[] b() {
        return a;
    }

    protected void onActivityResult(int i, int i2, Intent intent) {
        if (1357 == i) {
            if (i2 == 0) {
                c.b("PushSelfShowLog", "onActivityResult: RESULT_CANCELED");
            } else if (-1 == i2) {
                c.b("PushSelfShowLog", "onActivityResult: RESULT_OK");
                if (!(this.b == null || this.b.length == 0 || !a(this.b))) {
                    c.b("PushSelfShowLog", "onActivityResult: Permission is granted");
                    c.b("PushSelfShowLog", "mCacheIntents size: " + c.size());
                    for (Intent onReceive : c) {
                        new SelfShowReceiver().onReceive(this, onReceive);
                    }
                }
            }
        }
        try {
            a(true);
            c.clear();
            finish();
        } catch (Throwable e) {
            c.d("PushSelfShowLog", e.toString(), e);
        }
    }

    protected void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        c.a((Context) this);
        c.b("PushSelfShowLog", "enter RequestPermissionsActivity onCreate");
        requestWindowFeature(1);
        Intent intent = getIntent();
        if (intent == null) {
            c.b("PushSelfShowLog", "enter RequestPermissionsActivity onCreate, intent is null, finish");
            finish();
        } else if (VERSION.SDK_INT >= 23) {
            try {
                Bundle extras = intent.getExtras();
                if (extras != null) {
                    Intent intent2 = (Intent) extras.get("previous_intent");
                    c.a("PushSelfShowLog", "mCacheIntents size is " + c.size());
                    if (c.size() >= 30) {
                        c.remove(0);
                    }
                    c.add(intent2);
                }
            } catch (Throwable e) {
                c.d("PushSelfShowLog", e.toString(), e);
            }
            c.a("PushSelfShowLog", "savedInstanceState is " + bundle);
            if (bundle == null) {
                c();
            }
        } else {
            c.b("PushSelfShowLog", "enter RequestPermissionsActivity onCreate, SDK version is less than 23, finish");
            finish();
        }
    }

    protected void onDestroy() {
        c.a("PushSelfShowLog", "enter RequestPermissionsActivity onDestroy");
        super.onDestroy();
    }

    protected void onNewIntent(Intent intent) {
        c.a("PushSelfShowLog", "enter RequestPermissionsActivity onNewIntent");
        super.onNewIntent(intent);
    }

    protected void onPause() {
        c.a("PushSelfShowLog", "RequestPermissionsActivity onPause");
        super.onPause();
    }

    public void onRequestPermissionsResult(int i, String[] strArr, int[] iArr) {
        c.b("PushSelfShowLog", "RequestPermissionsActivity onRequestPermissionsResult");
        if (1357 == i && strArr != null && strArr.length > 0 && iArr != null && iArr.length > 0 && a(strArr, iArr)) {
            for (Intent onReceive : c) {
                new SelfShowReceiver().onReceive(this, onReceive);
            }
        }
        a(false);
        c.clear();
        finish();
    }

    protected void onStop() {
        c.a("PushSelfShowLog", "RequestPermissionsActivity onStop");
        super.onStop();
    }
}
