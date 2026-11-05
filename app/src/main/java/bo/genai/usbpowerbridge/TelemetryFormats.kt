package bo.genai.usbpowerbridge

import java.util.Locale

/** Lógica independiente de Android, testeable sin dispositivo físico. */
object TelemetryFormats {
    fun bytes(value: Long?): String {
        if (value == null || value < 0L) return "No disponible"
        if (value < 1024L) return "$value B"
        var size = value.toDouble()
        val units = arrayOf("B", "KiB", "MiB", "GiB", "TiB")
        var idx = 0
        while (size >= 1024.0 && idx < units.lastIndex) { size /= 1024.0; idx++ }
        return String.format(Locale.US, "%.2f %s", size, units[idx])
    }

    fun percent(done: Long, total: Long?): Int? {
        if (total == null || total < 0) return null
        if (total == 0L) return if (done == 0L) 100 else null
        return ((done.coerceAtLeast(0).toDouble() * 100.0) / total.toDouble())
            .coerceIn(0.0, 100.0).toInt()
    }
}
