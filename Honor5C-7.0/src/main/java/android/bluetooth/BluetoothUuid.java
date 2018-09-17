package android.bluetooth;

import android.os.ParcelUuid;
import android.os.Process;
import android.os.Trace;
import android.security.keymaster.KeymasterArguments;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;
import java.util.HashSet;
import java.util.UUID;

public final class BluetoothUuid {
    public static final ParcelUuid AdvAudioDist = null;
    public static final ParcelUuid AudioSink = null;
    public static final ParcelUuid AudioSource = null;
    public static final ParcelUuid AvrcpController = null;
    public static final ParcelUuid AvrcpTarget = null;
    public static final ParcelUuid BASE_UUID = null;
    public static final ParcelUuid BNEP = null;
    public static final ParcelUuid HSP = null;
    public static final ParcelUuid HSP_AG = null;
    public static final ParcelUuid Handsfree = null;
    public static final ParcelUuid Handsfree_AG = null;
    public static final ParcelUuid Hid = null;
    public static final ParcelUuid Hogp = null;
    public static final ParcelUuid MAP = null;
    public static final ParcelUuid MAS = null;
    public static final ParcelUuid MNS = null;
    public static final ParcelUuid NAP = null;
    public static final ParcelUuid ObexObjectPush = null;
    public static final ParcelUuid PANU = null;
    public static final ParcelUuid PBAP_PCE = null;
    public static final ParcelUuid PBAP_PSE = null;
    public static final ParcelUuid[] RESERVED_UUIDS = null;
    public static final ParcelUuid SAP = null;
    public static final int UUID_BYTES_128_BIT = 16;
    public static final int UUID_BYTES_16_BIT = 2;
    public static final int UUID_BYTES_32_BIT = 4;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.bluetooth.BluetoothUuid.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.bluetooth.BluetoothUuid.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 7 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 8 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: android.bluetooth.BluetoothUuid.<clinit>():void");
    }

    public static boolean isAudioSource(ParcelUuid uuid) {
        return uuid.equals(AudioSource);
    }

    public static boolean isAudioSink(ParcelUuid uuid) {
        return uuid.equals(AudioSink);
    }

    public static boolean isAdvAudioDist(ParcelUuid uuid) {
        return uuid.equals(AdvAudioDist);
    }

    public static boolean isHandsfree(ParcelUuid uuid) {
        return uuid.equals(Handsfree);
    }

    public static boolean isHeadset(ParcelUuid uuid) {
        return uuid.equals(HSP);
    }

    public static boolean isAvrcpController(ParcelUuid uuid) {
        return uuid.equals(AvrcpController);
    }

    public static boolean isAvrcpTarget(ParcelUuid uuid) {
        return uuid.equals(AvrcpTarget);
    }

    public static boolean isInputDevice(ParcelUuid uuid) {
        return uuid.equals(Hid);
    }

    public static boolean isPanu(ParcelUuid uuid) {
        return uuid.equals(PANU);
    }

    public static boolean isNap(ParcelUuid uuid) {
        return uuid.equals(NAP);
    }

    public static boolean isBnep(ParcelUuid uuid) {
        return uuid.equals(BNEP);
    }

    public static boolean isMap(ParcelUuid uuid) {
        return uuid.equals(MAP);
    }

    public static boolean isMns(ParcelUuid uuid) {
        return uuid.equals(MNS);
    }

    public static boolean isMas(ParcelUuid uuid) {
        return uuid.equals(MAS);
    }

    public static boolean isSap(ParcelUuid uuid) {
        return uuid.equals(SAP);
    }

    public static boolean isUuidPresent(ParcelUuid[] uuidArray, ParcelUuid uuid) {
        if ((uuidArray == null || uuidArray.length == 0) && uuid == null) {
            return true;
        }
        if (uuidArray == null) {
            return false;
        }
        for (ParcelUuid element : uuidArray) {
            if (element.equals(uuid)) {
                return true;
            }
        }
        return false;
    }

    public static boolean containsAnyUuid(ParcelUuid[] uuidA, ParcelUuid[] uuidB) {
        boolean z = true;
        if (uuidA == null && uuidB == null) {
            return true;
        }
        if (uuidA == null) {
            if (uuidB.length != 0) {
                z = false;
            }
            return z;
        } else if (uuidB == null) {
            if (uuidA.length != 0) {
                z = false;
            }
            return z;
        } else {
            HashSet<ParcelUuid> uuidSet = new HashSet(Arrays.asList(uuidA));
            for (ParcelUuid uuid : uuidB) {
                if (uuidSet.contains(uuid)) {
                    return true;
                }
            }
            return false;
        }
    }

    public static boolean containsAllUuids(ParcelUuid[] uuidA, ParcelUuid[] uuidB) {
        boolean z = true;
        if (uuidA == null && uuidB == null) {
            return true;
        }
        if (uuidA == null) {
            if (uuidB.length != 0) {
                z = false;
            }
            return z;
        } else if (uuidB == null) {
            return true;
        } else {
            HashSet<ParcelUuid> uuidSet = new HashSet(Arrays.asList(uuidA));
            for (ParcelUuid uuid : uuidB) {
                if (!uuidSet.contains(uuid)) {
                    return false;
                }
            }
            return true;
        }
    }

    public static int getServiceIdentifierFromParcelUuid(ParcelUuid parcelUuid) {
        return (int) ((parcelUuid.getUuid().getMostSignificantBits() & 281470681743360L) >>> 32);
    }

    public static ParcelUuid parseUuidFrom(byte[] uuidBytes) {
        if (uuidBytes == null) {
            throw new IllegalArgumentException("uuidBytes cannot be null");
        }
        int length = uuidBytes.length;
        if (length != UUID_BYTES_16_BIT && length != UUID_BYTES_32_BIT && length != UUID_BYTES_128_BIT) {
            throw new IllegalArgumentException("uuidBytes length invalid - " + length);
        } else if (length == UUID_BYTES_128_BIT) {
            ByteBuffer buf = ByteBuffer.wrap(uuidBytes).order(ByteOrder.LITTLE_ENDIAN);
            return new ParcelUuid(new UUID(buf.getLong(8), buf.getLong(0)));
        } else {
            long shortUuid;
            if (length == UUID_BYTES_16_BIT) {
                shortUuid = ((long) (uuidBytes[0] & Process.PROC_TERM_MASK)) + ((long) ((uuidBytes[1] & Process.PROC_TERM_MASK) << 8));
            } else {
                shortUuid = ((((long) (uuidBytes[0] & Process.PROC_TERM_MASK)) + ((long) ((uuidBytes[1] & Process.PROC_TERM_MASK) << 8))) + ((long) ((uuidBytes[UUID_BYTES_16_BIT] & Process.PROC_TERM_MASK) << UUID_BYTES_128_BIT))) + ((long) ((uuidBytes[3] & Process.PROC_TERM_MASK) << 24));
            }
            return new ParcelUuid(new UUID(BASE_UUID.getUuid().getMostSignificantBits() + (shortUuid << 32), BASE_UUID.getUuid().getLeastSignificantBits()));
        }
    }

    public static boolean is16BitUuid(ParcelUuid parcelUuid) {
        boolean z = false;
        UUID uuid = parcelUuid.getUuid();
        if (uuid.getLeastSignificantBits() != BASE_UUID.getUuid().getLeastSignificantBits()) {
            return false;
        }
        if ((uuid.getMostSignificantBits() & -281470681743361L) == Trace.TRACE_TAG_APP) {
            z = true;
        }
        return z;
    }

    public static boolean is32BitUuid(ParcelUuid parcelUuid) {
        boolean z = false;
        UUID uuid = parcelUuid.getUuid();
        if (uuid.getLeastSignificantBits() != BASE_UUID.getUuid().getLeastSignificantBits() || is16BitUuid(parcelUuid)) {
            return false;
        }
        if ((uuid.getMostSignificantBits() & KeymasterArguments.UINT32_MAX_VALUE) == Trace.TRACE_TAG_APP) {
            z = true;
        }
        return z;
    }
}
