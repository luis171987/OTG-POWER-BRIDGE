import bo.genai.usbpowerbridge.TelemetryFormats
fun main() {
    check(TelemetryFormats.bytes(null) == "No disponible")
    check(TelemetryFormats.bytes(0) == "0 B")
    check(TelemetryFormats.bytes(1024) == "1.00 KiB")
    check(TelemetryFormats.bytes(1073741824) == "1.00 GiB")
    check(TelemetryFormats.percent(50, 100) == 50)
    check(TelemetryFormats.percent(1000, 100) == 100)
    check(TelemetryFormats.percent(10, null) == null)
    check(TelemetryFormats.percent(0, 0) == 100)
    println("TESTS_PUROS_KOTLIN=8/8")
}
