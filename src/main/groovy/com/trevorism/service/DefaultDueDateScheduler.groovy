package com.trevorism.service

import com.trevorism.https.SecureHttpClient
import com.trevorism.model.Question
import com.trevorism.schedule.DefaultScheduleService
import com.trevorism.schedule.ScheduleService
import com.trevorism.schedule.factory.DefaultScheduledTaskFactory
import com.trevorism.schedule.factory.EndpointSpec
import com.trevorism.schedule.factory.ScheduledTaskFactory
import com.trevorism.schedule.model.HttpMethod
import com.trevorism.schedule.model.ScheduledTask
import jakarta.inject.Singleton

@Singleton
class DefaultDueDateScheduler implements DueDateScheduler {

    private final ScheduleService scheduleService
    private final ScheduledTaskFactory factory = new DefaultScheduledTaskFactory()

    DefaultDueDateScheduler(SecureHttpClient secureHttpClient) {
        this.scheduleService = new DefaultScheduleService(secureHttpClient)
    }

    @Override
    void scheduleDueDateCallback(Question question) {
        String url = "https://prompt.action.trevorism.com/api/question/${question.id}/duedate/callback"
        EndpointSpec endpointSpec = new EndpointSpec(url.toString(), HttpMethod.POST, "{}")
        ScheduledTask task = factory.createImmediateTask("prompt_due_${question.id}".toString(), question.dueDate, endpointSpec)
        scheduleService.create(task)
    }
}
