package com.carbonfive.test.spring.dbunit;

import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.junit4.AbstractTransactionalJUnit4SpringContextTests;

/**
 * @see DataSet
 * @see DataSetTestExecutionListener
 */
@TestExecutionListeners({ DataSetTestExecutionListener.class })
public abstract class AbstractTransactionalDataSetTestCase extends AbstractTransactionalJUnit4SpringContextTests
{
}
