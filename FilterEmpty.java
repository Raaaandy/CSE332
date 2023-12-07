package filterEmpty;

import java.util.Arrays;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveAction;

public class FilterEmpty {
    static ForkJoinPool POOL = new ForkJoinPool();

    /**
     * Use the ForkJoin framework to write the following method in Java.
     *
     * Returns an array with the lengths of the non-empty strings from arr (in order)
     * For example, if arr is ["", "", "cse", "332", "", "hw", "", "7", "rox"], then
     * main.java.filterEmpty(arr) == [3, 3, 2, 1, 3].
     *
     * A parallel algorithm to solve this problem in O(lg n) span and O(n) work is the following:
     * (1) Do a parallel map to produce a bit set
     * (2) Do a parallel prefix over the bit set
     * (3) Do a parallel map to produce the output
     *
     * In lecture, we wrote parallelPrefix together, and it is included in the gitlab repository.
     * Rather than reimplementing that piece yourself, you should just use it. For the other two
     * parts though, you should write them.
     *
     * Do not bother with a sequential cutoff for this exercise, just have a base case that processes a single element.
     */
    public static int[] filterEmpty(String[] arr) {
        int[] bits = mapToBitSet(arr);

        int[] bitsum = ParallelPrefixSum.parallelPrefixSum(bits);

        return mapToOutput(arr, bits, bitsum);
    }

    public static int[] mapToBitSet(String[] arr) {
        int[] bits = new int[arr.length];
        POOL.invoke(new MapToBit(arr.length, 0, arr, bits));
        return bits;
    }

    public static class MapToBit extends RecursiveAction {
        private int hi, lo;
        private String[] arr;
        private int[] bits;
        private ForkJoinPool POOL = new ForkJoinPool();

        public MapToBit(int hi, int lo, String[] arr, int[] bits){
            this.hi = hi;
            this.lo = lo;
            this.arr = arr;
            this.bits = bits;
        }

        @Override
        protected void compute() {
            if(hi - lo <= 1){
                for(int i = lo; i < hi; i++){
                    if(!arr[lo].isEmpty()){
                        bits[lo] = 1;
                    }else{
                        bits[lo] = 0;
                    }
                }
            }else{
                int mid = lo + (hi - lo) / 2;

                MapToBit left = new MapToBit(mid, lo, arr, bits);
                MapToBit right = new MapToBit(hi, mid, arr, bits);
                left.fork();

                right.compute();
                left.join();


            }
        }


    }

    /* TODO: Add a sequential method and parallel task here */

    public static int[] mapToOutput(String[] input, int[] bits, int[] bitsum) {
        if(input.length == 0){
            return new int[0];
        }
        int[] output = new int[bitsum[input.length - 1]];
        POOL.invoke(new MapToOutput(input, bits, bitsum, output, 0, input.length));
        return output;

    }

    public static class MapToOutput extends RecursiveAction{
        private String[] input;
        private int[] bits, bitsum, output;
        private int lo, hi;
        private ForkJoinPool POOL = new ForkJoinPool();

        public MapToOutput(String[] input, int[] bits, int[] bitsum, int[] output, int lo, int hi){
            this.input = input;
            this.bits = bits;
            this.bitsum = bitsum;
            this.lo = lo;
            this.hi = hi;
            this.output = output;
        }


        @Override
        protected void compute() {
            if(hi - lo <= 1){
                for (int i = lo; i < hi; i++) {
                    if(bitsum[i] > 0 && bits[i] == 1){
                        output[bitsum[i] - 1] = input[i].length();
                    }
                }
            }else {
                int mid = (hi + lo) / 2;
                MapToOutput left = new MapToOutput(input, bits, bitsum, output, lo, mid);
                MapToOutput right = new MapToOutput(input, bits, bitsum, output, mid, hi);
                left.fork();
                right.compute();
                left.join();
            }
        }

        private int[] parallel(String[] input, int[] bits, int[] bitsum, int[] output, int lo, int hi){
            POOL.invoke(new MapToOutput(input, bits, bitsum, output, 0, input.length));
            return output;
        }
    }
    /* TODO: Add a sequential method and parallel task here */

    private static void usage() {
        System.err.println("USAGE: FilterEmpty <String array>");
        System.exit(1);
    }

    public static void main(String[] args) {
        if (args.length != 1) {
            usage();
        }

        String[] arr = args[0].replaceAll("\\s*", "").split(",");
        System.out.println(Arrays.toString(filterEmpty(arr)));
    }
}