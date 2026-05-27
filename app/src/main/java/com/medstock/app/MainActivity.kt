package com.medstock.app

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.print.PrintAttributes
import android.print.PrintManager
import android.view.View
import android.webkit.JavascriptInterface
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.FrameLayout
import androidx.activity.ComponentActivity
import androidx.activity.OnBackPressedCallback

class MainActivity : ComponentActivity() {

    private lateinit var webView: WebView

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Edge-to-edge friendly background
        val container = FrameLayout(this).apply {
            setBackgroundColor(0xFFF5F7FA.toInt())
        }

        webView = WebView(this).apply {
            settings.javaScriptEnabled = true
            settings.domStorageEnabled = true
            settings.databaseEnabled = true
            settings.allowFileAccess = true
            settings.allowContentAccess = true
            settings.builtInZoomControls = false
            settings.displayZoomControls = false
            settings.setSupportZoom(false)
            settings.useWideViewPort = false
            settings.loadWithOverviewMode = false
            settings.textZoom = 100
            // Important: persist localStorage between launches
            isVerticalScrollBarEnabled = true
            isHorizontalScrollBarEnabled = false
            overScrollMode = View.OVER_SCROLL_NEVER
            webViewClient = WebViewClient()
            addJavascriptInterface(AndroidBridge(this@MainActivity, this), "Android")
            loadUrl("file:///android_asset/index.html")
        }

        container.addView(webView,
            FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT
            )
        )
        setContentView(container)

        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                webView.evaluateJavascript("typeof MedStock !== 'undefined' && MedStock.handleBack()") { result ->
                    if (result == "true") {
                        // JS handled the back press
                    } else {
                        if (webView.canGoBack()) webView.goBack() else finish()
                    }
                }
            }
        })
    }

    override fun onPause() {
        super.onPause()
        webView.onPause()
    }

    override fun onResume() {
        super.onResume()
        webView.onResume()
    }

    override fun onDestroy() {
        webView.destroy()
        super.onDestroy()
    }

    /**
     * Bridge object exposed to JavaScript as `Android`.
     * - Android.printInvoice('INV-00001') -> opens system Print/Save-as-PDF dialog
     */
    class AndroidBridge(private val activity: MainActivity, private val webView: WebView) {

        @JavascriptInterface
        fun printInvoice(invoiceNo: String) {
            activity.runOnUiThread {
                val pm = activity.getSystemService(Context.PRINT_SERVICE) as PrintManager
                val jobName = "MedStock-Invoice-$invoiceNo"
                val adapter = webView.createPrintDocumentAdapter(jobName)
                pm.print(jobName, adapter, PrintAttributes.Builder().build())
            }
        }
    }
}
