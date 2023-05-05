package com.itsmealdo.storyapps.utils

import java.util.concurrent.Executor
import java.util.concurrent.Executors

class storyExecutor {
    val diskIO: Executor = Executors.newSingleThreadExecutor()
}