package com.chetantuteja.pocketredditreader

import android.util.Log


class ExtractXML(var xml: String, var tag: String, var endtag: String) {

    companion object {
        private const val TAG: String = "ExtractXML"
    }

    constructor(xml: String, tag: String): this(xml, tag, "NONE") {
    }



    /*private var tag = tag
    private var xml = xml*/

    fun start(): ArrayList<String?> {
        val result = ArrayList<String?>()
        var splitXML: List<String>
        var marker: String? = null

        if(endtag == "NONE") {
            marker = "\""
            splitXML = xml.split(tag+marker)
        } else {
            marker = endtag
            splitXML = xml.split(tag)
        }

        //val splitXML = xml.split(tag+"\"")

        for(i in 1 until splitXML.size){
            var temp = splitXML[i]
            val index = temp.indexOf(marker)
            Log.d(TAG, "start: index: $index ")
            Log.d(TAG, "start: extraced $temp ")

            temp = temp.substring(0,index)
            Log.d(TAG, "start: Splitted: $temp ")
            result.add(temp)
        }

        return result
    }

}