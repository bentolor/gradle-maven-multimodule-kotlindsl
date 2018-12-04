package de.bentolor.sampleproject.module2;

import de.bentolor.sampleproject.module1.HelloModule1;
import org.junit.Assert;
import org.junit.Test;

public class HelloModule2Test {

    @Test
    public void myFunction() {
        Assert.assertEquals("Abbreviate, bro!", "1234…", HelloModule1.myFunction("1234567890"));
    }
}