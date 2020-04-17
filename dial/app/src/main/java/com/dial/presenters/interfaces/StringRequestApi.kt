package com.dial.presenters.interfaces

import io.reactivex.Observable
import retrofit2.Response
import retrofit2.http.*

interface StringRequestApi {

    @GET
    fun getRequest(
        @HeaderMap heads: Map<String, String>,
        @Url url: String
    ): Observable<Response<String>>

    @POST
    fun postRequest(
        @HeaderMap heads: Map<String, String>,
        @Url url: String
    ): Observable<Response<String>>

    @DELETE
    fun deleteRequest(
        @HeaderMap heads: Map<String, String>,
        @Url url: String
    ): Observable<Response<String>>
}