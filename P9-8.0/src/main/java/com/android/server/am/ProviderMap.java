package com.android.server.am;

import android.content.ComponentName;
import android.os.RemoteException;
import android.os.UserHandle;
import android.util.SparseArray;
import com.android.internal.os.TransferPipe;
import java.io.FileDescriptor;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.Set;

public final class ProviderMap {
    private static final boolean DBG = false;
    private static final String TAG = "ProviderMap";
    private final ActivityManagerService mAm;
    private final SparseArray<HashMap<ComponentName, ContentProviderRecord>> mProvidersByClassPerUser = new SparseArray();
    private final SparseArray<HashMap<String, ContentProviderRecord>> mProvidersByNamePerUser = new SparseArray();
    private final HashMap<ComponentName, ContentProviderRecord> mSingletonByClass = new HashMap();
    private final HashMap<String, ContentProviderRecord> mSingletonByName = new HashMap();

    ProviderMap(ActivityManagerService am) {
        this.mAm = am;
    }

    ContentProviderRecord getProviderByName(String name) {
        return getProviderByName(name, -1);
    }

    ContentProviderRecord getProviderByName(String name, int userId) {
        ContentProviderRecord record = (ContentProviderRecord) this.mSingletonByName.get(name);
        if (record != null) {
            return record;
        }
        return (ContentProviderRecord) getProvidersByName(userId).get(name);
    }

    ContentProviderRecord getProviderByClass(ComponentName name) {
        return getProviderByClass(name, -1);
    }

    ContentProviderRecord getProviderByClass(ComponentName name, int userId) {
        ContentProviderRecord record = (ContentProviderRecord) this.mSingletonByClass.get(name);
        if (record != null) {
            return record;
        }
        return (ContentProviderRecord) getProvidersByClass(userId).get(name);
    }

    void putProviderByName(String name, ContentProviderRecord record) {
        if (record.singleton) {
            this.mSingletonByName.put(name, record);
        } else {
            getProvidersByName(UserHandle.getUserId(record.appInfo.uid)).put(name, record);
        }
    }

    void putProviderByClass(ComponentName name, ContentProviderRecord record) {
        if (record.singleton) {
            this.mSingletonByClass.put(name, record);
        } else {
            getProvidersByClass(UserHandle.getUserId(record.appInfo.uid)).put(name, record);
        }
    }

    void removeProviderByName(String name, int userId) {
        if (this.mSingletonByName.containsKey(name)) {
            this.mSingletonByName.remove(name);
        } else if (userId < 0) {
            throw new IllegalArgumentException("Bad user " + userId);
        } else {
            HashMap<String, ContentProviderRecord> map = getProvidersByName(userId);
            map.remove(name);
            if (map.size() == 0) {
                this.mProvidersByNamePerUser.remove(userId);
            }
        }
    }

    void removeProviderByClass(ComponentName name, int userId) {
        if (this.mSingletonByClass.containsKey(name)) {
            this.mSingletonByClass.remove(name);
        } else if (userId < 0) {
            throw new IllegalArgumentException("Bad user " + userId);
        } else {
            HashMap<ComponentName, ContentProviderRecord> map = getProvidersByClass(userId);
            map.remove(name);
            if (map.size() == 0) {
                this.mProvidersByClassPerUser.remove(userId);
            }
        }
    }

    private HashMap<String, ContentProviderRecord> getProvidersByName(int userId) {
        if (userId < 0) {
            throw new IllegalArgumentException("Bad user " + userId);
        }
        HashMap<String, ContentProviderRecord> map = (HashMap) this.mProvidersByNamePerUser.get(userId);
        if (map != null) {
            return map;
        }
        HashMap<String, ContentProviderRecord> newMap = new HashMap();
        this.mProvidersByNamePerUser.put(userId, newMap);
        return newMap;
    }

    HashMap<ComponentName, ContentProviderRecord> getProvidersByClass(int userId) {
        if (userId < 0) {
            throw new IllegalArgumentException("Bad user " + userId);
        }
        HashMap<ComponentName, ContentProviderRecord> map = (HashMap) this.mProvidersByClassPerUser.get(userId);
        if (map != null) {
            return map;
        }
        HashMap<ComponentName, ContentProviderRecord> newMap = new HashMap();
        this.mProvidersByClassPerUser.put(userId, newMap);
        return newMap;
    }

    private boolean collectPackageProvidersLocked(String packageName, Set<String> filterByClasses, boolean doit, boolean evenPersistent, HashMap<ComponentName, ContentProviderRecord> providers, ArrayList<ContentProviderRecord> result) {
        boolean didSomething = false;
        for (ContentProviderRecord provider : providers.values()) {
            boolean sameComponent;
            if (packageName == null) {
                sameComponent = true;
            } else if (!provider.info.packageName.equals(packageName)) {
                sameComponent = false;
            } else if (filterByClasses != null) {
                sameComponent = filterByClasses.contains(provider.name.getClassName());
            } else {
                sameComponent = true;
            }
            if (sameComponent && (provider.proc == null || evenPersistent || (provider.proc.persistent ^ 1) != 0)) {
                if (!doit) {
                    return true;
                }
                didSomething = true;
                result.add(provider);
            }
        }
        return didSomething;
    }

    boolean collectPackageProvidersLocked(String packageName, Set<String> filterByClasses, boolean doit, boolean evenPersistent, int userId, ArrayList<ContentProviderRecord> result) {
        boolean didSomething = false;
        if (userId == -1 || userId == 0) {
            didSomething = collectPackageProvidersLocked(packageName, (Set) filterByClasses, doit, evenPersistent, this.mSingletonByClass, (ArrayList) result);
        }
        if (!doit && didSomething) {
            return true;
        }
        if (userId == -1) {
            for (int i = 0; i < this.mProvidersByClassPerUser.size(); i++) {
                if (collectPackageProvidersLocked(packageName, (Set) filterByClasses, doit, evenPersistent, (HashMap) this.mProvidersByClassPerUser.valueAt(i), (ArrayList) result)) {
                    if (!doit) {
                        return true;
                    }
                    didSomething = true;
                }
            }
        } else {
            HashMap items = getProvidersByClass(userId);
            if (items != null) {
                didSomething |= collectPackageProvidersLocked(packageName, (Set) filterByClasses, doit, evenPersistent, items, (ArrayList) result);
            }
        }
        return didSomething;
    }

    private boolean dumpProvidersByClassLocked(PrintWriter pw, boolean dumpAll, String dumpPackage, String header, boolean needSep, HashMap<ComponentName, ContentProviderRecord> map) {
        boolean written = false;
        for (Entry<ComponentName, ContentProviderRecord> e : map.entrySet()) {
            ContentProviderRecord r = (ContentProviderRecord) e.getValue();
            if (dumpPackage == null || (dumpPackage.equals(r.appInfo.packageName) ^ 1) == 0) {
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
        for (Entry<String, ContentProviderRecord> e : map.entrySet()) {
            ContentProviderRecord r = (ContentProviderRecord) e.getValue();
            if (dumpPackage == null || (dumpPackage.equals(r.appInfo.packageName) ^ 1) == 0) {
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
                pw.print((String) e.getKey());
                pw.print(": ");
                pw.println(r.toShortString());
            }
        }
        return written;
    }

    boolean dumpProvidersLocked(PrintWriter pw, boolean dumpAll, String dumpPackage) {
        int i;
        boolean needSep = false;
        if (this.mSingletonByClass.size() > 0) {
            needSep = dumpProvidersByClassLocked(pw, dumpAll, dumpPackage, "  Published single-user content providers (by class):", false, this.mSingletonByClass);
        }
        for (i = 0; i < this.mProvidersByClassPerUser.size(); i++) {
            HashMap<ComponentName, ContentProviderRecord> map = (HashMap) this.mProvidersByClassPerUser.valueAt(i);
            needSep |= dumpProvidersByClassLocked(pw, dumpAll, dumpPackage, "  Published user " + this.mProvidersByClassPerUser.keyAt(i) + " content providers (by class):", needSep, map);
        }
        if (dumpAll) {
            needSep |= dumpProvidersByNameLocked(pw, dumpPackage, "  Single-user authority to provider mappings:", needSep, this.mSingletonByName);
            for (i = 0; i < this.mProvidersByNamePerUser.size(); i++) {
                needSep |= dumpProvidersByNameLocked(pw, dumpPackage, "  User " + this.mProvidersByNamePerUser.keyAt(i) + " authority to provider mappings:", needSep, (HashMap) this.mProvidersByNamePerUser.valueAt(i));
            }
        }
        return needSep;
    }

    protected boolean dumpProvider(FileDescriptor fd, PrintWriter pw, String name, String[] args, int opti, boolean dumpAll) {
        int i;
        ArrayList<ContentProviderRecord> allProviders = new ArrayList();
        ArrayList<ContentProviderRecord> providers = new ArrayList();
        synchronized (this.mAm) {
            try {
                ActivityManagerService.boostPriorityForLockedSection();
                allProviders.addAll(this.mSingletonByClass.values());
                for (i = 0; i < this.mProvidersByClassPerUser.size(); i++) {
                    allProviders.addAll(((HashMap) this.mProvidersByClassPerUser.valueAt(i)).values());
                }
                if ("all".equals(name)) {
                    providers.addAll(allProviders);
                } else {
                    Object componentName = name != null ? ComponentName.unflattenFromString(name) : null;
                    int objectId = 0;
                    if (componentName == null) {
                        objectId = Integer.parseInt(name, 16);
                        name = null;
                        componentName = null;
                    }
                    for (i = 0; i < allProviders.size(); i++) {
                        ContentProviderRecord r1 = (ContentProviderRecord) allProviders.get(i);
                        if (componentName != null) {
                            if (r1.name.equals(componentName)) {
                                providers.add(r1);
                            }
                        } else if (name != null) {
                            if (r1.name.flattenToString().contains(name)) {
                                providers.add(r1);
                            }
                        } else if (System.identityHashCode(r1) == objectId) {
                            providers.add(r1);
                        }
                    }
                }
            } catch (RuntimeException e) {
            } catch (Throwable th) {
                ActivityManagerService.resetPriorityAfterLockedSection();
            }
        }
        ActivityManagerService.resetPriorityAfterLockedSection();
        if (providers.size() <= 0) {
            return false;
        }
        boolean needSep = false;
        for (i = 0; i < providers.size(); i++) {
            if (needSep) {
                pw.println();
            }
            needSep = true;
            dumpProvider("", fd, pw, (ContentProviderRecord) providers.get(i), args, dumpAll);
        }
        return true;
    }

    private void dumpProvider(String prefix, FileDescriptor fd, PrintWriter pw, ContentProviderRecord r, String[] args, boolean dumpAll) {
        int i = 0;
        int length = args.length;
        while (i < length) {
            String s = args[i];
            if (dumpAll || !s.contains("--proto")) {
                i++;
            } else {
                if (!(r.proc == null || r.proc.thread == null)) {
                    dumpToTransferPipe(null, fd, pw, r, args);
                }
                return;
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
            } finally {
                ActivityManagerService.resetPriorityAfterLockedSection();
            }
        }
        if (!(r.proc == null || r.proc.thread == null)) {
            pw.println("    Client:");
            pw.flush();
            dumpToTransferPipe("      ", fd, pw, r, args);
        }
    }

    private void dumpToTransferPipe(String prefix, FileDescriptor fd, PrintWriter pw, ContentProviderRecord r, String[] args) {
        TransferPipe tp;
        try {
            tp = new TransferPipe();
            r.proc.thread.dumpProvider(tp.getWriteFd(), r.provider.asBinder(), args);
            tp.setBufferPrefix(prefix);
            tp.go(fd, 2000);
            tp.kill();
        } catch (IOException ex) {
            pw.println("      Failure while dumping the provider: " + ex);
        } catch (RemoteException e) {
            pw.println("      Got a RemoteException while dumping the service");
        } catch (Throwable th) {
            tp.kill();
        }
    }
}
