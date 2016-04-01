package com.darvds.shinylinenubers.views;

import android.content.Context;

import com.darvds.shinylinenumbers.views.NumberView;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.junit.Assert.*;

/**
 * Created by davidscott1 on 26/03/2016.
 */
@RunWith(MockitoJUnitRunner.class)
public class NumberViewTest {

    @Mock
    Context mockContext;

    @Test
    public void testSetNumberValid() throws Exception {

        Exception e = null;

        try {
            NumberView nv = new NumberView(mockContext);
            nv.setNumber(5);
        }catch(Exception ex){
            e = ex;
        }

        assertNull(e);

    }

    @Test
    public void testSetNumberInvalidLow() throws Exception {

        Exception e = null;

        try {
            NumberView nv = new NumberView(mockContext);
            nv.setNumber(-1);
        }catch(Exception ex){
            e = ex;
        }

        assertNotNull(e);

    }

    @Test
    public void testSetNumberInvalidHigh() throws Exception {

        Exception e = null;

        try {
            NumberView nv = new NumberView(mockContext);
            nv.setNumber(10);
        }catch(Exception ex){
            e = ex;
        }

        assertNotNull(e);

    }
}