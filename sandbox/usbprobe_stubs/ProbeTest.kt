import android.content.Context
import android.content.pm.PackageManager
import android.hardware.usb.UsbDevice
import android.hardware.usb.UsbInterface
import android.hardware.usb.UsbManager
import bo.genai.usbpowerbridge.UsbProbe

private class FakeManager(override val deviceList: Map<String, UsbDevice>): UsbManager()
private class FakeContext(private val manager: UsbManager?): Context() {
    override val packageManager: PackageManager = object: PackageManager() {
        override fun hasSystemFeature(feature: String): Boolean = true
    }
    override fun getSystemService(name: String): Any? = manager
}
fun main() {
    var count = 0
    fun verify(label: String, condition: Boolean) {
        check(condition) { label }; count++
    }
    val drive = UsbDevice(0x03f0, 0x0001, 0, listOf(UsbInterface(8)))
    val keyboard = UsbDevice(0x046d, 0x1234, 3, listOf(UsbInterface(3)))
    val snapshot = UsbProbe.read(FakeContext(FakeManager(mapOf("1" to drive, "2" to keyboard))))
    verify("host", snapshot.hostFeatureReported)
    verify("cantidad", snapshot.devices.size == 2)
    verify("msd", snapshot.devices[0].massStorageDescriptor)
    verify("no_msd", !snapshot.devices[1].massStorageDescriptor)
    verify("ids", snapshot.devices[0].vendorId == 0x03f0)
    val empty = UsbProbe.read(FakeContext(FakeManager(emptyMap())))
    verify("vacio", empty.devices.isEmpty() && empty.error == null)
    val missing = UsbProbe.read(FakeContext(null))
    verify("sin_servicio", missing.error != null)
    println("USB PROBE KOTLIN (dobles de Android): $count/$count pruebas aprobadas. No es prueba en Android.")
}
