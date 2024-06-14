package com.example.math_catcutate

import android.content.pm.PackageInfo
import android.content.pm.PackageManager
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

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private var lastNum = false
    private var lastDot = false
    private var stmtError = false

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
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val inflater: MenuInflater = menuInflater
        inflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_history -> {
                // Handle History action
                Toast.makeText(this, "History selected", Toast.LENGTH_SHORT).show()
                true
            }
            R.id.action_privacy_policy -> {
                // Handle Privacy Policy action
                Toast.makeText(this, "Privacy Policy selected", Toast.LENGTH_SHORT).show()
                true
            }
            R.id.action_about -> {
                // Handle About action
                Toast.makeText(this, "About selected", Toast.LENGTH_SHORT).show()
                true
            }
            R.id.action_version -> {
                val packageManager: PackageManager = this.packageManager
                val packageInfo: PackageInfo = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    packageManager.getPackageInfo(this.packageName, PackageManager.PackageInfoFlags.of(0))
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
                true
            }
            else -> super.onOptionsItemSelected(item)
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
                Log.e("last char error", e.toString())
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
                val result = expression.evaluate()
                val txtresult = if (result % 1 == 0.0) {
                    result.toInt().toString()
                } else {
                    result.toString()
                }

                binding.resultTv.visibility = View.VISIBLE
                binding.resultTv.text = "= $txtresult"
                stmtError = false
                lastNum = true
            } catch (ex: ArithmeticException) {
                Log.e("evaluate error", ex.toString())
                showError("Error")
            } catch (ex: Exception) {
                Log.e("evaluate error", ex.toString())
                showError("Error")
            }
        }
    }

    private fun showError(message: String) {
        binding.resultTv.visibility = View.VISIBLE
        binding.resultTv.text = message
        stmtError = true
        lastNum = false
    }
}
