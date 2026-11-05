package bo.genai.usbpowerbridge

import android.content.ContentResolver
import androidx.documentfile.provider.DocumentFile
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.security.MessageDigest
import java.nio.ByteBuffer
import java.nio.charset.CodingErrorAction

/** Operaciones SAF: no usa permisos globales de almacenamiento ni acceso root. */
object SafeDocumentOps {
    private const val MAX_TEXT_BYTES = 256 * 1024
    private val textExt = setOf("txt", "md", "csv", "log", "json", "xml", "yaml", "yml", "ini", "kt", "py", "html")

    /** Hash real calculado con lectura por bloques; no edita ni genera archivos. */
    fun checksum(resolver: ContentResolver, file: DocumentFile): String {
        require(file.isFile) { "Selecciona un archivo real." }
        val digest = MessageDigest.getInstance("SHA-256")
        resolver.openInputStream(file.uri)?.use { input ->
            val buffer = ByteArray(64 * 1024)
            while (true) {
                val size = input.read(buffer)
                if (size < 0) break
                if (size > 0) digest.update(buffer, 0, size)
            }
        } ?: throw IOException("No fue posible abrir el archivo")
        return digest.digest().joinToString("") { "%02x".format(it.toInt() and 0xff) }
    }

    fun isEditableText(file: DocumentFile): Boolean =
        file.isFile && file.name.orEmpty().substringAfterLast('.', "").lowercase() in textExt

    fun readText(resolver: ContentResolver, file: DocumentFile): String {
        require(isEditableText(file)) { "Solo se editan archivos de texto permitidos." }
        val expectedSize = file.length()
        require(expectedSize <= MAX_TEXT_BYTES || expectedSize == 0L) {
            "Archivo mayor a 256 KiB. Abrir con otra aplicación para evitar saturar la memoria."
        }
        val bytes = resolver.openInputStream(file.uri)?.use { input ->
            val out = ByteArrayOutputStream()
            val buffer = ByteArray(8192)
            while (true) {
                val count = input.read(buffer)
                if (count < 0) break
                if (count > 0) out.write(buffer, 0, count)
                require(out.size() <= MAX_TEXT_BYTES) { "El archivo supera 256 KiB." }
            }
            out.toByteArray()
        } ?: throw IOException("No fue posible leer el archivo")
        require(bytes.none { it == 0.toByte() }) { "Archivo binario no editable." }
        return Charsets.UTF_8.newDecoder()
            .onMalformedInput(CodingErrorAction.REPORT)
            .onUnmappableCharacter(CodingErrorAction.REPORT)
            .decode(ByteBuffer.wrap(bytes)).toString()
    }

    fun saveEditedVersion(
        resolver: ContentResolver, parent: DocumentFile, origin: DocumentFile, text: String
    ): DocumentFile {
        require(isEditableText(origin)) { "Solo archivos de texto." }
        require(text.toByteArray(Charsets.UTF_8).size <= MAX_TEXT_BYTES) { "Texto superior a 256 KiB." }
        val sourceName = origin.name ?: "archivo.txt"
        val base = sourceName.substringBeforeLast('.', sourceName)
        val ext = sourceName.substringAfterLast('.', "txt")
        val name = uniqueName(parent, "${base}_GENAI_EDITADO_${System.currentTimeMillis()}.$ext")
        val created = parent.createFile(origin.type ?: "text/plain", name)
            ?: throw IOException("No se pudo crear una copia nueva")
        try {
            val bytes = text.toByteArray(Charsets.UTF_8)
            resolver.openOutputStream(created.uri, "wt")?.use { it.write(bytes); it.flush() }
                ?: throw IOException("No se pudo abrir el destino para escritura")
            val actual = resolver.openInputStream(created.uri)?.use { it.readBytes() }
                ?: throw IOException("No se pudo verificar la copia")
            check(MessageDigest.isEqual(sha256(bytes), sha256(actual))) {
                "Verificación SHA-256 falló; el original permanece intacto"
            }
            return created
        } catch (error: Exception) {
            created.delete() // Únicamente el archivo NUEVO, nunca el original.
            throw error
        }
    }

    fun copyWithVerification(
        resolver: ContentResolver, parent: DocumentFile, origin: DocumentFile, suffix: String,
        progress: ((copied: Long, total: Long?) -> Unit)? = null
    ): DocumentFile {
        require(origin.isFile) { "La operación requiere un archivo." }
        val originalName = origin.name ?: "archivo.bin"
        val (base, ext) = splitName(originalName)
        val newName = uniqueName(parent, "${base}_${suffix}_${System.currentTimeMillis()}$ext")
        val created = parent.createFile(origin.type ?: "application/octet-stream", newName)
            ?: throw IOException("El proveedor no permite crear archivos")
        try {
            val originalHash = MessageDigest.getInstance("SHA-256")
            val copiedHash = MessageDigest.getInstance("SHA-256")
            val output = resolver.openOutputStream(created.uri, "w")
                ?: throw IOException("El destino no es escribible")
            val total = origin.length().takeIf { it > 0L }
            var written = 0L
            var lastProgressNs = 0L
            progress?.invoke(0L, total)
            resolver.openInputStream(origin.uri)?.use { input ->
                output.use { dest ->
                    val buffer = ByteArray(64 * 1024)
                    while (true) {
                        val count = input.read(buffer)
                        if (count < 0) break
                        if (count == 0) continue
                        originalHash.update(buffer, 0, count)
                        dest.write(buffer, 0, count)
                        written += count
                        val now = System.nanoTime()
                        if (now - lastProgressNs >= 120_000_000L) {
                            progress?.invoke(written, total)
                            lastProgressNs = now
                        }
                    }
                    dest.flush()
                }
            } ?: throw IOException("El origen no es legible")
            progress?.invoke(written, total)
            resolver.openInputStream(created.uri)?.use { input ->
                val buffer = ByteArray(64 * 1024)
                while (true) {
                    val count = input.read(buffer)
                    if (count < 0) break
                    if (count > 0) copiedHash.update(buffer, 0, count)
                }
            } ?: throw IOException("No se puede verificar el destino")
            check(MessageDigest.isEqual(originalHash.digest(), copiedHash.digest())) {
                "La copia difiere del archivo original"
            }
            return created
        } catch (error: Exception) {
            created.delete() // Revertir únicamente el archivo de salida incompleto.
            throw error
        }
    }

    fun deleteAfterBackup(
        resolver: ContentResolver, parent: DocumentFile, origin: DocumentFile
    ): DocumentFile {
        require(origin.isFile) { "No se borran carpetas desde esta aplicación." }
        require(origin.canWrite()) { "El archivo no concede eliminación." }
        val backup = copyWithVerification(resolver, parent, origin, "RESPALDO_ANTES_DE_BORRAR")
        // La copia verificada se conserva, incluso si el proveedor rechaza el borrado.
        if (!origin.delete()) throw IOException("El archivo original no pudo eliminarse; respaldo guardado: ${backup.name}")
        return backup
    }

    fun renameFile(origin: DocumentFile, newName: String) {
        require(origin.isFile) { "No se renombran carpetas." }
        require(newName.isNotBlank() && newName.length < 200 &&
            newName.none { it == '/' || it == '\\' || it == '\u0000' }) { "Nombre no válido." }
        require(origin.renameTo(newName)) { "El proveedor rechazó el cambio de nombre." }
    }

    private fun splitName(name: String): Pair<String, String> =
        if ('.' in name && !name.startsWith('.')) {
            name.substringBeforeLast('.') to ("." + name.substringAfterLast('.'))
        } else name to ""

    private fun uniqueName(parent: DocumentFile, suggested: String): String {
        if (parent.findFile(suggested) == null) return suggested
        val (base, ext) = splitName(suggested)
        for (i in 1..1000) {
            val candidate = "${base}_$i$ext"
            if (parent.findFile(candidate) == null) return candidate
        }
        throw IOException("No se pudo crear nombre único")
    }

    private fun sha256(bytes: ByteArray): ByteArray = MessageDigest.getInstance("SHA-256").digest(bytes)
}
