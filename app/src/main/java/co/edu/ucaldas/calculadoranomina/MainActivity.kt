package co.edu.ucaldas.calculadoranomina

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.annotation.StringRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import co.edu.ucaldas.calculadoranomina.ui.theme.CalculadoraNominaTheme
import java.text.NumberFormat
import java.util.Locale

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            CalculadoraNominaTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    CalculadoraNominaScreen(modifier = Modifier.padding(innerPadding))
                }
            }
        }
    }
}

@Composable
fun CalculadoraNominaScreen(modifier: Modifier = Modifier) {
    var salarioInput by remember { mutableStateOf("") }
    var horasDiurnasInput by remember { mutableStateOf("") }
    var horasNocturnasInput by remember { mutableStateOf("") }
    var esDominical by remember { mutableStateOf(false) }
    var transporteEmpresa by remember { mutableStateOf(false) }

    var resultado by remember { mutableStateOf<ResultadoNomina?>(null) }
    var mensajeError by remember { mutableStateOf<String?>(null) }
    var tieneErrorSalario by remember { mutableStateOf(false) }
    var tieneErrorHoras by remember { mutableStateOf(false) }

    fun ejecutarCalculo() {
        val salario = salarioInput.toDoubleOrNull()
        val horasDiurnas = if (horasDiurnasInput.isBlank()) 0.0 else horasDiurnasInput.toDoubleOrNull()
        val horasNocturnas = if (horasNocturnasInput.isBlank()) 0.0 else horasNocturnasInput.toDoubleOrNull()

        if (salario == null) {
            mensajeError = "Ingrese un salario válido"
            tieneErrorSalario = true
            resultado = null
            return
        }
        if (salario < SMMLV_2026) {
            mensajeError = "El salario no puede ser inferior al mínimo ($ 1.750.905)"
            tieneErrorSalario = true
            resultado = null
            return
        }
        tieneErrorSalario = false

        if (horasDiurnas == null || horasNocturnas == null || horasDiurnas < 0 || horasNocturnas < 0) {
            mensajeError = "Las horas extra deben ser un número mayor o igual a cero"
            tieneErrorHoras = true
            resultado = null
            return
        }

        if (horasDiurnas + horasNocturnas > 48) {
            mensajeError = "El total de horas extra no puede superar 48 en el mes"
            tieneErrorHoras = true
            resultado = null
            return
        }
        tieneErrorHoras = false

        mensajeError = null
        resultado = calcularNomina(
            salarioBasico = salario,
            horasDiurnas = horasDiurnas,
            horasNocturnas = horasNocturnas,
            esDominical = esDominical,
            transporteEmpresa = transporteEmpresa
        )
    }

    val focusManager = LocalFocusManager.current

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = stringResource(R.string.titulo_app),
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        CampoNumerico(
            etiqueta = R.string.salario_basico_label,
            valor = salarioInput,
            onValueChange = { salarioInput = it },
            isError = tieneErrorSalario,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Number,
                imeAction = ImeAction.Next
            ),
            keyboardActions = KeyboardActions(
                onNext = { focusManager.moveFocus(FocusDirection.Down) }
            )
        )

        CampoNumerico(
            etiqueta = R.string.horas_diurnas_label,
            valor = horasDiurnasInput,
            onValueChange = { horasDiurnasInput = it },
            isError = tieneErrorHoras,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Number,
                imeAction = ImeAction.Next
            ),
            keyboardActions = KeyboardActions(
                onNext = { focusManager.moveFocus(FocusDirection.Down) }
            )
        )

        CampoNumerico(
            etiqueta = R.string.horas_nocturnas_label,
            valor = horasNocturnasInput,
            onValueChange = { horasNocturnasInput = it },
            isError = tieneErrorHoras,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Number,
                imeAction = ImeAction.Done
            ),
            keyboardActions = KeyboardActions(
                onDone = { focusManager.clearFocus() }
            )
        )

        FilaInterruptor(
            etiqueta = R.string.es_dominical_label,
            checked = esDominical,
            onCheckedChange = {
                esDominical = it
                if (resultado != null) ejecutarCalculo()
            }
        )

        FilaInterruptor(
            etiqueta = R.string.transporte_empresa_label,
            checked = transporteEmpresa,
            onCheckedChange = {
                transporteEmpresa = it
                if (resultado != null) ejecutarCalculo()
            }
        )

        mensajeError?.let {
            Text(
                text = it,
                color = MaterialTheme.colorScheme.error,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.padding(vertical = 4.dp)
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Button(
                onClick = { ejecutarCalculo() },
                modifier = Modifier.weight(1f)
            ) {
                Text(stringResource(R.string.btn_calcular))
            }

            OutlinedButton(
                onClick = {
                    salarioInput = ""
                    horasDiurnasInput = ""
                    horasNocturnasInput = ""
                    esDominical = false
                    transporteEmpresa = false
                    resultado = null
                    mensajeError = null
                    tieneErrorSalario = false
                    tieneErrorHoras = false
                },
                modifier = Modifier.weight(1f)
            ) {
                Text(stringResource(R.string.btn_limpiar))
            }
        }

        resultado?.let { res ->
            Spacer(modifier = Modifier.height(8.dp))

            SeccionRangoSalarial(rango = res.rangoSalarial)

            Divider(modifier = Modifier.padding(vertical = 8.dp))

            Text(
                text = stringResource(R.string.seccion_devengado),
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            FilaDesglose(stringResource(R.string.concepto_valor_hora), formatearMoneda(res.valorHora))
            FilaDesglose(stringResource(R.string.concepto_salario_basico), formatearMoneda(salarioInput.toDoubleOrNull() ?: 0.0))
            FilaDesglose(stringResource(R.string.concepto_horas_extra), formatearMoneda(res.totalHorasExtra))
            FilaDesglose(stringResource(R.string.concepto_aux_transporte), formatearMoneda(res.auxilioTransporte))
            FilaDesglose(stringResource(R.string.concepto_total_devengado), formatearMoneda(res.totalDevengado), esNegrita = true)

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = stringResource(R.string.seccion_deducciones),
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            FilaDesglose(stringResource(R.string.concepto_salud), formatearMoneda(res.aporteSalud))
            FilaDesglose(stringResource(R.string.concepto_pension), formatearMoneda(res.aportePension))
            FilaDesglose(stringResource(R.string.concepto_fondo_solidaridad), formatearMoneda(res.fondoSolidaridad))
            FilaDesglose(stringResource(R.string.concepto_total_deducciones), formatearMoneda(res.totalDeducciones), esNegrita = true)

            Divider(modifier = Modifier.padding(vertical = 8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = stringResource(R.string.concepto_salario_neto),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = formatearMoneda(res.salarioNeto),
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@Composable
fun CampoNumerico(
    @StringRes etiqueta: Int,
    valor: String,
    onValueChange: (String) -> Unit,
    keyboardOptions: KeyboardOptions,
    keyboardActions: KeyboardActions,
    isError: Boolean = false,
    modifier: Modifier = Modifier
) {
    OutlinedTextField(
        value = valor,
        onValueChange = onValueChange,
        label = { Text(stringResource(etiqueta)) },
        singleLine = true,
        isError = isError,
        keyboardOptions = keyboardOptions,
        keyboardActions = keyboardActions,
        modifier = modifier.fillMaxWidth()
    )
}

@Composable
fun FilaInterruptor(
    @StringRes etiqueta: Int,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = stringResource(etiqueta),
            modifier = Modifier.weight(1f)
        )
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange
        )
    }
}

@Composable
fun FilaDesglose(concepto: String, valor: String, esNegrita: Boolean = false) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = concepto,
            fontWeight = if (esNegrita) FontWeight.Bold else FontWeight.Normal
        )
        Text(
            text = valor,
            fontWeight = if (esNegrita) FontWeight.Bold else FontWeight.Normal
        )
    }
}

@Composable
fun SeccionRangoSalarial(rango: RangoSalarial) {
    val (imagenRes, textoRes) = when (rango) {
        RangoSalarial.RANGO_1 -> Pair(R.drawable.rango_1, R.string.rango_1_desc)
        RangoSalarial.RANGO_2 -> Pair(R.drawable.rango_2, R.string.rango_2_desc)
        RangoSalarial.RANGO_3 -> Pair(R.drawable.rango_3, R.string.rango_3_desc)
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxWidth()
    ) {
        Image(
            painter = painterResource(id = imagenRes),
            contentDescription = stringResource(R.string.img_rango_desc),
            modifier = Modifier.size(100.dp)
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = stringResource(textoRes),
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium
        )
    }
}

fun formatearMoneda(valor: Double): String {
    val format = NumberFormat.getCurrencyInstance(Locale("es", "CO"))
    format.maximumFractionDigits = 0
    return format.format(valor)
}

@Preview(showBackground = true)
@Composable
fun CalculadoraNominaPreview() {
    CalculadoraNominaTheme {
        CalculadoraNominaScreen()
    }
}