package android.service.autofill.augmented;

import android.annotation.SystemApi;
import android.graphics.Rect;
import android.service.autofill.augmented.AugmentedAutofillService;
import java.io.PrintWriter;

@SystemApi
public abstract class PresentationParams {
    /* access modifiers changed from: package-private */
    public abstract void dump(String str, PrintWriter printWriter);

    PresentationParams() {
    }

    public Area getSuggestionArea() {
        return null;
    }

    @SystemApi
    public static abstract class Area {
        private final Rect mBounds;
        public final AugmentedAutofillService.AutofillProxy proxy;

        private Area(AugmentedAutofillService.AutofillProxy proxy2, Rect bounds) {
            this.proxy = proxy2;
            this.mBounds = bounds;
        }

        public Rect getBounds() {
            return this.mBounds;
        }

        public String toString() {
            return this.mBounds.toString();
        }
    }

    public static final class SystemPopupPresentationParams extends PresentationParams {
        private final Area mSuggestionArea;

        public SystemPopupPresentationParams(AugmentedAutofillService.AutofillProxy proxy, Rect rect) {
            this.mSuggestionArea = new Area(proxy, rect) {
                /* class android.service.autofill.augmented.PresentationParams.SystemPopupPresentationParams.AnonymousClass1 */
            };
        }

        @Override // android.service.autofill.augmented.PresentationParams
        public Area getSuggestionArea() {
            return this.mSuggestionArea;
        }

        /* access modifiers changed from: package-private */
        @Override // android.service.autofill.augmented.PresentationParams
        public void dump(String prefix, PrintWriter pw) {
            pw.print(prefix);
            pw.print("area: ");
            pw.println(this.mSuggestionArea);
        }
    }
}
