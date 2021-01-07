package org.edu.ncu.part1

import java.io.Serializable

//Int Long
data class PlayList(var code: Int,
                    var relatedVideos: String,
                    var playlist: PlayListData,
                    var urls: String,var privileges: List<Privilege>): Serializable {

    data class PlayListData(var subscribers: List<String>,
                            var subscribed: Boolean,
                            var creator: RPlayList.PlayLists.Creator,
                            var tracks: List<Track>,
                            var videoIds: String,
                            var videos: String,
                            var trackIds: List<TrackId>,
                            var updateFrequency: String,
                            var backgroundCoverId: Int,
                            var backgroundCoverUrl: String,
                            var titleImage: Int,
                            var titleImageUrl: String,
                            var englishTitle: String,
                            var opRecommend: Boolean,
                            var description: String,
                            var userId: Long,
                            var createTime: Long,
                            var highQuality: Boolean,
                            var adType: Int,
                            var trackNumberUpdateTime: Long,
                            var ordered: Boolean,
                            var tags: List<String>,
                            var subscribedCount: Int,
                            var specialType: Int,
                            var coverImgUrl: String,
                            var playCount: Int,
                            var updateTime: Long,
                            var newImported: Boolean,
                            var trackCount: Int,
                            var commentThreadId: String,
                            var privacy: Int,
                            var trackUpdateTime: Long,
                            var coverImgId: Long,
                            var cloudTrackCount: Int,
                            var status: Int,
                            var name: String,
                            var id: Long,
                            var shareCount: Int,
                            var commentCount: Int): Serializable{

        data class Track(var name: String,
                         var id: Long,
                         var pst: Int,
                         var t: Int,
                         var ar: List<Ar>,
                         var alia: List<String>,
                         var pop: Int,
                         var st: Int,
                         var rt: String,
                         var fee: Int,
                         var v: Int,
                         var crbt: String,
                         var cf: String,
                         var al: Al,
                         var dt: Int,
                         var h: H,
                         var m: H,
                         var l: H,
                         var a: String,
                         var cd: String,
                         var no: Int,
                         var rtUrl: String,
                         var ftype: Int,
                         var rtUrls: List<String>,
                         var djId: Int,
                         var copyright: Int,
                         var s_id: Int,
                         var mark: Int,
                         var originCoverType: Int,
                         var originSongSimpleData: String,
                         var single: Int,
                         var noCopyrightRcmd: String,
                         var mst: Int,
                         var cp: Int,
                         var mv: Int,
                         var rtype: Int,
                         var rurl: String,
                         var publishTime: Long): Serializable{

            data class Ar(var id: Int,
                          var name: String,
                          var tns: List<String>,
                          var alias: List<String>): Serializable

            data class Al(var id: Int,
                          var name: String,
                          var picUrl: String,
                          var tns: List<String>,
                          var pic_str: String,
                          var pic: Long): Serializable

            data class H(var br: Int,
                         var fid: Int,
                         var size: Int,
                         var vd: Double): Serializable

        }

        data class TrackId(var id: Int,
                           var v: Int,
                           var at: Long,
                           var alg: String): Serializable

    }

    data class Privilege(var id: Int,
                         var fee: Int,
                         var payed: Int,
                         var st: Int,
                         var pl: Int,
                         var dl: Int,
                         var sp: Int,
                         var cp: Int,
                         var subp: Int,
                         var cs: Boolean,
                         var maxbr: Int,
                         var fl: Int,
                         var toast: Boolean,
                         var flag: Int,
                         var preSell: Boolean,
                         var playMaxbr: Int,
                         var downloadMaxbr: Int,
                         var freeTrialPrivilege: TrialPrivilege,
                         var chargeInfoList: List<ChangeInfo>): Serializable{

        data class TrialPrivilege(var resConsumable: Boolean,
                                  var userConsumable: Boolean): Serializable

        data class ChangeInfo(var rate: Int,
                              var chargeUrl: String,
                              var chargeMessage: String,
                              var chargeType: Int): Serializable

    }

}