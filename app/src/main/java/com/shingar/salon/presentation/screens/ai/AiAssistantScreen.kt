package com.shingar.salon.presentation.screens.ai

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.shingar.salon.data.model.AiMessage
import com.shingar.salon.presentation.components.SalonTopBar
import com.shingar.salon.ui.theme.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun AiAssistantScreen(navController: NavController) {
    var userInput by remember { mutableStateOf("") }
    var messages by remember { mutableStateOf(listOf(
        AiMessage(content = "Hello! I'm your Shingar AI Business Assistant. I can help you with:\n\n" +
            "- Poster text ideas\n- Offer ideas\n- Customer reply suggestions\n" +
            "- Instagram caption ideas\n- Business growth tips\n\nHow can I help you today?",
            isFromUser = false)
    )) }
    var isTyping by remember { mutableStateOf(false) }
    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()

    val quickPrompts = listOf(
        "Poster text ideas", "New offer ideas", "Instagram captions",
        "Customer reply help", "Growth tips", "Festival offers"
    )

    Scaffold(
        topBar = {
            SalonTopBar(title = "AI Business Assistant", onBackClick = { navController.popBackStack() })
        }
    ) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).background(ScreenBackground)
        ) {
            // Messages
            LazyColumn(
                modifier = Modifier.weight(1f),
                state = listState,
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(messages) { message ->
                    ChatBubble(message = message)
                }
                if (isTyping) {
                    item {
                        Row(
                            modifier = Modifier.padding(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier.size(32.dp).clip(CircleShape)
                                    .background(SoftPurple.copy(alpha = 0.1f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.AutoAwesome, contentDescription = null,
                                    tint = SoftPurple, modifier = Modifier.size(16.dp))
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Typing...", color = TextSecondary, fontSize = 14.sp)
                        }
                    }
                }
            }

            // Quick Prompts
            if (messages.size <= 1) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    quickPrompts.take(3).forEach { prompt ->
                        SuggestionChip(
                            onClick = { userInput = prompt },
                            label = { Text(prompt, fontSize = 11.sp) },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(20.dp)
                        )
                    }
                }
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    quickPrompts.drop(3).forEach { prompt ->
                        SuggestionChip(
                            onClick = { userInput = prompt },
                            label = { Text(prompt, fontSize = 11.sp) },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(20.dp)
                        )
                    }
                }
            }

            // Input
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shadowElevation = 8.dp,
                color = White
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = userInput,
                        onValueChange = { userInput = it },
                        modifier = Modifier.weight(1f),
                        placeholder = { Text("Ask me anything...", color = TextSecondary) },
                        shape = RoundedCornerShape(24.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = PinkPrimary,
                            unfocusedBorderColor = MediumGray.copy(alpha = 0.3f)
                        ),
                        maxLines = 3
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    FloatingActionButton(
                        onClick = {
                            if (userInput.isNotBlank() && !isTyping) {
                                val query = userInput
                                messages = messages + AiMessage(content = query, isFromUser = true)
                                userInput = ""
                                isTyping = true
                                scope.launch {
                                    listState.animateScrollToItem(messages.size)
                                    delay(1500)
                                    val response = getMockAiResponse(query)
                                    messages = messages + AiMessage(content = response, isFromUser = false)
                                    isTyping = false
                                    delay(100)
                                    listState.animateScrollToItem(messages.size)
                                }
                            }
                        },
                        containerColor = PinkPrimary,
                        contentColor = White,
                        modifier = Modifier.size(48.dp)
                    ) {
                        Icon(Icons.Default.Send, contentDescription = "Send")
                    }
                }
            }
        }
    }
}

@Composable
private fun ChatBubble(message: AiMessage) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (message.isFromUser) Arrangement.End else Arrangement.Start
    ) {
        if (!message.isFromUser) {
            Box(
                modifier = Modifier.size(36.dp).clip(CircleShape)
                    .background(Brush.linearGradient(listOf(PinkPrimary, SoftPurple))),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = White, modifier = Modifier.size(18.dp))
            }
            Spacer(modifier = Modifier.width(8.dp))
        }

        Card(
            modifier = Modifier.widthIn(max = 300.dp),
            shape = RoundedCornerShape(
                topStart = if (message.isFromUser) 16.dp else 4.dp,
                topEnd = if (message.isFromUser) 4.dp else 16.dp,
                bottomStart = 16.dp,
                bottomEnd = 16.dp
            ),
            colors = CardDefaults.cardColors(
                containerColor = if (message.isFromUser) PinkPrimary else White
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
        ) {
            Text(
                text = message.content,
                modifier = Modifier.padding(12.dp),
                color = if (message.isFromUser) White else TextPrimary,
                fontSize = 14.sp,
                lineHeight = 20.sp
            )
        }
    }
}

private fun getMockAiResponse(query: String): String {
    val lowerQuery = query.lowercase()
    return when {
        lowerQuery.contains("poster") || lowerQuery.contains("banner") -> """
            Here are some poster text ideas:

            1. "Glow Up Season is Here! Book your bridal package today and save 20%"

            2. "Monsoon Special - Hair Spa + Cut just ₹1999! Limited time offer"

            3. "Your beauty, our passion. Visit Shingar Sallon for the ultimate makeover experience"

            4. "Celebrate your special day with our expert bridal makeup artists. Book now!"

            5. "New Month, New You! Get flat 15% off on all facial treatments"
        """.trimIndent()

        lowerQuery.contains("offer") || lowerQuery.contains("deal") || lowerQuery.contains("festival") -> """
            Here are some offer ideas for your salon:

            1. Monsoon Hair Care Package - Hair Spa + Cut + Styling at ₹1999 (Save ₹1500)

            2. Bridal Season Combo - Complete bridal package with 20% off for early bookings

            3. Refer-a-Friend - Both get 15% off on next visit

            4. Birthday Special - 25% off for customers on their birthday week

            5. First Visit Offer - Get a free threading with any service above ₹500

            6. Festival Package - Diwali/Navratri special combo at ₹2499
        """.trimIndent()

        lowerQuery.contains("instagram") || lowerQuery.contains("caption") || lowerQuery.contains("social") -> """
            Instagram caption ideas:

            1. "Every face tells a story. Let us make yours unforgettable ✨ #ShringarSallon #BeautyGoals"

            2. "From natural to glamorous, we've got you covered 💄 Book your slot today!"

            3. "Because you deserve to shine! ⭐ Our bridal makeup transformation"

            4. "Self-care Sunday starts here 🧖‍♀️ #SalonLife #GlowUp"

            5. "New week, new look! Who's ready for a makeover? 💇‍♀️ #HairGoals"

            6. "Behind every beautiful bride is an amazing makeup artist 👰 #BridalMakeup"
        """.trimIndent()

        lowerQuery.contains("customer") || lowerQuery.contains("reply") || lowerQuery.contains("message") -> """
            Customer reply templates:

            1. Booking Confirmation:
            "Thank you for booking with Shingar Sallon! Your appointment is confirmed for [date/time]. See you soon! ✨"

            2. Reminder:
            "Hi [Name]! Just a reminder about your appointment tomorrow. Looking forward to seeing you! 💇‍♀️"

            3. Thank You:
            "Thank you for visiting Shingar Sallon! We hope you loved your [service]. See you again soon! 🌟"

            4. Birthday Wish:
            "Happy Birthday, [Name]! 🎂 Enjoy 25% off on any service this week as our birthday gift to you!"

            5. Re-engagement:
            "We miss you at Shingar Sallon! Book your next appointment and get 10% off. 💕"
        """.trimIndent()

        lowerQuery.contains("growth") || lowerQuery.contains("tips") || lowerQuery.contains("business") -> """
            Business growth tips for your salon:

            1. Loyalty Program - Start a points system where customers earn rewards on every visit

            2. Google My Business - Set up and optimize your listing for local search visibility

            3. Before/After Posts - Share transformation photos on social media regularly

            4. Seasonal Packages - Create time-limited combos for festivals and seasons

            5. Staff Training - Invest in upskilling your team for new trends

            6. Customer Feedback - Collect reviews and act on feedback to improve services

            7. WhatsApp Marketing - Send personalized offers and reminders via WhatsApp

            8. Partnerships - Collaborate with wedding planners, photographers for referrals
        """.trimIndent()

        else -> """
            Great question! Here are some suggestions:

            - Try creating seasonal offers to boost bookings
            - Use social media to showcase before/after transformations
            - Send birthday greetings to customers with special discounts
            - Keep track of popular services and stock accordingly
            - Train staff on trending techniques

            Feel free to ask me about poster ideas, offers, Instagram captions, customer replies, or business growth tips!
        """.trimIndent()
    }
}
