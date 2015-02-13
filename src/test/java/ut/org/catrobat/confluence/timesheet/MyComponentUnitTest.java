package ut.org.catrobat.confluence.timesheet;

import org.junit.Test;
import org.catrobat.confluence.timesheet.MyPluginComponent;
import org.catrobat.confluence.timesheet.MyPluginComponentImpl;

import static org.junit.Assert.assertEquals;

public class MyComponentUnitTest
{
    @Test
    public void testMyName()
    {
        MyPluginComponent component = new MyPluginComponentImpl(null);
        assertEquals("names do not match!", "myComponent",component.getName());
    }
}