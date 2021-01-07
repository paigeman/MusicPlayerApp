package org.edu.ncu.part1

import android.graphics.Bitmap
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.fragment_song_list.*
import java.io.Serializable

class OSongAdapter(val oSongList: List<OSSong>,val activity: FragmentActivity,val sBitmapList: List<Bitmap>): RecyclerView.Adapter<OSongAdapter.ViewHolder>() {

    private val TAG = "DEBUG"

//    val apiService = HttpUtils.create(IApi::class.java)
//
//    private var holderList = ArrayList<ViewHolder>()
//
//    private var sBitmapList: ArrayList<Bitmap> = ArrayList<Bitmap>()

//    private val viewHolderHandler = object : Handler(){
//
//        override fun handleMessage(msg: Message) {
//            val data = msg.data
//            val position = data.getInt("position")
//            Log.d(TAG,"positiondfsd=${position}")
//            val holder = holderList.get(position)
//            val bitmap = sBitmapList.get(position)
//            holder.songImage.setImageBitmap(bitmap)
//        }
//
//    }

    inner class ViewHolder(view: View):RecyclerView.ViewHolder(view){
        val songName: TextView = view.findViewById(R.id.songName)
        val songImage: ImageView = view.findViewById(R.id.songImage)
        val songIntroduction: TextView = view.findViewById(R.id.songIntroduction)
        val songIndex: TextView = view.findViewById(R.id.songIndex)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OSongAdapter.ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.song_item,parent,false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return oSongList.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val oSong = oSongList[position]
//        holder.songImage.setImageResource(song.imageId)
//        apiService.doNothing().subscribeOn(Schedulers.io())
//            .observeOn(Schedulers.newThread())
//            .map {
//
//            }
//            .doOnNext{
//
//            }
//            .subscribe{
//                val input = HttpUtils.getImageViewInputStream(oSong.imageUrl!!)
//                val bitmap = BitmapFactory.decodeStream(input)
//                sBitmapList.add(bitmap)
//                holderList.add(holder)
//                val msg = viewHolderHandler.obtainMessage()
//                val data = Bundle()
//                data.putInt("position",position)
//                msg.data = data
//                msg.sendToTarget()
////                holder.songImage.setImageBitmap(bitmap)
//            }
//        holderList.add(holder)
//        thread {
////            for (i in 0..oSongList.size-1) {
////                Log.d(TAG, i.toString())
////                Log.d(TAG, oSongList[i].imageUrl)
//                val input = HttpUtils.getImageViewInputStream(oSongList[position].imageUrl)
//                sBitmapList.add(BitmapFactory.decodeStream(input))
//                val msg = viewHolderHandler.obtainMessage()
//                val data = Bundle()
//                data.putInt("position", position)
//                msg.data = data
//                msg.sendToTarget()
////            }
//        }
        Log.d(TAG,"adpater=${position}")
        holder.songImage.setImageBitmap(sBitmapList.get(position))
        holder.songName.text = oSong.name
        holder.songIntroduction.text = oSong.introduction
        holder.songIndex.text = "${(position+1).toString()}"
        holder.itemView.setOnClickListener{
            val songFragment = SongPlayFragment()
            val data = Bundle()
            data.putString("type","online")
            data.putSerializable("sBitmapList",sBitmapList as Serializable)
            data.putSerializable("song",oSong)
            data.putSerializable("songList",oSongList as Serializable)
            data.putInt("position",position)
            data.putString("listName",activity.list_name.text as String)
            songFragment.arguments = data
            replaceFragment(songFragment)
        }
    }

    private fun replaceFragment(fragment: Fragment){
        val fragmentManager = activity.supportFragmentManager
        val transaction = fragmentManager.beginTransaction()
        transaction.replace(R.id.replace,fragment)
        transaction.commit()
    }

}