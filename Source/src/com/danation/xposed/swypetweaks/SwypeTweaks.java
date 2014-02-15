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
	@Override
	public void handleLoadPackage(LoadPackageParam lpparam) throws Throwable
	{	
		if (lpparam.packageName.contains("com.nuance.swype"))
		{	
			log("Found Swype package: " + lpparam.packageName);
			
			try
			{
				XposedHelpers.findAndHookMethod("com.nuance.swype.input.InputView", lpparam.classLoader, "startSpeech", new XC_MethodReplacement()
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
								// if (info.getId().contains("iwnnime"))
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
			catch (Exception ex)
			{
				XposedBridge.log(ex);
			}
		}
	}
	
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
