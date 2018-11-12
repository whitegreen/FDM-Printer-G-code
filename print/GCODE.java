package print;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DecimalFormat;

/**
 * 
 * @author Zifeng Guo, ETH Zurich & Hao Hua, Southeast University
 *
 * Southeast University, http://labaaa.org, whitegreen@163.com
 * 
 * based on https://reprap.org/wiki/G-code
 * 
 */

public class GCODE { // only for absolute coordinates, G90
	private final DecimalFormat df = new DecimalFormat("####.##");
	private float materialRadius = 0.875f;         //depends on filament
	public static final float width = 0.38f;       //horizontal gap depends on filament
	public float thick = 0.2f;                               //vertical gap depends on filament
	public static final float thick1st = 0.24f;  //slightly larger than normal thickness
	private float extrudeRate;                            //managed internally         
	private BufferedWriter writer = null;
	private int currentSpeed = 1500;                 
	private boolean applySpeed = true;
	private float lastExtrudeLen = 0;
	private float lastX = 0, lastY = 0;
	private int materialPullStack = 0;
	private final float cx, cy;                       //workpiece's origin in the coordinate system of the 3D printer

	public GCODE( float cx, float cy, String filepath,float temp, float tempBed) throws IOException {
		this.cx=cx;
		this.cy=cy;
		try {
			writer = new BufferedWriter(new FileWriter(filepath));
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		updateExtrudeRate();
		writeCommand_InitiateCommands( temp, tempBed);
	}

	public void setThick(float v) {
		thick = v;
		updateExtrudeRate();
	}

	private void updateExtrudeRate() {
		float volumn = (float) (materialRadius * materialRadius * Math.PI);
		// extrudeRate_first =thick1st * width / volumn;
		extrudeRate = thick * width / volumn;
	}

	public void write(String s) throws IOException {
		writer.write(s);
		writer.newLine();
	}

	private void writeCommand_InitiateCommands( float temp, float tempBed) throws IOException {
		write("M140 S" + String.valueOf(tempBed));  //see https://reprap.org/wiki/G-code
		write("M109 T0 S" + String.valueOf(temp));
		write("T0");
		write("M190 S" + String.valueOf(tempBed));

		lastX = cx;
		lastY = cy;
		// lastZ = 0;

		lastExtrudeLen = 0;
		applySpeed = true;
		currentSpeed = 1500;

		//refer to https://reprap.org/wiki/G-code
		write("G21");
		write("G90");
		write("M107");
		write("G28");
		write("G29");

		write("G1 Z15.0 F12600");
		write("G92 E0");
		write("G1 F200 E3");
		write("G92 E0");
		write("G1 F12600");
		write("M117 Printing...");
		//writeWithNewLine("M106 S180");  //FAN ON
	}

	public void setSpeed(int s) {
		if (s != currentSpeed)
			applySpeed = true;
		currentSpeed = s;
	}

	private String getSpeedCommand() {
		if (applySpeed) {
			applySpeed = false;
			return " F" + currentSpeed;
		}
		return "";
	}

	private String getExtrudeCommand(float len) {
		lastExtrudeLen += len;
		return " E" + df.format(lastExtrudeLen);
	}

	private void updateLocation(float x, float y, float z) {
		if (!Float.isNaN(x))
			lastX = x;
		if (!Float.isNaN(y))
			lastY = y;
//		if (!Float.isNaN(z))
//			lastZ = z;
	}

	private float distToLastLocation(float x, float y) {
		float dx = x - lastX;
		float dy = y - lastY;
		return (float) Math.sqrt(dx * dx + dy * dy);
	}

	public void finish() throws IOException {
		//writeWithNewLine("M107");  //FAN OFF
		write("M104 S0");
		write("M140 S0");
		write("G91");
		write("G1 E-1 F300");
		write("G28");
		write("M84");
		write("G90");
		if (materialPullStack != 0)
			System.err.println("Warning: Material pull stack are not zero!");
		writer.close();
		System.err.println("G-code saved.");
	}

	public void writeNote(String note) throws IOException {
		write(";" + note);
	}

	// G-commands********************************************************************************************
	public void G0(float rx, float ry) throws IOException {
		float x= cx+rx;
		float y= cy+ry;
		write("G0" + getSpeedCommand() + " X" + df.format(x) + " Y" + df.format(y));
		updateLocation(x, y, Float.NaN);
	}

	public void G0(float rx, float ry, float z) throws IOException {
		float x= cx+rx;
		float y= cy+ry;
		write("G0" + getSpeedCommand() + " X" + df.format(x) + " Y" + df.format(y) + " Z" + df.format(z));
		updateLocation(x, y, z);
	}

	public void G0(float z) throws IOException {
		write("G0" + getSpeedCommand() + " Z" + df.format(z));
		updateLocation(Float.NaN, Float.NaN, z);
	}

	public void G1(float rx, float ry) throws IOException {
		float x= cx+rx;
		float y= cy+ry;
		float lengthOfExtrude = distToLastLocation(x, y) * extrudeRate;// getExtrudeLength(dis);
		write("G1" + getSpeedCommand() + " X" + df.format(x) + " Y" + df.format(y) + getExtrudeCommand(lengthOfExtrude));
		updateLocation(x, y, Float.NaN);
	}

	public void G2(float[] v, float[] p, boolean clockwise) throws IOException {  //print arc
		// true-> G02 clockwise, false -> G03 counter-clockwise
		// p is relative
		G2(v[0], v[1], p, clockwise);
	}

	public void G2(float rx, float ry, float[] p, boolean clockwise) throws IOException { //print arc
		// true-> G02 clockwise, false -> G03 counter-clockwise
		// p is relative
		float x = cx + rx;
		float y = cy + ry;
		float[] va = { -p[0], -p[1] };
		float[] vb = M.sub(new float[] { x, y }, new float[] { lastX + p[0], lastY + p[1] });
		if (0.01 < Math.abs(M.magSq(va) - M.magSq(vb)))
			throw new RuntimeException();
		float radi = M.mag(p);
		double anga = Math.atan2(va[1], va[0]);
		double angb = Math.atan2(vb[1], vb[0]);
		double ang;
		if (angb > anga)
			ang = angb - anga;
		else
			ang = angb - anga + Math.PI * 2;
		
		if(clockwise)
			ang= Math.PI * 2-ang;
		//System.out.println(clockwise+": "+df.format(ang*180/Math.PI));

		float lengthOfExtrude = (float) (ang * radi * extrudeRate);
		String st = clockwise ? "G2" : "G3";
		write(st + getSpeedCommand() + " X" + df.format(x) + " Y" + df.format(y) + " I" + df.format(p[0]) + " J" + df.format(p[1]) + getExtrudeCommand(lengthOfExtrude));
		updateLocation(x, y, Float.NaN);
	}

}
