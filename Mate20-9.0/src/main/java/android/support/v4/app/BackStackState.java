package android.support.v4.app;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.v4.app.BackStackRecord;
import android.text.TextUtils;
import android.util.Log;
import java.util.ArrayList;

/* compiled from: BackStackRecord */
final class BackStackState implements Parcelable {
    public static final Parcelable.Creator<BackStackState> CREATOR = new Parcelable.Creator<BackStackState>() {
        public BackStackState createFromParcel(Parcel in) {
            return new BackStackState(in);
        }

        public BackStackState[] newArray(int size) {
            return new BackStackState[size];
        }
    };
    final int mBreadCrumbShortTitleRes;
    final CharSequence mBreadCrumbShortTitleText;
    final int mBreadCrumbTitleRes;
    final CharSequence mBreadCrumbTitleText;
    final int mIndex;
    final String mName;
    final int[] mOps;
    final boolean mReorderingAllowed;
    final ArrayList<String> mSharedElementSourceNames;
    final ArrayList<String> mSharedElementTargetNames;
    final int mTransition;
    final int mTransitionStyle;

    public BackStackState(BackStackRecord bse) {
        int numOps = bse.mOps.size();
        this.mOps = new int[(numOps * 6)];
        if (bse.mAddToBackStack) {
            int pos = 0;
            int opNum = 0;
            while (opNum < numOps) {
                BackStackRecord.Op op = bse.mOps.get(opNum);
                int pos2 = pos + 1;
                this.mOps[pos] = op.cmd;
                int pos3 = pos2 + 1;
                this.mOps[pos2] = op.fragment != null ? op.fragment.mIndex : -1;
                int pos4 = pos3 + 1;
                this.mOps[pos3] = op.enterAnim;
                int pos5 = pos4 + 1;
                this.mOps[pos4] = op.exitAnim;
                int pos6 = pos5 + 1;
                this.mOps[pos5] = op.popEnterAnim;
                this.mOps[pos6] = op.popExitAnim;
                opNum++;
                pos = pos6 + 1;
            }
            this.mTransition = bse.mTransition;
            this.mTransitionStyle = bse.mTransitionStyle;
            this.mName = bse.mName;
            this.mIndex = bse.mIndex;
            this.mBreadCrumbTitleRes = bse.mBreadCrumbTitleRes;
            this.mBreadCrumbTitleText = bse.mBreadCrumbTitleText;
            this.mBreadCrumbShortTitleRes = bse.mBreadCrumbShortTitleRes;
            this.mBreadCrumbShortTitleText = bse.mBreadCrumbShortTitleText;
            this.mSharedElementSourceNames = bse.mSharedElementSourceNames;
            this.mSharedElementTargetNames = bse.mSharedElementTargetNames;
            this.mReorderingAllowed = bse.mReorderingAllowed;
            return;
        }
        throw new IllegalStateException("Not on back stack");
    }

    public BackStackState(Parcel in) {
        this.mOps = in.createIntArray();
        this.mTransition = in.readInt();
        this.mTransitionStyle = in.readInt();
        this.mName = in.readString();
        this.mIndex = in.readInt();
        this.mBreadCrumbTitleRes = in.readInt();
        this.mBreadCrumbTitleText = (CharSequence) TextUtils.CHAR_SEQUENCE_CREATOR.createFromParcel(in);
        this.mBreadCrumbShortTitleRes = in.readInt();
        this.mBreadCrumbShortTitleText = (CharSequence) TextUtils.CHAR_SEQUENCE_CREATOR.createFromParcel(in);
        this.mSharedElementSourceNames = in.createStringArrayList();
        this.mSharedElementTargetNames = in.createStringArrayList();
        this.mReorderingAllowed = in.readInt() != 0;
    }

    public BackStackRecord instantiate(FragmentManagerImpl fm) {
        BackStackRecord bse = new BackStackRecord(fm);
        int pos = 0;
        int num = 0;
        while (pos < this.mOps.length) {
            BackStackRecord.Op op = new BackStackRecord.Op();
            int pos2 = pos + 1;
            op.cmd = this.mOps[pos];
            if (FragmentManagerImpl.DEBUG) {
                Log.v("FragmentManager", "Instantiate " + bse + " op #" + num + " base fragment #" + this.mOps[pos2]);
            }
            int pos3 = pos2 + 1;
            int findex = this.mOps[pos2];
            if (findex >= 0) {
                op.fragment = fm.mActive.get(findex);
            } else {
                op.fragment = null;
            }
            int pos4 = pos3 + 1;
            op.enterAnim = this.mOps[pos3];
            int pos5 = pos4 + 1;
            op.exitAnim = this.mOps[pos4];
            int pos6 = pos5 + 1;
            op.popEnterAnim = this.mOps[pos5];
            op.popExitAnim = this.mOps[pos6];
            bse.mEnterAnim = op.enterAnim;
            bse.mExitAnim = op.exitAnim;
            bse.mPopEnterAnim = op.popEnterAnim;
            bse.mPopExitAnim = op.popExitAnim;
            bse.addOp(op);
            num++;
            pos = pos6 + 1;
        }
        bse.mTransition = this.mTransition;
        bse.mTransitionStyle = this.mTransitionStyle;
        bse.mName = this.mName;
        bse.mIndex = this.mIndex;
        bse.mAddToBackStack = true;
        bse.mBreadCrumbTitleRes = this.mBreadCrumbTitleRes;
        bse.mBreadCrumbTitleText = this.mBreadCrumbTitleText;
        bse.mBreadCrumbShortTitleRes = this.mBreadCrumbShortTitleRes;
        bse.mBreadCrumbShortTitleText = this.mBreadCrumbShortTitleText;
        bse.mSharedElementSourceNames = this.mSharedElementSourceNames;
        bse.mSharedElementTargetNames = this.mSharedElementTargetNames;
        bse.mReorderingAllowed = this.mReorderingAllowed;
        bse.bumpBackStackNesting(1);
        return bse;
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeIntArray(this.mOps);
        dest.writeInt(this.mTransition);
        dest.writeInt(this.mTransitionStyle);
        dest.writeString(this.mName);
        dest.writeInt(this.mIndex);
        dest.writeInt(this.mBreadCrumbTitleRes);
        TextUtils.writeToParcel(this.mBreadCrumbTitleText, dest, 0);
        dest.writeInt(this.mBreadCrumbShortTitleRes);
        TextUtils.writeToParcel(this.mBreadCrumbShortTitleText, dest, 0);
        dest.writeStringList(this.mSharedElementSourceNames);
        dest.writeStringList(this.mSharedElementTargetNames);
        dest.writeInt(this.mReorderingAllowed ? 1 : 0);
    }
}
