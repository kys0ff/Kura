package off.kys.kura.features.lock.side_effect

sealed class LockSideEffect {
    object Finish : LockSideEffect()
    object GoHome : LockSideEffect()
}