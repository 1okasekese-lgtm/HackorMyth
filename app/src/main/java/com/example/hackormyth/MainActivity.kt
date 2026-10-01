package com.example.hackormyth

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.tooling.preview.Preview

val dark = Color(0xFF202020)
val grey = Color(0xFFDDDDDD)

data class Question(val text: String, val answer: Boolean, val why: String)

val questions = listOf(
    Question("Ctrl+F can help you find words on a web page.", true,
        "Most browsers use Ctrl+F to search the page."),
    Question("Keeping a separate backup can help if your device breaks.", true,
        "You can get your files back from the copy."),
    Question("Renaming a PNG file to JPG converts the image.", false,
        "Only the name changes, the image data stays the same."),
    Question("A password manager can help you use different passwords for each account.", true,
        "It makes and stores passwords for you so you don't have to remember them."),
    Question("Private browsing makes you invisible to your internet provider.", false,
        "It only stops the browser saving stuff on your device."),
    Question("Closing a document always saves your latest changes.", false,
        "Depends on the app. Unsaved changes can get lost."),
    Question("A screen lock helps protect your phone if it is lost.", true,
        "Other people can't just open your apps and data."),
    Question("A familiar email sender name proves the message is genuine.", false,
        "Display names are easy to fake."),
    Question("Turning off unnecessary notifications can reduce interruptions.", true,
        "Less alerts, less distraction."),
    Question("You can often reopen a closed browser tab with Ctrl+Shift+T.", true,
        "Works in most desktop browsers."),
    Question("HTTPS guarantees that a website is honest.", false,
        "HTTPS only encrypts the connection. Scam sites can use it too."),
    Question("A screenshot can capture an error message for troubleshooting.", true,
        "You can look at it later or send it to someone who can help."),
    Question("A QR code is always safe because it is an image.", false,
        "It can still send you to a fake or dangerous website."),
    Question("Emptying the recycle bin guarantees files can never be recovered.", false,
        "Sometimes they can still be recovered, it depends on the device."),
    Question("Reviewing app permissions can help limit access to your data.", true,
        "You can turn off things the app doesn't need, like location or microphone.")
)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MaterialTheme(
                colorScheme = lightColorScheme(
                    primary = dark,
                    onPrimary = Color.White,
                    background = Color.White,
                    onBackground = dark,
                    surface = Color.White,
                    onSurface = dark,
                    onSurfaceVariant = dark,
                    outline = grey
                )
            ) {
                QuizApp()
            }
        }
    }
}

@Composable
fun QuizApp() {
    var screen by rememberSaveable { mutableStateOf("home") }
    var current by rememberSaveable { mutableStateOf(0) }
    var showDialog by rememberSaveable { mutableStateOf(false) }
    var order by rememberSaveable { mutableStateOf(questions.indices.toList().toIntArray()) }

    // -1 = not answered, 0 = myth, 1 = hack. indexed by question, not by position
    var answers by rememberSaveable { mutableStateOf(IntArray(questions.size) { -1 }) }

    var score = 0
    for (i in questions.indices) {
        if (answers[i] != -1 && (answers[i] == 1) == questions[i].answer) score++
    }

    fun start() {
        order = questions.indices.toList().shuffled().toIntArray()
        answers = IntArray(questions.size) { -1 }
        current = 0
        screen = "quiz"
    }

    BackHandler(enabled = screen != "home") {
        if (screen == "quiz") showDialog = true
        else if (screen == "review") screen = "results"
        else screen = "home"
    }

    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            containerColor = Color.White,
            title = { Text("Leave quiz?") },
            text = { Text("You will lose your progress.") },
            confirmButton = {
                TextButton(onClick = {
                    showDialog = false
                    screen = "home"
                }) { Text("Leave") }
            },
            dismissButton = {
                TextButton(onClick = { showDialog = false }) { Text("Stay") }
            }
        )
    }

    Scaffold { padding ->
        Box(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentAlignment = Alignment.TopCenter
        ) {
            key(screen, current) {
                Column(
                    modifier = Modifier
                        .widthIn(max = 560.dp)
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(24.dp),
                    verticalArrangement = Arrangement.spacedBy(24.dp)
                ) {
                    if (screen == "home") {
                        Spacer(Modifier.height(24.dp))
                        Text("Hack or Myth?", style = MaterialTheme.typography.displaySmall,
                            fontWeight = FontWeight.Bold)
                        Text("Useful tip or total myth?", style = MaterialTheme.typography.titleLarge)

                        QuizCard {
                            Text("${questions.size} questions", fontWeight = FontWeight.SemiBold)
                            Text("Pick Hack if you think it's true and Myth if it's false. 1 point for each right answer.")
                            Text("There's no timer.")
                        }

                        QuizButton("Start Quiz") { start() }
                    }

                    if (screen == "quiz") {
                        val qIndex = order[current]
                        val q = questions[qIndex]
                        val picked = answers[qIndex]
                        val answered = picked != -1

                        Row(modifier = Modifier.fillMaxWidth()) {
                            Text("Question ${current + 1} / ${questions.size}",
                                modifier = Modifier.weight(1f))
                            Text("$score points", fontWeight = FontWeight.Bold)
                        }

                        var done = current
                        if (answered) done++
                        LinearProgressIndicator(
                            progress = { done.toFloat() / questions.size },
                            modifier = Modifier.fillMaxWidth(),
                            color = dark,
                            trackColor = grey
                        )

                        QuizCard {
                            Text("HACK OR MYTH?", style = MaterialTheme.typography.labelMedium)
                            Heading(q.text)
                        }

                        for (choice in listOf(true, false)) {
                            val value = if (choice) 1 else 0
                            val isPicked = picked == value
                            QuizButton(
                                text = label(choice) + if (isPicked) " · Selected" else "",
                                enabled = !answered,
                                outlined = true,
                                selected = isPicked
                            ) {
                                val copy = answers.copyOf()
                                copy[qIndex] = value
                                answers = copy
                            }
                        }

                        if (answered) {
                            QuizCard { Result(q, picked) }

                            val last = current == order.size - 1
                            QuizButton(if (last) "See Results" else "Next Question") {
                                if (last) screen = "results" else current++
                            }
                        }

                        TextButton(onClick = { showDialog = true }) { Text("Quit quiz") }
                    }

                    if (screen == "results") {
                        val percent = score * 100 / questions.size

                        Heading("Round complete")

                        QuizCard {
                            Text("$score / ${questions.size}",
                                style = MaterialTheme.typography.displayLarge,
                                fontWeight = FontWeight.Bold)
                            Text("$percent% correct")

                            val msg = if (score == questions.size) "Perfect round!"
                            else if (percent >= 80) "Myth master!"
                            else if (percent >= 50) "Not bad!"
                            else "Keep trying!"
                            Heading(msg)
                        }

                        QuizButton("Review Answers") { screen = "review" }
                        QuizButton("Play Again", outlined = true) { start() }
                        TextButton(onClick = { screen = "home" }) { Text("Back to Home") }
                    }

                    if (screen == "review") {
                        Heading("Your answers")
                        Text("$score correct out of ${questions.size}")

                        order.forEachIndexed { pos, qIndex ->
                            val q = questions[qIndex]
                            QuizCard {
                                Text("Question ${pos + 1}", style = MaterialTheme.typography.labelLarge)
                                Text(q.text, style = MaterialTheme.typography.titleMedium)
                                HorizontalDivider(color = grey)
                                Result(q, answers[qIndex])
                            }
                        }

                        QuizButton("Back to Results") { screen = "results" }
                    }
                }
            }
        }
    }
}

@Composable
fun Heading(text: String) {
    Text(text, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.SemiBold)
}

@Composable
fun QuizCard(content: @Composable ColumnScope.() -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, grey),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            content = content
        )
    }
}

@Composable
fun QuizButton(
    text: String,
    enabled: Boolean = true,
    outlined: Boolean = false,
    selected: Boolean = false,
    onClick: () -> Unit
) {
    val filled = !outlined || selected

    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = Modifier.fillMaxWidth().heightIn(min = 52.dp),
        shape = RoundedCornerShape(12.dp),
        border = if (filled) null else BorderStroke(1.dp, grey),
        colors = ButtonDefaults.buttonColors(
            containerColor = if (filled) dark else Color.White,
            contentColor = if (filled) Color.White else dark,
            disabledContainerColor = if (selected) dark else Color.White,
            disabledContentColor = if (selected) Color.White else dark
        )
    ) {
        Text(text, style = MaterialTheme.typography.titleMedium)
    }
}

@Composable
fun Result(q: Question, picked: Int) {
    val right = picked != -1 && (picked == 1) == q.answer

    Text(
        if (picked == -1) "Not answered" else if (right) "Correct!" else "Wrong!",
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold
    )
    Text("Your answer: " + if (picked == -1) "none" else label(picked == 1))
    Text("Correct answer: " + label(q.answer))
    Text(q.why)
}

fun label(isTrue: Boolean): String {
    return if (isTrue) "Hack (True)" else "Myth (False)"
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun QuizPreview() {
    MaterialTheme(
        colorScheme = lightColorScheme(
            primary = dark,
            onPrimary = Color.White,
            background = Color.White,
            onBackground = dark,
            surface = Color.White,
            onSurface = dark,
            onSurfaceVariant = dark,
            outline = grey
        )
    ) {
        QuizApp()
    }
}