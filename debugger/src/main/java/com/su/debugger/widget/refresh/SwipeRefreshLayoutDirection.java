package com.su.debugger.widget.refresh;

/**
 * Created by su on 16-4-18.
 */
public enum SwipeRefreshLayoutDirection {

    DISABLE(-1),
    TOP(0),
    BOTTOM(1),
    BOTH(2);

    private int mValue;

    SwipeRefreshLayoutDirection(int value) {
        this.mValue = value;
    }

    public static SwipeRefreshLayoutDirection getFromInt(int value) {
        for (SwipeRefreshLayoutDirection direction : SwipeRefreshLayoutDirection.values()) {
            if (direction.mValue == value) {
                return direction;
            }
        }
        return BOTH;
    }
}
