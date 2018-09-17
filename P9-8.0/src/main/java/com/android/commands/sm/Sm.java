package com.android.commands.sm;

import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.SystemProperties;
import android.os.storage.DiskInfo;
import android.os.storage.IStorageManager;
import android.os.storage.IStorageManager.Stub;
import android.os.storage.StorageManager;
import android.os.storage.VolumeInfo;
import android.util.Log;

public final class Sm {
    private static final String TAG = "Sm";
    private String[] mArgs;
    private String mCurArgData;
    private int mNextArg;
    IStorageManager mSm;

    public static void main(String[] args) {
        int i = 1;
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
        if (success) {
            i = 0;
        }
        System.exit(i);
    }

    public void run(String[] args) throws Exception {
        if (args.length < 1) {
            throw new IllegalArgumentException();
        }
        this.mSm = Stub.asInterface(ServiceManager.getService("mount"));
        if (this.mSm == null) {
            throw new RemoteException("Failed to find running mount service");
        }
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
        } else if ("fstrim".equals(op)) {
            runFstrim();
        } else if ("set-virtual-disk".equals(op)) {
            runSetVirtualDisk();
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
        int i = 0;
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
        VolumeInfo[] vols = this.mSm.getVolumes(0);
        int length = vols.length;
        while (i < length) {
            VolumeInfo vol = vols[i];
            if (filterType == -1 || filterType == vol.getType()) {
                System.out.println(vol.getId() + " " + VolumeInfo.getEnvironmentForState(vol.getState()) + " " + vol.getFsUuid());
            }
            i++;
        }
    }

    public void runHasAdoptable() {
        System.out.println(SystemProperties.getBoolean("vold.has_adoptable", false));
    }

    public void runGetPrimaryStorageUuid() throws RemoteException {
        System.out.println(this.mSm.getPrimaryStorageUuid());
    }

    public void runSetForceAdoptable() throws RemoteException {
        this.mSm.setDebugFlags(Boolean.parseBoolean(nextArg()) ? 1 : 0, 1);
    }

    public void runSetSdcardfs() throws RemoteException {
        String nextArg = nextArg();
        if (nextArg.equals("on")) {
            this.mSm.setDebugFlags(4, 12);
        } else if (nextArg.equals("off")) {
            this.mSm.setDebugFlags(8, 12);
        } else if (nextArg.equals("default")) {
            this.mSm.setDebugFlags(0, 12);
        }
    }

    public void runSetEmulateFbe() throws RemoteException {
        this.mSm.setDebugFlags(Boolean.parseBoolean(nextArg()) ? 2 : 0, 2);
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

    public void runBenchmark() throws RemoteException {
        this.mSm.benchmark(nextArg());
    }

    public void runForget() throws RemoteException {
        String fsUuid = nextArg();
        if ("all".equals(fsUuid)) {
            this.mSm.forgetAllVolumes();
        } else {
            this.mSm.forgetVolume(fsUuid);
        }
    }

    public void runFstrim() throws RemoteException {
        this.mSm.fstrim(0);
    }

    public void runSetVirtualDisk() throws RemoteException {
        this.mSm.setDebugFlags(Boolean.parseBoolean(nextArg()) ? 16 : 0, 16);
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
        System.err.println("       sm set-force-adoptable [true|false]");
        System.err.println("       sm set-virtual-disk [true|false]");
        System.err.println("");
        System.err.println("       sm partition DISK [public|private|mixed] [ratio]");
        System.err.println("       sm mount VOLUME");
        System.err.println("       sm unmount VOLUME");
        System.err.println("       sm format VOLUME");
        System.err.println("       sm benchmark VOLUME");
        System.err.println("       sm fstrim");
        System.err.println("");
        System.err.println("       sm forget [UUID|all]");
        System.err.println("");
        System.err.println("       sm set-emulate-fbe [true|false]");
        System.err.println("");
        return 1;
    }
}
