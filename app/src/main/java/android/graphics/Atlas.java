package android.graphics;

import android.speech.tts.TextToSpeech.Engine;
import android.telecom.AudioState;
import android.util.Log;

public class Atlas {
    private static final /* synthetic */ int[] -android-graphics-Atlas$TypeSwitchesValues = null;
    public static final int FLAG_ADD_PADDING = 2;
    public static final int FLAG_DEFAULTS = 2;
    private static final String TAG = "Atlas";
    private final int mFlags;
    private final int mHeight;
    private Policy mPolicy;
    private final Type mType;
    private final int mWidth;

    public static class Entry {
        public int x;
        public int y;
    }

    private static abstract class Policy {
        abstract Entry pack(int i, int i2, Entry entry);

        private Policy() {
        }
    }

    private static class SlicePolicy extends Policy {
        private final int mPadding;
        private final Cell mRoot;
        private final SplitDecision mSplitDecision;

        private static class Cell {
            int height;
            Cell next;
            int width;
            int x;
            int y;

            private Cell() {
            }

            public String toString() {
                return String.format("cell[x=%d y=%d width=%d height=%d", new Object[]{Integer.valueOf(this.x), Integer.valueOf(this.y), Integer.valueOf(this.width), Integer.valueOf(this.height)});
            }
        }

        private interface SplitDecision {
            boolean splitHorizontal(int i, int i2, int i3, int i4);
        }

        private static class LongerFreeAxisSplitDecision implements SplitDecision {
            private LongerFreeAxisSplitDecision() {
            }

            public boolean splitHorizontal(int freeWidth, int freeHeight, int rectWidth, int rectHeight) {
                return freeWidth > freeHeight;
            }
        }

        private static class MaxAreaSplitDecision implements SplitDecision {
            private MaxAreaSplitDecision() {
            }

            public boolean splitHorizontal(int freeWidth, int freeHeight, int rectWidth, int rectHeight) {
                return rectWidth * freeHeight <= freeWidth * rectHeight;
            }
        }

        private static class MinAreaSplitDecision implements SplitDecision {
            private MinAreaSplitDecision() {
            }

            public boolean splitHorizontal(int freeWidth, int freeHeight, int rectWidth, int rectHeight) {
                return rectWidth * freeHeight > freeWidth * rectHeight;
            }
        }

        private static class ShorterFreeAxisSplitDecision implements SplitDecision {
            private ShorterFreeAxisSplitDecision() {
            }

            public boolean splitHorizontal(int freeWidth, int freeHeight, int rectWidth, int rectHeight) {
                return freeWidth <= freeHeight;
            }
        }

        SlicePolicy(int width, int height, int flags, SplitDecision splitDecision) {
            int i = 0;
            super();
            this.mRoot = new Cell();
            if ((flags & Atlas.FLAG_DEFAULTS) != 0) {
                i = 1;
            }
            this.mPadding = i;
            Cell first = new Cell();
            i = this.mPadding;
            first.y = i;
            first.x = i;
            first.width = width - (this.mPadding * Atlas.FLAG_DEFAULTS);
            first.height = height - (this.mPadding * Atlas.FLAG_DEFAULTS);
            this.mRoot.next = first;
            this.mSplitDecision = splitDecision;
        }

        Entry pack(int width, int height, Entry entry) {
            Cell prev = this.mRoot;
            for (Cell cell = this.mRoot.next; cell != null; cell = cell.next) {
                if (insert(cell, prev, width, height, entry)) {
                    return entry;
                }
                prev = cell;
            }
            return null;
        }

        private boolean insert(Cell cell, Cell prev, int width, int height, Entry entry) {
            if (cell.width < width || cell.height < height) {
                return false;
            }
            int deltaWidth = cell.width - width;
            int deltaHeight = cell.height - height;
            Cell first = new Cell();
            Cell second = new Cell();
            first.x = (cell.x + width) + this.mPadding;
            first.y = cell.y;
            first.width = deltaWidth - this.mPadding;
            second.x = cell.x;
            second.y = (cell.y + height) + this.mPadding;
            second.height = deltaHeight - this.mPadding;
            if (this.mSplitDecision.splitHorizontal(deltaWidth, deltaHeight, width, height)) {
                first.height = height;
                second.width = cell.width;
            } else {
                first.height = cell.height;
                second.width = width;
                Cell temp = first;
                Cell first2 = second;
                second = first;
                first = first2;
            }
            if (first.width > 0 && first.height > 0) {
                prev.next = first;
                prev = first;
            }
            if (second.width <= 0 || second.height <= 0) {
                prev.next = cell.next;
            } else {
                prev.next = second;
                second.next = cell.next;
            }
            cell.next = null;
            entry.x = cell.x;
            entry.y = cell.y;
            return true;
        }
    }

    public enum Type {
        ;

        static {
            /* JADX: method processing error */
/*
            Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.graphics.Atlas.Type.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:263)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.graphics.Atlas.Type.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 6 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 7 more
*/
            /*
            // Can't load method instructions.
            */
            throw new UnsupportedOperationException("Method not decompiled: android.graphics.Atlas.Type.<clinit>():void");
        }
    }

    private static /* synthetic */ int[] -getandroid-graphics-Atlas$TypeSwitchesValues() {
        if (-android-graphics-Atlas$TypeSwitchesValues != null) {
            return -android-graphics-Atlas$TypeSwitchesValues;
        }
        int[] iArr = new int[Type.values().length];
        try {
            iArr[Type.SliceLongAxis.ordinal()] = 1;
        } catch (NoSuchFieldError e) {
        }
        try {
            iArr[Type.SliceMaxArea.ordinal()] = FLAG_DEFAULTS;
        } catch (NoSuchFieldError e2) {
        }
        try {
            iArr[Type.SliceMinArea.ordinal()] = 3;
        } catch (NoSuchFieldError e3) {
        }
        try {
            iArr[Type.SliceShortAxis.ordinal()] = 4;
        } catch (NoSuchFieldError e4) {
        }
        -android-graphics-Atlas$TypeSwitchesValues = iArr;
        return iArr;
    }

    public Atlas(Type type, int width, int height) {
        this(type, width, height, FLAG_DEFAULTS);
    }

    public Atlas(Type type, int width, int height, int flags) {
        this.mType = type;
        this.mWidth = width;
        this.mHeight = height;
        this.mFlags = flags;
        this.mPolicy = findPolicy(type, width, height, flags);
    }

    public Entry pack(int width, int height) {
        return pack(width, height, null);
    }

    public Entry pack(int width, int height, Entry entry) {
        if (entry == null) {
            entry = new Entry();
        }
        if (this.mPolicy == null) {
            Log.d(TAG, "mPolicy is not set, find it again");
            this.mPolicy = findPolicy(this.mType, this.mWidth, this.mHeight, this.mFlags);
            if (this.mPolicy == null) {
                Log.d(TAG, "Also cannot find the mPolicy");
                return null;
            }
        }
        return this.mPolicy.pack(width, height, entry);
    }

    private static Policy findPolicy(Type type, int width, int height, int flags) {
        switch (-getandroid-graphics-Atlas$TypeSwitchesValues()[type.ordinal()]) {
            case AudioState.ROUTE_EARPIECE /*1*/:
                return new SlicePolicy(width, height, flags, new LongerFreeAxisSplitDecision());
            case FLAG_DEFAULTS /*2*/:
                return new SlicePolicy(width, height, flags, new MaxAreaSplitDecision());
            case Engine.DEFAULT_STREAM /*3*/:
                return new SlicePolicy(width, height, flags, new MinAreaSplitDecision());
            case AudioState.ROUTE_WIRED_HEADSET /*4*/:
                return new SlicePolicy(width, height, flags, new ShorterFreeAxisSplitDecision());
            default:
                return null;
        }
    }
}
