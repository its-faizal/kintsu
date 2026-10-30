package com.kintsu.app.wellbeing.domain

import com.kintsu.app.data.model.BlockedWebsiteEntity
import java.net.URI

object WebsiteFilterManager {

    /**
     * Normalizes a raw user input into a clean lowercase domain/host.
     * e.g. "https://www.tiktok.com/explore" -> "www.tiktok.com"
     * "instagram.com/" -> "instagram.com"
     * "*.reddit.com" -> "reddit.com" (with matchSubdomains)
     */
    fun cleanDomainInput(raw: String): Pair<String, Boolean> {
        var str = raw.trim().lowercase()
        var matchSubdomains = true

        if (str.startsWith("*.")) {
            str = str.removePrefix("*.")
            matchSubdomains = true
        }

        if (str.startsWith("http://") || str.startsWith("https://")) {
            try {
                val uri = URI(str)
                str = uri.host ?: str
            } catch (_: Exception) {
                str = str.replace("http://", "").replace("https://", "")
            }
        }

        val slashIdx = str.indexOf('/')
        if (slashIdx != -1) {
            str = str.substring(0, slashIdx)
        }

        val colonIdx = str.indexOf(':')
        if (colonIdx != -1) {
            str = str.substring(0, colonIdx)
        }

        return str.trim() to matchSubdomains
    }

    /**
     * Checks if a queried hostname matches any of the active blocked website rules.
     */
    fun isHostBlocked(host: String, activeRules: List<BlockedWebsiteEntity>): BlockedWebsiteEntity? {
        val queryHost = host.trim().lowercase()
        for (rule in activeRules) {
            if (!rule.isEnabled) continue
            val target = rule.domain.trim().lowercase().removePrefix("*.")

            if (queryHost == target) {
                return rule
            }

            if (rule.matchSubdomains && queryHost.endsWith(".$target")) {
                return rule
            }
        }
        return null
    }
}
