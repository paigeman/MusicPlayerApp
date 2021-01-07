package org.edu.ncu.part1

import java.io.Serializable

data class OSSong(var id: Long,
                  var name: String,
                  var author: String,
                  var imageUrl: String,
                  var introduction: String,
                  var playUrl: String): Serializable