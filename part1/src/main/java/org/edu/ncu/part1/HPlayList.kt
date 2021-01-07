package org.edu.ncu.part1

import java.io.Serializable

data class HPlayList(var tags: List<Tag>,var code: Int): Serializable {

    data class Tag(var playlistTag: PlayListTag,
                   var activity: Boolean,
                   var usedCount: Int,
                   var hot: Boolean,
                   var position: Int,
                   var category: Int,
                   var createTime: Long,
                   var name: String,
                   var id: Int,
                   var type: Int): Serializable{

        data class PlayListTag(var id: Int,
                               var name: String,
                               var category: Int,
                               var usedCount: Int,
                               var type: Int,
                               var position: Int,
                               var createTime: Long,
                               var highQuality: Int,
                               var highQualityPos: Int,
                               var officialPos: Int): Serializable

    }

}