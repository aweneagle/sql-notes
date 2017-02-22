
public class TestShutdownHook
{
	private static boolean running;
	public static void main(final String[] args) throws InterruptedException
	{
		running = true;
		Runtime.getRuntime().addShutdownHook(new Thread(){
			@Override
			public void run()
			{
				System.out.println("Shutdown hook ran!");
				running = false;
			}
		});

		while (running)
		{
			Thread.sleep(1000);
			System.out.println("sleeping ...");
		}
		System.out.println("finished");
	}
}
