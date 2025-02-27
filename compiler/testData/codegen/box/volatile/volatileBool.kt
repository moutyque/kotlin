// WITH_STDLIB
// IGNORE_BACKEND: WASM, JS
// IGNORE_BACKEND_K1: JS_IR
// !API_VERSION: 1.9

import kotlin.concurrent.*

@OptIn(kotlin.ExperimentalStdlibApi::class)
class BoolWrapper(@Volatile var x: Boolean)

val global = BoolWrapper(false)

fun box() : String {
    val local = BoolWrapper(false)
    if (global.x || local.x) return "FAIL"
    global.x = true
    local.x = true
    return if (global.x && local.x) "OK" else "FAIL"
}