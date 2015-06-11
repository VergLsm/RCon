package org.verg.rcon;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.widget.Toast;

/**
 * Created by verg on 15/5/24.
 */
public class Splash extends Activity {
    private ServiceConnection conn;

    private class GoServiceConnection implements ServiceConnection {

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            // Service被杀短开才调用这个
        }

        @Override
        public void onServiceConnected(ComponentName arg0, IBinder ibinder) {
            // Service.onBind()有返回一个IBinder实例时调用
            GoService goService = ((GoService.GoBinder) ibinder).getService();
            if (goService.cheakDevice()) {
                if (goService.isEnableBT()) {
                    clear();
                } else {
                    goService.setOnDataChangeListener(new CallbackInterface());
                    goService.enableBT();
                    Toast.makeText(Splash.this, "正在自动打开蓝牙", Toast.LENGTH_SHORT)
                            .show();
                }
            } else {
                Toast.makeText(Splash.this, "本机没有蓝牙设备。", Toast.LENGTH_LONG)
                        .show();
                finish();
            }
        }
    }

    private class CallbackInterface extends Callback {

        @Override
        public void localStatusChanged(boolean isEnable) {
            // TODO 自动生成的方法存根
            if (isEnable) {
                clear();
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        conn = new GoServiceConnection();
        Intent intent = new Intent(this, GoService.class);
        bindService(intent, conn, Context.BIND_AUTO_CREATE);
        Toast.makeText(Splash.this, "使用期间请不要关闭蓝牙", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onBackPressed() {
        finish();
    }

    private void clear() {
        Intent intent = new Intent(Splash.this, MainActivity.class);
        startActivity(intent);
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbindService(conn);
    }

}