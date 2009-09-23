package com.carbonfive.testutils.dbunit;

import com.carbonfive.db.jdbc.DatabaseType;
import com.carbonfive.db.jdbc.DatabaseUtils;
import org.dbunit.dataset.datatype.DefaultDataTypeFactory;
import org.dbunit.dataset.datatype.IDataTypeFactory;
import org.dbunit.ext.db2.Db2DataTypeFactory;
import org.dbunit.ext.h2.H2DataTypeFactory;
import org.dbunit.ext.hsqldb.HsqldbDataTypeFactory;
import org.dbunit.ext.mssql.MsSqlDataTypeFactory;
import org.dbunit.ext.mysql.MySqlDataTypeFactory;
import org.dbunit.ext.oracle.Oracle10DataTypeFactory;

import java.sql.Connection;
import java.sql.SQLException;

public class DBUnitUtils
{
    public static IDataTypeFactory determineDataTypeFactory(Connection connection)
    {
        try
        {
            DatabaseType databaseType = DatabaseUtils.databaseType(connection.getMetaData().getURL());
            switch (databaseType)
            {
                case DB2:
                    return new Db2DataTypeFactory();
                case H2:
                    return new H2DataTypeFactory();
                case HSQL:
                    return new HsqldbDataTypeFactory();
                case MYSQL:
                    return new MySqlDataTypeFactory();
                case ORACLE:
                    return new Oracle10DataTypeFactory();
                case SQL_SERVER:
                    return new MsSqlDataTypeFactory();
            }
        }
        catch (SQLException ignored)
        {
        }
        return new DefaultDataTypeFactory();
    }
}
