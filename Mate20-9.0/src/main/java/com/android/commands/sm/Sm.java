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
import java.util.concurrent.CompletableFuture;

public final class Sm {
    private static final String TAG = "Sm";
    private String[] mArgs;
    private String mCurArgData;
    private int mNextArg;
    IStorageManager mSm;

    public static void main(String[] args) {
        int i = 0;
        boolean success = false;
        try {
            new Sm().run(args);
            success = true;
        } catch (Exception e) {
            if (e instanceof IllegalArgumentException) {
                showUsage();
                System.exit(1);
            }
            Log.e(TAG, "Error", e);
            System.err.println("Error: " + e);
        }
        if (!success) {
            i = 1;
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
        for (DiskInfo disk : this.mSm.getDisks()) {
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
        } else {
            filterType = -1;
        }
        for (VolumeInfo vol : this.mSm.getVolumes(0)) {
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

    /* JADX WARNING: Removed duplicated region for block: B:27:0x005d  */
    /* JADX WARNING: Removed duplicated region for block: B:28:0x0063  */
    /* JADX WARNING: Removed duplicated region for block: B:29:0x0069  */
    /* JADX WARNING: Removed duplicated region for block: B:33:? A[RETURN, SYNTHETIC] */
    public void runSetForceAdoptable() throws RemoteException {
        char c;
        String nextArg = nextArg();
        int hashCode = nextArg.hashCode();
        if (hashCode == 3551) {
            if (nextArg.equals("on")) {
                c = 0;
                switch (c) {
                    case 0:
                    case 1:
                        break;
                    case 2:
                        break;
                    case 3:
                    case 4:
                        break;
                }
            }
        } else if (hashCode == 109935) {
            if (nextArg.equals("off")) {
                c = 2;
                switch (c) {
                    case 0:
                    case 1:
                        break;
                    case 2:
                        break;
                    case 3:
                    case 4:
                        break;
                }
            }
        } else if (hashCode == 3569038) {
            if (nextArg.equals("true")) {
                c = 1;
                switch (c) {
                    case 0:
                    case 1:
                        break;
                    case 2:
                        break;
                    case 3:
                    case 4:
                        break;
                }
            }
        } else if (hashCode == 97196323) {
            if (nextArg.equals("false")) {
                c = 4;
                switch (c) {
                    case 0:
                    case 1:
                        break;
                    case 2:
                        break;
                    case 3:
                    case 4:
                        break;
                }
            }
        } else if (hashCode == 1544803905 && nextArg.equals("default")) {
            c = 3;
            switch (c) {
                case 0:
                case 1:
                    this.mSm.setDebugFlags(1, 3);
                    return;
                case 2:
                    this.mSm.setDebugFlags(2, 3);
                    return;
                case 3:
                case 4:
                    this.mSm.setDebugFlags(0, 3);
                    return;
                default:
                    return;
            }
        }
        c = 65535;
        switch (c) {
            case 0:
            case 1:
                break;
            case 2:
                break;
            case 3:
            case 4:
                break;
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:18:0x003f  */
    /* JADX WARNING: Removed duplicated region for block: B:19:0x0045  */
    /* JADX WARNING: Removed duplicated region for block: B:20:0x004d  */
    /* JADX WARNING: Removed duplicated region for block: B:24:? A[RETURN, SYNTHETIC] */
    public void runSetSdcardfs() throws RemoteException {
        char c;
        String nextArg = nextArg();
        int hashCode = nextArg.hashCode();
        if (hashCode == 3551) {
            if (nextArg.equals("on")) {
                c = 0;
                switch (c) {
                    case 0:
                        break;
                    case 1:
                        break;
                    case 2:
                        break;
                }
            }
        } else if (hashCode == 109935) {
            if (nextArg.equals("off")) {
                c = 1;
                switch (c) {
                    case 0:
                        break;
                    case 1:
                        break;
                    case 2:
                        break;
                }
            }
        } else if (hashCode == 1544803905 && nextArg.equals("default")) {
            c = 2;
            switch (c) {
                case 0:
                    this.mSm.setDebugFlags(8, 24);
                    return;
                case 1:
                    this.mSm.setDebugFlags(16, 24);
                    return;
                case 2:
                    this.mSm.setDebugFlags(0, 24);
                    return;
                default:
                    return;
            }
        }
        c = 65535;
        switch (c) {
            case 0:
                break;
            case 1:
                break;
            case 2:
                break;
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

    public void runIdleMaint() throws RemoteException {
        if ("run".equals(nextArg())) {
            this.mSm.runIdleMaintenance();
        } else {
            this.mSm.abortIdleMaintenance();
        }
    }

    private String nextArg() {
        if (this.mNextArg >= this.mArgs.length) {
            return null;
        }
        String arg = this.mArgs[this.mNextArg];
        this.mNextArg++;
        return arg;
    }

    private static int showUsage() {
        System.err.println("usage: sm list-disks [adoptable]");
        System.err.println("       sm list-volumes [public|private|emulated|all]");
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
        return 1;
    }
}
