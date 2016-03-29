package org.catrobat.confluence.services;

import com.atlassian.mail.queue.MailQueueItem;

/**
 * This service has the responsibility of sending an email
 */
public interface MailService {
    /**
     * This will send an email based on the details stored in the ConfluenceMailQueueItem
     *
     * @param mailQueueItem the item to send
     */
    void sendEmail(MailQueueItem mailQueueItem);
}