package android.net;

import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import java.util.Objects;

public class ScoredNetwork implements Parcelable {
    public static final String ATTRIBUTES_KEY_BADGING_CURVE = "android.net.attributes.key.BADGING_CURVE";
    public static final String ATTRIBUTES_KEY_HAS_CAPTIVE_PORTAL = "android.net.attributes.key.HAS_CAPTIVE_PORTAL";
    public static final String ATTRIBUTES_KEY_RANKING_SCORE_OFFSET = "android.net.attributes.key.RANKING_SCORE_OFFSET";
    public static final Creator<ScoredNetwork> CREATOR = new Creator<ScoredNetwork>() {
        public ScoredNetwork createFromParcel(Parcel in) {
            return new ScoredNetwork(in, null);
        }

        public ScoredNetwork[] newArray(int size) {
            return new ScoredNetwork[size];
        }
    };
    public final Bundle attributes;
    public final boolean meteredHint;
    public final NetworkKey networkKey;
    public final RssiCurve rssiCurve;

    public ScoredNetwork(NetworkKey networkKey, RssiCurve rssiCurve) {
        this(networkKey, rssiCurve, false);
    }

    public ScoredNetwork(NetworkKey networkKey, RssiCurve rssiCurve, boolean meteredHint) {
        this(networkKey, rssiCurve, meteredHint, null);
    }

    public ScoredNetwork(NetworkKey networkKey, RssiCurve rssiCurve, boolean meteredHint, Bundle attributes) {
        this.networkKey = networkKey;
        this.rssiCurve = rssiCurve;
        this.meteredHint = meteredHint;
        this.attributes = attributes;
    }

    private ScoredNetwork(Parcel in) {
        this.networkKey = (NetworkKey) NetworkKey.CREATOR.createFromParcel(in);
        if (in.readByte() == (byte) 1) {
            this.rssiCurve = (RssiCurve) RssiCurve.CREATOR.createFromParcel(in);
        } else {
            this.rssiCurve = null;
        }
        this.meteredHint = in.readByte() == (byte) 1;
        this.attributes = in.readBundle();
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel out, int flags) {
        int i = 1;
        this.networkKey.writeToParcel(out, flags);
        if (this.rssiCurve != null) {
            out.writeByte((byte) 1);
            this.rssiCurve.writeToParcel(out, flags);
        } else {
            out.writeByte((byte) 0);
        }
        if (!this.meteredHint) {
            i = 0;
        }
        out.writeByte((byte) i);
        out.writeBundle(this.attributes);
    }

    public boolean equals(Object o) {
        boolean z = false;
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ScoredNetwork that = (ScoredNetwork) o;
        if (Objects.equals(this.networkKey, that.networkKey) && Objects.equals(this.rssiCurve, that.rssiCurve) && Objects.equals(Boolean.valueOf(this.meteredHint), Boolean.valueOf(that.meteredHint))) {
            z = Objects.equals(this.attributes, that.attributes);
        }
        return z;
    }

    public int hashCode() {
        return Objects.hash(new Object[]{this.networkKey, this.rssiCurve, Boolean.valueOf(this.meteredHint), this.attributes});
    }

    public String toString() {
        StringBuilder out = new StringBuilder("ScoredNetwork{networkKey=" + this.networkKey + ", rssiCurve=" + this.rssiCurve + ", meteredHint=" + this.meteredHint);
        if (!(this.attributes == null || (this.attributes.isEmpty() ^ 1) == 0)) {
            out.append(", attributes=").append(this.attributes);
        }
        out.append('}');
        return out.toString();
    }

    public boolean hasRankingScore() {
        if (this.rssiCurve != null) {
            return true;
        }
        if (this.attributes != null) {
            return this.attributes.containsKey(ATTRIBUTES_KEY_RANKING_SCORE_OFFSET);
        }
        return false;
    }

    public int calculateRankingScore(int rssi) throws UnsupportedOperationException {
        if (hasRankingScore()) {
            int offset = 0;
            if (this.attributes != null) {
                offset = this.attributes.getInt(ATTRIBUTES_KEY_RANKING_SCORE_OFFSET, 0) + 0;
            }
            int score = this.rssiCurve == null ? 0 : this.rssiCurve.lookupScore(rssi) << 8;
            try {
                return Math.addExact(score, offset);
            } catch (ArithmeticException e) {
                return score < 0 ? Integer.MIN_VALUE : Integer.MAX_VALUE;
            }
        }
        throw new UnsupportedOperationException("Either rssiCurve or rankingScoreOffset is required to calculate the ranking score");
    }

    public int calculateBadge(int rssi) {
        if (this.attributes == null || !this.attributes.containsKey(ATTRIBUTES_KEY_BADGING_CURVE)) {
            return 0;
        }
        return ((RssiCurve) this.attributes.getParcelable(ATTRIBUTES_KEY_BADGING_CURVE)).lookupScore(rssi);
    }
}
