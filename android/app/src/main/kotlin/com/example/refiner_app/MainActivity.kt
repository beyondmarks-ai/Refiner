package com.example.refiner_app

import io.flutter.embedding.android.FlutterActivity
import io.flutter.embedding.engine.FlutterEngine
import io.flutter.plugin.common.MethodChannel
import java.util.concurrent.Executors

class MainActivity : FlutterActivity() {
    private val executor = Executors.newSingleThreadExecutor()

    override fun configureFlutterEngine(flutterEngine: FlutterEngine) {
        super.configureFlutterEngine(flutterEngine)
        MethodChannel(
            flutterEngine.dartExecutor.binaryMessenger,
            "com.example.refiner_app/backend",
        ).setMethodCallHandler { call, result ->
            when (call.method) {
                "refine" -> {
                    val input = call.argument<String>("text").orEmpty()
                    executor.execute {
                        try {
                            val output = RefinerBackendClient.refine(this, input)
                            runOnUiThread { result.success(output) }
                        } catch (error: Exception) {
                            runOnUiThread {
                                result.error(
                                    "REFINE_FAILED",
                                    error.message ?: "Could not refine the text.",
                                    null,
                                )
                            }
                        }
                    }
                }

                else -> result.notImplemented()
            }
        }
    }

    override fun onDestroy() {
        executor.shutdown()
        super.onDestroy()
    }
}