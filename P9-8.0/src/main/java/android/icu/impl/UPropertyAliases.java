package android.icu.impl;

import android.icu.impl.ICUBinary.Authenticate;
import android.icu.util.BytesTrie;
import android.icu.util.BytesTrie.Result;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.MissingResourceException;

public final class UPropertyAliases {
    private static final int DATA_FORMAT = 1886282093;
    public static final UPropertyAliases INSTANCE;
    private static final IsAcceptable IS_ACCEPTABLE = new IsAcceptable();
    private static final int IX_BYTE_TRIES_OFFSET = 1;
    private static final int IX_NAME_GROUPS_OFFSET = 2;
    private static final int IX_RESERVED3_OFFSET = 3;
    private static final int IX_VALUE_MAPS_OFFSET = 0;
    private byte[] bytesTries;
    private String nameGroups;
    private int[] valueMaps;

    private static final class IsAcceptable implements Authenticate {
        /* synthetic */ IsAcceptable(IsAcceptable -this0) {
            this();
        }

        private IsAcceptable() {
        }

        public boolean isDataVersionAcceptable(byte[] version) {
            return version[0] == (byte) 2;
        }
    }

    static {
        try {
            INSTANCE = new UPropertyAliases();
        } catch (IOException e) {
            MissingResourceException mre = new MissingResourceException("Could not construct UPropertyAliases. Missing pnames.icu", "", "");
            mre.initCause(e);
            throw mre;
        }
    }

    private void load(ByteBuffer bytes) throws IOException {
        ICUBinary.readHeader(bytes, DATA_FORMAT, IS_ACCEPTABLE);
        int indexesLength = bytes.getInt() / 4;
        if (indexesLength < 8) {
            throw new IOException("pnames.icu: not enough indexes");
        }
        int i;
        int[] inIndexes = new int[indexesLength];
        inIndexes[0] = indexesLength * 4;
        for (i = 1; i < indexesLength; i++) {
            inIndexes[i] = bytes.getInt();
        }
        int offset = inIndexes[0];
        int nextOffset = inIndexes[1];
        this.valueMaps = ICUBinary.getInts(bytes, (nextOffset - offset) / 4, 0);
        offset = nextOffset;
        nextOffset = inIndexes[2];
        this.bytesTries = new byte[(nextOffset - offset)];
        bytes.get(this.bytesTries);
        int numBytes = inIndexes[3] - nextOffset;
        StringBuilder sb = new StringBuilder(numBytes);
        for (i = 0; i < numBytes; i++) {
            sb.append((char) bytes.get());
        }
        this.nameGroups = sb.toString();
    }

    private UPropertyAliases() throws IOException {
        load(ICUBinary.getRequiredData("pnames.icu"));
    }

    private int findProperty(int property) {
        int i = 1;
        int numRanges = this.valueMaps[0];
        while (numRanges > 0) {
            int start = this.valueMaps[i];
            int limit = this.valueMaps[i + 1];
            i += 2;
            if (property < start) {
                break;
            } else if (property < limit) {
                return ((property - start) * 2) + i;
            } else {
                i += (limit - start) * 2;
                numRanges--;
            }
        }
        return 0;
    }

    private int findPropertyValueNameGroup(int valueMapIndex, int value) {
        if (valueMapIndex == 0) {
            return 0;
        }
        valueMapIndex++;
        int i = valueMapIndex + 1;
        int numRanges = this.valueMaps[valueMapIndex];
        if (numRanges >= 16) {
            int valuesStart = i;
            int nameGroupOffsetsStart = (i + numRanges) - 16;
            valueMapIndex = i;
            while (true) {
                int v = this.valueMaps[valueMapIndex];
                if (value >= v) {
                    if (value != v) {
                        valueMapIndex++;
                        if (valueMapIndex >= nameGroupOffsetsStart) {
                            break;
                        }
                    } else {
                        return this.valueMaps[(nameGroupOffsetsStart + valueMapIndex) - i];
                    }
                }
                break;
            }
        }
        valueMapIndex = i;
        while (numRanges > 0) {
            int start = this.valueMaps[valueMapIndex];
            int limit = this.valueMaps[valueMapIndex + 1];
            valueMapIndex += 2;
            if (value < start) {
                break;
            } else if (value < limit) {
                return this.valueMaps[(valueMapIndex + value) - start];
            } else {
                valueMapIndex += limit - start;
                numRanges--;
            }
        }
        return 0;
    }

    private String getName(int nameGroupsIndex, int nameIndex) {
        int nameGroupsIndex2 = nameGroupsIndex + 1;
        int numNames = this.nameGroups.charAt(nameGroupsIndex);
        if (nameIndex < 0 || numNames <= nameIndex) {
            throw new IllegalIcuArgumentException("Invalid property (value) name choice");
        }
        nameGroupsIndex = nameGroupsIndex2;
        while (nameIndex > 0) {
            while (true) {
                nameGroupsIndex2 = nameGroupsIndex + 1;
                if (this.nameGroups.charAt(nameGroupsIndex) == 0) {
                    break;
                }
                nameGroupsIndex = nameGroupsIndex2;
            }
            nameIndex--;
            nameGroupsIndex = nameGroupsIndex2;
        }
        int nameStart = nameGroupsIndex;
        while (this.nameGroups.charAt(nameGroupsIndex) != 0) {
            nameGroupsIndex++;
        }
        if (nameStart == nameGroupsIndex) {
            return null;
        }
        return this.nameGroups.substring(nameStart, nameGroupsIndex);
    }

    private static int asciiToLowercase(int c) {
        return (65 > c || c > 90) ? c : c + 32;
    }

    private boolean containsName(BytesTrie trie, CharSequence name) {
        Result result = Result.NO_VALUE;
        for (int i = 0; i < name.length(); i++) {
            int c = name.charAt(i);
            if (!(c == 45 || c == 95 || c == 32 || (9 <= c && c <= 13))) {
                if (!result.hasNext()) {
                    return false;
                }
                result = trie.next(asciiToLowercase(c));
            }
        }
        return result.hasValue();
    }

    public String getPropertyName(int property, int nameChoice) {
        int valueMapIndex = findProperty(property);
        if (valueMapIndex != 0) {
            return getName(this.valueMaps[valueMapIndex], nameChoice);
        }
        throw new IllegalArgumentException("Invalid property enum " + property + " (0x" + Integer.toHexString(property) + ")");
    }

    public String getPropertyValueName(int property, int value, int nameChoice) {
        int valueMapIndex = findProperty(property);
        if (valueMapIndex == 0) {
            throw new IllegalArgumentException("Invalid property enum " + property + " (0x" + Integer.toHexString(property) + ")");
        }
        int nameGroupOffset = findPropertyValueNameGroup(this.valueMaps[valueMapIndex + 1], value);
        if (nameGroupOffset != 0) {
            return getName(nameGroupOffset, nameChoice);
        }
        throw new IllegalArgumentException("Property " + property + " (0x" + Integer.toHexString(property) + ") does not have named values");
    }

    private int getPropertyOrValueEnum(int bytesTrieOffset, CharSequence alias) {
        BytesTrie trie = new BytesTrie(this.bytesTries, bytesTrieOffset);
        if (containsName(trie, alias)) {
            return trie.getValue();
        }
        return -1;
    }

    public int getPropertyEnum(CharSequence alias) {
        return getPropertyOrValueEnum(0, alias);
    }

    public int getPropertyValueEnum(int property, CharSequence alias) {
        int valueMapIndex = findProperty(property);
        if (valueMapIndex == 0) {
            throw new IllegalArgumentException("Invalid property enum " + property + " (0x" + Integer.toHexString(property) + ")");
        }
        valueMapIndex = this.valueMaps[valueMapIndex + 1];
        if (valueMapIndex != 0) {
            return getPropertyOrValueEnum(this.valueMaps[valueMapIndex], alias);
        }
        throw new IllegalArgumentException("Property " + property + " (0x" + Integer.toHexString(property) + ") does not have named values");
    }

    public int getPropertyValueEnumNoThrow(int property, CharSequence alias) {
        int valueMapIndex = findProperty(property);
        if (valueMapIndex == 0) {
            return -1;
        }
        valueMapIndex = this.valueMaps[valueMapIndex + 1];
        if (valueMapIndex == 0) {
            return -1;
        }
        return getPropertyOrValueEnum(this.valueMaps[valueMapIndex], alias);
    }

    /* JADX WARNING: Removed duplicated region for block: B:20:0x0038  */
    /* JADX WARNING: Removed duplicated region for block: B:11:0x0025  */
    /* JADX WARNING: Removed duplicated region for block: B:21:0x003a  */
    /* JADX WARNING: Removed duplicated region for block: B:14:0x002c  */
    /* JADX WARNING: Removed duplicated region for block: B:26:0x004a  */
    /* JADX WARNING: Removed duplicated region for block: B:16:0x002f  */
    /* JADX WARNING: Removed duplicated region for block: B:28:0x004e  */
    /* JADX WARNING: Removed duplicated region for block: B:30:0x0049 A:{SYNTHETIC} */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public static int compare(String stra, String strb) {
        int istra = 0;
        int istrb = 0;
        int cstra = 0;
        int cstrb = 0;
        while (true) {
            boolean endstra;
            boolean endstrb;
            int rc;
            if (istra < stra.length()) {
                cstra = stra.charAt(istra);
                switch (cstra) {
                    case 9:
                    case 10:
                    case 11:
                    case 12:
                    case 13:
                    case 32:
                    case 45:
                    case 95:
                        istra++;
                        break;
                }
            }
            while (istrb < strb.length()) {
                cstrb = strb.charAt(istrb);
                switch (cstrb) {
                    case 9:
                    case 10:
                    case 11:
                    case 12:
                    case 13:
                    case 32:
                    case 45:
                    case 95:
                        istrb++;
                    default:
                        break;
                }
                endstra = istra != stra.length();
                endstrb = istrb != strb.length();
                if (endstra) {
                    if (endstrb) {
                        cstrb = 0;
                    }
                } else if (endstrb) {
                    return 0;
                } else {
                    cstra = 0;
                }
                rc = asciiToLowercase(cstra) - asciiToLowercase(cstrb);
                if (rc == 0) {
                    return rc;
                }
                istra++;
                istrb++;
            }
            if (istra != stra.length()) {
            }
            if (istrb != strb.length()) {
            }
            if (endstra) {
            }
            rc = asciiToLowercase(cstra) - asciiToLowercase(cstrb);
            if (rc == 0) {
            }
        }
    }
}
