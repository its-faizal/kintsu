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
        "org.mozilla.focus",
        "com.sec.android.app.sbrowser",
        "com.microsoft.emmx",
        "com.opera.browser",
        "com.opera.mini.native",
        "com.opera.gx",
        "com.brave.browser",
        "com.duckduckgo.mobile.android",
        "com.kiwibrowser.browser",
        "com.vivaldi.browser",
        "mark.via.gp",
        "org.torproject.torbrowser",
        "com.cloudmosa.puffinFree",
        "com.ecosia.android",
        "com.google.android.apps.searchlite",
        "com.google.android.googlequicksearchbox"
    )

    fun isYouTubeLikePackage(pkgName: String): Boolean {
        val lower = pkgName.lowercase()
        return lower == PKG_YOUTUBE ||
                lower.contains("youtube") ||
                lower.contains("revanced") ||
                lower.contains("vanced") ||
                lower.contains("newpipe") ||
                lower.contains("libretube") ||
                lower.contains("skytube") ||
                lower.contains("tubular") ||
                lower.contains("songtube") ||
                lower.contains("piped")
    }

    fun isShortFormPackage(pkgName: String): Boolean {
        val lower = pkgName.lowercase()
        return lower.contains("instagram") || lower.contains("youtube") ||
                lower.contains("tiktok") || lower.contains("facebook") ||
                lower.contains("snapchat") || isYouTubeLikePackage(lower)
    }

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
        if (isYouTubeLikePackage(packageName)) {
            if (!blockYouTube) return false
            val className = event.className?.toString() ?: ""
            if (className.contains("Shorts", ignoreCase = true) || className.contains("Reel", ignoreCase = true)) {
                return true
            }
            return rootNode?.let { root ->
                hasMatchingNode(root) { id, text, desc ->
                    id.contains("reel") ||
                    id.contains("shorts") ||
                    id.contains("short_video") ||
                    id.contains("reel_player") ||
                    id.contains("shorts_player") ||
                    id.contains("shorts_container") ||
                    id.contains("feed_shorts") ||
                    id.contains("piped_shorts") ||
                    desc.contains("Shorts", ignoreCase = true) ||
                    desc.contains("Short video", ignoreCase = true) ||
                    text.equals("Shorts", ignoreCase = true) ||
                    text.contains("#shorts", ignoreCase = true)
                }
            } ?: false
        }

        return when (packageName) {
            PKG_TIKTOK, PKG_TIKTOK_LITE -> blockTikTok
            PKG_INSTAGRAM -> {
                if (!blockInstagram) return false
                val className = event.className?.toString() ?: ""
                if (className.contains("Clips", ignoreCase = true) || className.contains("Reel", ignoreCase = true)) {
                    return true
                }
                rootNode?.let { root ->
                    hasMatchingNode(root) { id, text, desc ->
                        id.contains("clips") ||
                        id.contains("reel") ||
                        desc.contains("Reel", ignoreCase = true) ||
                        desc.contains("Short video", ignoreCase = true) ||
                        text.contains("Reels", ignoreCase = true)
                    }
                } ?: false
            }
            PKG_FACEBOOK, PKG_FACEBOOK_LITE -> {
                if (!blockFacebook) return false
                rootNode?.let { root ->
                    hasMatchingNode(root) { id, text, desc ->
                        id.contains("fb_shorts") || id.contains("reels_tray") || id.contains("reel_viewer") ||
                        desc.contains("Reel", ignoreCase = true) || text.contains("Reels", ignoreCase = true)
                    }
                } ?: false
            }
            PKG_SNAPCHAT -> {
                if (!blockSnapchat) return false
                rootNode?.let { root ->
                    hasMatchingNode(root) { id, text, desc ->
                        id.contains("spotlight") || desc.contains("Spotlight", ignoreCase = true) || text.contains("Spotlight", ignoreCase = true)
                    }
                } ?: false
            }
            else -> {
                if (blockWeb && BROWSER_PACKAGES.contains(packageName)) {
                    val url = extractBrowserUrl(rootNode)
                    isShortFormUrl(url)
                } else {
                    val className = event.className?.toString() ?: ""
                    if (className.contains("Shorts", ignoreCase = true) || className.contains("Reel", ignoreCase = true) || className.contains("Clips", ignoreCase = true)) {
                        return true
                    }
                    rootNode?.let { root ->
                        hasMatchingNode(root) { id, text, desc ->
                            (id.contains("shorts_player") || id.contains("reel_viewer") || id.contains("shorts_container")) ||
                            ((id.contains("shorts") || id.contains("reel")) && (desc.contains("Shorts", ignoreCase = true) || text.equals("Shorts", ignoreCase = true)))
                        }
                    } ?: false
                }
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
            "org.mozilla.firefox:id/toolbar",
            "com.microsoft.emmx:id/url_bar",
            "com.brave.browser:id/url_bar",
            "com.opera.browser:id/url_field",
            "com.opera.mini.native:id/url_field",
            "com.duckduckgo.mobile.android:id/omnibarTextInput",
            "com.kiwibrowser.browser:id/url_bar",
            "com.vivaldi.browser:id/url_bar",
            "mark.via.gp:id/address_bar",
            "com.google.android.googlequicksearchbox:id/googleapp_search_box"
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
        predicate: (viewId: String, text: String, desc: String) -> Boolean
    ): Boolean {
        if (node == null || depth > 12) return false
        val id = node.viewIdResourceName?.lowercase() ?: ""
        val text = node.text?.toString() ?: ""
        val desc = node.contentDescription?.toString() ?: ""
        if (predicate(id, text, desc)) return true
        for (i in 0 until node.childCount) {
            val child = node.getChild(i)
            if (hasMatchingNode(child, depth + 1, predicate)) return true
        }
        return false
    }
}
