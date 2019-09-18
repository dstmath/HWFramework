package com.huawei.nb.searchmanager.client;

import android.os.Parcel;
import android.os.Parcelable;

public class Word implements Parcelable {
    public static final Parcelable.Creator<Word> CREATOR = new Parcelable.Creator<Word>() {
        public Word createFromParcel(Parcel in) {
            return new Word(in);
        }

        public Word[] newArray(int size) {
            return new Word[size];
        }
    };
    private int endOffset;
    private int startOffset;
    private String wordText;
    private String wordType;

    public Word() {
        this.wordText = "";
        this.startOffset = 0;
        this.endOffset = 0;
        this.wordType = "word";
    }

    protected Word(Parcel in) {
        this.wordText = in.readString();
        this.startOffset = in.readInt();
        this.endOffset = in.readInt();
        this.wordType = in.readString();
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.wordText);
        dest.writeInt(this.startOffset);
        dest.writeInt(this.endOffset);
        dest.writeString(this.wordType);
    }

    public String getWordText() {
        return this.wordText;
    }

    public void setWordText(String wordText2) {
        this.wordText = wordText2;
    }

    public int getStartOffset() {
        return this.startOffset;
    }

    public void setStartOffset(int startOffset2) {
        this.startOffset = startOffset2;
    }

    public int getEndOffset() {
        return this.endOffset;
    }

    public void setEndOffset(int endOffset2) {
        this.endOffset = endOffset2;
    }

    public String getWordType() {
        return this.wordType;
    }

    public void setWordType(String wordType2) {
        this.wordType = wordType2;
    }

    public int describeContents() {
        return 0;
    }
}
