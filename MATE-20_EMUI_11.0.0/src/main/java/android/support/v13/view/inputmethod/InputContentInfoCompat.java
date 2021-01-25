package android.support.v13.view.inputmethod;

import android.content.ClipDescription;
import android.net.Uri;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.view.inputmethod.InputContentInfo;

public final class InputContentInfoCompat {
    private final InputContentInfoCompatImpl mImpl;

    private interface InputContentInfoCompatImpl {
        @NonNull
        Uri getContentUri();

        @NonNull
        ClipDescription getDescription();

        @Nullable
        Object getInputContentInfo();

        @Nullable
        Uri getLinkUri();

        void releasePermission();

        void requestPermission();
    }

    private static final class InputContentInfoCompatBaseImpl implements InputContentInfoCompatImpl {
        @NonNull
        private final Uri mContentUri;
        @NonNull
        private final ClipDescription mDescription;
        @Nullable
        private final Uri mLinkUri;

        InputContentInfoCompatBaseImpl(@NonNull Uri contentUri, @NonNull ClipDescription description, @Nullable Uri linkUri) {
            this.mContentUri = contentUri;
            this.mDescription = description;
            this.mLinkUri = linkUri;
        }

        @Override // android.support.v13.view.inputmethod.InputContentInfoCompat.InputContentInfoCompatImpl
        @NonNull
        public Uri getContentUri() {
            return this.mContentUri;
        }

        @Override // android.support.v13.view.inputmethod.InputContentInfoCompat.InputContentInfoCompatImpl
        @NonNull
        public ClipDescription getDescription() {
            return this.mDescription;
        }

        @Override // android.support.v13.view.inputmethod.InputContentInfoCompat.InputContentInfoCompatImpl
        @Nullable
        public Uri getLinkUri() {
            return this.mLinkUri;
        }

        @Override // android.support.v13.view.inputmethod.InputContentInfoCompat.InputContentInfoCompatImpl
        @Nullable
        public Object getInputContentInfo() {
            return null;
        }

        @Override // android.support.v13.view.inputmethod.InputContentInfoCompat.InputContentInfoCompatImpl
        public void requestPermission() {
        }

        @Override // android.support.v13.view.inputmethod.InputContentInfoCompat.InputContentInfoCompatImpl
        public void releasePermission() {
        }
    }

    @RequiresApi(25)
    private static final class InputContentInfoCompatApi25Impl implements InputContentInfoCompatImpl {
        @NonNull
        final InputContentInfo mObject;

        InputContentInfoCompatApi25Impl(@NonNull Object inputContentInfo) {
            this.mObject = (InputContentInfo) inputContentInfo;
        }

        InputContentInfoCompatApi25Impl(@NonNull Uri contentUri, @NonNull ClipDescription description, @Nullable Uri linkUri) {
            this.mObject = new InputContentInfo(contentUri, description, linkUri);
        }

        @Override // android.support.v13.view.inputmethod.InputContentInfoCompat.InputContentInfoCompatImpl
        @NonNull
        public Uri getContentUri() {
            return this.mObject.getContentUri();
        }

        @Override // android.support.v13.view.inputmethod.InputContentInfoCompat.InputContentInfoCompatImpl
        @NonNull
        public ClipDescription getDescription() {
            return this.mObject.getDescription();
        }

        @Override // android.support.v13.view.inputmethod.InputContentInfoCompat.InputContentInfoCompatImpl
        @Nullable
        public Uri getLinkUri() {
            return this.mObject.getLinkUri();
        }

        @Override // android.support.v13.view.inputmethod.InputContentInfoCompat.InputContentInfoCompatImpl
        @Nullable
        public Object getInputContentInfo() {
            return this.mObject;
        }

        @Override // android.support.v13.view.inputmethod.InputContentInfoCompat.InputContentInfoCompatImpl
        public void requestPermission() {
            this.mObject.requestPermission();
        }

        @Override // android.support.v13.view.inputmethod.InputContentInfoCompat.InputContentInfoCompatImpl
        public void releasePermission() {
            this.mObject.releasePermission();
        }
    }

    public InputContentInfoCompat(@NonNull Uri contentUri, @NonNull ClipDescription description, @Nullable Uri linkUri) {
        if (Build.VERSION.SDK_INT >= 25) {
            this.mImpl = new InputContentInfoCompatApi25Impl(contentUri, description, linkUri);
        } else {
            this.mImpl = new InputContentInfoCompatBaseImpl(contentUri, description, linkUri);
        }
    }

    private InputContentInfoCompat(@NonNull InputContentInfoCompatImpl impl) {
        this.mImpl = impl;
    }

    @NonNull
    public Uri getContentUri() {
        return this.mImpl.getContentUri();
    }

    @NonNull
    public ClipDescription getDescription() {
        return this.mImpl.getDescription();
    }

    @Nullable
    public Uri getLinkUri() {
        return this.mImpl.getLinkUri();
    }

    @Nullable
    public static InputContentInfoCompat wrap(@Nullable Object inputContentInfo) {
        if (inputContentInfo != null && Build.VERSION.SDK_INT >= 25) {
            return new InputContentInfoCompat(new InputContentInfoCompatApi25Impl(inputContentInfo));
        }
        return null;
    }

    @Nullable
    public Object unwrap() {
        return this.mImpl.getInputContentInfo();
    }

    public void requestPermission() {
        this.mImpl.requestPermission();
    }

    public void releasePermission() {
        this.mImpl.releasePermission();
    }
}
