package com.nullform.ashbox.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.fragment.app.Fragment
import com.nullform.ashbox.R

class WebViewFragment : Fragment() {

    private lateinit var webView: WebView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_webview, container, false)
        webView = view.findViewById(R.id.webview)
        setupWebView()
        return view
    }

    private fun setupWebView() {
        webView.webViewClient = WebViewClient() // This keeps the loading inside your app
        webView.settings.javaScriptEnabled = true // Enable JavaScript if your page uses it
        // Load your "Buy Me a Coffee" page URL
        val url = "https://buymeacoffee.com/zacharymcdaniel_nullform" // <--- REMEMBER TO UPDATE THIS
        webView.loadUrl(url)
    }

    // Handle back button presses within the WebView
    override fun onResume() {
        super.onResume()
        view?.isFocusableInTouchMode = true
        view?.requestFocus()
        view?.setOnKeyListener { v, keyCode, event ->
            if (event.action == android.view.KeyEvent.ACTION_UP && keyCode == android.view.KeyEvent.KEYCODE_BACK) {
                if (webView.canGoBack()) {
                    webView.goBack()
                    true
                } else {
                    false // Let the system handle the back press
                }
            }
            else {
                false
            }
        }
    }
}