package org.catrobat.confluence.services.impl;

import com.atlassian.core.task.MultiQueueTaskManager;
import com.atlassian.mail.queue.MailQueueItem;
import org.catrobat.confluence.services.MailService;

/**
 * Default implementation of the {@link MailService}
 */
public class MailServiceImpl implements MailService {
    public static final String MAIL = "mail";
    private final MultiQueueTaskManager taskManager;

    public MailServiceImpl(MultiQueueTaskManager taskManager) {
        this.taskManager = taskManager;
    }


    public void sendEmail(MailQueueItem mailQueueItem) {
        taskManager.addTask(MAIL, mailQueueItem);
    }
}