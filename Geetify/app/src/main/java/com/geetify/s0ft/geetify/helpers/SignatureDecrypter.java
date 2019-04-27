package com.geetify.s0ft.geetify.helpers;

import com.geetify.s0ft.geetify.exceptions.FunctionExtractionException;

import org.json.JSONException;
import org.json.JSONObject;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.Function;
import org.mozilla.javascript.Scriptable;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SignatureDecrypter {

    public static String DecryptSignature(String[] functionNameSearchRegexArray, String encryptedSignature, JSONObject ytConfigArgsJSON) throws FunctionExtractionException {

        try {
            JSONObject assetsJSON = ytConfigArgsJSON.getJSONObject("assets");
            String jsPlayerPath = assetsJSON.getString("js");
            String jsPlayerURL = "https://www.youtube.com" + jsPlayerPath;
            String scriptText = HelperClass.DownloadFromUrl(jsPlayerURL);

            String functionName = "";
            for (int i = 0; i < functionNameSearchRegexArray.length; i++) {
                Pattern pattern = Pattern.compile(functionNameSearchRegexArray[i]);
                Matcher matcher = pattern.matcher(scriptText);
                if (matcher.find()) {
                    switch (i) {
                        case 0:
                            functionName = matcher.group(2);
                            break;
                        case 1:
                            functionName = matcher.group(1);
                            break;
                        case 2:
                            functionName = matcher.group(1);
                            break;
                        case 3:
                            functionName = matcher.group(1);
                            break;
                        case 4:
                            functionName = matcher.group(1);
                            break;
                    }
                    break;
                }
            }
            if (functionName.equals("")) {
                throw new FunctionExtractionException("Function name not found!",scriptText);
            }
            String extractedFunctionText = "";
            String functionPattern = AppSettings.getDecryptionFunctionDefinitionFilterRegexPattern(functionName);//"\\b(?<!\\.)" + functionName + "\\s*=\\s*function\\b\\(.*?\\)\\{.*\\};";
            Pattern pattern = Pattern.compile(functionPattern);
            Matcher matcher = pattern.matcher(scriptText);
            if (matcher.find()) {
                extractedFunctionText = "function "+ functionName+"("+matcher.group(1)+"){"+matcher.group(2)+"}";
            }
            String referencedObjectPattern = "\\b(\\w+)\\.\\w+\\(.*?\\)";
            pattern = Pattern.compile(referencedObjectPattern);
            matcher = pattern.matcher(extractedFunctionText);
            List<String> objectMatches = new ArrayList<>();
            while (matcher.find()) {
                objectMatches.add(matcher.group(1));
            }
            HashMap<String, Integer> objectReference = new HashMap<>();
            for (int i = 0; i < objectMatches.size(); i++) {
                if (objectReference.containsKey(objectMatches.get(i))) {
                    objectReference.put(objectMatches.get(i), objectReference.get(objectMatches.get(i)) + 1);
                } else {
                    objectReference.put(objectMatches.get(i), 0);
                }
            }
            String mostReferencedObject = "";
            Integer referenceCount = 0;
            for (Map.Entry<String, Integer> entry : objectReference.entrySet()) {
                if (entry.getValue() > referenceCount) {
                    referenceCount = entry.getValue();
                    mostReferencedObject = entry.getKey();
                }
            }
            String objectDefinitionPattern = "(?s)" + mostReferencedObject + "=\\{.*?\\};";
            pattern = Pattern.compile(objectDefinitionPattern);
            matcher = pattern.matcher(scriptText);
            if (matcher.find()) {
                extractedFunctionText += matcher.group();
            } else {
                throw new FunctionExtractionException(mostReferencedObject + "Object definition not found.",scriptText);
            }

            Context context = Context.enter();
            context.setOptimizationLevel(-1);
            Scriptable scope = context.initStandardObjects();
            context.evaluateString(scope, extractedFunctionText, "DecryptFunction", 1, null);
            Object func = scope.get(functionName, scope);
            if (func instanceof Function) {
                Function function = (Function) func;
                Object result = function.call(context, scope, scope, new String[]{encryptedSignature});
                return Context.toString(result);
            }


        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }

        throw new FunctionExtractionException("Decryption function could not be extracted.");

    }

}
