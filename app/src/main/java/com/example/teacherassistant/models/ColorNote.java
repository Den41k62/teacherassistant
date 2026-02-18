package com.example.teacherassistant.models;

public class ColorNote {
    private int colorCode;
    private String label;

    public ColorNote(int colorCode, String label) {
        this.colorCode = colorCode;
        this.label = label;
    }

    public int getColorCode() { return colorCode; }
    public void setColorCode(int colorCode) { this.colorCode = colorCode; }

    public String getLabel() { return label; }
    public void setLabel(String label) { this.label = label; }
}