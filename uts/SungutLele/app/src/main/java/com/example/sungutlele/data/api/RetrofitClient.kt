package com.example.sungutlele.data.api

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import okhttp3.OkHttpClient
import okhttp3.Interceptor
import com.example.sungutlele.BuildConfig

object RetrofitClient {

    /**
     * Base URL for the FastAPI backend.
     *
     * - Android Emulator: "http://10.0.2.2:8000/api/v1/"
     * - Physical device on same Wi-Fi: use your computer's local IP,
     *   e.g. "http://192.168.1.100:8000/api/v1/"
     *
     * To find your computer's IP: run `ipconfig` (Windows) or `ifconfig` (Mac/Linux).
     */
//    private const val BASE_URL = "http://192.168.2.102:8000/api/v1/"
//    private const val BASE_URL = "https://sungutlele.mkapp.qzz.io/api/v1/"
    private const val BASE_URL = "https://sungutlele2.vercel.app/api/v1/"

    private val authInterceptor = Interceptor { chain ->
        val originalRequest = chain.request()
        val builder = originalRequest.newBuilder()
            .header("Authorization", "Bearer ${BuildConfig.ACCESS_TOKEN}")
        
        val newRequest = builder.build()
        chain.proceed(newRequest)
    }

    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(authInterceptor)
        .connectTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
        .readTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
        .writeTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
        .build()

    val apiService: ApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }
}
