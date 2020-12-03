package com.inbrain.sdk.config;

import java.io.Serializable;

public class ToolBarConfig implements Serializable {
    private Boolean elevationEnabled;
    private int toolbarColorResId;
    private int toolbarColor;
    private int backButtonColorResId;
    private int backButtonColor;
    private String title;
    private int titleColorResId;
    private int titleColor;

    public Boolean isElevationEnabled() {
        return elevationEnabled;
    }

    public ToolBarConfig setElevationEnabled(boolean elevationEnabled) {
        this.elevationEnabled = elevationEnabled;
        return this;
    }

    public int getToolbarColor() {
        return toolbarColor;
    }

    public ToolBarConfig setToolbarColor(int toolbarColor) {
        this.toolbarColor = toolbarColor;
        return this;
    }

    public int getToolbarColorResId() {
        return toolbarColorResId;
    }

    public ToolBarConfig setToolbarColorResId(int toolbarColorResId) {
        this.toolbarColorResId = toolbarColorResId;
        return this;
    }

    public int getBackButtonColor() {
        return backButtonColor;
    }

    public ToolBarConfig setBackButtonColor(int backButtonColor) {
        this.backButtonColor = backButtonColor;
        return this;
    }

    public int getBackButtonColorResId() {
        return backButtonColorResId;
    }

    public ToolBarConfig setBackButtonColorResId(int backButtonColorResId) {
        this.backButtonColorResId = backButtonColorResId;
        return this;
    }

    public String getTitle() {
        return title;
    }

    public ToolBarConfig setTitle(String title) {
        this.title = title;
        return this;
    }

    public int getTitleColor() {
        return titleColor;
    }

    public ToolBarConfig setTitleColor(int titleColor) {
        this.titleColor = titleColor;
        return this;
    }

    public int getTitleColorResId() {
        return titleColorResId;
    }

    public ToolBarConfig setTitleColorResId(int titleColorResId) {
        this.titleColorResId = titleColorResId;
        return this;
    }
}
