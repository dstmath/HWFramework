package android.service.persistentdata;

import android.os.RemoteException;

public class PersistentDataBlockManager {
    public static final int FLASH_LOCK_LOCKED = 1;
    public static final int FLASH_LOCK_UNKNOWN = -1;
    public static final int FLASH_LOCK_UNLOCKED = 0;
    private static final String TAG = null;
    private IPersistentDataBlockService sService;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.service.persistentdata.PersistentDataBlockManager.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.service.persistentdata.PersistentDataBlockManager.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 7 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e9
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 8 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: android.service.persistentdata.PersistentDataBlockManager.<clinit>():void");
    }

    public PersistentDataBlockManager(IPersistentDataBlockService service) {
        this.sService = service;
    }

    public int write(byte[] data) {
        try {
            return this.sService.write(data);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public byte[] read() {
        try {
            return this.sService.read();
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public int getDataBlockSize() {
        try {
            return this.sService.getDataBlockSize();
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public long getMaximumDataBlockSize() {
        try {
            return this.sService.getMaximumDataBlockSize();
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public void wipe() {
        try {
            this.sService.wipe();
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public void setOemUnlockEnabled(boolean enabled) {
        try {
            this.sService.setOemUnlockEnabled(enabled);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public boolean getOemUnlockEnabled() {
        try {
            return this.sService.getOemUnlockEnabled();
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public int getFlashLockState() {
        try {
            return this.sService.getFlashLockState();
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }
}
