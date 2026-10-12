package app.zcode.pocket.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.zcode.pocket.protocol.TimelineItem
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun StatusDot(running: Boolean, modifier: Modifier = Modifier) {
    Box(
        modifier
            .size(9.dp)
            .background(
                if (running) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.outline,
                CircleShape,
            ),
    )
}

@Composable
fun ErrorBanner(text: String, onRetry: (() -> Unit)? = null) {
    Surface(
        color = MaterialTheme.colorScheme.error.copy(alpha = 0.15f),
        shape = RoundedCornerShape(10.dp),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(Modifier.padding(horizontal = 12.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
            Text(
                text,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.weight(1f),
                maxLines = 3,
                overflow = TextOverflow.Ellipsis,
            )
            if (onRetry != null) TextButton(onClick = onRetry) { Text("Retry") }
        }
    }
}

@Composable
fun SectionHeader(text: String) {
    Text(
        text.uppercase(),
        style = MaterialTheme.typography.labelSmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(start = 4.dp, top = 14.dp, bottom = 6.dp),
    )
}

@Composable
fun ToolCard(tool: TimelineItem.Tool) {
    val statusColor = when (tool.status) {
        "completed" -> MaterialTheme.colorScheme.secondary
        "failed" -> MaterialTheme.colorScheme.error
        "in_progress" -> MaterialTheme.colorScheme.tertiary
        else -> MaterialTheme.colorScheme.onSurfaceVariant
    }
    Surface(
        color = MaterialTheme.colorScheme.surfaceVariant,
        shape = RoundedCornerShape(10.dp),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(Modifier.padding(10.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    (tool.kind ?: "tool").uppercase(),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary,
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    tool.title,
                    style = MaterialTheme.typography.labelLarge,
                    modifier = Modifier.weight(1f),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(tool.status.replace('_', ' '), style = MaterialTheme.typography.labelSmall, color = statusColor)
            }
            if (!tool.detail.isNullOrBlank()) {
                Text(
                    tool.detail.take(600),
                    style = MaterialTheme.typography.bodySmall,
                    fontFamily = FontFamily.Monospace,
                    maxLines = 6,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(top = 6.dp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

sealed interface MdSegment {
    data class Text(val text: String) : MdSegment
    data class Code(val text: String) : MdSegment
}

/** Split on ``` fences so code renders monospace; everything else is plain text. */
fun splitCodeFences(text: String): List<MdSegment> {
    val parts = mutableListOf<MdSegment>()
    var i = 0
    while (i < text.length) {
        val start = text.indexOf("```", i)
        if (start < 0) {
            parts.add(MdSegment.Text(text.substring(i)))
            break
        }
        if (start > i) parts.add(MdSegment.Text(text.substring(i, start)))
        val nl = text.indexOf('\n', start + 3)
        val end = text.indexOf("```", if (nl >= 0) nl else start + 3)
        if (end < 0) {
            parts.add(MdSegment.Text(text.substring(start)))
            break
        }
        val codeStart = if (nl in (start + 3) until end) nl + 1 else start + 3
        parts.add(MdSegment.Code(text.substring(codeStart, end)))
        i = end + 3
    }
    return parts
}

@Composable
fun MarkdownText(text: String, modifier: Modifier = Modifier) {
    val segments = remember(text) { splitCodeFences(text) }
    Column(modifier) {
        for (seg in segments) {
            when (seg) {
                is MdSegment.Code -> Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                ) {
                    Text(
                        seg.text.trimEnd('\n'),
                        fontFamily = FontFamily.Monospace,
                        fontSize = 13.sp,
                        modifier = Modifier.padding(10.dp),
                    )
                }
                is MdSegment.Text -> if (seg.text.isNotBlank()) {
                    Text(
                        seg.text.trimEnd(),
                        style = MaterialTheme.typography.bodyMedium,
                        lineHeight = 21.sp,
                    )
                }
            }
        }
    }
}

private val timeFmt = SimpleDateFormat("HH:mm", Locale.getDefault())
private val dateFmt = SimpleDateFormat("MMM d", Locale.getDefault())

fun formatEpoch(ms: Long): String =
    if (ms <= 0L) "" else timeFmt.format(Date(ms))

fun formatEpochDate(ms: Long): String =
    if (ms <= 0L) "" else dateFmt.format(Date(ms))

fun percentColor(fraction: Float): Color = when {
    fraction >= 0.85f -> Color(0xFFFF5C7A)
    fraction >= 0.6f -> Color(0xFFFFB86B)
    else -> Color(0xFF2EE6C8)
}
