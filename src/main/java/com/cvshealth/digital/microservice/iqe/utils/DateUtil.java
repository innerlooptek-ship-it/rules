package com.cvshealth.digital.microservice.iqe.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.format.DateTimeParseException;
import java.util.*;


/**
 * The Date Utils class to include date conversion functions.
 *
 * @author c246301
 */
public class DateUtil {

	/** The Constant logger. */
	private static final Logger logger = LoggerFactory.getLogger("DateUtils");

	private static final List<String> mcDateFormats = Arrays.asList("M/d/yyyy h:mm:ss a", "MM/dd/yyyy HH:mm:ss", "yyyy-MM-dd HH:mm:ss");
    private static final String AVAILABLE_DATE_FORMAT ="" ;

    private static LocalDateTime parseWithMultiFormat(String dateStr) {
		for (String format : mcDateFormats) {
			try {
				return LocalDateTime.parse(dateStr, DateTimeFormatter.ofPattern(format));
			} catch (DateTimeParseException e) {
				// continue to the next loop iteration
			}
		}
		return null;
	}


	public static ZonedDateTime parseDate(String date) {
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern(AVAILABLE_DATE_FORMAT);
		LocalDate localDateTime = LocalDate.parse(date, formatter);

		ZoneId zoneId = ZoneId.systemDefault();

		return ZonedDateTime.of(localDateTime.atStartOfDay(), zoneId);
	}



	public static String reformatWaitlistMCDate(String date) {
		DateTimeFormatter formatterInput = DateTimeFormatter.ofPattern("MM/dd/yyyy", Locale.ENGLISH);
		DateTimeFormatter formatterOutput = DateTimeFormatter.ofPattern("yyyy-MM-dd", Locale.ENGLISH);

		LocalDate localDate = LocalDate.parse(date, formatterInput);
		ZonedDateTime zonedDateTime = localDate.atStartOfDay(ZoneId.systemDefault());

		return formatterOutput.format(zonedDateTime);
	}

	public static String convertMcAppointmentDTFormat(String dateInString) {
		DateTimeFormatter formatterInput = DateTimeFormatter.ofPattern("MM/dd/yyyy HH:mm:ss");
		LocalDateTime dateTime = LocalDateTime.parse(dateInString, formatterInput);

		DateTimeFormatter formatterOutput = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
		return dateTime.format(formatterOutput);
	}

	public static String formatDateForPattern(String dateString, String format) {
		ZonedDateTime date = parseDate(dateString);
		DateTimeFormatter dateFormat = DateTimeFormatter.ofPattern(format);

		return dateFormat.format(date);
	}

	public static int calculateAge(String birthDate) {
		int age = 0;
		try {
			age = Period.between(LocalDate.parse(birthDate), LocalDate.now()).getYears();
		}catch (Exception e){
			logger.info("Error parsing date in calculateAge: {}", e.getMessage());
		}
		return age;
	}

	public static String timeConvert(String appointmentTime) {
		try {
			LocalTime localTime = LocalTime.parse(appointmentTime, DateTimeFormatter.ofPattern("hh:mm a"));
			DateTimeFormatter outputFormatter = DateTimeFormatter.ofPattern("HH:mm");
			return localTime.format(outputFormatter);
		}catch (Exception e){
			logger.info("Error parsing date in timeConvert : {}", e.getMessage());
		}
		return null;
	}



	public static String dateFormatter(String dateTime, String fromPattern, String toPattern) {
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern(fromPattern);
		DateTimeFormatter newFormat = DateTimeFormatter.ofPattern(toPattern);
		LocalDateTime dt = LocalDateTime.parse(dateTime, formatter);

		return dt.format(newFormat);
	}

	public static String convertToUTC(String dateInString, String timezone,String fromPattern, String toPattern) {
		DateTimeFormatter fromFormatter = DateTimeFormatter.ofPattern(fromPattern);

		LocalDateTime localdateTime = LocalDateTime.parse(dateInString, fromFormatter);

		ZonedDateTime dateTimeWithZone = localdateTime.atZone(ZoneId.of(timezone));

		ZonedDateTime dateTimeInUTC = dateTimeWithZone.withZoneSameInstant(ZoneId.of("UTC"));

		DateTimeFormatter outputFormatter = DateTimeFormatter.ofPattern(toPattern);

		return dateTimeInUTC.format(outputFormatter);
	}
}