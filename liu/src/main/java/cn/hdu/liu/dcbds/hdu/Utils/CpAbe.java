package cn.hdu.liu.dcbds.hdu.Utils;

import cn.hdu.liu.dcbds.hdu.bswabe.*;
import it.unisa.dia.gas.jpbc.Element;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class CpAbe {
    public  BswabeElementBoolean dec(BswabePub pub, BswabePrv prv, BswabeCph cph) {
        Element t;
        Element m;
        BswabeElementBoolean beb = new BswabeElementBoolean();

        m = pub.p.getGT().newElement();
        t = pub.p.getGT().newElement();

        checkSatisfy(pub,cph.p, prv);
        if (!cph.p.satisfiable) {

            beb.e = null;
            beb.b = false;
            return beb;
        }

        pickSatisfyMinLeaves(cph.p, prv);

        decFlatten(t, cph.p, prv, pub);

        m = cph.cs.duplicate();
        m.mul(t); /* num_muls++; */

        t = pub.p.pairing(cph.c, prv.d);
        t.invert();
        m.mul(t); /* num_muls++; */

        beb.e = m;
        beb.b = true;

        return beb;
    }

    private void checkSatisfy(BswabePub bswabePub, BswabePolicy p, BswabePrv prv) {
        int i, l;
        String prvAttr;

        p.satisfiable = false;
        if (p.children == null || p.children.length == 0) {
            for (i = 0; i < prv.comps.size(); i++) {
                prvAttr = prv.comps.get(i).attr;
                if (prvAttr.compareTo(p.attr) == 0 ) {
                    p.satisfiable = true;
                    p.attri = i;
                    break;
                }
            }
        } else {
            for (i = 0; i < p.children.length; i++) // 递归查询子节点是否满足
                checkSatisfy(bswabePub,p.children[i], prv);

            l = 0;
            for (i = 0; i < p.children.length; i++)
                if (p.children[i].satisfiable)
                    l++;

            if (l >= p.k)
                p.satisfiable = true;
        }
    }

    private  void pickSatisfyMinLeaves(BswabePolicy p, BswabePrv prv) {
        int i, k, l, c_i;
        int len;
        ArrayList<Integer> c = new ArrayList<>();

        if (p.children == null || p.children.length == 0)
            p.min_leaves = 1;
        else {
            len = p.children.length;
            for (i = 0; i < len; i++)
                if (p.children[i].satisfiable)
                    pickSatisfyMinLeaves(p.children[i], prv);

            for (i = 0; i < len; i++)
                c.add(i);

            Collections.sort(c, new IntegerComparator(p));

            p.satl = new ArrayList<Integer>();
            p.min_leaves = 0;
            l = 0;

            for (i = 0; i < len && l < p.k; i++) {
                c_i = c.get(i).intValue(); /* c[i] */
                if (p.children[c_i].satisfiable) {
                    l++;
                    p.min_leaves += p.children[c_i].min_leaves;
                    k = c_i + 1;
                    p.satl.add(k);
                }
            }
        }
    }
    private void decFlatten(Element r, BswabePolicy p, BswabePrv prv,
                            BswabePub pub) {
        Element one;
        one = pub.p.getZr().newElement();
        one.setToOne();
        r.setToOne();

        decNodeFlatten(r, one, p, prv, pub);
    }
    private void decNodeFlatten(Element r, Element exp, BswabePolicy p,
                                BswabePrv prv, BswabePub pub) {
        if (p.children == null || p.children.length == 0)
            decLeafFlatten(r, exp, p, prv, pub);
        else
            decInternalFlatten(r, exp, p, prv, pub);
    }

    private void decLeafFlatten(Element r, Element exp, BswabePolicy p,
                                BswabePrv prv, BswabePub pub) {
        BswabePrvComp c;
        Element s, t;

        c = prv.comps.get(p.attri);

        s = pub.p.getGT().newElement();
        t = pub.p.getGT().newElement();

        s = pub.p.pairing(p.c, c.d); /* num_pairings++; */
        t = pub.p.pairing(p.cp, c.dp); /* num_pairings++;p.gri */

        t.invert();
        s.mul(t); /* num_muls++; */
        s.powZn(exp); /* num_exps++; */

        r.mul(s); /* num_muls++; */
    }

    private  void decInternalFlatten(Element r, Element exp,
                                     BswabePolicy p, BswabePrv prv, BswabePub pub) {
        int i;
        Element t, expnew;

        t = pub.p.getZr().newElement();
        expnew = pub.p.getZr().newElement();

        for (i = 0; i < p.satl.size(); i++) {
            lagrangeCoef(t, p.satl, (p.satl.get(i)).intValue());
            expnew = exp.duplicate();
            expnew.mul(t);
            decNodeFlatten(r, expnew, p.children[p.satl.get(i) - 1], prv, pub);
        }
    }

    private  void lagrangeCoef(Element r, ArrayList<Integer> s, int i) {
        int j, k;
        Element t;

        t = r.duplicate();

        r.setToOne();
        for (k = 0; k < s.size(); k++) {
            j = s.get(k).intValue();
            if (j == i)
                continue;
            t.set(-j);
            r.mul(t); /* num_muls++; */
            t.set(i - j);
            t.invert();
            r.mul(t); /* num_muls++; */
        }
    }

    private static class IntegerComparator implements Comparator<Integer> {
        BswabePolicy policy;

        public IntegerComparator(BswabePolicy p) {
            this.policy = p;
        }

        @Override
        public int compare(Integer o1, Integer o2) {
            int k, l;

            k = policy.children[o1].min_leaves;
            l = policy.children[o2].min_leaves;

            return Integer.compare(k, l);
        }
    }
}
