package com.designpatterns.observer;

public interface Listener<T> {
    void update(T data);
}
