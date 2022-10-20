package dev.msartore.ares.ui.compose

import android.util.Size
import androidx.camera.core.CameraControl
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import java.util.concurrent.Executor

@Composable
fun CameraPreview(
    analyzer: ImageAnalysis.Analyzer,
    cameraControl: MutableState<CameraControl?>,
    isFlashEnabled: MutableState<Boolean>
) {

    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val lifecycle = LocalLifecycleOwner.current.lifecycle
    val cameraProviderFuture = remember { ProcessCameraProvider.getInstance(context) }
    val cameraProvider = remember { mutableStateOf<ProcessCameraProvider?>(null) }
    val latestLifecycleEvent = remember { mutableStateOf(Lifecycle.Event.ON_ANY) }

    DisposableEffect(lifecycle) {

        val observer = LifecycleEventObserver { _, event ->
            latestLifecycleEvent.value = event
        }

        lifecycle.addObserver(observer)

        onDispose {
            cameraProvider.value?.unbindAll()
            lifecycle.removeObserver(observer)
        }
    }

    LaunchedEffect(latestLifecycleEvent.value == Lifecycle.Event.ON_RESUME) {
        cameraControl.value?.enableTorch(isFlashEnabled.value)
    }

    AndroidView(
        factory = { ctx ->
            val previewView = PreviewView(ctx)
            val executor = ContextCompat.getMainExecutor(ctx)

            cameraProviderFuture.addListener({
                cameraProvider.value = cameraProviderFuture.get()
                val preview = Preview.Builder().build().apply {
                    setSurfaceProvider(previewView.surfaceProvider)
                }

                val cameraSelector = CameraSelector.Builder()
                    .requireLensFacing(CameraSelector.LENS_FACING_BACK)
                    .build()

                cameraProvider.value?.unbindAll()

                val camera = cameraProvider.value?.bindToLifecycle(
                    lifecycleOwner,
                    cameraSelector,
                    setupImageAnalysis(executor, analyzer),
                    preview
                )

                cameraControl.value = camera?.cameraControl
            }, executor)
            previewView
        },
        modifier = Modifier.fillMaxSize(),
    )
}

private fun setupImageAnalysis(executor: Executor, analyzer: ImageAnalysis.Analyzer) =
    ImageAnalysis.Builder()
        .setTargetResolution(Size(720, 1280))
        .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
        .build()
        .apply {
            setAnalyzer(executor,analyzer)
        }
