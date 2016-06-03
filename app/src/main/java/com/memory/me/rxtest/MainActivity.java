package com.memory.me.rxtest;

import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.Toast;

import com.jakewharton.rxbinding.view.RxView;
import com.memory.me.rxtest.rx.Subscriber2Ob;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnCheckedChanged;
import butterknife.OnClick;
import rx.Observable;
import rx.Subscriber;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.functions.Func1;

public class MainActivity extends AppCompatActivity {

    @InjectView(R.id.timer)
    TextView mTimer;
    Subscription timerOb;
    Observable o;
    int count;
    @InjectView(R.id.f1)
    CheckBox mF1;
    @InjectView(R.id.f2)
    CheckBox mF2;
    @InjectView(R.id.f3)
    CheckBox mF3;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.inject(this);
        o = Observable.interval(1, TimeUnit.SECONDS);

        RxView.clicks(mTimer).subscribe(new Action1<Void>() {
            @Override
            public void call(Void aVoid) {
                if (timerOb == null) {
                    timerOb = o.observeOn(AndroidSchedulers.mainThread())
                            .subscribe(new Action1<Long>() {
                                @Override
                                public void call(Long aLong) {
                                    mTimer.setText("时间" + aLong);
                                }
                            });

                } else {
                    timerOb.unsubscribe();
                    timerOb = null;
                }

            }
        });

//        mButton3.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                retryData();
//            }
//        });


    }

    @OnClick({R.id.button, R.id.button2, R.id.button3, R.id.button4})
    public void click(View view) {
        int id = view.getId();
        switch (id) {
            case R.id.button:
                method1();
                break;
            case R.id.button2:
                retry();
                break;
            case R.id.button3:
                String path = Environment.getExternalStorageDirectory().getPath();
                File file = new File(path + "/hello.txt");
                if (!file.exists()) {
                    try {
                        file.createNewFile();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                break;
            case R.id.button4:
                flatmapStream();
                break;

        }

    }

    boolean f1 = true;
    boolean f2 = true;
    boolean f3 = true;

    /**
     * 测试complete机制
     */
    private void flatmapStream() {
        Observable.just(100l)
                .flatMap(new Func1<Long, Observable<String>>() {

                    @Override
                    public Observable<String> call(final Long aLong) {
                        return Observable.create(new Observable.OnSubscribe<String>() {
                            @Override
                            public void call(Subscriber<? super String> subscriber) {
                                subscriber.onNext(aLong + "f1-next");
                                if (f1) {
                                    subscriber.onCompleted();
                                }
                            }
                        });
                    }
                })
                .flatMap(new Func1<String, Observable<String>>() {
                    @Override
                    public Observable<String> call(final String s) {
                        return Observable.create(new Observable.OnSubscribe<String>() {
                            @Override
                            public void call(Subscriber<? super String> subscriber) {
                                subscriber.onNext(s + "f2-next");
                                if (f2) {
                                    subscriber.onCompleted();
                                }
                            }
                        });
                    }
                })
                .flatMap(new Func1<String, Observable<String>>() {
                    @Override
                    public Observable<String> call(final String s) {
                        return Observable.create(new Observable.OnSubscribe<String>() {
                            @Override
                            public void call(Subscriber<? super String> subscriber) {
                                subscriber.onNext(s + "f3-next");
                                if (f3) {
                                    subscriber.onCompleted();
                                }
                            }
                        });
                    }
                })
                .subscribe(new Subscriber<String>() {
                    @Override
                    public void onCompleted() {
                        Log.e("sub-complete", "=====");
                    }

                    @Override
                    public void onError(Throwable e) {

                    }

                    @Override
                    public void onNext(String s) {
                        Log.e("sub-next", s);
                    }
                });


    }

    @OnCheckedChanged(R.id.f1)
    public void switchComplete1() {
        f1 = mF1.isChecked();
    }

    @OnCheckedChanged(R.id.f2)
    public void switchComplete2() {
        f2 = mF2.isChecked();
    }

    @OnCheckedChanged(R.id.f3)
    public void switchComplete3() {
        f3 = mF3.isChecked();
    }


    public void retry() {
        retryData().retry().subscribe(new Subscriber() {
            @Override
            public void onCompleted() {
                Toast.makeText(MainActivity.this, "test:" + count, Toast.LENGTH_LONG).show();
            }

            @Override
            public void onError(Throwable e) {

            }

            @Override
            public void onNext(Object o) {

            }
        });

    }

    public Observable retryData() {
        return Observable.create(new Observable.OnSubscribe<Integer>() {

            @Override
            public void call(Subscriber<? super Integer> subscriber) {
                count++;
                if (count < 5) {
                    subscriber.onError(new NullPointerException());
                } else {
                    subscriber.onCompleted();
                }
            }
        });
    }


    /**
     * 把两个ob链接到一起,然后订阅到一个sub里,然后sub中针对ob1,ob2 分别处理
     * ob1,ob2 不同时发送,使用场景 在同一个sub中处理不同的ob
     */
    private void method1() {
        Observable ob1 = Observable.interval(1, TimeUnit.SECONDS).map(new Func1() {
            @Override
            public Object call(Object o) {
                return "ob1-" + o;
            }
        });

        Observable ob2 = Observable.interval(3, TimeUnit.SECONDS).map(new Func1() {
            @Override
            public Object call(Object o) {
                return "ob2-" + o;
            }
        });


        ob1.mergeWith(ob2).subscribe(new Subscriber2Ob() {

            @Override
            public void log1(String str) {
                Log.e("log1", "my name is log1:" + str);
            }

            @Override
            public void log2(String str) {
                Log.e("log2", "my name is log2:" + str);
            }
        });


    }


}
