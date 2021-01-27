package ohos.aafwk.ability;

public abstract class OnClickListener extends ViewListener {
    public abstract void onClick(int i, AbilityForm abilityForm, ViewsStatus viewsStatus);

    @Override // ohos.aafwk.ability.ViewListener
    public void onTouchEvent(AbilityForm abilityForm, ViewsStatus viewsStatus) {
        onClick(this.componentId, abilityForm, viewsStatus);
    }
}
