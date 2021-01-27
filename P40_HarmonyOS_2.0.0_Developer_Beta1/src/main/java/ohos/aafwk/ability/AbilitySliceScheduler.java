package ohos.aafwk.ability;

import java.io.PrintWriter;
import java.lang.reflect.InvocationTargetException;
import java.util.List;
import ohos.aafwk.ability.AbilitySliceLifecycleExecutor;
import ohos.aafwk.content.Intent;
import ohos.aafwk.content.IntentParams;
import ohos.aafwk.utils.dfx.hiview.AbilityHiviewWrapper;
import ohos.aafwk.utils.dfx.hiview.EventInfo;
import ohos.aafwk.utils.log.Log;
import ohos.aafwk.utils.log.LogLabel;
import ohos.agp.transition.TransitionScheduler;
import ohos.app.dispatcher.TaskDispatcher;
import ohos.bundle.AbilityInfo;
import ohos.com.sun.org.apache.xalan.internal.templates.Constants;
import ohos.tools.Bytrace;

public final class AbilitySliceScheduler {
    private static final LogLabel LABEL = LogLabel.create();
    private static final String SAVE_DATA_STACK = "AbilitySliceStack";
    private static final int STACK_MAX_NUM = 1024;
    private static final String STACK_SUFFIX = ":index";
    private static final String TOP_SUFFIX = ":top";
    private AbilitySliceJumpRecordSet abilitySliceJumpRecordsSet = null;
    private AbilitySliceManager abilitySliceManager = null;
    private AbilitySliceStack abilitySliceStack = null;
    private final Object syncLock = new Object();
    private TaskDispatcher taskDispatcher = null;
    private AbilitySlice topAbilitySlice;

    /* access modifiers changed from: package-private */
    public void attach(AbilitySliceManager abilitySliceManager2) {
        if (abilitySliceManager2 != null) {
            this.abilitySliceManager = abilitySliceManager2;
            this.abilitySliceStack = new AbilitySliceStack();
            this.abilitySliceJumpRecordsSet = new AbilitySliceJumpRecordSet();
            if (abilitySliceManager2.getContext() != null) {
                this.taskDispatcher = abilitySliceManager2.getContext().getUITaskDispatcher();
            }
            if (this.taskDispatcher == null) {
                throw new IllegalArgumentException("Instance taskDispatcher failed.");
            } else if (Log.isDebuggable()) {
                Log.debug(LABEL, "AbilitySliceScheduler init success.", new Object[0]);
            }
        } else {
            throw new IllegalArgumentException("AbilitySliceManager is null.");
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private boolean checkLegalForAdd(AbilitySlice abilitySlice, AbilitySlice abilitySlice2, Intent intent, int i) {
        if (this.topAbilitySlice == null) {
            Log.error(LABEL, "topAbilitySlice is null, not allow jump.", new Object[0]);
            return false;
        } else if (this.abilitySliceManager.getAbilityState() != Ability.STATE_ACTIVE) {
            Log.warn(LABEL, "Ability not active, not allow jump.", new Object[0]);
            return false;
        } else if (abilitySlice.getState() == AbilitySliceLifecycleExecutor.LifecycleState.INITIAL) {
            Log.warn(LABEL, "Caller Slice is INITIAL, not allow call jump.", new Object[0]);
            return false;
        } else if (abilitySlice2.getState() == AbilitySliceLifecycleExecutor.LifecycleState.ACTIVE) {
            Log.warn(LABEL, "Target Slice already active, do nothing.", new Object[0]);
            return false;
        } else if (this.topAbilitySlice.equals(abilitySlice2)) {
            Log.warn(LABEL, "Target Slice already top, do nothing.", new Object[0]);
            return false;
        } else if (this.abilitySliceStack.size() >= 1024) {
            Log.error(LABEL, "Stack is full(%{public}d), jump failed.", 1024);
            return false;
        } else {
            if (!(intent == null || (intent.getFlags() & 67108864) == 0)) {
                if (!checkLegalForAddWithResult(abilitySlice2)) {
                    return false;
                }
                if (i >= 0) {
                    Log.error(LABEL, "forResult can't work with flag FLAG_ABILITYSLICE_FORWARD_RESULT simultaneously, jump failed", new Object[0]);
                    return false;
                } else if (!this.abilitySliceJumpRecordsSet.checkForwardCaller(abilitySlice)) {
                    Log.error(LABEL, "no forward target found for caller: %{public}s, maybe no presentForResult called before, jump failed.", abilitySlice);
                    return false;
                }
            }
            return true;
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private boolean checkLegalForAddWithResult(AbilitySlice abilitySlice) {
        if (this.topAbilitySlice == null) {
            Log.error(LABEL, "topAbilitySlice is null, not allow jump.", new Object[0]);
            return false;
        } else if (!this.abilitySliceStack.exist(abilitySlice) && !this.topAbilitySlice.equals(abilitySlice)) {
            return true;
        } else {
            Log.error(LABEL, "refuse to jump. target: %{public}s already exists in the stack or top. singleton jump is forbidden for presentForResult.", abilitySlice.getClass().getName());
            return false;
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private boolean checkLegalForRemove(AbilitySlice abilitySlice) {
        if (this.topAbilitySlice == null) {
            Log.error(LABEL, "topAbilitySlice is null, not allow jump.", new Object[0]);
            return false;
        } else if (this.abilitySliceManager.getAbilityState() == Ability.STATE_INITIAL || this.abilitySliceManager.getAbilityState() == Ability.STATE_UNINITIALIZED) {
            Log.warn(LABEL, "Ability is intialized or stopped, not allow jump.", new Object[0]);
            return false;
        } else if (abilitySlice == null) {
            Log.error(LABEL, "Slice is not in the Control List, do nothing.", new Object[0]);
            return false;
        } else if (!this.abilitySliceStack.exist(abilitySlice) && abilitySlice != this.topAbilitySlice) {
            Log.error(LABEL, "Slice %{public}s is not in the stack and not on top, can't terminate.", abilitySlice);
            return false;
        } else if (abilitySlice.getState() == AbilitySliceLifecycleExecutor.LifecycleState.INITIAL) {
            Log.error(LABEL, "Slice is already in State:INITIAL, do nothing.", new Object[0]);
            return false;
        } else if (!this.abilitySliceStack.isEmpty()) {
            return true;
        } else {
            Log.info(LABEL, "Slice is the last AS, follow the Ability.", new Object[0]);
            this.abilitySliceManager.terminateAbility();
            return false;
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void getSliceResultAndDeliver(AbilitySlice abilitySlice) {
        for (AbilitySliceResultInfo abilitySliceResultInfo : this.abilitySliceJumpRecordsSet.getResultInfoByDst(abilitySlice)) {
            abilitySlice.onResult(abilitySliceResultInfo.requestCode, abilitySliceResultInfo.resultIntent);
        }
        this.abilitySliceJumpRecordsSet.cleanResultInfoByDst(abilitySlice);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void addAbilitySliceSchedule(AbilitySlice abilitySlice, AbilitySlice abilitySlice2, Intent intent) {
        if (this.topAbilitySlice.getState() == AbilitySliceLifecycleExecutor.LifecycleState.ACTIVE) {
            this.topAbilitySlice.inactive();
        }
        if (abilitySlice2.getState() == AbilitySliceLifecycleExecutor.LifecycleState.INITIAL) {
            abilitySlice2.start(intent);
            AbilitySliceTransitionController transitionController = this.abilitySliceManager.getTransitionController();
            if (transitionController == null || !transitionController.isTransitionEnabled()) {
                abilitySlice2.componentEnterAnimator();
            } else {
                transitionController.startTransition(abilitySlice.getCurrentUI(), abilitySlice2.getCurrentUI());
            }
            abilitySlice2.active();
        } else if (abilitySlice2.getState() == AbilitySliceLifecycleExecutor.LifecycleState.BACKGROUND) {
            getSliceResultAndDeliver(abilitySlice2);
            abilitySlice2.setLatestUIAttachedFlag(false);
            abilitySlice2.foreground(intent);
            abilitySlice2.active();
        } else {
            Log.error(LABEL, "Target Slice state illegal: %{public}s", abilitySlice2.getState());
        }
        if (this.topAbilitySlice.getState() == AbilitySliceLifecycleExecutor.LifecycleState.INACTIVE) {
            this.topAbilitySlice.background();
        }
        if (this.abilitySliceStack.exist(abilitySlice2)) {
            stopAbilitySlice(this.topAbilitySlice);
            while (this.abilitySliceStack.top() != abilitySlice2) {
                stopAbilitySlice(this.abilitySliceStack.pop());
            }
            this.topAbilitySlice = this.abilitySliceStack.pop();
            return;
        }
        this.abilitySliceStack.push(this.topAbilitySlice);
        this.topAbilitySlice = abilitySlice2;
    }

    /* access modifiers changed from: package-private */
    public void addAbilitySlice(final AbilitySlice abilitySlice, final AbilitySlice abilitySlice2, final Intent intent, boolean z) {
        if (abilitySlice == null || abilitySlice2 == null || intent == null) {
            Log.error(LABEL, "Input paras is NULL, jump failed", new Object[0]);
        } else if (this.taskDispatcher == null || this.topAbilitySlice == null) {
            Log.error(LABEL, "Internal state error, jump failed", new Object[0]);
        } else {
            final int hashCode = String.valueOf(abilitySlice.hashCode() + abilitySlice2.hashCode()).hashCode();
            Bytrace.startAsyncTrace(2147483648L, "present", hashCode);
            if (z) {
                this.taskDispatcher.syncDispatch(new Runnable() {
                    /* class ohos.aafwk.ability.AbilitySliceScheduler.AnonymousClass1 */

                    @Override // java.lang.Runnable
                    public void run() {
                        synchronized (AbilitySliceScheduler.this.syncLock) {
                            AbilitySliceScheduler.this.doAddAbilitySlice(hashCode, abilitySlice, abilitySlice2, intent);
                        }
                    }
                });
            } else {
                this.taskDispatcher.asyncDispatch(new Runnable() {
                    /* class ohos.aafwk.ability.AbilitySliceScheduler.AnonymousClass2 */

                    @Override // java.lang.Runnable
                    public void run() {
                        synchronized (AbilitySliceScheduler.this.syncLock) {
                            AbilitySliceScheduler.this.doAddAbilitySlice(hashCode, abilitySlice, abilitySlice2, intent);
                        }
                    }
                });
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void doAddAbilitySlice(int i, AbilitySlice abilitySlice, AbilitySlice abilitySlice2, Intent intent) {
        Bytrace.finishAsyncTrace(2147483648L, "present", i);
        if (checkLegalForAdd(abilitySlice, abilitySlice2, intent, -1)) {
            abilitySlice2.setAbilityShell(this.abilitySliceManager.getAbilityShell());
            abilitySlice2.init(this.abilitySliceManager.getContext(), this.abilitySliceManager);
            if (!(intent == null || (intent.getFlags() & 67108864) == 0)) {
                if (Log.isDebuggable()) {
                    Log.debug(LABEL, "add AbilitySlice with forward flag. caller: %{public}s, target: %{public}s.", abilitySlice.getClass().getName(), abilitySlice2.getClass().getName());
                }
                this.abilitySliceJumpRecordsSet.updateForwardCaller(abilitySlice, abilitySlice2);
            }
            addAbilitySliceSchedule(abilitySlice, abilitySlice2, intent);
        } else if (intent != null && (intent.getFlags() & 67108864) != 0) {
            abilitySlice.onResult(-1, null);
        }
    }

    /* access modifiers changed from: package-private */
    public void addAbilitySliceWithNewIntent(AbilitySlice abilitySlice, AbilitySlice abilitySlice2, Intent intent) {
        if (abilitySlice == null || abilitySlice2 == null || intent == null) {
            Log.error(LABEL, "Input paras is NULL, jump failed", new Object[0]);
        } else if (this.topAbilitySlice == null) {
            Log.error(LABEL, "Internal state error, jump failed", new Object[0]);
        } else if (this.abilitySliceStack.size() >= 1024) {
            Log.error(LABEL, "Stack is full(%{public}d), jump failed.", 1024);
        } else {
            abilitySlice2.setAbilityShell(this.abilitySliceManager.getAbilityShell());
            abilitySlice2.init(this.abilitySliceManager.getContext(), this.abilitySliceManager);
            Log.info(LABEL, "newIntent caller slice %{public}s, add slice %{public}s scheduler, topAbilitySlice = %{public}s", abilitySlice, abilitySlice2, this.topAbilitySlice);
            if (abilitySlice2.getState() == AbilitySliceLifecycleExecutor.LifecycleState.INITIAL) {
                abilitySlice2.start(intent);
            } else {
                Log.error(LABEL, "Target Slice state illegal: %{public}s", abilitySlice2.getState());
            }
            this.topAbilitySlice.setLatestUIAttachedFlag(false);
            if (this.topAbilitySlice.getState() == AbilitySliceLifecycleExecutor.LifecycleState.INACTIVE) {
                this.topAbilitySlice.background();
            }
            if (this.abilitySliceStack.exist(abilitySlice2)) {
                stopAbilitySlice(this.topAbilitySlice);
                while (this.abilitySliceStack.top() != abilitySlice2) {
                    stopAbilitySlice(this.abilitySliceStack.pop());
                }
                this.topAbilitySlice = this.abilitySliceStack.pop();
                return;
            }
            this.abilitySliceStack.push(this.topAbilitySlice);
            this.topAbilitySlice = abilitySlice2;
        }
    }

    /* access modifiers changed from: package-private */
    public void addAbilitySliceForResult(final AbilitySlice abilitySlice, final AbilitySlice abilitySlice2, final Intent intent, final int i) {
        if (abilitySlice == null || abilitySlice2 == null || intent == null) {
            Log.error(LABEL, "Input paras is NULL, jump failed", new Object[0]);
        } else if (i < 0) {
            Log.error(LABEL, "Input para requestCode: %{public}d, jump failed", Integer.valueOf(i));
        } else if (this.taskDispatcher == null || this.topAbilitySlice == null) {
            Log.error(LABEL, "Internal state error, jump for result failed", new Object[0]);
        } else {
            final int hashCode = String.valueOf(abilitySlice.hashCode() + abilitySlice2.hashCode()).hashCode();
            Bytrace.startAsyncTrace(2147483648L, "presentForResult", hashCode);
            this.taskDispatcher.asyncDispatch(new Runnable() {
                /* class ohos.aafwk.ability.AbilitySliceScheduler.AnonymousClass3 */

                @Override // java.lang.Runnable
                public void run() {
                    synchronized (AbilitySliceScheduler.this.syncLock) {
                        Bytrace.finishAsyncTrace(2147483648L, "presentForResult", hashCode);
                        if (AbilitySliceScheduler.this.checkLegalForAddWithResult(abilitySlice2)) {
                            if (AbilitySliceScheduler.this.checkLegalForAdd(abilitySlice, abilitySlice2, intent, i)) {
                                abilitySlice2.setAbilityShell(AbilitySliceScheduler.this.abilitySliceManager.getAbilityShell());
                                abilitySlice2.init(AbilitySliceScheduler.this.abilitySliceManager.getContext(), AbilitySliceScheduler.this.abilitySliceManager);
                                AbilitySliceScheduler.this.abilitySliceJumpRecordsSet.prepareEmptyRecord(abilitySlice, abilitySlice2, i);
                                AbilitySliceScheduler.this.addAbilitySliceSchedule(abilitySlice, abilitySlice2, intent);
                                return;
                            }
                        }
                        abilitySlice.onResult(i, null);
                    }
                }
            });
        }
    }

    private void removeAllAbilitySlice() {
        for (AbilitySlice abilitySlice : this.abilitySliceStack.getAllSlices()) {
            Log.debug(LABEL, "removeAllAbilitySlice remove %{public}s", abilitySlice.getClass().getSimpleName());
            removeAbilitySlice(abilitySlice, null, false);
        }
        AbilitySlice abilitySlice2 = this.topAbilitySlice;
        if (abilitySlice2 != null) {
            Log.debug(LABEL, "removeAllAbilitySlice remove topSlice %{public}s", abilitySlice2.getClass().getSimpleName());
            this.topAbilitySlice.inactive();
            this.topAbilitySlice.background();
            this.topAbilitySlice.stop();
            this.topAbilitySlice = null;
        }
    }

    /* access modifiers changed from: private */
    public class RemoveAbilitySliceTask implements Runnable {
        volatile AbilitySlice abilitySlice;
        Intent intent;

        /* access modifiers changed from: private */
        public class TransitionEndListener implements TransitionScheduler.ITransitionEndListener {
            private TransitionEndListener() {
            }

            @Override // ohos.agp.transition.TransitionScheduler.ITransitionEndListener
            public void onTransitionEnd() {
                AbilitySliceScheduler.this.stopAbilitySlice(RemoveAbilitySliceTask.this.abilitySlice);
            }
        }

        RemoveAbilitySliceTask(AbilitySlice abilitySlice2, Intent intent2) {
            this.abilitySlice = abilitySlice2;
            this.intent = intent2;
        }

        private void scheduleSliceWhenAbilityActive() {
            AbilitySliceScheduler.this.topAbilitySlice.inactive();
            AbilitySlice pop = AbilitySliceScheduler.this.abilitySliceStack.pop();
            if (pop == null) {
                Log.error(AbilitySliceScheduler.LABEL, "Stack is empty, back failed", new Object[0]);
                AbilitySliceScheduler.this.topAbilitySlice.active();
                return;
            }
            AbilitySliceScheduler.this.getSliceResultAndDeliver(pop);
            pop.setLatestUIAttachedFlag(false);
            pop.foreground(this.intent);
            AbilitySliceTransitionController transitionController = AbilitySliceScheduler.this.abilitySliceManager.getTransitionController();
            if (transitionController == null || !transitionController.isTransitionEnabled()) {
                pop.componentExitAnimator();
            } else {
                transitionController.startTransition(AbilitySliceScheduler.this.topAbilitySlice.getCurrentUI(), pop.getCurrentUI(), new TransitionEndListener());
            }
            pop.active();
            AbilitySliceScheduler.this.topAbilitySlice.background();
            if (transitionController == null || !transitionController.isTransitionEnabled()) {
                AbilitySliceScheduler.this.stopAbilitySlice(this.abilitySlice);
            }
            AbilitySliceScheduler.this.topAbilitySlice = pop;
        }

        private void scheduleSliceWhenAbilityBackground() {
            AbilitySlice pop = AbilitySliceScheduler.this.abilitySliceStack.pop();
            if (pop == null) {
                Log.error(AbilitySliceScheduler.LABEL, "Stack is empty, back failed", new Object[0]);
                return;
            }
            pop.setLatestUIAttachedFlag(false);
            if (AbilitySliceScheduler.this.abilitySliceManager.getAbilityState() == Ability.STATE_INACTIVE) {
                pop.foreground(this.intent);
            }
            AbilitySliceScheduler.this.topAbilitySlice.background();
            AbilitySliceScheduler.this.stopAbilitySlice(this.abilitySlice);
            AbilitySliceScheduler.this.topAbilitySlice = pop;
        }

        @Override // java.lang.Runnable
        public void run() {
            synchronized (AbilitySliceScheduler.this.syncLock) {
                Bytrace.startAsyncTrace(2147483648L, Constants.ATTRNAME_TERMINATE, String.valueOf(this.abilitySlice.hashCode()).hashCode());
                if (AbilitySliceScheduler.this.checkLegalForRemove(this.abilitySlice)) {
                    AbilitySliceScheduler.this.abilitySliceJumpRecordsSet.saveResultInfo(this.abilitySlice, this.intent);
                    if (!this.abilitySlice.equals(AbilitySliceScheduler.this.topAbilitySlice)) {
                        AbilitySliceScheduler.this.stopAbilitySlice(this.abilitySlice);
                        if (!AbilitySliceScheduler.this.abilitySliceStack.remove(this.abilitySlice)) {
                            Log.error(AbilitySliceScheduler.LABEL, "Remove Slice from stack failed.", new Object[0]);
                        }
                    } else if (AbilitySliceScheduler.this.abilitySliceManager.getAbilityState() == Ability.STATE_ACTIVE) {
                        scheduleSliceWhenAbilityActive();
                    } else {
                        scheduleSliceWhenAbilityBackground();
                    }
                }
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void removeAbilitySlice(AbilitySlice abilitySlice, Intent intent, boolean z) {
        if (abilitySlice == null) {
            Log.error(LABEL, "Input paras is NULL, back failed", new Object[0]);
        } else if (this.taskDispatcher == null || this.topAbilitySlice == null) {
            Log.error(LABEL, "Internal state error, back failed", new Object[0]);
        } else {
            Bytrace.startAsyncTrace(2147483648L, Constants.ATTRNAME_TERMINATE, String.valueOf(abilitySlice.hashCode()).hashCode());
            if (z) {
                this.taskDispatcher.syncDispatch(new RemoveAbilitySliceTask(abilitySlice, intent));
            } else {
                this.taskDispatcher.asyncDispatch(new RemoveAbilitySliceTask(abilitySlice, intent));
            }
        }
    }

    private AbilitySlice getTargetAbilitySlice(Intent intent) throws IllegalStateException, IllegalArgumentException {
        String str;
        AbilitySlice abilitySlice;
        if (intent != null) {
            String mainRoute = this.abilitySliceManager.getAbilitySliceRoute().getMainRoute();
            if (mainRoute == null) {
                return null;
            }
            String action = intent.getAction();
            if (action == null || action.isEmpty()) {
                str = null;
            } else {
                Log.debug(LABEL, "getTargetAbilitySlice, action is: %{public}s", action);
                str = this.abilitySliceManager.getAbilitySliceRoute().matchRoute(action);
            }
            if (str == null) {
                AbilitySlice abilitySlice2 = this.topAbilitySlice;
                if (abilitySlice2 != null) {
                    return abilitySlice2;
                }
                str = mainRoute;
            }
            AbilitySlice abilitySlice3 = this.topAbilitySlice;
            if (abilitySlice3 != null && abilitySlice3.getClass().getName().equals(str)) {
                return this.topAbilitySlice;
            }
            if (!this.abilitySliceStack.isEmpty() && (abilitySlice = this.abilitySliceStack.get(str)) != null) {
                return abilitySlice;
            }
            AbilitySlice loadAbilitySlice = loadAbilitySlice(str);
            if (loadAbilitySlice != null) {
                loadAbilitySlice.setAbilityShell(this.abilitySliceManager.getAbilityShell());
                loadAbilitySlice.init(this.abilitySliceManager.getContext(), this.abilitySliceManager);
                return loadAbilitySlice;
            }
            throw new IllegalStateException("START failed, Class:" + str + " instance failed.");
        }
        throw new IllegalArgumentException("Intent is null.");
    }

    private AbilitySlice loadAbilitySlice(String str) {
        Bytrace.startTrace(2147483648L, "loadSliceInner");
        AbilitySlice abilitySlice = null;
        try {
            ClassLoader classLoader = this.abilitySliceManager.getClassLoader();
            if (classLoader != null) {
                Object newInstance = classLoader.loadClass(str).getDeclaredConstructor(new Class[0]).newInstance(new Object[0]);
                if (newInstance instanceof AbilitySlice) {
                    abilitySlice = (AbilitySlice) newInstance;
                }
                Bytrace.finishTrace(2147483648L, "loadSliceInner");
                return abilitySlice;
            }
            throw new IllegalAccessException("Can't get ClassLoader for slice");
        } catch (ClassCastException | ClassNotFoundException | IllegalAccessException | InstantiationException | NoSuchMethodException | InvocationTargetException unused) {
            Log.error(LABEL, "Can not instance %{public}s.", str);
        }
    }

    /* access modifiers changed from: package-private */
    public void handleStartAbilitySlice(Intent intent) throws IllegalStateException, IllegalArgumentException {
        if (this.topAbilitySlice == null) {
            this.topAbilitySlice = getTargetAbilitySlice(intent);
        }
        AbilitySlice abilitySlice = this.topAbilitySlice;
        if (abilitySlice != null) {
            try {
                abilitySlice.start(intent);
                if (this.topAbilitySlice.getState() != AbilitySliceLifecycleExecutor.LifecycleState.INACTIVE) {
                    throw new IllegalStateException("CMD start, Switch to state:INACTIVE failed.");
                }
            } catch (LifecycleException unused) {
                throw new IllegalStateException("Unable to handle start when:" + this.topAbilitySlice.getState());
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void handleStopAbilitySlice() {
        AbilitySlice abilitySlice = this.topAbilitySlice;
        if (abilitySlice != null) {
            try {
                stopAbilitySlice(abilitySlice);
                while (this.abilitySliceStack.top() != null) {
                    stopAbilitySlice(this.abilitySliceStack.pop());
                }
                if (this.topAbilitySlice.getState() == AbilitySliceLifecycleExecutor.LifecycleState.INITIAL) {
                    this.topAbilitySlice = null;
                    return;
                }
                throw new IllegalStateException("CMD stop, Switch to state:INITIAL failed.");
            } catch (LifecycleException unused) {
                throw new IllegalStateException("Unable handle stop when:" + this.topAbilitySlice.getState());
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void handleActiveAbilitySlice(Intent intent) {
        AbilitySlice abilitySlice = this.topAbilitySlice;
        if (abilitySlice != null) {
            try {
                abilitySlice.active();
                if (this.topAbilitySlice.getState() != AbilitySliceLifecycleExecutor.LifecycleState.ACTIVE) {
                    throw new IllegalStateException("CMD active, Switch to state:ACTIVE failed.");
                }
            } catch (LifecycleException unused) {
                throw new IllegalStateException("Unable handle active when:" + this.topAbilitySlice.getState());
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void handleAbilityNewIntent(Intent intent) {
        AbilitySlice targetAbilitySlice;
        if (this.topAbilitySlice != null && intent != null && this.topAbilitySlice != (targetAbilitySlice = getTargetAbilitySlice(intent))) {
            if (Log.isDebuggable()) {
                Log.debug(LABEL, "receive new intent with action, present other ability slice", new Object[0]);
            }
            addAbilitySliceWithNewIntent(this.topAbilitySlice, targetAbilitySlice, intent);
        }
    }

    /* access modifiers changed from: package-private */
    public void handleInactiveAbilitySlice() {
        AbilitySlice abilitySlice = this.topAbilitySlice;
        if (abilitySlice != null) {
            try {
                abilitySlice.inactive();
                if (this.topAbilitySlice.getState() != AbilitySliceLifecycleExecutor.LifecycleState.INACTIVE) {
                    throw new IllegalStateException("CMD inactive, Switch to state:INACTIVE failed.");
                }
            } catch (LifecycleException unused) {
                throw new IllegalStateException("Unable handle inactive when:" + this.topAbilitySlice.getState());
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void handleMoveAbilitySliceToBackground() {
        AbilitySlice abilitySlice = this.topAbilitySlice;
        if (abilitySlice != null) {
            try {
                abilitySlice.background();
                if (this.topAbilitySlice.getState() != AbilitySliceLifecycleExecutor.LifecycleState.BACKGROUND) {
                    throw new IllegalStateException("CMD background, Switch to state:BACKGROUND failed.");
                }
            } catch (LifecycleException unused) {
                throw new IllegalStateException("Unable handle background when:" + this.topAbilitySlice.getState());
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void handleMoveAbilitySliceToForeground(Intent intent) {
        AbilitySlice abilitySlice = this.topAbilitySlice;
        if (abilitySlice != null) {
            try {
                abilitySlice.foreground(intent);
            } catch (LifecycleException unused) {
                throw new IllegalStateException("Unable handle transaction when:" + this.topAbilitySlice.getState());
            }
        }
    }

    /* access modifiers changed from: package-private */
    public boolean onStartContinuation() {
        synchronized (this.syncLock) {
            if (this.topAbilitySlice == null) {
                return true;
            }
            if (!this.topAbilitySlice.scheduleStartContinuation()) {
                Log.warn(LABEL, "%{public}s not allow to StartContinuation.", this.topAbilitySlice.getClass().getName());
                return false;
            }
            for (AbilitySlice abilitySlice : this.abilitySliceStack.getAllSlices()) {
                if (!abilitySlice.scheduleStartContinuation()) {
                    Log.warn(LABEL, "%{public}s not allow to StartContinuation.", abilitySlice.getClass().getName());
                    return false;
                }
            }
            return true;
        }
    }

    private void saveData(IntentParams intentParams) {
        int size = this.abilitySliceStack.size() + 1;
        String[] strArr = new String[size];
        List<AbilitySlice> allSlices = this.abilitySliceStack.getAllSlices();
        for (int i = 0; i < allSlices.size(); i++) {
            strArr[i] = allSlices.get(i).getClass().getName();
        }
        strArr[size - 1] = this.topAbilitySlice.getClass().getName();
        intentParams.setParam(SAVE_DATA_STACK, strArr);
    }

    /* access modifiers changed from: package-private */
    public boolean onSaveData(IntentParams intentParams) {
        synchronized (this.syncLock) {
            if (this.topAbilitySlice == null) {
                return true;
            }
            IntentParams intentParams2 = new IntentParams();
            boolean scheduleSaveData = this.topAbilitySlice.scheduleSaveData(intentParams2);
            StringBuilder sb = new StringBuilder();
            String str = this.topAbilitySlice.getClass().getName() + TOP_SUFFIX;
            for (String str2 : intentParams2.keySet()) {
                sb.setLength(0);
                sb.append(str2);
                sb.append(str);
                intentParams.setParam(sb.toString(), intentParams2.getParam(str2));
            }
            if (!scheduleSaveData) {
                Log.warn(LABEL, "%{public}s failed to save data.", this.topAbilitySlice.getClass().getName());
                return false;
            }
            int i = 0;
            for (AbilitySlice abilitySlice : this.abilitySliceStack.getAllSlices()) {
                IntentParams intentParams3 = new IntentParams();
                boolean scheduleSaveData2 = abilitySlice.scheduleSaveData(intentParams3);
                StringBuilder sb2 = new StringBuilder();
                sb2.append(abilitySlice.getClass().getName());
                sb2.append(STACK_SUFFIX);
                int i2 = i + 1;
                sb2.append(i);
                String sb3 = sb2.toString();
                for (String str3 : intentParams3.keySet()) {
                    sb.setLength(0);
                    sb.append(str3);
                    sb.append(sb3);
                    intentParams.setParam(sb.toString(), intentParams3.getParam(str3));
                }
                if (!scheduleSaveData2) {
                    Log.warn(LABEL, "%{public}s failed to save data.", abilitySlice.getClass().getName());
                    return false;
                }
                i = i2;
            }
            saveData(intentParams);
            return true;
        }
    }

    /* access modifiers changed from: package-private */
    public boolean clearAndRestore(IntentParams intentParams) {
        removeAllAbilitySlice();
        boolean onRestoreData = onRestoreData(intentParams);
        activeTopSliceAfterRestore();
        return onRestoreData;
    }

    private void activeTopSliceAfterRestore() {
        AbilitySlice abilitySlice = this.topAbilitySlice;
        if (abilitySlice != null) {
            abilitySlice.setUiAttachedDisable(false);
            this.topAbilitySlice.start(new Intent());
            this.topAbilitySlice.active();
        }
    }

    private boolean restoreData(IntentParams intentParams) {
        if (!intentParams.hasParam(SAVE_DATA_STACK)) {
            Log.info(LABEL, "RestoreData missing %{public}s, ability may has no slice.", SAVE_DATA_STACK);
            return true;
        }
        Object param = intentParams.getParam(SAVE_DATA_STACK);
        if (param instanceof String[]) {
            String[] strArr = (String[]) param;
            for (int i = 0; i < strArr.length; i++) {
                Log.info(LABEL, "Restore -- %{public}s", strArr[i]);
                AbilitySlice loadAbilitySlice = loadAbilitySlice(strArr[i]);
                if (loadAbilitySlice == null) {
                    Log.error(LABEL, "Restore -- %{public}s failed", strArr[i]);
                    return false;
                }
                loadAbilitySlice.init(this.abilitySliceManager.getContext(), this.abilitySliceManager);
                if (i == strArr.length - 1) {
                    this.topAbilitySlice = loadAbilitySlice;
                } else {
                    this.abilitySliceStack.push(loadAbilitySlice);
                }
            }
        }
        return false;
    }

    /* access modifiers changed from: package-private */
    public boolean onRestoreData(IntentParams intentParams) {
        synchronized (this.syncLock) {
            if (this.topAbilitySlice != null) {
                reportHiviewEvent(AbilityHiviewWrapper.EVENT_ID_ABILITY_ERROR, 1);
                Log.error(LABEL, "Top Slice already running, failed to restore data", new Object[0]);
                return false;
            } else if (restoreData(intentParams)) {
                return true;
            } else {
                if (this.topAbilitySlice == null) {
                    Log.warn(LABEL, "Top Slice is null, failed to restore data.", new Object[0]);
                    return false;
                }
                IntentParams intentParams2 = new IntentParams();
                String str = this.topAbilitySlice.getClass().getName() + TOP_SUFFIX;
                for (String str2 : intentParams.keySet()) {
                    if (str2.endsWith(str)) {
                        intentParams2.setParam(str2.substring(0, str2.length() - str.length()), intentParams.getParam(str2));
                    }
                }
                if (!this.topAbilitySlice.scheduleRestoreData(intentParams2)) {
                    Log.warn(LABEL, "%{public}s failed to restore data.", this.topAbilitySlice.getClass().getName());
                    return false;
                }
                return restoreDataInSliceStack(intentParams);
            }
        }
    }

    /* JADX INFO: finally extract failed */
    private boolean restoreDataInSliceStack(IntentParams intentParams) {
        boolean z = true;
        int i = 0;
        for (AbilitySlice abilitySlice : this.abilitySliceStack.getAllSlices()) {
            IntentParams intentParams2 = new IntentParams();
            StringBuilder sb = new StringBuilder();
            sb.append(abilitySlice.getClass().getName());
            sb.append(STACK_SUFFIX);
            int i2 = i + 1;
            sb.append(i);
            String sb2 = sb.toString();
            for (String str : intentParams.keySet()) {
                if (str.endsWith(sb2)) {
                    intentParams2.setParam(str.substring(0, str.length() - sb2.length()), intentParams.getParam(str));
                }
            }
            if (!abilitySlice.scheduleRestoreData(intentParams2)) {
                Log.warn(LABEL, "%{public}s failed to restore data.", abilitySlice.getClass().getName());
                z = false;
            }
            try {
                abilitySlice.setUiAttachedDisable(true);
                abilitySlice.start(new Intent());
                abilitySlice.active();
                abilitySlice.inactive();
                abilitySlice.background();
                abilitySlice.setUiAttachedDisable(false);
            } catch (LifecycleException unused) {
                Log.error("Switch Lifecycle failed, current state: %{public}s", abilitySlice.getState());
                abilitySlice.setUiAttachedDisable(false);
                z = false;
            } catch (Throwable th) {
                abilitySlice.setUiAttachedDisable(false);
                throw th;
            }
            if (!z) {
                return false;
            }
            i = i2;
        }
        return true;
    }

    /* access modifiers changed from: package-private */
    public void onCompleteContinuation(int i) {
        synchronized (this.syncLock) {
            if (this.topAbilitySlice != null) {
                this.topAbilitySlice.scheduleCompleteContinuation(i);
                for (AbilitySlice abilitySlice : this.abilitySliceStack.getAllSlices()) {
                    abilitySlice.scheduleCompleteContinuation(i);
                }
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void onRemoteTerminated() {
        synchronized (this.syncLock) {
            if (this.topAbilitySlice != null) {
                this.topAbilitySlice.notifyRemoteTerminated();
                for (AbilitySlice abilitySlice : this.abilitySliceStack.getAllSlices()) {
                    abilitySlice.notifyRemoteTerminated();
                }
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void stopAbilitySlice(AbilitySlice abilitySlice) {
        if (abilitySlice != null) {
            abilitySlice.stop();
            this.abilitySliceJumpRecordsSet.removeRecordByDst(abilitySlice);
        }
    }

    /* access modifiers changed from: package-private */
    public void dumpSliceStack(String str, PrintWriter printWriter, String[] strArr) {
        if (this.topAbilitySlice == null) {
            Log.info(LABEL, "even no top slice exists, ignore dump ability slice stack", new Object[0]);
            return;
        }
        printWriter.println(str + "[Ability slice name: " + this.topAbilitySlice.getClass().getName() + "]");
        AbilitySlice abilitySlice = this.topAbilitySlice;
        StringBuilder sb = new StringBuilder();
        sb.append(Ability.PREFIX);
        sb.append(str);
        abilitySlice.dumpAbilitySlice(sb.toString(), printWriter, strArr[1]);
        synchronized (this.syncLock) {
            for (AbilitySlice abilitySlice2 : this.abilitySliceStack.getAllSlices()) {
                printWriter.println(str + "[Ability slice name: " + abilitySlice2.getClass().getName() + "]");
                StringBuilder sb2 = new StringBuilder();
                sb2.append(Ability.PREFIX);
                sb2.append(str);
                abilitySlice2.dumpAbilitySlice(sb2.toString(), printWriter, "");
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void notifyOrientationChange(AbilityInfo.DisplayOrientation displayOrientation) {
        AbilitySlice abilitySlice = this.topAbilitySlice;
        if (abilitySlice == null) {
            Log.info(LABEL, "even no top slice exists, ignore orientation notifying", new Object[0]);
            return;
        }
        abilitySlice.onOrientationChanged(displayOrientation);
        synchronized (this.syncLock) {
            for (AbilitySlice abilitySlice2 : this.abilitySliceStack.getAllSlices()) {
                abilitySlice2.onOrientationChanged(displayOrientation);
            }
        }
    }

    /* access modifiers changed from: package-private */
    public boolean notifyBackKeyPressed() {
        AbilitySlice abilitySlice = this.topAbilitySlice;
        if (abilitySlice == null) {
            Log.info(LABEL, "no top slice exists, ignore notifying", new Object[0]);
            return false;
        }
        abilitySlice.onBackPressed();
        return true;
    }

    private void reportHiviewEvent(int i, int i2) {
        EventInfo eventInfo = new EventInfo();
        eventInfo.setEventId(i);
        eventInfo.setErrorType(i2);
        AbilitySliceManager abilitySliceManager2 = this.abilitySliceManager;
        if (abilitySliceManager2 == null || abilitySliceManager2.getContext() == null) {
            AbilityHiviewWrapper.sendEvent(eventInfo);
            return;
        }
        AbilityInfo abilityInfo = this.abilitySliceManager.getContext().getAbilityInfo();
        if (abilityInfo == null) {
            AbilityHiviewWrapper.sendEvent(eventInfo);
            return;
        }
        eventInfo.setBundleName(abilityInfo.getBundleName());
        eventInfo.setAbilityName(abilityInfo.getClassName());
        AbilityHiviewWrapper.sendEvent(eventInfo);
    }

    /* access modifiers changed from: package-private */
    public AbilitySlice getTopAbilitySlice() {
        return this.topAbilitySlice;
    }
}
