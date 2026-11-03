package com.kintsu.app.wellbeing.domain

import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo

object ShortFormContentDetector {

    const val PKG_YOUTUBE = "com.google.android.youtube"
    const val PKG_INSTAGRAM = "com.instagram.android"
    const val PKG_TIKTOK = "com.zhiliaoapp.musically"
    const val PKG_TIKTOK_LITE = "com.ss.android.ugc.trill"
    const val PKG_FACEBOOK = "com.facebook.katana"
    const val PKG_FACEBOOK_LITE = "com.facebook.lite"
    const val PKG_SNAPCHAT = "com.snapchat.android"
    const val PKG_REDDIT = "com.reddit.frontpage"

    val BROWSER_PACKAGES = setOf(
        "com.android.chrome",
        "org.mozilla.firefox",
        "com.sec.android.app.sbrowser",
        "com.microsoft.emmx",
        "com.opera.browser",
        "com.brave.browser",
        "com.duckduckgo.mobile.android"
    )

    fun isShortFormContent(
        event: AccessibilityEvent,
        rootNode: AccessibilityNodeInfo?,
        packageName: String,
        blockYouTube: Boolean = true,
        blockInstagram: Boolean = true,
        blockTikTok: Boolean = true,
        blockFacebook: Boolean = true,
        blockSnapchat: Boolean = true,
        blockWeb: Boolean = true
    ): Boolean {
        return when (packageName) {
            PKG_TIKTOK, PKG_TIKTOK_LITE -> blockTikTok
            PKG_YOUTUBE -> {
                if (!blockYouTube) return false
                val className = event.className?.toString() ?: ""
                if (className.contains("Shorts", ignoreCase = true) || className.contains("Reel", ignoreCase = true)) {
                    return true
                }
                rootNode?.let { root ->
                    hasMatchingNode(root) { id, text ->
                        id.contains("reel_recycler") ||
                        id.contains("shorts_container") ||
                        id.contains("shorts_player") ||
                        id.contains("reel_player") ||
                        text.equals("Shorts", ignoreCase = true)
                    }
                } ?: false
            }
            PKG_INSTAGRAM -> {
                if (!blockInstagram) return false
                val className = event.className?.toString() ?: ""
                if (className.contains("Clips", ignoreCase = true) || className.contains("Reel", ignoreCase = true)) {
                    return true
                }
                rootNode?.let { root ->
                    hasMatchingNode(root) { id, text ->
                        id.contains("clips_viewer_fragment") ||
                        id.contains("reel_viewer") ||
                        id.contains("clips_video_container") ||
                        id.contains("clips_recycler_view")
                    }
                } ?: false
            }
            PKG_FACEBOOK, PKG_FACEBOOK_LITE -> {
                if (!blockFacebook) return false
                rootNode?.let { root ->
                    hasMatchingNode(root) { id, text ->
                        id.contains("fb_shorts") || id.contains("reels_tray") || id.contains("reel_viewer")
                    }
                } ?: false
            }
            PKG_SNAPCHAT -> {
                if (!blockSnapchat) return false
                rootNode?.let { root ->
                    hasMatchingNode(root) { id, text ->
                        id.contains("spotlight")
                    }
                } ?: false
            }
            else -> {
                if (blockWeb && BROWSER_PACKAGES.contains(packageName)) {
                    val url = extractBrowserUrl(rootNode)
                    isShortFormUrl(url)
                } else false
            }
        }
    }

    fun isShortFormUrl(url: String?): Boolean {
        if (url == null) return false
        val lower = url.lowercase()
        return lower.contains("youtube.com/shorts") ||
               lower.contains("youtu.be/shorts") ||
               lower.contains("instagram.com/reels") ||
               lower.contains("instagram.com/reel") ||
               lower.contains("tiktok.com") ||
               lower.contains("facebook.com/reel") ||
               lower.contains("snapchat.com/spotlight")
    }

    fun extractBrowserUrl(rootNode: AccessibilityNodeInfo?): String? {
        if (rootNode == null) return null
        val knownIds = listOf(
            "com.android.chrome:id/url_bar",
            "com.sec.android.app.sbrowser:id/location_bar_edit_text",
            "org.mozilla.firefox:id/mozac_browser_toolbar_url_view",
            "com.microsoft.emmx:id/url_bar",
            "com.brave.browser:id/url_bar",
            "com.opera.browser:id/url_field"
        )
        for (id in knownIds) {
            val nodes = rootNode.findAccessibilityNodeInfosByViewId(id)
            if (!nodes.isNullOrEmpty()) {
                val text = nodes[0].text?.toString()
                if (!text.isNullOrBlank()) return text
            }
        }
        return findUrlInHierarchy(rootNode, depth = 0)
    }

    private fun findUrlInHierarchy(node: AccessibilityNodeInfo?, depth: Int): String? {
        if (node == null || depth > 8) return null
        val text = node.text?.toString()
        if (!text.isNullOrBlank() && (text.contains("http://") || text.contains("https://") || text.contains(".com") || text.contains(".org") || text.contains(".net"))) {
            return text
        }
        for (i in 0 until node.childCount) {
            val child = node.getChild(i)
            val res = findUrlInHierarchy(child, depth + 1)
            if (res != null) return res
        }
        return null
    }

    private fun hasMatchingNode(
        node: AccessibilityNodeInfo?,
        depth: Int = 0,
        predicate: (viewId: String, text: String) -> Boolean
    ): Boolean {
        if (node == null || depth > 10) return false
        val id = node.viewIdResourceName?.lowercase() ?: ""
        val text = node.text?.toString() ?: ""
        if (predicate(id, text)) return true
        for (i in 0 until node.childCount) {
            val child = node.getChild(i)
            if (hasMatchingNode(child, depth + 1, predicate)) return true
        }
        return false
    }
}
