import java.nio.file.WatchEvent;
import java.time.temporal.ValueRange;

/*
 * A self-balancing 2-3-4 tree. Allows insertion, checking, and deletion. Primarily uses
 * ints for values.
 */
public class TwoFourTree {
    private class TwoFourTreeItem {
        int values = 1;
        int value1 = 0;                             // always exists.
        int value2 = 0;                             // exists iff the node is a 3-node or 4-node.
        int value3 = 0;                             // exists iff the node is a 4-node.
        boolean isLeaf = true;
        
        TwoFourTreeItem parent = null;              // parent exists iff the node is not root.
        TwoFourTreeItem leftChild = null;           // left and right child exist iff the note is a non-leaf.
        TwoFourTreeItem rightChild = null;          
        TwoFourTreeItem centerChild = null;         // center child exists iff the node is a non-leaf 3-node.
        TwoFourTreeItem centerLeftChild = null;     // center-left and center-right children exist iff the node is a non-leaf 4-node.
        TwoFourTreeItem centerRightChild = null;

        public boolean isTwoNode() {
            return values == 1;
        }

        public boolean isThreeNode() {
            return values == 2;
        }

        public boolean isFourNode() {
            return values == 3;
        }

        public boolean isRoot() {
            return parent == null;
        }

        public TwoFourTreeItem(int value1) {
            this.value1 = value1;
        }

        public TwoFourTreeItem(int value1, int value2) {
            this.value1 = value1;
            this.value2 = value2;
            this.values = 2;
        }

        public TwoFourTreeItem(int value1, int value2, int value3) {
            this.value1 = value1;
            this.value2 = value2;
            this.value3 = value3;
            this.values= 3;
        }

        private void printIndents(int indent) {
            for(int i = 0; i < indent; i++) System.out.printf("  ");
        }

        public void printInOrder(int indent) {
            if(!isLeaf) leftChild.printInOrder(indent + 1);
            printIndents(indent);
            System.out.printf("%d\n", value1);
            if(isThreeNode()) {
                if(!isLeaf) {centerChild.printInOrder(indent + 1);}
                printIndents(indent);
                System.out.printf("%d\n", value2);
            } else if(isFourNode()) {
                if(!isLeaf) centerLeftChild.printInOrder(indent + 1);
                printIndents(indent);
                System.out.printf("%d\n", value2);
                if(!isLeaf) centerRightChild.printInOrder(indent + 1);
                printIndents(indent);
                System.out.printf("%d\n", value3);
            }
            if(!isLeaf) rightChild.printInOrder(indent + 1);
        }
    }

    //This variable either enables or disables all print statements for debugging.
    private boolean debugEnabled = false;    

    TwoFourTreeItem root = null;


/* Takes an int and inserts it into a self-balancing 2-3-4 tree.
 * It returns true if the insert was a success. Will likely break tree if
 * value is already in it.   */
    public boolean addValue(int value) {

        if(debugEnabled) System.out.printf("Starting addValue with value: %d\n", value);

        //if tree is empty we simply create the first item
        if(root == null){ 
            root = new TwoFourTreeItem(value);
            if(debugEnabled) System.out.println("Tree is empty, adding root with value.");
            return true;
        }
    
        //otherwise, we find a location and descend to place the value
        TwoFourTreeItem walker = root;
        
        //split at 4-nodes and stop at leafs
        while(!walker.isLeaf || walker.isFourNode()){
            
            //for four nodes, we do a split
            if(walker.isFourNode()){

                if(debugEnabled) System.out.println("Reached a 4-node during addValue.");

                walker = splitFourNode(walker, value);
            }
            
            //for three nodes, we move to a left, middle, or right child
            else if(walker.isThreeNode()){

                if(debugEnabled) System.out.printf("Arrived at 3 node (%d %d %d).\n", walker.value1, walker.value2, walker.value3);

                if(value < walker.value1){
                    //move to the left child
                    walker = walker.leftChild;
                    if(debugEnabled) System.out.println("Going left.");
                }
                else if(value < walker.value2){
                    //move to center child
                    walker = walker.centerChild;
                    if(debugEnabled) System.out.println("Going down.");
                }
                else{
                    //move to right child
                    walker = walker.rightChild;
                    if(debugEnabled) System.out.println("Going right.");
                }
            }

            //for two nodes, we move to left or right child
            else{

                if(debugEnabled) System.out.printf("Arrived at 2 node (%d %d %d).\n", walker.value1, walker.value2, walker.value3);

                if(value < walker.value1){
                    //move to the left child
                    walker = walker.leftChild;
                    if(debugEnabled) System.out.println("Going left.");
                }
                else{
                    //move to right child
                    walker = walker.rightChild;
                    if(debugEnabled) System.out.println("Going right.");
                }
            }
        }

        //now, we've reached a non-full leaf node, meaning we can insert our value

        if(debugEnabled) System.out.printf("Placing (%d) at node (%d %d %d).\n", value, walker.value1, walker.value2, walker.value3);

        //if its a three node leaf, insert into left, right, or middle
        if(walker.isThreeNode()){
            if(value < walker.value1){
                //update the values with new value at lowest
                walker.value3 = walker.value2;
                walker.value2 = walker.value1;
                walker.value1 = value;
            }
            else if(value < walker.value2){
                //update the values with new value in middle
                walker.value3 = walker.value2;
                walker.value2 = value;
            }
            else{
                //update the values with new value at highest
                walker.value3 = value;
            }

            walker.values = 3;
        }

        //othwerise, its a two node leaf, so insert at left or right
        else{
            if(value < walker.value1){
                //update the value with new value at lowest
                walker.value2 = walker.value1;
                walker.value1 = value;
            }
            else{
                //update the values with new value at highest
                walker.value2 = value;
            }

            walker.values = 2;
        }

        if(debugEnabled) System.out.printf("Value successfully added: (%d %d %d)\n", walker.value1, walker.value2, walker.value3);

        return true;
    }

/* This method checks to see if the tree has a certain value. Takes an int.
 * It returns true if the value is in the tree and false otherwise.
 */
    public boolean hasValue(int value) {

        if(debugEnabled) System.out.printf("Starting search for value: %d.\n", value);
        
        TwoFourTreeItem walker = root;
        boolean locationFound = false;

        //search move down the tree and compare value at the nodes
        while(walker != null){

            //check data values
            if(walker.isTwoNode()){
                locationFound = (value == walker.value1);
            }
            else if(walker.isThreeNode()){
                locationFound = (value == walker.value1 || value == walker.value2);
            }
            else{
                locationFound = (value == walker.value1 || value == walker.value2 || value == walker.value3);
            }

            if(locationFound){
                if(debugEnabled) System.out.printf("Successfully found value (%d) at node (%d %d %d).\n", value, walker.value1, walker.value2, walker.value3);
                return true;
            }

            //move to child if not found
            if(walker.isTwoNode()){
                if(value < walker.value1){
                    walker = walker.leftChild;
                }
                else{
                    walker = walker.rightChild;
                }
            }
            else if(walker.isThreeNode()){
                if(value < walker.value1){
                    walker = walker.leftChild;
                }
                else if(value < walker.value2){
                    walker = walker.centerChild;
                }
                else{
                    walker = walker.rightChild;
                }
            }
            else{
                if(value < walker.value1){
                    walker = walker.leftChild;
                }
                else if(value < walker.value2){
                    walker = walker.centerLeftChild;
                }
                else if (value < walker.value3){
                    walker = walker.centerRightChild;
                }
                else{
                    walker = walker.rightChild;
                }
            }

        }

        //if we reach this point, we never found the item
        if(debugEnabled) System.out.println("Failed to find value.");
        return false;
    }


/* This method removes a value from the tree. It takes an int.
 * It returns true if the value was removed and false otherwise.
 * It will likely fail if the tree contains duplicates.
 */
    public boolean deleteValue(int value) {

        if(debugEnabled) System.out.printf("Starting delete for value: %d.\n", value);
        

        //After leaving root node, we set the siblings every time we descend
        TwoFourTreeItem walker = root;
        TwoFourTreeItem rightSibling = null;
        TwoFourTreeItem leftSibling = null;
        boolean locationFound = false;

        //search move down the tree and compare value at the nodes
        while(walker != null){

            if(debugEnabled) System.out.printf("At new node (%d, %d, %d) searching for %d.\n", walker.value1, walker.value2, walker.value3, value);


            //for base case, we delete 1 value and exit
            if(walker.isTwoNode() && walker.isRoot() && walker.isLeaf){
                if(value == walker.value1){
                    root = null;
                    if(debugEnabled) System.out.println("Tree only had one value, removing root.");
                    return true;
                }
            }
            //for a 2-node at root with kids, we continue
            else if(walker.isTwoNode() && walker.isRoot()){
                locationFound = (value == walker.value1);
            }
            //for all other 2-nodes we perform shifting operations and continue search at new node
            else if(walker.isTwoNode()){

                if(debugEnabled) System.out.println("Reached a 2-node. Starting operation.");

                //try to rotate first
                walker = rotate(leftSibling, walker, rightSibling);

                //if rotate wasnt the right choice, walker still needs updating
                if(walker.isTwoNode()){
                    walker = merge(leftSibling, walker, rightSibling);
                }

                //if walker is still a two node, we failed to rotate or merge
                if(walker.isTwoNode()){
                    if(debugEnabled) System.out.println("Failed to fix 2-node. Tree may have an error.");
                }

            }

            //walker must be a 3-node or 4-node by now so check location
            if(walker.isThreeNode()){
                locationFound = (value == walker.value1 || value == walker.value2);
            }
            else if(walker.isFourNode()){
                locationFound = (value == walker.value1 || value == walker.value2 || value == walker.value3);
            }

            //begin delete operation if we find location
            if(locationFound){
                if(debugEnabled) System.out.printf("Found value %d at node (%d %d %d) Starting delete operation.\n", value, walker.value1, walker.value2, walker.value3);

                //these will be the kids we use to find the replacement value for delete
                TwoFourTreeItem valueChildL = null;
                TwoFourTreeItem valueChildR = null;
                //these are the important children's siblings
                TwoFourTreeItem extraChildL = null;
                TwoFourTreeItem extraChildR = null;

                //first, find the value's kids
                valueChildL = findValueChildL(value, walker);
                valueChildR = findValueChildR(value, walker);
                extraChildL = findVCLSiblingL(value, walker);
                extraChildR = findVCRSiblingR(value, walker);

                //for debugging
                if(!walker.isLeaf){
                    if(debugEnabled) System.out.printf("Left value child: (%d %d %d)\n", valueChildL.value1, valueChildL.value2, valueChildL.value3);
                    if(debugEnabled) System.out.printf("Right value child: (%d %d %d)\n", valueChildR.value1, valueChildR.value2, valueChildR.value3);
                }
                
                //Try to do a simple delete at a leaf, or merge with 2-node children if they exist
                while(walker.isLeaf || (valueChildL.isTwoNode() && valueChildR.isTwoNode())){
                    
                    //if at a leaf, just delete the value
                    if(walker.isLeaf){

                        if(debugEnabled) System.out.println("Value is safe to delete at current node. Deleting Value.");

                        //delete for 4-nodes
                        if(walker.isFourNode()){
                            if(value == walker.value1){
                                walker.value1 = walker.value2;
                                walker.value2 = walker.value3;
                            }
                            if(value == walker.value2){
                                walker.value2 = walker.value3;
                            }
                            //for all locations we reset value3 last
                            walker.value3 = 0;
                        }
                        //this is for 3 nodes
                        else{
                            if(value == walker.value1){
                                walker.value1 = walker.value2;
                            }
                            //for all locations we reset value 2 last
                            walker.value2 = 0;
                        }

                        walker.values--;
                        if(debugEnabled) System.out.printf("Post-delete node: (%d, %d, %d).\n", walker.value1, walker.value2, walker.value3);
                        return true;
                    }
                    else{  //by now,  we know both subtree's are 2-node so we must steal or merge

                        //let the left value child steal from its extra sibling if possible
                        if(extraChildL != null && !extraChildL.isTwoNode()){
                            rotate(extraChildL, valueChildL, null);
                        }
                        //let the right value child steal from its extra sibling if possible
                        else if(extraChildR != null && !extraChildR.isTwoNode()){
                            rotate(null, valueChildR, extraChildR);
                        }
                        //otherwise we pull the walker down into its kids and update it
                        else{
                            //Send in values to merge. Assume we merge into the right sibling for simplicity.
                            walker = merge(valueChildL, valueChildR, null);
                            if(debugEnabled) System.out.printf("Succesfully merged delete value down to node: (%d, %d, %d).\n", walker.value1, walker.value2, walker.value3);    
                        }

                        //re-locate the nearest children for the updated walker
                        valueChildL = findValueChildL(value, walker);
                        valueChildR = findValueChildR(value, walker);
                    }
                }

                if(debugEnabled) System.out.printf("Right-leftmost or Left-rightmost value available!\n Using subtree to retrieve replacement value.\n");

                //start seeking a value closest to the value
                TwoFourTreeItem seeker;

                //these will help determine the rightmost or leftmost sub value
                boolean usingSubtreeR = false;
                boolean usingSubtreeL = false;
                int targetValue;

                if(!valueChildL.isTwoNode()){
                    usingSubtreeL = true;
                    seeker = valueChildL;

                    if(debugEnabled) System.out.println("Choosing left subtree.");
                }
                else{
                    usingSubtreeR = true;
                    seeker = valueChildR;

                    if(debugEnabled) System.out.println("Choosing right subtree.");
                }
                
                if(debugEnabled) System.out.printf("Subtree Selected: (%d, %d, %d)\n", seeker.value1, seeker.value2, seeker.value3);

                //navigate down and fix two nodes before we reach them
                while(!seeker.isLeaf){

                    if(debugEnabled) System.out.println("Seeker is not a leaf. Attempting to navigate down.");
                    
      
                    //we go different directions depending on which subtree we use
                    if(usingSubtreeL && seeker.isFourNode()){
                        targetValue = seeker.value3;
                    }
                    else if(usingSubtreeL && seeker.isThreeNode()){
                        targetValue = seeker.value2;
                    }
                    else{
                        targetValue = seeker.value1;
                    }

                    if(debugEnabled) System.out.printf("Chose target value (%d).\n", targetValue);
                    
                    //prepare for a 2-node target value node
                    valueChildL = findValueChildL(targetValue, seeker);
                    valueChildR = findValueChildR(targetValue, seeker);

                    //merge with target value if the node is a 2-node.
                    if(valueChildL.isTwoNode() && valueChildR.isTwoNode()){
                        seeker = merge(valueChildL, valueChildR, null);
                    }
                    else if(usingSubtreeL && valueChildR.isTwoNode() && !valueChildL.isTwoNode()){
                        seeker = rotate(valueChildL, valueChildR, null);
                    }
                    else if(usingSubtreeR && valueChildL.isTwoNode() && !valueChildR.isTwoNode()){
                        seeker = rotate(null, valueChildL, valueChildR);
                    }
                    
                    //at this point, we can confirm our value child is not a 2-node, its safe to move to
                    else if(usingSubtreeL){
                        seeker = valueChildR;
                    }
                    else{
                        seeker = valueChildL;
                    }

                }

                //now we've reached a bigger leaf containing the target value
                if(debugEnabled) System.out.printf("Finished seeking target value at node (%d, %d, %d).\n", seeker.value1, seeker.value2, seeker.value3);

                //finally, we can extract the largest/smallest value
                if(usingSubtreeL && seeker.isFourNode()){
                    targetValue = seeker.value3;
                    seeker.value3 = 0;
                }
                else if(usingSubtreeL && seeker.isThreeNode()){
                    targetValue = seeker.value2;
                    seeker.value2 = 0;
                }
                else{
                    targetValue = seeker.value1;
                    seeker.value1 = seeker.value2;
                    seeker.value2 = seeker.value3;
                    seeker.value3 = 0;
                }

                if(debugEnabled) System.out.printf("Retrieving value (%d).\n", targetValue);
                seeker.values--;

                //then replace the original value held by the walker

                //for 4-nodes
                if(walker.isFourNode()){
                    if(value == walker.value1){
                        walker.value1 = targetValue;
                    }
                    else if(value == walker.value2){
                        walker.value2 = targetValue;
                    }
                    else{
                        walker.value3 = targetValue;
                    }
                }
                //this is for 3 nodes
                else{
                    if(value == walker.value1){
                        walker.value1 = targetValue;
                    }
                    else{
                        walker.value2 = targetValue;
                    }
                }

                if(debugEnabled) System.out.printf("Successfully swapped and deleted values at node (%d, %d, %d).\n", walker.value1, walker.value2, walker.value3);
                return true;
            }


            //pick up here if the delete value isn't at the current node
            if(debugEnabled) System.out.printf("Value was not at node (%d, %d, %d). Descending futher.\n", walker.value1, walker.value2, walker.value3);

            //descend and track siblings for rotations if needed
            if(walker.isFourNode()){
                if(value < walker.value1){
                    rightSibling = walker.centerLeftChild;
                    leftSibling = null;
                    walker = walker.leftChild;
                }
                else if(value < walker.value2){
                    rightSibling = walker.centerRightChild;
                    leftSibling = walker.leftChild;
                    walker = walker.centerLeftChild;
                }
                else if (value < walker.value3){
                    rightSibling = walker.rightChild;
                    leftSibling = walker.centerLeftChild;
                    walker = walker.centerRightChild;
                }
                else{
                    rightSibling = null;
                    leftSibling = walker.centerRightChild;
                    walker = walker.rightChild;
                }
            }
            else if(walker.isThreeNode()){
                if(value < walker.value1){
                    rightSibling = walker.centerChild;
                    leftSibling = null;
                    walker = walker.leftChild;
                }
                else if(value < walker.value2){
                    rightSibling = walker.rightChild;
                    leftSibling = walker.leftChild;
                    walker = walker.centerChild;
                }
                else{
                    rightSibling = null;
                    leftSibling = walker.centerChild;
                    walker = walker.rightChild;
                }
            }
            else{   //this is only for the root since other 2-nodes are fixed
                if(value < walker.value1){
                    rightSibling = walker.rightChild;
                    leftSibling = null;
                    walker = walker.leftChild;
                }
                else{
                    rightSibling = null;
                    leftSibling = walker.leftChild;
                    walker = walker.rightChild;
                }
            }
        }

        //if we reach this point, we never found the item
        if(debugEnabled) System.out.println("Failed to locate and delete value!");
        return false;
    }

/*
 * This method prints the tree starting at the root.
 * It will do nothing if root is null.
 */
    public void printInOrder() {
        if(root != null) root.printInOrder(0);
    }


/*
 * This method splits a 4-node into a parent with two 2-node children.
 * It takes the large node and the value you wish to move toward after the operation.
 * It either makes a new parent or pushes the middle value of the node into an existing parent,
 * then it returns the child closest to the provided value. This won't work for non 4-nodes.
 */
    public TwoFourTreeItem splitFourNode(TwoFourTreeItem walker, int value){
        
        TwoFourTreeItem parent = walker.parent;

        //these are the closest children to our original value in our resulting merge node
        TwoFourTreeItem leftishChild = null;
        TwoFourTreeItem rightishChild = null;

        int centerValue = walker.value2;


        //for the root, we must create a parent as we split
        if(walker.isRoot()){
            

            if(debugEnabled) System.out.println("At root, making 3 new nodes.");
            //push the middle value up
            parent = new TwoFourTreeItem(centerValue);
            
            //split the old values to attach later on
            rightishChild = parent.rightChild = new TwoFourTreeItem(walker.value3);
            leftishChild = parent.leftChild = new TwoFourTreeItem(walker.value1);

            //let the parent know it has kids
            parent.isLeaf = false;

            //update the root with the new parent
            root = parent;
        }

        //otherwise the parent could be a three node
        //we should insert correctly and track the children
        else if(parent.isThreeNode()){

            if(debugEnabled) System.out.println("Splitting into two nodes, giving middle value to 3-node parent.");

            if(centerValue < parent.value1){
                //push the center value on the left of the parent's data
                parent.value3 = parent.value2;
                parent.value2 = parent.value1;
                parent.value1 = centerValue;
                parent.values = 3;

                //move the parents middle child to the right to make space for new kids
                parent.centerRightChild = parent.centerChild;
                parent.centerChild = null;

                //create a node for the original right-side value
                rightishChild = parent.centerLeftChild = new TwoFourTreeItem(walker.value3);

                //create a node for the original left-side value
                leftishChild = parent.leftChild = new TwoFourTreeItem(walker.value1);

            }
            else if(centerValue < parent.value2){
                //update the values with new value in middle
                parent.value3 = parent.value2;
                parent.value2 = centerValue;
                parent.values = 3;

                //create a node for the original right-side value
                rightishChild = parent.centerRightChild = new TwoFourTreeItem(walker.value3);

                //create a node for the original left-side value
                leftishChild = parent.centerLeftChild = new TwoFourTreeItem(walker.value1);
            }
            else{
                //update the values with new value at highest
                parent.value3 = centerValue;
                parent.values = 3;

                //move the parents middle child to the left to make space for new kids
                parent.centerLeftChild = parent.centerChild;
                parent.centerChild = null;

                //create a node for the original right-side value
                rightishChild = parent.rightChild = new TwoFourTreeItem(walker.value3);

                //create a node for the original left-side value
                leftishChild = parent.centerRightChild = new TwoFourTreeItem(walker.value1);
            }
        }

        //otherwise the parent is a two node
        else{

            if(debugEnabled) System.out.println("Splitting into two nodes, giving middle value to 2-node parent.");

            if(centerValue < parent.value1){
                //push the center value on the left of the parent's data
                parent.value2 = parent.value1;
                parent.value1 = centerValue;
                parent.values = 2;

                //create a node for the original right-side value
                rightishChild = parent.centerChild = new TwoFourTreeItem(walker.value3);

                //create a node for the original left-side value
                leftishChild = parent.leftChild = new TwoFourTreeItem(walker.value1);
            }
            else{
                //update the values with new value at highest
                parent.value2 = centerValue;
                parent.values = 2;

                //create a node for the original right-side value
                rightishChild = parent.rightChild = new TwoFourTreeItem(walker.value3);

                //create a node for the original left-side value
                leftishChild = parent.centerChild = new TwoFourTreeItem(walker.value1);
            }
        }

        if(debugEnabled) System.out.println("Split nearly complete. Updating parents and children.");

        //assign the children to the split nodes from the 3 node
        rightishChild.rightChild = walker.rightChild;
        rightishChild.leftChild = walker.centerRightChild;
        leftishChild.rightChild = walker.centerLeftChild;
        leftishChild.leftChild = walker.leftChild;

        //assign parents to new kids
        rightishChild.parent = parent;
        leftishChild.parent = parent;
        
        //if original 4-node had kids, we should assign their parents too
        if(!walker.isLeaf){
            rightishChild.rightChild.parent = rightishChild;
            rightishChild.leftChild.parent = rightishChild;
            leftishChild.rightChild.parent = leftishChild;
            leftishChild.leftChild.parent = leftishChild;
            
            //also need to tell the new nodes that they are parents
            rightishChild.isLeaf = false;
            leftishChild.isLeaf = false;
        }
    
        //last we decide where to place the walker to continue navigation
        if(value < centerValue){
            walker = leftishChild;
        }
        else{
            walker = rightishChild;
        }

        if(debugEnabled) System.out.println("Finished split operation, continuing add process.");
        return walker;

    }


/*
 * This method uses a node/walker, sibling, and parent create a new walker with a value stolen from
 * the parent. It also shifts a sibling's value up to the parent to maintain balance.
 * It needs the walker and a 3 or 4 node sibling to have any effect.
 * It returns the walker with one extra value from its parent and the child
 * of the sibling it used. This will break the tree if walker is not a 2-node, or if walker is parentless.
 */
    public TwoFourTreeItem rotate(TwoFourTreeItem leftSibling, TwoFourTreeItem walker, TwoFourTreeItem rightSibling){

        TwoFourTreeItem parent = walker.parent;

        //use left sibling if it's available
        if(leftSibling != null && !leftSibling.isTwoNode()){

            if(debugEnabled) System.out.println("Taking value from left sibling.");

            int upValue;
            //store the value to move upward to the parent
            if(leftSibling.isFourNode()){
                upValue = leftSibling.value3;
            }
            else{
                upValue = leftSibling.value2;
            }

            //make space for the parents value
            walker.value2 = walker.value1;

            //perform the swap from sibling to parent to walker
            if(parent.isFourNode()){
                if(upValue < parent.value1){
                    walker.value1 = parent.value1 ;
                    parent.value1 = upValue;
                }
                else if(upValue < parent.value2){
                    walker.value1 = parent.value2;
                    parent.value2 = upValue;
                }
                else{
                    walker.value1 = parent.value3;
                    parent.value3 = upValue;
                }
            }
            else if(parent.isThreeNode()){
                if(upValue < parent.value1){
                    walker.value1 = parent.value1 ;
                    parent.value1 = upValue;
                }
                else{
                    walker.value1 = parent.value2;
                    parent.value2 = upValue;
                }
            }
            else{  //we know parent is two node, must be the root
                walker.value1 = parent.value1 ;
                parent.value1 = upValue;
            }

            //update walker info
            walker.values = 2;

            //adopt child
            walker.centerChild = walker.leftChild;
            walker.leftChild = leftSibling.rightChild;
            //update child's parent
            if(!walker.isLeaf){
                walker.leftChild.parent = walker;
            }

            if(leftSibling.isFourNode()){
                //update left sibling
                leftSibling.value3 = 0;
                leftSibling.values = 2;

                //fix broken family
                leftSibling.rightChild = leftSibling.centerRightChild;
                leftSibling.centerChild = leftSibling.centerLeftChild;
                leftSibling.centerLeftChild = null;
                leftSibling.centerRightChild = null;
            }
            else{
                //update left sibling
                leftSibling.value2 = 0;
                leftSibling.values = 1;

                //fix broken family
                leftSibling.rightChild = leftSibling.centerChild;
                leftSibling.centerChild = null;
            }

            //walker is now a 3-node with the parents value pulled down
            return walker;
        }

        //otherwise try to rotate using the right sibling
        else if(rightSibling != null && !rightSibling.isTwoNode()){

            if(debugEnabled) System.out.println("Taking value from right sibling.");

            //store the value to move upward to the parent
            int upValue = rightSibling.value1;


            //perform the swap from sibling to parent to walker
            if(parent.isFourNode()){
                if(upValue > parent.value3){
                    walker.value2 = parent.value3 ;
                    parent.value3 = upValue;
                }
                else if(upValue > parent.value2){
                    walker.value2 = parent.value2;
                    parent.value2 = upValue;
                }
                else{
                    walker.value2 = parent.value1;
                    parent.value1 = upValue;
                }
            }
            else if(parent.isThreeNode()){
                if(upValue > parent.value2){
                    walker.value2 = parent.value2 ;
                    parent.value2 = upValue;
                }
                else{
                    walker.value2 = parent.value1;
                    parent.value1 = upValue;
                }
            }
            else{  //we know parent is two node, must be the root
                walker.value2 = parent.value1 ;
                parent.value1 = upValue;
            }

            //update walker info
            walker.values = 2;

            //adopt child
            walker.centerChild = walker.rightChild;
            walker.rightChild = rightSibling.leftChild;
            //update child's parent
            if(!walker.isLeaf){
                walker.rightChild.parent = walker;
            }

            if(rightSibling.isFourNode()){
                //update right sibling
                rightSibling.value1 = rightSibling.value2;
                rightSibling.value2 = rightSibling.value3;
                rightSibling.value3 = 0;
                rightSibling.values = 2;

                //fix broken family
                rightSibling.leftChild = rightSibling.centerLeftChild;
                rightSibling.centerChild = rightSibling.centerRightChild;
                rightSibling.centerLeftChild = null;
                rightSibling.centerRightChild = null;
            }
            else{
                //update right sibling
                rightSibling.value1 = rightSibling.value2;
                rightSibling.value2 = 0;
                rightSibling.values = 1;

                //fix broken family
                rightSibling.leftChild = rightSibling.centerChild;
                rightSibling.centerChild = null;
            }

            //walker is now a 3-node with the parents value pulled down
            return walker;
        }

        //return the original 2-node walker if we weren't meant to rotate
        if(debugEnabled) System.out.println("Attempted rotation but siblings arent suitable.");
        return walker;
            
    }

/*
 * This will steal one parent and one 2-node sibling and form a new node with the walker.
 * This takes a 2-node walker and a 2-node sibling. It returns the combined sibling+parent+walker
 * to keep the tree balanced for deletions. It will break if the walker is parentless
 * or if the sibling provided is not a 2 node.
 */
    public TwoFourTreeItem merge(TwoFourTreeItem leftSibling, TwoFourTreeItem walker, TwoFourTreeItem rightSibling){
        
        TwoFourTreeItem parent = walker.parent;

        //pull down parent and merge with left sibling
        if(leftSibling != null){

            if(debugEnabled) System.out.println("Merging with left sibling.");

            //these will store the sibling's value and the parent's grabbed value
            int newCenter;
            int newLeft = leftSibling.value1;
            int newRight = walker.value1;

            //determine which parent value to pull down, then update the parent and store the value
            if(parent.isFourNode()){
                if(newLeft < parent.value1){
                    newCenter = parent.value1;
                    parent.value1 = parent.value2;
                    parent.value2 = parent.value3;
                    parent.centerChild = parent.centerRightChild;
                }
                else if(newLeft < parent.value2){
                    newCenter = parent.value2;
                    parent.value2 = parent.value3;
                    parent.centerChild = parent.centerLeftChild;
                }
                else{
                    newCenter = parent.value3;
                    parent.rightChild = parent.centerRightChild;
                    parent.centerChild = parent.centerLeftChild;
                }

                //these updates are consistent regardless of location
                parent.value3 = 0;
                parent.values = 2;
                parent.centerLeftChild = null;
                parent.centerRightChild = null;
            
            }
            else if(parent.isThreeNode()){
                if(newLeft < parent.value1){
                    newCenter = parent.value1;
                    parent.value1 = parent.value2;
                }
                else{
                    newCenter = parent.value2;
                    parent.rightChild = parent.centerChild;
                }

                //these updates are consistent regardless of location
                parent.value2 = 0;
                parent.values = 1;                            
                parent.centerChild = null;

            }
            else{  //we know parent is two node, must be the root
                //no need to update the current root since it will dissapear
                newCenter = parent.value1;
                root = leftSibling;
                leftSibling.parent = null;
            }

            //make a new 3-node out of the left sibling

            //first update values
            leftSibling.value1 = newLeft;
            leftSibling.value2 = newCenter;
            leftSibling.value3 = newRight;
            leftSibling.values = 3;

            
            //update relationships
            //if sibling is a leaf, walker is too, so children don't exist
            if(!leftSibling.isLeaf){
                
                //update children
                leftSibling.centerLeftChild = leftSibling.rightChild;
                leftSibling.centerRightChild = walker.leftChild;
                leftSibling.rightChild = walker.rightChild;
                //update parents
                leftSibling.centerRightChild.parent = leftSibling;
                leftSibling.rightChild.parent = leftSibling;
            }

            //now we can reset the walker to continue at the merged node
            walker = leftSibling;
        }

        //othwerwise we use 2-node right sibling
        else if(rightSibling != null){

            if(debugEnabled) System.out.println("Merging with right sibling.");

            //these will store the sibling's value and the parent's grabbed value
            int newCenter;
            int newLeft = walker.value1;
            int newRight = rightSibling.value1;

            //determine which parent value to pull down
            //then update the parent and store the value
            if(parent.isFourNode()){
                if(newRight > parent.value3){
                    newCenter = parent.value3;
                    parent.centerChild = parent.centerLeftChild;
                }
                else if(newRight > parent.value2){
                    newCenter = parent.value2;
                    parent.value2 = parent.value3;
                    parent.centerChild = parent.centerRightChild;
                }
                else{
                    newCenter = parent.value1;
                    parent.value1 = parent.value2;
                    parent.value2 = parent.value3;
                    parent.leftChild = parent.centerLeftChild;
                    parent.centerChild = parent.centerRightChild;
                }

                //these updates are consistent regardless of location
                parent.value3 = 0;
                parent.values = 2;
                parent.centerLeftChild = null;
                parent.centerRightChild = null;
            
            }
            else if(parent.isThreeNode()){
                if(newRight > parent.value2){
                    newCenter = parent.value2;
                }
                else{
                    newCenter = parent.value1;
                    parent.value1 = parent.value2;
                    parent.leftChild = parent.centerChild;
                }

                //these updates are consistent regardless of location
                parent.value2 = 0;
                parent.values = 1;                            
                parent.centerChild = null;

            }
            else{  //we know parent is two node, must be the root
                //no need to update the current root since it will dissapear
                newCenter = parent.value1;
                root = rightSibling;
                rightSibling.parent = null;
            }

            //make a new 3-node out of the right sibling

            //first update values
            rightSibling.value3 = newRight;
            rightSibling.value2 = newCenter;
            rightSibling.value1 = newLeft;
            rightSibling.values = 3;
            
            //update relationships
            //if sibling is a leaf, walker is two, so children don't exist
            if(!rightSibling.isLeaf){
                
                //update children
                rightSibling.centerRightChild = rightSibling.leftChild;
                rightSibling.centerLeftChild = walker.rightChild;
                rightSibling.leftChild = walker.leftChild;
                //update parents
                rightSibling.centerLeftChild.parent = rightSibling;
                rightSibling.leftChild.parent = rightSibling;
            }

            //now we can reset the walker to continue at the merged node
            walker = rightSibling;
            }

        //we finished our merge or skipped it
        return walker;

    }

/*
 * This function finds the left subtree of a *value* within a given node. It takes a value and a node
 * and returns the left child of that node thats closest in value to the value. It will not work
 * if the value is not in the node.
 */
    public TwoFourTreeItem findValueChildL(int value, TwoFourTreeItem walker){
        if(walker.isFourNode()){
            if(value == walker.value1){
                return walker.leftChild;
            }
            else if(value == walker.value2){
                return walker.centerLeftChild;
            }
            else{
                return walker.centerRightChild;
            }
        }
        else if(walker.isThreeNode()){
            if(value == walker.value1){
                return walker.leftChild;
            }
            else{
                return walker.centerChild;
            }
        }
        //if the above cases weren't used, we must be at root
        return walker.leftChild;
    }

/*
 * This function finds the right subtree of a *value* within a given node. It takes a value and a node
 * and returns the right child of that node thats closest in value to the value. It will not work
 * if the value is not in the node.
 */
    public TwoFourTreeItem findValueChildR(int value, TwoFourTreeItem walker){
        if(walker.isFourNode()){
            if(value == walker.value1){
                return walker.centerLeftChild;
            }
            else if(value == walker.value2){
                return walker.centerRightChild;
            }
            else{
                return walker.rightChild;
            }
        }
        else if(walker.isThreeNode()){
            if(value == walker.value1){
                return walker.centerChild;
            }
            else{
                return walker.rightChild;
            }
        }
        //if the above cases weren't used, we must be at root
        return walker.rightChild;

    }

/*
 * This function finds grabs the left value child's left sibling. It takes a value and a node
 * and returns the second closest left child to the value. It will not work
 * if the value is not in the node.
 */
    public TwoFourTreeItem findVCLSiblingL(int value, TwoFourTreeItem walker){
        if(walker.isFourNode()){
            if(value == walker.value1){
                return null;
            }
            else if(value == walker.value2){
                return walker.leftChild;
            }
            else{
                return walker.centerLeftChild;
            }
        }
        else if(walker.isThreeNode()){
            if(value == walker.value1){
                return null;
            }
            else{
                return walker.leftChild;
            }
        }
        //if the above cases weren't used, we must be at root
        return null;
    }

/*
 * This function finds grabs the right value child's right sibling. It takes a value and a node
 * and returns the second closest right child to the value. It will not work
 * if the value is not in the node.
 */
    public TwoFourTreeItem findVCRSiblingR(int value, TwoFourTreeItem walker){
        if(walker.isFourNode()){
            if(value == walker.value1){
                return walker.centerRightChild;
            }
            else if(value == walker.value2){
                return walker.rightChild;
            }
            else{
                return null;
            }
        }
        else if(walker.isThreeNode()){
            if(value == walker.value1){
                return walker.rightChild;
            }
            else{
                return null;
            }
        }
        //if the above cases weren't used, we must be at root
        return null;

    }

/* This is the constructor. It is not used. */
    public TwoFourTree() {
        //Root is established/deleted in the add and delete functions
    }
}
