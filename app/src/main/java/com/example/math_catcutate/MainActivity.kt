package com.example.math_catcutate

import android.content.Context
import android.content.Intent
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.graphics.Color.WHITE
import android.net.Uri
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.transition.Fade
import android.transition.TransitionManager
import android.util.Log
import android.util.TypedValue
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.math_catcutate.databinding.ActivityMainBinding
import net.objecthunter.exp4j.ExpressionBuilder
import java.math.BigDecimal
import java.math.BigInteger
import java.math.MathContext

data class HistoryEntry(val expression: String, val result: String)

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private var lastNum = false
    private var lastDot = false
    private var stmtError = false
    private val historyList = mutableListOf<HistoryEntry>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        window.setFlags(
            android.view.WindowManager.LayoutParams.FLAG_FULLSCREEN,
            android.view.WindowManager.LayoutParams.FLAG_FULLSCREEN)

        val toolbar: androidx.appcompat.widget.Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        window.statusBarColor = WHITE

        loadHistoryFromPreferences()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val inflater: MenuInflater = menuInflater
        inflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        vibratePhone(55)
        return when (item.itemId) {
            R.id.action_history -> {
                toggleHistoryView()
                true
            }
            R.id.action_clearHistory -> {
                clearHistory()
                true
            }
            R.id.action_privacy_policy -> {
                openWebPage("https://d-s.netlify.app/privacy_policy")
                true
            }
            R.id.action_about -> {
                openWebPage("https://d-s.netlify.app/")
                true
            }
            R.id.action_version -> {
                showAppVersion()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun Context.vibratePhone(duration: Long = 100) {
        val vib = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        vib.vibrate(VibrationEffect.createOneShot(duration, VibrationEffect.DEFAULT_AMPLITUDE))
    }

    private fun openWebPage(url: String) {
        val webpage: Uri = Uri.parse(url)
        val intent = Intent(Intent.ACTION_VIEW, webpage)
        startActivity(intent)
    }

    private fun showAppVersion() {
        try {
            val packageManager: PackageManager = this.packageManager
            val packageInfo: PackageInfo =
                packageManager.getPackageInfo(this.packageName, PackageManager.GET_SIGNING_CERTIFICATES)
            val versionName: String = packageInfo.versionName
            val versionCode: Long =
                packageInfo.longVersionCode
            Toast.makeText(this, "Version: $versionName ($versionCode)", Toast.LENGTH_LONG).show()
        } catch (e: Exception) {
            Log.e("MainActivity", "Failed to get version info", e)
            Toast.makeText(this, "Failed to get version info", Toast.LENGTH_SHORT).show()
        }
    }

    fun onClickClear(view: View) {
        binding.resultTv.setTextSize(TypedValue.COMPLEX_UNIT_SP, 40f)
        clear()
        vibratePhone(60)
    }

    fun onClickBackspace(view: View) {
        if (binding.operationTv.text.isNotEmpty()) {
            binding.operationTv.text = binding.operationTv.text.toString().dropLast(1)
            try {
                val lastChar = binding.operationTv.text.toString().lastOrNull()
                if (lastChar != null && lastChar.isDigit()) {
                    onEquals()
                }
            } catch (e: Exception) {
                binding.resultTv.text = ""
                binding.resultTv.visibility = View.GONE
                Log.e("MainActivity", "last char error", e)
            }
        }
        vibratePhone(55)
        dynamicAdjustTextSize()
    }

    fun onClickOperator(view: View) {
        if (!stmtError) {
            val inputText = binding.operationTv.text.toString()
            val lastChar = inputText.lastOrNull()

            if (lastChar != null && !lastChar.isDigit() && lastChar.toString() != ")" && lastChar.toString() != "(") {
                binding.operationTv.text = inputText.dropLast(1)
            }

            binding.operationTv.append((view as Button).text)
            lastDot = false
            lastNum = false
        }
        dynamicAdjustTextSize()
        vibratePhone(50)
    }

    fun onClickNum(view: View) {
        if (stmtError) {
            binding.operationTv.text = (view as Button).text
            stmtError = false
        } else {
            binding.operationTv.append((view as Button).text)
        }
        lastNum = true
        dynamicAdjustTextSize()
        onEquals()
        vibratePhone(50)
    }

    fun onClick00(view: View) {
        if (stmtError) {
            binding.operationTv.text = R.id.button00.toString()
            stmtError = false
        } else {
            binding.operationTv.append("00")
        }
        lastNum = true
        dynamicAdjustTextSize()
        onEquals()
        vibratePhone(50)
    }

    fun onClickFact(view: View) {
        val inputText = binding.operationTv.text.toString()
        val number = inputText.toBigIntegerOrNull()
        binding.operationTv.append("!")

        if (inputText.isEmpty()) {
            showError("Error: Empty Expression")
            return
        }

        if (number != null && number >= BigInteger.ZERO) {
            val result = fact(number)
            binding.resultTv.text = String.format(result.toString())
        } else {
            showError("Enter a non-negative integer")
        }
        dynamicAdjustTextSize()
        vibratePhone(50)
        stmtError = false
    }

    private fun fact(n: BigInteger?): BigInteger {
        if (n == null || n == BigInteger.ZERO) {
            return BigInteger.ONE
        }
        return n * fact(n - BigInteger.ONE)
    }

    fun onClickEquals(view: View) {
        val resultText = binding.resultTv.text.toString()
        dynamicAdjustTextSize()
        if (!stmtError && binding.resultTv.text.toString() != "I Love You Kittu beta ❤\uFE0F\uD83E\uDD17") {
            saveToHistory(binding.operationTv.text.toString(), resultText)
        }

        binding.operationTv.text = binding.resultTv.text
        binding.resultTv.visibility = View.GONE
        vibratePhone(60)
    }

    private fun onEquals() {
        if (lastNum && !stmtError) {
            val inputText = binding.operationTv.text.toString()

            if (inputText == "143") {
                binding.resultTv.visibility = View.VISIBLE
                binding.resultTv.setText(R.string.loveMsg)
                return
            }

            if (inputText.isEmpty()) {
                showError("Error: Empty Expression")
                return
            }

            val expressionText = preprocessExpression(inputText)

            if (expressionText.isEmpty()) {
                showError("Error: Empty Expression")
                return
            }

            try {
                val expression = ExpressionBuilder(expressionText).build()
                val result = BigDecimal(expression.evaluate(), MathContext.DECIMAL128)
                val textResult = if (result.stripTrailingZeros().scale() <= 0) {
                    result.toPlainString()
                } else {
                    result.setScale(10, BigDecimal.ROUND_HALF_UP).toPlainString()
                }

                binding.resultTv.visibility = View.VISIBLE
                binding.resultTv.text = textResult
                stmtError = false
                lastNum = true

            } catch (ex: ArithmeticException) {
                Log.e("MainActivity", "evaluate error", ex)
                showError("Error: Arithmetic Exception")
            } catch (ex: Exception) {
                Log.e("MainActivity", "evaluate error", ex)
                showError("Error: Invalid Expression")
            }
        }
        dynamicAdjustTextSize()
    }

    private fun preprocessExpression(expression: String): String {
        return expression
            .replace("×", "*")
            .replace("÷", "/")
            .replace("\uD835\uDF45","π")
            .replace("√","sqrt")
//            .replace(Regex("""(\d+)%""")) { matchResult ->
//                val number = matchResult.groupValues[1].toDouble()
//                (number / 100).toString()
//            }
    }

    private fun showError(message: String) {
        binding.resultTv.visibility = View.VISIBLE
        binding.resultTv.text = message
        stmtError = true
        lastNum = false
    }

    private fun saveToHistory(inputText: String, txtResult: String) {
        val entry = HistoryEntry(inputText, txtResult)
        historyList.add(0, entry)
        updateHistoryView()
        saveHistoryToPreferences()
    }

    private fun updateHistoryView() {
        val historyText = historyList.joinToString(separator = "\n") { "${it.expression} = ${it.result}" }
        if (historyText.isEmpty()) {
            binding.historyTv.setText(R.string.no_history_message)
            return
        }
        binding.historyTv.text = historyText
    }

    private fun toggleHistoryView() {
        if (binding.historyScrollView.visibility == View.VISIBLE) {
            binding.historyScrollView.visibility = View.GONE
        } else {
            binding.historyScrollView.visibility = View.VISIBLE
        }
    }

    private fun clear() {
        binding.operationTv.text = ""
        binding.resultTv.text = ""
        binding.resultTv.visibility = View.GONE
        lastNum = false
        lastDot = false
        stmtError = false
    }

    private fun clearHistory() {
        historyList.clear()
        updateHistoryView()
        saveHistoryToPreferences()
        val message = "History cleared successfully."
        Toast.makeText(applicationContext, message, Toast.LENGTH_SHORT).show()
    }

    fun collapse(view: View) {
        val transition = Fade()
        transition.duration = 500
        transition.interpolator = AccelerateDecelerateInterpolator()
        TransitionManager.beginDelayedTransition(binding.root, transition)
        vibratePhone(55)

        if (binding.spOP.visibility == View.VISIBLE) {
            binding.spOP.visibility = View.GONE
            binding.collapse.text = "∧"
        } else {
            binding.spOP.visibility = View.VISIBLE
            binding.collapse.text = "∨"
        }

        if (binding.spOP.visibility == View.VISIBLE) {
            binding.historyScrollView.visibility = View.GONE
        }
    }

    private fun dynamicAdjustTextSize() {
        val resultText = binding.resultTv.text.toString()
        val inputText = binding.operationTv.text.toString()

        val operationTextSize = when {
            binding.resultTv.visibility == View.VISIBLE -> when {
                inputText.length > 30 -> 15f
                inputText.length > 25 -> 18f
                inputText.length > 20 -> 20f
                inputText.length > 15 -> 25f
                inputText.length > 10 -> 30f
                else -> 35f
            }
            else -> when {
                inputText.length > 30 -> 25f
                inputText.length > 25 -> 30f
                inputText.length > 20 -> 35f
                inputText.length > 15 -> 40f
                inputText.length > 10 -> 45f
                else -> 45f
            }
        }
        binding.operationTv.setTextSize(TypedValue.COMPLEX_UNIT_SP, operationTextSize)

        val resultTextSize = when {
            resultText.length > 30 -> 25f
            resultText.length > 25 -> 30f
            resultText.length > 20 -> 35f
            resultText.length > 15 -> 40f
            resultText.length > 10 -> 45f
            else -> 50f
        }
        binding.resultTv.setTextSize(TypedValue.COMPLEX_UNIT_SP, resultTextSize)
    }

    private fun saveHistoryToPreferences() {
        val sharedPreferences = getSharedPreferences("MathCatculatePrefs", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        val historySet = historyList.map { "${it.expression}=${it.result}" }.toSet()
        editor.putStringSet("historyList", historySet)
        editor.apply()
    }

    private fun loadHistoryFromPreferences() {
        val sharedPreferences = getSharedPreferences("MathCatculatePrefs", Context.MODE_PRIVATE)
        val historySet = sharedPreferences.getStringSet("historyList", emptySet())
        historyList.clear()
        historySet?.forEach {
            val parts = it.split("=")
            if (parts.size == 2) {
                historyList.add(HistoryEntry(parts[0], parts[1]))
            }
        }
        updateHistoryView()
    }
}
