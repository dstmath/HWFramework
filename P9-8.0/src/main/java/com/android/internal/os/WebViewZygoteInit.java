package com.android.internal.os;

import android.app.ApplicationLoaders;
import android.net.LocalSocket;
import android.os.Build;
import android.system.ErrnoException;
import android.system.Os;
import android.text.TextUtils;
import android.util.Log;
import android.webkit.WebViewFactory;
import com.android.internal.os.Zygote.MethodAndArgsCaller;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;

class WebViewZygoteInit {
    public static final String TAG = "WebViewZygoteInit";
    private static ZygoteServer sServer;

    private static class WebViewZygoteConnection extends ZygoteConnection {
        WebViewZygoteConnection(LocalSocket socket, String abiList) throws IOException {
            super(socket, abiList);
        }

        protected void preload() {
        }

        protected boolean isPreloadComplete() {
            return true;
        }

        /* JADX WARNING: Removed duplicated region for block: B:13:0x005f A:{Splitter: B:3:0x0024, ExcHandler: java.lang.ClassNotFoundException (r0_0 'e' java.lang.Exception)} */
        /* JADX WARNING: Removed duplicated region for block: B:13:0x005f A:{Splitter: B:3:0x0024, ExcHandler: java.lang.ClassNotFoundException (r0_0 'e' java.lang.Exception)} */
        /* JADX WARNING: Removed duplicated region for block: B:13:0x005f A:{Splitter: B:3:0x0024, ExcHandler: java.lang.ClassNotFoundException (r0_0 'e' java.lang.Exception)} */
        /* JADX WARNING: Removed duplicated region for block: B:13:0x005f A:{Splitter: B:3:0x0024, ExcHandler: java.lang.ClassNotFoundException (r0_0 'e' java.lang.Exception)} */
        /* JADX WARNING: Missing block: B:13:0x005f, code:
            r0 = move-exception;
     */
        /* JADX WARNING: Missing block: B:14:0x0060, code:
            android.util.Log.e(com.android.internal.os.WebViewZygoteInit.TAG, "Exception while preloading package", r0);
     */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        protected boolean handlePreloadPackage(String packagePath, String libsPath, String cacheKey) {
            Log.i(WebViewZygoteInit.TAG, "Beginning package preload");
            ClassLoader loader = ApplicationLoaders.getDefault().createAndCacheWebViewClassLoader(packagePath, libsPath, cacheKey);
            for (String packageEntry : TextUtils.split(packagePath, File.pathSeparator)) {
                Zygote.nativeAllowFileAcrossFork(packageEntry);
            }
            try {
                if (!((Boolean) WebViewFactory.getWebViewProviderClass(loader).getMethod("preloadInZygote", new Class[0]).invoke(null, new Object[0])).booleanValue()) {
                    Log.e(WebViewZygoteInit.TAG, "preloadInZygote returned false");
                }
            } catch (Exception e) {
            }
            try {
                DataOutputStream socketOutStream = getDataOutputStream();
                if (socketOutStream != null) {
                    socketOutStream.writeInt(0);
                }
                Log.i(WebViewZygoteInit.TAG, "Package preload done");
                return false;
            } catch (IOException ioe) {
                Log.e(WebViewZygoteInit.TAG, "Error writing to command socket", ioe);
                return true;
            }
        }
    }

    private static class WebViewZygoteServer extends ZygoteServer {
        /* synthetic */ WebViewZygoteServer(WebViewZygoteServer -this0) {
            this();
        }

        private WebViewZygoteServer() {
        }

        protected ZygoteConnection createNewConnection(LocalSocket socket, String abiList) throws IOException {
            return new WebViewZygoteConnection(socket, abiList);
        }
    }

    WebViewZygoteInit() {
    }

    public static void main(String[] argv) {
        sServer = new WebViewZygoteServer();
        try {
            Os.setpgid(0, 0);
            try {
                sServer.registerServerSocket("webview_zygote");
                sServer.runSelectLoop(TextUtils.join((CharSequence) ",", Build.SUPPORTED_ABIS));
                sServer.closeServerSocket();
            } catch (MethodAndArgsCaller caller) {
                caller.run();
            } catch (RuntimeException e) {
                Log.e(TAG, "Fatal exception:", e);
            }
            System.exit(0);
        } catch (ErrnoException ex) {
            throw new RuntimeException("Failed to setpgid(0,0)", ex);
        }
    }
}
