/* a modus that functions as a self balancing binary tree. can only safely
    pop from the leaves, popping an item will also pop all subtree items.
    this means popping the root will cause the entire tree to be popped.
    inserting an item will happen as a leaf and can trigger a tree rebalancing.
    the tree will use a struct and pointers to nodes.

the uniqueness of this modus is that it challenges the user to insert and pop
    items to rebalance the tree to where the items they want become more
    accessable (rebalance the tree to move the root to a further subtree)
