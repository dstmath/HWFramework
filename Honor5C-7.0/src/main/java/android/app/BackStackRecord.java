package android.app;

import android.app.FragmentManager.BackStackEntry;
import android.graphics.Rect;
import android.net.wifi.WifiEnterpriseConfig;
import android.provider.DocumentsContract.Document;
import android.security.keymaster.KeymasterDefs;
import android.transition.Transition;
import android.transition.Transition.EpicenterCallback;
import android.transition.TransitionManager;
import android.transition.TransitionSet;
import android.util.ArrayMap;
import android.util.Log;
import android.util.LogWriter;
import android.util.SparseArray;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver.OnPreDrawListener;
import com.android.internal.util.FastPrintWriter;
import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

final class BackStackRecord extends FragmentTransaction implements BackStackEntry, Runnable {
    static final int OP_ADD = 1;
    static final int OP_ATTACH = 7;
    static final int OP_DETACH = 6;
    static final int OP_HIDE = 4;
    static final int OP_NULL = 0;
    static final int OP_REMOVE = 3;
    static final int OP_REPLACE = 2;
    static final int OP_SHOW = 5;
    static final String TAG = "FragmentManager";
    boolean mAddToBackStack;
    boolean mAllowAddToBackStack;
    int mBreadCrumbShortTitleRes;
    CharSequence mBreadCrumbShortTitleText;
    int mBreadCrumbTitleRes;
    CharSequence mBreadCrumbTitleText;
    boolean mCommitted;
    int mEnterAnim;
    int mExitAnim;
    Op mHead;
    int mIndex;
    final FragmentManagerImpl mManager;
    String mName;
    int mNumOp;
    int mPopEnterAnim;
    int mPopExitAnim;
    ArrayList<String> mSharedElementSourceNames;
    ArrayList<String> mSharedElementTargetNames;
    Op mTail;
    int mTransition;
    int mTransitionStyle;

    /* renamed from: android.app.BackStackRecord.1 */
    class AnonymousClass1 implements OnPreDrawListener {
        final /* synthetic */ View val$container;
        final /* synthetic */ Transition val$enterTransition;
        final /* synthetic */ ArrayList val$enteringViews;
        final /* synthetic */ Transition val$exitTransition;
        final /* synthetic */ ArrayList val$hiddenFragmentViews;
        final /* synthetic */ Fragment val$inFragment;
        final /* synthetic */ boolean val$isBack;
        final /* synthetic */ Fragment val$outFragment;
        final /* synthetic */ Transition val$overallTransition;
        final /* synthetic */ ArrayList val$sharedElementTargets;
        final /* synthetic */ TransitionSet val$sharedElementTransition;
        final /* synthetic */ TransitionState val$state;

        AnonymousClass1(View val$container, Fragment val$inFragment, ArrayList val$hiddenFragmentViews, Transition val$overallTransition, TransitionSet val$sharedElementTransition, TransitionState val$state, boolean val$isBack, ArrayList val$sharedElementTargets, Transition val$exitTransition, Transition val$enterTransition, Fragment val$outFragment, ArrayList val$enteringViews) {
            this.val$container = val$container;
            this.val$inFragment = val$inFragment;
            this.val$hiddenFragmentViews = val$hiddenFragmentViews;
            this.val$overallTransition = val$overallTransition;
            this.val$sharedElementTransition = val$sharedElementTransition;
            this.val$state = val$state;
            this.val$isBack = val$isBack;
            this.val$sharedElementTargets = val$sharedElementTargets;
            this.val$exitTransition = val$exitTransition;
            this.val$enterTransition = val$enterTransition;
            this.val$outFragment = val$outFragment;
            this.val$enteringViews = val$enteringViews;
        }

        public boolean onPreDraw() {
            this.val$container.getViewTreeObserver().removeOnPreDrawListener(this);
            if (this.val$inFragment != null) {
                BackStackRecord.this.excludeHiddenFragments(this.val$hiddenFragmentViews, this.val$inFragment.mContainerId, this.val$overallTransition);
            }
            ArrayMap arrayMap = null;
            if (this.val$sharedElementTransition != null) {
                arrayMap = BackStackRecord.this.mapSharedElementsIn(this.val$state, this.val$isBack, this.val$inFragment);
                BackStackRecord.removeTargets(this.val$sharedElementTransition, this.val$sharedElementTargets);
                this.val$sharedElementTargets.remove(this.val$state.nonExistentView);
                BackStackRecord.excludeViews(this.val$exitTransition, this.val$sharedElementTransition, this.val$sharedElementTargets, false);
                BackStackRecord.excludeViews(this.val$enterTransition, this.val$sharedElementTransition, this.val$sharedElementTargets, false);
                BackStackRecord.setSharedElementTargets(this.val$sharedElementTransition, this.val$state.nonExistentView, arrayMap, this.val$sharedElementTargets);
                BackStackRecord.this.setEpicenterIn(arrayMap, this.val$state);
                BackStackRecord.this.callSharedElementEnd(this.val$state, this.val$inFragment, this.val$outFragment, this.val$isBack, arrayMap);
            }
            if (this.val$enterTransition != null) {
                this.val$enterTransition.removeTarget(this.val$state.nonExistentView);
                View view = this.val$inFragment.getView();
                if (view != null) {
                    view.captureTransitioningViews(this.val$enteringViews);
                    if (arrayMap != null) {
                        this.val$enteringViews.removeAll(arrayMap.values());
                    }
                    this.val$enteringViews.add(this.val$state.nonExistentView);
                    BackStackRecord.addTargets(this.val$enterTransition, this.val$enteringViews);
                }
                BackStackRecord.this.setSharedElementEpicenter(this.val$enterTransition, this.val$state);
            }
            BackStackRecord.excludeViews(this.val$exitTransition, this.val$enterTransition, this.val$enteringViews, true);
            BackStackRecord.excludeViews(this.val$exitTransition, this.val$sharedElementTransition, this.val$sharedElementTargets, true);
            BackStackRecord.excludeViews(this.val$enterTransition, this.val$sharedElementTransition, this.val$sharedElementTargets, true);
            return true;
        }
    }

    /* renamed from: android.app.BackStackRecord.2 */
    class AnonymousClass2 implements OnPreDrawListener {
        final /* synthetic */ Transition val$enterTransition;
        final /* synthetic */ ArrayList val$enteringViews;
        final /* synthetic */ Transition val$exitTransition;
        final /* synthetic */ ArrayList val$exitingViews;
        final /* synthetic */ ArrayList val$hiddenViews;
        final /* synthetic */ View val$nonExistingView;
        final /* synthetic */ Transition val$overallTransition;
        final /* synthetic */ ViewGroup val$sceneRoot;
        final /* synthetic */ ArrayList val$sharedElementTargets;
        final /* synthetic */ Transition val$sharedElementTransition;

        AnonymousClass2(ViewGroup val$sceneRoot, Transition val$enterTransition, ArrayList val$enteringViews, Transition val$exitTransition, ArrayList val$exitingViews, Transition val$sharedElementTransition, ArrayList val$sharedElementTargets, ArrayList val$hiddenViews, Transition val$overallTransition, View val$nonExistingView) {
            this.val$sceneRoot = val$sceneRoot;
            this.val$enterTransition = val$enterTransition;
            this.val$enteringViews = val$enteringViews;
            this.val$exitTransition = val$exitTransition;
            this.val$exitingViews = val$exitingViews;
            this.val$sharedElementTransition = val$sharedElementTransition;
            this.val$sharedElementTargets = val$sharedElementTargets;
            this.val$hiddenViews = val$hiddenViews;
            this.val$overallTransition = val$overallTransition;
            this.val$nonExistingView = val$nonExistingView;
        }

        public boolean onPreDraw() {
            this.val$sceneRoot.getViewTreeObserver().removeOnPreDrawListener(this);
            if (this.val$enterTransition != null) {
                BackStackRecord.removeTargets(this.val$enterTransition, this.val$enteringViews);
                BackStackRecord.excludeViews(this.val$enterTransition, this.val$exitTransition, this.val$exitingViews, false);
                BackStackRecord.excludeViews(this.val$enterTransition, this.val$sharedElementTransition, this.val$sharedElementTargets, false);
            }
            if (this.val$exitTransition != null) {
                BackStackRecord.removeTargets(this.val$exitTransition, this.val$exitingViews);
                BackStackRecord.excludeViews(this.val$exitTransition, this.val$enterTransition, this.val$enteringViews, false);
                BackStackRecord.excludeViews(this.val$exitTransition, this.val$sharedElementTransition, this.val$sharedElementTargets, false);
            }
            if (this.val$sharedElementTransition != null) {
                BackStackRecord.removeTargets(this.val$sharedElementTransition, this.val$sharedElementTargets);
            }
            int numViews = this.val$hiddenViews.size();
            for (int i = BackStackRecord.OP_NULL; i < numViews; i += BackStackRecord.OP_ADD) {
                this.val$overallTransition.excludeTarget((View) this.val$hiddenViews.get(i), false);
            }
            this.val$overallTransition.excludeTarget(this.val$nonExistingView, false);
            return true;
        }
    }

    /* renamed from: android.app.BackStackRecord.3 */
    static class AnonymousClass3 extends EpicenterCallback {
        final /* synthetic */ Rect val$epicenter;

        AnonymousClass3(Rect val$epicenter) {
            this.val$epicenter = val$epicenter;
        }

        public Rect onGetEpicenter(Transition transition) {
            return this.val$epicenter;
        }
    }

    /* renamed from: android.app.BackStackRecord.4 */
    class AnonymousClass4 extends EpicenterCallback {
        private Rect mEpicenter;
        final /* synthetic */ TransitionState val$state;

        AnonymousClass4(TransitionState val$state) {
            this.val$state = val$state;
        }

        public Rect onGetEpicenter(Transition transition) {
            if (this.mEpicenter == null && this.val$state.enteringEpicenterView != null) {
                this.mEpicenter = new Rect();
                this.val$state.enteringEpicenterView.getBoundsOnScreen(this.mEpicenter);
            }
            return this.mEpicenter;
        }
    }

    static final class Op {
        int cmd;
        int enterAnim;
        int exitAnim;
        Fragment fragment;
        Op next;
        int popEnterAnim;
        int popExitAnim;
        Op prev;
        ArrayList<Fragment> removed;

        Op() {
        }
    }

    public class TransitionState {
        public View enteringEpicenterView;
        public ArrayMap<String, String> nameOverrides;
        public View nonExistentView;

        public TransitionState() {
            this.nameOverrides = new ArrayMap();
        }
    }

    public String toString() {
        StringBuilder sb = new StringBuilder(KeymasterDefs.KM_ALGORITHM_HMAC);
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
        if (this.mHead != null) {
            writer.print(prefix);
            writer.println("Operations:");
            String innerPrefix = prefix + "    ";
            Op op = this.mHead;
            int num = OP_NULL;
            while (op != null) {
                String cmdStr;
                switch (op.cmd) {
                    case OP_NULL /*0*/:
                        cmdStr = WifiEnterpriseConfig.EMPTY_VALUE;
                        break;
                    case OP_ADD /*1*/:
                        cmdStr = "ADD";
                        break;
                    case OP_REPLACE /*2*/:
                        cmdStr = "REPLACE";
                        break;
                    case OP_REMOVE /*3*/:
                        cmdStr = "REMOVE";
                        break;
                    case OP_HIDE /*4*/:
                        cmdStr = "HIDE";
                        break;
                    case OP_SHOW /*5*/:
                        cmdStr = "SHOW";
                        break;
                    case OP_DETACH /*6*/:
                        cmdStr = "DETACH";
                        break;
                    case OP_ATTACH /*7*/:
                        cmdStr = "ATTACH";
                        break;
                    default:
                        cmdStr = "cmd=" + op.cmd;
                        break;
                }
                writer.print(prefix);
                writer.print("  Op #");
                writer.print(num);
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
                    if (!(op.popEnterAnim == 0 && op.popExitAnim == 0)) {
                        writer.print(innerPrefix);
                        writer.print("popEnterAnim=#");
                        writer.print(Integer.toHexString(op.popEnterAnim));
                        writer.print(" popExitAnim=#");
                        writer.println(Integer.toHexString(op.popExitAnim));
                    }
                }
                if (op.removed != null && op.removed.size() > 0) {
                    for (int i = OP_NULL; i < op.removed.size(); i += OP_ADD) {
                        writer.print(innerPrefix);
                        if (op.removed.size() == OP_ADD) {
                            writer.print("Removed: ");
                        } else {
                            if (i == 0) {
                                writer.println("Removed:");
                            }
                            writer.print(innerPrefix);
                            writer.print("  #");
                            writer.print(i);
                            writer.print(": ");
                        }
                        writer.println(op.removed.get(i));
                    }
                }
                op = op.next;
                num += OP_ADD;
            }
        }
    }

    public BackStackRecord(FragmentManagerImpl manager) {
        this.mAllowAddToBackStack = true;
        this.mIndex = -1;
        this.mManager = manager;
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
        if (this.mBreadCrumbTitleRes != 0) {
            return this.mManager.mHost.getContext().getText(this.mBreadCrumbTitleRes);
        }
        return this.mBreadCrumbTitleText;
    }

    public CharSequence getBreadCrumbShortTitle() {
        if (this.mBreadCrumbShortTitleRes != 0) {
            return this.mManager.mHost.getContext().getText(this.mBreadCrumbShortTitleRes);
        }
        return this.mBreadCrumbShortTitleText;
    }

    void addOp(Op op) {
        if (this.mHead == null) {
            this.mTail = op;
            this.mHead = op;
        } else {
            op.prev = this.mTail;
            this.mTail.next = op;
            this.mTail = op;
        }
        op.enterAnim = this.mEnterAnim;
        op.exitAnim = this.mExitAnim;
        op.popEnterAnim = this.mPopEnterAnim;
        op.popExitAnim = this.mPopExitAnim;
        this.mNumOp += OP_ADD;
    }

    public FragmentTransaction add(Fragment fragment, String tag) {
        doAddOp(OP_NULL, fragment, tag, OP_ADD);
        return this;
    }

    public FragmentTransaction add(int containerViewId, Fragment fragment) {
        doAddOp(containerViewId, fragment, null, OP_ADD);
        return this;
    }

    public FragmentTransaction add(int containerViewId, Fragment fragment, String tag) {
        doAddOp(containerViewId, fragment, tag, OP_ADD);
        return this;
    }

    private void doAddOp(int containerViewId, Fragment fragment, String tag, int opcmd) {
        fragment.mFragmentManager = this.mManager;
        if (tag != null) {
            if (fragment.mTag == null || tag.equals(fragment.mTag)) {
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
        Op op = new Op();
        op.cmd = opcmd;
        op.fragment = fragment;
        addOp(op);
    }

    public FragmentTransaction replace(int containerViewId, Fragment fragment) {
        return replace(containerViewId, fragment, null);
    }

    public FragmentTransaction replace(int containerViewId, Fragment fragment, String tag) {
        if (containerViewId == 0) {
            throw new IllegalArgumentException("Must use non-zero containerViewId");
        }
        doAddOp(containerViewId, fragment, tag, OP_REPLACE);
        return this;
    }

    public FragmentTransaction remove(Fragment fragment) {
        Op op = new Op();
        op.cmd = OP_REMOVE;
        op.fragment = fragment;
        addOp(op);
        return this;
    }

    public FragmentTransaction hide(Fragment fragment) {
        Op op = new Op();
        op.cmd = OP_HIDE;
        op.fragment = fragment;
        addOp(op);
        return this;
    }

    public FragmentTransaction show(Fragment fragment) {
        Op op = new Op();
        op.cmd = OP_SHOW;
        op.fragment = fragment;
        addOp(op);
        return this;
    }

    public FragmentTransaction detach(Fragment fragment) {
        Op op = new Op();
        op.cmd = OP_DETACH;
        op.fragment = fragment;
        addOp(op);
        return this;
    }

    public FragmentTransaction attach(Fragment fragment) {
        Op op = new Op();
        op.cmd = OP_ATTACH;
        op.fragment = fragment;
        addOp(op);
        return this;
    }

    public FragmentTransaction setCustomAnimations(int enter, int exit) {
        return setCustomAnimations(enter, exit, OP_NULL, OP_NULL);
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
        this.mBreadCrumbTitleRes = OP_NULL;
        this.mBreadCrumbTitleText = text;
        return this;
    }

    public FragmentTransaction setBreadCrumbShortTitle(int res) {
        this.mBreadCrumbShortTitleRes = res;
        this.mBreadCrumbShortTitleText = null;
        return this;
    }

    public FragmentTransaction setBreadCrumbShortTitle(CharSequence text) {
        this.mBreadCrumbShortTitleRes = OP_NULL;
        this.mBreadCrumbShortTitleText = text;
        return this;
    }

    void bumpBackStackNesting(int amt) {
        if (this.mAddToBackStack) {
            if (FragmentManagerImpl.DEBUG) {
                Log.v(TAG, "Bump nesting in " + this + " by " + amt);
            }
            for (Op op = this.mHead; op != null; op = op.next) {
                if (op.fragment != null) {
                    Fragment fragment = op.fragment;
                    fragment.mBackStackNesting += amt;
                    if (FragmentManagerImpl.DEBUG) {
                        Log.v(TAG, "Bump nesting of " + op.fragment + " to " + op.fragment.mBackStackNesting);
                    }
                }
                if (op.removed != null) {
                    for (int i = op.removed.size() - 1; i >= 0; i--) {
                        Fragment r = (Fragment) op.removed.get(i);
                        r.mBackStackNesting += amt;
                        if (FragmentManagerImpl.DEBUG) {
                            Log.v(TAG, "Bump nesting of " + r + " to " + r.mBackStackNesting);
                        }
                    }
                }
            }
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

    int commitInternal(boolean allowStateLoss) {
        if (this.mCommitted) {
            throw new IllegalStateException("commit already called");
        }
        if (FragmentManagerImpl.DEBUG) {
            Log.v(TAG, "Commit: " + this);
            PrintWriter pw = new FastPrintWriter(new LogWriter(OP_REPLACE, TAG), false, Document.FLAG_SUPPORTS_REMOVE);
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

    public void run() {
        if (FragmentManagerImpl.DEBUG) {
            Log.v(TAG, "Run: " + this);
        }
        if (!this.mAddToBackStack || this.mIndex >= 0) {
            bumpBackStackNesting(OP_ADD);
            if (this.mManager.mCurState >= OP_ADD) {
                SparseArray<Fragment> firstOutFragments = new SparseArray();
                SparseArray<Fragment> lastInFragments = new SparseArray();
                calculateFragments(firstOutFragments, lastInFragments);
                beginTransition(firstOutFragments, lastInFragments, false);
            }
            for (Op op = this.mHead; op != null; op = op.next) {
                Fragment f;
                switch (op.cmd) {
                    case OP_ADD /*1*/:
                        f = op.fragment;
                        f.mNextAnim = op.enterAnim;
                        this.mManager.addFragment(f, false);
                        break;
                    case OP_REPLACE /*2*/:
                        f = op.fragment;
                        int containerId = f.mContainerId;
                        if (this.mManager.mAdded != null) {
                            for (int i = this.mManager.mAdded.size() - 1; i >= 0; i--) {
                                Fragment old = (Fragment) this.mManager.mAdded.get(i);
                                if (FragmentManagerImpl.DEBUG) {
                                    Log.v(TAG, "OP_REPLACE: adding=" + f + " old=" + old);
                                }
                                if (old.mContainerId == containerId) {
                                    if (old == f) {
                                        f = null;
                                        op.fragment = null;
                                    } else {
                                        if (op.removed == null) {
                                            op.removed = new ArrayList();
                                        }
                                        op.removed.add(old);
                                        old.mNextAnim = op.exitAnim;
                                        if (this.mAddToBackStack) {
                                            old.mBackStackNesting += OP_ADD;
                                            if (FragmentManagerImpl.DEBUG) {
                                                Log.v(TAG, "Bump nesting of " + old + " to " + old.mBackStackNesting);
                                            }
                                        }
                                        this.mManager.removeFragment(old, this.mTransition, this.mTransitionStyle);
                                    }
                                }
                            }
                        }
                        if (f == null) {
                            break;
                        }
                        f.mNextAnim = op.enterAnim;
                        this.mManager.addFragment(f, false);
                        break;
                    case OP_REMOVE /*3*/:
                        f = op.fragment;
                        f.mNextAnim = op.exitAnim;
                        this.mManager.removeFragment(f, this.mTransition, this.mTransitionStyle);
                        break;
                    case OP_HIDE /*4*/:
                        f = op.fragment;
                        f.mNextAnim = op.exitAnim;
                        this.mManager.hideFragment(f, this.mTransition, this.mTransitionStyle);
                        break;
                    case OP_SHOW /*5*/:
                        f = op.fragment;
                        f.mNextAnim = op.enterAnim;
                        this.mManager.showFragment(f, this.mTransition, this.mTransitionStyle);
                        break;
                    case OP_DETACH /*6*/:
                        f = op.fragment;
                        f.mNextAnim = op.exitAnim;
                        this.mManager.detachFragment(f, this.mTransition, this.mTransitionStyle);
                        break;
                    case OP_ATTACH /*7*/:
                        f = op.fragment;
                        f.mNextAnim = op.enterAnim;
                        this.mManager.attachFragment(f, this.mTransition, this.mTransitionStyle);
                        break;
                    default:
                        throw new IllegalArgumentException("Unknown cmd: " + op.cmd);
                }
            }
            this.mManager.moveToState(this.mManager.mCurState, this.mTransition, this.mTransitionStyle, true);
            if (this.mAddToBackStack) {
                this.mManager.addBackStackState(this);
                return;
            }
            return;
        }
        throw new IllegalStateException("addToBackStack() called after commit()");
    }

    private static void setFirstOut(SparseArray<Fragment> firstOutFragments, SparseArray<Fragment> lastInFragments, Fragment fragment) {
        if (fragment != null) {
            int containerId = fragment.mContainerId;
            if (containerId != 0 && !fragment.isHidden()) {
                if (fragment.isAdded() && fragment.getView() != null && firstOutFragments.get(containerId) == null) {
                    firstOutFragments.put(containerId, fragment);
                }
                if (lastInFragments.get(containerId) == fragment) {
                    lastInFragments.remove(containerId);
                }
            }
        }
    }

    private void setLastIn(SparseArray<Fragment> firstOutFragments, SparseArray<Fragment> lastInFragments, Fragment fragment) {
        if (fragment != null) {
            int containerId = fragment.mContainerId;
            if (containerId != 0) {
                if (!fragment.isAdded()) {
                    lastInFragments.put(containerId, fragment);
                }
                if (firstOutFragments.get(containerId) == fragment) {
                    firstOutFragments.remove(containerId);
                }
            }
            if (fragment.mState < OP_ADD && this.mManager.mCurState >= OP_ADD) {
                this.mManager.makeActive(fragment);
                this.mManager.moveToState(fragment, OP_ADD, OP_NULL, OP_NULL, false);
            }
        }
    }

    private void calculateFragments(SparseArray<Fragment> firstOutFragments, SparseArray<Fragment> lastInFragments) {
        if (this.mManager.mContainer.onHasView()) {
            for (Op op = this.mHead; op != null; op = op.next) {
                switch (op.cmd) {
                    case OP_ADD /*1*/:
                        setLastIn(firstOutFragments, lastInFragments, op.fragment);
                        break;
                    case OP_REPLACE /*2*/:
                        Fragment f = op.fragment;
                        if (this.mManager.mAdded != null) {
                            for (int i = OP_NULL; i < this.mManager.mAdded.size(); i += OP_ADD) {
                                Fragment old = (Fragment) this.mManager.mAdded.get(i);
                                if (f == null || old.mContainerId == f.mContainerId) {
                                    if (old == f) {
                                        f = null;
                                        lastInFragments.remove(old.mContainerId);
                                    } else {
                                        setFirstOut(firstOutFragments, lastInFragments, old);
                                    }
                                }
                            }
                        }
                        setLastIn(firstOutFragments, lastInFragments, op.fragment);
                        break;
                    case OP_REMOVE /*3*/:
                        setFirstOut(firstOutFragments, lastInFragments, op.fragment);
                        break;
                    case OP_HIDE /*4*/:
                        setFirstOut(firstOutFragments, lastInFragments, op.fragment);
                        break;
                    case OP_SHOW /*5*/:
                        setLastIn(firstOutFragments, lastInFragments, op.fragment);
                        break;
                    case OP_DETACH /*6*/:
                        setFirstOut(firstOutFragments, lastInFragments, op.fragment);
                        break;
                    case OP_ATTACH /*7*/:
                        setLastIn(firstOutFragments, lastInFragments, op.fragment);
                        break;
                    default:
                        break;
                }
            }
        }
    }

    public void calculateBackFragments(SparseArray<Fragment> firstOutFragments, SparseArray<Fragment> lastInFragments) {
        if (this.mManager.mContainer.onHasView()) {
            for (Op op = this.mTail; op != null; op = op.prev) {
                switch (op.cmd) {
                    case OP_ADD /*1*/:
                        setFirstOut(firstOutFragments, lastInFragments, op.fragment);
                        break;
                    case OP_REPLACE /*2*/:
                        if (op.removed != null) {
                            for (int i = op.removed.size() - 1; i >= 0; i--) {
                                setLastIn(firstOutFragments, lastInFragments, (Fragment) op.removed.get(i));
                            }
                        }
                        setFirstOut(firstOutFragments, lastInFragments, op.fragment);
                        break;
                    case OP_REMOVE /*3*/:
                        setLastIn(firstOutFragments, lastInFragments, op.fragment);
                        break;
                    case OP_HIDE /*4*/:
                        setLastIn(firstOutFragments, lastInFragments, op.fragment);
                        break;
                    case OP_SHOW /*5*/:
                        setFirstOut(firstOutFragments, lastInFragments, op.fragment);
                        break;
                    case OP_DETACH /*6*/:
                        setLastIn(firstOutFragments, lastInFragments, op.fragment);
                        break;
                    case OP_ATTACH /*7*/:
                        setFirstOut(firstOutFragments, lastInFragments, op.fragment);
                        break;
                    default:
                        break;
                }
            }
        }
    }

    private TransitionState beginTransition(SparseArray<Fragment> firstOutFragments, SparseArray<Fragment> lastInFragments, boolean isBack) {
        int i;
        TransitionState state = new TransitionState();
        state.nonExistentView = new View(this.mManager.mHost.getContext());
        for (i = OP_NULL; i < firstOutFragments.size(); i += OP_ADD) {
            configureTransitions(firstOutFragments.keyAt(i), state, isBack, firstOutFragments, lastInFragments);
        }
        for (i = OP_NULL; i < lastInFragments.size(); i += OP_ADD) {
            int containerId = lastInFragments.keyAt(i);
            if (firstOutFragments.get(containerId) == null) {
                configureTransitions(containerId, state, isBack, firstOutFragments, lastInFragments);
            }
        }
        return state;
    }

    private static Transition cloneTransition(Transition transition) {
        if (transition != null) {
            return transition.clone();
        }
        return transition;
    }

    private static Transition getEnterTransition(Fragment inFragment, boolean isBack) {
        if (inFragment == null) {
            return null;
        }
        Transition reenterTransition;
        if (isBack) {
            reenterTransition = inFragment.getReenterTransition();
        } else {
            reenterTransition = inFragment.getEnterTransition();
        }
        return cloneTransition(reenterTransition);
    }

    private static Transition getExitTransition(Fragment outFragment, boolean isBack) {
        if (outFragment == null) {
            return null;
        }
        Transition returnTransition;
        if (isBack) {
            returnTransition = outFragment.getReturnTransition();
        } else {
            returnTransition = outFragment.getExitTransition();
        }
        return cloneTransition(returnTransition);
    }

    private static TransitionSet getSharedElementTransition(Fragment inFragment, Fragment outFragment, boolean isBack) {
        if (inFragment == null || outFragment == null) {
            return null;
        }
        Transition sharedElementReturnTransition;
        if (isBack) {
            sharedElementReturnTransition = outFragment.getSharedElementReturnTransition();
        } else {
            sharedElementReturnTransition = inFragment.getSharedElementEnterTransition();
        }
        Transition transition = cloneTransition(sharedElementReturnTransition);
        if (transition == null) {
            return null;
        }
        TransitionSet transitionSet = new TransitionSet();
        transitionSet.addTransition(transition);
        return transitionSet;
    }

    private static ArrayList<View> captureExitingViews(Transition exitTransition, Fragment outFragment, ArrayMap<String, View> namedViews, View nonExistentView) {
        ArrayList<View> viewList = null;
        if (exitTransition != null) {
            viewList = new ArrayList();
            outFragment.getView().captureTransitioningViews(viewList);
            if (namedViews != null) {
                viewList.removeAll(namedViews.values());
            }
            if (!viewList.isEmpty()) {
                viewList.add(nonExistentView);
                addTargets(exitTransition, viewList);
            }
        }
        return viewList;
    }

    private ArrayMap<String, View> remapSharedElements(TransitionState state, Fragment outFragment, boolean isBack) {
        ArrayMap namedViews = new ArrayMap();
        if (this.mSharedElementSourceNames != null) {
            outFragment.getView().findNamedViews(namedViews);
            if (isBack) {
                namedViews.retainAll(this.mSharedElementTargetNames);
            } else {
                namedViews = remapNames(this.mSharedElementSourceNames, this.mSharedElementTargetNames, namedViews);
            }
        }
        if (isBack) {
            outFragment.mEnterTransitionCallback.onMapSharedElements(this.mSharedElementTargetNames, namedViews);
            setBackNameOverrides(state, namedViews, false);
        } else {
            outFragment.mExitTransitionCallback.onMapSharedElements(this.mSharedElementTargetNames, namedViews);
            setNameOverrides(state, namedViews, false);
        }
        return namedViews;
    }

    private ArrayList<View> addTransitionTargets(TransitionState state, Transition enterTransition, TransitionSet sharedElementTransition, Transition exitTransition, Transition overallTransition, View container, Fragment inFragment, Fragment outFragment, ArrayList<View> hiddenFragmentViews, boolean isBack, ArrayList<View> sharedElementTargets) {
        if (enterTransition == null && sharedElementTransition == null && overallTransition == null) {
            return null;
        }
        ArrayList<View> enteringViews = new ArrayList();
        container.getViewTreeObserver().addOnPreDrawListener(new AnonymousClass1(container, inFragment, hiddenFragmentViews, overallTransition, sharedElementTransition, state, isBack, sharedElementTargets, exitTransition, enterTransition, outFragment, enteringViews));
        return enteringViews;
    }

    private void callSharedElementEnd(TransitionState state, Fragment inFragment, Fragment outFragment, boolean isBack, ArrayMap<String, View> namedViews) {
        SharedElementCallback sharedElementCallback;
        if (isBack) {
            sharedElementCallback = outFragment.mEnterTransitionCallback;
        } else {
            sharedElementCallback = inFragment.mEnterTransitionCallback;
        }
        sharedElementCallback.onSharedElementEnd(new ArrayList(namedViews.keySet()), new ArrayList(namedViews.values()), null);
    }

    private void setEpicenterIn(ArrayMap<String, View> namedViews, TransitionState state) {
        if (this.mSharedElementTargetNames != null && !namedViews.isEmpty()) {
            View epicenter = (View) namedViews.get(this.mSharedElementTargetNames.get(OP_NULL));
            if (epicenter != null) {
                state.enteringEpicenterView = epicenter;
            }
        }
    }

    private ArrayMap<String, View> mapSharedElementsIn(TransitionState state, boolean isBack, Fragment inFragment) {
        ArrayMap namedViews = mapEnteringSharedElements(state, inFragment, isBack);
        if (isBack) {
            inFragment.mExitTransitionCallback.onMapSharedElements(this.mSharedElementTargetNames, namedViews);
            setBackNameOverrides(state, namedViews, true);
        } else {
            inFragment.mEnterTransitionCallback.onMapSharedElements(this.mSharedElementTargetNames, namedViews);
            setNameOverrides(state, namedViews, true);
        }
        return namedViews;
    }

    private static Transition mergeTransitions(Transition enterTransition, Transition exitTransition, Transition sharedElementTransition, Fragment inFragment, boolean isBack) {
        boolean overlap = true;
        if (!(enterTransition == null || exitTransition == null || inFragment == null)) {
            overlap = isBack ? inFragment.getAllowReturnTransitionOverlap() : inFragment.getAllowEnterTransitionOverlap();
        }
        if (overlap) {
            Transition transitionSet = new TransitionSet();
            if (enterTransition != null) {
                transitionSet.addTransition(enterTransition);
            }
            if (exitTransition != null) {
                transitionSet.addTransition(exitTransition);
            }
            if (sharedElementTransition != null) {
                transitionSet.addTransition(sharedElementTransition);
            }
            return transitionSet;
        }
        Transition staggered = null;
        if (exitTransition != null && enterTransition != null) {
            staggered = new TransitionSet().addTransition(exitTransition).addTransition(enterTransition).setOrdering(OP_ADD);
        } else if (exitTransition != null) {
            staggered = exitTransition;
        } else if (enterTransition != null) {
            staggered = enterTransition;
        }
        if (sharedElementTransition == null) {
            return staggered;
        }
        Transition together = new TransitionSet();
        if (staggered != null) {
            together.addTransition(staggered);
        }
        together.addTransition(sharedElementTransition);
        return together;
    }

    private void configureTransitions(int containerId, TransitionState state, boolean isBack, SparseArray<Fragment> firstOutFragments, SparseArray<Fragment> lastInFragments) {
        ViewGroup sceneRoot = (ViewGroup) this.mManager.mContainer.onFindViewById(containerId);
        if (sceneRoot != null) {
            Fragment inFragment = (Fragment) lastInFragments.get(containerId);
            Fragment outFragment = (Fragment) firstOutFragments.get(containerId);
            Transition enterTransition = getEnterTransition(inFragment, isBack);
            Transition sharedElementTransition = getSharedElementTransition(inFragment, outFragment, isBack);
            Transition exitTransition = getExitTransition(outFragment, isBack);
            if (enterTransition != null || sharedElementTransition != null || exitTransition != null) {
                if (enterTransition != null) {
                    enterTransition.addTarget(state.nonExistentView);
                }
                ArrayMap<String, View> arrayMap = null;
                ArrayList<View> sharedElementTargets = new ArrayList();
                if (sharedElementTransition != null) {
                    SharedElementCallback callback;
                    arrayMap = remapSharedElements(state, outFragment, isBack);
                    setSharedElementTargets(sharedElementTransition, state.nonExistentView, arrayMap, sharedElementTargets);
                    if (isBack) {
                        callback = outFragment.mEnterTransitionCallback;
                    } else {
                        callback = inFragment.mEnterTransitionCallback;
                    }
                    callback.onSharedElementStart(new ArrayList(arrayMap.keySet()), new ArrayList(arrayMap.values()), null);
                }
                ArrayList<View> exitingViews = captureExitingViews(exitTransition, outFragment, arrayMap, state.nonExistentView);
                if (exitingViews == null || exitingViews.isEmpty()) {
                    exitTransition = null;
                }
                excludeViews(enterTransition, exitTransition, exitingViews, true);
                excludeViews(enterTransition, sharedElementTransition, sharedElementTargets, true);
                excludeViews(exitTransition, sharedElementTransition, sharedElementTargets, true);
                if (!(this.mSharedElementTargetNames == null || arrayMap == null)) {
                    View epicenterView = (View) arrayMap.get(this.mSharedElementTargetNames.get(OP_NULL));
                    if (epicenterView != null) {
                        if (exitTransition != null) {
                            setEpicenter(exitTransition, epicenterView);
                        }
                        if (sharedElementTransition != null) {
                            setEpicenter(sharedElementTransition, epicenterView);
                        }
                    }
                }
                Transition transition = mergeTransitions(enterTransition, exitTransition, sharedElementTransition, inFragment, isBack);
                if (transition != null) {
                    ArrayList<View> hiddenFragments = new ArrayList();
                    ArrayList<View> enteringViews = addTransitionTargets(state, enterTransition, sharedElementTransition, exitTransition, transition, sceneRoot, inFragment, outFragment, hiddenFragments, isBack, sharedElementTargets);
                    transition.setNameOverrides(state.nameOverrides);
                    transition.excludeTarget(state.nonExistentView, true);
                    excludeHiddenFragments(hiddenFragments, containerId, transition);
                    TransitionManager.beginDelayedTransition(sceneRoot, transition);
                    ViewGroup viewGroup = sceneRoot;
                    removeTargetedViewsFromTransitions(viewGroup, state.nonExistentView, enterTransition, enteringViews, exitTransition, exitingViews, sharedElementTransition, sharedElementTargets, transition, hiddenFragments);
                }
            }
        }
    }

    private static void setSharedElementTargets(TransitionSet transition, View nonExistentView, ArrayMap<String, View> namedViews, ArrayList<View> sharedElementTargets) {
        sharedElementTargets.clear();
        sharedElementTargets.addAll(namedViews.values());
        List<View> views = transition.getTargets();
        views.clear();
        int count = sharedElementTargets.size();
        for (int i = OP_NULL; i < count; i += OP_ADD) {
            bfsAddViewChildren(views, (View) sharedElementTargets.get(i));
        }
        sharedElementTargets.add(nonExistentView);
        addTargets(transition, sharedElementTargets);
    }

    private static void bfsAddViewChildren(List<View> views, View startView) {
        int startIndex = views.size();
        if (!containedBeforeIndex(views, startView, startIndex)) {
            views.add(startView);
            for (int index = startIndex; index < views.size(); index += OP_ADD) {
                View view = (View) views.get(index);
                if (view instanceof ViewGroup) {
                    ViewGroup viewGroup = (ViewGroup) view;
                    int childCount = viewGroup.getChildCount();
                    for (int childIndex = OP_NULL; childIndex < childCount; childIndex += OP_ADD) {
                        View child = viewGroup.getChildAt(childIndex);
                        if (!containedBeforeIndex(views, child, startIndex)) {
                            views.add(child);
                        }
                    }
                }
            }
        }
    }

    private static boolean containedBeforeIndex(List<View> views, View view, int maxIndex) {
        for (int i = OP_NULL; i < maxIndex; i += OP_ADD) {
            if (views.get(i) == view) {
                return true;
            }
        }
        return false;
    }

    private static void excludeViews(Transition transition, Transition fromTransition, ArrayList<View> views, boolean exclude) {
        if (transition != null) {
            int viewCount = fromTransition == null ? OP_NULL : views.size();
            for (int i = OP_NULL; i < viewCount; i += OP_ADD) {
                transition.excludeTarget((View) views.get(i), exclude);
            }
        }
    }

    private void removeTargetedViewsFromTransitions(ViewGroup sceneRoot, View nonExistingView, Transition enterTransition, ArrayList<View> enteringViews, Transition exitTransition, ArrayList<View> exitingViews, Transition sharedElementTransition, ArrayList<View> sharedElementTargets, Transition overallTransition, ArrayList<View> hiddenViews) {
        if (overallTransition != null) {
            sceneRoot.getViewTreeObserver().addOnPreDrawListener(new AnonymousClass2(sceneRoot, enterTransition, enteringViews, exitTransition, exitingViews, sharedElementTransition, sharedElementTargets, hiddenViews, overallTransition, nonExistingView));
        }
    }

    public static void removeTargets(Transition transition, ArrayList<View> views) {
        int i;
        if (transition instanceof TransitionSet) {
            TransitionSet set = (TransitionSet) transition;
            int numTransitions = set.getTransitionCount();
            for (i = OP_NULL; i < numTransitions; i += OP_ADD) {
                removeTargets(set.getTransitionAt(i), views);
            }
        } else if (!hasSimpleTarget(transition)) {
            List<View> targets = transition.getTargets();
            if (targets != null && targets.size() == views.size() && targets.containsAll(views)) {
                for (i = views.size() - 1; i >= 0; i--) {
                    transition.removeTarget((View) views.get(i));
                }
            }
        }
    }

    public static void addTargets(Transition transition, ArrayList<View> views) {
        int i;
        if (transition instanceof TransitionSet) {
            TransitionSet set = (TransitionSet) transition;
            int numTransitions = set.getTransitionCount();
            for (i = OP_NULL; i < numTransitions; i += OP_ADD) {
                addTargets(set.getTransitionAt(i), views);
            }
        } else if (!hasSimpleTarget(transition) && isNullOrEmpty(transition.getTargets())) {
            int numViews = views.size();
            for (i = OP_NULL; i < numViews; i += OP_ADD) {
                transition.addTarget((View) views.get(i));
            }
        }
    }

    private static boolean hasSimpleTarget(Transition transition) {
        if (isNullOrEmpty(transition.getTargetIds()) && isNullOrEmpty(transition.getTargetNames()) && isNullOrEmpty(transition.getTargetTypes())) {
            return false;
        }
        return true;
    }

    private static boolean isNullOrEmpty(List list) {
        return list != null ? list.isEmpty() : true;
    }

    private static ArrayMap<String, View> remapNames(ArrayList<String> inMap, ArrayList<String> toGoInMap, ArrayMap<String, View> namedViews) {
        ArrayMap<String, View> remappedViews = new ArrayMap();
        if (!namedViews.isEmpty()) {
            int numKeys = inMap.size();
            for (int i = OP_NULL; i < numKeys; i += OP_ADD) {
                View view = (View) namedViews.get(inMap.get(i));
                if (view != null) {
                    remappedViews.put((String) toGoInMap.get(i), view);
                }
            }
        }
        return remappedViews;
    }

    private ArrayMap<String, View> mapEnteringSharedElements(TransitionState state, Fragment inFragment, boolean isBack) {
        ArrayMap<String, View> namedViews = new ArrayMap();
        View root = inFragment.getView();
        if (root == null || this.mSharedElementSourceNames == null) {
            return namedViews;
        }
        root.findNamedViews(namedViews);
        if (isBack) {
            return remapNames(this.mSharedElementSourceNames, this.mSharedElementTargetNames, namedViews);
        }
        namedViews.retainAll(this.mSharedElementTargetNames);
        return namedViews;
    }

    private void excludeHiddenFragments(ArrayList<View> hiddenFragmentViews, int containerId, Transition transition) {
        if (this.mManager.mAdded != null) {
            for (int i = OP_NULL; i < this.mManager.mAdded.size(); i += OP_ADD) {
                Fragment fragment = (Fragment) this.mManager.mAdded.get(i);
                if (!(fragment.mView == null || fragment.mContainer == null || fragment.mContainerId != containerId)) {
                    if (!fragment.mHidden) {
                        transition.excludeTarget(fragment.mView, false);
                        hiddenFragmentViews.remove(fragment.mView);
                    } else if (!hiddenFragmentViews.contains(fragment.mView)) {
                        transition.excludeTarget(fragment.mView, true);
                        hiddenFragmentViews.add(fragment.mView);
                    }
                }
            }
        }
    }

    private static void setEpicenter(Transition transition, View view) {
        Rect epicenter = new Rect();
        view.getBoundsOnScreen(epicenter);
        transition.setEpicenterCallback(new AnonymousClass3(epicenter));
    }

    private void setSharedElementEpicenter(Transition transition, TransitionState state) {
        transition.setEpicenterCallback(new AnonymousClass4(state));
    }

    public TransitionState popFromBackStack(boolean doStateMove, TransitionState state, SparseArray<Fragment> firstOutFragments, SparseArray<Fragment> lastInFragments) {
        if (FragmentManagerImpl.DEBUG) {
            Log.v(TAG, "popFromBackStack: " + this);
            PrintWriter pw = new FastPrintWriter(new LogWriter(OP_REPLACE, TAG), false, Document.FLAG_SUPPORTS_REMOVE);
            dump("  ", null, pw, null);
            pw.flush();
        }
        if (this.mManager.mCurState >= OP_ADD) {
            if (state == null) {
                if (!(firstOutFragments.size() == 0 && lastInFragments.size() == 0)) {
                    state = beginTransition(firstOutFragments, lastInFragments, true);
                }
            } else if (!doStateMove) {
                setNameOverrides(state, this.mSharedElementTargetNames, this.mSharedElementSourceNames);
            }
        }
        bumpBackStackNesting(-1);
        for (Op op = this.mTail; op != null; op = op.prev) {
            Fragment f;
            switch (op.cmd) {
                case OP_ADD /*1*/:
                    f = op.fragment;
                    f.mNextAnim = op.popExitAnim;
                    this.mManager.removeFragment(f, FragmentManagerImpl.reverseTransit(this.mTransition), this.mTransitionStyle);
                    break;
                case OP_REPLACE /*2*/:
                    f = op.fragment;
                    if (f != null) {
                        f.mNextAnim = op.popExitAnim;
                        this.mManager.removeFragment(f, FragmentManagerImpl.reverseTransit(this.mTransition), this.mTransitionStyle);
                    }
                    if (op.removed == null) {
                        break;
                    }
                    for (int i = OP_NULL; i < op.removed.size(); i += OP_ADD) {
                        Fragment old = (Fragment) op.removed.get(i);
                        old.mNextAnim = op.popEnterAnim;
                        this.mManager.addFragment(old, false);
                    }
                    break;
                case OP_REMOVE /*3*/:
                    f = op.fragment;
                    f.mNextAnim = op.popEnterAnim;
                    this.mManager.addFragment(f, false);
                    break;
                case OP_HIDE /*4*/:
                    f = op.fragment;
                    f.mNextAnim = op.popEnterAnim;
                    this.mManager.showFragment(f, FragmentManagerImpl.reverseTransit(this.mTransition), this.mTransitionStyle);
                    break;
                case OP_SHOW /*5*/:
                    f = op.fragment;
                    f.mNextAnim = op.popExitAnim;
                    this.mManager.hideFragment(f, FragmentManagerImpl.reverseTransit(this.mTransition), this.mTransitionStyle);
                    break;
                case OP_DETACH /*6*/:
                    f = op.fragment;
                    f.mNextAnim = op.popEnterAnim;
                    this.mManager.attachFragment(f, FragmentManagerImpl.reverseTransit(this.mTransition), this.mTransitionStyle);
                    break;
                case OP_ATTACH /*7*/:
                    f = op.fragment;
                    f.mNextAnim = op.popExitAnim;
                    this.mManager.detachFragment(f, FragmentManagerImpl.reverseTransit(this.mTransition), this.mTransitionStyle);
                    break;
                default:
                    throw new IllegalArgumentException("Unknown cmd: " + op.cmd);
            }
        }
        if (doStateMove) {
            this.mManager.moveToState(this.mManager.mCurState, FragmentManagerImpl.reverseTransit(this.mTransition), this.mTransitionStyle, true);
            state = null;
        }
        if (this.mIndex >= 0) {
            this.mManager.freeBackStackIndex(this.mIndex);
            this.mIndex = -1;
        }
        return state;
    }

    private static void setNameOverride(ArrayMap<String, String> overrides, String source, String target) {
        if (!(source == null || target == null || source.equals(target))) {
            for (int index = OP_NULL; index < overrides.size(); index += OP_ADD) {
                if (source.equals(overrides.valueAt(index))) {
                    overrides.setValueAt(index, target);
                    return;
                }
            }
            overrides.put(source, target);
        }
    }

    private static void setNameOverrides(TransitionState state, ArrayList<String> sourceNames, ArrayList<String> targetNames) {
        if (sourceNames != null && targetNames != null) {
            for (int i = OP_NULL; i < sourceNames.size(); i += OP_ADD) {
                setNameOverride(state.nameOverrides, (String) sourceNames.get(i), (String) targetNames.get(i));
            }
        }
    }

    private void setBackNameOverrides(TransitionState state, ArrayMap<String, View> namedViews, boolean isEnd) {
        int sourceCount;
        int targetCount = this.mSharedElementTargetNames == null ? OP_NULL : this.mSharedElementTargetNames.size();
        if (this.mSharedElementSourceNames == null) {
            sourceCount = OP_NULL;
        } else {
            sourceCount = this.mSharedElementSourceNames.size();
        }
        int count = Math.min(targetCount, sourceCount);
        for (int i = OP_NULL; i < count; i += OP_ADD) {
            String source = (String) this.mSharedElementSourceNames.get(i);
            View view = (View) namedViews.get((String) this.mSharedElementTargetNames.get(i));
            if (view != null) {
                String target = view.getTransitionName();
                if (isEnd) {
                    setNameOverride(state.nameOverrides, source, target);
                } else {
                    setNameOverride(state.nameOverrides, target, source);
                }
            }
        }
    }

    private void setNameOverrides(TransitionState state, ArrayMap<String, View> namedViews, boolean isEnd) {
        int count = namedViews == null ? OP_NULL : namedViews.size();
        for (int i = OP_NULL; i < count; i += OP_ADD) {
            String source = (String) namedViews.keyAt(i);
            String target = ((View) namedViews.valueAt(i)).getTransitionName();
            if (isEnd) {
                setNameOverride(state.nameOverrides, source, target);
            } else {
                setNameOverride(state.nameOverrides, target, source);
            }
        }
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
        return this.mNumOp == 0;
    }
}
