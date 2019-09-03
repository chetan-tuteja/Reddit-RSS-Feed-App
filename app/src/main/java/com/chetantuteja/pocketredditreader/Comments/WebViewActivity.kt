package com.chetantuteja.pocketredditreader.Comments

import android.opengl.Visibility
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.webkit.WebView
import android.webkit.WebViewClient
import com.chetantuteja.pocketredditreader.R
import kotlinx.android.synthetic.main.activity_web_view.*

class WebViewActivity : AppCompatActivity() {
    companion object {
        private const val TAG: String = "WebViewActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_web_view)
        Log.d(TAG, "onCreate: Started ")
        webviewLoadingProgressBar.visibility = View.VISIBLE
        loadingWebpageTV.visibility = View.VISIBLE

        setupWebpage()
    }

    private fun setupWebpage() {
        val intent = intent
        if(intent!=null && intent.hasExtra(getString(R.string.webpage_url))){
            val url = intent.getStringExtra(getString(R.string.webpage_url))
            webviewLayout.settings.javaScriptEnabled = true
            webviewLayout.loadUrl(url)

            webviewLayout.webViewClient = object: WebViewClient(){
                override fun onPageFinished(view: WebView?, url: String?) {
                    super.onPageFinished(view, url)
                    webviewLoadingProgressBar.visibility = View.GONE
                    loadingWebpageTV.text = ""
                    loadingWebpageTV.visibility = View.GONE
                }
            }
        }
    }
}
