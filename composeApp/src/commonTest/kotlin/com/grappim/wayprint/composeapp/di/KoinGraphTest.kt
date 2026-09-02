package com.grappim.wayprint.composeapp.di

import org.koin.test.verify.verify
import kotlin.test.Test

/**
 * A missing Koin definition is otherwise a launch-time crash that no gate catches: a
 * `@ComponentScan` that misses a class, or an `AppModule` that forgets an `includes` line, both
 * compile perfectly.
 *
 * `verify()` walks every definition's constructor by reflection and fails on a parameter type the
 * module set can't supply — without instantiating anything.
 */
class KoinGraphTest {

    @Test
    fun `every definition in the app graph can be resolved`() {
        AppModule().module().verify()
    }
}
