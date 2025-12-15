package net.mcfarb.testing.ddmock.model;

public class MockMethodInfo {

	private String methodName;
	private Class<?>[] methodArguments;
	private String returnId;

	public String getMethodName() {
		return methodName;
	}

	public void setMethodName(String methodName) {
		this.methodName = methodName;
	}

	public Class<?>[] getMethodArguments() {
		return methodArguments;
	}

	public void setMethodArguments(Class<?>[] methodArguments) {
		this.methodArguments = methodArguments;
	}

	public String getReturnId() {
		return returnId;
	}

	public void setReturnId(String returnId) {
		this.returnId = returnId;
	}
}
