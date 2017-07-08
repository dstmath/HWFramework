package com.android.commands.settings;

import android.app.ActivityManagerNative;
import android.app.IActivityManager;
import android.app.IActivityManager.ContentProviderHolder;
import android.content.IContentProvider;
import android.database.Cursor;
import android.net.Uri;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Process;
import android.os.RemoteException;
import android.provider.Settings.Global;
import android.provider.Settings.Secure;
import android.provider.Settings.System;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class SettingsCmd {
    private static final /* synthetic */ int[] -com-android-commands-settings-SettingsCmd$CommandVerbSwitchesValues = null;
    static String[] mArgs;
    String mKey;
    int mNextArg;
    String mTable;
    int mUser;
    String mValue;
    CommandVerb mVerb;

    enum CommandVerb {
        ;

        static {
            /* JADX: method processing error */
/*
            Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.commands.settings.SettingsCmd.CommandVerb.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:263)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.commands.settings.SettingsCmd.CommandVerb.<clinit>():void
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
            throw new UnsupportedOperationException("Method not decompiled: com.android.commands.settings.SettingsCmd.CommandVerb.<clinit>():void");
        }
    }

    private static /* synthetic */ int[] -getcom-android-commands-settings-SettingsCmd$CommandVerbSwitchesValues() {
        if (-com-android-commands-settings-SettingsCmd$CommandVerbSwitchesValues != null) {
            return -com-android-commands-settings-SettingsCmd$CommandVerbSwitchesValues;
        }
        int[] iArr = new int[CommandVerb.values().length];
        try {
            iArr[CommandVerb.DELETE.ordinal()] = 1;
        } catch (NoSuchFieldError e) {
        }
        try {
            iArr[CommandVerb.GET.ordinal()] = 2;
        } catch (NoSuchFieldError e2) {
        }
        try {
            iArr[CommandVerb.LIST.ordinal()] = 3;
        } catch (NoSuchFieldError e3) {
        }
        try {
            iArr[CommandVerb.PUT.ordinal()] = 4;
        } catch (NoSuchFieldError e4) {
        }
        try {
            iArr[CommandVerb.UNSPECIFIED.ordinal()] = 5;
        } catch (NoSuchFieldError e5) {
        }
        -com-android-commands-settings-SettingsCmd$CommandVerbSwitchesValues = iArr;
        return iArr;
    }

    public SettingsCmd() {
        this.mUser = -1;
        this.mVerb = CommandVerb.UNSPECIFIED;
        this.mTable = null;
        this.mKey = null;
        this.mValue = null;
    }

    public static void main(String[] args) {
        if (args == null || args.length < 2) {
            printUsage();
            return;
        }
        mArgs = args;
        try {
            new SettingsCmd().run();
        } catch (Exception e) {
            System.err.println("Unable to run settings command");
        }
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void run() {
        String arg;
        IBinder token;
        boolean valid = false;
        while (true) {
            try {
                arg = nextArg();
                if (arg != null) {
                    if (!"--user".equals(arg)) {
                        if (this.mVerb != CommandVerb.UNSPECIFIED) {
                            if (this.mTable != null) {
                                if (this.mVerb != CommandVerb.GET && this.mVerb != CommandVerb.DELETE) {
                                    if (this.mKey != null) {
                                        break;
                                    }
                                    this.mKey = arg;
                                } else {
                                    this.mKey = arg;
                                }
                            } else if (!"system".equalsIgnoreCase(arg) && !"secure".equalsIgnoreCase(arg) && !"global".equalsIgnoreCase(arg)) {
                                break;
                            } else {
                                this.mTable = arg.toLowerCase();
                                if (this.mVerb == CommandVerb.LIST) {
                                    break;
                                }
                            }
                        } else if (!"get".equalsIgnoreCase(arg)) {
                            if (!"put".equalsIgnoreCase(arg)) {
                                if (!"delete".equalsIgnoreCase(arg)) {
                                    if (!"list".equalsIgnoreCase(arg)) {
                                        break;
                                    }
                                    this.mVerb = CommandVerb.LIST;
                                } else {
                                    this.mVerb = CommandVerb.DELETE;
                                }
                            } else {
                                this.mVerb = CommandVerb.PUT;
                            }
                        } else {
                            this.mVerb = CommandVerb.GET;
                        }
                    } else if (this.mUser != -1) {
                        break;
                    } else {
                        arg = nextArg();
                        if ("current".equals(arg) || "cur".equals(arg)) {
                            this.mUser = -2;
                        } else {
                            this.mUser = Integer.parseInt(arg);
                        }
                    }
                } else {
                    break;
                }
            } catch (Exception e) {
                valid = false;
            }
        }
        this.mValue = arg;
        if (this.mNextArg >= mArgs.length) {
            valid = true;
        } else {
            System.err.println("Too many arguments");
        }
        if (valid) {
            IActivityManager activityManager;
            IContentProvider provider;
            try {
                activityManager = ActivityManagerNative.getDefault();
                if (this.mUser == -2) {
                    this.mUser = activityManager.getCurrentUser().id;
                }
                if (this.mUser < 0) {
                    this.mUser = 0;
                }
                provider = null;
                token = new Binder();
                ContentProviderHolder holder = activityManager.getContentProviderExternal("settings", 0, token);
                if (holder == null) {
                    throw new IllegalStateException("Could not find settings provider");
                }
                provider = holder.provider;
                switch (-getcom-android-commands-settings-SettingsCmd$CommandVerbSwitchesValues()[this.mVerb.ordinal()]) {
                    case 1:
                        System.out.println("Deleted " + deleteForUser(provider, this.mUser, this.mTable, this.mKey) + " rows");
                        break;
                    case 2:
                        System.out.println(getForUser(provider, this.mUser, this.mTable, this.mKey));
                        break;
                    case 3:
                        for (String line : listForUser(provider, this.mUser, this.mTable)) {
                            System.out.println(line);
                        }
                        break;
                    case 4:
                        putForUser(provider, this.mUser, this.mTable, this.mKey, this.mValue);
                        break;
                    default:
                        System.err.println("Unspecified command");
                        break;
                }
                if (provider != null) {
                    activityManager.removeContentProviderExternal("settings", token);
                    return;
                }
                return;
            } catch (Exception e2) {
                System.err.println("Error while accessing settings provider");
                e2.printStackTrace();
                return;
            } catch (Throwable th) {
                if (provider != null) {
                    activityManager.removeContentProviderExternal("settings", token);
                }
            }
        }
        printUsage();
    }

    private List<String> listForUser(IContentProvider provider, int userHandle, String table) {
        Uri uri;
        if ("system".equals(table)) {
            uri = System.CONTENT_URI;
        } else if ("secure".equals(table)) {
            uri = Secure.CONTENT_URI;
        } else if ("global".equals(table)) {
            uri = Global.CONTENT_URI;
        } else {
            uri = null;
        }
        ArrayList<String> lines = new ArrayList();
        if (uri == null) {
            return lines;
        }
        Cursor cursor;
        try {
            cursor = provider.query(resolveCallingPackage(), uri, null, null, null, null, null);
            while (cursor != null) {
                if (!cursor.moveToNext()) {
                    break;
                }
                lines.add(cursor.getString(1) + "=" + cursor.getString(2));
            }
            if (cursor != null) {
                cursor.close();
            }
            Collections.sort(lines);
        } catch (RemoteException e) {
            System.err.println("List failed in " + table + " for user " + userHandle);
        } catch (Throwable th) {
            if (cursor != null) {
                cursor.close();
            }
        }
        return lines;
    }

    private String nextArg() {
        if (this.mNextArg >= mArgs.length) {
            return null;
        }
        String arg = mArgs[this.mNextArg];
        this.mNextArg++;
        return arg;
    }

    String getForUser(IContentProvider provider, int userHandle, String table, String key) {
        String callGetCommand;
        if ("system".equals(table)) {
            callGetCommand = "GET_system";
        } else if ("secure".equals(table)) {
            callGetCommand = "GET_secure";
        } else if ("global".equals(table)) {
            callGetCommand = "GET_global";
        } else {
            System.err.println("Invalid table; no put performed");
            throw new IllegalArgumentException("Invalid table " + table);
        }
        String result = null;
        try {
            Bundle arg = new Bundle();
            arg.putInt("_user", userHandle);
            Bundle b = provider.call(resolveCallingPackage(), callGetCommand, key, arg);
            if (b != null) {
                result = b.getPairValue();
            }
        } catch (RemoteException e) {
            System.err.println("Can't read key " + key + " in " + table + " for user " + userHandle);
        }
        return result;
    }

    void putForUser(IContentProvider provider, int userHandle, String table, String key, String value) {
        String callPutCommand;
        if ("system".equals(table)) {
            callPutCommand = "PUT_system";
        } else if ("secure".equals(table)) {
            callPutCommand = "PUT_secure";
        } else if ("global".equals(table)) {
            callPutCommand = "PUT_global";
        } else {
            System.err.println("Invalid table; no put performed");
            return;
        }
        try {
            Bundle arg = new Bundle();
            arg.putString("value", value);
            arg.putInt("_user", userHandle);
            provider.call(resolveCallingPackage(), callPutCommand, key, arg);
        } catch (RemoteException e) {
            System.err.println("Can't set key " + key + " in " + table + " for user " + userHandle);
        }
    }

    int deleteForUser(IContentProvider provider, int userHandle, String table, String key) {
        Uri targetUri;
        if ("system".equals(table)) {
            targetUri = System.getUriFor(key);
        } else if ("secure".equals(table)) {
            targetUri = Secure.getUriFor(key);
        } else if ("global".equals(table)) {
            targetUri = Global.getUriFor(key);
        } else {
            System.err.println("Invalid table; no delete performed");
            throw new IllegalArgumentException("Invalid table " + table);
        }
        int num = 0;
        try {
            num = provider.delete(resolveCallingPackage(), targetUri, null, null);
        } catch (RemoteException e) {
            System.err.println("Can't clear key " + key + " in " + table + " for user " + userHandle);
        }
        return num;
    }

    private static void printUsage() {
        System.err.println("usage:  settings [--user <USER_ID> | current] get namespace key");
        System.err.println("        settings [--user <USER_ID> | current] put namespace key value");
        System.err.println("        settings [--user <USER_ID> | current] delete namespace key");
        System.err.println("        settings [--user <USER_ID> | current] list namespace");
        System.err.println("\n'namespace' is one of {system, secure, global}, case-insensitive");
        System.err.println("If '--user <USER_ID> | current' is not given, the operations are performed on the system user.");
    }

    public static String resolveCallingPackage() {
        switch (Process.myUid()) {
            case 0:
                return "root";
            case 2000:
                return "com.android.shell";
            default:
                return null;
        }
    }
}
