package com.rentall.radicalstart.util.rx

import io.reactivex.Scheduler

interface Scheduler {

    fun mainThread() : Scheduler

    fun io() : Scheduler

    fun computation() : Scheduler
}