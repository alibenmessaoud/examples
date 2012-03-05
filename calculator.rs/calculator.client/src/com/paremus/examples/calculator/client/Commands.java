package com.paremus.examples.calculator.client;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import aQute.bnd.annotation.component.Component;
import aQute.bnd.annotation.component.Reference;

import com.paremus.command.options.Option;
import com.paremus.command.options.Options;
import com.paremus.examples.calculator.api.BinaryOp;
import com.paremus.types.criteria.TypedProperty;

@Component(name = "calculator.commands", 
           provide = Commands.class, 
           properties = {
             "osgi.command.scope=calc",
             "osgi.command.function=calc" 
           })
public class Commands {

    private Map<BinaryOp, Map> opToProperties = new TreeMap<BinaryOp, Map>(
            new PrecedenceComparator());

    @Reference(multiple = true, optional = true, dynamic = true)
    public void setOp(BinaryOp op, Map props) {
        synchronized (opToProperties) {
            opToProperties.put(op, props);
        }
    }

    public void unsetOp(BinaryOp op) {
        synchronized (opToProperties) {
            opToProperties.remove(op);
        }
    }

    public Object calc(String[] args) throws Exception {
        final String[] usage = {
            "",
            "calc - runs a calculation",
            "Usage: calc [-v] [-l] [expression] ",
            "  -l --list-ops          list the available operations",
            "  -v --verbose           show additional information",
            "  -? --help              show help" };

        Option opt = Options.compile(usage).parse(args);
        if (opt.isSet("help")) {
            opt.usage();
            return null;
        }

        boolean verbose = opt.isSet("verbose");

        if (opt.isSet("list-ops")) {
            synchronized (opToProperties) {
                if (opToProperties.size() == 0) {
                    System.out.println("no binary operations are available");
                }
                for (BinaryOp binOp : opToProperties.keySet()) {
                    System.out.println(binOp.getSymbol() + " " + binOp.getOpName());
                    if (verbose) {
                        Map props = opToProperties.get(binOp);
                        if (props.isEmpty())
                            System.out.println("no properties");
                        else {
                            for (Map.Entry entry : (Set<Map.Entry>) props.entrySet()) {
                                TypedProperty tp = new TypedProperty((String) entry.getKey(),
                                        entry.getValue());
                                System.out.println("    " + tp);
                            }
                        }
                    }
                }
            }
            return null;
        }

        List<String> opArgs = opt.args();
        StringBuilder sb = new StringBuilder(32);
        for (String arg : opArgs)
            sb.append(arg);
        String expr = sb.toString();

        try {
            int res = eval(expr);
            System.out.println(res);
            return res;

        } catch (Exception nfe) {
            throw opt.usageError("Can't parse expression: " + expr);
        }

    }

    static class PrecedenceComparator implements Comparator<BinaryOp> {
        @Override
        public int compare(BinaryOp lhs, BinaryOp rhs) {
            int cmp = lhs.getPrecedence() - rhs.getPrecedence();
            if (cmp != 0)
                return cmp;
            return System.identityHashCode(lhs) - System.identityHashCode(rhs);
        }
    }

    private int eval(String expr) {
        return doEval(expr.replaceAll("\\s+", ""));
    }

    private int doEval(String expr) {

        for (BinaryOp op : opToProperties.keySet()) {
            int splitPos = expr.lastIndexOf(op.getSymbol());
            if (splitPos <= 0)
                continue;
            String lhs = expr.substring(0, splitPos);
            String rhs = expr.substring(splitPos + 1, expr.length());
            return op.apply(eval(lhs), eval(rhs));
        }
        return Integer.parseInt(expr);
    }

}
