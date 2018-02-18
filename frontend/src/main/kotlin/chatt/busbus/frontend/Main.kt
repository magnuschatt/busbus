@file:Suppress("unused")

package chatt.busbus.frontend

import chatt.kotlinspa.Pages

fun main(args: Array<String>) {
    Pages.register(departuresPage)
    Pages.renderCurrent()
}

