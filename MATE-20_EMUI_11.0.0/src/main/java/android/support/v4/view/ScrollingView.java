package android.support.v4.view;

public interface ScrollingView {
    @Override // android.support.v4.view.ScrollingView
    int computeHorizontalScrollExtent();

    @Override // android.support.v4.view.ScrollingView
    int computeHorizontalScrollOffset();

    @Override // android.support.v4.view.ScrollingView
    int computeHorizontalScrollRange();

    @Override // android.support.v4.view.ScrollingView
    int computeVerticalScrollExtent();

    @Override // android.support.v4.view.ScrollingView
    int computeVerticalScrollOffset();

    @Override // android.support.v4.view.ScrollingView
    int computeVerticalScrollRange();
}
