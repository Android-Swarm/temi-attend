package com.zetzaus.temiattend.temperature

import java.io.InputStream
import java.io.OutputStream
import java.net.InetSocketAddress
import java.net.Socket

/**
 * Exposes a [Socket], [InputStream], and [OutputStream] to the lambda. All of these is called by
 * the [use] extension function, hence, they will be closed appropriately regardless of what happened.
 *
 * @param ip The IP address where the [Socket] should connect to.
 * @param port The port number where the [Socket] should connect to.
 * @param block The operations to be done.
 */
inline fun withSocketOperation(
    ip: String,
    port: Int,
    block: (Socket, OutputStream, InputStream) -> Unit
) {
    Socket().apply { connect(InetSocketAddress(ip, port), 10 * 1000) }.use { socket ->
        socket.soTimeout = 10 * 1000
        
        socket.getOutputStream().use { writer ->
            socket.getInputStream().use { out ->
                block(socket, writer, out)
            }
        }
    }
}