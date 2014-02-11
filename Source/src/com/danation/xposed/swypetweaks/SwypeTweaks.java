package com.danation.xposed.swypetweaks;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XC_MethodReplacement;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage.LoadPackageParam;

public class SwypeTweaks implements IXposedHookLoadPackage {
	@Override
	public void handleLoadPackage(LoadPackageParam lpparam) throws Throwable {
		
		final String logPrefix = "SwypeTweaks: ";
		
		if (lpparam.packageName.equals("com.nuance.swype.dtc")) {  //twice?  And what about input?
			//Lcom/nuance/swype/input/SpeechWrapper;->showPopupSpeech()V
			XposedBridge.log("SwypeTweaks: Found Swype");

			//showPopupSpeech
			//startDictation
			//getDictation()
			
			//startSpeech(2 params)
			//createDictation(2 params)
			//InputView.startSpeech()
			//input.IME.startVoiceRecognition
			
			
			/*XposedHelpers.findAndHookMethod("com.nuance.swype.input.SpeechWrapper", lpparam.classLoader,
					"showPopupSpeech", new XC_MethodReplacement() {
				@Override
				protected Object replaceHookedMethod(MethodHookParam param) throws Throwable {
					XposedBridge.log(logPrefix + "Intercepted showPopupSpeech");
					
					//try to use findField to get mHostView, which is a com.nuance.swype.input.InputView
					
					return null;
				}
			});*/

			XposedHelpers.findAndHookMethod("com.nuance.swype.input.InputView", lpparam.classLoader,
					"startSpeech", new XC_MethodHook() {
				@Override
				protected void afterHookedMethod(MethodHookParam param) throws Throwable {
					XposedBridge.log(logPrefix +  "startSpeech");
					
				}
			});
			
			/*XposedHelpers.findAndHookMethod("com.nuance.swype.input.IME", lpparam.classLoader,
					"startVoiceRecognition", new XC_MethodHook() {
				@Override
				protected void afterHookedMethod(MethodHookParam param) throws Throwable {
					XposedBridge.log(logPrefix + "startVoiceRecognition");
					//This one is broken for some reason
				}
			});*/
			
			XposedHelpers.findAndHookMethod("com.nuance.swype.input.SpeechWrapper", lpparam.classLoader,
					"showPopupSpeech", new XC_MethodHook() {
				@Override
				protected void afterHookedMethod(MethodHookParam param) throws Throwable {
					XposedBridge.log(logPrefix + "showPopupSpeech");
					
				}
			});
			
			XposedHelpers.findAndHookMethod("com.nuance.swype.input.SpeechWrapper", lpparam.classLoader,
					"startDictation", new XC_MethodHook() {
				@Override
				protected void afterHookedMethod(MethodHookParam param) throws Throwable {
					XposedBridge.log(logPrefix + "startDictation");
					
				}
			});
			
			XposedHelpers.findAndHookMethod("com.nuance.swype.input.SpeechWrapper", lpparam.classLoader,
					"getDictation", new XC_MethodHook() {
				@Override
				protected void afterHookedMethod(MethodHookParam param) throws Throwable {
					XposedBridge.log(logPrefix + "getDictation");
					
				}
			});
			
			/*XposedHelpers.findAndHookMethod("com.nuance.swype.input.SpeechWrapper", lpparam.classLoader,
					"createDictation", new XC_MethodHook() {
				@Override
				protected void afterHookedMethod(MethodHookParam param) throws Throwable {
					XposedBridge.log(logPrefix + "createDictation");
					//This one is broken as well
				}
			});*/
			
			XposedHelpers.findAndHookMethod("com.nuance.swype.input.SpeechWrapper", lpparam.classLoader,
					"restartDictation", new XC_MethodHook() {
				@Override
				protected void afterHookedMethod(MethodHookParam param) throws Throwable {
					XposedBridge.log(logPrefix + "restartDictation");
					
				}
			});
		}
	}
}
