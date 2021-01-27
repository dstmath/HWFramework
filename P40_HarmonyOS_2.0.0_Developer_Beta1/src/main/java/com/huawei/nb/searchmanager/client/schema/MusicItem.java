package com.huawei.nb.searchmanager.client.schema;

import com.huawei.nb.searchmanager.client.model.IndexForm;
import com.huawei.nb.searchmanager.client.model.IndexType;
import java.util.ArrayList;
import java.util.List;

public final class MusicItem extends Schema<MusicItem> {
    public static final String ALBUM = "album";
    public static final String ARTIST = "artist";
    public static final String DURATION = "duration";
    public static final String GENRE = "genre";
    public static final String SIZE = "size";

    @Override // com.huawei.nb.searchmanager.client.schema.Schema
    public /* bridge */ /* synthetic */ String getAlternateName() {
        return super.getAlternateName();
    }

    @Override // com.huawei.nb.searchmanager.client.schema.Schema
    public /* bridge */ /* synthetic */ String getCategory() {
        return super.getCategory();
    }

    @Override // com.huawei.nb.searchmanager.client.schema.Schema
    public /* bridge */ /* synthetic */ Long getDateCreate() {
        return super.getDateCreate();
    }

    @Override // com.huawei.nb.searchmanager.client.schema.Schema
    public /* bridge */ /* synthetic */ String getDescription() {
        return super.getDescription();
    }

    @Override // com.huawei.nb.searchmanager.client.schema.Schema
    public /* bridge */ /* synthetic */ String getIdentifier() {
        return super.getIdentifier();
    }

    @Override // com.huawei.nb.searchmanager.client.schema.Schema
    public /* bridge */ /* synthetic */ String getKeywords() {
        return super.getKeywords();
    }

    @Override // com.huawei.nb.searchmanager.client.schema.Schema
    public /* bridge */ /* synthetic */ String getName() {
        return super.getName();
    }

    @Override // com.huawei.nb.searchmanager.client.schema.Schema
    public /* bridge */ /* synthetic */ String getPotentialAction() {
        return super.getPotentialAction();
    }

    @Override // com.huawei.nb.searchmanager.client.schema.Schema
    public /* bridge */ /* synthetic */ String getReserved1() {
        return super.getReserved1();
    }

    @Override // com.huawei.nb.searchmanager.client.schema.Schema
    public /* bridge */ /* synthetic */ String getReserved2() {
        return super.getReserved2();
    }

    @Override // com.huawei.nb.searchmanager.client.schema.Schema
    public /* bridge */ /* synthetic */ String getSubTitle() {
        return super.getSubTitle();
    }

    @Override // com.huawei.nb.searchmanager.client.schema.Schema
    public /* bridge */ /* synthetic */ String getThumbnailUrl() {
        return super.getThumbnailUrl();
    }

    @Override // com.huawei.nb.searchmanager.client.schema.Schema
    public /* bridge */ /* synthetic */ String getTitle() {
        return super.getTitle();
    }

    @Override // com.huawei.nb.searchmanager.client.schema.Schema
    public /* bridge */ /* synthetic */ String getUrl() {
        return super.getUrl();
    }

    public static List<IndexForm> getMusicSchema() {
        AnonymousClass1 r0 = new ArrayList<IndexForm>() {
            /* class com.huawei.nb.searchmanager.client.schema.MusicItem.AnonymousClass1 */

            {
                add(new IndexForm(MusicItem.ARTIST, IndexType.SORTED, false, true, true));
                add(new IndexForm(MusicItem.GENRE, IndexType.SORTED, false, true, true));
                add(new IndexForm("duration", IndexType.NO, false, true, false));
                add(new IndexForm(MusicItem.ALBUM, IndexType.SORTED, false, true, true));
                add(new IndexForm("size", IndexType.NO, false, true, false));
            }
        };
        r0.addAll(CommonItem.getCommonSchema());
        return r0;
    }

    public MusicItem() {
        super.set(this);
    }

    public MusicItem setArtist(String str) {
        super.put(ARTIST, str);
        return this;
    }

    public MusicItem setGenre(String str) {
        super.put(GENRE, str);
        return this;
    }

    public MusicItem setDuration(Integer num) {
        super.put("duration", num);
        return this;
    }

    public MusicItem setAlbum(String str) {
        super.put(ALBUM, str);
        return this;
    }

    public MusicItem setSize(Integer num) {
        super.put("size", num);
        return this;
    }

    public Integer getDuration() {
        return super.getAsInteger("duration");
    }

    public String getArtist() {
        return super.getAsString(ARTIST);
    }

    public String getGenre() {
        return super.getAsString(GENRE);
    }

    public String getAlbum() {
        return super.getAsString(ALBUM);
    }

    public Integer getSize() {
        return super.getAsInteger("size");
    }
}
