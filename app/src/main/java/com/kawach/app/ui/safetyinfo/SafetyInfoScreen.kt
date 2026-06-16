package com.kawach.app.ui.safetyinfo

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.kawach.app.ui.theme.SosRed

// Pre-defined Indian emergency helplines
private data class Helpline(val number: String, val name: String, val icon: ImageVector)

private val nationalHelplines = listOf(
    Helpline("112", "National Emergency", Icons.Filled.LocalPolice),
    Helpline("100", "Police", Icons.Filled.LocalPolice),
    Helpline("108", "Ambulance", Icons.Filled.LocalHospital),
    Helpline("101", "Fire Service", Icons.Filled.Fireplace),
    Helpline("1091", "Women's Helpline", Icons.Filled.Woman),
    Helpline("1098", "Child Helpline (Childline)", Icons.Filled.ChildCare),
    Helpline("181", "Women Domestic Violence Helpline", Icons.Filled.SafetyCheck),
    Helpline("1930", "Cyber Crime Helpline", Icons.Filled.Security),
)

private val safetyTips = listOf(
    "अपने घर से निकलने से पहले किसी को बताएं — कहाँ जा रहे हैं, कब वापस आएंगे।",
    "Kawach Walking Timer का इस्तेमाल करें रात को अकेले चलते समय।",
    "अनजान जगहों पर अकेले न जाएं — group में जाएं।",
    "फोन हमेशा charge रखें। Emergency contacts पहले से save करें।",
    "अगर कोई follow कर रहा हो — भीड़ वाली जगह, दुकान, या police station की ओर जाएं।",
    "SOS दबाने के बाद — शांत रहें, location pin नोट करें, आवाज़ लगाएं।",
    "अपने घर के पास का police station का नंबर save करें।",
    "रात को well-lit रास्तों से जाएं। Shortcuts से बचें।",
    "Public transport में — front की seat prefer करें।",
    "अगर गाड़ी में बैठाने की कोशिश हो — लड़ें, चिल्लाएं, भागें। गाड़ी में मत बैठें।",
)

@Composable
fun SafetyInfoScreen(paddingValues: PaddingValues) {
    val context = LocalContext.current

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        item { Spacer(Modifier.height(8.dp)) }

        // ── National Helplines ─────────────────────────────────────────────
        item {
            Text(
                "Emergency Helplines",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
            Text(
                "एक tap करें — सीधे call होगी",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
            Spacer(Modifier.height(8.dp))
        }

        nationalHelplines.forEach { helpline ->
            item(key = helpline.number) {
                HelplineCard(helpline) {
                    context.startActivity(
                        Intent(Intent.ACTION_DIAL, Uri.parse("tel:${helpline.number}"))
                    )
                }
            }
        }

        item { Spacer(Modifier.height(16.dp)) }

        // ── Safety Tips ────────────────────────────────────────────────────
        item {
            Text(
                "Safety Tips",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(8.dp))
        }

        safetyTips.forEachIndexed { index, tip ->
            item(key = "tip_$index") {
                SafetyTipCard(number = index + 1, tip = tip)
            }
        }

        item { Spacer(Modifier.height(24.dp)) }

        // ── About Kawach ───────────────────────────────────────────────────
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Column(Modifier.padding(16.dp)) {
                    Text(
                        "कवच के बारे में",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        "Kawach एक offline-first personal safety app है। " +
                            "कोई login नहीं, कोई internet नहीं, कोई data cloud पर नहीं जाता। " +
                            "सब कुछ आपके phone में रहता है।",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        "Version 1.0 • Made in India 🇮🇳",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                }
            }
        }

        item { Spacer(Modifier.height(16.dp)) }
    }
}

@Composable
private fun HelplineCard(helpline: Helpline, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = MaterialTheme.shapes.medium,
                color = MaterialTheme.colorScheme.primaryContainer,
                modifier = Modifier.size(44.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        helpline.icon,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(helpline.name, style = MaterialTheme.typography.bodyLarge)
                Text(
                    helpline.number,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.ExtraBold,
                    color = SosRed
                )
            }
            Icon(
                Icons.Filled.Call,
                contentDescription = "Call",
                tint = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
private fun SafetyTipCard(number: Int, tip: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Surface(
            shape = MaterialTheme.shapes.small,
            color = MaterialTheme.colorScheme.primaryContainer,
            modifier = Modifier.size(32.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Text(
                    number.toString(),
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
        Text(
            tip,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier
                .weight(1f)
                .padding(top = 6.dp)
        )
    }
}
