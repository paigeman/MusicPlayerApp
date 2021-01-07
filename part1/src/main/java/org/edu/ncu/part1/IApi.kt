package org.edu.ncu.part1

import io.reactivex.Observable
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface IApi {

    //获取推荐歌单
//    @GET("/top/playlist?limit=1&order=new")
//    fun getRPlayList(): Call<RPlayList>
    @GET("/top/playlist?limit=1&order=new")
    fun getRPlayList(): Observable<RPlayList>

    //获取热门歌单
    @GET("/playlist/hot")
    fun getHPlayList(): Call<HPlayList>

    //获取歌单详情
//    @GET("/playlist/detail")
//    fun getPlayList(@Query("id")id: Long): Call<PlayList>
    @GET("/playlist/detail")
    fun getPlayList(@Query("id")id: Long): Observable<PlayList>

    //获取音乐url
//    @GET("/song/url")
//    fun getSongUrl(@Query("id")id: Long): Call<OSong>
    @GET("/song/url")
    fun getSongUrl(@Query("id")id: Long): Observable<OSong>

    @GET("/")
    fun doNothing(): Observable<Unit>

}