package tmsdkobf;

import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;

public interface ju {

    public static class a implements Parcelable {
        public static final Creator<a> CREATOR = new Creator<a>() {
            /* renamed from: ak */
            public a[] newArray(int i) {
                return new a[i];
            }

            /* renamed from: b */
            public a createFromParcel(Parcel parcel) {
                return a.b(parcel);
            }
        };
        public p tA;
        public c tB;
        public long ty;
        public long tz;

        public a(long j, long j2, p pVar) {
            this.ty = j;
            this.tz = j2;
            this.tA = pVar;
        }

        private static byte[] a(p pVar) {
            return pVar != null ? nn.d(pVar) : new byte[0];
        }

        private static a b(Parcel parcel) {
            long readLong = parcel.readLong();
            long readLong2 = parcel.readLong();
            int readInt = parcel.readInt();
            byte[] bArr = null;
            if (readInt > 0) {
                bArr = new byte[readInt];
                parcel.readByteArray(bArr);
            }
            a aVar = new a(readLong, readLong2, i(bArr));
            if (parcel.readByte() == (byte) 1) {
                aVar.tB = new c(parcel.readInt(), parcel.readInt());
            }
            return aVar;
        }

        private static p i(byte[] bArr) {
            return (bArr == null || bArr.length == 0) ? null : (p) nn.a(bArr, new p(), false);
        }

        public int describeContents() {
            return 0;
        }

        public void writeToParcel(Parcel parcel, int i) {
            parcel.writeLong(this.ty);
            parcel.writeLong(this.tz);
            byte[] a = a(this.tA);
            parcel.writeInt(a.length);
            if (a.length > 0) {
                parcel.writeByteArray(a);
            }
            if (this.tB == null) {
                parcel.writeByte((byte) 0);
                return;
            }
            parcel.writeByte((byte) 1);
            parcel.writeInt(this.tB.tD);
            parcel.writeInt(this.tB.tE);
        }
    }

    public static abstract class b {
        public int tC = 0;

        public abstract void b(a aVar);
    }

    public static class c {
        public int tD;
        public int tE;

        public c(int i, int i2) {
            this.tD = i;
            this.tE = i2;
        }
    }

    void C(int i);

    void a(int i, b bVar);

    void a(a aVar, int i, int i2);

    void h();

    void i();
}
