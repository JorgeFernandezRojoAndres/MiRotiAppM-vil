package com.jorge.mirotimobile.util;

import androidx.annotation.Nullable;

public class Event<T> {
    private final T content;
    private boolean handled = false;

    public Event(T content) {
        this.content = content;
    }

    @Nullable
    public synchronized T getContentIfNotHandled() {
        if (handled) return null;
        handled = true;
        return content;
    }

    public T peekContent() {
        return content;
    }
}

