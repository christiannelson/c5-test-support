package com.carbonfive.testutils.spring.dbunit;

import org.springframework.test.context.*;
import org.springframework.test.context.junit4.*;

/**
 * @see DataSet
 * @see DataSetTestExecutionListener
 */
@TestExecutionListeners({ DataSetTestExecutionListener.class })
public abstract class AbstractTransactionalDataSetTestCase extends AbstractTransactionalJUnit4SpringContextTests
{
}
