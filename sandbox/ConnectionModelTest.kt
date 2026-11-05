import bo.genai.core.Connection
import bo.genai.core.ConnectionModel
import bo.genai.core.Validation

fun main() {
    val expected = listOf(Validation.NO, Validation.NO, Validation.YES, Validation.NO,
        Validation.UNKNOWN, Validation.NO, Validation.YES)
    val actual = Connection.entries.map { ConnectionModel.evaluate(it).simultaneous }
    check(expected == actual) { "Estados inesperados: $actual" }
    check(!ConnectionModel.evaluate(Connection.MEMORY_VIA_POWERBANK_POWER_ONLY).dataPath)
    check(!ConnectionModel.evaluate(Connection.READ_ONLY_DRIVE).writable)
    println("KOTLIN CORE: 9/9 pruebas lógicas aprobadas")
}
