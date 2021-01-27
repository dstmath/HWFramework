package android.nfc;

import android.annotation.UnsupportedAppUsage;
import android.nfc.INfcTag;
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
import android.os.RemoteException;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;

public final class Tag implements Parcelable {
    public static final Parcelable.Creator<Tag> CREATOR = new Parcelable.Creator<Tag>() {
        /* class android.nfc.Tag.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public Tag createFromParcel(Parcel in) {
            INfcTag tagService;
            byte[] id = Tag.readBytesWithNull(in);
            int[] techList = new int[in.readInt()];
            in.readIntArray(techList);
            Bundle[] techExtras = (Bundle[]) in.createTypedArray(Bundle.CREATOR);
            int serviceHandle = in.readInt();
            if (in.readInt() == 0) {
                tagService = INfcTag.Stub.asInterface(in.readStrongBinder());
            } else {
                tagService = null;
            }
            return new Tag(id, techList, techExtras, serviceHandle, tagService);
        }

        @Override // android.os.Parcelable.Creator
        public Tag[] newArray(int size) {
            return new Tag[size];
        }
    };
    int mConnectedTechnology;
    @UnsupportedAppUsage
    final byte[] mId;
    final int mServiceHandle;
    final INfcTag mTagService;
    final Bundle[] mTechExtras;
    final int[] mTechList;
    final String[] mTechStringList;

    public Tag(byte[] id, int[] techList, Bundle[] techListExtras, int serviceHandle, INfcTag tagService) {
        if (techList != null) {
            this.mId = id;
            this.mTechList = Arrays.copyOf(techList, techList.length);
            this.mTechStringList = generateTechStringList(techList);
            this.mTechExtras = (Bundle[]) Arrays.copyOf(techListExtras, techList.length);
            this.mServiceHandle = serviceHandle;
            this.mTagService = tagService;
            this.mConnectedTechnology = -1;
            return;
        }
        throw new IllegalArgumentException("rawTargets cannot be null");
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
        if (techStringList != null) {
            int[] techIntList = new int[techStringList.length];
            HashMap<String, Integer> stringToCodeMap = getTechStringToCodeMap();
            for (int i = 0; i < techStringList.length; i++) {
                Integer code = stringToCodeMap.get(techStringList[i]);
                if (code != null) {
                    techIntList[i] = code.intValue();
                } else {
                    throw new IllegalArgumentException("Unknown tech type " + techStringList[i]);
                }
            }
            return techIntList;
        }
        throw new IllegalArgumentException("List cannot be null");
    }

    private static HashMap<String, Integer> getTechStringToCodeMap() {
        HashMap<String, Integer> techStringToCodeMap = new HashMap<>();
        techStringToCodeMap.put(IsoDep.class.getName(), 3);
        techStringToCodeMap.put(MifareClassic.class.getName(), 8);
        techStringToCodeMap.put(MifareUltralight.class.getName(), 9);
        techStringToCodeMap.put(Ndef.class.getName(), 6);
        techStringToCodeMap.put(NdefFormatable.class.getName(), 7);
        techStringToCodeMap.put(NfcA.class.getName(), 1);
        techStringToCodeMap.put(NfcB.class.getName(), 2);
        techStringToCodeMap.put(NfcF.class.getName(), 4);
        techStringToCodeMap.put(NfcV.class.getName(), 5);
        techStringToCodeMap.put(NfcBarcode.class.getName(), 10);
        return techStringToCodeMap;
    }

    @UnsupportedAppUsage
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
        if (getConnectedTechnology() == -1) {
            INfcTag iNfcTag = this.mTagService;
            if (iNfcTag != null) {
                try {
                    Tag newTag = iNfcTag.rediscover(getServiceHandle());
                    if (newTag != null) {
                        return newTag;
                    }
                    throw new IOException("Failed to rediscover tag");
                } catch (RemoteException e) {
                    throw new IOException("NFC service dead");
                }
            } else {
                throw new IOException("Mock tags don't support this operation.");
            }
        } else {
            throw new IllegalStateException("Close connection to the technology first!");
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
        int idx = 0;
        while (true) {
            int[] iArr = this.mTechList;
            if (idx >= iArr.length) {
                break;
            } else if (iArr[idx] == tech) {
                pos = idx;
                break;
            } else {
                idx++;
            }
        }
        if (pos < 0) {
            return null;
        }
        return this.mTechExtras[pos];
    }

    @UnsupportedAppUsage
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

    @Override // android.os.Parcelable
    public int describeContents() {
        return 0;
    }

    @Override // android.os.Parcelable
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
