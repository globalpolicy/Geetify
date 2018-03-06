package com.geetify.s0ft.geetify.listview

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.TextView
import com.geetify.s0ft.geetify.R
import com.geetify.s0ft.geetify.datamodels.YoutubeSong

/**
 * Created by s0ft on 2/11/2018.
 */

class ListviewAdapter4Library(context: Context, listItemLayoutId: Int, listOfYoutubeSongs: ArrayList<YoutubeSong>) : ArrayAdapter<YoutubeSong>(context, listItemLayoutId, listOfYoutubeSongs) {

    var layoutInflater: LayoutInflater
    var listItemLayoutId: Int
    var listOfYoutubeSongs: ArrayList<YoutubeSong>

    init {
        this.layoutInflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        this.listItemLayoutId = listItemLayoutId
        this.listOfYoutubeSongs = listOfYoutubeSongs
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View? {
        val listRowView: View
        val listRowHolder: ListItemHolder


        if (null == convertView) {
            listRowView = layoutInflater.inflate(listItemLayoutId, null)
            listRowHolder = ListItemHolder(listRowView)
            listRowView.tag = listRowHolder
        } else {
            listRowView = convertView
            listRowHolder = listRowView.tag as ListItemHolder
        }
        listRowHolder.songTitleTextView?.text = listOfYoutubeSongs[position].title
        listRowHolder.songPublishedDateTextView?.text = listOfYoutubeSongs[position].publishedDate
        listRowHolder.songThumbnailImageView?.setImageBitmap(listOfYoutubeSongs[position].hqThumbnailBitmap)

        return listRowView
    }

    private class ListItemHolder(rowLayoutView: View?) {
        val songTitleTextView: TextView?
        val songPublishedDateTextView: TextView?
        val songThumbnailImageView: ImageView?

        init {
            this.songTitleTextView = rowLayoutView?.findViewById<TextView>(R.id.LibrarySongTitle)
            this.songPublishedDateTextView = rowLayoutView?.findViewById<TextView>(R.id.LibrarySongPublishedDate)
            this.songThumbnailImageView = rowLayoutView?.findViewById<ImageView>(R.id.LibrarySongBitmap)
        }

    }

}