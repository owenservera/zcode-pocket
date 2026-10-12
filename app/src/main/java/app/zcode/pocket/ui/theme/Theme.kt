package app.zcode.pocket.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// Dark-first by design: an agent console you check at night should not flash
// white. Accent = electric indigo; running = mint; warnings = amber.
private val Colors = darkColorScheme(
    primary = Color(0xFF7C7CFF),
    onPrimary = Color(0xFF0B0D12),
    primaryContainer = Color(0xFF26264A),
    onPrimaryContainer = Color(0xFFDDDDFF),
    secondary = Color(0xFF2EE6C8),
    onSecondary = Color(0xFF06251F),
    secondaryContainer = Color(0xFF123A34),
    onSecondaryContainer = Color(0xFFB8F5EA),
    tertiary = Color(0xFFFFB86B),
    onTertiary = Color(0xFF2A1802),
    background = Color(0xFF0B0D12),
    onBackground = Color(0xFFE6E8F0),
    surface = Color(0xFF12151D),
    onSurface = Color(0xFFE6E8F0),
    surfaceVariant = Color(0xFF1B202B),
    onSurfaceVariant = Color(0xFFAAB2C5),
    outline = Color(0xFF3A4152),
    error = Color(0xFFFF5C7A),
    onError = Color(0xFF2A0A12),
)

@Composable
fun ZpTheme(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = Colors, content = content)
}
