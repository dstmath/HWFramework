package com.android.internal.os;

import android.app.LoadedApk;
import android.app.ZygotePreload;
import android.content.ComponentName;
import android.content.pm.ApplicationInfo;
import android.net.LocalSocket;
import android.util.Log;
import java.io.DataOutputStream;
import java.io.IOException;

class AppZygoteInit {
    public static final String TAG = "AppZygoteInit";
    private static ZygoteServer sServer;

    AppZygoteInit() {
    }

    private static class AppZygoteServer extends ZygoteServer {
        private AppZygoteServer() {
        }

        /* access modifiers changed from: protected */
        @Override // com.android.internal.os.ZygoteServer
        public ZygoteConnection createNewConnection(LocalSocket socket, String abiList) throws IOException {
            return new AppZygoteConnection(socket, abiList);
        }
    }

    private static class AppZygoteConnection extends ZygoteConnection {
        AppZygoteConnection(LocalSocket socket, String abiList) throws IOException {
            super(socket, abiList);
        }

        /* access modifiers changed from: protected */
        @Override // com.android.internal.os.ZygoteConnection
        public void preload() {
        }

        /* access modifiers changed from: protected */
        @Override // com.android.internal.os.ZygoteConnection
        public boolean isPreloadComplete() {
            return true;
        }

        /* access modifiers changed from: protected */
        @Override // com.android.internal.os.ZygoteConnection
        public boolean canPreloadApp() {
            return true;
        }

        /* access modifiers changed from: protected */
        @Override // com.android.internal.os.ZygoteConnection
        public void handlePreloadApp(ApplicationInfo appInfo) {
            Log.i(AppZygoteInit.TAG, "Beginning application preload for " + appInfo.packageName);
            ClassLoader loader = new LoadedApk(null, appInfo, null, null, false, true, false).getClassLoader();
            Zygote.allowAppFilesAcrossFork(appInfo);
            int i = 1;
            if (appInfo.zygotePreloadName != null) {
                try {
                    ComponentName preloadName = ComponentName.createRelative(appInfo.packageName, appInfo.zygotePreloadName);
                    Class<?> cl = Class.forName(preloadName.getClassName(), true, loader);
                    if (!ZygotePreload.class.isAssignableFrom(cl)) {
                        Log.e(AppZygoteInit.TAG, preloadName.getClassName() + " does not implement " + ZygotePreload.class.getName());
                    } else {
                        ((ZygotePreload) cl.getConstructor(new Class[0]).newInstance(new Object[0])).doPreload(appInfo);
                    }
                } catch (ReflectiveOperationException e) {
                    Log.e(AppZygoteInit.TAG, "AppZygote application preload failed for " + appInfo.zygotePreloadName, e);
                }
            } else {
                Log.i(AppZygoteInit.TAG, "No zygotePreloadName attribute specified.");
            }
            try {
                DataOutputStream socketOut = getSocketOutputStream();
                if (loader == null) {
                    i = 0;
                }
                socketOut.writeInt(i);
                Log.i(AppZygoteInit.TAG, "Application preload done");
            } catch (IOException e2) {
                throw new IllegalStateException("Error writing to command socket", e2);
            }
        }
    }

    public static void main(String[] argv) {
        ChildZygoteInit.runZygoteServer(new AppZygoteServer(), argv);
    }
}
