package com.huawei.android.pushselfshow.richpush.html;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build.VERSION;
import android.os.Bundle;
import android.os.Message;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.webkit.WebSettings.PluginState;
import android.webkit.WebView;
import android.widget.RelativeLayout;
import com.huawei.android.pushselfshow.richpush.RichPushActivity;
import com.huawei.android.pushselfshow.richpush.html.api.ExposedJsApi;
import com.huawei.android.pushselfshow.richpush.provider.RichMediaProvider;
import com.huawei.android.pushselfshow.richpush.tools.Console;
import com.huawei.android.pushselfshow.utils.a.e;
import com.huawei.android.pushselfshow.utils.b.b;
import com.huawei.android.pushselfshow.utils.c;
import com.huawei.android.pushselfshow.utils.c.a;
import com.huawei.android.pushselfshow.utils.d;
import com.huawei.systemmanager.rainbow.comm.request.util.RainbowRequestBasic.CheckVersionField;
import java.io.File;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Method;

public class HtmlViewer implements a {
    public static final String TAG = "PushSelfShowLog";
    PageProgressView a;
    c b = new c(this);
    c c = new c(this);
    private Activity d;
    public b dtl = null;
    private WebView e;
    private com.huawei.android.pushselfshow.richpush.tools.a f;
    private com.huawei.android.pushselfshow.c.a g = null;
    private String h;
    private ExposedJsApi i;
    private MenuItem j;
    private MenuItem k;
    private MenuItem l;
    private boolean m;
    private boolean n = false;
    private boolean o = false;
    private AlertDialog p = null;
    private AlertDialog q = null;
    private boolean r = false;

    private View a(Context context) {
        View view = null;
        View inflate = ((LayoutInflater) context.getSystemService("layout_inflater")).inflate(d.c(context, "hwpush_collect_tip_image"), null);
        try {
            Class cls = Class.forName("huawei.android.widget.DialogContentHelper");
            Object newInstance = cls.getDeclaredConstructor(new Class[]{Boolean.TYPE, Boolean.TYPE, Context.class}).newInstance(new Object[]{Boolean.valueOf(true), Boolean.valueOf(true), context});
            Method declaredMethod = cls.getDeclaredMethod("beginLayout", new Class[0]);
            Method declaredMethod2 = cls.getDeclaredMethod("insertView", new Class[]{View.class, OnClickListener.class});
            Method declaredMethod3 = cls.getDeclaredMethod("insertBodyText", new Class[]{CharSequence.class});
            Method declaredMethod4 = cls.getDeclaredMethod("endLayout", new Class[0]);
            declaredMethod.invoke(newInstance, new Object[0]);
            declaredMethod2.invoke(newInstance, new Object[]{inflate, null});
            String string = this.d.getString(d.a(context, "hwpush_collect_tip"));
            declaredMethod3.invoke(newInstance, new Object[]{string});
            return (View) declaredMethod4.invoke(newInstance, new Object[0]);
        } catch (ClassNotFoundException e) {
            com.huawei.android.pushagent.a.a.c.d("PushSelfShowLog", "DialogContentHelper ClassNotFoundException");
            return view;
        } catch (Throwable e2) {
            com.huawei.android.pushagent.a.a.c.d("PushSelfShowLog", e2.toString(), e2);
            return view;
        } catch (Throwable e22) {
            com.huawei.android.pushagent.a.a.c.d("PushSelfShowLog", e22.toString(), e22);
            return view;
        } catch (Throwable e222) {
            com.huawei.android.pushagent.a.a.c.d("PushSelfShowLog", e222.toString(), e222);
            return view;
        } catch (Throwable e2222) {
            com.huawei.android.pushagent.a.a.c.d("PushSelfShowLog", e2222.toString(), e2222);
            return view;
        } catch (Throwable e22222) {
            com.huawei.android.pushagent.a.a.c.d("PushSelfShowLog", e22222.toString(), e22222);
            return view;
        } catch (Throwable e222222) {
            com.huawei.android.pushagent.a.a.c.d("PushSelfShowLog", e222222.toString(), e222222);
            return view;
        }
    }

    private void a() {
        this.e.getSettings().setJavaScriptEnabled(true);
        if (VERSION.SDK_INT >= 11 && VERSION.SDK_INT <= 16) {
            this.e.removeJavascriptInterface("searchBoxJavaBridge_");
            this.e.removeJavascriptInterface("accessibility");
            this.e.removeJavascriptInterface("accessibilityTraversal");
        }
        if (VERSION.SDK_INT <= 18) {
            this.e.getSettings().setSavePassword(false);
        }
        this.e.getSettings().setPluginState(PluginState.ON);
        this.e.getSettings().setLoadsImagesAutomatically(true);
        this.e.getSettings().setDomStorageEnabled(true);
        this.e.getSettings().setSupportZoom(true);
        this.e.setScrollBarStyle(0);
        this.e.setHorizontalScrollBarEnabled(false);
        this.e.setVerticalScrollBarEnabled(false);
        this.e.getSettings().setSupportMultipleWindows(true);
        this.e.setDownloadListener(new a(this));
        this.e.setOnTouchListener(new c(this));
        this.e.setWebChromeClient(new d(this));
        this.e.setWebViewClient(new e(this));
    }

    private void a(Activity activity) {
        if (activity != null) {
            this.l.setEnabled(false);
            this.r = true;
            if (VERSION.SDK_INT >= 23 && !com.huawei.android.pushselfshow.utils.a.e(activity) && com.huawei.android.pushselfshow.utils.a.g(activity) && activity.checkSelfPermission("com.huawei.pushagent.permission.RICHMEDIA_PROVIDER") != 0) {
                a(new String[]{"com.huawei.pushagent.permission.RICHMEDIA_PROVIDER"}, 10003);
                return;
            }
            new Thread(new h(this, activity)).start();
        }
    }

    private void a(String str) {
        ActionBar actionBar = this.d.getActionBar();
        if (actionBar != null) {
            actionBar.setTitle(str);
        }
    }

    private void a(String[] strArr, int i) {
        try {
            Intent intent = new Intent("huawei.intent.action.REQUEST_PERMISSIONS");
            intent.setPackage("com.huawei.systemmanager");
            intent.putExtra("KEY_HW_PERMISSION_ARRAY", strArr);
            intent.putExtra("KEY_HW_PERMISSION_PKG", this.d.getPackageName());
            if (com.huawei.android.pushselfshow.utils.a.a(this.d, "com.huawei.systemmanager", intent).booleanValue()) {
                try {
                    com.huawei.android.pushagent.a.a.c.b("PushSelfShowLog", "checkAndRequestPermission: systemmanager permission activity is exist");
                    this.d.startActivityForResult(intent, i);
                    return;
                } catch (Throwable e) {
                    com.huawei.android.pushagent.a.a.c.d("PushSelfShowLog", "checkAndRequestPermission: Exception", e);
                    this.d.requestPermissions(strArr, i);
                    return;
                }
            }
            com.huawei.android.pushagent.a.a.c.b("PushSelfShowLog", "checkAndRequestPermission: systemmanager permission activity is not exist");
            this.d.requestPermissions(strArr, i);
        } catch (Throwable e2) {
            com.huawei.android.pushagent.a.a.c.d("PushSelfShowLog", e2.toString(), e2);
        }
    }

    private int b(Activity activity) {
        int i = 0;
        if (activity == null) {
            return 0;
        }
        Cursor cursor = null;
        try {
            cursor = e.a().a((Context) activity, RichMediaProvider.a.f, "SELECT pushmsg._id,pushmsg.msg,pushmsg.token,pushmsg.url,notify.bmp FROM pushmsg LEFT OUTER JOIN notify ON pushmsg.url = notify.url order by pushmsg._id desc limit 1000;", null);
            if (cursor != null) {
                i = cursor.getCount();
            }
            if (cursor != null) {
                cursor.close();
            }
        } catch (Throwable e) {
            com.huawei.android.pushagent.a.a.c.d("PushSelfShowLog", e.toString(), e);
            if (cursor != null) {
                cursor.close();
            }
        } catch (Throwable th) {
            if (cursor != null) {
                cursor.close();
            }
        }
        com.huawei.android.pushagent.a.a.c.a("PushSelfShowLog", "currentExistCount:" + i);
        return i;
    }

    private void c(Activity activity) {
        this.q = new Builder(activity, com.huawei.android.pushselfshow.utils.a.i(activity)).setTitle(d.a(activity, "hwpush_dialog_limit_title")).setMessage(d.a(activity, "hwpush_dialog_limit_message")).setNegativeButton(17039360, null).setPositiveButton(d.a(activity, "hwpush_dialog_limit_ok"), new j(this)).setOnDismissListener(new i(this, activity)).create();
        this.q.show();
    }

    private void d(Activity activity) {
        if (activity != null) {
            Intent intent = new Intent(activity, RichPushActivity.class);
            if (this.g != null) {
                try {
                    intent.putExtra("selfshow_token", this.g.d());
                    intent.putExtra("selfshow_info", com.huawei.android.pushagent.a.a.a.d.a(null, new String(this.g.c(), "UTF-8")));
                } catch (UnsupportedEncodingException e) {
                    com.huawei.android.pushagent.a.a.c.d("PushSelfShowLog", e.toString());
                }
            }
            intent.setFlags(268468240);
            intent.putExtra("selfshowMsgOutOfBound", true);
            intent.setPackage(activity.getPackageName());
            activity.finish();
            activity.startActivity(intent);
        }
    }

    public void downLoadFailed() {
        com.huawei.android.pushagent.a.a.c.a("PushSelfShowLog", "downLoadFailed:");
        this.c = null;
        showErrorHtmlURI(this.d.getString(d.a(this.d, "hwpush_download_failed")));
    }

    public void downLoadSuccess(String str) {
        try {
            com.huawei.android.pushagent.a.a.c.a("PushSelfShowLog", "downLoadSuccess:" + str + "，and start loadLocalZip");
            loadLocalZip(str);
        } catch (Throwable e) {
            com.huawei.android.pushagent.a.a.c.d("PushSelfShowLog", "downLoadSuccess failed", e);
        }
    }

    public void enableJavaJS(String str) {
        try {
            com.huawei.android.pushagent.a.a.c.a("PushSelfShowLog", "enable JavaJs support and indexFileUrl is " + str);
            String str2 = null;
            if (str != null) {
                str2 = str.substring(0, str.lastIndexOf("/"));
            }
            com.huawei.android.pushagent.a.a.c.a("PushSelfShowLog", "m_activity is " + this.d);
            com.huawei.android.pushagent.a.a.c.a("PushSelfShowLog", "webView is " + this.e);
            com.huawei.android.pushagent.a.a.c.a("PushSelfShowLog", "localPath is " + str2);
            if (this.g.B() == 0) {
                com.huawei.android.pushagent.a.a.c.a("PushSelfShowLog", "pushmsg.needUserId false");
                this.i = new ExposedJsApi(this.d, this.e, str2, false, "");
            } else {
                com.huawei.android.pushagent.a.a.c.a("PushSelfShowLog", "pushmsg.needUserId true");
                this.i = new ExposedJsApi(this.d, this.e, str2, true, this.g.K());
            }
            this.e.addJavascriptInterface(new Console(), "console");
            this.e.addJavascriptInterface(this.i, "_nativeApi");
        } catch (Throwable e) {
            com.huawei.android.pushagent.a.a.c.d("PushSelfShowLog", "enable JavaJs support failed ", e);
        }
    }

    public void handleMessage(Message message) {
        com.huawei.android.pushagent.a.a.c.a("PushSelfShowLog", "handleMessage " + message.what + "," + message.toString());
        switch (message.what) {
            case 1:
                downLoadSuccess((String) message.obj);
                return;
            case 2:
                downLoadFailed();
                return;
            case CheckVersionField.CHECK_VERSION_MAX_UPDATE_DAY /*1000*/:
                c(this.d);
                return;
            default:
                return;
        }
    }

    public void loadLocalZip(String str) {
        if (str != null && str.length() > 0) {
            this.h = com.huawei.android.pushselfshow.richpush.tools.d.a(this.d, str);
            if (this.h != null && this.h.length() > 0) {
                Uri fromFile = Uri.fromFile(new File(this.h));
                enableJavaJS(this.h);
                this.g.d(fromFile.toString());
                this.g.f("text/html_local");
                this.f.a(this.g);
                this.e.loadUrl(fromFile.toString());
                return;
            }
            com.huawei.android.pushagent.a.a.c.d("PushSelfShowLog", "check index.html file failed");
            this.c = null;
        }
        showErrorHtmlURI(this.d.getString(d.a(this.d, "hwpush_invalid_content")));
    }

    public void onActivityResult(int i, int i2, Intent intent) {
        try {
            com.huawei.android.pushagent.a.a.c.a("PushSelfShowLog", "run HtmlViewer onActivityResult");
            if (this.i != null) {
                this.i.onActivityResult(i, i2, intent);
            }
            if (10003 != i) {
                if (10004 != i) {
                    if (10005 == i && this.g != null) {
                        loadLocalZip(this.g.x());
                    }
                } else if (this.g != null && this.c != null) {
                    this.dtl = new b(this.c, this.d, this.g.x(), com.huawei.android.pushselfshow.richpush.tools.b.a("application/zip"));
                    this.dtl.b();
                }
            } else if (i2 == 0) {
                com.huawei.android.pushagent.a.a.c.b("PushSelfShowLog", "onActivityResult: RESULT_CANCELED");
                this.l.setEnabled(true);
                this.r = false;
            } else if (-1 == i2) {
                com.huawei.android.pushagent.a.a.c.b("PushSelfShowLog", "onActivityResult: RESULT_OK");
                if (this.d.checkSelfPermission("com.huawei.pushagent.permission.RICHMEDIA_PROVIDER") != 0) {
                    this.l.setEnabled(true);
                    this.r = false;
                    return;
                }
                com.huawei.android.pushagent.a.a.c.b("PushSelfShowLog", "onActivityResult: Permission is granted");
                new Thread(new f(this)).start();
            }
        } catch (Throwable e) {
            com.huawei.android.pushagent.a.a.c.d("PushSelfShowLog", e.toString(), e);
        }
    }

    public void onCreate(Intent intent) {
        com.huawei.android.pushagent.a.a.c.b("PushSelfShowLog", "HtmlViewer onCreate");
        if (intent != null) {
            try {
                this.m = intent.getBooleanExtra("selfshow_from_list", false);
                this.r = intent.getBooleanExtra("collect_img_disable", false);
                com.huawei.android.pushagent.a.a.c.a("PushSelfShowLog", "mCollectImgDisable:" + this.r);
                this.f = new com.huawei.android.pushselfshow.richpush.tools.a(this.d);
                ActionBar actionBar = this.d.getActionBar();
                if (actionBar != null) {
                    actionBar.setDisplayHomeAsUpEnabled(true);
                }
                this.d.setRequestedOrientation(5);
                View relativeLayout = new RelativeLayout(this.d);
                int c = d.c(this.d, "hwpush_msg_show");
                if (com.huawei.android.pushagent.a.a.a.a() < 11) {
                    c = d.c(this.d, "hwpush_msg_show_before_emui5_0");
                }
                RelativeLayout relativeLayout2 = (RelativeLayout) this.d.getLayoutInflater().inflate(c, null);
                relativeLayout.addView(relativeLayout2);
                this.d.setContentView(relativeLayout);
                this.a = (PageProgressView) relativeLayout2.findViewById(d.e(this.d, "hwpush_progressbar"));
                this.e = (WebView) relativeLayout2.findViewById(d.e(this.d, "hwpush_msg_show_view"));
                a();
                if (intent.hasExtra("selfshow_info")) {
                    this.g = new com.huawei.android.pushselfshow.c.a(intent.getByteArrayExtra("selfshow_info"), intent.getByteArrayExtra("selfshow_token"));
                    if (this.g.b()) {
                        com.huawei.android.pushagent.a.a.c.a("PushSelfShowLog", "pushmsg.rpct:" + this.g.z());
                        this.f.a(this.g);
                    } else {
                        com.huawei.android.pushagent.a.a.c.a("PushSelfShowLog", "parseMessage failed");
                        return;
                    }
                }
                com.huawei.android.pushagent.a.a.c.a("PushSelfShowLog", "pushmsg is null");
                showErrorHtmlURI(this.d.getString(d.a(this.d, "hwpush_invalid_content")));
                if (this.g == null) {
                    com.huawei.android.pushagent.a.a.c.a("PushSelfShowLog", "pushmsg is null :");
                    this.g = new com.huawei.android.pushselfshow.c.a();
                } else {
                    com.huawei.android.pushagent.a.a.c.a("PushSelfShowLog", "fileurl :" + this.g.x() + ", the pushmsg is " + this.g.toString());
                }
                com.huawei.android.pushagent.a.a.c.b("PushSelfShowLog", "pushmsg.rpct:" + this.g.z());
                if (!"application/zip".equals(this.g.z())) {
                    if ("application/zip_local".equals(this.g.z())) {
                        loadLocalZip(this.g.x());
                    } else {
                        if (!"text/html".equals(this.g.z())) {
                            if (!"text/html_local".equals(this.g.z())) {
                                showErrorHtmlURI(this.d.getString(d.a(this.d, "hwpush_invalid_content")));
                            }
                        }
                        String str = null;
                        if ("text/html_local".equals(this.g.z())) {
                            str = this.g.x();
                        }
                        enableJavaJS(str);
                        this.e.loadUrl(this.g.x());
                    }
                } else if (-1 != com.huawei.android.pushagent.a.a.a.a(this.d)) {
                    this.dtl = new b(this.c, this.d, this.g.x(), com.huawei.android.pushselfshow.richpush.tools.b.a("application/zip"));
                    this.dtl.b();
                } else {
                    com.huawei.android.pushagent.a.a.c.a("PushSelfShowLog", "no network. can not load message");
                }
            } catch (Throwable e) {
                com.huawei.android.pushagent.a.a.c.d("PushSelfShowLog", "call" + HtmlViewer.class.getName() + " onCreate(Intent intent) err: " + e.toString(), e);
            }
            return;
        }
        com.huawei.android.pushagent.a.a.c.b("PushSelfShowLog", "onCreate, intent is null");
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        com.huawei.android.pushagent.a.a.c.b("PushSelfShowLog", "HtmlViewer onCreateOptionsMenu:" + menu);
        this.d.getMenuInflater().inflate(d.d(this.d, "hwpush_msg_show_menu"), menu);
        return true;
    }

    public void onDestroy() {
        try {
            if (this.p != null && this.p.isShowing()) {
                this.p.dismiss();
            }
            if (this.q != null) {
                if (this.q.isShowing()) {
                    this.q.dismiss();
                }
            }
            if (this.i != null) {
                this.i.onDestroy();
            }
            if (!(this.h == null || this.o)) {
                String substring = this.h.substring(0, this.h.lastIndexOf("/"));
                com.huawei.android.pushagent.a.a.c.a("PushSelfShowLog", "try to remove dir " + substring);
                com.huawei.android.pushselfshow.utils.a.a(new File(substring));
            }
            if (this.dtl != null && this.dtl.e) {
                com.huawei.android.pushagent.a.a.c.a("PushSelfShowLog", "cancel ProgressDialog loading dialog when richpush file is downloading");
                this.dtl.a();
                this.c = null;
            }
            this.e.stopLoading();
            this.e = null;
        } catch (IndexOutOfBoundsException e) {
            com.huawei.android.pushagent.a.a.c.a("PushSelfShowLog", "remove unsuccess ,maybe removed before");
        } catch (Exception e2) {
            com.huawei.android.pushagent.a.a.c.a("PushSelfShowLog", "remove unsuccess ,maybe removed before");
        }
    }

    public boolean onKeyDown(int i, KeyEvent keyEvent) {
        if (i == 4 && keyEvent.getAction() == 0) {
            if (this.m) {
                Intent intent = new Intent(this.d, RichPushActivity.class);
                intent.putExtra("type", "favorite");
                intent.setPackage(this.d.getPackageName());
                this.d.finish();
                this.d.startActivity(intent);
            } else {
                this.d.finish();
            }
        }
        return true;
    }

    public boolean onOptionsItemSelected(MenuItem menuItem) {
        com.huawei.android.pushagent.a.a.c.b("PushSelfShowLog", "HtmlViewer onOptionsItemSelected:" + menuItem);
        if (menuItem != null) {
            int itemId = menuItem.getItemId();
            if (itemId == 16908332) {
                onKeyDown(4, new KeyEvent(0, 4));
            } else if (itemId != d.e(this.d, "hwpush_menu_back")) {
                if (itemId != d.e(this.d, "hwpush_menu_forward")) {
                    if (itemId == d.e(this.d, "hwpush_menu_refresh")) {
                        setProgress(0);
                        this.e.reload();
                    } else if (itemId == d.e(this.d, "hwpush_menu_collect")) {
                        com.huawei.android.pushagent.a.a.e eVar = new com.huawei.android.pushagent.a.a.e(this.d, "push_client_self_info");
                        String str = "isFirstCollect";
                        Object obj = 1;
                        if (eVar.c("isFirstCollect")) {
                            obj = null;
                        }
                        if (obj == null) {
                            a(this.d);
                        } else {
                            this.p = new Builder(this.d).setPositiveButton(d.a(this.d, "hwpush_collect_tip_known"), new b(this, eVar)).create();
                            if (com.huawei.android.pushagent.a.a.a.a() <= 9) {
                                this.p.setTitle(d.a(this.d, "hwpush_msg_collect"));
                                this.p.setMessage(this.d.getString(d.a(this.d, "hwpush_collect_tip")));
                            } else {
                                View a = a(this.d);
                                if (a == null) {
                                    this.p.setTitle(d.a(this.d, "hwpush_msg_collect"));
                                    this.p.setMessage(this.d.getString(d.a(this.d, "hwpush_collect_tip")));
                                } else {
                                    this.p.setView(a);
                                }
                            }
                            this.p.show();
                        }
                    }
                } else if (this.e != null && this.e.canGoForward()) {
                    com.huawei.android.pushagent.a.a.c.a("PushSelfShowLog", " can Go Forward " + this.e.canGoForward());
                    this.e.goForward();
                }
            } else if (this.e != null && this.e.canGoBack()) {
                com.huawei.android.pushagent.a.a.c.a("PushSelfShowLog", "can go back " + this.e.canGoBack());
                this.e.goBack();
            }
            return true;
        }
        com.huawei.android.pushagent.a.a.c.d("PushSelfShowLog", "onOptionsItemSelected, item is null");
        return false;
    }

    public void onPause() {
        if (this.i != null) {
            this.i.onPause();
        }
        try {
            this.e.getClass().getMethod("onPause", new Class[0]).invoke(this.e, (Object[]) null);
        } catch (Throwable e) {
            com.huawei.android.pushagent.a.a.c.d("PushSelfShowLog", "htmlviewer onpause error", e);
        }
    }

    public boolean onPrepareOptionsMenu(Menu menu) {
        com.huawei.android.pushagent.a.a.c.b("PushSelfShowLog", "HtmlViewer onPrepareOptionsMenu:" + menu);
        this.j = menu.findItem(d.e(this.d, "hwpush_menu_back"));
        this.k = menu.findItem(d.e(this.d, "hwpush_menu_forward"));
        this.l = menu.findItem(d.e(this.d, "hwpush_menu_collect"));
        if (this.e != null) {
            if (this.e.canGoBack()) {
                this.j.setEnabled(true);
            } else {
                this.j.setEnabled(false);
            }
            if (this.e.canGoForward()) {
                this.k.setEnabled(true);
            } else {
                this.k.setEnabled(false);
            }
        }
        if (this.m || this.r) {
            this.l.setEnabled(false);
            this.r = true;
        }
        return true;
    }

    public void onRequestPermissionsResult(int i, String[] strArr, int[] iArr) {
        com.huawei.android.pushagent.a.a.c.a("PushSelfShowLog", "enter HtmlViewer onRequestPermissionsResult");
        if (10003 != i) {
            if (10004 != i) {
                if (10005 == i && this.g != null) {
                    loadLocalZip(this.g.x());
                }
            } else if (this.g != null && this.c != null) {
                this.dtl = new b(this.c, this.d, this.g.x(), com.huawei.android.pushselfshow.richpush.tools.b.a("application/zip"));
                this.dtl.b();
            }
        } else if (iArr != null && iArr.length > 0 && iArr[0] == 0) {
            new Thread(new g(this)).start();
        } else {
            this.l.setEnabled(true);
            this.r = false;
        }
    }

    public void onResume() {
        com.huawei.android.pushagent.a.a.c.b("PushSelfShowLog", "HtmlViewer onResume");
        if (this.i != null) {
            this.i.onResume();
        }
        try {
            this.e.getClass().getMethod("onResume", new Class[0]).invoke(this.e, (Object[]) null);
        } catch (Throwable e) {
            com.huawei.android.pushagent.a.a.c.d("PushSelfShowLog", "htmlviewer onResume error", e);
        }
    }

    public void onSaveInstanceState(Bundle bundle) {
        bundle.putBoolean("collect_img_disable", this.r);
    }

    public void onStop() {
        if (this.i != null) {
            this.i.onPause();
        }
    }

    public String prepareJS(String str) {
        Object obj = 1;
        try {
            String str2;
            String str3 = b.b(this.d) + File.separator + this.d.getPackageName().replace(".", "");
            if (!new File(str3).exists()) {
                if (new File(str3).mkdirs()) {
                    com.huawei.android.pushagent.a.a.c.e("PushSelfShowLog", "mkdir true");
                }
            }
            com.huawei.android.pushagent.a.a.c.e("PushSelfShowLog", "prepareJS fileHeader is " + str3);
            String str4 = null;
            String str5 = str3 + File.separator + "newpush.js";
            File file = new File(str5);
            if (file.exists()) {
                if (System.currentTimeMillis() - file.lastModified() > 1300000000) {
                    obj = null;
                }
                if (obj == null) {
                    com.huawei.android.pushagent.a.a.c.a("PushSelfShowLog", "new push.js may be out of date ,or try to update");
                    if (com.huawei.android.pushagent.a.a.a.a(this.d) != -1 && new b().b(this.d, "http://open.hicloud.com/android/push1.0.js", str5) && new File(str5).exists()) {
                        str4 = str5;
                        com.huawei.android.pushagent.a.a.c.e("PushSelfShowLog", "prepareJS dlUtil.downLoadSgThread  pushUrl is " + str5);
                    }
                } else {
                    str4 = str5;
                    com.huawei.android.pushagent.a.a.c.e("PushSelfShowLog", "prepareJS  not arrival update  pushUrl is " + str5);
                }
            } else if (com.huawei.android.pushagent.a.a.a.a(this.d) != -1 && new b().b(this.d, "http://open.hicloud.com/android/push1.0.js", str5) && new File(str5).exists()) {
                str4 = str5;
                com.huawei.android.pushagent.a.a.c.e("PushSelfShowLog", "prepareJS new js isnot exist, so  downloaded  pushUrl is " + str5);
            }
            if (str4 == null || str4.length() == 0) {
                com.huawei.android.pushagent.a.a.c.e("PushSelfShowLog", "  pushUrl is " + str4);
                str2 = str3 + File.separator + "push1.0.js";
                com.huawei.android.pushagent.a.a.c.e("PushSelfShowLog", "  pushjsPath is " + str2);
                if (new File(str2).exists()) {
                    if (new File(str2).delete()) {
                        com.huawei.android.pushagent.a.a.c.a("PushSelfShowLog", "delete pushjsPath success");
                    }
                    com.huawei.android.pushagent.a.a.c.e("PushSelfShowLog", "prepareJS new js  is not prepared so use local  pushUrl is " + str4);
                } else {
                    com.huawei.android.pushagent.a.a.c.e("PushSelfShowLog", " new File(pushjsPath) not exists() ");
                }
                com.huawei.android.pushselfshow.utils.a.a(this.d, "pushresources" + File.separator + "push1.0.js", str2);
                str4 = str2;
            }
            if (str4.length() > 0) {
                com.huawei.android.pushagent.a.a.c.e("PushSelfShowLog", "  pushUrl is " + str4);
                str2 = str4.substring(str4.lastIndexOf("/"));
                com.huawei.android.pushagent.a.a.c.a("PushSelfShowLog", " pushUrlName is %s,destPath is %s ", str2, str.substring("file://".length(), str.lastIndexOf("/")) + str2);
                com.huawei.android.pushselfshow.utils.a.a(new File(str4), new File(r5));
                return "." + str2;
            }
        } catch (Throwable e) {
            com.huawei.android.pushagent.a.a.c.e("PushSelfShowLog", "prepareJS", e);
        } catch (Throwable e2) {
            com.huawei.android.pushagent.a.a.c.e("PushSelfShowLog", "prepareJS", e2);
        }
        return "http://open.hicloud.com/android/push1.0.js";
    }

    public void setActivity(Activity activity) {
        this.d = activity;
    }

    public void setProgress(int i) {
        if (i < 100) {
            if (!this.n) {
                this.a.setVisibility(0);
                this.n = true;
            }
            this.a.a((i * 10000) / 100);
            return;
        }
        this.a.a(10000);
        this.a.setVisibility(4);
        this.n = false;
    }

    public void showErrorHtmlURI(String str) {
        try {
            String a = new com.huawei.android.pushselfshow.richpush.tools.c(this.d, str).a();
            com.huawei.android.pushagent.a.a.c.a("PushSelfShowLog", "showErrorHtmlURI,filePath is " + a);
            if (a != null) {
                if (a.length() > 0) {
                    Uri fromFile = Uri.fromFile(new File(a));
                    enableJavaJS(null);
                    this.e.loadUrl(fromFile.toString());
                }
            }
        } catch (Throwable e) {
            com.huawei.android.pushagent.a.a.c.d("PushSelfShowLog", "showErrorHtmlURI failed", e);
        }
        if (this.d.getString(d.a(this.d, "hwpush_download_failed")).equals(str)) {
            com.huawei.android.pushselfshow.utils.a.a(this.d, "12", this.g, -1);
            return;
        }
        com.huawei.android.pushselfshow.utils.a.a(this.d, "6", this.g, -1);
    }
}
