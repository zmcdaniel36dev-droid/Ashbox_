package com.nullform.ashbox.ui

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.nullform.ashbox.R

class WebViewFragment : Fragment() {

    private lateinit var webView: WebView
    private var url: String? = null

    // Public methods to expose WebView back navigation
    fun canWebViewGoBack(): Boolean {
        return webView.canGoBack()
    }

    fun webViewGoBack() {
        webView.goBack()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            url = it.getString(URL_KEY)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_webview, container, false)
        webView = view.findViewById(R.id.webview)
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupWebView()
    }

    private fun setupWebView() {
        webView.webViewClient = WebViewClient() // This keeps the loading inside your app
        webView.webChromeClient = object : WebChromeClient() {
            override fun onReceivedTitle(view: WebView?, title: String?) {
                super.onReceivedTitle(view, title)
                // Update toolbar title with the WebView's page title
                (activity as? AppCompatActivity)?.supportActionBar?.title = title
            }
        }
        webView.settings.javaScriptEnabled = true // Enable JavaScript if your page uses it

        url?.let {
            Log.d(TAG, "Loading URL: $it")
            webView.loadUrl(it)
        } ?: run {
            Log.d(TAG, "URL not provided, loading about:blank")
            webView.loadUrl("about:blank")
        }
    }

    // Handle back button presses within the WebView (for physical/software back button)
    override fun onResume() {
        super.onResume()
        view?.isFocusableInTouchMode = true
        view?.requestFocus()
        view?.setOnKeyListener { _, keyCode, event ->
            if (event.action == android.view.KeyEvent.ACTION_UP && keyCode == android.view.KeyEvent.KEYCODE_BACK) {
                if (webView.canGoBack()) {
                    webView.goBack()
                    true
                } else {
                    false // Let the system handle the back press (e.g., pop fragment)
                }
            } else {
                false
            }
        }
    }

    companion object {
        const val URL_KEY = "url"
        private const val TAG = "WebViewFragment"

        @JvmStatic
        fun newInstance(url: String) =
            WebViewFragment().apply {
                arguments = Bundle().apply {
                    putString(URL_KEY, url)
                }
            }
    }
}
