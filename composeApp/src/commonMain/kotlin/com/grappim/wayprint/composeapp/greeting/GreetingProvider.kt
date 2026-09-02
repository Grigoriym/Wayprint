package com.grappim.wayprint.composeapp.greeting

import org.koin.core.annotation.Single

// M0.4 placeholder proving the Koin graph resolves a real injection — M4 replaces this call site.
@Single
class GreetingProvider {
    @Suppress("FunctionOnlyReturningConstant")
    fun greeting(): String = "Wayprint"
}
