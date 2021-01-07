package org.edu.ncu.part1

import java.io.Serializable

data class Lyric(var sgc: Boolean,
                 var sfy: Boolean,
                 var qfy: Boolean,
                 var lrc: LyricData,
                 var klyric: KLyricData,
                 var tlyric: TLyricData,
                 var code: Int): Serializable {

    data class LyricData(var version: Int,
                         var lyric: String): Serializable

    data class KLyricData(var version: Int,
                          var lyric: String): Serializable

    data class TLyricData(var version: Int,
                          var lyric: String): Serializable

}