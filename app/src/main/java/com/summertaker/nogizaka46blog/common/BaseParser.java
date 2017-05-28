package com.summertaker.nogizaka46blog.common;

public class BaseParser {

    public String TAG;

    public BaseParser() {
        TAG = "== " + this.getClass().getSimpleName();
    }
}