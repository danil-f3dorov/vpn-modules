package core.util.parse

import core.domain.model.Server

object ParseSpeed {
    fun convertSpeedForAdapter(server: Server): String {
        val speedInMB = server.speed.toLong() / 1048576
        return if (speedInMB > 0) {
            "$speedInMB"
        } else {
            val speedInKB = server.speed.toLong() / 1024
            "$speedInKB"
        }
    }

    fun parseSpeed(speed: String?): String {
        try {
            if (speed == null) return ""

            val numericValue = speed.substring(0, speed.length - 2).replace(",", ".").trim()
            val factor = when {
                speed.endsWith("MB") -> 1024.0
                speed.endsWith("KB") -> 1.0
                speed.endsWith("B") && !speed.endsWith("KB") -> 1.0 / 1024
                else -> return "--"
            }

            val temp = numericValue.toDouble() * factor
            val result = if (temp % 1 == 0.0) {
                String.format("%.0f", temp)
            } else {
                String.format("%.1f", temp)
            }.replace(",", ".")

            return if (result.endsWith(".0")) result.substring(0, result.length - 2) else result
        }
        catch (_ : Exception) {
            return "0 KB"
        }
    }
}