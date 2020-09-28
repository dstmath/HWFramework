package android.service.autofill;

import android.os.Parcel;
import android.view.autofill.Helper;
import com.android.internal.util.Preconditions;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public final class FieldClassification {
    private final ArrayList<Match> mMatches;

    public FieldClassification(ArrayList<Match> matches) {
        this.mMatches = (ArrayList) Preconditions.checkNotNull(matches);
        Collections.sort(this.mMatches, new Comparator<Match>() {
            /* class android.service.autofill.FieldClassification.AnonymousClass1 */

            public int compare(Match o1, Match o2) {
                if (o1.mScore > o2.mScore) {
                    return -1;
                }
                if (o1.mScore < o2.mScore) {
                    return 1;
                }
                return 0;
            }
        });
    }

    public List<Match> getMatches() {
        return this.mMatches;
    }

    public String toString() {
        if (!Helper.sDebug) {
            return super.toString();
        }
        return "FieldClassification: " + this.mMatches;
    }

    private void writeToParcel(Parcel parcel) {
        parcel.writeInt(this.mMatches.size());
        for (int i = 0; i < this.mMatches.size(); i++) {
            this.mMatches.get(i).writeToParcel(parcel);
        }
    }

    private static FieldClassification readFromParcel(Parcel parcel) {
        int size = parcel.readInt();
        ArrayList<Match> matches = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            matches.add(i, Match.readFromParcel(parcel));
        }
        return new FieldClassification(matches);
    }

    static FieldClassification[] readArrayFromParcel(Parcel parcel) {
        int length = parcel.readInt();
        FieldClassification[] fcs = new FieldClassification[length];
        for (int i = 0; i < length; i++) {
            fcs[i] = readFromParcel(parcel);
        }
        return fcs;
    }

    static void writeArrayToParcel(Parcel parcel, FieldClassification[] fcs) {
        parcel.writeInt(fcs.length);
        for (FieldClassification fieldClassification : fcs) {
            fieldClassification.writeToParcel(parcel);
        }
    }

    public static final class Match {
        private final String mCategoryId;
        private final float mScore;

        public Match(String categoryId, float score) {
            this.mCategoryId = (String) Preconditions.checkNotNull(categoryId);
            this.mScore = score;
        }

        public String getCategoryId() {
            return this.mCategoryId;
        }

        public float getScore() {
            return this.mScore;
        }

        public String toString() {
            if (!Helper.sDebug) {
                return super.toString();
            }
            StringBuilder string = new StringBuilder("Match: categoryId=");
            Helper.appendRedacted(string, this.mCategoryId);
            string.append(", score=");
            string.append(this.mScore);
            return string.toString();
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private void writeToParcel(Parcel parcel) {
            parcel.writeString(this.mCategoryId);
            parcel.writeFloat(this.mScore);
        }

        /* access modifiers changed from: private */
        public static Match readFromParcel(Parcel parcel) {
            return new Match(parcel.readString(), parcel.readFloat());
        }
    }
}
