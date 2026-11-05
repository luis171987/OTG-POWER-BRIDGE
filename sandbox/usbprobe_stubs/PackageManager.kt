package android.content.pm
open class PackageManager {
    companion object { const val FEATURE_USB_HOST: String = "android.hardware.usb.host" }
    open fun hasSystemFeature(feature: String): Boolean = false
}
