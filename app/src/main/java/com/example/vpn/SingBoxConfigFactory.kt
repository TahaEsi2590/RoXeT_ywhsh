package com.example.vpn

import android.net.Uri
import android.util.Base64
import org.json.JSONArray
import org.json.JSONObject

/** Converts the URI/raw configuration stored by the existing admin panel into a sing-box JSON profile. */
object SingBoxConfigFactory {
    fun build(raw: String, dns: String = "1.1.1.1"): String {
        val trimmed = raw.trim()
        val outbound = if (trimmed.startsWith("{")) {
            val source = JSONObject(trimmed)
            if (source.has("outbounds")) return withTun(source, dns).toString()
            source
        } else {
            parseUri(trimmed)
        }

        val outbounds = JSONArray().put(outbound.put("tag", "proxy")).put(JSONObject().put("type", "direct").put("tag", "direct"))
        return JSONObject()
            .put("log", JSONObject().put("level", "warn"))
            .put("dns", JSONObject()
                .put("servers", JSONArray().put(JSONObject().put("tag", "dns").put("address", dns)))
                .put("final", "dns"))
            .put("inbounds", JSONArray().put(JSONObject()
                .put("type", "tun")
                .put("interface_name", "reno-tun")
                .put("address", JSONArray().put("172.19.0.1/30"))
                .put("auto_route", true)
                .put("strict_route", false)
                .put("stack", "system")
                .put("sniff", true)))
            .put("outbounds", outbounds)
            .put("route", JSONObject().put("auto_detect_interface", true).put("final", "proxy"))
            .toString()
    }

    private fun withTun(source: JSONObject, dns: String): JSONObject {
        val result = JSONObject(source.toString())
        if (!result.has("dns")) result.put("dns", JSONObject()
            .put("servers", JSONArray().put(JSONObject().put("tag", "dns").put("address", dns)))
            .put("final", "dns"))
        if (!result.has("inbounds")) result.put("inbounds", JSONArray().put(JSONObject()
            .put("type", "tun").put("interface_name", "reno-tun")
            .put("address", JSONArray().put("172.19.0.1/30"))
            .put("auto_route", true).put("strict_route", false).put("stack", "system")))
        if (!result.has("route")) result.put("route", JSONObject().put("auto_detect_interface", true))
        return result
    }

    private fun parseUri(value: String): JSONObject {
        val uri = Uri.parse(value)
        return when (uri.scheme?.lowercase()) {
            "vless" -> vless(uri)
            "vmess" -> vmess(value)
            "trojan" -> trojan(uri)
            "ss" -> shadowsocks(uri, value)
            "hysteria2", "hy2" -> hysteria2(uri)
            else -> error("Unsupported proxy format: ${uri.scheme}")
        }
    }

    private fun vless(uri: Uri): JSONObject {
        val o = JSONObject()
            .put("type", "vless")
            .put("server", uri.host ?: error("VLESS server missing"))
            .put("server_port", uri.port.takeIf { it > 0 } ?: 443)
            .put("uuid", uri.userInfo ?: error("VLESS UUID missing"))
        val q = uri.queryParameterNames.associateWith { uri.getQueryParameter(it).orEmpty() }
        if (q["flow"].orEmpty().isNotBlank()) o.put("flow", q["flow"])
        addTls(o, q)
        addTransport(o, q)
        return o
    }

    private fun vmess(value: String): JSONObject {
        val encoded = value.substringAfter("vmess://")
        val decoded = Base64.decode(encoded.replace("-", "+").replace("_", "/"), Base64.DEFAULT)
            .toString(Charsets.UTF_8)
        val j = JSONObject(decoded)
        val o = JSONObject()
            .put("type", "vmess")
            .put("server", j.optString("add"))
            .put("server_port", j.optInt("port", 443))
            .put("uuid", j.optString("id"))
            .put("security", j.optString("scy", "auto"))
        if (j.optInt("aid", 0) != 0) o.put("alter_id", j.optInt("aid"))
        if (j.optString("tls").equals("tls", true)) {
            o.put("tls", JSONObject().put("enabled", true)
                .put("server_name", j.optString("sni", j.optString("host"))))
        }
        if (j.optString("net").isNotBlank() && j.optString("net") != "tcp") {
            val t = JSONObject().put("type", j.optString("net"))
            if (j.optString("path").isNotBlank()) t.put("path", j.optString("path"))
            if (j.optString("host").isNotBlank()) t.put("headers", JSONObject().put("Host", j.optString("host")))
            o.put("transport", t)
        }
        return o
    }

    private fun trojan(uri: Uri): JSONObject {
        val q = uri.queryParameterNames.associateWith { uri.getQueryParameter(it).orEmpty() }
        return JSONObject()
            .put("type", "trojan")
            .put("server", uri.host ?: error("Trojan server missing"))
            .put("server_port", uri.port.takeIf { it > 0 } ?: 443)
            .put("password", uri.userInfo ?: error("Trojan password missing"))
            .put("tls", JSONObject().put("enabled", true).put("server_name", q["sni"].orEmpty().ifBlank { uri.host }))
    }

    private fun shadowsocks(uri: Uri, raw: String): JSONObject {
        var user = uri.userInfo.orEmpty()
        var host = uri.host
        var port = uri.port
        if (user.isBlank()) {
            val body = raw.substringAfter("ss://").substringBefore('#')
            val decoded = Base64.decode(body.substringBefore('@'), Base64.DEFAULT).toString(Charsets.UTF_8)
            user = decoded
            host = body.substringAfter('@').substringBefore(':')
            port = body.substringAfter(':').toIntOrNull() ?: 443
        }
        val method = user.substringBefore(':', "")
        val password = user.substringAfter(':', "")
        return JSONObject().put("type", "shadowsocks")
            .put("server", host ?: error("Shadowsocks server missing"))
            .put("server_port", port.takeIf { it > 0 } ?: 443)
            .put("method", method)
            .put("password", password)
    }

    private fun hysteria2(uri: Uri): JSONObject {
        val q = uri.queryParameterNames.associateWith { uri.getQueryParameter(it).orEmpty() }
        val o = JSONObject().put("type", "hysteria2")
            .put("server", uri.host ?: error("Hysteria2 server missing"))
            .put("server_port", uri.port.takeIf { it > 0 } ?: 443)
            .put("password", uri.userInfo ?: "")
        val tls = JSONObject().put("enabled", true)
        if (q["sni"].orEmpty().isNotBlank()) tls.put("server_name", q["sni"])
        o.put("tls", tls)
        if (q["obfs"].orEmpty().isNotBlank()) {
            o.put("obfs", JSONObject().put("type", q["obfs"]).put("password", q["obfs-password"].orEmpty()))
        }
        return o
    }

    private fun addTls(o: JSONObject, q: Map<String, String>) {
        val security = q["security"].orEmpty().lowercase()
        if (security == "tls" || q["sni"].orEmpty().isNotBlank() || q["fp"].orEmpty().isNotBlank()) {
            val tls = JSONObject().put("enabled", true)
            q["sni"]?.takeIf { it.isNotBlank() }?.let { tls.put("server_name", it) }
            q["fp"]?.takeIf { it.isNotBlank() }?.let { tls.put("utls", JSONObject().put("enabled", true).put("fingerprint", it)) }
            o.put("tls", tls)
        }
    }

    private fun addTransport(o: JSONObject, q: Map<String, String>) {
        val type = q["type"].orEmpty().lowercase()
        if (type.isBlank() || type == "tcp") return
        val t = JSONObject().put("type", when (type) { "ws" -> "ws"; "grpc" -> "grpc"; "httpupgrade" -> "httpupgrade"; "splithttp", "xhttp" -> "http"; else -> type })
        q["path"]?.takeIf { it.isNotBlank() }?.let { t.put("path", it) }
        q["host"]?.takeIf { it.isNotBlank() }?.let { t.put("headers", JSONObject().put("Host", it)) }
        q["serviceName"]?.takeIf { it.isNotBlank() }?.let { t.put("service_name", it) }
        o.put("transport", t)
    }
}
