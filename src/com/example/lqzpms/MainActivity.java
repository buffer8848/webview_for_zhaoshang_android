package com.example.lqzpms;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.Window;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageView;
import android.widget.Toast;


public class MainActivity extends Activity {
	private String url = "http://lqz.zhaoshang.pw";
	private String cookiefile = "cookie.data";
	private boolean ready = false;
	
	private WebView mWebView; 
	private ImageView mImageView;
	private ProgressDialog progressBar; 

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE); //show on page not the title
		setContentView(R.layout.activity_main);
		
		//loading image animate
		mImageView = (ImageView) findViewById(R.id.imageView1);
		new Handler().postDelayed(new Runnable() {  
            public void run() {
            	mImageView.setImageBitmap(null);
                mImageView.destroyDrawingCache(); 
                mWebView.invalidate();
                ready = true;
            }  
  
        }, 3000); 
		
		//load cookie
		String cookies = null;
		FileInputStream inStream = null;
		try {
			inStream = getBaseContext().openFileInput(cookiefile);
		} catch (FileNotFoundException e1) {
			e1.printStackTrace();
		}
		
		if (inStream != null) {
			byte[] bytes = new byte[1024];  
	        ByteArrayOutputStream arrayOutputStream = new ByteArrayOutputStream();  
	        try {
				while (inStream.read(bytes) != -1) {  
					arrayOutputStream.write(bytes, 0, bytes.length);  
				}
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
	        
	        try {
	        	inStream.close();
	        	arrayOutputStream.close();
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
	        cookies = new String(arrayOutputStream.toByteArray()); 
		}
        
		//set cookie
		CookieSyncManager.createInstance(this.getBaseContext());
		CookieManager cookieManager = CookieManager.getInstance();
		cookieManager.setAcceptCookie(true);
		cookieManager.setCookie(url, cookies);
		CookieSyncManager.getInstance().sync();
		
		progressBar = new ProgressDialog(this);
		
		//load url
		mWebView = (WebView) findViewById(R.id.webView1);  
		mWebView.getSettings().setJavaScriptEnabled(true); 
		mWebView.getSettings().setBuiltInZoomControls(true);
		mWebView.setScrollBarStyle(WebView.SCROLLBARS_OUTSIDE_OVERLAY); 
		mWebView.setWebViewClient(new WebViewClient() {
			@Override
			public boolean shouldOverrideUrlLoading(WebView view,String url){    
				//调用拨号程序  
				if (url.startsWith("mailto:") || url.startsWith("geo:") ||url.startsWith("tel:")) {  
					Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));  
					startActivity(intent);  
				} else {
					//当有新连接时，使用当前的 WebView    
					view.loadUrl(url);
				}
				return true;     
			}
			
			@Override
			public void onPageStarted(WebView view, String url, Bitmap favicon) {
				if (ready)
					progressBar.show();
			}
			
			@Override		
			public void onPageFinished(WebView view, String url) {
				CookieSyncManager cookieSyncManager = CookieSyncManager.createInstance(MainActivity.this);
				cookieSyncManager.sync();
				CookieManager cookieManager = CookieManager.getInstance();
				cookieManager.setAcceptCookie(true);
				FileOutputStream outStream = null;
				try {
					getBaseContext();
					outStream = openFileOutput(cookiefile, Context.MODE_PRIVATE);
				} catch (FileNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					return;
				}
				
				try {
					if (cookieManager.getCookie(url) != null)
						outStream.write(cookieManager.getCookie(url).getBytes());
					outStream.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
				
				progressBar.hide();
            		
            }
            	
			@Override		
			public void onReceivedError(WebView view, int errorCode,  String description, String failingUrl) {  
            	// TODO Auto-generated method stub  
            	Toast.makeText(MainActivity.this,  description, Toast.LENGTH_SHORT).show(); 
			}  
		});
		
		mWebView.loadUrl(url);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
	
	@Override 
    public boolean onKeyDown(int keyCode, KeyEvent event) {  
        // TODO Auto-generated method stub  
        if ((keyCode == KeyEvent.KEYCODE_BACK) && mWebView.canGoBack()) {
        	mWebView.goBack();  
            return true;  
        }  
        return super.onKeyDown(keyCode, event);  
    }  

}
