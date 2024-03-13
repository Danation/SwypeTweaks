package com.danation.xposed.swypetweaks;

import java.util.List;

import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.graphics.Canvas;
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
			
			try
			{
                XSharedPreferences preferences = new XSharedPreferences(PACKAGE_NAME);
                
                if (preferences.getBoolean("changeColors", false))
                {
                    changeColors(lpparam);
                }
                if (preferences.getBoolean("disableDragon", true))
                {
                    replaceDragon(lpparam);
                }
			    longPressSpeechChangeIME(lpparam);
                lowerLongpressMinimum(lpparam);
			}
			catch (Exception ex)
			{
				log(ex);
			}
		}
	}
	
	private void changeColors(LoadPackageParam lpparam)
	{
	    final ClassLoader loader = lpparam.classLoader;
	    
        XposedHelpers.findAndHookMethod("com.nuance.swype.input.KeyboardViewEx", loader, "bufferDrawKeyboard", Canvas.class, new XC_MethodHook()
        {
            
            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable
            {
                XSharedPreferences preferences = new XSharedPreferences(PACKAGE_NAME);

                XposedHelpers.setIntField(param.thisObject, "miTraceColor", preferences.getInt("traceColorInt", 0xb2ffa200));
                
                XposedHelpers.setIntField(param.thisObject, "mPopupTextColor", preferences.getInt("popupTextColor", 0xFF000000));
                
                ColorStateList textColor = ColorStateList.valueOf(preferences.getInt("keyTextColor", 0xFFFFFFFF));
                int altTextColor = preferences.getInt("altTextColor", 0xFFFFFFFF);
                
                Object[] keys = (Object[])XposedHelpers.getObjectField(param.thisObject, "mKeys");
                for (Object key : keys)
                {
                    XposedHelpers.setObjectField(key, "mKeyTextColor", textColor);
                    XposedHelpers.setIntField(key, "altTextColor", altTextColor);
                }
                
                //These don't seem to do much, as far as I can tell...
                //XposedHelpers.setIntField(key, "mDefaultStrokeCandidateColor", 0xFFFF0000);
                //XposedHelpers.setIntField(param.thisObject, "mAltShadowColor", 0xFFFF0000);
                //XposedHelpers.setIntField(param.thisObject, "mShadowColor", 0xFFFF0000);
                //XposedHelpers.setIntField(param.thisObject, "miHighlightTextColor", 0xFFFF0000);

            }
        });
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
                
                switchIME(mIme, "com.google.android.tts/com.google.android.apps.speech.tts.googletts.settings.asr.voiceime.VoiceInputMethodService");
                
                return null;
            }
        });
	}
	
	private void longPressSpeechChangeIME(LoadPackageParam lpparam)
    {
        final ClassLoader loader = lpparam.classLoader;
        
	    Class<?> KeyClass = XposedHelpers.findClass("com.nuance.swype.input.KeyboardEx.Key", loader);
        final int SPEECH_KEY_CODE = 6463;
        XposedHelpers.findAndHookMethod("com.nuance.swype.input.KeyboardViewEx", lpparam.classLoader, "handleLongPress", KeyClass, new XC_MethodHook()
        {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable
            {
                log("before handleLongPress()");
                
                int code = ((int[])XposedHelpers.getObjectField(param.args[0], "codes"))[0];

                log("key code: " + code);
                
                if (code != SPEECH_KEY_CODE)
                {
                    return;
                }
                
                XSharedPreferences preferences = new XSharedPreferences(PACKAGE_NAME);
                String inputId = preferences.getString("longPressAction", "None");
                
                if (inputId.equals("None"))
                {
                	return;
                }
                
                //Get instance of com.nuance.swype.input.IME
                Object mIme = XposedHelpers.getObjectField(param.thisObject, "mIme");
                
                switchIME(mIme, inputId);
            }
        });
	}

    private void lowerLongpressMinimum(LoadPackageParam lpparam)
    {
        XposedHelpers.findAndHookMethod("android.content.res.Resources", lpparam.classLoader,
                "getInteger", int.class, new XC_MethodHook() {
                    @Override
                    protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                        if (((Resources)param.thisObject).getResourceEntryName(
                                        (Integer)param.args[0]).equals("long_press_timeout_min_ms"))
                            param.setResult(50);
                    }
                    protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                    }
                });
    }

	private static void switchIME(Object mIme, String inputId)
	{
		Context context = (Context)XposedHelpers.callMethod(mIme, "getApplicationContext");
        
        //Get mIme.myInputMethodImpl
        Object mIme_myInputMethodImpl = XposedHelpers.getObjectField(mIme, "myInputMethodImpl");
        //Get mIme.myInputMethodImpl.myToken (needed for switching input method)
        IBinder mIme_myInputMethodImpl_myToken = (IBinder)XposedHelpers.getObjectField(mIme_myInputMethodImpl, "myToken");
        
        InputMethodManager imm = (InputMethodManager)XposedHelpers.callMethod(mIme, "getSystemService", "input_method");
        
        try
        {
        	boolean changedInput = false;
        	
            //Get list of all input methods
            List<InputMethodInfo> inputMethodInfo = imm.getInputMethodList();
            
            //Search list for the right keyboard
            for (InputMethodInfo info : inputMethodInfo)
            {
                if (info.getId().equalsIgnoreCase(inputId))
                {
                	imm.setInputMethod(mIme_myInputMethodImpl_myToken, inputId);
                	changedInput = true;
                    break;
                }
            }
            
            if (!changedInput)
            {
                log(inputId + ": input not found");
                showMessage(inputId + " keyboard was not found", context);
            }
        }
        catch (IllegalArgumentException ex)
        {
            showMessage("An unexpected error occurred and was logged.", context);
            log(ex);
        }
	}
	
	private static void showMessage(String message, Context context)
	{
		Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
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
