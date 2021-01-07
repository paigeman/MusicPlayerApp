package org.edu.ncu.part1

import android.util.Log

object AnalyzeJsonUtils {

    private val TAG = "DEBUG"

    fun analyzeRPlayList(rPlayList: RPlayList): ArrayList<OSSong>{
        val id = rPlayList.playlists[0].id
        var playList: PlayList? = null
        val oSongList = ArrayList<OSSong>()
        playList = HttpUtils.sendRequestWithOkHttp("PlayList","http://120.79.175.255:3000/playlist/detail?id="+id) as PlayList
        Log.d(TAG,"ff=${playList?.privileges}")
        Log.d(TAG,"ee=${playList?.playlist}")
        val tracks = playList?.playlist?.tracks!!
        for(i in 0..(tracks.size-1)){
            val tid = tracks[i].id
            val name = tracks[i].name
            val ar = tracks[i].ar
            var author: String = ""
            for (j in 0..(ar.size-1)){
                if(j==0){
                    author += ar[j].name
                }
                else{
                    author += ( "/" + ar[j].name )
                }
            }
            val al = tracks[i].al
            val album = al.name
            val imageUrl = al.picUrl
            val introduction = author + "-" + album
            var oSong: OSong? = HttpUtils.sendRequestWithOkHttp("OSong","http://120.79.175.255:3000/song/url?id="+tid) as OSong
            val playUrl = (oSong?.data)?.get(0)?.url!!
            val song = OSSong(tid,name,author,imageUrl,introduction,playUrl)
            oSongList.add(song)
            Log.d(TAG,"size=${oSongList.size}")
        }
        return oSongList
    }

    fun getPlayListName(rPlayList: RPlayList):String{
        val id = rPlayList.playlists[0].id
        val playList: PlayList = HttpUtils.sendRequestWithOkHttp("PlayList","http://120.79.175.255:3000/playlist/detail?id="+id) as PlayList
        return playList.playlist.name
    }

    fun getLyric(song: OSSong): String{
        val id = song.id
        val lyric: Lyric = HttpUtils.sendRequestWithOkHttp("Lyric","http://120.79.175.255:3000/lyric?id="+id) as Lyric
        return lyric.lrc.lyric
    }

}