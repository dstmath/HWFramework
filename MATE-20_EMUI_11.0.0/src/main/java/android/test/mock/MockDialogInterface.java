package android.test.mock;

import android.content.DialogInterface;

@Deprecated
public class MockDialogInterface implements DialogInterface {
    @Override // android.content.DialogInterface
    public void cancel() {
        throw new UnsupportedOperationException("not implemented yet");
    }

    @Override // android.content.DialogInterface
    public void dismiss() {
        throw new UnsupportedOperationException("not implemented yet");
    }
}
