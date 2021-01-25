package com.huawei.nb.searchmanager.client;

import android.os.Parcel;
import android.os.Parcelable;

public class Word implements Parcelable {
    public static final Parcelable.Creator<Word> CREATOR = new Parcelable.Creator<Word>() {
        /* class com.huawei.nb.searchmanager.client.Word.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public Word createFromParcel(Parcel parcel) {
            return new Word(parcel);
        }

        @Override // android.os.Parcelable.Creator
        public Word[] newArray(int i) {
            return new Word[i];
        }
    };
    private int endOffset;
    private int startOffset;
    private String wordText;
    private String wordType;

    @Override // android.os.Parcelable
    public int describeContents() {
        return 0;
    }

    public Word() {
        this.wordText = "";
        this.startOffset = 0;
        this.endOffset = 0;
        this.wordType = "word";
    }

    protected Word(Parcel parcel) {
        this.wordText = parcel.readString();
        this.startOffset = parcel.readInt();
        this.endOffset = parcel.readInt();
        this.wordType = parcel.readString();
    }

    @Override // android.os.Parcelable
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(this.wordText);
        parcel.writeInt(this.startOffset);
        parcel.writeInt(this.endOffset);
        parcel.writeString(this.wordType);
    }

    public String getWordText() {
        return this.wordText;
    }

    public void setWordText(String str) {
        this.wordText = str;
    }

    public int getStartOffset() {
        return this.startOffset;
    }

    public void setStartOffset(int i) {
        this.startOffset = i;
    }

    public int getEndOffset() {
        return this.endOffset;
    }

    public void setEndOffset(int i) {
        this.endOffset = i;
    }

    public String getWordType() {
        return this.wordType;
    }

    public void setWordType(String str) {
        this.wordType = str;
    }
}
