package com.inbrain.sdk.config;

import java.io.Serializable;

public class StatusBarConfig implements Serializable {
    private Boolean lightStatusBarIcons;
    private int statusBarColorResId;
    private int statusBarColor;

    public Boolean isStatusBarIconsLight() {
        return lightStatusBarIcons;
    }

    public StatusBarConfig setStatusBarIconsLight(boolean lightStatusBarIcons) {
        this.lightStatusBarIcons = lightStatusBarIcons;
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
