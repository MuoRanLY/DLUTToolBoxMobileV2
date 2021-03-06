package com.Shirai_Kuroko.DLUTMobile.Helpers;

import android.annotation.SuppressLint;
import android.app.UiModeManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatDelegate;
import androidx.preference.PreferenceManager;

import com.Shirai_Kuroko.DLUTMobile.Entities.ApplicationConfig;
import com.Shirai_Kuroko.DLUTMobile.Entities.GridAppID;
import com.Shirai_Kuroko.DLUTMobile.Entities.IDPhotoResult;
import com.Shirai_Kuroko.DLUTMobile.Entities.LoginResponseBean;
import com.Shirai_Kuroko.DLUTMobile.Entities.NotificationHistoryDataBaseBean;
import com.Shirai_Kuroko.DLUTMobile.Entities.UserScoreBean;
import com.Shirai_Kuroko.DLUTMobile.Managers.MsgHistoryManager;
import com.alibaba.fastjson.JSON;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class ConfigHelper {

    public static ArrayList<ApplicationConfig> Getmlist(Context context) {
        String defconfig = GetDefaultConfigString(context);
        ArrayList<ApplicationConfig> mList;
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        String acfjson = prefs.getString("APPCONFIG", defconfig);
        if (Objects.equals(acfjson, ""))
        {
            acfjson=defconfig;
        }
        List<ApplicationConfig> jsonlist = JSON.parseArray(acfjson, ApplicationConfig.class);
        ApplicationConfig[] acfs = new ApplicationConfig[0];
        if (jsonlist != null) {
            acfs = jsonlist.toArray(new ApplicationConfig[0]);
        }
        mList = new ArrayList<>(Arrays.asList(acfs));
        return mList;
    }

    public static String GetDefaultConfigString(Context context) {
        StringBuilder termsString = new StringBuilder();
        BufferedReader reader;
        try {
            reader = new BufferedReader(
                    new InputStreamReader(context.getAssets().open("defconfig")));

            String str;
            while ((str = reader.readLine()) != null) {
                termsString.append(str);
            }

            reader.close();
            return termsString.toString();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static void addsubscription(Context context, int appnumid) {
        ArrayList<ApplicationConfig> mlist = Getmlist(context);
        mlist.get(appnumid).setIssubscription(1);
        String json = JSON.toJSONString(mlist);
        SavePrefJson(context, json);
        AddtoGrid(context, appnumid);
        Toast.makeText(context, Getmlist(context).get(appnumid).getAppName() + "????????????", Toast.LENGTH_SHORT).show();
    }

    public static void removesubscription(Context context, int appnumid) {
        ArrayList<ApplicationConfig> mlist = Getmlist(context);
        mlist.get(appnumid).setIssubscription(0);
        String json = JSON.toJSONString(mlist);
        SavePrefJson(context, json);
        DeleteFromGrid(context, appnumid);
        Toast.makeText(context, Getmlist(context).get(appnumid).getAppName() + "????????????", Toast.LENGTH_SHORT).show();
    }

    public static void SavePrefJson(Context context, String json) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        prefs.edit().putString("APPCONFIG", json).apply();
    }

    public static void SaveGridPrefJson(Context context, String json) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        prefs.edit().putString("HomeGridConfig", json).apply();
    }

    public static boolean GetThemeType(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        boolean pref = prefs.getBoolean("Dark", false);
        int app = AppCompatDelegate.getDefaultNightMode();
        if (app == 1) {
            return false;
        } else if (app == 2) {
            return true;
        }
        if(app==-1)
        {
            UiModeManager uiModeManager = (UiModeManager) context.getSystemService(Context.UI_MODE_SERVICE);
            if (uiModeManager.getNightMode()==UiModeManager.MODE_NIGHT_YES) {
                return true;
            }
        }
        return pref;
    }

    public static void AddtoGrid(Context context, int id) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        String GridConfig = prefs.getString("HomeGridConfig", "[]");
        List<GridAppID> jsonlist = JSON.parseArray(GridConfig, GridAppID.class);
        for (GridAppID ga : jsonlist) {
            if (ga.getId().equals(id)) {
                return;
            }
        }
        jsonlist.add(new GridAppID(id));
        String json = JSON.toJSONString(jsonlist);
        SaveGridPrefJson(context, json);
    }

    public static void DeleteFromGrid(Context context, int id) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        String GridConfig = prefs.getString("HomeGridConfig", "[]");
        List<GridAppID> jsonlist = JSON.parseArray(GridConfig, GridAppID.class);
        for (int i = 0; i < jsonlist.size(); i++) {
            if (jsonlist.get(i).getId().equals(id)) {
                jsonlist.remove(i);
                i--;
            }
        }
        String json = JSON.toJSONString(jsonlist);
        SaveGridPrefJson(context, json);
    }

    public static ArrayList<GridAppID> GetGridIDList(Context context) {
        ArrayList<GridAppID> mList;
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        String GridConfig = prefs.getString("HomeGridConfig", "[]");
        List<GridAppID> jsonlist = JSON.parseArray(GridConfig, GridAppID.class);
        GridAppID[] gais = jsonlist.toArray(new GridAppID[0]);
        mList = new ArrayList<>(Arrays.asList(gais));
        return mList;
    }

    public static List<NotificationHistoryDataBaseBean> GetNotificationHistoryList(Context context) {
        List<NotificationHistoryDataBaseBean> list = new MsgHistoryManager(context).select();
        return list;
    }

    public static void SaveDebugInfoPrefJson(Context context, String json) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        prefs.edit().putString("DebugInfo", json + prefs.getString("DebugInfo", "DebugInfo:")).apply();
    }

    public static String GetCatogoryName(String cat) {
        if (cat.equals("study")) {
            return "?????????";
        }
        if (cat.equals("office")) {
            return "?????????";
        }
        if (cat.equals("life")) {
            return "?????????";
        }
        if (cat.equals("social")) {
            return "?????????";
        }
        if (cat.equals("game")) {
            return "?????????";
        }
        return "????????????";
    }

    @SuppressLint("ApplySharedPref")
    public static void SaveLoginResultToPref(String result, Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        prefs.edit().putString("UserBean", result).apply();
    }

    public static Boolean NeedLogin(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        return Objects.equals(prefs.getString("UserBean", ""), "");
    }

    public static LoginResponseBean GetUserBean(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        String Json = prefs.getString("UserBean", "");
        LoginResponseBean loginResponseBean = new LoginResponseBean();
        loginResponseBean = JSON.parseObject(Json, LoginResponseBean.class);
        return loginResponseBean;
    }

    public static UserScoreBean GetUserScoreBean(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        String Json = prefs.getString("UserScoreBean", "");
        UserScoreBean userScoreBean = new UserScoreBean();
        userScoreBean = JSON.parseObject(Json, UserScoreBean.class);
        return userScoreBean;
    }

    public static void SaveUserScoreBean(Context context, String json) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        prefs.edit().putString("UserScoreBean", json).apply();
    }

    public static IDPhotoResult GetIDPhoto(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        String Json = prefs.getString("IDPhotoResult", "");
        IDPhotoResult idPhotoResult = new IDPhotoResult();
        idPhotoResult = JSON.parseObject(Json, IDPhotoResult.class);
        return idPhotoResult;
    }

    public static void SaveIDPhoto(Context context, String json) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        prefs.edit().putString("IDPhotoResult", json).apply();
    }

    public static boolean FPStatus(Context context)
    {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        return prefs.getBoolean("FP", false);
    }

    public static void AgreeFP(Context context)
    {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        prefs.edit().putBoolean("FP",true).apply();
    }
}
