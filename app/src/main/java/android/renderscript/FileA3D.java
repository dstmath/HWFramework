package android.renderscript;

import android.content.res.AssetManager;
import android.content.res.AssetManager.AssetInputStream;
import android.content.res.Resources;
import android.telecom.AudioState;
import java.io.File;
import java.io.InputStream;

public class FileA3D extends BaseObj {
    IndexEntry[] mFileEntries;
    InputStream mInputStream;

    public enum EntryType {
        ;
        
        int mID;

        static {
            /* JADX: method processing error */
/*
            Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.renderscript.FileA3D.EntryType.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:263)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.renderscript.FileA3D.EntryType.<clinit>():void
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
            throw new UnsupportedOperationException("Method not decompiled: android.renderscript.FileA3D.EntryType.<clinit>():void");
        }

        private EntryType(int id) {
            this.mID = id;
        }

        static EntryType toEntryType(int intID) {
            return values()[intID];
        }
    }

    public static class IndexEntry {
        private static final /* synthetic */ int[] -android-renderscript-FileA3D$EntryTypeSwitchesValues = null;
        EntryType mEntryType;
        long mID;
        int mIndex;
        BaseObj mLoadedObj;
        String mName;
        RenderScript mRS;

        private static /* synthetic */ int[] -getandroid-renderscript-FileA3D$EntryTypeSwitchesValues() {
            if (-android-renderscript-FileA3D$EntryTypeSwitchesValues != null) {
                return -android-renderscript-FileA3D$EntryTypeSwitchesValues;
            }
            int[] iArr = new int[EntryType.values().length];
            try {
                iArr[EntryType.MESH.ordinal()] = 1;
            } catch (NoSuchFieldError e) {
            }
            try {
                iArr[EntryType.UNKNOWN.ordinal()] = 2;
            } catch (NoSuchFieldError e2) {
            }
            -android-renderscript-FileA3D$EntryTypeSwitchesValues = iArr;
            return iArr;
        }

        public String getName() {
            return this.mName;
        }

        public EntryType getEntryType() {
            return this.mEntryType;
        }

        public BaseObj getObject() {
            this.mRS.validate();
            return internalCreate(this.mRS, this);
        }

        public Mesh getMesh() {
            return (Mesh) getObject();
        }

        static synchronized BaseObj internalCreate(RenderScript rs, IndexEntry entry) {
            synchronized (IndexEntry.class) {
                BaseObj baseObj;
                if (entry.mLoadedObj != null) {
                    baseObj = entry.mLoadedObj;
                    return baseObj;
                } else if (entry.mEntryType == EntryType.UNKNOWN) {
                    return null;
                } else {
                    long objectID = rs.nFileA3DGetEntryByIndex(entry.mID, entry.mIndex);
                    if (objectID == 0) {
                        return null;
                    }
                    switch (-getandroid-renderscript-FileA3D$EntryTypeSwitchesValues()[entry.mEntryType.ordinal()]) {
                        case AudioState.ROUTE_EARPIECE /*1*/:
                            entry.mLoadedObj = new Mesh(objectID, rs);
                            entry.mLoadedObj.updateFromNative();
                            baseObj = entry.mLoadedObj;
                            return baseObj;
                        default:
                            throw new RSRuntimeException("Unrecognized object type in file.");
                    }
                }
            }
        }

        IndexEntry(RenderScript rs, int index, long id, String name, EntryType type) {
            this.mRS = rs;
            this.mIndex = index;
            this.mID = id;
            this.mName = name;
            this.mEntryType = type;
            this.mLoadedObj = null;
        }
    }

    FileA3D(long id, RenderScript rs, InputStream stream) {
        super(id, rs);
        this.mInputStream = stream;
        this.guard.open("destroy");
    }

    private void initEntries() {
        int numFileEntries = this.mRS.nFileA3DGetNumIndexEntries(getID(this.mRS));
        if (numFileEntries > 0) {
            this.mFileEntries = new IndexEntry[numFileEntries];
            int[] ids = new int[numFileEntries];
            String[] names = new String[numFileEntries];
            this.mRS.nFileA3DGetIndexEntries(getID(this.mRS), numFileEntries, ids, names);
            for (int i = 0; i < numFileEntries; i++) {
                this.mFileEntries[i] = new IndexEntry(this.mRS, i, getID(this.mRS), names[i], EntryType.toEntryType(ids[i]));
            }
        }
    }

    public int getIndexEntryCount() {
        if (this.mFileEntries == null) {
            return 0;
        }
        return this.mFileEntries.length;
    }

    public IndexEntry getIndexEntry(int index) {
        if (getIndexEntryCount() == 0 || index < 0 || index >= this.mFileEntries.length) {
            return null;
        }
        return this.mFileEntries[index];
    }

    public static FileA3D createFromAsset(RenderScript rs, AssetManager mgr, String path) {
        rs.validate();
        long fileId = rs.nFileA3DCreateFromAsset(mgr, path);
        if (fileId == 0) {
            throw new RSRuntimeException("Unable to create a3d file from asset " + path);
        }
        FileA3D fa3d = new FileA3D(fileId, rs, null);
        fa3d.initEntries();
        return fa3d;
    }

    public static FileA3D createFromFile(RenderScript rs, String path) {
        long fileId = rs.nFileA3DCreateFromFile(path);
        if (fileId == 0) {
            throw new RSRuntimeException("Unable to create a3d file from " + path);
        }
        FileA3D fa3d = new FileA3D(fileId, rs, null);
        fa3d.initEntries();
        return fa3d;
    }

    public static FileA3D createFromFile(RenderScript rs, File path) {
        return createFromFile(rs, path.getAbsolutePath());
    }

    public static FileA3D createFromResource(RenderScript rs, Resources res, int id) {
        rs.validate();
        try {
            InputStream is = res.openRawResource(id);
            if (is instanceof AssetInputStream) {
                long fileId = rs.nFileA3DCreateFromAssetStream(((AssetInputStream) is).getNativeAsset());
                if (fileId == 0) {
                    throw new RSRuntimeException("Unable to create a3d file from resource " + id);
                }
                FileA3D fa3d = new FileA3D(fileId, rs, is);
                fa3d.initEntries();
                return fa3d;
            }
            throw new RSRuntimeException("Unsupported asset stream");
        } catch (Exception e) {
            throw new RSRuntimeException("Unable to open resource " + id);
        }
    }
}
