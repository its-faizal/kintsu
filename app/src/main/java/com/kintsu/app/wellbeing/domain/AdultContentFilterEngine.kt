package com.kintsu.app.wellbeing.domain

import java.net.URI
import java.net.URLDecoder

object AdultContentFilterEngine {

    enum class AdultBlockReason {
        KNOWN_ADULT_DOMAIN,
        ADULT_KEYWORD_MATCH,
        ADULT_SEARCH_QUERY,
        ADULT_TLD
    }

    data class FilterResult(
        val isBlocked: Boolean,
        val reason: AdultBlockReason? = null,
        val matchedTerm: String? = null
    )

    // Curated high-impact adult, porn, cam, escort, and hentai domains
    private val KNOWN_ADULT_DOMAINS = hashSetOf(
        // Major Porn Tubes
        "pornhub.com", "pornhubpremium.com", "xvideos.com", "xvideos2.com", "xvideos3.com",
        "xnxx.com", "xnxx2.com", "xnxx.tv", "xhamster.com", "xhamster.desi", "xhamster.one",
        "redtube.com", "youporn.com", "tube8.com", "spankbang.com", "spankbang.party",
        "brazzers.com", "bangbros.com", "naughtyamerica.com", "tnaflix.com", "beeg.com",
        "thumbzilla.com", "daftsex.com", "fapster.xxx", "txxx.com", "vporn.com",
        "eporner.com", "hqporner.com", "javbus.com", "missav.com", "javdb.com", "jable.tv",
        "javtrailers.com", "pronhub.com", "pornhat.com", "hdporn.net", "realitykings.com",
        "digitalplayground.com", "twistys.com", "babes.com", "evilangel.com", "kink.com",
        "wicked.com", "metart.com", "erome.com", "heavy-r.com", "motherless.com",
        "xcafe.com", "porn.com", "tube8.es", "porngo.com", "sunporno.com", "porn555.com",
        "daftporn.com", "drfap.com", "pornpics.com", "pornhd.com", "porn300.com",
        "palimas.com", "nuvid.com", "cliphunter.com", "empflix.com", "anyporn.com",
        "gotporn.com", "ixxx.com", "pornone.com", "alphaporno.com", "pornburst.xxx",

        // Live Cams & Seductive Streaming
        "chaturbate.com", "stripchat.com", "cam4.com", "livejasmin.com", "bongacams.com",
        "myfreecams.com", "camsoda.com", "jerkmate.com", "adultwork.com", "streamate.com",
        "flirt4free.com", "imlive.com", "cammodels.com", "babestation.tv", "cams.com",
        "sexcam.com", "freecam.com", "camster.com", "camzap.com", "webcamdarts.com",

        // Adult Creator & Subscription Platforms
        "onlyfans.com", "fansly.com", "loyalfans.com", "manyvids.com", "pocketstars.com",
        "justforfans.com", "mym.fans", "fanvue.com", "admire.me", "fancentro.com",

        // Hentai, Adult Anime, NSFW Art & Games
        "nhentai.net", "hanime.tv", "rule34.xxx", "rule34.paheal.net", "e-hentai.org",
        "exhentai.org", "gelbooru.com", "danbooru.donmai.us", "f95zone.to", "tbib.org",
        "sankakucomplex.com", "hitomi.la", "pururin.io", "hentaihaven.xxx", "hentaihaven.red",
        "hentai2read.com", "hentaifox.com", "luscious.net", "eroticmv.com", "lushstories.com",
        "literotica.com", "asmhentai.com", "simply-hentai.com", "tsumino.com", "fakku.net",

        // Adult Dating & Escort Portals
        "ashley-madison.com", "ashleymadison.com", "adultfriendfinder.com", "seeking.com",
        "bedpage.com", "skipsearch.com", "tryst.link", "eros.com", "rubratings.com",
        "eurogirlsescort.com", "adultsearch.com", "cityvibe.com", "redbook.com",
        "escortalligator.com", "listcrawler.com", "skipthegames.com"
    )

    // High-risk keywords that denote adult / explicit / seductive content
    private val ADULT_KEYWORDS = listOf(
        "pornhub", "xvideos", "xnxx", "xhamster", "spankbang", "brazzers", "chaturbate",
        "stripchat", "onlyfans", "fansly", "nhentai", "rule34", "hanime", "hentai",
        "porn", "xxx", "adult video", "adult movie", "adult film",
        "erotic", "nsfw video", "seductive video", "intimacy video", "camgirl", "webcam girl",
        "naked girl", "naked woman", "nude video", "boobs video", "milf video",
        "anal sex", "blowjob", "masturbat", "fetish video", "bdsm sex", "stripper video",
        "leaked sex", "sex tape", "sex video", "hardcore sex", "softcore sex",
        "gangbang", "creampie", "threesome sex", "incest sex", "escort service",
        "dildo video", "vagina video", "penis video", "taboo sex", "swinger party"
    )

    private val ADULT_TLDS = setOf(".xxx", ".porn", ".adult", ".sex")

    /**
     * Checks if a domain or hostname is blocked as adult content.
     */
    fun isDomainBlocked(host: String): FilterResult {
        val clean = host.trim().lowercase().removePrefix("www.").removePrefix("m.")
        
        // Check TLD
        for (tld in ADULT_TLDS) {
            if (clean.endsWith(tld)) {
                return FilterResult(true, AdultBlockReason.ADULT_TLD, tld)
            }
        }

        // Direct match or subdomain match
        if (KNOWN_ADULT_DOMAINS.contains(clean)) {
            return FilterResult(true, AdultBlockReason.KNOWN_ADULT_DOMAIN, clean)
        }

        for (known in KNOWN_ADULT_DOMAINS) {
            if (clean == known || clean.endsWith(".$known")) {
                return FilterResult(true, AdultBlockReason.KNOWN_ADULT_DOMAIN, known)
            }
        }

        // Keyword in host (e.g. sex-videos-live.net, pornclips.cc)
        val hostKeywords = listOf("porn", "xxx", "hentai", "sex", "camgirl", "stripchat", "nude", "milf", "brazzers")
        for (kw in hostKeywords) {
            if (clean.contains(kw)) {
                return FilterResult(true, AdultBlockReason.ADULT_KEYWORD_MATCH, kw)
            }
        }

        return FilterResult(false)
    }

    /**
     * Inspects a full URL or search query for adult content.
     */
    fun isUrlOrQueryBlocked(rawUrlOrQuery: String): FilterResult {
        if (rawUrlOrQuery.isBlank()) return FilterResult(false)

        val text = rawUrlOrQuery.trim().lowercase()

        // 1. Try URI host check
        try {
            val uri = if (text.startsWith("http://") || text.startsWith("https://")) {
                URI(text)
            } else {
                URI("https://$text")
            }
            val host = uri.host
            if (!host.isNullOrBlank()) {
                val domainRes = isDomainBlocked(host)
                if (domainRes.isBlocked) return domainRes
            }
        } catch (_: Exception) {}

        // 2. Decode URL query parameters if present
        val decodedText = try {
            URLDecoder.decode(text, "UTF-8")
        } catch (_: Exception) {
            text
        }

        // 3. Search query / URL path keyword matching
        for (keyword in ADULT_KEYWORDS) {
            if (containsWordOrPhrase(decodedText, keyword)) {
                val reason = if (isSearchEngineQuery(decodedText)) {
                    AdultBlockReason.ADULT_SEARCH_QUERY
                } else {
                    AdultBlockReason.ADULT_KEYWORD_MATCH
                }
                return FilterResult(true, reason, keyword)
            }
        }

        return FilterResult(false)
    }

    private fun isSearchEngineQuery(text: String): Boolean {
        return text.contains("google.com/search") ||
                text.contains("bing.com/search") ||
                text.contains("duckduckgo.com/?q=") ||
                text.contains("search.yahoo.com") ||
                text.contains("youtube.com/results")
    }

    private fun containsWordOrPhrase(source: String, phrase: String): Boolean {
        val idx = source.indexOf(phrase)
        if (idx == -1) return false

        // Boundary checks to avoid false positives on words like "sussex" or "essex" for "sex"
        if (phrase == "sex") {
            val before = if (idx > 0) source[idx - 1] else ' '
            val after = if (idx + 3 < source.length) source[idx + 3] else ' '
            val isBoundaryBefore = !before.isLetterOrDigit()
            val isBoundaryAfter = !after.isLetterOrDigit()
            return isBoundaryBefore && isBoundaryAfter
        }

        return true
    }
}
