package com.tiket.findgithubmember.view

import android.annotation.SuppressLint
import android.os.Bundle
import android.webkit.WebSettings
import android.webkit.WebViewClient
import androidx.appcompat.app.AppCompatActivity
import com.tiket.findgithubmember.databinding.ActivityProfileMemberBinding
import com.tiket.findgithubmember.urlmanager.UrlManager

class ProfileMember : AppCompatActivity() {

    private lateinit var binding: ActivityProfileMemberBinding
    private var html_url: String = "Sorry data Not Found!"

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProfileMemberBinding.inflate(layoutInflater)
        setContentView(binding.root)

        html_url = intent.getStringExtra("htmlUrl").toString()

        val webSettings: WebSettings = binding.contentWeb.settings
        webSettings.javaScriptEnabled = true
        binding.contentWeb.loadUrl(html_url)
        binding.contentWeb.webViewClient = WebViewClient()
    }
}