package ohos.nfc.tag;

import java.util.Arrays;
import java.util.Optional;
import ohos.aafwk.content.IntentParams;
import ohos.utils.Parcel;
import ohos.utils.Sequenceable;

public class TagInfo implements Sequenceable {
    public static final String EXTRA_TAG_EXTRAS = "extra_nfc_TAG_EXTRAS";
    public static final String EXTRA_TAG_HANDLE = "extra_nfc_TAG_HANDLE";
    private IntentParams[] mProfileExtras = null;
    private int mTagHandle;
    private byte[] mTagId = null;
    private int[] mTagSupportedProfiles = null;

    public TagInfo(byte[] bArr, int[] iArr, IntentParams[] intentParamsArr, int i) {
        if (bArr != null) {
            this.mTagId = Arrays.copyOf(bArr, bArr.length);
        }
        if (iArr != null) {
            this.mTagSupportedProfiles = Arrays.copyOf(iArr, iArr.length);
        }
        if (intentParamsArr != null) {
            this.mProfileExtras = (IntentParams[]) Arrays.copyOf(intentParamsArr, intentParamsArr.length);
        }
        this.mTagHandle = i;
    }

    @Override // ohos.utils.Sequenceable
    public boolean marshalling(Parcel parcel) {
        parcel.writeByteArray(this.mTagId);
        parcel.writeIntArray(this.mTagSupportedProfiles);
        parcel.writeSequenceableArray(this.mProfileExtras);
        parcel.writeInt(this.mTagHandle);
        return true;
    }

    @Override // ohos.utils.Sequenceable
    public boolean unmarshalling(Parcel parcel) {
        this.mTagId = parcel.readByteArray();
        this.mTagSupportedProfiles = parcel.readIntArray();
        parcel.readSequenceableArray(this.mProfileExtras);
        this.mTagHandle = parcel.readInt();
        return true;
    }

    public byte[] getTagId() {
        byte[] bArr = this.mTagId;
        return bArr != null ? Arrays.copyOf(bArr, bArr.length) : new byte[0];
    }

    public int[] getTagSupportedProfiles() {
        int[] iArr = this.mTagSupportedProfiles;
        return iArr != null ? Arrays.copyOf(iArr, iArr.length) : new int[0];
    }

    public int getTagHandle() {
        return this.mTagHandle;
    }

    /* JADX WARNING: Removed duplicated region for block: B:10:0x0015  */
    /* JADX WARNING: Removed duplicated region for block: B:12:0x001a  */
    public Optional<IntentParams> getProfileExtras(int i) {
        int i2;
        if (this.mTagSupportedProfiles != null) {
            i2 = 0;
            while (true) {
                int[] iArr = this.mTagSupportedProfiles;
                if (i2 >= iArr.length) {
                    break;
                } else if (iArr[i2] == i) {
                    break;
                } else {
                    i2++;
                }
            }
            if (i2 >= 0) {
                return Optional.empty();
            }
            return Optional.of(this.mProfileExtras[i2]);
        }
        i2 = -1;
        if (i2 >= 0) {
        }
    }

    public boolean isProfileSupported(int i) {
        int[] iArr = this.mTagSupportedProfiles;
        if (iArr != null) {
            for (int i2 : iArr) {
                if (i2 == i) {
                    return true;
                }
            }
        }
        return false;
    }
}
