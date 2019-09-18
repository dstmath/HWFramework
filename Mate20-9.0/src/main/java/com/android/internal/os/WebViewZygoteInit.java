package com.android.internal.os;

import android.app.ApplicationLoaders;
import android.net.LocalSocket;
import android.os.Build;
import android.system.ErrnoException;
import android.system.Os;
import android.system.OsConstants;
import android.text.TextUtils;
import android.util.Log;
import android.webkit.WebViewFactory;
import android.webkit.WebViewFactoryProvider;
import android.webkit.WebViewLibraryLoader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;

class WebViewZygoteInit {
    public static final String TAG = "WebViewZygoteInit";
    private static ZygoteServer sServer;

    private static class WebViewZygoteConnection extends ZygoteConnection {
        WebViewZygoteConnection(LocalSocket socket, String abiList) throws IOException {
            super(socket, abiList);
        }

        /* access modifiers changed from: protected */
        public void preload() {
        }

        /* access modifiers changed from: protected */
        public boolean isPreloadComplete() {
            return true;
        }

        /* access modifiers changed from: protected */
        public void handlePreloadPackage(String packagePath, String libsPath, String libFileName, String cacheKey) {
            Log.i(WebViewZygoteInit.TAG, "Beginning package preload");
            ClassLoader loader = ApplicationLoaders.getDefault().createAndCacheWebViewClassLoader(packagePath, libsPath, cacheKey);
            WebViewLibraryLoader.loadNativeLibrary(loader, libFileName);
            int i = 0;
            for (String packageEntry : TextUtils.split(packagePath, File.pathSeparator)) {
                Zygote.nativeAllowFileAcrossFork(packageEntry);
            }
            boolean preloadSucceeded = false;
            try {
                Class<WebViewFactoryProvider> providerClass = WebViewFactory.getWebViewProviderClass(loader);
                Method preloadInZygote = providerClass.getMethod("preloadInZygote", new Class[0]);
                preloadInZygote.setAccessible(true);
                if (preloadInZygote.getReturnType() != Boolean.TYPE) {
                    Log.e(WebViewZygoteInit.TAG, "Unexpected return type: preloadInZygote must return boolean");
                } else {
                    preloadSucceeded = ((Boolean) providerClass.getMethod("preloadInZygote", new Class[0]).invoke(null, new Object[0])).booleanValue();
                    if (!preloadSucceeded) {
                        Log.e(WebViewZygoteInit.TAG, "preloadInZygote returned false");
                    }
                }
            } catch (ReflectiveOperationException e) {
                Log.e(WebViewZygoteInit.TAG, "Exception while preloading package", e);
            }
            try {
                DataOutputStream socketOut = getSocketOutputStream();
                if (preloadSucceeded) {
                    i = 1;
                }
                socketOut.writeInt(i);
                Log.i(WebViewZygoteInit.TAG, "Package preload done");
            } catch (IOException ioe) {
                throw new IllegalStateException("Error writing to command socket", ioe);
            }
        }
    }

    private static class WebViewZygoteServer extends ZygoteServer {
        private WebViewZygoteServer() {
        }

        /* access modifiers changed from: protected */
        public ZygoteConnection createNewConnection(LocalSocket socket, String abiList) throws IOException {
            return new WebViewZygoteConnection(socket, abiList);
        }
    }

    WebViewZygoteInit() {
    }

    public static void main(String[] argv) {
        Log.i(TAG, "Starting WebViewZygoteInit");
        String socketName = null;
        for (String arg : argv) {
            Log.i(TAG, arg);
            if (arg.startsWith(Zygote.CHILD_ZYGOTE_SOCKET_NAME_ARG)) {
                socketName = arg.substring(Zygote.CHILD_ZYGOTE_SOCKET_NAME_ARG.length());
            }
        }
        if (socketName != null) {
            try {
                Os.prctl(OsConstants.PR_SET_NO_NEW_PRIVS, 1, 0, 0, 0);
                sServer = new WebViewZygoteServer();
                try {
                    sServer.registerServerSocketAtAbstractName(socketName);
                    Zygote.nativeAllowFileAcrossFork("ABSTRACT/" + socketName);
                    Runnable caller = sServer.runSelectLoop(TextUtils.join(",", Build.SUPPORTED_ABIS));
                    sServer.closeServerSocket();
                    if (caller != null) {
                        caller.run();
                    }
                } catch (RuntimeException e) {
                    Log.e(TAG, "Fatal exception:", e);
                    throw e;
                } catch (Throwable th) {
                    sServer.closeServerSocket();
                    throw th;
                }
            } catch (ErrnoException ex) {
                throw new RuntimeException("Failed to set PR_SET_NO_NEW_PRIVS", ex);
            }
        } else {
            throw new RuntimeException("No --zygote-socket= specified");
        }
    }
}
