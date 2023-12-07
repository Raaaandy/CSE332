package getLeftMostIndex;

import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveTask;

public class GetLeftMostIndex {
    /**
     * Use the ForkJoin framework to write the following method in Java.
     *
     * Returns the index of the left-most occurrence of needle in haystack (think of needle and haystack as
     * Strings) or -1 if there is no such occurrence.
     *
     * For example, main.java.getLeftMostIndex("cse332", "Dudecse4ocse332momcse332Rox") == 9 and
     * main.java.getLeftMostIndex("sucks", "Dudecse4ocse332momcse332Rox") == -1.
     *
     * Your code must actually use the sequentialCutoff argument. You may assume that needle.length is much
     * smaller than haystack.length. A solution that peeks across subproblem boundaries to decide partial matches
     * will be significantly cleaner and simpler than one that does not.
     */

    private static final ForkJoinPool POOL = new ForkJoinPool();

    public static int getLeftMostIndex(char[] needle, char[] haystack, int sequentialCutoff) {
        /* TODO: Edit this with your code */
        return POOL.invoke(new getLeftIndexClass(needle, haystack, sequentialCutoff, haystack.length, 0));
    }

    /* TODO: Add a sequential method and parallel task here */

    private static int sequential(char[] needle, char[] haystack, int lo, int hi){
        boolean found;
        for(int i = lo; i < hi; i++){
            if(haystack[i] == needle[0] && i <= haystack.length - needle.length){
                found = true;
                for(int n_idx = 0; n_idx < needle.length; n_idx++){
                    if(needle[n_idx] != haystack[n_idx + i]){
                      found = false;
                    }
                }
                if(found){
                    return i;
                }
            }

        }
        return -1;

    }

    private static class getLeftIndexClass extends RecursiveTask<Integer> {
        int cutoff, hi, lo;
        char[] needle;
        char[] haystack;

        public getLeftIndexClass(char[] needle, char[] haystack, int cutoff, int hi, int lo){
            this.cutoff = cutoff;
            this.needle = needle;
            this.haystack = haystack;
            this.hi = hi;
            this.lo = lo;
        }


        @Override
        protected Integer compute() {
            if(hi - lo <= cutoff){
                return sequential(needle, haystack, lo, hi);
            } else{
                int mid = (lo + hi) / 2;

                getLeftIndexClass left = new getLeftIndexClass(needle, haystack, cutoff, mid, lo);
                getLeftIndexClass right = new getLeftIndexClass(needle, haystack, cutoff, hi, mid);

                left.fork();

                int rightIdx = right.compute();
                int leftIdx = left.join();

                return leftIdx == -1 ? rightIdx : leftIdx;
//                if(rightIdx >= 0 && leftIdx >= 0){
//                    return Math.min(leftIdx, rightIdx);
//                } else if(rightIdx < 0 && leftIdx < 0){
//                    return -1;
//                } else {
//                    return Math.max(leftIdx, rightIdx);
//                }
            }
        }
    }

    private static void usage() {
        System.err.println("USAGE: GetLeftMostIndex <needle> <haystack> <sequential cutoff>");
        System.exit(2);
    }

    public static void main(String[] args) {
        if (args.length != 3) {
            usage();
        }

        char[] needle = args[0].toCharArray();
        char[] haystack = args[1].toCharArray();
        try {
            System.out.println(getLeftMostIndex(needle, haystack, Integer.parseInt(args[2])));
        } catch (NumberFormatException e) {
            usage();
        }
    }
}
