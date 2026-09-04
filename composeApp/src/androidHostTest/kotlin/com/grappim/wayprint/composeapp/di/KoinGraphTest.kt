package com.grappim.wayprint.composeapp.di

import android.content.Context
import org.koin.test.verify.verify
import kotlin.test.Test

/**
 * A missing Koin definition is otherwise a launch-time crash that no gate catches: a
 * `@ComponentScan` that misses a class, or an `AppModule` that forgets an `includes` line, both
 * compile perfectly.
 *
 * `verify()` walks every definition's constructor by reflection and fails on a parameter type the
 * module set can't supply — without instantiating anything.
 *
 * Lives in `androidHostTest` rather than `commonTest` (M15.8): `EXTERNALLY_SUPPLIED` names
 * `android.content.Context`, a type that only exists on the Android target's classpath — once
 * `jvm()` became a real target, a `commonTest` copy of this file failed to compile for it. See
 * `jvmTest/.../KoinGraphTest.kt` for the JVM target's equivalent, which needs no such type.
 */
class KoinGraphTest {

    @Test
    fun `every definition in the app graph can be resolved`() {
        AppModule().module().verify(extraTypes = EXTERNALLY_SUPPLIED)
    }

    private companion object {
        /** `Context` comes from `androidContext()` in `WayprintApp`, not from any definition here. */
        val EXTERNALLY_SUPPLIED = listOf(Context::class)
    }
}
