package com.android.server.am;

import android.content.ComponentName;
import android.os.RemoteException;
import android.os.UserHandle;
import android.util.SparseArray;
import com.android.internal.os.TransferPipe;
import com.android.internal.util.CollectionUtils;
import com.android.internal.util.DumpUtils;
import com.android.server.utils.PriorityDump;
import java.io.FileDescriptor;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;

public final class ProviderMap {
    private static final boolean DBG = false;
    private static final String TAG = "ProviderMap";
    private final ActivityManagerService mAm;
    private final SparseArray<HashMap<ComponentName, ContentProviderRecord>> mProvidersByClassPerUser = new SparseArray<>();
    private final SparseArray<HashMap<String, ContentProviderRecord>> mProvidersByNamePerUser = new SparseArray<>();
    private final HashMap<ComponentName, ContentProviderRecord> mSingletonByClass = new HashMap<>();
    private final HashMap<String, ContentProviderRecord> mSingletonByName = new HashMap<>();

    ProviderMap(ActivityManagerService am) {
        this.mAm = am;
    }

    /* access modifiers changed from: package-private */
    public ContentProviderRecord getProviderByName(String name) {
        return getProviderByName(name, -1);
    }

    /* access modifiers changed from: package-private */
    public ContentProviderRecord getProviderByName(String name, int userId) {
        ContentProviderRecord record = this.mSingletonByName.get(name);
        if (record != null) {
            return record;
        }
        return getProvidersByName(userId).get(name);
    }

    /* access modifiers changed from: package-private */
    public ContentProviderRecord getProviderByClass(ComponentName name) {
        return getProviderByClass(name, -1);
    }

    /* access modifiers changed from: package-private */
    public ContentProviderRecord getProviderByClass(ComponentName name, int userId) {
        ContentProviderRecord record = this.mSingletonByClass.get(name);
        if (record != null) {
            return record;
        }
        return getProvidersByClass(userId).get(name);
    }

    /* access modifiers changed from: package-private */
    public void putProviderByName(String name, ContentProviderRecord record) {
        if (record.singleton) {
            this.mSingletonByName.put(name, record);
        } else {
            getProvidersByName(UserHandle.getUserId(record.appInfo.uid)).put(name, record);
        }
    }

    /* access modifiers changed from: package-private */
    public void putProviderByClass(ComponentName name, ContentProviderRecord record) {
        if (record.singleton) {
            this.mSingletonByClass.put(name, record);
        } else {
            getProvidersByClass(UserHandle.getUserId(record.appInfo.uid)).put(name, record);
        }
    }

    /* access modifiers changed from: package-private */
    public void removeProviderByName(String name, int userId) {
        if (this.mSingletonByName.containsKey(name)) {
            this.mSingletonByName.remove(name);
        } else if (userId >= 0) {
            HashMap<String, ContentProviderRecord> map = getProvidersByName(userId);
            map.remove(name);
            if (map.size() == 0) {
                this.mProvidersByNamePerUser.remove(userId);
            }
        } else {
            throw new IllegalArgumentException("Bad user " + userId);
        }
    }

    /* access modifiers changed from: package-private */
    public void removeProviderByClass(ComponentName name, int userId) {
        if (this.mSingletonByClass.containsKey(name)) {
            this.mSingletonByClass.remove(name);
        } else if (userId >= 0) {
            HashMap<ComponentName, ContentProviderRecord> map = getProvidersByClass(userId);
            map.remove(name);
            if (map.size() == 0) {
                this.mProvidersByClassPerUser.remove(userId);
            }
        } else {
            throw new IllegalArgumentException("Bad user " + userId);
        }
    }

    private HashMap<String, ContentProviderRecord> getProvidersByName(int userId) {
        if (userId >= 0) {
            HashMap<String, ContentProviderRecord> map = this.mProvidersByNamePerUser.get(userId);
            if (map != null) {
                return map;
            }
            HashMap<String, ContentProviderRecord> newMap = new HashMap<>();
            this.mProvidersByNamePerUser.put(userId, newMap);
            return newMap;
        }
        throw new IllegalArgumentException("Bad user " + userId);
    }

    /* access modifiers changed from: package-private */
    public HashMap<ComponentName, ContentProviderRecord> getProvidersByClass(int userId) {
        if (userId >= 0) {
            HashMap<ComponentName, ContentProviderRecord> map = this.mProvidersByClassPerUser.get(userId);
            if (map != null) {
                return map;
            }
            HashMap<ComponentName, ContentProviderRecord> newMap = new HashMap<>();
            this.mProvidersByClassPerUser.put(userId, newMap);
            return newMap;
        }
        throw new IllegalArgumentException("Bad user " + userId);
    }

    private boolean collectPackageProvidersLocked(String packageName, Set<String> filterByClasses, boolean doit, boolean evenPersistent, HashMap<ComponentName, ContentProviderRecord> providers, ArrayList<ContentProviderRecord> result) {
        boolean didSomething = false;
        for (ContentProviderRecord provider : providers.values()) {
            if ((packageName == null || (provider.info.packageName.equals(packageName) && (filterByClasses == null || filterByClasses.contains(provider.name.getClassName())))) && (provider.proc == null || evenPersistent || !provider.proc.isPersistent())) {
                if (!doit) {
                    return true;
                }
                didSomething = true;
                result.add(provider);
            }
        }
        return didSomething;
    }

    /* access modifiers changed from: package-private */
    public boolean collectPackageProvidersLocked(String packageName, Set<String> filterByClasses, boolean doit, boolean evenPersistent, int userId, ArrayList<ContentProviderRecord> result) {
        boolean didSomething = false;
        if (userId == -1 || userId == 0) {
            didSomething = collectPackageProvidersLocked(packageName, filterByClasses, doit, evenPersistent, this.mSingletonByClass, result);
        }
        if (!doit && didSomething) {
            return true;
        }
        if (userId == -1) {
            boolean didSomething2 = didSomething;
            for (int i = 0; i < this.mProvidersByClassPerUser.size(); i++) {
                if (collectPackageProvidersLocked(packageName, filterByClasses, doit, evenPersistent, this.mProvidersByClassPerUser.valueAt(i), result)) {
                    if (!doit) {
                        return true;
                    }
                    didSomething2 = true;
                }
            }
            return didSomething2;
        }
        HashMap<ComponentName, ContentProviderRecord> items = getProvidersByClass(userId);
        if (items != null) {
            return didSomething | collectPackageProvidersLocked(packageName, filterByClasses, doit, evenPersistent, items, result);
        }
        return didSomething;
    }

    private boolean dumpProvidersByClassLocked(PrintWriter pw, boolean dumpAll, String dumpPackage, String header, boolean needSep, HashMap<ComponentName, ContentProviderRecord> map) {
        boolean written = false;
        for (Map.Entry<ComponentName, ContentProviderRecord> e : map.entrySet()) {
            ContentProviderRecord r = e.getValue();
            if (dumpPackage == null || dumpPackage.equals(r.appInfo.packageName)) {
                if (needSep) {
                    pw.println("");
                    needSep = false;
                }
                if (header != null) {
                    pw.println(header);
                    header = null;
                }
                written = true;
                pw.print("  * ");
                pw.println(r);
                r.dump(pw, "    ", dumpAll);
            }
        }
        return written;
    }

    private boolean dumpProvidersByNameLocked(PrintWriter pw, String dumpPackage, String header, boolean needSep, HashMap<String, ContentProviderRecord> map) {
        boolean written = false;
        for (Map.Entry<String, ContentProviderRecord> e : map.entrySet()) {
            ContentProviderRecord r = e.getValue();
            if (dumpPackage == null || dumpPackage.equals(r.appInfo.packageName)) {
                if (needSep) {
                    pw.println("");
                    needSep = false;
                }
                if (header != null) {
                    pw.println(header);
                    header = null;
                }
                written = true;
                pw.print("  ");
                pw.print(e.getKey());
                pw.print(": ");
                pw.println(r.toShortString());
            }
        }
        return written;
    }

    /* access modifiers changed from: package-private */
    public boolean dumpProvidersLocked(PrintWriter pw, boolean dumpAll, String dumpPackage) {
        boolean needSep = false;
        if (this.mSingletonByClass.size() > 0) {
            needSep = false | dumpProvidersByClassLocked(pw, dumpAll, dumpPackage, "  Published single-user content providers (by class):", false, this.mSingletonByClass);
        }
        boolean needSep2 = needSep;
        for (int i = 0; i < this.mProvidersByClassPerUser.size(); i++) {
            HashMap<ComponentName, ContentProviderRecord> map = this.mProvidersByClassPerUser.valueAt(i);
            needSep2 |= dumpProvidersByClassLocked(pw, dumpAll, dumpPackage, "  Published user " + this.mProvidersByClassPerUser.keyAt(i) + " content providers (by class):", needSep2, map);
        }
        if (!dumpAll) {
            return needSep2;
        }
        boolean needSep3 = dumpProvidersByNameLocked(pw, dumpPackage, "  Single-user authority to provider mappings:", needSep2, this.mSingletonByName) | needSep2;
        for (int i2 = 0; i2 < this.mProvidersByNamePerUser.size(); i2++) {
            needSep3 |= dumpProvidersByNameLocked(pw, dumpPackage, "  User " + this.mProvidersByNamePerUser.keyAt(i2) + " authority to provider mappings:", needSep3, this.mProvidersByNamePerUser.valueAt(i2));
        }
        return needSep3;
    }

    /* JADX INFO: finally extract failed */
    private ArrayList<ContentProviderRecord> getProvidersForName(String name) {
        ArrayList<ContentProviderRecord> allProviders = new ArrayList<>();
        ArrayList<ContentProviderRecord> ret = new ArrayList<>();
        Predicate<ContentProviderRecord> filter = DumpUtils.filterRecord(name);
        synchronized (this.mAm) {
            try {
                ActivityManagerService.boostPriorityForLockedSection();
                allProviders.addAll(this.mSingletonByClass.values());
                for (int i = 0; i < this.mProvidersByClassPerUser.size(); i++) {
                    allProviders.addAll(this.mProvidersByClassPerUser.valueAt(i).values());
                }
                CollectionUtils.addIf(allProviders, ret, filter);
            } catch (Throwable th) {
                ActivityManagerService.resetPriorityAfterLockedSection();
                throw th;
            }
        }
        ActivityManagerService.resetPriorityAfterLockedSection();
        ret.sort(Comparator.comparing($$Lambda$HKoBBTwYfMTyX1rzuzxIXu0s2cc.INSTANCE));
        return ret;
    }

    /* access modifiers changed from: protected */
    public boolean dumpProvider(FileDescriptor fd, PrintWriter pw, String name, String[] args, int opti, boolean dumpAll) {
        ArrayList<ContentProviderRecord> providers = getProvidersForName(name);
        if (providers.size() <= 0) {
            return false;
        }
        boolean needSep = false;
        int i = 0;
        while (i < providers.size()) {
            if (needSep) {
                pw.println();
            }
            dumpProvider("", fd, pw, providers.get(i), args, dumpAll);
            i++;
            needSep = true;
        }
        return true;
    }

    /* JADX INFO: finally extract failed */
    private void dumpProvider(String prefix, FileDescriptor fd, PrintWriter pw, ContentProviderRecord r, String[] args, boolean dumpAll) {
        for (String s : args) {
            if (!dumpAll && s.contains(PriorityDump.PROTO_ARG)) {
                if (r.proc != null && r.proc.thread != null) {
                    dumpToTransferPipe(null, fd, pw, r, args);
                    return;
                } else {
                    return;
                }
            }
        }
        String innerPrefix = prefix + "  ";
        synchronized (this.mAm) {
            try {
                ActivityManagerService.boostPriorityForLockedSection();
                pw.print(prefix);
                pw.print("PROVIDER ");
                pw.print(r);
                pw.print(" pid=");
                if (r.proc != null) {
                    pw.println(r.proc.pid);
                } else {
                    pw.println("(not running)");
                }
                if (dumpAll) {
                    r.dump(pw, innerPrefix, true);
                }
            } catch (Throwable th) {
                ActivityManagerService.resetPriorityAfterLockedSection();
                throw th;
            }
        }
        ActivityManagerService.resetPriorityAfterLockedSection();
        if (!(r.proc == null || r.proc.thread == null)) {
            pw.println("    Client:");
            pw.flush();
            dumpToTransferPipe("      ", fd, pw, r, args);
        }
    }

    /* access modifiers changed from: protected */
    public boolean dumpProviderProto(FileDescriptor fd, PrintWriter pw, String name, String[] args) {
        String[] newArgs = (String[]) Arrays.copyOf(args, args.length + 1);
        newArgs[args.length] = PriorityDump.PROTO_ARG;
        ArrayList<ContentProviderRecord> providers = getProvidersForName(name);
        if (providers.size() <= 0) {
            return false;
        }
        for (int i = 0; i < providers.size(); i++) {
            ContentProviderRecord r = providers.get(i);
            if (r.proc != null && r.proc.thread != null) {
                dumpToTransferPipe(null, fd, pw, r, newArgs);
                return true;
            }
        }
        return false;
    }

    private void dumpToTransferPipe(String prefix, FileDescriptor fd, PrintWriter pw, ContentProviderRecord r, String[] args) {
        try {
            TransferPipe tp = new TransferPipe();
            try {
                r.proc.thread.dumpProvider(tp.getWriteFd(), r.provider.asBinder(), args);
                tp.setBufferPrefix(prefix);
                tp.go(fd, 2000);
            } finally {
                tp.kill();
            }
        } catch (IOException ex) {
            pw.println("      Failure while dumping the provider: " + ex);
        } catch (RemoteException e) {
            pw.println("      Got a RemoteException while dumping the service");
        }
    }
}
