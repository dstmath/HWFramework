package android.animation;

import android.animation.Animator.AnimatorListener;
import android.app.ActivityThread;
import android.app.Application;
import android.net.wifi.WifiEnterpriseConfig;
import android.speech.tts.TextToSpeech.Engine;
import android.util.ArrayMap;
import android.util.Log;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public final class AnimatorSet extends Animator {
    private static final String TAG = "AnimatorSet";
    private ValueAnimator mDelayAnim;
    private boolean mDependencyDirty;
    private long mDuration;
    private TimeInterpolator mInterpolator;
    private ArrayMap<Animator, Node> mNodeMap;
    private ArrayList<Node> mNodes;
    private ArrayList<Animator> mPlayingSet;
    private boolean mReversible;
    private Node mRootNode;
    private AnimatorSetListener mSetListener;
    private final boolean mShouldIgnoreEndWithoutStart;
    private long mStartDelay;
    private boolean mStarted;
    private boolean mTerminated;
    private long mTotalDuration;

    private static class AnimatorSetListener implements AnimatorListener {
        private AnimatorSet mAnimatorSet;

        AnimatorSetListener(AnimatorSet animatorSet) {
            this.mAnimatorSet = animatorSet;
        }

        public void onAnimationCancel(Animator animation) {
            if (!this.mAnimatorSet.mTerminated && this.mAnimatorSet.mPlayingSet.size() == 0) {
                ArrayList<AnimatorListener> listeners = this.mAnimatorSet.mListeners;
                if (listeners != null) {
                    int numListeners = listeners.size();
                    for (int i = 0; i < numListeners; i++) {
                        ((AnimatorListener) listeners.get(i)).onAnimationCancel(this.mAnimatorSet);
                    }
                }
            }
        }

        public void onAnimationEnd(Animator animation) {
            animation.removeListener(this);
            this.mAnimatorSet.mPlayingSet.remove(animation);
            this.mAnimatorSet.onChildAnimatorEnded(animation);
        }

        public void onAnimationRepeat(Animator animation) {
        }

        public void onAnimationStart(Animator animation) {
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
            AnimatorSet.this.mReversible = false;
            this.mCurrentNode.addChild(AnimatorSet.this.getNodeForAnimation(anim));
            return this;
        }

        public Builder after(Animator anim) {
            AnimatorSet.this.mReversible = false;
            this.mCurrentNode.addParent(AnimatorSet.this.getNodeForAnimation(anim));
            return this;
        }

        public Builder after(long delay) {
            Animator anim = ValueAnimator.ofFloat(0.0f, Engine.DEFAULT_VOLUME);
            anim.setDuration(delay);
            after(anim);
            return this;
        }
    }

    private static class Node implements Cloneable {
        Animator mAnimation;
        ArrayList<Node> mChildNodes;
        long mEndTime;
        boolean mEnded;
        Node mLatestParent;
        ArrayList<Node> mParents;
        boolean mParentsAdded;
        ArrayList<Node> mSiblings;
        long mStartTime;
        private Node mTmpClone;
        long mTotalDuration;

        public Node(Animator animation) {
            this.mChildNodes = null;
            this.mTmpClone = null;
            this.mEnded = false;
            this.mLatestParent = null;
            this.mParentsAdded = false;
            this.mStartTime = 0;
            this.mEndTime = 0;
            this.mTotalDuration = 0;
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

    public AnimatorSet() {
        this.mPlayingSet = new ArrayList();
        this.mNodeMap = new ArrayMap();
        this.mNodes = new ArrayList();
        this.mSetListener = new AnimatorSetListener(this);
        this.mTerminated = false;
        this.mDependencyDirty = false;
        this.mStarted = false;
        this.mStartDelay = 0;
        this.mDelayAnim = ValueAnimator.ofFloat(0.0f, Engine.DEFAULT_VOLUME).setDuration(0);
        this.mRootNode = new Node(this.mDelayAnim);
        this.mDuration = -1;
        this.mInterpolator = null;
        this.mReversible = true;
        this.mTotalDuration = 0;
        this.mNodeMap.put(this.mDelayAnim, this.mRootNode);
        this.mNodes.add(this.mRootNode);
        Application app = ActivityThread.currentApplication();
        if (app == null || app.getApplicationInfo() == null) {
            this.mShouldIgnoreEndWithoutStart = true;
        } else if (app.getApplicationInfo().targetSdkVersion < 24) {
            this.mShouldIgnoreEndWithoutStart = true;
        } else {
            this.mShouldIgnoreEndWithoutStart = false;
        }
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
        this.mReversible = false;
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
            this.mReversible = false;
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
        this.mTerminated = true;
        if (isStarted()) {
            int size;
            int i;
            ArrayList arrayList = null;
            if (this.mListeners != null) {
                arrayList = (ArrayList) this.mListeners.clone();
                size = arrayList.size();
                for (i = 0; i < size; i++) {
                    ((AnimatorListener) arrayList.get(i)).onAnimationCancel(this);
                }
            }
            ArrayList<Animator> playingSet = new ArrayList(this.mPlayingSet);
            int setSize = playingSet.size();
            for (i = 0; i < setSize; i++) {
                ((Animator) playingSet.get(i)).cancel();
            }
            if (arrayList != null) {
                size = arrayList.size();
                for (i = 0; i < size; i++) {
                    ((AnimatorListener) arrayList.get(i)).onAnimationEnd(this);
                }
            }
            this.mStarted = false;
        }
    }

    public void end() {
        if (!this.mShouldIgnoreEndWithoutStart || isStarted()) {
            this.mTerminated = true;
            if (isStarted()) {
                endRemainingAnimations();
            }
            if (this.mListeners != null) {
                ArrayList<AnimatorListener> tmpListeners = (ArrayList) this.mListeners.clone();
                for (int i = 0; i < tmpListeners.size(); i++) {
                    ((AnimatorListener) tmpListeners.get(i)).onAnimationEnd(this);
                }
            }
            this.mStarted = false;
        }
    }

    private void endRemainingAnimations() {
        ArrayList<Animator> remainingList = new ArrayList(this.mNodes.size());
        remainingList.addAll(this.mPlayingSet);
        int index = 0;
        while (index < remainingList.size()) {
            Animator anim = (Animator) remainingList.get(index);
            anim.end();
            index++;
            Node node = (Node) this.mNodeMap.get(anim);
            if (node.mChildNodes != null) {
                int childSize = node.mChildNodes.size();
                for (int i = 0; i < childSize; i++) {
                    Node child = (Node) node.mChildNodes.get(i);
                    if (child.mLatestParent == node) {
                        remainingList.add(child.mAnimation);
                    }
                }
            }
        }
    }

    public boolean isRunning() {
        int size = this.mNodes.size();
        for (int i = 0; i < size; i++) {
            Node node = (Node) this.mNodes.get(i);
            if (node != this.mRootNode && node.mAnimation.isStarted()) {
                return true;
            }
        }
        return false;
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
            if (this.mStartDelay > 0) {
                this.mReversible = false;
            }
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
        boolean previouslyPaused = this.mPaused;
        super.pause();
        if (!previouslyPaused && this.mPaused) {
            if (this.mDelayAnim.isStarted()) {
                this.mDelayAnim.pause();
                return;
            }
            int size = this.mNodes.size();
            for (int i = 0; i < size; i++) {
                Node node = (Node) this.mNodes.get(i);
                if (node != this.mRootNode) {
                    node.mAnimation.pause();
                }
            }
        }
    }

    public void resume() {
        boolean previouslyPaused = this.mPaused;
        super.resume();
        if (previouslyPaused && !this.mPaused) {
            if (this.mDelayAnim.isStarted()) {
                this.mDelayAnim.resume();
                return;
            }
            int size = this.mNodes.size();
            for (int i = 0; i < size; i++) {
                Node node = (Node) this.mNodes.get(i);
                if (node != this.mRootNode) {
                    node.mAnimation.resume();
                }
            }
        }
    }

    public void start() {
        int i;
        this.mTerminated = false;
        this.mStarted = true;
        this.mPaused = false;
        int size = this.mNodes.size();
        for (i = 0; i < size; i++) {
            Node node = (Node) this.mNodes.get(i);
            node.mEnded = false;
            node.mAnimation.setAllowRunningAsynchronously(false);
        }
        if (this.mInterpolator != null) {
            for (i = 0; i < size; i++) {
                ((Node) this.mNodes.get(i)).mAnimation.setInterpolator(this.mInterpolator);
            }
        }
        updateAnimatorsDuration();
        createDependencyGraph();
        boolean setIsEmpty = false;
        if (this.mStartDelay > 0) {
            start(this.mRootNode);
        } else if (this.mNodes.size() > 1) {
            onChildAnimatorEnded(this.mDelayAnim);
        } else {
            setIsEmpty = true;
        }
        if (this.mListeners != null) {
            ArrayList<AnimatorListener> tmpListeners = (ArrayList) this.mListeners.clone();
            int numListeners = tmpListeners.size();
            for (i = 0; i < numListeners; i++) {
                ((AnimatorListener) tmpListeners.get(i)).onAnimationStart(this);
            }
        }
        if (setIsEmpty) {
            onChildAnimatorEnded(this.mDelayAnim);
        }
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

    void start(Node node) {
        Animator anim = node.mAnimation;
        this.mPlayingSet.add(anim);
        anim.addListener(this.mSetListener);
        anim.start();
    }

    public AnimatorSet clone() {
        int n;
        int i;
        AnimatorSet anim = (AnimatorSet) super.clone();
        int nodeCount = this.mNodes.size();
        anim.mTerminated = false;
        anim.mStarted = false;
        anim.mPlayingSet = new ArrayList();
        anim.mNodeMap = new ArrayMap();
        anim.mNodes = new ArrayList(nodeCount);
        anim.mReversible = this.mReversible;
        anim.mSetListener = new AnimatorSetListener(anim);
        for (n = 0; n < nodeCount; n++) {
            Node node = (Node) this.mNodes.get(n);
            Node nodeClone = node.clone();
            node.mTmpClone = nodeClone;
            anim.mNodes.add(nodeClone);
            anim.mNodeMap.put(nodeClone.mAnimation, nodeClone);
            ArrayList<AnimatorListener> cloneListeners = nodeClone.mAnimation.getListeners();
            if (cloneListeners != null) {
                for (i = cloneListeners.size() - 1; i >= 0; i--) {
                    if (((AnimatorListener) cloneListeners.get(i)) instanceof AnimatorSetListener) {
                        cloneListeners.remove(i);
                    }
                }
            }
        }
        anim.mRootNode = this.mRootNode.mTmpClone;
        anim.mDelayAnim = (ValueAnimator) anim.mRootNode.mAnimation;
        for (i = 0; i < nodeCount; i++) {
            Node node2;
            int j;
            node = (Node) this.mNodes.get(i);
            Node -get0 = node.mTmpClone;
            if (node.mLatestParent == null) {
                node2 = null;
            } else {
                node2 = node.mLatestParent.mTmpClone;
            }
            -get0.mLatestParent = node2;
            int size = node.mChildNodes == null ? 0 : node.mChildNodes.size();
            for (j = 0; j < size; j++) {
                node.mTmpClone.mChildNodes.set(j, ((Node) node.mChildNodes.get(j)).mTmpClone);
            }
            size = node.mSiblings == null ? 0 : node.mSiblings.size();
            for (j = 0; j < size; j++) {
                node.mTmpClone.mSiblings.set(j, ((Node) node.mSiblings.get(j)).mTmpClone);
            }
            if (node.mParents == null) {
                size = 0;
            } else {
                size = node.mParents.size();
            }
            for (j = 0; j < size; j++) {
                node.mTmpClone.mParents.set(j, ((Node) node.mParents.get(j)).mTmpClone);
            }
        }
        for (n = 0; n < nodeCount; n++) {
            ((Node) this.mNodes.get(n)).mTmpClone = null;
        }
        return anim;
    }

    private void onChildAnimatorEnded(Animator animation) {
        Node animNode = (Node) this.mNodeMap.get(animation);
        animNode.mEnded = true;
        if (!this.mTerminated) {
            int i;
            List<Node> children = animNode.mChildNodes;
            int childrenSize = children == null ? 0 : children.size();
            for (i = 0; i < childrenSize; i++) {
                if (((Node) children.get(i)).mLatestParent == animNode) {
                    start((Node) children.get(i));
                }
            }
            boolean allDone = true;
            int size = this.mNodes.size();
            for (i = 0; i < size; i++) {
                if (!((Node) this.mNodes.get(i)).mEnded) {
                    allDone = false;
                    break;
                }
            }
            if (allDone) {
                if (this.mListeners != null) {
                    ArrayList<AnimatorListener> tmpListeners = (ArrayList) this.mListeners.clone();
                    int numListeners = tmpListeners.size();
                    for (i = 0; i < numListeners; i++) {
                        ((AnimatorListener) tmpListeners.get(i)).onAnimationEnd(this);
                    }
                }
                this.mStarted = false;
                this.mPaused = false;
            }
        }
    }

    public boolean canReverse() {
        if (!this.mReversible) {
            return false;
        }
        int size = this.mNodes.size();
        for (int i = 0; i < size; i++) {
            Node node = (Node) this.mNodes.get(i);
            if (!node.mAnimation.canReverse() || node.mAnimation.getStartDelay() > 0) {
                return false;
            }
        }
        return true;
    }

    public void reverse() {
        if (canReverse()) {
            int size = this.mNodes.size();
            for (int i = 0; i < size; i++) {
                ((Node) this.mNodes.get(i)).mAnimation.reverse();
            }
        }
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
            Node node = (Node) this.mNodes.get(i);
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
        long maxEndTime = 0;
        for (i = 0; i < size; i++) {
            node = (Node) this.mNodes.get(i);
            node.mTotalDuration = node.mAnimation.getTotalDuration();
            if (node.mEndTime == -1) {
                maxEndTime = -1;
                break;
            }
            if (node.mEndTime > maxEndTime) {
                maxEndTime = node.mEndTime;
            }
        }
        this.mTotalDuration = maxEndTime;
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
        return this.mRootNode.mChildNodes.size() == this.mNodes.size() + -1;
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
