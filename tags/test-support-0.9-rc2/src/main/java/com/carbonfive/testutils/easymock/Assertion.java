package com.carbonfive.testutils.easymock;

public interface Assertion<E>
{
    /**
     * Allows specifying varying assertions about an argument.
     *
     * @param argument the argument being check
     * @throws Error throw an exception in the case of an assertion failure
     */
    void check(E argument) throws Error, Exception;
}
