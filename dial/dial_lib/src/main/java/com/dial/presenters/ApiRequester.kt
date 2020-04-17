package com.dial.presenters

import com.dial.presenters.interfaces.StringRequestApi
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.scalars.ScalarsConverterFactory


object ApiRequester {

    private val mRetrofit:Retrofit = Retrofit.Builder()
        .baseUrl("http://127.0.0.1")
        .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
        .addConverterFactory(ScalarsConverterFactory.create())
        .build()

    fun getStringRequestApi(): StringRequestApi {
        return mRetrofit.create(StringRequestApi::class.java)
    }
}