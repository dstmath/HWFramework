package android.service.autofill.augmented;

import android.view.WindowManager;
import java.util.function.BiConsumer;

/* renamed from: android.service.autofill.augmented.-$$Lambda$FillWindow$FillWindowPresenter$hdkNZGuYdsvcArvQ2SoMspO1EO8  reason: invalid class name */
/* compiled from: lambda */
public final /* synthetic */ class $$Lambda$FillWindow$FillWindowPresenter$hdkNZGuYdsvcArvQ2SoMspO1EO8 implements BiConsumer {
    public static final /* synthetic */ $$Lambda$FillWindow$FillWindowPresenter$hdkNZGuYdsvcArvQ2SoMspO1EO8 INSTANCE = new $$Lambda$FillWindow$FillWindowPresenter$hdkNZGuYdsvcArvQ2SoMspO1EO8();

    private /* synthetic */ $$Lambda$FillWindow$FillWindowPresenter$hdkNZGuYdsvcArvQ2SoMspO1EO8() {
    }

    @Override // java.util.function.BiConsumer
    public final void accept(Object obj, Object obj2) {
        ((FillWindow) obj).handleShow((WindowManager.LayoutParams) obj2);
    }
}
