import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.TreeSet;
import java.util.Random;
import java.util.Collections;

public class App {
    /*
     * CONFIGURATION OPTIONS
     * Use these options to configure the test harness as you work with it.
     * 
     * Shuffle the created lists. If your code works without this but not with it,
     * something is wrong while "moving left" in a case.
     */
    static boolean ShouldShuffle = true;

    /*
     * Actually randomize the test case. We will use this in testing. When this is
     * false, the random seed will be 1.
     * 
     * With both ShouldShuffle and ShouldBeRandom false, the test cases are
     * deterministic on a given machine, which may aid you in finding specific bugs.
     */
    static boolean ShouldBeRandom = true;

    /*
     * Whether to test deletion. You can turn off this option if you know delete
     * isn't working yet. We'll also use it to determine partial credit.
     */
    static boolean RunDeleteCases = true;

    /*
     * Whether to run large cases, with and without deletion. You can use these
     * options for faster test runs, and we'll also use them to determine partial
     * credit.
     */
    static boolean RunLargeCases = true;
    static boolean RunLargeDeleteCases = true;

    /*
     * Whether to complain when a find returns false. Leaving this on when running
     * delete cases is very noisy.
     */
    static boolean NoisyFinds = false;

    /*
     * Whether to print the tree after the static tests. Useful in early debugging,
     * but just noisy later.
     */
    static boolean PrintStaticTree = false;

    /*
     * Print the tree before and after every delete in dynamic cases. This option
     * is *VERY* noisy, but running it for the small dynamic cases may help you find
     * deletion problems.
     */
    static boolean PrintDeleteTrees = false;

    /*
     * END OF CONFIGURATION OPTIONS
     */

    // Only gets used if !ShouldBeRandom.
    static Random RandomGenerator = new Random(1);

    private static ArrayList<Integer> deDuplicateAndScramble(ArrayList<Integer> list) {
        TreeSet<Integer> deDuped = new TreeSet<Integer>(list);
        ArrayList<Integer> outList = new ArrayList<Integer>(deDuped);
        if (ShouldShuffle) {
            Collections.shuffle(outList);
        }

        return outList;
    }

    private static ArrayList<Integer> generateIntArrayList(int howMany) {
        ArrayList<Integer> list = new ArrayList<Integer>(howMany);

        for (int i = 0; i < howMany; i++) {
            list.add(Integer.valueOf(RandomGenerator.nextInt(1000000000)));
        }
        list = deDuplicateAndScramble(list);

        return list;
    }

    private static ArrayList<Integer> generateStrikeList(List<Integer> fromList, int howMany) {
        ArrayList<Integer> strikeList = new ArrayList<Integer>(howMany);
        int fromLast = fromList.size() - 1;

        for (int i = 0; i < howMany; i++) {
            strikeList.add(fromList.get(RandomGenerator.nextInt(fromLast)));
        }
        strikeList = deDuplicateAndScramble(strikeList);

        return strikeList;
    }

    private static ArrayList<Integer> generateRemoveList(List<Integer> fromList) {
        ArrayList<Integer> removeList = new ArrayList<Integer>(fromList.size() / 2);

        for (int i = 0; i < fromList.size() / 2; i++) {
            removeList.add(fromList.get(i));
        }
        removeList = deDuplicateAndScramble(removeList);

        return removeList;
    }

    private static <T> int executeFinds(TwoFourTree coll, List<Integer> strikes) {
        boolean sentinel;
        int failures = 0;

        for (Integer e : strikes) {
            sentinel = coll.hasValue(e);
            if (sentinel == false) {
                if (NoisyFinds) {
                    System.out.printf("\nFailed to find %d", e);
                }
                failures++;
            }
        }

        if (failures > 0) {
            System.out.printf("(%,9d missing) ", failures);
        }

        return 0;
    }

    private static <T> int executeComparisonFinds(TreeSet<Integer> coll, List<Integer> strikes) {
        boolean sentinel;
        int failures = 0;

        for (Integer e : strikes) {
            sentinel = coll.contains(e);
            if (sentinel == false) {
                if (NoisyFinds) {
                    System.out.printf("\nFailed to find %d", e);
                }
                failures++;
            }
        }

        if (failures > 0) {
            System.out.printf("(%,9d missing) ", failures);
        }

        return 0;
    }

    public static void executeIntCase(int listSize, int strikeSize, boolean includeRemoves) {
        System.out.printf("CASE: %,8d integers, %,8d finds, %,8d removals.  Generating...\n", listSize, strikeSize,
                strikeSize / 2);

        ArrayList<Integer> intlist = generateIntArrayList(listSize);
        ArrayList<Integer> strikes = generateStrikeList(intlist, strikeSize);
        ArrayList<Integer> removeList = generateRemoveList(strikes);

        long start;
        long end;
        long ms;

        TwoFourTree theTree = new TwoFourTree();

        System.out.printf("  TwoFourTree ");

        start = System.currentTimeMillis();
        for (Integer e : intlist) {
            theTree.addValue(e);
        }
        end = System.currentTimeMillis();
        ms = end - start;
        System.out.printf("add: %,7dms  ", ms);

        start = System.currentTimeMillis();
        executeFinds(theTree, strikes);
        end = System.currentTimeMillis();
        ms = end - start;
        System.out.printf("find: %,7dms  ", ms);

        if (includeRemoves) {
            start = System.currentTimeMillis();
            for (Integer e : removeList) {
                if (PrintDeleteTrees) {
                    System.out.printf("----- delete %d from tree\n", e);
                    theTree.printInOrder();
                }
                theTree.deleteValue(e);
                if (theTree.hasValue(e)) {
                    System.out.printf("Failed to delete %d\n", e);
                } else {
                    // System.out.printf("Successfully deleted %d.\n", e);
                }
                if (PrintDeleteTrees) {
                    System.out.printf("----- After deleting %d from tree\n", e);
                    theTree.printInOrder();
                }
            }
            end = System.currentTimeMillis();
            ms = end - start;
            System.out.printf("del: %,7dms  ", ms);

            start = System.currentTimeMillis();
            executeFinds(theTree, strikes);
            end = System.currentTimeMillis();
            ms = end - start;
            System.out.printf("find: %,6dms  ", ms);
            System.out.printf("(Should be %,9d missing)  ", removeList.size());
        }

        System.out.printf("\n");
        // theTree.printInOrder();

        TreeSet<Integer> theComparison = new TreeSet<Integer>();

        System.out.printf("  TreeSet     ");

        start = System.currentTimeMillis();
        for (Integer e : intlist) {
            theComparison.add(e);
        }
        end = System.currentTimeMillis();
        ms = end - start;
        System.out.printf("add: %,7dms  ", ms);

        start = System.currentTimeMillis();
        executeComparisonFinds(theComparison, strikes);
        end = System.currentTimeMillis();
        ms = end - start;
        System.out.printf("find: %,7dms  ", ms);

        if (includeRemoves) {
            start = System.currentTimeMillis();
            for (Integer e : removeList) {
                // System.out.printf("----- delete %d from tree\n", e);
                /// theTree.printInOrder();
                theComparison.remove(e);
            }
            end = System.currentTimeMillis();
            ms = end - start;
            System.out.printf("del: %,7dms  ", ms);

            start = System.currentTimeMillis();
            executeComparisonFinds(theComparison, strikes);
            end = System.currentTimeMillis();
            ms = end - start;
            System.out.printf("find: %,6dms  ", ms);
            System.out.printf("(Should be %,9d missing)  ", removeList.size());
        }

        System.out.printf("\n");
        System.gc();
    }

    public static void executeStaticCase(List<Integer> values) {
        TwoFourTree tft = new TwoFourTree();

        if (ShouldShuffle)
            Collections.shuffle(values);

        for (int i : values) {
            tft.addValue(i);
        }

        for (int i : values) {
            if (!tft.hasValue(i)) {
                System.out.printf("Failed to add %d in static test\n", i);
            }
        }

        if (PrintStaticTree) {
            System.out.println("***** Static test:");
            tft.printInOrder();
        }

        if (RunDeleteCases) {
            List<Integer> deletes = generateStrikeList(values, values.size() / 5);

            for (int i : deletes) {
                tft.deleteValue(i);
                if (tft.hasValue(i)) {
                    System.out.printf("Failed to delete %d in static test\n", i);
                }
            }
            if (PrintStaticTree) {
                System.out.printf("***** After deleting nodes: ");
                System.out.println(deletes.toString());
                tft.printInOrder();
            }
        }

    }

    public static void main(String[] args) throws Exception {
        if (ShouldBeRandom)
            RandomGenerator = new Random();
        List<Integer> primeList = Arrays.asList(
                new Integer[] { 2, 3, 5, 7, 11, 13, 17, 19, 23, 29, 31, 37, 41, 43,
                        47, 53, 59, 67, 71, 73, 79, 83, 89, 97 });

        executeStaticCase(primeList);

        executeIntCase(100, 20, RunDeleteCases);
        executeIntCase(1000, 200, RunDeleteCases);
        executeIntCase(10000, 2000, RunDeleteCases);
        executeIntCase(100000, 20000, RunDeleteCases);
        if (RunLargeCases) {
            executeIntCase(1000000, 200000, RunDeleteCases && RunLargeDeleteCases);
            executeIntCase(10000000, 2000000, RunDeleteCases && RunLargeDeleteCases);
        }
    }
}
