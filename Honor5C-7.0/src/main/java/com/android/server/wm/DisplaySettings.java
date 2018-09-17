package com.android.server.wm;

import android.graphics.Rect;
import android.os.Environment;
import android.util.AtomicFile;
import android.util.Slog;
import com.android.internal.util.FastXmlSerializer;
import com.android.internal.util.XmlUtils;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlSerializer;

public class DisplaySettings {
    private static final String TAG = null;
    private final HashMap<String, Entry> mEntries;
    private final AtomicFile mFile;

    public static class Entry {
        public final String name;
        public int overscanBottom;
        public int overscanLeft;
        public int overscanRight;
        public int overscanTop;

        public Entry(String _name) {
            this.name = _name;
        }
    }

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.server.wm.DisplaySettings.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.server.wm.DisplaySettings.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: com.android.server.wm.DisplaySettings.<clinit>():void");
    }

    public void readSettingsLocked() {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.JadxRuntimeException: Can't find block by offset: 0x004f in list []
	at jadx.core.utils.BlockUtils.getBlockByOffset(BlockUtils.java:42)
	at jadx.core.dex.instructions.IfNode.initBlocks(IfNode.java:58)
	at jadx.core.dex.visitors.blocksmaker.BlockFinish.initBlocksInIfNodes(BlockFinish.java:48)
	at jadx.core.dex.visitors.blocksmaker.BlockFinish.visit(BlockFinish.java:33)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:31)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:17)
	at jadx.core.ProcessClass.process(ProcessClass.java:37)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
*/
        /*
        r17 = this;
        r0 = r17;	 Catch:{ FileNotFoundException -> 0x0053 }
        r14 = r0.mFile;	 Catch:{ FileNotFoundException -> 0x0053 }
        r10 = r14.openRead();	 Catch:{ FileNotFoundException -> 0x0053 }
        r11 = 0;
        r9 = android.util.Xml.newPullParser();	 Catch:{ IllegalStateException -> 0x002c, NullPointerException -> 0x00ac, NumberFormatException -> 0x00f7, XmlPullParserException -> 0x018a, IOException -> 0x015f, IndexOutOfBoundsException -> 0x0134, all -> 0x01b8 }
        r14 = java.nio.charset.StandardCharsets.UTF_8;	 Catch:{ IllegalStateException -> 0x002c, NullPointerException -> 0x00ac, NumberFormatException -> 0x00f7, XmlPullParserException -> 0x018a, IOException -> 0x015f, IndexOutOfBoundsException -> 0x0134, all -> 0x01b8 }
        r14 = r14.name();	 Catch:{ IllegalStateException -> 0x002c, NullPointerException -> 0x00ac, NumberFormatException -> 0x00f7, XmlPullParserException -> 0x018a, IOException -> 0x015f, IndexOutOfBoundsException -> 0x0134, all -> 0x01b8 }
        r9.setInput(r10, r14);	 Catch:{ IllegalStateException -> 0x002c, NullPointerException -> 0x00ac, NumberFormatException -> 0x00f7, XmlPullParserException -> 0x018a, IOException -> 0x015f, IndexOutOfBoundsException -> 0x0134, all -> 0x01b8 }
    L_0x0016:
        r13 = r9.next();	 Catch:{ IllegalStateException -> 0x002c, NullPointerException -> 0x00ac, NumberFormatException -> 0x00f7, XmlPullParserException -> 0x018a, IOException -> 0x015f, IndexOutOfBoundsException -> 0x0134, all -> 0x01b8 }
        r14 = 2;	 Catch:{ IllegalStateException -> 0x002c, NullPointerException -> 0x00ac, NumberFormatException -> 0x00f7, XmlPullParserException -> 0x018a, IOException -> 0x015f, IndexOutOfBoundsException -> 0x0134, all -> 0x01b8 }
        if (r13 == r14) goto L_0x0020;	 Catch:{ IllegalStateException -> 0x002c, NullPointerException -> 0x00ac, NumberFormatException -> 0x00f7, XmlPullParserException -> 0x018a, IOException -> 0x015f, IndexOutOfBoundsException -> 0x0134, all -> 0x01b8 }
    L_0x001d:
        r14 = 1;	 Catch:{ IllegalStateException -> 0x002c, NullPointerException -> 0x00ac, NumberFormatException -> 0x00f7, XmlPullParserException -> 0x018a, IOException -> 0x015f, IndexOutOfBoundsException -> 0x0134, all -> 0x01b8 }
        if (r13 != r14) goto L_0x0016;	 Catch:{ IllegalStateException -> 0x002c, NullPointerException -> 0x00ac, NumberFormatException -> 0x00f7, XmlPullParserException -> 0x018a, IOException -> 0x015f, IndexOutOfBoundsException -> 0x0134, all -> 0x01b8 }
    L_0x0020:
        r14 = 2;	 Catch:{ IllegalStateException -> 0x002c, NullPointerException -> 0x00ac, NumberFormatException -> 0x00f7, XmlPullParserException -> 0x018a, IOException -> 0x015f, IndexOutOfBoundsException -> 0x0134, all -> 0x01b8 }
        if (r13 == r14) goto L_0x007f;	 Catch:{ IllegalStateException -> 0x002c, NullPointerException -> 0x00ac, NumberFormatException -> 0x00f7, XmlPullParserException -> 0x018a, IOException -> 0x015f, IndexOutOfBoundsException -> 0x0134, all -> 0x01b8 }
    L_0x0023:
        r14 = new java.lang.IllegalStateException;	 Catch:{ IllegalStateException -> 0x002c, NullPointerException -> 0x00ac, NumberFormatException -> 0x00f7, XmlPullParserException -> 0x018a, IOException -> 0x015f, IndexOutOfBoundsException -> 0x0134, all -> 0x01b8 }
        r15 = "no start tag found";	 Catch:{ IllegalStateException -> 0x002c, NullPointerException -> 0x00ac, NumberFormatException -> 0x00f7, XmlPullParserException -> 0x018a, IOException -> 0x015f, IndexOutOfBoundsException -> 0x0134, all -> 0x01b8 }
        r14.<init>(r15);	 Catch:{ IllegalStateException -> 0x002c, NullPointerException -> 0x00ac, NumberFormatException -> 0x00f7, XmlPullParserException -> 0x018a, IOException -> 0x015f, IndexOutOfBoundsException -> 0x0134, all -> 0x01b8 }
        throw r14;	 Catch:{ IllegalStateException -> 0x002c, NullPointerException -> 0x00ac, NumberFormatException -> 0x00f7, XmlPullParserException -> 0x018a, IOException -> 0x015f, IndexOutOfBoundsException -> 0x0134, all -> 0x01b8 }
    L_0x002c:
        r3 = move-exception;
        r14 = TAG;	 Catch:{ IllegalStateException -> 0x002c, NullPointerException -> 0x00ac, NumberFormatException -> 0x00f7, XmlPullParserException -> 0x018a, IOException -> 0x015f, IndexOutOfBoundsException -> 0x0134, all -> 0x01b8 }
        r15 = new java.lang.StringBuilder;	 Catch:{ IllegalStateException -> 0x002c, NullPointerException -> 0x00ac, NumberFormatException -> 0x00f7, XmlPullParserException -> 0x018a, IOException -> 0x015f, IndexOutOfBoundsException -> 0x0134, all -> 0x01b8 }
        r15.<init>();	 Catch:{ IllegalStateException -> 0x002c, NullPointerException -> 0x00ac, NumberFormatException -> 0x00f7, XmlPullParserException -> 0x018a, IOException -> 0x015f, IndexOutOfBoundsException -> 0x0134, all -> 0x01b8 }
        r16 = "Failed parsing ";	 Catch:{ IllegalStateException -> 0x002c, NullPointerException -> 0x00ac, NumberFormatException -> 0x00f7, XmlPullParserException -> 0x018a, IOException -> 0x015f, IndexOutOfBoundsException -> 0x0134, all -> 0x01b8 }
        r15 = r15.append(r16);	 Catch:{ IllegalStateException -> 0x002c, NullPointerException -> 0x00ac, NumberFormatException -> 0x00f7, XmlPullParserException -> 0x018a, IOException -> 0x015f, IndexOutOfBoundsException -> 0x0134, all -> 0x01b8 }
        r15 = r15.append(r3);	 Catch:{ IllegalStateException -> 0x002c, NullPointerException -> 0x00ac, NumberFormatException -> 0x00f7, XmlPullParserException -> 0x018a, IOException -> 0x015f, IndexOutOfBoundsException -> 0x0134, all -> 0x01b8 }
        r15 = r15.toString();	 Catch:{ IllegalStateException -> 0x002c, NullPointerException -> 0x00ac, NumberFormatException -> 0x00f7, XmlPullParserException -> 0x018a, IOException -> 0x015f, IndexOutOfBoundsException -> 0x0134, all -> 0x01b8 }
        android.util.Slog.w(r14, r15);	 Catch:{ IllegalStateException -> 0x002c, NullPointerException -> 0x00ac, NumberFormatException -> 0x00f7, XmlPullParserException -> 0x018a, IOException -> 0x015f, IndexOutOfBoundsException -> 0x0134, all -> 0x01b8 }
        if (r11 != 0) goto L_0x004f;
    L_0x0048:
        r0 = r17;
        r14 = r0.mEntries;
        r14.clear();
    L_0x004f:
        r10.close();	 Catch:{ IOException -> 0x01b5 }
    L_0x0052:
        return;
    L_0x0053:
        r1 = move-exception;
        r14 = TAG;
        r15 = new java.lang.StringBuilder;
        r15.<init>();
        r16 = "No existing display settings ";
        r15 = r15.append(r16);
        r0 = r17;
        r0 = r0.mFile;
        r16 = r0;
        r16 = r16.getBaseFile();
        r15 = r15.append(r16);
        r16 = "; starting empty";
        r15 = r15.append(r16);
        r15 = r15.toString();
        android.util.Slog.i(r14, r15);
        return;
    L_0x007f:
        r8 = r9.getDepth();	 Catch:{ IllegalStateException -> 0x002c, NullPointerException -> 0x00ac, NumberFormatException -> 0x00f7, XmlPullParserException -> 0x018a, IOException -> 0x015f, IndexOutOfBoundsException -> 0x0134, all -> 0x01b8 }
    L_0x0083:
        r13 = r9.next();	 Catch:{ IllegalStateException -> 0x002c, NullPointerException -> 0x00ac, NumberFormatException -> 0x00f7, XmlPullParserException -> 0x018a, IOException -> 0x015f, IndexOutOfBoundsException -> 0x0134, all -> 0x01b8 }
        r14 = 1;	 Catch:{ IllegalStateException -> 0x002c, NullPointerException -> 0x00ac, NumberFormatException -> 0x00f7, XmlPullParserException -> 0x018a, IOException -> 0x015f, IndexOutOfBoundsException -> 0x0134, all -> 0x01b8 }
        if (r13 == r14) goto L_0x0122;	 Catch:{ IllegalStateException -> 0x002c, NullPointerException -> 0x00ac, NumberFormatException -> 0x00f7, XmlPullParserException -> 0x018a, IOException -> 0x015f, IndexOutOfBoundsException -> 0x0134, all -> 0x01b8 }
    L_0x008a:
        r14 = 3;	 Catch:{ IllegalStateException -> 0x002c, NullPointerException -> 0x00ac, NumberFormatException -> 0x00f7, XmlPullParserException -> 0x018a, IOException -> 0x015f, IndexOutOfBoundsException -> 0x0134, all -> 0x01b8 }
        if (r13 != r14) goto L_0x0093;	 Catch:{ IllegalStateException -> 0x002c, NullPointerException -> 0x00ac, NumberFormatException -> 0x00f7, XmlPullParserException -> 0x018a, IOException -> 0x015f, IndexOutOfBoundsException -> 0x0134, all -> 0x01b8 }
    L_0x008d:
        r14 = r9.getDepth();	 Catch:{ IllegalStateException -> 0x002c, NullPointerException -> 0x00ac, NumberFormatException -> 0x00f7, XmlPullParserException -> 0x018a, IOException -> 0x015f, IndexOutOfBoundsException -> 0x0134, all -> 0x01b8 }
        if (r14 <= r8) goto L_0x0122;	 Catch:{ IllegalStateException -> 0x002c, NullPointerException -> 0x00ac, NumberFormatException -> 0x00f7, XmlPullParserException -> 0x018a, IOException -> 0x015f, IndexOutOfBoundsException -> 0x0134, all -> 0x01b8 }
    L_0x0093:
        r14 = 3;	 Catch:{ IllegalStateException -> 0x002c, NullPointerException -> 0x00ac, NumberFormatException -> 0x00f7, XmlPullParserException -> 0x018a, IOException -> 0x015f, IndexOutOfBoundsException -> 0x0134, all -> 0x01b8 }
        if (r13 == r14) goto L_0x0083;	 Catch:{ IllegalStateException -> 0x002c, NullPointerException -> 0x00ac, NumberFormatException -> 0x00f7, XmlPullParserException -> 0x018a, IOException -> 0x015f, IndexOutOfBoundsException -> 0x0134, all -> 0x01b8 }
    L_0x0096:
        r14 = 4;	 Catch:{ IllegalStateException -> 0x002c, NullPointerException -> 0x00ac, NumberFormatException -> 0x00f7, XmlPullParserException -> 0x018a, IOException -> 0x015f, IndexOutOfBoundsException -> 0x0134, all -> 0x01b8 }
        if (r13 == r14) goto L_0x0083;	 Catch:{ IllegalStateException -> 0x002c, NullPointerException -> 0x00ac, NumberFormatException -> 0x00f7, XmlPullParserException -> 0x018a, IOException -> 0x015f, IndexOutOfBoundsException -> 0x0134, all -> 0x01b8 }
    L_0x0099:
        r12 = r9.getName();	 Catch:{ IllegalStateException -> 0x002c, NullPointerException -> 0x00ac, NumberFormatException -> 0x00f7, XmlPullParserException -> 0x018a, IOException -> 0x015f, IndexOutOfBoundsException -> 0x0134, all -> 0x01b8 }
        r14 = "display";	 Catch:{ IllegalStateException -> 0x002c, NullPointerException -> 0x00ac, NumberFormatException -> 0x00f7, XmlPullParserException -> 0x018a, IOException -> 0x015f, IndexOutOfBoundsException -> 0x0134, all -> 0x01b8 }
        r14 = r12.equals(r14);	 Catch:{ IllegalStateException -> 0x002c, NullPointerException -> 0x00ac, NumberFormatException -> 0x00f7, XmlPullParserException -> 0x018a, IOException -> 0x015f, IndexOutOfBoundsException -> 0x0134, all -> 0x01b8 }
        if (r14 == 0) goto L_0x00d6;	 Catch:{ IllegalStateException -> 0x002c, NullPointerException -> 0x00ac, NumberFormatException -> 0x00f7, XmlPullParserException -> 0x018a, IOException -> 0x015f, IndexOutOfBoundsException -> 0x0134, all -> 0x01b8 }
    L_0x00a6:
        r0 = r17;	 Catch:{ IllegalStateException -> 0x002c, NullPointerException -> 0x00ac, NumberFormatException -> 0x00f7, XmlPullParserException -> 0x018a, IOException -> 0x015f, IndexOutOfBoundsException -> 0x0134, all -> 0x01b8 }
        r0.readDisplay(r9);	 Catch:{ IllegalStateException -> 0x002c, NullPointerException -> 0x00ac, NumberFormatException -> 0x00f7, XmlPullParserException -> 0x018a, IOException -> 0x015f, IndexOutOfBoundsException -> 0x0134, all -> 0x01b8 }
        goto L_0x0083;
    L_0x00ac:
        r5 = move-exception;
        r14 = TAG;	 Catch:{ IllegalStateException -> 0x002c, NullPointerException -> 0x00ac, NumberFormatException -> 0x00f7, XmlPullParserException -> 0x018a, IOException -> 0x015f, IndexOutOfBoundsException -> 0x0134, all -> 0x01b8 }
        r15 = new java.lang.StringBuilder;	 Catch:{ IllegalStateException -> 0x002c, NullPointerException -> 0x00ac, NumberFormatException -> 0x00f7, XmlPullParserException -> 0x018a, IOException -> 0x015f, IndexOutOfBoundsException -> 0x0134, all -> 0x01b8 }
        r15.<init>();	 Catch:{ IllegalStateException -> 0x002c, NullPointerException -> 0x00ac, NumberFormatException -> 0x00f7, XmlPullParserException -> 0x018a, IOException -> 0x015f, IndexOutOfBoundsException -> 0x0134, all -> 0x01b8 }
        r16 = "Failed parsing ";	 Catch:{ IllegalStateException -> 0x002c, NullPointerException -> 0x00ac, NumberFormatException -> 0x00f7, XmlPullParserException -> 0x018a, IOException -> 0x015f, IndexOutOfBoundsException -> 0x0134, all -> 0x01b8 }
        r15 = r15.append(r16);	 Catch:{ IllegalStateException -> 0x002c, NullPointerException -> 0x00ac, NumberFormatException -> 0x00f7, XmlPullParserException -> 0x018a, IOException -> 0x015f, IndexOutOfBoundsException -> 0x0134, all -> 0x01b8 }
        r15 = r15.append(r5);	 Catch:{ IllegalStateException -> 0x002c, NullPointerException -> 0x00ac, NumberFormatException -> 0x00f7, XmlPullParserException -> 0x018a, IOException -> 0x015f, IndexOutOfBoundsException -> 0x0134, all -> 0x01b8 }
        r15 = r15.toString();	 Catch:{ IllegalStateException -> 0x002c, NullPointerException -> 0x00ac, NumberFormatException -> 0x00f7, XmlPullParserException -> 0x018a, IOException -> 0x015f, IndexOutOfBoundsException -> 0x0134, all -> 0x01b8 }
        android.util.Slog.w(r14, r15);	 Catch:{ IllegalStateException -> 0x002c, NullPointerException -> 0x00ac, NumberFormatException -> 0x00f7, XmlPullParserException -> 0x018a, IOException -> 0x015f, IndexOutOfBoundsException -> 0x0134, all -> 0x01b8 }
        if (r11 != 0) goto L_0x00cf;
    L_0x00c8:
        r0 = r17;
        r14 = r0.mEntries;
        r14.clear();
    L_0x00cf:
        r10.close();	 Catch:{ IOException -> 0x00d3 }
        goto L_0x0052;
    L_0x00d3:
        r2 = move-exception;
        goto L_0x0052;
    L_0x00d6:
        r14 = TAG;	 Catch:{ IllegalStateException -> 0x002c, NullPointerException -> 0x00ac, NumberFormatException -> 0x00f7, XmlPullParserException -> 0x018a, IOException -> 0x015f, IndexOutOfBoundsException -> 0x0134, all -> 0x01b8 }
        r15 = new java.lang.StringBuilder;	 Catch:{ IllegalStateException -> 0x002c, NullPointerException -> 0x00ac, NumberFormatException -> 0x00f7, XmlPullParserException -> 0x018a, IOException -> 0x015f, IndexOutOfBoundsException -> 0x0134, all -> 0x01b8 }
        r15.<init>();	 Catch:{ IllegalStateException -> 0x002c, NullPointerException -> 0x00ac, NumberFormatException -> 0x00f7, XmlPullParserException -> 0x018a, IOException -> 0x015f, IndexOutOfBoundsException -> 0x0134, all -> 0x01b8 }
        r16 = "Unknown element under <display-settings>: ";	 Catch:{ IllegalStateException -> 0x002c, NullPointerException -> 0x00ac, NumberFormatException -> 0x00f7, XmlPullParserException -> 0x018a, IOException -> 0x015f, IndexOutOfBoundsException -> 0x0134, all -> 0x01b8 }
        r15 = r15.append(r16);	 Catch:{ IllegalStateException -> 0x002c, NullPointerException -> 0x00ac, NumberFormatException -> 0x00f7, XmlPullParserException -> 0x018a, IOException -> 0x015f, IndexOutOfBoundsException -> 0x0134, all -> 0x01b8 }
        r16 = r9.getName();	 Catch:{ IllegalStateException -> 0x002c, NullPointerException -> 0x00ac, NumberFormatException -> 0x00f7, XmlPullParserException -> 0x018a, IOException -> 0x015f, IndexOutOfBoundsException -> 0x0134, all -> 0x01b8 }
        r15 = r15.append(r16);	 Catch:{ IllegalStateException -> 0x002c, NullPointerException -> 0x00ac, NumberFormatException -> 0x00f7, XmlPullParserException -> 0x018a, IOException -> 0x015f, IndexOutOfBoundsException -> 0x0134, all -> 0x01b8 }
        r15 = r15.toString();	 Catch:{ IllegalStateException -> 0x002c, NullPointerException -> 0x00ac, NumberFormatException -> 0x00f7, XmlPullParserException -> 0x018a, IOException -> 0x015f, IndexOutOfBoundsException -> 0x0134, all -> 0x01b8 }
        android.util.Slog.w(r14, r15);	 Catch:{ IllegalStateException -> 0x002c, NullPointerException -> 0x00ac, NumberFormatException -> 0x00f7, XmlPullParserException -> 0x018a, IOException -> 0x015f, IndexOutOfBoundsException -> 0x0134, all -> 0x01b8 }
        com.android.internal.util.XmlUtils.skipCurrentTag(r9);	 Catch:{ IllegalStateException -> 0x002c, NullPointerException -> 0x00ac, NumberFormatException -> 0x00f7, XmlPullParserException -> 0x018a, IOException -> 0x015f, IndexOutOfBoundsException -> 0x0134, all -> 0x01b8 }
        goto L_0x0083;
    L_0x00f7:
        r6 = move-exception;
        r14 = TAG;	 Catch:{ IllegalStateException -> 0x002c, NullPointerException -> 0x00ac, NumberFormatException -> 0x00f7, XmlPullParserException -> 0x018a, IOException -> 0x015f, IndexOutOfBoundsException -> 0x0134, all -> 0x01b8 }
        r15 = new java.lang.StringBuilder;	 Catch:{ IllegalStateException -> 0x002c, NullPointerException -> 0x00ac, NumberFormatException -> 0x00f7, XmlPullParserException -> 0x018a, IOException -> 0x015f, IndexOutOfBoundsException -> 0x0134, all -> 0x01b8 }
        r15.<init>();	 Catch:{ IllegalStateException -> 0x002c, NullPointerException -> 0x00ac, NumberFormatException -> 0x00f7, XmlPullParserException -> 0x018a, IOException -> 0x015f, IndexOutOfBoundsException -> 0x0134, all -> 0x01b8 }
        r16 = "Failed parsing ";	 Catch:{ IllegalStateException -> 0x002c, NullPointerException -> 0x00ac, NumberFormatException -> 0x00f7, XmlPullParserException -> 0x018a, IOException -> 0x015f, IndexOutOfBoundsException -> 0x0134, all -> 0x01b8 }
        r15 = r15.append(r16);	 Catch:{ IllegalStateException -> 0x002c, NullPointerException -> 0x00ac, NumberFormatException -> 0x00f7, XmlPullParserException -> 0x018a, IOException -> 0x015f, IndexOutOfBoundsException -> 0x0134, all -> 0x01b8 }
        r15 = r15.append(r6);	 Catch:{ IllegalStateException -> 0x002c, NullPointerException -> 0x00ac, NumberFormatException -> 0x00f7, XmlPullParserException -> 0x018a, IOException -> 0x015f, IndexOutOfBoundsException -> 0x0134, all -> 0x01b8 }
        r15 = r15.toString();	 Catch:{ IllegalStateException -> 0x002c, NullPointerException -> 0x00ac, NumberFormatException -> 0x00f7, XmlPullParserException -> 0x018a, IOException -> 0x015f, IndexOutOfBoundsException -> 0x0134, all -> 0x01b8 }
        android.util.Slog.w(r14, r15);	 Catch:{ IllegalStateException -> 0x002c, NullPointerException -> 0x00ac, NumberFormatException -> 0x00f7, XmlPullParserException -> 0x018a, IOException -> 0x015f, IndexOutOfBoundsException -> 0x0134, all -> 0x01b8 }
        if (r11 != 0) goto L_0x011a;
    L_0x0113:
        r0 = r17;
        r14 = r0.mEntries;
        r14.clear();
    L_0x011a:
        r10.close();	 Catch:{ IOException -> 0x011f }
        goto L_0x0052;
    L_0x011f:
        r2 = move-exception;
        goto L_0x0052;
    L_0x0122:
        r11 = 1;
        if (r11 != 0) goto L_0x012c;
    L_0x0125:
        r0 = r17;
        r14 = r0.mEntries;
        r14.clear();
    L_0x012c:
        r10.close();	 Catch:{ IOException -> 0x0131 }
        goto L_0x0052;
    L_0x0131:
        r2 = move-exception;
        goto L_0x0052;
    L_0x0134:
        r4 = move-exception;
        r14 = TAG;	 Catch:{ IllegalStateException -> 0x002c, NullPointerException -> 0x00ac, NumberFormatException -> 0x00f7, XmlPullParserException -> 0x018a, IOException -> 0x015f, IndexOutOfBoundsException -> 0x0134, all -> 0x01b8 }
        r15 = new java.lang.StringBuilder;	 Catch:{ IllegalStateException -> 0x002c, NullPointerException -> 0x00ac, NumberFormatException -> 0x00f7, XmlPullParserException -> 0x018a, IOException -> 0x015f, IndexOutOfBoundsException -> 0x0134, all -> 0x01b8 }
        r15.<init>();	 Catch:{ IllegalStateException -> 0x002c, NullPointerException -> 0x00ac, NumberFormatException -> 0x00f7, XmlPullParserException -> 0x018a, IOException -> 0x015f, IndexOutOfBoundsException -> 0x0134, all -> 0x01b8 }
        r16 = "Failed parsing ";	 Catch:{ IllegalStateException -> 0x002c, NullPointerException -> 0x00ac, NumberFormatException -> 0x00f7, XmlPullParserException -> 0x018a, IOException -> 0x015f, IndexOutOfBoundsException -> 0x0134, all -> 0x01b8 }
        r15 = r15.append(r16);	 Catch:{ IllegalStateException -> 0x002c, NullPointerException -> 0x00ac, NumberFormatException -> 0x00f7, XmlPullParserException -> 0x018a, IOException -> 0x015f, IndexOutOfBoundsException -> 0x0134, all -> 0x01b8 }
        r15 = r15.append(r4);	 Catch:{ IllegalStateException -> 0x002c, NullPointerException -> 0x00ac, NumberFormatException -> 0x00f7, XmlPullParserException -> 0x018a, IOException -> 0x015f, IndexOutOfBoundsException -> 0x0134, all -> 0x01b8 }
        r15 = r15.toString();	 Catch:{ IllegalStateException -> 0x002c, NullPointerException -> 0x00ac, NumberFormatException -> 0x00f7, XmlPullParserException -> 0x018a, IOException -> 0x015f, IndexOutOfBoundsException -> 0x0134, all -> 0x01b8 }
        android.util.Slog.w(r14, r15);	 Catch:{ IllegalStateException -> 0x002c, NullPointerException -> 0x00ac, NumberFormatException -> 0x00f7, XmlPullParserException -> 0x018a, IOException -> 0x015f, IndexOutOfBoundsException -> 0x0134, all -> 0x01b8 }
        if (r11 != 0) goto L_0x0157;
    L_0x0150:
        r0 = r17;
        r14 = r0.mEntries;
        r14.clear();
    L_0x0157:
        r10.close();	 Catch:{ IOException -> 0x015c }
        goto L_0x0052;
    L_0x015c:
        r2 = move-exception;
        goto L_0x0052;
    L_0x015f:
        r2 = move-exception;
        r14 = TAG;	 Catch:{ IllegalStateException -> 0x002c, NullPointerException -> 0x00ac, NumberFormatException -> 0x00f7, XmlPullParserException -> 0x018a, IOException -> 0x015f, IndexOutOfBoundsException -> 0x0134, all -> 0x01b8 }
        r15 = new java.lang.StringBuilder;	 Catch:{ IllegalStateException -> 0x002c, NullPointerException -> 0x00ac, NumberFormatException -> 0x00f7, XmlPullParserException -> 0x018a, IOException -> 0x015f, IndexOutOfBoundsException -> 0x0134, all -> 0x01b8 }
        r15.<init>();	 Catch:{ IllegalStateException -> 0x002c, NullPointerException -> 0x00ac, NumberFormatException -> 0x00f7, XmlPullParserException -> 0x018a, IOException -> 0x015f, IndexOutOfBoundsException -> 0x0134, all -> 0x01b8 }
        r16 = "Failed parsing ";	 Catch:{ IllegalStateException -> 0x002c, NullPointerException -> 0x00ac, NumberFormatException -> 0x00f7, XmlPullParserException -> 0x018a, IOException -> 0x015f, IndexOutOfBoundsException -> 0x0134, all -> 0x01b8 }
        r15 = r15.append(r16);	 Catch:{ IllegalStateException -> 0x002c, NullPointerException -> 0x00ac, NumberFormatException -> 0x00f7, XmlPullParserException -> 0x018a, IOException -> 0x015f, IndexOutOfBoundsException -> 0x0134, all -> 0x01b8 }
        r15 = r15.append(r2);	 Catch:{ IllegalStateException -> 0x002c, NullPointerException -> 0x00ac, NumberFormatException -> 0x00f7, XmlPullParserException -> 0x018a, IOException -> 0x015f, IndexOutOfBoundsException -> 0x0134, all -> 0x01b8 }
        r15 = r15.toString();	 Catch:{ IllegalStateException -> 0x002c, NullPointerException -> 0x00ac, NumberFormatException -> 0x00f7, XmlPullParserException -> 0x018a, IOException -> 0x015f, IndexOutOfBoundsException -> 0x0134, all -> 0x01b8 }
        android.util.Slog.w(r14, r15);	 Catch:{ IllegalStateException -> 0x002c, NullPointerException -> 0x00ac, NumberFormatException -> 0x00f7, XmlPullParserException -> 0x018a, IOException -> 0x015f, IndexOutOfBoundsException -> 0x0134, all -> 0x01b8 }
        if (r11 != 0) goto L_0x0182;
    L_0x017b:
        r0 = r17;
        r14 = r0.mEntries;
        r14.clear();
    L_0x0182:
        r10.close();	 Catch:{ IOException -> 0x0187 }
        goto L_0x0052;
    L_0x0187:
        r2 = move-exception;
        goto L_0x0052;
    L_0x018a:
        r7 = move-exception;
        r14 = TAG;	 Catch:{ IllegalStateException -> 0x002c, NullPointerException -> 0x00ac, NumberFormatException -> 0x00f7, XmlPullParserException -> 0x018a, IOException -> 0x015f, IndexOutOfBoundsException -> 0x0134, all -> 0x01b8 }
        r15 = new java.lang.StringBuilder;	 Catch:{ IllegalStateException -> 0x002c, NullPointerException -> 0x00ac, NumberFormatException -> 0x00f7, XmlPullParserException -> 0x018a, IOException -> 0x015f, IndexOutOfBoundsException -> 0x0134, all -> 0x01b8 }
        r15.<init>();	 Catch:{ IllegalStateException -> 0x002c, NullPointerException -> 0x00ac, NumberFormatException -> 0x00f7, XmlPullParserException -> 0x018a, IOException -> 0x015f, IndexOutOfBoundsException -> 0x0134, all -> 0x01b8 }
        r16 = "Failed parsing ";	 Catch:{ IllegalStateException -> 0x002c, NullPointerException -> 0x00ac, NumberFormatException -> 0x00f7, XmlPullParserException -> 0x018a, IOException -> 0x015f, IndexOutOfBoundsException -> 0x0134, all -> 0x01b8 }
        r15 = r15.append(r16);	 Catch:{ IllegalStateException -> 0x002c, NullPointerException -> 0x00ac, NumberFormatException -> 0x00f7, XmlPullParserException -> 0x018a, IOException -> 0x015f, IndexOutOfBoundsException -> 0x0134, all -> 0x01b8 }
        r15 = r15.append(r7);	 Catch:{ IllegalStateException -> 0x002c, NullPointerException -> 0x00ac, NumberFormatException -> 0x00f7, XmlPullParserException -> 0x018a, IOException -> 0x015f, IndexOutOfBoundsException -> 0x0134, all -> 0x01b8 }
        r15 = r15.toString();	 Catch:{ IllegalStateException -> 0x002c, NullPointerException -> 0x00ac, NumberFormatException -> 0x00f7, XmlPullParserException -> 0x018a, IOException -> 0x015f, IndexOutOfBoundsException -> 0x0134, all -> 0x01b8 }
        android.util.Slog.w(r14, r15);	 Catch:{ IllegalStateException -> 0x002c, NullPointerException -> 0x00ac, NumberFormatException -> 0x00f7, XmlPullParserException -> 0x018a, IOException -> 0x015f, IndexOutOfBoundsException -> 0x0134, all -> 0x01b8 }
        if (r11 != 0) goto L_0x01ad;
    L_0x01a6:
        r0 = r17;
        r14 = r0.mEntries;
        r14.clear();
    L_0x01ad:
        r10.close();	 Catch:{ IOException -> 0x01b2 }
        goto L_0x0052;
    L_0x01b2:
        r2 = move-exception;
        goto L_0x0052;
    L_0x01b5:
        r2 = move-exception;
        goto L_0x0052;
    L_0x01b8:
        r14 = move-exception;
        if (r11 != 0) goto L_0x01c2;
    L_0x01bb:
        r0 = r17;
        r15 = r0.mEntries;
        r15.clear();
    L_0x01c2:
        r10.close();	 Catch:{ IOException -> 0x01c6 }
    L_0x01c5:
        throw r14;
    L_0x01c6:
        r2 = move-exception;
        goto L_0x01c5;
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.server.wm.DisplaySettings.readSettingsLocked():void");
    }

    public DisplaySettings() {
        this.mEntries = new HashMap();
        this.mFile = new AtomicFile(new File(new File(Environment.getDataDirectory(), "system"), "display_settings.xml"));
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void getOverscanLocked(String name, String uniqueId, Rect outRect) {
        Entry entry;
        if (uniqueId != null) {
            entry = (Entry) this.mEntries.get(uniqueId);
        }
        entry = (Entry) this.mEntries.get(name);
        if (entry != null) {
            outRect.left = entry.overscanLeft;
            outRect.top = entry.overscanTop;
            outRect.right = entry.overscanRight;
            outRect.bottom = entry.overscanBottom;
            return;
        }
        outRect.set(0, 0, 0, 0);
    }

    public void setOverscanLocked(String uniqueId, String name, int left, int top, int right, int bottom) {
        if (left == 0 && top == 0 && right == 0 && bottom == 0) {
            this.mEntries.remove(uniqueId);
            this.mEntries.remove(name);
            return;
        }
        Entry entry = (Entry) this.mEntries.get(uniqueId);
        if (entry == null) {
            entry = new Entry(uniqueId);
            this.mEntries.put(uniqueId, entry);
        }
        entry.overscanLeft = left;
        entry.overscanTop = top;
        entry.overscanRight = right;
        entry.overscanBottom = bottom;
    }

    private int getIntAttribute(XmlPullParser parser, String name) {
        int i = 0;
        try {
            String str = parser.getAttributeValue(null, name);
            if (str != null) {
                i = Integer.parseInt(str);
            }
            return i;
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    private void readDisplay(XmlPullParser parser) throws NumberFormatException, XmlPullParserException, IOException {
        String name = parser.getAttributeValue(null, "name");
        if (name != null) {
            Entry entry = new Entry(name);
            entry.overscanLeft = getIntAttribute(parser, "overscanLeft");
            entry.overscanTop = getIntAttribute(parser, "overscanTop");
            entry.overscanRight = getIntAttribute(parser, "overscanRight");
            entry.overscanBottom = getIntAttribute(parser, "overscanBottom");
            this.mEntries.put(name, entry);
        }
        XmlUtils.skipCurrentTag(parser);
    }

    public void writeSettingsLocked() {
        try {
            FileOutputStream stream = this.mFile.startWrite();
            try {
                XmlSerializer out = new FastXmlSerializer();
                out.setOutput(stream, StandardCharsets.UTF_8.name());
                out.startDocument(null, Boolean.valueOf(true));
                out.startTag(null, "display-settings");
                for (Entry entry : this.mEntries.values()) {
                    out.startTag(null, "display");
                    out.attribute(null, "name", entry.name);
                    if (entry.overscanLeft != 0) {
                        out.attribute(null, "overscanLeft", Integer.toString(entry.overscanLeft));
                    }
                    if (entry.overscanTop != 0) {
                        out.attribute(null, "overscanTop", Integer.toString(entry.overscanTop));
                    }
                    if (entry.overscanRight != 0) {
                        out.attribute(null, "overscanRight", Integer.toString(entry.overscanRight));
                    }
                    if (entry.overscanBottom != 0) {
                        out.attribute(null, "overscanBottom", Integer.toString(entry.overscanBottom));
                    }
                    out.endTag(null, "display");
                }
                out.endTag(null, "display-settings");
                out.endDocument();
                this.mFile.finishWrite(stream);
            } catch (IOException e) {
                Slog.w(TAG, "Failed to write display settings, restoring backup.", e);
                this.mFile.failWrite(stream);
            }
        } catch (IOException e2) {
            Slog.w(TAG, "Failed to write display settings: " + e2);
        }
    }
}
