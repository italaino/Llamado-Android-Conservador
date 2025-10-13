package cl.gpv.llamado_conservador.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import kotlinx.coroutines.delay
import cl.gpv.llamado_conservador.R

@Composable
fun SplashScreen(onSplashEnded: () -> Unit) {
    LaunchedEffect(Unit) {
        delay(2500)               // Duración: 2.5 s
        onSplashEnded()
    }
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White),
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painterResource(id = R.drawable.ic_splash),
            contentDescription = "Splash",
            modifier = Modifier.fillMaxSize()
        )
    }
}
