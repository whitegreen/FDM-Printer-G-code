package print;

import processing.core.PApplet;

public class M {

	public static float[] lineIntersect(float[] p0, float[] n0, float[] p1, float[] n1) { // in 2d
		float cross_base = cross(n0, n1);
		if (Math.abs(cross_base) < 0.00001)// parallel
			return null;
		float[] d = sub(p1, p0);
		float s = cross(d, n1) / cross_base;
		return new float[] { p0[0] + s * n0[0], p0[1] + s * n0[1] };
	}

	private static float cross(float[] a, float[] b) {
		return a[0] * b[1] - a[1] * b[0];
	}

	public static float area(float[][] ps) { // signed
		float sum = 0;
		for (int i = 0; i < ps.length; i++) {
			float[] pa = ps[i];
			float[] pb = ps[(i + 1) % ps.length];
			sum += pa[1] * pb[0] - pa[0] * pb[1];
		}
		return 0.5f * sum;
	}

	public static float[] rotateX(float theta, float[] p) {
		float c = PApplet.cos(theta);
		float s = PApplet.sin(theta);
		float[][] m = { { 1, 0, 0 }, { 0, c, -s }, { 0, s, c } };
		return mul(m, p);
	}

	public static float[] rotateY(float theta, float[] p) {
		float c = PApplet.cos(theta);
		float s = PApplet.sin(theta);
		float[][] m = { { c, 0, s }, { 0, 1, 0 }, { -s, 0, c } };
		return mul(m, p);
	}

	public static float[] rotateZ(float theta, float[] p) {
		float c = PApplet.cos(theta);
		float s = PApplet.sin(theta);
		float[][] m = { { c, -s, 0 }, { s, c, 0 }, { 0, 0, 1 } };
		return mul(m, p);
	}

	public static float[] mul(float[][] m, float[] p) {
		float[] v = new float[p.length];
		for (int i = 0; i < p.length; i++) { // row
			for (int j = 0; j < p.length; j++) { // col
				v[i] += m[i][j] * p[j];
			}
		}
		return v;
	}

	float[][] offset(float s, float[][] ps) {
		float[][] nps = new float[ps.length][];
		float[] cnt = center(ps);
		for (int i = 0; i < ps.length; i++) {
			nps[i] = between(s, ps[i], cnt);
		}
		return nps;
	}

	float[] center(float[][] ps) {
		int len = ps[0].length;
		float[] sum = new float[len];
		for (int i = 0; i < ps.length; i++) {
			for (int j = 0; j < len; j++) {
				sum[j] += ps[i][j];
			}
		}
		for (int j = 0; j < len; j++) {
			sum[j] /= ps.length;
		}
		return sum;
	}

	public static float dist_sq(float[] a, float[] b) {
		float sum = 0;
		for (int i = 0; i < a.length; i++) {
			float d = a[i] - b[i];
			sum += d * d;
		}
		return sum;
	}

	public static float dist(float[] a, float[] b) {
		float sum = 0;
		for (int i = 0; i < a.length; i++) {
			float d = a[i] - b[i];
			sum += d * d;
		}
		return PApplet.sqrt(sum);
	}

	public static float mag(float[] a) {
		float sum = 0;
		for (int i = 0; i < a.length; i++) {
			sum += a[i] * a[i];
		}
		return PApplet.sqrt(sum);
	}
	public static float magSq(float[] a) {
		float sum = 0;
		for (int i = 0; i < a.length; i++) 
			sum += a[i] * a[i];
		return sum;
	}
	
	public static float[] sub(float[] a, float[] b) {
		float[] p = new float[a.length];
		for (int i = 0; i < p.length; i++) {
			p[i] = a[i] - b[i];
		}
		return p;
	}

	public static void _sub(float[] a, float[] b) {
		for (int i = 0; i < a.length; i++) {
			a[i] -= b[i];
		}
	}

	public static float[] add(float[] a, float[] b) {
		float[] p = new float[a.length];
		for (int i = 0; i < p.length; i++) {
			p[i] = a[i] + b[i];
		}
		return p;
	}

	public static void _add(float[] a, float[] b) {
		for (int i = 0; i < a.length; i++) {
			a[i] += b[i];
		}
	}

	public static float[] scale(float s, float[] a) {
		float[] p = new float[a.length];
		for (int i = 0; i < p.length; i++) {
			p[i] = a[i] * s;
		}
		return p;
	}

	public static float[] scaleTo(float s, float[] a) {
		float mag = mag(a);
		float[] p = new float[a.length];
		for (int i = 0; i < p.length; i++) {
			p[i] = a[i] * s / mag;
		}
		return p;
	}

	public static float[] normalize(float[] v) {
		float mag = mag(v);
		float[] p = new float[v.length];
		for (int i = 0; i < p.length; i++) {
			p[i] = v[i] / mag;
		}
		return p;
	}

	public static float[] between(float s, float[] a, float[] b) {
		float[] p = new float[a.length];
		for (int i = 0; i < p.length; i++) {
			p[i] = (1 - s) * a[i] + s * b[i];
		}
		return p;
	}

	public static float[] crossProduct(float[] a, float[] b) {
		float x = a[1] * b[2] - a[2] * b[1];
		float y = a[2] * b[0] - a[0] * b[2];
		float z = a[0] * b[1] - a[1] * b[0];
		float[] v = { x, y, z };
		return v;
	}

	public static boolean intersection(float[] a, float[] b, float[] c, float[] d) {
		float[][] abd = { a, b, d };
		float[][] abc = { a, b, c };
		float[][] cda = { c, d, a };
		float[][] cdb = { c, d, b };
		return area(abd) * area(abc) < 0 && area(cda) * area(cdb) < 0;
	}


}
