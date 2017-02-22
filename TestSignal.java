
class TestSignal implements sun.misc.SignalHandler {
	  private static boolean running;
	  public static void main(final String[] args) throws InterruptedException
	  {
		  running = true;
		  TestSignal handler = new TestSignal();
		  //handler.handleSignal("HUB");
		  handler.handleSignal("INT");
		  handler.handleSignal("TERM");
		  while (running)
		  {
			  Thread.sleep(1000);
			  System.out.println("sleeping ...");
		  }
		  System.out.println("finished");
	  }

	  public void handleSignal( final String signalName ) throws IllegalArgumentException
	  {
		  try
		  {
			  sun.misc.Signal.handle( new sun.misc.Signal(signalName), this );
		  }
		  catch( IllegalArgumentException x )
		  {
			  // Most likely this is a signal that's not supported on this
			  // platform or with the JVM as it is currently configured
			  throw x;
		  }
		  catch( Throwable x )
		  {
			  // We may have a serious problem, including missing classes
			  // or changed APIs
			  throw new IllegalArgumentException( "Signal unsupported: "+signalName, x );
		  }
	  }
	  /**
	   * Called by Sun Microsystems' signal trapping routines in the JVM.
	   * @param signal The {@link sun.misc.Signal} that we received
	   **/
	  public void handle( final sun.misc.Signal signal )
	  {
		  System.out.println("signal catch");
		  running = false;
	  }
}
