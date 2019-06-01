package edu.gatech.chai.omoponfhir.smart.servlet;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.json.JSONObject;

public class SmartLauncherCodec {
	private static final Map<String, String> valueToCode = new HashMap<>();
	private static final Map<String, String> codeToValue = new HashMap<>();
	private static final List<String> simErrors = Arrays.asList(
			"auth_invalid_client_id", 
			"auth_invalid_redirect_uri",
			"auth_invalid_scope", 
			"auth_invalid_client_secret", 
			"token_invalid_token", 
			"token_expired_refresh_token",
			"request_invalid_token", 
			"request_expired_token"
	);

	static {
		valueToCode.put("launch_ehr", "a");
		valueToCode.put("patient", "b");
		valueToCode.put("encounter", "c");
		valueToCode.put("auth_error", "d");
		valueToCode.put("provider", "e");
		valueToCode.put("sim_ehr", "f");
		valueToCode.put("select_encounter", "g");
		valueToCode.put("launch_prov", "h");
		valueToCode.put("skip_login", "i");
		valueToCode.put("skip_auth", "j");
		valueToCode.put("launch_pt", "k");
		valueToCode.put("launch_cds", "l");
	}

	static {
		codeToValue.put("a", "launch_ehr");
		codeToValue.put("b", "patient");
		codeToValue.put("c", "encounter");
		codeToValue.put("d", "auth_error");
		codeToValue.put("e", "provider");
		codeToValue.put("f", "sim_ehr");
		codeToValue.put("g", "select_encounter");
		codeToValue.put("h", "launch_prov");
		codeToValue.put("i", "skip_login");
		codeToValue.put("j", "skip_auth");
		codeToValue.put("k", "launch_pt");
		codeToValue.put("l", "launch_cds");
	}
	
	static JSONObject decode(JSONObject code) {
		JSONObject result = new JSONObject();

		for (Iterator<?> iter = code.keySet().iterator(); iter.hasNext();) {
			String key = (String) iter.next();
			String decodedKey = codeToValue.get(code.get(key));
			
			if ("auth_error".equals(decodedKey)) {
				result.put(decodedKey, simErrors.get(Integer.parseInt(code.getString(decodedKey))));
			} else {
				result.put(decodedKey, code.get(key));
			}
		}
		
		return result;
	}
}
