package android.app;

import android.app.FragmentManager.BackStackEntry;
import android.net.wifi.WifiEnterpriseConfig;
import android.util.Log;
import android.util.LogWriter;
import android.view.View;
import com.android.internal.util.FastPrintWriter;
import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.lang.reflect.Modifier;
import java.util.ArrayList;

final class BackStackRecord extends FragmentTransaction implements BackStackEntry, OpGenerator {
    static final int OP_ADD = 1;
    static final int OP_ATTACH = 7;
    static final int OP_DETACH = 6;
    static final int OP_HIDE = 4;
    static final int OP_NULL = 0;
    static final int OP_REMOVE = 3;
    static final int OP_REPLACE = 2;
    static final int OP_SET_PRIMARY_NAV = 8;
    static final int OP_SHOW = 5;
    static final int OP_UNSET_PRIMARY_NAV = 9;
    static final String TAG = "FragmentManager";
    boolean mAddToBackStack;
    boolean mAllowAddToBackStack = true;
    int mBreadCrumbShortTitleRes;
    CharSequence mBreadCrumbShortTitleText;
    int mBreadCrumbTitleRes;
    CharSequence mBreadCrumbTitleText;
    ArrayList<Runnable> mCommitRunnables;
    boolean mCommitted;
    int mEnterAnim;
    int mExitAnim;
    int mIndex = -1;
    final FragmentManagerImpl mManager;
    String mName;
    ArrayList<Op> mOps = new ArrayList();
    int mPopEnterAnim;
    int mPopExitAnim;
    boolean mReorderingAllowed;
    ArrayList<String> mSharedElementSourceNames;
    ArrayList<String> mSharedElementTargetNames;
    int mTransition;
    int mTransitionStyle;

    static final class Op {
        int cmd;
        int enterAnim;
        int exitAnim;
        Fragment fragment;
        int popEnterAnim;
        int popExitAnim;

        Op() {
        }

        Op(int cmd, Fragment fragment) {
            this.cmd = cmd;
            this.fragment = fragment;
        }
    }

    public String toString() {
        StringBuilder sb = new StringBuilder(128);
        sb.append("BackStackEntry{");
        sb.append(Integer.toHexString(System.identityHashCode(this)));
        if (this.mIndex >= 0) {
            sb.append(" #");
            sb.append(this.mIndex);
        }
        if (this.mName != null) {
            sb.append(WifiEnterpriseConfig.CA_CERT_ALIAS_DELIMITER);
            sb.append(this.mName);
        }
        sb.append("}");
        return sb.toString();
    }

    public void dump(String prefix, FileDescriptor fd, PrintWriter writer, String[] args) {
        dump(prefix, writer, true);
    }

    void dump(String prefix, PrintWriter writer, boolean full) {
        if (full) {
            writer.print(prefix);
            writer.print("mName=");
            writer.print(this.mName);
            writer.print(" mIndex=");
            writer.print(this.mIndex);
            writer.print(" mCommitted=");
            writer.println(this.mCommitted);
            if (this.mTransition != 0) {
                writer.print(prefix);
                writer.print("mTransition=#");
                writer.print(Integer.toHexString(this.mTransition));
                writer.print(" mTransitionStyle=#");
                writer.println(Integer.toHexString(this.mTransitionStyle));
            }
            if (!(this.mEnterAnim == 0 && this.mExitAnim == 0)) {
                writer.print(prefix);
                writer.print("mEnterAnim=#");
                writer.print(Integer.toHexString(this.mEnterAnim));
                writer.print(" mExitAnim=#");
                writer.println(Integer.toHexString(this.mExitAnim));
            }
            if (!(this.mPopEnterAnim == 0 && this.mPopExitAnim == 0)) {
                writer.print(prefix);
                writer.print("mPopEnterAnim=#");
                writer.print(Integer.toHexString(this.mPopEnterAnim));
                writer.print(" mPopExitAnim=#");
                writer.println(Integer.toHexString(this.mPopExitAnim));
            }
            if (!(this.mBreadCrumbTitleRes == 0 && this.mBreadCrumbTitleText == null)) {
                writer.print(prefix);
                writer.print("mBreadCrumbTitleRes=#");
                writer.print(Integer.toHexString(this.mBreadCrumbTitleRes));
                writer.print(" mBreadCrumbTitleText=");
                writer.println(this.mBreadCrumbTitleText);
            }
            if (!(this.mBreadCrumbShortTitleRes == 0 && this.mBreadCrumbShortTitleText == null)) {
                writer.print(prefix);
                writer.print("mBreadCrumbShortTitleRes=#");
                writer.print(Integer.toHexString(this.mBreadCrumbShortTitleRes));
                writer.print(" mBreadCrumbShortTitleText=");
                writer.println(this.mBreadCrumbShortTitleText);
            }
        }
        if (!this.mOps.isEmpty()) {
            writer.print(prefix);
            writer.println("Operations:");
            String innerPrefix = prefix + "    ";
            int numOps = this.mOps.size();
            for (int opNum = 0; opNum < numOps; opNum++) {
                String cmdStr;
                Op op = (Op) this.mOps.get(opNum);
                switch (op.cmd) {
                    case 0:
                        cmdStr = WifiEnterpriseConfig.EMPTY_VALUE;
                        break;
                    case 1:
                        cmdStr = "ADD";
                        break;
                    case 2:
                        cmdStr = "REPLACE";
                        break;
                    case 3:
                        cmdStr = "REMOVE";
                        break;
                    case 4:
                        cmdStr = "HIDE";
                        break;
                    case 5:
                        cmdStr = "SHOW";
                        break;
                    case 6:
                        cmdStr = "DETACH";
                        break;
                    case 7:
                        cmdStr = "ATTACH";
                        break;
                    case 8:
                        cmdStr = "SET_PRIMARY_NAV";
                        break;
                    case 9:
                        cmdStr = "UNSET_PRIMARY_NAV";
                        break;
                    default:
                        cmdStr = "cmd=" + op.cmd;
                        break;
                }
                writer.print(prefix);
                writer.print("  Op #");
                writer.print(opNum);
                writer.print(": ");
                writer.print(cmdStr);
                writer.print(WifiEnterpriseConfig.CA_CERT_ALIAS_DELIMITER);
                writer.println(op.fragment);
                if (full) {
                    if (!(op.enterAnim == 0 && op.exitAnim == 0)) {
                        writer.print(innerPrefix);
                        writer.print("enterAnim=#");
                        writer.print(Integer.toHexString(op.enterAnim));
                        writer.print(" exitAnim=#");
                        writer.println(Integer.toHexString(op.exitAnim));
                    }
                    if (op.popEnterAnim != 0 || op.popExitAnim != 0) {
                        writer.print(innerPrefix);
                        writer.print("popEnterAnim=#");
                        writer.print(Integer.toHexString(op.popEnterAnim));
                        writer.print(" popExitAnim=#");
                        writer.println(Integer.toHexString(op.popExitAnim));
                    }
                }
            }
        }
    }

    public BackStackRecord(FragmentManagerImpl manager) {
        boolean z = true;
        this.mManager = manager;
        if (this.mManager.getTargetSdk() <= 25) {
            z = false;
        }
        this.mReorderingAllowed = z;
    }

    public int getId() {
        return this.mIndex;
    }

    public int getBreadCrumbTitleRes() {
        return this.mBreadCrumbTitleRes;
    }

    public int getBreadCrumbShortTitleRes() {
        return this.mBreadCrumbShortTitleRes;
    }

    public CharSequence getBreadCrumbTitle() {
        if (this.mBreadCrumbTitleRes == 0 || this.mManager.mHost == null) {
            return this.mBreadCrumbTitleText;
        }
        return this.mManager.mHost.getContext().getText(this.mBreadCrumbTitleRes);
    }

    public CharSequence getBreadCrumbShortTitle() {
        if (this.mBreadCrumbShortTitleRes == 0 || this.mManager.mHost == null) {
            return this.mBreadCrumbShortTitleText;
        }
        return this.mManager.mHost.getContext().getText(this.mBreadCrumbShortTitleRes);
    }

    void addOp(Op op) {
        this.mOps.add(op);
        op.enterAnim = this.mEnterAnim;
        op.exitAnim = this.mExitAnim;
        op.popEnterAnim = this.mPopEnterAnim;
        op.popExitAnim = this.mPopExitAnim;
    }

    public FragmentTransaction add(Fragment fragment, String tag) {
        doAddOp(0, fragment, tag, 1);
        return this;
    }

    public FragmentTransaction add(int containerViewId, Fragment fragment) {
        doAddOp(containerViewId, fragment, null, 1);
        return this;
    }

    public FragmentTransaction add(int containerViewId, Fragment fragment, String tag) {
        doAddOp(containerViewId, fragment, tag, 1);
        return this;
    }

    private void doAddOp(int containerViewId, Fragment fragment, String tag, int opcmd) {
        if (this.mManager.getTargetSdk() > 25) {
            Class fragmentClass = fragment.getClass();
            int modifiers = fragmentClass.getModifiers();
            if (fragmentClass.isAnonymousClass() || (Modifier.isPublic(modifiers) ^ 1) != 0 || (fragmentClass.isMemberClass() && (Modifier.isStatic(modifiers) ^ 1) != 0)) {
                throw new IllegalStateException("Fragment " + fragmentClass.getCanonicalName() + " must be a public static class to be  properly recreated from" + " instance state.");
            }
        }
        fragment.mFragmentManager = this.mManager;
        if (tag != null) {
            if (fragment.mTag == null || (tag.equals(fragment.mTag) ^ 1) == 0) {
                fragment.mTag = tag;
            } else {
                throw new IllegalStateException("Can't change tag of fragment " + fragment + ": was " + fragment.mTag + " now " + tag);
            }
        }
        if (containerViewId != 0) {
            if (containerViewId == -1) {
                throw new IllegalArgumentException("Can't add fragment " + fragment + " with tag " + tag + " to container view with no id");
            } else if (fragment.mFragmentId == 0 || fragment.mFragmentId == containerViewId) {
                fragment.mFragmentId = containerViewId;
                fragment.mContainerId = containerViewId;
            } else {
                throw new IllegalStateException("Can't change container ID of fragment " + fragment + ": was " + fragment.mFragmentId + " now " + containerViewId);
            }
        }
        addOp(new Op(opcmd, fragment));
    }

    public FragmentTransaction replace(int containerViewId, Fragment fragment) {
        return replace(containerViewId, fragment, null);
    }

    public FragmentTransaction replace(int containerViewId, Fragment fragment, String tag) {
        if (containerViewId == 0) {
            throw new IllegalArgumentException("Must use non-zero containerViewId");
        }
        doAddOp(containerViewId, fragment, tag, 2);
        return this;
    }

    public FragmentTransaction remove(Fragment fragment) {
        addOp(new Op(3, fragment));
        return this;
    }

    public FragmentTransaction hide(Fragment fragment) {
        addOp(new Op(4, fragment));
        return this;
    }

    public FragmentTransaction show(Fragment fragment) {
        addOp(new Op(5, fragment));
        return this;
    }

    public FragmentTransaction detach(Fragment fragment) {
        addOp(new Op(6, fragment));
        return this;
    }

    public FragmentTransaction attach(Fragment fragment) {
        addOp(new Op(7, fragment));
        return this;
    }

    public FragmentTransaction setPrimaryNavigationFragment(Fragment fragment) {
        addOp(new Op(8, fragment));
        return this;
    }

    public FragmentTransaction setCustomAnimations(int enter, int exit) {
        return setCustomAnimations(enter, exit, 0, 0);
    }

    public FragmentTransaction setCustomAnimations(int enter, int exit, int popEnter, int popExit) {
        this.mEnterAnim = enter;
        this.mExitAnim = exit;
        this.mPopEnterAnim = popEnter;
        this.mPopExitAnim = popExit;
        return this;
    }

    public FragmentTransaction setTransition(int transition) {
        this.mTransition = transition;
        return this;
    }

    public FragmentTransaction addSharedElement(View sharedElement, String name) {
        String transitionName = sharedElement.getTransitionName();
        if (transitionName == null) {
            throw new IllegalArgumentException("Unique transitionNames are required for all sharedElements");
        }
        if (this.mSharedElementSourceNames == null) {
            this.mSharedElementSourceNames = new ArrayList();
            this.mSharedElementTargetNames = new ArrayList();
        } else if (this.mSharedElementTargetNames.contains(name)) {
            throw new IllegalArgumentException("A shared element with the target name '" + name + "' has already been added to the transaction.");
        } else if (this.mSharedElementSourceNames.contains(transitionName)) {
            throw new IllegalArgumentException("A shared element with the source name '" + transitionName + " has already been added to the transaction.");
        }
        this.mSharedElementSourceNames.add(transitionName);
        this.mSharedElementTargetNames.add(name);
        return this;
    }

    public FragmentTransaction setTransitionStyle(int styleRes) {
        this.mTransitionStyle = styleRes;
        return this;
    }

    public FragmentTransaction addToBackStack(String name) {
        if (this.mAllowAddToBackStack) {
            this.mAddToBackStack = true;
            this.mName = name;
            return this;
        }
        throw new IllegalStateException("This FragmentTransaction is not allowed to be added to the back stack.");
    }

    public boolean isAddToBackStackAllowed() {
        return this.mAllowAddToBackStack;
    }

    public FragmentTransaction disallowAddToBackStack() {
        if (this.mAddToBackStack) {
            throw new IllegalStateException("This transaction is already being added to the back stack");
        }
        this.mAllowAddToBackStack = false;
        return this;
    }

    public FragmentTransaction setBreadCrumbTitle(int res) {
        this.mBreadCrumbTitleRes = res;
        this.mBreadCrumbTitleText = null;
        return this;
    }

    public FragmentTransaction setBreadCrumbTitle(CharSequence text) {
        this.mBreadCrumbTitleRes = 0;
        this.mBreadCrumbTitleText = text;
        return this;
    }

    public FragmentTransaction setBreadCrumbShortTitle(int res) {
        this.mBreadCrumbShortTitleRes = res;
        this.mBreadCrumbShortTitleText = null;
        return this;
    }

    public FragmentTransaction setBreadCrumbShortTitle(CharSequence text) {
        this.mBreadCrumbShortTitleRes = 0;
        this.mBreadCrumbShortTitleText = text;
        return this;
    }

    void bumpBackStackNesting(int amt) {
        if (this.mAddToBackStack) {
            if (FragmentManagerImpl.DEBUG) {
                Log.v(TAG, "Bump nesting in " + this + " by " + amt);
            }
            int numOps = this.mOps.size();
            for (int opNum = 0; opNum < numOps; opNum++) {
                Op op = (Op) this.mOps.get(opNum);
                if (op.fragment != null) {
                    Fragment fragment = op.fragment;
                    fragment.mBackStackNesting += amt;
                    if (FragmentManagerImpl.DEBUG) {
                        Log.v(TAG, "Bump nesting of " + op.fragment + " to " + op.fragment.mBackStackNesting);
                    }
                }
            }
        }
    }

    public FragmentTransaction runOnCommit(Runnable runnable) {
        if (runnable == null) {
            throw new IllegalArgumentException("runnable cannot be null");
        }
        disallowAddToBackStack();
        if (this.mCommitRunnables == null) {
            this.mCommitRunnables = new ArrayList();
        }
        this.mCommitRunnables.add(runnable);
        return this;
    }

    public void runOnCommitRunnables() {
        if (this.mCommitRunnables != null) {
            int N = this.mCommitRunnables.size();
            for (int i = 0; i < N; i++) {
                ((Runnable) this.mCommitRunnables.get(i)).run();
            }
            this.mCommitRunnables = null;
        }
    }

    public int commit() {
        return commitInternal(false);
    }

    public int commitAllowingStateLoss() {
        return commitInternal(true);
    }

    public void commitNow() {
        disallowAddToBackStack();
        this.mManager.execSingleAction(this, false);
    }

    public void commitNowAllowingStateLoss() {
        disallowAddToBackStack();
        this.mManager.execSingleAction(this, true);
    }

    public FragmentTransaction setReorderingAllowed(boolean reorderingAllowed) {
        this.mReorderingAllowed = reorderingAllowed;
        return this;
    }

    int commitInternal(boolean allowStateLoss) {
        if (this.mCommitted) {
            throw new IllegalStateException("commit already called");
        }
        if (FragmentManagerImpl.DEBUG) {
            Log.v(TAG, "Commit: " + this);
            PrintWriter pw = new FastPrintWriter(new LogWriter(2, TAG), false, 1024);
            dump("  ", null, pw, null);
            pw.flush();
        }
        this.mCommitted = true;
        if (this.mAddToBackStack) {
            this.mIndex = this.mManager.allocBackStackIndex(this);
        } else {
            this.mIndex = -1;
        }
        this.mManager.enqueueAction(this, allowStateLoss);
        return this.mIndex;
    }

    public boolean generateOps(ArrayList<BackStackRecord> records, ArrayList<Boolean> isRecordPop) {
        if (FragmentManagerImpl.DEBUG) {
            Log.v(TAG, "Run: " + this);
        }
        records.add(this);
        isRecordPop.add(Boolean.valueOf(false));
        if (this.mAddToBackStack) {
            this.mManager.addBackStackState(this);
        }
        return true;
    }

    boolean interactsWith(int containerId) {
        int numOps = this.mOps.size();
        for (int opNum = 0; opNum < numOps; opNum++) {
            Op op = (Op) this.mOps.get(opNum);
            int fragContainer = op.fragment != null ? op.fragment.mContainerId : 0;
            if (fragContainer != 0 && fragContainer == containerId) {
                return true;
            }
        }
        return false;
    }

    boolean interactsWith(ArrayList<BackStackRecord> records, int startIndex, int endIndex) {
        if (endIndex == startIndex) {
            return false;
        }
        int numOps = this.mOps.size();
        int lastContainer = -1;
        for (int opNum = 0; opNum < numOps; opNum++) {
            Op op = (Op) this.mOps.get(opNum);
            int container = op.fragment != null ? op.fragment.mContainerId : 0;
            if (!(container == 0 || container == lastContainer)) {
                lastContainer = container;
                for (int i = startIndex; i < endIndex; i++) {
                    BackStackRecord record = (BackStackRecord) records.get(i);
                    int numThoseOps = record.mOps.size();
                    for (int thoseOpIndex = 0; thoseOpIndex < numThoseOps; thoseOpIndex++) {
                        Op thatOp = (Op) record.mOps.get(thoseOpIndex);
                        if ((thatOp.fragment != null ? thatOp.fragment.mContainerId : 0) == container) {
                            return true;
                        }
                    }
                }
                continue;
            }
        }
        return false;
    }

    void executeOps() {
        int numOps = this.mOps.size();
        for (int opNum = 0; opNum < numOps; opNum++) {
            Op op = (Op) this.mOps.get(opNum);
            Fragment f = op.fragment;
            if (f != null) {
                f.setNextTransition(this.mTransition, this.mTransitionStyle);
            }
            switch (op.cmd) {
                case 1:
                    f.setNextAnim(op.enterAnim);
                    this.mManager.addFragment(f, false);
                    break;
                case 3:
                    f.setNextAnim(op.exitAnim);
                    this.mManager.removeFragment(f);
                    break;
                case 4:
                    f.setNextAnim(op.exitAnim);
                    this.mManager.hideFragment(f);
                    break;
                case 5:
                    f.setNextAnim(op.enterAnim);
                    this.mManager.showFragment(f);
                    break;
                case 6:
                    f.setNextAnim(op.exitAnim);
                    this.mManager.detachFragment(f);
                    break;
                case 7:
                    f.setNextAnim(op.enterAnim);
                    this.mManager.attachFragment(f);
                    break;
                case 8:
                    this.mManager.setPrimaryNavigationFragment(f);
                    break;
                case 9:
                    this.mManager.setPrimaryNavigationFragment(null);
                    break;
                default:
                    throw new IllegalArgumentException("Unknown cmd: " + op.cmd);
            }
            if (!(this.mReorderingAllowed || op.cmd == 1 || f == null)) {
                this.mManager.moveFragmentToExpectedState(f);
            }
        }
        if (!this.mReorderingAllowed) {
            this.mManager.moveToState(this.mManager.mCurState, true);
        }
    }

    void executePopOps(boolean moveToState) {
        for (int opNum = this.mOps.size() - 1; opNum >= 0; opNum--) {
            Op op = (Op) this.mOps.get(opNum);
            Fragment f = op.fragment;
            if (f != null) {
                f.setNextTransition(FragmentManagerImpl.reverseTransit(this.mTransition), this.mTransitionStyle);
                switch (op.cmd) {
                    case 1:
                        f.setNextAnim(op.popExitAnim);
                        this.mManager.removeFragment(f);
                        break;
                    case 3:
                        f.setNextAnim(op.popEnterAnim);
                        this.mManager.addFragment(f, false);
                        break;
                    case 4:
                        f.setNextAnim(op.popEnterAnim);
                        this.mManager.showFragment(f);
                        break;
                    case 5:
                        f.setNextAnim(op.popExitAnim);
                        this.mManager.hideFragment(f);
                        break;
                    case 6:
                        f.setNextAnim(op.popEnterAnim);
                        this.mManager.attachFragment(f);
                        break;
                    case 7:
                        f.setNextAnim(op.popExitAnim);
                        this.mManager.detachFragment(f);
                        break;
                    case 8:
                        this.mManager.setPrimaryNavigationFragment(null);
                        break;
                    case 9:
                        this.mManager.setPrimaryNavigationFragment(f);
                        break;
                    default:
                        throw new IllegalArgumentException("Unknown cmd: " + op.cmd);
                }
                if (!(this.mReorderingAllowed || op.cmd == 3 || f == null)) {
                    this.mManager.moveFragmentToExpectedState(f);
                }
            }
        }
        if (!this.mReorderingAllowed && moveToState) {
            this.mManager.moveToState(this.mManager.mCurState, true);
        }
    }

    Fragment expandOps(ArrayList<Fragment> added, Fragment oldPrimaryNav) {
        int opNum = 0;
        while (opNum < this.mOps.size()) {
            Op op = (Op) this.mOps.get(opNum);
            switch (op.cmd) {
                case 1:
                case 7:
                    added.add(op.fragment);
                    break;
                case 2:
                    Fragment f = op.fragment;
                    int containerId = f.mContainerId;
                    boolean alreadyAdded = false;
                    for (int i = added.size() - 1; i >= 0; i--) {
                        Fragment old = (Fragment) added.get(i);
                        if (old.mContainerId == containerId) {
                            if (old == f) {
                                alreadyAdded = true;
                            } else {
                                if (old == oldPrimaryNav) {
                                    this.mOps.add(opNum, new Op(9, old));
                                    opNum++;
                                    oldPrimaryNav = null;
                                }
                                Op removeOp = new Op(3, old);
                                removeOp.enterAnim = op.enterAnim;
                                removeOp.popEnterAnim = op.popEnterAnim;
                                removeOp.exitAnim = op.exitAnim;
                                removeOp.popExitAnim = op.popExitAnim;
                                this.mOps.add(opNum, removeOp);
                                added.remove(old);
                                opNum++;
                            }
                        }
                    }
                    if (!alreadyAdded) {
                        op.cmd = 1;
                        added.add(f);
                        break;
                    }
                    this.mOps.remove(opNum);
                    opNum--;
                    break;
                case 3:
                case 6:
                    added.remove(op.fragment);
                    if (op.fragment != oldPrimaryNav) {
                        break;
                    }
                    this.mOps.add(opNum, new Op(9, op.fragment));
                    opNum++;
                    oldPrimaryNav = null;
                    break;
                case 8:
                    this.mOps.add(opNum, new Op(9, oldPrimaryNav));
                    opNum++;
                    oldPrimaryNav = op.fragment;
                    break;
                default:
                    break;
            }
            opNum++;
        }
        return oldPrimaryNav;
    }

    void trackAddedFragmentsInPop(ArrayList<Fragment> added) {
        for (int opNum = 0; opNum < this.mOps.size(); opNum++) {
            Op op = (Op) this.mOps.get(opNum);
            switch (op.cmd) {
                case 1:
                case 7:
                    added.remove(op.fragment);
                    break;
                case 3:
                case 6:
                    added.add(op.fragment);
                    break;
                default:
                    break;
            }
        }
    }

    boolean isPostponed() {
        for (int opNum = 0; opNum < this.mOps.size(); opNum++) {
            if (isFragmentPostponed((Op) this.mOps.get(opNum))) {
                return true;
            }
        }
        return false;
    }

    void setOnStartPostponedListener(OnStartEnterTransitionListener listener) {
        for (int opNum = 0; opNum < this.mOps.size(); opNum++) {
            Op op = (Op) this.mOps.get(opNum);
            if (isFragmentPostponed(op)) {
                op.fragment.setOnStartEnterTransitionListener(listener);
            }
        }
    }

    private static boolean isFragmentPostponed(Op op) {
        Fragment fragment = op.fragment;
        if (fragment == null || !fragment.mAdded || fragment.mView == null || (fragment.mDetached ^ 1) == 0 || (fragment.mHidden ^ 1) == 0) {
            return false;
        }
        return fragment.isPostponed();
    }

    public String getName() {
        return this.mName;
    }

    public int getTransition() {
        return this.mTransition;
    }

    public int getTransitionStyle() {
        return this.mTransitionStyle;
    }

    public boolean isEmpty() {
        return this.mOps.isEmpty();
    }
}
