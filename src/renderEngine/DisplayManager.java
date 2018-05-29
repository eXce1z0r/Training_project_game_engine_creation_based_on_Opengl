package renderEngine;

import org.lwjgl.LWJGLException;
import org.lwjgl.Sys;
import org.lwjgl.opengl.ContextAttribs;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.DisplayMode;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.PixelFormat;
import org.omg.CORBA.SystemException;

public class DisplayManager 
{	
	private static int WIDTH = 1280;
	private static int HEIGHT = 720;
	private static int FPS = 120;
	private static int currFPS = 0;
	private static final String gameTitle = "Survoror ver. 0.1";
	
	private static long lastFrameTime;// time at the end of rendering last frame
	private static float delta;// time which was spent on rendering previous frame
	
	public static void createDisplay()
	{
		/*ContextAttribs attribs = new ContextAttribs(3, 2);
		attribs.withForwardCompatible(true);
		attribs.withProfileCore(true);*/
		
		ContextAttribs attribs = new ContextAttribs(3, 2)
		.withForwardCompatible(true)
		.withProfileCore(true);// the same like code below
		
		/*ContextAttribs attribs = new ContextAttribs(3, 2);
		attribs.withForwardCompatible(true);
		attribs.withProfileCore(true);*/
		
		try 
		{
			Display.setDisplayMode(new DisplayMode(WIDTH, HEIGHT));
			Display.create(new PixelFormat(), attribs);
			Display.setTitle(gameTitle+" (FPS: --)");
		} 
		catch (LWJGLException e) 
		{
			e.printStackTrace();
		}
		
		GL11.glViewport(0, 0, WIDTH, HEIGHT);
		lastFrameTime = getCurrentTime();
	}
	
	public static void updateDisplay()
	{
		long lastTime = System.nanoTime();
		
		Display.sync(FPS);
		
		Display.update();
		
		long currentFrameTime = getCurrentTime();
		delta = (currentFrameTime - lastFrameTime)/1000f;// dividing on 1000f - convert result from milliseconds to seconds
		
		lastFrameTime = currentFrameTime;
		
		currFPS = (int)(1000000000.0 / (System.nanoTime() - lastTime));
		if(currFPS > FPS)
		{
			currFPS = FPS;
		}
		Display.setTitle(gameTitle+" (FPS: "+currFPS+")");
	}
	
	public static void closeDisplay()
	{
		Display.destroy();
	}
	
	private static long getCurrentTime()// return current time at the milliseconds
	{
		return Sys.getTime()*1000/Sys.getTimerResolution();// ??? multiplying on 1000 - convert result from seconds to milliseconds
	}
	
	public static float getFrameTimeSeconds()// returns time which was spent on rendering previous frame
	{
		return delta;
	}

}
