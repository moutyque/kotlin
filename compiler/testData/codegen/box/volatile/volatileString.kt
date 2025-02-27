// WITH_STDLIB
// IGNORE_BACKEND: WASM, JS
// IGNORE_BACKEND_K1: JS_IR
// !API_VERSION: 1.9

import kotlin.concurrent.*

@OptIn(kotlin.ExperimentalStdlibApi::class)
class StringWrapper(@Volatile var x: String)

val global = StringWrapper("FA")

fun box() : String {
    val local = StringWrapper("IL")
    if (global.x + local.x != "FAIL") return "FAIL"
    global.x = "O"
    local.x = "K"
    return global.x + local.x
}