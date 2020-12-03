package com.inbrain.sdk.config;

import java.io.Serializable;

public class StatusBarConfig implements Serializable {
    private Boolean lightStatusBar;
    private int statusBarColorResId;
    private int statusBarColor;

    public Boolean isLightStatusBar() {
        return lightStatusBar;
    }

    public StatusBarConfig setLightStatusBar(boolean lightStatusBar) {
        this.lightStatusBar = lightStatusBar;
        return this;
    }

    public int getStatusBarColor() {
        return statusBarColor;
    }

    public StatusBarConfig setStatusBarColor(int statusBarColor) {
        this.statusBarColor = statusBarColor;
        return this;
    }

    public int getStatusBarColorResId() {
        return statusBarColorResId;
    }

    public StatusBarConfig setStatusBarColorResId(int statusBarColorResId) {
        this.statusBarColorResId = statusBarColorResId;
        return this;
    }
}
