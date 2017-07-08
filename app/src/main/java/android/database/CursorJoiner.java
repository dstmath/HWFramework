package android.database;

import android.speech.tts.TextToSpeech;
import android.speech.tts.TextToSpeech.Engine;
import android.telecom.AudioState;
import java.util.Iterator;

public final class CursorJoiner implements Iterator<Result>, Iterable<Result> {
    private static final /* synthetic */ int[] -android-database-CursorJoiner$ResultSwitchesValues = null;
    static final /* synthetic */ boolean -assertionsDisabled = false;
    private int[] mColumnsLeft;
    private int[] mColumnsRight;
    private Result mCompareResult;
    private boolean mCompareResultIsValid;
    private Cursor mCursorLeft;
    private Cursor mCursorRight;
    private String[] mValues;

    public enum Result {
        ;

        static {
            /* JADX: method processing error */
/*
            Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.database.CursorJoiner.Result.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:263)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.database.CursorJoiner.Result.<clinit>():void
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
            throw new UnsupportedOperationException("Method not decompiled: android.database.CursorJoiner.Result.<clinit>():void");
        }
    }

    private static /* synthetic */ int[] -getandroid-database-CursorJoiner$ResultSwitchesValues() {
        if (-android-database-CursorJoiner$ResultSwitchesValues != null) {
            return -android-database-CursorJoiner$ResultSwitchesValues;
        }
        int[] iArr = new int[Result.values().length];
        try {
            iArr[Result.BOTH.ordinal()] = 1;
        } catch (NoSuchFieldError e) {
        }
        try {
            iArr[Result.LEFT.ordinal()] = 2;
        } catch (NoSuchFieldError e2) {
        }
        try {
            iArr[Result.RIGHT.ordinal()] = 3;
        } catch (NoSuchFieldError e3) {
        }
        -android-database-CursorJoiner$ResultSwitchesValues = iArr;
        return iArr;
    }

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.database.CursorJoiner.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.database.CursorJoiner.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 5 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e9
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 6 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: android.database.CursorJoiner.<clinit>():void");
    }

    public CursorJoiner(Cursor cursorLeft, String[] columnNamesLeft, Cursor cursorRight, String[] columnNamesRight) {
        if (columnNamesLeft.length != columnNamesRight.length) {
            throw new IllegalArgumentException("you must have the same number of columns on the left and right, " + columnNamesLeft.length + " != " + columnNamesRight.length);
        }
        this.mCursorLeft = cursorLeft;
        this.mCursorRight = cursorRight;
        this.mCursorLeft.moveToFirst();
        this.mCursorRight.moveToFirst();
        this.mCompareResultIsValid = false;
        this.mColumnsLeft = buildColumnIndiciesArray(cursorLeft, columnNamesLeft);
        this.mColumnsRight = buildColumnIndiciesArray(cursorRight, columnNamesRight);
        this.mValues = new String[(this.mColumnsLeft.length * 2)];
    }

    public Iterator<Result> iterator() {
        return this;
    }

    private int[] buildColumnIndiciesArray(Cursor cursor, String[] columnNames) {
        int[] columns = new int[columnNames.length];
        for (int i = 0; i < columnNames.length; i++) {
            columns[i] = cursor.getColumnIndexOrThrow(columnNames[i]);
        }
        return columns;
    }

    public boolean hasNext() {
        boolean z = true;
        if (this.mCompareResultIsValid) {
            switch (-getandroid-database-CursorJoiner$ResultSwitchesValues()[this.mCompareResult.ordinal()]) {
                case AudioState.ROUTE_EARPIECE /*1*/:
                    if (this.mCursorLeft.isLast() && this.mCursorRight.isLast()) {
                        z = false;
                    }
                    return z;
                case AudioState.ROUTE_BLUETOOTH /*2*/:
                    if (this.mCursorLeft.isLast() && this.mCursorRight.isAfterLast()) {
                        z = false;
                    }
                    return z;
                case Engine.DEFAULT_STREAM /*3*/:
                    if (this.mCursorLeft.isAfterLast() && this.mCursorRight.isLast()) {
                        z = false;
                    }
                    return z;
                default:
                    throw new IllegalStateException("bad value for mCompareResult, " + this.mCompareResult);
            }
        }
        if (this.mCursorLeft.isAfterLast() && this.mCursorRight.isAfterLast()) {
            z = false;
        }
        return z;
    }

    public /* bridge */ /* synthetic */ Object m66next() {
        return next();
    }

    public Result next() {
        if (hasNext()) {
            incrementCursors();
            if (-assertionsDisabled || hasNext()) {
                boolean hasLeft = !this.mCursorLeft.isAfterLast();
                boolean hasRight = !this.mCursorRight.isAfterLast();
                if (hasLeft && hasRight) {
                    populateValues(this.mValues, this.mCursorLeft, this.mColumnsLeft, 0);
                    populateValues(this.mValues, this.mCursorRight, this.mColumnsRight, 1);
                    switch (compareStrings(this.mValues)) {
                        case TextToSpeech.LANG_MISSING_DATA /*-1*/:
                            this.mCompareResult = Result.LEFT;
                            break;
                        case TextToSpeech.SUCCESS /*0*/:
                            this.mCompareResult = Result.BOTH;
                            break;
                        case AudioState.ROUTE_EARPIECE /*1*/:
                            this.mCompareResult = Result.RIGHT;
                            break;
                    }
                } else if (hasLeft) {
                    this.mCompareResult = Result.LEFT;
                } else if (-assertionsDisabled || hasRight) {
                    this.mCompareResult = Result.RIGHT;
                } else {
                    throw new AssertionError();
                }
                this.mCompareResultIsValid = true;
                return this.mCompareResult;
            }
            throw new AssertionError();
        }
        throw new IllegalStateException("you must only call next() when hasNext() is true");
    }

    public void remove() {
        throw new UnsupportedOperationException("not implemented");
    }

    private static void populateValues(String[] values, Cursor cursor, int[] columnIndicies, int startingIndex) {
        Object obj = 1;
        if (!-assertionsDisabled) {
            if (!(startingIndex == 0 || startingIndex == 1)) {
                obj = null;
            }
            if (obj == null) {
                throw new AssertionError();
            }
        }
        for (int i = 0; i < columnIndicies.length; i++) {
            values[(i * 2) + startingIndex] = cursor.getString(columnIndicies[i]);
        }
    }

    private void incrementCursors() {
        if (this.mCompareResultIsValid) {
            switch (-getandroid-database-CursorJoiner$ResultSwitchesValues()[this.mCompareResult.ordinal()]) {
                case AudioState.ROUTE_EARPIECE /*1*/:
                    this.mCursorLeft.moveToNext();
                    this.mCursorRight.moveToNext();
                    break;
                case AudioState.ROUTE_BLUETOOTH /*2*/:
                    this.mCursorLeft.moveToNext();
                    break;
                case Engine.DEFAULT_STREAM /*3*/:
                    this.mCursorRight.moveToNext();
                    break;
            }
            this.mCompareResultIsValid = false;
        }
    }

    private static int compareStrings(String... values) {
        int i = -1;
        if (values.length % 2 != 0) {
            throw new IllegalArgumentException("you must specify an even number of values");
        }
        for (int index = 0; index < values.length; index += 2) {
            if (values[index] == null) {
                if (values[index + 1] != null) {
                    return -1;
                }
            } else if (values[index + 1] == null) {
                return 1;
            } else {
                int comp = values[index].compareTo(values[index + 1]);
                if (comp != 0) {
                    if (comp >= 0) {
                        i = 1;
                    }
                    return i;
                }
            }
        }
        return 0;
    }
}
