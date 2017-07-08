package android.transition;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.TimeInterpolator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Rect;
import android.util.ArrayMap;
import android.util.AttributeSet;
import android.util.LongSparseArray;
import android.util.PtmLog;
import android.util.SparseArray;
import android.util.SparseLongArray;
import android.view.InflateException;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowId;
import android.view.animation.AnimationUtils;
import android.widget.ListView;
import com.android.internal.R;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

public abstract class Transition implements Cloneable {
    static final boolean DBG = false;
    private static final int[] DEFAULT_MATCH_ORDER = null;
    private static final String LOG_TAG = "Transition";
    private static final int MATCH_FIRST = 1;
    public static final int MATCH_ID = 3;
    private static final String MATCH_ID_STR = "id";
    public static final int MATCH_INSTANCE = 1;
    private static final String MATCH_INSTANCE_STR = "instance";
    public static final int MATCH_ITEM_ID = 4;
    private static final String MATCH_ITEM_ID_STR = "itemId";
    private static final int MATCH_LAST = 4;
    public static final int MATCH_NAME = 2;
    private static final String MATCH_NAME_STR = "name";
    private static final String MATCH_VIEW_NAME_STR = "viewName";
    private static final PathMotion STRAIGHT_PATH_MOTION = null;
    private static ThreadLocal<ArrayMap<Animator, AnimationInfo>> sRunningAnimators;
    ArrayList<Animator> mAnimators;
    boolean mCanRemoveViews;
    private ArrayList<Animator> mCurrentAnimators;
    long mDuration;
    private TransitionValuesMaps mEndValues;
    ArrayList<TransitionValues> mEndValuesList;
    private boolean mEnded;
    EpicenterCallback mEpicenterCallback;
    TimeInterpolator mInterpolator;
    ArrayList<TransitionListener> mListeners;
    int[] mMatchOrder;
    private String mName;
    ArrayMap<String, String> mNameOverrides;
    int mNumInstances;
    TransitionSet mParent;
    PathMotion mPathMotion;
    boolean mPaused;
    TransitionPropagation mPropagation;
    ViewGroup mSceneRoot;
    long mStartDelay;
    private TransitionValuesMaps mStartValues;
    ArrayList<TransitionValues> mStartValuesList;
    ArrayList<View> mTargetChildExcludes;
    ArrayList<View> mTargetExcludes;
    ArrayList<Integer> mTargetIdChildExcludes;
    ArrayList<Integer> mTargetIdExcludes;
    ArrayList<Integer> mTargetIds;
    ArrayList<String> mTargetNameExcludes;
    ArrayList<String> mTargetNames;
    ArrayList<Class> mTargetTypeChildExcludes;
    ArrayList<Class> mTargetTypeExcludes;
    ArrayList<Class> mTargetTypes;
    ArrayList<View> mTargets;

    public interface TransitionListener {
        void onTransitionCancel(Transition transition);

        void onTransitionEnd(Transition transition);

        void onTransitionPause(Transition transition);

        void onTransitionResume(Transition transition);

        void onTransitionStart(Transition transition);
    }

    public static class TransitionListenerAdapter implements TransitionListener {
        public void onTransitionStart(Transition transition) {
        }

        public void onTransitionEnd(Transition transition) {
        }

        public void onTransitionCancel(Transition transition) {
        }

        public void onTransitionPause(Transition transition) {
        }

        public void onTransitionResume(Transition transition) {
        }
    }

    /* renamed from: android.transition.Transition.2 */
    class AnonymousClass2 extends AnimatorListenerAdapter {
        final /* synthetic */ ArrayMap val$runningAnimators;

        AnonymousClass2(ArrayMap val$runningAnimators) {
            this.val$runningAnimators = val$runningAnimators;
        }

        public void onAnimationStart(Animator animation) {
            Transition.this.mCurrentAnimators.add(animation);
        }

        public void onAnimationEnd(Animator animation) {
            this.val$runningAnimators.remove(animation);
            Transition.this.mCurrentAnimators.remove(animation);
        }
    }

    public static class AnimationInfo {
        String name;
        Transition transition;
        TransitionValues values;
        public View view;
        WindowId windowId;

        AnimationInfo(View view, String name, Transition transition, WindowId windowId, TransitionValues values) {
            this.view = view;
            this.name = name;
            this.values = values;
            this.windowId = windowId;
            this.transition = transition;
        }
    }

    private static class ArrayListManager {
        private ArrayListManager() {
        }

        static <T> ArrayList<T> add(ArrayList<T> list, T item) {
            if (list == null) {
                list = new ArrayList();
            }
            if (!list.contains(item)) {
                list.add(item);
            }
            return list;
        }

        static <T> ArrayList<T> remove(ArrayList<T> list, T item) {
            if (list == null) {
                return list;
            }
            list.remove(item);
            if (list.isEmpty()) {
                return null;
            }
            return list;
        }
    }

    public static abstract class EpicenterCallback {
        public abstract Rect onGetEpicenter(Transition transition);
    }

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.transition.Transition.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.transition.Transition.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: android.transition.Transition.<clinit>():void");
    }

    public abstract void captureEndValues(TransitionValues transitionValues);

    public abstract void captureStartValues(TransitionValues transitionValues);

    public Transition() {
        this.mName = getClass().getName();
        this.mStartDelay = -1;
        this.mDuration = -1;
        this.mInterpolator = null;
        this.mTargetIds = new ArrayList();
        this.mTargets = new ArrayList();
        this.mTargetNames = null;
        this.mTargetTypes = null;
        this.mTargetIdExcludes = null;
        this.mTargetExcludes = null;
        this.mTargetTypeExcludes = null;
        this.mTargetNameExcludes = null;
        this.mTargetIdChildExcludes = null;
        this.mTargetChildExcludes = null;
        this.mTargetTypeChildExcludes = null;
        this.mStartValues = new TransitionValuesMaps();
        this.mEndValues = new TransitionValuesMaps();
        this.mParent = null;
        this.mMatchOrder = DEFAULT_MATCH_ORDER;
        this.mSceneRoot = null;
        this.mCanRemoveViews = DBG;
        this.mCurrentAnimators = new ArrayList();
        this.mNumInstances = 0;
        this.mPaused = DBG;
        this.mEnded = DBG;
        this.mListeners = null;
        this.mAnimators = new ArrayList();
        this.mPathMotion = STRAIGHT_PATH_MOTION;
    }

    public Transition(Context context, AttributeSet attrs) {
        this.mName = getClass().getName();
        this.mStartDelay = -1;
        this.mDuration = -1;
        this.mInterpolator = null;
        this.mTargetIds = new ArrayList();
        this.mTargets = new ArrayList();
        this.mTargetNames = null;
        this.mTargetTypes = null;
        this.mTargetIdExcludes = null;
        this.mTargetExcludes = null;
        this.mTargetTypeExcludes = null;
        this.mTargetNameExcludes = null;
        this.mTargetIdChildExcludes = null;
        this.mTargetChildExcludes = null;
        this.mTargetTypeChildExcludes = null;
        this.mStartValues = new TransitionValuesMaps();
        this.mEndValues = new TransitionValuesMaps();
        this.mParent = null;
        this.mMatchOrder = DEFAULT_MATCH_ORDER;
        this.mSceneRoot = null;
        this.mCanRemoveViews = DBG;
        this.mCurrentAnimators = new ArrayList();
        this.mNumInstances = 0;
        this.mPaused = DBG;
        this.mEnded = DBG;
        this.mListeners = null;
        this.mAnimators = new ArrayList();
        this.mPathMotion = STRAIGHT_PATH_MOTION;
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.Transition);
        long duration = (long) a.getInt(MATCH_INSTANCE, -1);
        if (duration >= 0) {
            setDuration(duration);
        }
        long startDelay = (long) a.getInt(MATCH_NAME, -1);
        if (startDelay > 0) {
            setStartDelay(startDelay);
        }
        int resID = a.getResourceId(0, 0);
        if (resID > 0) {
            setInterpolator(AnimationUtils.loadInterpolator(context, resID));
        }
        String matchOrder = a.getString(MATCH_ID);
        if (matchOrder != null) {
            setMatchOrder(parseMatchOrder(matchOrder));
        }
        a.recycle();
    }

    private static int[] parseMatchOrder(String matchOrderString) {
        StringTokenizer st = new StringTokenizer(matchOrderString, PtmLog.PAIRE_DELIMETER);
        int[] matches = new int[st.countTokens()];
        int index = 0;
        while (st.hasMoreTokens()) {
            String token = st.nextToken().trim();
            if (MATCH_ID_STR.equalsIgnoreCase(token)) {
                matches[index] = MATCH_ID;
            } else if (MATCH_INSTANCE_STR.equalsIgnoreCase(token)) {
                matches[index] = MATCH_INSTANCE;
            } else if (MATCH_NAME_STR.equalsIgnoreCase(token)) {
                matches[index] = MATCH_NAME;
            } else if (MATCH_VIEW_NAME_STR.equalsIgnoreCase(token)) {
                matches[index] = MATCH_NAME;
            } else if (MATCH_ITEM_ID_STR.equalsIgnoreCase(token)) {
                matches[index] = MATCH_LAST;
            } else if (token.isEmpty()) {
                int[] smallerMatches = new int[(matches.length - 1)];
                System.arraycopy(matches, 0, smallerMatches, 0, index);
                matches = smallerMatches;
                index--;
            } else {
                throw new InflateException("Unknown match type in matchOrder: '" + token + "'");
            }
            index += MATCH_INSTANCE;
        }
        return matches;
    }

    public Transition setDuration(long duration) {
        this.mDuration = duration;
        return this;
    }

    public long getDuration() {
        return this.mDuration;
    }

    public Transition setStartDelay(long startDelay) {
        this.mStartDelay = startDelay;
        return this;
    }

    public long getStartDelay() {
        return this.mStartDelay;
    }

    public Transition setInterpolator(TimeInterpolator interpolator) {
        this.mInterpolator = interpolator;
        return this;
    }

    public TimeInterpolator getInterpolator() {
        return this.mInterpolator;
    }

    public String[] getTransitionProperties() {
        return null;
    }

    public Animator createAnimator(ViewGroup sceneRoot, TransitionValues startValues, TransitionValues endValues) {
        return null;
    }

    public void setMatchOrder(int... matches) {
        if (matches == null || matches.length == 0) {
            this.mMatchOrder = DEFAULT_MATCH_ORDER;
            return;
        }
        int i = 0;
        while (i < matches.length) {
            if (!isValidMatch(matches[i])) {
                throw new IllegalArgumentException("matches contains invalid value");
            } else if (alreadyContains(matches, i)) {
                throw new IllegalArgumentException("matches contains a duplicate value");
            } else {
                i += MATCH_INSTANCE;
            }
        }
        this.mMatchOrder = (int[]) matches.clone();
    }

    private static boolean isValidMatch(int match) {
        return (match < MATCH_INSTANCE || match > MATCH_LAST) ? DBG : true;
    }

    private static boolean alreadyContains(int[] array, int searchIndex) {
        int value = array[searchIndex];
        for (int i = 0; i < searchIndex; i += MATCH_INSTANCE) {
            if (array[i] == value) {
                return true;
            }
        }
        return DBG;
    }

    private void matchInstances(ArrayMap<View, TransitionValues> unmatchedStart, ArrayMap<View, TransitionValues> unmatchedEnd) {
        for (int i = unmatchedStart.size() - 1; i >= 0; i--) {
            View view = (View) unmatchedStart.keyAt(i);
            if (view != null && isValidTarget(view)) {
                TransitionValues end = (TransitionValues) unmatchedEnd.remove(view);
                if (!(end == null || end.view == null || !isValidTarget(end.view))) {
                    this.mStartValuesList.add((TransitionValues) unmatchedStart.removeAt(i));
                    this.mEndValuesList.add(end);
                }
            }
        }
    }

    private void matchItemIds(ArrayMap<View, TransitionValues> unmatchedStart, ArrayMap<View, TransitionValues> unmatchedEnd, LongSparseArray<View> startItemIds, LongSparseArray<View> endItemIds) {
        int numStartIds = startItemIds.size();
        for (int i = 0; i < numStartIds; i += MATCH_INSTANCE) {
            View startView = (View) startItemIds.valueAt(i);
            if (startView != null && isValidTarget(startView)) {
                View endView = (View) endItemIds.get(startItemIds.keyAt(i));
                if (endView != null && isValidTarget(endView)) {
                    TransitionValues startValues = (TransitionValues) unmatchedStart.get(startView);
                    TransitionValues endValues = (TransitionValues) unmatchedEnd.get(endView);
                    if (!(startValues == null || endValues == null)) {
                        this.mStartValuesList.add(startValues);
                        this.mEndValuesList.add(endValues);
                        unmatchedStart.remove(startView);
                        unmatchedEnd.remove(endView);
                    }
                }
            }
        }
    }

    private void matchIds(ArrayMap<View, TransitionValues> unmatchedStart, ArrayMap<View, TransitionValues> unmatchedEnd, SparseArray<View> startIds, SparseArray<View> endIds) {
        int numStartIds = startIds.size();
        for (int i = 0; i < numStartIds; i += MATCH_INSTANCE) {
            View startView = (View) startIds.valueAt(i);
            if (startView != null && isValidTarget(startView)) {
                View endView = (View) endIds.get(startIds.keyAt(i));
                if (endView != null && isValidTarget(endView)) {
                    TransitionValues startValues = (TransitionValues) unmatchedStart.get(startView);
                    TransitionValues endValues = (TransitionValues) unmatchedEnd.get(endView);
                    if (!(startValues == null || endValues == null)) {
                        this.mStartValuesList.add(startValues);
                        this.mEndValuesList.add(endValues);
                        unmatchedStart.remove(startView);
                        unmatchedEnd.remove(endView);
                    }
                }
            }
        }
    }

    private void matchNames(ArrayMap<View, TransitionValues> unmatchedStart, ArrayMap<View, TransitionValues> unmatchedEnd, ArrayMap<String, View> startNames, ArrayMap<String, View> endNames) {
        int numStartNames = startNames.size();
        for (int i = 0; i < numStartNames; i += MATCH_INSTANCE) {
            View startView = (View) startNames.valueAt(i);
            if (startView != null && isValidTarget(startView)) {
                View endView = (View) endNames.get(startNames.keyAt(i));
                if (endView != null && isValidTarget(endView)) {
                    TransitionValues startValues = (TransitionValues) unmatchedStart.get(startView);
                    TransitionValues endValues = (TransitionValues) unmatchedEnd.get(endView);
                    if (!(startValues == null || endValues == null)) {
                        this.mStartValuesList.add(startValues);
                        this.mEndValuesList.add(endValues);
                        unmatchedStart.remove(startView);
                        unmatchedEnd.remove(endView);
                    }
                }
            }
        }
    }

    private void addUnmatched(ArrayMap<View, TransitionValues> unmatchedStart, ArrayMap<View, TransitionValues> unmatchedEnd) {
        int i;
        for (i = 0; i < unmatchedStart.size(); i += MATCH_INSTANCE) {
            TransitionValues start = (TransitionValues) unmatchedStart.valueAt(i);
            if (isValidTarget(start.view)) {
                this.mStartValuesList.add(start);
                this.mEndValuesList.add(null);
            }
        }
        for (i = 0; i < unmatchedEnd.size(); i += MATCH_INSTANCE) {
            TransitionValues end = (TransitionValues) unmatchedEnd.valueAt(i);
            if (isValidTarget(end.view)) {
                this.mEndValuesList.add(end);
                this.mStartValuesList.add(null);
            }
        }
    }

    private void matchStartAndEnd(TransitionValuesMaps startValues, TransitionValuesMaps endValues) {
        ArrayMap<View, TransitionValues> unmatchedStart = new ArrayMap(startValues.viewValues);
        ArrayMap<View, TransitionValues> unmatchedEnd = new ArrayMap(endValues.viewValues);
        for (int i = 0; i < this.mMatchOrder.length; i += MATCH_INSTANCE) {
            switch (this.mMatchOrder[i]) {
                case MATCH_INSTANCE /*1*/:
                    matchInstances(unmatchedStart, unmatchedEnd);
                    break;
                case MATCH_NAME /*2*/:
                    matchNames(unmatchedStart, unmatchedEnd, startValues.nameValues, endValues.nameValues);
                    break;
                case MATCH_ID /*3*/:
                    matchIds(unmatchedStart, unmatchedEnd, startValues.idValues, endValues.idValues);
                    break;
                case MATCH_LAST /*4*/:
                    matchItemIds(unmatchedStart, unmatchedEnd, startValues.itemIdValues, endValues.itemIdValues);
                    break;
                default:
                    break;
            }
        }
        addUnmatched(unmatchedStart, unmatchedEnd);
    }

    protected void createAnimators(ViewGroup sceneRoot, TransitionValuesMaps startValues, TransitionValuesMaps endValues, ArrayList<TransitionValues> startValuesList, ArrayList<TransitionValues> endValuesList) {
        int i;
        ArrayMap<Animator, AnimationInfo> runningAnimators = getRunningAnimators();
        long minStartDelay = Long.MAX_VALUE;
        int minAnimator = this.mAnimators.size();
        SparseLongArray startDelays = new SparseLongArray();
        int startValuesListCount = startValuesList.size();
        for (i = 0; i < startValuesListCount; i += MATCH_INSTANCE) {
            TransitionValues start = (TransitionValues) startValuesList.get(i);
            TransitionValues end = (TransitionValues) endValuesList.get(i);
            if (!(start == null || start.targetedTransitions.contains(this))) {
                start = null;
            }
            if (!(end == null || end.targetedTransitions.contains(this))) {
                end = null;
            }
            if (start != null || end != null) {
                boolean isChanged;
                if (start == null || end == null) {
                    isChanged = true;
                } else {
                    isChanged = isTransitionRequired(start, end);
                }
                if (isChanged) {
                    Object createAnimator = createAnimator(sceneRoot, start, end);
                    if (createAnimator != null) {
                        View view;
                        TransitionValues infoValues = null;
                        if (end != null) {
                            view = end.view;
                            String[] properties = getTransitionProperties();
                            if (!(view == null || properties == null || properties.length <= 0)) {
                                int j;
                                infoValues = new TransitionValues();
                                infoValues.view = view;
                                TransitionValues newValues = (TransitionValues) endValues.viewValues.get(view);
                                if (newValues != null) {
                                    for (j = 0; j < properties.length; j += MATCH_INSTANCE) {
                                        infoValues.values.put(properties[j], newValues.values.get(properties[j]));
                                    }
                                }
                                int numExistingAnims = runningAnimators.size();
                                for (j = 0; j < numExistingAnims; j += MATCH_INSTANCE) {
                                    AnimationInfo info = (AnimationInfo) runningAnimators.get((Animator) runningAnimators.keyAt(j));
                                    if (info.values != null && info.view == view && (((info.name == null && getName() == null) || info.name.equals(getName())) && info.values.equals(infoValues))) {
                                        createAnimator = null;
                                        break;
                                    }
                                }
                            }
                        } else {
                            view = start != null ? start.view : null;
                        }
                        if (createAnimator != null) {
                            if (this.mPropagation != null) {
                                long delay = this.mPropagation.getStartDelay(sceneRoot, this, start, end);
                                startDelays.put(this.mAnimators.size(), delay);
                                minStartDelay = Math.min(delay, minStartDelay);
                            }
                            runningAnimators.put(createAnimator, new AnimationInfo(view, getName(), this, sceneRoot.getWindowId(), infoValues));
                            this.mAnimators.add(createAnimator);
                        }
                    }
                }
            }
        }
        if (startDelays.size() != 0) {
            for (i = 0; i < startDelays.size(); i += MATCH_INSTANCE) {
                Animator animator = (Animator) this.mAnimators.get(startDelays.keyAt(i));
                animator.setStartDelay((startDelays.valueAt(i) - minStartDelay) + animator.getStartDelay());
            }
        }
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    boolean isValidTarget(View target) {
        if (target == null) {
            return DBG;
        }
        int targetId = target.getId();
        if (this.mTargetIdExcludes != null && this.mTargetIdExcludes.contains(Integer.valueOf(targetId))) {
            return DBG;
        }
        if (this.mTargetExcludes != null && this.mTargetExcludes.contains(target)) {
            return DBG;
        }
        int i;
        if (!(this.mTargetTypeExcludes == null || target == null)) {
            int numTypes = this.mTargetTypeExcludes.size();
            for (i = 0; i < numTypes; i += MATCH_INSTANCE) {
                if (((Class) this.mTargetTypeExcludes.get(i)).isInstance(target)) {
                    return DBG;
                }
            }
        }
        if (this.mTargetNameExcludes != null && target != null && target.getTransitionName() != null && this.mTargetNameExcludes.contains(target.getTransitionName())) {
            return DBG;
        }
        if ((this.mTargetIds.size() == 0 && this.mTargets.size() == 0 && ((this.mTargetTypes == null || this.mTargetTypes.isEmpty()) && (this.mTargetNames == null || this.mTargetNames.isEmpty()))) || this.mTargetIds.contains(Integer.valueOf(targetId)) || this.mTargets.contains(target)) {
            return true;
        }
        if (this.mTargetNames != null && this.mTargetNames.contains(target.getTransitionName())) {
            return true;
        }
        if (this.mTargetTypes != null) {
            for (i = 0; i < this.mTargetTypes.size(); i += MATCH_INSTANCE) {
                if (((Class) this.mTargetTypes.get(i)).isInstance(target)) {
                    return true;
                }
            }
        }
        return DBG;
    }

    private static ArrayMap<Animator, AnimationInfo> getRunningAnimators() {
        ArrayMap<Animator, AnimationInfo> runningAnimators = (ArrayMap) sRunningAnimators.get();
        if (runningAnimators != null) {
            return runningAnimators;
        }
        runningAnimators = new ArrayMap();
        sRunningAnimators.set(runningAnimators);
        return runningAnimators;
    }

    protected void runAnimators() {
        start();
        ArrayMap<Animator, AnimationInfo> runningAnimators = getRunningAnimators();
        for (Animator anim : this.mAnimators) {
            if (runningAnimators.containsKey(anim)) {
                start();
                runAnimator(anim, runningAnimators);
            }
        }
        this.mAnimators.clear();
        end();
    }

    private void runAnimator(Animator animator, ArrayMap<Animator, AnimationInfo> runningAnimators) {
        if (animator != null) {
            animator.addListener(new AnonymousClass2(runningAnimators));
            animate(animator);
        }
    }

    public Transition addTarget(int targetId) {
        if (targetId > 0) {
            this.mTargetIds.add(Integer.valueOf(targetId));
        }
        return this;
    }

    public Transition addTarget(String targetName) {
        if (targetName != null) {
            if (this.mTargetNames == null) {
                this.mTargetNames = new ArrayList();
            }
            this.mTargetNames.add(targetName);
        }
        return this;
    }

    public Transition addTarget(Class targetType) {
        if (targetType != null) {
            if (this.mTargetTypes == null) {
                this.mTargetTypes = new ArrayList();
            }
            this.mTargetTypes.add(targetType);
        }
        return this;
    }

    public Transition removeTarget(int targetId) {
        if (targetId > 0) {
            this.mTargetIds.remove(Integer.valueOf(targetId));
        }
        return this;
    }

    public Transition removeTarget(String targetName) {
        if (!(targetName == null || this.mTargetNames == null)) {
            this.mTargetNames.remove(targetName);
        }
        return this;
    }

    public Transition excludeTarget(int targetId, boolean exclude) {
        if (targetId >= 0) {
            this.mTargetIdExcludes = excludeObject(this.mTargetIdExcludes, Integer.valueOf(targetId), exclude);
        }
        return this;
    }

    public Transition excludeTarget(String targetName, boolean exclude) {
        this.mTargetNameExcludes = excludeObject(this.mTargetNameExcludes, targetName, exclude);
        return this;
    }

    public Transition excludeChildren(int targetId, boolean exclude) {
        if (targetId >= 0) {
            this.mTargetIdChildExcludes = excludeObject(this.mTargetIdChildExcludes, Integer.valueOf(targetId), exclude);
        }
        return this;
    }

    public Transition excludeTarget(View target, boolean exclude) {
        this.mTargetExcludes = excludeObject(this.mTargetExcludes, target, exclude);
        return this;
    }

    public Transition excludeChildren(View target, boolean exclude) {
        this.mTargetChildExcludes = excludeObject(this.mTargetChildExcludes, target, exclude);
        return this;
    }

    private static <T> ArrayList<T> excludeObject(ArrayList<T> list, T target, boolean exclude) {
        if (target == null) {
            return list;
        }
        if (exclude) {
            return ArrayListManager.add(list, target);
        }
        return ArrayListManager.remove(list, target);
    }

    public Transition excludeTarget(Class type, boolean exclude) {
        this.mTargetTypeExcludes = excludeObject(this.mTargetTypeExcludes, type, exclude);
        return this;
    }

    public Transition excludeChildren(Class type, boolean exclude) {
        this.mTargetTypeChildExcludes = excludeObject(this.mTargetTypeChildExcludes, type, exclude);
        return this;
    }

    public Transition addTarget(View target) {
        this.mTargets.add(target);
        return this;
    }

    public Transition removeTarget(View target) {
        if (target != null) {
            this.mTargets.remove(target);
        }
        return this;
    }

    public Transition removeTarget(Class target) {
        if (target != null) {
            this.mTargetTypes.remove(target);
        }
        return this;
    }

    public List<Integer> getTargetIds() {
        return this.mTargetIds;
    }

    public List<View> getTargets() {
        return this.mTargets;
    }

    public List<String> getTargetNames() {
        return this.mTargetNames;
    }

    public List<String> getTargetViewNames() {
        return this.mTargetNames;
    }

    public List<Class> getTargetTypes() {
        return this.mTargetTypes;
    }

    void captureValues(ViewGroup sceneRoot, boolean start) {
        int i;
        View view;
        clearValues(start);
        if ((this.mTargetIds.size() > 0 || this.mTargets.size() > 0) && ((this.mTargetNames == null || this.mTargetNames.isEmpty()) && (this.mTargetTypes == null || this.mTargetTypes.isEmpty()))) {
            TransitionValues values;
            for (i = 0; i < this.mTargetIds.size(); i += MATCH_INSTANCE) {
                view = sceneRoot.findViewById(((Integer) this.mTargetIds.get(i)).intValue());
                if (view != null) {
                    values = new TransitionValues();
                    values.view = view;
                    if (start) {
                        captureStartValues(values);
                    } else {
                        captureEndValues(values);
                    }
                    values.targetedTransitions.add(this);
                    capturePropagationValues(values);
                    if (start) {
                        addViewValues(this.mStartValues, view, values);
                    } else {
                        addViewValues(this.mEndValues, view, values);
                    }
                }
            }
            for (i = 0; i < this.mTargets.size(); i += MATCH_INSTANCE) {
                view = (View) this.mTargets.get(i);
                values = new TransitionValues();
                values.view = view;
                if (start) {
                    captureStartValues(values);
                } else {
                    captureEndValues(values);
                }
                values.targetedTransitions.add(this);
                capturePropagationValues(values);
                if (start) {
                    addViewValues(this.mStartValues, view, values);
                } else {
                    addViewValues(this.mEndValues, view, values);
                }
            }
        } else {
            captureHierarchy(sceneRoot, start);
        }
        if (!start && this.mNameOverrides != null) {
            int numOverrides = this.mNameOverrides.size();
            ArrayList<View> overriddenViews = new ArrayList(numOverrides);
            for (i = 0; i < numOverrides; i += MATCH_INSTANCE) {
                overriddenViews.add((View) this.mStartValues.nameValues.remove((String) this.mNameOverrides.keyAt(i)));
            }
            for (i = 0; i < numOverrides; i += MATCH_INSTANCE) {
                view = (View) overriddenViews.get(i);
                if (view != null) {
                    this.mStartValues.nameValues.put((String) this.mNameOverrides.valueAt(i), view);
                }
            }
        }
    }

    static void addViewValues(TransitionValuesMaps transitionValuesMaps, View view, TransitionValues transitionValues) {
        transitionValuesMaps.viewValues.put(view, transitionValues);
        int id = view.getId();
        if (id >= 0) {
            if (transitionValuesMaps.idValues.indexOfKey(id) >= 0) {
                transitionValuesMaps.idValues.put(id, null);
            } else {
                transitionValuesMaps.idValues.put(id, view);
            }
        }
        String name = view.getTransitionName();
        if (name != null) {
            if (transitionValuesMaps.nameValues.containsKey(name)) {
                transitionValuesMaps.nameValues.put(name, null);
            } else {
                transitionValuesMaps.nameValues.put(name, view);
            }
        }
        if (view.getParent() instanceof ListView) {
            ListView listview = (ListView) view.getParent();
            if (listview.getAdapter().hasStableIds()) {
                long itemId = listview.getItemIdAtPosition(listview.getPositionForView(view));
                if (transitionValuesMaps.itemIdValues.indexOfKey(itemId) >= 0) {
                    View alreadyMatched = (View) transitionValuesMaps.itemIdValues.get(itemId);
                    if (alreadyMatched != null) {
                        alreadyMatched.setHasTransientState(DBG);
                        transitionValuesMaps.itemIdValues.put(itemId, null);
                        return;
                    }
                    return;
                }
                view.setHasTransientState(true);
                transitionValuesMaps.itemIdValues.put(itemId, view);
            }
        }
    }

    void clearValues(boolean start) {
        if (start) {
            this.mStartValues.viewValues.clear();
            this.mStartValues.idValues.clear();
            this.mStartValues.itemIdValues.clear();
            this.mStartValues.nameValues.clear();
            this.mStartValuesList = null;
            return;
        }
        this.mEndValues.viewValues.clear();
        this.mEndValues.idValues.clear();
        this.mEndValues.itemIdValues.clear();
        this.mEndValues.nameValues.clear();
        this.mEndValuesList = null;
    }

    private void captureHierarchy(View view, boolean start) {
        if (view != null) {
            int id = view.getId();
            if (this.mTargetIdExcludes != null && this.mTargetIdExcludes.contains(Integer.valueOf(id))) {
                return;
            }
            if (this.mTargetExcludes == null || !this.mTargetExcludes.contains(view)) {
                int numTypes;
                int i;
                if (!(this.mTargetTypeExcludes == null || view == null)) {
                    numTypes = this.mTargetTypeExcludes.size();
                    i = 0;
                    while (i < numTypes) {
                        if (!((Class) this.mTargetTypeExcludes.get(i)).isInstance(view)) {
                            i += MATCH_INSTANCE;
                        } else {
                            return;
                        }
                    }
                }
                if (view.getParent() instanceof ViewGroup) {
                    TransitionValues values = new TransitionValues();
                    values.view = view;
                    if (start) {
                        captureStartValues(values);
                    } else {
                        captureEndValues(values);
                    }
                    values.targetedTransitions.add(this);
                    capturePropagationValues(values);
                    if (start) {
                        addViewValues(this.mStartValues, view, values);
                    } else {
                        addViewValues(this.mEndValues, view, values);
                    }
                }
                if ((view instanceof ViewGroup) && (this.mTargetIdChildExcludes == null || !this.mTargetIdChildExcludes.contains(Integer.valueOf(id)))) {
                    if (this.mTargetChildExcludes == null || !this.mTargetChildExcludes.contains(view)) {
                        if (this.mTargetTypeChildExcludes != null) {
                            numTypes = this.mTargetTypeChildExcludes.size();
                            i = 0;
                            while (i < numTypes) {
                                if (!((Class) this.mTargetTypeChildExcludes.get(i)).isInstance(view)) {
                                    i += MATCH_INSTANCE;
                                } else {
                                    return;
                                }
                            }
                        }
                        ViewGroup parent = (ViewGroup) view;
                        for (i = 0; i < parent.getChildCount(); i += MATCH_INSTANCE) {
                            captureHierarchy(parent.getChildAt(i), start);
                        }
                    }
                }
            }
        }
    }

    public TransitionValues getTransitionValues(View view, boolean start) {
        if (this.mParent != null) {
            return this.mParent.getTransitionValues(view, start);
        }
        return (TransitionValues) (start ? this.mStartValues : this.mEndValues).viewValues.get(view);
    }

    TransitionValues getMatchedTransitionValues(View view, boolean viewInStart) {
        if (this.mParent != null) {
            return this.mParent.getMatchedTransitionValues(view, viewInStart);
        }
        ArrayList<TransitionValues> lookIn = viewInStart ? this.mStartValuesList : this.mEndValuesList;
        if (lookIn == null) {
            return null;
        }
        TransitionValues values;
        int count = lookIn.size();
        int index = -1;
        for (int i = 0; i < count; i += MATCH_INSTANCE) {
            values = (TransitionValues) lookIn.get(i);
            if (values == null) {
                return null;
            }
            if (values.view == view) {
                index = i;
                break;
            }
        }
        values = null;
        if (index >= 0) {
            values = (TransitionValues) (viewInStart ? this.mEndValuesList : this.mStartValuesList).get(index);
        }
        return values;
    }

    public void pause(View sceneRoot) {
        if (!this.mEnded) {
            int i;
            ArrayMap<Animator, AnimationInfo> runningAnimators = getRunningAnimators();
            int numOldAnims = runningAnimators.size();
            if (sceneRoot != null) {
                WindowId windowId = sceneRoot.getWindowId();
                for (i = numOldAnims - 1; i >= 0; i--) {
                    AnimationInfo info = (AnimationInfo) runningAnimators.valueAt(i);
                    if (!(info.view == null || windowId == null || !windowId.equals(info.windowId))) {
                        ((Animator) runningAnimators.keyAt(i)).pause();
                    }
                }
            }
            if (this.mListeners != null && this.mListeners.size() > 0) {
                ArrayList<TransitionListener> tmpListeners = (ArrayList) this.mListeners.clone();
                int numListeners = tmpListeners.size();
                for (i = 0; i < numListeners; i += MATCH_INSTANCE) {
                    ((TransitionListener) tmpListeners.get(i)).onTransitionPause(this);
                }
            }
            this.mPaused = true;
        }
    }

    public void resume(View sceneRoot) {
        if (this.mPaused) {
            if (!this.mEnded) {
                int i;
                ArrayMap<Animator, AnimationInfo> runningAnimators = getRunningAnimators();
                int numOldAnims = runningAnimators.size();
                WindowId windowId = sceneRoot.getWindowId();
                for (i = numOldAnims - 1; i >= 0; i--) {
                    AnimationInfo info = (AnimationInfo) runningAnimators.valueAt(i);
                    if (!(info.view == null || windowId == null || !windowId.equals(info.windowId))) {
                        ((Animator) runningAnimators.keyAt(i)).resume();
                    }
                }
                if (this.mListeners != null && this.mListeners.size() > 0) {
                    ArrayList<TransitionListener> tmpListeners = (ArrayList) this.mListeners.clone();
                    int numListeners = tmpListeners.size();
                    for (i = 0; i < numListeners; i += MATCH_INSTANCE) {
                        ((TransitionListener) tmpListeners.get(i)).onTransitionResume(this);
                    }
                }
            }
            this.mPaused = DBG;
        }
    }

    void playTransition(ViewGroup sceneRoot) {
        this.mStartValuesList = new ArrayList();
        this.mEndValuesList = new ArrayList();
        matchStartAndEnd(this.mStartValues, this.mEndValues);
        ArrayMap<Animator, AnimationInfo> runningAnimators = getRunningAnimators();
        int numOldAnims = runningAnimators.size();
        WindowId windowId = sceneRoot.getWindowId();
        for (int i = numOldAnims - 1; i >= 0; i--) {
            Animator anim = (Animator) runningAnimators.keyAt(i);
            if (anim != null) {
                AnimationInfo oldInfo = (AnimationInfo) runningAnimators.get(anim);
                if (!(oldInfo == null || oldInfo.view == null || oldInfo.windowId != windowId)) {
                    boolean cancel;
                    TransitionValues oldValues = oldInfo.values;
                    View oldView = oldInfo.view;
                    TransitionValues startValues = getTransitionValues(oldView, true);
                    TransitionValues endValues = getMatchedTransitionValues(oldView, true);
                    if (startValues == null && endValues == null) {
                        endValues = (TransitionValues) this.mEndValues.viewValues.get(oldView);
                    }
                    if (startValues == null && endValues == null) {
                        cancel = DBG;
                    } else {
                        cancel = oldInfo.transition.isTransitionRequired(oldValues, endValues);
                    }
                    if (cancel) {
                        if (anim.isRunning() || anim.isStarted()) {
                            anim.cancel();
                        } else {
                            runningAnimators.remove(anim);
                        }
                    }
                }
            }
        }
        createAnimators(sceneRoot, this.mStartValues, this.mEndValues, this.mStartValuesList, this.mEndValuesList);
        runAnimators();
    }

    public boolean isTransitionRequired(TransitionValues startValues, TransitionValues endValues) {
        if (startValues == null || endValues == null) {
            return DBG;
        }
        String[] properties = getTransitionProperties();
        if (properties != null) {
            int count = properties.length;
            for (int i = 0; i < count; i += MATCH_INSTANCE) {
                if (isValueChanged(startValues, endValues, properties[i])) {
                    return true;
                }
            }
            return DBG;
        }
        for (String key : startValues.values.keySet()) {
            if (isValueChanged(startValues, endValues, key)) {
                return true;
            }
        }
        return DBG;
    }

    private static boolean isValueChanged(TransitionValues oldValues, TransitionValues newValues, String key) {
        if (oldValues.values.containsKey(key) != newValues.values.containsKey(key)) {
            return DBG;
        }
        Object oldValue = oldValues.values.get(key);
        Object newValue = newValues.values.get(key);
        boolean changed = (oldValue == null && newValue == null) ? DBG : (oldValue == null || newValue == null) ? true : oldValue.equals(newValue) ? DBG : true;
        return changed;
    }

    protected void animate(Animator animator) {
        if (animator == null) {
            end();
            return;
        }
        if (getDuration() >= 0) {
            animator.setDuration(getDuration());
        }
        if (getStartDelay() >= 0) {
            animator.setStartDelay(getStartDelay() + animator.getStartDelay());
        }
        if (getInterpolator() != null) {
            animator.setInterpolator(getInterpolator());
        }
        animator.addListener(new AnimatorListenerAdapter() {
            public void onAnimationEnd(Animator animation) {
                Transition.this.end();
                animation.removeListener(this);
            }
        });
        animator.start();
    }

    protected void start() {
        if (this.mNumInstances == 0) {
            if (this.mListeners != null && this.mListeners.size() > 0) {
                ArrayList<TransitionListener> tmpListeners = (ArrayList) this.mListeners.clone();
                int numListeners = tmpListeners.size();
                for (int i = 0; i < numListeners; i += MATCH_INSTANCE) {
                    ((TransitionListener) tmpListeners.get(i)).onTransitionStart(this);
                }
            }
            this.mEnded = DBG;
        }
        this.mNumInstances += MATCH_INSTANCE;
    }

    protected void end() {
        this.mNumInstances--;
        if (this.mNumInstances == 0) {
            int i;
            View view;
            if (this.mListeners != null && this.mListeners.size() > 0) {
                ArrayList<TransitionListener> tmpListeners = (ArrayList) this.mListeners.clone();
                int numListeners = tmpListeners.size();
                for (i = 0; i < numListeners; i += MATCH_INSTANCE) {
                    ((TransitionListener) tmpListeners.get(i)).onTransitionEnd(this);
                }
            }
            for (i = 0; i < this.mStartValues.itemIdValues.size(); i += MATCH_INSTANCE) {
                view = (View) this.mStartValues.itemIdValues.valueAt(i);
                if (view != null) {
                    view.setHasTransientState(DBG);
                }
            }
            for (i = 0; i < this.mEndValues.itemIdValues.size(); i += MATCH_INSTANCE) {
                view = (View) this.mEndValues.itemIdValues.valueAt(i);
                if (view != null) {
                    view.setHasTransientState(DBG);
                }
            }
            this.mEnded = true;
        }
    }

    protected void cancel() {
        int i;
        for (i = this.mCurrentAnimators.size() - 1; i >= 0; i--) {
            ((Animator) this.mCurrentAnimators.get(i)).cancel();
        }
        if (this.mListeners != null && this.mListeners.size() > 0) {
            ArrayList<TransitionListener> tmpListeners = (ArrayList) this.mListeners.clone();
            int numListeners = tmpListeners.size();
            for (i = 0; i < numListeners; i += MATCH_INSTANCE) {
                ((TransitionListener) tmpListeners.get(i)).onTransitionCancel(this);
            }
        }
    }

    public Transition addListener(TransitionListener listener) {
        if (this.mListeners == null) {
            this.mListeners = new ArrayList();
        }
        this.mListeners.add(listener);
        return this;
    }

    public Transition removeListener(TransitionListener listener) {
        if (this.mListeners == null) {
            return this;
        }
        this.mListeners.remove(listener);
        if (this.mListeners.size() == 0) {
            this.mListeners = null;
        }
        return this;
    }

    public void setEpicenterCallback(EpicenterCallback epicenterCallback) {
        this.mEpicenterCallback = epicenterCallback;
    }

    public EpicenterCallback getEpicenterCallback() {
        return this.mEpicenterCallback;
    }

    public Rect getEpicenter() {
        if (this.mEpicenterCallback == null) {
            return null;
        }
        return this.mEpicenterCallback.onGetEpicenter(this);
    }

    public void setPathMotion(PathMotion pathMotion) {
        if (pathMotion == null) {
            this.mPathMotion = STRAIGHT_PATH_MOTION;
        } else {
            this.mPathMotion = pathMotion;
        }
    }

    public PathMotion getPathMotion() {
        return this.mPathMotion;
    }

    public void setPropagation(TransitionPropagation transitionPropagation) {
        this.mPropagation = transitionPropagation;
    }

    public TransitionPropagation getPropagation() {
        return this.mPropagation;
    }

    void capturePropagationValues(TransitionValues transitionValues) {
        if (!(this.mPropagation == null || transitionValues.values.isEmpty())) {
            String[] propertyNames = this.mPropagation.getPropagationProperties();
            if (propertyNames != null) {
                boolean containsAll = true;
                for (int i = 0; i < propertyNames.length; i += MATCH_INSTANCE) {
                    if (!transitionValues.values.containsKey(propertyNames[i])) {
                        containsAll = DBG;
                        break;
                    }
                }
                if (!containsAll) {
                    this.mPropagation.captureValues(transitionValues);
                }
            }
        }
    }

    Transition setSceneRoot(ViewGroup sceneRoot) {
        this.mSceneRoot = sceneRoot;
        return this;
    }

    void setCanRemoveViews(boolean canRemoveViews) {
        this.mCanRemoveViews = canRemoveViews;
    }

    public boolean canRemoveViews() {
        return this.mCanRemoveViews;
    }

    public void setNameOverrides(ArrayMap<String, String> overrides) {
        this.mNameOverrides = overrides;
    }

    public ArrayMap<String, String> getNameOverrides() {
        return this.mNameOverrides;
    }

    public String toString() {
        return toString("");
    }

    public Transition clone() {
        Transition transition = null;
        try {
            transition = (Transition) super.clone();
            transition.mAnimators = new ArrayList();
            transition.mStartValues = new TransitionValuesMaps();
            transition.mEndValues = new TransitionValuesMaps();
            transition.mStartValuesList = null;
            transition.mEndValuesList = null;
            return transition;
        } catch (CloneNotSupportedException e) {
            return transition;
        }
    }

    public String getName() {
        return this.mName;
    }

    String toString(String indent) {
        String result = indent + getClass().getSimpleName() + "@" + Integer.toHexString(hashCode()) + ": ";
        if (this.mDuration != -1) {
            result = result + "dur(" + this.mDuration + ") ";
        }
        if (this.mStartDelay != -1) {
            result = result + "dly(" + this.mStartDelay + ") ";
        }
        if (this.mInterpolator != null) {
            result = result + "interp(" + this.mInterpolator + ") ";
        }
        if (this.mTargetIds.size() <= 0 && this.mTargets.size() <= 0) {
            return result;
        }
        int i;
        result = result + "tgts(";
        if (this.mTargetIds.size() > 0) {
            for (i = 0; i < this.mTargetIds.size(); i += MATCH_INSTANCE) {
                if (i > 0) {
                    result = result + ", ";
                }
                result = result + this.mTargetIds.get(i);
            }
        }
        if (this.mTargets.size() > 0) {
            for (i = 0; i < this.mTargets.size(); i += MATCH_INSTANCE) {
                if (i > 0) {
                    result = result + ", ";
                }
                result = result + this.mTargets.get(i);
            }
        }
        return result + ")";
    }
}
