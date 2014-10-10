
package offset.oct6_group2;


import java.util.ArrayList;

/**
 * A book-keeping data structure used by the GomokuAgent in its alpha-beta search of
 * Gomoku states.
 *
 * For the most part made to be generic (other than its int[] action) so it can be
 * easily reused. Contains useful info like root node, search depth, parent node,
 * successors, etc.
 */
public class SearchNode<T> {

    private SearchNode<T> root;
    public int depth;
    private T data;
    private int[] action;
    private SearchNode<T> parent;
    private ArrayList<SearchNode<T>> successors;

    /**
     * Constructor for the root of a search tree takes only some data to hold, and sets
     * up necessary bookkeeping stuff like depth of 0, null parent, etc.
     */
    public SearchNode(T data) {
        this.root = this;
        this.parent = null;
        this.data = data;
        this.depth = 0;
        this.action = null;
        this.successors = new ArrayList<SearchNode<T>>();
    }

    /**
     * Constructor for some child node takes a root, a parent, data to old, and an action from
     * parent to itself that got it here.
     */
    public SearchNode(SearchNode<T> root, SearchNode<T> parent, T data, int[] action) {
        this.root = root;
        this.parent = parent;
        this.data = data;
        this.depth = parent.depth + 1;
        this.action = action;
        this.successors = new ArrayList<SearchNode<T>>();
    }

    /**
     * Accessor for the node's data
     */
    public T getData() {
        return this.data;
    }

    /**
     * Accessor for the action that got the node it its current state.
     */
    public int[] getAction() {
        return action;
    }

    /**
     * Accessor for depth of the node in a search tree.
     */
    public int getDepth() {
        return depth;
    }

    /**
     * Mutator for a node's data
     */
    public void setData(T data) {
        this.data = data;
    }

    /**
     * Accessor for the root node of this one's search tree.
     */
    public SearchNode<T> getRoot() {
        return this.root;
    }

    /**
     * Accesses a list of all this node's children.
     */
    public ArrayList<SearchNode<T>> getSuccessors() {
        return this.successors;
    }

    /**
     * Accessor for this node's number of children.
     */
    public int numSuccessors() {
        return this.successors.size();
    }

    /**
     * Adds and returns a child to this bad boy with the given data.
     */
    public SearchNode<T> addSuccessor(T data, int[] action) {
        SearchNode<T> child = new SearchNode<T>(this.root, this, data, action);
        getSuccessors().add(child);
        return child;
    }

}
