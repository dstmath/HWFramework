package android.media.tv;

import android.text.TextUtils;
import com.android.internal.util.Preconditions;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public final class TvContentRating {
    private static final String DELIMITER = "/";
    public static final TvContentRating UNRATED = null;
    private final String mDomain;
    private final int mHashCode;
    private final String mRating;
    private final String mRatingSystem;
    private final String[] mSubRatings;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.media.tv.TvContentRating.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.media.tv.TvContentRating.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: android.media.tv.TvContentRating.<clinit>():void");
    }

    public static TvContentRating createRating(String domain, String ratingSystem, String rating, String... subRatings) {
        if (TextUtils.isEmpty(domain)) {
            throw new IllegalArgumentException("domain cannot be empty");
        } else if (TextUtils.isEmpty(ratingSystem)) {
            throw new IllegalArgumentException("ratingSystem cannot be empty");
        } else if (!TextUtils.isEmpty(rating)) {
            return new TvContentRating(domain, ratingSystem, rating, subRatings);
        } else {
            throw new IllegalArgumentException("rating cannot be empty");
        }
    }

    public static TvContentRating unflattenFromString(String ratingString) {
        if (TextUtils.isEmpty(ratingString)) {
            throw new IllegalArgumentException("ratingString cannot be empty");
        }
        String[] strs = ratingString.split(DELIMITER);
        if (strs.length < 3) {
            throw new IllegalArgumentException("Invalid rating string: " + ratingString);
        } else if (strs.length <= 3) {
            return new TvContentRating(strs[0], strs[1], strs[2], null);
        } else {
            String[] subRatings = new String[(strs.length - 3)];
            System.arraycopy(strs, 3, subRatings, 0, subRatings.length);
            return new TvContentRating(strs[0], strs[1], strs[2], subRatings);
        }
    }

    private TvContentRating(String domain, String ratingSystem, String rating, String[] subRatings) {
        this.mDomain = domain;
        this.mRatingSystem = ratingSystem;
        this.mRating = rating;
        if (subRatings == null || subRatings.length == 0) {
            this.mSubRatings = null;
        } else {
            Arrays.sort(subRatings);
            this.mSubRatings = subRatings;
        }
        this.mHashCode = (Objects.hash(new Object[]{this.mDomain, this.mRating}) * 31) + Arrays.hashCode(this.mSubRatings);
    }

    public String getDomain() {
        return this.mDomain;
    }

    public String getRatingSystem() {
        return this.mRatingSystem;
    }

    public String getMainRating() {
        return this.mRating;
    }

    public List<String> getSubRatings() {
        if (this.mSubRatings == null) {
            return null;
        }
        return Collections.unmodifiableList(Arrays.asList(this.mSubRatings));
    }

    public String flattenToString() {
        StringBuilder builder = new StringBuilder();
        builder.append(this.mDomain);
        builder.append(DELIMITER);
        builder.append(this.mRatingSystem);
        builder.append(DELIMITER);
        builder.append(this.mRating);
        if (this.mSubRatings != null) {
            for (String subRating : this.mSubRatings) {
                builder.append(DELIMITER);
                builder.append(subRating);
            }
        }
        return builder.toString();
    }

    public final boolean contains(TvContentRating rating) {
        Preconditions.checkNotNull(rating);
        if (!rating.getMainRating().equals(this.mRating) || !rating.getDomain().equals(this.mDomain) || !rating.getRatingSystem().equals(this.mRatingSystem) || !rating.getMainRating().equals(this.mRating)) {
            return false;
        }
        List<String> subRatings = getSubRatings();
        List<String> subRatingsOther = rating.getSubRatings();
        if (subRatings == null && subRatingsOther == null) {
            return true;
        }
        if (subRatings == null && subRatingsOther != null) {
            return false;
        }
        if (subRatings == null || subRatingsOther != null) {
            return subRatings.containsAll(subRatingsOther);
        }
        return true;
    }

    public boolean equals(Object obj) {
        if (!(obj instanceof TvContentRating)) {
            return false;
        }
        TvContentRating other = (TvContentRating) obj;
        if (this.mHashCode == other.mHashCode && TextUtils.equals(this.mDomain, other.mDomain) && TextUtils.equals(this.mRatingSystem, other.mRatingSystem) && TextUtils.equals(this.mRating, other.mRating)) {
            return Arrays.equals(this.mSubRatings, other.mSubRatings);
        }
        return false;
    }

    public int hashCode() {
        return this.mHashCode;
    }
}
