
package eu.natesantos.german1.presentation

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.isUnspecified
import androidx.compose.ui.unit.sp
import androidx.wear.compose.material.Colors
import androidx.wear.compose.material.Icon
import androidx.wear.compose.material.MaterialTheme
import androidx.wear.compose.material.Scaffold
import androidx.wear.compose.material.Shapes
import androidx.wear.compose.material.Text
import androidx.wear.compose.material.TimeText
import androidx.wear.compose.material.Typography
import androidx.wear.compose.material.Vignette
import androidx.wear.compose.material.VignettePosition
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import kotlin.math.abs
import kotlin.random.Random

// Data class to hold the German word and its English translation.
data class Word(val german: String, val english: String)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // The list of words will be loaded from the CSV file.
        val wordList = mutableStateListOf<Word>()

        try {
            // Get the resource identifier for the CSV file.
            val resourceId = resources.getIdentifier("words_a1_2", "raw", packageName)
            val inputStream = resources.openRawResource(resourceId)
            val reader = BufferedReader(InputStreamReader(inputStream))

            // Read each line from the CSV file.
            reader.forEachLine { line ->
                val tokens = line.split(',')
                if (tokens.size == 2) {
                    wordList.add(Word(tokens[0], tokens[1]))
                }
            }
        } catch (e: IOException) {
            Log.e("MainActivity", "Error reading CSV file", e)
            // Add a default word if the file can't be read, so the app doesn't crash.
            wordList.add(Word("Fehler", "Error"))
        }


        setContent {
            if (wordList.isNotEmpty()) {
                WearApp(wordList)
            } else {
                // Show a loading or error state if the word list is empty.
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Loading words...")
                }
            }
        }
    }
}

@Composable
fun WearApp(wordList: List<Word>) {
    // State to hold the index of the current word in the list.
    var currentWordIndex by remember { mutableStateOf(Random.nextInt(wordList.size)) }
    // State to track if the English translation should be revealed.
    var revealed by remember { mutableStateOf(false) }

    // Animation for the reveal effect.
    val animatedOffsetY by animateFloatAsState(targetValue = if (revealed) -100f else 0f)

    GermanFlashcardsTheme {
        Scaffold(
            vignette = { Vignette(vignettePosition = VignettePosition.TopAndBottom) },
            timeText = { TimeText() }
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colors.background)
                    .pointerInput(Unit) {
                        detectDragGestures(
                            onDrag = { change, dragAmount ->
                                change.consume()
                                val (x, y) = dragAmount

                                // Check for horizontal vs. vertical swipe dominance.
                                if (abs(x) > abs(y)) {
                                    // Horizontal swipe (left or right) for a new word.
                                    if (abs(x) > 20) {
                                        var newIndex: Int
                                        do {
                                            newIndex = Random.nextInt(wordList.size)
                                        } while (newIndex == currentWordIndex) // Ensure new word is different.
                                        currentWordIndex = newIndex
                                        revealed = false // Hide translation for the new word.
                                    }
                                } else {
                                    // Vertical swipe.
                                    if (y < -20) { // Swipe up to reveal translation.
                                        revealed = true
                                    } else if (y > 20) { // Swipe down to hide translation.
                                        revealed = false
                                    }
                                }
                            }
                        )
                    },
                contentAlignment = Alignment.Center
            ) {
                // Box for the German word.
                Box(
                    modifier = Modifier
                        .offset(y = animatedOffsetY.dp)
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    AutoResizeText(
                        text = wordList[currentWordIndex].german,
                        style = TextStyle(
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colors.primary
                        ),
                        maxFontSize = 34.sp
                    )
                }
                // Box for the English translation, shown when 'revealed' is true.
                if (revealed) {
                    Box(
                        modifier = Modifier
                            .offset(y = (animatedOffsetY + 100).dp)
                            .padding(16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        AutoResizeText(
                            text = wordList[currentWordIndex].english,
                            style = TextStyle(
                                textAlign = TextAlign.Center,
                                color = MaterialTheme.colors.secondary
                            ),
                            maxFontSize = 24.sp
                        )
                    }
                }

                // Visual indicator to swipe up for translation.
                AnimatedVisibility(
                    visible = !revealed,
                    modifier = Modifier.align(Alignment.BottomCenter)
                ) {
                    Icon(
                        imageVector = Icons.Default.KeyboardArrowUp,
                        contentDescription = "Swipe up to reveal translation",
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                }

                // Visual indicator to swipe down to hide translation.
                AnimatedVisibility(
                    visible = revealed,
                    modifier = Modifier.align(Alignment.BottomCenter)
                ) {
                    Icon(
                        imageVector = Icons.Default.KeyboardArrowDown,
                        contentDescription = "Swipe down to hide translation",
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                }
            }
        }
    }
}

/**
 * A composable that automatically resizes text to fit its container.
 * @param text The text to display.
 * @param style The base style of the text.
 * @param modifier The modifier for this composable.
 * @param maxFontSize The maximum font size the text can be.
 */
@Composable
fun AutoResizeText(
    text: String,
    style: TextStyle,
    modifier: Modifier = Modifier,
    maxFontSize: androidx.compose.ui.unit.TextUnit
) {
    var scaledTextStyle by remember { mutableStateOf(style.copy(fontSize = maxFontSize)) }
    var readyToDraw by remember { mutableStateOf(false) }

    Text(
        text = text,
        modifier = modifier.drawWithContent {
            if (readyToDraw) {
                drawContent()
            }
        },
        style = scaledTextStyle,
        softWrap = false,
        onTextLayout = { textLayoutResult ->
            if (textLayoutResult.didOverflowWidth) {
                if (scaledTextStyle.fontSize.isUnspecified) {
                    scaledTextStyle = scaledTextStyle.copy(fontSize = maxFontSize)
                }
                scaledTextStyle = scaledTextStyle.copy(fontSize = scaledTextStyle.fontSize * 0.9)
            } else {
                readyToDraw = true
            }
        }
    )
}


// Custom theme for the flashcards app.
@Composable
fun GermanFlashcardsTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colors = Colors(
            primary = Color(0xFF66BB6A), // A pleasant green for the German word
            secondary = Color(0xFF4DB6AC), // A teal color for the English translation
            background = Color.Black
        ),
        typography = Typography(),
        shapes = Shapes(),
        content = content
    )
}
