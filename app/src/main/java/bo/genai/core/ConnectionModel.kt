package bo.genai.core

/** Modelo lógico. No controla USB-C, USB-PD ni la PCB del power bank. */
enum class Validation { YES, NO, UNKNOWN }
enum class Connection {
    MEMORY_DIRECT, MEMORY_VIA_POWERBANK_POWER_ONLY, HUB_COMPATIBLE,
    HUB_PHONE_INCOMPATIBLE, HUB_UNVERIFIED, DISCONNECTED_DRIVE, READ_ONLY_DRIVE
}
data class Prediction(
    val charging: Boolean,
    val dataPath: Boolean,
    val simultaneous: Validation,
    val writable: Boolean,
    val note: String
)
object ConnectionModel {
    fun evaluate(mode: Connection): Prediction = when (mode) {
        Connection.MEMORY_DIRECT -> Prediction(false, true, Validation.NO, true,
            "Memoria conectada directamente al teléfono; sin carga externa.")
        Connection.MEMORY_VIA_POWERBANK_POWER_ONLY -> Prediction(true, false, Validation.NO, false,
            "Hipótesis sin puente de datos dentro del power bank: no hay ruta USB.")
        Connection.HUB_COMPATIBLE -> Prediction(true, true, Validation.YES, true,
            "Solo simulación: hub y teléfono capaces de OTG y carga a la vez.")
        Connection.HUB_PHONE_INCOMPATIBLE -> Prediction(true, false, Validation.NO, false,
            "Si el teléfono no mantiene host OTG mientras carga, el hub no basta.")
        Connection.HUB_UNVERIFIED -> Prediction(true, false, Validation.UNKNOWN, false,
            "Estado real pendiente: capacidad de USB OTG + carga no demostrada.")
        Connection.DISCONNECTED_DRIVE -> Prediction(true, false, Validation.NO, false,
            "El almacenamiento fue desconectado.")
        Connection.READ_ONLY_DRIVE -> Prediction(true, true, Validation.YES, false,
            "Hipótesis compatible, pero proveedor de almacenamiento solo permite lectura.")
    }
}
