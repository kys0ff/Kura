package off.kys.kura.core.common.extensions

/**
 * Run the given block if the condition is true.
 *
 * @param condition The condition to check.
 * @param block The block of code to execute if the condition is true.
 * 
 * @return The receiver object.
 */
inline fun <T> T.runIf(condition: Boolean, block: T.() -> Unit): T {
    if (condition) block()
    return this
}