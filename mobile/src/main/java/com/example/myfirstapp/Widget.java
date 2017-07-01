package com.example.myfirstapp;

import android.app.Service;
import android.graphics.PixelFormat;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;

/**
 * Abstract class for all specific widget classes to extend from
 */

public abstract class Widget {
    protected Service service;
    protected WindowManager windowManager;
    protected ImageView widget;
    protected int widgetDrawableResource;

    private WindowManager.LayoutParams layoutParams;
    private int gravity;
    private int x;
    private int y;

    Widget(Service service, WindowManager windowManager) {
        this.service = service;
        this. windowManager = windowManager;
        widget = new ImageView(service);
    }

    public void setPosition(int gravity, int x, int y) {
        this.gravity = gravity;
        this.x = x;
        this.y = y;
    }

    /**
     * Displays the widget on screen
     */
    public void show() {
        layoutParams = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.TYPE_PHONE,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSLUCENT);

        widget.setImageResource(widgetDrawableResource);

        // Set position on screen
        layoutParams.gravity = this.gravity;
        layoutParams.x = this.x;
        layoutParams.y = this.y;

        windowManager.addView(widget, layoutParams);
    }

    /**
     * Removes the widget from screen
     */
    public void hide() {
        if (widget.getVisibility() == View.VISIBLE) {
            windowManager.removeView(widget);
        }
    }

    /**
     * Toggles the visibility of the widget on screen
     */
    public void toggle() {
        if(widget.getWindowToken() != null) {
            hide();
        } else {
            show();
        }
    }
}
