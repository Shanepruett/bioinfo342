Names: Shane Pruett, Stephanie Potter, Brandon Quirarte
Project: Lab 3 BTree
Class: CS 342




RUNNING PROGRAM:

-To run the GeneBankCreateBTree program open a terminal and navigate to the correct directory.

  -Compile the program by entering:
  javac GeneBankCreateBTree.java
  
  -Then run the program using
  java GeneBankCreateBTree <0/1(no/with Cache)> <degree> <gbk file> <sequence length> [<cache size>] [<debug level>]

  (cache size is only required when using a cache.  Debug level is optional)
  
  

-To run the GeneBankSearch program open a terminal and navigate to the correct directory.

  -Compile the program by entering:
  javac GeneBankSearch.java
  
  -Then run the program using
  java GeneBankSearch <0/1(no/with Cache)> <btree file> <query file> [<cache size>] [<debug level>]

  (cache size is only required when using a cache.  Debug level is optional)



BTREE FILE DESIGN:

Our BTree class has an inner class called BTreeNode which has another TreeObject innerclass.

The binary file is set up in a way such that the BTree metadata is written at the very beginning of the binary
file and is always the same number of bits, 128. 

The Sizes of what is writen to the binary file is as follows:

  BTree metadata size = 128 =  32 [int size of node] + 32 [int sequence length] + 32 [int degree of BTree] + 32[int number of nodes]

  nodeSize offset = 32 [int current objects] + 32 [int parent location] + 32 * (degree * 2) [int child locations] +
	      96 * (degree * 2 - 1) [size of TreeObjects in node]

  TreeObject size = 96 = 32 [int frequency of sequence] + 64 [long representation of sequence]
  
  
Every BTreeNode is offset in the binary file by the BTree's metadata plus the integer position of the node (with
a range of 0 to the number of nodes) times the node Size + the BTree metadata size (128). 




OBSERVATIONS:

We observed that using a cache makes huge improvements in the runtimes of create a BTree and Searching a BTree.

Using a cloud repository for our application files greatly improves our group performance.



CACHE SPEED IMPROVMENTS:

Create BTree: Test1.gbk, length 5, degree 16 (optimal)
  Time without Cache: 44.464 seconds
  Time with Cache (100): 8.406 seconds
  Time with Cache (500): 8.782 seconds

Create BTree: Test1.gbk, length 16, degree 16 (optimal)
  Time without Cache: 51.979 seconds
  Time with Cache (100): 11.565 seconds
  Time with Cache (500): 11.885 seconds

Create BTree: Test2.gbk, length 6, degree 16 (optimal)
  Time without Cache: 98.460 seconds
  Time with Cache (100): 19.559 seconds
  Time with Cache (500): 17.445 seconds

Create BTree: Test2.gbk, length 17, degree 16 (optimal)
  Time without Cache: 111.324 seconds
  Time with Cache (100): 22.331 seconds
  Time with Cache (500): 25.581 seconds

Create BTree: Test3.gbk, length 8, degree 16 (optimal)
  Time without Cache: 97.600 seconds
  Time with Cache (100): 30.434 seconds
  Time with Cache (500): 19.296 seconds

Create BTree: Test3.gbk, length 31, degree 16 (optimal)
  Time without Cache: 112.968 seconds
  Time with Cache (100): 21.663 seconds
  Time with Cache (500): 25.119 seconds