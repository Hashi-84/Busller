package jp.mirable.busller.classes

import android.app.Application
import android.content.Context
//Contextをどこでも使えるようにするクラス
class SingletonContext : Application() {
    init {
        instance = this
    }

    companion object {
        private var instance: SingletonContext? = null

        fun applicationContext() : Context {
            return instance!!.applicationContext
        }
    }
}