package android_maps_conflict_avoidance.com.google.common.io.protocol;

import android_maps_conflict_avoidance.com.google.common.util.IntMap;

public class ProtoBufType {
    private static final TypeInfo[] NULL_DATA_TYPEINFOS = null;
    private final String typeName;
    private final IntMap types;

    static class TypeInfo {
        private Object data;
        private int type;

        TypeInfo(int t, Object d) {
            this.type = t;
            this.data = d;
        }

        public int hashCode() {
            return this.type;
        }

        public boolean equals(Object obj) {
            boolean z = true;
            if (this == obj) {
                return true;
            }
            if (obj == null || !(obj instanceof TypeInfo)) {
                return false;
            }
            TypeInfo peerTypeInfo = (TypeInfo) obj;
            if (this.type == peerTypeInfo.type) {
                if (this.data != peerTypeInfo.data) {
                    if (this.data != null) {
                        if (!this.data.equals(peerTypeInfo.data)) {
                        }
                    }
                }
                return z;
            }
            z = false;
            return z;
        }

        public String toString() {
            return "TypeInfo{type=" + this.type + ", data=" + this.data + "}";
        }
    }

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android_maps_conflict_avoidance.com.google.common.io.protocol.ProtoBufType.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android_maps_conflict_avoidance.com.google.common.io.protocol.ProtoBufType.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: android_maps_conflict_avoidance.com.google.common.io.protocol.ProtoBufType.<clinit>():void");
    }

    public ProtoBufType() {
        this.types = new IntMap();
        this.typeName = null;
    }

    public ProtoBufType(String typeName) {
        this.types = new IntMap();
        this.typeName = typeName;
    }

    private static TypeInfo getCacheTypeInfoForNullData(int optionsAndType) {
        return NULL_DATA_TYPEINFOS[(((65280 & optionsAndType) >> 8) * 21) + ((optionsAndType & 255) - 16)];
    }

    public ProtoBufType addElement(int optionsAndType, int tag, Object data) {
        this.types.put(tag, data != null ? new TypeInfo(optionsAndType, data) : getCacheTypeInfoForNullData(optionsAndType));
        return this;
    }

    public int getType(int tag) {
        TypeInfo typeInfo = (TypeInfo) this.types.get(tag);
        return typeInfo != null ? typeInfo.type & 255 : 16;
    }

    public Object getData(int tag) {
        TypeInfo typeInfo = (TypeInfo) this.types.get(tag);
        return typeInfo != null ? typeInfo.data : typeInfo;
    }

    public String toString() {
        return "ProtoBufType Name: " + this.typeName;
    }

    public boolean equals(Object object) {
        if (object == null) {
            return false;
        }
        if (this == object) {
            return true;
        }
        if (getClass() != object.getClass()) {
            return false;
        }
        return this.types.equals(((ProtoBufType) object).types);
    }

    public int hashCode() {
        if (this.types == null) {
            return super.hashCode();
        }
        return this.types.hashCode();
    }
}
