package getLongestSequence;

import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveTask;

public class GetLongestSequence {
    /**
     * Use the ForkJoin framework to write the following method in Java.
     *
     * Returns the length of the longest consecutive sequence of val in arr.
     * For example, if arr is [2, 17, 17, 8, 17, 17, 17, 0, 17, 1], then
     * getLongestSequence(17, arr) == 3 and getLongestSequence(35, arr) == 0.
     *
     * Your code must have O(n) work, O(lg n) span, where n is the length of arr, and actually use the sequentialCutoff
     * argument. We have provided you with an extra class SequenceRange. We recommend you use this class as
     * your return value, but this is not required.
     */
    private static final ForkJoinPool POOL = new ForkJoinPool();

    public static int getLongestSequence(int val, int[] arr, int sequentialCutoff) {
        return parallel(arr, val, sequentialCutoff);

    }

    public static int parallel(int[] arr, int val, int cutoff) {
        SequenceRange range = POOL.invoke(new getLongestClass(arr, 0, arr.length, cutoff, val));
        return range.longestRange;
    }


    private static class getLongestClass extends RecursiveTask<SequenceRange> {
        private final int[] arr;
        private final int lo, hi, cutoff;
        private final int val;



        private getLongestClass(int[] arr, int lo, int hi, int cutoff, int val) {
            this.arr = arr;
            this.lo = lo;
            this.hi = hi;
            this.cutoff = cutoff;
            this.val = val;
        }


        @Override
        protected SequenceRange compute() {
            if(hi - lo <= cutoff){
                int temp = 0;
                SequenceRange range = new SequenceRange(-1, -1, 0);
                for(int i = lo; i < hi; i++){
                    if(arr[i] == val){
                        if(range.longestRange == 0){
                            range.matchingOnLeft = i;
                        }
                        temp++;
                        range.matchingOnRight = i;
                        range.longestRange = Math.max(range.longestRange, temp);
                    } else{
                        temp = 0;
                    }
                }
                return range;
            } else{
                int mid = (lo + hi) / 2;

                getLongestClass left = new getLongestClass(arr, lo, mid, cutoff, val);
                getLongestClass right = new getLongestClass(arr, mid, hi, cutoff, val);

                left.fork();

                SequenceRange rightRag = right.compute();
                SequenceRange leftRag = left.join();

                return combine(leftRag, rightRag, val, arr);
            }
        }

        private SequenceRange combine(SequenceRange left, SequenceRange right, int val, int[] arr){
            int leftMatching = Integer.MAX_VALUE;
            int rightMatching = -1;
            int longestRange = 0;
            if(left.longestRange == 0 && right.longestRange == 0){
                return new SequenceRange(-1, -1, 0);
            }
            if(left.longestRange > 0){
                leftMatching = left.matchingOnLeft;
                rightMatching = left.matchingOnRight;
            }
            if(right.longestRange > 0){
                leftMatching = Math.min(right.matchingOnLeft, leftMatching);
                rightMatching = Math.max(right.matchingOnRight, rightMatching);
            }
            if(right.matchingOnLeft - left.matchingOnRight != 1){
                longestRange = Math.max(left.longestRange, right.longestRange);
                return new SequenceRange(leftMatching, rightMatching, longestRange);
            } else{
                int temp = 0;
                int start_l = left.matchingOnRight;
                int start_r = right.matchingOnLeft;
                while(start_l >= 0 && arr[start_l] == val ){
                    temp++;
                    start_l--;
                }
                while(start_r < arr.length && arr[start_r] == val){
                    temp++;
                    start_r++;
                }
                longestRange = Math.max(temp, Math.max(left.longestRange, right.longestRange));
            }
            return new SequenceRange(leftMatching, rightMatching, longestRange);
        }
    }

    /* TODO: Add a sequential method and parallel task here */

    private static void usage() {
        System.err.println("USAGE: GetLongestSequence <number> <array> <sequential cutoff>");
        System.exit(2);
    }

    public static void main(String[] args) {
        if (args.length != 3) {
            usage();
        }

        int val = 0;
        int[] arr = null;

        try {
            val = Integer.parseInt(args[0]);
            String[] stringArr = args[1].replaceAll("\\s*", "").split(",");
            arr = new int[stringArr.length];
            for (int i = 0; i < stringArr.length; i++) {
                arr[i] = Integer.parseInt(stringArr[i]);
            }
            System.out.println(getLongestSequence(val, arr, Integer.parseInt(args[2])));
        } catch (NumberFormatException e) {
            usage();
        }
    }
}