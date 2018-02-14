package chatt.busbus.frontend.framework

import org.w3c.xhr.XMLHttpRequest


object Http {

    enum class ReadyState(val value: Short) {
        UNSET(0), OPENED(1), HEADERS_RECEIVED(2), LOADING(3), DONE(4);
    }

    private fun sendPayload(url: String, method: String, payload: String, mimeType: String, charset: String,
                            then: (XMLHttpRequest) -> Unit = {}) {

        val req = XMLHttpRequest()

        req.onreadystatechange = {
            if (req.readyState == ReadyState.DONE.value) then(req)
        }

        req.open(method, url, true)
        req.setRequestHeader("Content-Type", "$mimeType; $charset")
        req.send(payload)
    }

    fun put(url: String,
            payload: String,
            mimeType: String = "text/plain",
            charset: String = "UTF-8",
            then: (XMLHttpRequest) -> Unit = {}) {
        sendPayload(url, "PUT", payload, mimeType, charset, then)
    }

    fun post(url: String,
            payload: String,
            mimeType: String = "text/plain",
            charset: String = "UTF-8",
            then: (XMLHttpRequest) -> Unit = {}) {
        sendPayload(url, "POST", payload, mimeType, charset, then)
    }

    fun get(url: String, then: (XMLHttpRequest) -> Unit = {}) {
        val req = XMLHttpRequest()

        req.open("GET", url, true)

        req.onreadystatechange = {
            if (req.readyState == ReadyState.DONE.value) then(req)
        }

        req.send()
    }

    fun delete(url: String, then: (XMLHttpRequest) -> Unit = {}) {
        val req = XMLHttpRequest()

        req.open("DELETE", url, true)
        req.onreadystatechange = {
            if (req.readyState == ReadyState.DONE.value) then(req)
        }

        req.send()
    }

    object Json {

        fun put(url: String, payload: Any, then: (XMLHttpRequest) -> Unit = {}) {
            val json = JSON.stringify(payload)
            Http.put(url, json, mimeType = "application/json", then = then)
        }

        fun post(url: String, payload: Any, then: (XMLHttpRequest) -> Unit = {}) {
            val json = JSON.stringify(payload)
            Http.post(url, json, mimeType = "application/json", then = then)
        }

        fun <T> get(url: String, then: (T) -> Unit = {}) {
            Http.get(url) { req ->
                then(JSON.parse(req.responseText))
            }
        }

        fun delete(url: String, then: (XMLHttpRequest) -> Unit) = Http.delete(url, then)

    }


}