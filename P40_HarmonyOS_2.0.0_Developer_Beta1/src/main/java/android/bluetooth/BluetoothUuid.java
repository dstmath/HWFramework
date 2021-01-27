package android.bluetooth;

import android.annotation.UnsupportedAppUsage;
import android.os.ParcelUuid;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;
import java.util.HashSet;
import java.util.UUID;

public final class BluetoothUuid {
    @UnsupportedAppUsage(maxTargetSdk = 28, trackingBug = 115609023)
    public static final ParcelUuid AdvAudioDist = ParcelUuid.fromString("0000110D-0000-1000-8000-00805F9B34FB");
    @UnsupportedAppUsage(maxTargetSdk = 28, trackingBug = 115609023)
    public static final ParcelUuid AudioSink = ParcelUuid.fromString("0000110B-0000-1000-8000-00805F9B34FB");
    public static final ParcelUuid AudioSource = ParcelUuid.fromString("0000110A-0000-1000-8000-00805F9B34FB");
    public static final ParcelUuid AvrcpController = ParcelUuid.fromString("0000110E-0000-1000-8000-00805F9B34FB");
    public static final ParcelUuid AvrcpTarget = ParcelUuid.fromString("0000110C-0000-1000-8000-00805F9B34FB");
    public static final ParcelUuid BASE_UUID = ParcelUuid.fromString("00000000-0000-1000-8000-00805F9B34FB");
    public static final ParcelUuid BNEP = ParcelUuid.fromString("0000000f-0000-1000-8000-00805F9B34FB");
    @UnsupportedAppUsage(maxTargetSdk = 28, trackingBug = 115609023)
    public static final ParcelUuid HSP = ParcelUuid.fromString("00001108-0000-1000-8000-00805F9B34FB");
    public static final ParcelUuid HSP_AG = ParcelUuid.fromString("00001112-0000-1000-8000-00805F9B34FB");
    @UnsupportedAppUsage(maxTargetSdk = 28, trackingBug = 115609023)
    public static final ParcelUuid Handsfree = ParcelUuid.fromString("0000111E-0000-1000-8000-00805F9B34FB");
    public static final ParcelUuid Handsfree_AG = ParcelUuid.fromString("0000111F-0000-1000-8000-00805F9B34FB");
    public static final ParcelUuid HearingAid = ParcelUuid.fromString("0000FDF0-0000-1000-8000-00805f9b34fb");
    public static final ParcelUuid Hid = ParcelUuid.fromString("00001124-0000-1000-8000-00805f9b34fb");
    @UnsupportedAppUsage
    public static final ParcelUuid Hogp = ParcelUuid.fromString("00001812-0000-1000-8000-00805f9b34fb");
    public static final ParcelUuid MAP = ParcelUuid.fromString("00001134-0000-1000-8000-00805F9B34FB");
    public static final ParcelUuid MAS = ParcelUuid.fromString("00001132-0000-1000-8000-00805F9B34FB");
    public static final ParcelUuid MNS = ParcelUuid.fromString("00001133-0000-1000-8000-00805F9B34FB");
    @UnsupportedAppUsage
    public static final ParcelUuid NAP = ParcelUuid.fromString("00001116-0000-1000-8000-00805F9B34FB");
    @UnsupportedAppUsage
    public static final ParcelUuid ObexObjectPush = ParcelUuid.fromString("00001105-0000-1000-8000-00805f9b34fb");
    public static final ParcelUuid PANU = ParcelUuid.fromString("00001115-0000-1000-8000-00805F9B34FB");
    public static final ParcelUuid PBAP_PCE = ParcelUuid.fromString("0000112e-0000-1000-8000-00805F9B34FB");
    @UnsupportedAppUsage
    public static final ParcelUuid PBAP_PSE = ParcelUuid.fromString("0000112f-0000-1000-8000-00805F9B34FB");
    @UnsupportedAppUsage
    public static final ParcelUuid[] RESERVED_UUIDS = {AudioSink, AudioSource, AdvAudioDist, HSP, Handsfree, AvrcpController, AvrcpTarget, ObexObjectPush, PANU, NAP, MAP, MNS, MAS, SAP};
    public static final ParcelUuid SAP = ParcelUuid.fromString("0000112D-0000-1000-8000-00805F9B34FB");
    public static final int UUID_BYTES_128_BIT = 16;
    public static final int UUID_BYTES_16_BIT = 2;
    public static final int UUID_BYTES_32_BIT = 4;

    @UnsupportedAppUsage
    public static boolean isAudioSource(ParcelUuid uuid) {
        return uuid.equals(AudioSource);
    }

    public static boolean isAudioSink(ParcelUuid uuid) {
        return uuid.equals(AudioSink);
    }

    @UnsupportedAppUsage
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

    @UnsupportedAppUsage
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

    @UnsupportedAppUsage
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

    @UnsupportedAppUsage
    public static boolean containsAnyUuid(ParcelUuid[] uuidA, ParcelUuid[] uuidB) {
        if (uuidA == null && uuidB == null) {
            return true;
        }
        if (uuidA == null) {
            if (uuidB.length == 0) {
                return true;
            }
            return false;
        } else if (uuidB != null) {
            HashSet<ParcelUuid> uuidSet = new HashSet<>(Arrays.asList(uuidA));
            for (ParcelUuid uuid : uuidB) {
                if (uuidSet.contains(uuid)) {
                    return true;
                }
            }
            return false;
        } else if (uuidA.length == 0) {
            return true;
        } else {
            return false;
        }
    }

    public static boolean containsAllUuids(ParcelUuid[] uuidA, ParcelUuid[] uuidB) {
        if (uuidA == null && uuidB == null) {
            return true;
        }
        if (uuidA == null) {
            if (uuidB.length == 0) {
                return true;
            }
            return false;
        } else if (uuidB == null) {
            return true;
        } else {
            HashSet<ParcelUuid> uuidSet = new HashSet<>(Arrays.asList(uuidA));
            for (ParcelUuid uuid : uuidB) {
                if (!uuidSet.contains(uuid)) {
                    return false;
                }
            }
            return true;
        }
    }

    public static int getServiceIdentifierFromParcelUuid(ParcelUuid parcelUuid) {
        return (int) ((parcelUuid.getUuid().getMostSignificantBits() & -4294967296L) >>> 32);
    }

    public static ParcelUuid parseUuidFrom(byte[] uuidBytes) {
        long shortUuid;
        if (uuidBytes != null) {
            int length = uuidBytes.length;
            if (length != 2 && length != 4 && length != 16) {
                throw new IllegalArgumentException("uuidBytes length invalid - " + length);
            } else if (length == 16) {
                ByteBuffer buf = ByteBuffer.wrap(uuidBytes).order(ByteOrder.LITTLE_ENDIAN);
                return new ParcelUuid(new UUID(buf.getLong(8), buf.getLong(0)));
            } else {
                if (length == 2) {
                    shortUuid = ((long) (uuidBytes[0] & 255)) + ((long) ((uuidBytes[1] & 255) << 8));
                } else {
                    shortUuid = ((long) ((uuidBytes[3] & 255) << 24)) + ((long) (uuidBytes[0] & 255)) + ((long) ((uuidBytes[1] & 255) << 8)) + ((long) ((uuidBytes[2] & 255) << 16));
                }
                return new ParcelUuid(new UUID(BASE_UUID.getUuid().getMostSignificantBits() + (shortUuid << 32), BASE_UUID.getUuid().getLeastSignificantBits()));
            }
        } else {
            throw new IllegalArgumentException("uuidBytes cannot be null");
        }
    }

    public static byte[] uuidToBytes(ParcelUuid uuid) {
        if (uuid == null) {
            throw new IllegalArgumentException("uuid cannot be null");
        } else if (is16BitUuid(uuid)) {
            int uuidVal = getServiceIdentifierFromParcelUuid(uuid);
            return new byte[]{(byte) (uuidVal & 255), (byte) ((65280 & uuidVal) >> 8)};
        } else if (is32BitUuid(uuid)) {
            int uuidVal2 = getServiceIdentifierFromParcelUuid(uuid);
            return new byte[]{(byte) (uuidVal2 & 255), (byte) ((65280 & uuidVal2) >> 8), (byte) ((16711680 & uuidVal2) >> 16), (byte) ((-16777216 & uuidVal2) >> 24)};
        } else {
            long msb = uuid.getUuid().getMostSignificantBits();
            long lsb = uuid.getUuid().getLeastSignificantBits();
            byte[] uuidBytes = new byte[16];
            ByteBuffer buf = ByteBuffer.wrap(uuidBytes).order(ByteOrder.LITTLE_ENDIAN);
            buf.putLong(8, msb);
            buf.putLong(0, lsb);
            return uuidBytes;
        }
    }

    @UnsupportedAppUsage
    public static boolean is16BitUuid(ParcelUuid parcelUuid) {
        UUID uuid = parcelUuid.getUuid();
        if (uuid.getLeastSignificantBits() == BASE_UUID.getUuid().getLeastSignificantBits() && (uuid.getMostSignificantBits() & -281470681743361L) == 4096) {
            return true;
        }
        return false;
    }

    @UnsupportedAppUsage
    public static boolean is32BitUuid(ParcelUuid parcelUuid) {
        UUID uuid = parcelUuid.getUuid();
        if (uuid.getLeastSignificantBits() == BASE_UUID.getUuid().getLeastSignificantBits() && !is16BitUuid(parcelUuid) && (uuid.getMostSignificantBits() & 4294967295L) == 4096) {
            return true;
        }
        return false;
    }
}
