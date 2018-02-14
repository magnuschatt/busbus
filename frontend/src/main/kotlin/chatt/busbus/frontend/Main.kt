@file:Suppress("unused")

package chatt.busbus.frontend

import chatt.busbus.frontend.framework.Pages
import chatt.busbus.frontend.page.createPost
import chatt.busbus.frontend.page.index
import chatt.busbus.frontend.page.viewPost

fun main(args: Array<String>) {

    Pages.register(index)
    Pages.register(createPost)
    Pages.register(viewPost)

    Pages.renderCurrent()

}

