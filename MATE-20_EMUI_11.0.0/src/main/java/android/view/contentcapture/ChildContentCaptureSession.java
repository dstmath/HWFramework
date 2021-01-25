package android.view.contentcapture;

import android.view.autofill.AutofillId;
import android.view.contentcapture.ViewNode;

final class ChildContentCaptureSession extends ContentCaptureSession {
    private final ContentCaptureSession mParent;

    protected ChildContentCaptureSession(ContentCaptureSession parent, ContentCaptureContext clientContext) {
        super(clientContext);
        this.mParent = parent;
    }

    /* access modifiers changed from: package-private */
    @Override // android.view.contentcapture.ContentCaptureSession
    public MainContentCaptureSession getMainCaptureSession() {
        ContentCaptureSession contentCaptureSession = this.mParent;
        if (contentCaptureSession instanceof MainContentCaptureSession) {
            return (MainContentCaptureSession) contentCaptureSession;
        }
        return contentCaptureSession.getMainCaptureSession();
    }

    /* access modifiers changed from: package-private */
    @Override // android.view.contentcapture.ContentCaptureSession
    public ContentCaptureSession newChild(ContentCaptureContext clientContext) {
        ContentCaptureSession child = new ChildContentCaptureSession(this, clientContext);
        getMainCaptureSession().notifyChildSessionStarted(this.mId, child.mId, clientContext);
        return child;
    }

    /* access modifiers changed from: package-private */
    @Override // android.view.contentcapture.ContentCaptureSession
    public void flush(int reason) {
        this.mParent.flush(reason);
    }

    @Override // android.view.contentcapture.ContentCaptureSession
    public void updateContentCaptureContext(ContentCaptureContext context) {
        getMainCaptureSession().notifyContextUpdated(this.mId, context);
    }

    /* access modifiers changed from: package-private */
    @Override // android.view.contentcapture.ContentCaptureSession
    public void onDestroy() {
        getMainCaptureSession().notifyChildSessionFinished(this.mParent.mId, this.mId);
    }

    /* access modifiers changed from: package-private */
    @Override // android.view.contentcapture.ContentCaptureSession
    public void internalNotifyViewAppeared(ViewNode.ViewStructureImpl node) {
        getMainCaptureSession().notifyViewAppeared(this.mId, node);
    }

    /* access modifiers changed from: package-private */
    @Override // android.view.contentcapture.ContentCaptureSession
    public void internalNotifyViewDisappeared(AutofillId id) {
        getMainCaptureSession().notifyViewDisappeared(this.mId, id);
    }

    /* access modifiers changed from: package-private */
    @Override // android.view.contentcapture.ContentCaptureSession
    public void internalNotifyViewTextChanged(AutofillId id, CharSequence text) {
        getMainCaptureSession().notifyViewTextChanged(this.mId, id, text);
    }

    @Override // android.view.contentcapture.ContentCaptureSession
    public void internalNotifyViewTreeEvent(boolean started) {
        getMainCaptureSession().notifyViewTreeEvent(this.mId, started);
    }

    /* access modifiers changed from: package-private */
    @Override // android.view.contentcapture.ContentCaptureSession
    public boolean isContentCaptureEnabled() {
        return getMainCaptureSession().isContentCaptureEnabled();
    }
}
