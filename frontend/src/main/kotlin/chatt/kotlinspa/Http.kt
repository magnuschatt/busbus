package chatt.kotlinspa

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


}