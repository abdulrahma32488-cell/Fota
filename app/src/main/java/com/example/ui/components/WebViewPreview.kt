package com.example.ui.components

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.util.Base64
import android.view.ViewGroup
import android.webkit.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Computer
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Tablet
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.model.ProjectFile
import com.example.ui.theme.FlotaGreenDark
import com.example.ui.viewmodel.ViewportMode

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun WebViewPreview(
    files: List<ProjectFile>,
    viewportMode: ViewportMode,
    onViewportChange: (ViewportMode) -> Unit,
    modifier: Modifier = Modifier
) {
    var webViewInstance by remember { mutableStateOf<WebView?>(null) }
    var isLoading by remember { mutableStateOf(false) }
    var hasError by remember { mutableStateOf(false) }

    // Bundle HTML + CSS + JS into complete runnable document
    val htmlBundle = remember(files) {
        val html = files.find { it.path == "index.html" }?.content ?: "<h1>No index.html</h1>"
        val css = files.find { it.path == "styles.css" }?.content ?: ""
        val js = files.find { it.path == "app.js" }?.content ?: ""

        var integratedHtml = html
        if (css.isNotBlank()) {
            val styleTag = "<style>\n$css\n</style>"
            integratedHtml = if (integratedHtml.contains("</head>")) {
                integratedHtml.replace("</head>", "$styleTag\n</head>")
            } else {
                "$styleTag\n$integratedHtml"
            }
        }

        if (js.isNotBlank()) {
            val scriptTag = "<script>\n$js\n</script>"
            integratedHtml = if (integratedHtml.contains("</body>")) {
                integratedHtml.replace("</body>", "$scriptTag\n</body>")
            } else {
                "$integratedHtml\n$scriptTag"
            }
        }

        integratedHtml
    }

    Column(modifier = modifier.fillMaxSize()) {
        // Top Toolbar: Viewport Mode Switcher & Reload Button
        Surface(
            tonalElevation = 1.dp,
            border = null,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Viewport selector
                Row(
                    modifier = Modifier
                        .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(8.dp))
                        .padding(2.dp),
                    horizontalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    IconButton(
                        onClick = { onViewportChange(ViewportMode.MOBILE) },
                        modifier = Modifier
                            .size(36.dp)
                            .background(
                                if (viewportMode == ViewportMode.MOBILE) MaterialTheme.colorScheme.surface else Color.Transparent,
                                RoundedCornerShape(6.dp)
                            )
                            .testTag("viewport_mobile_btn")
                    ) {
                        Icon(
                            Icons.Default.PhoneAndroid,
                            contentDescription = "Mobile Viewport",
                            tint = if (viewportMode == ViewportMode.MOBILE) FlotaGreenDark else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    IconButton(
                        onClick = { onViewportChange(ViewportMode.TABLET) },
                        modifier = Modifier
                            .size(36.dp)
                            .background(
                                if (viewportMode == ViewportMode.TABLET) MaterialTheme.colorScheme.surface else Color.Transparent,
                                RoundedCornerShape(6.dp)
                            )
                            .testTag("viewport_tablet_btn")
                    ) {
                        Icon(
                            Icons.Default.Tablet,
                            contentDescription = "Tablet Viewport",
                            tint = if (viewportMode == ViewportMode.TABLET) FlotaGreenDark else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    IconButton(
                        onClick = { onViewportChange(ViewportMode.DESKTOP) },
                        modifier = Modifier
                            .size(36.dp)
                            .background(
                                if (viewportMode == ViewportMode.DESKTOP) MaterialTheme.colorScheme.surface else Color.Transparent,
                                RoundedCornerShape(6.dp)
                            )
                            .testTag("viewport_desktop_btn")
                    ) {
                        Icon(
                            Icons.Default.Computer,
                            contentDescription = "Desktop Viewport",
                            tint = if (viewportMode == ViewportMode.DESKTOP) FlotaGreenDark else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                // Viewport resolution label
                Text(
                    text = when (viewportMode) {
                        ViewportMode.MOBILE -> "Mobile: 375px"
                        ViewportMode.TABLET -> "Tablet: 768px"
                        ViewportMode.DESKTOP -> "Desktop: Fluid 100%"
                    },
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                // Refresh Button
                IconButton(
                    onClick = {
                        webViewInstance?.reload()
                    },
                    modifier = Modifier.testTag("refresh_preview_btn")
                ) {
                    Icon(
                        Icons.Default.Refresh,
                        contentDescription = "Reload Preview",
                        tint = FlotaGreenDark
                    )
                }
            }
        }

        if (isLoading) {
            LinearProgressIndicator(
                modifier = Modifier.fillMaxWidth(),
                color = FlotaGreenDark
            )
        }

        // Viewport Frame Container
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFF1F5F9))
                .padding(if (viewportMode != ViewportMode.DESKTOP) 12.dp else 0.dp),
            contentAlignment = Alignment.TopCenter
        ) {
            val frameModifier = when (viewportMode) {
                ViewportMode.MOBILE -> Modifier
                    .width(360.dp)
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(16.dp))
                    .border(2.dp, Color(0xFFCBD5E1), RoundedCornerShape(16.dp))
                ViewportMode.TABLET -> Modifier
                    .width(580.dp)
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(12.dp))
                    .border(2.dp, Color(0xFFCBD5E1), RoundedCornerShape(12.dp))
                ViewportMode.DESKTOP -> Modifier.fillMaxSize()
            }

            AndroidView(
                factory = { ctx ->
                    WebView(ctx).apply {
                        layoutParams = ViewGroup.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.MATCH_PARENT
                        )
                        settings.apply {
                            javaScriptEnabled = true
                            domStorageEnabled = true
                            loadWithOverviewMode = true
                            useWideViewPort = true
                            builtInZoomControls = true
                            displayZoomControls = false
                            allowFileAccess = false
                            allowContentAccess = false
                        }

                        webViewClient = object : WebViewClient() {
                            override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                                isLoading = true
                                hasError = false
                            }

                            override fun onPageFinished(view: WebView?, url: String?) {
                                isLoading = false
                            }

                            override fun onReceivedError(
                                view: WebView?,
                                request: WebResourceRequest?,
                                error: WebResourceError?
                            ) {
                                isLoading = false
                                hasError = true
                            }
                        }

                        webChromeClient = object : WebChromeClient() {
                            override fun onJsAlert(
                                view: WebView?,
                                url: String?,
                                message: String?,
                                result: JsResult?
                            ): Boolean {
                                // Allow alert dialogs in preview
                                result?.confirm()
                                return false
                            }
                        }

                        val encodedHtml = Base64.encodeToString(htmlBundle.toByteArray(Charsets.UTF_8), Base64.NO_PADDING)
                        loadData(encodedHtml, "text/html; charset=utf-8", "base64")
                        webViewInstance = this
                    }
                },
                update = { wv ->
                    val encodedHtml = Base64.encodeToString(htmlBundle.toByteArray(Charsets.UTF_8), Base64.NO_PADDING)
                    wv.loadData(encodedHtml, "text/html; charset=utf-8", "base64")
                },
                modifier = frameModifier.testTag("webview_preview")
            )
        }
    }
}
