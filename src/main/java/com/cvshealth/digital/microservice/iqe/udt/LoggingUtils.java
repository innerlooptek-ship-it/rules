package com.cvshealth.digital.microservice.iqe.udt;

//import jakarta.servlet.http.HttpServletRequest;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Component
public class LoggingUtils implements SchedulingConstants {

	private static final Map<String, String> keyMappings = Map.ofEntries(
		Map.entry(CONST_EXP, CONST_EXP),
		Map.entry(CONST_EXP_ID, CONST_EXP_ID),
		Map.entry(X_CAT, X_CAT),
		Map.entry(X_STATE_ID, X_STATE_ID),
		Map.entry(X_FINGERPRINT_ID, X_FINGERPRINT_ID),
		Map.entry(CONST_SRC_LOC_CD, CONST_SRC_LOC_CD),
		Map.entry(CONST_ORIGIN, CONST_ORIGIN),
		Map.entry(CONST_USER_ID, CONST_USER_ID),
		Map.entry(CONST_MSG_SRC_CD, CONST_MSG_SRC_CD),
		Map.entry(CONST_CATEGORY, CONST_CATEGORY),
		Map.entry(CONST_APP_NAME, CONST_CHAN_PLAT),
		Map.entry(CONST_DEVICE_TYPE, CONST_DEVICE_TYPE),
		Map.entry(CONST_APP_VERSION, CONST_REQ_APP_VERSION),
		Map.entry(CONST_REQ_ORIGIN, CONST_REQ_ORIGIN),
		Map.entry(CONST_AKAMAI_CLIENT_IP, CONST_CLIENT_IP),
		Map.entry(CONST_X_B3_PARENTSPANID, CONST_X_B3_PARENTSPANID),
		Map.entry(CONST_X_B3_SAMPLED, CONST_X_B3_SAMPLED),
		Map.entry(CONST_X_B3_SPANID, CONST_X_B3_SPANID),
		Map.entry(CONST_X_B3_TRACEID, CONST_X_B3_TRACEID),
		Map.entry(CONST_ENV, CONST_ENV),
		Map.entry(CONST_USER_AGENT, CONST_USER_AGENT),
		Map.entry(CONST_REFERER, CONST_REFERER),
		Map.entry(CONST_GRID, CONST_GRID),
		Map.entry(CONST_X_GRID, CONST_X_GRID),
		Map.entry(CONST_CLIENTREFID, CONST_CLIENTREFID)
	);
    
    @Value( "${custom.logHttpHeaders}" )
  	private List<String> logHttpHeaders;

    /**
     * Entry event logging.
     *
     * @param logger  the logger
     * @param pParams the params
     */
    public void entryEventLogging(Logger logger, Map<String, Object> pParams) {
        if (pParams == null) {
            return;
        }
        Map<String, Object> logMessage = new LinkedHashMap<>();
        logMessage.put("CVSEVENT", "ENTRY");
        logMessage.putAll(pParams);
        
        logger.info(SchedulingUtils.toJSON(logMessage, false));
    }

    /**
     * Exit event logging.
     *
     * @param logger  the logger
     * @param pParams the params
     */
    public void exitEventLogging(Logger logger, Map<String, Object> pParams) {
        if (pParams == null) {
            return;
        }
        Map<String, Object> logMessage = new LinkedHashMap<>();
        logMessage.put("CVSEVENT", "EXIT");
        logMessage.putAll(pParams);
        
        logger.info(SchedulingUtils.toJSON(logMessage, false));

    }

	/**
	 * INFO event logging.
	 *
	 * @param logger  the logger
	 * @param pParams the params
	 */
	public void infoEventLogging(Logger logger, Map<String, Object> pParams) {
		if (pParams == null) {
			return;
		}
		Map<String, Object> logMessage = new LinkedHashMap<>();
		logMessage.put("CVSEVENT", "INFO");
		logMessage.putAll(pParams);

		logger.info(SchedulingUtils.toJSON(logMessage, false));

	}

    /**
     * Error event logging.
     *
     * @param logger  the logger
     * @param pParams the params
     */
    public void errorEventLogging(Logger logger, Map<String, Object> pParams) {
        if (pParams == null) {
            return;
        }
        Map<String, Object> logMessage = new LinkedHashMap<>();
        logMessage.put("CVSEVENT", "ERROR");
        logMessage.putAll(pParams);
        
        logger.info(SchedulingUtils.toJSON(logMessage, false));
    }
    
	/**
	 * Populate header info.
	 *
	 * @param eventMap  the event map
	 * @param reqHdrMap the request header map
	 */
	public void populateHeaderInfo(Map<String, Object> eventMap, Map<String, String> reqHdrMap) {
		if (CollectionUtils.isEmpty(reqHdrMap)) return;

		// Populate the eventMap based on mappings and check for non-blank values
		keyMappings.forEach((reqKey, eventKey) -> {
			String value = reqHdrMap.get(reqKey);

			if (StringUtils.isNotBlank(value)) eventMap.put(eventKey, value);
		});
	}
	
}