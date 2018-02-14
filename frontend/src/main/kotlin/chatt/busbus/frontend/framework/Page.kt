package chatt.busbus.frontend.framework

import org.w3c.dom.HTMLDivElement

interface Page {
    val url: String
    val builder: HTMLDivElement.() -> Unit

    companion object {
        fun create(url: String, builder: HTMLDivElement.() -> Unit): Page = object : Page {
            override val url: String = url
            override val builder: HTMLDivElement.() -> Unit = builder
        }
    }

}