// ScoreWebView.kt (최종 단순화 버전)

import android.annotation.SuppressLint
import android.graphics.Color
import android.util.Log
import android.view.View
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import com.lessonsync.app.entity.AnnotationEntity
import com.lessonsync.app.util.JsBridge

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

    Log.d("ScoreWebView", "annotations: ${annotations.size}, showAnnotations: $showAnnotations")

    AndroidView(
        modifier = modifier,
        factory = { context ->
            // 1. factory에서는 WebView를 생성하고 초기 설정만 담당합니다.
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
                        // 2. 페이지 로딩이 끝나면, OSMD를 초기화하고 악보를 그리는 스크립트만 실행합니다.
                        //    이 스크립트는 모든 주석을 그리고, 초기 상태를 한 번에 적용합니다.
                        val initialLoadScript = """
                            function checkAndRun() {
                                if (typeof opensheetmusicdisplay !== 'undefined') {
                                    window.osmd = new opensheetmusicdisplay.OpenSheetMusicDisplay("score-container", { autoResize: true, backend: "svg" });
                                    const xmlData = window.jsBridge.getUncompressedMusicXML();
                                    if (xmlData) {
                                        osmd.load(xmlData).then(() => {
                                            osmd.zoom = ${zoomLevel}; // 초기 줌 레벨 적용
                                            osmd.render();
                                            console.log("Initial score rendered with zoom: ${zoomLevel}");
                                            
                                            // 저장된 모든 주석을 초기에 한 번에 그림
                                            const annotationList = ${annotationsToJsArray(annotations)};
                                            annotationList.forEach(a => addAnnotationToMeasure(a.measure, a.text));
                                            
                                            // 초기 주석 표시 상태 및 하이라이트 적용
                                            toggleAnnotations(${showAnnotations});
                                            highlightMeasure(${highlightedMeasure});
                                        });
                                    }
                                } else {
                                    setTimeout(checkAndRun, 100);
                                }
                            }
                            checkAndRun();
                        """.trimIndent()
                        view?.evaluateJavascript(initialLoadScript, null)
                    }
                }
                loadUrl(scoreUrl)
            }
        },
        update = { webView ->
            // 3. update 블록은 Composable이 리컴포지션될 때마다 호출됩니다.
            //    이곳에서 변경된 상태를 기반으로 JS 함수를 호출하여 화면을 갱신합니다.
            //    JS 함수 내부에 'if(window.osmd)' 안전장치가 있어, OSMD가 준비되기 전에는 실행되지 않습니다.

            // 줌 레벨 업데이트
            val zoomScript = "if (window.osmd) { osmd.zoom = ${zoomLevel}; osmd.render(); scaleAnnotationLayer(${zoomLevel}); }"
            webView.evaluateJavascript(zoomScript, null)

            // 주석 표시여부 업데이트
            val toggleScript = "if(window.osmd) { toggleAnnotations(${showAnnotations}); }"
            webView.evaluateJavascript(toggleScript, null)

            // 마디 하이라이트 업데이트
            val highlightScript = "if(window.osmd && ${highlightedMeasure != null}) { highlightMeasure(${highlightedMeasure}); }"
            webView.evaluateJavascript(highlightScript, null)
        }
    )
}

// 헬퍼 함수
private fun annotationsToJsArray(annotations: List<AnnotationEntity>): String {
    return annotations.joinToString(prefix = "[", postfix = "]") {
        "{ measure: ${it.measureNumber}, text: '${it.directive.replace("'", "\\'")}' }"
    }
}