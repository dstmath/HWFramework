package android.animation;

import android.animation.Animator.AnimatorListener;
import android.app.ActivityThread;
import android.app.Application;
import android.hardware.camera2.params.TonemapCurve;
import android.net.wifi.WifiEnterpriseConfig;
import android.os.Looper;
import android.util.AndroidRuntimeException;
import android.util.ArrayMap;
import android.util.Log;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

public final class AnimatorSet extends Animator implements AnimationFrameCallback {
    private static final String TAG = "AnimatorSet";
    private boolean mChildrenInitialized = false;
    private ValueAnimator mDelayAnim = ValueAnimator.ofFloat(TonemapCurve.LEVEL_BLACK, 1.0f).setDuration(0);
    private boolean mDependencyDirty = false;
    private AnimatorListenerAdapter mDummyListener = new AnimatorListenerAdapter() {
        public void onAnimationEnd(Animator animation) {
            if (AnimatorSet.this.mNodeMap.get(animation) == null) {
                Log.w(AnimatorSet.TAG, "onAnimationEnd: animation = " + animation + "\n" + "mDummyListener = " + this);
                throw new AndroidRuntimeException("Error: animation ended is not in the node map");
            } else {
                ((Node) AnimatorSet.this.mNodeMap.get(animation)).mEnded = true;
            }
        }
    };
    private long mDuration = -1;
    private final boolean mEndCanBeCalled;
    private ArrayList<AnimationEvent> mEvents = new ArrayList();
    private long mFirstFrame = -1;
    private TimeInterpolator mInterpolator = null;
    private int mLastEventId = -1;
    private long mLastFrameTime = -1;
    private ArrayMap<Animator, Node> mNodeMap = new ArrayMap();
    private ArrayList<Node> mNodes = new ArrayList();
    private long mPauseTime = -1;
    private ArrayList<Node> mPlayingSet = new ArrayList();
    private boolean mReversing = false;
    private Node mRootNode = new Node(this.mDelayAnim);
    private SeekState mSeekState = new SeekState(this, null);
    private boolean mSelfPulse = true;
    private final boolean mShouldIgnoreEndWithoutStart;
    private final boolean mShouldResetValuesAtStart;
    private long mStartDelay = 0;
    private boolean mStarted = false;
    private long mTotalDuration = 0;

    private static class AnimationEvent {
        static final int ANIMATION_DELAY_ENDED = 1;
        static final int ANIMATION_END = 2;
        static final int ANIMATION_START = 0;
        final int mEvent;
        final Node mNode;

        AnimationEvent(Node node, int event) {
            this.mNode = node;
            this.mEvent = event;
        }

        long getTime() {
            long j = -1;
            if (this.mEvent == 0) {
                return this.mNode.mStartTime;
            }
            if (this.mEvent != 1) {
                return this.mNode.mEndTime;
            }
            if (this.mNode.mStartTime != -1) {
                j = this.mNode.mStartTime + this.mNode.mAnimation.getStartDelay();
            }
            return j;
        }

        public String toString() {
            String eventStr = this.mEvent == 0 ? "start" : this.mEvent == 1 ? "delay ended" : TtmlUtils.ATTR_END;
            return eventStr + WifiEnterpriseConfig.CA_CERT_ALIAS_DELIMITER + this.mNode.mAnimation.toString();
        }
    }

    public class Builder {
        private Node mCurrentNode;

        Builder(Animator anim) {
            AnimatorSet.this.mDependencyDirty = true;
            this.mCurrentNode = AnimatorSet.this.getNodeForAnimation(anim);
        }

        public Builder with(Animator anim) {
            this.mCurrentNode.addSibling(AnimatorSet.this.getNodeForAnimation(anim));
            return this;
        }

        public Builder before(Animator anim) {
            this.mCurrentNode.addChild(AnimatorSet.this.getNodeForAnimation(anim));
            return this;
        }

        public Builder after(Animator anim) {
            this.mCurrentNode.addParent(AnimatorSet.this.getNodeForAnimation(anim));
            return this;
        }

        public Builder after(long delay) {
            Animator anim = ValueAnimator.ofFloat(TonemapCurve.LEVEL_BLACK, 1.0f);
            anim.setDuration(delay);
            after(anim);
            return this;
        }
    }

    private static class Node implements Cloneable {
        Animator mAnimation;
        ArrayList<Node> mChildNodes = null;
        long mEndTime = 0;
        boolean mEnded = false;
        Node mLatestParent = null;
        ArrayList<Node> mParents;
        boolean mParentsAdded = false;
        ArrayList<Node> mSiblings;
        long mStartTime = 0;
        long mTotalDuration = 0;

        public Node(Animator animation) {
            this.mAnimation = animation;
        }

        public Node clone() {
            try {
                Node node = (Node) super.clone();
                node.mAnimation = this.mAnimation.clone();
                if (this.mChildNodes != null) {
                    node.mChildNodes = new ArrayList(this.mChildNodes);
                }
                if (this.mSiblings != null) {
                    node.mSiblings = new ArrayList(this.mSiblings);
                }
                if (this.mParents != null) {
                    node.mParents = new ArrayList(this.mParents);
                }
                node.mEnded = false;
                return node;
            } catch (CloneNotSupportedException e) {
                throw new AssertionError();
            }
        }

        void addChild(Node node) {
            if (this.mChildNodes == null) {
                this.mChildNodes = new ArrayList();
            }
            if (!this.mChildNodes.contains(node)) {
                this.mChildNodes.add(node);
                node.addParent(this);
            }
        }

        public void addSibling(Node node) {
            if (this.mSiblings == null) {
                this.mSiblings = new ArrayList();
            }
            if (!this.mSiblings.contains(node)) {
                this.mSiblings.add(node);
                node.addSibling(this);
            }
        }

        public void addParent(Node node) {
            if (this.mParents == null) {
                this.mParents = new ArrayList();
            }
            if (!this.mParents.contains(node)) {
                this.mParents.add(node);
                node.addChild(this);
            }
        }

        public void addParents(ArrayList<Node> parents) {
            if (parents != null) {
                int size = parents.size();
                for (int i = 0; i < size; i++) {
                    addParent((Node) parents.get(i));
                }
            }
        }
    }

    private class SeekState {
        private long mPlayTime;
        private boolean mSeekingInReverse;

        /* synthetic */ SeekState(AnimatorSet this$0, SeekState -this1) {
            this();
        }

        private SeekState() {
            this.mPlayTime = -1;
            this.mSeekingInReverse = false;
        }

        void reset() {
            this.mPlayTime = -1;
            this.mSeekingInReverse = false;
        }

        void setPlayTime(long playTime, boolean inReverse) {
            if (AnimatorSet.this.getTotalDuration() != -1) {
                this.mPlayTime = Math.min(playTime, AnimatorSet.this.getTotalDuration() - AnimatorSet.this.mStartDelay);
            }
            this.mPlayTime = Math.max(0, this.mPlayTime);
            this.mSeekingInReverse = inReverse;
        }

        void updateSeekDirection(boolean inReverse) {
            if (inReverse && AnimatorSet.this.getTotalDuration() == -1) {
                throw new UnsupportedOperationException("Error: Cannot reverse infinite animator set");
            } else if (this.mPlayTime >= 0 && inReverse != this.mSeekingInReverse) {
                this.mPlayTime = (AnimatorSet.this.getTotalDuration() - AnimatorSet.this.mStartDelay) - this.mPlayTime;
                this.mSeekingInReverse = inReverse;
            }
        }

        long getPlayTime() {
            return this.mPlayTime;
        }

        long getPlayTimeNormalized() {
            if (AnimatorSet.this.mReversing) {
                return (AnimatorSet.this.getTotalDuration() - AnimatorSet.this.mStartDelay) - this.mPlayTime;
            }
            return this.mPlayTime;
        }

        boolean isActive() {
            return this.mPlayTime != -1;
        }
    }

    public AnimatorSet() {
        boolean isPreO;
        this.mNodeMap.put(this.mDelayAnim, this.mRootNode);
        this.mNodes.add(this.mRootNode);
        Application app = ActivityThread.currentApplication();
        if (app == null || app.getApplicationInfo() == null) {
            this.mShouldIgnoreEndWithoutStart = true;
            isPreO = true;
        } else {
            if (app.getApplicationInfo().targetSdkVersion < 24) {
                this.mShouldIgnoreEndWithoutStart = true;
            } else {
                this.mShouldIgnoreEndWithoutStart = false;
            }
            isPreO = app.getApplicationInfo().targetSdkVersion < 26;
        }
        this.mShouldResetValuesAtStart = isPreO ^ 1;
        this.mEndCanBeCalled = isPreO ^ 1;
    }

    public void playTogether(Animator... items) {
        if (items != null) {
            Builder builder = play(items[0]);
            for (int i = 1; i < items.length; i++) {
                builder.with(items[i]);
            }
        }
    }

    public void playTogether(Collection<Animator> items) {
        if (items != null && items.size() > 0) {
            Builder builder = null;
            for (Animator anim : items) {
                if (builder == null) {
                    builder = play(anim);
                } else {
                    builder.with(anim);
                }
            }
        }
    }

    public void playSequentially(Animator... items) {
        if (items == null) {
            return;
        }
        if (items.length == 1) {
            play(items[0]);
            return;
        }
        for (int i = 0; i < items.length - 1; i++) {
            play(items[i]).before(items[i + 1]);
        }
    }

    public void playSequentially(List<Animator> items) {
        if (items != null && items.size() > 0) {
            if (items.size() == 1) {
                play((Animator) items.get(0));
                return;
            }
            for (int i = 0; i < items.size() - 1; i++) {
                play((Animator) items.get(i)).before((Animator) items.get(i + 1));
            }
        }
    }

    public ArrayList<Animator> getChildAnimations() {
        ArrayList<Animator> childList = new ArrayList();
        int size = this.mNodes.size();
        for (int i = 0; i < size; i++) {
            Node node = (Node) this.mNodes.get(i);
            if (node != this.mRootNode) {
                childList.add(node.mAnimation);
            }
        }
        return childList;
    }

    public void setTarget(Object target) {
        int size = this.mNodes.size();
        for (int i = 0; i < size; i++) {
            Animator animation = ((Node) this.mNodes.get(i)).mAnimation;
            if (animation instanceof AnimatorSet) {
                ((AnimatorSet) animation).setTarget(target);
            } else if (animation instanceof ObjectAnimator) {
                ((ObjectAnimator) animation).setTarget(target);
            }
        }
    }

    public int getChangingConfigurations() {
        int conf = super.getChangingConfigurations();
        for (int i = 0; i < this.mNodes.size(); i++) {
            conf |= ((Node) this.mNodes.get(i)).mAnimation.getChangingConfigurations();
        }
        return conf;
    }

    public void setInterpolator(TimeInterpolator interpolator) {
        this.mInterpolator = interpolator;
    }

    public TimeInterpolator getInterpolator() {
        return this.mInterpolator;
    }

    public Builder play(Animator anim) {
        if (anim != null) {
            return new Builder(anim);
        }
        return null;
    }

    public void cancel() {
        if (Looper.myLooper() == null) {
            throw new AndroidRuntimeException("Animators may only be run on Looper threads");
        } else if (isStarted()) {
            int i;
            if (this.mListeners != null) {
                ArrayList<AnimatorListener> tmpListeners = (ArrayList) this.mListeners.clone();
                int size = tmpListeners.size();
                for (i = 0; i < size; i++) {
                    ((AnimatorListener) tmpListeners.get(i)).onAnimationCancel(this);
                }
            }
            ArrayList<Node> playingSet = new ArrayList(this.mPlayingSet);
            int setSize = playingSet.size();
            for (i = 0; i < setSize; i++) {
                ((Node) playingSet.get(i)).mAnimation.cancel();
            }
            this.mPlayingSet.clear();
            endAnimation();
        }
    }

    private void forceToEnd() {
        if (this.mEndCanBeCalled) {
            end();
            return;
        }
        if (this.mReversing) {
            handleAnimationEvents(this.mLastEventId, 0, getTotalDuration());
        } else {
            long zeroScalePlayTime = getTotalDuration();
            if (zeroScalePlayTime == -1) {
                zeroScalePlayTime = 2147483647L;
            }
            handleAnimationEvents(this.mLastEventId, this.mEvents.size() - 1, zeroScalePlayTime);
        }
        this.mPlayingSet.clear();
        endAnimation();
    }

    public void end() {
        if (Looper.myLooper() == null) {
            throw new AndroidRuntimeException("Animators may only be run on Looper threads");
        } else if (!this.mShouldIgnoreEndWithoutStart || (isStarted() ^ 1) == 0) {
            if (isStarted()) {
                AnimationEvent event;
                Animator anim;
                if (this.mReversing) {
                    this.mLastEventId = this.mLastEventId == -1 ? this.mEvents.size() : this.mLastEventId;
                    while (this.mLastEventId > 0) {
                        this.mLastEventId--;
                        event = (AnimationEvent) this.mEvents.get(this.mLastEventId);
                        anim = event.mNode.mAnimation;
                        if (!((Node) this.mNodeMap.get(anim)).mEnded) {
                            if (event.mEvent == 2) {
                                anim.reverse();
                            } else if (event.mEvent == 1 && anim.isStarted()) {
                                anim.end();
                            }
                        }
                    }
                } else {
                    while (this.mLastEventId < this.mEvents.size() - 1) {
                        this.mLastEventId++;
                        event = (AnimationEvent) this.mEvents.get(this.mLastEventId);
                        anim = event.mNode.mAnimation;
                        if (!((Node) this.mNodeMap.get(anim)).mEnded) {
                            if (event.mEvent == 0) {
                                anim.start();
                            } else if (event.mEvent == 2 && anim.isStarted()) {
                                anim.end();
                            }
                        }
                    }
                }
                this.mPlayingSet.clear();
            }
            endAnimation();
        }
    }

    public boolean isRunning() {
        if (this.mStartDelay == 0) {
            return this.mStarted;
        }
        return this.mLastFrameTime > 0;
    }

    public boolean isStarted() {
        return this.mStarted;
    }

    public long getStartDelay() {
        return this.mStartDelay;
    }

    public void setStartDelay(long startDelay) {
        if (startDelay < 0) {
            Log.w(TAG, "Start delay should always be non-negative");
            startDelay = 0;
        }
        long delta = startDelay - this.mStartDelay;
        if (delta != 0) {
            this.mStartDelay = startDelay;
            if (!this.mDependencyDirty) {
                int size = this.mNodes.size();
                for (int i = 0; i < size; i++) {
                    Node node = (Node) this.mNodes.get(i);
                    if (node == this.mRootNode) {
                        node.mEndTime = this.mStartDelay;
                    } else {
                        long j;
                        if (node.mStartTime == -1) {
                            j = -1;
                        } else {
                            j = node.mStartTime + delta;
                        }
                        node.mStartTime = j;
                        if (node.mEndTime == -1) {
                            j = -1;
                        } else {
                            j = node.mEndTime + delta;
                        }
                        node.mEndTime = j;
                    }
                }
                if (this.mTotalDuration != -1) {
                    this.mTotalDuration += delta;
                }
            }
        }
    }

    public long getDuration() {
        return this.mDuration;
    }

    public AnimatorSet setDuration(long duration) {
        if (duration < 0) {
            throw new IllegalArgumentException("duration must be a value of zero or greater");
        }
        this.mDependencyDirty = true;
        this.mDuration = duration;
        return this;
    }

    public void setupStartValues() {
        int size = this.mNodes.size();
        for (int i = 0; i < size; i++) {
            Node node = (Node) this.mNodes.get(i);
            if (node != this.mRootNode) {
                node.mAnimation.setupStartValues();
            }
        }
    }

    public void setupEndValues() {
        int size = this.mNodes.size();
        for (int i = 0; i < size; i++) {
            Node node = (Node) this.mNodes.get(i);
            if (node != this.mRootNode) {
                node.mAnimation.setupEndValues();
            }
        }
    }

    public void pause() {
        if (Looper.myLooper() == null) {
            throw new AndroidRuntimeException("Animators may only be run on Looper threads");
        }
        boolean previouslyPaused = this.mPaused;
        super.pause();
        if (!previouslyPaused && this.mPaused) {
            this.mPauseTime = -1;
        }
    }

    public void resume() {
        if (Looper.myLooper() == null) {
            throw new AndroidRuntimeException("Animators may only be run on Looper threads");
        }
        boolean previouslyPaused = this.mPaused;
        super.resume();
        if (previouslyPaused && (this.mPaused ^ 1) != 0 && this.mPauseTime >= 0) {
            addAnimationCallback(0);
        }
    }

    public void start() {
        start(false, true);
    }

    void startWithoutPulsing(boolean inReverse) {
        start(inReverse, false);
    }

    private void initAnimation() {
        if (this.mInterpolator != null) {
            for (int i = 0; i < this.mNodes.size(); i++) {
                ((Node) this.mNodes.get(i)).mAnimation.setInterpolator(this.mInterpolator);
            }
        }
        updateAnimatorsDuration();
        createDependencyGraph();
    }

    private void start(boolean inReverse, boolean selfPulse) {
        if (Looper.myLooper() == null) {
            throw new AndroidRuntimeException("Animators may only be run on Looper threads");
        }
        int i;
        this.mStarted = true;
        this.mSelfPulse = selfPulse;
        this.mPaused = false;
        this.mPauseTime = -1;
        int size = this.mNodes.size();
        for (i = 0; i < size; i++) {
            Node node = (Node) this.mNodes.get(i);
            node.mEnded = false;
            node.mAnimation.setAllowRunningAsynchronously(false);
        }
        initAnimation();
        if (!inReverse || (canReverse() ^ 1) == 0) {
            this.mReversing = inReverse;
            boolean isEmptySet = isEmptySet(this);
            if (!isEmptySet) {
                startAnimation();
            }
            if (this.mListeners != null) {
                ArrayList<AnimatorListener> tmpListeners = (ArrayList) this.mListeners.clone();
                int numListeners = tmpListeners.size();
                for (i = 0; i < numListeners; i++) {
                    ((AnimatorListener) tmpListeners.get(i)).onAnimationStart(this, inReverse);
                }
            }
            if (isEmptySet) {
                end();
                return;
            }
            return;
        }
        throw new UnsupportedOperationException("Cannot reverse infinite AnimatorSet");
    }

    private static boolean isEmptySet(AnimatorSet set) {
        if (set.getStartDelay() > 0) {
            return false;
        }
        for (int i = 0; i < set.getChildAnimations().size(); i++) {
            Animator anim = (Animator) set.getChildAnimations().get(i);
            if (!(anim instanceof AnimatorSet) || !isEmptySet((AnimatorSet) anim)) {
                return false;
            }
        }
        return true;
    }

    private void updateAnimatorsDuration() {
        if (this.mDuration >= 0) {
            int size = this.mNodes.size();
            for (int i = 0; i < size; i++) {
                ((Node) this.mNodes.get(i)).mAnimation.setDuration(this.mDuration);
            }
        }
        this.mDelayAnim.setDuration(this.mStartDelay);
    }

    void skipToEndValue(boolean inReverse) {
        if (isInitialized()) {
            initAnimation();
            int i;
            if (inReverse) {
                for (i = this.mEvents.size() - 1; i >= 0; i--) {
                    if (((AnimationEvent) this.mEvents.get(i)).mEvent == 1) {
                        ((AnimationEvent) this.mEvents.get(i)).mNode.mAnimation.skipToEndValue(true);
                    }
                }
                return;
            }
            for (i = 0; i < this.mEvents.size(); i++) {
                if (((AnimationEvent) this.mEvents.get(i)).mEvent == 2) {
                    ((AnimationEvent) this.mEvents.get(i)).mNode.mAnimation.skipToEndValue(false);
                }
            }
            return;
        }
        throw new UnsupportedOperationException("Children must be initialized.");
    }

    void animateBasedOnPlayTime(long currentPlayTime, long lastPlayTime, boolean inReverse) {
        if (currentPlayTime < 0 || lastPlayTime < 0) {
            throw new UnsupportedOperationException("Error: Play time should never be negative.");
        }
        int i;
        if (inReverse) {
            if (getTotalDuration() == -1) {
                throw new UnsupportedOperationException("Cannot reverse AnimatorSet with infinite duration");
            }
            long duration = getTotalDuration() - this.mStartDelay;
            currentPlayTime = duration - Math.min(currentPlayTime, duration);
            lastPlayTime = duration - lastPlayTime;
            inReverse = false;
        }
        skipToStartValue(false);
        ArrayList<Node> unfinishedNodes = new ArrayList();
        for (i = 0; i < this.mEvents.size(); i++) {
            AnimationEvent event = (AnimationEvent) this.mEvents.get(i);
            if (event.getTime() > currentPlayTime) {
                break;
            }
            if (event.mEvent == 1 && (event.mNode.mEndTime == -1 || event.mNode.mEndTime > currentPlayTime)) {
                unfinishedNodes.add(event.mNode);
            }
            if (event.mEvent == 2) {
                event.mNode.mAnimation.skipToEndValue(false);
            }
        }
        for (i = 0; i < unfinishedNodes.size(); i++) {
            Node node = (Node) unfinishedNodes.get(i);
            long playTime = getPlayTimeForNode(currentPlayTime, node, inReverse);
            if (!inReverse) {
                playTime -= node.mAnimation.getStartDelay();
            }
            node.mAnimation.animateBasedOnPlayTime(playTime, lastPlayTime, inReverse);
        }
    }

    boolean isInitialized() {
        if (this.mChildrenInitialized) {
            return true;
        }
        boolean allInitialized = true;
        for (int i = 0; i < this.mNodes.size(); i++) {
            if (!((Node) this.mNodes.get(i)).mAnimation.isInitialized()) {
                allInitialized = false;
                break;
            }
        }
        this.mChildrenInitialized = allInitialized;
        return this.mChildrenInitialized;
    }

    private void skipToStartValue(boolean inReverse) {
        skipToEndValue(inReverse ^ 1);
    }

    public void setCurrentPlayTime(long playTime) {
        if (this.mReversing && getTotalDuration() == -1) {
            throw new UnsupportedOperationException("Error: Cannot seek in reverse in an infinite AnimatorSet");
        } else if ((getTotalDuration() == -1 || playTime <= getTotalDuration() - this.mStartDelay) && playTime >= 0) {
            initAnimation();
            if (isStarted()) {
                this.mSeekState.setPlayTime(playTime, this.mReversing);
            } else if (this.mReversing) {
                throw new UnsupportedOperationException("Error: Something went wrong. mReversing should not be set when AnimatorSet is not started.");
            } else {
                if (!this.mSeekState.isActive()) {
                    findLatestEventIdForTime(0);
                    initChildren();
                    skipToStartValue(this.mReversing);
                    this.mSeekState.setPlayTime(0, this.mReversing);
                }
                animateBasedOnPlayTime(playTime, 0, this.mReversing);
                this.mSeekState.setPlayTime(playTime, this.mReversing);
            }
        } else {
            throw new UnsupportedOperationException("Error: Play time should always be in between0 and duration.");
        }
    }

    public long getCurrentPlayTime() {
        if (this.mSeekState.isActive()) {
            return this.mSeekState.getPlayTime();
        }
        if (this.mLastFrameTime == -1) {
            return 0;
        }
        float durationScale = ValueAnimator.getDurationScale();
        if (durationScale == TonemapCurve.LEVEL_BLACK) {
            durationScale = 1.0f;
        }
        if (this.mReversing) {
            return (long) (((float) (this.mLastFrameTime - this.mFirstFrame)) / durationScale);
        }
        return (long) (((float) ((this.mLastFrameTime - this.mFirstFrame) - this.mStartDelay)) / durationScale);
    }

    private void initChildren() {
        if (!isInitialized()) {
            this.mChildrenInitialized = true;
            skipToEndValue(false);
        }
    }

    public boolean doAnimationFrame(long frameTime) {
        float durationScale = ValueAnimator.getDurationScale();
        if (durationScale == TonemapCurve.LEVEL_BLACK) {
            forceToEnd();
            return true;
        }
        if (this.mFirstFrame < 0) {
            this.mFirstFrame = frameTime;
        }
        if (this.mPaused) {
            this.mPauseTime = frameTime;
            removeAnimationCallback();
            return false;
        }
        if (this.mPauseTime > 0) {
            this.mFirstFrame += frameTime - this.mPauseTime;
            this.mPauseTime = -1;
        }
        if (this.mSeekState.isActive()) {
            this.mSeekState.updateSeekDirection(this.mReversing);
            if (this.mReversing) {
                this.mFirstFrame = (long) (((float) frameTime) - (((float) this.mSeekState.getPlayTime()) * durationScale));
            } else {
                this.mFirstFrame = (long) (((float) frameTime) - (((float) (this.mSeekState.getPlayTime() + this.mStartDelay)) * durationScale));
            }
            this.mSeekState.reset();
        }
        if (!this.mReversing && ((float) frameTime) < ((float) this.mFirstFrame) + (((float) this.mStartDelay) * durationScale)) {
            return false;
        }
        int i;
        long unscaledPlayTime = (long) (((float) (frameTime - this.mFirstFrame)) / durationScale);
        this.mLastFrameTime = frameTime;
        int latestId = findLatestEventIdForTime(unscaledPlayTime);
        handleAnimationEvents(this.mLastEventId, latestId, unscaledPlayTime);
        this.mLastEventId = latestId;
        for (i = 0; i < this.mPlayingSet.size(); i++) {
            Node node = (Node) this.mPlayingSet.get(i);
            if (!node.mEnded) {
                pulseFrame(node, getPlayTimeForNode(unscaledPlayTime, node));
            }
        }
        for (i = this.mPlayingSet.size() - 1; i >= 0; i--) {
            if (((Node) this.mPlayingSet.get(i)).mEnded) {
                this.mPlayingSet.remove(i);
            }
        }
        boolean finished = false;
        if (!this.mReversing) {
            finished = this.mPlayingSet.isEmpty() && this.mLastEventId == this.mEvents.size() - 1;
        } else if (this.mPlayingSet.size() == 1 && this.mPlayingSet.get(0) == this.mRootNode) {
            finished = true;
        } else if (this.mPlayingSet.isEmpty() && this.mLastEventId < 3) {
            finished = true;
        }
        if (!finished) {
            return false;
        }
        endAnimation();
        return true;
    }

    public void commitAnimationFrame(long frameTime) {
    }

    boolean pulseAnimationFrame(long frameTime) {
        return doAnimationFrame(frameTime);
    }

    private void handleAnimationEvents(int startId, int latestId, long playTime) {
        int i;
        AnimationEvent event;
        Node node;
        if (this.mReversing) {
            if (startId == -1) {
                startId = this.mEvents.size();
            }
            for (i = startId - 1; i >= latestId; i--) {
                event = (AnimationEvent) this.mEvents.get(i);
                node = event.mNode;
                if (event.mEvent == 2) {
                    if (node.mAnimation.isStarted()) {
                        node.mAnimation.cancel();
                    }
                    node.mEnded = false;
                    this.mPlayingSet.add(event.mNode);
                    node.mAnimation.startWithoutPulsing(true);
                    pulseFrame(node, 0);
                } else if (event.mEvent == 1 && (node.mEnded ^ 1) != 0) {
                    pulseFrame(node, getPlayTimeForNode(playTime, node));
                }
            }
            return;
        }
        for (i = startId + 1; i <= latestId; i++) {
            event = (AnimationEvent) this.mEvents.get(i);
            node = event.mNode;
            if (event.mEvent == 0) {
                this.mPlayingSet.add(event.mNode);
                if (node.mAnimation.isStarted()) {
                    node.mAnimation.cancel();
                }
                node.mEnded = false;
                node.mAnimation.startWithoutPulsing(false);
                pulseFrame(node, 0);
            } else if (event.mEvent == 2 && (node.mEnded ^ 1) != 0) {
                pulseFrame(node, getPlayTimeForNode(playTime, node));
            }
        }
    }

    private void pulseFrame(Node node, long animPlayTime) {
        if (!node.mEnded) {
            float durationScale = ValueAnimator.getDurationScale();
            if (durationScale == TonemapCurve.LEVEL_BLACK) {
                durationScale = 1.0f;
            }
            node.mEnded = node.mAnimation.pulseAnimationFrame((long) (((float) animPlayTime) * durationScale));
        }
    }

    private long getPlayTimeForNode(long overallPlayTime, Node node) {
        return getPlayTimeForNode(overallPlayTime, node, this.mReversing);
    }

    private long getPlayTimeForNode(long overallPlayTime, Node node, boolean inReverse) {
        if (!inReverse) {
            return overallPlayTime - node.mStartTime;
        }
        return node.mEndTime - (getTotalDuration() - overallPlayTime);
    }

    private void startAnimation() {
        int i;
        addDummyListener();
        addAnimationCallback(0);
        if (this.mSeekState.getPlayTimeNormalized() == 0 && this.mReversing) {
            this.mSeekState.reset();
        }
        if (this.mShouldResetValuesAtStart) {
            if (isInitialized()) {
                skipToEndValue(this.mReversing ^ 1);
            } else if (this.mReversing) {
                initChildren();
                skipToEndValue(this.mReversing ^ 1);
            } else {
                for (i = this.mEvents.size() - 1; i >= 0; i--) {
                    if (((AnimationEvent) this.mEvents.get(i)).mEvent == 1) {
                        Animator anim = ((AnimationEvent) this.mEvents.get(i)).mNode.mAnimation;
                        if (anim.isInitialized()) {
                            anim.skipToEndValue(true);
                        }
                    }
                }
            }
        }
        if (this.mReversing || this.mStartDelay == 0 || this.mSeekState.isActive()) {
            long playTime;
            if (this.mSeekState.isActive()) {
                this.mSeekState.updateSeekDirection(this.mReversing);
                playTime = this.mSeekState.getPlayTime();
            } else {
                playTime = 0;
            }
            int toId = findLatestEventIdForTime(playTime);
            handleAnimationEvents(-1, toId, playTime);
            for (i = this.mPlayingSet.size() - 1; i >= 0; i--) {
                if (((Node) this.mPlayingSet.get(i)).mEnded) {
                    this.mPlayingSet.remove(i);
                }
            }
            this.mLastEventId = toId;
        }
    }

    private void addDummyListener() {
        for (int i = 1; i < this.mNodes.size(); i++) {
            ((Node) this.mNodes.get(i)).mAnimation.addListener(this.mDummyListener);
            if (this.mNodeMap.get(((Node) this.mNodes.get(i)).mAnimation) == null) {
                Log.w(TAG, "addDummyListener: animation = " + ((Node) this.mNodes.get(i)).mAnimation + "\n" + "node = " + this.mNodes.get(i) + "\n" + "mNodeMap = " + this.mNodeMap + "\n" + "mNodes = " + this.mNodes, new Exception());
            }
        }
    }

    private void removeDummyListener() {
        for (int i = 1; i < this.mNodes.size(); i++) {
            ((Node) this.mNodes.get(i)).mAnimation.removeListener(this.mDummyListener);
            if (this.mNodeMap.get(((Node) this.mNodes.get(i)).mAnimation) == null) {
                Log.w(TAG, "removeDummyListener: animation = " + ((Node) this.mNodes.get(i)).mAnimation + "\n" + "node = " + this.mNodes.get(i) + "\n" + "mNodeMap = " + this.mNodeMap + "\n" + "mNodes = " + this.mNodes, new Exception());
            }
        }
    }

    private int findLatestEventIdForTime(long currentPlayTime) {
        int size = this.mEvents.size();
        int latestId = this.mLastEventId;
        if (this.mReversing) {
            currentPlayTime = getTotalDuration() - currentPlayTime;
            if (this.mLastEventId != -1) {
                size = this.mLastEventId;
            }
            this.mLastEventId = size;
            for (int j = this.mLastEventId - 1; j >= 0; j--) {
                if (((AnimationEvent) this.mEvents.get(j)).getTime() >= currentPlayTime) {
                    latestId = j;
                }
            }
        } else {
            for (int i = this.mLastEventId + 1; i < size; i++) {
                if (((AnimationEvent) this.mEvents.get(i)).getTime() <= currentPlayTime) {
                    latestId = i;
                }
            }
        }
        return latestId;
    }

    private void endAnimation() {
        this.mStarted = false;
        this.mLastFrameTime = -1;
        this.mFirstFrame = -1;
        this.mLastEventId = -1;
        this.mPaused = false;
        this.mPauseTime = -1;
        this.mSeekState.reset();
        this.mPlayingSet.clear();
        removeAnimationCallback();
        if (this.mListeners != null) {
            ArrayList<AnimatorListener> tmpListeners = (ArrayList) this.mListeners.clone();
            int numListeners = tmpListeners.size();
            for (int i = 0; i < numListeners; i++) {
                ((AnimatorListener) tmpListeners.get(i)).onAnimationEnd(this, this.mReversing);
            }
        }
        removeDummyListener();
        this.mSelfPulse = true;
        this.mReversing = false;
    }

    private void removeAnimationCallback() {
        if (this.mSelfPulse) {
            AnimationHandler.getInstance().removeCallback(this);
        }
    }

    private void addAnimationCallback(long delay) {
        if (this.mSelfPulse) {
            AnimationHandler.getInstance().addAnimationFrameCallback(this, delay);
        }
    }

    public AnimatorSet clone() {
        Node node;
        Node nodeClone;
        final AnimatorSet anim = (AnimatorSet) super.clone();
        int nodeCount = this.mNodes.size();
        anim.mStarted = false;
        anim.mLastFrameTime = -1;
        anim.mFirstFrame = -1;
        anim.mLastEventId = -1;
        anim.mPaused = false;
        anim.mPauseTime = -1;
        anim.mSeekState = new SeekState(this, null);
        anim.mSelfPulse = true;
        anim.mPlayingSet = new ArrayList();
        anim.mNodeMap = new ArrayMap();
        anim.mNodes = new ArrayList(nodeCount);
        anim.mEvents = new ArrayList();
        anim.mDummyListener = new AnimatorListenerAdapter() {
            public void onAnimationEnd(Animator animation) {
                if (anim.mNodeMap.get(animation) == null) {
                    Log.w(AnimatorSet.TAG, "onAnimationEnd: animation = " + animation + "\n" + "mDummyListener = " + this);
                    throw new AndroidRuntimeException("Error: animation ended is not in the node map");
                } else {
                    ((Node) anim.mNodeMap.get(animation)).mEnded = true;
                }
            }
        };
        anim.mReversing = false;
        anim.mDependencyDirty = true;
        HashMap<Node, Node> clonesMap = new HashMap(nodeCount);
        for (int n = 0; n < nodeCount; n++) {
            node = (Node) this.mNodes.get(n);
            nodeClone = node.clone();
            nodeClone.mAnimation.removeListener(this.mDummyListener);
            clonesMap.put(node, nodeClone);
            anim.mNodes.add(nodeClone);
            anim.mNodeMap.put(nodeClone.mAnimation, nodeClone);
        }
        anim.mRootNode = (Node) clonesMap.get(this.mRootNode);
        anim.mDelayAnim = (ValueAnimator) anim.mRootNode.mAnimation;
        for (int i = 0; i < nodeCount; i++) {
            Node node2;
            int j;
            node = (Node) this.mNodes.get(i);
            nodeClone = (Node) clonesMap.get(node);
            if (node.mLatestParent == null) {
                node2 = null;
            } else {
                node2 = (Node) clonesMap.get(node.mLatestParent);
            }
            nodeClone.mLatestParent = node2;
            int size = node.mChildNodes == null ? 0 : node.mChildNodes.size();
            for (j = 0; j < size; j++) {
                nodeClone.mChildNodes.set(j, (Node) clonesMap.get(node.mChildNodes.get(j)));
            }
            size = node.mSiblings == null ? 0 : node.mSiblings.size();
            for (j = 0; j < size; j++) {
                nodeClone.mSiblings.set(j, (Node) clonesMap.get(node.mSiblings.get(j)));
            }
            if (node.mParents == null) {
                size = 0;
            } else {
                size = node.mParents.size();
            }
            for (j = 0; j < size; j++) {
                nodeClone.mParents.set(j, (Node) clonesMap.get(node.mParents.get(j)));
            }
        }
        return anim;
    }

    public boolean canReverse() {
        return getTotalDuration() != -1;
    }

    public void reverse() {
        start(true, true);
    }

    public String toString() {
        String returnVal = "AnimatorSet@" + Integer.toHexString(hashCode()) + "{";
        int size = this.mNodes.size();
        for (int i = 0; i < size; i++) {
            returnVal = returnVal + "\n    " + ((Node) this.mNodes.get(i)).mAnimation.toString();
        }
        return returnVal + "\n}";
    }

    private void printChildCount() {
        ArrayList<Node> list = new ArrayList(this.mNodes.size());
        list.add(this.mRootNode);
        Log.d(TAG, "Current tree: ");
        int index = 0;
        while (index < list.size()) {
            int listSize = list.size();
            StringBuilder builder = new StringBuilder();
            while (index < listSize) {
                Node node = (Node) list.get(index);
                int num = 0;
                if (node.mChildNodes != null) {
                    for (int i = 0; i < node.mChildNodes.size(); i++) {
                        Node child = (Node) node.mChildNodes.get(i);
                        if (child.mLatestParent == node) {
                            num++;
                            list.add(child);
                        }
                    }
                }
                builder.append(WifiEnterpriseConfig.CA_CERT_ALIAS_DELIMITER);
                builder.append(num);
                index++;
            }
            Log.d(TAG, builder.toString());
        }
    }

    private void createDependencyGraph() {
        int i;
        Node node;
        if (!this.mDependencyDirty) {
            boolean durationChanged = false;
            for (i = 0; i < this.mNodes.size(); i++) {
                if (((Node) this.mNodes.get(i)).mTotalDuration != ((Node) this.mNodes.get(i)).mAnimation.getTotalDuration()) {
                    durationChanged = true;
                    break;
                }
            }
            if (!durationChanged) {
                return;
            }
        }
        this.mDependencyDirty = false;
        int size = this.mNodes.size();
        for (i = 0; i < size; i++) {
            ((Node) this.mNodes.get(i)).mParentsAdded = false;
        }
        for (i = 0; i < size; i++) {
            node = (Node) this.mNodes.get(i);
            if (!node.mParentsAdded) {
                node.mParentsAdded = true;
                if (node.mSiblings != null) {
                    int j;
                    findSiblings(node, node.mSiblings);
                    node.mSiblings.remove(node);
                    int siblingSize = node.mSiblings.size();
                    for (j = 0; j < siblingSize; j++) {
                        node.addParents(((Node) node.mSiblings.get(j)).mParents);
                    }
                    for (j = 0; j < siblingSize; j++) {
                        Node sibling = (Node) node.mSiblings.get(j);
                        sibling.addParents(node.mParents);
                        sibling.mParentsAdded = true;
                    }
                }
            }
        }
        for (i = 0; i < size; i++) {
            node = (Node) this.mNodes.get(i);
            if (node != this.mRootNode && node.mParents == null) {
                node.addParent(this.mRootNode);
            }
        }
        ArrayList<Node> visited = new ArrayList(this.mNodes.size());
        this.mRootNode.mStartTime = 0;
        this.mRootNode.mEndTime = this.mDelayAnim.getDuration();
        updatePlayTime(this.mRootNode, visited);
        sortAnimationEvents();
        this.mTotalDuration = ((AnimationEvent) this.mEvents.get(this.mEvents.size() - 1)).getTime();
    }

    private void sortAnimationEvents() {
        int i;
        this.mEvents.clear();
        for (i = 1; i < this.mNodes.size(); i++) {
            Node node = (Node) this.mNodes.get(i);
            this.mEvents.add(new AnimationEvent(node, 0));
            this.mEvents.add(new AnimationEvent(node, 1));
            this.mEvents.add(new AnimationEvent(node, 2));
        }
        this.mEvents.sort(new Comparator<AnimationEvent>() {
            public int compare(AnimationEvent e1, AnimationEvent e2) {
                long t1 = e1.getTime();
                long t2 = e2.getTime();
                if (t1 == t2) {
                    if (e2.mEvent + e1.mEvent == 1) {
                        return e1.mEvent - e2.mEvent;
                    }
                    return e2.mEvent - e1.mEvent;
                } else if (t2 == -1) {
                    return -1;
                } else {
                    if (t1 == -1) {
                        return 1;
                    }
                    return (int) (t1 - t2);
                }
            }
        });
        int eventSize = this.mEvents.size();
        i = 0;
        while (i < eventSize) {
            AnimationEvent event = (AnimationEvent) this.mEvents.get(i);
            if (event.mEvent == 2) {
                boolean needToSwapStart;
                if (event.mNode.mStartTime == event.mNode.mEndTime) {
                    needToSwapStart = true;
                } else if (event.mNode.mEndTime == event.mNode.mStartTime + event.mNode.mAnimation.getStartDelay()) {
                    needToSwapStart = false;
                } else {
                    i++;
                }
                int startEventId = eventSize;
                int startDelayEndId = eventSize;
                for (int j = i + 1; j < eventSize && (startEventId >= eventSize || startDelayEndId >= eventSize); j++) {
                    if (((AnimationEvent) this.mEvents.get(j)).mNode == event.mNode) {
                        if (((AnimationEvent) this.mEvents.get(j)).mEvent == 0) {
                            startEventId = j;
                        } else if (((AnimationEvent) this.mEvents.get(j)).mEvent == 1) {
                            startDelayEndId = j;
                        }
                    }
                }
                if (needToSwapStart && startEventId == this.mEvents.size()) {
                    throw new UnsupportedOperationException("Something went wrong, no start isfound after stop for an animation that has the same start and endtime.");
                } else if (startDelayEndId == this.mEvents.size()) {
                    throw new UnsupportedOperationException("Something went wrong, no startdelay end is found after stop for an animation");
                } else {
                    if (needToSwapStart) {
                        this.mEvents.add(i, (AnimationEvent) this.mEvents.remove(startEventId));
                        i++;
                    }
                    this.mEvents.add(i, (AnimationEvent) this.mEvents.remove(startDelayEndId));
                    i += 2;
                }
            } else {
                i++;
            }
        }
        if (this.mEvents.isEmpty() || ((AnimationEvent) this.mEvents.get(0)).mEvent == 0) {
            this.mEvents.add(0, new AnimationEvent(this.mRootNode, 0));
            this.mEvents.add(1, new AnimationEvent(this.mRootNode, 1));
            this.mEvents.add(2, new AnimationEvent(this.mRootNode, 2));
            if (((AnimationEvent) this.mEvents.get(this.mEvents.size() - 1)).mEvent == 0 || ((AnimationEvent) this.mEvents.get(this.mEvents.size() - 1)).mEvent == 1) {
                throw new UnsupportedOperationException("Something went wrong, the last event is not an end event");
            }
            return;
        }
        throw new UnsupportedOperationException("Sorting went bad, the start event should always be at index 0");
    }

    private void updatePlayTime(Node parent, ArrayList<Node> visited) {
        int i;
        if (parent.mChildNodes == null) {
            if (parent == this.mRootNode) {
                for (i = 0; i < this.mNodes.size(); i++) {
                    Node node = (Node) this.mNodes.get(i);
                    if (node != this.mRootNode) {
                        node.mStartTime = -1;
                        node.mEndTime = -1;
                    }
                }
            }
            return;
        }
        visited.add(parent);
        int childrenSize = parent.mChildNodes.size();
        for (i = 0; i < childrenSize; i++) {
            Node child = (Node) parent.mChildNodes.get(i);
            int index = visited.indexOf(child);
            if (index >= 0) {
                for (int j = index; j < visited.size(); j++) {
                    ((Node) visited.get(j)).mLatestParent = null;
                    ((Node) visited.get(j)).mStartTime = -1;
                    ((Node) visited.get(j)).mEndTime = -1;
                }
                child.mStartTime = -1;
                child.mEndTime = -1;
                child.mLatestParent = null;
                Log.w(TAG, "Cycle found in AnimatorSet: " + this);
            } else {
                if (child.mStartTime != -1) {
                    if (parent.mEndTime == -1) {
                        child.mLatestParent = parent;
                        child.mStartTime = -1;
                        child.mEndTime = -1;
                    } else {
                        if (parent.mEndTime >= child.mStartTime) {
                            child.mLatestParent = parent;
                            child.mStartTime = parent.mEndTime;
                        }
                        long duration = child.mAnimation.getTotalDuration();
                        child.mEndTime = duration == -1 ? -1 : child.mStartTime + duration;
                    }
                }
                updatePlayTime(child, visited);
            }
        }
        visited.remove(parent);
    }

    private void findSiblings(Node node, ArrayList<Node> siblings) {
        if (!siblings.contains(node)) {
            siblings.add(node);
            if (node.mSiblings != null) {
                for (int i = 0; i < node.mSiblings.size(); i++) {
                    findSiblings((Node) node.mSiblings.get(i), siblings);
                }
            }
        }
    }

    public boolean shouldPlayTogether() {
        updateAnimatorsDuration();
        createDependencyGraph();
        if (this.mRootNode.mChildNodes == null || this.mRootNode.mChildNodes.size() == this.mNodes.size() - 1) {
            return true;
        }
        return false;
    }

    public long getTotalDuration() {
        updateAnimatorsDuration();
        createDependencyGraph();
        return this.mTotalDuration;
    }

    private Node getNodeForAnimation(Animator anim) {
        Node node = (Node) this.mNodeMap.get(anim);
        if (node != null) {
            return node;
        }
        node = new Node(anim);
        this.mNodeMap.put(anim, node);
        this.mNodes.add(node);
        return node;
    }
}
