package com.hacybeyker.finflow.feature.widget.ui

import android.content.Context
import androidx.glance.appwidget.updateAll
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters

/**
 * Re-renders every widget instance once a day. No injection on purpose: `updateAll` just kicks
 * [FinFlowWidget.provideGlance], which loads its own fresh snapshot.
 */
class WidgetRefreshWorker(context: Context, params: WorkerParameters) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        FinFlowWidget().updateAll(applicationContext)
        return Result.success()
    }

    companion object {
        const val WORK_NAME = "finflow-widget-refresh"
    }
}
