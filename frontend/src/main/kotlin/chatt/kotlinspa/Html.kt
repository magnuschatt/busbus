package chatt.kotlinspa

import org.w3c.dom.HTMLBodyElement
import org.w3c.dom.HTMLHeadElement
import kotlin.browser.window
import kotlin.browser.document as doc

object Html {

    val currentUrl: String get() = window.location.href

    val queryParams: Map<String, String?> get() {
        val result = mutableMapOf<String, String?>()
        val parts = currentUrl.substringAfter("?").split("&").filterNot { it.isBlank() }
        parts.forEach { part ->
            when (part.count { it == '=' }) {
                0 -> result[part] = null
                1 -> result[part.substringBefore("=")] = part.substringAfter("=")
            }
        }
        return result
    }

    fun head(): HTMLHeadElement
            = doc.head ?: throw IllegalStateException("Missing <head> element")

    fun body(): HTMLBodyElement {
        val temp = (doc.body ?: doc.createElement("body")) as HTMLBodyElement
        doc.body = temp
        return temp
    }

}