package tests;

import java.io.IOException;

import print.GCODE;

/**
 * 
 * @author Zifeng Guo, ETH Zurich & Hao Hua, Southeast University
 *
 * Southeast University, http://labaaa.org, whitegreen@163.com
 * 
 * based on https://reprap.org/wiki/G-code
 * 
 */

public class SimpleTest { // print a polygon layer by layer

	public static void main(String[] s) {
		new SimpleTest();
	}

	public SimpleTest() {
		try {
			printShape("/Users/huahao/Desktop/printPoly.gcode"); // put your folder directory
		} catch (Exception e) {
		}
	}

	private void printShape(String filename) throws IOException {
		GCODE gcode = new GCODE(90, 120, filename, 205, 60);
		// 90,120 are the workpiece's origin in the coordinate system of the 3D printer
		// 205: head temperature, 60: bed temperature

		// preparation, make sure the extrusion is smooth *********************************************************
		float thick1st = 0.24f;   // thick (vertical gap) depends on  filament
		gcode.setSpeed(12600);   
		gcode.setThick(thick1st);  
		gcode.G0(40, 20, thick1st); // Rapid positioning, head up
		
		// print arcs centered at (20,20)
		gcode.setSpeed(800);  // the speed internally manages the extrusion velocity
		for (int i = 0; i < 6; i++) {
			float t = i * GCODE.width;  // width (horizontal gap) depends on  filament
			gcode.G1(40 - t, 20);
			gcode.G2(t, 20, new float[] { t - 20, 0 }, false); // half circle
			gcode.G2(40 - t, 20, new float[] { 20 - t, 0 }, false); // true-> G02 clockwise, false -> G03 counter-clockwise
		}

		// print a rotated polygon **********************************************************************************
		float high = thick1st;
		for (int i = 0; i < 64; i++) { // 64 layers
			int speed = 0 == i ? 650 : 1300; // low speed for the 1st layer
			gcode.writeNote("Layer" + i);
			gcode.setSpeed(speed);  // the speed internally manages the extrusion velocity
			gcode.G0(high); // z-coordinate, each layer is 0.2f thick
			high += 0.2f;  // thick (vertical gap) depends on  filament
			double rad = 12 - i * 0.1;
			double rot = i * 0.005;
			for (int j = 0; j <= 6; j++) { // use <=, there are 7 points
				double x = 20 + rad * Math.cos(rot + j * Math.PI / 3);
				double y = 20 + rad * Math.sin(rot + j * Math.PI / 3);
				gcode.G1((float) x, (float) y); // x,y-coordinates
			}
		}
		gcode.setSpeed(12600);
		gcode.G0(high);
		gcode.finish();
	}

}
