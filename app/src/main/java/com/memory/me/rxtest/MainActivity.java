package com.memory.me.rxtest;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.memory.me.rxtest.rx.Subscriber2Ob;

import java.util.concurrent.TimeUnit;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;
import rx.Observable;
import rx.functions.Func1;

public class MainActivity extends AppCompatActivity {

    @InjectView(R.id.button)
    Button mButton;
    @InjectView(R.id.button2)
    Button mButton2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.inject(this);

    }

    @OnClick({R.id.button})
    public void click(View view) {
        int id = view.getId();
        switch (id) {
            case R.id.button:
                method1();
                break;
        }

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
