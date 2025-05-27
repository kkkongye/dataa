package cn.hdu.liu.dcbds.hdu.bswabe;


import it.unisa.dia.gas.jpbc.Element;

import java.util.ArrayList;
public class BswabePrv {
	/*
	 * A private key
	 */
	public Element d; /* G_2 */
	public ArrayList<BswabePrvComp> comps; /* BswabePrvComp */

	public Element gr;
}