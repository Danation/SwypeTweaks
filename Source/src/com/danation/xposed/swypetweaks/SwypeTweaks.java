package com.danation.xposed.swypetweaks;

import java.lang.reflect.Method;

import de.robv.android.xposed.*;
import de.robv.android.xposed.callbacks.XC_LoadPackage.LoadPackageParam;

public class SwypeTweaks implements IXposedHookLoadPackage {
	@Override
	public void handleLoadPackage(LoadPackageParam lpparam) throws Throwable {
		
		final ClassLoader loader = lpparam.classLoader;
		
		if (lpparam.packageName.equals("com.nuance.swype.dtc")) {
			
			XposedHelpers.findAndHookMethod("com.nuance.swype.input.InputView", lpparam.classLoader,
					"startSpeech", new XC_MethodReplacement() {
				@Override
				protected Object replaceHookedMethod(MethodHookParam param) throws Throwable
				{
					Object mIme = XposedHelpers.getObjectField(param.thisObject, "mIme");
					Method mImeStartVoiceRecognition = XposedHelpers.findMethodExact("com.nuance.swype.input.IME", loader, "startVoiceRecognition", String.class);
					
					mImeStartVoiceRecognition.invoke(mIme, "");
					
					return null;
				}
			});
		}
	}
}
