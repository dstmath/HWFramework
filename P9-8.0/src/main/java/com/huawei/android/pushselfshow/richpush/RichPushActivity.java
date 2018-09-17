package com.huawei.android.pushselfshow.richpush;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import com.huawei.android.pushagent.a.a.c;
import com.huawei.android.pushselfshow.richpush.favorites.FavoritesActivity;
import com.huawei.android.pushselfshow.richpush.html.HtmlViewer;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;

public class RichPushActivity extends Activity {
    public static final String TAG = "PushSelfShowLog";
    String a = "";
    private Class b;
    private Object c;
    private HashMap d = null;
    public Activity m_activity = this;
    public boolean mkInstance = false;

    private HashMap a() {
        HashMap hashMap = new HashMap();
        hashMap.put("html", HtmlViewer.class);
        hashMap.put("favorite", FavoritesActivity.class);
        return hashMap;
    }

    /* JADX WARNING: Missing block: B:2:0x0004, code:
            return;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void a(String str, Class[] clsArr, Object[] objArr) {
        if (this.b != null && this.c != null && !TextUtils.isEmpty(str) && clsArr != null && objArr != null) {
            try {
                this.b.getDeclaredMethod(str, clsArr).invoke(this.c, objArr);
            } catch (NoSuchMethodException e) {
                c.a("PushSelfShowLog", this.b.getName() + " doesn't has " + str + " method,err info " + e.toString());
            } catch (IllegalAccessException e2) {
                c.a("PushSelfShowLog", this.b.getName() + " doesn't has " + str + " method,err info " + e2.toString());
            } catch (IllegalArgumentException e3) {
                c.a("PushSelfShowLog", this.b.getName() + " doesn't has " + str + " method,err info " + e3.toString());
            } catch (InvocationTargetException e4) {
                c.a("PushSelfShowLog", this.b.getName() + " doesn't has " + str + " method,err info " + e4.toString());
            }
        }
    }

    protected boolean isFavoriteList() {
        return true;
    }

    public void onActivityResult(int i, int i2, Intent intent) {
        c.a("PushSelfShowLog", "enter onActivityResult of RichPush");
        if (!this.mkInstance) {
            super.onActivityResult(i, i2, intent);
        }
        a("onActivityResult", new Class[]{Integer.TYPE, Integer.TYPE, Intent.class}, new Object[]{Integer.valueOf(i), Integer.valueOf(i2), intent});
    }

    public void onCreate(Bundle bundle) {
        this.m_activity.setRequestedOrientation(5);
        if (!this.mkInstance) {
            super.onCreate(bundle);
        }
        c.a(this.m_activity);
        c.a("PushSelfShowLog", "enter onCreate of RichPush ");
        if (this.d == null || this.d.isEmpty()) {
            this.d = a();
        }
        Intent intent = this.m_activity.getIntent();
        c.a("PushSelfShowLog", "enter onCreate of RichPush  intent " + intent);
        if (intent != null) {
            if (bundle != null) {
                intent.putExtra("collect_img_disable", bundle.getBoolean("collect_img_disable"));
            }
            try {
                this.a = intent.getStringExtra("type");
            } catch (Exception e) {
                c.d("PushSelfShowLog", "getStringExtra type error");
            }
            c.a("PushSelfShowLog", "the showType from intent is :" + this.a);
            if (isFavoriteList()) {
                this.a = "favorite";
            }
            c.b("PushSelfShowLog", "the showType is :" + this.a);
            if (this.d.containsKey(this.a)) {
                this.b = (Class) this.d.get(this.a);
                try {
                    this.c = this.b.getConstructor(new Class[0]).newInstance(new Object[0]);
                    Method declaredMethod = this.b.getDeclaredMethod("setActivity", new Class[]{Activity.class});
                    c.a("PushSelfShowLog", "call setActivity in RichPush!");
                    declaredMethod.invoke(this.c, new Object[]{this.m_activity});
                    this.b.getDeclaredMethod("onCreate", new Class[]{Intent.class}).invoke(this.c, new Object[]{intent});
                } catch (NoSuchMethodException e2) {
                    c.a("PushSelfShowLog", this.b.getName() + " doesn't has onCreate method,err info " + e2.toString());
                } catch (InstantiationException e3) {
                    c.a("PushSelfShowLog", this.b.getName() + " doesn't has onCreate method,err info " + e3.toString());
                } catch (IllegalAccessException e4) {
                    c.a("PushSelfShowLog", this.b.getName() + " doesn't has onCreate method,err info " + e4.toString());
                } catch (IllegalArgumentException e5) {
                    c.a("PushSelfShowLog", this.b.getName() + " doesn't has onCreate method,err info " + e5.toString());
                } catch (InvocationTargetException e6) {
                    c.a("PushSelfShowLog", this.b.getName() + " doesn't has onCreate method,err info " + e6.toString());
                }
                return;
            }
            c.a("PushSelfShowLog", "the showType is invalid");
            finish();
            return;
        }
        finish();
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        a("onCreateOptionsMenu", new Class[]{Menu.class}, new Object[]{menu});
        return super.onCreateOptionsMenu(menu);
    }

    public void onDestroy() {
        c.a("PushSelfShowLog", "enter onDestroy of RichPush");
        if (!this.mkInstance) {
            super.onDestroy();
        }
        a("onDestroy", new Class[0], new Object[0]);
    }

    public boolean onKeyDown(int i, KeyEvent keyEvent) {
        c.a("PushSelfShowLog", "enter onKeyDown of RichPush");
        a("onKeyDown", new Class[]{Integer.TYPE, KeyEvent.class}, new Object[]{Integer.valueOf(i), keyEvent});
        return true;
    }

    public boolean onOptionsItemSelected(MenuItem menuItem) {
        a("onOptionsItemSelected", new Class[]{MenuItem.class}, new Object[]{menuItem});
        return super.onOptionsItemSelected(menuItem);
    }

    public void onPause() {
        c.a("PushSelfShowLog", "enter onPause of RichPush");
        if (!this.mkInstance) {
            super.onPause();
        }
        a("onPause", new Class[0], new Object[0]);
    }

    public boolean onPrepareOptionsMenu(Menu menu) {
        a("onPrepareOptionsMenu", new Class[]{Menu.class}, new Object[]{menu});
        return super.onPrepareOptionsMenu(menu);
    }

    public void onRequestPermissionsResult(int i, String[] strArr, int[] iArr) {
        c.a("PushSelfShowLog", "enter onRequestPermissionsResult of RichPush");
        if (!this.mkInstance) {
            super.onRequestPermissionsResult(i, strArr, iArr);
        }
        a("onRequestPermissionsResult", new Class[]{Integer.TYPE, String[].class, int[].class}, new Object[]{Integer.valueOf(i), strArr, iArr});
    }

    public void onRestart() {
        c.a("PushSelfShowLog", "enter onRestart of RichPush");
        if (!this.mkInstance) {
            super.onRestart();
        }
        a("onRestart", new Class[0], new Object[0]);
    }

    protected void onResume() {
        c.a("PushSelfShowLog", "enter onResume of RichPush");
        if (!this.mkInstance) {
            super.onResume();
        }
        a("onResume", new Class[0], new Object[0]);
    }

    protected void onSaveInstanceState(Bundle bundle) {
        c.a("PushSelfShowLog", "enter onSaveInstanceState of RichPush");
        if (!this.mkInstance) {
            super.onSaveInstanceState(bundle);
        }
        a("onSaveInstanceState", new Class[]{Bundle.class}, new Object[]{bundle});
    }

    public void onStart() {
        c.a("PushSelfShowLog", "enter onStart of RichPush");
        if (!this.mkInstance) {
            super.onStart();
        }
        a("onStart", new Class[0], new Object[0]);
    }

    public void onStop() {
        c.a("PushSelfShowLog", "enter onStop of RichPush， and mkInstance is " + this.mkInstance + "and pActivityClass is " + this.b + ",and pActivityInstance is " + this.c);
        if (!this.mkInstance) {
            super.onStop();
        }
        a("onStop", new Class[0], new Object[0]);
    }

    public void setActivity(Activity activity) {
        this.m_activity = activity;
        this.mkInstance = true;
    }
}
