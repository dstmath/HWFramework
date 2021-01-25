package ohos.aafwk.ability;

public abstract class ViewListener {
    int viewId;

    public abstract void onTouchEvent(AbilityForm abilityForm, ViewsStatus viewsStatus);
}
