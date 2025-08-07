package com.cvshealth.digital.microservice.iqe.utils;

import com.cvshealth.digital.microservice.iqe.constants.SchedulingConstants;
import jakarta.servlet.http.HttpServletRequest;
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
     * Populate event map.
     *
     * @param className the class name
     * @param serviceName the service name
     * @param methodName the method name
     * @param serviceDesc the service desc

     * @return the map
     */
	public static Map<String, Object> populateEventMap(String className, String methodName, String serviceName, String serviceDesc,
													   Map<String, String> reqHdrMap) {
		Map<String, Object> eventMap = new LinkedHashMap<>();
		eventMap.put(CLASS_NAME, className);
		eventMap.put(SERVICE_NAME, serviceName);
		eventMap.put(METHOD_NAME, methodName);
		eventMap.put(SERVICE_DESC, serviceDesc);
		eventMap.put(OPERATION_NAME, methodName);

		if(reqHdrMap!=null) {
			populateHeaderInfo(eventMap, reqHdrMap);
		}

		return eventMap;
	}


	public static Map<String, Object> populateHeaderInfo(Map<String, Object> eventMap, Map<String, String> reqHdrMap) {

		if (null != reqHdrMap && !CollectionUtils.isEmpty(reqHdrMap)) {

			if (StringUtils.isNotBlank(reqHdrMap.get(SchedulingConstants.CONST_SRC_LOC_CD))) {
				eventMap.put(SchedulingConstants.CONST_SRC_LOC_CD, reqHdrMap.get(SchedulingConstants.CONST_SRC_LOC_CD));
			}

			if (StringUtils.isNotBlank(reqHdrMap.get(SchedulingConstants.CONST_ORIGIN))) {
				eventMap.put(SchedulingConstants.CONST_ORIGIN, reqHdrMap.get(SchedulingConstants.CONST_ORIGIN));
			}

			if (StringUtils.isNotBlank(reqHdrMap.get(SchedulingConstants.CONST_USER_ID))) {
				eventMap.put(SchedulingConstants.CONST_USER_ID, reqHdrMap.get(SchedulingConstants.CONST_USER_ID));
			}

			if (StringUtils.isNotBlank(reqHdrMap.get(SchedulingConstants.CONST_MSG_SRC_CD))) {
				eventMap.put(SchedulingConstants.CONST_MSG_SRC_CD, reqHdrMap.get(SchedulingConstants.CONST_MSG_SRC_CD));
			}

			if (StringUtils.isNotBlank(reqHdrMap.get(SchedulingConstants.CONST_CATEGORY))) {
				eventMap.put(SchedulingConstants.CONST_CATEGORY, reqHdrMap.get(SchedulingConstants.CONST_CATEGORY)!=null?reqHdrMap.get(SchedulingConstants.CONST_CATEGORY):SchedulingConstants.DEFAULT_CAT);
			}

			if (StringUtils.isNotBlank(reqHdrMap.get(SchedulingConstants.CONST_APP_NAME))) {
				eventMap.put(SchedulingConstants.CONST_CHAN_PLAT, reqHdrMap.get(SchedulingConstants.CONST_APP_NAME));
			}

			if (StringUtils.isNotBlank(reqHdrMap.get(SchedulingConstants.CONST_DEVICE_TYPE))) {
				eventMap.put(SchedulingConstants.CONST_DEVICE_TYPE, reqHdrMap.get(SchedulingConstants.CONST_DEVICE_TYPE));
			}

			if (StringUtils.isNotBlank(reqHdrMap.get(SchedulingConstants.CONST_APP_VERSION))) {
				eventMap.put(SchedulingConstants.CONST_REQ_APP_VERSION, reqHdrMap.get(SchedulingConstants.CONST_APP_VERSION));
			}

			if (StringUtils.isNotBlank(reqHdrMap.get(SchedulingConstants.CONST_REQ_ORIGIN))) {
				eventMap.put(SchedulingConstants.CONST_REQ_ORIGIN, reqHdrMap.get(SchedulingConstants.CONST_REQ_ORIGIN));
			}

			if (StringUtils.isNotBlank(reqHdrMap.get(SchedulingConstants.CONST_AKAMAI_CLIENT_IP))) {
				eventMap.put(SchedulingConstants.CONST_CLIENT_IP, reqHdrMap.get(SchedulingConstants.CONST_AKAMAI_CLIENT_IP));
			}

			if (StringUtils.isNotBlank(reqHdrMap.get(SchedulingConstants.CONST_X_B3_PARENTSPANID))) {
				eventMap.put(SchedulingConstants.CONST_X_B3_PARENTSPANID,
						reqHdrMap.get(SchedulingConstants.CONST_X_B3_PARENTSPANID));
			}

			if (StringUtils.isNotBlank(reqHdrMap.get(SchedulingConstants.CONST_X_B3_SAMPLED))) {
				eventMap.put(SchedulingConstants.CONST_X_B3_SAMPLED, reqHdrMap.get(SchedulingConstants.CONST_X_B3_SAMPLED));
			}

			if (StringUtils.isNotBlank(reqHdrMap.get(SchedulingConstants.CONST_X_B3_SPANID))) {
				eventMap.put(SchedulingConstants.CONST_X_B3_SPANID, reqHdrMap.get(SchedulingConstants.CONST_X_B3_SPANID));
			}

			if (StringUtils.isNotBlank(reqHdrMap.get(SchedulingConstants.CONST_X_B3_TRACEID))) {
				eventMap.put(SchedulingConstants.CONST_X_B3_TRACEID, reqHdrMap.get(SchedulingConstants.CONST_X_B3_TRACEID));
			}



			if (StringUtils.isNotBlank(reqHdrMap.get(SchedulingConstants.CONST_ENV))) {
				eventMap.put(SchedulingConstants.CONST_ENV, reqHdrMap.get(SchedulingConstants.CONST_ENV));
			}

			if (StringUtils.isNotBlank(reqHdrMap.get(SchedulingConstants.CONST_USER_AGENT))) {
				eventMap.put(SchedulingConstants.CONST_USER_AGENT, reqHdrMap.get(SchedulingConstants.CONST_USER_AGENT));
			}

			if (StringUtils.isNotBlank(reqHdrMap.get(SchedulingConstants.CONST_REFERER))) {
				eventMap.put(SchedulingConstants.CONST_REFERER, reqHdrMap.get(SchedulingConstants.CONST_REFERER));
			}

			if (StringUtils.isNotBlank(reqHdrMap.get(SchedulingConstants.CONST_CATEGORY))) {
				eventMap.put(SchedulingConstants.CONST_CATEGORY, reqHdrMap.get(SchedulingConstants.CONST_CATEGORY));
			} else {
				eventMap.put(SchedulingConstants.CONST_CATEGORY, SchedulingConstants.CONST_DEFAULT_CATEGORY);
			}

			if (StringUtils.isNotBlank(reqHdrMap.get(SchedulingConstants.CONST_GRID))) {
				eventMap.put(SchedulingConstants.CONST_GRID, reqHdrMap.get(SchedulingConstants.CONST_GRID));
			} else {
				eventMap.put(SchedulingConstants.CONST_GRID, "default-" + java.util.UUID.randomUUID());
			}

			if (StringUtils.isNotBlank(reqHdrMap.get(SchedulingConstants.CONST_X_GRID))) {
				eventMap.put(SchedulingConstants.CONST_X_GRID, reqHdrMap.get(SchedulingConstants.CONST_X_GRID));
			} else {
				eventMap.put(SchedulingConstants.CONST_X_GRID, "default-" + java.util.UUID.randomUUID());
			}

			if (StringUtils.isNotBlank(reqHdrMap.get(SchedulingConstants.CONST_EXP_ID))) {
				eventMap.put(SchedulingConstants.CONST_EXP_ID, reqHdrMap.get(SchedulingConstants.CONST_EXP_ID));
			}
			if (StringUtils.isNotBlank(reqHdrMap.get(EXPERIENCE_ID))) {
				eventMap.put(EXPERIENCE_ID, reqHdrMap.get(EXPERIENCE_ID));
			}
			if (StringUtils.isNotBlank(reqHdrMap.get(EXPERIENCE_ID_SMALL_CASE))) {
				eventMap.put(EXPERIENCE_ID, reqHdrMap.get(EXPERIENCE_ID_SMALL_CASE));
			}


		}
		return eventMap;
	}
	/**
	 * Populate header info.
	 * 
	 * @param eventMap  the event map
	 * @param reqHdrMap the request header map
	 * @return the event map
	 */

}