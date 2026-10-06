package co.edu.ucaldas.calculadoranomina

// Constantes de Ley 2026
const val SMMLV_2026 = 1750905.0
const val AUX_TRANSPORTE_2026 = 249095.0
const val HORAS_ORDINARIAS_MES = 210.0

const val PORCENTAJE_SALUD = 0.04
const val PORCENTAJE_PENSION = 0.04
const val PORCENTAJE_FONDO_SOLIDARIDAD = 0.01

const val FACTOR_EXTRA_DIURNA_HABIL = 1.25
const val FACTOR_EXTRA_NOCTURNA_HABIL = 1.75
const val FACTOR_EXTRA_DIURNA_DOMINICAL = 2.15
const val FACTOR_EXTRA_NOCTURNA_DOMINICAL = 2.65

data class ResultadoNomina(
    val valorHora: Double,
    val totalHorasExtra: Double,
    val auxilioTransporte: Double,
    val totalDevengado: Double,
    val aporteSalud: Double,
    val aportePension: Double,
    val fondoSolidaridad: Double,
    val totalDeducciones: Double,
    val salarioNeto: Double,
    val rangoSalarial: RangoSalarial
)

enum class RangoSalarial {
    RANGO_1,
    RANGO_2,
    RANGO_3
}

fun clasificarRango(salarioBasico: Double): RangoSalarial {
    return when {
        salarioBasico <= (2 * SMMLV_2026) -> RangoSalarial.RANGO_1
        salarioBasico < (4 * SMMLV_2026) -> RangoSalarial.RANGO_2
        else -> RangoSalarial.RANGO_3
    }
}

fun calcularNomina(
    salarioBasico: Double,
    horasDiurnas: Double,
    horasNocturnas: Double,
    esDominical: Boolean,
    transporteEmpresa: Boolean
): ResultadoNomina {
    val valorHora = salarioBasico / HORAS_ORDINARIAS_MES

    val factorDiurno = if (esDominical) FACTOR_EXTRA_DIURNA_DOMINICAL else FACTOR_EXTRA_DIURNA_HABIL
    val factorNocturno = if (esDominical) FACTOR_EXTRA_NOCTURNA_DOMINICAL else FACTOR_EXTRA_NOCTURNA_HABIL

    val pagoExtrasDiurnas = horasDiurnas * valorHora * factorDiurno
    val pagoExtrasNocturnas = horasNocturnas * valorHora * factorNocturno
    val totalHorasExtra = pagoExtrasDiurnas + pagoExtrasNocturnas

    val ibc = salarioBasico + totalHorasExtra

    val auxilioTransporte = if (salarioBasico <= (2 * SMMLV_2026) && !transporteEmpresa) {
        AUX_TRANSPORTE_2026
    } else {
        0.0
    }

    val totalDevengado = ibc + auxilioTransporte

    val aporteSalud = ibc * PORCENTAJE_SALUD
    val aportePension = ibc * PORCENTAJE_PENSION
    val fondoSolidaridad = if (ibc >= (4 * SMMLV_2026)) {
        ibc * PORCENTAJE_FONDO_SOLIDARIDAD
    } else {
        0.0
    }
    val totalDeducciones = aporteSalud + aportePension + fondoSolidaridad

    val salarioNeto = totalDevengado - totalDeducciones
    val rango = clasificarRango(salarioBasico)

    return ResultadoNomina(
        valorHora = valorHora,
        totalHorasExtra = totalHorasExtra,
        auxilioTransporte = auxilioTransporte,
        totalDevengado = totalDevengado,
        aporteSalud = aporteSalud,
        aportePension = aportePension,
        fondoSolidaridad = fondoSolidaridad,
        totalDeducciones = totalDeducciones,
        salarioNeto = salarioNeto,
        rangoSalarial = rango
    )
}