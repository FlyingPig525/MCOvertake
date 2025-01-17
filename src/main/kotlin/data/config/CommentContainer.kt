package io.github.flyingpig525.data.config

import io.github.flyingpig525.json
import kotlinx.serialization.Contextual
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.*

interface CommentContainer {
    val comments: List<String>
}

fun getCommentString(obj: Config): String {
    val jsonArray = json.encodeToJsonElement(obj).jsonObject
    var str = "{"
    var i = 0
    for ((key, el) in jsonArray) {
        str += "\n\t" + Config.comments[i] + "\n\t"
        str += "\"$key\": ${json.encodeToString(el).replace("\n", "\n\t")},"
        i++
    }
    str = str.dropLast(1)
    str += "\n}"
    return str
}

fun getCommentString(obj: InstanceConfig): String {
    val jsonArray = json.encodeToJsonElement(obj).jsonObject
    var str = "{"
    var i = 0
    for ((key, el) in jsonArray) {
        str += "\n\t" + InstanceConfig.comments[i] + "\n\t"
        str += "\"$key\": ${json.encodeToString(el).replace("\n", "\n\t")},"
        i++
    }
    str = str.dropLast(1)
    str += "\n}"
    return str
}

fun main() {
    println(getCommentString(Config()))
    println("")
    println(getCommentString(InstanceConfig()))
}