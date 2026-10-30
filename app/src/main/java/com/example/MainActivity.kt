package com.example

import android.annotation.SuppressLint
import android.content.pm.ActivityInfo
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.webkit.WebChromeClient
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color as ComposeColor
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE
    super.onCreate(savedInstanceState)
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
      window.attributes.layoutInDisplayCutoutMode =
        WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES
    }
    enableEdgeToEdge()
    hideSystemBars()
    setContent {
      MyApplicationTheme {
        HOI4CommandScreen()
      }
    }
  }

  override fun onWindowFocusChanged(hasFocus: Boolean) {
    super.onWindowFocusChanged(hasFocus)
    if (hasFocus) {
      hideSystemBars()
    }
  }

  private fun hideSystemBars() {
    val windowInsetsController = WindowCompat.getInsetsController(window, window.decorView)
    windowInsetsController.systemBarsBehavior =
      WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
    windowInsetsController.hide(WindowInsetsCompat.Type.systemBars())
  }
}

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun HOI4CommandScreen() {
  var webViewRef by remember { mutableStateOf<WebView?>(null) }

  BackHandler(enabled = true) {
    val wv = webViewRef
    if (wv != null) {
      wv.evaluateJavascript(
        """
        (function() {
          var setOv = document.getElementById('setOv');
          if (setOv) { setOv.remove(); return true; }
          var leadOv = document.getElementById('leadOv');
          if (leadOv) { leadOv.remove(); return true; }
          var tabMod = document.getElementById('tabModalOverlay');
          if (tabMod && !tabMod.classList.contains('hidden')) {
            tabMod.classList.add('hidden');
            return true;
          }
          var newsMod = document.getElementById('eventNewspaperOverlay');
          if (newsMod && !newsMod.classList.contains('hidden')) {
            newsMod.classList.add('hidden');
            return true;
          }
          var insp = document.getElementById('provinceInspector');
          if (insp && !insp.classList.contains('hidden')) {
            insp.classList.add('hidden');
            return true;
          }
          return false;
        })();
        """.trimIndent(),
        null
      )
    }
  }

  DisposableEffect(Unit) {
    onDispose {
      webViewRef?.destroy()
    }
  }

  Box(
    modifier = Modifier
      .fillMaxSize()
      .background(ComposeColor(0xFF0A0D10))
  ) {
    AndroidView(
      modifier = Modifier
        .fillMaxSize()
        .testTag("hoi4_game_webview"),
      factory = { context ->
        WebView(context).apply {
          layoutParams = ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT
          )
          setBackgroundColor(Color.parseColor("#0A0D10"))
          setLayerType(View.LAYER_TYPE_HARDWARE, null)
          isVerticalScrollBarEnabled = false
          isHorizontalScrollBarEnabled = false
          overScrollMode = View.OVER_SCROLL_NEVER

          settings.apply {
            javaScriptEnabled = true
            domStorageEnabled = true
            databaseEnabled = true
            allowFileAccess = true
            allowContentAccess = true
            loadWithOverviewMode = true
            useWideViewPort = true
            builtInZoomControls = false
            displayZoomControls = false
            setSupportZoom(false)
            mediaPlaybackRequiresUserGesture = false
            cacheMode = WebSettings.LOAD_DEFAULT
            textZoom = 100
          }

          webChromeClient = WebChromeClient()
          webViewClient = WebViewClient()
          loadUrl("file:///android_asset/index.html")
          webViewRef = this
        }
      },
      update = { view ->
        webViewRef = view
      }
    )
  }
}
