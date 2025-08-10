/* While this template provides a good starting point for using Wear Compose, you can always
 * take a look at https://github.com/android/wear-os-samples/tree/main/ComposeStarter to find the
 * most up to date changes to the libraries and their usages.
 */

package eu.natesantos.german1.presentation

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.wear.compose.material.*
import kotlin.math.abs
import kotlin.random.Random

// Data class to hold the German word and its English translation.
data class Word(val german: String, val english: String)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // The list of German words with their English translations.
        // These are extracted and translated from the provided RTF file.
        val wordList = listOf(
            Word("Mann", "Man"), Word("Kann", "Can"), Word("Dann", "Then"),
            Word("Denn", "Because"), Word("Kennt", "Knows"), Word("Kennen", "To know"),
            Word("Bin", "Am"), Word("Ist", "Is"), Word("Kiste", "Box"),
            Word("Ich", "I"), Word("Mich", "Me"), Word("Nicht", "Not"),
            Word("Feucht", "Moist"), Word("Euch", "You (plural)"), Word("Nennen", "To name"),
            Word("Nannte", "Named"), Word("Eule", "Owl"), Word("Leute", "People"),
            Word("Nichte", "Niece"), Word("Leicht", "Easy / Light"), Word("Heute", "Today"),
            Word("Meine", "My"), Word("Leiten", "To lead"), Word("Leuchten", "To shine"),
            Word("Heule", "Cry"), Word("Nass", "Wet"), Word("Heiße", "Am called"),
            Word("Guten Tag", "Good day"), Word("Kleider", "Clothes"), Word("Aber", "But"),
            Word("Beißen", "To bite"), Word("Leider", "Unfortunately"), Word("Lieder", "Songs"),
            Word("Diene", "Serve"), Word("Leiden", "To suffer"), Word("Klein", "Small"),
            Word("Bleiben", "To stay"), Word("Hund", "Dog"), Word("Sind", "Are"),
            Word("Nein", "No"), Word("Name", "Name"), Word("Groß", "Big / Large"),
            Word("Danke", "Thank you"), Word("Gut", "Good"), Word("Auch", "Also"),
            Word("Wo", "Where"), Word("Hier", "Here"), Word("Sicher", "Sure / Safe"),
            Word("Links", "Left"), Word("Wasser", "Water"), Word("Bier", "Beer"),
            Word("Wein", "Wine"), Word("Tee", "Tea"), Word("Glas", "Glass"),
            Word("Besser", "Better"), Word("Möglich", "Possible"), Word("Spät", "Late"),
            Word("Essen", "To eat"), Word("Traurig", "Sad"), Word("Fertig", "Finished"),
            Word("Sehr", "Very"), Word("Billig", "Cheap"), Word("Reich", "Rich"),
            Word("Nötig", "Necessary"), Word("Sauber", "Clean"), Word("Sagen", "To say"),
            Word("Mädchen", "Girl"), Word("Kind", "Child"), Word("Schläft", "Sleeps"),
            Word("Karte", "Card / Map"), Word("Löffel", "Spoon"), Word("Suppe", "Soup"),
            Word("Fragen", "To ask"), Word("Antworten", "To answer"), Word("Trägt", "Wears / Carries"),
            Word("Essig", "Vinegar"), Word("Öl", "Oil"), Word("Montag", "Monday"),
            Word("Verstehen", "To understand"), Word("Verkaufen", "To sell"), Word("Weit", "Far"),
            Word("Viel", "Much / A lot"), Word("Geld", "Money"), Word("Wieviel", "How much"),
            Word("Juni", "June"), Word("Jahr", "Year"), Word("Fahren", "To drive"),
            Word("Weil", "Because"), Word("Zeit", "Time"), Word("Auto", "Car"),
            Word("Arbeiten", "To work"), Word("Frau", "Woman / Mrs."), Word("Abend", "Evening"),
            Word("Kaufen", "To buy"), Word("Wann", "When"), Word("Helfen", "To help"),
            Word("Gerne", "Gladly"), Word("Zwei", "Two"), Word("Milch", "Milk")
        )

        setContent {
            WearApp(wordList)
        }
    }
}

@Composable
fun WearApp(wordList: List<Word>) {
    // State to hold the index of the current word in the list.
    var currentWordIndex by remember { mutableStateOf(Random.nextInt(wordList.size)) }
    // State to track if the English translation should be revealed.
    var revealed by remember { mutableStateOf(false) }
    // State for managing swipe gestures.
    val offsetY = remember { mutableStateOf(0f) }

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
                            onDragEnd = {
                                // Reset offset after drag ends.
                                offsetY.value = 0f
                            },
                            onDrag = { change, dragAmount ->
                                change.consume()
                                val (x, y) = dragAmount
                                // Detect vertical swipe up to reveal translation.
                                if (abs(y) > abs(x)) {
                                    if (y < -20) { // Swipe up
                                        revealed = true
                                    }
                                }
                                // Detect horizontal swipe right for the next word.
                                if (abs(x) > abs(y)) {
                                    if (x > 20) { // Swipe right
                                        var newIndex: Int
                                        do {
                                            newIndex = Random.nextInt(wordList.size)
                                        } while (newIndex == currentWordIndex) // Ensure new word is different
                                        currentWordIndex = newIndex
                                        revealed = false // Hide translation for the new word
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
                    Text(
                        text = wordList[currentWordIndex].german,
                        fontWeight = FontWeight.Bold,
                        fontSize = 34.sp,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colors.primary
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
                        Text(
                            text = wordList[currentWordIndex].english,
                            fontSize = 24.sp,
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colors.secondary
                        )
                    }
                }
            }
        }
    }
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