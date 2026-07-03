package com.trevorism.service

import com.trevorism.model.Question

interface DueDateScheduler {

    /**
     * Schedules a one-shot callback at the question's due date that lets prompt emit an overdue
     * event if the question is still unanswered.
     */
    void scheduleDueDateCallback(Question question)
}
