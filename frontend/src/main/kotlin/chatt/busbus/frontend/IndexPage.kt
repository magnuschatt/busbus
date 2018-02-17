package chatt.busbus.frontend

import chatt.kotlinspa.Page
import chatt.kotlinspa.Pages
import kotlinx.html.dom.append
import kotlinx.html.js.button
import kotlinx.html.js.h1
import kotlinx.html.js.onClickFunction

val index: Page = Page.create("/") {
    append {
        button {
            +"Home"
            onClickFunction = { Pages.renderCurrent() }
        }

        h1 {
            +"Index"
        }



    }

}