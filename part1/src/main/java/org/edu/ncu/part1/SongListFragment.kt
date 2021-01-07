package org.edu.ncu.part1

import android.content.Context
import android.content.res.TypedArray
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.BitmapDrawable
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.util.Log
import android.view.*
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.fragment_song_list.*
import kotlinx.android.synthetic.main.fragment_song_list.list_cover
import kotlinx.android.synthetic.main.list_tool.*
import kotlinx.android.synthetic.main.song_item.view.*
import kotlin.concurrent.thread

class SongListFragment : Fragment() {

    private val TAG = "DEBUG"

    var listName = ""

    private val songList = ArrayList<Song>()

    lateinit private var oSongList : ArrayList<OSSong>

    private var songAdapter:SongAdapter? = null

    private var oSongAdapter:OSongAdapter? = null

    lateinit private var listActivity: AppCompatActivity

    lateinit private var oBitmap: Bitmap

    private var sBitmapList: ArrayList<Bitmap> = ArrayList<Bitmap>()

    private val imageHandler = object : Handler(){

        override fun handleMessage(msg: Message) {
            list_top.background = BitmapDrawable(oBitmap)
        }

    }

    private val viewHolderHandler = object : Handler(){

        override fun handleMessage(msg: Message) {
//            val data = msg.data
//            val position = data.getInt("position")
//            Log.d(TAG,"position=${position}")
//            recycleView.findViewHolderForAdapterPosition(position)?.itemView?.songImage?.setImageBitmap(sBitmapList.get(position))
            oSongAdapter = OSongAdapter(oSongList,activity as AppCompatActivity,sBitmapList)
            recycleView.adapter = oSongAdapter
            list_cover.setImageBitmap(sBitmapList.get(0))
        }

    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_song_list,container,false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // TODO: add code below
        val aty = activity
        listActivity = activity as AppCompatActivity
        setTranslucent(listActivity)
        listActivity.setSupportActionBar(list_tool)
        setHasOptionsMenu(true)
        val layoutManager = LinearLayoutManager(activity)
        recycleView.layoutManager = layoutManager
        val type = arguments?.getString("type")
        if(type=="local"){
            initialSongs(listActivity)
            songAdapter = SongAdapter(songList,aty!!)
            recycleView.adapter = songAdapter
        }
        else if(type=="recommend"){
            list_tool.title = "推荐歌单"
            oSongList = arguments?.getSerializable("songList") as ArrayList<OSSong>
//            oSongAdapter = OSongAdapter(oSongList,aty!!)
//            recycleView.adapter = oSongAdapter
            listName = arguments?.getString("listName")!!
            list_name.text = arguments?.getString("listName")
            initOSSongs(listActivity)
            initViewHolder()
        }
        else{
//            hPlayList = arguments?.getSerializable("hPlayList") as HPlayList
        }
        //recycleView.addOnScrollListener(ListScrollListener())
        list_tool.setNavigationOnClickListener {
//            Log.d(TAG,"已点击导航按钮")
            replaceFragment(IndexFragment())
        }
    }

    private fun initialSongs(context: Context){
        val songNameArray: Array<String> = resources.getStringArray(R.array.songName)
        val songAuthorArray: Array<String> = resources.getStringArray(R.array.songAuthor)
        val songImageIdArray: TypedArray = resources.obtainTypedArray(R.array.songImage)
        val songIntroduction: Array<String> = resources.getStringArray(R.array.songDescription)
        list_cover.setImageResource(songImageIdArray.getResourceId(0,R.mipmap.song1))
        for(i in 0..(songNameArray.size-1)){
            songList.add(Song(songNameArray[i],songAuthorArray[i],songImageIdArray.getResourceId(i,R.mipmap.song1),songIntroduction[i]))
        }
        // 设置高斯模糊图
        val bitmap = BitmapFactory.decodeResource(resources,songImageIdArray.getResourceId(0,R.mipmap.song1))
        val blurBitmap = ImageFilter.blurBitmap(context,bitmap,25f)
        list_top.background = BitmapDrawable(blurBitmap)
    }

    private fun initViewHolder(){
        thread {
            for (i in 0..oSongList.size-1){
                Log.d(TAG,i.toString())
                Log.d(TAG,oSongList[i].imageUrl)
                val input = HttpUtils.getImageViewInputStream(oSongList[i].imageUrl)
                sBitmapList.add(BitmapFactory.decodeStream(input))
//                val msg = viewHolderHandler.obtainMessage()
//                val data = Bundle()
////                data.putInt("position",i)
//                msg.data = data
//                msg.sendToTarget()
            }
            val msg = viewHolderHandler.obtainMessage()
            val data = Bundle()
            msg.data = data
            msg.sendToTarget()
            Log.d(TAG,"lsitSize=${sBitmapList.size}")
        }
    }

    private fun initOSSongs(context: Context){
        thread {
            val input = HttpUtils.getImageViewInputStream(oSongList[0].imageUrl)
            val bitmap = BitmapFactory.decodeStream(input)
            oBitmap = ImageFilter.blurBitmap(context,bitmap,25f)
            val msg = imageHandler.obtainMessage()
            msg.sendToTarget()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        menu.clear()
        inflater.inflate(R.menu.bar_1,menu)
    }

    /*
    * 沉浸式状态栏
    * */

    fun setTranslucent(activity: AppCompatActivity){
        //Build.VERSION.SDK_INT
        //The SDK version of the software currently running on this hardware device
        //Build.VERSION_CODES.KITKAT
        //Enumeration of the currently known SDK version codes.
        //October 2013: Android 4.4, KitKat, another tasty treat.
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT){
            //设置状态栏透明
            activity.window.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
            //设置根布局的参数
            val temp = activity.findViewById<View>(R.id.list_content_main) as ViewGroup
            val rootView = temp.getChildAt(0) as ViewGroup
            rootView.fitsSystemWindows = true
            rootView.clipToPadding = true
        }
    }

//    inner class ListScrollListener:RecyclerView.OnScrollListener(){
//
//        override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
//            super.onScrollStateChanged(recyclerView, newState)
//        }
//
//        override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
//            super.onScrolled(recyclerView, dx, dy)
//            //TODO: add code below
//            var headView:View? = null
//            val manager = recyclerView.layoutManager as LinearLayoutManager
//            val firstVisibleItem = manager.findFirstVisibleItemPosition()
//            if(firstVisibleItem == 0){
//                headView = recycleView.getChildAt(firstVisibleItem)
//            }
//            if(headView==null){
//                return
//            }
//            val alpha = Math.abs(headView.top) * 1.0f / headView.height
//            val drawable = list_top.background
//            if(drawable!=null){
//                drawable.mutate().alpha = (alpha * 255).toInt()
//                list_top.background = drawable
//            }
//            list_head.visibility = View.GONE
//            Log.d(TAG,"dx:${dx},dy:${dy}")
//        }
//
//    }

    private fun replaceFragment(fragment: Fragment){
        val fragmentManager = listActivity.supportFragmentManager
        val transaction = fragmentManager.beginTransaction()
        transaction.replace(R.id.replace,fragment)
        transaction.commit()
    }

}