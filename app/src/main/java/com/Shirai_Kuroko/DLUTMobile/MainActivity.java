package com.Shirai_Kuroko.DLUTMobile;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.NavigationUI;
import androidx.preference.PreferenceManager;

import com.Shirai_Kuroko.DLUTMobile.Common.LogToFile;
import com.Shirai_Kuroko.DLUTMobile.Helpers.ConfigHelper;
import com.Shirai_Kuroko.DLUTMobile.Helpers.PermissionHelper;
import com.Shirai_Kuroko.DLUTMobile.Managers.AutoCleaner;
import com.Shirai_Kuroko.DLUTMobile.UI.LoginActivity;
import com.Shirai_Kuroko.DLUTMobile.Utils.BackendUtils;
import com.Shirai_Kuroko.DLUTMobile.Utils.MobileUtils;
import com.Shirai_Kuroko.DLUTMobile.databinding.ActivityMainBinding;
import com.google.android.material.badge.BadgeDrawable;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.Objects;

public class MainActivity extends AppCompatActivity {
    @SuppressLint("StaticFieldLeak")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        PermissionHelper.GetAllPermission(this);
        LocalReceiver localReceiver = new LocalReceiver();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("com.Shirai_Kuroko.DLUTMobile.ReceivedNew");
        LocalBroadcastManager.getInstance(this).registerReceiver(localReceiver, intentFilter);
        Window window = getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.setNavigationBarColor(getResources().getColor(R.color.main_theme_color));
        com.Shirai_Kuroko.DLUTMobile.databinding.ActivityMainBinding binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_activity_main);
        NavigationUI.setupWithNavController(binding.navView, navController);
        MobileUtils.CheckUpDateOnStartUp(this);
        MobileUtils.CheckConfigUpdates(this);
        if (ConfigHelper.NeedLogin(this)) {
            Log.i("????????????", "????????????");
            LogToFile.i("????????????", "????????????");
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
            String Un = prefs.getString("Username", "");
            String Pd = prefs.getString("Password", "");
            if (Un.length() * Pd.length() != 0) {
                Log.i("????????????", "????????????");
                LogToFile.i("????????????", "????????????");
                BackendUtils.Login(this, Un, Pd);
            } else {
                Log.i("??????????????????", "????????????");
                LogToFile.i("??????????????????", "????????????");
                Intent intent = new Intent(this, LoginActivity.class);
                startActivity(intent);
            }
        } else {
            Log.i("???????????????", "??????????????????");
            LogToFile.i("???????????????", "??????????????????");
            BackendUtils.ReSendUserInfo(this);
        }
        //ConfigHelper.MakeupNotificationList(this);
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        BottomNavigationView bottomNavigationView = findViewById(R.id.nav_view);
        if (prefs.getBoolean("unread", false)) {
            BadgeDrawable badgeDrawable = bottomNavigationView.getOrCreateBadge(bottomNavigationView.getMenu().getItem(1).getItemId());
            badgeDrawable.setBackgroundColor(Color.RED);
            badgeDrawable.setVisible(true);
            badgeDrawable.setNumber(prefs.getInt("unreadcount", 0));
        }
        Intent intent2 = new Intent("android.appwidget.action.APPWIDGET_UPDATE");
        sendBroadcast(intent2);
    }

    public class LocalReceiver extends BroadcastReceiver {
        public void onReceive(Context context, Intent intent) {
            Log.i("????????????", intent.getAction());
            if (Objects.equals(intent.getAction(), "com.Shirai_Kuroko.DLUTMobile.ReceivedNew")) {
                SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
                if (prefs.getBoolean("unread", false)) {
                    BottomNavigationView bottomNavigationView = findViewById(R.id.nav_view);
                    BadgeDrawable badgeDrawable = bottomNavigationView.getOrCreateBadge(bottomNavigationView.getMenu().getItem(1).getItemId());
                    badgeDrawable.setBackgroundColor(Color.RED);
                    badgeDrawable.setVisible(true);
                    badgeDrawable.setNumber(prefs.getInt("unreadcount", 0));
                }
            }
        }
    }


    private static boolean isExit = false;

    @SuppressLint("HandlerLeak")
    static Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            isExit = false;
        }
    };

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            exit();
            return false;
        }
        return super.onKeyDown(keyCode, event);
    }

    private void exit() {
        if (!isExit) {
            isExit = true;
            Toast.makeText(getApplicationContext(), "????????????????????????", Toast.LENGTH_SHORT).show();
            //??????handler??????????????????????????????
            handler.sendEmptyMessageDelayed(0, 1800);
        } else {
            AutoCleaner.Clean(this);
            finish();
        }
    }

    @Override
    protected void onDestroy() {
        AutoCleaner.Clean(this);
        super.onDestroy();
    }

    /**
     * ????????????????????????
     */
    @Override
    public Resources getResources() {
        return MobileUtils.getResources(super.getResources());
    }

    @Override
    protected void onResume() {
        super.onResume();
    }
}