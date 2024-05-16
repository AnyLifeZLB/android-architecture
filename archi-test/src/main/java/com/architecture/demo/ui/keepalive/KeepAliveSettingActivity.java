package com.architecture.demo.ui.keepalive;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.anylife.keepalive.h5.H5KeepAliveGuideActivity;
import com.anylife.keepalive.utils.BatteryUtils;
import com.anylife.keepalive.utils.KeepCompactUtil;
import com.architecture.demo.R;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * 保活设置，这里处理的数据移动到其他地方去，不要和Activity 绑定
 * 单独使用一个Service 来验证吧，
 */
public class KeepAliveSettingActivity extends AppCompatActivity {

    private Socket socket;
    private TextView ignoringBatteryOptiTextView;
    private boolean isDestroy = false;

    private boolean isIgnoringBatteryOptimizations=false;

    @Override
    protected void onResume() {
        super.onResume();

        isIgnoringBatteryOptimizations=BatteryUtils.isIgnoringBatteryOptimizations(this);

        if(isIgnoringBatteryOptimizations){
            ignoringBatteryOptiTextView.setText("已优化");
            ignoringBatteryOptiTextView.setBackgroundResource(R.drawable.shape_green_btn_bg);
        }else {
            ignoringBatteryOptiTextView.setText("去设置");
            ignoringBatteryOptiTextView.setBackgroundResource(R.drawable.shape_blue_btn_bg);
        }

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_keep_alive_setting);
        setTitle("App运行设置");



        ignoringBatteryOptiTextView=findViewById(R.id.keep_alive_battery_set);
        ignoringBatteryOptiTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!isIgnoringBatteryOptimizations){
                    BatteryUtils.requestIgnoreBatteryOptimizations(KeepAliveSettingActivity.this);
                }
            }
        });

        findViewById(R.id.keep_alive_tips_background_set).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                H5KeepAliveGuideActivity.Companion.startWebView(getBaseContext(),"file:///android_asset/keepalive_guide/html/mi_cn.html");

//                RestartSettingUtils.setReStartAction(getBaseContext());
            }
        });


        findViewById(R.id.battery_set_background_set).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                KeepCompactUtil.INSTANCE.noSleepSet(KeepAliveSettingActivity.this);
//                BatteryUtils.addWhiteList(KeepAliveSettingActivity.this);
            }
        });



        createSocket();
    }


















    /**
     * 创建Socket 链接
     */
    private void createSocket() {
        new Thread(() -> {
            try {
                //1、创建客户端Socket，指定服务器地址和端口
                socket = new Socket("10.39.169.59", 8886);

                while (!isDestroy) {
                    //2、获取输出流，向服务端发送信息
                    OutputStream os = socket.getOutputStream();//字节输出流
                    PrintWriter pw = new PrintWriter(os);//将输出流包装为打印流

                    //3，收集手机型号和时间。加个时间戳网络活动可能受限制，看看本地Log 有没有问题和间隔时间统计
                    pw.println(Build.BRAND + ": Hello Server " + stampToDate(System.currentTimeMillis()));
                    Log.e("TAG", "发送 hello Server a");
                    pw.flush();


                    //4.获取输入流，并读取服务器端的响应信息
                    InputStream is = socket.getInputStream();
                    BufferedReader br = new BufferedReader(new InputStreamReader(is));
                    String info = br.readLine();
                    if (info != null && info.length() != 0) {
                        // 循环读取客户端的信息
                        Log.e("Socket", "收到服务器消息：" + info);
                    }

                    Thread.sleep(2000);
                }

            } catch (IOException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            } finally {
                System.out.println("-- finally error -----------");
            }

        }).start();
    }


    /*
     * 将时间戳转换为时间
     *
     */
    public String stampToDate(long s) {
        String res;
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date date = new Date(s);
        res = simpleDateFormat.format(date);
        return res;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        isDestroy = true;
    }

}