package ohos.app;

import ohos.abilityshell.utils.LifecycleState;
import ohos.app.dispatcher.TaskDispatcher;
import ohos.app.dispatcher.threading.TaskLooper;
import ohos.bundle.AbilityInfo;
import ohos.global.resource.ResourceManager;

public class AbilityRecord {
    private AbilityContext abilityContext;
    private AbilityInfo abilityInfo;
    private LifecycleState.AbilityState curState = LifecycleState.AbilityState.INITIAL_STATE;
    private boolean initResMgrSuccess = false;
    private TaskLooper looper;
    private ResourceManager resourceManager;
    private Object token;
    private TaskDispatcher uiTaskDispatcher;

    public AbilityInfo getAbilityInfo() {
        return this.abilityInfo;
    }

    public void setAbilityInfo(AbilityInfo abilityInfo2) {
        this.abilityInfo = abilityInfo2;
    }

    public TaskLooper getTaskLooper() {
        return this.looper;
    }

    public void setTaskLooper(TaskLooper taskLooper) {
        this.looper = taskLooper;
    }

    public Object getToken() {
        return this.token;
    }

    public void setToken(Object obj) {
        this.token = obj;
    }

    public AbilityContext getAbilityContext() {
        return this.abilityContext;
    }

    public void setAbilityContext(AbilityContext abilityContext2) {
        this.abilityContext = abilityContext2;
    }

    public void setUITaskDispatcher(TaskDispatcher taskDispatcher) {
        this.uiTaskDispatcher = taskDispatcher;
    }

    public TaskDispatcher getUITaskDispatcher() {
        return this.uiTaskDispatcher;
    }

    public void setResourceManager(ResourceManager resourceManager2) {
        this.resourceManager = resourceManager2;
    }

    public ResourceManager getResourceManager() {
        return this.resourceManager;
    }

    public void setInitResMgrSuccess(boolean z) {
        this.initResMgrSuccess = z;
    }

    public boolean isInitResMgrSuccess() {
        return this.initResMgrSuccess;
    }

    public void setCurState(LifecycleState.AbilityState abilityState) {
        this.curState = abilityState;
    }

    public LifecycleState.AbilityState getCurState() {
        return this.curState;
    }
}
