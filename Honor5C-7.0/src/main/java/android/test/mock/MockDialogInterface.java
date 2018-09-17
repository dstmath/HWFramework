package android.test.mock;

import android.content.DialogInterface;

@Deprecated
public class MockDialogInterface implements DialogInterface {
    public void cancel() {
        throw new UnsupportedOperationException("not implemented yet");
    }

    public void dismiss() {
        throw new UnsupportedOperationException("not implemented yet");
    }
}
