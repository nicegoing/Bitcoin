package com.example.puhanhui.bitcoin;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;

import butterknife.BindView;
import butterknife.ButterKnife;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity {

    @BindView(R.id.et_low_price)
    EditText etLowPrice;
    @BindView(R.id.switch_low)
    Switch switchLow;
    @BindView(R.id.et_high_price)
    EditText etHighPrice;
    @BindView(R.id.switch_high)
    Switch switchHigh;
    @BindView(R.id.tv_info)
    TextView tvInfo;
    @BindView(R.id.tv_low_price)
    TextView tvLowPrice;
    @BindView(R.id.tv_high_price)
    TextView tvHighPrice;
    OkHttpClient mOkHttpClient = new OkHttpClient();
    private Timer timer;
    private TimerTask task;

    Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            //
            Ticker ticker = (Ticker) msg.obj;
            tvInfo.setText("当前价格：" + ticker.last + "\n"
                    + "当前买入价：" + ticker.buy + "\n"
                    + "当前卖出价：" + ticker.sell + "\n"
                    + "今日最低价：" + ticker.low + "\n"
                    + "今日最高价：" + ticker.high);
            alertUse(ticker);
        }
    };
    private double lowPrice;//期待最低价
    private double highPrice;//期待最高价

    /**
     * 提醒用户
     *
     * @param ticker
     */
    private void alertUse(Ticker ticker) {
        if (switchLow.isChecked()) {
            Log.d("MainActivity", "lowPrice=" + lowPrice + ";last=" + ticker.last);

            if (lowPrice > 0 && lowPrice >= ticker.last) {
                Toast.makeText(MainActivity.this, "可以买了", Toast.LENGTH_SHORT).show();
                Log.d("MainActivity", "可以买了");
            }
        }
        if (switchHigh.isChecked()) {
            if (highPrice != 0 && highPrice <= ticker.last) {
                Toast.makeText(MainActivity.this, "可以卖了", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        getInfo();
        initEvent();
    }

    private void initEvent() {

        switchLow.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (b) {
                    //开启定时监控
                    String low = etLowPrice.getText().toString().trim();
                    lowPrice = TextUtils.isEmpty(low) ? 0 : Double.valueOf(low);
                    tvLowPrice.setText("期望最低价:" + lowPrice);
                    if (!switchHigh.isChecked()) {
                        startMonitor();
                    }
                } else {
                    //关闭定时任务
                    tvLowPrice.setText("期望最低价:");
                    lowPrice = 0;
                    if (!switchHigh.isChecked()) {
                        timer.cancel();
                        timer.purge();
                    }
                }
            }
        });


        switchHigh.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (b) {
                    //开启定时监控
                    String high = etHighPrice.getText().toString().trim();
                    highPrice = TextUtils.isEmpty(high) ? 0 : Double.valueOf(high);
                    tvHighPrice.setText("期望最高价:" + lowPrice);
                    if (!switchLow.isChecked()) {
                        startMonitor();
                    }

                } else {
                    //关闭定时任务
                    highPrice = 0;
                    tvHighPrice.setText("期望最高价:");
                    if (!switchLow.isChecked()) {
                        timer.cancel();
                        timer.purge();
                    }
                }
            }
        });

    }

    /**
     * 监控任务开启
     */
    private void startMonitor() {
        timer = new Timer();
        task = new TimerTask() {

            @Override
            public void run() {
                getInfo();
            }
        };
        timer.schedule(task, 0, 10000);
    }

    /**
     * 获取dash币信息
     */
    private void getInfo() {
        final Request request = new Request.Builder()
                .url("http://api.btc38.com/v1/ticker.php?c=dash&mk_type=cny")
                .build();
        Call call = mOkHttpClient.newCall(request);
        call.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {

            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String str = response.body().string();
                try {
                    JSONObject jsonObject = new JSONObject(str);
                    String tickerStr = jsonObject.optJSONObject("ticker").toString();
                    Gson gson = new Gson();
                    Ticker ticker = gson.fromJson(tickerStr, Ticker.class);
                    Message message = Message.obtain();
                    message.obj = ticker;
                    handler.sendMessage(message);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }

}
