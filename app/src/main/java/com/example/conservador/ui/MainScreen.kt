package cl.gpv.llamado_conservador.ui

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
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

private val SECTION_COLORS = mapOf(
    "Atención Preferencial" to Color(0xFF008080),
    "Retiro de Documentos"   to Color(0xFF2B8C7F),
    "Revisión de Libros"     to Color(0xFF003180),
    "Atención General"       to Color(0xFF800064)
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
    val timeFormatter = remember { SimpleDateFormat("HH:mm", Locale("es", "CL")) }

    val alphaAnim = remember { Animatable(1f) }
    LaunchedEffect(ultimo?.ticket) {
        alphaAnim.snapTo(0.2f)
        alphaAnim.animateTo(1f, animationSpec = tween(600, easing = FastOutSlowInEasing))
    }

    Row(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F5F5))
            .padding(24.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // ---------------- IZQUIERDA: Ticket + Módulo (sin sección) ----------------
        Box(modifier = Modifier.weight(1f).alpha(alphaAnim.value)) {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Image(
                    painter = painterResource(R.drawable.logo_conservador),
                    contentDescription = "Logotipo",
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp)
                        .padding(horizontal = 10.dp),
                    contentScale = ContentScale.Fit
                )

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        if (ultimo == null) {
                            Text(
                                "Esperando llamado…",
                                style = MaterialTheme.typography.headlineMedium,
                                color = Color(0xFF6C757D)
                            )
                        } else {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = ultimo?.ticket ?: "--",
                                    style = MaterialTheme.typography.displayLarge,
                                    fontSize = 140.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color(0xFF212121),
                                    textAlign = TextAlign.Center
                                )

                                Spacer(Modifier.height(16.dp))

                                val moduloTxt = (ultimo?.modulo ?: "")
                                    .removePrefix("Módulo ")
                                    .removePrefix("MÓDULO ")
                                if (moduloTxt.isNotBlank()) {
                                    Box(
                                        modifier = Modifier
                                            .background(Color(0xFF212121), RoundedCornerShape(12.dp))
                                            .padding(horizontal = 24.dp, vertical = 10.dp)
                                    ) {
                                        Text(
                                            text = "MÓDULO ${moduloTxt.uppercase()}",
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

        // ---------------- DERECHA: Historial (4 tarjetas completas visibles, sin título) ----------------
        Column(modifier = Modifier.weight(1f)) {
            Card(
                modifier = Modifier.fillMaxSize(),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                // Sin encabezado para ganar espacio
                Spacer(Modifier.height(8.dp))

                val items = remember(historial) { historial.take(4) }
                val spacing = 10.dp

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(spacing)
                ) {
                    items.forEachIndexed { index, item ->
                        // Cada tarjeta ocupa la misma altura
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth()
                        ) {
                            val itemColor = getSectionColor(item.seccion)
                            Surface(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .heightIn(min = 96.dp), // mínimo por seguridad
                                color = Color(0xFFF8F9FA),
                                shape = RoundedCornerShape(12.dp),
                                shadowElevation = 0.dp,
                                tonalElevation = 0.dp,
                                border = BorderStroke(2.dp, itemColor)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(horizontal = 16.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(
                                            text = item.ticket,
                                            style = MaterialTheme.typography.headlineSmall,
                                            fontSize = 36.sp,
                                            fontWeight = FontWeight.ExtraBold,
                                            color = Color(0xFF212121)
                                        )
                                        Text(
                                            text = item.seccion,
                                            style = MaterialTheme.typography.bodyLarge,
                                            fontSize = 18.sp,
                                            fontWeight = FontWeight.Medium,
                                            color = Color(0xFF666666)
                                        )
                                        if (item.modulo.isNotBlank()) {
                                            Text(
                                                text = "Módulo ${item.modulo.removePrefix("Módulo ").removePrefix("MÓDULO ")}",
                                                style = MaterialTheme.typography.bodyLarge,
                                                fontSize = 16.sp,
                                                fontWeight = FontWeight.SemiBold,
                                                color = Color(0xFF333333)
                                            )
                                        }
                                    }
                                    val time = item.timestamp?.toDate()?.let(timeFormatter::format) ?: "--:--"
                                    Text(
                                        text = time,
                                        style = MaterialTheme.typography.titleLarge,
                                        fontWeight = FontWeight.SemiBold,
                                        color = Color(0xFF495057)
                                    )
                                }
                            }
                        }
                        if (index < items.lastIndex) {
                            // el espacio entre tarjetas ya lo maneja Arrangement.spacedBy(spacing)
                        }
                    }
                }
            }
        }
    }
}
