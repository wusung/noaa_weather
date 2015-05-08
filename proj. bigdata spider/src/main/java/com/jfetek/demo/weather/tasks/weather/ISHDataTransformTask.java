package com.jfetek.demo.weather.tasks.weather;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintStream;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.zip.GZIPOutputStream;

import com.jfetek.common.data.Result;

public class ISHDataTransformTask implements Runnable {

	File src;
//	File tgt;
//	File ahv;

	public ISHDataTransformTask(File src) {
		this.src = src;
//		this.tgt = tgt;
//		this.ahv = ahv;
		sProgramName = src.getName();
	}

	public void run() {
		transfer();
	}

	String sProgramName = "ishJava.java";
	String sDebugName = "ishJava";
	String sInFileName = "";
	String sOutFileName = "";
	FileOutputStream fDebug = null;

	boolean bVerbose = false;
	boolean bOnce = false;
	boolean bStdErr = false;

	String sOutputRecord = "";
	String sControlSection = "";
	String sMandatorySection = "";
	int iCounter = 0;
	int iLength = 0;
	int iOffset = 25;
	int iObsKey = 0;
	int iWork = 0;
	String[] sWW1234;
	String[] sAW1234;
	float fWork = 0;
	float fWorkSave = 0;
	String sConcat = "";
	String sConcatDate = "";
	String sConcatMonth = "";
	String sMessage = "";

	final int iPROD = 0; // Only print basic run info
	final int iDEBUG = 1; // Print lots of info, such as debug messages
	int iLogLevel = 0; // Default value for this run.
	String p_sFilter1 = "None";
	String p_sFilter2 = "None";

	NumberFormat fmt03 = new DecimalFormat("000"); // 3-char int (keep
													// leading zeros)
	NumberFormat fmt4_1 = new DecimalFormat("#0.0"); // 4-char float
	NumberFormat fmt6_1 = new DecimalFormat("###0.0"); // 6-char float
	NumberFormat fmt5_2 = new DecimalFormat("##0.00"); // 5-char float
	NumberFormat fmt02 = new DecimalFormat("#0"); // 2-char int

	// Fields making up the Control Data Section.
	String sCDS = "";
	String sCDS_Fill1 = "";
	String sCDS_ID = "";
	String sCDS_Wban = "";
	String sCDS_Year = "";
	String sCDS_Month = "";
	String sCDS_Day = "";
	String sCDS_Hour = "";
	String sCDS_Minute = "";
	String sCDS_Fill2 = "";
	// Fields making up the Mandatory Data Section.
	String sMDS = "";
	String sMDS_Dir = "";
	String sMDS_DirQ = "";
	String sMDS_DirType = "";
	String sMDS_Spd = "";
	String sMDS_Fill2 = "";
	String sMDS_Clg = "";
	String sMDS_Fill3 = "";
	String sMDS_Vsb = "";
	String sMDS_Fill4 = "";
	String sMDS_TempSign = "";
	String sMDS_Temp = "";
	String sMDS_Fill5 = "";
	String sMDS_DewpSign = "";
	String sMDS_Dewp = "";
	String sMDS_Fill6 = "";
	String sMDS_Slp = "";
	String sMDS_Fill7 = "";

	// REM offset
	int iREM_IndexOf = 0;

	// Fields making up the OC1 element
	// Sample Element=[OC1...]
	int iOC1_IndexOf = 0;
	int iOC1_Length = 8;
	String sOC1 = "";
	String sOC1_Fill1 = "";
	String sOC1_Gus = "";
	String sOC1_Fill2 = "";

	// Fields making up the GF1 element
	int iGF1_IndexOf = 0;
	int iGF1_Length = 26;
	String sGF1 = "";
	String sGF1_Fill1 = "";
	String sGF1_Skc = "";
	String sGF1_Fill2 = "";
	String sGF1_Low = "";
	String sGF1_Fill3 = "";
	String sGF1_Med = "";
	String sGF1_Fill4 = "";
	String sGF1_Hi = "";
	String sGF1_Fill5 = "";

	// int iMW_Counter = 0;
	// Fields making up the MW1-7 elements
	int iMW1_IndexOf = 0;
	int iMW1_Length = 6;
	String sMW1 = "";
	String sMW1_Fill1 = "";
	String sMW1_Ww = "";
	String sMW1_Fill2 = "";

	int iMW2_IndexOf = 0;
	int iMW2_Length = 6;
	String sMW2 = "";
	String sMW2_Fill1 = "";
	String sMW2_Ww = "";
	String sMW2_Fill2 = "";

	int iMW3_IndexOf = 0;
	int iMW3_Length = 6;
	String sMW3 = "";
	String sMW3_Fill1 = "";
	String sMW3_Ww = "";
	String sMW3_Fill2 = "";

	int iMW4_IndexOf = 0;
	int iMW4_Length = 6;
	String sMW4 = "";
	String sMW4_Fill1 = "";
	String sMW4_Ww = "";
	String sMW4_Fill2 = "";

	// Fields making up the AY1 element
	int iAY1_IndexOf = 0;
	int iAY1_Length = 8;
	String sAY1 = "";
	String sAY1_Fill1 = "";
	String sAY1_Pw = "";
	String sAY1_Fill2 = "";

	// Fields making up the MA1 element
	int iMA1_IndexOf = 0;
	int iMA1_Length = 15;
	String sMA1 = "";
	String sMA1_Fill1 = "";
	String sMA1_Alt = "";
	String sMA1_Fill2 = "";
	String sMA1_Stp = "";
	String sMA1_Fill3 = "";

	// Max/Min fields
	String sMaxTemp = "";
	String sMinTemp = "";

	// Fields making up the KA1 element
	int iKA1_IndexOf = 0;
	int iKA1_Length = 13;
	String sKA1 = "";
	String sKA1_Fill1 = "";
	String sKA1_Code = "";
	String sKA1_Temp = "";
	String sKA1_Fill2 = "";

	// Fields making up the KA2 element
	int iKA2_IndexOf = 0;
	int iKA2_Length = 13;
	String sKA2 = "";
	String sKA2_Fill1 = "";
	String sKA2_Code = "";
	String sKA2_Temp = "";
	String sKA2_Fill2 = "";

	// Precip fields
	String sPcp01 = "*****";
	String sPcp01t = " ";
	String sPcp06 = "*****";
	String sPcp06t = " ";
	String sPcp24 = "*****";
	String sPcp24t = " ";
	String sPcp12 = "*****";
	String sPcp12t = " ";

	// Fields making up the AA1 element
	int iAA1_IndexOf = 0;
	int iAA1_Length = 11;
	String sAA1 = "";
	String sAA1_Fill1 = "";
	String sAA1_Hours = "";
	String sAA1_Pcp = "";
	String sAA1_Trace = "";
	String sAA1_Fill2 = "";

	// Fields making up the AA2 element
	int iAA2_IndexOf = 0;
	int iAA2_Length = 11;
	String sAA2 = "";
	String sAA2_Fill1 = "";
	String sAA2_Hours = "";
	String sAA2_Pcp = "";
	String sAA2_Trace = "";
	String sAA2_Fill2 = "";

	// Fields making up the AA3 element
	int iAA3_IndexOf = 0;
	int iAA3_Length = 11;
	String sAA3 = "";
	String sAA3_Fill1 = "";
	String sAA3_Hours = "";
	String sAA3_Pcp = "";
	String sAA3_Trace = "";
	String sAA3_Fill2 = "";

	// Fields making up the AA4 element
	int iAA4_IndexOf = 0;
	int iAA4_Length = 11;
	String sAA4 = "";
	String sAA4_Fill1 = "";
	String sAA4_Hours = "";
	String sAA4_Pcp = "";
	String sAA4_Trace = "";
	String sAA4_Fill2 = "";

	// Fields making up the AJ1 element
	int iAJ1_IndexOf = 0;
	int iAJ1_Length = 17;
	String sAJ1 = "";
	String sAJ1_Fill1 = "";
	String sAJ1_Sd = "";
	String sAJ1_Fill2 = "";

	// Fields making up the AW1-4 elements
	int iAW1_IndexOf = 0;
	int iAW1_Length = 6;
	String sAW1 = "";
	String sAW1_Fill1 = "";
	String sAW1_Zz = "";
	String sAW1_Fill2 = "";

	int iAW2_IndexOf = 0;
	int iAW2_Length = 6;
	String sAW2 = "";
	String sAW2_Fill1 = "";
	String sAW2_Zz = "";
	String sAW2_Fill2 = "";

	int iAW3_IndexOf = 0;
	int iAW3_Length = 6;
	String sAW3 = "";
	String sAW3_Fill1 = "";
	String sAW3_Zz = "";
	String sAW3_Fill2 = "";

	int iAW4_IndexOf = 0;
	int iAW4_Length = 6;
	String sAW4 = "";
	String sAW4_Fill1 = "";
	String sAW4_Zz = "";
	String sAW4_Fill2 = "";

	String sHeader = "  USAF  WBAN YR--MODAHRMN DIR SPD GUS CLG SKC L M H  VSB "
			+ "MW MW MW MW AW AW AW AW W TEMP DEWP    SLP   ALT    STP MAX MIN PCP01 "
			+ "PCP06 PCP24 PCPXX SD\n";

	public int getTransformCount() {
		return iCounter;
	}
	
	public Result<File> transfer() {
		boolean ok = true;
		File tmp = null;
		Result<File> result;
		try {
			tmp = File.createTempFile("weather-", ".tmp");
			result = Result.wrap(tmp);
			// BufferedReader fInReader = new BufferedReader(new
			// FileReader(src));
//			BufferedReader fInReader = new BufferedReader(
//					new InputStreamReader(new GZIPInputStream(
//							new FileInputStream(src))));
			BufferedReader fInReader = new BufferedReader(new InputStreamReader(new com.jcraft.jzlib.GZIPInputStream(new FileInputStream(src))));

			// FileWriter fFixed = new FileWriter(tgt);
			// BufferedWriter fFixedWriter = new BufferedWriter(fFixed);
			BufferedWriter fFixedWriter = new BufferedWriter(
					new OutputStreamWriter(new GZIPOutputStream(
							new FileOutputStream(tmp))));

			fFixedWriter.write(sHeader); // Put header into output file.

			try {
				String line = null;
				while ((line = fInReader.readLine()) != null) {
					iCounter++;
					// iOffset = 25;
					iLength = line.length();
					// logIt(fDebug, iDEBUG, false,
					// "Record # "+iCounter+" had iLength=["+iLength+"]");
					// System.out.println(line);

					// See where the REM section begins
					iREM_IndexOf = line.indexOf("REM");
					if (iREM_IndexOf == -1) {
						iREM_IndexOf = 9999; // If no REM section then set
												// to high value
					}

					getCDS(line); // Fields making up the Control Data
									// Section.

					sConcat = sCDS_ID + "-" + sCDS_Wban + "-" + sCDS_Year
							+ "-" + sCDS_Month + "-" + sCDS_Day + " "
							+ sCDS_Hour + ":" + sCDS_Minute;
					sConcatDate = sCDS_Year + "-" + sCDS_Month + "-"
							+ sCDS_Day;
					sConcatMonth = sCDS_Year + "-" + sCDS_Month;

					// =-=-=-=-=-=-=-=-=-=-=-=-=-= Filter out all but a
					// certain station/date =-=-=-=-=-=-=-=-=-=-=-=-=-=
					// if ( (! sConcatDate.equals("2011-01-01")) && (!
					// sConcatDate.equals("2010-01-02")) )
					// if ( (! sConcatDate.equals("2012-04-12")) ) // Whole
					// Day
					// if ( (! sConcatMonth.equals("2009-04")) ) // Whole
					// month
					// {
					// continue;
					// }
					//
					// logIt(fDebug, iDEBUG, false, "line=["+line+"] ");
					//
					// logIt(fDebug, iDEBUG, false,
					// "Record # "+iCounter+" had sConcat=["+sConcat+"]");
					//
					// if (iCounter >= 100)
					// {
					// logIt(fDebug, iDEBUG, false,
					// "Max count reached.  Stopping...");
					// fFixedWriter.flush();
					// fFixedWriter.close();
					// System.exit(22);
					// }
					// =-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-= Done
					// =-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=

					getMDS(line); // Fields making up the Mandatory Data
									// Section.
					getOC1(line); // Fields making up the OC1 element.
					getGF1(line); // Fields making up the GF1 element.
					getMW1(line); // Fields making up the MW1 element.
					getMW2(line); // Fields making up the MW2 element.
					getMW3(line); // Fields making up the MW3 element.
					getMW4(line); // Fields making up the MW3 element. //
									// 06/21/2012 ras
					getAY1(line); // Fields making up the AY1 element.
					getMA1(line); // Fields making up the MA1 element.
					sMaxTemp = "***";
					sMinTemp = "***";
					getKA1(line); // Fields making up the KA1 element.
					getKA2(line); // Fields making up the KA2 element.
					sPcp01 = "*****";
					sPcp01t = " ";
					sPcp06 = "*****";
					sPcp06t = " ";
					sPcp24 = "*****";
					sPcp24t = " ";
					sPcp12 = "*****";
					sPcp12t = " ";
					getAA1(line); // Fields making up the AA1 element.
					getAA2(line); // Fields making up the AA2 element.
					getAA3(line); // Fields making up the AA3 element.
					getAA4(line); // Fields making up the AA4 element.
					getAJ1(line); // Fields making up the AJ1 element.
					getAW1(line); // Fields making up the AW1 element. //
									// 06/06/2012 ras
					getAW2(line); // Fields making up the AW2 element. //
									// 06/06/2012 ras
					getAW3(line); // Fields making up the AW3 element. //
									// 06/06/2012 ras
					getAW4(line); // Fields making up the AW4 element. //
									// 06/06/2012 ras

					// Begin formatting output
					// record..............................................................

					// Post-processing format changes
					if (sCDS_Wban.equals("99999")) // Show WBAN=99999 as
													// missing "*****" in
													// output file
					{
						sCDS_Wban = "*****";
					}
					// Build Control Data Section
					sControlSection = sCDS_ID + " " + sCDS_Wban + " "
							+ sCDS_Year + sCDS_Month + sCDS_Day + sCDS_Hour
							+ sCDS_Minute;

					// Sort Present Weather elements
					sWW1234 = new String[] { sMW1_Ww, sMW2_Ww, sMW3_Ww,
							sMW4_Ww };
					Arrays.sort(sWW1234);

					// Sort Present Weather (Automated) elements
					sAW1234 = new String[] { sAW1_Zz, sAW2_Zz, sAW3_Zz,
							sAW4_Zz };
					Arrays.sort(sAW1234);

					// Build Mandatory Data Section + the rest of the record
					sMandatorySection = sMDS_Dir + " " + sMDS_Spd + " "
							+ sOC1_Gus + " " + sMDS_Clg + " " + sGF1_Skc
							+ " " + sGF1_Low + " " + sGF1_Med + " "
							+ sGF1_Hi + " " + sMDS_Vsb + " " + sWW1234[3]
							+ " " + sWW1234[2] + " " + sWW1234[1] + " "
							+ sWW1234[0] + " " + sAW1234[3] + " "
							+ sAW1234[2] + " " + sAW1234[1] + " "
							+ sAW1234[0] + " " + sAY1_Pw + " " + sMDS_Temp
							+ " " + sMDS_Dewp + " " + sMDS_Slp + " "
							+ sMA1_Alt + " " + sMA1_Stp + " " + sMaxTemp
							+ " " + sMinTemp + " " + sPcp01 + sPcp01t
							+ sPcp06 + sPcp06t + sPcp24 + sPcp24t + sPcp12
							+ sPcp12t + sAJ1_Sd;

					sOutputRecord = sControlSection + " "
							+ sMandatorySection; // Put it all together
					fFixedWriter.write(sOutputRecord + "\n"); // Write out
																// the
																// record

				} // while read

			} catch (IOException ex) {
				System.err.println(sProgramName
						+ ": IOException 2. Error=[" + ex.getMessage()
						+ "]");
				System.err.println(sProgramName + ": Stack trace follows:");
				ex.printStackTrace();
				//System.exit(2);
				ok = false;
				result = Result.failure(ex);
			}

			fInReader.close();
			fFixedWriter.flush();
			fFixedWriter.close();
			
		} catch (Exception e) { // Catch exception if any
			sMessage = sProgramName + ": Unspecified Exception 1. Error=["
					+ e.getMessage() + "]";
			bStdErr = true;
			logIt(fDebug, iPROD, false, sMessage); // Append output to log.
			System.err.println(sProgramName + ": Stack trace follows:");
			e.printStackTrace();
			// System.exit(1);
			ok = false;
			result = Result.failure(e);
		} finally {
			if (ok) {
				// archieve
//				if (tmp.renameTo(tgt)) {
//					src.renameTo(ahv);
//				}
			}
			else {
				tmp.delete();
			}
		}

		logIt(fDebug, iDEBUG, false, "Processed " + iCounter + " records");
		logIt(fDebug, iDEBUG, false, "Done.");

		return result;
	}

	// =-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=
	// formatInt - Right-justifies an int.
	// =-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=
	public String formatInt(int i, int len) {
		final String blanks = "                 ";
		String s = Integer.toString(i);
		if (s.length() < len)
			s = blanks.substring(0, len - s.length()) + s;
		return s;
	}

	// =-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=
	// formatFloat - Right-justifies a float.
	// =-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=
	public String formatFloat(float i, int len) {
		final String blanks = "                 ";
		String s = Float.toString(i);
		if (s.length() < len)
			s = blanks.substring(0, len - s.length()) + s;
		return s;
	}

	// =-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=
	// getCDS - Get CDS section and format its output.
	// =-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=
	public void getCDS(String p_sRecd) {
		// Extract fields making up the Control Data Section.
		sCDS = p_sRecd.substring(0, 60);
		sCDS_Fill1 = p_sRecd.substring(0, 4);
		sCDS_ID = p_sRecd.substring(4, 10);
		sCDS_Wban = p_sRecd.substring(10, 15);
		sCDS_Year = p_sRecd.substring(15, 19);
		sCDS_Month = p_sRecd.substring(19, 21);
		sCDS_Day = p_sRecd.substring(21, 23);
		sCDS_Hour = p_sRecd.substring(23, 25);
		sCDS_Minute = p_sRecd.substring(25, 27);
		sCDS_Fill2 = p_sRecd.substring(27, 60);
	} // End of getCDS

	// =-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=
	// getMDS - Get MDS section and format its output.
	// =-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=
	public void getMDS(String p_sRecd) {
		// Extract fields making up the Mandatory Data Section.
		sMDS = p_sRecd.substring(60, 105);
		sMDS_Dir = p_sRecd.substring(60, 63);
		sMDS_DirQ = p_sRecd.substring(63, 64);
		sMDS_DirType = p_sRecd.substring(64, 65);
		sMDS_Spd = p_sRecd.substring(65, 69);
		sMDS_Fill2 = p_sRecd.substring(69, 70);
		sMDS_Clg = p_sRecd.substring(70, 75);
		sMDS_Fill3 = p_sRecd.substring(75, 78);
		sMDS_Vsb = p_sRecd.substring(78, 84);
		sMDS_Fill4 = p_sRecd.substring(84, 87);
		sMDS_TempSign = p_sRecd.substring(87, 88);
		sMDS_Temp = p_sRecd.substring(88, 92);
		sMDS_Fill5 = p_sRecd.substring(92, 93);
		sMDS_DewpSign = p_sRecd.substring(93, 94);
		sMDS_Dewp = p_sRecd.substring(94, 98);
		sMDS_Fill6 = p_sRecd.substring(98, 99);
		sMDS_Slp = p_sRecd.substring(99, 104);
		sMDS_Fill7 = p_sRecd.substring(104, 105);

		if (sMDS_Dir.equals("999")) {
			sMDS_Dir = "***";
		}

		if (sMDS_DirType.equals("V")) // 06/21/2012 ras
		{
			sMDS_Dir = "990";
		}

		// logIt(fDebug, iDEBUG, false,
		// "sInFileName=["+sInFileName+"] DateTime=["+sConcat+"] sMDS_Dir=["+sMDS_Dir+"] sMDS_DirQ=["+sMDS_DirQ+"] sMDS_DirType=["+sMDS_DirType+"]");
		// // temporary - ras

		if (sMDS_Spd.equals("9999")) {
			sMDS_Spd = "***";
		} else {
			// System.out.println("sMDS=["+sMDS+"] Spd=["+sMDS_Spd+"]");
			iWork = Integer.parseInt(sMDS_Spd); // Convert to integer
			// System.out.println("iWork=["+iWork+"]");
			iWork = (int) (((float) iWork / 10.0) * 2.237 + .5); // Convert
																	// Meters
																	// Per
																	// Second
																	// to
																	// Miles
																	// Per
																	// Hour
			// System.out.println("iWork=["+iWork+"]");
			// sMDS_Spd = fmt3.format(iWork);
			sMDS_Spd = formatInt(iWork, 3);
			// System.out.println("Spd=["+sMDS_Spd+"]");
			// logIt(fDebug, iDEBUG, false,
			// "sInFileName=["+sInFileName+"] DateTime=["+sConcat+"] sMDS_Spd=["+sMDS_Spd+"]");
			// // temporary - ras
		}

		if (sMDS_Clg.equals("99999")) {
			sMDS_Clg = "***";
		} else {
			try {
				iWork = Integer.parseInt(sMDS_Clg); // Convert to integer
			} catch (Exception e) {
				logIt(fDebug,
						iDEBUG,
						false,
						"sInFileName=["
								+ sInFileName
								+ "] DateTime=["
								+ sConcat
								+ "] sMDS_Clg value could not be converted to integer=["
								+ sMDS_Clg + "]");
				sMDS_Clg = "***"; // Data error. Set to missing.
			}
			if (!sMDS_Clg.equals("***")) {
				iWork = (int) (((float) iWork * 3.281) / 100.0 + .5); // Convert
																		// Meters
																		// to
																		// Hundreds
																		// of
																		// Feet
				sMDS_Clg = formatInt(iWork, 3);
				// logIt(fDebug, iDEBUG, false,
				// "sInFileName=["+sInFileName+"] DateTime=["+sConcat+"] sMDS_Clg=["+sMDS_Clg+"]");
				// // temporary - ras
			}
		}

		if (sMDS_Vsb.equals("999999")) {
			sMDS_Vsb = "****";
		} else {
			fWork = Float.parseFloat(sMDS_Vsb); // Convert to floating point
			fWork = ((float) (fWork * (float) 0.000625)); // Convert Meters
															// to Miles
															// using CDO's
															// value
			// fWork = ((float)(fWork * (float) 0.000621371192237334)); //
			// Convert Meters to Miles
			fWorkSave = fWork; // Save this value for possible display
			if (fWork > 99.9) {
				fWork = (float) 99.0; // Set to value that will fit
			}

			if (fWork == (float) 10.058125) // Match CDO 2011-04-28 ras
			{
				logIt(fDebug, iDEBUG, false, "sInFileName=[" + sInFileName
						+ "] DateTime=[" + sConcat
						+ "] sMDS_Vsb value rounded to 10 miles");
				fWork = (float) 10.0;
			}
			sMDS_Vsb = fmt4_1.format(fWork);
			sMDS_Vsb = String.format("%4s", sMDS_Vsb);
		}

		if (sMDS_Temp.equals("9999")) {
			sMDS_Temp = "****";
		} else {
			// System.out.println(sMDS_Temp);
			iWork = Integer.parseInt(sMDS_Temp); // Convert to integer
			if (sMDS_TempSign.equals("-")) {
				iWork *= -1;
			}
			if (iWork < -178) {
				iWork = (int) (((float) iWork / 10.0) * 1.8 + 32.0 - .5); // Handle
																			// temps
																			// below
																			// 0F
			} else {
				iWork = (int) (((float) iWork / 10.0) * 1.8 + 32.0 + .5);
			}
			sMDS_Temp = formatInt(iWork, 4);
			// System.out.println(sMDS_Temp);
		}

		if (sMDS_Dewp.equals("9999")) {
			sMDS_Dewp = "****";
		} else {
			// System.out.println(sMDS_Dewp);
			iWork = Integer.parseInt(sMDS_Dewp); // Convert to integer
			if (sMDS_DewpSign.equals("-")) {
				iWork *= -1;
			}
			if (iWork < -178) {
				iWork = (int) (((float) iWork / 10.0) * 1.8 + 32.0 - .5); // Handle
																			// temps
																			// below
																			// 0F
			} else {
				iWork = (int) (((float) iWork / 10.0) * 1.8 + 32.0 + .5);
			}
			sMDS_Dewp = formatInt(iWork, 4);
			// System.out.println(sMDS_Dewp);
		}

		if (sMDS_Slp.equals("99999")) {
			sMDS_Slp = "******";
		} else {
			fWork = Float.parseFloat(sMDS_Slp); // Convert to floating point
			fWork = ((float) (fWork / 10.0)); // Convert convert
												// Hectopascals to Millibars
			sMDS_Slp = fmt6_1.format(fWork);
			sMDS_Slp = String.format("%6s", sMDS_Slp);
		}
	} // End of getMDS

	// =-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=
	// getOC1 - Get OC1 element and format its output.
	// =-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=
	public void getOC1(String p_sRecd) {
		sOC1 = "";
		sOC1_Fill1 = "";
		sOC1_Gus = "***";
		sOC1_Fill2 = "";
		iOC1_IndexOf = p_sRecd.indexOf("OC1");
		if ((iOC1_IndexOf >= 0) && (iOC1_IndexOf < iREM_IndexOf)) {
			sOC1 = p_sRecd.substring(iOC1_IndexOf, iOC1_IndexOf
					+ iOC1_Length);
			sOC1_Fill1 = sOC1.substring(1, 3); // 3
			sOC1_Gus = sOC1.substring(3, 7); // 4
			sOC1_Fill2 = sOC1.substring(7, 8); // 1

			if (sOC1_Gus.equals("9999")) // 06/06/2012 ras
			{
				sOC1_Gus = "***";
				// logIt(fDebug, iDEBUG, false,
				// "sInFileName=["+sInFileName+"] DateTime=["+sConcat+"] sOC1_Gus missing=["+sOC1_Gus+"]");
				// // temporary - ras
			} else {
				try {
					iWork = Integer.parseInt(sOC1_Gus); // Convert to
														// integer
				} catch (Exception e) {
					logIt(fDebug,
							iDEBUG,
							false,
							"sInFileName=["
									+ sInFileName
									+ "] DateTime=["
									+ sConcat
									+ "] sOC1_Gus value could not be converted to integer=["
									+ sOC1_Gus + "]");
					sOC1_Gus = "***"; // Data error. Set to missing.
				}
				if (!sOC1_Gus.equals("***")) {
					iWork = (int) (((float) iWork / 10.0) * 2.237 + .5); // Convert
																			// Meters
																			// Per
																			// Second
																			// to
																			// Miles
																			// Per
																			// Hour
					sOC1_Gus = formatInt(iWork, 3);
					// logIt(fDebug, iDEBUG, false,
					// "sInFileName=["+sInFileName+"] DateTime=["+sConcat+"] sOC1_Gus=["+sOC1_Gus+"]");
					// // temporary - ras
				}
			}
		}
	} // End of getOC1

	// =-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=
	// getGF1 - Get GF1 element and format its output.
	// =-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=
	public void getGF1(String p_sRecd) {
		sGF1 = "";
		sGF1_Fill1 = "";
		sGF1_Skc = "***";
		sGF1_Fill2 = "";
		sGF1_Low = "*";
		sGF1_Fill3 = "";
		sGF1_Med = "*";
		sGF1_Fill4 = "";
		sGF1_Hi = "*";
		sGF1_Fill5 = "";
		iGF1_IndexOf = p_sRecd.indexOf("GF1");
		if ((iGF1_IndexOf >= 0) && (iGF1_IndexOf < iREM_IndexOf)) {
			sGF1 = p_sRecd.substring(iGF1_IndexOf, iGF1_IndexOf
					+ iGF1_Length);
			sGF1_Fill1 = sGF1.substring(1, 3);
			sGF1_Skc = sGF1.substring(3, 5);
			sGF1_Fill2 = sGF1.substring(5, 11);
			sGF1_Low = sGF1.substring(11, 13);
			sGF1_Fill3 = sGF1.substring(13, 20);
			sGF1_Med = sGF1.substring(20, 22);
			sGF1_Fill4 = sGF1.substring(22, 23);
			sGF1_Hi = sGF1.substring(23, 25);
			sGF1_Fill5 = sGF1.substring(25, 26);
		}

		if ((iGF1_IndexOf >= 0) && (iGF1_IndexOf < iREM_IndexOf)) {
			if (sGF1_Skc.equals("99")) {
				sGF1_Skc = "***";
			} else {
				// System.out.println("DateTime=["+sConcat+"] GF1=["+sGF1+"]  Skc=["+sGF1_Skc+"]");
				try {
					iWork = Integer.parseInt(sGF1_Skc); // Convert to
														// integer
				} catch (Exception e) {
					logIt(fDebug,
							iDEBUG,
							false,
							"sInFileName=["
									+ sInFileName
									+ "] DateTime=["
									+ sConcat
									+ "] sGF1_Skc value could not be converted to integer=["
									+ sGF1_Skc + "]");
					sGF1_Skc = "***"; // Data error. Set to missing.
				}
				if (!sGF1_Skc.equals("***")) {
					if (iWork == 0) {
						sGF1_Skc = "CLR";
					} else {
						if ((iWork >= 1) && (iWork <= 4)) {
							sGF1_Skc = "SCT";
						} else {
							if ((iWork >= 5) && (iWork <= 7)) {
								sGF1_Skc = "BKN";
							} else {
								if (iWork == 8) {
									sGF1_Skc = "OVC";
								} else {
									if (iWork == 9) {
										sGF1_Skc = "OBS";
									} else {
										if (iWork == 10) {
											sGF1_Skc = "POB";
										}
									}
								}
							}
						}
					}
				}
			}
			if (sGF1_Low.equals("99")) // Low cloud type
			{
				sGF1_Low = "*";
			} else {
				sGF1_Low = sGF1_Low.substring(1, 2);
			}

			if (sGF1_Med.equals("99")) // Med cloud type
			{
				sGF1_Med = "*";
			} else {
				sGF1_Med = sGF1_Med.substring(1, 2);
			}

			if (sGF1_Hi.equals("99")) // High cloud type
			{
				sGF1_Hi = "*";
			} else {
				sGF1_Hi = sGF1_Hi.substring(1, 2);
			}
		}
	} // End of getGF1

	// =-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=
	// getMW1 - Get MW1 element and format its output.
	// =-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=
	public void getMW1(String p_sRecd) {
		sMW1 = "";
		sMW1_Fill1 = "";
		sMW1_Ww = "**";
		sMW1_Fill2 = "";
		iMW1_IndexOf = p_sRecd.indexOf("MW1");
		if ((iMW1_IndexOf >= 0) && (iMW1_IndexOf < iREM_IndexOf)) {
			sMW1 = p_sRecd.substring(iMW1_IndexOf, iMW1_IndexOf
					+ iMW1_Length);
			sMW1_Fill1 = sMW1.substring(1, 3); // 3
			sMW1_Ww = sMW1.substring(3, 5); // 2
			sMW1_Fill2 = sMW1.substring(5, 6); // 1
			// System.out.println("MW1=["+sMW1+"] Ww=["+sMW1_Ww+"]");
		}
	} // End of getMW1

	// =-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=
	// getMW2 - Get MW2 element and format its output.
	// =-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=
	public void getMW2(String p_sRecd) {
		sMW2 = "";
		sMW2_Fill1 = "";
		sMW2_Ww = "**";
		sMW2_Fill2 = "";
		iMW2_IndexOf = p_sRecd.indexOf("MW2");
		if ((iMW2_IndexOf >= 0) && (iMW2_IndexOf < iREM_IndexOf)) {
			sMW2 = p_sRecd.substring(iMW2_IndexOf, iMW2_IndexOf
					+ iMW2_Length);
			sMW2_Fill1 = sMW2.substring(1, 3); // 3
			sMW2_Ww = sMW2.substring(3, 5); // 2
			sMW2_Fill2 = sMW2.substring(5, 6); // 1
			// System.out.println("MW2=["+sMW2+"] Ww=["+sMW2_Ww+"]");
		}
	} // End of getMW2

	// =-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=
	// getMW3 - Get MW3 element and format its output.
	// =-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=
	public void getMW3(String p_sRecd) {
		sMW3 = "";
		sMW3_Fill1 = "";
		sMW3_Ww = "**";
		sMW3_Fill2 = "";
		iMW3_IndexOf = p_sRecd.indexOf("MW3");
		if ((iMW3_IndexOf >= 0) && (iMW3_IndexOf < iREM_IndexOf)) {
			sMW3 = p_sRecd.substring(iMW3_IndexOf, iMW3_IndexOf
					+ iMW3_Length);
			sMW3_Fill1 = sMW3.substring(1, 3); // 3
			sMW3_Ww = sMW3.substring(3, 5); // 2
			sMW3_Fill2 = sMW3.substring(5, 6); // 1
			// System.out.println("MW3=["+sMW3+"] Ww=["+sMW3_Ww+"]");
		}
	} // End of getMW3

	// =-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=
	// getMW4 - Get MW4 element and format its output.
	// =-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=
	public void getMW4(String p_sRecd) {
		sMW4 = "";
		sMW4_Fill1 = "";
		sMW4_Ww = "**";
		sMW4_Fill2 = "";
		iMW4_IndexOf = p_sRecd.indexOf("MW4");
		if ((iMW4_IndexOf >= 0) && (iMW4_IndexOf < iREM_IndexOf)) {
			sMW4 = p_sRecd.substring(iMW4_IndexOf, iMW4_IndexOf
					+ iMW4_Length);
			sMW4_Fill1 = sMW4.substring(1, 3); // 3
			sMW4_Ww = sMW4.substring(3, 5); // 2
			sMW4_Fill2 = sMW4.substring(5, 6); // 1
			// logIt(fDebug, iDEBUG, false,
			// "sInFileName=["+sInFileName+"] DateTime=["+sConcat+"] sMW4_Ww=["+sMW4_Ww+"]");
			// // temporary - ras
		}
	} // End of getMW4

	// =-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=
	// getAY1 - Get AY1 element and format its output.
	// =-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=
	public void getAY1(String p_sRecd) {
		sAY1 = "";
		sAY1_Fill1 = "";
		sAY1_Pw = "*";
		sAY1_Fill2 = "";
		iAY1_IndexOf = p_sRecd.indexOf("AY1");
		if ((iAY1_IndexOf >= 0) && (iAY1_IndexOf < iREM_IndexOf)) {
			sAY1 = p_sRecd.substring(iAY1_IndexOf, iAY1_IndexOf
					+ iAY1_Length);
			sAY1_Fill1 = sAY1.substring(1, 3); // 3
			sAY1_Pw = sAY1.substring(3, 4); // 1
			sAY1_Fill2 = sAY1.substring(4, 8); // 4
			// System.out.println("AY1=["+sAY1+"] Pw=["+sAY1_Pw+"]");
		}
	} // End of getAY1

	// =-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=
	// getMA1 - Get MA1 element and format its output.
	// =-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=
	public void getMA1(String p_sRecd) {
		sMA1 = "";
		sMA1_Fill1 = "";
		sMA1_Alt = "*****";
		sMA1_Fill2 = "";
		sMA1_Stp = "******";
		sMA1_Fill3 = "";
		iMA1_IndexOf = p_sRecd.indexOf("MA1");
		if ((iMA1_IndexOf >= 0) && (iMA1_IndexOf < iREM_IndexOf)) {
			sMA1 = p_sRecd.substring(iMA1_IndexOf, iMA1_IndexOf
					+ iMA1_Length);
			sMA1_Fill1 = sMA1.substring(1, 3); // 3
			sMA1_Alt = sMA1.substring(3, 8); // 5
			sMA1_Fill2 = sMA1.substring(8, 9); // 1
			sMA1_Stp = sMA1.substring(9, 14); // 5
			sMA1_Fill3 = sMA1.substring(14, 15); // 1

			if (sMA1_Alt.equals("99999")) {
				sMA1_Alt = "*****";
			} else {
				try {
					fWork = Float.parseFloat(sMA1_Alt); // Convert to
														// floating point
				} catch (Exception e) {
					logIt(fDebug,
							iDEBUG,
							false,
							"sInFileName=["
									+ sInFileName
									+ "] DateTime=["
									+ sConcat
									+ "] sMA1_Alt value could not be converted to floating point=["
									+ sMA1_Alt + "]");
					sMA1_Alt = "*****"; // Data error. Set to missing.
				}
				if (!sMA1_Alt.equals("*****")) {
					fWork = ((float) ((fWork / 10.0) * 100.0) / (float) 3386.39); // Convert
																					// Hectopascals
																					// to
																					// Inches
					sMA1_Alt = fmt5_2.format(fWork);
					sMA1_Alt = String.format("%5s", sMA1_Alt);
				}
			}
			if (sMA1_Stp.equals("99999")) {
				sMA1_Stp = "******";
			} else {
				try {
					fWork = Float.parseFloat(sMA1_Stp); // Convert to
														// floating point
				} catch (Exception e) {
					logIt(fDebug,
							iDEBUG,
							false,
							"sInFileName=["
									+ sInFileName
									+ "] DateTime=["
									+ sConcat
									+ "] sMA1_Stp value could not be converted to floating point=["
									+ sMA1_Stp + "]");
					sMA1_Stp = "******"; // Data error. Set to missing.
				}
				if (!sMA1_Stp.equals("******")) {
					fWork = ((float) (fWork / 10.0)); // Convert convert
														// Hectopascals to
														// Millibars
					sMA1_Stp = fmt6_1.format(fWork);
					sMA1_Stp = String.format("%6s", sMA1_Stp);
				}
			}
		}
	} // End of getMA1

	// =-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=
	// getKA1 - Get KA1 element and format its output.
	// =-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=
	public void getKA1(String p_sRecd) {
		sKA1 = "";
		sKA1_Fill1 = "";
		sKA1_Code = "*";
		sKA1_Temp = "***";
		sKA1_Fill2 = "";
		iKA1_IndexOf = p_sRecd.indexOf("KA1");
		if ((iKA1_IndexOf >= 0) && (iKA1_IndexOf < iREM_IndexOf)) {
			sKA1 = p_sRecd.substring(iKA1_IndexOf, iKA1_IndexOf
					+ iKA1_Length);
			sKA1_Fill1 = sKA1.substring(1, 6); // 6
			sKA1_Code = sKA1.substring(6, 7); // 1
			sKA1_Temp = sKA1.substring(7, 12); // 5
			sKA1_Fill2 = sKA1.substring(12, 13); // 1
			// System.out.println("KA1=["+sKA1+"] Code=["+sKA1_Code+"] Temp=["+sKA1_Temp+"]");
			if (sKA1_Temp.equals("+9999")) {
				sKA1_Temp = "***";
			} else {
				try {
					fWork = Float.parseFloat(sKA1_Temp); // Convert to
															// floating
															// point
				} catch (Exception e) {
					logIt(fDebug,
							iDEBUG,
							false,
							"sInFileName=["
									+ sInFileName
									+ "] DateTime=["
									+ sConcat
									+ "] sKA1_Temp value could not be converted to floating point=["
									+ sKA1_Temp + "]");
					sKA1_Temp = "***"; // Data error. Set to missing.
				}
				if (!sKA1_Temp.equals("***")) {
					if (fWork < -178) {
						fWork = (int) (((float) fWork / 10.0) * 1.8 + 32.0 - .5); // Handle
																					// temps
																					// below
																					// 0F
					} else {
						fWork = (int) (((float) fWork / 10.0) * 1.8 + 32.0 + .5);
					}
					if (sKA1_Code.equals("N")) {
						sMinTemp = formatInt((int) fWork, 3);
					} else {
						if (sKA1_Code.equals("M")) {
							sMaxTemp = formatInt((int) fWork, 3);
						}
					}
				}
			}
		}
	} // End of getKA1

	// =-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=
	// getKA2 - Get KA2 element and format its output.
	// =-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=
	public void getKA2(String p_sRecd) {
		sKA2 = "";
		sKA2_Fill1 = "";
		sKA2_Code = "*";
		sKA2_Temp = "***";
		sKA2_Fill2 = "";
		iKA2_IndexOf = p_sRecd.indexOf("KA2");
		if ((iKA2_IndexOf >= 0) && (iKA2_IndexOf < iREM_IndexOf)) {
			sKA2 = p_sRecd.substring(iKA2_IndexOf, iKA2_IndexOf
					+ iKA2_Length);
			sKA2_Fill1 = sKA2.substring(1, 6); // 6
			sKA2_Code = sKA2.substring(6, 7); // 1
			sKA2_Temp = sKA2.substring(7, 12); // 5
			sKA2_Fill2 = sKA2.substring(12, 13); // 1
			// System.out.println("KA2=["+sKA2+"] Code=["+sKA2_Code+"] Temp=["+sKA2_Temp+"]");
			if (sKA2_Temp.equals("+9999")) {
				sKA2_Temp = "***";
			} else {
				try {
					fWork = Float.parseFloat(sKA2_Temp); // Convert to
															// floating
															// point
				} catch (Exception e) {
					logIt(fDebug,
							iDEBUG,
							false,
							"sInFileName=["
									+ sInFileName
									+ "] DateTime=["
									+ sConcat
									+ "] sKA2_Temp value could not be converted to floating point=["
									+ sKA2_Temp + "]");
					sKA2_Temp = "***"; // Data error. Set to missing.
				}
				if (!sKA2_Temp.equals("***")) {
					if (fWork < -178) {
						fWork = (int) (((float) fWork / 10.0) * 1.8 + 32.0 - .5); // Handle
																					// temps
																					// below
																					// 0F
					} else {
						fWork = (int) (((float) fWork / 10.0) * 1.8 + 32.0 + .5);
					}
					if (sKA2_Code.equals("N")) {
						sMinTemp = formatInt((int) fWork, 3);
					} else {
						if (sKA2_Code.equals("M")) {
							sMaxTemp = formatInt((int) fWork, 3);
						}
					}
				}
			}
		}
	} // End of getKA2

	// =-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=
	// getAA1 - Get AA1 element and format its output.
	// =-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=
	public void getAA1(String p_sRecd) {
		sAA1 = "";
		sAA1_Fill1 = "";
		sAA1_Hours = "";
		sAA1_Pcp = "";
		sAA1_Trace = "";
		sAA1_Fill2 = "";
		iAA1_IndexOf = p_sRecd.indexOf("AA1");
		if ((iAA1_IndexOf >= 0) && (iAA1_IndexOf < iREM_IndexOf)) {
			sAA1 = p_sRecd.substring(iAA1_IndexOf, iAA1_IndexOf
					+ iAA1_Length);
			sAA1_Fill1 = sAA1.substring(1, 3); // 3
			sAA1_Hours = sAA1.substring(3, 5); // 2
			sAA1_Pcp = sAA1.substring(5, 9); // 4
			sAA1_Trace = sAA1.substring(9, 10); // 1
			sAA1_Fill2 = sAA1.substring(10, 11); // 1
			// System.out.println("AA1=["+sAA1+"] Pcp=["+sAA1_Pcp+"]");
			if (sAA1_Pcp.equals("9999")) {
				sAA1_Pcp = "*****";
			} else {
				try {
					fWork = Float.parseFloat(sAA1_Pcp); // Convert to
														// floating point
				} catch (Exception e) {
					logIt(fDebug,
							iDEBUG,
							false,
							"sInFileName=["
									+ sInFileName
									+ "] DateTime=["
									+ sConcat
									+ "] AA1_Pcp value could not be converted to floating point=["
									+ sAA1_Pcp + "]");
					sAA1_Pcp = "*****"; // Data error. Set to missing.
				}
				if (!sAA1_Pcp.equals("*****")) {
					setPcp(sAA1_Hours, sAA1_Trace);
				}
			}
		}
	} // End of getAA1

	// =-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=
	// getAA2 - Get AA2 element and format its output.
	// =-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=
	public void getAA2(String p_sRecd) {
		sAA2 = "";
		sAA2_Fill1 = "";
		sAA2_Hours = "";
		sAA2_Pcp = "";
		sAA2_Trace = "";
		sAA2_Fill2 = "";
		iAA2_IndexOf = p_sRecd.indexOf("AA2");
		if ((iAA2_IndexOf >= 0) && (iAA2_IndexOf < iREM_IndexOf)) {
			// System.out.println("DateTime=["+sConcat+"] iAA2_IndexOf=["+iAA2_IndexOf+"] iAA2_Length=["+iAA2_Length+"] Line Length=["+iLength+"]");
			sAA2 = p_sRecd.substring(iAA2_IndexOf, iAA2_IndexOf
					+ iAA2_Length);
			sAA2_Fill1 = sAA2.substring(1, 3); // 3
			sAA2_Hours = sAA2.substring(3, 5); // 2
			sAA2_Pcp = sAA2.substring(5, 9); // 4
			sAA2_Trace = sAA2.substring(9, 10); // 1
			sAA2_Fill2 = sAA2.substring(10, 11); // 1
			// System.out.println("AA2=["+sAA2+"] Pcp=["+sAA2_Pcp+"]");
			if (sAA2_Pcp.equals("9999")) {
				sAA2_Pcp = "*****";
			} else {
				try {
					fWork = Float.parseFloat(sAA2_Pcp); // Convert to
														// floating point
				} catch (Exception e) {
					logIt(fDebug,
							iDEBUG,
							false,
							"sInFileName=["
									+ sInFileName
									+ "] DateTime=["
									+ sConcat
									+ "] AA2_Pcp value could not be converted to floating point=["
									+ sAA2_Pcp + "]");
					sAA2_Pcp = "*****"; // Data error. Set to missing.
				}
				if (!sAA2_Pcp.equals("*****")) {
					setPcp(sAA2_Hours, sAA2_Trace);
				}
			}
		}
	} // End of getAA2

	// =-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=
	// getAA3 - Get AA3 element and format its output.
	// =-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=
	public void getAA3(String p_sRecd) {
		sAA3 = "";
		sAA3_Fill1 = "";
		sAA3_Hours = "";
		sAA3_Pcp = "";
		sAA3_Trace = "";
		sAA3_Fill2 = "";
		iAA3_IndexOf = p_sRecd.indexOf("AA3");
		if ((iAA3_IndexOf >= 0) && (iAA3_IndexOf < iREM_IndexOf)) {
			// System.out.println("DateTime=["+sConcat+"] iAA3_IndexOf=["+iAA3_IndexOf+"] iAA3_Length=["+iAA3_Length+"] Line Length=["+iLength+"]");
			sAA3 = p_sRecd.substring(iAA3_IndexOf, iAA3_IndexOf
					+ iAA3_Length);
			sAA3_Fill1 = sAA3.substring(1, 3); // 3
			sAA3_Hours = sAA3.substring(3, 5); // 2
			sAA3_Pcp = sAA3.substring(5, 9); // 4
			sAA3_Trace = sAA3.substring(9, 10); // 1
			sAA3_Fill2 = sAA3.substring(10, 11); // 1
			// System.out.println("AA3=["+sAA3+"] Pcp=["+sAA3_Pcp+"]");
			if (sAA3_Pcp.equals("9999")) {
				sAA3_Pcp = "*****";
			} else {
				try {
					fWork = Float.parseFloat(sAA3_Pcp); // Convert to
														// floating point
				} catch (Exception e) {
					logIt(fDebug,
							iPROD,
							false,
							"sInFileName=["
									+ sInFileName
									+ "] DateTime=["
									+ sConcat
									+ "] AA3_Pcp value could not be converted to floating point=["
									+ sAA3_Pcp + "]");
					sAA3_Pcp = "*****"; // Data error. Set to missing.
				}
				if (!sAA3_Pcp.equals("*****")) {
					setPcp(sAA3_Hours, sAA3_Trace);
				}
			}
		}
	} // End of getAA3

	// =-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=
	// getAA4 - Get AA4 element and format its output.
	// =-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=
	public void getAA4(String p_sRecd) {
		sAA4 = "";
		sAA4_Fill1 = "";
		sAA4_Hours = "";
		sAA4_Pcp = "";
		sAA4_Trace = "";
		sAA4_Fill2 = "";
		iAA4_IndexOf = p_sRecd.indexOf("AA4");
		if ((iAA4_IndexOf >= 0) && (iAA4_IndexOf < iREM_IndexOf)) {
			// System.out.println("DateTime=["+sConcat+"] iAA4_IndexOf=["+iAA4_IndexOf+"] iAA4_Length=["+iAA4_Length+"] Line Length=["+iLength+"]");
			sAA4 = p_sRecd.substring(iAA4_IndexOf, iAA4_IndexOf
					+ iAA4_Length);
			sAA4_Fill1 = sAA4.substring(1, 3); // 3
			sAA4_Hours = sAA4.substring(3, 5); // 2
			sAA4_Pcp = sAA4.substring(5, 9); // 4
			sAA4_Trace = sAA4.substring(9, 10); // 1
			sAA4_Fill2 = sAA4.substring(10, 11); // 1
			// System.out.println("AA4=["+sAA4+"] Pcp=["+sAA4_Pcp+"]");
			if (sAA4_Pcp.equals("9999")) {
				sAA4_Pcp = "*****";
			} else {
				try {
					fWork = Float.parseFloat(sAA4_Pcp); // Convert to
														// floating point
				} catch (Exception e) {
					logIt(fDebug,
							iDEBUG,
							false,
							"sInFileName=["
									+ sInFileName
									+ "] DateTime=["
									+ sConcat
									+ "] AA4_Pcp value could not be converted to floating point=["
									+ sAA4_Pcp + "]");
					sAA4_Pcp = "*****"; // Data error. Set to missing.
				}
				if (!sAA4_Pcp.equals("*****")) {
					setPcp(sAA4_Hours, sAA4_Trace);
				}
			}
		}
	} // End of getAA4

	// =-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=
	// setPcp - Take AA elements and set Precip values.
	// =-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=
	public void setPcp(String p_sHours, String p_sTrace) {
		fWork = (fWork / (float) 10.0) * (float) .03937008; // Convert
															// precip depths
															// from
															// Millimeters
															// to Inches
		if (p_sHours.equals("01")) {
			sPcp01 = fmt5_2.format(fWork);
			sPcp01 = String.format("%5s", sPcp01);
			if (p_sTrace.equals("2")) {
				// sPcp01t = "T";
			}
		} else {
			if (p_sHours.equals("06")) {
				sPcp06 = fmt5_2.format(fWork);
				sPcp06 = String.format("%5s", sPcp06);
				if (p_sTrace.equals("2")) {
					// sPcp06t = "T";
				}
			} else {
				if (p_sHours.equals("24")) {
					sPcp24 = fmt5_2.format(fWork);
					sPcp24 = String.format("%5s", sPcp24);
					if (p_sTrace.equals("2")) {
						// sPcp24t = "T";
					}
				} else {
					sPcp12 = fmt5_2.format(fWork);
					sPcp12 = String.format("%5s", sPcp12);
					if (p_sTrace.equals("2")) {
						// sPcp12t = "T";
					}
				}
			}
		}
	} // End of setPcp

	// =-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=
	// getAJ1 - Get AJ1 element and format its output.
	// =-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=
	public void getAJ1(String p_sRecd) {
		sAJ1 = "";
		sAJ1_Fill1 = "";
		sAJ1_Sd = "**";
		sAJ1_Fill2 = "";
		iAJ1_IndexOf = p_sRecd.indexOf("AJ1");
		if ((iAJ1_IndexOf >= 0) && (iAJ1_IndexOf < iREM_IndexOf)) {
			sAJ1 = p_sRecd.substring(iAJ1_IndexOf, iAJ1_IndexOf
					+ iAJ1_Length);
			sAJ1_Fill1 = sAJ1.substring(1, 3); // 3
			sAJ1_Sd = sAJ1.substring(3, 7); // 4
			sAJ1_Fill2 = sAJ1.substring(7, 17); // 10
			// System.out.println("AJ1_Fill1=["+sAJ1_Fill1+"] Sd=["+sAJ1_Sd+"]");
			if (sAJ1_Sd.equals("9999")) {
				sAJ1_Sd = "**";
			} else {
				try {
					fWork = Float.parseFloat(sAJ1_Sd); // Convert to
														// floating point
				} catch (Exception e) {
					logIt(fDebug,
							iDEBUG,
							false,
							"sInFileName=["
									+ sInFileName
									+ "] DateTime=["
									+ sConcat
									+ "] sAJ1_Sd value could not be converted to floating point=["
									+ sAJ1_Sd + "]");
					sAJ1_Sd = "**"; // Data error. Set to missing.
				}
				if (!sAJ1_Sd.equals("**")) {
					iWork = (int) (fWork * (float) .3937008 + .5); // Convert
																	// precip
																	// depths
																	// from
																	// Millimeters
																	// to
																	// Inches
					sAJ1_Sd = fmt02.format(iWork);
					sAJ1_Sd = String.format("%2s", sAJ1_Sd);
				}
			}
		}
	} // End of getAJ1

	// =-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=
	// getAW1 - Get AW1 element and format its output.
	// =-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=
	public void getAW1(String p_sRecd) {
		sAW1 = "";
		sAW1_Fill1 = "";
		sAW1_Zz = "**";
		sAW1_Fill2 = "";
		iAW1_IndexOf = p_sRecd.indexOf("AW1");
		if ((iAW1_IndexOf >= 0) && (iAW1_IndexOf < iREM_IndexOf)) {
			sAW1 = p_sRecd.substring(iAW1_IndexOf, iAW1_IndexOf
					+ iAW1_Length);
			sAW1_Fill1 = sAW1.substring(1, 3); // 3
			sAW1_Zz = sAW1.substring(3, 5); // 2
			sAW1_Fill2 = sAW1.substring(5, 6); // 1
			// logIt(fDebug, iDEBUG, false,
			// "sInFileName=["+sInFileName+"] DateTime=["+sConcat+"] sAW1_Zz=["+sAW1_Zz+"]");
			// // temporary - ras
		}
	} // End of getAW1

	// =-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=
	// getAW2 - Get AW2 element and format its output.
	// =-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=
	public void getAW2(String p_sRecd) {
		sAW2 = "";
		sAW2_Fill1 = "";
		sAW2_Zz = "**";
		sAW2_Fill2 = "";
		iAW2_IndexOf = p_sRecd.indexOf("AW2");
		if ((iAW2_IndexOf >= 0) && (iAW2_IndexOf < iREM_IndexOf)) {
			sAW2 = p_sRecd.substring(iAW2_IndexOf, iAW2_IndexOf
					+ iAW2_Length);
			sAW2_Fill1 = sAW2.substring(1, 3); // 3
			sAW2_Zz = sAW2.substring(3, 5); // 2
			sAW2_Fill2 = sAW2.substring(5, 6); // 1
			// logIt(fDebug, iDEBUG, false,
			// "sInFileName=["+sInFileName+"] DateTime=["+sConcat+"] sAW2_Zz=["+sAW2_Zz+"]");
			// // temporary - ras
		}
	} // End of getAW2

	// =-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=
	// getAW3 - Get AW3 element and format its output.
	// =-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=
	public void getAW3(String p_sRecd) {
		sAW3 = "";
		sAW3_Fill1 = "";
		sAW3_Zz = "**";
		sAW3_Fill2 = "";
		iAW3_IndexOf = p_sRecd.indexOf("AW3");
		if ((iAW3_IndexOf >= 0) && (iAW3_IndexOf < iREM_IndexOf)) {
			sAW3 = p_sRecd.substring(iAW3_IndexOf, iAW3_IndexOf
					+ iAW3_Length);
			sAW3_Fill1 = sAW3.substring(1, 3); // 3
			sAW3_Zz = sAW3.substring(3, 5); // 2
			sAW3_Fill2 = sAW3.substring(5, 6); // 1
			// logIt(fDebug, iDEBUG, false,
			// "sInFileName=["+sInFileName+"] DateTime=["+sConcat+"] sAW3_Zz=["+sAW3_Zz+"]");
			// // temporary - ras
		}
	} // End of getAW3

	// =-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=
	// getAW4 - Get AW4 element and format its output.
	// =-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=
	public void getAW4(String p_sRecd) {
		sAW4 = "";
		sAW4_Fill1 = "";
		sAW4_Zz = "**";
		sAW4_Fill2 = "";
		iAW4_IndexOf = p_sRecd.indexOf("AW4");
		if ((iAW4_IndexOf >= 0) && (iAW4_IndexOf < iREM_IndexOf)) {
			sAW4 = p_sRecd.substring(iAW4_IndexOf, iAW4_IndexOf
					+ iAW4_Length);
			sAW4_Fill1 = sAW4.substring(1, 3); // 3
			sAW4_Zz = sAW4.substring(3, 5); // 2
			sAW4_Fill2 = sAW4.substring(5, 6); // 1
			// logIt(fDebug, iDEBUG, false,
			// "sInFileName=["+sInFileName+"] DateTime=["+sConcat+"] sAW4_Zz=["+sAW4_Zz+"]");
			// // temporary - ras
		}
	} // End of getAW4

	// =-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=
	// logIt - Append records to the log file.
	// =-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=
	public int logIt(FileOutputStream p_fDebug, int p_iLogLevel,
			boolean p_bFilter, String p_sIn) {
		int iRetCode = 99; // Set default return code to something crazy.
		String sMessageFormatted = "";

		SimpleDateFormat format = new SimpleDateFormat(
				"yyyy-MM-dd HH:mm:ss");
		Date now = new Date();
		sMessageFormatted = sProgramName + ": " + format.format(now) + "_"
				+ p_sIn;

		if (bStdErr) {
			System.err.println(sMessageFormatted); // Error mode will echo
													// message to standard
													// error
		}

		if (bVerbose) {
			System.out.println(sMessageFormatted); // Verbose mode will echo
													// message to screen
		}

		if (iLogLevel < p_iLogLevel) // 04/01/2009 ras
		{
			return 0; // No logging for this
		}

		if (p_bFilter) // 04/01/2009 ras
		{
			if (p_sFilter1.equals("None")) // 04/01/2009 ras
			{
			} else {
				if (sConcat.equals(p_sFilter1) || // 04/01/2009 ras // Life
													// is good
						sConcat.equals(p_sFilter2)) {
				} else {
					return 0; // 04/01/2009 ras // No logging for this
				}
			}
		}

		try {
			p_fDebug = new FileOutputStream(sDebugName + ".debug", true); // Append
																			// mode.
			new PrintStream(p_fDebug).println(format.format(now) + "_"
					+ p_sIn); // Write output to debug log.
			iRetCode = 0; // Good.
			p_fDebug.close();
		} catch (IOException e) {
			System.out.println("5. Unable to open debug log");
			System.err.println(sProgramName + ": Stack trace follows:");
			e.printStackTrace();
			System.exit(5);
		} catch (Exception e) {
			iRetCode = 6; // An error occurred.
			System.err.println(sProgramName
					+ ": Unspecified Exception in logIt. Error=["
					+ e.getMessage() + "]");
			System.err.println(sProgramName + ": Stack trace follows:");
			e.printStackTrace();
			System.exit(6);
		}
		return iRetCode;
	} // End of logIt

}
