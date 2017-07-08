package android.icu.impl;

import android.icu.impl.ICUBinary.Authenticate;
import android.icu.util.BytesTrie;
import android.icu.util.BytesTrie.Result;
import dalvik.bytecode.Opcodes;
import java.io.IOException;
import java.nio.ByteBuffer;
import libcore.icu.ICU;
import org.w3c.dom.traversal.NodeFilter;
import org.xmlpull.v1.XmlPullParser;

public final class UPropertyAliases {
    private static final int DATA_FORMAT = 1886282093;
    public static final UPropertyAliases INSTANCE = null;
    private static final IsAcceptable IS_ACCEPTABLE = null;
    private static final int IX_BYTE_TRIES_OFFSET = 1;
    private static final int IX_NAME_GROUPS_OFFSET = 2;
    private static final int IX_RESERVED3_OFFSET = 3;
    private static final int IX_VALUE_MAPS_OFFSET = 0;
    private byte[] bytesTries;
    private String nameGroups;
    private int[] valueMaps;

    private static final class IsAcceptable implements Authenticate {
        private IsAcceptable() {
        }

        public boolean isDataVersionAcceptable(byte[] version) {
            return version[0] == UPropertyAliases.IX_NAME_GROUPS_OFFSET;
        }
    }

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.icu.impl.UPropertyAliases.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.icu.impl.UPropertyAliases.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 5 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 6 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: android.icu.impl.UPropertyAliases.<clinit>():void");
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
        for (i = IX_BYTE_TRIES_OFFSET; i < indexesLength; i += IX_BYTE_TRIES_OFFSET) {
            inIndexes[i] = bytes.getInt();
        }
        int offset = inIndexes[0];
        int nextOffset = inIndexes[IX_BYTE_TRIES_OFFSET];
        this.valueMaps = ICUBinary.getInts(bytes, (nextOffset - offset) / 4, 0);
        offset = nextOffset;
        nextOffset = inIndexes[IX_NAME_GROUPS_OFFSET];
        this.bytesTries = new byte[(nextOffset - offset)];
        bytes.get(this.bytesTries);
        int numBytes = inIndexes[IX_RESERVED3_OFFSET] - nextOffset;
        StringBuilder sb = new StringBuilder(numBytes);
        for (i = 0; i < numBytes; i += IX_BYTE_TRIES_OFFSET) {
            sb.append((char) bytes.get());
        }
        this.nameGroups = sb.toString();
    }

    private UPropertyAliases() throws IOException {
        load(ICUBinary.getRequiredData("pnames.icu"));
    }

    private int findProperty(int property) {
        int i = IX_BYTE_TRIES_OFFSET;
        int numRanges = this.valueMaps[0];
        while (numRanges > 0) {
            int start = this.valueMaps[i];
            int limit = this.valueMaps[i + IX_BYTE_TRIES_OFFSET];
            i += IX_NAME_GROUPS_OFFSET;
            if (property < start) {
                break;
            } else if (property < limit) {
                return ((property - start) * IX_NAME_GROUPS_OFFSET) + i;
            } else {
                i += (limit - start) * IX_NAME_GROUPS_OFFSET;
                numRanges--;
            }
        }
        return 0;
    }

    private int findPropertyValueNameGroup(int valueMapIndex, int value) {
        if (valueMapIndex == 0) {
            return 0;
        }
        valueMapIndex += IX_BYTE_TRIES_OFFSET;
        int i = valueMapIndex + IX_BYTE_TRIES_OFFSET;
        int numRanges = this.valueMaps[valueMapIndex];
        if (numRanges >= 16) {
            int valuesStart = i;
            int nameGroupOffsetsStart = (i + numRanges) - 16;
            valueMapIndex = i;
            while (true) {
                int v = this.valueMaps[valueMapIndex];
                if (value >= v) {
                    if (value != v) {
                        valueMapIndex += IX_BYTE_TRIES_OFFSET;
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
            int limit = this.valueMaps[valueMapIndex + IX_BYTE_TRIES_OFFSET];
            valueMapIndex += IX_NAME_GROUPS_OFFSET;
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
        int nameGroupsIndex2 = nameGroupsIndex + IX_BYTE_TRIES_OFFSET;
        int numNames = this.nameGroups.charAt(nameGroupsIndex);
        if (nameIndex < 0 || numNames <= nameIndex) {
            throw new IllegalIcuArgumentException("Invalid property (value) name choice");
        }
        nameGroupsIndex = nameGroupsIndex2;
        while (nameIndex > 0) {
            while (true) {
                nameGroupsIndex2 = nameGroupsIndex + IX_BYTE_TRIES_OFFSET;
                if (this.nameGroups.charAt(nameGroupsIndex) == '\u0000') {
                    break;
                }
                nameGroupsIndex = nameGroupsIndex2;
            }
            nameIndex--;
            nameGroupsIndex = nameGroupsIndex2;
        }
        int nameStart = nameGroupsIndex;
        while (this.nameGroups.charAt(nameGroupsIndex) != '\u0000') {
            nameGroupsIndex += IX_BYTE_TRIES_OFFSET;
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
        for (int i = 0; i < name.length(); i += IX_BYTE_TRIES_OFFSET) {
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
        int nameGroupOffset = findPropertyValueNameGroup(this.valueMaps[valueMapIndex + IX_BYTE_TRIES_OFFSET], value);
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
        valueMapIndex = this.valueMaps[valueMapIndex + IX_BYTE_TRIES_OFFSET];
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
        valueMapIndex = this.valueMaps[valueMapIndex + IX_BYTE_TRIES_OFFSET];
        if (valueMapIndex == 0) {
            return -1;
        }
        return getPropertyOrValueEnum(this.valueMaps[valueMapIndex], alias);
    }

    public static int compare(String stra, String strb) {
        int istra = 0;
        int istrb = 0;
        int cstra = 0;
        int cstrb = 0;
        while (true) {
            int rc;
            if (istra < stra.length()) {
                cstra = stra.charAt(istra);
                switch (cstra) {
                    case XmlPullParser.COMMENT /*9*/:
                    case XmlPullParser.DOCDECL /*10*/:
                    case ICU.U_TRUNCATED_CHAR_FOUND /*11*/:
                    case ICU.U_ILLEGAL_CHAR_FOUND /*12*/:
                    case Opcodes.OP_MOVE_EXCEPTION /*13*/:
                    case NodeFilter.SHOW_ENTITY /*32*/:
                    case Opcodes.OP_CMPL_FLOAT /*45*/:
                    case Opcodes.OP_IPUT_SHORT /*95*/:
                        istra += IX_BYTE_TRIES_OFFSET;
                        break;
                }
            }
            while (istrb < strb.length()) {
                cstrb = strb.charAt(istrb);
                switch (cstrb) {
                    case XmlPullParser.COMMENT /*9*/:
                    case XmlPullParser.DOCDECL /*10*/:
                    case ICU.U_TRUNCATED_CHAR_FOUND /*11*/:
                    case ICU.U_ILLEGAL_CHAR_FOUND /*12*/:
                    case Opcodes.OP_MOVE_EXCEPTION /*13*/:
                    case NodeFilter.SHOW_ENTITY /*32*/:
                    case Opcodes.OP_CMPL_FLOAT /*45*/:
                    case Opcodes.OP_IPUT_SHORT /*95*/:
                        istrb += IX_BYTE_TRIES_OFFSET;
                    default:
                        break;
                }
                boolean endstra = istra != stra.length();
                boolean endstrb = istrb != strb.length();
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
                if (rc != 0) {
                    return rc;
                }
                istra += IX_BYTE_TRIES_OFFSET;
                istrb += IX_BYTE_TRIES_OFFSET;
            }
            if (istra != stra.length()) {
            }
            if (istrb != strb.length()) {
            }
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
            if (rc != 0) {
                return rc;
            }
            istra += IX_BYTE_TRIES_OFFSET;
            istrb += IX_BYTE_TRIES_OFFSET;
        }
    }
}
