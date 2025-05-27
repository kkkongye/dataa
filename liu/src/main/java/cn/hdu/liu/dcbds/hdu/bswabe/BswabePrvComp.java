package cn.hdu.liu.dcbds.hdu.bswabe;

import it.unisa.dia.gas.jpbc.Element;

public class BswabePrvComp {
	/* these actually get serialized */
	public String attr;
	public Element d;					/* G_2 */
	public Element dp;				/* G_2 *///最好后期删了
	
	/* only used during dec */
	public int used;
	public Element z;					/* G_1 */
	public Element zp;				/* G_1 */

	public Element h_rp;
}
