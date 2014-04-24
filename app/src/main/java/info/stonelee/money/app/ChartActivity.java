package info.stonelee.money.app;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.ShareActionProvider;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.webkit.ConsoleMessage;
import android.webkit.JavascriptInterface;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

public class ChartActivity extends ActionBarActivity {
    public static final String TAG = "ChartActivity";
    private WebView webView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chart);

        webView = (WebView) findViewById(R.id.webView);
        WebSettings webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webView.addJavascriptInterface(new WebAppInterface(this), "Android");
        webView.setWebChromeClient(new WebChromeClient() {
            public boolean onConsoleMessage(ConsoleMessage cm) {
                Log.e(TAG, cm.message() + " -- From line "
                        + cm.lineNumber() + " of "
                        + cm.sourceId());
                return true;
            }
        });
        webView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return true;
            }
        });
        webView.clearCache(true);
//        webView.loadUrl("file:///android_asset/index.html");
        webView.loadUrl("http://192.168.0.142:8080/Personal/money/Money/app/src/main/assets/index.html");
    }

    private ShareActionProvider mShareActionProvider;

    @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.chart, menu);

        MenuItem item = menu.findItem(R.id.action_share);
        Context themedContext = this.getActionBar().getThemedContext();
        mShareActionProvider = new ShareActionProvider(themedContext);
        MenuItemCompat.setActionProvider(item, mShareActionProvider);

        return true;
    }

    private void setShareIntent(File file) {
        Intent share = new Intent(Intent.ACTION_SEND);
        share.setType("image/jpeg");
        share.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(file));
        mShareActionProvider.setShareIntent(share);
    }

    private Bitmap captureWebkit() {
        Bitmap bitmap = Bitmap.createBitmap(webView.getWidth(), webView.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        webView.draw(canvas);
        return bitmap;
    }

    private File saveImageToFile(Bitmap image) {
        ByteArrayOutputStream mByteArrayOS = new ByteArrayOutputStream();
        image.compress(Bitmap.CompressFormat.JPEG, 90, mByteArrayOS);
        try {
            File sdCard = Environment.getExternalStorageDirectory();
            File dir = new File(sdCard.getAbsolutePath());
            File file = new File(dir, "tempChart.jpg");
            FileOutputStream fos = new FileOutputStream(file);
            fos.write(mByteArrayOS.toByteArray());
            fos.close();
            return file;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public class WebAppInterface {
        Context mContext;

        WebAppInterface(Context c) {
            mContext = c;
        }

        @JavascriptInterface
        public String getBills() {
            Bundle args = getIntent().getExtras();
            return args.getString("bills");
        }

        @JavascriptInterface
        public void showToast(String toast) {
            Toast.makeText(mContext, toast, Toast.LENGTH_SHORT).show();
        }

        @JavascriptInterface
        public void onLoad() {
            runOnUiThread(new Runnable() {
                public void run() {
                    Bitmap image = captureWebkit();
                    File file = saveImageToFile(image);
                    setShareIntent(file);
                }
            });
        }

        @JavascriptInterface
        public void onFinish() {
            runOnUiThread(new Runnable() {
                public void run() {
                    webView.setVisibility(View.VISIBLE);
                }
            });
        }

    }
}
