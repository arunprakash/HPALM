package org.hp.qc.web.restapi.docexamples.docexamples.infrastructure;

public class Assert {

	public static final void assertTrue(final String errorMessage, boolean assertee) {
		if (!assertee) {
			throw new RuntimeException(errorMessage);
		}
	}

	public static final void assertEquals(final String errorMessage, final String expressionOne,
			final String expressionTwo) {
		if (!expressionOne.equals(expressionTwo)) {
			throw new RuntimeException(errorMessage);
		}
	}

	public static void assertEquals(String errorMessage, int expressionOne, int expressionTwo) {
		if (expressionOne != expressionTwo) {
			throw new RuntimeException(errorMessage);
		}
	}

	public static void assertNull(String errorMessage, String assertee) {
		if (assertee != null) {
			throw new RuntimeException(errorMessage);
		}
	}

	public static void assertNotNull(String errorMessage, String assertee) {
		if (assertee == null) {
			throw new RuntimeException(errorMessage);
		}
	}
}