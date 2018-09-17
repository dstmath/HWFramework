package com.android.server.webkit;

import android.app.ActivityManagerNative;
import android.app.AppGlobals;
import android.content.ContentResolver;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.IPackageDeleteObserver.Stub;
import android.content.pm.IPackageManager;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.UserInfo;
import android.content.res.XmlResourceParser;
import android.os.Build;
import android.os.RemoteException;
import android.os.UserManager;
import android.provider.Settings.Global;
import android.util.AndroidRuntimeException;
import android.util.Log;
import android.webkit.WebViewFactory;
import android.webkit.WebViewProviderInfo;
import com.android.internal.util.XmlUtils;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.xmlpull.v1.XmlPullParserException;

public class SystemImpl implements SystemInterface {
    private static final int PACKAGE_FLAGS = 268443840;
    private static final String TAG = null;
    private static final String TAG_AVAILABILITY = "availableByDefault";
    private static final String TAG_DESCRIPTION = "description";
    private static final String TAG_FALLBACK = "isFallback";
    private static final String TAG_PACKAGE_NAME = "packageName";
    private static final String TAG_SIGNATURE = "signature";
    private static final String TAG_START = "webviewproviders";
    private static final String TAG_WEBVIEW_PROVIDER = "webviewprovider";
    private final WebViewProviderInfo[] mWebViewProviderPackages;

    /* renamed from: com.android.server.webkit.SystemImpl.1 */
    class AnonymousClass1 extends Stub {
        final /* synthetic */ Context val$context;

        AnonymousClass1(Context val$context) {
            this.val$context = val$context;
        }

        public void packageDeleted(String packageName, int returnCode) {
            SystemImpl.this.enablePackageForAllUsers(this.val$context, packageName, false);
        }
    }

    private static class LazyHolder {
        private static final SystemImpl INSTANCE = null;

        static {
            /* JADX: method processing error */
/*
            Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.server.webkit.SystemImpl.LazyHolder.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:263)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.server.webkit.SystemImpl.LazyHolder.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 6 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 7 more
*/
            /*
            // Can't load method instructions.
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.server.webkit.SystemImpl.LazyHolder.<clinit>():void");
        }

        private LazyHolder() {
        }
    }

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.server.webkit.SystemImpl.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.server.webkit.SystemImpl.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 5 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e9
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 6 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.server.webkit.SystemImpl.<clinit>():void");
    }

    /* synthetic */ SystemImpl(SystemImpl systemImpl) {
        this();
    }

    public static SystemImpl getInstance() {
        return LazyHolder.INSTANCE;
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private SystemImpl() {
        int numFallbackPackages = 0;
        int numAvailableByDefaultPackages = 0;
        int numAvByDefaultAndNotFallback = 0;
        XmlResourceParser xmlResourceParser = null;
        List<WebViewProviderInfo> webViewProviders = new ArrayList();
        xmlResourceParser = AppGlobals.getInitialApplication().getResources().getXml(17891332);
        XmlUtils.beginDocument(xmlResourceParser, TAG_START);
        while (true) {
            XmlUtils.nextElement(xmlResourceParser);
            String element = xmlResourceParser.getName();
            if (element == null) {
                break;
            } else if (element.equals(TAG_WEBVIEW_PROVIDER)) {
                String packageName = xmlResourceParser.getAttributeValue(null, TAG_PACKAGE_NAME);
                if (packageName == null) {
                    break;
                }
                try {
                    String description = xmlResourceParser.getAttributeValue(null, TAG_DESCRIPTION);
                    if (description == null) {
                        break;
                    }
                    WebViewProviderInfo currentProvider = new WebViewProviderInfo(packageName, description, "true".equals(xmlResourceParser.getAttributeValue(null, TAG_AVAILABILITY)), "true".equals(xmlResourceParser.getAttributeValue(null, TAG_FALLBACK)), readSignatures(xmlResourceParser));
                    if (currentProvider.isFallback) {
                        numFallbackPackages++;
                        if (currentProvider.availableByDefault) {
                            if (numFallbackPackages > 1) {
                                break;
                            }
                        }
                        break;
                    }
                    if (currentProvider.availableByDefault) {
                        numAvailableByDefaultPackages++;
                        if (!currentProvider.isFallback) {
                            numAvByDefaultAndNotFallback++;
                        }
                    }
                    webViewProviders.add(currentProvider);
                } catch (Exception e) {
                    throw new AndroidRuntimeException("Error when parsing WebView config " + e);
                } catch (Throwable th) {
                    if (xmlResourceParser != null) {
                        xmlResourceParser.close();
                    }
                }
            } else {
                Log.e(TAG, "Found an element that is not a WebView provider");
            }
        }
        throw new AndroidRuntimeException("Each WebView fallback package must be available by default.");
    }

    public WebViewProviderInfo[] getWebViewPackages() {
        return this.mWebViewProviderPackages;
    }

    public int getFactoryPackageVersion(String packageName) throws NameNotFoundException {
        return AppGlobals.getInitialApplication().getPackageManager().getPackageInfo(packageName, 2097152).versionCode;
    }

    private static String[] readSignatures(XmlResourceParser parser) throws IOException, XmlPullParserException {
        List<String> signatures = new ArrayList();
        int outerDepth = parser.getDepth();
        while (XmlUtils.nextElementWithin(parser, outerDepth)) {
            if (parser.getName().equals(TAG_SIGNATURE)) {
                signatures.add(parser.nextText());
            } else {
                Log.e(TAG, "Found an element in a webview provider that is not a signature");
            }
        }
        return (String[]) signatures.toArray(new String[signatures.size()]);
    }

    public int onWebViewProviderChanged(PackageInfo packageInfo) {
        return WebViewFactory.onWebViewProviderChanged(packageInfo);
    }

    public String getUserChosenWebViewProvider(Context context) {
        return Global.getString(context.getContentResolver(), "webview_provider");
    }

    public void updateUserSetting(Context context, String newProviderName) {
        ContentResolver contentResolver = context.getContentResolver();
        String str = "webview_provider";
        if (newProviderName == null) {
            newProviderName = "";
        }
        Global.putString(contentResolver, str, newProviderName);
    }

    public void killPackageDependents(String packageName) {
        try {
            ActivityManagerNative.getDefault().killPackageDependents(packageName, -1);
        } catch (RemoteException e) {
        }
    }

    public boolean isFallbackLogicEnabled() {
        return Global.getInt(AppGlobals.getInitialApplication().getContentResolver(), "webview_fallback_logic_enabled", 1) == 1;
    }

    public void enableFallbackLogic(boolean enable) {
        Global.putInt(AppGlobals.getInitialApplication().getContentResolver(), "webview_fallback_logic_enabled", enable ? 1 : 0);
    }

    public void uninstallAndDisablePackageForAllUsers(Context context, String packageName) {
        enablePackageForAllUsers(context, packageName, false);
        try {
            PackageManager pm = AppGlobals.getInitialApplication().getPackageManager();
            ApplicationInfo applicationInfo = pm.getApplicationInfo(packageName, 0);
            if (applicationInfo != null && applicationInfo.isUpdatedSystemApp()) {
                pm.deletePackage(packageName, new AnonymousClass1(context), 6);
            }
        } catch (NameNotFoundException e) {
        }
    }

    public void enablePackageForAllUsers(Context context, String packageName, boolean enable) {
        for (UserInfo userInfo : ((UserManager) context.getSystemService("user")).getUsers()) {
            enablePackageForUser(packageName, enable, userInfo.id);
        }
    }

    public void enablePackageForUser(String packageName, boolean enable, int userId) {
        int i = 0;
        try {
            IPackageManager packageManager = AppGlobals.getPackageManager();
            if (!enable) {
                i = 3;
            }
            packageManager.setApplicationEnabledSetting(packageName, i, 0, userId, null);
        } catch (Exception e) {
            Log.w(TAG, "Tried to " + (!enable ? "disable " : "enable ") + packageName + " for user " + userId + ": " + e);
        }
    }

    public boolean systemIsDebuggable() {
        return Build.IS_DEBUGGABLE;
    }

    public PackageInfo getPackageInfoForProvider(WebViewProviderInfo configInfo) throws NameNotFoundException {
        return AppGlobals.getInitialApplication().getPackageManager().getPackageInfo(configInfo.packageName, PACKAGE_FLAGS);
    }
}
