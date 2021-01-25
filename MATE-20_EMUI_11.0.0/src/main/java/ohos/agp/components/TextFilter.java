package ohos.agp.components;

public abstract class TextFilter {

    protected static class FilterResults {
        public int count;
        public Object values;
    }

    /* access modifiers changed from: protected */
    public abstract FilterResults performFiltering(CharSequence charSequence);

    /* access modifiers changed from: protected */
    public abstract void publishResults(CharSequence charSequence, FilterResults filterResults);

    public void filter(CharSequence charSequence) {
        publishResults(charSequence, performFiltering(charSequence));
    }
}
