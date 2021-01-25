package android.view;

import android.os.Parcel;
import android.os.Parcelable;

public abstract class DisplayAddress implements Parcelable {
    public static Physical fromPhysicalDisplayId(long physicalDisplayId) {
        return new Physical(physicalDisplayId);
    }

    public static Network fromMacAddress(String macAddress) {
        return new Network(macAddress);
    }

    public static final class Physical extends DisplayAddress {
        public static final Parcelable.Creator<Physical> CREATOR = new Parcelable.Creator<Physical>() {
            /* class android.view.DisplayAddress.Physical.AnonymousClass1 */

            @Override // android.os.Parcelable.Creator
            public Physical createFromParcel(Parcel in) {
                return new Physical(in.readLong());
            }

            @Override // android.os.Parcelable.Creator
            public Physical[] newArray(int size) {
                return new Physical[size];
            }
        };
        private static final int MODEL_SHIFT = 8;
        private static final int PORT_MASK = 255;
        private static final long UNKNOWN_MODEL = 0;
        private final long mPhysicalDisplayId;

        public byte getPort() {
            return (byte) ((int) this.mPhysicalDisplayId);
        }

        public Long getModel() {
            long model = this.mPhysicalDisplayId >>> 8;
            if (model == 0) {
                return null;
            }
            return Long.valueOf(model);
        }

        public boolean equals(Object other) {
            return (other instanceof Physical) && this.mPhysicalDisplayId == ((Physical) other).mPhysicalDisplayId;
        }

        public String toString() {
            StringBuilder sb = new StringBuilder("{");
            sb.append("port=");
            StringBuilder builder = sb.append(getPort() & 255);
            Long model = getModel();
            if (model != null) {
                builder.append(", model=0x");
                builder.append(Long.toHexString(model.longValue()));
            }
            builder.append("}");
            return builder.toString();
        }

        public int hashCode() {
            return Long.hashCode(this.mPhysicalDisplayId);
        }

        @Override // android.os.Parcelable
        public void writeToParcel(Parcel out, int flags) {
            out.writeLong(this.mPhysicalDisplayId);
        }

        private Physical(long physicalDisplayId) {
            this.mPhysicalDisplayId = physicalDisplayId;
        }
    }

    public static final class Network extends DisplayAddress {
        public static final Parcelable.Creator<Network> CREATOR = new Parcelable.Creator<Network>() {
            /* class android.view.DisplayAddress.Network.AnonymousClass1 */

            @Override // android.os.Parcelable.Creator
            public Network createFromParcel(Parcel in) {
                return new Network(in.readString());
            }

            @Override // android.os.Parcelable.Creator
            public Network[] newArray(int size) {
                return new Network[size];
            }
        };
        private final String mMacAddress;

        public boolean equals(Object other) {
            return (other instanceof Network) && this.mMacAddress.equals(((Network) other).mMacAddress);
        }

        public String toString() {
            return this.mMacAddress;
        }

        public int hashCode() {
            return this.mMacAddress.hashCode();
        }

        @Override // android.os.Parcelable
        public void writeToParcel(Parcel out, int flags) {
            out.writeString(this.mMacAddress);
        }

        private Network(String macAddress) {
            this.mMacAddress = macAddress;
        }
    }

    @Override // android.os.Parcelable
    public int describeContents() {
        return 0;
    }
}
