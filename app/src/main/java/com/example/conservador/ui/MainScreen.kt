package com.example.conservador.ui

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.text.SimpleDateFormat
import java.util.*
import cl.gpv.llamado_conservador.R
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.text.TextStyle
import cl.gpv.llamado_conservador.ui.MainViewModel

private val SECTION_COLORS = mapOf(
    "Atención Preferencial" to Color(0xFF008080),
    "Retiro de Documentos"   to Color(0xFFDC143C),
    "Revisión de Libros"     to Color(0xFF003180),
    "Atención General"       to Color(0xFF930074)
)

private fun getSectionColor(seccion: String): Color =
    SECTION_COLORS.entries.find { seccion.contains(it.key, ignoreCase = true) }?.value
        ?: Color(0xFF055160)

@Composable
fun MainScreen(
    viewModel: MainViewModel
) {
    val ultimo by viewModel.ultimo.collectAsState()
    val historial by viewModel.historial.collectAsState()
    val timeFormatter = remember { SimpleDateFormat("h:mm a", Locale("es", "CL")) }

    val alphaAnim = remember { Animatable(1f) }
    val showHistorial = remember { mutableStateOf(true) }

    // Usar timestamp en lugar de solo ticket para detectar nuevos llamados
    LaunchedEffect(ultimo?.ticket, ultimo?.timestamp) {
        if (ultimo?.ticket != null) {
            // Ocultar historial
            showHistorial.value = false

            // Primer parpadeo
            alphaAnim.animateTo(0.1f, animationSpec = tween(500, easing = FastOutSlowInEasing))
            alphaAnim.animateTo(1f, animationSpec = tween(500, easing = FastOutSlowInEasing))
            // Segundo parpadeo
            alphaAnim.animateTo(0.1f, animationSpec = tween(500, easing = FastOutSlowInEasing))
            alphaAnim.animateTo(1f, animationSpec = tween(500, easing = FastOutSlowInEasing))

            // Mostrar historial nuevamente
            showHistorial.value = true
        }
    }

    Row(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F5F5))
            .padding(24.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // ---------------- IZQUIERDA: Ticket + Módulo (sin sección) ----------------
        Box(modifier = Modifier.weight(1f)) {
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Image(
                    painter = painterResource(R.drawable.logo_conservador),
                    contentDescription = "Logotipo",
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(100.dp)
                        .padding(horizontal = 10.dp),
                    contentScale = ContentScale.Fit
                )

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .alpha(alphaAnim.value),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        if (ultimo == null ) {
                            Text(
                                "Esperando llamado …",
                                style = MaterialTheme.typography.headlineMedium,
                                color = Color(0xFF6C757D)
                            )
                        } else {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {

                                Text(
                                    text = "Llamando al Ticket :",
                                    style = MaterialTheme.typography.displayLarge,
                                    fontSize = 30.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color(0xFF001E54),
                                    textAlign = TextAlign.Center
                                )
                                Spacer(Modifier.height(0.dp))
                                Text(
                                    text = ultimo?.ticket ?: "--",
                                    style = MaterialTheme.typography.displayLarge,
                                    fontSize = 100.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color(0xFF001E54),
                                    textAlign = TextAlign.Center
                                )

                                Spacer(Modifier.height(16.dp))

                                val moduloOriginal = ultimo?.modulo ?: ""
                                val moduloTxt = moduloOriginal
                                    .removePrefix("Módulo ")
                                    .removePrefix("MÓDULO ")

                                if (moduloTxt.isNotBlank()) {
                                    Box(
                                        modifier = Modifier
                                            .background(Color(0xFF003488), RoundedCornerShape(12.dp))
                                            .padding(horizontal = 55.dp, vertical = 10.dp)
                                    ) {
                                        if (moduloTxt.contains("Revisión de Libros", ignoreCase = true)) {
                                            // Caso especial: Revisión de Libros en dos líneas
                                            Text(
                                                text = "Revisión\nde Libros",
                                                fontSize = 36.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Color.White,
                                                textAlign = TextAlign.Center
                                            )
                                        } else {
                                            // Caso normal: Módulo + número
                                            Text(
                                                text = "Módulo ${moduloTxt.uppercase()}",
                                                fontSize = 36.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Color.White
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // ---------------- DERECHA: Historial (4 tarjetas con fondo de color) ----------------
        if (showHistorial.value) {
            Column(modifier = Modifier.weight(1f)) {

                Spacer(Modifier.height(20.dp))

                val items = remember(historial) { historial.take(4) }
                val spacing = 10.dp

                if (items.isEmpty()) {
                    // Mostrar mensaje cuando no hay historial
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            "No hay llamados previos …",
                            style = MaterialTheme.typography.headlineMedium,
                            color = Color(0xFF6C757D),
                            textAlign = TextAlign.Center
                        )
                    }
                } else {
                    // Mostrar historial
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 8.dp, vertical = 12.dp),
                        verticalArrangement = Arrangement.spacedBy(spacing)
                    ) {
                        items.forEachIndexed { index, item ->
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(105.dp)
                            ) {
                                val itemColor = getSectionColor(item.seccion)

                                // Estilo de texto con sombra negra
                                val textShadowStyle = Shadow(
                                    color = Color.Black.copy(alpha = 0.7f),
                                    offset = Offset(6f, 6f),
                                    blurRadius = 2f
                                )

                                Surface(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .heightIn(min = 96.dp),
                                    color = itemColor, // Ahora el color de fondo es el de la sección
                                    shape = RoundedCornerShape(12.dp),
                                    shadowElevation = 2.dp,
                                    tonalElevation = 0.dp
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .padding(horizontal = 20.dp, vertical = 15.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                    ) {

                                        Column {
                                            // IZQUIERDA: Ticket
                                            Text(
                                                text = item.ticket,
                                                style = TextStyle(
                                                    fontSize = 35.sp,
                                                    fontWeight = FontWeight.ExtraBold,
                                                    color = Color.White,
                                                    shadow = textShadowStyle
                                                )
                                            )

                                            if (item.modulo.isNotBlank()) {
                                                val moduloTexto = item.modulo
                                                    .removePrefix("Módulo ")
                                                    .removePrefix("MÓDULO ")

                                                val textoFinal = if (moduloTexto.contains("Revisión de Libros", ignoreCase = true)) {
                                                    "Revisión Libros"
                                                } else {
                                                    "Módulo $moduloTexto"
                                                }

                                                Text(
                                                    text = textoFinal,
                                                    style = TextStyle(
                                                        fontSize = 27.sp,
                                                        fontWeight = FontWeight.ExtraBold,
                                                        color = Color.White,
                                                        shadow = textShadowStyle,
                                                    )
                                                )
                                            }

                                        }

                                        Column {
                                            // DERECHA: Hora
                                            val time = item.timestamp?.toDate()?.let(timeFormatter::format) ?: "--:--"

                                            Text(
                                                text = time,
                                                style = TextStyle(
                                                    fontSize = 30.sp,
                                                    fontWeight = FontWeight.ExtraBold,
                                                    color = Color.White,
                                                    shadow = textShadowStyle,
                                                    textAlign = TextAlign.End,
                                                )
                                            )
                                        }

                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}