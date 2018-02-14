package chatt.busbus.frontend.page

import chatt.busbus.frontend.Backend
import chatt.busbus.frontend.framework.Html
import chatt.busbus.frontend.framework.Page
import chatt.busbus.frontend.framework.Pages
import kotlinx.html.dom.append
import kotlinx.html.js.*

val viewPost: Page = Page.create("/post/view") {
    append {
        button {
            +"Home"
            onClickFunction = { Pages.switchTo(index) }
        }

        h1 {
            +"View Post"
        }
    }

    val id = Html.queryParams["id"]!!
    Backend.Posts.fetchOneById(id) { post ->
        append {
            div(classes = "whitebox") {
                p { b { +post.title } }
                p { +post.content }
            }
        }
    }

}