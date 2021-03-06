package com.Shirai_Kuroko.DLUTMobile.UI.InnerBrowsers;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ClipData;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
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
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.preference.PreferenceManager;

import com.Shirai_Kuroko.DLUTMobile.Entities.LoginResponseBean;
import com.Shirai_Kuroko.DLUTMobile.Helpers.ConfigHelper;
import com.Shirai_Kuroko.DLUTMobile.Helpers.QRCodeHelper;
import com.Shirai_Kuroko.DLUTMobile.R;
import com.Shirai_Kuroko.DLUTMobile.UI.InnerBrowsers.SDK.BaseActivity;
import com.Shirai_Kuroko.DLUTMobile.UI.InnerBrowsers.SDK.BrowserProxy;
import com.Shirai_Kuroko.DLUTMobile.UI.InnerBrowsers.SDK.WebDownloadListener;
import com.Shirai_Kuroko.DLUTMobile.Utils.MobileUtils;
import com.Shirai_Kuroko.DLUTMobile.Widgets.LoadingView;

import java.util.Date;
import java.util.Objects;

public class PureBrowserActivity extends BaseActivity {

    private WebView webView;
    private LoadingView loading;
    boolean NoTitle = false;

    @SuppressLint({"SetJavaScriptEnabled", "NewApi"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        WebView.enableSlowWholeDocumentDraw();
        setContentView(R.layout.activity_pure_browser);
        Intent intent = getIntent();
        String Url = intent.getStringExtra("Url");
        String Name = intent.getStringExtra("Name");
        webView = findViewById(R.id.PureBrowser);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setDisplayShowTitleEnabled(true);
            actionBar.setTitle(Name);
        }
        if (Objects.equals(Name, "")) {
            NoTitle = true;
        }
        loading = new LoadingView(this, R.style.CustomDialog);
        loading.show();
        webView.setWebChromeClient(this.webChromeClient);
        webView.setWebViewClient(this.webViewClient);
        webView.setDownloadListener(new WebDownloadListener(this));
        webView.addJavascriptInterface(new BrowserProxy(this, webView), "__nativeWhistleProxy");
        webView.addJavascriptInterface(new PicShareInterFace(), "Share");
        WebSettings webSettings = webView.getSettings();
        webSettings.setUserAgentString(getString(R.string.UserAgent));//????????????UA
        webSettings.setJavaScriptEnabled(true);//????????????js
        webSettings.setCacheMode(WebSettings.LOAD_NO_CACHE);//??????????????????????????????????????????.
        //??????????????????
        webSettings.setSupportZoom(true);
        webSettings.setBuiltInZoomControls(false);
        webView.setHorizontalScrollBarEnabled(false);
        //??????????????????
        webSettings.setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);
        webSettings.setDomStorageEnabled(true);
        webSettings.setJavaScriptCanOpenWindowsAutomatically(true);
        webSettings.setSupportMultipleWindows(false);
        //????????????????????????
        webSettings.setTextZoom(100);
        webView.setDrawingCacheEnabled(true);
        if (ConfigHelper.GetThemeType(this)) { //?????????????????????????????????
            webSettings.setForceDark(WebSettings.FORCE_DARK_ON);//????????????webview??????????????????
        } else {
            webSettings.setForceDark(WebSettings.FORCE_DARK_OFF);
        }
        //????????????
        webView.setBackgroundColor(Color.WHITE); // ???????????????
        webView.getBackground().setAlpha(125); // ??????????????? ?????????0-255
        SyncCookie(this);
        if (!Url.contains("rj")) {
            loading.show();
        }
        webView.loadUrl(Url);
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

    private final WebViewClient webViewClient = new WebViewClient() {
        @Override
        public void onPageFinished(WebView view, String url) {//??????????????????
            Log.i("????????????", url);
            loading.dismiss();
            if (NoTitle) {
                Objects.requireNonNull(getSupportActionBar()).setTitle(webView.getTitle());
            }
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
            webView.clearHistory();
        }

        @Override
        public void onPageStarted(WebView view, String url, Bitmap favicon) {//??????????????????
            if (!url.contains("https://api.m.dlut.edu.cn/login?")) {
                loading.show();//???????????????
            }
            Log.i("????????????", url);//?????????????????????????????????
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

    //WebChromeClient????????????WebView??????Javascript????????????????????????????????????title??????????????????
    private final WebChromeClient webChromeClient = new WebChromeClient() {
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

        //??????????????????
        @Override
        public void onReceivedTitle(WebView view, String title) {
            super.onReceivedTitle(view, title);
            if (NoTitle) {
                Objects.requireNonNull(getSupportActionBar()).setTitle(title);
            }
        }

        @Override
        public boolean onShowFileChooser(WebView webView, ValueCallback<Uri[]> filePathCallback, FileChooserParams fileChooserParams) {
            Intent i = new Intent(Intent.ACTION_GET_CONTENT);
            i.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
            i.setType("*/*"); // ??????????????????
            String[] mimeTypes = fileChooserParams.getAcceptTypes();
            i.putExtra(Intent.EXTRA_MIME_TYPES, mimeTypes); // ??????????????????
            i.addCategory(Intent.CATEGORY_OPENABLE);
            startActivityForResultHere(Intent.createChooser(i, "Image Chooser"), 2, (requestCode, resultCode, intent) -> {
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
        CookieManager.getInstance().removeAllCookies(null);
        CookieManager.getInstance().flush();
        webView = null;
    }

    /* ???????????? */
    public boolean onCreateOptionsMenu(Menu menu) {
        menu.add(0, 0, 0, "???????????????");
        menu.add(0, 1, 0, "???????????????");
        menu.add(0, 2, 0, "???????????????");
        menu.add(0, 3, 0, "???????????????");
        menu.add(0, 4, 0, "???????????????");
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
                MobileUtils.ShareTextToFriend(getBaseContext(),"???????????????"+webView.getOriginalUrl()+"\n???????????????"+webView.getUrl());
                return true;
            }
            case 4: {
                Toast.makeText(getBaseContext(),"????????????????????????",Toast.LENGTH_SHORT).show();
                webView.evaluateJavascript("window.Share.StartSave(document.getElementsByTagName('html')[0].scrollWidth,document.getElementsByTagName('html')[0].scrollHeight)", null);
                return true;
            }
        }
        return super.onOptionsItemSelected(item);
    }

    public class PicShareInterFace {
        @JavascriptInterface
        public void StartShare(String s,String s1) {
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
                MobileUtils.PureBrowserSharePictureToFriend(getBaseContext(), webView, bitmap);
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
                MobileUtils.SaveImageToGallery(getBaseContext(),bitmap,webView.getTitle()+new Date().toLocaleString()+".bmp");
            });
        }
    }

    @Override
    public void onBackPressed() {

        if (webView.canGoBack()) {//???????????????????????????????????????????????????
            webView.goBack(); // goBack()????????????webView???????????????
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public Resources getResources() {
        return MobileUtils.getResources(super.getResources());
    }
}