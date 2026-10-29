package cl.duoc.clorocontrol.ui.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.size
import androidx.compose.ui.res.painterResource
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MailOutline
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import cl.duoc.clorocontrol.R
import cl.duoc.clorocontrol.ui.viewmodel.AuthViewModel

@Composable
fun LoginScreen(viewModel: AuthViewModel) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    Surface(color = MaterialTheme.colorScheme.background) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Surface(
                    shape = MaterialTheme.shapes.extraLarge,
                    color = MaterialTheme.colorScheme.primaryContainer,
                    modifier = Modifier.padding(bottom = 4.dp)
                ) {
                    Image(
                        painter = painterResource(R.drawable.ic_cloro_drop),
                        contentDescription = "Símbolo de CloroControl",
                        modifier = Modifier.padding(16.dp).size(56.dp)
                    )
                }
                Text("CloroControl", style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.Bold)
                Text(
                    "Control de cloro libre y seguimiento de incidencias",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = state.email,
                    onValueChange = viewModel::onEmailChange,
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Correo electrónico") },
                    leadingIcon = { Icon(Icons.Default.MailOutline, contentDescription = null) },
                    singleLine = true,
                    isError = state.emailError != null,
                    supportingText = { state.emailError?.let { Text(it) } },
                    trailingIcon = { if (state.emailError != null) Icon(Icons.Default.ErrorOutline, contentDescription = "Correo inválido") }
                )
                OutlinedTextField(
                    value = state.password,
                    onValueChange = viewModel::onPasswordChange,
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Contraseña") },
                    leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
                    visualTransformation = PasswordVisualTransformation(),
                    singleLine = true,
                    isError = state.passwordError != null,
                    supportingText = { state.passwordError?.let { Text(it) } },
                    trailingIcon = { if (state.passwordError != null) Icon(Icons.Default.ErrorOutline, contentDescription = "Contraseña inválida") }
                )
                state.formMessage?.let {
                    Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                }
                Button(onClick = viewModel::signIn, modifier = Modifier.fillMaxWidth().height(52.dp)) {
                    Text("Iniciar sesión")
                }
                Spacer(Modifier.height(6.dp))
                Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(5.dp)) {
                        Text("Cuentas de prueba", style = MaterialTheme.typography.titleSmall)
                        Text("operario@demo.cl · Demo123", style = MaterialTheme.typography.bodySmall)
                        Text("supervisor@demo.cl · Demo123", style = MaterialTheme.typography.bodySmall)
                        Text("jefatura@demo.cl · Demo123", style = MaterialTheme.typography.bodySmall)
                        Text("Usuarios ficticios para el MVP académico.", style = MaterialTheme.typography.labelSmall)
                    }
                }
            }
        }
    }
}
