package ohos.agp.components;

public abstract class TextFilter {

    protected static class FilterResults {
        public Object results;
        public int size;
    }

    /* access modifiers changed from: protected */
    public abstract FilterResults executeFiltering(CharSequence charSequence);

    /* access modifiers changed from: protected */
    public abstract void publishFilterResults(CharSequence charSequence, FilterResults filterResults);

    public void filter(CharSequence charSequence) {
        publishFilterResults(charSequence, executeFiltering(charSequence));
    }
}
