package android.content

import android.content.pm.PackageManager
open class Context {
    companion object { const val USB_SERVICE: String = "usb" }
    open val packageManager = PackageManager()
    open fun getSystemService(name: String): Any? = null
}
