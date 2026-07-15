package com.example.refiner_app

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.view.Gravity
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import java.util.concurrent.Executors

class RefineActivity : Activity() {
    private val executor = Executors.newSingleThreadExecutor()
    private lateinit var selectedText: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (intent?.action != Intent.ACTION_PROCESS_TEXT) {
            finish()
            return
        }

        selectedText = intent
            .getCharSequenceExtra(Intent.EXTRA_PROCESS_TEXT)
            ?.toString()
            .orEmpty()

        if (selectedText.isBlank()) {
            showError("No text was selected.")
            return
        }

        showProgress()
        runRefinement()
    }

    private fun showProgress() {
        val padding = (24 * resources.displayMetrics.density).toInt()
        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(padding, padding, padding, padding)
            addView(ProgressBar(this@RefineActivity))
            addView(
                TextView(this@RefineActivity).apply {
                    text = "Refining…"
                    textSize = 18f
                    setPadding(padding, 0, 0, 0)
                },
                LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                ),
            )
        }
        setContentView(layout)
    }

    private fun runRefinement() {
        executor.execute {
            try {
                val refined = RefinerBackendClient.refine(this, selectedText)
                runOnUiThread {
                    setResult(
                        RESULT_OK,
                        Intent().putExtra(Intent.EXTRA_PROCESS_TEXT, refined),
                    )
                    finish()
                }
            } catch (error: Exception) {
                runOnUiThread {
                    showError(error.message ?: "Could not refine the text.")
                }
            }
        }
    }

    private fun showError(message: String) {
        AlertDialog.Builder(this)
            .setTitle("Couldn’t refine text")
            .setMessage(message)
            .setPositiveButton("Retry") { _, _ ->
                showProgress()
                runRefinement()
            }
            .setNegativeButton("Cancel") { _, _ -> finish() }
            .setOnCancelListener { finish() }
            .show()
    }

    override fun onDestroy() {
        executor.shutdownNow()
        super.onDestroy()
    }
}