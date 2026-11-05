package bo.genai.usbpowerbridge

import android.content.Context
import android.content.pm.PackageManager
import android.hardware.usb.UsbConstants
import android.hardware.usb.UsbManager

/** Enumeración USB real, sin asumir que un descriptor corresponde al volumen SAF elegido. */
data class UsbDeviceSummary(
    val vendorId: Int,
    val productId: Int,
    val deviceClass: Int,
    val interfaceCount: Int,
    val massStorageDescriptor: Boolean,
    val manufacturer: String? = null,
    val product: String? = null
)

data class UsbSnapshot(
    val hostFeatureReported: Boolean,
    val devices: List<UsbDeviceSummary>,
    val error: String? = null
)

object UsbProbe {
    fun read(context: Context): UsbSnapshot {
        val host = context.packageManager.hasSystemFeature(PackageManager.FEATURE_USB_HOST)
        return try {
            val manager = context.getSystemService(Context.USB_SERVICE) as? UsbManager
                ?: return UsbSnapshot(host, emptyList(), "Servicio USB no disponible")
            val devices = manager.deviceList.values.map { dev ->
                val storage = dev.deviceClass == UsbConstants.USB_CLASS_MASS_STORAGE ||
                    (0 until dev.interfaceCount).any { i ->
                        dev.getInterface(i).interfaceClass == UsbConstants.USB_CLASS_MASS_STORAGE
                    }
                UsbDeviceSummary(
                    dev.vendorId, dev.productId, dev.deviceClass, dev.interfaceCount,
                    storage,
                    runCatching { dev.manufacturerName?.trim()?.takeIf { it.isNotEmpty() } }.getOrNull(),
                    runCatching { dev.productName?.trim()?.takeIf { it.isNotEmpty() } }.getOrNull()
                )
            }.sortedWith(compareBy<UsbDeviceSummary> { it.vendorId }.thenBy { it.productId })
            UsbSnapshot(host, devices)
        } catch (_: SecurityException) {
            UsbSnapshot(host, emptyList(), "Android no autorizó enumerar dispositivos USB")
        } catch (e: RuntimeException) {
            UsbSnapshot(host, emptyList(), "Consulta USB: ${e.javaClass.simpleName}")
        }
    }
}
