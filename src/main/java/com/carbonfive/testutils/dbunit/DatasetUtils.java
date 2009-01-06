package com.carbonfive.testutils.dbunit;

import org.dbunit.*;
import org.dbunit.dataset.*;
import org.dbunit.dataset.xml.*;
import org.springframework.util.*;

import javax.sql.*;
import java.io.*;

public class DatasetUtils
{

    public static void loadDataSet(Class clazz, final DataSource dataSource) throws Exception
    {
        IDataSet dataSet = new FlatXmlDataSet(datasetInputStream(clazz));
        IDatabaseTester tester = new ExistingConnectionDatabaseTester(dataSource);
        tester.setDataSet(dataSet);
        tester.onSetup();
    }

    private static InputStream datasetInputStream(Class clazz)
    {
        return clazz.getResourceAsStream(ClassUtils.getShortName(clazz) + ".xml");
    }

}
