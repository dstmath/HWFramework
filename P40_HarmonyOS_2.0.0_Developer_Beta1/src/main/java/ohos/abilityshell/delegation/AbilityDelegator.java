package ohos.abilityshell.delegation;

import android.app.Activity;
import android.os.Handler;
import android.os.Looper;
import java.lang.Thread;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import ohos.aafwk.ability.Ability;
import ohos.aafwk.ability.AbilityLifecycleExecutor;
import ohos.aafwk.ability.AbilitySlice;
import ohos.aafwk.ability.AbilitySliceLifecycleExecutor;
import ohos.aafwk.ability.AbilitySliceManager;
import ohos.aafwk.ability.AbilitySliceScheduler;
import ohos.aafwk.ability.AbilitySliceStack;
import ohos.aafwk.ability.Lifecycle;
import ohos.aafwk.ability.LifecycleException;
import ohos.aafwk.ability.delegation.AbilityDelegatorRegistry;
import ohos.aafwk.ability.delegation.AbilityTestCase;
import ohos.aafwk.ability.delegation.IAbilityDelegator;
import ohos.aafwk.ability.delegation.IAbilityMonitor;
import ohos.aafwk.ability.delegation.TestRunner;
import ohos.aafwk.content.Intent;
import ohos.aafwk.content.Skills;
import ohos.aafwk.utils.log.KeyLog;
import ohos.abilityshell.HarmonyApplication;
import ohos.abilityshell.delegation.AbilityDelegator;
import ohos.agp.components.Component;
import ohos.app.AbilityContext;
import ohos.app.Application;
import ohos.app.Context;
import ohos.app.dispatcher.TaskDispatcher;
import ohos.appexecfwk.utils.AppLog;
import ohos.bundle.AbilityInfo;
import ohos.bundle.BundleManager;
import ohos.bundle.ElementName;
import ohos.com.sun.org.apache.xpath.internal.compiler.PsuedoNames;
import ohos.hiviewdfx.HiLogLabel;
import ohos.multimodalinput.event.KeyEvent;
import ohos.multimodalinput.event.TouchEvent;
import ohos.rpc.IRemoteObject;
import ohos.rpc.MessageOption;
import ohos.rpc.MessageParcel;
import ohos.rpc.RemoteException;
import ohos.sysability.samgr.SysAbilityManager;
import ohos.utils.PacMap;

public class AbilityDelegator implements IAbilityDelegator {
    private static final String ABILITY_TOOLS_TOKEN = "ohos.abilitytools.accessToken";
    private static final int BACK_KEY_CODE = 2;
    private static final String COMPATIABLE_UNIT_TEST = "AbilityTestCase";
    private static final int COUNT_ONE = 1;
    private static final int COUNT_TWO = 2;
    private static final String DEVICE_ID = "LocalDeviceId";
    private static final String FINISH_TAG = "All test cases run completely.";
    private static final int FLAG_ABILITY_CLEAR_TASK = 32768;
    private static final int FLAG_ABILITY_MULTIPLE_TASK = 134217728;
    private static final int FLAG_ABILITY_NEW_TASK = 268435456;
    private static final HiLogLabel LABEL = new HiLogLabel(3, 218108160, "AbilityDelegator");
    private static final Object LOCK = new Object();
    private static final long MAX_WAIT_TIME = 2000;
    private static final String SERVICE_ID = "ServiceId";
    private static final String UNIT_TEST = "unittest";
    private static volatile AbilityDelegator instance = new AbilityDelegator();
    private final List<IAbilityMonitor> abilityMonitors = new ArrayList();
    private AbilityToolsProxy abilityToolsProxy = null;
    private final Object abilityWaiterLock = new Object();
    private List<AbilityWaiter> abilityWaiters = new ArrayList();
    private AbilityDelegatorArgs arguments = new AbilityDelegatorArgs();
    private DelegatorThread delegatorThread = new DelegatorThread("DelegatorThread");

    private AbilityDelegator() {
    }

    public static AbilityDelegator getInstance() {
        return instance;
    }

    public void matchAbility(Ability ability, Intent intent) {
        synchronized (LOCK) {
            if (!this.abilityMonitors.isEmpty()) {
                for (IAbilityMonitor iAbilityMonitor : this.abilityMonitors) {
                    if (iAbilityMonitor instanceof AbilityMonitor) {
                        ((AbilityMonitor) iAbilityMonitor).match(ability, intent);
                    }
                }
            }
        }
    }

    private void startAbilityToolProxy(int i, String str) {
        AppLog.d(LABEL, "startAbilityToolProxy()", new Object[0]);
        if (this.abilityToolsProxy == null) {
            this.abilityToolsProxy = new AbilityToolsProxy(i, str);
            this.abilityToolsProxy.start();
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void stopAbilityToolProxy() {
        AppLog.d(LABEL, "stopAbilityToolProxy()", new Object[0]);
        AbilityToolsProxy abilityToolsProxy2 = this.abilityToolsProxy;
        if (abilityToolsProxy2 != null) {
            abilityToolsProxy2.stop();
        }
    }

    @Override // ohos.aafwk.ability.delegation.IAbilityDelegator
    public void print(String str) {
        AbilityToolsProxy abilityToolsProxy2 = this.abilityToolsProxy;
        if (abilityToolsProxy2 != null) {
            abilityToolsProxy2.output(str);
        } else {
            AppLog.e(LABEL, "AbilityToolProxy is null", new Object[0]);
        }
    }

    @Override // ohos.aafwk.ability.delegation.IAbilityDelegator
    public void println(String str) {
        if (str != null) {
            String str2 = str + System.lineSeparator();
            if (str2.length() <= 1000) {
                print(str2);
                return;
            }
            throw new IllegalArgumentException("Message length limit is " + (1000 - System.lineSeparator().length()));
        }
        throw new IllegalArgumentException("Send message should not be null");
    }

    @Override // ohos.aafwk.ability.delegation.IAbilityDelegator
    public String getThreadName() {
        return this.delegatorThread.getName();
    }

    @Override // ohos.aafwk.ability.delegation.IAbilityDelegator
    public boolean isAbilityHasSlice(Ability ability) {
        if (ability != null) {
            checkAbilityState(ability);
            return getCurrentAbilitySlice(ability) != null;
        }
        throw new IllegalArgumentException("ability is null.");
    }

    private void checkAbilityState(Ability ability) {
        AbilityLifecycleExecutor.LifecycleState state = ability.getState();
        if (state == AbilityLifecycleExecutor.LifecycleState.UNINITIALIZED || state == AbilityLifecycleExecutor.LifecycleState.INITIAL) {
            throw new IllegalStateException("Ability cannot be in the UNINITIALIZED or INITIAL state.");
        }
    }

    @Override // ohos.aafwk.ability.delegation.IAbilityDelegator
    public boolean isSliceStackEmpty(Ability ability) {
        if (ability != null) {
            checkAbilityState(ability);
            AbilitySliceStack reflectAbilitySliceStack = AbilityHelper.reflectAbilitySliceStack(ability);
            if (reflectAbilitySliceStack == null) {
                return false;
            }
            return AbilityHelper.reflectAbilitySliceStackIsEmpty(reflectAbilitySliceStack);
        }
        throw new IllegalArgumentException("ability is null.");
    }

    @Override // ohos.aafwk.ability.delegation.IAbilityDelegator
    public int getSliceStackSize(Ability ability) {
        if (ability != null) {
            checkAbilityState(ability);
            AbilitySliceStack reflectAbilitySliceStack = AbilityHelper.reflectAbilitySliceStack(ability);
            if (reflectAbilitySliceStack == null) {
                return 0;
            }
            return AbilityHelper.reflectAbilitySliceStackSize(reflectAbilitySliceStack);
        }
        throw new IllegalArgumentException("ability is null.");
    }

    @Override // ohos.aafwk.ability.delegation.IAbilityDelegator
    public void invokeAbilityOnStart(Ability ability) {
        if (ability != null) {
            AbilityHelper.reflectInvokeAbilityMethod(ability, "onStart", Intent.class, ability.getIntent());
            return;
        }
        throw new IllegalArgumentException("ability is null.");
    }

    @Override // ohos.aafwk.ability.delegation.IAbilityDelegator
    public void invokeAbilityOnPostStart(Ability ability) {
        if (ability != null) {
            AbilityHelper.reflectInvokeAbilityMethod(ability, "onPostStart", PacMap.class, new PacMap());
            return;
        }
        throw new IllegalArgumentException("ability is null.");
    }

    @Override // ohos.aafwk.ability.delegation.IAbilityDelegator
    public void invokeAbilityOnInactive(Ability ability) {
        if (ability != null) {
            AbilityHelper.reflectInvokeAbilityMethod(ability, "onInactive");
            return;
        }
        throw new IllegalArgumentException("ability is null.");
    }

    @Override // ohos.aafwk.ability.delegation.IAbilityDelegator
    public void invokeAbilityOnActive(Ability ability) {
        if (ability != null) {
            AbilityHelper.reflectInvokeAbilityMethod(ability, "onActive");
            return;
        }
        throw new IllegalArgumentException("ability is null.");
    }

    @Override // ohos.aafwk.ability.delegation.IAbilityDelegator
    public void invokeAbilityOnPostActive(Ability ability) {
        if (ability != null) {
            AbilityHelper.reflectInvokeAbilityMethod(ability, "onPostActive");
            return;
        }
        throw new IllegalArgumentException("ability is null.");
    }

    @Override // ohos.aafwk.ability.delegation.IAbilityDelegator
    public void invokeAbilityOnBackground(Ability ability) {
        if (ability != null) {
            AbilityHelper.reflectInvokeAbilityMethod(ability, "onBackground");
            return;
        }
        throw new IllegalArgumentException("ability is null.");
    }

    @Override // ohos.aafwk.ability.delegation.IAbilityDelegator
    public void invokeAbilityOnForeground(Ability ability) {
        if (ability != null) {
            AbilityHelper.reflectInvokeAbilityMethod(ability, "onForeground", Intent.class, ability.getIntent());
            return;
        }
        throw new IllegalArgumentException("ability is null.");
    }

    @Override // ohos.aafwk.ability.delegation.IAbilityDelegator
    public void invokeAbilityOnStop(Ability ability) {
        if (ability != null) {
            AbilityHelper.reflectInvokeAbilityMethod(ability, "onStop");
            return;
        }
        throw new IllegalArgumentException("ability is null.");
    }

    @Override // ohos.aafwk.ability.delegation.IAbilityDelegator
    public Ability getCurrentTopAbility() {
        FutureTask futureTask = new FutureTask($$Lambda$AbilityDelegator$ASxK1JHnJqBblNqz5yfXS_KLkI.INSTANCE);
        if (runOnUIThreadSync(futureTask)) {
            try {
                return (Ability) futureTask.get();
            } catch (InterruptedException | ExecutionException e) {
                AppLog.e(LABEL, "triggerTouchEvent failed: %{public}s", e.getMessage());
            }
        }
        return null;
    }

    @Override // ohos.aafwk.ability.delegation.IAbilityDelegator
    public IAbilityMonitor addAbilityMonitor(String str) {
        if (str == null || str.isEmpty()) {
            throw new IllegalArgumentException("the abilityName is null or empty.");
        }
        AbilityMonitor abilityMonitor = new AbilityMonitor(str);
        synchronized (LOCK) {
            this.abilityMonitors.add(abilityMonitor);
        }
        return abilityMonitor;
    }

    @Override // ohos.aafwk.ability.delegation.IAbilityDelegator
    public IAbilityMonitor addAbilityMonitor(Skills skills) {
        if (skills != null) {
            AbilityMonitor abilityMonitor = new AbilityMonitor(skills);
            synchronized (LOCK) {
                this.abilityMonitors.add(abilityMonitor);
            }
            return abilityMonitor;
        }
        throw new IllegalArgumentException("the skills is null.");
    }

    @Override // ohos.aafwk.ability.delegation.IAbilityDelegator
    public Optional<Ability> waitAbilityMonitor(IAbilityMonitor iAbilityMonitor) {
        if (iAbilityMonitor == null) {
            return Optional.empty();
        }
        Ability waitForAbility = iAbilityMonitor.waitForAbility(MAX_WAIT_TIME);
        removeAbilityMonitor(iAbilityMonitor);
        return Optional.ofNullable(waitForAbility);
    }

    @Override // ohos.aafwk.ability.delegation.IAbilityDelegator
    public Optional<Ability> waitAbilityMonitor(IAbilityMonitor iAbilityMonitor, long j) {
        if (iAbilityMonitor == null) {
            return Optional.empty();
        }
        Ability waitForAbility = iAbilityMonitor.waitForAbility(j);
        removeAbilityMonitor(iAbilityMonitor);
        return Optional.ofNullable(waitForAbility);
    }

    @Override // ohos.aafwk.ability.delegation.IAbilityDelegator
    public Optional<Ability> startAbilitySync(Intent intent) {
        if (intent != null) {
            return startAbilitySync(intent, MAX_WAIT_TIME);
        }
        AppLog.e(LABEL, "intent is null", new Object[0]);
        return Optional.empty();
    }

    @Override // ohos.aafwk.ability.delegation.IAbilityDelegator
    public boolean runOnUIThreadSync(Runnable runnable) {
        if (runnable == null) {
            AppLog.e(LABEL, "task is null", new Object[0]);
            return false;
        } else if (getAppContext() == null) {
            AppLog.e(LABEL, "context is null, dispatcher task failed!", new Object[0]);
            return false;
        } else {
            TaskDispatcher uITaskDispatcher = getAppContext().getUITaskDispatcher();
            if (uITaskDispatcher == null) {
                AppLog.e(LABEL, "getUITaskDispatcher failed, dispatcher task failed!", new Object[0]);
                return false;
            }
            uITaskDispatcher.syncDispatch(runnable);
            return true;
        }
    }

    public void matchAbilityWaiter(Ability ability, Intent intent) {
        ArrayList<AbilityWaiter> arrayList;
        if (ability == null) {
            AppLog.e(LABEL, "active is null", new Object[0]);
        } else if (intent == null) {
            AppLog.e(LABEL, "intent is null", new Object[0]);
        } else {
            synchronized (this.abilityWaiterLock) {
                if (!this.abilityWaiters.isEmpty()) {
                    arrayList = new ArrayList();
                    Iterator<AbilityWaiter> it = this.abilityWaiters.iterator();
                    while (it.hasNext()) {
                        AbilityWaiter next = it.next();
                        HiLogLabel hiLogLabel = LABEL;
                        AppLog.d(hiLogLabel, "compare %{public}s with %{public}s", intent.getElement().getBundleName() + PsuedoNames.PSEUDONAME_ROOT + intent.getElement().getAbilityName(), next.intent.getElement().getBundleName() + PsuedoNames.PSEUDONAME_ROOT + next.intent.getElement().getAbilityName());
                        if (intent.equals(next.intent)) {
                            AppLog.d(LABEL, "get ability", new Object[0]);
                            next.ability = ability;
                            arrayList.add(next);
                            it.remove();
                        }
                    }
                } else {
                    return;
                }
            }
            for (AbilityWaiter abilityWaiter : arrayList) {
                abilityWaiter.countDownLatch.countDown();
            }
        }
    }

    private Optional<AbilityInfo> queryAbilityInfo(Intent intent) {
        BundleManager instance2 = BundleManager.getInstance();
        if (instance2 == null) {
            AppLog.e(LABEL, "service is not ready", new Object[0]);
            return Optional.empty();
        }
        try {
            List<AbilityInfo> queryAbilityByIntent = instance2.queryAbilityByIntent(intent);
            if (queryAbilityByIntent != null && !queryAbilityByIntent.isEmpty()) {
                return Optional.of(queryAbilityByIntent.get(0));
            }
            AppLog.e(LABEL, "no such ability", new Object[0]);
            return Optional.empty();
        } catch (RemoteException unused) {
            AppLog.e(LABEL, "Query ability info failed", new Object[0]);
            return Optional.empty();
        }
    }

    private Intent formatIntent(Intent intent, AbilityInfo abilityInfo) {
        Intent intent2 = new Intent(intent);
        ElementName elementName = new ElementName();
        elementName.setBundleName(abilityInfo.getBundleName());
        elementName.setAbilityName(abilityInfo.getClassName());
        intent2.setElement(elementName);
        intent2.addFlags(268468224);
        return intent2;
    }

    @Override // ohos.aafwk.ability.delegation.IAbilityDelegator
    public Optional<Ability> startAbilitySync(Intent intent, long j) {
        AbilityWaiter abilityWaiter;
        if (getAppContext() == null) {
            AppLog.e(LABEL, "app context is null", new Object[0]);
            return Optional.empty();
        } else if (intent == null) {
            AppLog.e(LABEL, "intent is null", new Object[0]);
            return Optional.empty();
        } else {
            synchronized (this.abilityWaiterLock) {
                AppLog.d(LABEL, "startAbilitySync()", new Object[0]);
                Optional<AbilityInfo> queryAbilityInfo = queryAbilityInfo(intent);
                if (!queryAbilityInfo.isPresent()) {
                    AppLog.e(LABEL, "query ability info failed", new Object[0]);
                    return Optional.empty();
                }
                Intent formatIntent = formatIntent(intent, queryAbilityInfo.get());
                abilityWaiter = new AbilityWaiter(formatIntent);
                this.abilityWaiters.add(abilityWaiter);
                AppLog.d(LABEL, KeyLog.START_ABILITY, new Object[0]);
                getAppContext().startAbility(formatIntent, 0);
            }
            try {
                if (!abilityWaiter.countDownLatch.await(j, TimeUnit.MILLISECONDS)) {
                    AppLog.w(LABEL, "count down failed", new Object[0]);
                }
                Optional<Ability> ofNullable = Optional.ofNullable(abilityWaiter.ability);
                synchronized (this.abilityWaiterLock) {
                    this.abilityWaiters.remove(abilityWaiter);
                }
                return ofNullable;
            } catch (InterruptedException unused) {
                try {
                    Optional<Ability> empty = Optional.empty();
                    synchronized (this.abilityWaiterLock) {
                        this.abilityWaiters.remove(abilityWaiter);
                        return empty;
                    }
                } catch (Throwable th) {
                    synchronized (this.abilityWaiterLock) {
                        this.abilityWaiters.remove(abilityWaiter);
                        throw th;
                    }
                }
            }
        }
    }

    @Override // ohos.aafwk.ability.delegation.IAbilityDelegator
    public boolean doAbilityActive(Ability ability, Intent intent) {
        if (ability == null) {
            AppLog.e(LABEL, "active is null", new Object[0]);
            return false;
        }
        try {
            AbilityHelper.reflectAbilityLifecycle(ability, intent, AbilityHelper.LifecycleAction.ACTIVE);
            return true;
        } catch (AbilityDelegatorException e) {
            AppLog.e(LABEL, "doAbilityActive failed %{public}s", e.getMessage());
            return false;
        }
    }

    @Override // ohos.aafwk.ability.delegation.IAbilityDelegator
    public boolean doAbilityInactive(Ability ability) {
        if (ability == null) {
            AppLog.e(LABEL, "active is null", new Object[0]);
            return false;
        }
        try {
            AbilityHelper.reflectAbilityLifecycle(ability, new Intent(), AbilityHelper.LifecycleAction.INACTIVE);
            return true;
        } catch (AbilityDelegatorException e) {
            AppLog.e(LABEL, "doAbilityInactive failed %{public}s", e.getMessage());
            return false;
        }
    }

    @Override // ohos.aafwk.ability.delegation.IAbilityDelegator
    public boolean doAbilityForeground(Ability ability, Intent intent) {
        if (ability == null) {
            AppLog.e(LABEL, "active is null", new Object[0]);
            return false;
        }
        try {
            AbilityHelper.reflectAbilityLifecycle(ability, intent, AbilityHelper.LifecycleAction.FOREGROUND);
            return true;
        } catch (AbilityDelegatorException e) {
            AppLog.e(LABEL, "doAbilityForeground failed %{public}s", e.getMessage());
            return false;
        }
    }

    @Override // ohos.aafwk.ability.delegation.IAbilityDelegator
    public boolean doAbilityBackground(Ability ability) {
        if (ability == null) {
            AppLog.e(LABEL, "active is null", new Object[0]);
            return false;
        }
        try {
            AbilityHelper.reflectAbilityLifecycle(ability, new Intent(), AbilityHelper.LifecycleAction.BACKGROUND);
            return true;
        } catch (AbilityDelegatorException e) {
            AppLog.e(LABEL, "doAbilityBackground failed %{public}s", e.getMessage());
            return false;
        }
    }

    private Optional<Object> getAbilityShell(Ability ability) {
        if (ability == null) {
            AppLog.e(LABEL, "ability is invalid", new Object[0]);
            return Optional.empty();
        }
        try {
            try {
                Object invoke = AbilityContext.class.getDeclaredMethod("getAbilityShell", new Class[0]).invoke(ability, new Object[0]);
                if (invoke instanceof Activity) {
                    return Optional.of(invoke);
                }
                AppLog.e(LABEL, "getAbilityShell failed", new Object[0]);
                return Optional.empty();
            } catch (IllegalAccessException | InvocationTargetException unused) {
                AppLog.e(LABEL, "getAbilityShell failed for unknown action", new Object[0]);
                return Optional.empty();
            }
        } catch (NoSuchMethodException unused2) {
            AppLog.e(LABEL, "getAbilityShell failed", new Object[0]);
            return Optional.empty();
        }
    }

    private boolean isInMainLoop() {
        return Looper.getMainLooper() == Looper.myLooper();
    }

    private Optional<Method> getNotHMethod(String str) {
        try {
            Method declaredMethod = Activity.class.getDeclaredMethod(str, new Class[0]);
            declaredMethod.setAccessible(true);
            return Optional.of(declaredMethod);
        } catch (NoSuchMethodException unused) {
            AppLog.e(LABEL, "unknown action!", new Object[0]);
            return Optional.empty();
        }
    }

    private <T> boolean invokeNotHMethod(T t, Method method) {
        try {
            method.invoke(t, new Object[0]);
            return true;
        } catch (IllegalAccessException unused) {
            AppLog.e(LABEL, "illegal action!", new Object[0]);
            return false;
        } catch (InvocationTargetException unused2) {
            AppLog.e(LABEL, "action execute failed!", new Object[0]);
            return false;
        }
    }

    @Override // ohos.aafwk.ability.delegation.IAbilityDelegator
    public boolean stopAbility(Ability ability) {
        if (ability == null) {
            AppLog.e(LABEL, "ability is null", new Object[0]);
            return false;
        }
        Optional<Object> abilityShell = getAbilityShell(ability);
        if (!abilityShell.isPresent()) {
            AppLog.e(LABEL, "stopAbility failed for invalid ability", new Object[0]);
            return false;
        }
        Optional<Method> notHMethod = getNotHMethod("finish");
        if (!notHMethod.isPresent()) {
            AppLog.e(LABEL, "stopAbility failed for unknown operation", new Object[0]);
            return false;
        } else if (isInMainLoop()) {
            AppLog.d(LABEL, "stopAbility in current thread", new Object[0]);
            return invokeNotHMethod(abilityShell.get(), notHMethod.get());
        } else {
            FutureTask futureTask = new FutureTask(new Callable(abilityShell, notHMethod) {
                /* class ohos.abilityshell.delegation.$$Lambda$AbilityDelegator$ZdTZogw6n7fPORX0k1MpdVuC1Q */
                private final /* synthetic */ Optional f$1;
                private final /* synthetic */ Optional f$2;

                {
                    this.f$1 = r2;
                    this.f$2 = r3;
                }

                @Override // java.util.concurrent.Callable
                public final Object call() {
                    return AbilityDelegator.this.lambda$stopAbility$1$AbilityDelegator(this.f$1, this.f$2);
                }
            });
            new Handler(Looper.getMainLooper()).post(futureTask);
            try {
                return ((Boolean) futureTask.get(MAX_WAIT_TIME, TimeUnit.MILLISECONDS)).booleanValue();
            } catch (InterruptedException | ExecutionException | TimeoutException e) {
                AppLog.e(LABEL, "stopAbility failed for %{private}s", e);
                return false;
            }
        }
    }

    public /* synthetic */ Boolean lambda$stopAbility$1$AbilityDelegator(Optional optional, Optional optional2) throws Exception {
        return Boolean.valueOf(invokeNotHMethod(optional.get(), (Method) optional2.get()));
    }

    @Override // ohos.aafwk.ability.delegation.IAbilityDelegator
    public boolean triggerTouchEvent(Ability ability, TouchEvent touchEvent) {
        AppLog.d(LABEL, "triggerTouchEvent()", new Object[0]);
        if (ability == null) {
            AppLog.e(LABEL, "active is null", new Object[0]);
            return false;
        } else if (touchEvent == null) {
            AppLog.e(LABEL, "touchEvent is null", new Object[0]);
            return false;
        } else {
            FutureTask futureTask = new FutureTask(new Callable(touchEvent) {
                /* class ohos.abilityshell.delegation.$$Lambda$AbilityDelegator$spipEm0WsnUwfIlYTWaLKljLM */
                private final /* synthetic */ TouchEvent f$1;

                {
                    this.f$1 = r2;
                }

                @Override // java.util.concurrent.Callable
                public final Object call() {
                    return Boolean.valueOf(AbilityDelegator.AbilityHelper.reflectDispatchTouchEvent(Ability.this, this.f$1));
                }
            });
            if (runOnUIThreadSync(futureTask)) {
                try {
                    return ((Boolean) futureTask.get()).booleanValue();
                } catch (InterruptedException | ExecutionException e) {
                    AppLog.e(LABEL, "triggerTouchEvent failed: %{public}s", e.getMessage());
                }
            }
            return false;
        }
    }

    @Override // ohos.aafwk.ability.delegation.IAbilityDelegator
    public boolean triggerClickEvent(Ability ability, Component component) {
        AppLog.d(LABEL, "triggerClickEvent()", new Object[0]);
        if (ability == null) {
            AppLog.e(LABEL, "Ability is NULL, triggerClickEvent failed.", new Object[0]);
            return false;
        } else if (component == null) {
            AppLog.e(LABEL, "component is null", new Object[0]);
            return false;
        } else {
            FutureTask futureTask = new FutureTask(new Callable() {
                /* class ohos.abilityshell.delegation.$$Lambda$0Dq5vrZyPfIl_89b4ReE9yNcOz8 */

                @Override // java.util.concurrent.Callable
                public final Object call() {
                    return Boolean.valueOf(Component.this.simulateClick());
                }
            });
            if (runOnUIThreadSync(futureTask)) {
                try {
                    return ((Boolean) futureTask.get()).booleanValue();
                } catch (InterruptedException | ExecutionException e) {
                    AppLog.e(LABEL, "triggerClickEvent failed: %{public}s", e.getMessage());
                }
            }
            return false;
        }
    }

    @Override // ohos.aafwk.ability.delegation.IAbilityDelegator
    public boolean triggerKeyEvent(Ability ability, KeyEvent keyEvent) {
        AppLog.d(LABEL, "triggerKeyEvent()", new Object[0]);
        if (ability == null) {
            AppLog.e(LABEL, "Ability is NULL, triggerKeyEvent failed.", new Object[0]);
            return false;
        } else if (keyEvent == null) {
            AppLog.e(LABEL, "keyevent is null", new Object[0]);
            return false;
        } else {
            FutureTask futureTask = new FutureTask(new Callable(keyEvent, ability) {
                /* class ohos.abilityshell.delegation.$$Lambda$AbilityDelegator$p5ShXX4VpLYrDzswTj0CCiRiHI */
                private final /* synthetic */ KeyEvent f$0;
                private final /* synthetic */ Ability f$1;

                {
                    this.f$0 = r1;
                    this.f$1 = r2;
                }

                @Override // java.util.concurrent.Callable
                public final Object call() {
                    return AbilityDelegator.lambda$triggerKeyEvent$3(this.f$0, this.f$1);
                }
            });
            if (runOnUIThreadSync(futureTask)) {
                try {
                    return ((Boolean) futureTask.get()).booleanValue();
                } catch (InterruptedException | ExecutionException e) {
                    AppLog.e(LABEL, "triggerKeyEvent failed: %{public}s", e.getMessage());
                }
            }
            return false;
        }
    }

    static /* synthetic */ Boolean lambda$triggerKeyEvent$3(KeyEvent keyEvent, Ability ability) throws Exception {
        if (keyEvent.getKeyCode() == 2) {
            try {
                AbilityHelper.reflectDispatchBackKey(ability);
            } catch (AbilityDelegatorException e) {
                AppLog.e(LABEL, "triggerKeyEvent failed: %{public}s", e.getMessage());
                return false;
            }
        }
        try {
            return Boolean.valueOf(AbilityHelper.reflectDispatchKeyEvent(ability, keyEvent));
        } catch (AbilityDelegatorException e2) {
            AppLog.e(LABEL, "triggerKeyEvent failed: %{public}s", e2.getMessage());
            return false;
        }
    }

    @Override // ohos.aafwk.ability.delegation.IAbilityDelegator
    public int getMonitorsNum() {
        int size;
        synchronized (LOCK) {
            size = this.abilityMonitors.size();
        }
        return size;
    }

    @Override // ohos.aafwk.ability.delegation.IAbilityDelegator
    public void clearAllMonitors() {
        synchronized (LOCK) {
            AppLog.d(LABEL, "clearAllMonitors()", new Object[0]);
            this.abilityMonitors.clear();
        }
    }

    @Override // ohos.aafwk.ability.delegation.IAbilityDelegator
    public int getAbilityState(Ability ability) {
        if (ability != null) {
            return AbilityHelper.reflectGetAbilityState(ability);
        }
        throw new IllegalArgumentException("the ability is null.");
    }

    @Override // ohos.aafwk.ability.delegation.IAbilityDelegator
    public void removeAbilityMonitor(IAbilityMonitor iAbilityMonitor) {
        synchronized (LOCK) {
            AppLog.d(LABEL, "removeAbilityMonitor()", new Object[0]);
            this.abilityMonitors.remove(iAbilityMonitor);
        }
    }

    @Override // ohos.aafwk.ability.delegation.IAbilityDelegator
    public boolean doAbilitySliceActive(Ability ability) {
        if (ability == null) {
            AppLog.e(LABEL, "ability is null", new Object[0]);
            return false;
        }
        checkAbilityState(ability);
        try {
            AbilityHelper.reflectAbilitySliceLifecycle(ability, new Intent(), AbilityHelper.LifecycleAction.ACTIVE);
            return true;
        } catch (AbilityDelegatorException e) {
            AppLog.e(LABEL, "activeAbility failed %{public}s", e.getMessage());
            return false;
        }
    }

    @Override // ohos.aafwk.ability.delegation.IAbilityDelegator
    public boolean doAbilitySliceBackground(Ability ability) {
        if (ability == null) {
            AppLog.e(LABEL, "ability is null", new Object[0]);
            return false;
        }
        checkAbilityState(ability);
        try {
            AbilityHelper.reflectAbilitySliceLifecycle(ability, new Intent(), AbilityHelper.LifecycleAction.BACKGROUND);
            return true;
        } catch (AbilityDelegatorException e) {
            AppLog.e(LABEL, "backgroundAbility failed %{public}s", e.getMessage());
            return false;
        }
    }

    @Override // ohos.aafwk.ability.delegation.IAbilityDelegator
    public boolean doAbilitySliceForeground(Ability ability, Intent intent) {
        if (ability == null) {
            AppLog.e(LABEL, "ability is null", new Object[0]);
            return false;
        }
        checkAbilityState(ability);
        try {
            AbilityHelper.reflectAbilitySliceLifecycle(ability, intent, AbilityHelper.LifecycleAction.FOREGROUND);
            return true;
        } catch (AbilityDelegatorException e) {
            AppLog.e(LABEL, "foregroundAbility failed %{public}s", e.getMessage());
            return false;
        }
    }

    @Override // ohos.aafwk.ability.delegation.IAbilityDelegator
    public boolean doAbilitySliceStop(Ability ability) {
        if (ability == null) {
            AppLog.e(LABEL, "ability is null", new Object[0]);
            return false;
        } else if (ability.getLifecycle().getLifecycleState() != Lifecycle.Event.ON_ACTIVE) {
            AppLog.e(LABEL, "ability state is not active", new Object[0]);
            return false;
        } else if (getCurrentAbilitySlice(ability) == null) {
            AppLog.e(LABEL, "currentAbilitySlice is null", new Object[0]);
            return false;
        } else {
            AbilitySliceStack reflectAbilitySliceStack = AbilityHelper.reflectAbilitySliceStack(ability);
            if (reflectAbilitySliceStack == null) {
                AppLog.e(LABEL, "abilitySliceStack is null", new Object[0]);
                return false;
            } else if (AbilityHelper.reflectAbilitySliceStackIsEmpty(reflectAbilitySliceStack)) {
                return stopAbilitySliceByAbility(ability);
            } else {
                return terminateAbilitySlice(ability);
            }
        }
    }

    private boolean terminateAbilitySlice(Ability ability) {
        AbilitySliceManager reflectAbilitySliceManager = AbilityHelper.reflectAbilitySliceManager(ability);
        if (reflectAbilitySliceManager == null) {
            AppLog.e(LABEL, "abilitySliceManager is null", new Object[0]);
            return false;
        }
        AbilitySlice currentAbilitySlice = getCurrentAbilitySlice(ability);
        if (currentAbilitySlice == null) {
            AppLog.e(LABEL, "currentAbilitySlice is null", new Object[0]);
            return false;
        }
        reflectAbilitySliceManager.terminateSync(currentAbilitySlice, new Intent());
        return true;
    }

    private boolean stopAbilitySliceByAbility(Ability ability) {
        if (!doAbilityInactive(ability)) {
            AppLog.e(LABEL, "doAbilityInactive failed", new Object[0]);
            return false;
        } else if (doAbilityBackground(ability)) {
            return stopAbility(ability);
        } else {
            AppLog.e(LABEL, "doAbilityBackground failed", new Object[0]);
            return false;
        }
    }

    @Override // ohos.aafwk.ability.delegation.IAbilityDelegator
    public boolean doAbilitySliceStart(Ability ability, Intent intent) {
        if (ability == null) {
            AppLog.e(LABEL, "ability is null", new Object[0]);
            return false;
        }
        checkAbilityState(ability);
        try {
            AbilityHelper.reflectAbilitySliceLifecycle(ability, intent, AbilityHelper.LifecycleAction.START);
            return true;
        } catch (AbilityDelegatorException e) {
            AppLog.e(LABEL, "startAbility failed %{public}s", e.getMessage());
            return false;
        }
    }

    @Override // ohos.aafwk.ability.delegation.IAbilityDelegator
    public boolean doAbilitySliceStart(Ability ability, AbilitySlice abilitySlice) {
        if (ability == null || abilitySlice == null) {
            AppLog.e(LABEL, "ability or targetSlice is null", new Object[0]);
            return false;
        } else if (ability.getLifecycle().getLifecycleState() != Lifecycle.Event.ON_ACTIVE) {
            AppLog.e(LABEL, "ability state is not active", new Object[0]);
            return false;
        } else {
            AbilitySlice currentAbilitySlice = getCurrentAbilitySlice(ability);
            if (currentAbilitySlice == null) {
                AppLog.e(LABEL, "currentAbilitySlice is null", new Object[0]);
                return false;
            }
            AbilitySliceManager reflectAbilitySliceManager = AbilityHelper.reflectAbilitySliceManager(ability);
            if (reflectAbilitySliceManager == null) {
                AppLog.e(LABEL, "abilitySliceManager is null", new Object[0]);
                return false;
            }
            reflectAbilitySliceManager.presentSync(currentAbilitySlice, abilitySlice, new Intent());
            return true;
        }
    }

    @Override // ohos.aafwk.ability.delegation.IAbilityDelegator
    public boolean doAbilitySliceInactive(Ability ability) {
        if (ability == null) {
            AppLog.e(LABEL, "ability is null", new Object[0]);
            return false;
        }
        checkAbilityState(ability);
        try {
            AbilityHelper.reflectAbilitySliceLifecycle(ability, new Intent(), AbilityHelper.LifecycleAction.INACTIVE);
            return true;
        } catch (AbilityDelegatorException e) {
            AppLog.e(LABEL, "doAbilitySliceInactive failed %{public}s", e.getMessage());
            return false;
        }
    }

    @Override // ohos.aafwk.ability.delegation.IAbilityDelegator
    public int getAbilitySliceState(AbilitySlice abilitySlice) {
        if (abilitySlice != null) {
            return AbilityHelper.reflectGetAbilitySliceState(abilitySlice);
        }
        throw new IllegalArgumentException("targetSlice is null.");
    }

    @Override // ohos.aafwk.ability.delegation.IAbilityDelegator
    public List<AbilitySlice> getAllAbilitySlice(Ability ability) {
        if (ability != null) {
            checkAbilityState(ability);
            return AbilityHelper.reflectGetAllAbilitySlice(ability);
        }
        throw new IllegalArgumentException("ability is null.");
    }

    @Override // ohos.aafwk.ability.delegation.IAbilityDelegator
    public AbilitySlice getCurrentAbilitySlice(Ability ability) {
        if (ability != null) {
            checkAbilityState(ability);
            return AbilityHelper.reflectGetCurrentAbilitySlice(ability);
        }
        throw new IllegalArgumentException("ability is null.");
    }

    public void setClassLoader(ClassLoader classLoader) {
        AppLog.d(LABEL, "setClassLoader()", new Object[0]);
        this.arguments.setClassLoader(classLoader);
    }

    private void startAbilityToolProxy(Intent intent) {
        String stringParam;
        AppLog.d(LABEL, "AbilityDelegator::startAbilityToolProxy", new Object[0]);
        int intParam = intent.getIntParam(SERVICE_ID, 0);
        if (intParam == 0 && (stringParam = intent.getStringParam(SERVICE_ID)) != null) {
            try {
                intParam = Integer.parseInt(stringParam);
            } catch (NumberFormatException unused) {
                AppLog.e(LABEL, "AbilityDelegator::startAbilityToolProxy failed", new Object[0]);
                throw new IllegalArgumentException("get serviceid failed");
            }
        }
        AppLog.d(LABEL, "unittest ServiceId is: %{public}d", Integer.valueOf(intParam));
        String stringParam2 = intent.getStringParam(DEVICE_ID);
        AppLog.d(LABEL, "unittest DeviceId is: %{private}s", stringParam2);
        startAbilityToolProxy(intParam, stringParam2);
    }

    private String getUnittestParams(Intent intent) {
        String stringParam = intent.getStringParam(UNIT_TEST);
        if (stringParam != null && !stringParam.isEmpty()) {
            return stringParam;
        }
        String stringParam2 = intent.getStringParam("AbilityTestCase");
        if (stringParam2 != null && !stringParam2.isEmpty()) {
            return stringParam2;
        }
        AppLog.d(LABEL, "unittest parameter is empty, skip unittest", new Object[0]);
        return "";
    }

    private String filterTestCases(String str, ClassLoader classLoader) {
        if (str == null) {
            return "";
        }
        ArrayList arrayList = new ArrayList();
        String[] split = str.split(",");
        for (String str2 : split) {
            try {
                if (!AbilityTestCase.class.isAssignableFrom(classLoader.loadClass(str2))) {
                    arrayList.add(str2);
                    AppLog.d(LABEL, "found unittest case %{public}s", str2);
                } else {
                    AppLog.w(LABEL, "AbilityDelegator unittest not support cases inherited from AbilityTestCase", new Object[0]);
                }
            } catch (ClassNotFoundException unused) {
                AppLog.e(LABEL, "undefined class %{public}s", str2);
            }
        }
        return String.join(",", arrayList);
    }

    public final boolean isRunning() {
        return this.delegatorThread.isAlive();
    }

    private boolean isCustomTestRunner(String str, ClassLoader classLoader) {
        if (str == null || classLoader == null || str.equals(JunitTestRunner.class.getName())) {
            return false;
        }
        try {
            if (TestRunner.class.isAssignableFrom(classLoader.loadClass(str))) {
                return true;
            }
            return false;
        } catch (ClassNotFoundException unused) {
            return false;
        }
    }

    private boolean checkUnittestEnv(Intent intent) {
        if (intent == null) {
            return false;
        }
        if (getAppContext() == null) {
            AppLog.d(LABEL, "app context is null", new Object[0]);
            return false;
        } else if (this.arguments.getTestClassLoader() == null) {
            AppLog.d(LABEL, "unittest classloader is empty, skip unittest", new Object[0]);
            return false;
        } else if (!this.delegatorThread.isAlive()) {
            return true;
        } else {
            AppLog.d(LABEL, "Delegator thread is running, skip", new Object[0]);
            return false;
        }
    }

    private boolean parseCasesAndRunner(Intent intent) {
        String str;
        String unittestParams = getUnittestParams(intent);
        if (unittestParams.isEmpty()) {
            AppLog.d(LABEL, "unittest parameter is empty, skip unittest", new Object[0]);
            return false;
        }
        AppLog.d(LABEL, "unittest parameter is %{public}s", unittestParams);
        String[] split = unittestParams.split(PsuedoNames.PSEUDONAME_ROOT);
        String name = JunitTestRunner.class.getName();
        if (split.length == 1) {
            if (isCustomTestRunner(split[0], this.arguments.getTestClassLoader())) {
                name = split[0];
                str = "";
            } else {
                str = filterTestCases(split[0], this.arguments.getTestClassLoader());
            }
        } else if (split.length == 2) {
            name = split[0];
            str = filterTestCases(split[1], this.arguments.getTestClassLoader());
        } else {
            AppLog.e(LABEL, "unittest parameter is invalid, there is no unittest found", new Object[0]);
            return false;
        }
        AppLog.d(LABEL, "unittest runner is %{private}s, unittest cases is %{public}s", name, str);
        if (!str.isEmpty() || !name.equals(JunitTestRunner.class.getName())) {
            this.arguments.setTestRunnerClassName(name);
            this.arguments.setTestCaseNames(str);
            return true;
        }
        AppLog.w(LABEL, "unittest parameter is invalid, there is no unittest found", new Object[0]);
        return false;
    }

    public final void runUnittest(Intent intent) {
        if (checkUnittestEnv(intent) && parseCasesAndRunner(intent) && getAppContext() != null) {
            getInstance().startAbilityToolProxy(intent);
            this.arguments.setTestBundleName(getAppContext().getBundleName());
            this.arguments.setTestParameters(intent.getParams().getParams());
            this.arguments.removeTestParameter(SERVICE_ID);
            AbilityDelegatorRegistry.registerInstance(this, this.arguments);
            this.delegatorThread.start();
        }
    }

    @Override // ohos.aafwk.ability.delegation.IAbilityDelegator
    public Context getAppContext() {
        Application application;
        HarmonyApplication instance2 = HarmonyApplication.getInstance();
        if (instance2 == null || (application = instance2.getApplication()) == null) {
            return null;
        }
        return application.getContext();
    }

    /* access modifiers changed from: private */
    public static final class AbilityWaiter {
        Ability ability;
        final CountDownLatch countDownLatch = new CountDownLatch(1);
        final Intent intent;

        AbilityWaiter(Intent intent2) {
            this.intent = intent2;
        }
    }

    /* access modifiers changed from: private */
    public static class AbilityToolsProxy {
        public static final int MSG_LEN_LIMIT = 1000;
        public static final int OUTPUT_MSG = 0;
        private AbilityToolsDeathRecipient abilityToolsDeathRecipient;
        private String deviceId;
        private IRemoteObject remote;
        private final Object remoteLock = new Object();
        private int serviceId;

        public AbilityToolsProxy(int i, String str) {
            this.serviceId = i;
            this.deviceId = str;
        }

        /* access modifiers changed from: package-private */
        public void output(String str) {
            if (str == null) {
                throw new IllegalArgumentException("Send message should not be null");
            } else if (str.length() <= 1000) {
                MessageParcel obtain = MessageParcel.obtain();
                MessageParcel obtain2 = MessageParcel.obtain();
                MessageOption messageOption = new MessageOption();
                if (obtain.writeInterfaceToken(AbilityDelegator.ABILITY_TOOLS_TOKEN)) {
                    obtain.writeString(str);
                    synchronized (this.remoteLock) {
                        if (this.remote != null) {
                            try {
                                this.remote.sendRequest(0, obtain, obtain2, messageOption);
                            } catch (RemoteException unused) {
                                AppLog.e(AbilityDelegator.LABEL, "Remote object communicate exception", new Object[0]);
                            }
                        } else {
                            obtain2.reclaim();
                            obtain.reclaim();
                            throw new IllegalStateException("You should get remote object first");
                        }
                    }
                    obtain2.reclaim();
                    obtain.reclaim();
                }
            } else {
                throw new IllegalArgumentException("Message length limit is 1000");
            }
        }

        /* access modifiers changed from: package-private */
        /* JADX WARNING: Removed duplicated region for block: B:16:0x002d  */
        /* JADX WARNING: Removed duplicated region for block: B:21:0x0043  */
        public void start() {
            synchronized (this.remoteLock) {
                if (this.remote == null) {
                    if (this.deviceId != null) {
                        if (!this.deviceId.isEmpty()) {
                            this.remote = SysAbilityManager.getSysAbility(this.serviceId, this.deviceId);
                            if (this.remote == null) {
                                if (this.abilityToolsDeathRecipient == null) {
                                    this.abilityToolsDeathRecipient = new AbilityToolsDeathRecipient();
                                    this.remote.addDeathRecipient(this.abilityToolsDeathRecipient, 0);
                                }
                                return;
                            }
                            throw new IllegalStateException("Get remote service error, serviceId is:" + this.serviceId);
                        }
                    }
                    this.remote = SysAbilityManager.getSysAbility(this.serviceId);
                    if (this.remote == null) {
                    }
                }
            }
        }

        /* access modifiers changed from: package-private */
        public void stop() {
            synchronized (this.remoteLock) {
                if (!(this.abilityToolsDeathRecipient == null || this.remote == null)) {
                    this.remote.removeDeathRecipient(this.abilityToolsDeathRecipient, 0);
                }
                this.remote = null;
                this.abilityToolsDeathRecipient = null;
            }
        }

        /* access modifiers changed from: private */
        public class AbilityToolsDeathRecipient implements IRemoteObject.DeathRecipient {
            private AbilityToolsDeathRecipient() {
            }

            public void onRemoteDied() {
                synchronized (AbilityToolsProxy.this.remoteLock) {
                    AppLog.i(AbilityDelegator.LABEL, "Remote died", new Object[0]);
                    AbilityToolsProxy.this.remote = null;
                    AbilityToolsProxy.this.abilityToolsDeathRecipient = null;
                }
            }
        }
    }

    /* access modifiers changed from: private */
    public static final class AbilityHelper {
        private AbilityHelper() {
        }

        static boolean reflectDispatchKeyEvent(Ability ability, KeyEvent keyEvent) throws AbilityDelegatorException {
            try {
                Method method = Ability.class.getMethod("dispatchKeyBoardEvent", KeyEvent.class);
                AccessController.doPrivileged(new PrivilegedAction(method) {
                    /* class ohos.abilityshell.delegation.$$Lambda$AbilityDelegator$AbilityHelper$ylwhuzT6PhE6UPNsmvTEGEfLY4M */
                    private final /* synthetic */ Method f$0;

                    {
                        this.f$0 = r1;
                    }

                    @Override // java.security.PrivilegedAction
                    public final Object run() {
                        return this.f$0.setAccessible(true);
                    }
                });
                Object invoke = method.invoke(ability, keyEvent);
                if (invoke instanceof Boolean) {
                    return ((Boolean) invoke).booleanValue();
                }
                return false;
            } catch (IllegalAccessException | NoSuchMethodException e) {
                throw new AbilityDelegatorException(new IllegalStateException("reflect dispatchKeyBoardEvent failed: " + e));
            } catch (InvocationTargetException e2) {
                throw new AbilityDelegatorException(e2.getCause());
            }
        }

        static void reflectDispatchBackKey(Ability ability) throws AbilityDelegatorException {
            try {
                Method method = Ability.class.getMethod("notifyBackKeyPressed", new Class[0]);
                AccessController.doPrivileged(new PrivilegedAction(method) {
                    /* class ohos.abilityshell.delegation.$$Lambda$AbilityDelegator$AbilityHelper$JB6O3ujPnJ47hlVG1iJ3qNDYf9c */
                    private final /* synthetic */ Method f$0;

                    {
                        this.f$0 = r1;
                    }

                    @Override // java.security.PrivilegedAction
                    public final Object run() {
                        return this.f$0.setAccessible(true);
                    }
                });
                method.invoke(ability, new Object[0]);
            } catch (IllegalAccessException | NoSuchMethodException e) {
                throw new AbilityDelegatorException(new IllegalStateException("reflect notifyBackKeyPressed failed: " + e));
            } catch (InvocationTargetException e2) {
                throw new AbilityDelegatorException(e2.getCause());
            }
        }

        static boolean reflectDispatchTouchEvent(Ability ability, TouchEvent touchEvent) throws AbilityDelegatorException {
            try {
                Method method = Ability.class.getMethod("dispatchTouchEvent", TouchEvent.class);
                AccessController.doPrivileged(new PrivilegedAction(method) {
                    /* class ohos.abilityshell.delegation.$$Lambda$AbilityDelegator$AbilityHelper$x4Vdp0kyjhX9eNtlIVS8oTVzOcI */
                    private final /* synthetic */ Method f$0;

                    {
                        this.f$0 = r1;
                    }

                    @Override // java.security.PrivilegedAction
                    public final Object run() {
                        return this.f$0.setAccessible(true);
                    }
                });
                Object invoke = method.invoke(ability, touchEvent);
                if (invoke instanceof Boolean) {
                    return ((Boolean) invoke).booleanValue();
                }
                return false;
            } catch (IllegalAccessException | NoSuchMethodException e) {
                throw new AbilityDelegatorException(new IllegalStateException("reflect dispatchTouchEvent failed: " + e));
            } catch (InvocationTargetException e2) {
                throw new AbilityDelegatorException(e2.getCause());
            }
        }

        static void reflectAbilityLifecycle(Ability ability, Intent intent, LifecycleAction lifecycleAction) throws AbilityDelegatorException {
            String value = lifecycleAction.getValue();
            if (lifecycleAction == LifecycleAction.START || lifecycleAction == LifecycleAction.FOREGROUND || lifecycleAction == LifecycleAction.ACTIVE) {
                try {
                    Method declaredMethod = Ability.class.getDeclaredMethod(value, Intent.class);
                    AccessController.doPrivileged(new PrivilegedAction(declaredMethod) {
                        /* class ohos.abilityshell.delegation.$$Lambda$AbilityDelegator$AbilityHelper$KhM8vcTclMaqFNBTDVvRXAPXhh0 */
                        private final /* synthetic */ Method f$0;

                        {
                            this.f$0 = r1;
                        }

                        @Override // java.security.PrivilegedAction
                        public final Object run() {
                            return this.f$0.setAccessible(true);
                        }
                    });
                    declaredMethod.invoke(ability, intent);
                } catch (IllegalAccessException | NoSuchMethodException e) {
                    throw new AbilityDelegatorException(new IllegalStateException("reflect ability " + value + " failed: " + e));
                } catch (InvocationTargetException e2) {
                    throw new AbilityDelegatorException(e2.getCause());
                }
            } else {
                try {
                    Method declaredMethod2 = Ability.class.getDeclaredMethod(value, new Class[0]);
                    AccessController.doPrivileged(new PrivilegedAction(declaredMethod2) {
                        /* class ohos.abilityshell.delegation.$$Lambda$AbilityDelegator$AbilityHelper$CH4t05vDgALRSYMjMhxJ_lHFSk */
                        private final /* synthetic */ Method f$0;

                        {
                            this.f$0 = r1;
                        }

                        @Override // java.security.PrivilegedAction
                        public final Object run() {
                            return this.f$0.setAccessible(true);
                        }
                    });
                    declaredMethod2.invoke(ability, new Object[0]);
                } catch (IllegalAccessException | NoSuchMethodException e3) {
                    throw new AbilityDelegatorException(new IllegalStateException("reflect ability " + value + " failed: " + e3));
                } catch (InvocationTargetException e4) {
                    throw new AbilityDelegatorException(e4.getCause());
                }
            }
        }

        static int reflectGetAbilityState(Ability ability) {
            try {
                Method declaredMethod = Ability.class.getDeclaredMethod("getCurrentState", new Class[0]);
                AccessController.doPrivileged(new PrivilegedAction(declaredMethod) {
                    /* class ohos.abilityshell.delegation.$$Lambda$AbilityDelegator$AbilityHelper$7nCuOWEjBHGyXzh6qFTaansbHE */
                    private final /* synthetic */ Method f$0;

                    {
                        this.f$0 = r1;
                    }

                    @Override // java.security.PrivilegedAction
                    public final Object run() {
                        return this.f$0.setAccessible(true);
                    }
                });
                Object invoke = declaredMethod.invoke(ability, new Object[0]);
                if (invoke instanceof Integer) {
                    return ((Integer) invoke).intValue();
                }
                throw new IllegalStateException("get ability lifecycle state failed.");
            } catch (IllegalAccessException | NoSuchMethodException e) {
                throw new IllegalStateException("get ability lifecycle state failed: " + e);
            } catch (InvocationTargetException e2) {
                throw new IllegalStateException(e2.getCause());
            }
        }

        static void reflectAbilitySliceLifecycle(Ability ability, Intent intent, LifecycleAction lifecycleAction) throws AbilityDelegatorException {
            AbilitySlice reflectGetCurrentAbilitySlice = reflectGetCurrentAbilitySlice(ability);
            if (reflectGetCurrentAbilitySlice == null) {
                throw new AbilityDelegatorException(new IllegalStateException("topAbilitySliceImpl is null."));
            } else if (checkAbilitySliceStatus(reflectGetCurrentAbilitySlice, lifecycleAction)) {
                String value = lifecycleAction.getValue();
                if (lifecycleAction == LifecycleAction.START || lifecycleAction == LifecycleAction.FOREGROUND) {
                    try {
                        Method declaredMethod = AbilitySlice.class.getDeclaredMethod(value, Intent.class);
                        setMethodAccessible(declaredMethod);
                        declaredMethod.invoke(reflectGetCurrentAbilitySlice, intent);
                    } catch (IllegalAccessException | NoSuchMethodException e) {
                        throw new AbilityDelegatorException(new IllegalStateException("reflect abilitySlice " + value + " failed: " + e));
                    } catch (InvocationTargetException e2) {
                        throw new AbilityDelegatorException(e2.getCause());
                    }
                } else {
                    try {
                        Method declaredMethod2 = AbilitySlice.class.getDeclaredMethod(value, new Class[0]);
                        setMethodAccessible(declaredMethod2);
                        declaredMethod2.invoke(reflectGetCurrentAbilitySlice, new Object[0]);
                    } catch (IllegalAccessException | NoSuchMethodException e3) {
                        throw new AbilityDelegatorException(new IllegalStateException("reflect abilitySlice " + value + " failed: " + e3));
                    } catch (InvocationTargetException e4) {
                        throw new AbilityDelegatorException(e4.getCause());
                    }
                }
            } else {
                throw new AbilityDelegatorException(new LifecycleException("topAbilitySliceImpl state is not fit."));
            }
        }

        static int reflectGetAbilitySliceState(AbilitySlice abilitySlice) {
            try {
                Method declaredMethod = AbilitySlice.class.getDeclaredMethod("getState", new Class[0]);
                setMethodAccessible(declaredMethod);
                Object invoke = declaredMethod.invoke(abilitySlice, new Object[0]);
                if (invoke instanceof AbilitySliceLifecycleExecutor.LifecycleState) {
                    return ((AbilitySliceLifecycleExecutor.LifecycleState) invoke).getValue();
                }
                return -1;
            } catch (IllegalAccessException | NoSuchMethodException | InvocationTargetException e) {
                throw new IllegalStateException("reflect abilitySlice getState failed: " + e);
            }
        }

        static List<AbilitySlice> reflectGetAllAbilitySlice(Ability ability) {
            AbilitySlice reflectGetCurrentAbilitySlice = reflectGetCurrentAbilitySlice(ability);
            AbilitySliceStack reflectAbilitySliceStack = reflectAbilitySliceStack(ability);
            ArrayList arrayList = new ArrayList();
            if (reflectGetCurrentAbilitySlice != null) {
                arrayList.add(reflectGetCurrentAbilitySlice);
            }
            arrayList.addAll(reflectGetAllSlices(reflectAbilitySliceStack));
            return arrayList;
        }

        static AbilitySlice reflectGetCurrentAbilitySlice(Ability ability) {
            try {
                Field declaredField = AbilitySliceScheduler.class.getDeclaredField("topAbilitySlice");
                setFieldAccessible(declaredField);
                Object obj = declaredField.get(reflectAbilitySliceScheduler(ability));
                if (obj instanceof AbilitySlice) {
                    return (AbilitySlice) obj;
                }
                return null;
            } catch (IllegalAccessException | NoSuchFieldException e) {
                throw new IllegalStateException("reflect topAbilitySliceImpl failed: " + e);
            }
        }

        static List<AbilitySlice> reflectGetAllSlices(AbilitySliceStack abilitySliceStack) {
            try {
                Method declaredMethod = AbilitySliceStack.class.getDeclaredMethod("getAllSlices", new Class[0]);
                setMethodAccessible(declaredMethod);
                Object invoke = declaredMethod.invoke(abilitySliceStack, new Object[0]);
                ArrayList arrayList = new ArrayList();
                try {
                    if (invoke instanceof List) {
                        for (Object obj : (List) invoke) {
                            if (obj instanceof AbilitySlice) {
                                arrayList.add((AbilitySlice) obj);
                            }
                        }
                    }
                } catch (ClassCastException unused) {
                }
                return arrayList;
            } catch (IllegalAccessException | NoSuchMethodException | InvocationTargetException e) {
                throw new IllegalStateException("reflect abilitySliceStack getAllSlices failed: " + e);
            }
        }

        static boolean reflectAbilitySliceStackIsEmpty(AbilitySliceStack abilitySliceStack) {
            try {
                Method declaredMethod = AbilitySliceStack.class.getDeclaredMethod("isEmpty", new Class[0]);
                setMethodAccessible(declaredMethod);
                Object invoke = declaredMethod.invoke(abilitySliceStack, new Object[0]);
                if (invoke instanceof Boolean) {
                    return ((Boolean) invoke).booleanValue();
                }
                return false;
            } catch (IllegalAccessException | NoSuchMethodException | InvocationTargetException e) {
                throw new IllegalStateException("reflect AbilitySliceStack isEmpty failed: " + e);
            }
        }

        static void reflectInvokeAbilityMethod(Ability ability, String str) {
            try {
                Method declaredMethod = Ability.class.getDeclaredMethod(str, new Class[0]);
                setMethodAccessible(declaredMethod);
                declaredMethod.invoke(ability, new Object[0]);
            } catch (IllegalAccessException | NoSuchMethodException | InvocationTargetException e) {
                throw new IllegalStateException("reflect reflectInvokeAbilityMethod failed: " + e);
            }
        }

        static void reflectInvokeAbilityMethod(Ability ability, String str, Class<?> cls, Object obj) {
            try {
                Method declaredMethod = Ability.class.getDeclaredMethod(str, cls);
                setMethodAccessible(declaredMethod);
                declaredMethod.invoke(ability, obj);
            } catch (IllegalAccessException | NoSuchMethodException | InvocationTargetException e) {
                throw new IllegalStateException("reflect reflectInvokeAbilityMethod failed: " + e);
            }
        }

        static int reflectAbilitySliceStackSize(AbilitySliceStack abilitySliceStack) {
            try {
                Method declaredMethod = AbilitySliceStack.class.getDeclaredMethod("size", new Class[0]);
                setMethodAccessible(declaredMethod);
                Object invoke = declaredMethod.invoke(abilitySliceStack, new Object[0]);
                if (invoke instanceof Integer) {
                    return ((Integer) invoke).intValue();
                }
                return 0;
            } catch (IllegalAccessException | NoSuchMethodException | InvocationTargetException e) {
                throw new IllegalStateException("reflect AbilitySliceStack size failed: " + e);
            }
        }

        static boolean checkAbilitySliceStatus(AbilitySlice abilitySlice, LifecycleAction lifecycleAction) {
            int reflectGetAbilitySliceState = reflectGetAbilitySliceState(abilitySlice);
            switch (lifecycleAction) {
                case START:
                    return reflectGetAbilitySliceState == 0;
                case STOP:
                case FOREGROUND:
                    return reflectGetAbilitySliceState == 3;
                case ACTIVE:
                case BACKGROUND:
                    return reflectGetAbilitySliceState == 1;
                case INACTIVE:
                    return reflectGetAbilitySliceState == 2;
                default:
                    return false;
            }
        }

        static AbilitySliceStack reflectAbilitySliceStack(Ability ability) {
            try {
                Field declaredField = AbilitySliceScheduler.class.getDeclaredField("abilitySliceStack");
                setFieldAccessible(declaredField);
                Object obj = declaredField.get(reflectAbilitySliceScheduler(ability));
                if (obj instanceof AbilitySliceStack) {
                    return (AbilitySliceStack) obj;
                }
                return null;
            } catch (IllegalAccessException | NoSuchFieldException e) {
                throw new IllegalStateException("reflect AbilitySliceStack failed: " + e);
            }
        }

        static AbilitySliceScheduler reflectAbilitySliceScheduler(Ability ability) {
            try {
                Field declaredField = AbilitySliceManager.class.getDeclaredField("abilitySliceScheduler");
                setFieldAccessible(declaredField);
                Object obj = declaredField.get(reflectAbilitySliceManager(ability));
                if (obj instanceof AbilitySliceScheduler) {
                    return (AbilitySliceScheduler) obj;
                }
                return null;
            } catch (IllegalAccessException | NoSuchFieldException e) {
                throw new IllegalStateException("reflect AbilitySliceScheduler failed: " + e);
            }
        }

        static AbilitySliceManager reflectAbilitySliceManager(Ability ability) {
            try {
                Field declaredField = Ability.class.getDeclaredField("abilitySliceManager");
                setFieldAccessible(declaredField);
                Object obj = declaredField.get(ability);
                if (obj instanceof AbilitySliceManager) {
                    return (AbilitySliceManager) obj;
                }
                return null;
            } catch (IllegalAccessException | NoSuchFieldException e) {
                throw new IllegalStateException("reflect AbilitySliceManager failed: " + e);
            }
        }

        /* access modifiers changed from: package-private */
        public enum LifecycleAction {
            START("start"),
            INACTIVE("inactive"),
            ACTIVE("active"),
            BACKGROUND("background"),
            FOREGROUND("foreground"),
            STOP("stop");
            
            String value;

            private LifecycleAction(String str) {
                this.value = str;
            }

            /* access modifiers changed from: package-private */
            public String getValue() {
                return this.value;
            }
        }

        static void setFieldAccessible(Field field) {
            AccessController.doPrivileged(new PrivilegedAction(field) {
                /* class ohos.abilityshell.delegation.$$Lambda$AbilityDelegator$AbilityHelper$CPR9SIPNunSVKhHi4jFwR3eGfUE */
                private final /* synthetic */ Field f$0;

                {
                    this.f$0 = r1;
                }

                @Override // java.security.PrivilegedAction
                public final Object run() {
                    return this.f$0.setAccessible(true);
                }
            });
        }

        static void setMethodAccessible(Method method) {
            AccessController.doPrivileged(new PrivilegedAction(method) {
                /* class ohos.abilityshell.delegation.$$Lambda$AbilityDelegator$AbilityHelper$pVxXk_h6vfCUe3axILD3k4rcBk4 */
                private final /* synthetic */ Method f$0;

                {
                    this.f$0 = r1;
                }

                @Override // java.security.PrivilegedAction
                public final Object run() {
                    return this.f$0.setAccessible(true);
                }
            });
        }
    }

    /* access modifiers changed from: private */
    public static class AbilityDelegatorException extends Exception {
        private static final long serialVersionUID = 8469632626376666737L;
        Throwable cause;

        public AbilityDelegatorException(Throwable th) {
            this.cause = th;
        }

        @Override // java.lang.Throwable
        public String getMessage() {
            return this.cause.getMessage();
        }

        @Override // java.lang.Throwable
        public synchronized Throwable getCause() {
            return this.cause;
        }

        @Override // java.lang.Throwable, java.lang.Object
        public String toString() {
            return this.cause.toString();
        }
    }

    /* access modifiers changed from: private */
    public final class DelegatorThread extends Thread {
        public DelegatorThread(String str) {
            super(str);
            setUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler(AbilityDelegator.this) {
                /* class ohos.abilityshell.delegation.AbilityDelegator.DelegatorThread.AnonymousClass1 */

                @Override // java.lang.Thread.UncaughtExceptionHandler
                public void uncaughtException(Thread thread, Throwable th) {
                    AppLog.e(AbilityDelegator.LABEL, "DelegatorThread meet uncaughtException. %{public}s", th.getMessage());
                    AbilityDelegator instance = AbilityDelegator.getInstance();
                    instance.print("[ERROR] meet unknown error: " + th.getMessage());
                }
            });
        }

        private boolean checkArgs() {
            if (AbilityDelegator.this.arguments.getTestRunnerClassName() == null || AbilityDelegator.this.arguments.getTestRunnerClassName().isEmpty()) {
                AppLog.e(AbilityDelegator.LABEL, "invalid testrunner class %{public}s, its empty", AbilityDelegator.this.arguments.getTestRunnerClassName());
                AbilityDelegator.getInstance().print("[ERROR] invalid TestRunner class, its empty!");
                return false;
            } else if (AbilityDelegator.this.arguments.getTestCaseNames() != null && !AbilityDelegator.this.arguments.getTestCaseNames().isEmpty()) {
                return true;
            } else {
                AppLog.e(AbilityDelegator.LABEL, "invalid unittest cases %{public}s, its empty", AbilityDelegator.this.arguments.getTestCaseNames());
                AbilityDelegator.getInstance().print("[ERROR] testcase name is empty!");
                return false;
            }
        }

        private Optional<TestRunner> createTestRunner() {
            try {
                Class<?> loadClass = AbilityDelegator.this.arguments.getTestClassLoader().loadClass(AbilityDelegator.this.arguments.getTestRunnerClassName());
                if (!TestRunner.class.isAssignableFrom(loadClass)) {
                    AppLog.e(AbilityDelegator.LABEL, "%{public}s is inapposite!", AbilityDelegator.this.arguments.getTestRunnerClassName());
                    AbilityDelegator instance = AbilityDelegator.getInstance();
                    instance.print("[ERROR] " + AbilityDelegator.this.arguments.getTestRunnerClassName() + " must be child of " + TestRunner.class.getName());
                    return Optional.empty();
                }
                try {
                    Object newInstance = loadClass.newInstance();
                    if (newInstance instanceof TestRunner) {
                        return Optional.of((TestRunner) newInstance);
                    }
                    AppLog.e(AbilityDelegator.LABEL, "%{public}s is not TestRunner", AbilityDelegator.this.arguments.getTestRunnerClassName());
                    AbilityDelegator instance2 = AbilityDelegator.getInstance();
                    instance2.print("[ERROR] " + AbilityDelegator.this.arguments.getTestRunnerClassName() + " is not inherit from " + TestRunner.class.getName());
                    return Optional.empty();
                } catch (IllegalAccessException | InstantiationException e) {
                    AppLog.e(AbilityDelegator.LABEL, "create unittest runner object failed for %{public}s", e.getMessage());
                    return Optional.empty();
                }
            } catch (ClassNotFoundException unused) {
                AppLog.e(AbilityDelegator.LABEL, "%{public}s is not found!", AbilityDelegator.this.arguments.getTestRunnerClassName());
                AbilityDelegator instance3 = AbilityDelegator.getInstance();
                instance3.print("[ERROR] class " + AbilityDelegator.this.arguments.getTestRunnerClassName() + " is undefined!");
                return Optional.empty();
            }
        }

        @Override // java.lang.Thread, java.lang.Runnable
        public void run() {
            try {
                AppLog.d(AbilityDelegator.LABEL, "AbilityDelegator::DelegatorThread::run", new Object[0]);
                checkArgs();
                Optional<TestRunner> createTestRunner = createTestRunner();
                if (createTestRunner.isPresent()) {
                    AppLog.i(AbilityDelegator.LABEL, "prepare for running unittest", new Object[0]);
                    createTestRunner.get().prepare();
                    AppLog.i(AbilityDelegator.LABEL, "running unittest", new Object[0]);
                    createTestRunner.get().run();
                    AppLog.i(AbilityDelegator.LABEL, "finish running unittest", new Object[0]);
                }
            } finally {
                AbilityDelegator.getInstance().print(AbilityDelegator.FINISH_TAG);
                AbilityDelegator.getInstance().stopAbilityToolProxy();
            }
        }
    }
}
