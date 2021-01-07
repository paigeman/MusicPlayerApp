package org.edu.ncu.part1

import java.io.Serializable

data class OSong(var data: List<SongData>,
                 var code: Int): Serializable {

    data class SongData(var id: Int,
                        var url: String,
                        var br: Int,
                        var size: Int,
                        var md5: String,
                        var code: Int,
                        var expi: Int,
                        var type: String,
                        var gain: Int,
                        var fee: Int,
                        var uf: String,
                        var payed: Int,
                        var flag: Int,
                        var canExtend: Boolean,
                        var freeTrialInfo: Object,
                        var level: String,
                        var encodeType: String,
                        var freeTrialPrivilege: PlayList.Privilege.TrialPrivilege,
                        var urlSource: Int): Serializable

}