package ohos.aafwk.ability.fraction;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import ohos.aafwk.ability.Ability;
import ohos.aafwk.content.Intent;
import ohos.agp.components.Component;
import ohos.app.Context;
import ohos.utils.PacMap;

public class FractionAbility extends Ability {
    private static final String FRACTION_TAG = "harmony:fractions";
    private final FractionManager mFractionManager = new FractionManager();

    public FractionManager getFractionManager() {
        return this.mFractionManager;
    }

    /* access modifiers changed from: protected */
    @Override // ohos.aafwk.ability.Ability
    public void onPostStart(PacMap pacMap) {
        super.onPostStart(pacMap);
        if (pacMap != null) {
            Optional objectValue = pacMap.getObjectValue(FRACTION_TAG);
            if (objectValue.isPresent() && (objectValue.get() instanceof Map)) {
                this.mFractionManager.restoreAllState((HashMap) objectValue.get());
            }
        }
    }

    /* access modifiers changed from: protected */
    @Override // ohos.aafwk.ability.Ability
    public void onStart(Intent intent) {
        this.mFractionManager.attachAbility(this);
        super.onStart(intent);
        this.mFractionManager.handleComponentAttach();
        this.mFractionManager.handleStart();
    }

    /* access modifiers changed from: protected */
    @Override // ohos.aafwk.ability.Ability
    public void onActive() {
        super.onActive();
        this.mFractionManager.handleActive();
    }

    /* access modifiers changed from: protected */
    @Override // ohos.aafwk.ability.Ability
    public void onBackground() {
        super.onBackground();
        this.mFractionManager.handleBackground();
    }

    /* access modifiers changed from: protected */
    @Override // ohos.aafwk.ability.Ability
    public void onInactive() {
        super.onInactive();
        this.mFractionManager.handleInActive();
    }

    /* access modifiers changed from: protected */
    @Override // ohos.aafwk.ability.Ability
    public void onStop() {
        super.onStop();
        this.mFractionManager.handleStop();
    }

    /* access modifiers changed from: protected */
    @Override // ohos.aafwk.ability.Ability
    public void onForeground(Intent intent) {
        super.onForeground(intent);
        this.mFractionManager.handleForeground();
    }

    @Override // ohos.aafwk.ability.Ability
    public void onSaveAbilityState(PacMap pacMap) {
        super.onSaveAbilityState(pacMap);
        HashMap<String, Object> saveAllData = this.mFractionManager.saveAllData();
        if (saveAllData != null) {
            pacMap.putObjectValue(FRACTION_TAG, saveAllData);
        }
    }

    @Override // ohos.aafwk.ability.Ability
    public void onRestoreAbilityState(PacMap pacMap) {
        super.onRestoreAbilityState(pacMap);
    }

    /* access modifiers changed from: protected */
    @Override // ohos.aafwk.ability.Ability
    public void onBackPressed() {
        if (!this.mFractionManager.popStack()) {
            super.onBackPressed();
        }
    }

    /* access modifiers changed from: package-private */
    public Component getAbilityComponentById(int i) {
        return findComponentById(i);
    }

    /* access modifiers changed from: package-private */
    public Fraction init(Context context, String str, PacMap pacMap) {
        return Fraction.init(context, str, pacMap);
    }
}
