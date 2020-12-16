package com.tiket.findgithubmember.api

import android.os.SystemClock
import com.tiket.findgithubmember.model.ResponseUserModel
import com.tiket.findgithubmember.urlmanager.UrlManager
import io.reactivex.Observable
import okhttp3.Dispatcher
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.Query
import java.util.concurrent.TimeUnit

interface RequestAPI {

    @GET("search/users")
    @Headers("Content-Type: application/json")
    fun getSearchUser(
        @Query("q") q: String,
        @Query("page") page: Int,
        @Query("per_page") perPage: Int
    ): Observable<ResponseUserModel>

    companion object {
        fun create(): RequestAPI {

            val builder = OkHttpClient.Builder()
            val dispatcher = Dispatcher()

            dispatcher.maxRequests = 1

            val interceptor = Interceptor { chain ->
                SystemClock.sleep(2550)
                chain.proceed(chain.request())
            }

            builder.addNetworkInterceptor(interceptor)
            builder.dispatcher(dispatcher)

            //val client = builder.build()

            val okHttpClient = OkHttpClient.Builder()
                .readTimeout(12, TimeUnit.SECONDS)
                .connectTimeout(20, TimeUnit.SECONDS)
                .writeTimeout(15, TimeUnit.SECONDS)
                .followRedirects(false)
                .followSslRedirects(false)
                .retryOnConnectionFailure(false)
                .cache(null) //new Cache
                .build()

            val retrofit = Retrofit.Builder()
                .baseUrl(UrlManager.BASE_URL)
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .addConverterFactory(GsonConverterFactory.create())
                .client(okHttpClient)
                .build()

            return retrofit.create(RequestAPI::class.java)
        }
    }
}