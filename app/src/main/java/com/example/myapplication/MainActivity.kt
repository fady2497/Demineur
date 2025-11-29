package com.example.myapplication

import android.Manifest
import android.animation.ObjectAnimator
import android.animation.PropertyValuesHolder
import android.content.*
import android.graphics.*
import android.graphics.drawable.Drawable
import android.graphics.drawable.GradientDrawable
import android.media.SoundPool
import android.os.*
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.view.animation.OvershootInterpolator
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.annotation.RequiresPermission
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsControllerCompat
import kotlin.math.*
import java.util.*

@Suppress("DEPRECATION")
class MainActivity : AppCompatActivity() {

    private lateinit var mainLayout: LinearLayout
    private var selectedDifficulty: Difficulty = Difficulty.MEDIUM
    private var gameResult: GameResult? = null
    private var scoreManager = ScoreManager()
    private var soundManager: SoundManager? = null
    private var vibrator: Vibrator? = null

    private lateinit var aiAssistant: AIGameAssistant
    private lateinit var advancedAnalytics: AdvancedAnalytics
    private lateinit var futuristicAnimations: FuturisticAnimations
    private lateinit var saveManager: SaveManager
    private lateinit var achievementManager: AchievementManager
    private var currentTheme: Theme = Theme.CLASSIC
    private var gameMode: GameMode = GameMode.CLASSIC

    private var isBombMarkingMode = false
    private var markedBombsCount = 0

    private lateinit var bombMoodManager: BombMoodManager
    private lateinit var bombImageView: BombImageView

    var startTime: Long = 0
        private set

    enum class Difficulty(val rows: Int, val cols: Int, val mines: Int, val displayName: String, val color: Int) {
        EASY(9, 9, 10, "Facile", Color.GREEN),
        MEDIUM(16, 16, 40, "Moyen", Color.YELLOW),
        HARD(16, 30, 99, "Difficile", Color.RED)
    }

    enum class Theme(val primaryColor: Int, val secondaryColor: Int, val accentColor: Int, val displayName: String) {
        CLASSIC(Color.parseColor("#1a1a2e"), Color.parseColor("#16213e"), Color.parseColor("#0f3460"), "Classique"),
        DARK(Color.parseColor("#121212"), Color.parseColor("#1e1e1e"), Color.parseColor("#bb86fc"), "Sombre"),
        NATURE(Color.parseColor("#2e7d32"), Color.parseColor("#1b5e20"), Color.parseColor("#81c784"), "Nature"),
        SUNSET(Color.parseColor("#ff6b35"), Color.parseColor("#f7931e"), Color.parseColor("#ffd166"), "Couché de soleil"),
        NEON(Color.parseColor("#0f0f23"), Color.parseColor("#1a1a2e"), Color.parseColor("#00ffff"), "Néon")
    }

    enum class GameMode(val displayName: String, val description: String) {
        CLASSIC("Classique", "Jeu standard sans limite de temps"),
        TIMED("Chronométré", "Terminez le plus vite possible"),
        BLITZ("Blitz", "Seulement 3 minutes pour gagner"),
        SURVIVAL("Survie", "Évitez les mines le plus longtemps possible")
    }

    data class GameResult(
        val isWin: Boolean,
        val difficulty: Difficulty,
        val progress: Double,
        val cellsRevealed: Int,
        val totalSafeCells: Int,
        val time: Int = 0
    )

    class ScoreManager {
        private val scores = mutableListOf<Score>()

        fun addScore(score: Score) {
            scores.add(score)
            scores.sortBy { it.time }
        }

        fun getBestScores(difficulty: Difficulty): List<Score> {
            return scores.filter { it.difficulty == difficulty }.take(10)
        }

        fun getPlayerStats(): PlayerStats {
            val totalGames = scores.size
            val wonGames = scores.count { it.isWin }
            val bestTime = scores.filter { it.isWin }.minByOrNull { it.time }?.time ?: 0
            val averageTime = if (wonGames > 0) scores.filter { it.isWin }.map { it.time }.average() else 0.0
            val winRate = if (totalGames > 0) wonGames.toDouble() / totalGames.toDouble() else 0.0

            return PlayerStats(totalGames, wonGames, bestTime, averageTime, winRate)
        }
    }

    data class Score(
        val playerName: String = "Joueur",
        val difficulty: Difficulty,
        val time: Int,
        val isWin: Boolean,
        val date: Date = Date()
    )

    data class PlayerStats(
        val gamesPlayed: Int,
        val gamesWon: Int,
        val bestTime: Int,
        val averageTime: Double,
        val winRate: Double
    )

    class SoundManager(private val context: Context) {
        private val soundPool: SoundPool

        init {
            soundPool = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                SoundPool.Builder().setMaxStreams(5).build()
            } else {
                SoundPool(5, android.media.AudioManager.STREAM_MUSIC, 0)
            }
        }

        fun playSound(soundName: String) {
            // Simulation
        }

        fun release() {
            soundPool.release()
        }
    }

    class AIGameAssistant {
        fun getDifficultyHint(): String {
            return when ((0..3).random()) {
                0 -> "💡 Concentrez-vous sur les bords !"
                1 -> "🎯 Analysez les nombres pour déduire les mines"
                2 -> "🌟 Utilisez la logique plutôt que la chance"
                else -> "⚡ Prenez votre temps pour bien analyser"
            }
        }

        fun adaptiveDifficulty(stats: PlayerStats): Difficulty {
            return when {
                stats.winRate > 0.8 -> Difficulty.HARD
                stats.winRate > 0.5 -> Difficulty.MEDIUM
                else -> Difficulty.EASY
            }
        }
    }

    class AdvancedAnalytics {
        private val gameData = mutableListOf<GameSession>()

        fun trackGameSession(session: GameSession) {
            gameData.add(session)
        }

        fun generatePerformanceReport(): PerformanceReport {
            val efficiency = calculateEfficiency()
            return PerformanceReport(
                efficiencyScore = efficiency,
                riskAssessment = calculateRiskLevel(efficiency),
                improvementAreas = identifyWeaknesses(),
                predictedSkillLevel = predictSkillProgression(efficiency)
            )
        }

        private fun calculateEfficiency(): Int = (70 + (0..30).random()).coerceIn(0, 100)
        private fun calculateRiskLevel(efficiency: Int): String = when {
            efficiency > 80 -> "Faible"
            efficiency > 60 -> "Moyen"
            else -> "Élevé"
        }
        private fun identifyWeaknesses(): List<String> = listOf(
            "Trop de drapeaux placés au hasard",
            "Hésitation sur les cases évidentes",
            "Manque d'analyse des patterns"
        ).shuffled().take(2)
        private fun predictSkillProgression(efficiency: Int): String = when {
            efficiency > 85 -> "Expert"
            efficiency > 70 -> "Avancé"
            efficiency > 50 -> "Intermédiaire"
            else -> "Débutant"
        }
    }

    class FuturisticAnimations {
        fun createQuantumReveal(view: View) {
            ObjectAnimator.ofPropertyValuesHolder(
                view,
                PropertyValuesHolder.ofFloat("scaleX", 0f, 1.2f, 1f),
                PropertyValuesHolder.ofFloat("scaleY", 0f, 1.2f, 1f),
                PropertyValuesHolder.ofFloat("alpha", 0f, 1f)
            ).apply {
                duration = 600
                interpolator = OvershootInterpolator()
                start()
            }
        }
    }

    class SaveManager(private val context: Context) {
        private val sharedPref = context.getSharedPreferences("minesweeper_saves", Context.MODE_PRIVATE)
        fun hasSavedGame(): Boolean = sharedPref.contains("saved_game")
        fun deleteSave() = sharedPref.edit().remove("saved_game").apply()
    }

    class AchievementManager {
        private val achievements = mutableListOf<Achievement>()

        data class Achievement(
            val id: String,
            val name: String,
            val description: String,
            val icon: String,
            var isUnlocked: Boolean = false,
            var progress: Int = 0,
            val target: Int = 1
        )

        init {
            achievements.addAll(listOf(
                Achievement("first_win", "Première victoire", "Gagnez votre première partie", "🏆"),
                Achievement("expert", "Expert", "Gagnez 10 parties", "⭐", target = 10),
                Achievement("perfectionist", "Perfectionniste", "Gagnez sans faire d'erreur", "💎"),
                Achievement("speedrunner", "Speedrunner", "Gagnez en moins de 60 secondes", "⚡"),
                Achievement("mine_sweeper", "Démineur", "Découvrez 1000 cases", "💣", target = 1000)
            ))
        }

        fun unlockAchievement(id: String) {
            achievements.find { it.id == id }?.isUnlocked = true
        }

        fun updateProgress(id: String, progress: Int = 1) {
            achievements.find { it.id == id }?.let { achievement ->
                achievement.progress += progress
                if (achievement.progress >= achievement.target) {
                    achievement.isUnlocked = true
                }
            }
        }

        fun getAchievements(): List<Achievement> = achievements
        fun getUnlockedCount(): Int = achievements.count { it.isUnlocked }
    }

    data class GameSession(val duration: Int, val moves: Int, val efficiency: Double, val difficulty: Difficulty)
    data class PerformanceReport(val efficiencyScore: Int, val riskAssessment: String, val improvementAreas: List<String>, val predictedSkillLevel: String)

    // Système d'humeur de la bombe
    class BombMoodManager(private val context: Context) {

        private val prefs: SharedPreferences = context.getSharedPreferences("bomb_mood", Context.MODE_PRIVATE)
        private val LAST_USAGE_KEY = "last_usage"
        private val USAGE_COUNT_KEY = "usage_count"
        private val CONSECUTIVE_DAYS_KEY = "consecutive_days"

        fun updateUsage() {
            val currentTime = System.currentTimeMillis()
            val editor = prefs.edit()

            // Vérifier si c'est un nouveau jour
            val lastUsage = prefs.getLong(LAST_USAGE_KEY, 0L)
            val isNewDay = isNewDay(lastUsage, currentTime)

            // Mettre à jour les jours consécutifs
            val consecutiveDays = if (isNewDay) {
                prefs.getInt(CONSECUTIVE_DAYS_KEY, 0) + 1
            } else {
                prefs.getInt(CONSECUTIVE_DAYS_KEY, 0)
            }

            editor.putLong(LAST_USAGE_KEY, currentTime)
            editor.putInt(USAGE_COUNT_KEY, getUsageCount() + 1)
            editor.putInt(CONSECUTIVE_DAYS_KEY, consecutiveDays)
            editor.apply()
        }

        fun getBombMood(): BombMood {
            val lastUsage = prefs.getLong(LAST_USAGE_KEY, 0L)
            val usageCount = prefs.getInt(USAGE_COUNT_KEY, 0)
            val consecutiveDays = prefs.getInt(CONSECUTIVE_DAYS_KEY, 0)
            val daysSinceLastUsage = getDaysSinceLastUsage(lastUsage)

            return when {
                daysSinceLastUsage >= 7 -> BombMood.VERY_SAD
                daysSinceLastUsage >= 3 -> BombMood.SAD
                daysSinceLastUsage >= 1 -> BombMood.NEUTRAL
                consecutiveDays >= 7 -> BombMood.SUPER_HAPPY
                consecutiveDays >= 3 -> BombMood.HAPPY
                usageCount >= 5 -> BombMood.NORMAL
                else -> BombMood.NORMAL
            }
        }

        fun getStreakInfo(): String {
            val consecutiveDays = prefs.getInt(CONSECUTIVE_DAYS_KEY, 0)
            val lastUsage = prefs.getLong(LAST_USAGE_KEY, 0L)
            val daysSinceLastUsage = getDaysSinceLastUsage(lastUsage)

            return when {
                daysSinceLastUsage >= 1 -> "Série rompue! 😔"
                consecutiveDays == 0 -> "Commencez votre série! 🚀"
                else -> "Série de $consecutiveDays jour${if (consecutiveDays > 1) "s" else ""}! 🔥"
            }
        }

        private fun isNewDay(lastUsage: Long, currentTime: Long): Boolean {
            if (lastUsage == 0L) return true

            val lastCal = Calendar.getInstance().apply { timeInMillis = lastUsage }
            val currentCal = Calendar.getInstance().apply { timeInMillis = currentTime }

            return lastCal.get(Calendar.DAY_OF_YEAR) != currentCal.get(Calendar.DAY_OF_YEAR) ||
                    lastCal.get(Calendar.YEAR) != currentCal.get(Calendar.YEAR)
        }

        private fun getDaysSinceLastUsage(lastUsage: Long): Int {
            if (lastUsage == 0L) return Int.MAX_VALUE

            val lastCal = Calendar.getInstance().apply { timeInMillis = lastUsage }
            val currentCal = Calendar.getInstance()

            val diff = currentCal.timeInMillis - lastCal.timeInMillis
            return (diff / (1000 * 60 * 60 * 24)).toInt()
        }

        private fun getUsageCount(): Int {
            return prefs.getInt(USAGE_COUNT_KEY, 0)
        }

        enum class BombMood {
            SUPER_HAPPY, HAPPY, NORMAL, NEUTRAL, SAD, VERY_SAD
        }
    }

    // Bombe émotionnelle
    class BombImageView @JvmOverloads constructor(
        context: Context,
        attrs: AttributeSet? = null,
        defStyleAttr: Int = 0
    ) : View(context, attrs, defStyleAttr) {

        private var bombMood: BombMoodManager.BombMood = BombMoodManager.BombMood.NORMAL
        private val bombPaint = Paint(Paint.ANTI_ALIAS_FLAG)
        private val eyePaint = Paint(Paint.ANTI_ALIAS_FLAG)
        private val mouthPaint = Paint(Paint.ANTI_ALIAS_FLAG)
        private val fusePaint = Paint(Paint.ANTI_ALIAS_FLAG)
        private val sparklePaint = Paint(Paint.ANTI_ALIAS_FLAG)

        init {
            setupPaints()
        }

        private fun setupPaints() {
            bombPaint.color = Color.parseColor("#333333")
            bombPaint.style = Paint.Style.FILL

            eyePaint.color = Color.WHITE
            eyePaint.style = Paint.Style.FILL

            mouthPaint.color = Color.WHITE
            mouthPaint.style = Paint.Style.STROKE
            mouthPaint.strokeWidth = 8f
            mouthPaint.strokeCap = Paint.Cap.ROUND

            fusePaint.color = Color.parseColor("#8B4513")
            fusePaint.style = Paint.Style.FILL

            sparklePaint.color = Color.YELLOW
            sparklePaint.style = Paint.Style.FILL
        }

        fun setBombMood(mood: BombMoodManager.BombMood) {
            this.bombMood = mood
            invalidate()
        }

        override fun onDraw(canvas: Canvas) {
            super.onDraw(canvas)

            val centerX = width / 2f
            val centerY = height / 2f
            val bombRadius = (minOf(width, height) * 0.4f)

            // Dessiner le corps de la bombe
            canvas.drawCircle(centerX, centerY, bombRadius, bombPaint)

            // Dessiner la mèche
            drawFuse(canvas, centerX, centerY, bombRadius)

            // Dessiner les yeux selon l'humeur
            drawEyes(canvas, centerX, centerY, bombRadius)

            // Dessiner la bouche selon l'humeur
            drawMouth(canvas, centerX, centerY, bombRadius)

            // Effets spéciaux pour les humeurs positives
            if (bombMood == BombMoodManager.BombMood.SUPER_HAPPY) {
                drawSparkles(canvas, centerX, centerY, bombRadius)
            }
        }

        private fun drawFuse(canvas: Canvas, centerX: Float, centerY: Float, radius: Float) {
            val fuseRect = RectF(
                centerX - 15f,
                centerY - radius - 40f,
                centerX + 15f,
                centerY - radius
            )
            canvas.drawRoundRect(fuseRect, 8f, 8f, fusePaint)

            // Flamme de la mèche
            when (bombMood) {
                BombMoodManager.BombMood.SUPER_HAPPY -> {
                    // Flamme joyeuse
                    drawHappyFlame(canvas, centerX, centerY - radius - 40f)
                }
                BombMoodManager.BombMood.SAD, BombMoodManager.BombMood.VERY_SAD -> {
                    // Flamme faible
                    drawSadFlame(canvas, centerX, centerY - radius - 40f)
                }
                else -> {
                    // Flamme normale
                    drawNormalFlame(canvas, centerX, centerY - radius - 40f)
                }
            }
        }

        private fun drawNormalFlame(canvas: Canvas, x: Float, y: Float) {
            val flamePaint = Paint().apply {
                color = Color.RED
                style = Paint.Style.FILL
            }
            canvas.drawCircle(x, y - 15f, 12f, flamePaint)
        }

        private fun drawHappyFlame(canvas: Canvas, x: Float, y: Float) {
            val flamePaint = Paint().apply {
                color = Color.YELLOW
                style = Paint.Style.FILL
            }
            canvas.drawCircle(x, y - 20f, 15f, flamePaint)
        }

        private fun drawSadFlame(canvas: Canvas, x: Float, y: Float) {
            val flamePaint = Paint().apply {
                color = Color.parseColor("#8B0000")
                style = Paint.Style.FILL
            }
            canvas.drawCircle(x, y - 10f, 8f, flamePaint)
        }

        private fun drawEyes(canvas: Canvas, centerX: Float, centerY: Float, radius: Float) {
            val eyeSpacing = radius * 0.5f
            val eyeY = centerY - radius * 0.1f
            val eyeRadius = radius * 0.15f

            // Dessiner les yeux blancs
            canvas.drawCircle(centerX - eyeSpacing, eyeY, eyeRadius, eyePaint)
            canvas.drawCircle(centerX + eyeSpacing, eyeY, eyeRadius, eyePaint)

            // Dessiner les pupilles selon l'humeur
            drawPupils(canvas, centerX, eyeSpacing, eyeY, eyeRadius)
        }

        private fun drawPupils(canvas: Canvas, centerX: Float, eyeSpacing: Float, eyeY: Float, eyeRadius: Float) {
            val pupilPaint = Paint().apply {
                color = Color.BLACK
                style = Paint.Style.FILL
            }

            val (leftPupilX, leftPupilY, rightPupilX, rightPupilY, pupilSize) = when (bombMood) {
                BombMoodManager.BombMood.SUPER_HAPPY -> {
                    // Yeux brillants et joyeux
                    Quadruple(
                        centerX - eyeSpacing,
                        eyeY - eyeRadius * 0.3f,
                        centerX + eyeSpacing,
                        eyeY - eyeRadius * 0.3f,
                        eyeRadius * 0.4f
                    )
                }
                BombMoodManager.BombMood.HAPPY -> {
                    // Yeux joyeux
                    Quadruple(
                        centerX - eyeSpacing,
                        eyeY - eyeRadius * 0.2f,
                        centerX + eyeSpacing,
                        eyeY - eyeRadius * 0.2f,
                        eyeRadius * 0.5f
                    )
                }
                BombMoodManager.BombMood.NORMAL -> {
                    // Yeux normaux
                    Quadruple(
                        centerX - eyeSpacing,
                        eyeY,
                        centerX + eyeSpacing,
                        eyeY,
                        eyeRadius * 0.6f
                    )
                }
                BombMoodManager.BombMood.NEUTRAL -> {
                    // Yeux mi-clos
                    val halfEyeHeight = eyeRadius * 0.3f
                    canvas.drawRect(
                        centerX - eyeSpacing - eyeRadius * 0.8f,
                        eyeY - halfEyeHeight,
                        centerX - eyeSpacing + eyeRadius * 0.8f,
                        eyeY + halfEyeHeight,
                        Paint().apply { color = Color.BLACK }
                    )
                    canvas.drawRect(
                        centerX + eyeSpacing - eyeRadius * 0.8f,
                        eyeY - halfEyeHeight,
                        centerX + eyeSpacing + eyeRadius * 0.8f,
                        eyeY + halfEyeHeight,
                        Paint().apply { color = Color.BLACK }
                    )
                    return
                }
                BombMoodManager.BombMood.SAD -> {
                    // Yeux tristes
                    Quadruple(
                        centerX - eyeSpacing,
                        eyeY + eyeRadius * 0.3f,
                        centerX + eyeSpacing,
                        eyeY + eyeRadius * 0.3f,
                        eyeRadius * 0.5f
                    )
                }
                BombMoodManager.BombMood.VERY_SAD -> {
                    // Yeux très tristes
                    Quadruple(
                        centerX - eyeSpacing,
                        eyeY + eyeRadius * 0.5f,
                        centerX + eyeSpacing,
                        eyeY + eyeRadius * 0.5f,
                        eyeRadius * 0.4f
                    )
                }
            }

            canvas.drawCircle(leftPupilX, leftPupilY, pupilSize, pupilPaint)
            canvas.drawCircle(rightPupilX, rightPupilY, pupilSize, pupilPaint)
        }

        private fun drawMouth(canvas: Canvas, centerX: Float, centerY: Float, radius: Float) {
            val mouthY = centerY + radius * 0.3f
            val mouthWidth = radius * 0.6f

            when (bombMood) {
                BombMoodManager.BombMood.SUPER_HAPPY -> {
                    // Grand sourire
                    val mouthPath = Path()
                    mouthPath.addArc(
                        centerX - mouthWidth / 2,
                        mouthY - mouthWidth / 4,
                        centerX + mouthWidth / 2,
                        mouthY + mouthWidth / 2,
                        0f,
                        180f
                    )
                    canvas.drawPath(mouthPath, mouthPaint)
                }
                BombMoodManager.BombMood.HAPPY -> {
                    // Sourire normal
                    val mouthPath = Path()
                    mouthPath.addArc(
                        centerX - mouthWidth / 2,
                        mouthY - mouthWidth / 6,
                        centerX + mouthWidth / 2,
                        mouthY + mouthWidth / 3,
                        0f,
                        180f
                    )
                    canvas.drawPath(mouthPath, mouthPaint)
                }
                BombMoodManager.BombMood.NORMAL -> {
                    // Bouche neutre légèrement souriante
                    canvas.drawLine(
                        centerX - mouthWidth / 2,
                        mouthY,
                        centerX + mouthWidth / 2,
                        mouthY,
                        mouthPaint
                    )
                }
                BombMoodManager.BombMood.NEUTRAL -> {
                    // Bouche droite
                    canvas.drawLine(
                        centerX - mouthWidth / 3,
                        mouthY,
                        centerX + mouthWidth / 3,
                        mouthY,
                        mouthPaint
                    )
                }
                BombMoodManager.BombMood.SAD -> {
                    // Bouche triste
                    val mouthPath = Path()
                    mouthPath.addArc(
                        centerX - mouthWidth / 2,
                        mouthY,
                        centerX + mouthWidth / 2,
                        mouthY + mouthWidth / 2,
                        180f,
                        180f
                    )
                    canvas.drawPath(mouthPath, mouthPaint)
                }
                BombMoodManager.BombMood.VERY_SAD -> {
                    // Bouche très triste
                    val mouthPath = Path()
                    mouthPath.addArc(
                        centerX - mouthWidth / 2,
                        mouthY + mouthWidth / 4,
                        centerX + mouthWidth / 2,
                        mouthY + mouthWidth,
                        180f,
                        180f
                    )
                    canvas.drawPath(mouthPath, mouthPaint)
                }
            }
        }

        private fun drawSparkles(canvas: Canvas, centerX: Float, centerY: Float, radius: Float) {
            // Dessiner des étincelles autour de la bombe
            val sparkleRadius = radius * 1.5f
            for (i in 0..7) {
                val angle = i * 45f
                val x = centerX + sparkleRadius * cos(Math.toRadians(angle.toDouble())).toFloat()
                val y = centerY + sparkleRadius * sin(Math.toRadians(angle.toDouble())).toFloat()
                canvas.drawCircle(x, y, radius * 0.08f, sparklePaint)
            }
        }

        // Classe helper pour retourner 4 valeurs
        private data class Quadruple(val first: Float, val second: Float, val third: Float, val fourth: Float, val fifth: Float)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialisation
        aiAssistant = AIGameAssistant()
        advancedAnalytics = AdvancedAnalytics()
        futuristicAnimations = FuturisticAnimations()
        saveManager = SaveManager(this)
        achievementManager = AchievementManager()
        bombMoodManager = BombMoodManager(this)

        // Layout principal
        mainLayout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT
            )
            background = createGradientBackground()
        }

        soundManager = SoundManager(this)
        vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vibratorManager = getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
            vibratorManager?.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
        }

        if (saveManager.hasSavedGame()) {
            showResumeGameDialog()
        } else {
            showHomeScreen()
        }

        setContentView(mainLayout)
        hideSystemUi()
    }

    private fun showHomeScreen() {
        mainLayout.removeAllViews()

        // Mettre à jour l'utilisation pour le système d'humeur
        bombMoodManager.updateUsage()

        val homeLayout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(40, 60, 40, 40)
            gravity = android.view.Gravity.CENTER
            background = createGradientBackground()
        }

        // En-tête avec design amélioré
        val headerLayout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(0, 0, 0, 40)
            }
            gravity = android.view.Gravity.CENTER
            setPadding(30, 40, 30, 40)
            background = createModernCardBackground()
        }

        // Bombe émotionnelle
        bombImageView = BombImageView(this).apply {
            layoutParams = LinearLayout.LayoutParams(150, 150)
            setBombMood(bombMoodManager.getBombMood())
        }

        val titleText = TextView(this).apply {
            text = "💣 DÉMINEUR ÉMOTIONNEL 💣"
            textSize = 24f
            setTypeface(Typeface.create("sans-serif-black", Typeface.BOLD))
            setTextColor(Color.WHITE)
            textAlignment = TextView.TEXT_ALIGNMENT_CENTER
            setPadding(0, 20, 0, 10)
        }

        // Indicateur d'humeur et série
        val currentMood = bombMoodManager.getBombMood()
        val moodInfoText = TextView(this).apply {
            text = getMoodMessage(currentMood)
            textSize = 14f
            setTextColor(getMoodColor(currentMood))
            textAlignment = TextView.TEXT_ALIGNMENT_CENTER
            setPadding(0, 0, 0, 5)
        }

        val streakInfoText = TextView(this).apply {
            text = bombMoodManager.getStreakInfo()
            textSize = 12f
            setTextColor(Color.parseColor("#95a5a6"))
            textAlignment = TextView.TEXT_ALIGNMENT_CENTER
            setTypeface(null, Typeface.ITALIC)
        }

        // Statistiques joueur
        val stats = scoreManager.getPlayerStats()
        val unlockedAchievements = achievementManager.getUnlockedCount()
        val statsBadge = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(0, 20, 0, 0)
            }
            setPadding(25, 15, 25, 15)
            background = createRoundedRectBackground(Color.parseColor("#34495e"), 25f)
            gravity = android.view.Gravity.CENTER
        }

        val statsText = TextView(this).apply {
            text = "📊 ${stats.gamesPlayed} parties • ⭐ $unlockedAchievements succès • 🏆 ${(stats.winRate * 100).toInt()}% victoires"
            textSize = 12f
            setTextColor(Color.WHITE)
            setTypeface(null, Typeface.BOLD)
        }

        statsBadge.addView(statsText)
        headerLayout.addView(bombImageView)
        headerLayout.addView(titleText)
        headerLayout.addView(moodInfoText)
        headerLayout.addView(streakInfoText)
        headerLayout.addView(statsBadge)

        // Conteneur boutons amélioré
        val buttonsContainer = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            setPadding(25, 35, 25, 35)
            background = createModernCardBackground()
        }

        // Boutons avec design premium
        val playButton = createModernButton("🚀 JOUER MAINTENANT", Color.parseColor("#00b09b"), "🎮", 120)
        playButton.setOnClickListener {
            animateButtonClick(playButton)
            Handler(Looper.getMainLooper()).postDelayed({ showGameModeSelection() }, 200)
        }

        val modeButton = createModernButton("⚡ MODES DE JEU", Color.parseColor("#9B59B6"), "🎯", 100)
        modeButton.setOnClickListener {
            animateButtonClick(modeButton)
            Handler(Looper.getMainLooper()).postDelayed({ showGameModeSelection() }, 200)
        }

        val themeButton = createModernButton("🎨 THÈMES", Color.parseColor("#FF6B35"), "🌈", 100)
        themeButton.setOnClickListener {
            animateButtonClick(themeButton)
            Handler(Looper.getMainLooper()).postDelayed({ showThemeSelection() }, 200)
        }

        val statsButton = createModernButton("📈 STATISTIQUES", Color.parseColor("#3498db"), "⭐", 100)
        statsButton.setOnClickListener {
            animateButtonClick(statsButton)
            Handler(Looper.getMainLooper()).postDelayed({ showStatisticsScreen() }, 200)
        }

        val achievementButton = createModernButton("🏆 SUCCÈS", Color.parseColor("#FFD700"), "🎖️", 100)
        achievementButton.setTextColor(Color.BLACK)
        achievementButton.setOnClickListener {
            animateButtonClick(achievementButton)
            Handler(Looper.getMainLooper()).postDelayed({ showAchievementsScreen() }, 200)
        }

        val quitButton = createModernButton("🚪 QUITTER", Color.parseColor("#e74c3c"), "👋", 90)
        quitButton.setOnClickListener {
            animateButtonClick(quitButton)
            Handler(Looper.getMainLooper()).postDelayed({ finish() }, 200)
        }

        buttonsContainer.addView(playButton)
        buttonsContainer.addView(modeButton)
        buttonsContainer.addView(themeButton)
        buttonsContainer.addView(statsButton)
        buttonsContainer.addView(achievementButton)
        buttonsContainer.addView(quitButton)

        // Footer
        val footerLayout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(0, 40, 0, 0)
            }
            gravity = android.view.Gravity.CENTER
        }

        val quoteText = TextView(this).apply {
            text = "\"La stratégie surpasse la chance\""
            textSize = 12f
            setTextColor(Color.parseColor("#95a5a6"))
            textAlignment = TextView.TEXT_ALIGNMENT_CENTER
            setTypeface(null, Typeface.ITALIC)
        }

        footerLayout.addView(quoteText)

        homeLayout.addView(headerLayout)
        homeLayout.addView(buttonsContainer)
        homeLayout.addView(footerLayout)
        mainLayout.addView(homeLayout)
    }

    private fun getMoodMessage(mood: BombMoodManager.BombMood): String {
        return when (mood) {
            BombMoodManager.BombMood.SUPER_HAPPY -> "🔥 Super heureux! Série impressionnante!"
            BombMoodManager.BombMood.HAPPY -> "😊 Content de vous revoir!"
            BombMoodManager.BombMood.NORMAL -> "😐 Tout va bien!"
            BombMoodManager.BombMood.NEUTRAL -> "😑 Ça fait un moment..."
            BombMoodManager.BombMood.SAD -> "😔 Je m'ennuyais sans vous!"
            BombMoodManager.BombMood.VERY_SAD -> "😭 Vous m'avez oublié!"
        }
    }

    private fun getMoodColor(mood: BombMoodManager.BombMood): Int {
        return when (mood) {
            BombMoodManager.BombMood.SUPER_HAPPY -> Color.parseColor("#FFD700")
            BombMoodManager.BombMood.HAPPY -> Color.parseColor("#2ecc71")
            BombMoodManager.BombMood.NORMAL -> Color.parseColor("#3498db")
            BombMoodManager.BombMood.NEUTRAL -> Color.parseColor("#f39c12")
            BombMoodManager.BombMood.SAD -> Color.parseColor("#e67e22")
            BombMoodManager.BombMood.VERY_SAD -> Color.parseColor("#e74c3c")
        }
    }

    private fun showGameModeSelection() {
        mainLayout.removeAllViews()

        val modeLayout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(32, 32, 32, 32)
            gravity = android.view.Gravity.CENTER
            background = createGradientBackground()
        }

        val titleText = TextView(this).apply {
            text = "🎮 CHOISISSEZ UN MODE"
            textSize = 28f
            setTypeface(null, Typeface.BOLD)
            setTextColor(Color.WHITE)
            textAlignment = TextView.TEXT_ALIGNMENT_CENTER
            setShadowLayer(8f, 3f, 3f, Color.BLUE)
        }

        val modesContainer = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(25, 30, 25, 30)
            background = createModernCardBackground()
        }

        GameMode.values().forEach { mode ->
            val button = createModernButton(
                "${mode.displayName}\n${mode.description}",
                when (mode) {
                    GameMode.CLASSIC -> Color.parseColor("#3498db")
                    GameMode.TIMED -> Color.parseColor("#f39c12")
                    GameMode.BLITZ -> Color.parseColor("#e74c3c")
                    GameMode.SURVIVAL -> Color.parseColor("#9b59b6")
                },
                when (mode) {
                    GameMode.CLASSIC -> "🎯"
                    GameMode.TIMED -> "⏱️"
                    GameMode.BLITZ -> "⚡"
                    GameMode.SURVIVAL -> "🏃"
                },
                120
            ).apply {
                setOnClickListener {
                    gameMode = mode
                    showDifficultyScreen()
                }
            }
            modesContainer.addView(button)
        }

        val backButton = createModernButton("⬅️ RETOUR", Color.parseColor("#7f8c8d"), "🔙", 80)
        backButton.setOnClickListener { showHomeScreen() }

        modeLayout.addView(titleText)
        modeLayout.addView(modesContainer)
        modeLayout.addView(backButton)
        mainLayout.addView(modeLayout)
    }

    private fun showThemeSelection() {
        mainLayout.removeAllViews()

        val themeLayout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(32, 32, 32, 32)
            gravity = android.view.Gravity.CENTER
            background = createGradientBackground()
        }

        val titleText = TextView(this).apply {
            text = "🎨 THÈMES DISPONIBLES"
            textSize = 28f
            setTypeface(null, Typeface.BOLD)
            setTextColor(Color.WHITE)
            textAlignment = TextView.TEXT_ALIGNMENT_CENTER
        }

        val themesContainer = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(25, 30, 25, 30)
            background = createModernCardBackground()
        }

        Theme.values().forEach { theme ->
            val button = createModernButton(
                theme.displayName,
                theme.accentColor,
                if (theme == currentTheme) "✅" else "🎨",
                100
            ).apply {
                setOnClickListener {
                    currentTheme = theme
                    showHomeScreen()
                }
            }
            themesContainer.addView(button)
        }

        val backButton = createModernButton("⬅️ RETOUR", Color.parseColor("#7f8c8d"), "🔙", 80)
        backButton.setOnClickListener { showHomeScreen() }

        themeLayout.addView(titleText)
        themeLayout.addView(themesContainer)
        themeLayout.addView(backButton)
        mainLayout.addView(themeLayout)
    }

    private fun showAchievementsScreen() {
        mainLayout.removeAllViews()

        val achievementsLayout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(32, 32, 32, 32)
            gravity = android.view.Gravity.CENTER
            background = createGradientBackground()
        }

        val titleText = TextView(this).apply {
            text = "🏆 MES SUCCÈS"
            textSize = 28f
            setTypeface(null, Typeface.BOLD)
            setTextColor(Color.WHITE)
            textAlignment = TextView.TEXT_ALIGNMENT_CENTER
        }

        val achievementsContainer = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(25, 30, 25, 30)
            background = createModernCardBackground()
        }

        achievementManager.getAchievements().forEach { achievement ->
            val achievementLayout = LinearLayout(this).apply {
                orientation = LinearLayout.HORIZONTAL
                setPadding(20, 15, 20, 15)
                background = createRoundedRectBackground(
                    if (achievement.isUnlocked) Color.parseColor("#27ae60") else Color.parseColor("#34495e"),
                    15f
                )
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply {
                    setMargins(0, 0, 0, 10)
                }
            }

            val iconText = TextView(this).apply {
                text = achievement.icon
                textSize = 24f
                setPadding(0, 0, 15, 0)
            }

            val textLayout = LinearLayout(this).apply {
                orientation = LinearLayout.VERTICAL
                layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
            }

            val nameText = TextView(this).apply {
                text = achievement.name
                textSize = 16f
                setTypeface(null, Typeface.BOLD)
                setTextColor(if (achievement.isUnlocked) Color.WHITE else Color.LTGRAY)
            }

            val descText = TextView(this).apply {
                text = achievement.description
                textSize = 12f
                setTextColor(if (achievement.isUnlocked) Color.parseColor("#CCCCCC") else Color.DKGRAY)
            }

            val progressText = TextView(this).apply {
                text = if (achievement.isUnlocked) "✅ Débloqué"
                else "Progression: ${achievement.progress}/${achievement.target}"
                textSize = 10f
                setTextColor(if (achievement.isUnlocked) Color.parseColor("#FFD700") else Color.GRAY)
            }

            textLayout.addView(nameText)
            textLayout.addView(descText)
            textLayout.addView(progressText)
            achievementLayout.addView(iconText)
            achievementLayout.addView(textLayout)
            achievementsContainer.addView(achievementLayout)
        }

        val backButton = createModernButton("⬅️ RETOUR", Color.parseColor("#7f8c8d"), "🔙", 80)
        backButton.setOnClickListener { showHomeScreen() }

        achievementsLayout.addView(titleText)
        achievementsLayout.addView(achievementsContainer)
        achievementsLayout.addView(backButton)
        mainLayout.addView(achievementsLayout)
    }

    private fun showDifficultyScreen() {
        mainLayout.removeAllViews()

        val difficultyLayout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(32, 32, 32, 32)
            gravity = android.view.Gravity.CENTER
            background = createGradientBackground()
        }

        val titleText = TextView(this).apply {
            text = "🎯 NIVEAU DE DIFFICULTÉ"
            textSize = 28f
            setTypeface(null, Typeface.BOLD)
            setTextColor(Color.WHITE)
            textAlignment = TextView.TEXT_ALIGNMENT_CENTER
        }

        val difficultyContainer = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(25, 30, 25, 30)
            background = createModernCardBackground()
        }

        val easyButton = createModernButton("FACILE\n9x9 • 10 mines • Débutant", Color.parseColor("#27ae60"), "😊", 120)
        easyButton.setOnClickListener {
            animateButtonClick(easyButton)
            Handler(Looper.getMainLooper()).postDelayed({
                selectedDifficulty = Difficulty.EASY
                showGameScreen()
            }, 200)
        }

        val mediumButton = createModernButton("MOYEN\n16x16 • 40 mines • Expert", Color.parseColor("#f39c12"), "😐", 120)
        mediumButton.setOnClickListener {
            animateButtonClick(mediumButton)
            Handler(Looper.getMainLooper()).postDelayed({
                selectedDifficulty = Difficulty.MEDIUM
                showGameScreen()
            }, 200)
        }

        val hardButton = createModernButton("DIFFICILE\n16x30 • 99 mines • Maître", Color.parseColor("#e74c3c"), "😰", 120)
        hardButton.setOnClickListener {
            animateButtonClick(hardButton)
            Handler(Looper.getMainLooper()).postDelayed({
                selectedDifficulty = Difficulty.HARD
                showGameScreen()
            }, 200)
        }

        difficultyContainer.addView(easyButton)
        difficultyContainer.addView(mediumButton)
        difficultyContainer.addView(hardButton)

        val backButton = createModernButton("⬅️ RETOUR", Color.parseColor("#7f8c8d"), "🔙", 80)
        backButton.setOnClickListener { showHomeScreen() }

        difficultyLayout.addView(titleText)
        difficultyLayout.addView(difficultyContainer)
        difficultyLayout.addView(backButton)
        mainLayout.addView(difficultyLayout)
    }

    private fun showGameScreen() {
        mainLayout.removeAllViews()
        markedBombsCount = 0 // Réinitialiser le compteur

        val gameLayout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            background = createGradientBackground()
        }

        // Header amélioré avec statistiques en temps réel
        val headerLayout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            background = createModernCardBackground()
            setPadding(20, 15, 20, 15)
        }

        // Première ligne : informations principales
        val topInfoLayout = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
        }

        val modeInfoText = TextView(this).apply {
            text = "🎯 ${gameMode.displayName}"
            textSize = 14f
            setTextColor(Color.WHITE)
            setTypeface(null, Typeface.BOLD)
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
        }

        val difficultyInfoText = TextView(this).apply {
            text = "⚡ ${selectedDifficulty.displayName}"
            textSize = 14f
            setTextColor(Color.WHITE)
            setTypeface(null, Typeface.BOLD)
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
        }

        topInfoLayout.addView(modeInfoText)
        topInfoLayout.addView(difficultyInfoText)

        // Deuxième ligne : compteurs
        val bottomInfoLayout = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(0, 10, 0, 0)
            }
        }

        val minesCounterText = TextView(this).apply {
            text = "💣 ${selectedDifficulty.mines}"
            textSize = 16f
            setTypeface(null, Typeface.BOLD)
            setTextColor(Color.WHITE)
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
        }

        val progressText = TextView(this).apply {
            text = "📊 0%"
            textSize = 16f
            setTextColor(Color.WHITE)
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
        }

        val timerText = TextView(this).apply {
            text = "⏱ 00:00"
            textSize = 16f
            setTextColor(Color.WHITE)
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
        }

        bottomInfoLayout.addView(minesCounterText)
        bottomInfoLayout.addView(progressText)
        bottomInfoLayout.addView(timerText)

        headerLayout.addView(topInfoLayout)
        headerLayout.addView(bottomInfoLayout)

        // Contrôles de jeu
        val controlLayout = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = android.view.Gravity.CENTER
            setPadding(15, 10, 15, 10)
            background = createModernCardBackground()
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(0, 5, 0, 5)
            }
        }

        // Déclarer minesweeperView avant de l'utiliser dans les callbacks
        val minesweeperView = MinesweeperView(this, selectedDifficulty).apply {
            setGameMode(gameMode)
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                0
            ).apply {
                weight = 1f
            }
        }

        // Bouton marquage des bombes
        val bombMarkingButton = createModernButton(
            if (isBombMarkingMode) "🎯 MARQUAGE ACTIVÉ ($markedBombsCount)" else "📝 MARQUER BOMBES",
            if (isBombMarkingMode) Color.parseColor("#FF6B35") else Color.parseColor("#9B59B6"),
            if (isBombMarkingMode) "🎯" else "📝",
            70
        )

        // Indicateur de mode
        val modeIndicator = TextView(this).apply {
            text = if (isBombMarkingMode)
                "📝 Mode marquage: $markedBombsCount bombes suspectées (${selectedDifficulty.mines - markedBombsCount} restantes)"
            else
                "🎮 Mode jeu normal: Clic pour révéler, appui long pour drapeau"
            textSize = 11f
            setTextColor(if (isBombMarkingMode) Color.parseColor("#FFD700") else Color.parseColor("#95a5a6"))
            textAlignment = TextView.TEXT_ALIGNMENT_CENTER
            setPadding(20, 5, 20, 5)
            background = createRoundedRectBackground(Color.parseColor("#2c3e50"), 10f)
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(15, 0, 15, 5)
            }
        }

        // Configurer le bouton de marquage
        bombMarkingButton.setOnClickListener {
            isBombMarkingMode = !isBombMarkingMode
            if (!isBombMarkingMode) {
                markedBombsCount = 0
            }

            bombMarkingButton.text = if (isBombMarkingMode) "🎯 MARQUAGE ACTIVÉ ($markedBombsCount)" else "📝 MARQUER BOMBES"
            bombMarkingButton.background = createRoundedRectBackground(
                if (isBombMarkingMode) Color.parseColor("#FF6B35") else Color.parseColor("#9B59B6"),
                15f
            )

            // Mettre à jour le compteur
            val remainingMines = selectedDifficulty.mines - markedBombsCount
            minesCounterText.text = "💣 $remainingMines"

            // Mettre à jour l'indicateur
            modeIndicator.text = if (isBombMarkingMode)
                "📝 Mode marquage: $markedBombsCount bombes suspectées (${selectedDifficulty.mines - markedBombsCount} restantes)"
            else
                "🎮 Mode jeu normal: Clic pour révéler, appui long pour drapeau"

            // Transmettre le mode à la vue de jeu
            minesweeperView.setBombMarkingMode(isBombMarkingMode)
        }

        // Bouton terminer
        val finishButton = createModernButton("🏁 TERMINER", Color.parseColor("#e74c3c"), "🏁", 70)
        finishButton.setOnClickListener {
            showFinishGameConfirmation()
        }

        controlLayout.addView(bombMarkingButton)
        controlLayout.addView(finishButton)

        // Configurer les callbacks après avoir déclaré toutes les variables
        // Configurer les callbacks après avoir déclaré toutes les variables
        minesweeperView.setGameCallbacks(object : MinesweeperView.GameCallbacks {
            override fun onGameStart() {
                startTimer(timerText)
                advancedAnalytics.trackGameSession(GameSession(0, 0, 0.0, selectedDifficulty))
            }

            override fun onGameWin(progress: Double, cellsRevealed: Int, totalSafeCells: Int, time: Int) {
                gameResult = GameResult(true, selectedDifficulty, progress, cellsRevealed, totalSafeCells, time)
                scoreManager.addScore(Score("Joueur", selectedDifficulty, time, true))
                achievementManager.updateProgress("first_win")
                achievementManager.updateProgress("expert")
                if (time < 60) achievementManager.updateProgress("speedrunner")
                soundManager?.playSound("win")
                vibrate(200)
                Handler(Looper.getMainLooper()).postDelayed({ showGameOverScreen() }, 1500) // ← À SUPPRIMER
            }

            override fun onGameOver(progress: Double, cellsRevealed: Int, totalSafeCells: Int, time: Int) {
                // Enregistrer le résultat
                gameResult = GameResult(false, selectedDifficulty, progress, cellsRevealed, totalSafeCells, time)

                // Jouer le son
                soundManager?.playSound("lose")

                // Vibrer de façon sécurisée
                vibrate(500)

                // Afficher l'écran de fin après un délai
                Handler(Looper.getMainLooper()).postDelayed({
                    showGameOverScreen()
                }, 1500) // ← À SUPPRIMER
            }

            // ... le reste des callbacks ...


            override fun onMinesCounterUpdate(minesLeft: Int) {
                minesCounterText.text = "💣 $minesLeft"
            }

            override fun onProgressUpdate(progress: Double) {
                progressText.text = "📊 ${(progress * 100).toInt()}%"
                achievementManager.updateProgress("mine_sweeper", (progress * 10).toInt())
            }

            override fun onCellRevealed() {
                soundManager?.playSound("reveal")
                vibrate(50)
            }

            override fun onFlagToggled() {
                soundManager?.playSound("flag")
                vibrate(100)
            }

            override fun onBombMarked(count: Int) {
                markedBombsCount = count
                // Mettre à jour le compteur de mines restantes
                val remainingMines = selectedDifficulty.mines - markedBombsCount
                minesCounterText.text = "💣 $remainingMines"

                // Mettre à jour le bouton
                bombMarkingButton.text = if (isBombMarkingMode) "🎯 MARQUAGE ACTIVÉ ($markedBombsCount)" else "📝 MARQUER BOMBES"

                // Mettre à jour l'indicateur
                modeIndicator.text = if (isBombMarkingMode)
                    "📝 Mode marquage: $markedBombsCount bombes suspectées (${selectedDifficulty.mines - markedBombsCount} restantes)"
                else
                    "🎮 Mode jeu normal: Clic pour révéler, appui long pour drapeau"
            }
        })

        gameLayout.addView(headerLayout)
        gameLayout.addView(controlLayout)
        gameLayout.addView(modeIndicator)
        gameLayout.addView(minesweeperView)
        mainLayout.addView(gameLayout)
    }
    private fun showFinishGameConfirmation() {
        AlertDialog.Builder(this)
            .setTitle("🏁 Terminer la partie")
            .setMessage("Voulez-vous vraiment abandonner cette partie ?\n\nVotre progression sera perdue.")
            .setPositiveButton("✅ OUI, ABANDONNER") { dialog, which ->
                val time = ((System.currentTimeMillis() - startTime) / 1000).toInt()
                gameResult = GameResult(false, selectedDifficulty, 0.0, 0, 0, time)
                showGameOverScreen()
            }
            .setNegativeButton("❌ CONTINUER", null)
            .setCancelable(true)
            .show()
    }

    private fun showStatisticsScreen() {
        mainLayout.removeAllViews()

        val stats = scoreManager.getPlayerStats()

        val statsLayout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(32, 32, 32, 32)
            gravity = android.view.Gravity.CENTER
            background = createGradientBackground()
        }

        val titleText = TextView(this).apply {
            text = "📈 MES STATISTIQUES"
            textSize = 28f
            setTypeface(null, Typeface.BOLD)
            setTextColor(Color.WHITE)
            textAlignment = TextView.TEXT_ALIGNMENT_CENTER
        }

        val statsCard = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(30, 30, 30, 30)
            background = createModernCardBackground()
        }

        val statsData = listOf(
            "🎯 Parties jouées" to stats.gamesPlayed.toString(),
            "🏆 Parties gagnées" to stats.gamesWon.toString(),
            "📊 Taux de victoire" to "${(stats.winRate * 100).toInt()}%",
            "⚡ Meilleur temps" to if (stats.bestTime > 0) "${stats.bestTime}s" else "-",
            "⏱️ Temps moyen" to if (stats.averageTime > 0) "${stats.averageTime.toInt()}s" else "-"
        )

        statsData.forEach { (label, value) ->
            val statRow = LinearLayout(this).apply {
                orientation = LinearLayout.HORIZONTAL
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply {
                    setMargins(0, 0, 0, 15)
                }
            }

            val labelText = TextView(this).apply {
                text = label
                textSize = 16f
                setTextColor(Color.WHITE)
                layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
            }

            val valueText = TextView(this).apply {
                text = value
                textSize = 16f
                setTypeface(null, Typeface.BOLD)
                setTextColor(Color.parseColor("#FFD700"))
                textAlignment = TextView.TEXT_ALIGNMENT_TEXT_END
            }

            statRow.addView(labelText)
            statRow.addView(valueText)
            statsCard.addView(statRow)
        }

        val backButton = createModernButton("⬅️ RETOUR", Color.parseColor("#7f8c8d"), "🏠", 90)
        backButton.setOnClickListener { showHomeScreen() }

        statsLayout.addView(titleText)
        statsLayout.addView(statsCard)
        statsLayout.addView(backButton)
        mainLayout.addView(statsLayout)
    }

    private fun showGameOverScreen() {
        stopTimer()
        mainLayout.removeAllViews()

        val result = gameResult ?: return

        val gameOverLayout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(32, 32, 32, 32)
            gravity = android.view.Gravity.CENTER
            background = if (result.isWin) createGradientBackground(Color.parseColor("#27ae60"), Color.parseColor("#2ecc71"))
            else createGradientBackground(Color.parseColor("#c0392b"), Color.parseColor("#e74c3c"))
        }

        val resultIcon = TextView(this).apply {
            text = if (result.isWin) "🎉" else "💥"
            textSize = 80f
            textAlignment = TextView.TEXT_ALIGNMENT_CENTER
        }

        ObjectAnimator.ofPropertyValuesHolder(
            resultIcon,
            PropertyValuesHolder.ofFloat("scaleX", 0f, 1.2f, 1f),
            PropertyValuesHolder.ofFloat("scaleY", 0f, 1.2f, 1f)
        ).apply {
            duration = 800
            interpolator = OvershootInterpolator()
            start()
        }

        val resultText = TextView(this).apply {
            text = if (result.isWin) "FÉLICITATIONS !" else "PARTIE TERMINÉE"
            textSize = 32f
            setTypeface(null, Typeface.BOLD)
            setTextColor(Color.WHITE)
            textAlignment = TextView.TEXT_ALIGNMENT_CENTER
            setPadding(0, 20, 0, 10)
        }

        val messageText = TextView(this).apply {
            text = if (result.isWin)
                "Vous avez déminé avec succès !\nTemps: ${result.time}s"
            else
                "Une bombe a explosé !\nProgression: ${(result.progress * 100).toInt()}%"
            textSize = 16f
            setTextColor(Color.WHITE)
            textAlignment = TextView.TEXT_ALIGNMENT_CENTER
            setPadding(0, 10, 0, 30)
        }

        val statsCard = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            background = createModernCardBackground()
            setPadding(25, 25, 25, 25)
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(0, 20, 0, 30)
            }
        }

        val timeText = TextView(this).apply {
            text = "⏱️ Temps: ${result.time} secondes"
            textSize = 16f
            setTextColor(Color.WHITE)
            setPadding(0, 8, 0, 8)
        }

        val progressText = TextView(this).apply {
            text = "📊 Progression: ${(result.progress * 100).toInt()}%"
            textSize = 16f
            setTextColor(Color.WHITE)
            setPadding(0, 8, 0, 8)
        }

        val cellsText = TextView(this).apply {
            text = "🔍 Cases révélées: ${result.cellsRevealed}/${result.totalSafeCells}"
            textSize = 16f
            setTextColor(Color.WHITE)
            setPadding(0, 8, 0, 8)
        }

        val difficultyText = TextView(this).apply {
            text = "⚡ Difficulté: ${result.difficulty.displayName}"
            textSize = 16f
            setTextColor(Color.WHITE)
            setPadding(0, 8, 0, 8)
        }

        statsCard.addView(timeText)
        statsCard.addView(progressText)
        statsCard.addView(cellsText)
        statsCard.addView(difficultyText)

        val buttonsLayout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
        }

        val replayButton = createModernButton(
            if (result.isWin) "🔄 REJOUER" else "🔄 NOUVELLE PARTIE",
            Color.parseColor("#3498db"),
            "🎮",
            100
        ).apply {
            setOnClickListener {
                animateButtonClick(this)
                Handler(Looper.getMainLooper()).postDelayed({
                    showGameScreen()
                }, 200)
            }
        }

        val homeButton = createModernButton("🏠 ACCUEIL", Color.parseColor("#2ecc71"), "🏠", 100)
        homeButton.setOnClickListener {
            animateButtonClick(homeButton)
            Handler(Looper.getMainLooper()).postDelayed({
                showHomeScreen()
            }, 200)
        }

        // Message d'instruction
        val instructionText = TextView(this).apply {
            text = "Choisissez une action pour continuer"
            textSize = 12f
            setTextColor(Color.parseColor("#CCCCCC"))
            textAlignment = TextView.TEXT_ALIGNMENT_CENTER
            setTypeface(null, Typeface.ITALIC)
            setPadding(0, 20, 0, 10)
        }

        buttonsLayout.addView(replayButton)
        buttonsLayout.addView(homeButton)

        gameOverLayout.addView(resultIcon)
        gameOverLayout.addView(resultText)
        gameOverLayout.addView(messageText)
        gameOverLayout.addView(statsCard)
        gameOverLayout.addView(instructionText)
        gameOverLayout.addView(buttonsLayout)

        mainLayout.addView(gameOverLayout)

        if (result.isWin) {
            soundManager?.playSound("celebration")
        } else {
            soundManager?.playSound("game_over")
        }
    }
    private fun createGradientBackground(): Drawable {
        val colors = intArrayOf(
            currentTheme.primaryColor,
            currentTheme.secondaryColor,
            currentTheme.accentColor
        )
        val gradient = LinearGradient(0f, 0f, 0f, 1000f, colors, null, Shader.TileMode.CLAMP)
        val paint = Paint().apply { shader = gradient }
        return object : Drawable() {
            override fun draw(canvas: Canvas) = canvas.drawRect(bounds, paint)
            override fun setAlpha(alpha: Int) {}
            override fun setColorFilter(colorFilter: ColorFilter?) {}
            @Deprecated("Deprecated in Java") override fun getOpacity() = PixelFormat.OPAQUE
        }
    }

    private fun createGradientBackground(startColor: Int, endColor: Int): Drawable {
        val gradient = LinearGradient(0f, 0f, 0f, 1000f, intArrayOf(startColor, endColor), null, Shader.TileMode.CLAMP)
        val paint = Paint().apply { shader = gradient }
        return object : Drawable() {
            override fun draw(canvas: Canvas) = canvas.drawRect(bounds, paint)
            override fun setAlpha(alpha: Int) {}
            override fun setColorFilter(colorFilter: ColorFilter?) {}
            @Deprecated("Deprecated in Java") override fun getOpacity() = PixelFormat.OPAQUE
        }
    }

    private fun createModernCardBackground(): Drawable {
        return GradientDrawable().apply {
            shape = GradientDrawable.RECTANGLE
            cornerRadius = 25f
            setColor(Color.parseColor("#2c3e50"))
            setStroke(2, Color.parseColor("#34495e"))
        }
    }

    private fun createRoundedRectBackground(color: Int, cornerRadius: Float): Drawable {
        return GradientDrawable().apply {
            shape = GradientDrawable.RECTANGLE
            this.cornerRadius = cornerRadius
            setColor(color)
        }
    }

    private fun createModernButton(text: String, backgroundColor: Int, icon: String, height: Int): Button {
        return Button(this).apply {
            this.text = "$icon $text"
            textSize = 14f
            setTypeface(Typeface.create("sans-serif", Typeface.BOLD))
            setTextColor(Color.WHITE)
            setAllCaps(false)
            background = createRoundedRectBackground(backgroundColor, 20f)
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                this.height = dpToPx(height)
                setMargins(0, 0, 0, 10)
            }
            setOnTouchListener { v, event ->
                when (event.action) {
                    MotionEvent.ACTION_DOWN -> {
                        v.alpha = 0.7f
                        v.scaleX = 0.95f
                        v.scaleY = 0.95f
                    }
                    MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                        v.alpha = 1f
                        v.scaleX = 1f
                        v.scaleY = 1f
                    }
                }
                false
            }
        }
    }

    private fun animateButtonClick(button: Button) {
        ObjectAnimator.ofPropertyValuesHolder(
            button,
            PropertyValuesHolder.ofFloat("scaleX", 0.9f, 1f),
            PropertyValuesHolder.ofFloat("scaleY", 0.9f, 1f)
        ).apply {
            duration = 200
            start()
        }
        vibrate(50)
    }

    private val timerHandler = Handler(Looper.getMainLooper())
    private lateinit var timerRunnable: Runnable

    private fun startTimer(timerText: TextView) {
        startTime = System.currentTimeMillis()
        timerRunnable = object : Runnable {
            override fun run() {
                val currentTime = ((System.currentTimeMillis() - startTime) / 1000).toInt()
                val minutes = currentTime / 60
                val seconds = currentTime % 60
                timerText.text = "⏱ ${String.format("%02d:%02d", minutes, seconds)}"
                timerHandler.postDelayed(this, 1000)
            }
        }
        timerHandler.postDelayed(timerRunnable, 1000)
    }

    private fun stopTimer() {
        timerHandler.removeCallbacks(timerRunnable)
    }

    @RequiresPermission(Manifest.permission.VIBRATE)
    fun vibrate(duration: Long) {
        if (vibrator?.hasVibrator() == true) {
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    vibrator?.vibrate(VibrationEffect.createOneShot(duration, VibrationEffect.DEFAULT_AMPLITUDE))
                } else {
                    @Suppress("DEPRECATION")
                    vibrator?.vibrate(duration)
                }
            } catch (e: Exception) {}
        }
    }

    private fun showResumeGameDialog() {
        AlertDialog.Builder(this)
            .setTitle("💾 Partie sauvegardée")
            .setMessage("Voulez-vous reprendre votre partie précédente ?")
            .setPositiveButton("🔄 REPRENDRE") { _, _ -> showGameScreen() }
            .setNegativeButton("🎮 NOUVELLE PARTIE") { _, _ ->
                saveManager.deleteSave()
                showHomeScreen()
            }
            .show()
    }

    private fun showAIAssistantDialog() {
        val hint = aiAssistant.getDifficultyHint()
        AlertDialog.Builder(this)
            .setTitle("🤖 ASSISTANT IA")
            .setMessage("$hint\n\n💡 Conseil: Utilisez le mode marquage pour noter les bombes suspectées !")
            .setPositiveButton("👌 COMPRIS", null)
            .show()
    }

    private fun hideSystemUi() {
        // ✅ CODE COMPATIBLE API 26
        @Suppress("DEPRECATION")
        window.decorView.systemUiVisibility = (
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        or View.SYSTEM_UI_FLAG_FULLSCREEN
                        or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                )
    }

    private fun dpToPx(dp: Int): Int = (dp * resources.displayMetrics.density).toInt()
}

class Cell {
    var isMine: Boolean = false
    var isRevealed: Boolean = false
    var isFlagged: Boolean = false
    var adjacentMines: Int = 0
}

class MinesweeperView(context: Context, private val difficulty: MainActivity.Difficulty) : View(context) {

    private val rows = difficulty.rows
    private val cols = difficulty.cols
    private val totalMines = difficulty.mines
    private val totalCells = rows * cols
    private val totalSafeCells = totalCells - totalMines

    private var cellSize = 0f
    private lateinit var grid: Array<Array<Cell>>
    private var gameOver = false
    private var gameWon = false
    private var firstClick = true
    private var flagsPlaced = 0
    private var cellsRevealed = 0
    private var unrevealedSafeCells = totalSafeCells
    private var gameCallbacks: GameCallbacks? = null

    private var bombMarkingMode = false
    private val markedBombCells = mutableSetOf<Pair<Int, Int>>()

    // Variables pour détection de clic long
    private var longPressDetected = false
    private val longPressTimeout = 500L
    private var lastTouchTime = 0L
    private var touchX = 0f
    private var touchY = 0f

    private val bgColor = Color.parseColor("#1a1a2e")
    private val hiddenColor = Color.parseColor("#34495e")
    private val revealedColor = Color.parseColor("#ecf0f1")
    private val mineColor = Color.parseColor("#e74c3c")
    private val flagColor = Color.parseColor("#2ecc71")
    private val markedColor = Color.parseColor("#FFA500")
    private val borderColor = Color.parseColor("#2c3e50")

    private val cellPaint = Paint().apply {
        style = Paint.Style.FILL
        isAntiAlias = true
    }

    private val textPaint = Paint().apply {
        color = Color.BLACK
        textAlign = Paint.Align.CENTER
        typeface = Typeface.DEFAULT_BOLD
        isAntiAlias = true
    }

    private val borderPaint = Paint().apply {
        color = borderColor
        style = Paint.Style.STROKE
        strokeWidth = 1f
        isAntiAlias = true
    }

    private val markPaint = Paint().apply {
        color = Color.WHITE
        textAlign = Paint.Align.CENTER
        typeface = Typeface.DEFAULT_BOLD
        isAntiAlias = true
    }

    interface GameCallbacks {
        fun onGameStart()
        fun onGameWin(progress: Double, cellsRevealed: Int, totalSafeCells: Int, time: Int)
        fun onGameOver(progress: Double, cellsRevealed: Int, totalSafeCells: Int, time: Int)
        fun onMinesCounterUpdate(minesLeft: Int)
        fun onProgressUpdate(progress: Double)
        fun onCellRevealed()
        fun onFlagToggled()
        fun onBombMarked(count: Int)
    }

    fun setGameCallbacks(callbacks: GameCallbacks) {
        this.gameCallbacks = callbacks
    }

    fun setGameMode(mode: MainActivity.GameMode) {
        // Implémentation pour les différents modes
    }

    fun setBombMarkingMode(enabled: Boolean) {
        bombMarkingMode = enabled
        invalidate()
    }

    init {
        initializeGame()
    }

    private fun initializeGame() {
        grid = Array(rows) { Array(cols) { Cell() } }
        firstClick = true
        gameOver = false
        gameWon = false
        flagsPlaced = 0
        cellsRevealed = 0
        unrevealedSafeCells = totalSafeCells
        markedBombCells.clear()
        bombMarkingMode = false
        gameCallbacks?.onMinesCounterUpdate(totalMines - flagsPlaced)
        gameCallbacks?.onProgressUpdate(0.0)
        gameCallbacks?.onBombMarked(0)
        invalidate()
    }

    private fun placeMines(avoidRow: Int, avoidCol: Int) {
        var minesPlaced = 0
        val random = Random()

        while (minesPlaced < totalMines) {
            val row = random.nextInt(rows)
            val col = random.nextInt(cols)
            if (!grid[row][col].isMine && (row != avoidRow || col != avoidCol)) {
                grid[row][col].isMine = true
                minesPlaced++
            }
        }
        calculateAdjacentMines()
    }

    private fun calculateAdjacentMines() {
        for (i in 0 until rows) {
            for (j in 0 until cols) {
                if (!grid[i][j].isMine) {
                    grid[i][j].adjacentMines = countAdjacentMines(i, j)
                }
            }
        }
    }

    private fun countAdjacentMines(row: Int, col: Int): Int {
        var count = 0
        for (i in -1..1) {
            for (j in -1..1) {
                val newRow = row + i
                val newCol = col + j
                if (newRow in 0 until rows && newCol in 0 until cols && grid[newRow][newCol].isMine) {
                    count++
                }
            }
        }
        return count
    }

    private fun calculateProgress(): Double {
        return if (totalSafeCells > 0) {
            (cellsRevealed.toDouble() / totalSafeCells.toDouble()).coerceIn(0.0, 1.0)
        } else {
            0.0
        }
    }

    private fun toggleBombMark(row: Int, col: Int) {
        val cellKey = Pair(row, col)
        val cell = grid[row][col]

        // Ne pas marquer les cases déjà révélées ou avec drapeau
        if (!cell.isRevealed && !cell.isFlagged) {
            if (markedBombCells.contains(cellKey)) {
                markedBombCells.remove(cellKey)
            } else {
                markedBombCells.add(cellKey)
            }
            gameCallbacks?.onBombMarked(markedBombCells.size)
            invalidate()

            // Vibration pour feedback
            (context as? MainActivity)?.vibrate(30)
        }
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        val availableWidth = w - paddingLeft - paddingRight
        val availableHeight = h - paddingTop - paddingBottom
        cellSize = min(availableWidth / cols.toFloat(), availableHeight / rows.toFloat())
        textPaint.textSize = cellSize * 0.4f
        markPaint.textSize = cellSize * 0.35f
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        canvas.drawColor(bgColor)

        for (i in 0 until rows) {
            for (j in 0 until cols) {
                drawCell(canvas, i, j)
            }
        }

        if (firstClick && !gameOver && !gameWon) {
            drawWelcomeMessage(canvas)
        }

        if (gameOver || gameWon) {
            drawGameOverMessage(canvas)
        }
    }

    private fun drawCell(canvas: Canvas, row: Int, col: Int) {
        val left = col * cellSize + paddingLeft
        val top = row * cellSize + paddingTop
        val right = left + cellSize
        val bottom = top + cellSize
        val cell = grid[row][col]

        // Couleur de fond
        cellPaint.color = when {
            markedBombCells.contains(Pair(row, col)) -> markedColor
            gameOver && cell.isMine -> mineColor
            cell.isRevealed -> revealedColor
            else -> hiddenColor
        }

        canvas.drawRect(left, top, right, bottom, cellPaint)

        canvas.drawRect(left, top, right, bottom, borderPaint)

        when {
            markedBombCells.contains(Pair(row, col)) -> {
                markPaint.color = Color.WHITE
                val textY = top + cellSize / 2 - (markPaint.ascent() + markPaint.descent()) / 2
                canvas.drawText("💣", left + cellSize / 2, textY, markPaint)
            }
            cell.isRevealed && cell.isMine -> {
                cellPaint.color = Color.BLACK
                canvas.drawCircle(left + cellSize / 2, top + cellSize / 2, cellSize / 3, cellPaint)
            }
            cell.isRevealed && cell.adjacentMines > 0 -> {
                textPaint.color = getNumberColor(cell.adjacentMines)
                val textY = top + cellSize / 2 - (textPaint.ascent() + textPaint.descent()) / 2
                canvas.drawText(cell.adjacentMines.toString(), left + cellSize / 2, textY, textPaint)
            }
            cell.isFlagged -> {
                textPaint.color = flagColor
                val textY = top + cellSize / 2 - (textPaint.ascent() + textPaint.descent()) / 2
                canvas.drawText("🚩", left + cellSize / 2, textY, textPaint)
            }
        }
    }

    private fun getNumberColor(number: Int): Int {
        return when (number) {
            1 -> Color.BLUE
            2 -> Color.GREEN
            3 -> Color.RED
            4 -> Color.parseColor("#8B008B")
            5 -> Color.parseColor("#8B0000")
            6 -> Color.parseColor("#008B8B")
            7 -> Color.BLACK
            8 -> Color.GRAY
            else -> Color.BLACK
        }
    }

    private fun drawWelcomeMessage(canvas: Canvas) {
        textPaint.color = Color.WHITE
        textPaint.textSize = cellSize * 0.7f
        textPaint.setShadowLayer(6f, 3f, 3f, Color.BLACK)
        canvas.drawText("MODE: ${MainActivity.GameMode.CLASSIC.displayName.uppercase()}", width / 2f, height / 2f - cellSize * 2, textPaint)
        canvas.drawText("NIVEAU: ${difficulty.displayName.uppercase()}", width / 2f, height / 2f - cellSize, textPaint)
        textPaint.textSize = cellSize * 0.5f
        canvas.drawText("TOUCHEZ POUR COMMENCER !", width / 2f, height / 2f + cellSize, textPaint)
        textPaint.setShadowLayer(0f, 0f, 0f, Color.TRANSPARENT)
    }

    private fun drawGameOverMessage(canvas: Canvas) {
        val overlayPaint = Paint().apply {
            color = Color.argb(200, 0, 0, 0)
            style = Paint.Style.FILL
        }
        canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), overlayPaint)

        textPaint.color = if (gameWon) Color.parseColor("#2ecc71") else Color.parseColor("#e74c3c")
        textPaint.textSize = cellSize * 1.2f
        val message = if (gameWon) "🎉 VICTOIRE !" else "💥 GAME OVER"
        canvas.drawText(message, width / 2f, height / 2f, textPaint)

        textPaint.textSize = cellSize * 0.4f
        textPaint.color = Color.WHITE
        canvas.drawText("Touchez pour rejouer", width / 2f, height / 2f + cellSize, textPaint)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                if (gameOver || gameWon) {
                    initializeGame()
                    gameCallbacks?.onMinesCounterUpdate(totalMines - flagsPlaced)
                    return true
                }

                touchX = event.x
                touchY = event.y
                lastTouchTime = System.currentTimeMillis()
                longPressDetected = false

                if (bombMarkingMode) {
                    // En mode marquage, traitement immédiat
                    val col = ((touchX - paddingLeft) / cellSize).toInt()
                    val row = ((touchY - paddingTop) / cellSize).toInt()
                    if (row in 0 until rows && col in 0 until cols) {
                        toggleBombMark(row, col)
                    }
                } else {
                    handler.postDelayed({ checkLongPress() }, longPressTimeout)
                }
            }
            MotionEvent.ACTION_UP -> {
                handler.removeCallbacksAndMessages(null)
                if (!longPressDetected && !bombMarkingMode) {
                    val col = ((touchX - paddingLeft) / cellSize).toInt()
                    val row = ((touchY - paddingTop) / cellSize).toInt()
                    if (row in 0 until rows && col in 0 until cols) {
                        if (firstClick) {
                            placeMines(row, col)
                            firstClick = false
                            gameCallbacks?.onGameStart()
                        }
                        val cell = grid[row][col]
                        // Ne pas révéler les cases marquées comme bombes suspectées
                        if (!cell.isRevealed && !cell.isFlagged && !markedBombCells.contains(Pair(row, col))) {
                            revealCell(row, col)
                            checkGameWin()
                            invalidate()
                        }
                    }
                }
            }
            MotionEvent.ACTION_MOVE -> {
                val moveThreshold = cellSize * 0.3f
                if (Math.abs(event.x - touchX) > moveThreshold || Math.abs(event.y - touchY) > moveThreshold) {
                    handler.removeCallbacksAndMessages(null)
                }
            }
        }
        return true
    }

    private fun checkLongPress() {
        val currentTime = System.currentTimeMillis()
        if (currentTime - lastTouchTime >= longPressTimeout) {
            longPressDetected = true
            val col = ((touchX - paddingLeft) / cellSize).toInt()
            val row = ((touchY - paddingTop) / cellSize).toInt()
            if (row in 0 until rows && col in 0 until cols) {
                toggleFlag(row, col)
            }
        }
    }

    private fun toggleFlag(row: Int, col: Int) {
        val cell = grid[row][col]
        if (!cell.isRevealed && !markedBombCells.contains(Pair(row, col))) {
            cell.isFlagged = !cell.isFlagged
            flagsPlaced += if (cell.isFlagged) 1 else -1
            gameCallbacks?.onMinesCounterUpdate(totalMines - flagsPlaced)
            gameCallbacks?.onFlagToggled()
            invalidate()
        }
    }

    private fun revealCell(row: Int, col: Int) {
        val cell = grid[row][col]
        if (cell.isRevealed || cell.isFlagged) return

        cell.isRevealed = true
        gameCallbacks?.onCellRevealed()

        if (cell.isMine) {
            gameOver = true
            revealAllMines()
            val progress = calculateProgress()
            val time = ((System.currentTimeMillis() - (context as MainActivity).startTime) / 1000).toInt()
            gameCallbacks?.onGameOver(progress, cellsRevealed, totalSafeCells, time)
            return
        }

        if (!cell.isMine) {
            cellsRevealed++
            unrevealedSafeCells--
            gameCallbacks?.onProgressUpdate(calculateProgress())
        }

        if (cell.adjacentMines == 0) {
            for (i in -1..1) {
                for (j in -1..1) {
                    val newRow = row + i
                    val newCol = col + j
                    if (newRow in 0 until rows && newCol in 0 until cols) {
                        revealCell(newRow, newCol)
                    }
                }
            }
        }
    }

    private fun revealAllMines() {
        for (i in 0 until rows) {
            for (j in 0 until cols) {
                if (grid[i][j].isMine) {
                    grid[i][j].isRevealed = true
                }
            }
        }
    }

    private fun checkGameWin() {
        if (unrevealedSafeCells == 0) {
            gameWon = true
            val progress = calculateProgress()
            val time = ((System.currentTimeMillis() - (context as MainActivity).startTime) / 1000).toInt()
            gameCallbacks?.onGameWin(progress, cellsRevealed, totalSafeCells, time)
        }
    }
}