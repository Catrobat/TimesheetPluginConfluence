package ut.org.catrobat.confluence;

import org.junit.Test;
import org.catrobat.confluence.api.MyPluginComponent;
import org.catrobat.confluence.impl.MyPluginComponentImpl;

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