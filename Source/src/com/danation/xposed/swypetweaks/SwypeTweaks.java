package com.danation.xposed.swypetweaks;

import java.util.List;

import android.content.Context;
import android.os.IBinder;
import android.view.inputmethod.InputMethodInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;
import de.robv.android.xposed.*;
import de.robv.android.xposed.callbacks.XC_LoadPackage.LoadPackageParam;

public class SwypeTweaks implements IXposedHookLoadPackage
{
    public static final String PACKAGE_NAME = SwypeTweaks.class.getPackage().getName();
    
	@Override
	public void handleLoadPackage(LoadPackageParam lpparam) throws Throwable
	{	

		if (lpparam.packageName.contains("com.nuance.swype"))
		{	
			log("Found Swype package: " + lpparam.packageName);
			
			XSharedPreferences preferences = new XSharedPreferences(PACKAGE_NAME);
			//preferences.makeWorldReadable();
			
			try
			{
			    if (preferences.getBoolean("disableDragon", true))
			    {
			        replaceDragon(lpparam);
			    }
				
			    if (preferences.getBoolean("launchiWnn", false))
			    {
			        longPressiWnn(lpparam);
			    }
			}
			catch (Exception ex)
			{
				log(ex);
			}
		}
	}
	
	private void replaceDragon(LoadPackageParam lpparam)
	{
	    final ClassLoader loader = lpparam.classLoader;
	    
        XposedHelpers.findAndHookMethod("com.nuance.swype.input.InputView", loader, "startSpeech", new XC_MethodReplacement()
        {
            
            @Override
            protected Object replaceHookedMethod(MethodHookParam param) throws Throwable
            {
                log("Replacing startSpeech()");
                
                //Get instance of com.nuance.swype.input.IME
                Object mIme = XposedHelpers.getObjectField(param.thisObject, "mIme");
                //Get mIme.myInputMethodImpl
                Object mIme_myInputMethodImpl = XposedHelpers.getObjectField(mIme, "myInputMethodImpl");
                //Get mIme.myInputMethodImpl.myToken (needed for switching input method)
                IBinder mIme_myInputMethodImpl_myToken = (IBinder)XposedHelpers.getObjectField(mIme_myInputMethodImpl, "myToken");
                
                InputMethodManager imm = (InputMethodManager)XposedHelpers.callMethod(mIme, "getSystemService", "input_method");
                
                try
                {
                    //Get list of all input methods
                    List<InputMethodInfo> inputMethodInfo = imm.getInputMethodList();
                    String inputId = null;
                    
                    //Search list for the voice keyboard
                    for (InputMethodInfo info : inputMethodInfo)
                    {
                        //"iwnnime" is emoji, for future use
                        if (info.getId().contains("googlequicksearchbox"))
                        {
                            inputId = info.getId();
                            break;
                        }
                    }
                    
                    //If its found, launch it.  Otherwise, show error message
                    if (inputId != null)
                    {
                        imm.setInputMethod(mIme_myInputMethodImpl_myToken, inputId);
                    }
                    else
                    {
                        log("Voice input not found");
                        //showMessage("The Google Voice Recognition keyboard was not found", TODO context?);
                    }
                }
                catch (IllegalArgumentException ex)
                {
                    //showMessage("An unexpected error occurred and was logged.", TODO context?);
                    log(ex);
                }
                return null;
            }
        });
	}
	
	private void longPressiWnn(LoadPackageParam lpparam)
    {
        final ClassLoader loader = lpparam.classLoader;
        
	    Class<?> KeyClass = XposedHelpers.findClass("com.nuance.swype.input.KeyboardEx.Key", loader);
        final int ENTER_CODE = 10;
        XposedHelpers.findAndHookMethod("com.nuance.swype.input.KeyboardViewEx", lpparam.classLoader, "handleLongPress", KeyClass, new XC_MethodHook()
        {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable
            {
                log("before handleLongPress()");
                
                int code = ((int[])XposedHelpers.getObjectField(param.args[0], "codes"))[0];

                if (code != ENTER_CODE)
                {
                    return;
                }
                
                //Get instance of com.nuance.swype.input.IME
                Object mIme = XposedHelpers.getObjectField(param.thisObject, "mIme");
                //Get mIme.myInputMethodImpl
                Object mIme_myInputMethodImpl = XposedHelpers.getObjectField(mIme, "myInputMethodImpl");
                //Get mIme.myInputMethodImpl.myToken (needed for switching input method)
                IBinder mIme_myInputMethodImpl_myToken = (IBinder)XposedHelpers.getObjectField(mIme_myInputMethodImpl, "myToken");
                
                InputMethodManager imm = (InputMethodManager)XposedHelpers.callMethod(mIme, "getSystemService", "input_method");
                
                try
                {
                    //Get list of all input methods
                    List<InputMethodInfo> inputMethodInfo = imm.getInputMethodList();
                    String inputId = null;
                    
                    //Search list for the voice keyboard
                    for (InputMethodInfo info : inputMethodInfo)
                    {
                        if (info.getId().contains("iwnnime"))
                        {
                            inputId = info.getId();
                            break;
                        }
                    }
                    
                    //If its found, launch it.  Otherwise, show error message
                    if (inputId != null)
                    {
                        imm.setInputMethod(mIme_myInputMethodImpl_myToken, inputId);
                    }
                    else
                    {
                        log("Emoji input not found");
                        //showMessage("The Google Voice Recognition keyboard was not found", TODO context?);
                    }
                }
                catch (IllegalArgumentException ex)
                {
                    //showMessage("An unexpected error occurred and was logged.", TODO context?);
                    log(ex);
                }
            }
        });
	}
	
	@SuppressWarnings("unused")
    private static void showMessage(String message, Context context)
	{
		log("attempting to show toast");
		Toast toast = Toast.makeText(context, message, Toast.LENGTH_SHORT);
		toast.show();
	}
	
	private static final String logPrefix = "SwypeTweaks: ";
	
	private static void log(String message) 
	{
		XposedBridge.log(logPrefix + message);
	}
	
	private static void log(Throwable throwable)
	{
		XposedBridge.log(logPrefix + "Exception thrown");
		XposedBridge.log(throwable);
	}
}
