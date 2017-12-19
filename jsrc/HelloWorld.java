public class HelloWorld {
	private HelloWorld() {}
	
	public static HelloWorld getInstance() {
		return InstanceHolder.INSTANCE;
	}
	
	public void sayHelloWorld() {
		System.out.println("Hello, world!");
	}
	
	public static void main(String[] args) {
		HelloWorld.getInstance().sayHelloWorld();
	}
	
	private static class InstanceHolder {
		public static final HelloWorld INSTANCE = new HelloWorld();
	}	
}
