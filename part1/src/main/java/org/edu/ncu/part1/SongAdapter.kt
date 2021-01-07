package org.edu.ncu.part1

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

class SongAdapter(val songList:List<Song>,val activity: FragmentActivity):RecyclerView.Adapter<SongAdapter.ViewHolder>() {

    private val TAG = "DEBUG"

    inner class ViewHolder(view: View):RecyclerView.ViewHolder(view){
        val songName: TextView = view.findViewById(R.id.songName)
        val songImage: ImageView = view.findViewById(R.id.songImage)
        val songIntroduction: TextView = view.findViewById(R.id.songIntroduction)
        val songIndex: TextView = view.findViewById(R.id.songIndex)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder{
        val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.song_item,parent,false)
        //TODO: add code below
        //点击事件
//        val viewHolder = ViewHolder(view)
//        viewHolder.itemView.setOnClickListener {
//            val position = viewHolder.adapterPosition
//            Log.d(TAG,"position:${position}")
//            val song = songList[position+1]
//            val songFragment = SongPlayFragment()
//            val data = Bundle()
//            data.putSerializable("song",song)
//            songFragment.arguments = data
//            replaceFragment(songFragment)
//        }
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val song = songList[position]
        holder.songImage.setImageResource(song.imageId)
        holder.songName.text = song.name
        holder.songIntroduction.text = song.introduction
        holder.songIndex.text = "${(position+1).toString()}"
        holder.itemView.setOnClickListener {
            val songFragment = SongPlayFragment()
            val data = Bundle()
            data.putString("type","local")
            data.putSerializable("song",song)
            data.putSerializable("songList",songList.toTypedArray())
            data.putInt("position",position)
            songFragment.arguments = data
            replaceFragment(songFragment)
        }
    }

    override fun getItemCount(): Int {
        return songList.size
    }

    private fun replaceFragment(fragment: Fragment){
        val fragmentManager = activity.supportFragmentManager
        val transaction = fragmentManager.beginTransaction()
        transaction.replace(R.id.replace,fragment)
        transaction.commit()
    }

}