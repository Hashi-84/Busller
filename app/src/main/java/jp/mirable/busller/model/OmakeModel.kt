package jp.mirable.busller.model

import android.media.MediaPlayer
import android.util.Log
import jp.mirable.busller.classes.SingletonContext
import java.lang.Exception

class OmakeModel {
    fun loadSoundFile(fileName: String, loop: Boolean) : MediaPlayer{
        val mp = MediaPlayer()
        try {
            val afd = SingletonContext.applicationContext().assets.openFd(fileName)
            mp.setDataSource(afd.fileDescriptor, afd.startOffset, afd.length)
            mp.isLooping = loop
        } catch (e: Exception) { Log.d("Err:", "$e") }
        return mp
    }
}