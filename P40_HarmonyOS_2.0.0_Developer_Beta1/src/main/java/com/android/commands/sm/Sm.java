package com.android.commands.sm;

import android.os.IVoldTaskListener;
import android.os.PersistableBundle;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.storage.DiskInfo;
import android.os.storage.IStorageManager;
import android.os.storage.StorageManager;
import android.os.storage.VolumeInfo;
import android.util.Log;
import java.io.PrintStream;
import java.util.concurrent.CompletableFuture;

public final class Sm {
    private static final String TAG = "Sm";
    private String[] mArgs;
    private String mCurArgData;
    private int mNextArg;
    IStorageManager mSm;

    public static void main(String[] args) {
        boolean success = false;
        int i = 1;
        try {
            new Sm().run(args);
            success = true;
        } catch (Exception e) {
            if (e instanceof IllegalArgumentException) {
                showUsage();
                System.exit(1);
            }
            Log.e(TAG, "Error", e);
            PrintStream printStream = System.err;
            printStream.println("Error: " + e);
        }
        if (success) {
            i = 0;
        }
        System.exit(i);
    }

    public void run(String[] args) throws Exception {
        if (args.length >= 1) {
            this.mSm = IStorageManager.Stub.asInterface(ServiceManager.getService("mount"));
            if (this.mSm != null) {
                this.mArgs = args;
                String op = args[0];
                this.mNextArg = 1;
                if ("list-disks".equals(op)) {
                    runListDisks();
                } else if ("list-volumes".equals(op)) {
                    runListVolumes();
                } else if ("has-adoptable".equals(op)) {
                    runHasAdoptable();
                } else if ("get-primary-storage-uuid".equals(op)) {
                    runGetPrimaryStorageUuid();
                } else if ("set-force-adoptable".equals(op)) {
                    runSetForceAdoptable();
                } else if ("set-sdcardfs".equals(op)) {
                    runSetSdcardfs();
                } else if ("partition".equals(op)) {
                    runPartition();
                } else if ("mount".equals(op)) {
                    runMount();
                } else if ("unmount".equals(op)) {
                    runUnmount();
                } else if ("format".equals(op)) {
                    runFormat();
                } else if ("benchmark".equals(op)) {
                    runBenchmark();
                } else if ("forget".equals(op)) {
                    runForget();
                } else if ("set-emulate-fbe".equals(op)) {
                    runSetEmulateFbe();
                } else if ("get-fbe-mode".equals(op)) {
                    runGetFbeMode();
                } else if ("idle-maint".equals(op)) {
                    runIdleMaint();
                } else if ("fstrim".equals(op)) {
                    runFstrim();
                } else if ("set-virtual-disk".equals(op)) {
                    runSetVirtualDisk();
                } else if ("set-isolated-storage".equals(op)) {
                    runIsolatedStorage();
                } else {
                    throw new IllegalArgumentException();
                }
            } else {
                throw new RemoteException("Failed to find running mount service");
            }
        } else {
            throw new IllegalArgumentException();
        }
    }

    public void runListDisks() throws RemoteException {
        boolean onlyAdoptable = "adoptable".equals(nextArg());
        DiskInfo[] disks = this.mSm.getDisks();
        for (DiskInfo disk : disks) {
            if (!onlyAdoptable || disk.isAdoptable()) {
                System.out.println(disk.getId());
            }
        }
    }

    public void runListVolumes() throws RemoteException {
        int filterType;
        String filter = nextArg();
        if ("public".equals(filter)) {
            filterType = 0;
        } else if ("private".equals(filter)) {
            filterType = 1;
        } else if ("emulated".equals(filter)) {
            filterType = 2;
        } else if ("stub".equals(filter)) {
            filterType = 5;
        } else {
            filterType = -1;
        }
        VolumeInfo[] vols = this.mSm.getVolumes(0);
        for (VolumeInfo vol : vols) {
            if (filterType == -1 || filterType == vol.getType()) {
                String envState = VolumeInfo.getEnvironmentForState(vol.getState());
                System.out.println(vol.getId() + " " + envState + " " + vol.getFsUuid());
            }
        }
    }

    public void runHasAdoptable() {
        System.out.println(StorageManager.hasAdoptable());
    }

    public void runGetPrimaryStorageUuid() throws RemoteException {
        System.out.println(this.mSm.getPrimaryStorageUuid());
    }

    /* JADX INFO: Can't fix incorrect switch cases order, some code will duplicate */
    public void runSetForceAdoptable() throws RemoteException {
        char c;
        String nextArg = nextArg();
        switch (nextArg.hashCode()) {
            case 3551:
                if (nextArg.equals("on")) {
                    c = 0;
                    break;
                }
                c = 65535;
                break;
            case 109935:
                if (nextArg.equals("off")) {
                    c = 2;
                    break;
                }
                c = 65535;
                break;
            case 3569038:
                if (nextArg.equals("true")) {
                    c = 1;
                    break;
                }
                c = 65535;
                break;
            case 97196323:
                if (nextArg.equals("false")) {
                    c = 4;
                    break;
                }
                c = 65535;
                break;
            case 1544803905:
                if (nextArg.equals("default")) {
                    c = 3;
                    break;
                }
                c = 65535;
                break;
            default:
                c = 65535;
                break;
        }
        if (c == 0 || c == 1) {
            this.mSm.setDebugFlags(1, 3);
        } else if (c == 2) {
            this.mSm.setDebugFlags(2, 3);
        } else if (c == 3 || c == 4) {
            this.mSm.setDebugFlags(0, 3);
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:18:0x003f  */
    /* JADX WARNING: Removed duplicated region for block: B:22:0x0052  */
    public void runSetSdcardfs() throws RemoteException {
        char c;
        String nextArg = nextArg();
        int hashCode = nextArg.hashCode();
        if (hashCode != 3551) {
            if (hashCode != 109935) {
                if (hashCode == 1544803905 && nextArg.equals("default")) {
                    c = 2;
                    if (c != 0) {
                        this.mSm.setDebugFlags(8, 24);
                        return;
                    } else if (c == 1) {
                        this.mSm.setDebugFlags(16, 24);
                        return;
                    } else if (c == 2) {
                        this.mSm.setDebugFlags(0, 24);
                        return;
                    } else {
                        return;
                    }
                }
            } else if (nextArg.equals("off")) {
                c = 1;
                if (c != 0) {
                }
            }
        } else if (nextArg.equals("on")) {
            c = 0;
            if (c != 0) {
            }
        }
        c = 65535;
        if (c != 0) {
        }
    }

    public void runSetEmulateFbe() throws RemoteException {
        this.mSm.setDebugFlags(Boolean.parseBoolean(nextArg()) ? 4 : 0, 4);
    }

    public void runGetFbeMode() {
        if (StorageManager.isFileEncryptedNativeOnly()) {
            System.out.println("native");
        } else if (StorageManager.isFileEncryptedEmulatedOnly()) {
            System.out.println("emulated");
        } else {
            System.out.println("none");
        }
    }

    public void runPartition() throws RemoteException {
        String diskId = nextArg();
        String type = nextArg();
        if ("public".equals(type)) {
            this.mSm.partitionPublic(diskId);
        } else if ("private".equals(type)) {
            this.mSm.partitionPrivate(diskId);
        } else if ("mixed".equals(type)) {
            this.mSm.partitionMixed(diskId, Integer.parseInt(nextArg()));
        } else {
            throw new IllegalArgumentException("Unsupported partition type " + type);
        }
    }

    public void runMount() throws RemoteException {
        this.mSm.mount(nextArg());
    }

    public void runUnmount() throws RemoteException {
        this.mSm.unmount(nextArg());
    }

    public void runFormat() throws RemoteException {
        this.mSm.format(nextArg());
    }

    public void runBenchmark() throws Exception {
        String volId = nextArg();
        final CompletableFuture<PersistableBundle> result = new CompletableFuture<>();
        this.mSm.benchmark(volId, new IVoldTaskListener.Stub() {
            /* class com.android.commands.sm.Sm.AnonymousClass1 */

            public void onStatus(int status, PersistableBundle extras) {
            }

            public void onFinished(int status, PersistableBundle extras) {
                extras.size();
                result.complete(extras);
            }
        });
        System.out.println(result.get());
    }

    public void runForget() throws RemoteException {
        String fsUuid = nextArg();
        if ("all".equals(fsUuid)) {
            this.mSm.forgetAllVolumes();
        } else {
            this.mSm.forgetVolume(fsUuid);
        }
    }

    public void runFstrim() throws Exception {
        final CompletableFuture<PersistableBundle> result = new CompletableFuture<>();
        this.mSm.fstrim(0, new IVoldTaskListener.Stub() {
            /* class com.android.commands.sm.Sm.AnonymousClass2 */

            public void onStatus(int status, PersistableBundle extras) {
            }

            public void onFinished(int status, PersistableBundle extras) {
                extras.size();
                result.complete(extras);
            }
        });
        System.out.println(result.get());
    }

    public void runSetVirtualDisk() throws RemoteException {
        this.mSm.setDebugFlags(Boolean.parseBoolean(nextArg()) ? 32 : 0, 32);
    }

    /* JADX INFO: Can't fix incorrect switch cases order, some code will duplicate */
    public void runIsolatedStorage() throws RemoteException {
        char c;
        int value;
        String nextArg = nextArg();
        switch (nextArg.hashCode()) {
            case 3551:
                if (nextArg.equals("on")) {
                    c = 0;
                    break;
                }
                c = 65535;
                break;
            case 109935:
                if (nextArg.equals("off")) {
                    c = 2;
                    break;
                }
                c = 65535;
                break;
            case 3569038:
                if (nextArg.equals("true")) {
                    c = 1;
                    break;
                }
                c = 65535;
                break;
            case 97196323:
                if (nextArg.equals("false")) {
                    c = 4;
                    break;
                }
                c = 65535;
                break;
            case 1544803905:
                if (nextArg.equals("default")) {
                    c = 3;
                    break;
                }
                c = 65535;
                break;
            default:
                c = 65535;
                break;
        }
        if (c == 0 || c == 1) {
            value = 64;
        } else if (c == 2) {
            value = 128;
        } else if (c == 3 || c == 4) {
            value = 0;
        } else {
            return;
        }
        this.mSm.setDebugFlags(value, 192);
    }

    public void runIdleMaint() throws RemoteException {
        if ("run".equals(nextArg())) {
            this.mSm.runIdleMaintenance();
        } else {
            this.mSm.abortIdleMaintenance();
        }
    }

    private String nextArg() {
        int i = this.mNextArg;
        String[] strArr = this.mArgs;
        if (i >= strArr.length) {
            return null;
        }
        String arg = strArr[i];
        this.mNextArg = i + 1;
        return arg;
    }

    private static int showUsage() {
        System.err.println("usage: sm list-disks [adoptable]");
        System.err.println("       sm list-volumes [public|private|emulated|stub|all]");
        System.err.println("       sm has-adoptable");
        System.err.println("       sm get-primary-storage-uuid");
        System.err.println("       sm set-force-adoptable [on|off|default]");
        System.err.println("       sm set-virtual-disk [true|false]");
        System.err.println("");
        System.err.println("       sm partition DISK [public|private|mixed] [ratio]");
        System.err.println("       sm mount VOLUME");
        System.err.println("       sm unmount VOLUME");
        System.err.println("       sm format VOLUME");
        System.err.println("       sm benchmark VOLUME");
        System.err.println("       sm idle-maint [run|abort]");
        System.err.println("       sm fstrim");
        System.err.println("");
        System.err.println("       sm forget [UUID|all]");
        System.err.println("");
        System.err.println("       sm set-emulate-fbe [true|false]");
        System.err.println("");
        System.err.println("       sm set-isolated-storage [on|off|default]");
        System.err.println("");
        return 1;
    }
}
