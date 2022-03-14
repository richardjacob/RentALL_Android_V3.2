package com.rentall.radicalstart.ui

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.View
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import com.airbnb.epoxy.databinding.BR
import com.rentall.radicalstart.R
import com.rentall.radicalstart.databinding.ActivityUrlviewBinding
import com.rentall.radicalstart.host.payout.addPayout.AddPayoutActivity
import com.rentall.radicalstart.ui.base.BaseActivity
import com.rentall.radicalstart.ui.profile.about.AboutViewModel
import com.rentall.radicalstart.util.gone
import com.rentall.radicalstart.util.onClick
import com.rentall.radicalstart.util.visible
import timber.log.Timber
import javax.inject.Inject


class WebViewActivity : BaseActivity<ActivityUrlviewBinding, AboutViewModel>() {
    var screenText = ""
    var requestCode=0
    @Inject
    lateinit var mViewModelFactory: ViewModelProvider.Factory
    private lateinit var mBinding: ActivityUrlviewBinding
    override val bindingVariable: Int
        get() = BR.viewModel
    override val layoutId: Int
        get() = R.layout.activity_urlview
    override val viewModel: AboutViewModel
        get() = ViewModelProviders.of(this, mViewModelFactory).get(AboutViewModel::class.java)

    companion object {
        @JvmStatic
        fun openWebViewActivity(context: Context, url: String, screen: String) {
            val intent = Intent(context, WebViewActivity::class.java)
            intent.putExtra("url", url)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            intent.putExtra("screen", screen)
            context.startActivity(intent)
        }


        @JvmStatic
        fun openWebViewActivityForResult(requestCode: Int,context: Activity, url: String, screen: String) {
            val intent = Intent(context, WebViewActivity::class.java)
            intent.putExtra("url", url)
            intent.putExtra("screen", screen)
            intent.putExtra("requestCode",requestCode)
            context.startActivityForResult(intent,requestCode)
        }
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mBinding=viewDataBinding!!
        initView()
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun initView() {
        mBinding.wv.visible()
        mBinding.actionBar.ivCameraToolbar.gone()
        mBinding.actionBar.tvToolbarHeading.text = intent?.getStringExtra("screen")
        mBinding.actionBar.ivNavigateup.onClick { finish() }

        screenText = intent?.getStringExtra("screen").toString()
        if(intent.hasExtra("requestCode")){
            requestCode = intent.getIntExtra("requestCode",0)
        }
        val splitConnecturl = screenText.split("-".toRegex()).map { it.trim() }
        if (splitConnecturl[0] == "AddStripe Onboarding") {
            mBinding.actionBar.root.gone()
            mBinding.wv.webViewClient = object : WebViewClient() {
                override fun onPageFinished(view: WebView?, url: String?) {
                    mBinding.progressCyclic.gone()
                    mBinding.wv.settings.allowContentAccess = true
                    mBinding.wv.settings.domStorageEnabled = true
                    mBinding.wv.settings.userAgentString = "Mozilla/5.0 (Linux; Android 4.4.4; One Build/KTU84L.H4) AppleWebKit/537.36 (KHTML, like Gecko) Version/4.0 Chrome/33.0.0.0 Mobile Safari/537.36 [FB_IAB/FB4A;FBAV/28.0.0.20.16;]"
                    mBinding.wv.settings.javaScriptEnabled = true
                    mBinding.wv.loadUrl("javascript:(function() { " + "document.getElementsByClassName('qQ2mF')[0].style.display='none';})()")
                    mBinding.wv.visible()
                    Timber.d("WEBVIEW URL : ${url.toString()}")
                    if (url!!.contains("/payout/success")) {
                        AddPayoutActivity.openActivityFromWebView(this@WebViewActivity, "success", splitConnecturl[1])
                        finish()
                    }
                }
            }

            intent?.getStringExtra("url")?.let {
                mBinding.wv.settings.allowContentAccess = true
                mBinding.wv.settings.domStorageEnabled = true
                mBinding.wv.settings.userAgentString = "Mozilla/5.0 (Linux; Android 4.4.4; One Build/KTU84L.H4) AppleWebKit/537.36 (KHTML, like Gecko) Version/4.0 Chrome/33.0.0.0 Mobile Safari/537.36 [FB_IAB/FB4A;FBAV/28.0.0.20.16;]"
                mBinding.wv.settings.javaScriptEnabled = true
                mBinding.wv.loadUrl(intent?.getStringExtra("url").orEmpty())
                Timber.d("WEBVIEW URL : ${it.toString()}")
                mBinding.wv.gone()
            }
        }else if(splitConnecturl[0] == "EditStripe Onboarding"){
            mBinding.actionBar.root.gone()
            mBinding.wv.webViewClient = object : WebViewClient() {
                override fun onPageFinished(view: WebView?, url: String?) {
                    mBinding.progressCyclic.gone()
                    mBinding.wv.settings.allowContentAccess = true
                    mBinding.wv.settings.domStorageEnabled = true
                    mBinding.wv.settings.userAgentString = "Mozilla/5.0 (Linux; Android 4.4.4; One Build/KTU84L.H4) AppleWebKit/537.36 (KHTML, like Gecko) Version/4.0 Chrome/33.0.0.0 Mobile Safari/537.36 [FB_IAB/FB4A;FBAV/28.0.0.20.16;]"
                    mBinding.wv.settings.javaScriptEnabled = true
                    mBinding.wv.loadUrl("javascript:(function() { " + "document.getElementsByClassName('qQ2mF')[0].style.display='none';})()")
                    mBinding.wv.visible()
                    Timber.d("WEBVIEW URL : ${url.toString()}")
                    if (url!!.contains("/payout/success")) {
                        AddPayoutActivity.openActivityFromWebView(this@WebViewActivity, "success", splitConnecturl[1])
                        finish()
                    }
                }
            }

            intent?.getStringExtra("url")?.let {
                mBinding.wv.settings.allowContentAccess = true
                mBinding.wv.settings.domStorageEnabled = true
                mBinding.wv.settings.userAgentString = "Mozilla/5.0 (Linux; Android 4.4.4; One Build/KTU84L.H4) AppleWebKit/537.36 (KHTML, like Gecko) Version/4.0 Chrome/33.0.0.0 Mobile Safari/537.36 [FB_IAB/FB4A;FBAV/28.0.0.20.16;]"
                mBinding.wv.settings.javaScriptEnabled = true
                mBinding.wv.loadUrl(intent?.getStringExtra("url").orEmpty())
                Timber.d("WEBVIEW URL : ${it.toString()}")
                mBinding.wv.gone()
            }
        }else if(splitConnecturl[0] == "PayPalPayment"){
            mBinding.actionBar.root.gone()
            mBinding.wv.webViewClient = object : WebViewClient() {
                override fun onPageFinished(view: WebView?, url: String?) {
                    mBinding.progressCyclic.gone()
                    mBinding.wv.settings.allowContentAccess = true
                    mBinding.wv.settings.domStorageEnabled = true
                    mBinding.wv.settings.userAgentString = "Mozilla/5.0 (Linux; Android 4.4.4; One Build/KTU84L.H4) AppleWebKit/537.36 (KHTML, like Gecko) Version/4.0 Chrome/33.0.0.0 Mobile Safari/537.36 [FB_IAB/FB4A;FBAV/28.0.0.20.16;]"
                    mBinding.wv.settings.javaScriptEnabled = true
                    mBinding.wv.loadUrl("javascript:(function() { " + "document.getElementsByClassName('qQ2mF')[0].style.display='none';})()")
                    mBinding.wv.visible()
                    Timber.d("PayPal WebView URL : ${url.toString()}")
                    Timber.d("WEBVIEW URL : ${url.toString()}")
                }

                override fun onLoadResource(view: WebView?, url: String?) {
                    super.onLoadResource(view, url)
                    if (url!!.contains("/success".toRegex())) {
                        val resultIntent= Intent()
                        resultIntent.putExtra("url",url)
                        setResult(requestCode,resultIntent)
                        finish()
                    }else if(url.contains("/cancel".toRegex())){
                        val resultIntent= Intent()
                        setResult(107,resultIntent)
                        finish()
                    }
                }
            }

            intent?.getStringExtra("url")?.let {
                mBinding.wv.settings.allowContentAccess = true
                mBinding.wv.settings.domStorageEnabled = true
                mBinding.wv.settings.userAgentString = "Mozilla/5.0 (Linux; Android 4.4.4; One Build/KTU84L.H4) AppleWebKit/537.36 (KHTML, like Gecko) Version/4.0 Chrome/33.0.0.0 Mobile Safari/537.36 [FB_IAB/FB4A;FBAV/28.0.0.20.16;]"
                mBinding.wv.settings.javaScriptEnabled = true
                mBinding.wv.loadUrl(intent?.getStringExtra("url").orEmpty())
                mBinding.wv.gone()
            }
        } else {
            mBinding.actionBar.root.visible()
            mBinding.wv.visible()
            mBinding.wv.webViewClient = object : WebViewClient() {

                @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
                override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
                    val url: String = request?.url.toString()
                    if(url==intent?.getStringExtra("url")){
                        view?.loadUrl(url)
                    }else{
                        val intent=Intent(Intent.ACTION_VIEW)
                        intent.data = Uri.parse(url)
                        startActivity(intent)
                    }
                    return true
                }

                override fun shouldOverrideUrlLoading(webView: WebView, url: String): Boolean {
                    if(url==intent?.getStringExtra("url")){
                        webView.loadUrl(url)
                    }else{
                        val intent=Intent(Intent.ACTION_VIEW)
                        intent.data = Uri.parse(url)
                        startActivity(intent)
                    }
                    return true
                }

                override fun onPageFinished(view: WebView?, url: String?) {
                    mBinding.progressCyclic.visibility = View.GONE
                }


//                override fun onPageFinished(view: WebView?, url: String?) {
//                    if (url != null)   {
//                        Timber.d("WEBVIEW URL : ${url.toString()}")
//                        mBinding.progressCyclic.gone()
//                        mBinding.wv.settings.javaScriptEnabled = true
//                        if ("mailto" in url) {
//                           // mBinding.wv.gone()
//                            Timber.d("URL MAILTO")
//                            val emailIntent = Intent(Intent.ACTION_SENDTO).apply {
//                                data = Uri.parse(url)
//                            }
//                            startActivity(Intent.createChooser(emailIntent, "Send feedback"))
//                            mBinding.wv.stopLoading()
//
//                        }
//                        else {
//                            mBinding.wv.loadUrl("javascript:(function() { " + "document.getElementsByClassName('qQ2mF')[0].style.display='none';})()")
//                            mBinding.wv.visible()
//                        }
//                    }
//                }
/*
                override fun onPageFinished(view: WebView?, url: String?) {
                 if (url != null)   {
                        mBinding.progressCyclic.gone()
                        mBinding.wv.settings.javaScriptEnabled = true
                        if ("mailto" in url) {
                            Timber.d("URL MAILTO")
                            val emailIntent = Intent(Intent.ACTION_SENDTO).apply {
                                data = Uri.parse(url)
                            }
                            startActivity(Intent.createChooser(emailIntent, "Send feedback"))
                            this@WebViewActivity.onBackPressed()

                        }
                        mBinding.wv.loadUrl("javascript:(function() { " + "document.getElementsByClassName('qQ2mF')[0].style.display='none';})()")
                        mBinding.wv.visible()
                    }
                }*/
            }

            mBinding.wv.loadUrl(intent?.getStringExtra("url").orEmpty())

//            intent?.getStringExtra("url")?.let {
//                mBinding.wv.loadUrl(intent?.getStringExtra("url"))
//                Timber.d("WEBVIEW URL : ${it.toString()}")
//                mBinding.wv.gone()
//            }
        }


    }

    override fun onRetry() {

    }

}