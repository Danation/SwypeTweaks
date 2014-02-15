package com.danation.xposed.swypetweaks;

import java.lang.reflect.Method;

import de.robv.android.xposed.*;
import de.robv.android.xposed.callbacks.XC_LoadPackage.LoadPackageParam;

public class SwypeTweaks implements IXposedHookLoadPackage
{
	@Override
	public void handleLoadPackage(LoadPackageParam lpparam) throws Throwable
	{	
		final ClassLoader loader = lpparam.classLoader;
		final String logPrefix = "SwypeTweaks: ";
		
		if (lpparam.packageName.equals("com.nuance.swype.dtc") ||
			lpparam.packageName.equals("com.nuance.swype.trial"))
		{	
			XposedBridge.log(logPrefix + "Found Swype package");
			
			try
			{
				XposedHelpers.findAndHookMethod("com.nuance.swype.input.InputView", lpparam.classLoader, "startSpeech", new XC_MethodReplacement()
				{
					@Override
					protected Object replaceHookedMethod(MethodHookParam param) throws Throwable
					{
						XposedBridge.log(logPrefix + "Replacing startSpeech() and calling startVoiceRecognition()");
						Object mIme = XposedHelpers.getObjectField(param.thisObject, "mIme");
						Method mImeStartVoiceRecognition = XposedHelpers.findMethodExact("com.nuance.swype.input.IME", loader, "startVoiceRecognition", String.class);
						
						mImeStartVoiceRecognition.invoke(mIme, "");
						
						return null;
					}
				});
			}
			catch (Exception ex)
			{
				XposedBridge.log(logPrefix + "SwypeTweaks: Caught Exception");
				XposedBridge.log(ex);
			}
		}
	}
}
