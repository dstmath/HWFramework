package android.widget;

public interface SectionIndexer {
    int getPositionForSection(int i);

    int getSectionForPosition(int i);

    Object[] getSections();
}
