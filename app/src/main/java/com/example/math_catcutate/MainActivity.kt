package com.example.math_catcutate

import android.content.Intent
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.util.TypedValue
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.math_catcutate.databinding.ActivityMainBinding
import net.objecthunter.exp4j.ExpressionBuilder
import java.lang.ArithmeticException
import java.math.BigDecimal
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

        val toolbar: androidx.appcompat.widget.Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_STABLE or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
        window.statusBarColor = android.graphics.Color.TRANSPARENT

    }


    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val inflater: MenuInflater = menuInflater
        inflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_history -> {
                toggleHistoryView()
                true
            }
            R.id.action_clearHistory ->{
                historyList.clear()
                updateHistoryView()
                val message = "History cleared successfully."
                Toast.makeText(applicationContext, message, Toast.LENGTH_SHORT).show()

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

    private fun openWebPage(url: String) {
        val webpage: Uri = Uri.parse(url)
        val intent = Intent(Intent.ACTION_VIEW, webpage)
        startActivity(intent)
    }

    private fun showAppVersion() {
        try {
            val packageManager: PackageManager = this.packageManager
            val packageInfo: PackageInfo = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                packageManager.getPackageInfo(this.packageName, PackageManager.GET_SIGNING_CERTIFICATES)
            } else {
                packageManager.getPackageInfo(this.packageName, 0)
            }
            val versionName: String = packageInfo.versionName
            val versionCode: Long = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                packageInfo.longVersionCode
            } else {
                @Suppress("DEPRECATION")
                packageInfo.versionCode.toLong()
            }
            Toast.makeText(this, "Version: $versionName ($versionCode)", Toast.LENGTH_LONG).show()
        } catch (e: Exception) {
            Log.e("MainActivity", "Failed to get version info", e)
            Toast.makeText(this, "Failed to get version info", Toast.LENGTH_SHORT).show()
        }
    }

    fun onClickClear(view: View) {
        binding.operationTv.text = ""
        binding.resultTv.text = ""
        binding.resultTv.visibility = View.GONE
        lastNum = false
        lastDot = false
        stmtError = false
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
    }

    fun onClickNum(view: View) {
        if (stmtError) {
            binding.operationTv.text = (view as Button).text
            stmtError = false
        } else {
            binding.operationTv.append((view as Button).text)
        }
        lastNum = true
        onEquals()
    }

    fun onClick00(view: View) {
        if (stmtError) {
            binding.operationTv.text = "00"
            stmtError = false
        } else {
            binding.operationTv.append("00")
        }
        lastNum = true
        onEquals()
    }

    fun onClickEquals(view: View) {
        onEquals()
        binding.resultTv.setTextSize(TypedValue.COMPLEX_UNIT_SP, 50f)
        saveToHistory(binding.operationTv.text.toString(), binding.resultTv.text.toString().substring(2))
    }

    private fun onEquals() {
        if (lastNum && !stmtError) {
            val inputText = binding.operationTv.text.toString()

            if (inputText == "143") {
                binding.resultTv.visibility = View.VISIBLE
                binding.resultTv.text = "I Love You Cutie ❤️🤗"
                return
            }

            val expressionText = inputText.replace("×", "*").replace("÷", "/")

            if (expressionText.isEmpty()) {
                showError("Error: Empty Expression")
                return
            }

            try {
                val expression = ExpressionBuilder(expressionText).build()
                val result = BigDecimal(expression.evaluate(), MathContext.DECIMAL128)
                val txtresult = if (result.stripTrailingZeros().scale() <= 0) {
                    result.toPlainString()
                } else {
                    result.setScale(10, BigDecimal.ROUND_HALF_UP).toPlainString()
                }

                binding.resultTv.visibility = View.VISIBLE
                binding.resultTv.text = "= $txtresult"
                stmtError = false
                lastNum = true

            } catch (ex: ArithmeticException) {
                Log.e("MainActivity", "evaluate error", ex)
                showError("Error")
            } catch (ex: Exception) {
                Log.e("MainActivity", "evaluate error", ex)
                showError("Error")
            }
        }
    }

    private fun saveToHistory(inputText: String, txtResult: String) {
        val entry = HistoryEntry(inputText, txtResult)
        historyList.add(0, entry)
        updateHistoryView()
    }

    private fun updateHistoryView() {
        val historyText = historyList.joinToString(separator = "\n") { "${it.expression} = ${it.result}" }
        binding.historyTv.text = historyText
    }

    private fun toggleHistoryView() {
        if (binding.historyScrollView.visibility == View.VISIBLE) {
            binding.historyScrollView.visibility = View.GONE
        } else {
            binding.historyScrollView.visibility = View.VISIBLE
        }
    }

    private fun showError(message: String) {
        binding.resultTv.visibility = View.VISIBLE
        binding.resultTv.text = message
        stmtError = true
        lastNum = false
    }
}
