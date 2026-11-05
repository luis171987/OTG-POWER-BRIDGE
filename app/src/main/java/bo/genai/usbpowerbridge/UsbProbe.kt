package bo.genai.usbpowerbridge

import android.content.Context
import android.content.pm.PackageManager
import android.hardware.usb.UsbConstants
import android.hardware.usb.UsbManager

/** Diagnóstico de enumeración USB. No fuerza el modo host, PD ni permisos de almacenamiento. */
data class UsbDeviceSummary(
    val vendorId: Int,
    val productId: Int,
    val deviceClass: Int,
    val interfaceCount: Int,
    val massStorageDescriptor: Boolean
)

data class UsbSnapshot(
    val hostFeatureReported: Boolean,
    val devices: List<UsbDeviceSummary>,
    val error: String? = null
)

object UsbProbe {
    fun read(context: Context): UsbSnapshot {
        val supportsHost = context.packageManager.hasSystemFeature(PackageManager.FEATURE_USB_HOST)
        return try {
            val manager = context.getSystemService(Context.USB_SERVICE) as? UsbManager
                ?: return UsbSnapshot(supportsHost, emptyList(), "Servicio USB no disponible")
            val devices = manager.deviceList.values.map { dev ->
                val isMassStorage = dev.deviceClass == UsbConstants.USB_CLASS_MASS_STORAGE ||
                    (0 until dev.interfaceCount).any { index ->
                        dev.getInterface(index).interfaceClass == UsbConstants.USB_CLASS_MASS_STORAGE
                    }
                UsbDeviceSummary(dev.vendorId, dev.productId, dev.deviceClass,
                    dev.interfaceCount, isMassStorage)
            }.sortedWith(compareBy<UsbDeviceSummary> { it.vendorId }.thenBy { it.productId })
            UsbSnapshot(supportsHost, devices)
        } catch (e: SecurityException) {
            UsbSnapshot(supportsHost, emptyList(), "Sin autorización para enumerar USB")
        } catch (e: RuntimeException) {
            UsbSnapshot(supportsHost, emptyList(), "Error de consulta USB: ${e.javaClass.simpleName}")
        }
    }
}
