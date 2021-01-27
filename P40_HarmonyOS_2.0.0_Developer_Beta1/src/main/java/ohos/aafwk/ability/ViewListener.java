package ohos.aafwk.ability;

public abstract class ViewListener {
    int componentId;

    public abstract void onTouchEvent(AbilityForm abilityForm, ViewsStatus viewsStatus);
}
