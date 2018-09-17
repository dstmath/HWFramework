package android.bluetooth;

import android.content.res.HwPCMultiWindowCompatibility;
import android.net.wifi.hotspot2.pps.UpdateParameter;
import android.os.ParcelUuid;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;
import java.util.HashSet;
import java.util.UUID;

public final class BluetoothUuid {
    public static final ParcelUuid AdvAudioDist = ParcelUuid.fromString("0000110D-0000-1000-8000-00805F9B34FB");
    public static final ParcelUuid AudioSink = ParcelUuid.fromString("0000110B-0000-1000-8000-00805F9B34FB");
    public static final ParcelUuid AudioSource = ParcelUuid.fromString("0000110A-0000-1000-8000-00805F9B34FB");
    public static final ParcelUuid AvrcpController = ParcelUuid.fromString("0000110E-0000-1000-8000-00805F9B34FB");
    public static final ParcelUuid AvrcpTarget = ParcelUuid.fromString("0000110C-0000-1000-8000-00805F9B34FB");
    public static final ParcelUuid BASE_UUID = ParcelUuid.fromString("00000000-0000-1000-8000-00805F9B34FB");
    public static final ParcelUuid BNEP = ParcelUuid.fromString("0000000f-0000-1000-8000-00805F9B34FB");
    public static final ParcelUuid HSP = ParcelUuid.fromString("00001108-0000-1000-8000-00805F9B34FB");
    public static final ParcelUuid HSP_AG = ParcelUuid.fromString("00001112-0000-1000-8000-00805F9B34FB");
    public static final ParcelUuid Handsfree = ParcelUuid.fromString("0000111E-0000-1000-8000-00805F9B34FB");
    public static final ParcelUuid Handsfree_AG = ParcelUuid.fromString("0000111F-0000-1000-8000-00805F9B34FB");
    public static final ParcelUuid Hid = ParcelUuid.fromString("00001124-0000-1000-8000-00805f9b34fb");
    public static final ParcelUuid Hogp = ParcelUuid.fromString("00001812-0000-1000-8000-00805f9b34fb");
    public static final ParcelUuid MAP = ParcelUuid.fromString("00001134-0000-1000-8000-00805F9B34FB");
    public static final ParcelUuid MAS = ParcelUuid.fromString("00001132-0000-1000-8000-00805F9B34FB");
    public static final ParcelUuid MNS = ParcelUuid.fromString("00001133-0000-1000-8000-00805F9B34FB");
    public static final ParcelUuid NAP = ParcelUuid.fromString("00001116-0000-1000-8000-00805F9B34FB");
    public static final ParcelUuid ObexObjectPush = ParcelUuid.fromString("00001105-0000-1000-8000-00805f9b34fb");
    public static final ParcelUuid PANU = ParcelUuid.fromString("00001115-0000-1000-8000-00805F9B34FB");
    public static final ParcelUuid PBAP_PCE = ParcelUuid.fromString("0000112e-0000-1000-8000-00805F9B34FB");
    public static final ParcelUuid PBAP_PSE = ParcelUuid.fromString("0000112f-0000-1000-8000-00805F9B34FB");
    public static final ParcelUuid[] RESERVED_UUIDS = new ParcelUuid[]{AudioSink, AudioSource, AdvAudioDist, HSP, Handsfree, AvrcpController, AvrcpTarget, ObexObjectPush, PANU, NAP, MAP, MNS, MAS, SAP};
    public static final ParcelUuid SAP = ParcelUuid.fromString("0000112D-0000-1000-8000-00805F9B34FB");
    public static final int UUID_BYTES_128_BIT = 16;
    public static final int UUID_BYTES_16_BIT = 2;
    public static final int UUID_BYTES_32_BIT = 4;

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
        if (length != 2 && length != 4 && length != 16) {
            throw new IllegalArgumentException("uuidBytes length invalid - " + length);
        } else if (length == 16) {
            ByteBuffer buf = ByteBuffer.wrap(uuidBytes).order(ByteOrder.LITTLE_ENDIAN);
            return new ParcelUuid(new UUID(buf.getLong(8), buf.getLong(0)));
        } else {
            long shortUuid;
            if (length == 2) {
                shortUuid = ((long) (uuidBytes[0] & 255)) + ((long) ((uuidBytes[1] & 255) << 8));
            } else {
                shortUuid = ((((long) (uuidBytes[0] & 255)) + ((long) ((uuidBytes[1] & 255) << 8))) + ((long) ((uuidBytes[2] & 255) << 16))) + ((long) ((uuidBytes[3] & 255) << 24));
            }
            return new ParcelUuid(new UUID(BASE_UUID.getUuid().getMostSignificantBits() + (shortUuid << 32), BASE_UUID.getUuid().getLeastSignificantBits()));
        }
    }

    public static byte[] uuidToBytes(ParcelUuid uuid) {
        byte[] uuidBytes;
        int uuidVal;
        if (uuid == null) {
            throw new IllegalArgumentException("uuid cannot be null");
        } else if (is16BitUuid(uuid)) {
            uuidBytes = new byte[2];
            uuidVal = getServiceIdentifierFromParcelUuid(uuid);
            uuidBytes[0] = (byte) (uuidVal & 255);
            uuidBytes[1] = (byte) ((uuidVal & HwPCMultiWindowCompatibility.WINDOW_ACTION_MODE_MASK) >> 8);
            return uuidBytes;
        } else if (is32BitUuid(uuid)) {
            uuidBytes = new byte[4];
            uuidVal = getServiceIdentifierFromParcelUuid(uuid);
            uuidBytes[0] = (byte) (uuidVal & 255);
            uuidBytes[1] = (byte) ((uuidVal & HwPCMultiWindowCompatibility.WINDOW_ACTION_MODE_MASK) >> 8);
            uuidBytes[2] = (byte) ((HwPCMultiWindowCompatibility.WINDOW_VIDEO_MASK_COULD_ONLY_FULLSCREEN & uuidVal) >> 16);
            uuidBytes[3] = (byte) ((-16777216 & uuidVal) >> 24);
            return uuidBytes;
        } else {
            long msb = uuid.getUuid().getMostSignificantBits();
            long lsb = uuid.getUuid().getLeastSignificantBits();
            uuidBytes = new byte[16];
            ByteBuffer buf = ByteBuffer.wrap(uuidBytes).order(ByteOrder.LITTLE_ENDIAN);
            buf.putLong(8, msb);
            buf.putLong(0, lsb);
            return uuidBytes;
        }
    }

    public static boolean is16BitUuid(ParcelUuid parcelUuid) {
        boolean z = false;
        UUID uuid = parcelUuid.getUuid();
        if (uuid.getLeastSignificantBits() != BASE_UUID.getUuid().getLeastSignificantBits()) {
            return false;
        }
        if ((uuid.getMostSignificantBits() & -281470681743361L) == 4096) {
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
        if ((uuid.getMostSignificantBits() & UpdateParameter.UPDATE_CHECK_INTERVAL_NEVER) == 4096) {
            z = true;
        }
        return z;
    }
}
