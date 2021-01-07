package org.edu.ncu.part1

import android.animation.ObjectAnimator
import android.content.*
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.Matrix
import android.graphics.drawable.BitmapDrawable
import android.os.*
import android.util.Log
import android.view.*
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.view.animation.LinearInterpolator
import androidx.appcompat.app.AppCompatActivity
import androidx.core.graphics.drawable.RoundedBitmapDrawableFactory
import androidx.fragment.app.Fragment
import kotlinx.android.synthetic.main.fragment_song_play.*
import kotlinx.android.synthetic.main.play_tool.*


class SongPlayFragment : Fragment() {

    lateinit var type : String

    lateinit var playBinder : PlayService.PlayBinder

    lateinit private var songName: String

    lateinit private var songList: Array<Song>

    lateinit private var oSongList: ArrayList<OSSong>

    lateinit private var sBitmapList: List<Bitmap>

    lateinit private var rotateAnimator: ObjectAnimator

    private var songPosition: Int = 0

    private val TAG = "DEBUG"

    private var playActivity: AppCompatActivity = AppCompatActivity()

    private var clickTime = 0

    private val BROCAST_ACTION_PLAY = "org.edu.ncu.play"

    private val BROCAST_ACTION_BEFORE = "org.edu.ncu.before"

    private val BROCAST_ACTION_AFTER = "org.edu.ncu.after"

    private val progressHandler = object : Handler(){
        override fun handleMessage(msg: Message) {
            val data = msg.data
            val position = data.getInt("position")
            if(progress!=null){
                progress.progress = position
            }
        }
    }

    private val buttonPlayHandler = object : Handler(){
        override fun handleMessage(msg: Message) {
            val data = msg.data
            val action = data.getString("action")
            val duration = data.getInt("duration")
            Log.d(TAG,"action=${action}\tduration=${duration}")
            when(action){
                "play"->{
                    play.setImageResource(R.drawable.pause)
                    imageRotate(0,duration)
                }
                "pause"->{
                    play.setImageResource(R.drawable.play)
                    imageRotate(1,duration)
                }
                "finish"->{
                    play.setImageResource(R.drawable.play)
                    imageRotate(2,duration)
                }
            }
        }
    }

    private val PlayTimeInitHandler = object : Handler(){
        override fun handleMessage(msg: Message) {
            val data = msg.data
            val total = data.getString("totalTime")
            val current = data.getString("currentTime")
            val duration = data.getInt("duration")
            currentTime.text = current
            totalTime.text = total
            progress.progress = 0
            progress.max = duration
            imageRotate(2,0)
        }
    }

    private val currentTimeHandler = object : Handler(){
        override fun handleMessage(msg: Message) {
            val data = msg.data
            val current = data.getString("currentTime")
            if(currentTime!=null){
                currentTime.text = current
            }
        }
    }

    private val forwardOrBackPlayHandler = object : Handler(){
        override fun handleMessage(msg: Message) {
            val data = msg.data
            val current = data.getString("currentTime")
            val position = data.getInt("position")
//            Log.d(TAG,"current=${current}\tposition=${position}")
            currentTime.text = current
            progress.progress = position
        }
    }

    private val finishedHandler = object : Handler(){
        override fun handleMessage(msg: Message) {
            playBinder.afterPlay()
            songPosition  = (songPosition + 1)%(songList.size - 1)
            val song = songList[songPosition]
            val imageId = song.imageId
            songName = song.name
            val author = song.author
            initToolBar(songName, author)
            initBackground(playActivity, imageId)
            initWapaper(playActivity, imageId)
            play.setImageResource(R.drawable.play)
            playBinder.setSongName(songName,songPosition,songList)
            playBinder.init(PlayTimeInitHandler)
            play_lyric.text = ""
        }
    }

    private val lyricHandler = object : Handler(){
        override fun handleMessage(msg: Message) {
            val data = msg.data
            val lyric = data.getString("lyric")
            Log.d(TAG,"lyric=${lyric}")
            if(lyric!=null){
                play_lyric.text = lyric
            }
        }
    }

    inner class SongPlayFragmentBrocastReceiver: BroadcastReceiver(){

        override fun onReceive(context: Context?, intent: Intent?) {
            when(intent?.action){
                BROCAST_ACTION_PLAY->{
                    playBinder.isPlaying(buttonPlayHandler)
                    playBinder.updateProgress(progressHandler)
                    playBinder.updateCurrentTime(currentTimeHandler)
                    playBinder.updateLyric(lyricHandler)
                }
                BROCAST_ACTION_BEFORE->{
                    playBinder.beforePlay()
                    if(type=="local"){
                        songPosition  = (songPosition - 1)%(songList.size - 1)
                        val song = songList[songPosition]
                        val imageId = song.imageId
                        songName = song.name
                        val author = song.author
                        initToolBar(songName, author)
                        initBackground(playActivity, imageId)
                        initWapaper(playActivity, imageId)
                        playBinder.setSongName(songName,songPosition,songList)
                    }
                    else{
                        songPosition  = (songPosition - 1)%(oSongList.size - 1)
                        playBinder.setBitmapList(sBitmapList,oSongList.get(songPosition).playUrl)
                        val song = oSongList[songPosition]
                        songName = song.name
                        val author = song.author
                        initToolBar(songName, author)
                        initBackground(playActivity, songPosition)
                        initWapaper(playActivity, songPosition)
                        playBinder.setOSongName(songName,songPosition,oSongList)
                    }
                    play.setImageResource(R.drawable.play)
                    playBinder.init(PlayTimeInitHandler)
                    play_lyric.text = ""
                }
                BROCAST_ACTION_AFTER->{
                    playBinder.afterPlay()
                    if(type=="local"){
                        songPosition  = (songPosition + 1)%(songList.size - 1)
                        val song = songList[songPosition]
                        val imageId = song.imageId
                        songName = song.name
                        val author = song.author
                        initToolBar(songName, author)
                        initBackground(playActivity, imageId)
                        initWapaper(playActivity, imageId)
                        playBinder.setSongName(songName,songPosition,songList)
                    }
                    else{
                        songPosition  = (songPosition + 1)%(oSongList.size - 1)
                        playBinder.setBitmapList(sBitmapList,oSongList.get(songPosition).playUrl)
                        val song = oSongList[songPosition]
                        songName = song.name
                        val author = song.author
                        initToolBar(songName, author)
                        initBackground(playActivity, songPosition)
                        initWapaper(playActivity, songPosition)
                        playBinder.setOSongName(songName,songPosition,oSongList)
                    }
                    play.setImageResource(R.drawable.play)
                    playBinder.init(PlayTimeInitHandler)
                    play_lyric.text = ""
                }
            }
        }

    }

    lateinit private var songPlayFragmentBrocastReceiver: SongPlayFragmentBrocastReceiver

//    private val connection = object : ServiceConnection{
//
//        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
////            TODO("Not yet implemented")
//            rotateAnimator = ObjectAnimator.ofFloat(play_wallpaper,"rotation",0f,360f)
//            playBinder = service as PlayService.PlayBinder
////            Log.d(TAG,"songPosition1=${songPosition}")
//            playBinder.setSongName(songName,songPosition,songList)
//            playBinder.init(PlayTimeInitHandler)
//            playBinder.isFinish(buttonPlayHandler,finishedHandler)
//            play.setOnClickListener {
////                Log.d(TAG,"可以设置监听")
//                playBinder.play(buttonPlayHandler)
//                playBinder.updateProgress(progressHandler)
//                playBinder.updateCurrentTime(currentTimeHandler)
//                playBinder.updateLyric(lyricHandler)
//            }
//            forward.setOnClickListener {
////                Log.d(TAG,"触发了forward点击事件")
//                playBinder.forwardPlay(forwardOrBackPlayHandler)
//            }
//            back.setOnClickListener {
//                playBinder.backPlay(forwardOrBackPlayHandler)
//            }
//            after.setOnClickListener {
//                playBinder.afterPlay()
//                songPosition  = (songPosition + 1)%(songList.size - 1)
//                val song = songList[songPosition]
//                val imageId = song.imageId
//                songName = song.name
//                val author = song.author
//                initToolBar(songName, author)
//                initBackground(playActivity, imageId)
//                initWapaper(playActivity, imageId)
//                play.setImageResource(R.drawable.play)
//                playBinder.setSongName(songName,songPosition,songList)
//                playBinder.init(PlayTimeInitHandler)
//                play_lyric.text = ""
//            }
//            before.setOnClickListener {
//                playBinder.beforePlay()
//                songPosition  = (songPosition - 1)%(songList.size - 1)
//                val song = songList[songPosition]
//                val imageId = song.imageId
//                songName = song.name
//                val author = song.author
//                initToolBar(songName, author)
//                initBackground(playActivity, imageId)
//                initWapaper(playActivity, imageId)
//                play.setImageResource(R.drawable.play)
//                playBinder.setSongName(songName,songPosition,songList)
//                playBinder.init(PlayTimeInitHandler)
//                play_lyric.text = ""
//            }
//        }
//
//        override fun onServiceDisconnected(name: ComponentName?) {
////            TODO("Not yet implemented")
//
//        }
//
//    }

    fun imageRotate(choice: Int,duration: Int){
        if(choice==0){
//            val rotateAnimator = ObjectAnimator.ofFloat(play_wallpaper,"rotation",0f,360f)
            rotateAnimator.setDuration(10000)
            val animatorDuration = rotateAnimator.duration
            val count = (duration / animatorDuration) + 1
            rotateAnimator.repeatCount = count.toInt()
            if(clickTime==0){
                rotateAnimator.start()
            }
            else{
                rotateAnimator.resume()
            }
            clickTime++
        }
        else if(choice==1){
            rotateAnimator.pause()
            clickTime++
        }
        else{
            rotateAnimator.end()
            clickTime = 0
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_song_play, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        //TODO: add code below
        playActivity = activity as AppCompatActivity
        setTranslucent(playActivity)
        playActivity.setSupportActionBar(play_tool)
        type = arguments?.getString("type")!!
        songPosition = arguments?.getInt("position")!!
        if(type=="local"){
            val data = arguments?.getSerializable("song") as Song
            songList = arguments?.getSerializable("songList") as Array<Song>
//            songPosition = arguments?.getInt("position")!!
            val imageId = data.imageId
            val name = data.name
            songName = name
            val author = data.author
            initToolBar(name, author)
            initBackground(playActivity, imageId)
            initWapaper(playActivity, imageId)
        }
        else{
            val data = arguments?.getSerializable("song") as OSSong
            oSongList = arguments?.getSerializable("songList") as ArrayList<OSSong>
            sBitmapList = arguments?.getSerializable("sBitmapList") as ArrayList<Bitmap>
            val name = data.name
            songName = name
            val author = data.author
            initToolBar(name, author)
            initBackground(playActivity,songPosition)
            initWapaper(playActivity,songPosition)
        }
//        val data = arguments?.getSerializable("song") as Song
//        songList = arguments?.getSerializable("songList") as Array<Song>
//        songPosition = arguments?.getInt("position")!!
////        Log.d(TAG, "data:${data.toString()}")
//        val imageId = data.imageId
//        val name = data.name
//        songName = name
//        val author = data.author
//        initToolBar(name, author)
//        initBackground(playActivity, imageId)
//        initWapaper(playActivity, imageId)

        val connection = object : ServiceConnection{

            override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
//            TODO("Not yet implemented")
                rotateAnimator = ObjectAnimator.ofFloat(play_wallpaper,"rotation",0f,360f)
                playBinder = service as PlayService.PlayBinder
//            Log.d(TAG,"songPosition1=${songPosition}")
                playBinder.setType(type)
                if(type=="online"){
                    playBinder.setBitmapList(sBitmapList,oSongList.get(songPosition).playUrl)
                }
                if(type=="local"){
                    playBinder.setSongName(songName,songPosition,songList)
                }
                else{
                    playBinder.setOSongName(songName,songPosition,oSongList)
                }
                playBinder.init(PlayTimeInitHandler)
                playBinder.isFinish(buttonPlayHandler,finishedHandler)
                play.setOnClickListener {
//                Log.d(TAG,"可以设置监听")
                    playBinder.play(buttonPlayHandler)
                    playBinder.updateProgress(progressHandler)
                    playBinder.updateCurrentTime(currentTimeHandler)
                    playBinder.updateLyric(lyricHandler)
                }
                forward.setOnClickListener {
//                Log.d(TAG,"触发了forward点击事件")
                    playBinder.forwardPlay(forwardOrBackPlayHandler)
                }
                back.setOnClickListener {
                    playBinder.backPlay(forwardOrBackPlayHandler)
                }
                after.setOnClickListener {
                    playBinder.afterPlay()
                    if(type=="local"){
                        songPosition  = (songPosition + 1)%(songList.size - 1)
                        var song = songList[songPosition]
                        val imageId = song.imageId
                        songName = song.name
                        val author = song.author
                        initToolBar(songName, author)
                        initBackground(playActivity, imageId)
                        initWapaper(playActivity, imageId)
                        playBinder.setSongName(songName,songPosition,songList)
                    }
                    else{
                        songPosition  = (songPosition + 1)%(oSongList.size - 1)
                        playBinder.setBitmapList(sBitmapList,oSongList.get(songPosition).playUrl)
                        var song = oSongList[songPosition]
                        songName = song.name
                        val author = song.author
                        initToolBar(songName, author)
                        initBackground(playActivity, songPosition)
                        initWapaper(playActivity, songPosition)
                        playBinder.setOSongName(songName,songPosition,oSongList)
                    }
                    play.setImageResource(R.drawable.play)
                    playBinder.init(PlayTimeInitHandler)
                    play_lyric.text = ""
                }
                before.setOnClickListener {
                    playBinder.beforePlay()
                    if(type=="local"){
                        songPosition  = (songPosition - 1)%(songList.size - 1)
                        val song = songList[songPosition]
                        val imageId = song.imageId
                        songName = song.name
                        val author = song.author
                        initToolBar(songName, author)
                        initBackground(playActivity, imageId)
                        initWapaper(playActivity, imageId)
                        playBinder.setSongName(songName,songPosition,songList)
                    }
                    else{
                        songPosition  = (songPosition - 1)%(oSongList.size - 1)
                        playBinder.setBitmapList(sBitmapList,oSongList.get(songPosition).playUrl)
                        val song = oSongList[songPosition]
                        songName = song.name
                        val author = song.author
                        initToolBar(songName, author)
                        initBackground(playActivity, songPosition)
                        initWapaper(playActivity, songPosition)
                        playBinder.setOSongName(songName,songPosition,oSongList)
                    }
                    play.setImageResource(R.drawable.play)
                    playBinder.init(PlayTimeInitHandler)
                    play_lyric.text = ""
                }
            }

            override fun onServiceDisconnected(name: ComponentName?) {
//            TODO("Not yet implemented")

            }

        }

        val intent = Intent(activity,PlayService::class.java)
        playActivity.bindService(intent,connection,Context.BIND_AUTO_CREATE)
        //注册广播
        val intentFilter = IntentFilter()
        intentFilter.addAction(BROCAST_ACTION_PLAY)
        intentFilter.addAction(BROCAST_ACTION_AFTER)
        intentFilter.addAction(BROCAST_ACTION_BEFORE)
        songPlayFragmentBrocastReceiver = SongPlayFragmentBrocastReceiver()
        playActivity.registerReceiver(songPlayFragmentBrocastReceiver,intentFilter)
        play_tool.setNavigationOnClickListener {
            val fragment = SongListFragment()
            val data = Bundle()
            if(type=="local"){
                data.putString("type","local")
            }
            else{
                data.putString("type", "recommend")
                data.putSerializable("songList", oSongList)
                data.putString("listName",arguments?.getString("listName"))
            }
            fragment.arguments = data
            replaceFragment(fragment)
        }
    }

    private fun initBackground(context: Context, imageId: Int){
        if(type=="local"){
            val bitmap = BitmapFactory.decodeResource(resources, imageId)
            val blurBitmap = ImageFilter.blurBitmap(context, bitmap, 25f)
            play_content.background = BitmapDrawable(blurBitmap)
        }
        else{
            val blurBitmap = ImageFilter.blurBitmap(context, sBitmapList.get(imageId), 25f)
            play_content.background = BitmapDrawable(blurBitmap)
        }
    }

    private fun initToolBar(name: String, author: String){
        play_tool.title = name
        play_tool.setTitleTextColor(Color.WHITE)
        play_tool.subtitle = author
        setHasOptionsMenu(true)
    }

    //获取圆形图片
    private fun initWapaper(activity: AppCompatActivity, imageId: Int){
//        Log.d(TAG, "imageId==R.mipmap.song1${imageId == R.mipmap.song5}")
        val width = BitmapFactory.decodeResource(resources, R.mipmap.song5).width
        val height = BitmapFactory.decodeResource(resources, R.mipmap.song5).height
        var bitmap = BitmapFactory.decodeResource(resources, imageId)
        if(type=="online"){
            bitmap = sBitmapList.get(imageId)
        }
        val matrix = Matrix()
        matrix.postScale(0.35f * width / bitmap.width, 0.35f * height / bitmap.height)
        val small = Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
        val circleDrawable = RoundedBitmapDrawableFactory.create(resources, small)
        circleDrawable.setAntiAlias(true)
        circleDrawable.cornerRadius = Math.max(bitmap.getWidth(), bitmap.getHeight()).toFloat()
        play_wallpaper.setImageDrawable(circleDrawable)
        play_tool.bringToFront()
    }

    private fun setTranslucent(activity: AppCompatActivity){
        //Build.VERSION.SDK_INT
        //The SDK version of the software currently running on this hardware device
        //Build.VERSION_CODES.KITKAT
        //Enumeration of the currently known SDK version codes.
        //October 2013: Android 4.4, KitKat, another tasty treat.
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT){
            //设置状态栏透明
            activity.window.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
            //设置根布局的参数
            val temp = activity.findViewById<View>(R.id.play_content) as ViewGroup
            val rootView = temp.getChildAt(0) as ViewGroup
            rootView.fitsSystemWindows = true
            rootView.clipToPadding = true
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        menu.clear()
        inflater.inflate(R.menu.bar_2, menu)
    }

    override fun onDestroy() {
        super.onDestroy()
    }

    private fun replaceFragment(fragment: Fragment){
        val fragmentManager = activity?.supportFragmentManager
        val transaction = fragmentManager?.beginTransaction()
        transaction?.replace(R.id.replace,fragment)
        transaction?.commit()
    }

}