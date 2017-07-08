package android_maps_conflict_avoidance.com.google.googlenav.map;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

class FlashEntry {
    static int SIZE_IN_CATALOG;
    private final int dataSize;
    private FlashRecord flashRecord;
    private final Tile tile;
    private int time;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android_maps_conflict_avoidance.com.google.googlenav.map.FlashEntry.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android_maps_conflict_avoidance.com.google.googlenav.map.FlashEntry.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: android_maps_conflict_avoidance.com.google.googlenav.map.FlashEntry.<clinit>():void");
    }

    public FlashEntry(MapTile mapTile) {
        this(mapTile.getLocation(), mapTile.getLastAccessTime(), mapTile.getDataSize());
    }

    private FlashEntry(Tile tile, long time, int dataSize) {
        this.tile = tile;
        setLastAccessTime(time);
        this.dataSize = dataSize;
    }

    public void setFlashRecord(FlashRecord newRecord) {
        if (this.flashRecord == null) {
            this.flashRecord = newRecord;
            return;
        }
        throw new IllegalStateException("FlashRecord already set");
    }

    public Tile getTile() {
        return this.tile;
    }

    public void setLastAccessTime(long time) {
        this.time = (int) ((time / 1000) - 1112219496);
    }

    public long getLastAccessTime() {
        return (((long) this.time) + 1112219496) * 1000;
    }

    public int getByteSize() {
        return this.dataSize + 12;
    }

    public String toString() {
        return this.tile.toString() + "B" + getByteSize();
    }

    public FlashRecord getFlashRecord() {
        return this.flashRecord;
    }

    public static FlashEntry readFromCatalog(DataInput is) throws IOException {
        int time = is.readInt();
        return new FlashEntry(Tile.read(is), (long) time, is.readUnsignedShort());
    }

    public void writeToCatalog(DataOutput os) throws IOException {
        os.writeInt(this.time);
        os.writeShort(this.dataSize);
        this.tile.write(os);
    }

    public boolean equals(Object o) {
        boolean z = false;
        if (this == o) {
            return true;
        }
        if (!(o instanceof FlashEntry)) {
            return false;
        }
        FlashEntry flashEntry = (FlashEntry) o;
        if (this.dataSize != flashEntry.dataSize) {
            return false;
        }
        if (this.tile != null) {
            z = this.tile.equals(flashEntry.tile);
        } else if (flashEntry.tile == null) {
            z = true;
        }
        return z;
    }

    public int hashCode() {
        return ((this.tile == null ? 0 : this.tile.hashCode()) * 29) + this.dataSize;
    }
}
