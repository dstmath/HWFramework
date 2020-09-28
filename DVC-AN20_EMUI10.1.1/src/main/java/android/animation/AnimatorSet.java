package android.animation;

import android.animation.AnimationHandler;
import android.animation.Animator;
import android.app.ActivityThread;
import android.app.Application;
import android.net.wifi.WifiEnterpriseConfig;
import android.os.Looper;
import android.provider.Telephony;
import android.util.AndroidRuntimeException;
import android.util.ArrayMap;
import android.util.Log;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

public final class AnimatorSet extends Animator implements AnimationHandler.AnimationFrameCallback {
    private static final String TAG = "AnimatorSet";
    private boolean mChildrenInitialized;
    private ValueAnimator mDelayAnim;
    private boolean mDependencyDirty;
    private AnimatorListenerAdapter mDummyListener;
    private long mDuration;
    private final boolean mEndCanBeCalled;
    private ArrayList<AnimationEvent> mEvents = new ArrayList<>();
    private long mFirstFrame;
    private TimeInterpolator mInterpolator;
    private int mLastEventId;
    private long mLastFrameTime;
    private ArrayMap<Animator, Node> mNodeMap = new ArrayMap<>();
    private ArrayList<Node> mNodes = new ArrayList<>();
    private long mPauseTime;
    private ArrayList<Node> mPlayingSet = new ArrayList<>();
    private boolean mReversing;
    private Node mRootNode;
    private SeekState mSeekState;
    private boolean mSelfPulse;
    private final boolean mShouldIgnoreEndWithoutStart;
    private final boolean mShouldResetValuesAtStart;
    private long mStartDelay;
    private boolean mStarted;
    private long mTotalDuration;

    public AnimatorSet() {
        boolean isPreO;
        boolean z = false;
        this.mDependencyDirty = false;
        this.mStarted = false;
        this.mStartDelay = 0;
        this.mDelayAnim = ValueAnimator.ofFloat(0.0f, 1.0f).setDuration(0L);
        this.mRootNode = new Node(this.mDelayAnim);
        this.mDuration = -1;
        this.mInterpolator = null;
        this.mTotalDuration = 0;
        this.mLastFrameTime = -1;
        this.mFirstFrame = -1;
        this.mLastEventId = -1;
        this.mReversing = false;
        this.mSelfPulse = true;
        this.mSeekState = new SeekState();
        this.mChildrenInitialized = false;
        this.mPauseTime = -1;
        this.mDummyListener = new AnimatorListenerAdapter() {
            /* class android.animation.AnimatorSet.AnonymousClass1 */

            @Override // android.animation.Animator.AnimatorListener, android.animation.AnimatorListenerAdapter
            public void onAnimationEnd(Animator animation) {
                if (AnimatorSet.this.mNodeMap.get(animation) != null) {
                    ((Node) AnimatorSet.this.mNodeMap.get(animation)).mEnded = true;
                    return;
                }
                throw new AndroidRuntimeException("Error: animation ended is not in the node map");
            }
        };
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
        this.mShouldResetValuesAtStart = !isPreO;
        this.mEndCanBeCalled = !isPreO ? true : z;
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
                play(items.get(0));
                return;
            }
            for (int i = 0; i < items.size() - 1; i++) {
                play(items.get(i)).before(items.get(i + 1));
            }
        }
    }

    public ArrayList<Animator> getChildAnimations() {
        ArrayList<Animator> childList = new ArrayList<>();
        int size = this.mNodes.size();
        for (int i = 0; i < size; i++) {
            Node node = this.mNodes.get(i);
            if (node != this.mRootNode) {
                childList.add(node.mAnimation);
            }
        }
        return childList;
    }

    @Override // android.animation.Animator
    public void setTarget(Object target) {
        int size = this.mNodes.size();
        for (int i = 0; i < size; i++) {
            Animator animation = this.mNodes.get(i).mAnimation;
            if (animation instanceof AnimatorSet) {
                ((AnimatorSet) animation).setTarget(target);
            } else if (animation instanceof ObjectAnimator) {
                ((ObjectAnimator) animation).setTarget(target);
            }
        }
    }

    @Override // android.animation.Animator
    public int getChangingConfigurations() {
        int conf = super.getChangingConfigurations();
        int nodeCount = this.mNodes.size();
        for (int i = 0; i < nodeCount; i++) {
            conf |= this.mNodes.get(i).mAnimation.getChangingConfigurations();
        }
        return conf;
    }

    @Override // android.animation.Animator
    public void setInterpolator(TimeInterpolator interpolator) {
        this.mInterpolator = interpolator;
    }

    @Override // android.animation.Animator
    public TimeInterpolator getInterpolator() {
        return this.mInterpolator;
    }

    public Builder play(Animator anim) {
        if (anim != null) {
            return new Builder(anim);
        }
        return null;
    }

    @Override // android.animation.Animator
    public void cancel() {
        if (Looper.myLooper() == null) {
            throw new AndroidRuntimeException("Animators may only be run on Looper threads");
        } else if (isStarted()) {
            if (this.mListeners != null) {
                ArrayList<Animator.AnimatorListener> tmpListeners = (ArrayList) this.mListeners.clone();
                int size = tmpListeners.size();
                for (int i = 0; i < size; i++) {
                    tmpListeners.get(i).onAnimationCancel(this);
                }
            }
            ArrayList<Node> playingSet = new ArrayList<>(this.mPlayingSet);
            int setSize = playingSet.size();
            for (int i2 = 0; i2 < setSize; i2++) {
                playingSet.get(i2).mAnimation.cancel();
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

    @Override // android.animation.Animator
    public void end() {
        if (Looper.myLooper() == null) {
            throw new AndroidRuntimeException("Animators may only be run on Looper threads");
        } else if (!this.mShouldIgnoreEndWithoutStart || isStarted()) {
            if (isStarted()) {
                if (this.mReversing) {
                    int i = this.mLastEventId;
                    if (i == -1) {
                        i = this.mEvents.size();
                    }
                    this.mLastEventId = i;
                    while (true) {
                        int i2 = this.mLastEventId;
                        if (i2 <= 0) {
                            break;
                        }
                        this.mLastEventId = i2 - 1;
                        AnimationEvent event = this.mEvents.get(this.mLastEventId);
                        Animator anim = event.mNode.mAnimation;
                        if (!this.mNodeMap.get(anim).mEnded) {
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
                        AnimationEvent event2 = this.mEvents.get(this.mLastEventId);
                        Animator anim2 = event2.mNode.mAnimation;
                        if (!this.mNodeMap.get(anim2).mEnded) {
                            if (event2.mEvent == 0) {
                                anim2.start();
                            } else if (event2.mEvent == 2 && anim2.isStarted()) {
                                anim2.end();
                            }
                        }
                    }
                }
                this.mPlayingSet.clear();
            }
            endAnimation();
        }
    }

    @Override // android.animation.Animator
    public boolean isRunning() {
        if (this.mStartDelay == 0) {
            return this.mStarted;
        }
        return this.mLastFrameTime > 0;
    }

    @Override // android.animation.Animator
    public boolean isStarted() {
        return this.mStarted;
    }

    @Override // android.animation.Animator
    public long getStartDelay() {
        return this.mStartDelay;
    }

    @Override // android.animation.Animator
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
                int i = 0;
                while (true) {
                    long j = -1;
                    if (i >= size) {
                        break;
                    }
                    Node node = this.mNodes.get(i);
                    if (node == this.mRootNode) {
                        node.mEndTime = this.mStartDelay;
                    } else {
                        node.mStartTime = node.mStartTime == -1 ? -1 : node.mStartTime + delta;
                        if (node.mEndTime != -1) {
                            j = node.mEndTime + delta;
                        }
                        node.mEndTime = j;
                    }
                    i++;
                }
                long j2 = this.mTotalDuration;
                if (j2 != -1) {
                    this.mTotalDuration = j2 + delta;
                }
            }
        }
    }

    @Override // android.animation.Animator
    public long getDuration() {
        return this.mDuration;
    }

    @Override // android.animation.Animator
    public AnimatorSet setDuration(long duration) {
        if (duration >= 0) {
            this.mDependencyDirty = true;
            this.mDuration = duration;
            return this;
        }
        throw new IllegalArgumentException("duration must be a value of zero or greater");
    }

    @Override // android.animation.Animator
    public void setupStartValues() {
        int size = this.mNodes.size();
        for (int i = 0; i < size; i++) {
            Node node = this.mNodes.get(i);
            if (node != this.mRootNode) {
                node.mAnimation.setupStartValues();
            }
        }
    }

    @Override // android.animation.Animator
    public void setupEndValues() {
        int size = this.mNodes.size();
        for (int i = 0; i < size; i++) {
            Node node = this.mNodes.get(i);
            if (node != this.mRootNode) {
                node.mAnimation.setupEndValues();
            }
        }
    }

    @Override // android.animation.Animator
    public void pause() {
        if (Looper.myLooper() != null) {
            boolean previouslyPaused = this.mPaused;
            super.pause();
            if (!previouslyPaused && this.mPaused) {
                this.mPauseTime = -1;
                return;
            }
            return;
        }
        throw new AndroidRuntimeException("Animators may only be run on Looper threads");
    }

    @Override // android.animation.Animator
    public void resume() {
        if (Looper.myLooper() != null) {
            boolean previouslyPaused = this.mPaused;
            super.resume();
            if (previouslyPaused && !this.mPaused && this.mPauseTime >= 0) {
                addAnimationCallback(0);
                return;
            }
            return;
        }
        throw new AndroidRuntimeException("Animators may only be run on Looper threads");
    }

    @Override // android.animation.Animator
    public void start() {
        start(false, true);
    }

    /* access modifiers changed from: package-private */
    @Override // android.animation.Animator
    public void startWithoutPulsing(boolean inReverse) {
        start(inReverse, false);
    }

    private void initAnimation() {
        if (this.mInterpolator != null) {
            for (int i = 0; i < this.mNodes.size(); i++) {
                this.mNodes.get(i).mAnimation.setInterpolator(this.mInterpolator);
            }
        }
        updateAnimatorsDuration();
        createDependencyGraph();
    }

    private void start(boolean inReverse, boolean selfPulse) {
        if (Looper.myLooper() != null) {
            this.mStarted = true;
            this.mSelfPulse = selfPulse;
            this.mPaused = false;
            this.mPauseTime = -1;
            int size = this.mNodes.size();
            for (int i = 0; i < size; i++) {
                Node node = this.mNodes.get(i);
                node.mEnded = false;
                node.mAnimation.setAllowRunningAsynchronously(false);
            }
            initAnimation();
            if (!inReverse || canReverse()) {
                this.mReversing = inReverse;
                boolean isEmptySet = isEmptySet(this);
                if (!isEmptySet) {
                    startAnimation();
                }
                if (this.mListeners != null) {
                    ArrayList<Animator.AnimatorListener> tmpListeners = (ArrayList) this.mListeners.clone();
                    int numListeners = tmpListeners.size();
                    for (int i2 = 0; i2 < numListeners; i2++) {
                        tmpListeners.get(i2).onAnimationStart(this, inReverse);
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
        throw new AndroidRuntimeException("Animators may only be run on Looper threads");
    }

    private static boolean isEmptySet(AnimatorSet set) {
        if (set.getStartDelay() > 0) {
            return false;
        }
        for (int i = 0; i < set.getChildAnimations().size(); i++) {
            Animator anim = set.getChildAnimations().get(i);
            if (!((anim instanceof AnimatorSet) && isEmptySet((AnimatorSet) anim))) {
                return false;
            }
        }
        return true;
    }

    private void updateAnimatorsDuration() {
        if (this.mDuration >= 0) {
            int size = this.mNodes.size();
            for (int i = 0; i < size; i++) {
                this.mNodes.get(i).mAnimation.setDuration(this.mDuration);
            }
        }
        this.mDelayAnim.setDuration(this.mStartDelay);
    }

    /* access modifiers changed from: package-private */
    @Override // android.animation.Animator
    public void skipToEndValue(boolean inReverse) {
        if (isInitialized()) {
            initAnimation();
            if (inReverse) {
                for (int i = this.mEvents.size() - 1; i >= 0; i--) {
                    if (this.mEvents.get(i).mEvent == 1) {
                        this.mEvents.get(i).mNode.mAnimation.skipToEndValue(true);
                    }
                }
                return;
            }
            for (int i2 = 0; i2 < this.mEvents.size(); i2++) {
                if (this.mEvents.get(i2).mEvent == 2) {
                    this.mEvents.get(i2).mNode.mAnimation.skipToEndValue(false);
                }
            }
            return;
        }
        throw new UnsupportedOperationException("Children must be initialized.");
    }

    /* access modifiers changed from: package-private */
    @Override // android.animation.Animator
    public void animateBasedOnPlayTime(long currentPlayTime, long lastPlayTime, boolean inReverse) {
        long lastPlayTime2;
        long currentPlayTime2;
        boolean inReverse2;
        if (currentPlayTime < 0 || lastPlayTime < 0) {
            throw new UnsupportedOperationException("Error: Play time should never be negative.");
        }
        if (!inReverse) {
            lastPlayTime2 = lastPlayTime;
            currentPlayTime2 = currentPlayTime;
            inReverse2 = inReverse;
        } else if (getTotalDuration() != -1) {
            long duration = getTotalDuration() - this.mStartDelay;
            currentPlayTime2 = duration - Math.min(currentPlayTime, duration);
            lastPlayTime2 = duration - lastPlayTime;
            inReverse2 = false;
        } else {
            throw new UnsupportedOperationException("Cannot reverse AnimatorSet with infinite duration");
        }
        ArrayList<Node> unfinishedNodes = new ArrayList<>();
        for (int i = 0; i < this.mEvents.size(); i++) {
            AnimationEvent event = this.mEvents.get(i);
            if (event.getTime() > currentPlayTime2 || event.getTime() == -1) {
                break;
            }
            if (event.mEvent == 1 && (event.mNode.mEndTime == -1 || event.mNode.mEndTime > currentPlayTime2)) {
                unfinishedNodes.add(event.mNode);
            }
            if (event.mEvent == 2) {
                event.mNode.mAnimation.skipToEndValue(false);
            }
        }
        for (int i2 = 0; i2 < unfinishedNodes.size(); i2++) {
            Node node = unfinishedNodes.get(i2);
            long playTime = getPlayTimeForNode(currentPlayTime2, node, inReverse2);
            if (!inReverse2) {
                playTime -= node.mAnimation.getStartDelay();
            }
            node.mAnimation.animateBasedOnPlayTime(playTime, lastPlayTime2, inReverse2);
        }
        for (int i3 = 0; i3 < this.mEvents.size(); i3++) {
            AnimationEvent event2 = this.mEvents.get(i3);
            if (event2.getTime() > currentPlayTime2 && event2.mEvent == 1) {
                event2.mNode.mAnimation.skipToEndValue(true);
            }
        }
    }

    /* access modifiers changed from: package-private */
    @Override // android.animation.Animator
    public boolean isInitialized() {
        if (this.mChildrenInitialized) {
            return true;
        }
        boolean allInitialized = true;
        int i = 0;
        while (true) {
            if (i >= this.mNodes.size()) {
                break;
            } else if (!this.mNodes.get(i).mAnimation.isInitialized()) {
                allInitialized = false;
                break;
            } else {
                i++;
            }
        }
        this.mChildrenInitialized = allInitialized;
        return this.mChildrenInitialized;
    }

    private void skipToStartValue(boolean inReverse) {
        skipToEndValue(!inReverse);
    }

    public void setCurrentPlayTime(long playTime) {
        if (this.mReversing && getTotalDuration() == -1) {
            throw new UnsupportedOperationException("Error: Cannot seek in reverse in an infinite AnimatorSet");
        } else if ((getTotalDuration() == -1 || playTime <= getTotalDuration() - this.mStartDelay) && playTime >= 0) {
            initAnimation();
            if (isStarted() && !isPaused()) {
                this.mSeekState.setPlayTime(playTime, this.mReversing);
            } else if (!this.mReversing) {
                if (!this.mSeekState.isActive()) {
                    findLatestEventIdForTime(0);
                    initChildren();
                    this.mSeekState.setPlayTime(0, this.mReversing);
                }
                animateBasedOnPlayTime(playTime, 0, this.mReversing);
                this.mSeekState.setPlayTime(playTime, this.mReversing);
            } else {
                throw new UnsupportedOperationException("Error: Something went wrong. mReversing should not be set when AnimatorSet is not started.");
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
        float durationScale2 = durationScale == 0.0f ? 1.0f : durationScale;
        if (this.mReversing) {
            return (long) (((float) (this.mLastFrameTime - this.mFirstFrame)) / durationScale2);
        }
        return (long) (((float) ((this.mLastFrameTime - this.mFirstFrame) - this.mStartDelay)) / durationScale2);
    }

    private void initChildren() {
        if (!isInitialized()) {
            this.mChildrenInitialized = true;
            skipToEndValue(false);
        }
    }

    @Override // android.animation.AnimationHandler.AnimationFrameCallback
    public boolean doAnimationFrame(long frameTime) {
        float durationScale = ValueAnimator.getDurationScale();
        if (durationScale == 0.0f) {
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
        long j = this.mPauseTime;
        if (j > 0) {
            this.mFirstFrame += frameTime - j;
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
        long unscaledPlayTime = (long) (((float) (frameTime - this.mFirstFrame)) / durationScale);
        this.mLastFrameTime = frameTime;
        int latestId = findLatestEventIdForTime(unscaledPlayTime);
        handleAnimationEvents(this.mLastEventId, latestId, unscaledPlayTime);
        this.mLastEventId = latestId;
        for (int i = 0; i < this.mPlayingSet.size(); i++) {
            Node node = this.mPlayingSet.get(i);
            if (!node.mEnded) {
                pulseFrame(node, getPlayTimeForNode(unscaledPlayTime, node));
            }
        }
        for (int i2 = this.mPlayingSet.size() - 1; i2 >= 0; i2--) {
            if (this.mPlayingSet.get(i2).mEnded) {
                this.mPlayingSet.remove(i2);
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

    @Override // android.animation.AnimationHandler.AnimationFrameCallback
    public void commitAnimationFrame(long frameTime) {
    }

    /* access modifiers changed from: package-private */
    @Override // android.animation.Animator
    public boolean pulseAnimationFrame(long frameTime) {
        return doAnimationFrame(frameTime);
    }

    private void handleAnimationEvents(int startId, int latestId, long playTime) {
        if (this.mReversing) {
            for (int i = (startId == -1 ? this.mEvents.size() : startId) - 1; i >= latestId; i--) {
                AnimationEvent event = this.mEvents.get(i);
                Node node = event.mNode;
                if (event.mEvent == 2) {
                    if (node.mAnimation.isStarted()) {
                        node.mAnimation.cancel();
                    }
                    node.mEnded = false;
                    this.mPlayingSet.add(event.mNode);
                    node.mAnimation.startWithoutPulsing(true);
                    pulseFrame(node, 0);
                } else if (event.mEvent == 1 && !node.mEnded) {
                    pulseFrame(node, getPlayTimeForNode(playTime, node));
                }
            }
            return;
        }
        for (int i2 = startId + 1; i2 <= latestId; i2++) {
            AnimationEvent event2 = this.mEvents.get(i2);
            Node node2 = event2.mNode;
            if (event2.mEvent == 0) {
                this.mPlayingSet.add(event2.mNode);
                if (node2.mAnimation.isStarted()) {
                    node2.mAnimation.cancel();
                }
                node2.mEnded = false;
                node2.mAnimation.startWithoutPulsing(false);
                pulseFrame(node2, 0);
            } else if (event2.mEvent == 2 && !node2.mEnded) {
                pulseFrame(node2, getPlayTimeForNode(playTime, node2));
            }
        }
    }

    private void pulseFrame(Node node, long animPlayTime) {
        if (!node.mEnded) {
            float durationScale = ValueAnimator.getDurationScale();
            node.mEnded = node.mAnimation.pulseAnimationFrame((long) (((float) animPlayTime) * (durationScale == 0.0f ? 1.0f : durationScale)));
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
        long playTime;
        addDummyListener();
        addAnimationCallback(0);
        if (this.mSeekState.getPlayTimeNormalized() == 0 && this.mReversing) {
            this.mSeekState.reset();
        }
        if (this.mShouldResetValuesAtStart) {
            if (isInitialized()) {
                skipToEndValue(!this.mReversing);
            } else if (this.mReversing) {
                initChildren();
                skipToEndValue(!this.mReversing);
            } else {
                for (int i = this.mEvents.size() - 1; i >= 0; i--) {
                    if (this.mEvents.get(i).mEvent == 1) {
                        Animator anim = this.mEvents.get(i).mNode.mAnimation;
                        if (anim.isInitialized()) {
                            anim.skipToEndValue(true);
                        }
                    }
                }
            }
        }
        if (this.mReversing || this.mStartDelay == 0 || this.mSeekState.isActive()) {
            if (this.mSeekState.isActive()) {
                this.mSeekState.updateSeekDirection(this.mReversing);
                playTime = this.mSeekState.getPlayTime();
            } else {
                playTime = 0;
            }
            int toId = findLatestEventIdForTime(playTime);
            handleAnimationEvents(-1, toId, playTime);
            for (int i2 = this.mPlayingSet.size() - 1; i2 >= 0; i2--) {
                if (this.mPlayingSet.get(i2).mEnded) {
                    this.mPlayingSet.remove(i2);
                }
            }
            this.mLastEventId = toId;
        }
    }

    private void addDummyListener() {
        for (int i = 1; i < this.mNodes.size(); i++) {
            this.mNodes.get(i).mAnimation.addListener(this.mDummyListener);
        }
    }

    private void removeDummyListener() {
        for (int i = 1; i < this.mNodes.size(); i++) {
            this.mNodes.get(i).mAnimation.removeListener(this.mDummyListener);
        }
    }

    private int findLatestEventIdForTime(long currentPlayTime) {
        int size = this.mEvents.size();
        int latestId = this.mLastEventId;
        if (this.mReversing) {
            long currentPlayTime2 = getTotalDuration() - currentPlayTime;
            int i = this.mLastEventId;
            if (i == -1) {
                i = size;
            }
            this.mLastEventId = i;
            for (int j = this.mLastEventId - 1; j >= 0; j--) {
                if (this.mEvents.get(j).getTime() >= currentPlayTime2) {
                    latestId = j;
                }
            }
        } else {
            for (int i2 = this.mLastEventId + 1; i2 < size; i2++) {
                AnimationEvent event = this.mEvents.get(i2);
                if (event.getTime() != -1 && event.getTime() <= currentPlayTime) {
                    latestId = i2;
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
            ArrayList<Animator.AnimatorListener> tmpListeners = (ArrayList) this.mListeners.clone();
            int numListeners = tmpListeners.size();
            for (int i = 0; i < numListeners; i++) {
                tmpListeners.get(i).onAnimationEnd(this, this.mReversing);
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

    @Override // java.lang.Object, android.animation.Animator, android.animation.Animator
    public AnimatorSet clone() {
        final AnimatorSet anim = (AnimatorSet) super.clone();
        int nodeCount = this.mNodes.size();
        anim.mStarted = false;
        anim.mLastFrameTime = -1;
        anim.mFirstFrame = -1;
        anim.mLastEventId = -1;
        anim.mPaused = false;
        anim.mPauseTime = -1;
        anim.mSeekState = new SeekState();
        anim.mSelfPulse = true;
        anim.mPlayingSet = new ArrayList<>();
        anim.mNodeMap = new ArrayMap<>();
        anim.mNodes = new ArrayList<>(nodeCount);
        anim.mEvents = new ArrayList<>();
        anim.mDummyListener = new AnimatorListenerAdapter() {
            /* class android.animation.AnimatorSet.AnonymousClass2 */

            @Override // android.animation.Animator.AnimatorListener, android.animation.AnimatorListenerAdapter
            public void onAnimationEnd(Animator animation) {
                if (anim.mNodeMap.get(animation) != null) {
                    ((Node) anim.mNodeMap.get(animation)).mEnded = true;
                    return;
                }
                throw new AndroidRuntimeException("Error: animation ended is not in the node map");
            }
        };
        anim.mReversing = false;
        anim.mDependencyDirty = true;
        HashMap<Node, Node> clonesMap = new HashMap<>(nodeCount);
        for (int n = 0; n < nodeCount; n++) {
            Node node = this.mNodes.get(n);
            Node nodeClone = node.clone();
            nodeClone.mAnimation.removeListener(this.mDummyListener);
            clonesMap.put(node, nodeClone);
            anim.mNodes.add(nodeClone);
            anim.mNodeMap.put(nodeClone.mAnimation, nodeClone);
        }
        anim.mRootNode = clonesMap.get(this.mRootNode);
        anim.mDelayAnim = (ValueAnimator) anim.mRootNode.mAnimation;
        for (int i = 0; i < nodeCount; i++) {
            Node node2 = this.mNodes.get(i);
            Node nodeClone2 = clonesMap.get(node2);
            nodeClone2.mLatestParent = node2.mLatestParent == null ? null : clonesMap.get(node2.mLatestParent);
            int size = node2.mChildNodes == null ? 0 : node2.mChildNodes.size();
            for (int j = 0; j < size; j++) {
                nodeClone2.mChildNodes.set(j, clonesMap.get(node2.mChildNodes.get(j)));
            }
            int size2 = node2.mSiblings == null ? 0 : node2.mSiblings.size();
            for (int j2 = 0; j2 < size2; j2++) {
                nodeClone2.mSiblings.set(j2, clonesMap.get(node2.mSiblings.get(j2)));
            }
            int size3 = node2.mParents == null ? 0 : node2.mParents.size();
            for (int j3 = 0; j3 < size3; j3++) {
                nodeClone2.mParents.set(j3, clonesMap.get(node2.mParents.get(j3)));
            }
        }
        return anim;
    }

    @Override // android.animation.Animator
    public boolean canReverse() {
        return getTotalDuration() != -1;
    }

    @Override // android.animation.Animator
    public void reverse() {
        start(true, true);
    }

    public String toString() {
        String returnVal = "AnimatorSet@" + Integer.toHexString(hashCode()) + "{";
        for (int i = 0; i < this.mNodes.size(); i++) {
            returnVal = returnVal + "\n    " + this.mNodes.get(i).mAnimation.toString();
        }
        return returnVal + "\n}";
    }

    private void printChildCount() {
        ArrayList<Node> list = new ArrayList<>(this.mNodes.size());
        list.add(this.mRootNode);
        Log.d(TAG, "Current tree: ");
        int index = 0;
        while (index < list.size()) {
            int listSize = list.size();
            StringBuilder builder = new StringBuilder();
            while (index < listSize) {
                Node node = list.get(index);
                int num = 0;
                if (node.mChildNodes != null) {
                    for (int i = 0; i < node.mChildNodes.size(); i++) {
                        Node child = node.mChildNodes.get(i);
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
        if (!this.mDependencyDirty) {
            boolean durationChanged = false;
            int i = 0;
            while (true) {
                if (i >= this.mNodes.size()) {
                    break;
                }
                if (this.mNodes.get(i).mTotalDuration != this.mNodes.get(i).mAnimation.getTotalDuration()) {
                    durationChanged = true;
                    break;
                }
                i++;
            }
            if (!durationChanged) {
                return;
            }
        }
        this.mDependencyDirty = false;
        int size = this.mNodes.size();
        for (int i2 = 0; i2 < size; i2++) {
            this.mNodes.get(i2).mParentsAdded = false;
        }
        for (int i3 = 0; i3 < size; i3++) {
            Node node = this.mNodes.get(i3);
            if (!node.mParentsAdded) {
                node.mParentsAdded = true;
                if (node.mSiblings != null) {
                    findSiblings(node, node.mSiblings);
                    node.mSiblings.remove(node);
                    int siblingSize = node.mSiblings.size();
                    for (int j = 0; j < siblingSize; j++) {
                        node.addParents(node.mSiblings.get(j).mParents);
                    }
                    for (int j2 = 0; j2 < siblingSize; j2++) {
                        Node sibling = node.mSiblings.get(j2);
                        sibling.addParents(node.mParents);
                        sibling.mParentsAdded = true;
                    }
                }
            }
        }
        for (int i4 = 0; i4 < size; i4++) {
            Node node2 = this.mNodes.get(i4);
            if (node2 != this.mRootNode && node2.mParents == null) {
                node2.addParent(this.mRootNode);
            }
        }
        ArrayList<Node> visited = new ArrayList<>(this.mNodes.size());
        Node node3 = this.mRootNode;
        node3.mStartTime = 0;
        node3.mEndTime = this.mDelayAnim.getDuration();
        updatePlayTime(this.mRootNode, visited);
        sortAnimationEvents();
        ArrayList<AnimationEvent> arrayList = this.mEvents;
        this.mTotalDuration = arrayList.get(arrayList.size() - 1).getTime();
    }

    private void sortAnimationEvents() {
        boolean needToSwapStart;
        this.mEvents.clear();
        for (int i = 1; i < this.mNodes.size(); i++) {
            Node node = this.mNodes.get(i);
            this.mEvents.add(new AnimationEvent(node, 0));
            this.mEvents.add(new AnimationEvent(node, 1));
            this.mEvents.add(new AnimationEvent(node, 2));
        }
        this.mEvents.sort(new Comparator<AnimationEvent>() {
            /* class android.animation.AnimatorSet.AnonymousClass3 */

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
        int i2 = 0;
        while (i2 < eventSize) {
            AnimationEvent event = this.mEvents.get(i2);
            if (event.mEvent == 2) {
                if (event.mNode.mStartTime == event.mNode.mEndTime) {
                    needToSwapStart = true;
                } else if (event.mNode.mEndTime == event.mNode.mStartTime + event.mNode.mAnimation.getStartDelay()) {
                    needToSwapStart = false;
                } else {
                    i2++;
                }
                int startEventId = eventSize;
                int startDelayEndId = eventSize;
                for (int j = i2 + 1; j < eventSize && (startEventId >= eventSize || startDelayEndId >= eventSize); j++) {
                    if (this.mEvents.get(j).mNode == event.mNode) {
                        if (this.mEvents.get(j).mEvent == 0) {
                            startEventId = j;
                        } else if (this.mEvents.get(j).mEvent == 1) {
                            startDelayEndId = j;
                        }
                    }
                }
                if (needToSwapStart && startEventId == this.mEvents.size()) {
                    throw new UnsupportedOperationException("Something went wrong, no start isfound after stop for an animation that has the same start and endtime.");
                } else if (startDelayEndId != this.mEvents.size()) {
                    if (needToSwapStart) {
                        this.mEvents.add(i2, this.mEvents.remove(startEventId));
                        i2++;
                    }
                    this.mEvents.add(i2, this.mEvents.remove(startDelayEndId));
                    i2 += 2;
                } else {
                    throw new UnsupportedOperationException("Something went wrong, no startdelay end is found after stop for an animation");
                }
            } else {
                i2++;
            }
        }
        if (this.mEvents.isEmpty() || this.mEvents.get(0).mEvent == 0) {
            this.mEvents.add(0, new AnimationEvent(this.mRootNode, 0));
            this.mEvents.add(1, new AnimationEvent(this.mRootNode, 1));
            this.mEvents.add(2, new AnimationEvent(this.mRootNode, 2));
            ArrayList<AnimationEvent> arrayList = this.mEvents;
            if (arrayList.get(arrayList.size() - 1).mEvent != 0) {
                ArrayList<AnimationEvent> arrayList2 = this.mEvents;
                if (arrayList2.get(arrayList2.size() - 1).mEvent != 1) {
                    return;
                }
            }
            throw new UnsupportedOperationException("Something went wrong, the last event is not an end event");
        }
        throw new UnsupportedOperationException("Sorting went bad, the start event should always be at index 0");
    }

    private void updatePlayTime(Node parent, ArrayList<Node> visited) {
        if (parent.mChildNodes != null) {
            visited.add(parent);
            int childrenSize = parent.mChildNodes.size();
            for (int i = 0; i < childrenSize; i++) {
                Node child = parent.mChildNodes.get(i);
                child.mTotalDuration = child.mAnimation.getTotalDuration();
                int index = visited.indexOf(child);
                if (index >= 0) {
                    for (int j = index; j < visited.size(); j++) {
                        visited.get(j).mLatestParent = null;
                        visited.get(j).mStartTime = -1;
                        visited.get(j).mEndTime = -1;
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
                            child.mEndTime = child.mTotalDuration == -1 ? -1 : child.mStartTime + child.mTotalDuration;
                        }
                    }
                    updatePlayTime(child, visited);
                }
            }
            visited.remove(parent);
        } else if (parent == this.mRootNode) {
            for (int i2 = 0; i2 < this.mNodes.size(); i2++) {
                Node node = this.mNodes.get(i2);
                if (node != this.mRootNode) {
                    node.mStartTime = -1;
                    node.mEndTime = -1;
                }
            }
        }
    }

    private void findSiblings(Node node, ArrayList<Node> siblings) {
        if (!siblings.contains(node)) {
            siblings.add(node);
            if (node.mSiblings != null) {
                for (int i = 0; i < node.mSiblings.size(); i++) {
                    findSiblings(node.mSiblings.get(i), siblings);
                }
            }
        }
    }

    public boolean shouldPlayTogether() {
        updateAnimatorsDuration();
        createDependencyGraph();
        return this.mRootNode.mChildNodes == null || this.mRootNode.mChildNodes.size() == this.mNodes.size() - 1;
    }

    @Override // android.animation.Animator
    public long getTotalDuration() {
        updateAnimatorsDuration();
        createDependencyGraph();
        return this.mTotalDuration;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private Node getNodeForAnimation(Animator anim) {
        Node node = this.mNodeMap.get(anim);
        if (node != null) {
            return node;
        }
        Node node2 = new Node(anim);
        this.mNodeMap.put(anim, node2);
        this.mNodes.add(node2);
        return node2;
    }

    /* access modifiers changed from: private */
    public static class Node implements Cloneable {
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

        @Override // java.lang.Object
        public Node clone() {
            try {
                Node node = (Node) super.clone();
                node.mAnimation = this.mAnimation.clone();
                if (this.mChildNodes != null) {
                    node.mChildNodes = new ArrayList<>(this.mChildNodes);
                }
                if (this.mSiblings != null) {
                    node.mSiblings = new ArrayList<>(this.mSiblings);
                }
                if (this.mParents != null) {
                    node.mParents = new ArrayList<>(this.mParents);
                }
                node.mEnded = false;
                return node;
            } catch (CloneNotSupportedException e) {
                throw new AssertionError();
            }
        }

        /* access modifiers changed from: package-private */
        public void addChild(Node node) {
            if (this.mChildNodes == null) {
                this.mChildNodes = new ArrayList<>();
            }
            if (!this.mChildNodes.contains(node)) {
                this.mChildNodes.add(node);
                node.addParent(this);
            }
        }

        public void addSibling(Node node) {
            if (this.mSiblings == null) {
                this.mSiblings = new ArrayList<>();
            }
            if (!this.mSiblings.contains(node)) {
                this.mSiblings.add(node);
                node.addSibling(this);
            }
        }

        public void addParent(Node node) {
            if (this.mParents == null) {
                this.mParents = new ArrayList<>();
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
                    addParent(parents.get(i));
                }
            }
        }
    }

    /* access modifiers changed from: private */
    public static class AnimationEvent {
        static final int ANIMATION_DELAY_ENDED = 1;
        static final int ANIMATION_END = 2;
        static final int ANIMATION_START = 0;
        final int mEvent;
        final Node mNode;

        AnimationEvent(Node node, int event) {
            this.mNode = node;
            this.mEvent = event;
        }

        /* access modifiers changed from: package-private */
        public long getTime() {
            int i = this.mEvent;
            if (i == 0) {
                return this.mNode.mStartTime;
            }
            if (i != 1) {
                return this.mNode.mEndTime;
            }
            if (this.mNode.mStartTime == -1) {
                return -1;
            }
            return this.mNode.mAnimation.getStartDelay() + this.mNode.mStartTime;
        }

        public String toString() {
            String eventStr;
            int i = this.mEvent;
            if (i == 0) {
                eventStr = Telephony.BaseMmsColumns.START;
            } else {
                eventStr = i == 1 ? "delay ended" : "end";
            }
            return eventStr + WifiEnterpriseConfig.CA_CERT_ALIAS_DELIMITER + this.mNode.mAnimation.toString();
        }
    }

    /* access modifiers changed from: private */
    public class SeekState {
        private long mPlayTime;
        private boolean mSeekingInReverse;

        private SeekState() {
            this.mPlayTime = -1;
            this.mSeekingInReverse = false;
        }

        /* access modifiers changed from: package-private */
        public void reset() {
            this.mPlayTime = -1;
            this.mSeekingInReverse = false;
        }

        /* access modifiers changed from: package-private */
        public void setPlayTime(long playTime, boolean inReverse) {
            if (AnimatorSet.this.getTotalDuration() != -1) {
                this.mPlayTime = Math.min(playTime, AnimatorSet.this.getTotalDuration() - AnimatorSet.this.mStartDelay);
            }
            this.mPlayTime = Math.max(0L, this.mPlayTime);
            this.mSeekingInReverse = inReverse;
        }

        /* access modifiers changed from: package-private */
        public void updateSeekDirection(boolean inReverse) {
            if (inReverse && AnimatorSet.this.getTotalDuration() == -1) {
                throw new UnsupportedOperationException("Error: Cannot reverse infinite animator set");
            } else if (this.mPlayTime >= 0 && inReverse != this.mSeekingInReverse) {
                this.mPlayTime = (AnimatorSet.this.getTotalDuration() - AnimatorSet.this.mStartDelay) - this.mPlayTime;
                this.mSeekingInReverse = inReverse;
            }
        }

        /* access modifiers changed from: package-private */
        public long getPlayTime() {
            return this.mPlayTime;
        }

        /* access modifiers changed from: package-private */
        public long getPlayTimeNormalized() {
            if (AnimatorSet.this.mReversing) {
                return (AnimatorSet.this.getTotalDuration() - AnimatorSet.this.mStartDelay) - this.mPlayTime;
            }
            return this.mPlayTime;
        }

        /* access modifiers changed from: package-private */
        public boolean isActive() {
            return this.mPlayTime != -1;
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
            ValueAnimator anim = ValueAnimator.ofFloat(0.0f, 1.0f);
            anim.setDuration(delay);
            after(anim);
            return this;
        }
    }
}
