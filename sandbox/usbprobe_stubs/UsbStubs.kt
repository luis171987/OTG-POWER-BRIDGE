package android.hardware.usb

object UsbConstants { const val USB_CLASS_MASS_STORAGE = 8 }
class UsbInterface(val interfaceClass: Int)
class UsbDevice(val vendorId: Int, val productId: Int, val deviceClass: Int, val interfaces: List<UsbInterface>) {
    val interfaceCount: Int get() = interfaces.size
    fun getInterface(index: Int): UsbInterface = interfaces[index]
}
open class UsbManager {
    open val deviceList: Map<String, UsbDevice> = emptyMap()
}
