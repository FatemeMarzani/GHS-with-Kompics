# GHS-with-Kompics
The GHS algorithm is used for finding the minimum spanning tree in a weighted
graph. By considering all the assumptions for this algorithm, a graph will be
given to you in a file with the following format
![GitHub Logo](/images/pic1.png)
Format: ![Alt Text](url)
# Kompics
you can learn more about kompics in  /KopmicsGuidance
# GHS Algorithm Sketch
Initially, every singleton node is a fragment, which is the root of the
fragment.
Each node then looks for the least weight edge connecting to a neighbor.
If both nodes pick each other, then a fragment of two nodes is formed at
level 1.
 When two fragments join, the node with higher id across the least weight
outgoing edge serves as the new root. However, during absorb, the root
of the higher level fragment continues to serve as the new root.
 The notification about the change of root is sent out using the
changeroot message.
 Communication within a fragment takes place via the edges of the
spanning tree. Thus, each node keeps track of its parent and children
## Detecting Least Weight Outgoing Edge (lwoe)
Root broadcasts “initiate” in its own fragment, collects the report from
other nodes about eligible edges using a convergecast, and determines
the least weight outgoing edge.
 To test if an edge is outgoing, each node sends a test message through
a candidate edge. The receiving node may send
 a reject message (when it belongs to same fragment) or
 an accept message (when it belongs to a different fragment).
 ## Accept/Reject
Let process i send “test” to process j:
 1. Item 1 Case 1. If name(i) = name(j) then send “reject”
 1. Item 2 Case 2. If name(i) 6= name(j) ∧ level(i) ≤ level(j), then node j sends“accept”
 1. Item 3 Case 3. If name(i) 6= name(j) ∧ level(i) > level(j), then wait until level(j) =level(i) and then send “accept/reject”.
 ## The Major Steps
 repeat
1. Test edges as outgoing or not
2. Determine least weight outgoing edge - it becomes a tree edge
3. Send join (or respond to join)
4. Update level & name & identify new coordinator/root until there are no
outgoing edges
## Classification of Edges:
1. Basic: initially all branches are basic
2. Branch: all tree edges
3. Rejected: not a tree edge

Once the lwoe has been found, the root broadcasts a changeroot message to
its fragment. The node that found the lwoe receives a changeroot message, it
sends a join message along that edge indicating willingness to join
## Scenario 1: Merge
If i sends a join message to j, where level(i) = level(j) = L and receives a join
message from j, then:
* Edge (i, j) becomes a branch message.
* Between i and j whoever has the larger id becomes the new root.
* The name of the fragment will be w(i, j).
* Its level will be L + 1.
* The root will broadcast (initiate, L + 1, name) message to the entire
combine fragment.
## Scenario 2: Absorb
### Scenario 2.1:
If i in fragment T at level L sends a join message to j in
fragment T'
at level L'
, where
  
  L < L'
, and

T' has not yet found lwoe
, then T'
absorbs T:
* Process j sends a join message to i, indicating that T has been absorbed.
* The root of T' will be the root of the combined fragment.
* T changes its level to L' and adopts the name of T'.
* They collectively search for a lwoe.
* Edge (i, j) becomes a branch message.
### Scenario 2.2: 
If T'has already chosen a lwoe, then the lwoe cannot be(i, j). That means T'is joining another T''. Once this join is complete,
the combined fragment of T'and T'' starts a new search and will send an initiate message to T to signal that T has been absorbed.


