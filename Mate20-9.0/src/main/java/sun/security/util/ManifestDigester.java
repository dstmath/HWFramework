package sun.security.util;

import java.io.ByteArrayOutputStream;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.util.HashMap;

public class ManifestDigester {
    public static final String MF_MAIN_ATTRS = "Manifest-Main-Attributes";
    private HashMap<String, Entry> entries = new HashMap<>();
    private byte[] rawBytes;

    public static class Entry {
        int length;
        int lengthWithBlankLine;
        int offset;
        boolean oldStyle;
        byte[] rawBytes;

        public Entry(int offset2, int length2, int lengthWithBlankLine2, byte[] rawBytes2) {
            this.offset = offset2;
            this.length = length2;
            this.lengthWithBlankLine = lengthWithBlankLine2;
            this.rawBytes = rawBytes2;
        }

        public byte[] digest(MessageDigest md) {
            md.reset();
            if (this.oldStyle) {
                doOldStyle(md, this.rawBytes, this.offset, this.lengthWithBlankLine);
            } else {
                md.update(this.rawBytes, this.offset, this.lengthWithBlankLine);
            }
            return md.digest();
        }

        /* JADX WARNING: Incorrect type for immutable var: ssa=byte, code=int, for r3v2, types: [byte] */
        private void doOldStyle(MessageDigest md, byte[] bytes, int offset2, int length2) {
            int i = offset2;
            int start = offset2;
            int max = offset2 + length2;
            int prev = -1;
            while (i < max) {
                if (bytes[i] == 13 && prev == 32) {
                    md.update(bytes, start, (i - start) - 1);
                    start = i;
                }
                prev = bytes[i];
                i++;
            }
            md.update(bytes, start, i - start);
        }

        public byte[] digestWorkaround(MessageDigest md) {
            md.reset();
            md.update(this.rawBytes, this.offset, this.length);
            return md.digest();
        }
    }

    static class Position {
        int endOfFirstLine;
        int endOfSection;
        int startOfNext;

        Position() {
        }
    }

    private boolean findSection(int offset, Position pos) {
        int i = offset;
        int len = this.rawBytes.length;
        int last = offset;
        boolean allBlank = true;
        pos.endOfFirstLine = -1;
        while (i < len) {
            byte b = this.rawBytes[i];
            if (b != 10) {
                if (b != 13) {
                    allBlank = false;
                    i++;
                } else {
                    if (pos.endOfFirstLine == -1) {
                        pos.endOfFirstLine = i - 1;
                    }
                    if (i < len && this.rawBytes[i + 1] == 10) {
                        i++;
                    }
                }
            }
            if (pos.endOfFirstLine == -1) {
                pos.endOfFirstLine = i - 1;
            }
            if (allBlank || i == len - 1) {
                if (i == len - 1) {
                    pos.endOfSection = i;
                } else {
                    pos.endOfSection = last;
                }
                pos.startOfNext = i + 1;
                return true;
            }
            last = i;
            allBlank = true;
            i++;
        }
        return false;
    }

    public ManifestDigester(byte[] bytes) {
        int i;
        int wrapLen;
        this.rawBytes = bytes;
        new ByteArrayOutputStream();
        Position pos = new Position();
        if (findSection(0, pos)) {
            this.entries.put(MF_MAIN_ATTRS, new Entry(0, pos.endOfSection + 1, pos.startOfNext, this.rawBytes));
            for (int start = pos.startOfNext; findSection(start, pos); start = pos.startOfNext) {
                int len = (pos.endOfFirstLine - start) + 1;
                int sectionLen = (pos.endOfSection - start) + 1;
                int sectionLenWithBlank = pos.startOfNext - start;
                if (len > 6 && isNameAttr(bytes, start)) {
                    StringBuilder nameBuf = new StringBuilder(sectionLen);
                    try {
                        nameBuf.append(new String(bytes, start + 6, len - 6, "UTF8"));
                        int i2 = start + len;
                        if (i2 - start < sectionLen) {
                            if (bytes[i2] == 13) {
                                i2 += 2;
                            } else {
                                i2++;
                            }
                        }
                        while (true) {
                            if (i - start >= sectionLen) {
                                break;
                            }
                            int wrapStart = i + 1;
                            if (bytes[i] != 32) {
                                int i3 = wrapStart;
                                break;
                            }
                            i = wrapStart;
                            while (true) {
                                if (i - start >= sectionLen) {
                                    break;
                                }
                                int i4 = i + 1;
                                if (bytes[i] == 10) {
                                    i = i4;
                                    break;
                                }
                                i = i4;
                            }
                            if (bytes[i - 1] == 10) {
                                if (bytes[i - 2] == 13) {
                                    wrapLen = (i - wrapStart) - 2;
                                } else {
                                    wrapLen = (i - wrapStart) - 1;
                                }
                                nameBuf.append(new String(bytes, wrapStart, wrapLen, "UTF8"));
                            } else {
                                return;
                            }
                        }
                        this.entries.put(nameBuf.toString(), new Entry(start, sectionLen, sectionLenWithBlank, this.rawBytes));
                    } catch (UnsupportedEncodingException e) {
                        throw new IllegalStateException("UTF8 not available on platform");
                    }
                }
            }
        }
    }

    private boolean isNameAttr(byte[] bytes, int start) {
        return (bytes[start] == 78 || bytes[start] == 110) && (bytes[start + 1] == 97 || bytes[start + 1] == 65) && ((bytes[start + 2] == 109 || bytes[start + 2] == 77) && ((bytes[start + 3] == 101 || bytes[start + 3] == 69) && bytes[start + 4] == 58 && bytes[start + 5] == 32));
    }

    public Entry get(String name, boolean oldStyle) {
        Entry e = this.entries.get(name);
        if (e != null) {
            e.oldStyle = oldStyle;
        }
        return e;
    }

    public byte[] manifestDigest(MessageDigest md) {
        md.reset();
        md.update(this.rawBytes, 0, this.rawBytes.length);
        return md.digest();
    }
}
