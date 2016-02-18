package ut.org.catrobat.confluence.services.impl;

import com.atlassian.confluence.mail.template.ConfluenceMailQueueItem;
import com.atlassian.core.task.MultiQueueTaskManager;
import com.atlassian.mail.queue.MailQueueItem;
import org.catrobat.confluence.services.MailService;
import org.catrobat.confluence.services.impl.MailServiceImpl;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static com.atlassian.confluence.mail.template.ConfluenceMailQueueItem.MIME_TYPE_HTML;
import static org.mockito.Mockito.verify;


@RunWith(MockitoJUnitRunner.class)
public class MailServiceImplTest {
  private MailService mailService;


  @Mock
  private MultiQueueTaskManager taskManager;


  @Before
  public void setUp() {
    mailService = new MailServiceImpl(taskManager);
  }


  @Test
  public void testSendEmail() throws Exception {
    MailQueueItem mailQueueItem = new ConfluenceMailQueueItem("whoever@atlassian.com", "A test email", "The body of the message", MIME_TYPE_HTML);
    mailService.sendEmail(mailQueueItem);
    verify(taskManager).addTask(MailServiceImpl.MAIL, mailQueueItem);
  }
}