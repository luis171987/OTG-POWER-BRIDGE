package bo.genai.usbpowerbridge

import android.content.Context
import android.net.Uri
import android.os.Build
import android.os.StatFs
import android.os.storage.StorageManager
import android.provider.DocumentsContract

/** Datos reales de Android. Extraíble NO significa necesariamente USB: podría ser una SD. */
data class StorageEvidence(
    val description: String,
    val removable: Boolean?,
    val volumeLabel: String? = null,
    val volumeId: String? = null,
    val totalBytes: Long? = null,
    val availableBytes: Long? = null
)

object RealStorage {
    fun inspect(context: Context, uri: Uri): StorageEvidence {
        if (uri.authority != "com.android.externalstorage.documents") {
            return StorageEvidence("Proveedor de documentos: medio físico no identificable", null)
        }
        val id = runCatching { DocumentsContract.getTreeDocumentId(uri) }.getOrNull()
            ?: return StorageEvidence("No se pudo identificar el volumen", null)
        val volumeId = id.substringBefore(':')
        val primary = volumeId.equals("primary", ignoreCase = true)
        val mgr = context.getSystemService(Context.STORAGE_SERVICE) as? StorageManager
        val volume = runCatching {
            mgr?.storageVolumes?.firstOrNull { v ->
                if (primary) v.isPrimary else v.uuid?.equals(volumeId, ignoreCase = true) == true
            }
        }.getOrNull()
        val removable = if (primary) false else volume?.isRemovable
        val label = runCatching { volume?.getDescription(context) }.getOrNull()
        var capacity: Long? = null
        var available: Long? = null
        // Solo medimos si Android expone la ruta del volumen; no intentamos eludir SAF.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R && volume != null) {
            val stats = runCatching {
                val path = volume.directory ?: return@runCatching null
                StatFs(path.absolutePath)
            }.getOrNull()
            if (stats != null) {
                capacity = runCatching { stats.totalBytes }.getOrNull()?.takeIf { it > 0 }
                available = runCatching { stats.availableBytes }.getOrNull()?.takeIf { it >= 0 }
            }
        }
        val description = when {
            primary -> "Almacenamiento interno del teléfono (no es memoria USB)"
            removable == true -> "Volumen extraíble reconocido por Android (USB o microSD)"
            removable == false -> "Volumen no extraíble"
            else -> "Volumen identificado, origen físico no confirmado"
        }
        return StorageEvidence(description, removable, label, volumeId, capacity, available)
    }
}
