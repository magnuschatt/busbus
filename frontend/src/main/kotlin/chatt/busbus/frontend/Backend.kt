package chatt.busbus.frontend

import chatt.busbus.frontend.framework.Http
import chatt.busbus.shared.Post
import org.w3c.xhr.XMLHttpRequest

object Backend {
    const val BACKEND_BASE_URL = "http://localhost:9000"

    object Posts {
        private const val POST_URL = BACKEND_BASE_URL + "/post"

        fun insertOrReplace(post: Post, then: (XMLHttpRequest) -> Unit = {}) = Http.Json.put(POST_URL, post, then)
        fun fetchOneById(id: String, then: (Post) -> Unit = {}) = Http.Json.get(POST_URL + "/$id", then)
        fun fetchAll(then: (Array<Post>) -> Unit = {}) = Http.Json.get(POST_URL, then)
    }

}
