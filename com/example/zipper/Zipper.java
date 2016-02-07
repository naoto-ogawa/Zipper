package com.example.zipper;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Zipper<T> {

    /** current item */
    private T current;

    /** items before current item */
    private List<T> before;

    /** items after current item */
    private List<T> after;

    public Zipper(List<T> original) {
        before = new ArrayList<T>();
        after = new ArrayList<T>(original);
        current = after.remove(0); // remove first
    }

    public T getCurrent() {
        return current;
    }

    public List<T> getBefore() {
        return before;
    }

    public List<T> getAfter() {
        return after;
    }

    public int size() {
        return before.size() + 1 + after.size();
    }

    public int sizeBefore() {
        return before.size();
    }

    public int sizeAfter() {
        return after.size();
    }

    public void next() {
        if (after.size() == 0) {
            throw new IllegalStateException();
        }
        before.add(current); // add last
        current = after.remove(0); // remove first
    }

    public void before() {
        if (before.size() == 0) {
            throw new IllegalStateException();
        }
        after.add(0, current); // add first
        current = before.remove(before.size() -1);
    }

    public void moveTo(int pos) {
        if (pos<0 || this.size()-1 < pos ) {
            throw new IndexOutOfBoundsException(
                    String.format("size=%d, pos=%d", this.size(), pos));
        }
        // current
        if (pos == this.getPosition()) {
            return;
        }
        // before
        if (pos < this.sizeBefore()) {
            int tmpPos = this.sizeBefore() - pos;
            while (0 < tmpPos) {
                this.before();
                tmpPos--;
            }
            return;
        }
        // after
        int tmpPos = pos - this.getPosition();
        while (0 < tmpPos) {
            this.next();
            tmpPos--;
        }
    }

    interface IFoldFunc<B, A> {
        B apply(B b, A a);
    }

    public <B> B foldB(B init, IFoldFunc<B, T> func) {
        B b = init;
        for (int i = 0; i < this.size(); i++) {
            moveTo(i);
            b = func.apply(b, this.getCurrent());
        }
        return b;
    }

    public <B> B foldA(B init, IFoldFunc<B, T> func) {
        B b = init;
        for (int i = this.size() - 1; 0 <= i; i--) {
            moveTo(i);
            b = func.apply(b, this.getCurrent());
        }
        return b;
    }

    interface IMapFunc<B, A> {
        B apply(A a);
    }

    public <B> List<B> map(IMapFunc<B, T> func) {
        List<B> ret = new ArrayList<B>();
        for (int i = 0; i < this.size(); i++) {
            moveTo(i);
            ret.add(func.apply(this.getCurrent()));
        }
        return ret;
    }

    interface IPred<A> {
        boolean apply(A a);
    }


    public List<T> filter(IPred<T> func) {
        List<T> ret = new ArrayList<T>();
        for (int i = 0; i < this.size(); i++) {
            moveTo(i);
            T a = this.getCurrent();
            if(func.apply(a)) {
                ret.add(a);
            }
        }
        return ret;
    }

    interface IZipperFoldFunc<B, A> {
        B apply(B b, List<A> before, A current, List<A> after);
    }

    public <B> B foldB(B init, IZipperFoldFunc<B, T> func) {
        B b = init;
        for (int i = 0; i < this.size(); i++) {
            moveTo(i);
            b = func.apply(
                    b,
                    this.getBefore(),
                    this.getCurrent(),
                    this.getAfter()
            );
        }
        return b;
    }

    interface IZipperMapFunc<B, A> {
        B apply(List<A> before, A current, List<A> after);
    }

    public <B> List<B> foldA(IZipperMapFunc<B, T> func) {
        List<B> ret = new ArrayList<B>();
        for (int i = 0; i < this.size(); i++) {
            moveTo(i);
            ret.add(func.apply(
                    this.getBefore(),
                    this.getCurrent(),
                    this.getAfter()
            ));
        }
        return ret;
    }

    public <B> List<B> foldL(IZipperMapFunc<B, T> func) {
        List<B> ret = new ArrayList<B>();
        for (int i = this.size() - 1; 0 <= i; i--) {
            moveTo(i);
            ret.add(func.apply(
                    this.getBefore(),
                    this.getCurrent(),
                    this.getAfter()
            ));
        }
        return ret;
    }

    public String log() {
        return before.toString() + " <" + current + "> " + after.toString();
    }

    public String logStats() {
        return String.format("%s, pos=%d, size=%d, before=%d, after=%d",
                this.log(),
                this.getPosition(),
                this.size(),
                this.sizeBefore(),
                this.sizeAfter());
    }

    private int getPosition() {
        return this.sizeBefore();
    }


    // --------------------------------------------------------------------------------------------
    // Test
    public static void main(String[] a) {
        List<String> in = new ArrayList<String>();
        in.add("aaaa");
        in.add("bbbb");
        in.add("cccc");
        in.add("dddd");

        Zipper<String> zipper = new Zipper<String>(in);
        log(zipper);

        // forward
        log("* forward");
        zipper.next();
        log(zipper);
        zipper.next();
        log(zipper);
        zipper.next();
        log(zipper);

        try {
            zipper.next();
        } catch (IllegalStateException e) {
            System.out.println(e);
        }

        // backward
        log("* backward");

        zipper.before();
        log(zipper);
        zipper.before();
        log(zipper);
        zipper.before();
        log(zipper);
        try {
            zipper.before();
        } catch (IllegalStateException e) {
            System.out.println(e);
            log(zipper);
        }

        // position check
        log("* toPosition smaller error");
        try {
            zipper.moveTo(-1);
        } catch (IndexOutOfBoundsException e) {
            System.out.println(e);
        }
        log("* toPosition larger error");
        try {
            zipper.moveTo(4);
        } catch (IndexOutOfBoundsException e) {
            System.out.println(e);
        }

        log("* toPosition");
        zipper.moveTo(3);
        log(zipper);

        log("* toPosition");
        zipper.moveTo(0);
        log(zipper);

        log("* toPosition");
        zipper.moveTo(2);
        log(zipper);

        log("* higher order function");
        List<Integer> in2 = new ArrayList<Integer>();
        in2.add(1);
        in2.add(2);
        in2.add(3);
        in2.add(4);
        in2.add(5);
        in2.add(6);
        in2.add(7);
        in2.add(8);
        in2.add(9);
        in2.add(10);
        in2.add(11);
        in2.add(12);
        in2.add(13);
        Zipper<Integer> zipper2 = new Zipper<Integer>(in2);
        log(zipper2);

        log("* sum (fold)");
        int ret1 = zipper2.foldB(0, new IFoldFunc<Integer, Integer>(){
            @Override
            public Integer apply(Integer b, Integer a) {
                return b + a;
            }
        });
        log("ret=" + ret1);

        log("* product (fold)");
        int ret2 = zipper2.foldB(1, new IFoldFunc<Integer, Integer>(){
            @Override
            public Integer apply(Integer b, Integer a) {
                return b * a;
            }
        });
        log("ret=" + ret2);

        log("* map ");
        List<Integer> ret3 = zipper2.foldB(new ArrayList<Integer>(), new IFoldFunc<List<Integer>, Integer>(){
            @Override
            public List<Integer> apply(List<Integer> b, Integer a) {
                b.add(a * 3);
                return b;
            }
        });
        log("ret=" + ret3);

        log("* filter 1");
        List<Integer> ret4 = zipper2.foldB(new ArrayList<Integer>(), new IFoldFunc<List<Integer>, Integer>(){
            @Override
            public List<Integer> apply(List<Integer> b, Integer a) {
                if (a % 2 == 0) {
                    b.add(a);
                }
                return b;
            }
        });
        log("ret=" + ret4);

        log("* filter 2");
        List<Integer> ret5 = zipper2.filter(new IPred<Integer>(){
            @Override
            public boolean apply(Integer a) {
                return a % 2 == 0;
            }
        });
        log("ret=" + ret5);


        List<Integer> in3 = new ArrayList<Integer>();
        in3.add(4);
        in3.add(2);
        in3.add(3);
        in3.add(1);
        in3.add(8);
        in3.add(6);
        in3.add(7);
        in3.add(2);
        in3.add(11);
        in3.add(10);
        in3.add(9);
        in3.add(14);
        in3.add(1);
        log(in3.toString());
        Zipper<Integer> zipper3 = new Zipper<Integer>(in3);

        log("*  zipperfold  max ");
        List<Integer> ret6 = zipper3.foldB(new ArrayList<Integer>(), new IZipperFoldFunc<List<Integer>, Integer>(){
            @Override
            public List<Integer> apply(List<Integer> ret, List<Integer> before, Integer current, List<Integer> after) {
                if (ret.isEmpty()) {
                    ret.add(current);
                } else {
                    ret.add(Math.max(ret.get(ret.size()-1), current));
                }
                return ret;
            }
        });
        log(ret6.toString());

        log("*  zipperfold moving average ");
        final int den = 5;
        List<BigDecimal> ret7 = zipper3.foldB(new ArrayList<BigDecimal>(), new IZipperFoldFunc<List<BigDecimal>, Integer>(){
            @Override
            public List<BigDecimal> apply(List<BigDecimal> ret, List<Integer> before, Integer current, List<Integer> after) {
                List<Integer> tmp = new ArrayList<Integer>();
                for (int i = before.size() -1 ; 0 <= i && before.size() -1 - ((den-1)/2) < i; i--) {
                    tmp.add(before.get(i));
                }
                tmp.add(current);
                for (int i = 0 ; i < (den-1)/2 && i < after.size() -1; i++) {
                    tmp.add(after.get(i));
                }

                ret.add(calculateAverage(tmp));

                return ret;
            }
        });
        log(ret7.toString());

        log("*  zipperfold max < current < min ");
        List<Boolean> ret8 = zipper3.foldB(new ArrayList<Boolean>(), new IZipperFoldFunc<List<Boolean>, Integer>(){
            @Override
            public List<Boolean> apply(List<Boolean> ret, List<Integer> before, Integer current, List<Integer> after) {
                ret.add(
                        ((before.isEmpty() ? Integer.MIN_VALUE : Collections.max(before)) < current)
                     && ((after.isEmpty()  ? Integer.MAX_VALUE : Collections.max(after )) > current)
                );
                return ret;
            }
        });
        log(ret8.toString());

    }

    private static <T> void log(Zipper<T> zipper) {
        System.out.println(zipper.logStats());
    }

    private static void log(String s) {
        System.out.println(s);
    }

    private static BigDecimal calculateAverage(final List<Integer> values) {
        if (values.isEmpty()) {
            return BigDecimal.ZERO;
        } else {
            int sum = 0;
            for (final Integer v : values) {
                sum += v;
            }
            return new BigDecimal(sum).divide(new BigDecimal(values.size()), 2, BigDecimal.ROUND_HALF_UP);
        }
    }


}
