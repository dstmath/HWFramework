package android.icu.impl;

import android.icu.impl.ICUBinary;
import android.icu.util.BytesTrie;
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

    private static final class IsAcceptable implements ICUBinary.Authenticate {
        private IsAcceptable() {
        }

        public boolean isDataVersionAcceptable(byte[] version) {
            return version[0] == 2;
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
        if (indexesLength >= 8) {
            int[] inIndexes = new int[indexesLength];
            inIndexes[0] = indexesLength * 4;
            for (int i = 1; i < indexesLength; i++) {
                inIndexes[i] = bytes.getInt();
            }
            int i2 = inIndexes[0];
            int nextOffset = inIndexes[1];
            this.valueMaps = ICUBinary.getInts(bytes, (nextOffset - i2) / 4, 0);
            int offset = nextOffset;
            int nextOffset2 = inIndexes[2];
            this.bytesTries = new byte[(nextOffset2 - offset)];
            bytes.get(this.bytesTries);
            int numBytes = inIndexes[3] - nextOffset2;
            StringBuilder sb = new StringBuilder(numBytes);
            for (int i3 = 0; i3 < numBytes; i3++) {
                sb.append((char) bytes.get());
            }
            this.nameGroups = sb.toString();
            return;
        }
        throw new IOException("pnames.icu: not enough indexes");
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
            int i2 = i + 2;
            if (property < start) {
                break;
            } else if (property < limit) {
                return ((property - start) * 2) + i2;
            } else {
                i = i2 + ((limit - start) * 2);
                numRanges--;
            }
        }
        return 0;
    }

    private int findPropertyValueNameGroup(int valueMapIndex, int value) {
        if (valueMapIndex == 0) {
            return 0;
        }
        int valueMapIndex2 = valueMapIndex + 1;
        int valueMapIndex3 = valueMapIndex2 + 1;
        int numRanges = this.valueMaps[valueMapIndex2];
        if (numRanges >= 16) {
            int valuesStart = valueMapIndex3;
            int nameGroupOffsetsStart = (valueMapIndex3 + numRanges) - 16;
            do {
                int v = this.valueMaps[valueMapIndex3];
                if (value < v) {
                    break;
                } else if (value == v) {
                    return this.valueMaps[(nameGroupOffsetsStart + valueMapIndex3) - valuesStart];
                } else {
                    valueMapIndex3++;
                }
            } while (valueMapIndex3 < nameGroupOffsetsStart);
        } else {
            while (numRanges > 0) {
                int start = this.valueMaps[valueMapIndex3];
                int limit = this.valueMaps[valueMapIndex3 + 1];
                int valueMapIndex4 = valueMapIndex3 + 2;
                if (value < start) {
                    break;
                } else if (value < limit) {
                    return this.valueMaps[(valueMapIndex4 + value) - start];
                } else {
                    valueMapIndex3 = valueMapIndex4 + (limit - start);
                    numRanges--;
                }
            }
        }
        return 0;
    }

    private String getName(int nameGroupsIndex, int nameIndex) {
        int nameGroupsIndex2;
        int nameGroupsIndex3 = nameGroupsIndex + 1;
        int numNames = this.nameGroups.charAt(nameGroupsIndex);
        if (nameIndex < 0 || numNames <= nameIndex) {
            throw new IllegalIcuArgumentException("Invalid property (value) name choice");
        }
        while (nameIndex > 0) {
            while (true) {
                nameGroupsIndex2 = nameGroupsIndex3 + 1;
                if (this.nameGroups.charAt(nameGroupsIndex3) == 0) {
                    break;
                }
                nameGroupsIndex3 = nameGroupsIndex2;
            }
            nameIndex--;
            nameGroupsIndex3 = nameGroupsIndex2;
        }
        int nameGroupsIndex4 = nameGroupsIndex3;
        while (this.nameGroups.charAt(nameGroupsIndex4) != 0) {
            nameGroupsIndex4++;
        }
        if (nameGroupsIndex3 == nameGroupsIndex4) {
            return null;
        }
        return this.nameGroups.substring(nameGroupsIndex3, nameGroupsIndex4);
    }

    private static int asciiToLowercase(int c) {
        return (65 > c || c > 90) ? c : c + 32;
    }

    private boolean containsName(BytesTrie trie, CharSequence name) {
        BytesTrie.Result result = BytesTrie.Result.NO_VALUE;
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
        if (valueMapIndex != 0) {
            int nameGroupOffset = findPropertyValueNameGroup(this.valueMaps[valueMapIndex + 1], value);
            if (nameGroupOffset != 0) {
                return getName(nameGroupOffset, nameChoice);
            }
            throw new IllegalArgumentException("Property " + property + " (0x" + Integer.toHexString(property) + ") does not have named values");
        }
        throw new IllegalArgumentException("Invalid property enum " + property + " (0x" + Integer.toHexString(property) + ")");
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
        if (valueMapIndex != 0) {
            int valueMapIndex2 = this.valueMaps[valueMapIndex + 1];
            if (valueMapIndex2 != 0) {
                return getPropertyOrValueEnum(this.valueMaps[valueMapIndex2], alias);
            }
            throw new IllegalArgumentException("Property " + property + " (0x" + Integer.toHexString(property) + ") does not have named values");
        }
        throw new IllegalArgumentException("Invalid property enum " + property + " (0x" + Integer.toHexString(property) + ")");
    }

    public int getPropertyValueEnumNoThrow(int property, CharSequence alias) {
        int valueMapIndex = findProperty(property);
        if (valueMapIndex == 0) {
            return -1;
        }
        int valueMapIndex2 = this.valueMaps[valueMapIndex + 1];
        if (valueMapIndex2 == 0) {
            return -1;
        }
        return getPropertyOrValueEnum(this.valueMaps[valueMapIndex2], alias);
    }

    public static int compare(String stra, String strb) {
        int cstra = 0;
        int istrb = 0;
        int istra = 0;
        int istra2 = 0;
        while (true) {
            if (istra < stra.length()) {
                cstra = stra.charAt(istra);
                if (!(cstra == 32 || cstra == 45 || cstra == 95)) {
                    switch (cstra) {
                        case 9:
                        case 10:
                        case 11:
                        case 12:
                        case 13:
                            break;
                    }
                }
                istra++;
            }
            while (istrb < strb.length()) {
                istra2 = strb.charAt(istrb);
                if (!(istra2 == 32 || istra2 == 45 || istra2 == 95)) {
                    switch (istra2) {
                        case 9:
                        case 10:
                        case 11:
                        case 12:
                        case 13:
                            break;
                    }
                }
                istrb++;
            }
            boolean endstrb = true;
            boolean endstra = istra == stra.length();
            if (istrb != strb.length()) {
                endstrb = false;
            }
            if (endstra) {
                if (endstrb) {
                    return 0;
                }
                cstra = 0;
            } else if (endstrb) {
                istra2 = 0;
            }
            int rc = asciiToLowercase(cstra) - asciiToLowercase(istra2);
            if (rc != 0) {
                return rc;
            }
            istra++;
            istrb++;
        }
    }
}
