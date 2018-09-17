package android.nfc;

import android.nfc.INfcTag.Stub;
import android.nfc.tech.IsoDep;
import android.nfc.tech.MifareClassic;
import android.nfc.tech.MifareUltralight;
import android.nfc.tech.Ndef;
import android.nfc.tech.NdefFormatable;
import android.nfc.tech.NfcA;
import android.nfc.tech.NfcB;
import android.nfc.tech.NfcBarcode;
import android.nfc.tech.NfcF;
import android.nfc.tech.NfcV;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import android.os.RemoteException;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;

public final class Tag implements Parcelable {
    public static final Creator<Tag> CREATOR = new Creator<Tag>() {
        public Tag createFromParcel(Parcel in) {
            INfcTag tagService;
            byte[] id = Tag.readBytesWithNull(in);
            int[] techList = new int[in.readInt()];
            in.readIntArray(techList);
            Bundle[] techExtras = (Bundle[]) in.createTypedArray(Bundle.CREATOR);
            int serviceHandle = in.readInt();
            if (in.readInt() == 0) {
                tagService = Stub.asInterface(in.readStrongBinder());
            } else {
                tagService = null;
            }
            return new Tag(id, techList, techExtras, serviceHandle, tagService);
        }

        public Tag[] newArray(int size) {
            return new Tag[size];
        }
    };
    int mConnectedTechnology;
    final byte[] mId;
    final int mServiceHandle;
    final INfcTag mTagService;
    final Bundle[] mTechExtras;
    final int[] mTechList;
    final String[] mTechStringList;

    public Tag(byte[] id, int[] techList, Bundle[] techListExtras, int serviceHandle, INfcTag tagService) {
        if (techList == null) {
            throw new IllegalArgumentException("rawTargets cannot be null");
        }
        this.mId = id;
        this.mTechList = Arrays.copyOf(techList, techList.length);
        this.mTechStringList = generateTechStringList(techList);
        this.mTechExtras = (Bundle[]) Arrays.copyOf(techListExtras, techList.length);
        this.mServiceHandle = serviceHandle;
        this.mTagService = tagService;
        this.mConnectedTechnology = -1;
    }

    public static Tag createMockTag(byte[] id, int[] techList, Bundle[] techListExtras) {
        return new Tag(id, techList, techListExtras, 0, null);
    }

    private String[] generateTechStringList(int[] techList) {
        int size = techList.length;
        String[] strings = new String[size];
        for (int i = 0; i < size; i++) {
            switch (techList[i]) {
                case 1:
                    strings[i] = NfcA.class.getName();
                    break;
                case 2:
                    strings[i] = NfcB.class.getName();
                    break;
                case 3:
                    strings[i] = IsoDep.class.getName();
                    break;
                case 4:
                    strings[i] = NfcF.class.getName();
                    break;
                case 5:
                    strings[i] = NfcV.class.getName();
                    break;
                case 6:
                    strings[i] = Ndef.class.getName();
                    break;
                case 7:
                    strings[i] = NdefFormatable.class.getName();
                    break;
                case 8:
                    strings[i] = MifareClassic.class.getName();
                    break;
                case 9:
                    strings[i] = MifareUltralight.class.getName();
                    break;
                case 10:
                    strings[i] = NfcBarcode.class.getName();
                    break;
                default:
                    throw new IllegalArgumentException("Unknown tech type " + techList[i]);
            }
        }
        return strings;
    }

    static int[] getTechCodesFromStrings(String[] techStringList) throws IllegalArgumentException {
        if (techStringList == null) {
            throw new IllegalArgumentException("List cannot be null");
        }
        int[] techIntList = new int[techStringList.length];
        HashMap<String, Integer> stringToCodeMap = getTechStringToCodeMap();
        for (int i = 0; i < techStringList.length; i++) {
            Integer code = (Integer) stringToCodeMap.get(techStringList[i]);
            if (code == null) {
                throw new IllegalArgumentException("Unknown tech type " + techStringList[i]);
            }
            techIntList[i] = code.intValue();
        }
        return techIntList;
    }

    private static HashMap<String, Integer> getTechStringToCodeMap() {
        HashMap<String, Integer> techStringToCodeMap = new HashMap();
        techStringToCodeMap.put(IsoDep.class.getName(), Integer.valueOf(3));
        techStringToCodeMap.put(MifareClassic.class.getName(), Integer.valueOf(8));
        techStringToCodeMap.put(MifareUltralight.class.getName(), Integer.valueOf(9));
        techStringToCodeMap.put(Ndef.class.getName(), Integer.valueOf(6));
        techStringToCodeMap.put(NdefFormatable.class.getName(), Integer.valueOf(7));
        techStringToCodeMap.put(NfcA.class.getName(), Integer.valueOf(1));
        techStringToCodeMap.put(NfcB.class.getName(), Integer.valueOf(2));
        techStringToCodeMap.put(NfcF.class.getName(), Integer.valueOf(4));
        techStringToCodeMap.put(NfcV.class.getName(), Integer.valueOf(5));
        techStringToCodeMap.put(NfcBarcode.class.getName(), Integer.valueOf(10));
        return techStringToCodeMap;
    }

    public int getServiceHandle() {
        return this.mServiceHandle;
    }

    public int[] getTechCodeList() {
        return this.mTechList;
    }

    public byte[] getId() {
        return this.mId;
    }

    public String[] getTechList() {
        return this.mTechStringList;
    }

    public Tag rediscover() throws IOException {
        if (getConnectedTechnology() != -1) {
            throw new IllegalStateException("Close connection to the technology first!");
        } else if (this.mTagService == null) {
            throw new IOException("Mock tags don't support this operation.");
        } else {
            try {
                Tag newTag = this.mTagService.rediscover(getServiceHandle());
                if (newTag != null) {
                    return newTag;
                }
                throw new IOException("Failed to rediscover tag");
            } catch (RemoteException e) {
                throw new IOException("NFC service dead");
            }
        }
    }

    public boolean hasTech(int techType) {
        for (int tech : this.mTechList) {
            if (tech == techType) {
                return true;
            }
        }
        return false;
    }

    public Bundle getTechExtras(int tech) {
        int pos = -1;
        for (int idx = 0; idx < this.mTechList.length; idx++) {
            if (this.mTechList[idx] == tech) {
                pos = idx;
                break;
            }
        }
        if (pos < 0) {
            return null;
        }
        return this.mTechExtras[pos];
    }

    public INfcTag getTagService() {
        return this.mTagService;
    }

    public String toString() {
        StringBuilder sb = new StringBuilder("TAG: Tech [");
        String[] techList = getTechList();
        int length = techList.length;
        for (int i = 0; i < length; i++) {
            sb.append(techList[i]);
            if (i < length - 1) {
                sb.append(", ");
            }
        }
        sb.append("]");
        return sb.toString();
    }

    static byte[] readBytesWithNull(Parcel in) {
        int len = in.readInt();
        if (len < 0) {
            return null;
        }
        byte[] result = new byte[len];
        in.readByteArray(result);
        return result;
    }

    static void writeBytesWithNull(Parcel out, byte[] b) {
        if (b == null) {
            out.writeInt(-1);
            return;
        }
        out.writeInt(b.length);
        out.writeByteArray(b);
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel dest, int flags) {
        int isMock = this.mTagService == null ? 1 : 0;
        writeBytesWithNull(dest, this.mId);
        dest.writeInt(this.mTechList.length);
        dest.writeIntArray(this.mTechList);
        dest.writeTypedArray(this.mTechExtras, 0);
        dest.writeInt(this.mServiceHandle);
        dest.writeInt(isMock);
        if (isMock == 0) {
            dest.writeStrongBinder(this.mTagService.asBinder());
        }
    }

    public synchronized void setConnectedTechnology(int technology) {
        if (this.mConnectedTechnology == -1) {
            this.mConnectedTechnology = technology;
        } else {
            throw new IllegalStateException("Close other technology first!");
        }
    }

    public int getConnectedTechnology() {
        return this.mConnectedTechnology;
    }

    public void setTechnologyDisconnected() {
        this.mConnectedTechnology = -1;
    }
}
