package com.memory.me.rxtest.rx;

import rx.Subscriber;

public abstract class Subscriber2Ob extends Subscriber<String> {
    @Override
    public void onCompleted() {

    }

    @Override
    public void onError(Throwable e) {

    }

    @Override
    final public void onNext(String t) {
        if (t.startsWith("ob1-")) {
            log1(t);
        }
        if (t.startsWith("ob2-")) {
            log2(t);
        }
    }

    public abstract void log1(String str);

    public abstract void log2(String str);
}