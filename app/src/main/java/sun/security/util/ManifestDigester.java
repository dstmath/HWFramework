package sun.security.util;

import java.security.MessageDigest;
import java.util.Calendar;
import java.util.HashMap;
import sun.util.calendar.BaseCalendar;

public class ManifestDigester {
    public static final String MF_MAIN_ATTRS = "Manifest-Main-Attributes";
    private HashMap<String, Entry> entries;
    private byte[] rawBytes;

    public static class Entry {
        int length;
        int lengthWithBlankLine;
        int offset;
        boolean oldStyle;
        byte[] rawBytes;

        public Entry(int offset, int length, int lengthWithBlankLine, byte[] rawBytes) {
            this.offset = offset;
            this.length = length;
            this.lengthWithBlankLine = lengthWithBlankLine;
            this.rawBytes = rawBytes;
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

        private void doOldStyle(MessageDigest md, byte[] bytes, int offset, int length) {
            int i = offset;
            int start = offset;
            int max = offset + length;
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
            switch (this.rawBytes[i]) {
                case BaseCalendar.OCTOBER /*10*/:
                    break;
                case Calendar.SECOND /*13*/:
                    if (pos.endOfFirstLine == -1) {
                        pos.endOfFirstLine = i - 1;
                    }
                    if (i < len && this.rawBytes[i + 1] == 10) {
                        i++;
                        break;
                    }
                default:
                    allBlank = false;
                    continue;
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

    public ManifestDigester(byte[] r25) {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.JadxOverflowException: Regions stack size limit reached
	at jadx.core.utils.ErrorsCounter.addError(ErrorsCounter.java:42)
	at jadx.core.utils.ErrorsCounter.methodError(ErrorsCounter.java:66)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:33)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:17)
	at jadx.core.ProcessClass.process(ProcessClass.java:37)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
*/
        /*
        r24 = this;
        r24.<init>();
        r0 = r25;
        r1 = r24;
        r1.rawBytes = r0;
        r17 = new java.util.HashMap;
        r17.<init>();
        r0 = r17;
        r1 = r24;
        r1.entries = r0;
        r5 = new java.io.ByteArrayOutputStream;
        r5.<init>();
        r10 = new sun.security.util.ManifestDigester$Position;
        r10.<init>();
        r17 = 0;
        r0 = r24;
        r1 = r17;
        r17 = r0.findSection(r1, r10);
        if (r17 != 0) goto L_0x002b;
    L_0x002a:
        return;
    L_0x002b:
        r0 = r24;
        r0 = r0.entries;
        r17 = r0;
        r18 = "Manifest-Main-Attributes";
        r19 = new sun.security.util.ManifestDigester$Entry;
        r0 = r10.endOfSection;
        r20 = r0;
        r20 = r20 + 1;
        r0 = r10.startOfNext;
        r21 = r0;
        r0 = r24;
        r0 = r0.rawBytes;
        r22 = r0;
        r23 = 0;
        r0 = r19;
        r1 = r23;
        r2 = r20;
        r3 = r21;
        r4 = r22;
        r0.<init>(r1, r2, r3, r4);
        r17.put(r18, r19);
        r13 = r10.startOfNext;
    L_0x005a:
        r0 = r24;
        r17 = r0.findSection(r13, r10);
        if (r17 == 0) goto L_0x014f;
    L_0x0062:
        r0 = r10.endOfFirstLine;
        r17 = r0;
        r17 = r17 - r13;
        r8 = r17 + 1;
        r0 = r10.endOfSection;
        r17 = r0;
        r17 = r17 - r13;
        r11 = r17 + 1;
        r0 = r10.startOfNext;
        r17 = r0;
        r12 = r17 - r13;
        r17 = 6;
        r0 = r17;
        if (r8 <= r0) goto L_0x0141;
    L_0x007e:
        r0 = r24;
        r1 = r25;
        r17 = r0.isNameAttr(r1, r13);
        if (r17 == 0) goto L_0x0141;
    L_0x0088:
        r9 = new java.lang.StringBuilder;
        r9.<init>(r11);
        r17 = new java.lang.String;	 Catch:{ UnsupportedEncodingException -> 0x0145 }
        r18 = r13 + 6;	 Catch:{ UnsupportedEncodingException -> 0x0145 }
        r19 = r8 + -6;	 Catch:{ UnsupportedEncodingException -> 0x0145 }
        r20 = "UTF8";	 Catch:{ UnsupportedEncodingException -> 0x0145 }
        r0 = r17;	 Catch:{ UnsupportedEncodingException -> 0x0145 }
        r1 = r25;	 Catch:{ UnsupportedEncodingException -> 0x0145 }
        r2 = r18;	 Catch:{ UnsupportedEncodingException -> 0x0145 }
        r3 = r19;	 Catch:{ UnsupportedEncodingException -> 0x0145 }
        r4 = r20;	 Catch:{ UnsupportedEncodingException -> 0x0145 }
        r0.<init>(r1, r2, r3, r4);	 Catch:{ UnsupportedEncodingException -> 0x0145 }
        r0 = r17;	 Catch:{ UnsupportedEncodingException -> 0x0145 }
        r9.append(r0);	 Catch:{ UnsupportedEncodingException -> 0x0145 }
        r6 = r13 + r8;	 Catch:{ UnsupportedEncodingException -> 0x0145 }
        r17 = r6 - r13;	 Catch:{ UnsupportedEncodingException -> 0x0145 }
        r0 = r17;	 Catch:{ UnsupportedEncodingException -> 0x0145 }
        if (r0 >= r11) goto L_0x011d;	 Catch:{ UnsupportedEncodingException -> 0x0145 }
    L_0x00b0:
        r17 = r25[r6];	 Catch:{ UnsupportedEncodingException -> 0x0145 }
        r18 = 13;	 Catch:{ UnsupportedEncodingException -> 0x0145 }
        r0 = r17;	 Catch:{ UnsupportedEncodingException -> 0x0145 }
        r1 = r18;	 Catch:{ UnsupportedEncodingException -> 0x0145 }
        if (r0 != r1) goto L_0x00e6;	 Catch:{ UnsupportedEncodingException -> 0x0145 }
    L_0x00ba:
        r6 = r6 + 2;	 Catch:{ UnsupportedEncodingException -> 0x0145 }
        r7 = r6;	 Catch:{ UnsupportedEncodingException -> 0x0145 }
    L_0x00bd:
        r17 = r7 - r13;	 Catch:{ UnsupportedEncodingException -> 0x0145 }
        r0 = r17;	 Catch:{ UnsupportedEncodingException -> 0x0145 }
        if (r0 >= r11) goto L_0x0124;	 Catch:{ UnsupportedEncodingException -> 0x0145 }
    L_0x00c3:
        r6 = r7 + 1;	 Catch:{ UnsupportedEncodingException -> 0x0145 }
        r17 = r25[r7];	 Catch:{ UnsupportedEncodingException -> 0x0145 }
        r18 = 32;	 Catch:{ UnsupportedEncodingException -> 0x0145 }
        r0 = r17;	 Catch:{ UnsupportedEncodingException -> 0x0145 }
        r1 = r18;	 Catch:{ UnsupportedEncodingException -> 0x0145 }
        if (r0 != r1) goto L_0x0125;	 Catch:{ UnsupportedEncodingException -> 0x0145 }
    L_0x00cf:
        r16 = r6;	 Catch:{ UnsupportedEncodingException -> 0x0145 }
        r7 = r6;	 Catch:{ UnsupportedEncodingException -> 0x0145 }
    L_0x00d2:
        r17 = r7 - r13;	 Catch:{ UnsupportedEncodingException -> 0x0145 }
        r0 = r17;	 Catch:{ UnsupportedEncodingException -> 0x0145 }
        if (r0 >= r11) goto L_0x00ea;	 Catch:{ UnsupportedEncodingException -> 0x0145 }
    L_0x00d8:
        r6 = r7 + 1;	 Catch:{ UnsupportedEncodingException -> 0x0145 }
        r17 = r25[r7];	 Catch:{ UnsupportedEncodingException -> 0x0145 }
        r18 = 10;	 Catch:{ UnsupportedEncodingException -> 0x0145 }
        r0 = r17;	 Catch:{ UnsupportedEncodingException -> 0x0145 }
        r1 = r18;	 Catch:{ UnsupportedEncodingException -> 0x0145 }
        if (r0 == r1) goto L_0x00eb;	 Catch:{ UnsupportedEncodingException -> 0x0145 }
    L_0x00e4:
        r7 = r6;	 Catch:{ UnsupportedEncodingException -> 0x0145 }
        goto L_0x00d2;	 Catch:{ UnsupportedEncodingException -> 0x0145 }
    L_0x00e6:
        r6 = r6 + 1;	 Catch:{ UnsupportedEncodingException -> 0x0145 }
        r7 = r6;	 Catch:{ UnsupportedEncodingException -> 0x0145 }
        goto L_0x00bd;	 Catch:{ UnsupportedEncodingException -> 0x0145 }
    L_0x00ea:
        r6 = r7;	 Catch:{ UnsupportedEncodingException -> 0x0145 }
    L_0x00eb:
        r17 = r6 + -1;	 Catch:{ UnsupportedEncodingException -> 0x0145 }
        r17 = r25[r17];	 Catch:{ UnsupportedEncodingException -> 0x0145 }
        r18 = 10;	 Catch:{ UnsupportedEncodingException -> 0x0145 }
        r0 = r17;	 Catch:{ UnsupportedEncodingException -> 0x0145 }
        r1 = r18;	 Catch:{ UnsupportedEncodingException -> 0x0145 }
        if (r0 == r1) goto L_0x00f8;	 Catch:{ UnsupportedEncodingException -> 0x0145 }
    L_0x00f7:
        return;	 Catch:{ UnsupportedEncodingException -> 0x0145 }
    L_0x00f8:
        r17 = r6 + -2;	 Catch:{ UnsupportedEncodingException -> 0x0145 }
        r17 = r25[r17];	 Catch:{ UnsupportedEncodingException -> 0x0145 }
        r18 = 13;	 Catch:{ UnsupportedEncodingException -> 0x0145 }
        r0 = r17;	 Catch:{ UnsupportedEncodingException -> 0x0145 }
        r1 = r18;	 Catch:{ UnsupportedEncodingException -> 0x0145 }
        if (r0 != r1) goto L_0x011f;	 Catch:{ UnsupportedEncodingException -> 0x0145 }
    L_0x0104:
        r17 = r6 - r16;	 Catch:{ UnsupportedEncodingException -> 0x0145 }
        r15 = r17 + -2;	 Catch:{ UnsupportedEncodingException -> 0x0145 }
    L_0x0108:
        r17 = new java.lang.String;	 Catch:{ UnsupportedEncodingException -> 0x0145 }
        r18 = "UTF8";	 Catch:{ UnsupportedEncodingException -> 0x0145 }
        r0 = r17;	 Catch:{ UnsupportedEncodingException -> 0x0145 }
        r1 = r25;	 Catch:{ UnsupportedEncodingException -> 0x0145 }
        r2 = r16;	 Catch:{ UnsupportedEncodingException -> 0x0145 }
        r3 = r18;	 Catch:{ UnsupportedEncodingException -> 0x0145 }
        r0.<init>(r1, r2, r15, r3);	 Catch:{ UnsupportedEncodingException -> 0x0145 }
        r0 = r17;	 Catch:{ UnsupportedEncodingException -> 0x0145 }
        r9.append(r0);	 Catch:{ UnsupportedEncodingException -> 0x0145 }
    L_0x011d:
        r7 = r6;	 Catch:{ UnsupportedEncodingException -> 0x0145 }
        goto L_0x00bd;	 Catch:{ UnsupportedEncodingException -> 0x0145 }
    L_0x011f:
        r17 = r6 - r16;	 Catch:{ UnsupportedEncodingException -> 0x0145 }
        r15 = r17 + -1;	 Catch:{ UnsupportedEncodingException -> 0x0145 }
        goto L_0x0108;	 Catch:{ UnsupportedEncodingException -> 0x0145 }
    L_0x0124:
        r6 = r7;	 Catch:{ UnsupportedEncodingException -> 0x0145 }
    L_0x0125:
        r0 = r24;	 Catch:{ UnsupportedEncodingException -> 0x0145 }
        r0 = r0.entries;	 Catch:{ UnsupportedEncodingException -> 0x0145 }
        r17 = r0;	 Catch:{ UnsupportedEncodingException -> 0x0145 }
        r18 = r9.toString();	 Catch:{ UnsupportedEncodingException -> 0x0145 }
        r19 = new sun.security.util.ManifestDigester$Entry;	 Catch:{ UnsupportedEncodingException -> 0x0145 }
        r0 = r24;	 Catch:{ UnsupportedEncodingException -> 0x0145 }
        r0 = r0.rawBytes;	 Catch:{ UnsupportedEncodingException -> 0x0145 }
        r20 = r0;	 Catch:{ UnsupportedEncodingException -> 0x0145 }
        r0 = r19;	 Catch:{ UnsupportedEncodingException -> 0x0145 }
        r1 = r20;	 Catch:{ UnsupportedEncodingException -> 0x0145 }
        r0.<init>(r13, r11, r12, r1);	 Catch:{ UnsupportedEncodingException -> 0x0145 }
        r17.put(r18, r19);	 Catch:{ UnsupportedEncodingException -> 0x0145 }
    L_0x0141:
        r13 = r10.startOfNext;
        goto L_0x005a;
    L_0x0145:
        r14 = move-exception;
        r17 = new java.lang.IllegalStateException;
        r18 = "UTF8 not available on platform";
        r17.<init>(r18);
        throw r17;
    L_0x014f:
        return;
        */
        throw new UnsupportedOperationException("Method not decompiled: sun.security.util.ManifestDigester.<init>(byte[]):void");
    }

    private boolean isNameAttr(byte[] bytes, int start) {
        if (bytes[start] != 78 && bytes[start] != 110) {
            return false;
        }
        if (bytes[start + 1] != 97 && bytes[start + 1] != 65) {
            return false;
        }
        if (bytes[start + 2] == 109 || bytes[start + 2] == 77) {
            return (bytes[start + 3] == 101 || bytes[start + 3] == 69) && bytes[start + 4] == 58 && bytes[start + 5] == 32;
        } else {
            return false;
        }
    }

    public Entry get(String name, boolean oldStyle) {
        Entry e = (Entry) this.entries.get(name);
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
