package org.edu.ncu.part1

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.util.Log
import android.view.*
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.drawer_header.*
import kotlinx.android.synthetic.main.fragment_index.*
import kotlinx.android.synthetic.main.index_tool.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.Serializable

class IndexFragment : Fragment() {

    private val TAG = "DEBUG"

    lateinit private var indexActivity: AppCompatActivity

    val apiService = HttpUtils.create(IApi::class.java)

    lateinit private var rPlayList: RPlayList

    lateinit private var hPlayList: HPlayList

    lateinit private var r_image_url: String

    lateinit private var bitmap: Bitmap

    lateinit private var songList: List<OSSong>

    lateinit private var listName: String

    private val imageHandler = object : Handler(){

        override fun handleMessage(msg: Message) {
            daily_image.setImageBitmap(bitmap)
        }

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
//        Log.d(TAG,"enter1")
        //TODO: add code below
        //获取每日推荐数据
//        apiService.getRPlayList().enqueue(object : Callback<RPlayList> {
//
//            override fun onResponse(call: Call<RPlayList>, response: Response<RPlayList>) {
////                Log.d(TAG,"enter")
//                rPlayList = response.body()!!
//                r_image_url = rPlayList.playlists[0].coverImgUrl
//                Log.d(TAG,"rPlayList.code=${rPlayList.code}")
//                Log.d(TAG,"r_image_url=${r_image_url}")
//            }
//
//            override fun onFailure(call: Call<RPlayList>, t: Throwable) {
//                Log.d(TAG,t.printStackTrace().toString())
//            }
//
//        })
        apiService.getRPlayList().subscribeOn(Schedulers.io())
                .observeOn(Schedulers.newThread())
                .map {
                    rPlayList = it
                    Log.d(TAG,"rPlayList.code=${rPlayList.code}")
                }.doOnNext{
//                    Log.d(TAG,it.toString())
                    songList = AnalyzeJsonUtils.analyzeRPlayList(rPlayList)
                    listName = AnalyzeJsonUtils.getPlayListName(rPlayList)
                    Log.d(TAG,"list_name=${listName}")
                    r_image_url = rPlayList.playlists[0].coverImgUrl
//                    Log.d(TAG,r_image_url)
                }.subscribe{
//                    Log.d(TAG,it.toString())
                    val input = HttpUtils.getImageViewInputStream(r_image_url)
                    bitmap = BitmapFactory.decodeStream(input)
                    val msg = imageHandler.obtainMessage()
                    msg.sendToTarget()
                }

        //获取热门歌单
        apiService.getHPlayList().enqueue(object : Callback<HPlayList>{

            override fun onResponse(call: Call<HPlayList>, response: Response<HPlayList>) {
                hPlayList = response.body()!!
                Log.d(TAG,"hPlayList.code=${hPlayList.code}")
            }

            override fun onFailure(call: Call<HPlayList>, t: Throwable) {
                Log.d(TAG,t.printStackTrace().toString())
            }

        })
        return inflater.inflate(R.layout.fragment_index, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        //TODO: add code below
        indexActivity = activity as AppCompatActivity
        setTranslucent(indexActivity)
        //设置ToolBar为自定义ToolBar
        indexActivity.setSupportActionBar(index_tool)
        setHasOptionsMenu(true)
//        val input = HttpUtils.getImageViewInputStream(r_image_url)
//        val bitmap = BitmapFactory.decodeStream(input)
//        daily_image.setImageBitmap(bitmap)
        val username = arguments?.getString("username")
        val status = arguments?.getInt("status")
        //设置点击导航键显示菜单
        index_tool.setNavigationOnClickListener {
            index_content.openDrawer(GravityCompat.START)
            if(status==1){
                user_name.text = username
            }
            user_image.setOnClickListener {
                replaceFragment(LoginFragment())
            }
        }
        val songListFragment = SongListFragment()
        val data = Bundle()
        //跳转至本地音乐
        local.setOnClickListener {
            data.putString("type","local")
            songListFragment.arguments = data
            replaceFragment(songListFragment)
        }
        //跳转至每日推荐
        daily.setOnClickListener {
            if(status==1){
                data.putString("type", "recommend")
                data.putSerializable("songList", songList as Serializable)
                Log.d(TAG,"list_name=${listName}")
                data.putString("listName",listName)
                songListFragment.arguments = data
                replaceFragment(songListFragment)
            }
            else{
                Toast.makeText(indexActivity,"请登录后再使用",Toast.LENGTH_SHORT).show()
            }
        }
        //跳转至热门单曲
        top.setOnClickListener {
//            data.putString("type","hot")
//            songListFragment.arguments = data
//            replaceFragment(songListFragment)
//            Log.d(TAG,hPlayList.code.toString())
        }

        //TODO: add code below
        //数据库
        val dbHelper = DBUtils(activity as AppCompatActivity,"music.db",2)
        dbHelper.writableDatabase


    }

    //透明状态栏
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
            val temp = activity.findViewById<View>(R.id.index_content) as ViewGroup
            val rootView = temp.getChildAt(0) as ViewGroup
            rootView.fitsSystemWindows = true
            rootView.clipToPadding = true
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        menu.clear()
        inflater.inflate(R.menu.bar_3, menu)

    }

    private fun replaceFragment(fragment: Fragment){
        val fragmentManager = indexActivity.supportFragmentManager
        val transaction = fragmentManager.beginTransaction()
        transaction.replace(R.id.replace,fragment)
        transaction.commit()
    }

}