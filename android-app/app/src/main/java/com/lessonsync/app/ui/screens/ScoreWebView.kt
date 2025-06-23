// ScoreWebView.kt (최종 통합 버전)

import android.annotation.SuppressLint
import android.graphics.Color
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.lessonsync.app.entity.AnnotationEntity
import com.lessonsync.app.util.JsBridge
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun ScoreWebView(
    modifier: Modifier = Modifier,
    filePath: String?,
    zoomLevel: Float,
    annotations: List<AnnotationEntity>,
    showAnnotations: Boolean,
    highlightedMeasure: Int?
) {
    val scoreUrl = "file:///android_asset/score.html"
    // 웹뷰 초기화 여부를 추적
    val isWebViewInitialized = remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current

    // 현재 주석 데이터를 기억하는 상태 변수
    val currentAnnotations = remember { mutableStateOf(annotations) }
    val currentShowAnnotations = remember { mutableStateOf(showAnnotations) }

    // 디버깅용 로그 추가
    Log.d("ScoreWebView", "Rendering with ${annotations.size} annotations, showing: $showAnnotations")

    // 라이프사이클 관찰
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_RESUME -> {
                    Log.d("ScoreWebView", "Lifecycle: ON_RESUME")
                }
                else -> { /* 다른 라이프사이클 이벤트는 무시 */ }
            }
        }

        // 관찰자 등록 및 해제
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    // 주석 데이터가 변경될 때마다 현재 상태 업데이트
    LaunchedEffect(annotations, showAnnotations) {
        currentAnnotations.value = annotations
        currentShowAnnotations.value = showAnnotations
    }

    AndroidView(
        modifier = modifier,
        factory = { context ->
            // 매번 새로운 WebView 인스턴스 생성 (재사용하지 않음)
            WebView(context).apply {
                settings.javaScriptEnabled = true
                settings.domStorageEnabled = true
                settings.allowFileAccess = true
                settings.allowContentAccess = true
                setBackgroundColor(Color.TRANSPARENT)
                setLayerType(View.LAYER_TYPE_SOFTWARE, null)
                addJavascriptInterface(JsBridge(filePath), "jsBridge")

                webViewClient = object : WebViewClient() {
                    override fun onPageFinished(view: WebView?, url: String?) {
                        super.onPageFinished(view, url)
                        Log.d("ScoreWebView", "Web page loaded, initializing OSMD")

                        // 초기화 스크립트는 이제 OSMD 객체 생성만 담당
                        val initialLoadScript = """
                            function checkAndRun() {
                                if (typeof opensheetmusicdisplay !== 'undefined') {
                                    window.osmd = new opensheetmusicdisplay.OpenSheetMusicDisplay("score-container", { autoResize: true, backend: "svg" });
                                    const xmlData = window.jsBridge.getUncompressedMusicXML();
                                    if (xmlData) {
                                        console.log("XML data loaded, initializing score");
                                        // load만 하고 render는 updateDisplay에서 처리
                                        window.osmd.load(xmlData).then(() => {
                                            window.isScoreLoaded = true;
                                            console.log("Score loaded successfully");
                                            
                                            // 초기 상태로 악보 및 주석을 렌더링
                                            const initialState = {
                                                zoom: ${zoomLevel},
                                                annotations: ${annotationsToJsArray(annotations)},
                                                showAnnotations: ${showAnnotations},
                                                highlightMeasure: ${highlightedMeasure ?: "null"}
                                            };
                                            updateDisplay(initialState);
                                        });
                                    } else {
                                        console.error("Failed to get XML data");
                                    }
                                } else {
                                    setTimeout(checkAndRun, 100);
                                }
                            }
                            checkAndRun();
                        """.trimIndent()
                        view?.evaluateJavascript(initialLoadScript, null)
                        isWebViewInitialized.value = true
                    }
                }
                loadUrl(scoreUrl)
            }
        },
        update = { webView ->
            // 웹뷰가 초기화된 후에만 업데이트 수행
            if (isWebViewInitialized.value) {
                updateWebViewWithAnnotations(webView, zoomLevel, annotations, showAnnotations, highlightedMeasure)
            }
        }
    )

    // 마운트 해제 시 정리
    DisposableEffect(Unit) {
        onDispose {
            Log.d("ScoreWebView", "Disposing ScoreWebView")
        }
    }
}

// 웹뷰 업데이트 로직
private fun updateWebViewWithAnnotations(
    webView: WebView,
    zoomLevel: Float,
    annotations: List<AnnotationEntity>,
    showAnnotations: Boolean,
    highlightedMeasure: Int?
) {
    // 디버깅 로그
    Log.d("ScoreWebView", "Updating WebView with ${annotations.size} annotations, showing: $showAnnotations")

    // 모든 상태 업데이트는 이 함수를 통해 이루어집니다.
    val annotationsJson = annotationsToJsArray(annotations)
    val stateJson = """
        {
            "zoom": $zoomLevel,
            "annotations": $annotationsJson,
            "showAnnotations": $showAnnotations,
            "highlightMeasure": ${highlightedMeasure ?: "null"}
        }
    """.trimIndent()

    // 모든 상태를 담은 JSON 객체를 단 하나의 마스터 함수에 전달합니다.
    val updateScript = """
        try {
            if (window.isScoreLoaded) { 
                console.log("Updating display with annotations");
                updateDisplay($stateJson);
            } else {
                console.log("Score not loaded yet, will update later");
                // 로드되지 않았다면 잠시 후 다시 시도
                setTimeout(() => {
                    if (window.isScoreLoaded) {
                        updateDisplay($stateJson);
                    }
                }, 500);
            }
        } catch(e) {
            console.error("Error updating display:", e);
        }
    """
    webView.evaluateJavascript(updateScript, null)
}

// 헬퍼 함수 - JSONObject를 사용하여 더 안전하게 JSON 문자열 생성
private fun annotationsToJsArray(annotations: List<AnnotationEntity>): String {
    if (annotations.isEmpty()) return "[]"

    // 표준 JSON 라이브러리를 사용하여 안전하게 JSON 문자열 생성
    return try {
        val jsonArray = JSONArray()

        annotations.forEach { annotation ->
            val annotationObj = JSONObject()
            annotationObj.put("measure", annotation.measureNumber)
            annotationObj.put("text", annotation.directive) // JSONObject가 특수문자를 알아서 처리
            jsonArray.put(annotationObj)
        }

        jsonArray.toString()
    } catch (e: Exception) {
        Log.e("ScoreWebView", "Error creating annotations JSON", e)
        "[]" // 오류 발생 시 빈 배열 반환
    }
}
