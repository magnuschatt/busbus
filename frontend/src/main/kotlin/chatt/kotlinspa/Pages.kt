package chatt.kotlinspa

import kotlinx.html.dom.append
import kotlinx.html.dom.create
import kotlinx.html.h1
import kotlinx.html.h3
import kotlinx.html.js.div
import org.w3c.dom.HTMLDivElement
import kotlin.browser.document
import kotlin.browser.window

object Pages {

    val registeredUrls: Set<String>
        get() = map.keys

    private val currentPath: String
        get() = Html.currentUrl.fixed()

    private val map = mutableMapOf<String, HTMLDivElement.() -> Unit>()
    private var currentDiv: HTMLDivElement? = null
    private val pageNotFound: HTMLDivElement.() -> Unit = {
        append {
            h1 { +"404" }
            h3 { +"Page not found: $currentPath" }
        }
    }

    init {
        window.onpopstate = { renderCurrent() }
    }

    private fun String.fixed() = this
            .substringAfter("://")
            .substringAfter("/")
            .substringBefore("?")

    private fun Map<String, String>.toQueryString(): String {
        return if (isEmpty()) ""
        else "?" + entries.joinToString { (key, value) -> "$key=$value" }
    }

    fun register(page: Page) {
        validatePageUrl(page.url)
        map[page.url.fixed()] = page.builder
    }

    private fun validatePageUrl(url: String) {
        if (url.contains('?')) throw IllegalArgumentException("Query component not allowed in url: $url")
        if (!url.startsWith("/")) throw IllegalArgumentException("URL must start with a '/', but was '$url'")
        if (url in registeredUrls) throw IllegalStateException("URL registered twice: '$url'")
    }

    fun switchTo(page: Page, queryParams: Map<String, String> = emptyMap()) {
        val url = page.url + queryParams.toQueryString()
        if (url == currentPath) return
        window.history.pushState(null, url, url)
        renderCurrent()
    }

    fun renderCurrent() {
        currentDiv = document.create.div().also {
            it.apply(map[currentPath] ?: pageNotFound)
            currentDiv?.let { cd -> cd .parentNode?.removeChild(cd) }
            Html.body().appendChild(it)
        }
    }

}