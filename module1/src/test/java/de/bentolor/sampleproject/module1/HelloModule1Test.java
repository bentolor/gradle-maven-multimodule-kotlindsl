package de.bentolor.sampleproject.module1;

import org.junit.Assert;
import org.junit.Test;

public class HelloModule1Test {

    @Test
    public void myFunction() {
        Assert.assertEquals("Abbreviate, bro!", "1234…", HelloModule1.myFunction("1234567890"));
    }
}