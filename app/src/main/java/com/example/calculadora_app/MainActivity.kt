package com.example.calculadora_app

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import kotlin.math.*

class MainActivity : AppCompatActivity() {

    private lateinit var tvuno: TextView
    private lateinit var tvdos: TextView

    private val pi = "3.14159265"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        initializeViews()
        setButtonClickListeners()
    }

    private fun initializeViews() {
        tvuno = findViewById(R.id.tvuno)
        tvdos = findViewById(R.id.tvdos)
    }

    private fun setButtonClickListeners() {
        val numberButtons = listOf<Button>(findViewById(R.id.b0), findViewById(R.id.b1), findViewById(R.id.b2),
            findViewById(R.id.b3), findViewById(R.id.b4), findViewById(R.id.b5), findViewById(R.id.b6),
            findViewById(R.id.b7), findViewById(R.id.b8), findViewById(R.id.b9))

        numberButtons.forEach { button ->
            button.setOnClickListener {
                appendText(button.text.toString())
            }
        }

        val operationButtons = listOf<Button>(findViewById(R.id.btnmas), findViewById(R.id.btnres),
            findViewById(R.id.btnentre), findViewById(R.id.btnmul), findViewById(R.id.btnraiz),
            findViewById(R.id.btnx2), findViewById(R.id.btnx3), findViewById(R.id.btnsin),
            findViewById(R.id.btncos), findViewById(R.id.btntan), findViewById(R.id.btnlog),
            findViewById(R.id.btnln), findViewById(R.id.btnparent), findViewById(R.id.btnparent2),
            findViewById(R.id.bpi))

        operationButtons.forEach { button ->
            button.setOnClickListener {
                appendText(button.text.toString())
            }
        }

        findViewById<Button>(R.id.btnpunto).setOnClickListener {
            val res = tvdos.text.toString()
            if (!res.contains(".")) {
                appendText(".")
            }
        }

        findViewById<Button>(R.id.btnac).setOnClickListener {
            tvuno.text = ""
            tvdos.text = ""
        }

        findViewById<Button>(R.id.btnc).setOnClickListener {
            val res = tvdos.text.toString()
            if (res.isNotEmpty()) {
                tvdos.text = res.substring(0, res.length - 1)
            }
        }

        findViewById<Button>(R.id.bequal).setOnClickListener {
            evaluateExpression()
        }
    }

    private fun appendText(text: String) {
        tvdos.append(text)
    }

    private fun evaluateExpression() {
        val expression = tvdos.text.toString().replace('÷', '/').replace('x', '*')
        try {
            val result = eval(expression)
            tvdos.text = result.toString()
            tvuno.text = expression
        } catch (e: Exception) {
            tvdos.text = "Error"
            e.printStackTrace()
        }
    }

    private fun eval(expression: String): Double {
        val result = object {
            private var pos = -1
            private var ch = 0

            private fun nextChar(expression: String) {
                ch = if (++pos < expression.length) expression[pos].toInt() else -1
            }

            private fun eat(expression: String, charToEat: Int): Boolean {
                while (ch == ' '.toInt()) nextChar(expression)
                if (ch == charToEat) {
                    nextChar(expression)
                    return true
                }
                return false
            }

            fun parse(expression: String): Double {
                nextChar(expression)
                val X = parseExpression(expression)
                if (pos < expression.length) throw RuntimeException("Unexpected: " + ch.toChar())
                return X
            }

            private fun parseExpression(expression: String): Double {
                var X = parseTerm(expression)
                while (true) {
                    X = when {
                        eat(expression, '+'.toInt()) -> X + parseTerm(expression)
                        eat(expression, '-'.toInt()) -> X - parseTerm(expression)
                        else -> return X
                    }
                }
            }

            private fun parseTerm(expression: String): Double {
                var X = parseFactor(expression)
                while (true) {
                    X = when {
                        eat(expression, '*'.toInt()) -> X * parseFactor(expression)
                        eat(expression, '/'.toInt()) -> X / parseFactor(expression)
                        else -> return X
                    }
                }
            }

            private fun parseFactor(expression: String): Double {
                if (eat(expression, '+'.toInt())) return parseFactor(expression)
                if (eat(expression, '-'.toInt())) return -parseFactor(expression)

                var X: Double
                val startPost = pos
                if (eat(expression, '('.toInt())) {
                    X = parseExpression(expression)
                    eat(expression, ')'.toInt())
                } else if ((ch in '0'.toInt()..'9'.toInt()) || ch == '.'.toInt()) {
                    while ((ch in '0'.toInt()..'9'.toInt()) || ch == '.'.toInt()) nextChar(expression)
                    X = expression.substring(startPost, pos).toDouble()
                } else if (ch in 'a'.toInt()..'z'.toInt()) {
                    while (ch in 'a'.toInt()..'z'.toInt()) nextChar(expression)
                    val func = expression.substring(startPost, pos)
                    X = parseFactor(expression)
                    X = when (func) {
                        "sqrt" -> sqrt(X)
                        "sin" -> sin(Math.toRadians(X))
                        "cos" -> cos(Math.toRadians(X))
                        "tan" -> tan(Math.toRadians(X))
                        "log" -> log10(X)
                        "ln" -> ln(X)
                        else -> throw RuntimeException("Unknown function: $func")
                    }
                } else {
                    throw RuntimeException("Unexpected: " + ch.toChar())
                }
                if (eat(expression, '^'.toInt())) X = X.pow(parseFactor(expression))
                return X
            }

        }.parse(expression)

        return result
    }

}
