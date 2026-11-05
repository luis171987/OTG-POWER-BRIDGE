package bo.genai.usbpowerbridge

import android.content.Context
import android.net.Uri
import android.os.storage.StorageManager
import android.provider.DocumentsContract

/** Origen observado por Android. No convierte una tarjeta SD en USB ni infiere el modelo. */
data class StorageEvidence(val description: String, val removable: Boolean?)

object RealStorage {
    fun inspect(context: Context, uri: Uri): StorageEvidence {
        if (uri.authority != "com.android.externalstorage.documents") {
            return StorageEvidence("Proveedor de documentos; tipo físico sin confirmar", null)
        }
        val id = runCatching { DocumentsContract.getTreeDocumentId(uri) }.getOrNull()
            ?: return StorageEvidence("Volumen no identificado", null)
        val volumeId = id.substringBefore(':')
        if (volumeId.equals("primary", ignoreCase = true)) {
            return StorageEvidence("Almacenamiento interno (no es el pendrive)", false)
        }
        val volume = runCatching {
            val manager = context.getSystemService(Context.STORAGE_SERVICE) as? StorageManager
            manager?.storageVolumes?.firstOrNull {
                it.uuid?.equals(volumeId, ignoreCase = true) == true
            }
        }.getOrNull()
        return when {
            volume?.isRemovable == true -> StorageEvidence(
                "Almacenamiento extraíble confirmado por Android (USB o tarjeta SD)", true
            )
            volume != null -> StorageEvidence("Volumen indicado por Android; no extraíble", false)
            else -> StorageEvidence("Volumen ${volumeId.take(20)}: tipo físico sin confirmar", null)
        }
    }
}
