package org.edu.ncu.part1

import android.app.*
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Bitmap
import android.media.AudioManager
import android.media.MediaPlayer
import android.net.Uri
import android.os.*
import android.util.Log
import android.widget.RemoteViews
import androidx.core.app.NotificationCompat
import androidx.core.content.getSystemService
import java.io.BufferedReader
import java.io.InputStream
import java.io.InputStreamReader
import java.net.URL
import java.util.*
import java.util.concurrent.Executors
import java.util.concurrent.Semaphore
import java.util.concurrent.locks.ReentrantLock
import java.util.ArrayList

class PlayService : Service() {

    lateinit private var songName: String

    lateinit private var songList: Array<Song>

    lateinit private var oSongList: ArrayList<OSSong>

    lateinit private var sBitmapList: List<Bitmap>

    lateinit private var type: String

    private var songPosition: Int = 0

    private var lyricIndex: Int = 0

    lateinit var songUrl: String

    //调试标志
    private val TAG = "DEBUG"

    //音乐播放器
    private var mediaPlayer = MediaPlayer()

    //线程池
    private val executor = Executors.newCachedThreadPool()

    //通过绑定实现通信
    private val playBinder = PlayBinder()

    //锁
    private val progressLock = ReentrantLock()
    private val currentTimeLock = ReentrantLock()

    //条件
    private val progressCondition = progressLock.newCondition()
    private val currentTimeCondition = currentTimeLock.newCondition()

    //信号量
    private val progressSemaphore = Semaphore(1)
    private val currentTimeSemaphore = Semaphore(1)
    private val lyricSemaphore = Semaphore(1)

    //RemoteViews
    private val remoteView = RemoteViews("org.edu.ncu.part1",R.layout.notify)

    //通知
    lateinit private var notification: Notification

    lateinit private var manager: NotificationManager

    private val BROCAST_ACTION_PLAY = "org.edu.ncu.play"

    private val BROCAST_ACTION_BEFORE = "org.edu.ncu.before"

    private val BROCAST_ACTION_AFTER = "org.edu.ncu.after"

    private val BROCAST_ACTION_CLOSE = "org.edu.ncu.close"

    lateinit private var playServiceBroadcastReceiver: PlayServiceBroadcastReceiver

    inner class PlayServiceBroadcastReceiver : BroadcastReceiver(){

        override fun onReceive(context: Context?, intent: Intent?) {
            when(intent?.action){
                BROCAST_ACTION_PLAY->{
                    if(!mediaPlayer.isPlaying){
                        mediaPlayer.start()
                        remoteView.setImageViewResource(R.id.notify_play,R.drawable.notify_pause)
                        startForeground(1,notification)
                    }
                    else{
                        mediaPlayer.pause()
                        remoteView.setImageViewResource(R.id.notify_play,R.drawable.notify_play)
                        startForeground(1,notification)
                    }
                }
                BROCAST_ACTION_CLOSE->{
                    stopForeground(true)
                }
            }
        }

    }

    inner class PlayBinder : Binder(){

        fun isPlaying(handler: Handler){
            val msg = handler.obtainMessage()
            val data = Bundle()
            Log.d(TAG,"mediaPlayer.isPlaying=${mediaPlayer.isPlaying}")
            if(!mediaPlayer.isPlaying){
                data.putString("action","play")
            }
            else{
                data.putString("action","pause")
            }
            val duration = mediaPlayer.duration
            data.putInt("duration",duration)
            msg.data = data
            msg.sendToTarget()
        }

        fun setType(type: String){
            this@PlayService.type = type
        }

        fun setBitmapList(sBitmapList: List<Bitmap>,url: String){
            this@PlayService.sBitmapList = sBitmapList
            this@PlayService.songUrl = url
        }

//        fun setUrl(url: String){
//            this@PlayService.songUrl = url
//        }

        fun setSongName(songName: String,songPosition: Int,songList: Array<Song>){
            this@PlayService.songName = songName
            this@PlayService.songList = songList
            this@PlayService.songPosition = songPosition
            //实现前台Service
            manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
//            Log.d(TAG,"songPositon=${songPosition}")
            remoteView.setImageViewResource(R.id.notify_image,resources.obtainTypedArray(R.array.songImage)
                    .getResourceId(songPosition,R.mipmap.song1))
            remoteView.setTextViewText(R.id.notify_songName,resources.getStringArray(R.array.songName)
                    .get(songPosition))
            remoteView.setTextViewText(R.id.notify_songIntroduction,resources.getStringArray(R.array.songDescription)
                    .get(songPosition))
            val beforeIntent = Intent(BROCAST_ACTION_BEFORE)
            val playIntent = Intent(BROCAST_ACTION_PLAY)
            val nextIntent = Intent(BROCAST_ACTION_AFTER)
            val closeIntent = Intent(BROCAST_ACTION_CLOSE)
            val beforePendingIntent = PendingIntent.getBroadcast(this@PlayService,0,beforeIntent,0)
            val playPendingIntent = PendingIntent.getBroadcast(this@PlayService,0,playIntent,0)
            val nextPendingIntent = PendingIntent.getBroadcast(this@PlayService,0,nextIntent,0)
            val closePendingIntent = PendingIntent.getBroadcast(this@PlayService,0,closeIntent,0)
            remoteView.setOnClickPendingIntent(R.id.notify_before,beforePendingIntent)
            remoteView.setOnClickPendingIntent(R.id.notify_play,playPendingIntent)
            remoteView.setOnClickPendingIntent(R.id.notify_next,nextPendingIntent)
            remoteView.setOnClickPendingIntent(R.id.notify_close,closePendingIntent)
            if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.O){
                val channel = NotificationChannel("PlayService","前台Service",NotificationManager.IMPORTANCE_MIN)
                manager.createNotificationChannel(channel)
            }
            //需设置Icon
            notification = NotificationCompat.Builder(applicationContext,"PlayService")
                    .setSmallIcon(R.drawable.ic_launcher_background)
                    .setContent(remoteView)
                    .setCustomContentView(remoteView)
                    .setCustomBigContentView(remoteView)
                    .setAutoCancel(false)
                    .build()
            startForeground(1,notification)
        }

        fun setOSongName(songName: String,songPosition: Int,songList: ArrayList<OSSong>){
            this@PlayService.songName = songName
            this@PlayService.oSongList = songList
            this@PlayService.songPosition = songPosition
            Log.d(TAG,"sdfsdfsfs=${songPosition}+${songName}+${oSongList}")
            manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
//            remoteView.setImageViewUri(R.id.notify_image, Uri.parse(oSongList.get(songPosition).imageUrl))
            remoteView.setImageViewBitmap(R.id.notify_image,
                    sBitmapList.get(songPosition))
            remoteView.setTextViewText(R.id.notify_songName,
                    oSongList.get(songPosition).name)
            remoteView.setTextViewText(R.id.notify_songIntroduction,
                    oSongList.get(songPosition).introduction)
            val beforeIntent = Intent(BROCAST_ACTION_BEFORE)
            val playIntent = Intent(BROCAST_ACTION_PLAY)
            val nextIntent = Intent(BROCAST_ACTION_AFTER)
            val closeIntent = Intent(BROCAST_ACTION_CLOSE)
            val beforePendingIntent = PendingIntent.getBroadcast(this@PlayService,0,beforeIntent,0)
            val playPendingIntent = PendingIntent.getBroadcast(this@PlayService,0,playIntent,0)
            val nextPendingIntent = PendingIntent.getBroadcast(this@PlayService,0,nextIntent,0)
            val closePendingIntent = PendingIntent.getBroadcast(this@PlayService,0,closeIntent,0)
            remoteView.setOnClickPendingIntent(R.id.notify_before,beforePendingIntent)
            remoteView.setOnClickPendingIntent(R.id.notify_play,playPendingIntent)
            remoteView.setOnClickPendingIntent(R.id.notify_next,nextPendingIntent)
            remoteView.setOnClickPendingIntent(R.id.notify_close,closePendingIntent)
            if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.O){
                val channel = NotificationChannel("PlayService","前台Service",NotificationManager.IMPORTANCE_MIN)
                manager.createNotificationChannel(channel)
            }
            notification = NotificationCompat.Builder(applicationContext,"PlayService")
                    .setSmallIcon(R.drawable.ic_launcher_background)
                    .setContent(remoteView)
                    .setCustomContentView(remoteView)
                    .setCustomBigContentView(remoteView)
                    .setAutoCancel(false)
                    .build()
            startForeground(1,notification)
        }

        fun play(handler: Handler){
//            lock.lock()
            val msg = handler.obtainMessage()
            val data = Bundle()
            if(!mediaPlayer.isPlaying){
//                Thread.sleep(600)
                mediaPlayer.start()
                data.putString("action","play")
                remoteView.setImageViewResource(R.id.notify_play,R.drawable.notify_pause)
                startForeground(1,notification)
            }
            else{
                mediaPlayer.pause()
                data.putString("action","pause")
                remoteView.setImageViewResource(R.id.notify_play,R.drawable.notify_play)
                startForeground(1,notification)
            }
            val duration = mediaPlayer.duration
            data.putInt("duration",duration)
            msg.data = data
            msg.sendToTarget()
//            lock.unlock()
        }

        fun init(handler: Handler){
            if(type=="local"){
                initMediaPlayer(songName)
            }
            else{
                initOMediaPlayer(songUrl)
            }
            val totalTime = mediaPlayer.duration
            val currentTime = 0
            val msg = handler.obtainMessage()
            val data = Bundle()
            data.putString("totalTime",calculateTime(totalTime))
            data.putString("currentTime",calculateTime(currentTime))
            data.putInt("duration",totalTime)
            msg.data = data
            msg.sendToTarget()
            lyricIndex = 0
        }

        fun updateProgress(handler: Handler){
            val progressThread = Thread{
//                progressLock.lock()
//                semaphore.acquire()
                var flag = true
                progressSemaphore.acquire()
                while (flag){
                    if(mediaPlayer.isPlaying){
                        val position = mediaPlayer.currentPosition
//                    semaphore.release()
//                    progressCondition.signal()
                        val msg = handler.obtainMessage()
                        val data = Bundle()
                        data.putInt("position",position)
                        msg.data = data
                        msg.sendToTarget()
                    }
                    else{
                        progressSemaphore.release()
                        flag = false
                    }
                }
//                progressLock.unlock()
            }
            executor.execute(progressThread)
        }

        fun updateCurrentTime(handler: Handler){
            val currentTimeThread = Thread{
//                currentTimeLock.lock()
//                semaphore.acquire()
                var flag = true
                currentTimeSemaphore.acquire()
                while (flag){
                    if(mediaPlayer.isPlaying){
                        val position = mediaPlayer.currentPosition
//                    semaphore.release()
//                    currentTimeCondition.signal()
                        val currentTime = calculateTime(position)
                        val msg = handler.obtainMessage()
                        val data = Bundle()
                        data.putString("currentTime",currentTime)
                        msg.data = data
                        msg.sendToTarget()
                    }
                    else{
                        flag = false
                        currentTimeSemaphore.release()
                    }
                }
//                currentTimeLock.unlock()
            }
            executor.execute(currentTimeThread)
        }

        fun forwardPlay(handler: Handler){
            var position = mediaPlayer.currentPosition
            val totalTime = mediaPlayer.duration
            //快进3秒
            position = position + 3000
            if(position>totalTime){
                position = totalTime
            }
            val currentTime = calculateTime(position)
            val msg = handler.obtainMessage()
            val data = Bundle()
            data.putInt("position",position)
            data.putString("currentTime",currentTime)
            msg.data = data
            msg.sendToTarget()
            mediaPlayer.seekTo(position)
        }

        fun backPlay(handler: Handler){
            var position = mediaPlayer.currentPosition
            if(position<=3000){
                position = 0
            }
            else{
                position = position - 3000
            }
            val currentTime = calculateTime(position)
            val msg = handler.obtainMessage()
            val data = Bundle()
            data.putInt("position",position)
            data.putString("currentTime",currentTime)
            msg.data = data
            msg.sendToTarget()
            mediaPlayer.seekTo(position)
        }

        fun afterPlay(){
            mediaPlayer.stop()
            progressSemaphore.acquire()
            currentTimeSemaphore.acquire()
            lyricSemaphore.acquire()
//            mediaPlayer.reset()
            mediaPlayer.release()
            mediaPlayer = MediaPlayer()
//            mediaPlayer.reset()
            progressSemaphore.release()
            currentTimeSemaphore.release()
            lyricSemaphore.release()
            remoteView.setImageViewResource(R.id.notify_play,R.drawable.notify_play)
            startForeground(1,notification)
        }

        fun beforePlay(){
//            if(mediaPlayer!=null){
//                mediaPlayer.stop()
//            }
            mediaPlayer.stop()
            progressSemaphore.acquire()
            currentTimeSemaphore.acquire()
            lyricSemaphore.acquire()
//            mediaPlayer.reset()
            mediaPlayer.release()
            mediaPlayer = MediaPlayer()
//            mediaPlayer.reset()
            progressSemaphore.release()
            currentTimeSemaphore.release()
            lyricSemaphore.release()
            remoteView.setImageViewResource(R.id.notify_play,R.drawable.notify_play)
            startForeground(1,notification)
        }

        fun isFinish(handler1: Handler,handler2: Handler){
            val isFinishThread = Thread{
                while (true){
                    mediaPlayer.setOnCompletionListener {
                        val msg1 = handler1.obtainMessage()
                        val data1 = Bundle()
                        data1.putString("action","finish")
                        msg1.data = data1
                        msg1.sendToTarget()
                        val msg2 = handler2.obtainMessage()
                        val data2 = Bundle()
                        msg2.data = data2
                        msg2.sendToTarget()
                        remoteView.setImageViewResource(R.id.notify_play,R.drawable.notify_play)
                        startForeground(1,notification)
                    }
                }
            }
            executor.execute(isFinishThread)
        }

        fun updateLyric(handler: Handler){
            val lyricThread = Thread{
                var lyricRowsArray: ArrayList<String> = ArrayList<String>()
                if(type=="local"){
                    lyricRowsArray = analyzeLyric(switchLyricName(songName))
                }
                else{
                    lyricRowsArray = analyzeLyric("")
                }
                var flag = true
                //因获取信号量和if判断需要时间
                //故快进过快时可能存在bug
                lyricSemaphore.acquire()
                while(flag){
                    if(mediaPlayer.isPlaying){
                        val position = mediaPlayer.currentPosition
                        val msg = handler.obtainMessage()
                        val lyric = lyricRowsArray[lyricIndex]
                        val time = lyric.split("%")[0].toInt()
                        val lyricContent = lyric.split("%")[1]
                        Log.d(TAG,lyric)
                        Log.d(TAG,"time=${time}\tcontent=${lyricContent}")
                        val data = Bundle()
                        Log.d(TAG,"thread=${Thread.currentThread().name}\tlyricIndex=${lyricIndex}")
                        Log.d(TAG,"${mediaPlayer.currentPosition<=time}")
                        Log.d(TAG,"current=${mediaPlayer.currentPosition}\ttime=${time}")
                        if(lyricIndex==0){
                            val tempLyric = lyricRowsArray[lyricIndex+1]
                            val tempTime = tempLyric.split("%")[0].toInt()
                            if(mediaPlayer.currentPosition<tempTime){
                                data.putString("lyric",lyricContent)
                                msg.data = data
                                msg.sendToTarget()
                                lyricIndex++
                            }
                        }
                        else if (lyricIndex>=1&&lyricIndex<=lyricRowsArray.size-2){
                            val tempLyric1 = lyricRowsArray[lyricIndex+1]
                            val tempTime1 = tempLyric1.split("%")[0].toInt()
                            if(mediaPlayer.currentPosition>=time&&mediaPlayer.currentPosition<=position){
                                data.putString("lyric",lyricContent)
                                msg.data = data
                                msg.sendToTarget()
                                lyricIndex++
                            }
                        }
                        else if (lyricIndex==lyricRowsArray.size-1){
                            data.putString("lyric",lyricContent)
                            msg.data = data
                            msg.sendToTarget()
                        }
                    }
                    else{
                        flag = false
                        lyricSemaphore.release()
                    }
                }
            }
            executor.execute(lyricThread)
        }

    }

    override fun onBind(intent: Intent): IBinder {
//        TODO("Return the communication channel to the service.")
        return playBinder
    }

    override fun onCreate() {
        super.onCreate()
        //TODO add code below
        //实现前台Service
//        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
//        val remoteView = RemoteViews(packageName,R.layout.notify)
//        remoteView.setImageViewResource(R.id.notify_image,resources.obtainTypedArray(R.array.songImage).getResourceId(songPosition,R.mipmap.song1))
//        if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.O){
//            val channel = NotificationChannel("PlayService","前台Service",NotificationManager.IMPORTANCE_DEFAULT)
//            manager.createNotificationChannel(channel)
//        }
//        //需设置Icon
//        val notification = NotificationCompat.Builder(this,"PlayService")
//                .setSmallIcon(R.drawable.ic_launcher_background)
//                .setContent(remoteView)
//                .setCustomContentView(remoteView)
//                .setCustomBigContentView(remoteView).build()
//        startForeground(1,notification)
        //注册广播
        val intentFilter = IntentFilter()
        intentFilter.addAction(BROCAST_ACTION_PLAY)
        intentFilter.addAction(BROCAST_ACTION_AFTER)
        intentFilter.addAction(BROCAST_ACTION_BEFORE)
        intentFilter.addAction(BROCAST_ACTION_CLOSE)
        playServiceBroadcastReceiver = PlayServiceBroadcastReceiver()
        registerReceiver(playServiceBroadcastReceiver,intentFilter)
    }

    //初始化MediaPlayer
    private fun initMediaPlayer(songName: String){
        val assetManager = assets
        val fd = assetManager.openFd(switchSongName(songName))
        mediaPlayer.setDataSource(fd.fileDescriptor,fd.startOffset,fd.length)
        mediaPlayer.prepare()
    }

    //初始化在线播放
    private fun initOMediaPlayer(url: String){
        Log.d(TAG,"playUrl=${url}")
        mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC)
        mediaPlayer.setDataSource(url)
        mediaPlayer.prepare()
    }

    private fun switchSongName(songName: String):String{
        var fileName:String? = null
        when(songName){
            "Yellow"->fileName = "Coldplay - Yellow.mp3"
            "Hoppípolla"->fileName = "Sigur Rós - Hoppípolla.mp3"
            "暗号"->fileName = "暗号-周杰伦.mp3"
            "心做し"->fileName = "GUMI 一之瀬ユウ - 心做し.mp3"
        }
        return fileName!!
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return super.onStartCommand(intent, flags, startId)
    }

    //计算时间
    private fun calculateTime(time:Int):String{
        val totalSecond = time / 1000
        var second = 0
        var minute = 0
        if(totalSecond>60){
            second = totalSecond % 60
            minute = totalSecond / 60
            if(minute>=0&&minute<=9){
                if(second>=0&&second<=9){
                    return "0"+minute+":0"+second
                }
                else{
                    return "0"+minute+":"+second
                }
            }
            else{
                if(second>=0&&second<=9){
                    return ""+minute+":0"+second
                }
                else{
                    return ""+minute+":"+second
                }
            }
        }
        else{
            second = totalSecond
            if(second>=0&&second<=9){
                return "00"+":0"+second
            }
            else{
                return "00"+":"+second
            }
        }
    }

    //分析歌词
    private fun analyzeLyric(lyricName: String): ArrayList<String> {
        val lyricRowsArray = ArrayList<String>()
//        var input: InputStream? = null
        val lyricRows = ArrayList<String>()
        if(type=="local"){
            val input = resources.assets.open(lyricName)
            val reader = BufferedReader(InputStreamReader(input))
            var eof = false
            var line: String? = null
//        val lyricRows = ArrayList<String>()
            while (!eof){
                line = reader.readLine()
                if(line==null){
                    eof = true
                }
                else{
                    lyricRows.add(line)
                }
            }
            reader.close()
            input?.close()
        }
        else{
            var lyric = AnalyzeJsonUtils.getLyric(oSongList.get(songPosition))
            val st = StringTokenizer(lyric,"\n")
            while(st.hasMoreElements()){
                lyricRows.add(st.nextToken())
            }
        }
//        val reader = BufferedReader(InputStreamReader(input))
//        var eof = false
//        var line: String? = null
////        val lyricRows = ArrayList<String>()
//        while (!eof){
//            line = reader.readLine()
//            if(line==null){
//                eof = true
//            }
//            else{
//                lyricRows.add(line)
//            }
//        }
//        reader.close()
//        input?.close()
        var index = 0
        while(index<lyricRows.size-1){
            var lyricLine = lyricRows.get(index++)
            val min = lyricLine.substring(1,lyricLine.indexOf(":"))
            val second = lyricLine.substring(lyricLine.indexOf(":")+1,lyricLine.indexOf("."))
            val millsecond = lyricLine.substring(lyricLine.indexOf(".")+1,lyricLine.indexOf("]"))
            val lyric = lyricLine.substring(lyricLine.indexOf("]")+1)
            val totalTime = min.toInt()*60*1000 + second.toInt()*1000 + millsecond.toInt()
            lyricRowsArray.add(""+totalTime+"%"+lyric)
        }
        return lyricRowsArray
    }

    private fun switchLyricName(songName: String):String{
        var fileName:String? = null
        when(songName){
            "Yellow"->fileName = "Coldplay - Yellow.lrc"
            "Hoppípolla"->fileName = "Sigur Rós - Hoppípolla.lrc"
            "暗号"->fileName = "暗号-周杰伦.lrc"
            "心做し"->fileName = "GUMI 一之瀬ユウ - 心做し.lrc"
        }
        return fileName!!
    }

    override fun onDestroy() {
        executor.shutdown()
        super.onDestroy()
    }

}