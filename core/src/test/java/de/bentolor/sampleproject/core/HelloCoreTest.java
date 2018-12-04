package de.bentolor.sampleproject.core;

import org.junit.Assert;
import org.junit.Test;

import static org.junit.Assert.*;

public class HelloCoreTest {

    @Test
    public void myFunction() {
        Assert.assertEquals("Abbreviate, bro!", "1234…", HelloCore.myFunction("1234567890"));
    }
}