package org.edu.ncu.part1

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import okhttp3.OkHttpClient
import okhttp3.Request
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.create
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL

object HttpUtils {

    private val retrofit = Retrofit
            .Builder()
            .baseUrl("http://120.79.175.255:3000")
            .addConverterFactory(GsonConverterFactory.create())
            .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
            .build()

    fun <T> create(serviceClass: Class<T>): T = retrofit.create(serviceClass)

    fun getImageViewInputStream(urlString: String): InputStream{
        var input: InputStream? = null
        val url = URL(urlString)
        if(url!=null){
            val httpURLConnection = url.openConnection() as HttpURLConnection
            //设置网络连接超时的时间为3秒
            httpURLConnection.setConnectTimeout(3000)
            //请求方式为GET
            httpURLConnection.setRequestMethod("GET")
            //打开输入流
            httpURLConnection.setDoInput(true)
            //获取服务器响应值
            val responseCode = httpURLConnection.getResponseCode()
            if(responseCode==HttpURLConnection.HTTP_OK){
                input = httpURLConnection.getInputStream()
            }
        }
        return input!!
    }

    fun sendRequestWithOkHttp(type: String,url: String): Object?{
        val client = OkHttpClient()
        val request = Request.Builder()
                .url(url)
                .build()
        val response = client.newCall(request).execute()
        val responseData = response.body()?.string()
        if(responseData!=null){
            return parseJSONWithGson(type,responseData)
        }
        else{
            return null
        }
    }

    fun parseJSONWithGson(type: String,jsonData: String): Object{
        val gson = Gson()
        if (type=="PlayList"){
            val typeOf = object : TypeToken<PlayList>(){}.type
            return gson.fromJson(jsonData,typeOf)
        }
        else if(type=="Lyric"){
            val typeOf = object : TypeToken<Lyric>(){}.type
            return gson.fromJson(jsonData,typeOf)
        }
        else{
            val typeOf = object : TypeToken<OSong>(){}.type
            return gson.fromJson(jsonData,typeOf)
        }
    }

}