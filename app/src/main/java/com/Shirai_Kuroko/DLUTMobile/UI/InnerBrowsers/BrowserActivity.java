package com.Shirai_Kuroko.DLUTMobile.UI.InnerBrowsers;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ClipData;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.webkit.CookieManager;
import android.webkit.JavascriptInterface;
import android.webkit.JsResult;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.pm.ShortcutInfoCompat;
import androidx.core.content.pm.ShortcutManagerCompat;
import androidx.core.graphics.drawable.IconCompat;
import androidx.preference.PreferenceManager;

import com.Shirai_Kuroko.DLUTMobile.Entities.ApplicationConfig;
import com.Shirai_Kuroko.DLUTMobile.Entities.LoginResponseBean;
import com.Shirai_Kuroko.DLUTMobile.Helpers.ConfigHelper;
import com.Shirai_Kuroko.DLUTMobile.Helpers.QRCodeHelper;
import com.Shirai_Kuroko.DLUTMobile.R;
import com.Shirai_Kuroko.DLUTMobile.UI.InnerBrowsers.SDK.BaseActivity;
import com.Shirai_Kuroko.DLUTMobile.UI.InnerBrowsers.SDK.BrowserProxy;
import com.Shirai_Kuroko.DLUTMobile.UI.InnerBrowsers.SDK.WebDownloadListener;
import com.Shirai_Kuroko.DLUTMobile.Utils.MobileUtils;
import com.Shirai_Kuroko.DLUTMobile.Widgets.LoadingView;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.transition.Transition;

import java.util.Date;
import java.util.Objects;

@SuppressWarnings("ALL")
public class BrowserActivity extends BaseActivity {

    int numid = 0;
    ApplicationConfig thisapp;
    private WebView webView;
    private LoadingView loading;
    private Context mContext;

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        WebView.enableSlowWholeDocumentDraw();
        setContentView(R.layout.activity_browser);
        mContext = this;
        Intent intent = getIntent();
        String id = intent.getStringExtra("App_ID");
        numid = Integer.parseInt(id);
        thisapp = ConfigHelper.Getmlist(this).get(numid);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setDisplayShowTitleEnabled(true);
            actionBar.setTitle(thisapp.getAppName());
        }
        SyncCookie(this);
        webView = findViewById(R.id.BrowserWebView);
        loading = new LoadingView(this, R.style.CustomDialog);
        if (!thisapp.getUrl().contains("rj")) {
            loading.show();
        }
        BrowserProxy browserProxy = new BrowserProxy(this, webView);
        webView.addJavascriptInterface(browserProxy, "__nativeWhistleProxy");
        webView.addJavascriptInterface(new PicShareInterFace(), "Share");
        webView.setWebChromeClient(this.webChromeClient);
        webView.setWebViewClient(this.webViewClient);
        webView.setDownloadListener(new WebDownloadListener(this));
        WebSettings webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true);//????????????js
        switch (numid) {
            case 24: {
                webSettings.setUserAgentString(getString(R.string.PCUserAgent));//???????????????????????????PCUserAgent
                break;
            }
            default: {
                webSettings.setUserAgentString(getString(R.string.UserAgent));//????????????UA
                break;
            }
        }
        webSettings.setCacheMode(WebSettings.LOAD_NO_CACHE);//??????????????????????????????????????????.
        //??????????????????
        webSettings.setSupportZoom(true);
        webSettings.setBuiltInZoomControls(false);
        //??????????????????
        webSettings.setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);
        webSettings.setDomStorageEnabled(true);
        //????????????????????????
        webSettings.setTextZoom(100);
        webSettings.setJavaScriptCanOpenWindowsAutomatically(true);
        webSettings.setPluginState(WebSettings.PluginState.ON);
        webSettings.setAllowFileAccess(true);
        webSettings.setGeolocationEnabled(true);
        webSettings.setSupportMultipleWindows(true);
        webView.setDrawingCacheEnabled(true);
        webSettings.setSupportZoom(true);
        webSettings.setSavePassword(false);
        webSettings.setUseWideViewPort(true);
        webSettings.setLoadWithOverviewMode(true);
        webSettings.setAllowContentAccess(true);
        webSettings.setDatabaseEnabled(true);
        webSettings.setAllowFileAccessFromFileURLs(true);
        webSettings.setAllowUniversalAccessFromFileURLs(true);
        webSettings.setJavaScriptCanOpenWindowsAutomatically(true);
        webSettings.setSupportMultipleWindows(false);
        if (ConfigHelper.GetThemeType(this)) { //?????????????????????????????????
            webSettings.setForceDark(WebSettings.FORCE_DARK_ON);//????????????webview??????????????????
        } else {
            webSettings.setForceDark(WebSettings.FORCE_DARK_OFF);
        }
        //????????????
        webView.setBackgroundColor(0); // ???????????????
        webView.getBackground().setAlpha(0); // ??????????????? ?????????0-255
        if (thisapp.getId() != 66) {
            webView.loadUrl(thisapp.getUrl());
        } else {
            webView.loadUrl("https://news.dlut.edu.cn/ttgz.htm");
        }
        SyncCookie(this);
    }

    public void SyncCookie(Context context) {
        try {
            final CookieManager instance = CookieManager.getInstance();
            instance.setAcceptCookie(true);
            LoginResponseBean UserBean = ConfigHelper.GetUserBean(context);
            if (UserBean == null) {
                return;
            }
            LoginResponseBean.DataDTO.MyInfoDTO infoDTO = UserBean.getData().getMy_info();
            String skey = infoDTO.getSkey();
            final StringBuilder sb = new StringBuilder();
            sb.append("whistlekey");
            sb.append('=');
            sb.append(skey);
            instance.setCookie(".dlut.edu.cn", sb.toString());
            instance.setCookie("api.dlut.edu.cn", sb.toString());
            instance.setCookie("webvpn.dlut.edu.cn", sb.toString());
            instance.setCookie("sso.dlut.edu.cn", sb.toString());
            final StringBuilder sb3 = new StringBuilder();
            sb3.append(UserBean.getData().getTgtinfo().get(0).getName());
            sb3.append("=");
            sb3.append(UserBean.getData().getTgtinfo().get(0).getValue());
            sb3.append("; Max-Age=");
            sb3.append("3600");
            instance.setCookie(".dlut.edu.cn", sb3.toString());
            instance.setCookie("api.dlut.edu.cn", sb3.toString());
            instance.setCookie("webvpn.dlut.edu.cn", sb3.toString());
            instance.setCookie("sso.dlut.edu.cn", sb3.toString());
            instance.flush();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //WebViewClient????????????WebView?????????????????????????????????
    public final WebViewClient webViewClient = new WebViewClient() {
        @Override
        public void onPageFinished(WebView view, String url) {//??????????????????
            Log.i("????????????", url);
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
            String Un = prefs.getString("Username", "");
            String Pd = prefs.getString("Password", "");
            final boolean b = Un.length() * Pd.length() != 0;
            if (url.contains("sso.dlut.edu.cn/cas/login?service=") || url.contains("webvpn.dlut.edu.cn%2Flogin%3Fcas_login%3Dtrue"))//sso????????????
            {
                if (b) {
                    Toast.makeText(getBaseContext(), "?????????????????????????????????", Toast.LENGTH_SHORT).show();
                    view.evaluateJavascript("$(\"#un\").val('" + Un + "');$(\"#pd\").val('" + Pd + "');login()", value -> {
                    });
                } else {
                    AlertDialog.Builder localBuilder = new AlertDialog.Builder(webView.getContext());
                    localBuilder.setMessage("??????????????????????????????????????????????????????????????????????????????????????????????????????").setPositiveButton("??????", null);
                    localBuilder.setCancelable(false);
                    localBuilder.create().show();
                }
                return;
            }
            if (url.contains("api.m.dlut.edu.cn/login?client_id="))//api????????????
            {
                LoginResponseBean.DataDTO.MyInfoDTO myInfoDTO = ConfigHelper.GetUserBean(getBaseContext()).getData().getMy_info();
                if (myInfoDTO != null) {
                    if (myInfoDTO.getSkey() != null) {
                        view.evaluateJavascript("getSsoKey('" + myInfoDTO.getSkey().replace("%3D", "") + "')", value -> {
                        });
                    }
                } else {
                    if (b) {
                        Toast.makeText(getBaseContext(), "?????????????????????????????????", Toast.LENGTH_SHORT).show();
                        view.evaluateJavascript("username.value='" + Un + "';password.value='" + Pd + "';submit.disabled='';submit.click()", value -> {
                        });
                    } else {
                        AlertDialog.Builder localBuilder = new AlertDialog.Builder(webView.getContext());
                        localBuilder.setMessage("??????????????????????????????????????????????????????????????????????????????????????????????????????").setPositiveButton("??????", null);
                        localBuilder.setCancelable(false);
                        localBuilder.create().show();
                    }
                }
                return;
            }
            if (url.contains("webvpn.dlut.edu.cn/login"))//webvpn????????????
            {
                view.evaluateJavascript("document.getElementById('cas-login').click()", value -> {
                });
                return;
            }
            SpecialHandle(url);
        }

        @Override
        public void onPageStarted(WebView view, String url, Bitmap favicon) {//??????????????????
            Log.i("????????????", url);//?????????????????????????????????
            switch (thisapp.getId()) {
                case 70://????????????????????????
                {
                    if (url.contains("/student/home")) {
                        webView.evaluateJavascript("window.location.href='/student/for-std/course-select'", value -> {
                        });
                    }
                    break;
                }
                case 71://????????????????????????
                {
                    if (url.contains("/student/home")) {
                        webView.getSettings().setLoadWithOverviewMode(false);
                        webView.getSettings().setUseWideViewPort(false);
                        webView.evaluateJavascript("window.location.href='/student/for-std/exam-arrange'", value -> {
                        });
                    }
                    break;
                }
                case 73://????????????????????????
                {
                    if (url.contains("/student/home")) {
                        webView.getSettings().setLoadWithOverviewMode(false);
                        webView.getSettings().setUseWideViewPort(false);
                        webView.evaluateJavascript("window.location.href='/student/for-std/grade/sheet'", value -> {
                        });
                    }
                    break;
                }
                case 74://????????????????????????
                {
                    if (url.contains("/student/home")) {
                        webView.getSettings().setLoadWithOverviewMode(false);
                        webView.getSettings().setUseWideViewPort(false);
                        webView.evaluateJavascript("window.location.href='/student/for-std/evaluation/summative'", value -> {
                        });
                    }
                    break;
                }
                case 75://????????????????????????
                {
                    if (url.contains("/student/home")) {
                        webView.evaluateJavascript("window.location.href='/student/for-std/course-table'", value -> {
                        });
                    }
                    break;
                }
                case 76://????????????????????????
                {
                    if (url.contains("/student/home")) {
                        webView.evaluateJavascript("window.location.href='/student/for-std/adminclass-course-table'", value -> {
                        });
                    }
                    break;
                }
                case 77://????????????????????????
                {
                    if (url.contains("/student/home")) {
                        webView.evaluateJavascript("window.location.href='/student/for-std/program-completion-preview'", value -> {
                        });
                    }
                    break;
                }
                case 79://????????????????????????
                {
                    if (url.contains("/student/home")) {
                        webView.evaluateJavascript("window.location.href='/student/for-std/lesson-search'", value -> {
                        });
                    }
                    break;
                }
                default://?????????????????????????????????
                {
                    if (!url.contains("https://api.m.dlut.edu.cn/login?")) {
                        loading.show();//???????????????
                    }
                    break;
                }
            }
        }

        @Override
        public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
            String url = request.getUrl().toString();
            //??????????????????????????????
            if (url.startsWith("weixin://") || url.startsWith("alipays://") || url.startsWith("upwrp://")) {
                Intent intent = new Intent();
                intent.setAction(Intent.ACTION_VIEW);
                intent.setData(request.getUrl());
                startActivity(intent);
                return true;
            }
            if (!url.startsWith("http://") && !url.startsWith("https://")) {
                Intent intent = new Intent();
                intent.setAction(Intent.ACTION_VIEW);
                intent.setData(request.getUrl());
                startActivity(intent);
                return true;
            }
            return super.shouldOverrideUrlLoading(view, request);
        }
    };

    private void SpecialHandle(String url) {
        switch (thisapp.getId()) {
            case 14://??????????????????
            {
                if (url.equals("https://mail.dlut.edu.cn/coremail/common/xphone_dllg/index.jsp?locale=zh_CN")) {
                    SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
                    String MailAddress = prefs.getString("MailAddress", "");
                    String MailPassword = prefs.getString("MailPassword", "").split("@")[0];
                    if (MailAddress.length() * MailPassword.length() != 0) {
                        webView.evaluateJavascript("username.value='" + MailAddress + "'", value -> {
                        });
                        webView.evaluateJavascript("password.value='" + MailPassword + "'", value -> {
                        });
                        webView.evaluateJavascript("domain.value='mail.dlut.edu.cn'", value -> {
                        });
                        webView.evaluateJavascript("document.getElementsByClassName('loginBtn')[0].click()", value -> {
                        });
                    } else {
                        loading.dismiss();
                        AlertDialog.Builder localBuilder = new AlertDialog.Builder(webView.getContext());
                        localBuilder.setMessage("??????????????????????????????????????????????????????????????????????????????????????????????????????").setPositiveButton("??????", null);
                        localBuilder.setCancelable(false);
                        localBuilder.create().show();
                    }
                } else {
                    loading.dismiss();
                }

                break;
            }
            case 70://????????????????????????
            {
                if (url.contains("course-select")) {
                    loading.dismiss();
                }
                break;
            }
            case 71://????????????????????????
            {
                if (url.contains("exam-arrange")) {
                    loading.dismiss();
                }
                break;
            }
            case 73://????????????????????????
            {
                if (url.contains("for-std/grade/sheet")) {
                    loading.dismiss();
                }
                break;
            }
            case 74://????????????????????????
            {
                if (url.contains("evaluation")) {
                    loading.dismiss();
                }
                break;
            }
            case 75://????????????????????????
            {
                if (url.contains("course-table")) {
                    loading.dismiss();
                }
                break;
            }
            case 76://????????????????????????
            {
                if (url.contains("adminclass-course-table")) {
                    loading.dismiss();
                }
                break;
            }
            case 77://????????????????????????
            {
                if (url.contains("program-completion-preview")) {
                    loading.dismiss();
                }
                break;
            }
            case 78://??????????????????????????????
            {
                if (Objects.equals(url, "http://172.20.20.1:8800/user/operate/index")) {
                    webView.evaluateJavascript("document.getElementsByClassName('radio')[0].innerHTML='<label><input type=\"radio\" name=\"OperateForm[shiftType]\" value=\"1\"> ????????????</label><label><input type=\"radio\" name=\"OperateForm[shiftType]\" value=\"2\"> ??????????????????</label>'", value -> {
                    });
                    webView.evaluateJavascript("document.getElementsByTagName('select')[1].innerHTML+='<option value=\"13\">?????????100G</option>'", value -> {
                    });
                }
                if (Objects.equals(url, "http://172.20.20.1:8800/")) {
                    SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
                    String Un = prefs.getString("Username", "");
                    String NPd = prefs.getString("NetworkPassword", "");
                    final boolean c = Un.length() * NPd.length() != 0;
                    if (c) {
                        webView.evaluateJavascript("document.getElementById('loginform-username').value=" + Un, value -> {
                        });
                        webView.evaluateJavascript("document.getElementById('loginform-password').value='" + NPd + "'", value -> {
                        });
                    } else {
                        AlertDialog.Builder localBuilder = new AlertDialog.Builder(webView.getContext());
                        localBuilder.setMessage("?????????????????????????????????????????????????????????????????????????????????????????????????????????").setPositiveButton("??????", null);
                        localBuilder.setCancelable(false);
                        localBuilder.create().show();
                    }
                }
                break;
            }
            case 79://????????????????????????
            {
                if (url.contains("lesson-search")) {
                    loading.dismiss();
                }
                break;
            }
            default://?????????????????????????????????
            {
                loading.dismiss();
                break;
            }
        }
    }

    //WebChromeClient????????????WebView??????Javascript????????????????????????????????????title??????????????????
    public final WebChromeClient webChromeClient = new WebChromeClient() {
        //?????????js???alert???????????????????????????????????????dialog??????
        @Override
        public boolean onJsAlert(WebView webView, String url, String message, JsResult result) {
            Log.i("TAG", "onJsAlert: " + message);
            AlertDialog.Builder localBuilder = new AlertDialog.Builder(webView.getContext());
            localBuilder.setMessage(message).setPositiveButton("??????", null);
            localBuilder.setCancelable(false);
            localBuilder.create().show();
            result.confirm();
            return true;
        }

        @Override
        public boolean onShowFileChooser(WebView webView, ValueCallback<Uri[]> filePathCallback, FileChooserParams fileChooserParams) {
            Intent i = new Intent(Intent.ACTION_GET_CONTENT);
            i.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
            i.setType("*/*"); // ??????????????????
            String[] mimeTypes = fileChooserParams.getAcceptTypes();
            i.putExtra(Intent.EXTRA_MIME_TYPES, mimeTypes); // ??????????????????
            i.addCategory(Intent.CATEGORY_OPENABLE);
            startActivityForResultHere(Intent.createChooser(i, fileChooserParams.getTitle()), 2, (requestCode, resultCode, intent) -> {
                Uri[] results = null;
                if (resultCode == Activity.RESULT_OK) {
                    if (intent != null) {
                        String dataString = intent.getDataString();
                        ClipData clipData = intent.getClipData();
                        if (clipData != null) {
                            results = new Uri[clipData.getItemCount()];
                            for (int i1 = 0; i1 < clipData.getItemCount(); i1++) {
                                ClipData.Item item = clipData.getItemAt(i1);
                                results[i1] = item.getUri();
                            }
                        }
                        if (dataString != null)
                            results = new Uri[]{Uri.parse(dataString)};
                    }
                }
                filePathCallback.onReceiveValue(results);
                return false;
            });
            return true;
        }
    };

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //????????????
        webView.destroy();
        webView = null;
        CookieManager.getInstance().removeAllCookies(null);
        CookieManager.getInstance().flush();
        if (thisapp.getId() == 1) {
            Intent intent2 = new Intent("android.appwidget.action.APPWIDGET_UPDATE");
            sendBroadcast(intent2);
        }
    }

    /* ???????????? */
    public boolean onCreateOptionsMenu(Menu menu) {
        menu.add(0, 0, 0, "???????????????");
        menu.add(0, 1, 0, "???????????????");
        menu.add(0, 2, 0, "???????????????");
        menu.add(0, 3, 0, "???????????????");
        menu.add(0, 4, 0, "???????????????");
        menu.add(0, 5, 0, "???????????????");
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            this.finish();
            return true;
        }
        switch (item.getItemId()) {
            case 0: {
                webView.reload();
                return true;
            }
            case 1: {
                if (webView.getOriginalUrl().startsWith("file")) {
                    Toast.makeText(this, "????????????????????????????????????", Toast.LENGTH_SHORT).show();
                    return false;
                }
                Intent intent = new Intent();
                intent.setAction("android.intent.action.VIEW");
                Uri content_url = Uri.parse(webView.getOriginalUrl());
                intent.setData(content_url);
                startActivity(intent);
                return true;
            }
            case 2: {
                Toast.makeText(getBaseContext(),"????????????????????????",Toast.LENGTH_SHORT).show();
                webView.evaluateJavascript("window.Share.StartShare(document.getElementsByTagName('html')[0].scrollWidth,document.getElementsByTagName('html')[0].scrollHeight)", null);
                return true;
            }
            case 3: {
                MobileUtils.ShareTextToFriend(mContext,"???????????????"+webView.getOriginalUrl()+"\n???????????????"+webView.getUrl());
                return true;
            }
            case 4: {
                Toast.makeText(getBaseContext(),"????????????????????????",Toast.LENGTH_SHORT).show();
                webView.evaluateJavascript("window.Share.StartSave(document.getElementsByTagName('html')[0].scrollWidth,document.getElementsByTagName('html')[0].scrollHeight)", null);
                return true;
            }
            case 5: {
                downShortcutICon();
                return true;
            }
        }
        return super.onOptionsItemSelected(item);
    }

    public class PicShareInterFace {
        @JavascriptInterface
        public void StartShare(String s,String s1) {
            Handler handler = new Handler(Looper.getMainLooper());
            handler.post(new Runnable() {
                @Override
                public void run() {
                    float scale = webView.getScale();
                    int width = (int) (Integer.parseInt(s) * scale);
                    int height;
                    if (!webView.getOriginalUrl().startsWith("file")) {
                        height = (int) (Integer.parseInt(s1) * scale + 220);
                    } else {
                        height = (int) (Integer.parseInt(s1) * scale);
                    }
                    Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);//???????????????????????????
                    Canvas canvas = new Canvas(bitmap);
                    webView.draw(canvas);
                    if (!webView.getOriginalUrl().startsWith("file")) {
                        Bitmap qr = QRCodeHelper.createQRCodeBitmap(webView.getOriginalUrl(), 200, 200, "UTF-8", "L", "0", Color.BLACK, Color.WHITE);
                        canvas.drawBitmap(qr, 10, bitmap.getHeight() - 210, null);
                    }
                    MobileUtils.BrowserSharePictureToFriend(mContext, webView, thisapp, bitmap);
                }
            });
        }
        @JavascriptInterface
        public void StartSave(String s,String s1) {
            Handler handler = new Handler(Looper.getMainLooper());
            handler.post(() -> {
                float scale = webView.getScale();
                int width = (int) (Integer.parseInt(s) * scale);
                int height;
                if (!webView.getOriginalUrl().startsWith("file")) {
                    height = (int) (Integer.parseInt(s1) * scale + 220);
                } else {
                    height = (int) (Integer.parseInt(s1) * scale);
                }
                Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);//???????????????????????????
                Canvas canvas = new Canvas(bitmap);
                webView.draw(canvas);
                if (!webView.getOriginalUrl().startsWith("file")) {
                    Bitmap qr = QRCodeHelper.createQRCodeBitmap(webView.getOriginalUrl(), 200, 200, "UTF-8", "L", "0", Color.BLACK, Color.WHITE);
                    canvas.drawBitmap(qr, 10, bitmap.getHeight() - 210, null);
                }
                MobileUtils.SaveImageToGallery(getBaseContext(),bitmap,thisapp.getAppName()+new Date().toLocaleString()+".bmp");
            });
        }
    }

    private void downShortcutICon() {
        final Bitmap[] bitmap = new Bitmap[1]; //??????????????? ??????bitMap
        Glide.with(mContext).asBitmap().load(thisapp.getIcon()).into(new SimpleTarget() {
            @Override
            public void onResourceReady(@NonNull Object resource, @Nullable Transition transition) {
                bitmap[0] = (Bitmap) resource;
                if (bitmap[0] != null) {
                    addShortCutCompact(bitmap[0]);
                }
            }
        });
    }

    /**
     * ??????????????????
     */
    public void addShortCutCompact(Bitmap bitmap) {
        //???????????????????????????????????????
        if (ShortcutManagerCompat.isRequestPinShortcutSupported(mContext)) {
            Intent intent = getIntent();
            intent.setAction(Intent.ACTION_DEFAULT);
            intent.putExtra("Remain", false);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_NO_HISTORY);
            ShortcutInfoCompat info = new ShortcutInfoCompat.Builder(mContext, thisapp.getId() + thisapp.getAppName()) //????????????icon
                    .setIcon(IconCompat.createWithBitmap(bitmap)) //????????????
                    .setShortLabel(thisapp.getAppName())
                    .setIntent(intent)
                    .build(); //??????????????????
            ShortcutManagerCompat.requestPinShortcut(mContext, info, null);
        } else {
            Toast.makeText(mContext, "????????????????????????????????????", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onBackPressed() {
        if (webView.canGoBack()) {//???????????????????????????????????????????????????
            webView.goBack(); // goBack()????????????webView???????????????
        } else {
            if (getIntent().getBooleanExtra("Remain", true)) {
                super.onBackPressed();
            } else {
                finish();
            }
        }
    }
}