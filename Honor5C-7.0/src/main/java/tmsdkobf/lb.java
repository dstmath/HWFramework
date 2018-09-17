package tmsdkobf;

import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;

/* compiled from: Unknown */
public interface lb {

    /* compiled from: Unknown */
    public static class a implements Parcelable {
        public static final Creator<a> CREATOR = null;
        public long wv;
        public long ww;
        public m wx;
        public c wy;

        static {
            /* JADX: method processing error */
/*
            Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: tmsdkobf.lb.a.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:263)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: tmsdkobf.lb.a.<clinit>():void
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
            throw new UnsupportedOperationException("Method not decompiled: tmsdkobf.lb.a.<clinit>():void");
        }

        public a(long j, long j2, m mVar) {
            this.wv = j;
            this.ww = j2;
            this.wx = mVar;
        }

        private static byte[] a(m mVar) {
            return mVar != null ? ot.d(mVar) : new byte[0];
        }

        private static a c(Parcel parcel) {
            long readLong = parcel.readLong();
            long readLong2 = parcel.readLong();
            int readInt = parcel.readInt();
            byte[] bArr = null;
            if (readInt > 0) {
                bArr = new byte[readInt];
                parcel.readByteArray(bArr);
            }
            a aVar = new a(readLong, readLong2, i(bArr));
            if (parcel.readByte() == 1) {
                aVar.wy = new c(parcel.readInt(), parcel.readInt());
            }
            return aVar;
        }

        private static m i(byte[] bArr) {
            return (bArr == null || bArr.length == 0) ? null : (m) ot.a(bArr, new m(), false);
        }

        public int describeContents() {
            return 0;
        }

        public void writeToParcel(Parcel parcel, int i) {
            parcel.writeLong(this.wv);
            parcel.writeLong(this.ww);
            byte[] a = a(this.wx);
            parcel.writeInt(a.length);
            if (a.length > 0) {
                parcel.writeByteArray(a);
            }
            if (this.wy == null) {
                parcel.writeByte((byte) 0);
                return;
            }
            parcel.writeByte((byte) 1);
            parcel.writeInt(this.wy.wA);
            parcel.writeInt(this.wy.wB);
        }
    }

    /* compiled from: Unknown */
    public static abstract class b {
        public int wz;

        public b() {
            this.wz = 0;
        }

        public abstract void a(a aVar);
    }

    /* compiled from: Unknown */
    public static class c {
        public int wA;
        public int wB;

        public c(int i, int i2) {
            this.wA = i;
            this.wB = i2;
        }
    }

    void a(int i, b bVar);

    void a(a aVar, int i, int i2);

    void ac(int i);
}
