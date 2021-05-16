package com.mycompany.app;

public class Entity {
    private String Score;
    private String Type;
    private String Text;
    private String BeginOffset;
    private String EndOffset;

    public String getScore() {
        return Score;
    }

    public void setScore(String score) {
        Score = score;
    }

    public String getType() {
        return Type;
    }

    public void setType(String type) {
        Type = type;
    }

    public String getText() {
        return Text;
    }

    public void setText(String text) {
        Text = text;
    }

    public String getBeginOffset() {
        return BeginOffset;
    }

    public void setBeginOffset(String beginOffset) {
        BeginOffset = beginOffset;
    }

    public String getEndOffset() {
        return EndOffset;
    }

    public void setEndOffset(String endOffset) {
        EndOffset = endOffset;
    }
}
