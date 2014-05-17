package com.tiempolibre;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;
 
public class navegador extends Activity
{
        private WebView browser;
 
    private ProgressBar progressBar;
     
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_navegador);
 
        browser = (WebView)findViewById(R.id.webkit);
         
                 //habilitamos javascript y el zoom
        browser.getSettings().setJavaScriptEnabled(true);
        browser.getSettings().setBuiltInZoomControls(true);
 
                //habilitamos los plugins (flash)
     //   browser.getSettings().setPluginsEnabled(true);     
 
        Bundle bundle = getIntent().getExtras();
        
        browser.loadUrl(bundle.getString("url"));
         
        browser.setWebViewClient(new WebViewClient()
        {
                        // evita que los enlaces se abran fuera nuestra app en el navegador de android
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url)
            {
                return false;
            }  
             
        });
 
        progressBar = (ProgressBar) findViewById(R.id.progressbar);
         
        browser.setWebChromeClient(new WebChromeClient()
        {
            @Override
            public void onProgressChanged(WebView view, int progress)
            {              
                progressBar.setProgress(0);
                progressBar.setVisibility(View.VISIBLE);
                navegador.this.setProgress(progress * 1000);
 
                progressBar.incrementProgressBy(progress);
 
                if(progress == 100)
                {
                    progressBar.setVisibility(View.GONE);
                }
            }
        });
    }  
 
}