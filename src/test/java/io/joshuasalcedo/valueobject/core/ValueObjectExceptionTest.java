package io.joshuasalcedo.valueobject.core;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ValueObjectExceptionTest {

    @Test
    void constructorsKeepMessageAndCause() {
        IllegalStateException cause = new IllegalStateException("root");
        assertEquals("bad", new ValueObjectException("bad").getMessage());
        ValueObjectException withCause = new ValueObjectException("bad", cause);
        assertEquals("bad", withCause.getMessage());
        assertSame(cause, withCause.getCause());
        ValueObjectException fromCause = new ValueObjectException(cause);
        assertSame(cause, fromCause.getCause());
        assertTrue(fromCause.getMessage().contains("root"));
        assertNull(new ValueObjectException().getMessage());
        ValueObjectException noTrace = new ValueObjectException("m", cause, false, false);
        assertEquals(0, noTrace.getStackTrace().length);
        noTrace.addSuppressed(new RuntimeException());
        assertEquals(0, noTrace.getSuppressed().length);
        assertInstanceOf(RuntimeException.class, new ValueObjectException());
    }
}
